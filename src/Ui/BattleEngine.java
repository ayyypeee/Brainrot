package Ui;

import entities.*;
import entities.BattleStats.ActiveEffect;
import entities.BattleStats.FloatEvent;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.List;

public class BattleEngine {

    public final String name1, name2;
    public final String label1, label2;

    public final BattleStats stats1;
    public final BattleStats stats2;

    public int  round    = 1;
    public int  turnSide = 1;
    public boolean animationPlaying  = false;
    public boolean waitingForDialogue = false;

    private final ArrayList<String> dialogueQueue = new ArrayList<>();
    private final ArrayList<String> passiveQueue  = new ArrayList<>();
    private String  currentLine   = "";
    private int     charsRevealed = 0;
    private int     charTick      = 0;
    private static final int CHARS_PER_TICK = 2;
    private boolean lineFinished  = false;
    private boolean dialogueActive = false;

    public String tooltipText = null;
    public int    tooltipX, tooltipY;

    public BufferedImage head1, head2;

    private int     pendingDamage    = 0;
    private int     pendingAttacker  = 0;
    private boolean selfHit          = false;
    private boolean hasPendingDamage = false;

    // ── Rich floating numbers ─────────────────────────────────────────────────
    private final ArrayList<FloatNumber> floatNumbers = new ArrayList<>();

    public BattleEngine(String name1, String label1, String name2, String label2) {
        this.name1  = name1;
        this.label1 = label1;
        this.name2  = name2;
        this.label2 = label2;
        this.stats1 = new BattleStats(label1);
        this.stats2 = new BattleStats(label2);
    }

    // ── Turn management ───────────────────────────────────────────────────────

    /**
     * Called at the START of a side's turn.
     *
     * IMPORTANT: we do NOT tick effects here any more.
     * Effects are ticked inside the battle panels AFTER the stun-skip check,
     * so a stun/confuse applied during the enemy's turn always survives at
     * least one full round before it wears off.
     */
    public void beginTurn(int side) {
        if (side == 1) {
            stats1.regenMana();
            stats2.regenMana();
            enqueueDialogue("Round " + round + " begins! Both fighters regenerate 5 mana.");
            if (!dialogueActive && !dialogueQueue.isEmpty()) startNextLine();
        }
    }

    /**
     * Tick DOT / buff effects for the given side and drain their messages.
     * Call this AFTER the stun-skip check so effects are always consumed in
     * the round they were meant for.
     */
    public void tickEffectsForSide(int side) {
        BattleStats active = side == 1 ? stats1 : stats2;
        BattleStats other  = side == 1 ? stats2 : stats1;

        boolean activeOnRight = (side == 2);

        active.tickEffects();
        drainFloatEvents(active, activeOnRight);

        drainStatMessages(active);
        drainStatMessages(other);
    }

    public void endTurn(int side) {
        if (side == 2) round++;
        turnSide       = (side == 1) ? 2 : 1;
        animationPlaying = false;
    }

    // ── Skill resolution ──────────────────────────────────────────────────────

    public int resolveSkill(int attackerSide, int skillNum) {
        BattleStats attacker     = attackerSide == 1 ? stats1 : stats2;
        BattleStats defender     = attackerSide == 1 ? stats2 : stats1;
        String      attackerName = attackerSide == 1 ? name1  : name2;
        String      attackerLabel= attackerSide == 1 ? label1 : label2;

        // attackerOnRight is true when the attacker is Player 2 / AI (right side)
        boolean attackerOnRight = (attackerSide == 2);

        CharSkill skill     = CharSkillDB.get(attackerName, skillNum);
        String    skillName = skill != null ? skill.name : "Skill " + skillNum;

        int  dmg    = 0;
        selfHit = false;

        if (attackerName.equals("AIP") && skillNum == 3) {
            dmg = attacker.computeBagSmash(defender);
        } else {
            boolean trueDmg = attackerName.equals("Kimmay")
                    && skillNum == 1
                    && new java.util.Random().nextInt(100) < 20;
            // Pass attackerOnRight so every passive queues floats on the correct side
            dmg = attacker.computeSkill(skillNum, attackerName, defender, trueDmg, attackerOnRight);
        }

        if (dmg < 0) {
            selfHit      = true;
            pendingDamage = -dmg;
        } else {
            pendingDamage = dmg;
        }

        // Announcement dialogue
        String announcement = attackerLabel + " used " + skillName + "!";
        if (selfHit) {
            announcement += " They are confused and hit themselves for " + pendingDamage + " damage!";
        } else if (pendingDamage > 0) {
            announcement += " This attack will deal " + pendingDamage + " damage!";
        } else if (attackerName.equals("AIP") && skillNum == 3) {
            announcement += " Let's see if this huge gamble pays off!";
        } else {
            announcement += " But it missed!";
        }
        enqueueDialogue(announcement);

        collectPassiveMessages(attacker);
        collectPassiveMessages(defender);

        pendingAttacker  = attackerSide;
        hasPendingDamage = true;
        animationPlaying = true;
        waitingForDialogue = true;
        return dmg;
    }

    /**
     * Called when the attack animation finishes.
     * Applies HP changes and drains any queued float events from both BattleStats.
     */
    public int applyPendingDamage() {
        if (!hasPendingDamage) return 0;
        hasPendingDamage = false;

        BattleStats attacker = pendingAttacker == 1 ? stats1 : stats2;
        BattleStats defender = pendingAttacker == 1 ? stats2 : stats1;
        int dmg = pendingDamage;

        boolean attackerOnRight = (pendingAttacker == 2);
        boolean defenderOnRight = (pendingAttacker == 1);

        if (selfHit) {
            if (dmg > 0) {
                attacker.addHp(-dmg);
                spawnFloat(dmg, FloatNumber.Kind.DAMAGE, attackerOnRight);
            }
            selfHit = false;
        } else if (dmg > 0) {
            defender.addHp(-dmg);
            spawnFloat(dmg, FloatNumber.Kind.DAMAGE, defenderOnRight);
        }

        // Drain any extra float events queued by passives (mana, bonus dmg, dodge, etc.)
        drainFloatEvents(attacker, attackerOnRight);
        drainFloatEvents(defender, defenderOnRight);

        return dmg;
    }

    /** Pull FloatEvents from a BattleStats and turn them into live FloatNumbers. */
    private void drainFloatEvents(BattleStats src, boolean onRight) {
        FloatEvent fe;
        while ((fe = src.pollFloatEvent()) != null) {
            // FloatEvent.onRight is already set correctly by BattleStats, use it directly
            spawnFloat(fe.value, toKind(fe.kind), fe.onRight);
        }
    }

    private FloatNumber.Kind toKind(FloatEvent.Kind k) {
        switch (k) {
            case BONUS_DAMAGE: return FloatNumber.Kind.BONUS_DAMAGE;
            case MANA_DRAIN:   return FloatNumber.Kind.MANA_DRAIN;
            case MANA_GAIN:    return FloatNumber.Kind.MANA_GAIN;
            case DODGE:        return FloatNumber.Kind.DODGE;
            default:           return FloatNumber.Kind.DAMAGE;
        }
    }

    private void spawnFloat(int value, FloatNumber.Kind kind, boolean onRight) {
        floatNumbers.add(new FloatNumber(value, kind, onRight));
    }

    public boolean hasPendingDamage() { return hasPendingDamage; }
    public int     getPendingDamage() { return pendingDamage; }

    // ── Passive message queue ─────────────────────────────────────────────────

    public void drainPassiveQueue() {
        for (String msg : passiveQueue) enqueueDialogue(msg);
        passiveQueue.clear();
    }

    public boolean hasPassiveMessages() { return !passiveQueue.isEmpty(); }

    private void collectPassiveMessages(BattleStats src) {
        while (src.hasMessages()) passiveQueue.add(src.pollMessage());
    }

    private void drainStatMessages(BattleStats src) {
        while (src.hasMessages()) enqueueDialogue(src.pollMessage());
    }

    // ── Skill gating ──────────────────────────────────────────────────────────

    public boolean canUseSkill(int side, int skillNum) {
        BattleStats stats    = side == 1 ? stats1 : stats2;
        String      charName = side == 1 ? name1  : name2;

        if (skillNum == 1) return true;

        if (stats.hasEffect(StatusEffect.STUN) || stats.hasEffect(StatusEffect.HEAVY_STUN))
            return false;

        if (stats.hasEffect(StatusEffect.SILENCE) && (skillNum == 2 || skillNum == 3))
            return false;

        CharSkill skill = CharSkillDB.get(charName, skillNum);
        if (skill == null) return false;

        int cost = skill.manaCost;
        if (stats.hasEffect(StatusEffect.EXHAUSTION)) cost += 10;

        return stats.canAfford(cost);
    }

    // ── Stun helpers ──────────────────────────────────────────────────────────

    public boolean isSideStunned(int side) {
        BattleStats s = side == 1 ? stats1 : stats2;
        return s.hasEffect(StatusEffect.STUN) || s.hasEffect(StatusEffect.HEAVY_STUN);
    }

    public boolean isSideSilenced(int side) {
        BattleStats s = side == 1 ? stats1 : stats2;
        return s.hasEffect(StatusEffect.SILENCE);
    }

    public void consumeStun(int side) {
        BattleStats s  = side == 1 ? stats1 : stats2;
        ActiveEffect ae = findEffect(s, StatusEffect.STUN);
        if (ae != null) { ae.turnsLeft--; if (ae.turnsLeft <= 0) s.removeEffect(StatusEffect.STUN); }
        ae = findEffect(s, StatusEffect.HEAVY_STUN);
        if (ae != null) { ae.turnsLeft--; if (ae.turnsLeft <= 0) s.removeEffect(StatusEffect.HEAVY_STUN); }
    }

    private ActiveEffect findEffect(BattleStats stats, StatusEffect type) {
        for (ActiveEffect ae : stats.getEffects()) if (ae.type == type) return ae;
        return null;
    }

    // ── Dialogue system ───────────────────────────────────────────────────────

    public void enqueueDialogue(String line) {
        dialogueQueue.add(line);
        if (!dialogueActive) startNextLine();
    }

    public boolean isDialogueActive() { return dialogueActive; }

    public void tickDialogue() {
        if (!dialogueActive) {
            if (!dialogueQueue.isEmpty()) startNextLine();
            return;
        }
        if (lineFinished) return;
        charTick++;
        if (charTick >= CHARS_PER_TICK) {
            charTick = 0;
            charsRevealed = Math.min(charsRevealed + 1, currentLine.length());
            if (charsRevealed >= currentLine.length()) lineFinished = true;
        }
    }

    public void advanceDialogue() {
        if (!dialogueActive) return;
        if (!lineFinished) {
            charsRevealed = currentLine.length();
            lineFinished  = true;
        } else {
            if (!dialogueQueue.isEmpty()) startNextLine();
            else { dialogueActive = false; waitingForDialogue = false; }
        }
    }

    private void startNextLine() {
        currentLine   = dialogueQueue.remove(0);
        charsRevealed = 0;
        charTick      = 0;
        lineFinished  = false;
        dialogueActive = true;
    }

    public String  getVisibleText()   { return dialogueActive && currentLine != null ? currentLine.substring(0, charsRevealed) : ""; }
    public boolean isLineFinished()   { return lineFinished; }
    public boolean hasQueuedLines()   { return !dialogueQueue.isEmpty(); }

    // ── Character full-name map ───────────────────────────────────────────────

    private static String getFullCharacterName(String n) {
        switch (n) {
            case "Tung Tung":  return "Tung Tung Sahur";
            case "Ballerina":  return "Ballerina Cappuccina";
            case "Tralalelo":  return "Tralalelo Tralala";
            case "Cappucino":  return "Cappuccino Assassino";
            case "AIP":        return "Ai-p Villanueva";
            case "Kimmay":     return "Kim Cullen";
            case "Dianne":     return "Dianne Cube";
            case "Cyberg":     return "Cyberg Jay Monterroyo";
            case "Christian":  return "Christian Tejano";
            default:           return n;
        }
    }

    // ── HUD drawing ───────────────────────────────────────────────────────────

    public void drawHUD(Graphics2D g2, int sw, int sh, Component obs,
                        BufferedImage vsImage, boolean side1Turn, int round,
                        boolean showVs) {

        int headSz   = (int)(sh * 0.075);
        int topY     = (int)(sh * 0.015);
        int headGap  = (int)(sw * 0.010);
        int barMaxW  = (int)(sw * 0.295);
        int barH     = (int)(sh * 0.025);
        int manaH    = (int)(sh * 0.018);
        int fontSize = Math.max(11, (int)(sh * 0.022));

        int nameY  = topY + (int)(headSz * 0.55);
        int hpBarY = nameY + (int)(sh * 0.008);
        int mnBarY = hpBarY + barH + (int)(sh * 0.006);
        int iconsY = mnBarY + manaH + (int)(sh * 0.006);

        // Left side (player 1)
        int hx1 = (int)(sw * 0.01);
        drawHead(g2, head1, hx1, topY, headSz, new Color(100, 200, 255), obs);
        int bx1 = hx1 + headSz + headGap;
        g2.setFont(new Font(Font.MONOSPACED, Font.BOLD, fontSize));
        g2.setColor(side1Turn ? new Color(255, 200, 80) : Color.WHITE);
        g2.drawString(label1, bx1, nameY);
        drawHPBar(g2, bx1, hpBarY, barMaxW, barH, stats1, true);
        drawManaBar(g2, bx1, mnBarY, barMaxW, manaH, stats1);
        drawStatusIcons(g2, bx1, iconsY, stats1, sh);

        // Right side (player 2)
        int hx2 = sw - (int)(sw * 0.01) - headSz;
        drawHead(g2, head2, hx2, topY, headSz, new Color(255, 150, 100), obs);
        int bxR = hx2 - headGap;
        g2.setFont(new Font(Font.MONOSPACED, Font.BOLD, fontSize));
        g2.setColor(!side1Turn ? new Color(255, 200, 80) : Color.WHITE);
        g2.drawString(label2, bxR - g2.getFontMetrics().stringWidth(label2), nameY);
        int bx2 = bxR - barMaxW;
        drawHPBar(g2, bx2, hpBarY, barMaxW, barH, stats2, false);
        drawManaBar(g2, bx2, mnBarY, barMaxW, manaH, stats2);
        drawStatusIcons(g2, bx2, iconsY, stats2, sh);

        // Centre
        int cx   = sw / 2;
        int rfs  = Math.max(14, (int)(sh * 0.028));
        g2.setFont(new Font(Font.MONOSPACED, Font.BOLD, rfs));
        g2.setColor(Color.WHITE);
        String roundStr = "ROUND " + round;
        g2.drawString(roundStr, cx - g2.getFontMetrics().stringWidth(roundStr) / 2, topY + rfs);

        if (showVs && vsImage != null) {
            int vsSize = (int)(sh * 0.060);
            g2.drawImage(vsImage, cx - vsSize / 2, topY + rfs + 2, vsSize, vsSize, obs);
        }

        tickAndDrawFloatNumbers(g2, sw, sh);

        // Head tooltips
        if (tooltipText != null) {
            if (tooltipX >= hx1 && tooltipX <= hx1 + headSz && tooltipY >= topY && tooltipY <= topY + headSz)
                drawCharTooltip(g2, name1, stats1, tooltipX, tooltipY, sw, sh);
            if (tooltipX >= hx2 && tooltipX <= hx2 + headSz && tooltipY >= topY && tooltipY <= topY + headSz)
                drawCharTooltip(g2, name2, stats2, tooltipX, tooltipY, sw, sh);
        }
    }

    public void drawHUD(Graphics2D g2, int sw, int sh, Component obs,
                        BufferedImage vsImage, boolean side1Turn, int round) {
        drawHUD(g2, sw, sh, obs, vsImage, side1Turn, round, true);
    }

    // ── Rich floating numbers ─────────────────────────────────────────────────

    private static class FloatNumber {
        enum Kind { DAMAGE, BONUS_DAMAGE, MANA_DRAIN, MANA_GAIN, DODGE }

        int   value;
        Kind  kind;
        float x, y;
        int   alpha    = 255;
        boolean onRight;

        FloatNumber(int value, Kind kind, boolean onRight) {
            this.value   = value;
            this.kind    = kind;
            this.onRight = onRight;
        }
    }

    private void tickAndDrawFloatNumbers(Graphics2D g2, int sw, int sh) {
        ArrayList<FloatNumber> toRemove = new ArrayList<>();

        for (FloatNumber fn : floatNumbers) {
            if (fn.y == 0) {
                fn.x = fn.onRight ? sw * 0.72f : sw * 0.22f;
                fn.y = sh * 0.38f - (floatNumbers.indexOf(fn) * sh * 0.05f);
            }

            fn.y    -= 1.5f;
            fn.alpha = Math.max(0, fn.alpha - 5);
            if (fn.alpha <= 0) { toRemove.add(fn); continue; }

            Color  col;
            String label;

            switch (fn.kind) {
                case BONUS_DAMAGE:
                    col   = new Color(255, 140, 0, fn.alpha);
                    label = "+" + fn.value;
                    break;
                case MANA_DRAIN:
                    col   = new Color(80, 120, 255, fn.alpha);
                    label = "-" + fn.value + " MP";
                    break;
                case MANA_GAIN:
                    col   = new Color(80, 200, 255, fn.alpha);
                    label = "+" + fn.value + " MP";
                    break;
                case DODGE:
                    col   = new Color(180, 255, 180, fn.alpha);
                    label = "DODGED!";
                    break;
                default:
                    col   = new Color(255, 60, 60, fn.alpha);
                    label = "-" + fn.value;
                    break;
            }

            int fs = fn.kind == FloatNumber.Kind.DODGE
                    ? Math.max(18, (int)(sh * 0.040))
                    : Math.max(22, (int)(sh * 0.055));

            g2.setFont(new Font(Font.MONOSPACED, Font.BOLD, fs));

            g2.setColor(new Color(0, 0, 0, fn.alpha / 2));
            g2.drawString(label, (int)fn.x + 2, (int)fn.y + 2);

            g2.setColor(col);
            g2.drawString(label, (int)fn.x, (int)fn.y);
        }

        floatNumbers.removeAll(toRemove);
    }

    // ── Head drawing ──────────────────────────────────────────────────────────

    private void drawHead(Graphics2D g2, BufferedImage img, int x, int y,
                          int size, Color fallback, Component obs) {
        if (img != null) g2.drawImage(img, x, y, size, size, obs);
        else {
            g2.setColor(fallback);
            g2.fillRect(x, y, size, size);
        }
    }

    // ── Char tooltip ──────────────────────────────────────────────────────────

    private void drawCharTooltip(Graphics2D g2, String charName, BattleStats stats,
                                 int mx, int my, int sw, int sh) {
        CharSkill[] skills   = CharSkillDB.getAll(charName);
        String      fullName = getFullCharacterName(charName);

        StringBuilder sb = new StringBuilder(fullName);
        for (int i = 0; i < skills.length; i++) {
            CharSkill sk      = skills[i];
            String    manaInfo = i == 0 ? "[+" + sk.manaRegen + " MP]" : "[-" + sk.manaCost + " MP]";
            String    dmgRange = "[" + sk.minDmg + "-" + sk.maxDmg + " Dmg]";
            sb.append("\nSkill ").append(i + 1).append(" ").append(manaInfo)
                    .append(" ").append(sk.name).append(" ").append(dmgRange)
                    .append(" : ").append(sk.passiveDesc);
        }
        drawTooltip(g2, sb.toString(), mx, my, sw, sh);
    }

    // ── HP / Mana bars ────────────────────────────────────────────────────────

    private void drawHPBar(Graphics2D g2, int x, int y, int maxW, int h,
                           BattleStats stats, boolean ltr) {
        g2.setColor(new Color(60, 0, 0));
        g2.fillRect(x, y, maxW, h);

        int fillW = (int)(maxW * (double) stats.hp / BattleStats.MAX_HP);
        if (fillW > 0) {
            float ratio = (float) stats.hp / BattleStats.MAX_HP;
            g2.setColor(ratio > 0.6f ? new Color(60, 210, 60)
                    : ratio > 0.3f   ? new Color(230, 210, 40)
                    : new Color(230, 50, 50));
            g2.fillRect(ltr ? x : x + maxW - fillW, y, fillW, h);
        }

        g2.setColor(Color.WHITE);
        g2.setStroke(new BasicStroke(2f));
        g2.drawRect(x, y, maxW, h);
        g2.setStroke(new BasicStroke(1f));

        int    currentHp = Math.max(0, stats.hp);
        String hpStr     = currentHp + "/" + BattleStats.MAX_HP;
        int    pfs       = Math.max(9, h - 2);
        g2.setFont(new Font(Font.MONOSPACED, Font.BOLD, pfs));
        g2.setColor(Color.WHITE);
        FontMetrics fm = g2.getFontMetrics();
        g2.drawString(hpStr, x + maxW / 2 - fm.stringWidth(hpStr) / 2, y + h - 2);
    }

    private void drawManaBar(Graphics2D g2, int x, int y, int maxW, int h,
                             BattleStats stats) {
        g2.setColor(new Color(10, 10, 60));
        g2.fillRect(x, y, maxW, h);
        int fillW = (int)(maxW * (double) stats.mana / BattleStats.MAX_MANA);
        if (fillW > 0) {
            g2.setColor(new Color(60, 120, 240));
            g2.fillRect(x, y, fillW, h);
        }
        g2.setColor(new Color(100, 160, 255));
        g2.setStroke(new BasicStroke(1.5f));
        g2.drawRect(x, y, maxW, h);
        g2.setStroke(new BasicStroke(1f));

        String mStr = stats.mana + " MP";
        int    mfs  = Math.max(8, h - 2);
        g2.setFont(new Font(Font.MONOSPACED, Font.PLAIN, mfs));
        g2.setColor(Color.WHITE);
        FontMetrics fm = g2.getFontMetrics();
        g2.drawString(mStr, x + maxW / 2 - fm.stringWidth(mStr) / 2, y + h - 2);
    }

    // ── Status icons ──────────────────────────────────────────────────────────

    public void drawStatusIcons(Graphics2D g2, int x, int y,
                                BattleStats stats, int sh) {
        List<ActiveEffect> effects = stats.getEffects();
        if (effects.isEmpty()) return;
        int iconSz = Math.max(18, (int)(sh * 0.030));
        int gap    = 3;
        int cx     = x;
        for (ActiveEffect ae : effects) {
            StatusEffect se = ae.type;
            g2.setColor(new Color(se.colour.getRed(), se.colour.getGreen(),
                    se.colour.getBlue(), 180));
            g2.fillRoundRect(cx, y, iconSz + 22, iconSz, 8, 8);
            g2.setColor(se.colour.darker());
            g2.setStroke(new BasicStroke(1.5f));
            g2.drawRoundRect(cx, y, iconSz + 22, iconSz, 8, 8);
            g2.setStroke(new BasicStroke(1f));
            int icoFs = Math.max(10, iconSz - 4);
            g2.setFont(new Font("Segoe UI Emoji", Font.PLAIN, icoFs));
            g2.setColor(Color.WHITE);
            g2.drawString(se.icon, cx + 2, y + iconSz - 3);
            int numFs = Math.max(8, iconSz - 8);
            g2.setFont(new Font(Font.MONOSPACED, Font.BOLD, numFs));
            g2.setColor(Color.WHITE);
            g2.drawString(String.valueOf(ae.turnsLeft), cx + iconSz + 4, y + iconSz - 3);
            cx += iconSz + 22 + gap;
        }
    }

    // ── Skill meta / tooltip ──────────────────────────────────────────────────

    public void drawSkillMeta(Graphics2D g2, int side,
                              Rectangle b1, Rectangle b2, Rectangle b3, int sh) {
        drawOneSkillMeta(g2, side, 1, b1, sh);
        drawOneSkillMeta(g2, side, 2, b2, sh);
        drawOneSkillMeta(g2, side, 3, b3, sh);
    }

    private void drawOneSkillMeta(Graphics2D g2, int side, int skillNum,
                                  Rectangle b, int sh) { /* intentionally empty */ }

    public void drawSkillTooltip(Graphics2D g2, int side, int skillNum,
                                 int mouseX, int mouseY, int sw, int sh) {
        String    charName = side == 1 ? name1 : name2;
        CharSkill skill    = CharSkillDB.get(charName, skillNum);
        if (skill == null) return;

        String fullName = getFullCharacterName(charName);
        String manaLine = skillNum == 1
                ? "Mana: +" + skill.manaRegen + " (Regen)"
                : "Mana: -" + skill.manaCost  + " (Cost)";
        String dmgLine     = "Damage: " + skill.minDmg + " - " + skill.maxDmg;
        String passiveLine = "Passive: " + skill.passiveDesc;

        drawTooltip(g2,
                fullName + "\n" + skill.name + "\n" + dmgLine + "\n" + manaLine + "\n" + passiveLine,
                mouseX, mouseY, sw, sh);
    }

    // ── Dialogue box ──────────────────────────────────────────────────────────

    public void drawDialogueBox(Graphics2D g2, int sw, int sh) {
        if (!dialogueActive) return;
        int boxH = (int)(sh * 0.13);
        int boxW = (int)(sw * 0.70);
        int boxX = (sw - boxW) / 2;
        int boxY = sh - boxH - (int)(sh * 0.22);
        int pad  = (int)(sh * 0.015);

        g2.setColor(new Color(10, 10, 30, 220));
        g2.fillRoundRect(boxX, boxY, boxW, boxH, 16, 16);
        g2.setColor(new Color(100, 160, 255));
        g2.setStroke(new BasicStroke(2f));
        g2.drawRoundRect(boxX, boxY, boxW, boxH, 16, 16);
        g2.setStroke(new BasicStroke(1f));

        int     fs      = Math.max(13, (int)(sh * 0.025));
        g2.setFont(new Font(Font.MONOSPACED, Font.PLAIN, fs));
        g2.setColor(Color.WHITE);

        String      visible  = getVisibleText();
        FontMetrics fm       = g2.getFontMetrics();
        int         maxLineW = boxW - pad * 2;
        String[]    words    = visible.split(" ");
        StringBuilder line   = new StringBuilder();
        int lineY = boxY + pad + fs;
        for (String word : words) {
            String test = line.length() == 0 ? word : line + " " + word;
            if (fm.stringWidth(test) > maxLineW) {
                g2.drawString(line.toString(), boxX + pad, lineY);
                lineY += fs + 4;
                line   = new StringBuilder(word);
            } else {
                if (line.length() > 0) line.append(" ");
                line.append(word);
            }
        }
        if (line.length() > 0) g2.drawString(line.toString(), boxX + pad, lineY);

        if (lineFinished) {
            String prompt = hasQueuedLines() ? "▶ ENTER" : "▶ CLOSE";
            int    pfs    = Math.max(10, (int)(sh * 0.018));
            g2.setFont(new Font(Font.MONOSPACED, Font.BOLD, pfs));
            g2.setColor(new Color(255, 220, 80,
                    (int)(180 + 75 * Math.sin(System.currentTimeMillis() / 300.0))));
            g2.drawString(prompt,
                    boxX + boxW - g2.getFontMetrics().stringWidth(prompt) - pad,
                    boxY + boxH - pad / 2);
        }
    }

    // ── Generic tooltip ───────────────────────────────────────────────────────

    public void drawTooltip(Graphics2D g2, String text, int mx, int my, int sw, int sh) {
        if (text == null || text.isEmpty()) return;
        String[]    lines = text.split("\n");
        int         fs    = Math.max(11, (int)(sh * 0.020));
        g2.setFont(new Font(Font.MONOSPACED, Font.PLAIN, fs));
        FontMetrics fm    = g2.getFontMetrics();
        int maxW = 0;
        for (String l : lines) maxW = Math.max(maxW, fm.stringWidth(l));
        int pad  = 10;
        int boxW = maxW + pad * 2;
        int boxH = lines.length * (fs + 4) + pad;
        int bx   = mx + 16;
        int by   = my - boxH / 2;
        if (bx + boxW > sw - 4) bx = mx - boxW - 4;
        if (by < 4)              by = 4;
        if (by + boxH > sh - 4)  by = sh - boxH - 4;
        g2.setColor(new Color(8, 8, 28, 245));
        g2.fillRoundRect(bx, by, boxW, boxH, 10, 10);
        g2.setColor(new Color(180, 200, 255));
        g2.setStroke(new BasicStroke(1.5f));
        g2.drawRoundRect(bx, by, boxW, boxH, 10, 10);
        g2.setStroke(new BasicStroke(1f));
        int ly = by + pad + fs - 2;
        for (int i = 0; i < lines.length; i++) {
            g2.setColor(i == 0 ? new Color(255, 220, 80)
                    : i == 1  ? new Color(180, 230, 255)
                    : Color.WHITE);
            g2.drawString(lines[i], bx + pad, ly);
            ly += fs + 4;
        }
    }
}
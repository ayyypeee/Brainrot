package entities;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class BattleStats {

    public static final int MAX_HP   = 100;
    public static final int MAX_MANA = 100;
    public static final int MANA_REGEN_PER_ROUND = 5;

    public int hp;
    public int mana;

    private final ArrayList<ActiveEffect>  effects         = new ArrayList<>();
    private final ArrayList<String>        pendingMessages = new ArrayList<>();
    private final ArrayList<FloatEvent>    floatEvents     = new ArrayList<>();
    private final String name;

    public BattleStats(String name) {
        this.name = name;
        hp   = MAX_HP;
        mana = 0;
    }

    // ── Stat mutators ─────────────────────────────────────────────────────────

    public void addHp(int amount) {
        hp = Math.min(MAX_HP, Math.max(0, hp + amount));
    }

    public void addMana(int amount) {
        mana = Math.min(MAX_MANA, Math.max(0, mana + amount));
    }

    public boolean canAfford(int cost) { return mana >= cost; }

    public void regenMana() { addMana(MANA_REGEN_PER_ROUND); }

    // ── Effects ───────────────────────────────────────────────────────────────

    public void applyEffect(StatusEffect type, int turns) {
        for (ActiveEffect ae : effects) {
            if (ae.type == type) {
                ae.turnsLeft = Math.max(ae.turnsLeft, turns);
                return;
            }
        }
        effects.add(new ActiveEffect(type, turns));
    }

    public boolean hasEffect(StatusEffect type) {
        for (ActiveEffect ae : effects) if (ae.type == type) return true;
        return false;
    }

    public int effectTurns(StatusEffect type) {
        for (ActiveEffect ae : effects) if (ae.type == type) return ae.turnsLeft;
        return 0;
    }

    public void removeEffect(StatusEffect type) {
        effects.removeIf(ae -> ae.type == type);
    }

    public List<ActiveEffect> getEffects() { return new ArrayList<>(effects); }

    // ── Tick effects at start of this character's turn ────────────────────────
    // Returns total poison damage dealt (used by engine for float numbers).
    public int tickEffects() {
        int poisonDamage = 0;

        if (hasEffect(StatusEffect.POISON)) {
            poisonDamage += 5;
            addHp(-5);
            decrementEffect(StatusEffect.POISON);
            addMessage(name + " suffered 5 damage from poison.");
            queueFloat(5, FloatEvent.Kind.DAMAGE, false);
        }

        if (hasEffect(StatusEffect.SEVERE_POISON)) {
            poisonDamage += 12;
            addHp(-12);
            decrementEffect(StatusEffect.SEVERE_POISON);
            addMessage(name + " suffered 12 damage from severe poison.");
            queueFloat(12, FloatEvent.Kind.DAMAGE, false);
        }

        // Tick every other non-poison effect
        List<ActiveEffect> snap = new ArrayList<>(effects);
        for (ActiveEffect ae : snap) {
            if (ae.type == StatusEffect.POISON || ae.type == StatusEffect.SEVERE_POISON) continue;
            ae.turnsLeft--;
            if (ae.turnsLeft <= 0) {
                effects.remove(ae);
                addMessage("The " + ae.type.displayName + " effect wore off for " + name + ".");
            }
        }

        return poisonDamage;
    }

    private void decrementEffect(StatusEffect type) {
        for (ActiveEffect ae : effects) {
            if (ae.type == type) {
                ae.turnsLeft--;
                if (ae.turnsLeft <= 0) effects.remove(ae);
                return;
            }
        }
    }

    // ── Messages ──────────────────────────────────────────────────────────────

    public void addMessage(String msg)  { pendingMessages.add(msg); }
    public boolean hasMessages()        { return !pendingMessages.isEmpty(); }
    public String pollMessage()         { return pendingMessages.isEmpty() ? null : pendingMessages.remove(0); }
    public void clearMessages()         { pendingMessages.clear(); }

    // ── Float events ──────────────────────────────────────────────────────────

    /**
     * Queue a floating-number event to be consumed by the engine.
     *
     * @param value    number to display (always positive)
     * @param kind     DAMAGE, MANA_DRAIN, MANA_GAIN, DODGE
     * @param onRight  true = render on the right side of the screen (enemy side)
     */
    public void queueFloat(int value, FloatEvent.Kind kind, boolean onRight) {
        floatEvents.add(new FloatEvent(value, kind, onRight));
    }

    public boolean hasFloatEvents()     { return !floatEvents.isEmpty(); }
    public FloatEvent pollFloatEvent()  { return floatEvents.isEmpty() ? null : floatEvents.remove(0); }

    // ── Skill resolution ──────────────────────────────────────────────────────

    /**
     * Computes raw damage (positive = hit enemy, negative = self-hit from confuse).
     * Side-effects: mutates mana for both sides, queues messages and float events.
     *
     * @param attackerOnRight  true when this BattleStats belongs to the right-side character (Player 2 / AI)
     */
    public int computeSkill(int skillNum, String charName,
                            BattleStats target, boolean isTrueDmg,
                            boolean attackerOnRight) {
        Random rng = new Random();
        CharSkill skill = CharSkillDB.get(charName, skillNum);

        if (skill == null) {
            int dmg = 5 + rng.nextInt(6);
            addMessage(charName + " attacks for " + dmg + " damage!");
            return dmg;
        }

        // Mana
        if (skillNum == 1) addMana(skill.manaRegen);
        else {
            int cost = skill.manaCost;
            // EXHAUSTION: extra 10 mana cost, then remove effect
            if (hasEffect(StatusEffect.EXHAUSTION)) {
                cost += 10;
                removeEffect(StatusEffect.EXHAUSTION);
                addMessage(charName + " is exhausted — the skill cost 10 extra mana!");
            }
            addMana(-cost);
        }

        // Base damage roll
        int dmg = skill.minDmg + rng.nextInt(skill.maxDmg - skill.minDmg + 1);

        // ── Outgoing debuffs on attacker ──────────────────────────────────────
        if (hasEffect(StatusEffect.WEAKNESS)) {
            dmg = (int)(dmg * 0.80);
            addMessage(charName + " is weakened and deals 20% less damage!");
        }
        if (hasEffect(StatusEffect.MIND_MAZE)) {
            dmg = dmg / 2;
            addMessage(charName + " is lost in a mind maze — damage halved!");
        }

        // ── CONFUSE: 50% chance to self-hit ──────────────────────────────────
        if (hasEffect(StatusEffect.CONFUSE) && rng.nextInt(100) < 50) {
            addMessage(charName + " is confused and struck themselves!");
            removeEffect(StatusEffect.CONFUSE);
            // Return negative so caller knows it's a self-hit
            return -dmg;
        }

        // ── Kimmay skill 3: 30% chance to shatter all defenses ───────────────
        boolean celestialIgnore = false;
        if (charName.equals("Kimmay") && skillNum == 3 && rng.nextInt(100) < 30) {
            celestialIgnore = true;
            addMessage("Celestial Break shattered all defenses!");
        }
        boolean effectiveTrueDmg = isTrueDmg || celestialIgnore;

        // ── DODGE ─────────────────────────────────────────────────────────────
        if (!effectiveTrueDmg && target.hasEffect(StatusEffect.DODGE)) {
            target.removeEffect(StatusEffect.DODGE);
            addMessage(target.name + " dodged the attack!");
            // Queue a "DODGE" float on the defender's side (opposite of attacker)
            target.queueFloat(0, FloatEvent.Kind.DODGE, !attackerOnRight);
            return 0;
        }

        // ── BLOCK ─────────────────────────────────────────────────────────────
        if (!effectiveTrueDmg && target.hasEffect(StatusEffect.BLOCK)) {
            dmg = dmg / 2;
            target.removeEffect(StatusEffect.BLOCK);
            addMessage(target.name + " blocked! Damage reduced to " + dmg + ".");
        }

        // ── FRACTURE ──────────────────────────────────────────────────────────
        if (target.hasEffect(StatusEffect.FRACTURE)) {
            int bonus = (int)(dmg * 0.15);
            dmg += bonus;
            target.removeEffect(StatusEffect.FRACTURE);
            addMessage(target.name + " is fractured — takes " + bonus + " extra damage!");
        }

        addMessage(skill.name + " hits " + target.name + " for " + dmg + " damage!");

        // ── Passives ──────────────────────────────────────────────────────────
        computePassive(skillNum, charName, skill, target, rng, dmg, effectiveTrueDmg, attackerOnRight);

        return dmg;
    }

    /**
     * Backward-compatible overload — assumes attacker is on the left (Player 1).
     * Kept so existing call-sites that don't pass attackerOnRight still compile.
     */
    public int computeSkill(int skillNum, String charName,
                            BattleStats target, boolean isTrueDmg) {
        return computeSkill(skillNum, charName, target, isTrueDmg, false);
    }

    // ── Bag Smash (AIP skill 3) ───────────────────────────────────────────────

    public int computeBagSmash(BattleStats target) {
        Random rng = new Random();
        addMana(-50);
        if (rng.nextInt(100) < 50) {
            int dmg = 100;
            addMessage("Bag Smash connects for a massive " + dmg + " damage!");
            return dmg;
        } else {
            int lost = mana;
            mana = 0;
            addMessage("Bag Smash missed! AIP lost all " + lost + " mana!");
            return 0;
        }
    }

    // ── Per-character passive effects ─────────────────────────────────────────

    private void computePassive(int skillNum, String charName, CharSkill skill,
                                BattleStats target, Random rng,
                                int baseDmg, boolean trueDmg,
                                boolean attackerOnRight) {

        boolean skipRoll = trueDmg && charName.equals("Kimmay") && skillNum == 3;
        // Remember to change this back to `skipRoll ? 0 : rng.nextInt(100);` later!
        int roll = 0;

        // attackerOnRight == true  → attacker is on the RIGHT side of the screen
        // defenderOnRight == true  → defender is on the RIGHT side of the screen
        boolean defenderOnRight = !attackerOnRight;

        switch (charName) {

            case "Tung Tung":
                if (skillNum == 1 && roll < 20) {
                    // Steal up to 10 mana from target
                    int steal = Math.min(10, target.mana);
                    target.addMana(-steal);
                    addMana(steal);
                    addMessage("Tung Tung stole " + steal + " mana from " + target.name + "!");
                    if (steal > 0) {
                        // Attacker gains mana → float on attacker's side
                        queueFloat(steal, FloatEvent.Kind.MANA_GAIN,  attackerOnRight);
                        // Defender loses mana → float on defender's side
                        target.queueFloat(steal, FloatEvent.Kind.MANA_DRAIN, defenderOnRight);
                    }
                } else if (skillNum == 2 && roll < 25) {
                    int burned = Math.min(15, target.mana);
                    target.addMana(-burned);
                    addMessage("Tung Tung burned " + burned + " of " + target.name + "'s mana!");
                    if (burned > 0)
                        target.queueFloat(burned, FloatEvent.Kind.MANA_DRAIN, defenderOnRight);
                } else if (skillNum == 3 && roll < 30) {
                    target.applyEffect(StatusEffect.SILENCE, 1);
                    addMessage(target.name + " is silenced and cannot use skills 2 or 3 next turn!");
                }
                break;

            case "Ballerina":
                if (skillNum == 1 && roll < 20) {
                    addHp(8);
                    addMessage("Ballerina recovered 8 HP!");
                } else if (skillNum == 2 && roll < 25) {
                    applyEffect(StatusEffect.DODGE, 1);
                    addMessage("Ballerina is ready to dodge the next attack!");
                } else if (skillNum == 3 && roll < 30) {
                    addHp(25);
                    addMessage("Ballerina healed 25 HP!");
                }
                break;

            case "Cappucino":
                if (skillNum == 1 && roll < 15) {
                    int bonusDmg = Math.max(1, baseDmg / 2);
                    target.addHp(-bonusDmg);
                    addMessage("Cappucino double-struck for " + bonusDmg + " extra damage!");
                    target.queueFloat(bonusDmg, FloatEvent.Kind.DAMAGE, defenderOnRight);
                } else if (skillNum == 2 && roll < 20) {
                    addMana(15);
                    addMessage("Latte Blaze gave Cappucino 15 mana back!");
                    queueFloat(15, FloatEvent.Kind.MANA_GAIN, attackerOnRight);
                } else if (skillNum == 3 && roll < 20) {
                    CharSkill espresso = CharSkillDB.get("Cappucino", 1);
                    int freeDmg = espresso != null
                            ? espresso.minDmg + rng.nextInt(espresso.maxDmg - espresso.minDmg + 1)
                            : 8 + rng.nextInt(6);
                    target.addHp(-freeDmg);
                    addMana(15);
                    addMessage("Starbucks combo! Free Espresso Slash hits for " + freeDmg
                            + " and restores 15 mana!");
                    target.queueFloat(freeDmg, FloatEvent.Kind.DAMAGE, defenderOnRight);
                    queueFloat(15, FloatEvent.Kind.MANA_GAIN, attackerOnRight);
                }
                break;

            case "Tralalelo":
                if (skillNum == 1 && roll < 25) {
                    target.applyEffect(StatusEffect.POISON, 3);
                    addMessage(target.name + " is poisoned (5 dmg × 3 turns).");
                } else if (skillNum == 2 && roll < 20) {
                    if (target.hasEffect(StatusEffect.POISON)) {
                        int cur     = target.effectTurns(StatusEffect.POISON);
                        int doubled = cur * 2;
                        target.applyEffect(StatusEffect.POISON, doubled);
                        addMessage("Poison spread! Now lasts " + doubled + " turns!");
                    } else {
                        addMessage("There was no poison to spread.");
                    }
                } else if (skillNum == 3 && roll < 30) {
                    target.applyEffect(StatusEffect.SEVERE_POISON, 2);
                    addMessage(target.name + " was hit with severe poison (12 dmg × 2 turns).");
                }
                break;

            case "AIP":
                if (skillNum == 1 && roll < 20) {
                    addMana(10);
                    addMessage("AIP hyped up and gained 10 bonus mana!");
                    // Attacker (AIP) gains mana → float on AIP's side
                    queueFloat(10, FloatEvent.Kind.MANA_GAIN, attackerOnRight);
                } else if (skillNum == 2 && roll < 15) {
                    target.applyEffect(StatusEffect.CONFUSE, 2); // 2 turns so it survives the tick
                    addMessage(target.name + " is confused — might hit themselves next turn!");
                }
                break;

            case "Kimmay":
                if (skillNum == 1 && trueDmg) {
                    addMessage("Arcane Note pierces all defenses with true damage!");
                } else if (skillNum == 2 && roll < 20) {
                    target.applyEffect(StatusEffect.FRACTURE, 1);
                    addMessage(target.name + " is fractured — next hit deals 15% more damage.");
                }
                break;

            case "Dianne":
                if (skillNum == 1 && roll < 25) {
                    target.applyEffect(StatusEffect.WEAKNESS, 1);
                    addMessage(target.name + " is weakened — deals 20% less damage next turn.");
                } else if (skillNum == 2 && roll < 25) {
                    target.applyEffect(StatusEffect.EXHAUSTION, 1);
                    addMessage(target.name + " is exhausted — next skill costs 10 extra mana.");
                } else if (skillNum == 3 && roll < 30) {
                    target.applyEffect(StatusEffect.MIND_MAZE, 2);
                    addMessage(target.name + " is in a mind maze — damage halved for 2 turns.");
                }
                break;

            case "Cyberg":
                if (skillNum == 1 && roll < 20) {
                    target.addHp(-8);
                    addMessage("Shadow Punch hit a weak spot for 8 bonus damage!");
                    target.queueFloat(8, FloatEvent.Kind.BONUS_DAMAGE, defenderOnRight);
                } else if (skillNum == 2 && roll < 25) {
                    target.addHp(-12);
                    addHp(-5);
                    addMessage("Cyberg went berserk! Dealt 12 extra damage but took 5 self-damage!");
                    target.queueFloat(12, FloatEvent.Kind.BONUS_DAMAGE, defenderOnRight);
                    // Self-damage float on Cyberg's own side
                    queueFloat(5, FloatEvent.Kind.DAMAGE, attackerOnRight);
                } else if (skillNum == 3 && roll < 30) {
                    target.addHp(-8);
                    addMessage("Critical finisher! Cyberg slammed for 8 extra damage!");
                    target.queueFloat(8, FloatEvent.Kind.BONUS_DAMAGE, defenderOnRight);
                }
                break;

            case "Christian":
                if (skillNum == 1 && roll < 15) {
                    // Apply 2 turns so it survives the tick at the start of the enemy's next turn
                    target.applyEffect(StatusEffect.STUN, 2);
                    addMessage(target.name + " is stunned and skips their next turn!");
                } else if (skillNum == 2 && roll < 25) {
                    applyEffect(StatusEffect.BLOCK, 1);
                    addMessage("Christian raised his guard — blocks 50% of the next hit!");
                } else if (skillNum == 3 && roll < 20) {
                    // Apply 3 turns so 2 full skip-turns remain after the tick
                    target.applyEffect(StatusEffect.HEAVY_STUN, 3);
                    addMessage("Heavy Stun! " + target.name + " is knocked out for 2 turns!");
                }
                break;
        }
    }

    // ── Resolve helpers (called by BattleEngine) ──────────────────────────────

    /**
     * Full resolve — now accepts attackerOnRight so floats appear on the correct side.
     */
    public int resolveSkill(int skillNum, String charName,
                            BattleStats target, boolean isTrueDmgActive,
                            boolean attackerOnRight) {
        int dmg = computeSkill(skillNum, charName, target, isTrueDmgActive, attackerOnRight);
        if (dmg < 0) {
            // Self-hit from confuse
            addHp(dmg);                 // dmg is negative
            queueFloat(-dmg, FloatEvent.Kind.DAMAGE, attackerOnRight);
            return 0;
        }
        if (dmg > 0) {
            target.addHp(-dmg);
            target.queueFloat(dmg, FloatEvent.Kind.DAMAGE, !attackerOnRight);
        }
        return dmg;
    }

    /** Backward-compatible overload. */
    public int resolveSkill(int skillNum, String charName,
                            BattleStats target, boolean isTrueDmgActive) {
        return resolveSkill(skillNum, charName, target, isTrueDmgActive, false);
    }

    public int resolveBagSmash(BattleStats target) {
        int dmg = computeBagSmash(target);
        if (dmg > 0) {
            target.addHp(-dmg);
            target.queueFloat(dmg, FloatEvent.Kind.DAMAGE, true);
        }
        return dmg;
    }

    // ── Inner types ───────────────────────────────────────────────────────────

    public static class ActiveEffect {
        public final StatusEffect type;
        public int turnsLeft;

        public ActiveEffect(StatusEffect type, int turnsLeft) {
            this.type     = type;
            this.turnsLeft = turnsLeft;
        }
    }

    /** A request to show a floating label in the HUD. */
    public static class FloatEvent {
        public enum Kind { DAMAGE, BONUS_DAMAGE, MANA_DRAIN, MANA_GAIN, DODGE }

        public final int    value;
        public final Kind   kind;
        public final boolean onRight; // true = right side of screen (enemy)

        public FloatEvent(int value, Kind kind, boolean onRight) {
            this.value   = value;
            this.kind    = kind;
            this.onRight = onRight;
        }
    }
}
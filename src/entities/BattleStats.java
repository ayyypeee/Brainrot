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

    private final ArrayList<ActiveEffect>   effects         = new ArrayList<>();
    private final ArrayList<String>         pendingMessages = new ArrayList<>();
    private final ArrayList<FloatEvent>     floatEvents     = new ArrayList<>();
    private final ArrayList<PassiveAction>  passiveActions  = new ArrayList<>();
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



    public int tickEffects(boolean thisCharOnRight) {
        int poisonDamage = 0;

        if (hasEffect(StatusEffect.POISON)) {
            // Design doc: Poison = 4 damage per turn
            poisonDamage += 4;
            addHp(-4);
            decrementEffect(StatusEffect.POISON);
            addMessage(name + " suffered 4 damage from poison.");
            queueFloat(4, FloatEvent.Kind.DAMAGE, thisCharOnRight);
        }

        if (hasEffect(StatusEffect.SEVERE_POISON)) {
            // Design doc: Severe Poison = 10 damage per turn
            poisonDamage += 10;
            addHp(-10);
            decrementEffect(StatusEffect.SEVERE_POISON);
            addMessage(name + " suffered 10 damage from severe poison.");
            queueFloat(10, FloatEvent.Kind.DAMAGE, thisCharOnRight);
        }

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

    public int tickEffects() {
        return tickEffects(false);
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

    public void queueFloat(int value, FloatEvent.Kind kind, boolean onRight) {
        floatEvents.add(new FloatEvent(value, kind, onRight));
    }

    public boolean hasFloatEvents()    { return !floatEvents.isEmpty(); }
    public FloatEvent pollFloatEvent() { return floatEvents.isEmpty() ? null : floatEvents.remove(0); }

    // ── Passive actions ───────────────────────────────────────────────────────

    public void queuePassiveAction(BattleStats target,
                                   int hpDelta, int manaDelta,
                                   FloatEvent.Kind floatKind, int floatValue,
                                   boolean onRight, String message) {
        passiveActions.add(new PassiveAction(
                target, hpDelta, manaDelta,
                floatKind, floatValue, onRight, message));
    }

    public boolean hasPassiveActions() { return !passiveActions.isEmpty(); }

    public List<PassiveAction> drainPassiveActions() {
        List<PassiveAction> copy = new ArrayList<>(passiveActions);
        passiveActions.clear();
        return copy;
    }

    // ── Turn-count buffer helper ──────────────────────────────────────────────
    private static int buf(int base, boolean attackerOnRight) {
        return attackerOnRight ? base + 1 : base;
    }

    // ── Skill resolution ──────────────────────────────────────────────────────

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

        // ── Mana ──────────────────────────────────────────────────────────────
        if (skillNum == 1) addMana(skill.manaRegen);
        else {
            int cost = skill.manaCost;
            if (hasEffect(StatusEffect.EXHAUSTION)) {
                cost += 10;
                removeEffect(StatusEffect.EXHAUSTION);
                addMessage(charName + " is exhausted the skill cost 10 extra mana!");
            }
            addMana(-cost);
        }

        // ── Base damage ───────────────────────────────────────────────────────
        int dmg = skill.minDmg + rng.nextInt(skill.maxDmg - skill.minDmg + 1);

        // ── Outgoing debuffs on attacker ──────────────────────────────────────
        if (hasEffect(StatusEffect.WEAKNESS)) {
            dmg = (int)(dmg * 0.80);
            addMessage(charName + " is weakened and deals 20% less damage!");
        }
        if (hasEffect(StatusEffect.MIND_MAZE)) {
            dmg = dmg / 2;
            addMessage(charName + " is lost in a mind maze damage halved!");
        }

        // ── CONFUSE: 50% chance to self-hit ──────────────────────────────────
        if (hasEffect(StatusEffect.CONFUSE) && rng.nextInt(100) < 50) {
            addMessage(charName + " is confused and struck themselves!");
            removeEffect(StatusEffect.CONFUSE);
            return -dmg;
        }

        // ── Kimmay skill 3: 25% chance to shatter all defenses ───────────────
        boolean celestialIgnore = false;
        if (charName.equals("Kimmay") && skillNum == 3 && rng.nextInt(100) < 250) {
            celestialIgnore = true;
            addMessage("Celestial Break shattered all defenses!");
        }
        boolean effectiveTrueDmg = isTrueDmg || celestialIgnore;

        // ── DODGE ─────────────────────────────────────────────────────────────
        if (!effectiveTrueDmg && target.hasEffect(StatusEffect.DODGE)) {
            target.removeEffect(StatusEffect.DODGE);
            addMessage(target.name + " dodged the attack!");
            target.queueFloat(0, FloatEvent.Kind.DODGE, !attackerOnRight);
            return 0;
        }

        // ── BLOCK ─────────────────────────────────────────────────────────────
        if (!effectiveTrueDmg && target.hasEffect(StatusEffect.BLOCK)) {
            dmg = dmg / 2;
            target.removeEffect(StatusEffect.BLOCK);
            addMessage(target.name + " blocked! Damage reduced to " + dmg + ".");
        }

        // ── FRACTURE — now +20% bonus ─────────────────────────────────────────
        if (target.hasEffect(StatusEffect.FRACTURE)) {
            int bonus = (int)(dmg * 0.20);
            dmg += bonus;
            target.removeEffect(StatusEffect.FRACTURE);
            addMessage(target.name + " is fractured takes " + bonus + " extra damage!");
        }

        addMessage(skill.name + " hits " + target.name + " for " + dmg + " damage!");

        computePassive(skillNum, charName, skill, target, rng, dmg, effectiveTrueDmg, attackerOnRight);

        return dmg;
    }

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

    // compute passive.
    private void computePassive(int skillNum, String charName, CharSkill skill,
                                BattleStats target, Random rng,
                                int baseDmg, boolean trueDmg,
                                boolean attackerOnRight) {

        int roll = rng.nextInt(100);
        boolean defenderOnRight = !attackerOnRight;

        if (charName.equals("Tung Tung")) {
            if (skillNum == 1) {
                if (roll < 40) {
                    int steal = Math.min(8, target.mana);
                    if (steal > 0) {
                        target.queuePassiveAction(target, 0, -steal, FloatEvent.Kind.MANA_DRAIN, steal,
                                defenderOnRight, "Tung Tung skillfully stole " + steal + " mana from " + target.name + "!");
                        queuePassiveAction(this, 0, steal, FloatEvent.Kind.MANA_GAIN, steal,
                                attackerOnRight, null);
                    } else {
                        addMessage("Tung Tung tried to steal mana but " + target.name + " is completely out of magic!");
                    }
                }
            } else if (skillNum == 2) {
                if (roll < 30) {
                    int burned = Math.min(10, target.mana);
                    if (burned > 0) {
                        target.queuePassiveAction(target, 0, -burned, FloatEvent.Kind.MANA_DRAIN, burned,
                                defenderOnRight, "Tung Tung viciously burned " + burned + " of " + target.name + "s mana!");
                    } else {
                        addMessage("Tung Tung tried to burn mana but " + target.name + " had none left to lose!");
                    }
                }
            } else if (skillNum == 3) {
                if (roll < 250) {
                    // 2 so that after tickEffects runs this turn, it becomes 1 for the enemy's next turn
                    target.applyEffect(StatusEffect.SILENCE, 2);
                    addMessage(target.name + " is totally silenced and cannot use complex skills next turn!");
                }
            }

        } else if (charName.equals("Ballerina")) {
            if (skillNum == 1) {
                if (roll < 40) {
                    queuePassiveAction(this, 6, 0, FloatEvent.Kind.HEAL, 6,
                            attackerOnRight, "Ballerina gracefully recovered 6 health points!");
                }
            } else if (skillNum == 2) {
                if (roll < 250) {
                    // 2 so that after tickEffects runs this turn, dodge is still active next turn
                    applyEffect(StatusEffect.DODGE, 2);
                    addMessage("Ballerina prepares to nimbly dodge the very next attack!");
                }
            } else if (skillNum == 3) {
                if (roll < 30) {
                    queuePassiveAction(this, 20, 0, FloatEvent.Kind.HEAL, 20,
                            attackerOnRight, "Ballerina beautifully healed 20 health points!");
                }
            }

        } else if (charName.equals("Cappucino")) {
            if (skillNum == 1) {
                if (roll < 25) {
                    int bonusDmg = Math.max(1, baseDmg / 2);
                    target.queuePassiveAction(target, -bonusDmg, 0, FloatEvent.Kind.BONUS_DAMAGE, bonusDmg,
                            defenderOnRight, "Cappucino lands a swift double strike for " + bonusDmg + " extra damage!");
                }
            } else if (skillNum == 2) {
                if (roll < 30) {
                    queuePassiveAction(this, 0, 12, FloatEvent.Kind.MANA_GAIN, 12,
                            attackerOnRight, "Cappucino feels a caffeine rush from Latte Blaze and gets 12 mana back!");
                }
            } else if (skillNum == 3) {
                if (roll < 30) {
                    CharSkill espresso = CharSkillDB.get("Cappucino", 1);
                    int freeDmg;
                    if (espresso != null) {
                        freeDmg = espresso.minDmg + rng.nextInt(espresso.maxDmg - espresso.minDmg + 1);
                    } else {
                        freeDmg = 6 + rng.nextInt(5);
                    }
                    target.queuePassiveAction(target, -freeDmg, 0, FloatEvent.Kind.DAMAGE, freeDmg,
                            defenderOnRight, "Cappucino unleashes a Starbucks combo as a free Espresso Slash hits for " + freeDmg + " damage!");
                    queuePassiveAction(this, 0, 12, FloatEvent.Kind.MANA_GAIN, 12,
                            attackerOnRight, "Cappucino feels incredibly energized as Starbucks restores 12 mana!");
                }
            }

        } else if (charName.equals("Tralalelo")) {
            if (skillNum == 1) {
                if (roll < 35) {
                    // Poison ticks immediately, so 4 turns = 3 remaining ticks after first tick
                    target.applyEffect(StatusEffect.POISON, 4);
                    addMessage(target.name + " is now poisoned and will take lingering damage.");
                }
            } else if (skillNum == 2) {
                if (roll < 30) {
                    if (target.hasEffect(StatusEffect.POISON)) {
                        int cur     = target.effectTurns(StatusEffect.POISON);
                        int doubled = cur * 2;
                        target.applyEffect(StatusEffect.POISON, doubled);
                        addMessage("Oh no! The poison just spread and will now last " + doubled + " turns!");
                    } else {
                        addMessage("There was absolutely no poison on the target to spread around.");
                    }
                }
            } else if (skillNum == 3) {
                if (roll < 30) {
                    // Severe poison ticks immediately, so 3 turns = 2 remaining ticks after first tick
                    target.applyEffect(StatusEffect.SEVERE_POISON, 3);
                    addMessage(target.name + " was infected with severe poison and will take heavy damage.");
                }
            }

        } else if (charName.equals("AIP")) {
            if (skillNum == 1) {
                if (roll < 60) {
                    queuePassiveAction(this, 0, 8, FloatEvent.Kind.MANA_GAIN, 8,
                            attackerOnRight, "AIP gets totally hyped up and gains 8 bonus mana!");
                }
            } else if (skillNum == 2) {
                if (roll < 30) {
                    // 2 so that after tickEffects runs this turn, confuse is still active next turn
                    target.applyEffect(StatusEffect.CONFUSE, 2);
                    addMessage(target.name + " looks terribly confused and might accidentally hit themselves next turn!");
                }
            }

        } else if (charName.equals("Kimmay")) {
            if (skillNum == 1) {
                if (trueDmg) {
                    addMessage("Arcane Note beautifully pierces all defenses with pure true damage!");
                }
            } else if (skillNum == 2) {
                if (roll < 35) {
                    // 2 so that after tickEffects runs this turn, fracture is still active next turn
                    target.applyEffect(StatusEffect.FRACTURE, 2);
                    addMessage(target.name + " just got fractured and will take more damage from the next hit.");
                }
            }

        } else if (charName.equals("Dianne")) {
            if (skillNum == 1) {
                if (roll < 40) {
                    // 2 so that after tickEffects runs this turn, weakness is still active next turn
                    target.applyEffect(StatusEffect.WEAKNESS, 2);
                    addMessage(target.name + " feels incredibly weak and will deal less damage next turn.");
                }
            } else if (skillNum == 2) {
                if (roll < 45) {
                    // 2 so that after tickEffects runs this turn, exhaustion is still active next turn
                    target.applyEffect(StatusEffect.EXHAUSTION, 2);
                    addMessage(target.name + " is completely exhausted so their next skill will cost extra mana.");
                }
            } else if (skillNum == 3) {
                if (roll < 25) {
                    // 3 so that after tickEffects runs this turn, mind maze lasts 2 full turns
                    target.applyEffect(StatusEffect.MIND_MAZE, 3);
                    addMessage(target.name + " is wandering blindly in a mind maze and their damage is halved.");
                }
            }

        } else if (charName.equals("Cyberg")) {
            if (skillNum == 1) {
                if (roll < 40) {
                    target.queuePassiveAction(target, -6, 0, FloatEvent.Kind.BONUS_DAMAGE, 6,
                            defenderOnRight, "Shadow Punch hits a critical weak spot for 6 bonus damage!");
                }
            } else if (skillNum == 2) {
                if (roll < 35) {
                    target.queuePassiveAction(target, -10, 0, FloatEvent.Kind.BONUS_DAMAGE, 10,
                            defenderOnRight, "Cyberg goes completely berserk and deals 10 extra damage!");
                    queuePassiveAction(this, -4, 0, FloatEvent.Kind.DAMAGE, 4,
                            attackerOnRight, "Cyberg takes 4 recoil damage from being so reckless!");
                }
            } else if (skillNum == 3) {
                if (roll < 30) {
                    target.queuePassiveAction(target, -6, 0, FloatEvent.Kind.BONUS_DAMAGE, 6,
                            defenderOnRight, "Cyberg lands an awesome critical finisher for 6 extra damage!");
                }
            }

        } else if (charName.equals("Christian")) {
            if (skillNum == 1) {
                if (roll < 25) {
                    // 2 so that after tickEffects runs this turn, stun is still active next turn
                    target.applyEffect(StatusEffect.STUN, 2);
                    addMessage(target.name + " is completely stunned and has to skip their next turn!");
                }
            } else if (skillNum == 2) {
                if (roll < 50) {
                    // 2 so that after tickEffects runs this turn, block is still active next turn
                    applyEffect(StatusEffect.BLOCK, 2);
                    addMessage("Christian bravely raises his guard and will block half of the next attack!");
                }
            } else if (skillNum == 3) {
                if (roll < 25) {
                    // 3 so that after tickEffects runs this turn, heavy stun lasts 2 full turns
                    target.applyEffect(StatusEffect.HEAVY_STUN, 3);
                    addMessage("What a Heavy Stun as " + target.name + " gets knocked out cold for 2 full turns!");
                }
            }
        }
    }

    // ── Resolve helpers (called by BattleEngine) ──────────────────────────────

    public int resolveSkill(int skillNum, String charName,
                            BattleStats target, boolean isTrueDmgActive,
                            boolean attackerOnRight) {
        int dmg = computeSkill(skillNum, charName, target, isTrueDmgActive, attackerOnRight);
        if (dmg < 0) {
            queuePassiveAction(this,
                    dmg, 0,
                    FloatEvent.Kind.DAMAGE, -dmg,
                    attackerOnRight,
                    null);
            return 0;
        }
        return dmg;
    }

    public int resolveSkill(int skillNum, String charName,
                            BattleStats target, boolean isTrueDmgActive) {
        return resolveSkill(skillNum, charName, target, isTrueDmgActive, false);
    }

    public int resolveBagSmash(BattleStats target) {
        return computeBagSmash(target);
    }

    // ── Inner types ───────────────────────────────────────────────────────────

    public static class ActiveEffect {
        public final StatusEffect type;
        public int turnsLeft;

        public ActiveEffect(StatusEffect type, int turnsLeft) {
            this.type      = type;
            this.turnsLeft = turnsLeft;
        }
    }

    public static class FloatEvent {
        public enum Kind { DAMAGE, BONUS_DAMAGE, MANA_DRAIN, MANA_GAIN, DODGE, HEAL }

        public final int     value;
        public final Kind    kind;
        public final boolean onRight;

        public FloatEvent(int value, Kind kind, boolean onRight) {
            this.value   = value;
            this.kind    = kind;
            this.onRight = onRight;
        }
    }

    public static class PassiveAction {
        public final BattleStats     target;
        public final int             hpDelta;
        public final int             manaDelta;
        public final FloatEvent.Kind floatKind;
        public final int             floatValue;
        public final boolean         onRight;
        public final String          message;

        public PassiveAction(BattleStats target,
                             int hpDelta, int manaDelta,
                             FloatEvent.Kind floatKind, int floatValue,
                             boolean onRight, String message) {
            this.target     = target;
            this.hpDelta    = hpDelta;
            this.manaDelta  = manaDelta;
            this.floatKind  = floatKind;
            this.floatValue = floatValue;
            this.onRight    = onRight;
            this.message    = message;
        }
    }
}
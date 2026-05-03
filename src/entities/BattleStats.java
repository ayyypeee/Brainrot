package entities;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

// fighter stats and effects.
public class BattleStats {

    public static final int MAX_HP   = 100;
    public static final int MAX_MANA = 100;
    public static final int MANA_REGEN_PER_ROUND = 5;

    // public health and mana.
    public int hp;
    public int mana;

    private ArrayList<ActiveEffect>  effects         = new ArrayList<ActiveEffect>();
    private ArrayList<String>        pendingMessages = new ArrayList<String>();
    private ArrayList<FloatEvent>    floatEvents     = new ArrayList<FloatEvent>();
    private ArrayList<PassiveAction> passiveActions  = new ArrayList<PassiveAction>();

    private String name;

    // constructor.
    public BattleStats(String name) {
        this.name = name;
        hp   = MAX_HP;
        mana = 0;
    }

    // adjust health.
    public void addHp(int amount) {
        hp = hp + amount;
        if (hp > MAX_HP) {
            hp = MAX_HP;
        }
        if (hp < 0) {
            hp = 0;
        }
    }

    // adjust mana.
    public void addMana(int amount) {
        mana = mana + amount;
        if (mana > MAX_MANA) {
            mana = MAX_MANA;
        }
        if (mana < 0) {
            mana = 0;
        }
    }

    // check afford.
    public boolean canAfford(int cost) {
        if (mana >= cost) {
            return true;
        }
        return false;
    }

    // regen mana.
    public void regenMana() {
        addMana(MANA_REGEN_PER_ROUND);
    }

    // apply effect.
    public void applyEffect(StatusEffect type, int turns) {
        for (int i = 0; i < effects.size(); i++) {
            ActiveEffect ae = effects.get(i);
            if (ae.type == type) {
                if (turns > ae.turnsLeft) {
                    ae.turnsLeft = turns;
                }
                return;
            }
        }
        effects.add(new ActiveEffect(type, turns));
    }

    // check effect.
    public boolean hasEffect(StatusEffect type) {
        for (int i = 0; i < effects.size(); i++) {
            if (effects.get(i).type == type) {
                return true;
            }
        }
        return false;
    }

    // effect turns.
    public int effectTurns(StatusEffect type) {
        for (int i = 0; i < effects.size(); i++) {
            if (effects.get(i).type == type) {
                return effects.get(i).turnsLeft;
            }
        }
        return 0;
    }

    // remove effect.
    public void removeEffect(StatusEffect type) {
        for (int i = effects.size() - 1; i >= 0; i--) {
            if (effects.get(i).type == type) {
                effects.remove(i);
            }
        }
    }

    // get effects.
    public List<ActiveEffect> getEffects() {
        return new ArrayList<ActiveEffect>(effects);
    }

    // tick effects.
    public int tickEffects(boolean thisCharOnRight) {
        int poisonDamage = 0;

        if (hasEffect(StatusEffect.POISON)) {
            poisonDamage += 4;
            addHp(-4);
            decrementEffect(StatusEffect.POISON);
            addMessage("Ouch! " + name + " winces as the poison drains 4 health.");
            queueFloat(4, FloatEvent.Kind.DAMAGE, thisCharOnRight);
        }

        if (hasEffect(StatusEffect.SEVERE_POISON)) {
            poisonDamage += 10;
            addHp(-10);
            decrementEffect(StatusEffect.SEVERE_POISON);
            addMessage(name + " gasps in pain as severe poison violently strips away 10 health.");
            queueFloat(10, FloatEvent.Kind.DAMAGE, thisCharOnRight);
        }

        ArrayList<ActiveEffect> snapshot = new ArrayList<ActiveEffect>(effects);
        for (int i = 0; i < snapshot.size(); i++) {
            ActiveEffect ae = snapshot.get(i);
            if (ae.type == StatusEffect.POISON || ae.type == StatusEffect.SEVERE_POISON) {
                continue;
            }
            ae.turnsLeft--;
            if (ae.turnsLeft <= 0) {
                effects.remove(ae);
                addMessage("The " + ae.type.displayName + " effect has finally faded away from " + name + ".");
            }
        }

        return poisonDamage;
    }

    // decrement effect.
    private void decrementEffect(StatusEffect type) {
        for (int i = 0; i < effects.size(); i++) {
            ActiveEffect ae = effects.get(i);
            if (ae.type == type) {
                ae.turnsLeft--;
                if (ae.turnsLeft <= 0) {
                    effects.remove(ae);
                }
                return;
            }
        }
    }

    // add message.
    public void addMessage(String msg)  { pendingMessages.add(msg); }

    // has messages.
    public boolean hasMessages()        { return !pendingMessages.isEmpty(); }

    // poll message.
    public String pollMessage() {
        if (pendingMessages.isEmpty()) {
            return null;
        }
        return pendingMessages.remove(0);
    }

    // clear messages.
    public void clearMessages() { pendingMessages.clear(); }

    // queue float.
    public void queueFloat(int value, FloatEvent.Kind kind, boolean onRight) {
        floatEvents.add(new FloatEvent(value, kind, onRight));
    }

    // has float.
    public boolean hasFloatEvents() { return !floatEvents.isEmpty(); }

    // poll float.
    public FloatEvent pollFloatEvent() {
        if (floatEvents.isEmpty()) {
            return null;
        }
        return floatEvents.remove(0);
    }

    // queue passive.
    public void queuePassiveAction(BattleStats target,
                                   int hpDelta, int manaDelta,
                                   FloatEvent.Kind floatKind, int floatValue,
                                   boolean onRight, String message) {
        passiveActions.add(new PassiveAction(
                target, hpDelta, manaDelta,
                floatKind, floatValue, onRight, message));
    }

    // has passive.
    public boolean hasPassiveActions() { return !passiveActions.isEmpty(); }

    // drain passive.
    public List<PassiveAction> drainPassiveActions() {
        List<PassiveAction> copy = new ArrayList<PassiveAction>(passiveActions);
        passiveActions.clear();
        return copy;
    }

    // buf.
    private static int buf(int base, boolean attackerOnRight) {
        if (attackerOnRight) {
            return base + 1;
        }
        return base;
    }

    // compute skill.
    public int computeSkill(int skillNum, String charName,
                            BattleStats target, boolean isTrueDmg,
                            boolean attackerOnRight) {
        Random rng = new Random();
        CharSkill skill = CharSkillDB.get(charName, skillNum);

        if (skill == null) {
            int dmg = 5 + rng.nextInt(6);
            addMessage(charName + " swings hard and deals " + dmg + " damage!");
            return dmg;
        }

        if (skillNum == 1) {
            addMana(skill.manaRegen);
        } else {
            int cost = skill.manaCost;
            if (hasEffect(StatusEffect.EXHAUSTION)) {
                cost += 10;
                removeEffect(StatusEffect.EXHAUSTION);
                addMessage(charName + " feels exhausted and the skill takes 10 extra mana!");
            }
            addMana(-cost);
        }

        int dmg = skill.minDmg + rng.nextInt(skill.maxDmg - skill.minDmg + 1);

        if (hasEffect(StatusEffect.WEAKNESS)) {
            dmg = (int)(dmg * 0.80);
            addMessage(charName + " attacks weakly and deals less damage!");
        }

        if (hasEffect(StatusEffect.MIND_MAZE)) {
            dmg = dmg / 2;
            addMessage(charName + " is lost in a confusing mind maze and their damage is halved!");
        }

        if (hasEffect(StatusEffect.CONFUSE)) {
            if (rng.nextInt(100) < 50) {
                addMessage(charName + " is terribly confused and accidentally struck themselves!");
                removeEffect(StatusEffect.CONFUSE);
                return -dmg;
            }
        }

        boolean celestialIgnore = false;
        if (charName.equals("Kimmay")) {
            if (skillNum == 3) {
                if (rng.nextInt(100) < 25) {
                    celestialIgnore = true;
                    addMessage("Wow! Celestial Break shattered all defenses completely!");
                }
            }
        }

        boolean effectiveTrueDmg = false;
        if (isTrueDmg) {
            effectiveTrueDmg = true;
        } else if (celestialIgnore) {
            effectiveTrueDmg = true;
        }

        if (!effectiveTrueDmg) {
            if (target.hasEffect(StatusEffect.DODGE)) {
                target.removeEffect(StatusEffect.DODGE);
                addMessage(target.name + " gracefully dodged the incoming attack!");
                target.queueFloat(0, FloatEvent.Kind.DODGE, !attackerOnRight);
                return 0;
            }
        }

        if (!effectiveTrueDmg) {
            if (target.hasEffect(StatusEffect.BLOCK)) {
                dmg = dmg / 2;
                target.removeEffect(StatusEffect.BLOCK);
                addMessage(target.name + " bravely blocked the attack and reduced the damage to " + dmg + ".");
            }
        }

        if (target.hasEffect(StatusEffect.FRACTURE)) {
            int bonus = (int)(dmg * 0.20);
            dmg += bonus;
            target.removeEffect(StatusEffect.FRACTURE);
            addMessage(target.name + " is fractured and suffers " + bonus + " extra damage!");
        }

        addMessage(skill.name + " viciously smashes into " + target.name + " for " + dmg + " damage!");

        computePassive(skillNum, charName, skill, target, rng, dmg, effectiveTrueDmg, attackerOnRight);

        return dmg;
    }

    // compute bag smash.
    public int computeBagSmash(BattleStats target) {
        Random rng = new Random();
        addMana(-50);

        if (rng.nextInt(49) < 50) {
            int dmg = 100;
            addMessage("Bag Smash lands perfectly for a devastating " + dmg + " damage!");
            return dmg;
        } else {
            int lost = mana;
            mana = 0;
            addMessage("Oh no! Bag Smash missed entirely and AIP lost all " + lost + " mana!");
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
                if (roll < 25) {
                    target.applyEffect(StatusEffect.SILENCE, buf(1, attackerOnRight));
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
                if (roll < 25) {
                    applyEffect(StatusEffect.DODGE, buf(1, attackerOnRight));
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
                    target.applyEffect(StatusEffect.POISON, buf(3, attackerOnRight));
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
                    target.applyEffect(StatusEffect.SEVERE_POISON, buf(2, attackerOnRight));
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
                    target.applyEffect(StatusEffect.CONFUSE, buf(1, attackerOnRight));
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
                    target.applyEffect(StatusEffect.FRACTURE, buf(1, attackerOnRight));
                    addMessage(target.name + " just got fractured and will take more damage from the next hit.");
                }
            }

        } else if (charName.equals("Dianne")) {
            if (skillNum == 1) {
                if (roll < 40) {
                    target.applyEffect(StatusEffect.WEAKNESS, buf(1, attackerOnRight));
                    addMessage(target.name + " feels incredibly weak and will deal less damage next turn.");
                }
            } else if (skillNum == 2) {
                if (roll < 45) {
                    target.applyEffect(StatusEffect.EXHAUSTION, buf(1, attackerOnRight));
                    addMessage(target.name + " is completely exhausted so their next skill will cost extra mana.");
                }
            } else if (skillNum == 3) {
                if (roll < 25) {
                    target.applyEffect(StatusEffect.MIND_MAZE, buf(2, attackerOnRight));
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
                    target.applyEffect(StatusEffect.STUN, buf(1, attackerOnRight));
                    addMessage(target.name + " is completely stunned and has to skip their next turn!");
                }
            } else if (skillNum == 2) {
                if (roll < 50) {
                    applyEffect(StatusEffect.BLOCK, buf(1, attackerOnRight));
                    addMessage("Christian bravely raises his guard and will block half of the next attack!");
                }
            } else if (skillNum == 3) {
                if (roll < 25) {
                    target.applyEffect(StatusEffect.HEAVY_STUN, buf(2, attackerOnRight));
                    addMessage("What a Heavy Stun as " + target.name + " gets knocked out cold for 2 full turns!");
                }
            }
        }
    }

    // resolve skill.
    public int resolveSkill(int skillNum, String charName,
                            BattleStats target, boolean isTrueDmgActive,
                            boolean attackerOnRight) {

        int dmg = computeSkill(skillNum, charName, target, isTrueDmgActive, attackerOnRight);

        if (dmg < 0) {
            queuePassiveAction(this, dmg, 0, FloatEvent.Kind.DAMAGE, -dmg,
                    attackerOnRight, null);
            return 0;
        }
        return dmg;
    }

    // resolve bag smash.
    public int resolveBagSmash(BattleStats target) {
        return computeBagSmash(target);
    }

    // active effect class.
    public static class ActiveEffect {
        public StatusEffect type;
        public int turnsLeft;

        public ActiveEffect(StatusEffect type, int turnsLeft) {
            this.type      = type;
            this.turnsLeft = turnsLeft;
        }
    }

    // float event class.
    public static class FloatEvent {
        public enum Kind { DAMAGE, BONUS_DAMAGE, MANA_DRAIN, MANA_GAIN, DODGE, HEAL }

        public int     value;
        public Kind    kind;
        public boolean onRight;

        public FloatEvent(int value, Kind kind, boolean onRight) {
            this.value   = value;
            this.kind    = kind;
            this.onRight = onRight;
        }
    }

    // passive action class.
    public static class PassiveAction {
        public BattleStats     target;
        public int             hpDelta;
        public int             manaDelta;
        public FloatEvent.Kind floatKind;
        public int             floatValue;
        public boolean         onRight;
        public String          message;

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
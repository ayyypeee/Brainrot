package entities;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class BattleStats {

    public static final int MAX_HP = 100;
    public static final int MAX_MANA = 100;
    public static final int MANA_REGEN_PER_ROUND = 5;

    public int hp;
    public int mana;

    private final ArrayList<ActiveEffect> effects = new ArrayList<>();
    private final ArrayList<String> pendingMessages = new ArrayList<>();
    private final String name;

    public BattleStats(String name) {
        this.name = name;
        hp = MAX_HP;
        mana = 0;
    }

    public void addHp(int amount) {
        hp = Math.min(MAX_HP, Math.max(0, hp + amount));
    }

    public void addMana(int amount) {
        mana = Math.min(MAX_MANA, Math.max(0, mana + amount));
    }

    public boolean canAfford(int cost) {
        return mana >= cost;
    }

    public void regenMana() {
        addMana(MANA_REGEN_PER_ROUND);
    }

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
        for (ActiveEffect ae : effects) {
            if (ae.type == type) return true;
        }
        return false;
    }

    public int effectTurns(StatusEffect type) {
        for (ActiveEffect ae : effects) {
            if (ae.type == type) return ae.turnsLeft;
        }
        return 0;
    }

    public void removeEffect(StatusEffect type) {
        effects.removeIf(ae -> ae.type == type);
    }

    public List<ActiveEffect> getEffects() {
        return new ArrayList<>(effects);
    }

    public int tickEffects() {
        int poisonDamage = 0;

        if (hasEffect(StatusEffect.POISON)) {
            poisonDamage += 5;
            addHp(-5);
            decrementEffect(StatusEffect.POISON);
            addMessage(name + " suffered 5 damage from the poison.");
        }
        if (hasEffect(StatusEffect.SEVERE_POISON)) {
            poisonDamage += 12;
            addHp(-12);
            decrementEffect(StatusEffect.SEVERE_POISON);
            addMessage(name + " suffered 12 damage from severe poison.");
        }

        List<ActiveEffect> snap = new ArrayList<>(effects);
        for (ActiveEffect ae : snap) {
            if (ae.type != StatusEffect.POISON && ae.type != StatusEffect.SEVERE_POISON) {
                ae.turnsLeft--;
                if (ae.turnsLeft <= 0) {
                    effects.remove(ae);
                    addMessage("The " + ae.type.displayName + " effect finally wore off for " + name + ".");
                }
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

    public void addMessage(String msg) {
        pendingMessages.add(msg);
    }

    public boolean hasMessages() {
        return !pendingMessages.isEmpty();
    }

    public String pollMessage() {
        return pendingMessages.isEmpty() ? null : pendingMessages.remove(0);
    }

    public void clearMessages() {
        pendingMessages.clear();
    }

    public int computeSkill(int skillNum, String charName, BattleStats target, boolean isTrueDmg) {
        Random rng = new Random();
        CharSkill skill = CharSkillDB.get(charName, skillNum);

        if (skill == null) {
            int dmg = 5 + rng.nextInt(6);
            addMessage(charName + " attacks for " + dmg + " damage!");
            return dmg;
        }

        if (skillNum == 1) {
            addMana(skill.manaRegen);
        } else {
            addMana(-skill.manaCost);
        }

        int dmg = skill.minDmg + rng.nextInt(skill.maxDmg - skill.minDmg + 1);

        if (hasEffect(StatusEffect.WEAKNESS)) {
            dmg = (int)(dmg * 0.80);
            addMessage(charName + " is feeling weak and deals less damage!");
        }
        if (hasEffect(StatusEffect.MIND_MAZE)) {
            dmg = dmg / 2;
            addMessage(charName + " is trapped in a mind maze! Their damage is cut in half!");
        }

        if (hasEffect(StatusEffect.CONFUSE) && rng.nextInt(100) < 50) {
            addMessage(charName + " is confused and attacked themselves!");
            removeEffect(StatusEffect.CONFUSE);
            computePassive(skillNum, charName, skill, target, rng, dmg, isTrueDmg);
            return -(dmg);
        }

        boolean celestialIgnore = false;
        if (charName.equals("Kimmay") && skillNum == 3 && rng.nextInt(100) < 30) {
            celestialIgnore = true;
            addMessage("Celestial Break just shattered all defenses!");
        }
        boolean effectiveTrueDmg = isTrueDmg || celestialIgnore;

        if (!effectiveTrueDmg && target.hasEffect(StatusEffect.DODGE)) {
            target.removeEffect(StatusEffect.DODGE);
            addMessage(target.name + " easily dodged the attack!");
            return 0;
        }

        if (!effectiveTrueDmg && target.hasEffect(StatusEffect.BLOCK)) {
            dmg /= 2;
            target.removeEffect(StatusEffect.BLOCK);
            addMessage(target.name + " blocked the hit! Damage is reduced to " + dmg + "!");
        }

        if (target.hasEffect(StatusEffect.FRACTURE)) {
            int bonus = (int)(dmg * 0.15);
            dmg += bonus;
            addMessage(target.name + " is fractured and takes " + bonus + " extra damage!");
            target.removeEffect(StatusEffect.FRACTURE);
        }

        addMessage(skill.name + " hits " + target.name + " for " + dmg + " damage!");

        computePassive(skillNum, charName, skill, target, rng, dmg, effectiveTrueDmg);

        return dmg;
    }

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
            addMessage("Bag Smash completely missed! AIP dropped all " + lost + " of his mana!");
            return 0;
        }
    }

    private void computePassive(int skillNum, String charName, CharSkill skill, BattleStats target, Random rng, int baseDmg, boolean trueDmg) {

        boolean skipRoll = trueDmg && charName.equals("Kimmay") && skillNum == 3;
        int roll = skipRoll ? 0 : rng.nextInt(100);

        switch (charName) {

            case "Tung Tung":
                if (skillNum == 1 && roll < 20) {
                    int steal = Math.min(10, target.mana);
                    target.addMana(-steal);
                    addMana(steal);
                    addMessage("Tung Tung swiped " + steal + " mana from " + target.name + "!");
                } else if (skillNum == 2 && roll < 25) {
                    int burned = Math.min(15, target.mana);
                    target.addMana(-burned);
                    addMessage("Tung Tung burned away " + burned + " of " + target.name + "'s mana!");
                } else if (skillNum == 3 && roll < 30) {
                    target.applyEffect(StatusEffect.SILENCE, 1);
                    addMessage(target.name + " got silenced and cannot use skills next turn!");
                }
                break;

            case "Ballerina":
                if (skillNum == 1 && roll < 20) {
                    addHp(8);
                    addMessage("Ballerina quickly recovered 8 HP!");
                } else if (skillNum == 2 && roll < 25) {
                    applyEffect(StatusEffect.DODGE, 1);
                    addMessage("Ballerina is ready to dodge the next attack!");
                } else if (skillNum == 3 && roll < 30) {
                    addHp(25);
                    addMessage("Ballerina beautifully healed 25 HP!");
                }
                break;

            case "Cappucino":
                if (skillNum == 1 && roll < 15) {
                    int bonusDmg = Math.max(1, baseDmg / 2);
                    target.addHp(-bonusDmg);
                    addMessage("Cappucino went in for a double strike and dealt " + bonusDmg + " more damage!");
                } else if (skillNum == 2 && roll < 20) {
                    addMana(15);
                    addMessage("Latte Blaze gave Cappucino 15 mana back!");
                } else if (skillNum == 3 && roll < 20) {
                    CharSkill espresso = CharSkillDB.get("Cappucino", 1);
                    int freeDmg = espresso != null
                            ? espresso.minDmg + rng.nextInt(espresso.maxDmg - espresso.minDmg + 1)
                            : 8 + rng.nextInt(6);
                    target.addHp(-freeDmg);
                    addMana(15);
                    addMessage("Starbucks combo! A free Espresso Slash hits for " + freeDmg + " damage and restores 15 mana!");
                }
                break;

            case "Tralalelo":
                if (skillNum == 1 && roll < 25) {
                    target.applyEffect(StatusEffect.POISON, 3);
                    addMessage(target.name + " got poisoned! They will take 5 damage for the next 3 turns.");
                } else if (skillNum == 2 && roll < 20) {
                    if (target.hasEffect(StatusEffect.POISON)) {
                        int cur = target.effectTurns(StatusEffect.POISON);
                        int doubled = cur * 2;
                        target.applyEffect(StatusEffect.POISON, doubled);
                        addMessage("The poison spread! It will now last for " + doubled + " turns!");
                    } else {
                        addMessage("There was no poison to double up.");
                    }
                } else if (skillNum == 3 && roll < 30) {
                    target.applyEffect(StatusEffect.SEVERE_POISON, 2);
                    addMessage(target.name + " was hit with severe poison! They will take 12 damage for 2 turns.");
                }
                break;

            case "AIP":
                if (skillNum == 1 && roll < 20) {
                    addMana(10);
                    addMessage("AIP hyped up and gained 10 bonus mana!");
                } else if (skillNum == 2 && roll < 15) {
                    target.applyEffect(StatusEffect.CONFUSE, 1);
                    addMessage(target.name + " got confused! They might just hit themselves next turn!");
                }
                break;

            case "Kimmay":
                if (skillNum == 1 && trueDmg) {
                    addMessage("Arcane Note sliced right through all defenses with true damage!");
                } else if (skillNum == 1 && roll < 20) {
                    addMessage("Arcane Note hits directly with true damage!");
                } else if (skillNum == 2 && roll < 20) {
                    target.applyEffect(StatusEffect.FRACTURE, 1);
                    addMessage(target.name + " got fractured! They will take 15 percent more damage on the next hit.");
                }
                break;

            case "Dianne":
                if (skillNum == 1 && roll < 25) {
                    target.applyEffect(StatusEffect.WEAKNESS, 1);
                    addMessage(target.name + " got weakened and will deal 20 percent less damage next turn.");
                } else if (skillNum == 2 && roll < 25) {
                    target.applyEffect(StatusEffect.EXHAUSTION, 1);
                    addMessage(target.name + " is exhausted! Their next skill will cost 10 more mana.");
                } else if (skillNum == 3 && roll < 30) {
                    target.applyEffect(StatusEffect.MIND_MAZE, 2);
                    addMessage(target.name + " is lost in a mind maze! Their damage is cut in half for 2 turns.");
                }
                break;

            case "Cyberg":
                if (skillNum == 1 && roll < 20) {
                    target.addHp(-8);
                    addMessage("Shadow Punch hit a weak spot for 8 bonus damage!");
                } else if (skillNum == 2 && roll < 25) {
                    target.addHp(-12);
                    addHp(-5);
                    addMessage("Cyberg went berserk! He dealt 12 extra damage but took 5 damage in return!");
                } else if (skillNum == 3 && roll < 30) {
                    target.addHp(-8);
                    addMessage("A flawless critical hit! Cyberg slammed them for 8 extra damage!");
                }
                break;

            case "Christian":
                if (skillNum == 1 && roll < 15) {
                    target.applyEffect(StatusEffect.STUN, 1);
                    addMessage(target.name + " got stunned and has to skip their next turn!");
                } else if (skillNum == 2 && roll < 25) {
                    applyEffect(StatusEffect.BLOCK, 1);
                    addMessage("Christian put up his guard to block half the damage of the next attack!");
                } else if (skillNum == 3 && roll < 20) {
                    target.applyEffect(StatusEffect.HEAVY_STUN, 2);
                    addMessage("Heavy stun! " + target.name + " is knocked out cold for 2 turns!");
                }
                break;
        }
    }

    public int resolveSkill(int skillNum, String charName, BattleStats target, boolean isTrueDmgActive) {
        int dmg = computeSkill(skillNum, charName, target, isTrueDmgActive);
        if (dmg < 0) {
            addHp(dmg);
            return 0;
        }
        if (dmg > 0) target.addHp(-dmg);
        return dmg;
    }

    public int resolveBagSmash(BattleStats target) {
        int dmg = computeBagSmash(target);
        if (dmg > 0) target.addHp(-dmg);
        return dmg;
    }

    public static class ActiveEffect {
        public final StatusEffect type;
        public int turnsLeft;

        public ActiveEffect(StatusEffect type, int turnsLeft) {
            this.type = type;
            this.turnsLeft = turnsLeft;
        }
    }
}
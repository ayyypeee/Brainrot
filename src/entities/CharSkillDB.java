package entities;

import java.util.HashMap;
import java.util.Map;


public class CharSkillDB {

    private static final Map<String, CharSkill[]> DB = new HashMap<>();

    static {
        // ── Tung Tung ─────────────────────────────────────────────────────────
        reg("Tung Tung",
                sk("Tung Bat Smash",    7, 11, 0, 15, "20% steal 10 Mana from enemy",    20),
                sk("Triple Tung Strike",14, 19,20,  0, "25% burn 15 enemy Mana",          25),
                sk("Sahur Catastrophe", 26, 34,35,  0, "30% Silence enemy 1 turn",        30));

        // ── Ballerina ─────────────────────────────────────────────────────────
        reg("Ballerina",
                sk("Pirouette Kick",    6, 10, 0, 15, "20% heal self 8 HP",              20),
                sk("Love Melody",      13, 17,18,  0, "25% Dodge next enemy attack",     25),
                sk("Cappuccina Love Storm",24,30,30, 0,"30% heal self 25 HP",            30));

        // ── Tralalelo ─────────────────────────────────────────────────────────
        reg("Tralalelo",
                sk("Shark Bite",        8, 12, 0, 15, "25% Poison enemy (5 dmg×3 turns)",25),
                sk("Tralala Chomp",    14, 18,20,  0, "20% double active Poison duration",20),
                sk("Explosion Jaw Frenzy",25,33,32,0, "30% Severe Poison (12 dmg×2 turns)",30));

        // ── Cappucino (Assassino) ─────────────────────────────────────────────
        reg("Cappucino",
                sk("Espresso Slash",    8, 13, 0, 15, "15% attack again for half damage", 15),
                sk("Latte Blaze",      17, 23,20,  0, "20% refund 15 Mana",              20),
                sk("Starbucks",        29, 38,35,  0, "20% free Espresso Slash after ult",20));

        // ── AIP ───────────────────────────────────────────────────────────────
        reg("AIP",
                sk("Ballpen Stab",      7, 12, 0, 15, "20% gain extra 10 Mana",          20),
                sk("Takyan Throw",     15, 21,20,  0, "15% Confuse enemy (50% self-hit)", 15),
                sk("Bag Smash",         0,100,50,  0, "50% deal double dmg OR 0 dmg & lose all Mana",50));

        // ── Kimmay ────────────────────────────────────────────────────────────
        reg("Kimmay",
                sk("Arcane Note",       9, 13, 0, 15, "20% True Damage (bypasses passives)",20),
                sk("Mystic Sigil",     18, 24,22,  0, "20% Fracture (+15% dmg next hit)", 20),
                sk("Celestial Break",  30, 35,40,  0, "30% ignore all defensive passives",30));

        // ── Dianne ────────────────────────────────────────────────────────────
        reg("Dianne",
                sk("Riddle Strike",     6, 11, 0, 15, "25% Weakness (-20% enemy dmg 1 turn)",25),
                sk("Logic Trap",       14, 20,18,  0, "25% Exhaustion (next skill +10 Mana cost)",25),
                sk("Mind Maze",        25, 33,33,  0, "30% halve enemy damage for 2 turns",30));

        // ── Cyberg ────────────────────────────────────────────────────────────
        reg("Cyberg",
                sk("Shadow Punch",      8, 13, 0, 15, "20% deal +8 bonus damage",        20),
                sk("Cyber Dash",       17, 23,20,  0, "25% Berserk (+12 dmg, take 5 self)",25),
                sk("Silent Finisher",  28, 37,35,  0, "30% max crit (max dmg +8 extra)", 30));

        // ── Christian ────────────────────────────────────────────────────────
        reg("Christian",
                sk("Quick Jab",         7, 11, 0, 15, "15% Stun enemy (skip 1 turn)",    15),
                sk("Smart Counter",    14, 20,18,  0, "25% Block (50% less next damage)", 25),
                sk("Focus Strike",     25, 33,33,  0, "20% Heavy Stun (skip 2 turns)",   20));
    }

    private static void reg(String name, CharSkill s1, CharSkill s2, CharSkill s3) {
        DB.put(name, new CharSkill[]{ s1, s2, s3 });
    }

    private static CharSkill sk(String name, int min, int max,
                                int cost, int regen, String passive, int chance) {
        return new CharSkill(name, min, max, cost, regen, passive, chance);
    }

    /** Returns the CharSkill for the given character and skill number (1-based). */
    public static CharSkill get(String charName, int skillNum) {
        CharSkill[] arr = DB.get(charName);
        if (arr == null || skillNum < 1 || skillNum > 3) return null;
        return arr[skillNum - 1];
    }

    /** Returns all three skills for a character. */
    public static CharSkill[] getAll(String charName) {
        return DB.getOrDefault(charName, new CharSkill[0]);
    }
}
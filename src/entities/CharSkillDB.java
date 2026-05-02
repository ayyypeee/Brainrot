package entities;

import java.util.HashMap;
import java.util.Map;

public class CharSkillDB {

    private static final Map<String, CharSkill[]> DB = new HashMap<>();

    static {
        // ── Tung Tung ─────────────────────────────────────────────────────────
        reg("Tung Tung",
                sk("Tung Bat Smash",    7, 11, 0, 15, "20% steal 10 Mana from enemy",    20, "/characters/skill_icons/tungtung_sk1.png"),
                sk("Triple Tung Strike",14, 19,20,  0, "25% burn 15 enemy Mana",          25, "/characters/skill_icons/tungtung_sk2.png"),
                sk("Sahur Catastrophe", 26, 34,35,  0, "30% Silence enemy 1 turn",        30, "/characters/skill_icons/tungtung_sk3.png"));

        // ── Ballerina ─────────────────────────────────────────────────────────
        reg("Ballerina",
                sk("Pirouette Kick",    6, 10, 0, 15, "20% heal self 8 HP",              20, "/characters/skill_icons/ballerina_sk1.png"),
                sk("Love Melody",      13, 17,18,  0, "25% Dodge next enemy attack",     25, "/characters/skill_icons/ballerina_sk2.png"),
                sk("Cappuccina Love Storm",24,30,30, 0,"30% heal self 25 HP",            30, "/characters/skill_icons/ballerina_sk3.png"));

        // ── Tralalelo ─────────────────────────────────────────────────────────
        reg("Tralalelo",
                sk("Shark Bite",        8, 12, 0, 15, "25% Poison enemy (5 dmg×3 turns)",25, "/characters/skill_icons/tralalelo_sk1.png"),
                sk("Tralala Chomp",    14, 18,20,  0, "20% double active Poison duration",20, "/characters/skill_icons/tralalelo_sk2.png"),
                sk("Explosion Jaw Frenzy",25,33,32,0, "30% Severe Poison (12 dmg×2 turns)",30, "/characters/skill_icons/tralalelo_sk3.png"));

        // ── Cappucino (Assassino) ─────────────────────────────────────────────
        reg("Cappucino",
                sk("Espresso Slash",    8, 13, 0, 15, "15% attack again for half damage", 15, "/characters/skill_icons/cappucino_sk1.png"),
                sk("Latte Blaze",      17, 23,20,  0, "20% refund 15 Mana",              20, "/characters/skill_icons/cappucino_sk2.png"),
                sk("Starbucks",        29, 38,35,  0, "20% free Espresso Slash after ult",20, "/characters/skill_icons/cappucino_sk3.png"));

        // ── AIP ───────────────────────────────────────────────────────────────
        reg("AIP",
                sk("Ballpen Stab",      7, 12, 0, 15, "20% gain extra 10 Mana",          20, "/characters/skill_icons/aip_sk1.png"),
                sk("Takyan Throw",     15, 21,20,  0, "15% Confuse enemy (50% self-hit)", 15, "/characters/skill_icons/aip_sk2.png"),
                sk("Bag Smash",         0,100,50,  0, "50% deal double dmg OR 0 dmg & lose all Mana",50, "/characters/skill_icons/aip_sk3.png"));

        // ── Kimmay ────────────────────────────────────────────────────────────
        reg("Kimmay",
                sk("Arcane Note",       9, 13, 0, 15, "20% True Damage (bypasses passives)",20, "/characters/skill_icons/kimmay_sk1.png"),
                sk("Mystic Sigil",     18, 24,22,  0, "20% Fracture (+15% dmg next hit)", 20, "/characters/skill_icons/kimmay_sk2.png"),
                sk("Celestial Break",  30, 35,40,  0, "30% ignore all defensive passives",30, "/characters/skill_icons/kimmay_sk3.png"));

        // ── Dianne ────────────────────────────────────────────────────────────
        reg("Dianne",
                sk("Riddle Strike",     6, 11, 0, 15, "25% Weakness (-20% enemy dmg 1 turn)",25, "/characters/skill_icons/dianne_sk1.png"),
                sk("Logic Trap",       14, 20,18,  0, "25% Exhaustion (next skill +10 Mana cost)",25, "/characters/skill_icons/dianne_sk2.png"),
                sk("Mind Maze",        25, 33,33,  0, "30% halve enemy damage for 2 turns",30, "/characters/skill_icons/dianne_sk3.png"));

        // ── Cyberg ────────────────────────────────────────────────────────────
        reg("Cyberg",
                sk("Shadow Punch",      8, 13, 0, 15, "20% deal +8 bonus damage",        20, "/characters/skill_icons/cyberg_sk1.png"),
                sk("Cyber Dash",       17, 23,20,  0, "25% Berserk (+12 dmg, take 5 self)",25, "/characters/skill_icons/cyberg_sk2.png"),
                sk("Silent Finisher",  28, 37,35,  0, "30% max crit (max dmg +8 extra)", 30, "/characters/skill_icons/cyberg_sk3.png"));

        // ── Christian ────────────────────────────────────────────────────────
        reg("Christian",
                sk("Quick Jab",         7, 11, 0, 15, "15% Stun enemy (skip 1 turn)",    15, "/characters/skill_icons/christian_sk1.png"),
                sk("Smart Counter",    14, 20,18,  0, "25% Block (50% less next damage)", 25, "/characters/skill_icons/christian_sk2.png"),
                sk("Focus Strike",     25, 33,33,  0, "20% Heavy Stun (skip 2 turns)",   20, "/characters/skill_icons/christian_sk3.png"));
    }

    private static void reg(String name, CharSkill s1, CharSkill s2, CharSkill s3) {
        DB.put(name, new CharSkill[]{ s1, s2, s3 });
    }

    private static CharSkill sk(String name, int min, int max,
                                int cost, int regen, String passive, int chance,
                                String iconPath) {
        return new CharSkill(name, min, max, cost, regen, passive, chance, iconPath);
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
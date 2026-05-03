package entities;

import java.util.HashMap;
import java.util.Map;

public class CharSkillDB {

    // The database: character name maps to an array of 3 skills.
    private static Map<String, CharSkill[]> DB = new HashMap<String, CharSkill[]>();

    // Static block runs once when the class is first loaded, filling the database.
    static {

        // Tung Tung — mana thief
        // Sk1: 40% steal 8 mana | Sk2: 30% burn 10 enemy mana | Sk3: 25% Silence
        reg("Tung Tung",
                sk("Tung Bat Smash",     5,  8,  0, 15, "40% steal 8 Mana from enemy",       40, "/characters/skill_icons/tungtung_sk1.png"),
                sk("Triple Tung Strike", 11, 15, 20,  0, "30% burn 10 enemy Mana",             30, "/characters/skill_icons/tungtung_sk2.png"),
                sk("Sahur Catastrophe",  20, 26, 35,  0, "25% Silence enemy 1 turn",           25, "/characters/skill_icons/tungtung_sk3.png"));

        // Ballerina — healer and dodger
        // Sk1: 40% heal 6 HP | Sk2: 25% Dodge | Sk3: 30% heal 20 HP
        reg("Ballerina",
                sk("Pirouette Kick",         4,  7,  0, 15, "40% heal self 6 HP",              40, "/characters/skill_icons/ballerina_sk1.png"),
                sk("Love Melody",            9, 13, 18,  0, "25% Dodge next enemy attack",     25, "/characters/skill_icons/ballerina_sk2.png"),
                sk("Cappuccina Love Storm", 18, 24, 30,  0, "30% heal self 20 HP",             30, "/characters/skill_icons/ballerina_sk3.png"));

        // Tralalelo — poison specialist
        // Sk1: 35% Poison | Sk2: 30% double poison | Sk3: 30% Severe Poison
        reg("Tralalelo",
                sk("Shark Bite",            5,  9,  0, 15, "35% Poison enemy (4 dmg x3 turns)", 35, "/characters/skill_icons/tralalelo_sk1.png"),
                sk("Tralala Chomp",        10, 14, 20,  0, "30% double active Poison duration",  30, "/characters/skill_icons/tralalelo_sk2.png"),
                sk("Explosion Jaw Frenzy", 19, 25, 32,  0, "30% Severe Poison (10 dmg x2 turns)",30, "/characters/skill_icons/tralalelo_sk3.png"));

        // Cappucino — combo attacker
        // Sk1: 25% double hit | Sk2: 30% mana refund | Sk3: 30% free Espresso Slash
        reg("Cappucino",
                sk("Espresso Slash",  6, 10,  0, 15, "25% attack again for half damage",    25, "/characters/skill_icons/cappucino_sk1.png"),
                sk("Latte Blaze",    12, 16, 20,  0, "30% refund 12 Mana",                  30, "/characters/skill_icons/cappucino_sk2.png"),
                sk("Starbucks",      22, 28, 35,  0, "30% free Espresso Slash after ult",   30, "/characters/skill_icons/cappucino_sk3.png"));

        // AIP — high risk, high reward
        // Sk1: 60% gain +8 mana | Sk2: 30% Confuse | Sk3: 50/50 100 damage or nothing
        reg("AIP",
                sk("Ballpen Stab",  5,  9,  0, 15, "60% gain extra +8 Mana",                    60, "/characters/skill_icons/aip_sk1.png"),
                sk("Takyan Throw", 11, 16, 20,  0, "30% Confuse enemy (50% self-hit)",           30, "/characters/skill_icons/aip_sk2.png"),
                sk("Bag Smash",     0,100, 50,  0, "50% deal 100 dmg OR 0 dmg and lose all Mana",50, "/characters/skill_icons/aip_sk3.png"));

        // Kimmay — armor piercer
        // Sk1: 30% True Damage | Sk2: 35% Fracture | Sk3: 25% ignore all defenses
        reg("Kimmay",
                sk("Arcane Note",      6, 10,  0, 15, "30% True Damage (bypasses all passives)", 30, "/characters/skill_icons/kimmay_sk1.png"),
                sk("Mystic Sigil",    13, 18, 22,  0, "35% Fracture (+20% dmg next hit)",        35, "/characters/skill_icons/kimmay_sk2.png"),
                sk("Celestial Break", 24, 29, 40,  0, "25% ignore all defensive passives",       25, "/characters/skill_icons/kimmay_sk3.png"));

        // Dianne — debuffer
        // Sk1: 40% Weakness | Sk2: 45% Exhaustion | Sk3: 25% Mind Maze
        reg("Dianne",
                sk("Riddle Strike",  4,  8,  0, 15, "40% Weakness (-20% enemy dmg 1 turn)",    40, "/characters/skill_icons/dianne_sk1.png"),
                sk("Logic Trap",    10, 15, 18,  0, "45% Exhaustion (next skill +10 Mana cost)",45, "/characters/skill_icons/dianne_sk2.png"),
                sk("Mind Maze",     18, 25, 33,  0, "25% halve enemy damage for 2 turns",       25, "/characters/skill_icons/dianne_sk3.png"));

        // Cyberg — burst damage dealer
        // Sk1: 40% +6 bonus | Sk2: 35% Berserk | Sk3: 30% max crit
        reg("Cyberg",
                sk("Shadow Punch",    5,  9,  0, 15, "40% deal +6 bonus damage",            40, "/characters/skill_icons/cyberg_sk1.png"),
                sk("Cyber Dash",     12, 17, 20,  0, "35% Berserk (+10 dmg, take 4 self)",  35, "/characters/skill_icons/cyberg_sk2.png"),
                sk("Silent Finisher",21, 28, 35,  0, "30% max crit (max dmg +6 extra)",     30, "/characters/skill_icons/cyberg_sk3.png"));

        // Christian — tank and stunner
        // Sk1: 25% Stun | Sk2: 50% Block | Sk3: 25% Heavy Stun
        reg("Christian",
                sk("Quick Jab",      5,  9,  0, 15, "25% Stun enemy (skip 1 turn)",        25, "/characters/skill_icons/christian_sk1.png"),
                sk("Smart Counter", 10, 15, 18,  0, "50% Block (50% less next damage)",    50, "/characters/skill_icons/christian_sk2.png"),
                sk("Focus Strike",  16, 22, 33,  0, "25% Heavy Stun (skip 2 turns)",       25, "/characters/skill_icons/christian_sk3.png"));
    }

    // Adds one character's 3 skills to the database.
    private static void reg(String name, CharSkill s1, CharSkill s2, CharSkill s3) {
        CharSkill[] skills = new CharSkill[3];
        skills[0] = s1;
        skills[1] = s2;
        skills[2] = s3;
        DB.put(name, skills);
    }

    private static CharSkill sk(String name, int min, int max,
                                int cost, int regen, String passive, int chance,
                                String iconPath) {
        return new CharSkill(name, min, max, cost, regen, passive, chance, iconPath);
    }

    // Returns one skill for a character by skill number (1, 2, or 3).
    public static CharSkill get(String charName, int skillNum) {
        CharSkill[] arr = DB.get(charName);
        if (arr == null) {
            return null;
        }
        if (skillNum < 1 || skillNum > 3) {
            return null;
        }
        return arr[skillNum - 1];
    }

    // Returns all 3 skills for a character.
    public static CharSkill[] getAll(String charName) {
        CharSkill[] arr = DB.get(charName);
        if (arr == null) {
            return new CharSkill[0];
        }
        return arr;
    }
}
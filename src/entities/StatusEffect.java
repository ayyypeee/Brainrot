package entities;

import java.awt.Color;

// Enum listing every status effect a character can have during battle.
public enum StatusEffect {

    // Debuffs (negative effects applied to an enemy)
    POISON       ("Poison",       "☠",   new Color(140,  60, 200), false),
    SEVERE_POISON("Sev.Poison",   "💀",  new Color(100,  20, 160), false),
    SILENCE      ("Silenced",     "🔇",  new Color(180, 100,   0), false),
    WEAKNESS     ("Weakness",     "⬇",   new Color(200, 100,  50), false),
    EXHAUSTION   ("Exhaustion",   "💤",  new Color(100, 100, 180), false),
    MIND_MAZE    ("Mind Maze",    "🌀",  new Color(180,  50, 180), false),
    CONFUSE      ("Confused",     "❓",  new Color(200, 160,  20), false),
    FRACTURE     ("Fractured",    "🔩",  new Color(200,  80,  80), false),
    STUN         ("Stunned",      "⚡",  new Color(255, 220,  40), false),
    HEAVY_STUN   ("Heavy Stun",   "⚡⚡", new Color(255, 160,  20), false),

    // Buffs (positive effects applied to yourself)
    DODGE        ("Dodge",        "💨",  new Color( 80, 200, 255), true),
    BLOCK        ("Block",        "🛡",  new Color( 80, 180,  80), true);

    // Each effect has a display name, icon emoji, color for the UI, and a buff flag.
    public String displayName;
    public String icon;
    public Color  colour;
    public boolean isBuff;

    // Enum constructor assigns values to each constant above.
    StatusEffect(String displayName, String icon, Color colour, boolean isBuff) {
        this.displayName = displayName;
        this.icon        = icon;
        this.colour      = colour;
        this.isBuff      = isBuff;
    }
}
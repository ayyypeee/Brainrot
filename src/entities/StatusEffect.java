package entities;

import java.awt.Color;

public enum StatusEffect {

    // ── Debuffs ───────────────────────────────────────────────────────────────
    POISON       ("Poison",       "☠",  new Color(140,  60, 200), false),
    SEVERE_POISON("Sev.Poison",   "💀", new Color(100,  20, 160), false),
    SILENCE      ("Silenced",     "🔇", new Color(180, 100,   0), false),
    WEAKNESS     ("Weakness",     "⬇",  new Color(200, 100,  50), false),
    EXHAUSTION   ("Exhaustion",   "💤", new Color(100, 100, 180), false),
    MIND_MAZE    ("Mind Maze",    "🌀", new Color(180,  50, 180), false),
    CONFUSE      ("Confused",     "❓", new Color(200, 160,  20), false),
    FRACTURE     ("Fractured",    "🔩", new Color(200,  80,  80), false),
    STUN         ("Stunned",      "⚡", new Color(255, 220,  40), false),
    HEAVY_STUN   ("Heavy Stun",   "⚡⚡",new Color(255, 160,  20), false),

    // ── Buffs ─────────────────────────────────────────────────────────────────
    DODGE        ("Dodge",        "💨", new Color( 80, 200, 255), true),
    BLOCK        ("Block",        "🛡", new Color( 80, 180,  80), true),
    BERSERK      ("Berserk",      "🔥", new Color(255,  80,  40), true),
    TRUE_DMG     ("True Dmg",     "✨", new Color(255, 255, 100), true);

    public final String displayName;
    public final String icon;
    public final Color  colour;
    public final boolean isBuff;

    StatusEffect(String displayName, String icon, Color colour, boolean isBuff) {
        this.displayName = displayName;
        this.icon        = icon;
        this.colour      = colour;
        this.isBuff      = isBuff;
    }
}
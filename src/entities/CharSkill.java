package entities;

/** Plain data holder for one skill. */
public class CharSkill {
    public final String name;
    public final int    minDmg, maxDmg;
    public final int    manaCost;      // 0 for skill 1
    public final int    manaRegen;     // 0 for skill 2/3
    public final String passiveDesc;   // shown on hover
    public final int    passiveChance; // percent
    public final String iconPath;      // resource path for the skill icon image

    public CharSkill(String name, int minDmg, int maxDmg,
                     int manaCost, int manaRegen,
                     String passiveDesc, int passiveChance,
                     String iconPath) {
        this.name          = name;
        this.minDmg        = minDmg;
        this.maxDmg        = maxDmg;
        this.manaCost      = manaCost;
        this.manaRegen     = manaRegen;
        this.passiveDesc   = passiveDesc;
        this.passiveChance = passiveChance;
        this.iconPath      = iconPath;
    }

    /** True if this is a basic attack (no cost, generates mana). */
    public boolean isBasic() { return manaRegen > 0; }
}
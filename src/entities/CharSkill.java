package entities;


public class CharSkill {

    public String name;
    public int minDmg;
    public int maxDmg;
    public int manaCost;
    public int manaRegen;
    public String passiveDesc;
    public int passiveChance;
    public String iconPath;

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

    public boolean isBasic() {
        if (manaRegen > 0) {
            return true;
        }
        return false;
    }
}
package Ui;

import java.awt.*;
import java.awt.image.BufferedImage;


public class SkillButton {

    private Rectangle bounds;
    private final String    label;       // skill name from CharSkillDB
    private boolean   hovered;
    private boolean   disabled;
    private final int       skillNum;

    // Optional mana info — set by the battle panels after construction
    private String  manaLabel  = "";     // e.g. "+15 MP" or "-20 MP"
    private boolean isManaRegen = false; // true → blue, false → red/grey

    public SkillButton(int skillNum, String label) {
        this.skillNum = skillNum;
        this.label    = label != null ? label : "Skill " + skillNum;
        this.bounds   = new Rectangle();
    }

    public void setBounds(int x, int y, int w, int h) { bounds.setBounds(x, y, w, h); }
    public boolean   contains(Point p)      { return bounds.contains(p); }
    public void      setHovered(boolean b)  { hovered  = b; }
    public void      setDisabled(boolean b) { disabled = b; }
    public boolean   isDisabled()           { return disabled; }
    public int       getSkillNum()          { return skillNum; }
    public Rectangle getBounds()            { return bounds; }

    public void setManaInfo(String label, boolean isRegen) {
        this.manaLabel   = label  != null ? label : "";
        this.isManaRegen = isRegen;
    }

    public void draw(Graphics2D g2, BufferedImage boxImage, Component obs) {
        int x = bounds.x, y = bounds.y, w = bounds.width, h = bounds.height;

        // ── Background ────────────────────────────────────────────────────────
        if (boxImage != null) {
            if (disabled) {
                Composite orig = g2.getComposite();
                g2.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 0.45f));
                g2.drawImage(boxImage, x, y, w, h, obs);
                g2.setComposite(orig);
            } else {
                g2.drawImage(boxImage, x, y, w, h, obs);
            }
        } else {
            g2.setColor(disabled ? new Color(70, 50, 25) : new Color(120, 80, 40));
            g2.fillRoundRect(x, y, w, h, 14, 14);
            g2.setColor(disabled ? new Color(50, 35, 15) : new Color(80, 50, 20));
            g2.setStroke(new BasicStroke(3));
            g2.drawRoundRect(x, y, w, h, 14, 14);
            g2.setStroke(new BasicStroke(1));
        }

        // ── Hover glow ────────────────────────────────────────────────────────
        if (hovered && !disabled) {
            g2.setColor(new Color(255, 230, 80, 90));
            g2.fillRoundRect(x + 5, y + 5, w - 10, h - 10, 10, 10);
        }

        // ── Disabled overlay ──────────────────────────────────────────────────
        if (disabled) {
            g2.setColor(new Color(0, 0, 0, 100));
            g2.fillRoundRect(x + 4, y + 4, w - 8, h - 8, 10, 10);
        }

        // ── Layout constants ──────────────────────────────────────────────────
        int pad    = (int)(h * 0.10);
        int innerH = h - pad * 2;

        // ── Icon box (left side) ──────────────────────────────────────────────
        int iconSize = (int)(innerH * 0.82);
        int iconX    = x + pad;
        int iconY    = y + (h - iconSize) / 2;

        Color iconBg     = disabled ? new Color(60, 45, 20)  : new Color(180, 130, 60);
        Color iconBorder = disabled ? new Color(80, 60, 30)  : new Color(220, 170, 80);
        g2.setColor(iconBg);
        g2.fillRect(iconX, iconY, iconSize, iconSize);
        g2.setColor(iconBorder);
        g2.setStroke(new BasicStroke(2));
        g2.drawRect(iconX, iconY, iconSize, iconSize);
        g2.setStroke(new BasicStroke(1));

        // Skill number centred inside icon box
        int numFontSize = Math.max(10, (int)(iconSize * 0.55));
        g2.setFont(new Font(Font.MONOSPACED, Font.BOLD, numFontSize));
        g2.setColor(disabled ? new Color(90, 70, 40) : new Color(255, 230, 140));
        String sym = String.valueOf(skillNum);
        FontMetrics pfm = g2.getFontMetrics();
        g2.drawString(sym,
                iconX + (iconSize - pfm.stringWidth(sym)) / 2,
                iconY + (iconSize + pfm.getAscent() - pfm.getDescent()) / 2);

        // ── Text block (right of icon) ────────────────────────────────────────
        int textX  = iconX + iconSize + pad;
        int textW  = (x + w) - textX - pad;

        g2.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING,
                RenderingHints.VALUE_TEXT_ANTIALIAS_ON);

        int headerSize = Math.max(8,  (int)(h * 0.12));
        int nameSize   = Math.max(10, (int)(h * 0.16));
        int manaSize   = Math.max(8,  (int)(h * 0.13));

        int totalTextH = headerSize + 4 + nameSize + 4 + manaSize;
        int textStartY = y + (h - totalTextH) / 2 + headerSize;

        // Row 1: "SKILL N" label
        g2.setFont(new Font(Font.MONOSPACED, Font.BOLD, headerSize));
        g2.setColor(disabled ? new Color(90, 70, 40) : new Color(50, 25, 0));
        g2.drawString("Skill " + skillNum, textX, textStartY);

        // Row 2: Skill name (trim if too wide)
        int row2Y = textStartY + headerSize + 4;
        g2.setFont(new Font(Font.MONOSPACED, Font.BOLD, nameSize));
        g2.setColor(disabled ? new Color(80, 60, 35) : new Color(255, 240, 200));
        FontMetrics nameFm = g2.getFontMetrics();
        String displayName = label;
        while (displayName.length() > 1 && nameFm.stringWidth(displayName) > textW)
            displayName = displayName.substring(0, displayName.length() - 1);
        g2.drawString(displayName, textX, row2Y);

        // Row 3: Mana regen / cost
        int row3Y = row2Y + nameSize + 4;
        g2.setFont(new Font(Font.MONOSPACED, Font.BOLD, manaSize));

        String manaStr = manaLabel.isEmpty()
                ? (skillNum == 1 ? "+15 MP" : "")
                : manaLabel;

        if (!manaStr.isEmpty()) {
            if (disabled && !isManaRegen) {
                g2.setColor(new Color(100, 70, 70));
            } else if (isManaRegen) {
                g2.setColor(new Color(80, 190, 255));   // blue for regen
            } else {
                g2.setColor(new Color(255, 110, 110));  // red for cost
            }
            g2.drawString(manaStr, textX, row3Y);
        }
    }
}
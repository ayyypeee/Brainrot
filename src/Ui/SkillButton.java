package Ui;

import java.awt.*;
import java.awt.image.BufferedImage;

public class SkillButton {

    private Rectangle bounds;
    private String    label;
    private boolean   hovered;
    private boolean   disabled;
    private int       skillNum;

    public SkillButton(int skillNum, String label) {
        this.skillNum = skillNum;
        this.label    = label;
        this.bounds   = new Rectangle();
    }

    public void setBounds(int x, int y, int w, int h) { bounds.setBounds(x, y, w, h); }
    public boolean   contains(Point p)      { return bounds.contains(p); }
    public void      setHovered(boolean b)  { hovered  = b; }
    public void      setDisabled(boolean b) { disabled = b; }
    public int       getSkillNum()          { return skillNum; }
    public Rectangle getBounds()            { return bounds; }

    public void draw(Graphics2D g2, BufferedImage boxImage, Component obs) {
        int x = bounds.x, y = bounds.y, w = bounds.width, h = bounds.height;

        if (boxImage != null) {
            g2.drawImage(boxImage, x, y, w, h, obs);
        } else {
            g2.setColor(new Color(120, 80, 40));
            g2.fillRoundRect(x, y, w, h, 14, 14);
            g2.setColor(new Color(80, 50, 20));
            g2.setStroke(new BasicStroke(3));
            g2.drawRoundRect(x, y, w, h, 14, 14);
            g2.setStroke(new BasicStroke(1));
        }

        // Hover glow
        if (hovered && !disabled) {
            g2.setColor(new Color(255, 230, 80, 90));
            g2.fillRoundRect(x + 5, y + 5, w - 10, h - 10, 10, 10);
        }


        if (disabled) {
            g2.setColor(new Color(0, 0, 0, 110));
            g2.fillRoundRect(x + 4, y + 4, w - 8, h - 8, 10, 10);
        }


        int pad    = (int)(h * 0.12);
        int innerH = h - pad * 2;


        int iconSize = (int)(innerH * 0.90);
        int iconX    = x + pad;
        int iconY    = y + pad + (innerH - iconSize) / 2;

        Color iconBg     = disabled ? new Color(90,  65, 30)  : new Color(180, 130, 60);
        Color iconBorder = disabled ? new Color(110, 85, 40)  : new Color(220, 170, 80);
        g2.setColor(iconBg);
        g2.fillRect(iconX, iconY, iconSize, iconSize);
        g2.setColor(iconBorder);
        g2.setStroke(new BasicStroke(2));
        g2.drawRect(iconX, iconY, iconSize, iconSize);
        g2.setStroke(new BasicStroke(1));

        int pSize = Math.max(10, (int)(iconSize * 0.55));
        g2.setFont(new Font(Font.MONOSPACED, Font.BOLD, pSize));
        g2.setColor(disabled ? new Color(120, 100, 55) : new Color(255, 230, 140));
        String sym = String.valueOf(skillNum);
        FontMetrics pfm = g2.getFontMetrics();
        g2.drawString(sym,
                iconX + (iconSize - pfm.stringWidth(sym)) / 2,
                iconY + (iconSize + pfm.getAscent() - pfm.getDescent()) / 2);


        int textX = iconX + iconSize + pad;
        int textW = (x + w) - textX - pad;

        g2.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING,
                RenderingHints.VALUE_TEXT_ANTIALIAS_ON);


        int titleSize = Math.max(11, (int)(h * 0.18));
        g2.setFont(new Font(Font.MONOSPACED, Font.BOLD, titleSize));
        g2.setColor(disabled ? new Color(120, 100, 55) : new Color(50, 25, 0));
        String title = "Skill " + skillNum + " -";
        int textBlockH = titleSize + 6 + titleSize;
        int textStartY = y + h / 2 - textBlockH / 2 + titleSize;
        g2.drawString(title, textX, textStartY);

        // Skill name
        int labelSize = Math.max(10, (int)(h * 0.15));
        g2.setFont(new Font(Font.MONOSPACED, Font.PLAIN, labelSize));
        g2.setColor(disabled ? new Color(100, 80, 45) : new Color(35, 15, 0));
        FontMetrics fm = g2.getFontMetrics();
        String drawLabel = label != null ? label : "";
        while (drawLabel.length() > 2 && fm.stringWidth(drawLabel) > textW)
            drawLabel = drawLabel.substring(0, drawLabel.length() - 1);
        g2.drawString(drawLabel, textX, textStartY + titleSize + 4);
    }
}
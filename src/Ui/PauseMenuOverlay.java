package Ui;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;

public class PauseMenuOverlay {

    private BufferedImage border, sliderImg, btnImg,
            restartImg, mainMenuImg, exitImg;

    private Rectangle restartRect  = new Rectangle();
    private Rectangle mainMenuRect = new Rectangle();
    private Rectangle exitRect     = new Rectangle();
    private Rectangle trackRect    = new Rectangle();

    private float   volume     = 0.5f;
    private boolean dragging   = false;
    private int     hoveredBtn = 0;
    private long    openedAt   = 0;

    private static final Color SCRIM   = new Color(0, 0, 0, 160);
    private static final Color HOVER_T = new Color(255, 230, 80);

    public PauseMenuOverlay(Class<?> loader) {
        load(loader, "/ui/pause/v4_border.png",          img -> border      = img);
        load(loader, "/ui/pause/v1_slider-export.png",   img -> sliderImg   = img);
        load(loader, "/ui/pause/v1_button-export.png",   img -> btnImg      = img);
        load(loader, "/ui/pause/v1_restart-export.png",  img -> restartImg  = img);
        load(loader, "/ui/pause/v1_mainmenu-export.png", img -> mainMenuImg = img);
        load(loader, "/ui/pause/v1_exit-export.png",     img -> exitImg     = img);
    }

    @FunctionalInterface private interface Setter { void set(BufferedImage img); }
    private void load(Class<?> ldr, String path, Setter s) {
        try { s.set(ImageIO.read(ldr.getResource(path))); }
        catch (Exception e) { System.out.println("PauseMenu: missing " + path); }
    }

    public void reset() {
        openedAt   = System.currentTimeMillis();
        hoveredBtn = 0;
        dragging   = false;
    }

    public float getVolume() { return volume; }

    // ── Draw ──────────────────────────────────────────────────────────────────
    public void draw(Graphics2D g2, int sw, int sh) {
        g2.setColor(SCRIM);
        g2.fillRect(0, 0, sw, sh);

        int panH = (int)(sh * 0.70);
        int panW = (int)(panH * (1407.0 / 1536.0));
        panW = Math.min(panW, (int)(sw * 0.44));
        panH = (int)(panW * (1536.0 / 1407.0));

        double t    = Math.min((System.currentTimeMillis() - openedAt) / 220.0, 1.0);
        double ease = 1 - Math.pow(1 - t, 3);
        int panX = (sw - panW) / 2;
        int panY = (int)((sh - panH) / 2 - panH * 0.22 * (1 - ease));

        int cx = panX + panW / 2;

        if (border != null) {
            g2.drawImage(border, panX, panY, panW, panH, null);
        } else {
            g2.setColor(new Color(10, 45, 45, 235));
            g2.fillRoundRect(panX, panY, panW, panH, 20, 20);
            g2.setColor(new Color(72, 210, 210));
            g2.setStroke(new BasicStroke(2.5f));
            g2.drawRoundRect(panX, panY, panW, panH, 20, 20);
            g2.setStroke(new BasicStroke(1f));
        }

        int innerPadX   = (int)(panW * 0.13);
        int innerPadTop = (int)(panH * 0.09);
        int innerPadBot = (int)(panH * 0.11);

        int innerX = panX + innerPadX;
        int innerY = panY + innerPadTop;
        int innerW = panW - innerPadX * 2;
        int innerH = panH - innerPadTop - innerPadBot;

        // ── Title ─────────────────────────────────────────────────────────────
        int titleFs = Math.max(14, innerW / 9);
        g2.setFont(new Font(Font.MONOSPACED, Font.BOLD, titleFs));
        g2.setColor(new Color(80, 220, 220));
        String title = "PAUSE MENU";
        FontMetrics tfm = g2.getFontMetrics();
        int titleY = innerY + titleFs + (int)(innerH * 0.01);
        g2.setColor(new Color(0, 0, 0, 120));
        g2.drawString(title, cx - tfm.stringWidth(title) / 2 + 2, titleY + 2);
        g2.setColor(new Color(80, 220, 220));
        g2.drawString(title, cx - tfm.stringWidth(title) / 2, titleY);

        int lineY = titleY + (int)(innerH * 0.035);
        g2.setColor(new Color(72, 210, 210, 100));
        g2.setStroke(new BasicStroke(1.5f));
        g2.drawLine(innerX + (int)(innerW * 0.05), lineY,
                innerX + (int)(innerW * 0.95), lineY);
        g2.setStroke(new BasicStroke(1f));

        // ── Volume label ──────────────────────────────────────────────────────
        int volFs = Math.max(10, innerW / 22);
        g2.setFont(new Font(Font.MONOSPACED, Font.BOLD, volFs));
        g2.setColor(new Color(200, 200, 200));
        String volLbl = "Volume";
        FontMetrics vlFm = g2.getFontMetrics();
        int volLabelY = lineY + (int)(innerH * 0.09) + volFs;
        g2.drawString(volLbl, cx - vlFm.stringWidth(volLbl) / 2, volLabelY);

        // ── Slider track ──────────────────────────────────────────────────────
        int sliderW = (int)(innerW * 0.75);
        int sliderH = Math.max(8, (int)(innerH * 0.022));
        int sliderX = cx - sliderW / 2;
        int sliderY = volLabelY + (int)(innerH * 0.04);

        trackRect.setBounds(sliderX, sliderY, sliderW, sliderH);

        g2.setColor(new Color(15, 55, 55, 200));
        g2.fillRoundRect(sliderX, sliderY, sliderW, sliderH, sliderH, sliderH);

        int fillW = Math.max(0, (int)(sliderW * volume));
        if (fillW > 0) {
            g2.setColor(new Color(72, 210, 210, 180));
            g2.fillRoundRect(sliderX, sliderY, fillW, sliderH, sliderH, sliderH);
        }

        if (sliderImg != null) {
            g2.drawImage(sliderImg, sliderX, sliderY, sliderW, sliderH, null);
        }

        g2.setColor(new Color(72, 210, 210, 100));
        g2.setStroke(new BasicStroke(1f));
        g2.drawRoundRect(sliderX, sliderY, sliderW, sliderH, sliderH, sliderH);
        g2.setStroke(new BasicStroke(1f));

        int tR = Math.max(10, sliderH + 5);
        int tX  = sliderX + fillW - tR / 2;
        tX = Math.max(sliderX - tR / 2, Math.min(sliderX + sliderW - tR / 2, tX));
        int tY  = sliderY + sliderH / 2 - tR / 2;

        g2.setColor(new Color(0, 0, 0, 70));
        g2.fillOval(tX + 1, tY + 2, tR, tR);
        g2.setColor(new Color(72, 210, 210));
        g2.fillOval(tX, tY, tR, tR);
        g2.setColor(new Color(200, 255, 255, 160));
        g2.fillOval(tX + tR / 5, tY + tR / 6, tR / 3, tR / 4);
        g2.setColor(Color.WHITE);
        g2.setStroke(new BasicStroke(1.2f));
        g2.drawOval(tX, tY, tR, tR);
        g2.setStroke(new BasicStroke(1f));

        // ── Three buttons ─────────────────────────────────────────────────────
        int btnW = (int)(innerW * 0.80);
        int btnH = Math.max(32, (int)(innerH * 0.145));
        int btnX = cx - btnW / 2;
        int gap  = (int)(innerH * 0.030);

        int fy = sliderY + tR + (int)(innerH * 0.065);

        restartRect .setBounds(btnX, fy,                    btnW, btnH);
        mainMenuRect.setBounds(btnX, fy + (btnH + gap),     btnW, btnH);
        exitRect    .setBounds(btnX, fy + (btnH + gap) * 2, btnW, btnH);

        drawBtn(g2, restartRect,  restartImg,  "Restart",   hoveredBtn == 2);
        drawBtn(g2, mainMenuRect, mainMenuImg, "Main Menu", hoveredBtn == 3);
        drawBtn(g2, exitRect,     exitImg,     "Exit Game", hoveredBtn == 4);

        // ── Hint ──────────────────────────────────────────────────────────────
        int hintFs = Math.max(8, innerW / 34);
        g2.setFont(new Font(Font.MONOSPACED, Font.PLAIN, hintFs));
        g2.setColor(new Color(130, 180, 180, 150));
        String hint = "Press ESC to Resume";
        FontMetrics hFm = g2.getFontMetrics();
        int hintY = innerY + innerH - 4;
        g2.drawString(hint, cx - hFm.stringWidth(hint) / 2, hintY);
    }

    private void drawBtn(Graphics2D g2, Rectangle r, BufferedImage sprite,
                         String label, boolean hovered) {
        int x = r.x, y = r.y, w = r.width, h = r.height;

        if (hovered) {
            g2.setColor(new Color(255, 230, 80, 45));
            g2.fillRoundRect(x - 4, y - 4, w + 8, h + 8, 14, 14);
        }

        if (sprite != null) {
            g2.drawImage(sprite, x, y, w, h, null);
            if (hovered) {
                Composite old = g2.getComposite();
                g2.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 0.18f));
                g2.setColor(Color.WHITE);
                g2.fillRoundRect(x, y, w, h, 10, 10);
                g2.setComposite(old);
            }
        } else {
            if (btnImg != null) {
                g2.drawImage(btnImg, x, y, w, h, null);
            } else {
                GradientPaint gp = new GradientPaint(
                        x, y,     hovered ? new Color(30, 130, 130) : new Color(18, 80, 80),
                        x, y + h, hovered ? new Color(20, 100, 100) : new Color(10, 55, 55));
                g2.setPaint(gp);
                g2.fillRoundRect(x, y, w, h, 10, 10);
                g2.setPaint(null);
                g2.setColor(new Color(72, 210, 210));
                g2.setStroke(new BasicStroke(1.5f));
                g2.drawRoundRect(x, y, w, h, 10, 10);
                g2.setStroke(new BasicStroke(1f));
            }

            int fs = Math.max(11, h / 3);
            g2.setFont(new Font(Font.MONOSPACED, Font.BOLD, fs));
            FontMetrics fm = g2.getFontMetrics();
            int lx = x + (w - fm.stringWidth(label)) / 2;
            int ly = y + (h + fm.getAscent() - fm.getDescent()) / 2;
            g2.setColor(new Color(0, 0, 0, 100));
            g2.drawString(label, lx + 1, ly + 1);
            g2.setColor(hovered ? HOVER_T : Color.WHITE);
            g2.drawString(label, lx, ly);
        }
    }

    // ── Input ─────────────────────────────────────────────────────────────────
    public interface Callbacks {
        void onResume();
        void onRestart();
        void onMainMenu();
        void onExit();
    }

    public void handleHover(Point p) {
        hoveredBtn = 0;
        if      (restartRect.contains(p))  hoveredBtn = 2;
        else if (mainMenuRect.contains(p)) hoveredBtn = 3;
        else if (exitRect.contains(p))     hoveredBtn = 4;
    }

    public void handleClick(Point p, Callbacks cb) {
        if (restartRect.contains(p))  { cb.onRestart();  return; }
        if (mainMenuRect.contains(p)) { cb.onMainMenu(); return; }
        if (exitRect.contains(p))     { cb.onExit();     return; }
        if (trackRect.contains(p))    updateVolume(p.x);
    }

    public void handleMousePressed(Point p) {
        if (trackRect.contains(p)) { dragging = true; updateVolume(p.x); }
    }

    public void handleDrag(Point p) { if (dragging) updateVolume(p.x); }
    public void handleRelease()     { dragging = false; }

    private void updateVolume(int mx) {
        volume = Math.max(0f, Math.min(1f,
                (float)(mx - trackRect.x) / trackRect.width));
        MusicPlayer.setVolume(volume);
    }
}
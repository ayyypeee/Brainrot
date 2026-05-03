package Ui;

import java.awt.*;

public class AboutOverlay {

    private boolean open       = false;
    private long    openedAt   = 0;
    private boolean hoverClose = false;

    private Rectangle closeRect = new Rectangle();

    // ── Colors (same palette as LeaderboardOverlay) ───────────────────────────
    private static final Color SCRIM       = new Color(0, 0, 0, 180);
    private static final Color PANEL_BG    = new Color(6, 12, 28, 245);
    private static final Color BORDER      = new Color(72, 210, 210);
    private static final Color BORDER_DIM  = new Color(40, 120, 120, 120);
    private static final Color TEAL        = new Color(72, 210, 210);
    private static final Color TEXT_WHITE  = Color.WHITE;
    private static final Color TEXT_DIM    = new Color(160, 190, 190);
    private static final Color CLOSE_BG_N  = new Color(90, 20, 20);
    private static final Color CLOSE_BG_H  = new Color(180, 40, 40);
    private static final Color CLOSE_BDR_N = new Color(160, 60, 60);
    private static final Color CLOSE_BDR_H = new Color(255, 80, 80);

    // ── Content ───────────────────────────────────────────────────────────────
    private static final String TITLE    = "GUARDIANS OF SANITY";
    private static final String SUBTITLE = "When internet \"brainrot\" invades the real world,";
    private static final String[] BODY = {
            "ordinary students must fight back!",
            "",
            "Manage your mana, risk it all on powerful",
            "RNG passives, and survive the ultimate",
            "2D turn-based clash of logic vs. absurdity.",
    };

    // ── API ───────────────────────────────────────────────────────────────────
    public void open()      { open = true; openedAt = System.currentTimeMillis(); hoverClose = false; }
    public void close()     { open = false; }
    public boolean isOpen() { return open; }

    public void handleClick(Point p) { if (closeRect.contains(p)) close(); }

    public void handleKey(int keyCode) {
        if (keyCode == java.awt.event.KeyEvent.VK_ESCAPE
                || keyCode == java.awt.event.KeyEvent.VK_ENTER
                || keyCode == java.awt.event.KeyEvent.VK_SPACE) close();
    }

    public void handleHover(Point p) { hoverClose = closeRect.contains(p); }

    // ── Draw ─────────────────────────────────────────────────────────────────
    public void draw(Graphics2D g2, int sw, int sh) {
        if (!open) return;

        // slide-in easing (same as leaderboard)
        double t    = Math.min((System.currentTimeMillis() - openedAt) / 280.0, 1.0);
        double ease = 1 - Math.pow(1 - t, 3);

        // scrim
        g2.setColor(SCRIM);
        g2.fillRect(0, 0, sw, sh);


        int panW = (int)(sw * 0.46);
        panW = Math.max(420, Math.min(panW, 640));
        int panH = (int)(sh * 0.58);
        panH = Math.max(360, Math.min(panH, 520));
        int panX = (sw - panW) / 2;
        int panY = (int)((sh - panH) / 2 - panH * 0.18 * (1 - ease));

        drawPanel(g2, panX, panY, panW, panH);
        drawCorners(g2, panX, panY, panW, panH);

        int cx  = panX + panW / 2;
        int pad = (int)(panW * 0.08);

        // ── Game title ────────────────────────────────────────────────────────
        int titleFs = Math.max(16, (int)(sh * 0.030));
        g2.setFont(new Font(Font.MONOSPACED, Font.BOLD, titleFs));
        FontMetrics tfm = g2.getFontMetrics();
        g2.setColor(TEAL);
        g2.drawString(TITLE, cx - tfm.stringWidth(TITLE) / 2,
                panY + (int)(panH * 0.12) + titleFs);

        // divider under title
        int divY1 = panY + (int)(panH * 0.20);
        drawDivider(g2, panX + pad, panX + panW - pad, divY1);

        // ── Subtitle (italic, dimmer white) ───────────────────────────────────
        int subFs = Math.max(11, (int)(sh * 0.020));
        g2.setFont(new Font(Font.MONOSPACED, Font.PLAIN, subFs));
        FontMetrics sfm = g2.getFontMetrics();
        g2.setColor(new Color(220, 230, 230));
        g2.drawString(SUBTITLE, cx - sfm.stringWidth(SUBTITLE) / 2,
                divY1 + (int)(panH * 0.10));

        // ── Body lines ────────────────────────────────────────────────────────
        int bodyFs   = Math.max(11, (int)(sh * 0.019));
        int lineGap  = bodyFs + (int)(sh * 0.008);
        int bodyStartY = divY1 + (int)(panH * 0.18);

        for (int i = 0; i < BODY.length; i++) {
            String line = BODY[i];
            if (line.isEmpty()) continue;                  // blank = spacer

            g2.setFont(new Font(Font.MONOSPACED, Font.PLAIN, bodyFs));
            FontMetrics bfm = g2.getFontMetrics();
            g2.setColor(TEXT_WHITE);
            g2.drawString(line, cx - bfm.stringWidth(line) / 2,
                    bodyStartY + i * lineGap);
        }

        // divider above close
        int divY2 = panY + panH - (int)(panH * 0.22);
        drawDivider(g2, panX + pad, panX + panW - pad, divY2);

        // ── Close button ──────────────────────────────────────────────────────
        int btnW = (int)(panW * 0.38);
        int btnH = Math.max(30, (int)(sh * 0.046));
        int btnX = cx - btnW / 2;
        int btnY = divY2 + (panY + panH - divY2 - btnH) / 2;
        closeRect.setBounds(btnX, btnY, btnW, btnH);

        g2.setColor(hoverClose ? CLOSE_BG_H : CLOSE_BG_N);
        g2.fillRoundRect(btnX, btnY, btnW, btnH, 10, 10);
        g2.setColor(hoverClose ? CLOSE_BDR_H : CLOSE_BDR_N);
        g2.setStroke(new BasicStroke(1.8f));
        g2.drawRoundRect(btnX, btnY, btnW, btnH, 10, 10);
        g2.setStroke(new BasicStroke(1f));

        int closeLabelFs = Math.max(11, (int)(sh * 0.020));
        g2.setFont(new Font(Font.MONOSPACED, Font.BOLD, closeLabelFs));
        FontMetrics clFm = g2.getFontMetrics();
        String closeLabel = "CLOSE";
        int clx = btnX + (btnW - clFm.stringWidth(closeLabel)) / 2;
        int cly = btnY + (btnH + clFm.getAscent() - clFm.getDescent()) / 2;
        g2.setColor(hoverClose ? TEXT_WHITE : new Color(220, 120, 120));
        g2.drawString(closeLabel, clx, cly);
    }

    // ── Helpers ───────────────────────────────────────────────────────────────
    private void drawPanel(Graphics2D g2, int x, int y, int w, int h) {
        g2.setColor(new Color(0, 0, 0, 110));
        g2.fillRoundRect(x + 6, y + 8, w, h, 18, 18);

        g2.setColor(PANEL_BG);
        g2.fillRoundRect(x, y, w, h, 18, 18);

        g2.setColor(BORDER);
        g2.setStroke(new BasicStroke(2f));
        g2.drawRoundRect(x, y, w, h, 18, 18);

        g2.setColor(BORDER_DIM);
        g2.setStroke(new BasicStroke(1f));
        g2.drawRoundRect(x + 4, y + 4, w - 8, h - 8, 14, 14);
        g2.setStroke(new BasicStroke(1f));
    }

    private void drawCorners(Graphics2D g2, int x, int y, int w, int h) {
        int len = 16;
        g2.setColor(new Color(72, 210, 210, 180));
        g2.setStroke(new BasicStroke(2f));
        g2.drawLine(x + 14, y + 14, x + 14 + len, y + 14);
        g2.drawLine(x + 14, y + 14, x + 14, y + 14 + len);
        g2.drawLine(x + w - 14, y + 14, x + w - 14 - len, y + 14);
        g2.drawLine(x + w - 14, y + 14, x + w - 14, y + 14 + len);
        g2.drawLine(x + 14, y + h - 14, x + 14 + len, y + h - 14);
        g2.drawLine(x + 14, y + h - 14, x + 14, y + h - 14 - len);
        g2.drawLine(x + w - 14, y + h - 14, x + w - 14 - len, y + h - 14);
        g2.drawLine(x + w - 14, y + h - 14, x + w - 14, y + h - 14 - len);
        g2.setStroke(new BasicStroke(1f));
    }

    private void drawDivider(Graphics2D g2, int x1, int x2, int y) {
        g2.setColor(new Color(72, 210, 210, 90));
        g2.setStroke(new BasicStroke(1f));
        g2.drawLine(x1, y, x2, y);
        g2.setStroke(new BasicStroke(1f));
    }
}
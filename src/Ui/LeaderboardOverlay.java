package Ui;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.Rectangle;
import java.util.List;

// Draws a leaderboard panel on top of the game screen showing arcade run times.
public class LeaderboardOverlay {

    private boolean open       = false;
    private long    openedAt   = 0;
    private boolean hoverClose = false;

    // Rectangle used to detect clicks on the Close button.
    private Rectangle closeRect = new Rectangle();

    // Colors used throughout the panel.
    private static final Color SCRIM       = new Color(0, 0, 0, 180);
    private static final Color PANEL_BG    = new Color(6, 12, 28, 245);
    private static final Color BORDER      = new Color(72, 210, 210);
    private static final Color BORDER_DIM  = new Color(40, 120, 120, 120);
    private static final Color TEAL        = new Color(72, 210, 210);
    private static final Color ROW_STRIPE  = new Color(255, 255, 255, 10);
    private static final Color TEXT_WHITE  = Color.WHITE;
    private static final Color TEXT_DIM    = new Color(160, 190, 190);
    private static final Color CLOSE_BG_N  = new Color(90, 20, 20);
    private static final Color CLOSE_BG_H  = new Color(180, 40, 40);
    private static final Color CLOSE_BDR_N = new Color(160, 60, 60);
    private static final Color CLOSE_BDR_H = new Color(255, 80, 80);

    // Opens the leaderboard panel.
    public void open() {
        open     = true;
        openedAt = System.currentTimeMillis();
        hoverClose = false;
    }

    public void close()     { open = false; }
    public boolean isOpen() { return open; }

    public void handleClick(Point p) {
        if (closeRect.contains(p)) {
            close();
        }
    }

    public void handleKey(int keyCode) {
        if (keyCode == java.awt.event.KeyEvent.VK_ESCAPE ||
                keyCode == java.awt.event.KeyEvent.VK_ENTER  ||
                keyCode == java.awt.event.KeyEvent.VK_SPACE) {
            close();
        }
    }

    public void handleHover(Point p) {
        hoverClose = closeRect.contains(p);
    }

    // Draws the full leaderboard overlay onto the screen.
    public void draw(Graphics2D g2, int sw, int sh) {
        if (!open) {
            return;
        }

        // Slide-in animation: panel drops from above over 280ms.
        double t    = Math.min((System.currentTimeMillis() - openedAt) / 280.0, 1.0);
        double ease = 1 - Math.pow(1 - t, 3);

        g2.setColor(SCRIM);
        g2.fillRect(0, 0, sw, sh);

        // Panel size and position.
        int panW = (int)(sw * 0.52);
        if (panW < 480) { panW = 480; }
        if (panW > 720) { panW = 720; }
        int panH = (int)(sh * 0.76);
        int panX = (sw - panW) / 2;
        int panY = (int)((sh - panH) / 2 - panH * 0.18 * (1 - ease));

        drawPanel(g2, panX, panY, panW, panH);
        drawCorners(g2, panX, panY, panW, panH);

        int pad = (int)(panW * 0.07);
        int cx  = panX + panW / 2;

        // Title text.
        int titleFs = Math.max(20, (int)(sh * 0.038));
        g2.setFont(new Font(Font.MONOSPACED, Font.BOLD, titleFs));
        FontMetrics tfm = g2.getFontMetrics();
        String titleStr = "LEADERBOARD";
        g2.setColor(TEAL);
        g2.drawString(titleStr, cx - tfm.stringWidth(titleStr) / 2,
                panY + (int)(panH * 0.10) + titleFs);

        int divY = panY + (int)(panH * 0.17);
        drawDivider(g2, panX + pad, panX + panW - pad, divY);

        // Column x-positions.
        int col1X = panX + pad;
        int col2X = panX + pad + (int)(panW * 0.10);
        int col3X = panX + pad + (int)(panW * 0.42);
        int col4X = panX + panW - pad;

        // Column headers.
        int headerY  = divY + (int)(sh * 0.040);
        int headerFs = Math.max(9, (int)(sh * 0.016));
        g2.setFont(new Font(Font.MONOSPACED, Font.BOLD, headerFs));
        FontMetrics hfm = g2.getFontMetrics();
        g2.setColor(TEAL);
        g2.drawString("#",         col1X, headerY);
        g2.drawString("PLAYER",    col2X, headerY);
        g2.drawString("CHARACTER", col3X, headerY);
        String timeHdr = "TIME";
        g2.drawString(timeHdr, col4X - hfm.stringWidth(timeHdr), headerY);
        drawDivider(g2, panX + pad, panX + panW - pad, headerY + (int)(sh * 0.015));

        // Rows.
        List<ArcadeLeaderboard.Entry> entries = ArcadeLeaderboard.getEntries();
        int rowH      = (int)(sh * 0.058);
        int rowStartY = headerY + (int)(sh * 0.028);
        int maxRows   = entries.size();
        if (maxRows > 6) { maxRows = 6; }

        if (entries.isEmpty()) {
            int emptyFs = Math.max(12, (int)(sh * 0.020));
            g2.setFont(new Font(Font.MONOSPACED, Font.ITALIC, emptyFs));
            g2.setColor(TEXT_DIM);
            String l1 = "No entries yet.";
            String l2 = "Finish an Arcade run to appear here!";
            FontMetrics efm = g2.getFontMetrics();
            int emptyY = rowStartY + rowH * 2;
            g2.drawString(l1, cx - efm.stringWidth(l1) / 2, emptyY);
            g2.setFont(new Font(Font.MONOSPACED, Font.ITALIC, Math.max(10, emptyFs - 2)));
            efm = g2.getFontMetrics();
            g2.drawString(l2, cx - efm.stringWidth(l2) / 2, emptyY + emptyFs + 8);
        } else {
            for (int i = 0; i < maxRows; i++) {
                drawRow(g2, entries.get(i), i,
                        rowStartY + i * rowH, rowH,
                        panX + pad, panX + panW - pad,
                        col1X, col2X, col3X, col4X, sh);
            }
            if (entries.size() > maxRows) {
                int moreFs = Math.max(9, (int)(sh * 0.015));
                g2.setFont(new Font(Font.MONOSPACED, Font.ITALIC, moreFs));
                g2.setColor(TEXT_DIM);
                String more = "+ " + (entries.size() - maxRows) + " more entries...";
                g2.drawString(more, col2X, rowStartY + maxRows * rowH + moreFs + 4);
            }
        }

        // Bottom divider and Close button.
        int botDivY = panY + panH - (int)(panH * 0.18);
        drawDivider(g2, panX + pad, panX + panW - pad, botDivY);

        int btnW = (int)(panW * 0.38);
        int btnH = Math.max(32, (int)(sh * 0.048));
        int btnX = cx - btnW / 2;
        int btnY = botDivY + (panH - (int)(panH * 0.82)) / 2 - btnH / 2 + (int)(panH * 0.02);
        closeRect.setBounds(btnX, btnY, btnW, btnH);

        g2.setColor(hoverClose ? CLOSE_BG_H  : CLOSE_BG_N);
        g2.fillRoundRect(btnX, btnY, btnW, btnH, 10, 10);
        g2.setColor(hoverClose ? CLOSE_BDR_H : CLOSE_BDR_N);
        g2.setStroke(new BasicStroke(1.8f));
        g2.drawRoundRect(btnX, btnY, btnW, btnH, 10, 10);
        g2.setStroke(new BasicStroke(1f));

        int closeLabelFs = Math.max(11, (int)(sh * 0.022));
        g2.setFont(new Font(Font.MONOSPACED, Font.BOLD, closeLabelFs));
        FontMetrics clFm = g2.getFontMetrics();
        String closeLabel = "CLOSE";
        int clx = btnX + (btnW - clFm.stringWidth(closeLabel)) / 2;
        int cly = btnY + (btnH + clFm.getAscent() - clFm.getDescent()) / 2;
        g2.setColor(hoverClose ? TEXT_WHITE : new Color(220, 120, 120));
        g2.drawString(closeLabel, clx, cly);
    }

    // Draws one leaderboard row with rank, name, character, and time.
    private void drawRow(Graphics2D g2, ArcadeLeaderboard.Entry entry, int rank,
                         int rowY, int rowH,
                         int leftEdge, int rightEdge,
                         int col1X, int col2X, int col3X, int col4X, int sh) {
        // Alternating stripe for readability.
        if (rank % 2 == 0) {
            g2.setColor(ROW_STRIPE);
            g2.fillRoundRect(leftEdge, rowY + 2, rightEdge - leftEdge, rowH - 4, 6, 6);
        }

        int fs       = Math.max(12, (int)(sh * 0.021));
        int rowTextY = rowY + (rowH + fs) / 2;

        g2.setFont(new Font(Font.MONOSPACED, Font.BOLD, fs));
        g2.setColor(TEXT_DIM);
        g2.drawString(String.valueOf(rank + 1), col1X + 2, rowTextY);

        g2.setColor(TEXT_WHITE);
        g2.setFont(new Font(Font.MONOSPACED, Font.BOLD, fs));
        g2.drawString(truncate(entry.playerName, 13), col2X, rowTextY);

        g2.setColor(TEAL);
        g2.setFont(new Font(Font.MONOSPACED, Font.PLAIN, fs));
        g2.drawString(truncate(entry.characterName, 13), col3X, rowTextY);

        String time = formatTime(entry.seconds);
        g2.setFont(new Font(Font.MONOSPACED, Font.BOLD, fs));
        g2.setColor(TEXT_WHITE);
        FontMetrics fm = g2.getFontMetrics();
        g2.drawString(time, col4X - fm.stringWidth(time), rowTextY);
    }

    // Draws the rounded panel background and border.
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

    // Draws decorative corner accents on the panel.
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

    // Draws a thin horizontal separator line.
    private void drawDivider(Graphics2D g2, int x1, int x2, int y) {
        g2.setColor(new Color(72, 210, 210, 90));
        g2.setStroke(new BasicStroke(1f));
        g2.drawLine(x1, y, x2, y);
        g2.setStroke(new BasicStroke(1f));
    }

    // Trims a string and adds "…" if it exceeds the max length.
    private String truncate(String s, int max) {
        if (s == null) {
            return "";
        }
        if (s.length() > max) {
            return s.substring(0, max - 1) + "…";
        }
        return s;
    }

    // Formats seconds into mm:ss string (e.g. 125 -> "02:05").
    private String formatTime(long totalSeconds) {
        long m = totalSeconds / 60;
        long s = totalSeconds % 60;
        return String.format("%02d:%02d", m, s);
    }
}
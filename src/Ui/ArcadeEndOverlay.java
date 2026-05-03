package Ui;

import java.awt.*;
import java.awt.event.KeyEvent;

public class ArcadeEndOverlay {

    public interface Callbacks {
        void onPlayAgain();
        void onMainMenu();
    }

    private enum Screen { RESULT, LEADERBOARD }
    private Screen screen = Screen.RESULT;

    private boolean open     = false;
    private long    openedAt = 0;

    private String  resultTitle = "";
    private String  hpLine      = "";
    private String  timeLine    = "";

    private int hoveredBtn = 0;

    private final Rectangle btnA = new Rectangle(); // Play Again
    private final Rectangle btnB = new Rectangle(); // Leaderboard
    private final Rectangle btnC = new Rectangle(); // Main Menu

    private static final Color SCRIM      = new Color(0, 0, 0, 200);
    private static final Color PANEL_BG   = new Color(6, 12, 28, 250);
    private static final Color BORDER     = new Color(72, 210, 210);
    private static final Color BORDER_DIM = new Color(40, 120, 120, 120);
    private static final Color TEAL       = new Color(72, 210, 210);
    private static final Color TEXT_WHITE = Color.WHITE;
    private static final Color TEXT_DIM   = new Color(160, 190, 190);

    private static final Color[][] BTN_COLORS = {
            { new Color(20,90,30),  new Color(40,160,60),  new Color(40,160,60),  new Color(80,255,100),  new Color(120,255,140), TEXT_WHITE },
            { new Color(10,70,80),  new Color(20,130,150), new Color(40,160,180), new Color(72,210,210),  TEAL,                   TEXT_WHITE },
            { new Color(80,20,20),  new Color(160,40,40),  new Color(140,50,50),  new Color(220,70,70),   new Color(220,110,110), TEXT_WHITE },
    };

    private final LeaderboardOverlay lbOverlay = new LeaderboardOverlay();

    // ── API ───────────────────────────────────────────────────────────────────

    public void open(String resultTitle, boolean playerWon, int hpPct, String timeStr) {
        this.resultTitle = resultTitle;
        this.hpLine      = playerWon ? "Survived with " + hpPct + "% HP" : "";
        this.timeLine    = "Time: " + timeStr;
        this.screen      = Screen.RESULT;
        this.open        = true;
        this.openedAt    = System.currentTimeMillis();
        this.hoveredBtn  = 0;
    }

    public boolean isOpen() { return open; }

    public void handleHover(Point p) {
        if (!open) return;
        if (screen == Screen.LEADERBOARD) {
            lbOverlay.handleHover(p);
            hoveredBtn = 0;
            return;
        }
        hoveredBtn = btnA.contains(p) ? 1
                : btnB.contains(p) ? 2
                : btnC.contains(p) ? 3 : 0;
    }

    public void handleClick(Point p, Callbacks cb) {
        if (!open) return;

        if (screen == Screen.LEADERBOARD) {
            // Let the leaderboard handle its own close button
            lbOverlay.handleClick(p);
            // If leaderboard closed itself, go back to result screen
            if (!lbOverlay.isOpen()) {
                screen   = Screen.RESULT;
                openedAt = System.currentTimeMillis();
                hoveredBtn = 0;
            }
            return;
        }

        if (btnA.contains(p)) { open = false; cb.onPlayAgain(); return; }
        if (btnB.contains(p)) { openLeaderboard(); return; }
        if (btnC.contains(p)) { open = false; cb.onMainMenu();  return; }
    }

    public void handleKey(int keyCode, Callbacks cb) {
        if (!open) return;
        if (screen == Screen.LEADERBOARD) {
            lbOverlay.handleKey(keyCode);
            if (!lbOverlay.isOpen()) {
                screen   = Screen.RESULT;
                openedAt = System.currentTimeMillis();
                hoveredBtn = 0;
            }
            return;
        }
        if (keyCode == KeyEvent.VK_ESCAPE) {
            open = false;
            cb.onMainMenu();
        }
    }

    private void openLeaderboard() {
        screen   = Screen.LEADERBOARD;
        openedAt = System.currentTimeMillis();
        hoveredBtn = 0;
        lbOverlay.open();
    }

    // ── Draw ──────────────────────────────────────────────────────────────────

    public void draw(Graphics2D g2, int sw, int sh) {
        if (!open) return;

        double t    = Math.min((System.currentTimeMillis() - openedAt) / 300.0, 1.0);
        double ease = 1 - Math.pow(1 - t, 3);

        g2.setColor(SCRIM);
        g2.fillRect(0, 0, sw, sh);

        if (screen == Screen.LEADERBOARD) {
            lbOverlay.draw(g2, sw, sh);
        } else {
            drawResultScreen(g2, sw, sh, ease);
        }
    }

    // ── Result screen ─────────────────────────────────────────────────────────

    private void drawResultScreen(Graphics2D g2, int sw, int sh, double ease) {

        int panW = clamp((int)(sw * 0.44), 400, 620);
        int panH = clamp((int)(sh * 0.62), 380, 540);
        int panX = (sw - panW) / 2;
        int panY = (int)((sh - panH) / 2 - panH * 0.16 * (1 - ease));

        drawPanel(g2, panX, panY, panW, panH);
        drawCorners(g2, panX, panY, panW, panH);

        int cx  = panX + panW / 2;
        int pad = (int)(panW * 0.09);

        // ── Result title ──────────────────────────────────────────────────────
        int titleFs = clamp((int)(sh * 0.028), 13, 24);
        g2.setFont(new Font(Font.MONOSPACED, Font.BOLD, titleFs));
        FontMetrics tfm = g2.getFontMetrics();
        int maxW = panW - pad * 2;
        String[] titleWords = resultTitle.split(" ");
        java.util.List<String> titleLines = wrapWords(titleWords, tfm, maxW);
        int titleY = panY + (int)(panH * 0.09) + titleFs;
        for (String line : titleLines) {
            g2.setColor(TEAL);
            g2.drawString(line, cx - tfm.stringWidth(line) / 2, titleY);
            titleY += titleFs + 4;
        }

        // ── Divider ───────────────────────────────────────────────────────────
        int divY1 = panY + (int)(panH * 0.26);
        drawDivider(g2, panX + pad, panX + panW - pad, divY1);

        // ── Time line only (small font, compact) ──────────────────────────────
        int infoFs = clamp((int)(sh * 0.018), 10, 15);
        int infoY  = divY1 + infoFs + (int)(sh * 0.010);
        g2.setFont(new Font(Font.MONOSPACED, Font.PLAIN, infoFs));
        FontMetrics ifm = g2.getFontMetrics();

        if (!hpLine.isEmpty()) {
            g2.setColor(new Color(80, 220, 80));
            g2.drawString(hpLine, cx - ifm.stringWidth(hpLine) / 2, infoY);
            infoY += infoFs + (int)(sh * 0.007);
        }

        // Time line
        g2.setColor(TEXT_DIM);
        g2.drawString(timeLine, cx - ifm.stringWidth(timeLine) / 2, infoY);

        // ── Divider above buttons ─────────────────────────────────────────────
        // Push divider closer to the info text so buttons have more room below
        int divY2 = infoY + (int)(sh * 0.020);
        drawDivider(g2, panX + pad, panX + panW - pad, divY2);

        // ── Three buttons — sized to fit inside the panel ─────────────────────
        int btnH   = clamp((int)(sh * 0.056), 34, 48);
        int btnGap = clamp((int)(sh * 0.012), 7, 14);
        int btnW   = panW - pad * 2;
        int bx     = panX + pad;

        // Total height used by three buttons + two gaps
        int totalBtnBlock = btnH * 3 + btnGap * 2;

        int bottomMargin = (int)(panH * 0.04);
        int availableH   = (panY + panH - bottomMargin) - divY2 - (int)(sh * 0.010);


        int by = divY2 + (int)(sh * 0.010) + Math.max(0, (availableH - totalBtnBlock) / 2);

        btnA.setBounds(bx, by,                      btnW, btnH);
        btnB.setBounds(bx, by + (btnH + btnGap),    btnW, btnH);
        btnC.setBounds(bx, by + (btnH + btnGap) * 2, btnW, btnH);

        drawBtn(g2, btnA, "PLAY AGAIN",  BTN_COLORS[0], hoveredBtn == 1, sh);
        drawBtn(g2, btnB, "LEADERBOARD", BTN_COLORS[1], hoveredBtn == 2, sh);
        drawBtn(g2, btnC, "MAIN MENU",   BTN_COLORS[2], hoveredBtn == 3, sh);
    }

    // ── Panel helpers ─────────────────────────────────────────────────────────

    private void drawPanel(Graphics2D g2, int x, int y, int w, int h) {
        g2.setColor(new Color(0, 0, 0, 120));
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
        int len = 14;
        g2.setColor(new Color(72, 210, 210, 180));
        g2.setStroke(new BasicStroke(2f));
        g2.drawLine(x+14, y+14, x+14+len, y+14);     g2.drawLine(x+14, y+14, x+14, y+14+len);
        g2.drawLine(x+w-14, y+14, x+w-14-len, y+14); g2.drawLine(x+w-14, y+14, x+w-14, y+14+len);
        g2.drawLine(x+14, y+h-14, x+14+len, y+h-14); g2.drawLine(x+14, y+h-14, x+14, y+h-14-len);
        g2.drawLine(x+w-14, y+h-14, x+w-14-len, y+h-14); g2.drawLine(x+w-14, y+h-14, x+w-14, y+h-14-len);
        g2.setStroke(new BasicStroke(1f));
    }

    private void drawDivider(Graphics2D g2, int x1, int x2, int y) {
        g2.setColor(new Color(72, 210, 210, 80));
        g2.setStroke(new BasicStroke(1f));
        g2.drawLine(x1, y, x2, y);
    }

    private void drawBtn(Graphics2D g2, Rectangle r,
                         String label, Color[] palette,
                         boolean hovered, int sh) {
        Color bg  = hovered ? palette[1] : palette[0];
        Color bdr = hovered ? palette[3] : palette[2];
        Color txt = hovered ? palette[5] : palette[4];

        g2.setColor(bg);
        g2.fillRoundRect(r.x, r.y, r.width, r.height, 10, 10);
        g2.setColor(bdr);
        g2.setStroke(new BasicStroke(1.8f));
        g2.drawRoundRect(r.x, r.y, r.width, r.height, 10, 10);
        g2.setStroke(new BasicStroke(1f));

        int fs = clamp((int)(sh * 0.020), 10, 17);
        g2.setFont(new Font(Font.MONOSPACED, Font.BOLD, fs));
        FontMetrics fm = g2.getFontMetrics();
        g2.setColor(txt);
        g2.drawString(label,
                r.x + (r.width  - fm.stringWidth(label)) / 2,
                r.y + (r.height + fm.getAscent() - fm.getDescent()) / 2);
    }

    // ── Utilities ─────────────────────────────────────────────────────────────

    private static int clamp(int v, int lo, int hi) {
        return Math.max(lo, Math.min(hi, v));
    }

    private static java.util.List<String> wrapWords(String[] words,
                                                    FontMetrics fm, int maxW) {
        java.util.List<String> lines = new java.util.ArrayList<>();
        StringBuilder cur = new StringBuilder();
        for (String w : words) {
            String test = cur.length() == 0 ? w : cur + " " + w;
            if (fm.stringWidth(test) > maxW && cur.length() > 0) {
                lines.add(cur.toString());
                cur = new StringBuilder(w);
            } else {
                if (cur.length() > 0) cur.append(' ');
                cur.append(w);
            }
        }
        if (cur.length() > 0) lines.add(cur.toString());
        return lines;
    }
}
package Ui;

import entities.Character;
import java.awt.*;
import java.awt.event.*;
import java.awt.image.BufferedImage;
import javax.imageio.ImageIO;
import javax.swing.*;

public class BattlePanel extends JPanel implements MouseListener, MouseMotionListener {

    private JFrame  window;
    private Image   bgImage;

    private Character p1;
    private Character p2;

    private final boolean P1_FACES_RIGHT = true;
    private final boolean P2_FACES_RIGHT = false;

    private String p1Sk1Name, p1Sk2Name, p1Sk3Name;
    private String p2Sk1Name, p2Sk2Name, p2Sk3Name;

    private BufferedImage boxImage;
    private BufferedImage vsImage;
    private BufferedImage platformImage;
    private BufferedImage p1HeadImg;
    private BufferedImage p2HeadImg;

    private final int MAX_HP = 5;
    private int p1HP, p2HP;

    private int  skillTurn = 1;
    private int  round     = 1;
    private boolean skillUsedThisTurn = false;
    private boolean damageDealt       = false;
    private boolean gameOver          = false;
    private String  winner            = "";

    private SkillButton activeBtn1, activeBtn2, activeBtn3;
    private Point mousePos = new Point(0, 0);

    private int headSize, topY, hudFontSize, gameOverFontSize;
    private int btnW, btnH, btnY;

    private int groundY;

    private boolean layoutReady = false;

    private int[] p1Rect1, p1Rect2, p1Rect3;
    private int[] p2Rect1, p2Rect2, p2Rect3;

    private Timer gameTimer;

    public BattlePanel(Character p1, Character p2,
                       String p1HeadPath, String p2HeadPath,
                       String p1Sk1Name, String p1Sk2Name, String p1Sk3Name,
                       String p2Sk1Name, String p2Sk2Name, String p2Sk3Name,
                       int[] p1Rect1, int[] p1Rect2, int[] p1Rect3,
                       int[] p2Rect1, int[] p2Rect2, int[] p2Rect3,
                       Class<?> loader) {

        this.p1 = p1; this.p2 = p2;
        this.p1Sk1Name = p1Sk1Name; this.p1Sk2Name = p1Sk2Name; this.p1Sk3Name = p1Sk3Name;
        this.p2Sk1Name = p2Sk1Name; this.p2Sk2Name = p2Sk2Name; this.p2Sk3Name = p2Sk3Name;
        this.p1Rect1 = p1Rect1; this.p1Rect2 = p1Rect2; this.p1Rect3 = p1Rect3;
        this.p2Rect1 = p2Rect1; this.p2Rect2 = p2Rect2; this.p2Rect3 = p2Rect3;

        p1HP = MAX_HP; p2HP = MAX_HP;

        loadAssets(p1HeadPath, p2HeadPath, loader);
        p1.setImageObserver(this);
        p2.setImageObserver(this);

        setFocusable(true);
        addMouseListener(this);
        addMouseMotionListener(this);

        gameTimer = new Timer(16, e -> update());
        gameTimer.start();

        setupWindow();

        SwingUtilities.invokeLater(() -> {
            calculateLayout();
            placeCharacters();
        });
    }

    // ── Asset loading ─────────────────────────────────────────────────────────
    private void loadAssets(String p1HeadPath, String p2HeadPath, Class<?> loader) {
        try { bgImage = new ImageIcon(loader.getResource("/backgrounds/background.png")).getImage(); }
        catch (Exception e) { System.out.println("BG not found"); }

        try { boxImage = ImageIO.read(loader.getResource("/ui/v1_box_skills.png")); }
        catch (Exception e) { System.out.println("Box not found"); }

        try { vsImage = ImageIO.read(loader.getResource("/ui/Guardians_Of_Sanity__1_.png")); }
        catch (Exception e) {
            try { vsImage = ImageIO.read(loader.getResource("/ui/vs.png")); }
            catch (Exception e2) { System.out.println("VS image not found"); }
        }

        try { platformImage = ImageIO.read(loader.getResource("/level_assets/PLATFORM.png")); }
        catch (Exception e) { System.out.println("Platform not found"); }

        if (p1HeadPath != null)
            try { p1HeadImg = ImageIO.read(loader.getResource(p1HeadPath)); }
            catch (Exception e) { System.out.println("P1 head not found"); }

        if (p2HeadPath != null)
            try { p2HeadImg = ImageIO.read(loader.getResource(p2HeadPath)); }
            catch (Exception e) { System.out.println("P2 head not found"); }
    }

    // ── Layout ────────────────────────────────────────────────────────────────
    private void calculateLayout() {
        int sw = getWidth(), sh = getHeight();

        headSize         = (int)(sh * 0.075);
        topY             = (int)(sh * 0.015);
        hudFontSize      = Math.max(13, (int)(sh * 0.026));
        gameOverFontSize = Math.max(40, (int)(sh * 0.09));

        btnH = (int)(sh * 0.16);
        btnW = (int)(sw * 0.28);
        int gap    = (int)(sw * 0.02);
        int totalW = btnW * 3 + gap * 2;
        int startX = (sw - totalW) / 2;
        btnY = sh - btnH - (int)(sh * 0.02);

        groundY = (int)(sh * 0.60);

        rebuildButtons(startX, gap);
        layoutReady = true;
    }

    private void placeCharacters() {
        int sw = getWidth();

        int p1X = sw / 4     - p1.getWidth() / 2;
        int p2X = sw * 3 / 4 - p2.getWidth() / 2;

        p1.facingRight = P1_FACES_RIGHT;
        p2.facingRight = P2_FACES_RIGHT;

        p1.placeOnPlatform(p1X, groundY);
        p2.placeOnPlatform(p2X, groundY);
    }

    private void rebuildButtons(int startX, int gap) {
        boolean p1Turn = (skillTurn == 1);
        String n1 = p1Turn ? p1Sk1Name : p2Sk1Name;
        String n2 = p1Turn ? p1Sk2Name : p2Sk2Name;
        String n3 = p1Turn ? p1Sk3Name : p2Sk3Name;

        activeBtn1 = new SkillButton(1, n1);
        activeBtn1.setBounds(startX, btnY, btnW, btnH);

        activeBtn2 = new SkillButton(2, n2);
        activeBtn2.setBounds(startX + btnW + gap, btnY, btnW, btnH);

        activeBtn3 = new SkillButton(3, n3);
        activeBtn3.setBounds(startX + (btnW + gap) * 2, btnY, btnW, btnH);
    }

    private void setupWindow() {
        window = new JFrame("Guardians of Sanity BATTLE");
        window.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        window.setUndecorated(true);
        window.add(this);
        GraphicsDevice gd = GraphicsEnvironment.getLocalGraphicsEnvironment().getDefaultScreenDevice();
        if (gd.isFullScreenSupported()) gd.setFullScreenWindow(window);
        else { window.setExtendedState(JFrame.MAXIMIZED_BOTH); window.setVisible(true); }
    }

    // ── Game loop ─────────────────────────────────────────────────────────────
    private void update() {
        if (gameOver) return;

        p1.update();
        p2.update();

        if (!p1.isAnyCastingSkill()) p1.facingRight = P1_FACES_RIGHT;
        if (!p2.isAnyCastingSkill()) p2.facingRight = P2_FACES_RIGHT;

        if (skillUsedThisTurn && !damageDealt) {
            if (skillTurn == 1 && (p1.isCastingSkill1() || p1.isCastingSkill2() || p1.isCastingSkill3())) {
                p2HP--; damageDealt = true; checkGameOver();
            }
            if (skillTurn == 2 && (p2.isCastingSkill1() || p2.isCastingSkill2() || p2.isCastingSkill3())) {
                p1HP--; damageDealt = true; checkGameOver();
            }
        }

        if (skillUsedThisTurn && damageDealt) {
            if (skillTurn == 1 && !p1.isAnyCastingSkill()) {
                skillTurn = 2; skillUsedThisTurn = false; damageDealt = false; switchButtons();
            } else if (skillTurn == 2 && !p2.isAnyCastingSkill()) {
                skillTurn = 1; skillUsedThisTurn = false; damageDealt = false; round++; switchButtons();
            }
        }

        updateButtonStates();
        repaint();
    }

    private void switchButtons() {
        if (!layoutReady) return;
        int sw  = getWidth();
        int gap = (int)(sw * 0.02);
        rebuildButtons((sw - btnW * 3 - gap * 2) / 2, gap);
    }

    private void updateButtonStates() {
        if (!layoutReady || activeBtn1 == null) return;
        boolean animating = p1.isAnyCastingSkill() || p2.isAnyCastingSkill();
        boolean p1Turn    = skillTurn == 1;
        activeBtn1.setDisabled(animating || skillUsedThisTurn || !(p1Turn ? p1.hasSkill1() : p2.hasSkill1()));
        activeBtn2.setDisabled(animating || skillUsedThisTurn || !(p1Turn ? p1.hasSkill2() : p2.hasSkill2()));
        activeBtn3.setDisabled(animating || skillUsedThisTurn || !(p1Turn ? p1.hasSkill3() : p2.hasSkill3()));
        activeBtn1.setHovered(activeBtn1.getBounds().contains(mousePos));
        activeBtn2.setHovered(activeBtn2.getBounds().contains(mousePos));
        activeBtn3.setHovered(activeBtn3.getBounds().contains(mousePos));
    }

    // ── Game over ─────────────────────────────────────────────────────────────
    private void checkGameOver() {
        if (p1HP <= 0) { p1HP = 0; gameOver = true; winner = "PLAYER 2 WINS!"; end(); }
        else if (p2HP <= 0) { p2HP = 0; gameOver = true; winner = "PLAYER 1 WINS!"; end(); }
    }

    private void end() {
        gameTimer.stop();
        repaint();
        new Timer(1500, e -> {
            int choice = JOptionPane.showConfirmDialog(window,
                    winner + "\n\nPlay again?", "Game Over",
                    JOptionPane.YES_NO_OPTION, JOptionPane.PLAIN_MESSAGE);
            if (choice == JOptionPane.YES_OPTION) { window.dispose(); new CharacterSelect(); }
            else System.exit(0);
        }) {{ setRepeats(false); start(); }};
    }

    // ── Painting ──────────────────────────────────────────────────────────────
    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2 = (Graphics2D) g;
        g2.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
        g2.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);

        if (!layoutReady) { calculateLayout(); placeCharacters(); }

        int sw = getWidth(), sh = getHeight();

        if (bgImage != null) g2.drawImage(bgImage, 0, 0, sw, sh, this);
        else { g2.setColor(new Color(20, 40, 20)); g2.fillRect(0, 0, sw, sh); }

        drawGround(g2, sw, sh);

        p1.draw(g2, this);
        p2.draw(g2, this);

        drawSkillButtons(g2);

        drawTopHUD(g2);

        if (gameOver) drawGameOverOverlay(g2);
    }

    // ── Ground ────────────────────────────────────────────────────────────────
    private void drawGround(Graphics2D g2, int sw, int sh) {
        if (platformImage != null) {
            g2.drawImage(platformImage,
                    0, groundY, sw, sh,
                    0, 0, platformImage.getWidth(), platformImage.getHeight(),
                    this);
        } else {
            g2.setColor(new Color(100, 130, 60));
            g2.fillRect(0, groundY, sw, (int)((sh - groundY) * 0.15));
            g2.setColor(new Color(120, 80, 35));
            g2.fillRect(0, groundY + (int)((sh - groundY) * 0.15), sw, sh - groundY);
            g2.setColor(new Color(40, 55, 15));
            g2.setStroke(new BasicStroke(3f));
            g2.drawLine(0, groundY, sw, groundY);
            g2.setStroke(new BasicStroke(1f));
        }
    }

    // ── Top HUD ───────────────────────────────────────────────────────────────
    private void drawTopHUD(Graphics2D g2) {
        int sw = getWidth(), sh = getHeight();

        int barH    = (int)(sh * 0.028);
        int headGap = (int)(sw * 0.010);
        int barMaxW = (int)(sw * 0.315);
        int nameY   = topY + (int)(headSize * 0.55);
        int barY    = nameY + (int)(sh * 0.01);

        int p1HeadX = (int)(sw * 0.01);
        if (p1HeadImg != null) g2.drawImage(p1HeadImg, p1HeadX, topY, headSize, headSize, this);
        else { g2.setColor(new Color(100, 200, 255)); g2.fillRect(p1HeadX, topY, headSize, headSize); }
        g2.setFont(new Font(Font.MONOSPACED, Font.BOLD, hudFontSize));
        g2.setColor(skillTurn == 1 ? new Color(255, 200, 80) : Color.WHITE);
        g2.drawString("player 1", p1HeadX + headSize + headGap, nameY);
        drawHealthBar(g2, p1HeadX + headSize + headGap, barY, barMaxW, barH, p1HP, MAX_HP, true);

        int p2HeadX = sw - (int)(sw * 0.01) - headSize;
        if (p2HeadImg != null) g2.drawImage(p2HeadImg, p2HeadX, topY, headSize, headSize, this);
        else { g2.setColor(new Color(255, 150, 100)); g2.fillRect(p2HeadX, topY, headSize, headSize); }
        g2.setFont(new Font(Font.MONOSPACED, Font.BOLD, hudFontSize));
        g2.setColor(skillTurn == 2 ? new Color(255, 200, 80) : Color.WHITE);
        String p2name  = "player 2";
        int p2BarRight = p2HeadX - headGap;
        int p2BarLeft  = p2BarRight - barMaxW;
        g2.drawString(p2name, p2BarRight - g2.getFontMetrics().stringWidth(p2name), nameY);
        drawHealthBar(g2, p2BarLeft, barY, barMaxW, barH, p2HP, MAX_HP, false);

        int cx = sw / 2;
        int roundFS = Math.max(16, (int)(sh * 0.032));
        g2.setFont(new Font(Font.MONOSPACED, Font.BOLD, roundFS));
        g2.setColor(Color.WHITE);
        String roundStr = "ROUND  " + round;
        g2.drawString(roundStr, cx - g2.getFontMetrics().stringWidth(roundStr) / 2, topY + roundFS);
        int vsSize = (int)(sh * 0.065);
        if (vsImage != null)
            g2.drawImage(vsImage, cx - vsSize / 2, topY + roundFS + 2, vsSize, vsSize, this);
        else {
            g2.setColor(new Color(220, 50, 50));
            g2.setFont(new Font(Font.MONOSPACED, Font.BOLD, (int)(sh * 0.04)));
            g2.drawString("VS", cx - 18, topY + headSize / 2 + 12);
        }

        if (!gameOver && !p1.isAnyCastingSkill() && !p2.isAnyCastingSkill()) {
            String lbl   = skillTurn == 1 ? "PLAYER 1\nTURN" : "PLAYER 2\nTURN";
            Color  color = skillTurn == 1 ? new Color(100, 180, 255) : new Color(255, 80, 80);
            drawCenteredText(g2, lbl, color, sw, sh);
        }
    }

    private void drawHealthBar(Graphics2D g2, int x, int y, int maxW, int barH,
                               int hp, int maxHp, boolean leftToRight) {
        g2.setColor(new Color(60, 0, 0));
        g2.fillRect(x, y, maxW, barH);
        int fillW = (int)(maxW * (double) hp / maxHp);
        if (fillW > 0) {
            float ratio = (float) hp / maxHp;
            g2.setColor(ratio > 0.6f ? new Color(60, 210, 60)
                    : ratio > 0.3f  ? new Color(230, 210, 40)
                    :                  new Color(230, 50, 50));
            g2.fillRect(leftToRight ? x : x + maxW - fillW, y, fillW, barH);
        }
        g2.setColor(Color.WHITE);
        g2.setStroke(new BasicStroke(2f));
        g2.drawRect(x, y, maxW, barH);
        g2.setStroke(new BasicStroke(1f));
    }

    private void drawCenteredText(Graphics2D g2, String text, Color color, int sw, int sh) {
        String[] lines = text.split("\n");
        int fs = Math.max(22, (int)(sh * 0.048));
        g2.setFont(new Font(Font.MONOSPACED, Font.BOLD, fs));
        g2.setColor(color);
        int startY = sh / 2 - (lines.length * (fs + 6)) / 2;
        for (int i = 0; i < lines.length; i++) {
            int lw = g2.getFontMetrics().stringWidth(lines[i]);
            g2.drawString(lines[i], sw / 2 - lw / 2, startY + i * (fs + 6) + fs);
        }
    }

    // ── Skill buttons ─────────────────────────────────────────────────────────
    private void drawSkillButtons(Graphics2D g2) {
        if (!layoutReady || activeBtn1 == null) return;
        activeBtn1.draw(g2, boxImage, this);
        activeBtn2.draw(g2, boxImage, this);
        activeBtn3.draw(g2, boxImage, this);
    }

    private void drawGameOverOverlay(Graphics2D g2) {
        g2.setColor(new Color(0, 0, 0, 160));
        g2.fillRect(0, 0, getWidth(), getHeight());
        g2.setFont(new Font(Font.MONOSPACED, Font.BOLD, gameOverFontSize));
        g2.setColor(Color.YELLOW);
        int tw = g2.getFontMetrics().stringWidth(winner);
        g2.drawString(winner, getWidth() / 2 - tw / 2, getHeight() / 2);
    }

    // ── Mouse ─────────────────────────────────────────────────────────────────
    @Override
    public void mouseClicked(MouseEvent e) {
        if (gameOver || !layoutReady || activeBtn1 == null) return;
        if (skillUsedThisTurn || p1.isAnyCastingSkill() || p2.isAnyCastingSkill()) return;

        Point pt  = e.getPoint();
        int p1CX  = p1.getX() + p1.getWidth() / 2;
        int p2CX  = p2.getX() + p2.getWidth() / 2;

        if (skillTurn == 1) {
            if      (activeBtn1.contains(pt) && p1.hasSkill1()) { p1.startAttack(1, p2CX); skillUsedThisTurn = true; }
            else if (activeBtn2.contains(pt) && p1.hasSkill2()) { p1.startAttack(2, p2CX); skillUsedThisTurn = true; }
            else if (activeBtn3.contains(pt) && p1.hasSkill3()) { p1.startAttack(3, p2CX); skillUsedThisTurn = true; }
        } else {
            if      (activeBtn1.contains(pt) && p2.hasSkill1()) { p2.startAttack(1, p1CX); skillUsedThisTurn = true; }
            else if (activeBtn2.contains(pt) && p2.hasSkill2()) { p2.startAttack(2, p1CX); skillUsedThisTurn = true; }
            else if (activeBtn3.contains(pt) && p2.hasSkill3()) { p2.startAttack(3, p1CX); skillUsedThisTurn = true; }
        }
    }

    @Override
    public void mouseMoved(MouseEvent e) {
        mousePos = e.getPoint();
        if (!layoutReady || activeBtn1 == null) return;
        boolean onBtn = activeBtn1.getBounds().contains(mousePos)
                || activeBtn2.getBounds().contains(mousePos)
                || activeBtn3.getBounds().contains(mousePos);
        setCursor(onBtn ? Cursor.getPredefinedCursor(Cursor.HAND_CURSOR) : Cursor.getDefaultCursor());
    }

    @Override public void mousePressed(MouseEvent e)  {}
    @Override public void mouseReleased(MouseEvent e) {}
    @Override public void mouseEntered(MouseEvent e)  {}
    @Override public void mouseExited(MouseEvent e)   {}
    @Override public void mouseDragged(MouseEvent e)  {}
}
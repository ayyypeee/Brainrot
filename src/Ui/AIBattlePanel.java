package Ui;

import entities.Character;
import java.awt.*;
import java.awt.event.*;
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import javax.imageio.ImageIO;
import javax.swing.*;

public class AIBattlePanel extends JPanel implements MouseListener, MouseMotionListener {

    private final Character player;
    private final Character ai;

    private final boolean PLAYER_FACES_RIGHT = true;
    private final boolean AI_FACES_RIGHT     = false;

    private String plSk1Name, plSk2Name, plSk3Name;
    private String aiSk1Name, aiSk2Name, aiSk3Name;

    private Image         bgImage;
    private BufferedImage boxImage, vsImage, platformImage;
    private BufferedImage playerHeadImg, aiHeadImg;

    private final String playerName;
    private final String aiName;

    private final int MAX_HP = 5;
    private int playerHP = MAX_HP, aiHP = MAX_HP;

    private boolean playerTurn        = true;
    private int     round             = 1;
    private boolean skillUsedThisTurn = false;
    private boolean damageDealt       = false;
    private boolean gameOver          = false;
    private String  winner            = "";

    private Timer aiDelayTimer;

    private int groundY, headSize, topY, hudFontSize, gameOverFontSize;
    private int btnW, btnH, btnY;
    private boolean layoutReady = false;

    private SkillButton btn1, btn2, btn3;
    private Point mousePos = new Point();

    private int[] plRect1, plRect2, plRect3;
    private int[] aiRect1, aiRect2, aiRect3;

    private JFrame window;
    private Timer  gameTimer;

    // ── Constructor ───────────────────────────────────────────────────────────

    public AIBattlePanel(
            Character player,   Character ai,
            String playerHead,  String aiHead,
            String plSk1, String plSk2, String plSk3,
            String aiSk1, String aiSk2, String aiSk3,
            int[] plR1, int[] plR2, int[] plR3,
            int[] aiR1, int[] aiR2, int[] aiR3,
            String playerName, String aiName,
            Class<?> loader) {

        this.player = player; this.ai = ai;
        this.plSk1Name = plSk1; this.plSk2Name = plSk2; this.plSk3Name = plSk3;
        this.aiSk1Name = aiSk1; this.aiSk2Name = aiSk2; this.aiSk3Name = aiSk3;
        this.plRect1 = plR1; this.plRect2 = plR2; this.plRect3 = plR3;
        this.aiRect1 = aiR1; this.aiRect2 = aiR2; this.aiRect3 = aiR3;
        this.playerName = playerName; this.aiName = aiName;

        loadAssets(playerHead, aiHead, loader);
        player.setImageObserver(this);
        ai.setImageObserver(this);

        setFocusable(true);
        addMouseListener(this);
        addMouseMotionListener(this);

        gameTimer = new Timer(16, e -> update());
        gameTimer.start();

        setupWindow();
        SwingUtilities.invokeLater(() -> { calculateLayout(); placeCharacters(); });
    }

    // ── Assets ────────────────────────────────────────────────────────────────

    private void loadAssets(String playerHead, String aiHead, Class<?> loader) {
        try { bgImage       = new ImageIcon(loader.getResource("/backgrounds/background.png")).getImage(); } catch (Exception e) {}
        try { boxImage      = ImageIO.read(loader.getResource("/ui/v1_box_skills.png")); } catch (Exception e) {}
        try { vsImage       = ImageIO.read(loader.getResource("/ui/Guardians_Of_Sanity__1_.png")); } catch (Exception e) {
            try { vsImage   = ImageIO.read(loader.getResource("/ui/vs.png")); } catch (Exception e2) {}
        }
        try { platformImage = ImageIO.read(loader.getResource("/level_assets/PLATFORM.png")); } catch (Exception e) {}
        if (playerHead != null) try { playerHeadImg = ImageIO.read(loader.getResource(playerHead)); } catch (Exception e) {}
        if (aiHead     != null) try { aiHeadImg     = ImageIO.read(loader.getResource(aiHead));     } catch (Exception e) {}
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

        player.facingRight = PLAYER_FACES_RIGHT;
        ai.facingRight     = AI_FACES_RIGHT;

        int plX = sw / 4     - player.getWidth() / 2;
        int aiX = sw * 3 / 4 - ai.getWidth()     / 2;

        player.placeOnPlatform(plX, groundY);
        ai.placeOnPlatform(aiX, groundY);
    }

    private void rebuildButtons(int startX, int gap) {
        btn1 = new SkillButton(1, plSk1Name);
        btn1.setBounds(startX, btnY, btnW, btnH);
        btn2 = new SkillButton(2, plSk2Name);
        btn2.setBounds(startX + btnW + gap, btnY, btnW, btnH);
        btn3 = new SkillButton(3, plSk3Name);
        btn3.setBounds(startX + (btnW + gap) * 2, btnY, btnW, btnH);
    }

    private void setupWindow() {
        window = new JFrame("Guardians of Sanity vs Computer");
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

        player.update();
        ai.update();

        if (!player.isAnyCastingSkill()) player.facingRight = PLAYER_FACES_RIGHT;
        if (!ai.isAnyCastingSkill())     ai.facingRight     = AI_FACES_RIGHT;

        if (skillUsedThisTurn && !damageDealt) {
            if (playerTurn && (player.isCastingSkill1() || player.isCastingSkill2() || player.isCastingSkill3())) {
                aiHP--; damageDealt = true; checkGameOver();
            }
            if (!playerTurn && (ai.isCastingSkill1() || ai.isCastingSkill2() || ai.isCastingSkill3())) {
                playerHP--; damageDealt = true; checkGameOver();
            }
        }

        if (skillUsedThisTurn && damageDealt) {
            if (playerTurn && !player.isAnyCastingSkill()) {
                playerTurn = false; skillUsedThisTurn = false; damageDealt = false;
                scheduleAITurn();
            } else if (!playerTurn && !ai.isAnyCastingSkill()) {
                playerTurn = true; skillUsedThisTurn = false; damageDealt = false; round++;
            }
        }

        updateButtonStates();
        repaint();
    }

    // ── AI logic ──────────────────────────────────────────────────────────────

    private void scheduleAITurn() {
        if (gameOver) return;
        aiDelayTimer = new Timer(1200, e -> {
            if (!gameOver && !ai.isAnyCastingSkill()) {
                int skill   = pickAISkill();
                int plCentX = player.getX() + player.getWidth() / 2;
                ai.startAttack(skill, plCentX);
                skillUsedThisTurn = true;
            }
            ((Timer) e.getSource()).stop();
        });
        aiDelayTimer.setRepeats(false);
        aiDelayTimer.start();
    }

    private int pickAISkill() {
        ArrayList<Integer> avail = new ArrayList<>();
        if (ai.hasSkill1()) avail.add(1);
        if (ai.hasSkill2()) avail.add(2);
        if (ai.hasSkill3()) avail.add(3);
        if (avail.isEmpty()) return 1;
        return avail.get(new java.util.Random().nextInt(avail.size()));
    }

    // ── Game over ─────────────────────────────────────────────────────────────

    private void checkGameOver() {
        if (aiHP <= 0) {
            aiHP = 0; gameOver = true;
            winner = playerName.toUpperCase() + " YOU WIN!";
            end();
        } else if (playerHP <= 0) {
            playerHP = 0; gameOver = true;
            winner = aiName.toUpperCase() + " (AI) WIN!";
            end();
        }
    }

    private void end() {
        gameTimer.stop();
        if (aiDelayTimer != null) aiDelayTimer.stop();
        repaint();
        new Timer(1500, e -> {
            int choice = JOptionPane.showConfirmDialog(window,
                    winner + "\n\nPlay again?", "Game Over",
                    JOptionPane.YES_NO_OPTION, JOptionPane.PLAIN_MESSAGE);
            if (choice == JOptionPane.YES_OPTION) { window.dispose(); new LevelSelect(); }
            else System.exit(0);
        }) {{ setRepeats(false); start(); }};
    }

    // ── Button states ─────────────────────────────────────────────────────────

    private void updateButtonStates() {
        if (!layoutReady || btn1 == null) return;
        boolean animating = player.isAnyCastingSkill() || ai.isAnyCastingSkill();
        boolean disabled  = animating || skillUsedThisTurn || !playerTurn;
        btn1.setDisabled(disabled || !player.hasSkill1());
        btn2.setDisabled(disabled || !player.hasSkill2());
        btn3.setDisabled(disabled || !player.hasSkill3());
        btn1.setHovered(btn1.getBounds().contains(mousePos));
        btn2.setHovered(btn2.getBounds().contains(mousePos));
        btn3.setHovered(btn3.getBounds().contains(mousePos));
    }

    // ── Paint ─────────────────────────────────────────────────────────────────

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        if (!layoutReady) { calculateLayout(); placeCharacters(); }
        Graphics2D g2 = (Graphics2D) g;
        g2.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
        g2.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);

        int sw = getWidth(), sh = getHeight();

        if (bgImage != null) g2.drawImage(bgImage, 0, 0, sw, sh, this);
        else { g2.setColor(new Color(20, 40, 20)); g2.fillRect(0, 0, sw, sh); }

        drawGround(g2, sw, sh);
        player.draw(g2, this);
        ai.draw(g2, this);
        drawSkillButtons(g2);
        drawTopHUD(g2);
        if (gameOver) drawGameOverOverlay(g2);
    }

    private void drawGround(Graphics2D g2, int sw, int sh) {
        if (platformImage != null) {
            g2.drawImage(platformImage, 0, groundY, sw, sh,
                    0, 0, platformImage.getWidth(), platformImage.getHeight(), this);
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

    private void drawTopHUD(Graphics2D g2) {
        int sw = getWidth(), sh = getHeight();
        int barH    = (int)(sh * 0.028);
        int headGap = (int)(sw * 0.010);
        int barMaxW = (int)(sw * 0.315);
        int nameY   = topY + (int)(headSize * 0.55);
        int barY    = nameY + (int)(sh * 0.01);

        int plHeadX = (int)(sw * 0.01);
        if (playerHeadImg != null) g2.drawImage(playerHeadImg, plHeadX, topY, headSize, headSize, this);
        else { g2.setColor(new Color(100, 200, 255)); g2.fillRect(plHeadX, topY, headSize, headSize); }
        g2.setFont(new Font(Font.MONOSPACED, Font.BOLD, hudFontSize));
        g2.setColor(playerTurn ? new Color(255, 200, 80) : Color.WHITE);
        g2.drawString(playerName, plHeadX + headSize + headGap, nameY);
        drawHealthBar(g2, plHeadX + headSize + headGap, barY, barMaxW, barH, playerHP, MAX_HP, true);

        int aiHeadX = sw - (int)(sw * 0.01) - headSize;
        if (aiHeadImg != null) g2.drawImage(aiHeadImg, aiHeadX, topY, headSize, headSize, this);
        else { g2.setColor(new Color(255, 100, 100)); g2.fillRect(aiHeadX, topY, headSize, headSize); }
        g2.setFont(new Font(Font.MONOSPACED, Font.BOLD, hudFontSize));
        g2.setColor(!playerTurn ? new Color(255, 200, 80) : Color.WHITE);
        String aiLabel = "AI  " + aiName;
        int p2BarRight = aiHeadX - headGap;
        int p2BarLeft  = p2BarRight - barMaxW;
        g2.drawString(aiLabel, p2BarRight - g2.getFontMetrics().stringWidth(aiLabel), nameY);
        drawHealthBar(g2, p2BarLeft, barY, barMaxW, barH, aiHP, MAX_HP, false);

        int cx = sw / 2;
        int roundFS = Math.max(16, (int)(sh * 0.032));
        g2.setFont(new Font(Font.MONOSPACED, Font.BOLD, roundFS));
        g2.setColor(Color.WHITE);
        String roundStr = "ROUND  " + round;
        g2.drawString(roundStr, cx - g2.getFontMetrics().stringWidth(roundStr) / 2, topY + roundFS);
        int vsSize = (int)(sh * 0.065);
        if (vsImage != null)
            g2.drawImage(vsImage, cx - vsSize / 2, topY + roundFS + 2, vsSize, vsSize, this);

        if (!gameOver && !player.isAnyCastingSkill() && !ai.isAnyCastingSkill()) {
            String lbl   = playerTurn ? "YOUR TURN\nChoose a skill!" : aiName.toUpperCase() + "\nTHINKING...";
            Color  color = playerTurn ? new Color(100, 180, 255) : new Color(255, 80, 80);
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

    private void drawSkillButtons(Graphics2D g2) {
        if (!layoutReady || btn1 == null) return;
        btn1.draw(g2, boxImage, this);
        btn2.draw(g2, boxImage, this);
        btn3.draw(g2, boxImage, this);
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
        if (gameOver || !layoutReady || btn1 == null) return;
        if (!playerTurn || skillUsedThisTurn || player.isAnyCastingSkill() || ai.isAnyCastingSkill()) return;

        Point pt    = e.getPoint();
        int aiCentX = ai.getX() + ai.getWidth() / 2;

        if      (btn1.contains(pt) && player.hasSkill1()) { player.startAttack(1, aiCentX); skillUsedThisTurn = true; }
        else if (btn2.contains(pt) && player.hasSkill2()) { player.startAttack(2, aiCentX); skillUsedThisTurn = true; }
        else if (btn3.contains(pt) && player.hasSkill3()) { player.startAttack(3, aiCentX); skillUsedThisTurn = true; }
    }

    @Override
    public void mouseMoved(MouseEvent e) {
        mousePos = e.getPoint();
        if (!layoutReady || btn1 == null) return;
        boolean onBtn = btn1.getBounds().contains(mousePos)
                || btn2.getBounds().contains(mousePos)
                || btn3.getBounds().contains(mousePos);
        setCursor(onBtn ? Cursor.getPredefinedCursor(Cursor.HAND_CURSOR) : Cursor.getDefaultCursor());
    }

    @Override public void mousePressed(MouseEvent e)  {}
    @Override public void mouseReleased(MouseEvent e) {}
    @Override public void mouseEntered(MouseEvent e)  {}
    @Override public void mouseExited(MouseEvent e)   {}
    @Override public void mouseDragged(MouseEvent e)  {}
}
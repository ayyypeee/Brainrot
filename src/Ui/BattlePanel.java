package Ui;

import entities.Character;
import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.awt.image.BufferedImage;
import javax.imageio.ImageIO;

public class BattlePanel extends JPanel implements KeyListener {

    private JFrame window;
    private Image bgImage;
    private Character p1;
    private Character p2;
    private Timer gameTimer;
    private BufferedImage heartSprite;
    private BufferedImage p1HeadImg;
    private BufferedImage p2HeadImg;

    private final int MAX_HP = 5;
    private int p1HP;
    private int p2HP;
    private int skillTurn;
    private boolean skillUsedThisTurn;
    private boolean damageDealt;
    private boolean gameOver;
    private String winner;

    private boolean p1Left, p1Right, p2Left, p2Right;

    private int headSize;
    private int heartSize;
    private int heartGap;
    private int topY;
    private int hudFontSize;
    private int bottomFontSize;
    private int gameOverFontSize;

    public BattlePanel(Character p1, Character p2,
                       String p1HeadPath, String p2HeadPath,
                       Class<?> loader) {
        this.p1 = p1;
        this.p2 = p2;
        p1HP = MAX_HP; p2HP = MAX_HP;
        skillTurn = 1;
        skillUsedThisTurn = false;
        damageDealt = false;
        gameOver = false;
        winner = "";
        p1Left = p1Right = p2Left = p2Right = false;

        loadBackground(loader);
        loadHeartSprite(loader);
        loadHeadPortrait(p1HeadPath, true, loader);
        loadHeadPortrait(p2HeadPath, false, loader);

        p1.setImageObserver(this);
        p2.setImageObserver(this);
        setFocusable(true);
        addKeyListener(this);

        gameTimer = new Timer(16, new ActionListener() {
            public void actionPerformed(ActionEvent e) { update(); }
        });
        gameTimer.start();

        setupWindow();

        SwingUtilities.invokeLater(new Runnable() {
            public void run() {
                calculateDimensions();
                int w = getWidth();
                int h = getHeight();
                p1.placeAtBottom(w, h);
                p2.placeAtBottom(w, h);
                p1.setX(w / 4 - p1.getWidth() / 2);
                p2.setX(w * 3 / 4 - p2.getWidth() / 2);
                p1.facingRight = true;
                p2.facingRight = false;
            }
        });

        requestFocusInWindow();
    }

    private void calculateDimensions() {
        int sw = getWidth();
        int sh = getHeight();
        headSize       = (int)(sh * 0.07);
        heartSize      = (int)(sh * 0.04);
        heartGap       = Math.max(2, (int)(sw * 0.004));
        topY           = (int)(sh * 0.018);
        hudFontSize    = Math.max(12, (int)(sh * 0.022));
        bottomFontSize = Math.max(10, (int)(sh * 0.016));
        gameOverFontSize = Math.max(36, (int)(sh * 0.08));
    }

    private Font getSafeFont(int style, int size) {
        return new Font(Font.MONOSPACED, style, size);
    }

    private void loadBackground(Class<?> loader) {
        try { bgImage = new ImageIcon(loader.getResource("/backgrounds/background.png")).getImage(); }
        catch (Exception e) { System.out.println("BG not found"); }
    }

    private void loadHeartSprite(Class<?> loader) {
        try { heartSprite = ImageIO.read(loader.getResource("/level_assets/heart sprite.png")); }
        catch (Exception e) { System.out.println("Heart not found"); }
    }

    private void loadHeadPortrait(String path, boolean isP1, Class<?> loader) {
        if (path == null) return;
        try {
            BufferedImage img = ImageIO.read(loader.getResource(path));
            if (isP1) p1HeadImg = img;
            else      p2HeadImg = img;
        } catch (Exception e) { System.out.println("Head not found: " + path); }
    }

    private void setupWindow() {
        window = new JFrame("Human vs Brainrot");
        window.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        window.setUndecorated(true);
        window.add(this);
        GraphicsEnvironment ge = GraphicsEnvironment.getLocalGraphicsEnvironment();
        GraphicsDevice gd = ge.getDefaultScreenDevice();
        if (gd.isFullScreenSupported()) gd.setFullScreenWindow(window);
        else { window.setExtendedState(JFrame.MAXIMIZED_BOTH); window.setVisible(true); }
    }

    private void update() {
        if (gameOver) return;

        p1.movingRight = p1Right; p1.movingLeft = p1Left;
        p2.movingRight = p2Right; p2.movingLeft = p2Left;
        if (p1Right) p1.facingRight = true;
        if (p1Left)  p1.facingRight = false;
        if (p2Right) p2.facingRight = true;
        if (p2Left)  p2.facingRight = false;

        p1.update();
        p2.update();

        if (skillUsedThisTurn && !damageDealt) {
            if (skillTurn == 1 && (p1.isCastingSkill1() || p1.isCastingSkill2())) {
                p2HP--; damageDealt = true; checkGameOver();
            }
            if (skillTurn == 2 && (p2.isCastingSkill1() || p2.isCastingSkill2())) {
                p1HP--; damageDealt = true; checkGameOver();
            }
        }

        if (skillUsedThisTurn && damageDealt) {
            boolean p1Done = !p1.isCastingSkill1() && !p1.isCastingSkill2();
            boolean p2Done = !p2.isCastingSkill1() && !p2.isCastingSkill2();
            if (skillTurn == 1 && p1Done) { skillTurn = 2; skillUsedThisTurn = false; damageDealt = false; }
            else if (skillTurn == 2 && p2Done) { skillTurn = 1; skillUsedThisTurn = false; damageDealt = false; }
        }

        p1.setScreenBounds(getWidth());
        p2.setScreenBounds(getWidth());
        repaint();
    }

    private void checkGameOver() {
        if (p1HP <= 0) { p1HP = 0; gameOver = true; winner = "PLAYER 2 WINS!"; gameTimer.stop(); showGameOverDialog(); }
        else if (p2HP <= 0) { p2HP = 0; gameOver = true; winner = "PLAYER 1 WINS!"; gameTimer.stop(); showGameOverDialog(); }
    }

    private void showGameOverDialog() {
        repaint();
        Timer t = new Timer(1500, new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                int choice = JOptionPane.showConfirmDialog(window, winner + "\n\nPlay again?", "Game Over", JOptionPane.YES_NO_OPTION, JOptionPane.PLAIN_MESSAGE);
                if (choice == JOptionPane.YES_OPTION) { window.dispose(); new CharacterSelect(); }
                else System.exit(0);
            }
        });
        t.setRepeats(false);
        t.start();
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2 = (Graphics2D) g;
        g2.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);

        if (headSize == 0) calculateDimensions();

        if (bgImage != null) g2.drawImage(bgImage, 0, 0, getWidth(), getHeight(), this);
        else { g2.setColor(new Color(20, 40, 20)); g2.fillRect(0, 0, getWidth(), getHeight()); }

        p1.draw(g2, this);
        p2.draw(g2, this);
        drawHUD(g2);
        if (gameOver) drawGameOverOverlay(g2);
    }

    private void drawHUD(Graphics2D g2) {
        // P1 head
        if (p1HeadImg != null) g2.drawImage(p1HeadImg, 10, topY, headSize, headSize, this);
        else { g2.setColor(new Color(100, 200, 255)); g2.fillRect(10, topY, headSize, headSize); }

        // P1 hearts
        int hx1 = 10 + headSize + heartGap * 2;
        for (int i = 0; i < MAX_HP; i++) {
            int hx = hx1 + i * (heartSize + heartGap);
            int hy = topY + (headSize - heartSize) / 2;
            if (i < p1HP && heartSprite != null) g2.drawImage(heartSprite, hx, hy, heartSize, heartSize, this);
            else { g2.setColor(new Color(60, 0, 0)); g2.fillRect(hx, hy, heartSize, heartSize); }
        }

        // P2 head
        int p2HeadX = getWidth() - headSize - 10;
        if (p2HeadImg != null) g2.drawImage(p2HeadImg, p2HeadX, topY, headSize, headSize, this);
        else { g2.setColor(new Color(255, 150, 100)); g2.fillRect(p2HeadX, topY, headSize, headSize); }

        // P2 hearts
        int hx2 = p2HeadX - heartGap * 2 - MAX_HP * (heartSize + heartGap);
        for (int i = 0; i < MAX_HP; i++) {
            int hx = hx2 + i * (heartSize + heartGap);
            int hy = topY + (headSize - heartSize) / 2;
            if (i < p2HP && heartSprite != null) g2.drawImage(heartSprite, hx, hy, heartSize, heartSize, this);
            else { g2.setColor(new Color(60, 0, 0)); g2.fillRect(hx, hy, heartSize, heartSize); }
        }

        // Turn indicator
        g2.setFont(getSafeFont(Font.BOLD, hudFontSize));
        String turnText = skillTurn == 1 ? "P1 TURN" : "P2 TURN";
        g2.setColor(skillTurn == 1 ? new Color(100, 200, 255) : new Color(255, 150, 100));
        int tx = (getWidth() - g2.getFontMetrics().stringWidth(turnText)) / 2;
        g2.drawString(turnText, tx, topY + headSize / 2 + hudFontSize / 2);

        // Bottom controls
        g2.setFont(getSafeFont(Font.PLAIN, bottomFontSize));
        g2.setColor(Color.WHITE);
        g2.drawString("P1: A/D move  E/R skill", (int)(getWidth() * 0.01), getHeight() - (int)(getHeight() * 0.015));
        String p2ctrl = "P2: ←/→ move  , . skill";
        g2.drawString(p2ctrl, getWidth() - g2.getFontMetrics().stringWidth(p2ctrl) - (int)(getWidth() * 0.01), getHeight() - (int)(getHeight() * 0.015));
    }

    private void drawGameOverOverlay(Graphics2D g2) {
        g2.setColor(new Color(0, 0, 0, 160));
        g2.fillRect(0, 0, getWidth(), getHeight());
        g2.setFont(getSafeFont(Font.BOLD, gameOverFontSize));
        g2.setColor(Color.YELLOW);
        int tx = (getWidth() - g2.getFontMetrics().stringWidth(winner)) / 2;
        g2.drawString(winner, tx, getHeight() / 2);
    }

    @Override
    public void keyPressed(KeyEvent e) {
        if (gameOver) return;
        int key = e.getKeyCode();
        if      (key == KeyEvent.VK_A)      { p1Left  = true; }
        else if (key == KeyEvent.VK_D)      { p1Right = true; }
        else if (key == KeyEvent.VK_E) {
            if (skillTurn == 1 && !skillUsedThisTurn && !p1.isAnyCastingSkill() && p1.hasSkill1()) { p1.triggerSkill1(); skillUsedThisTurn = true; }
        } else if (key == KeyEvent.VK_R) {
            if (skillTurn == 1 && !skillUsedThisTurn && !p1.isAnyCastingSkill() && p1.hasSkill2()) { p1.triggerSkill2(); skillUsedThisTurn = true; }
        } else if (key == KeyEvent.VK_LEFT)  { p2Left  = true; }
        else if (key == KeyEvent.VK_RIGHT)   { p2Right = true; }
        else if (key == KeyEvent.VK_COMMA) {
            if (skillTurn == 2 && !skillUsedThisTurn && !p2.isAnyCastingSkill() && p2.hasSkill1()) { p2.triggerSkill1(); skillUsedThisTurn = true; }
        } else if (key == KeyEvent.VK_PERIOD) {
            if (skillTurn == 2 && !skillUsedThisTurn && !p2.isAnyCastingSkill() && p2.hasSkill2()) { p2.triggerSkill2(); skillUsedThisTurn = true; }
        }
    }

    @Override
    public void keyReleased(KeyEvent e) {
        int key = e.getKeyCode();
        if (key == KeyEvent.VK_A)     p1Left  = false;
        if (key == KeyEvent.VK_D)     p1Right = false;
        if (key == KeyEvent.VK_LEFT)  p2Left  = false;
        if (key == KeyEvent.VK_RIGHT) p2Right = false;
    }

    @Override public void keyTyped(KeyEvent e) {}
}
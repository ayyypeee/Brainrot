package Ui;

import entities.Character;
import entities.CharacterFactory;
import java.awt.*;
import java.awt.event.*;
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import javax.imageio.ImageIO;
import javax.swing.*;

public class ArcadeBattlePanel extends JPanel implements MouseListener, MouseMotionListener {

    private final int            playerIndex;
    private final ArrayList<Integer> opponentOrder;
    private       int            currentOpponentPos;
    private final CharacterFactory factory;
    private final Class<?>       loader;

    private Character player;
    private Character opponent;

    private final boolean PLAYER_FACES_RIGHT   = true;
    private final boolean OPPONENT_FACES_RIGHT = false;

    private String plSk1Name, plSk2Name, plSk3Name;
    private String opSk1Name, opSk2Name, opSk3Name;

    private Image         bgImage;
    private BufferedImage boxImage, vsImage, platformImage;
    private BufferedImage playerHeadImg, opponentHeadImg;

    private final int MAX_HP = 5;
    private int playerHP = MAX_HP, opponentHP = MAX_HP;

    private boolean playerTurn        = true;
    private int     round             = 1;
    private boolean skillUsedThisTurn = false;
    private boolean damageDealt       = false;
    private boolean gameOver          = false;
    private String  winner            = "";
    private boolean arcadeOver        = false;

    private Timer gameTimer, aiDelayTimer;

    private int groundY, headSize, topY, hudFontSize, gameOverFontSize;
    private int btnW, btnH, btnY;
    private boolean layoutReady = false;

    private SkillButton btn1, btn2, btn3;
    private Point mousePos = new Point();
    private JFrame window;

    private int screenW, screenH;

    // ── Constructor ───────────────────────────────────────────────────────────

    public ArcadeBattlePanel(int playerIndex, ArrayList<Integer> opponentOrder,
                             int currentOpponentPos, CharacterFactory factory,
                             int sw, int sh, Class<?> loader) {
        this.playerIndex        = playerIndex;
        this.opponentOrder      = opponentOrder;
        this.currentOpponentPos = currentOpponentPos;
        this.factory            = factory;
        this.loader             = loader;
        this.screenW = sw; this.screenH = sh;

        int opIndex = opponentOrder.get(currentOpponentPos);

        player   = factory.buildCharacter(playerIndex, loader, sw, sh);
        opponent = factory.buildCharacter(opIndex,     loader, sw, sh);

        plSk1Name = factory.getSkill1Name(playerIndex);
        plSk2Name = factory.getSkill2Name(playerIndex);
        plSk3Name = factory.getSkill3Name(playerIndex);
        opSk1Name = factory.getSkill1Name(opIndex);
        opSk2Name = factory.getSkill2Name(opIndex);
        opSk3Name = factory.getSkill3Name(opIndex);

        loadAssets(factory.getHeadPath(playerIndex), factory.getHeadPath(opIndex));
        player.setImageObserver(this);
        opponent.setImageObserver(this);

        setFocusable(true);
        addMouseListener(this);
        addMouseMotionListener(this);

        gameTimer = new Timer(16, e -> update());
        gameTimer.start();

        setupWindow(currentOpponentPos + 1, opponentOrder.size());
        SwingUtilities.invokeLater(() -> { calculateLayout(); placeCharacters(); });
    }

    // ── Assets ────────────────────────────────────────────────────────────────

    private void loadAssets(String plHead, String opHead) {
        try { bgImage       = new ImageIcon(loader.getResource("/backgrounds/background.png")).getImage(); } catch (Exception e) {}
        try { boxImage      = ImageIO.read(loader.getResource("/ui/v1_box_skills.png")); }   catch (Exception e) {}
        try { vsImage       = ImageIO.read(loader.getResource("/ui/Guardians_Of_Sanity__1_.png")); } catch (Exception e) {
            try { vsImage   = ImageIO.read(loader.getResource("/ui/vs.png")); } catch (Exception e2) {}
        }
        try { platformImage = ImageIO.read(loader.getResource("/level_assets/PLATFORM.png")); } catch (Exception e) {}
        if (plHead != null) try { playerHeadImg   = ImageIO.read(loader.getResource(plHead)); } catch (Exception e) {}
        if (opHead != null) try { opponentHeadImg = ImageIO.read(loader.getResource(opHead)); } catch (Exception e) {}
    }

    // ── Layout ────────────────────────────────────────────────────────────────

    private void calculateLayout() {
        int sw = getWidth(), sh = getHeight();
        screenW = sw; screenH = sh;

        headSize         = (int)(sh * 0.075);
        topY             = (int)(sh * 0.015);
        hudFontSize      = Math.max(13, (int)(sh * 0.026));
        gameOverFontSize = Math.max(36, (int)(sh * 0.09));

        btnH = (int)(sh * 0.16);
        btnW = (int)(sw * 0.28);
        int gap    = (int)(sw * 0.02);
        int totalW = btnW * 3 + gap * 2;
        int startX = (sw - totalW) / 2;
        btnY = sh - btnH - (int)(sh * 0.02);

        groundY = (int)(sh * 0.60);

        btn1 = new SkillButton(1, plSk1Name); btn1.setBounds(startX, btnY, btnW, btnH);
        btn2 = new SkillButton(2, plSk2Name); btn2.setBounds(startX + btnW + gap, btnY, btnW, btnH);
        btn3 = new SkillButton(3, plSk3Name); btn3.setBounds(startX + (btnW + gap) * 2, btnY, btnW, btnH);

        layoutReady = true;
    }

    private void placeCharacters() {
        int sw = getWidth();

        player.facingRight   = PLAYER_FACES_RIGHT;
        opponent.facingRight = OPPONENT_FACES_RIGHT;

        player.placeOnPlatform(  sw / 4     - player.getWidth()   / 2, groundY);
        opponent.placeOnPlatform(sw * 3 / 4 - opponent.getWidth() / 2, groundY);
    }

    private void setupWindow(int fightNum, int totalFights) {
        window = new JFrame("ARCADE Fight " + fightNum + " of " + totalFights);
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
        opponent.update();

        if (!player.isAnyCastingSkill())   player.facingRight   = PLAYER_FACES_RIGHT;
        if (!opponent.isAnyCastingSkill()) opponent.facingRight = OPPONENT_FACES_RIGHT;

        if (skillUsedThisTurn && !damageDealt) {
            if (playerTurn && (player.isCastingSkill1() || player.isCastingSkill2() || player.isCastingSkill3())) {
                opponentHP--; damageDealt = true; checkGameOver();
            }
            if (!playerTurn && (opponent.isCastingSkill1() || opponent.isCastingSkill2() || opponent.isCastingSkill3())) {
                playerHP--; damageDealt = true; checkGameOver();
            }
        }

        if (skillUsedThisTurn && damageDealt) {
            if (playerTurn && !player.isAnyCastingSkill()) {
                playerTurn = false; skillUsedThisTurn = false; damageDealt = false;
                scheduleOpponentTurn();
            } else if (!playerTurn && !opponent.isAnyCastingSkill()) {
                playerTurn = true; skillUsedThisTurn = false; damageDealt = false; round++;
            }
        }

        updateButtonStates();
        repaint();
    }

    private void scheduleOpponentTurn() {
        if (gameOver) return;
        aiDelayTimer = new Timer(1200, e -> {
            if (!gameOver) {
                ArrayList<Integer> avail = new ArrayList<>();
                if (opponent.hasSkill1()) avail.add(1);
                if (opponent.hasSkill2()) avail.add(2);
                if (opponent.hasSkill3()) avail.add(3);
                if (!avail.isEmpty()) {
                    int skill   = avail.get(new java.util.Random().nextInt(avail.size()));
                    int plCentX = player.getX() + player.getWidth() / 2;
                    opponent.startAttack(skill, plCentX);
                    skillUsedThisTurn = true;
                }
            }
            ((Timer) e.getSource()).stop();
        });
        aiDelayTimer.setRepeats(false);
        aiDelayTimer.start();
    }

    // ── Game over ─────────────────────────────────────────────────────────────

    private void checkGameOver() {
        if (opponentHP <= 0) {
            opponentHP = 0; gameOver = true;
            int nextPos = currentOpponentPos + 1;
            if (nextPos >= opponentOrder.size()) {
                winner     = "YOU WIN THE ARCADE!";
                arcadeOver = true;
            } else {
                winner = "OPPONENT DOWN!  Next fight...";
            }
            end();
        } else if (playerHP <= 0) {
            playerHP = 0; gameOver = true;
            winner     = "GAME OVER YOU LOST!";
            arcadeOver = true;
            end();
        }
    }

    private void end() {
        gameTimer.stop();
        if (aiDelayTimer != null) aiDelayTimer.stop();
        repaint();

        int delay = arcadeOver ? 1500 : 1000;
        new Timer(delay, e -> {
            if (!arcadeOver) {
                window.dispose();
                ArcadeBattlePanel next = new ArcadeBattlePanel(
                        playerIndex, opponentOrder, currentOpponentPos + 1,
                        factory, screenW, screenH, loader);
                next.playerHP = this.playerHP;
            } else {
                String msg = winner + (playerHP > 0 ? "\nYou survived with " + playerHP + " HP!" : "");
                int choice = JOptionPane.showConfirmDialog(window,
                        msg + "\n\nPlay again?", "Arcade Over",
                        JOptionPane.YES_NO_OPTION, JOptionPane.PLAIN_MESSAGE);
                if (choice == JOptionPane.YES_OPTION) { window.dispose(); new LevelSelect(); }
                else System.exit(0);
            }
        }) {{ setRepeats(false); start(); }};
    }

    // ── Button states ─────────────────────────────────────────────────────────

    private void updateButtonStates() {
        if (!layoutReady || btn1 == null) return;
        boolean animating = player.isAnyCastingSkill() || opponent.isAnyCastingSkill();
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
        g2.setRenderingHint(RenderingHints.KEY_INTERPOLATION,     RenderingHints.VALUE_INTERPOLATION_BILINEAR);

        int sw = getWidth(), sh = getHeight();

        if (bgImage != null) g2.drawImage(bgImage, 0, 0, sw, sh, this);
        else { g2.setColor(new Color(20, 20, 40)); g2.fillRect(0, 0, sw, sh); }

        drawGround(g2, sw, sh);
        player.draw(g2, this);
        opponent.draw(g2, this);

        if (layoutReady && btn1 != null) {
            btn1.draw(g2, boxImage, this);
            btn2.draw(g2, boxImage, this);
            btn3.draw(g2, boxImage, this);
        }

        drawHUD(g2, sw, sh);

        if (gameOver) {
            g2.setColor(new Color(0, 0, 0, 160));
            g2.fillRect(0, 0, sw, sh);
            g2.setFont(new Font(Font.MONOSPACED, Font.BOLD, gameOverFontSize));
            g2.setColor(arcadeOver && playerHP > 0 ? new Color(80, 255, 80) : Color.YELLOW);
            int tw = g2.getFontMetrics().stringWidth(winner);
            g2.drawString(winner, sw / 2 - tw / 2, sh / 2);
        }
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
        }
    }

    private void drawHUD(Graphics2D g2, int sw, int sh) {
        int barH    = (int)(sh * 0.028);
        int headGap = (int)(sw * 0.010);
        int barMaxW = (int)(sw * 0.315);
        int nameY   = topY + (int)(headSize * 0.55);
        int barY    = nameY + (int)(sh * 0.01);

        int plHX = (int)(sw * 0.01);
        if (playerHeadImg != null) g2.drawImage(playerHeadImg, plHX, topY, headSize, headSize, this);
        else { g2.setColor(new Color(100, 200, 255)); g2.fillRect(plHX, topY, headSize, headSize); }
        g2.setFont(new Font(Font.MONOSPACED, Font.BOLD, hudFontSize));
        g2.setColor(playerTurn ? new Color(255, 200, 80) : Color.WHITE);
        g2.drawString(factory.getName(playerIndex), plHX + headSize + headGap, nameY);
        drawBar(g2, plHX + headSize + headGap, barY, barMaxW, barH, playerHP, true);

        int opHX = sw - (int)(sw * 0.01) - headSize;
        if (opponentHeadImg != null) g2.drawImage(opponentHeadImg, opHX, topY, headSize, headSize, this);
        else { g2.setColor(new Color(255, 100, 100)); g2.fillRect(opHX, topY, headSize, headSize); }
        g2.setFont(new Font(Font.MONOSPACED, Font.BOLD, hudFontSize));
        g2.setColor(!playerTurn ? new Color(255, 200, 80) : Color.WHITE);
        String opLabel = factory.getName(opponentOrder.get(currentOpponentPos));
        int p2Right    = opHX - headGap;
        g2.drawString(opLabel, p2Right - g2.getFontMetrics().stringWidth(opLabel), nameY);
        drawBar(g2, p2Right - barMaxW, barY, barMaxW, barH, opponentHP, false);

        int cx = sw / 2;
        int rfs = Math.max(16, (int)(sh * 0.032));
        g2.setFont(new Font(Font.MONOSPACED, Font.BOLD, rfs));
        g2.setColor(new Color(255, 200, 80));
        String fightStr = "FIGHT " + (currentOpponentPos + 1) + " / " + opponentOrder.size();
        g2.drawString(fightStr, cx - g2.getFontMetrics().stringWidth(fightStr) / 2, topY + rfs);
        int vsSize = (int)(sh * 0.065);
        if (vsImage != null) g2.drawImage(vsImage, cx - vsSize / 2, topY + rfs + 2, vsSize, vsSize, this);

        if (!gameOver) {
            String lbl   = playerTurn ? "YOUR TURN!" : opLabel.toUpperCase() + "\nATTACKS!";
            Color  color = playerTurn ? new Color(100, 180, 255) : new Color(255, 80, 80);
            String[] lines = lbl.split("\n");
            int fs = Math.max(22, (int)(sh * 0.048));
            g2.setFont(new Font(Font.MONOSPACED, Font.BOLD, fs));
            g2.setColor(color);
            int startY = sh / 2 - (lines.length * (fs + 6)) / 2;
            for (int i = 0; i < lines.length; i++) {
                int lw = g2.getFontMetrics().stringWidth(lines[i]);
                g2.drawString(lines[i], cx - lw / 2, startY + i * (fs + 6) + fs);
            }
        }
    }

    private void drawBar(Graphics2D g2, int x, int y, int maxW, int barH, int hp, boolean ltr) {
        g2.setColor(new Color(60, 0, 0));
        g2.fillRect(x, y, maxW, barH);
        int fillW = (int)(maxW * (double) hp / MAX_HP);
        if (fillW > 0) {
            float ratio = (float) hp / MAX_HP;
            g2.setColor(ratio > 0.6f ? new Color(60, 210, 60)
                    : ratio > 0.3f  ? new Color(230, 210, 40)
                    :                  new Color(230, 50, 50));
            g2.fillRect(ltr ? x : x + maxW - fillW, y, fillW, barH);
        }
        g2.setColor(Color.WHITE);
        g2.setStroke(new BasicStroke(2f));
        g2.drawRect(x, y, maxW, barH);
        g2.setStroke(new BasicStroke(1f));
    }

    // ── Mouse ─────────────────────────────────────────────────────────────────

    @Override
    public void mouseClicked(MouseEvent e) {
        if (gameOver || !layoutReady || btn1 == null || !playerTurn || skillUsedThisTurn) return;
        if (player.isAnyCastingSkill() || opponent.isAnyCastingSkill()) return;
        Point pt    = e.getPoint();
        int opCentX = opponent.getX() + opponent.getWidth() / 2;
        if      (btn1.contains(pt) && player.hasSkill1()) { player.startAttack(1, opCentX); skillUsedThisTurn = true; }
        else if (btn2.contains(pt) && player.hasSkill2()) { player.startAttack(2, opCentX); skillUsedThisTurn = true; }
        else if (btn3.contains(pt) && player.hasSkill3()) { player.startAttack(3, opCentX); skillUsedThisTurn = true; }
    }

    @Override
    public void mouseMoved(MouseEvent e) {
        mousePos = e.getPoint();
        if (!layoutReady || btn1 == null) return;
        boolean on = btn1.getBounds().contains(mousePos)
                || btn2.getBounds().contains(mousePos)
                || btn3.getBounds().contains(mousePos);
        setCursor(on ? Cursor.getPredefinedCursor(Cursor.HAND_CURSOR) : Cursor.getDefaultCursor());
    }

    @Override public void mousePressed(MouseEvent e)  {}
    @Override public void mouseReleased(MouseEvent e) {}
    @Override public void mouseEntered(MouseEvent e)  {}
    @Override public void mouseExited(MouseEvent e)   {}
    @Override public void mouseDragged(MouseEvent e)  {}
}
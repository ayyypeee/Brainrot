package Ui;

import entities.Character;
import entities.BattleStats;
import java.awt.*;
import java.awt.event.*;
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import javax.imageio.ImageIO;
import javax.swing.*;

public class AIBattlePanel extends JPanel implements MouseListener, MouseMotionListener, KeyListener {

    private final Character player;
    private final Character ai;

    private static final boolean PLAYER_FACES_RIGHT = true;
    private static final boolean AI_FACES_RIGHT     = false;

    private final String playerName, aiName;

    private Image bgImage;
    private BufferedImage boxImage, vsImage, platformImage;
    // Keep head paths so we can re-assign them after engine reset
    private BufferedImage head1Img, head2Img;

    private BattleEngine engine;

    private enum Phase { IDLE, APPROACHING, ANIMATING, IMPACTED, RETREATING, PASSIVE_MSG }
    private Phase phase = Phase.IDLE;

    private boolean roundOver = false;
    private boolean matchOver = false;
    private String  roundWinnerLabel = "";
    private String  matchWinnerLabel = "";

    // ── Best-of-3 tracking ────────────────────────────────────────────────────
    private int winsPlayer = 0;
    private int winsAI     = 0;
    private int matchRound = 1;
    private static final int WINS_NEEDED = 2;

    // Overlay state
    private boolean showingRoundOverlay = false;
    private long    roundOverlayStart   = 0;
    private static final int ROUND_OVERLAY_MS = 2200;

    private boolean paused = false;

    private int groundY, btnW, btnH, btnY;
    private boolean layoutReady = false;

    private SkillButton btn1, btn2, btn3;
    private Point mousePos = new Point();
    private int hoveredSkill = 0;

    private JFrame window;
    private Timer gameTimer;
    private Timer aiDelayTimer;

    private PauseMenuOverlay pauseMenu;

    private final Runnable restartAction;
    private final Class<?> loader;
    private final String   playerHeadPath;
    private final String   aiHeadPath;

    public AIBattlePanel(
            Character player, Character ai,
            String playerHead, String aiHead,
            String plSk1, String plSk2, String plSk3,
            String aiSk1, String aiSk2, String aiSk3,
            int[] plR1, int[] plR2, int[] plR3,
            int[] aiR1, int[] aiR2, int[] aiR3,
            String playerName, String aiName,
            Class<?> loader,
            Runnable restartAction) {

        this.player         = player;
        this.ai             = ai;
        this.playerName     = playerName;
        this.aiName         = aiName;
        this.restartAction  = restartAction;
        this.loader         = loader;
        this.playerHeadPath = playerHead;
        this.aiHeadPath     = aiHead;
        MusicPlayer.stop();

        engine = new BattleEngine(playerName, playerName, aiName, "Enemy " + aiName);
        MusicPlayer.playIngame();

        loadAssets(playerHead, aiHead, loader);
        player.setImageObserver(this);
        ai.setImageObserver(this);

        pauseMenu = new PauseMenuOverlay(loader);

        setFocusable(true);
        addMouseListener(this);
        addMouseMotionListener(this);
        addKeyListener(this);

        gameTimer = new Timer(16, e -> update());
        gameTimer.start();

        setupWindow();
        SwingUtilities.invokeLater(() -> { calculateLayout(); placeCharacters(); });

        engine.enqueueDialogue("Match begins! Best of 3 rounds. " + playerName + " vs " + aiName + "!");
        engine.enqueueDialogue("Round 1! Your turn. Make your move!");
    }

    private void loadAssets(String playerHead, String aiHead, Class<?> ldr) {
        try { bgImage = new ImageIcon(ldr.getResource("/backgrounds/background.png")).getImage(); } catch (Exception e) {}
        try { boxImage = ImageIO.read(ldr.getResource("/ui/v1_box_skills.png")); } catch (Exception e) {}
        try {
            vsImage = ImageIO.read(ldr.getResource("/ui/Guardians_Of_Sanity__1_.png"));
        } catch (Exception e) {
            try { vsImage = ImageIO.read(ldr.getResource("/ui/vs.png")); } catch (Exception e2) {}
        }
        try { platformImage = ImageIO.read(ldr.getResource("/level_assets/PLATFORM.png")); } catch (Exception e) {}
        if (playerHead != null) try { head1Img = ImageIO.read(ldr.getResource(playerHead)); } catch (Exception e) {}
        if (aiHead     != null) try { head2Img = ImageIO.read(ldr.getResource(aiHead));     } catch (Exception e) {}
        engine.head1 = head1Img;
        engine.head2 = head2Img;
    }

    private void calculateLayout() {
        int sw = getWidth(), sh = getHeight();
        btnH = (int)(sh * 0.16);
        btnW = (int)(sw * 0.28);
        int gap    = (int)(sw * 0.02);
        int startX = (sw - btnW * 3 - gap * 2) / 2;
        btnY    = sh - btnH - (int)(sh * 0.02);
        groundY = (int)(sh * 0.60);
        rebuildButtons(startX, gap);
        layoutReady = true;
    }

    private void placeCharacters() {
        int sw = getWidth();
        player.facingRight = PLAYER_FACES_RIGHT;
        ai.facingRight     = AI_FACES_RIGHT;
        player.placeOnPlatform(sw / 4 - player.getWidth() / 2,  groundY);
        ai.placeOnPlatform(sw * 3 / 4 - ai.getWidth() / 2,      groundY);
    }

    private void rebuildButtons(int startX, int gap) {
        entities.CharSkill[] skills = entities.CharSkillDB.getAll(playerName);
        String n1 = skills.length > 0 ? skills[0].name : "Skill 1";
        String n2 = skills.length > 1 ? skills[1].name : "Skill 2";
        String n3 = skills.length > 2 ? skills[2].name : "Skill 3";

        btn1 = new SkillButton(1, n1);
        btn1.setBounds(startX, btnY, btnW, btnH);
        btn1.setManaInfo("+" + (skills.length > 0 ? skills[0].manaRegen : 15) + " MP", true);
        btn1.setIconPath(skills.length > 0 ? skills[0].iconPath : null);

        btn2 = new SkillButton(2, n2);
        btn2.setBounds(startX + btnW + gap, btnY, btnW, btnH);
        btn2.setManaInfo("-" + (skills.length > 1 ? skills[1].manaCost : 20) + " MP", false);
        btn2.setIconPath(skills.length > 1 ? skills[1].iconPath : null);

        btn3 = new SkillButton(3, n3);
        btn3.setBounds(startX + (btnW + gap) * 2, btnY, btnW, btnH);
        btn3.setManaInfo("-" + (skills.length > 2 ? skills[2].manaCost : 35) + " MP", false);
        btn3.setIconPath(skills.length > 2 ? skills[2].iconPath : null);
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

    // ── Pause ─────────────────────────────────────────────────────────────────
    private void togglePause() {
        paused = !paused;
        if (paused) {
            gameTimer.stop();
            if (aiDelayTimer != null) aiDelayTimer.stop();
            pauseMenu.reset();
        } else {
            gameTimer.start();
        }
        repaint();
    }

    private PauseMenuOverlay.Callbacks pauseCallbacks() {
        return new PauseMenuOverlay.Callbacks() {
            @Override public void onResume()   { togglePause(); }
            @Override public void onRestart()  {
                gameTimer.stop();
                if (aiDelayTimer != null) aiDelayTimer.stop();
                window.dispose();
                restartAction.run();
            }
            @Override public void onMainMenu() {
                gameTimer.stop();
                if (aiDelayTimer != null) aiDelayTimer.stop();
                window.dispose();
                new LevelSelect();
            }
            @Override public void onExit() { System.exit(0); }
        };
    }

    // ── Game loop ─────────────────────────────────────────────────────────────
    private void update() {
        if (paused) return;

        // Round-over overlay timer
        if (showingRoundOverlay) {
            if (System.currentTimeMillis() - roundOverlayStart >= ROUND_OVERLAY_MS) {
                showingRoundOverlay = false;
                if (matchOver) {
                    endMatch();
                } else {
                    startNextRound();
                }
            }
            repaint();
            return;
        }

        if (roundOver) return;

        engine.tickDialogue();
        if (engine.isDialogueActive()) { repaint(); return; }

        player.update();
        ai.update();

        if (!player.isAnyCastingSkill()) player.facingRight = PLAYER_FACES_RIGHT;
        if (!ai.isAnyCastingSkill())     ai.facingRight     = AI_FACES_RIGHT;

        Character attacker = engine.turnSide == 1 ? player : ai;

        switch (phase) {
            case APPROACHING:
                if (attacker.isCastingSkill1() || attacker.isCastingSkill2() || attacker.isCastingSkill3())
                    phase = Phase.ANIMATING;
                break;

            case ANIMATING:
                boolean stillAnim = attacker.isCastingSkill1()
                        || attacker.isCastingSkill2()
                        || attacker.isCastingSkill3();
                if (!stillAnim) {
                    engine.applyPendingDamage();
                    checkRoundOver();
                    phase = Phase.IMPACTED;
                }
                break;

            case IMPACTED:
                if (!attacker.isAnyCastingSkill()) phase = Phase.RETREATING;
                break;

            case RETREATING:
                if (!attacker.isAnyCastingSkill()) {
                    engine.drainPassiveQueue();
                    if (engine.isDialogueActive() || engine.hasPassiveMessages())
                        phase = Phase.PASSIVE_MSG;
                    else
                        finishTurn();
                }
                break;

            case PASSIVE_MSG:
                if (!engine.isDialogueActive()) finishTurn();
                break;

            default: break;
        }

        updateButtonStates();
        repaint();
    }

    private void finishTurn() {
        if (roundOver) return;
        int prevSide = engine.turnSide;
        engine.endTurn(prevSide);
        phase = Phase.IDLE;

        engine.beginTurn(engine.turnSide);

        if (engine.isSideStunned(engine.turnSide)) {
            String who = engine.turnSide == 1 ? engine.label1 : engine.label2;
            engine.enqueueDialogue(who + " is stunned and has to skip their turn!");
            engine.consumeStun(engine.turnSide);
            engine.tickEffectsForSide(engine.turnSide);
            engine.endTurn(engine.turnSide);
            engine.beginTurn(engine.turnSide);
        }

        engine.tickEffectsForSide(engine.turnSide);

        if (engine.turnSide == 1)
            engine.enqueueDialogue("Your turn! Choose your next attack.");
        else
            scheduleAITurn();
    }

    private void scheduleAITurn() {
        if (roundOver) return;
        aiDelayTimer = new Timer(1000, e -> {
            if (!roundOver && !paused) {
                int skill = pickAISkill();
                engine.resolveSkill(2, skill);
                ai.startAttack(skill, player.getX() + player.getWidth() / 2);
                phase = Phase.APPROACHING;
            }
            ((Timer) e.getSource()).stop();
        });
        aiDelayTimer.setRepeats(false);
        aiDelayTimer.start();
    }

    private int pickAISkill() {
        ArrayList<Integer> avail = new ArrayList<>();
        for (int s = 1; s <= 3; s++) if (engine.canUseSkill(2, s)) avail.add(s);
        if (avail.isEmpty()) return 1;
        return avail.get(new java.util.Random().nextInt(avail.size()));
    }

    // ── Round / match logic ───────────────────────────────────────────────────

    private void checkRoundOver() {
        if (engine.stats2.hp <= 0) {
            engine.stats2.hp = 0;
            winsPlayer++;
            roundWinnerLabel = playerName.toUpperCase() + " wins Round " + matchRound + "!";
            triggerRoundEnd();
        } else if (engine.stats1.hp <= 0) {
            engine.stats1.hp = 0;
            winsAI++;
            roundWinnerLabel = aiName.toUpperCase() + " wins Round " + matchRound + "!";
            triggerRoundEnd();
        }
    }

    private void triggerRoundEnd() {
        roundOver = true;
        phase     = Phase.IDLE;
        if (aiDelayTimer != null) aiDelayTimer.stop();

        if (winsPlayer >= WINS_NEEDED) {
            matchWinnerLabel = playerName.toUpperCase() + " WINS THE MATCH!";
            matchOver = true;
        } else if (winsAI >= WINS_NEEDED) {
            matchWinnerLabel = aiName.toUpperCase() + " WINS THE MATCH!";
            matchOver = true;
        }

        showingRoundOverlay = true;
        roundOverlayStart   = System.currentTimeMillis();
        repaint();
    }

    private void startNextRound() {
        matchRound++;
        roundOver = false;
        phase     = Phase.IDLE;

        // Fresh engine, re-attach cached head images
        engine       = new BattleEngine(playerName, playerName, aiName, "Enemy " + aiName);
        engine.head1 = head1Img;
        engine.head2 = head2Img;

        // Reset character positions
        int sw = getWidth();
        player.facingRight = PLAYER_FACES_RIGHT;
        ai.facingRight     = AI_FACES_RIGHT;
        player.placeOnPlatform(sw / 4 - player.getWidth() / 2,     groundY);
        ai.placeOnPlatform(sw * 3 / 4 - ai.getWidth() / 2,         groundY);

        int gap = (int)(sw * 0.02);
        rebuildButtons((sw - btnW * 3 - gap * 2) / 2, gap);

        engine.enqueueDialogue("Round " + matchRound + "! Score: "
                + playerName + " " + winsPlayer + " – " + winsAI + " " + aiName + ". Your turn!");
    }

    private void endMatch() {
        gameTimer.stop();
        if (aiDelayTimer != null) aiDelayTimer.stop();
        repaint();
        new Timer(1800, e -> {
            int choice = JOptionPane.showConfirmDialog(window,
                    matchWinnerLabel + "\n\nPlay again?", "Match Over",
                    JOptionPane.YES_NO_OPTION, JOptionPane.PLAIN_MESSAGE);
            if (choice == JOptionPane.YES_OPTION) { window.dispose(); restartAction.run(); }
            else { window.dispose(); new LevelSelect(); }
        }) {{ setRepeats(false); start(); }};
    }

    private void updateButtonStates() {
        if (!layoutReady || btn1 == null) return;
        boolean busy = phase != Phase.IDLE
                || engine.isDialogueActive()
                || engine.turnSide != 1
                || paused
                || roundOver;

        btn1.setDisabled(busy);
        btn2.setDisabled(busy || !engine.canUseSkill(1, 2));
        btn3.setDisabled(busy || !engine.canUseSkill(1, 3));

        btn1.setHovered(!btn1.isDisabled() && btn1.getBounds().contains(mousePos));
        btn2.setHovered(!btn2.isDisabled() && btn2.getBounds().contains(mousePos));
        btn3.setHovered(!btn3.isDisabled() && btn3.getBounds().contains(mousePos));
    }

    // ── Paint ─────────────────────────────────────────────────────────────────
    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        if (!layoutReady) { calculateLayout(); placeCharacters(); }

        Graphics2D g2 = (Graphics2D) g;
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g2.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
        g2.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);

        int sw = getWidth(), sh = getHeight();

        if (bgImage != null) g2.drawImage(bgImage, 0, 0, sw, sh, this);
        else { g2.setColor(new Color(20, 40, 20)); g2.fillRect(0, 0, sw, sh); }

        drawGround(g2, sw, sh);

        if (engine.turnSide == 1 && player.isAnyCastingSkill()) {
            ai.draw(g2, this); player.draw(g2, this);
        } else if (engine.turnSide == 2 && ai.isAnyCastingSkill()) {
            player.draw(g2, this); ai.draw(g2, this);
        } else {
            player.draw(g2, this); ai.draw(g2, this);
        }

        engine.drawHUD(g2, sw, sh, this, vsImage, engine.turnSide == 1, engine.round);
        drawMatchScore(g2, sw, sh);
        drawSkillButtons(g2, sw, sh);
        engine.drawDialogueBox(g2, sw, sh);

        if (hoveredSkill > 0 && !engine.isDialogueActive())
            engine.drawSkillTooltip(g2, 1, hoveredSkill, mousePos.x, mousePos.y, sw, sh);

        engine.tooltipX    = mousePos.x;
        engine.tooltipY    = mousePos.y;
        engine.tooltipText = "hover";

        if (!engine.isDialogueActive() && !roundOver && phase == Phase.IDLE)
            drawTurnLabel(g2, sw, sh);

        if (showingRoundOverlay) drawRoundOverlay(g2, sw, sh);

        if (paused) pauseMenu.draw(g2, sw, sh);
    }

    // ── Draw helpers ──────────────────────────────────────────────────────────

    private void drawMatchScore(Graphics2D g2, int sw, int sh) {
        int cx  = sw / 2;
        int y   = (int)(sh * 0.165);
        int pip = Math.max(10, (int)(sh * 0.018));
        int gap = pip + 4;

        int fs = Math.max(11, (int)(sh * 0.018));
        g2.setFont(new Font(Font.MONOSPACED, Font.BOLD, fs));
        String rLabel = "Round " + matchRound + " of 3";
        g2.setColor(new Color(255, 220, 80));
        int rw = g2.getFontMetrics().stringWidth(rLabel);
        g2.drawString(rLabel, cx - rw / 2, y - 4);

        // Player pips (left)
        for (int i = 0; i < WINS_NEEDED; i++) {
            int px = cx - (int)(sw * 0.06) - i * gap;
            if (i < winsPlayer) { g2.setColor(new Color(100, 200, 255)); g2.fillOval(px, y, pip, pip); }
            else                 { g2.setColor(new Color(60, 80, 100));   g2.fillOval(px, y, pip, pip); }
            g2.setColor(Color.WHITE); g2.drawOval(px, y, pip, pip);
        }

        // AI pips (right)
        for (int i = 0; i < WINS_NEEDED; i++) {
            int px = cx + (int)(sw * 0.04) + i * gap;
            if (i < winsAI) { g2.setColor(new Color(255, 100, 100)); g2.fillOval(px, y, pip, pip); }
            else             { g2.setColor(new Color(100, 60, 60));   g2.fillOval(px, y, pip, pip); }
            g2.setColor(Color.WHITE); g2.drawOval(px, y, pip, pip);
        }
    }

    private void drawRoundOverlay(Graphics2D g2, int sw, int sh) {
        double elapsed = System.currentTimeMillis() - roundOverlayStart;
        double t    = Math.min(elapsed / 300.0, 1.0);
        double ease = 1 - Math.pow(1 - t, 3);

        g2.setColor(new Color(0, 0, 0, (int)(180 * ease)));
        g2.fillRect(0, 0, sw, sh);

        int panW = (int)(sw * 0.55);
        int panH = (int)(sh * 0.32);
        int panX = (sw - panW) / 2;
        int panY = (int)((sh - panH) / 2 - panH * 0.20 * (1 - ease));

        g2.setColor(new Color(6, 12, 28, 230));
        g2.fillRoundRect(panX, panY, panW, panH, 20, 20);
        g2.setColor(new Color(72, 210, 210));
        g2.setStroke(new BasicStroke(2.5f));
        g2.drawRoundRect(panX, panY, panW, panH, 20, 20);
        g2.setStroke(new BasicStroke(1f));

        int cx = panX + panW / 2;

        int fs1 = Math.max(20, (int)(sh * 0.042));
        g2.setFont(new Font(Font.MONOSPACED, Font.BOLD, fs1));
        g2.setColor(Color.YELLOW);
        FontMetrics fm1 = g2.getFontMetrics();
        g2.drawString(roundWinnerLabel, cx - fm1.stringWidth(roundWinnerLabel) / 2,
                panY + (int)(panH * 0.38) + fs1);

        int fs2 = Math.max(14, (int)(sh * 0.026));
        g2.setFont(new Font(Font.MONOSPACED, Font.BOLD, fs2));
        String scoreLine = matchOver
                ? matchWinnerLabel
                : "Score  " + playerName + " " + winsPlayer + " – " + winsAI + " " + aiName;
        g2.setColor(matchOver ? new Color(255, 100, 100) : new Color(72, 210, 210));
        FontMetrics fm2 = g2.getFontMetrics();
        g2.drawString(scoreLine, cx - fm2.stringWidth(scoreLine) / 2,
                panY + (int)(panH * 0.68) + fs2);
    }

    private void drawGround(Graphics2D g2, int sw, int sh) {
        if (platformImage != null)
            g2.drawImage(platformImage, 0, groundY, sw, sh,
                    0, 0, platformImage.getWidth(), platformImage.getHeight(), this);
        else {
            g2.setColor(new Color(100, 130, 60));
            g2.fillRect(0, groundY, sw, (int)((sh - groundY) * 0.15));
            g2.setColor(new Color(120, 80, 35));
            g2.fillRect(0, groundY + (int)((sh - groundY) * 0.15), sw, sh - groundY);
        }
    }

    private void drawSkillButtons(Graphics2D g2, int sw, int sh) {
        if (!layoutReady || btn1 == null) return;
        btn1.draw(g2, boxImage, this);
        btn2.draw(g2, boxImage, this);
        btn3.draw(g2, boxImage, this);
        engine.drawSkillMeta(g2, 1, btn1.getBounds(), btn2.getBounds(), btn3.getBounds(), sh);
    }

    private void drawTurnLabel(Graphics2D g2, int sw, int sh) {
        String lbl   = engine.turnSide == 1 ? "YOUR TURN!\nMake your move!" : aiName.toUpperCase() + "\nIS PLANNING...";
        Color  color = engine.turnSide == 1 ? new Color(100, 180, 255) : new Color(255, 80, 80);
        String[] lines = lbl.split("\n");
        int fs = Math.max(22, (int)(sh * 0.048));
        g2.setFont(new Font(Font.MONOSPACED, Font.BOLD, fs));
        g2.setColor(color);
        int startY = sh / 2 - (lines.length * (fs + 6)) / 2;
        for (int i = 0; i < lines.length; i++) {
            int lw = g2.getFontMetrics().stringWidth(lines[i]);
            g2.drawString(lines[i], sw / 2 - lw / 2, startY + i * (fs + 6) + fs);
        }
    }

    private void handleSkillClick(int skillNum) {
        if (paused || phase != Phase.IDLE || engine.turnSide != 1 || roundOver) return;
        if (engine.isDialogueActive()) return;
        if (!engine.canUseSkill(1, skillNum)) return;

        engine.resolveSkill(1, skillNum);
        player.startAttack(skillNum, ai.getX() + ai.getWidth() / 2);
        phase = Phase.APPROACHING;
    }

    // ── Mouse ─────────────────────────────────────────────────────────────────
    @Override
    public void mouseClicked(MouseEvent e) {
        if (paused) { pauseMenu.handleClick(e.getPoint(), pauseCallbacks()); repaint(); return; }
        if (!layoutReady || roundOver || showingRoundOverlay) return;
        if (engine.isDialogueActive()) { engine.advanceDialogue(); repaint(); return; }
        if (phase != Phase.IDLE || engine.turnSide != 1) return;
        Point pt = e.getPoint();
        if      (btn1.contains(pt)) handleSkillClick(1);
        else if (btn2.contains(pt)) handleSkillClick(2);
        else if (btn3.contains(pt)) handleSkillClick(3);
    }

    @Override public void mousePressed(MouseEvent e)  { if (paused) { pauseMenu.handleMousePressed(e.getPoint()); repaint(); } }
    @Override public void mouseReleased(MouseEvent e) { if (paused) pauseMenu.handleRelease(); }

    @Override
    public void mouseMoved(MouseEvent e) {
        mousePos = e.getPoint();
        if (paused) { pauseMenu.handleHover(e.getPoint()); repaint(); return; }
        hoveredSkill = btn1 != null && btn1.getBounds().contains(mousePos) ? 1
                : btn2 != null && btn2.getBounds().contains(mousePos) ? 2
                : btn3 != null && btn3.getBounds().contains(mousePos) ? 3 : 0;
        setCursor(hoveredSkill > 0 ? Cursor.getPredefinedCursor(Cursor.HAND_CURSOR) : Cursor.getDefaultCursor());
        repaint();
    }

    @Override public void mouseDragged(MouseEvent e) { if (paused) { pauseMenu.handleDrag(e.getPoint()); repaint(); } }

    // ── Keys ──────────────────────────────────────────────────────────────────
    @Override
    public void keyPressed(KeyEvent e) {
        int k = e.getKeyCode();
        if (k == KeyEvent.VK_ENTER || k == KeyEvent.VK_SPACE) {
            if (paused) return;
            if (engine.isDialogueActive()) { engine.advanceDialogue(); repaint(); }
        } else if (k == KeyEvent.VK_ESCAPE) {
            if (!matchOver) togglePause();
        }
    }

    @Override public void mouseEntered(MouseEvent e)  {}
    @Override public void mouseExited(MouseEvent e)   {}
    @Override public void keyReleased(KeyEvent e)     {}
    @Override public void keyTyped(KeyEvent e)        {}
}
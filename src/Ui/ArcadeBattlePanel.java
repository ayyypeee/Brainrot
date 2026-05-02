    package Ui;
    
    import entities.Character;
    import entities.CharSkill;
    import entities.CharSkillDB;
    import entities.CharacterFactory;
    import java.awt.*;
    import java.awt.event.*;
    import java.awt.image.BufferedImage;
    import java.util.ArrayList;
    import javax.imageio.ImageIO;
    import javax.swing.*;
    
    public class ArcadeBattlePanel extends JPanel implements MouseListener, MouseMotionListener, KeyListener {
    
        private final int playerIndex;
        private final ArrayList<Integer> opponentOrder;
        private final int currentOpponentPos;
        private final CharacterFactory factory;
        private final Class<?> loader;
        private final String playerName;
    
        private Character player;
        private Character opponent;
    
        private static final boolean PLAYER_FACES_RIGHT = true;
        private static final boolean OPPONENT_FACES_RIGHT = false;
    
        private Image bgImage;
        private BufferedImage boxImage, platformImage;
    
        private final BattleEngine engine;
    
        private enum Phase { IDLE, APPROACHING, ANIMATING, IMPACTED, RETREATING, PASSIVE_MSG }
        private Phase phase = Phase.IDLE;
    
        private boolean gameOver = false;
        private String winner = "";
        private boolean arcadeOver = false;
    
        private long arcadeStartTime = 0;
        private long elapsedSeconds = 0;
        private Timer secondTicker;
    
        private boolean paused = false;
        private long pauseStartTime = 0;
        private long totalPausedMs = 0;
    
        private Timer gameTimer, opponentDelayTimer;
    
        private int groundY, btnW, btnH, btnY;
        private boolean layoutReady = false;
    
        private SkillButton btn1, btn2, btn3;
        private Point mousePos = new Point();
        private int hoveredSkill = 0;
    
        private int screenW, screenH;
        private JFrame window;
    
        // ── Pause menu ────────────────────────────────────────────────────────────
        private PauseMenuOverlay pauseMenu;
    
        public ArcadeBattlePanel(int playerIndex, ArrayList<Integer> opponentOrder,
                                 int currentOpponentPos, CharacterFactory factory,
                                 int sw, int sh, Class<?> loader, String playerName) {
            this.playerIndex = playerIndex;
            this.opponentOrder = opponentOrder;
            this.currentOpponentPos = currentOpponentPos;
            this.factory = factory;
            this.loader = loader;
            this.screenW = sw; this.screenH = sh;
            this.playerName = playerName;
    
            int opIndex = opponentOrder.get(currentOpponentPos);
    
            player = factory.buildCharacter(playerIndex, loader, sw, sh);
            opponent = factory.buildCharacter(opIndex, loader, sw, sh);
    
            String pName = factory.getName(playerIndex);
            String oName = factory.getName(opIndex);
            MusicPlayer.stop();
            engine = new BattleEngine(pName, pName, oName, oName);
            MusicPlayer.playIngame();
    
            loadAssets(factory.getHeadPath(playerIndex), factory.getHeadPath(opIndex));
            player.setImageObserver(this);
            opponent.setImageObserver(this);
    
            pauseMenu = new PauseMenuOverlay(loader);
    
            setFocusable(true);
            addMouseListener(this);
            addMouseMotionListener(this);
            addKeyListener(this);
    
            gameTimer = new Timer(16, e -> update());
            gameTimer.start();
    
            if (currentOpponentPos == 0) arcadeStartTime = System.currentTimeMillis();
    
            secondTicker = new Timer(1000, e -> {
                if (!paused && !gameOver) {
                    elapsedSeconds = (System.currentTimeMillis() - arcadeStartTime - totalPausedMs) / 1000;
                    repaint();
                }
            });
            secondTicker.start();
    
            setupWindow(currentOpponentPos + 1, opponentOrder.size());
            SwingUtilities.invokeLater(() -> { calculateLayout(); placeCharacters(); });
    
            engine.enqueueDialogue("Fight " + (currentOpponentPos + 1) + " begins! " + pName + " versus " + oName + "!");
            engine.enqueueDialogue("Round 1! It is your turn. Make your move!");
        }
    
        /** Carry-over constructor — HP resets to full, timer state preserved. */
        public ArcadeBattlePanel(int playerIndex, ArrayList<Integer> opponentOrder,
                                 int currentOpponentPos, CharacterFactory factory,
                                 int sw, int sh, Class<?> loader, String playerName,
                                 long arcadeStartTime, long totalPausedMs) {
            this(playerIndex, opponentOrder, currentOpponentPos, factory, sw, sh, loader, playerName);
            this.arcadeStartTime = arcadeStartTime;
            this.totalPausedMs   = totalPausedMs;
        }
    
        private void loadAssets(String plHead, String opHead) {
            try { bgImage = new ImageIcon(loader.getResource("/backgrounds/background.png")).getImage(); } catch (Exception e) {}
            try { boxImage = ImageIO.read(loader.getResource("/ui/v1_box_skills.png")); } catch (Exception e) {}
            try { platformImage = ImageIO.read(loader.getResource("/level_assets/PLATFORM.png")); } catch (Exception e) {}
            if (plHead != null) try { engine.head1 = ImageIO.read(loader.getResource(plHead)); } catch (Exception e) {}
            if (opHead != null) try { engine.head2 = ImageIO.read(loader.getResource(opHead)); } catch (Exception e) {}
        }
    
        private void calculateLayout() {
            int sw = getWidth(), sh = getHeight();
            screenW = sw; screenH = sh;
            btnH = (int)(sh * 0.16);
            btnW = (int)(sw * 0.28);
            int gap = (int)(sw * 0.02);
            int startX = (sw - btnW * 3 - gap * 2) / 2;
            btnY = sh - btnH - (int)(sh * 0.02);
            groundY = (int)(sh * 0.60);
            rebuildButtons(startX, gap);
            layoutReady = true;
        }
    
        private void placeCharacters() {
            int sw = getWidth();
            player.facingRight = PLAYER_FACES_RIGHT;
            opponent.facingRight = OPPONENT_FACES_RIGHT;
            player.placeOnPlatform(sw / 4 - player.getWidth() / 2, groundY);
            opponent.placeOnPlatform(sw * 3 / 4 - opponent.getWidth() / 2, groundY);
        }
    
        // ── Drop this into ArcadeBattlePanel.java, replacing the existing rebuildButtons() ──
    
        private void rebuildButtons(int startX, int gap) {
            CharSkill[] skills = CharSkillDB.getAll(factory.getName(playerIndex));
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
    
        private void setupWindow(int fightNum, int totalFights) {
            window = new JFrame("Arcade Fight " + fightNum + " of " + totalFights);
            window.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
            window.setUndecorated(true);
            window.add(this);
            GraphicsDevice gd = GraphicsEnvironment.getLocalGraphicsEnvironment().getDefaultScreenDevice();
            if (gd.isFullScreenSupported()) gd.setFullScreenWindow(window);
            else { window.setExtendedState(JFrame.MAXIMIZED_BOTH); window.setVisible(true); }
        }
    
        // ── Pause toggle ──────────────────────────────────────────────────────────
        private void togglePause() {
            paused = !paused;
            if (paused) {
                pauseStartTime = System.currentTimeMillis();
                gameTimer.stop();
                secondTicker.stop();
                if (opponentDelayTimer != null) opponentDelayTimer.stop();
                pauseMenu.reset();
            } else {
                totalPausedMs += System.currentTimeMillis() - pauseStartTime;
                gameTimer.start();
                secondTicker.start();
            }
            repaint();
        }
    
        private PauseMenuOverlay.Callbacks pauseCallbacks() {
            return new PauseMenuOverlay.Callbacks() {
                @Override public void onResume()   { togglePause(); }
                @Override public void onRestart()  {
                    // Restart arcade from fight 0
                    gameTimer.stop();
                    secondTicker.stop();
                    if (opponentDelayTimer != null) opponentDelayTimer.stop();
                    window.dispose();
                    new ArcadeBattlePanel(playerIndex, opponentOrder, 0,
                            factory, screenW, screenH, loader, playerName);
                }
                @Override public void onMainMenu() {
                    gameTimer.stop();
                    secondTicker.stop();
                    if (opponentDelayTimer != null) opponentDelayTimer.stop();
                    window.dispose();
                    new LevelSelect();
                }
                @Override public void onExit()     { System.exit(0); }
            };
        }
    
        // ── Game loop ─────────────────────────────────────────────────────────────
        private void update() {
            if (gameOver || paused) return;
    
            engine.tickDialogue();
            if (engine.isDialogueActive()) { repaint(); return; }
    
            player.update();
            opponent.update();
    
            if (!player.isAnyCastingSkill()) player.facingRight = PLAYER_FACES_RIGHT;
            if (!opponent.isAnyCastingSkill()) opponent.facingRight = OPPONENT_FACES_RIGHT;
    
            Character attacker = engine.turnSide == 1 ? player : opponent;
    
            switch (phase) {
    
                case APPROACHING:
                    if (attacker.isCastingSkill1() || attacker.isCastingSkill2() || attacker.isCastingSkill3()) {
                        phase = Phase.ANIMATING;
                    }
                    break;
    
                case ANIMATING:
                    boolean stillAnim = attacker.isCastingSkill1()
                            || attacker.isCastingSkill2()
                            || attacker.isCastingSkill3();
                    if (!stillAnim) {
                        engine.applyPendingDamage();
                        checkGameOver();
                        phase = Phase.IMPACTED;
                    }
                    break;
    
                case IMPACTED:
                    if (!attacker.isAnyCastingSkill()) {
                        phase = Phase.RETREATING;
                    }
                    break;
    
                case RETREATING:
                    if (!attacker.isAnyCastingSkill()) {
                        engine.drainPassiveQueue();
                        if (engine.isDialogueActive() || engine.hasPassiveMessages()) {
                            phase = Phase.PASSIVE_MSG;
                        } else {
                            finishTurn();
                        }
                    }
                    break;
    
                case PASSIVE_MSG:
                    if (!engine.isDialogueActive()) {
                        finishTurn();
                    }
                    break;
    
                default:
                    break;
            }
    
            updateButtonStates();
            repaint();
        }

        private void finishTurn() {
            if (gameOver) return;
            int prevSide = engine.turnSide;
            engine.endTurn(prevSide);
            phase = Phase.IDLE;

            engine.beginTurn(engine.turnSide);

            if (engine.isSideStunned(engine.turnSide)) {
                String who = engine.turnSide == 1 ? engine.label1 : engine.label2;
                engine.enqueueDialogue(who + " is stunned and has to skip their turn!");
                engine.consumeStun(engine.turnSide);
                engine.tickEffectsForSide(engine.turnSide);   // NEW
                engine.endTurn(engine.turnSide);
                engine.beginTurn(engine.turnSide);
            }

            engine.tickEffectsForSide(engine.turnSide);       // NEW

            if (engine.turnSide == 1) {
                engine.enqueueDialogue("Your turn! Choose your next attack.");
            } else {
                scheduleOpponentTurn();
            }
        }
    
        private void scheduleOpponentTurn() {
            if (gameOver) return;
            opponentDelayTimer = new Timer(1200, e -> {
                if (!gameOver && !paused) {
                    int skill = pickOpponentSkill();
                    engine.resolveSkill(2, skill);
                    opponent.startAttack(skill, player.getX() + player.getWidth() / 2);
                    phase = Phase.APPROACHING;
                }
                ((Timer) e.getSource()).stop();
            });
            opponentDelayTimer.setRepeats(false);
            opponentDelayTimer.start();
        }
    
        private int pickOpponentSkill() {
            ArrayList<Integer> avail = new ArrayList<>();
            for (int s = 1; s <= 3; s++) {
                if (engine.canUseSkill(2, s)) avail.add(s);
            }
            if (avail.isEmpty()) return 1;
            return avail.get(new java.util.Random().nextInt(avail.size()));
        }
    
        private void checkGameOver() {
            if (engine.stats2.hp <= 0) {
                engine.stats2.hp = 0;
                gameOver = true;
                int nextPos = currentOpponentPos + 1;
                if (nextPos >= opponentOrder.size()) {
                    winner = "YOU BEAT ARCADE MODE!";
                    arcadeOver = true;
                } else {
                    winner = "OPPONENT DEFEATED! Get ready for the next fight...";
                }
                endFight();
            } else if (engine.stats1.hp <= 0) {
                engine.stats1.hp = 0;
                gameOver = true;
                winner = "GAME OVER! YOU LOST!";
                arcadeOver = true;
                endFight();
            }
        }
    
        private void endFight() {
            gameTimer.stop();
            secondTicker.stop();
            if (opponentDelayTimer != null) opponentDelayTimer.stop();
    
            long finalMs = System.currentTimeMillis() - arcadeStartTime - totalPausedMs;
            elapsedSeconds = finalMs / 1000;
    
            repaint();
    
            int delay = arcadeOver ? 1500 : 1000;
            new Timer(delay, e -> {
                if (!arcadeOver) {
                    window.dispose();
                    new ArcadeBattlePanel(
                            playerIndex, opponentOrder, currentOpponentPos + 1,
                            factory, screenW, screenH, loader, playerName,
                            arcadeStartTime, totalPausedMs);
                } else {
                    long totalSecs = elapsedSeconds;
                    if (engine.stats1.hp > 0)
                        ArcadeLeaderboard.addEntry(playerName,
                                factory.getName(playerIndex), totalSecs);
    
                    String timeStr = formatTime(totalSecs);
                    String msg = winner
                            + (engine.stats1.hp > 0 ? "\nYou survived with "
                            + (int)Math.round(engine.stats1.hp * 100.0 / entities.BattleStats.MAX_HP)
                            + " percent HP!" : "")
                            + "\nTime: " + timeStr;
    
                    Object[] options = { "Play Again", "Leaderboard", "Main Menu" };
                    int choice = JOptionPane.showOptionDialog(window, msg, "Arcade Over",
                            JOptionPane.DEFAULT_OPTION, JOptionPane.PLAIN_MESSAGE,
                            null, options, options[0]);
    
                    if (choice == 0) { window.dispose(); new LevelSelect(); }
                    else if (choice == 1) { showLeaderboard(); }
                    else { window.dispose(); new LevelSelect(); }
                }
            }) {{ setRepeats(false); start(); }};
        }
    
        private void showLeaderboard() {
            java.util.List<ArcadeLeaderboard.Entry> entries = ArcadeLeaderboard.getEntries();
            StringBuilder sb = new StringBuilder();
            sb.append(String.format("%-4s %-14s %-14s %s%n", "#", "Player", "Character", "Time"));
            sb.append("─".repeat(46)).append("\n");
            int rank = 1;
            for (ArcadeLeaderboard.Entry en : entries)
                sb.append(String.format("%-4d %-14s %-14s %s%n",
                        rank++, en.playerName, en.characterName, formatTime(en.seconds)));
            if (entries.isEmpty()) sb.append("No entries yet!");
    
            JTextArea ta = new JTextArea(sb.toString());
            ta.setFont(new Font(Font.MONOSPACED, Font.PLAIN, 14));
            ta.setEditable(false);
            ta.setBackground(new Color(20, 20, 40));
            ta.setForeground(Color.WHITE);
            JScrollPane sp = new JScrollPane(ta);
            sp.setPreferredSize(new Dimension(460, 300));
    
            Object[] opts = { "Play Again", "Main Menu" };
            JOptionPane.showOptionDialog(window, sp, "Leaderboard",
                    JOptionPane.DEFAULT_OPTION, JOptionPane.PLAIN_MESSAGE, null, opts, opts[0]);
            window.dispose();
            new LevelSelect();
        }
    
        private void updateButtonStates() {
            if (!layoutReady || btn1 == null) return;
            boolean busy = phase != Phase.IDLE
                    || engine.isDialogueActive()
                    || engine.turnSide != 1
                    || paused;
    
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
            else { g2.setColor(new Color(20, 20, 40)); g2.fillRect(0, 0, sw, sh); }
    
            drawGround(g2, sw, sh);
    
            if (engine.turnSide == 1 && player.isAnyCastingSkill()) {
                opponent.draw(g2, this); player.draw(g2, this);
            } else if (engine.turnSide == 2 && opponent.isAnyCastingSkill()) {
                player.draw(g2, this); opponent.draw(g2, this);
            } else {
                player.draw(g2, this); opponent.draw(g2, this);
            }
    
            engine.drawHUD(g2, sw, sh, this, null, engine.turnSide == 1, engine.round, false);
            drawFightTimer(g2, sw, sh);
            drawSkillButtons(g2, sw, sh);
            engine.drawDialogueBox(g2, sw, sh);
    
            if (hoveredSkill > 0 && !engine.isDialogueActive())
                engine.drawSkillTooltip(g2, 1, hoveredSkill, mousePos.x, mousePos.y, sw, sh);
    
            engine.tooltipX = mousePos.x;
            engine.tooltipY = mousePos.y;
            engine.tooltipText = "hover";
    
            if (!engine.isDialogueActive() && !gameOver && phase == Phase.IDLE)
                drawTurnLabel(g2, sw, sh);
    
            if (gameOver) drawGameOverOverlay(g2, sw, sh);
    
            // Pause menu on top of everything
            if (paused) pauseMenu.draw(g2, sw, sh);
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
    
        private void drawFightTimer(Graphics2D g2, int sw, int sh) {
            int cx = sw / 2;
            int baseY = (int)(sh * 0.145);
    
            int fightFs = Math.max(13, (int)(sh * 0.024));
            int timerFs = Math.max(12, (int)(sh * 0.020));
    
            String fightStr = "Fight " + (currentOpponentPos + 1) + " of " + opponentOrder.size();
            g2.setFont(new Font(Font.MONOSPACED, Font.BOLD, fightFs));
            FontMetrics fightFm = g2.getFontMetrics();
    
            int fightW = fightFm.stringWidth(fightStr) + 20;
            int fightH = fightFs + 8;
            g2.setColor(new Color(0, 0, 0, 160));
            g2.fillRoundRect(cx - fightW / 2, baseY, fightW, fightH, 10, 10);
            g2.setColor(new Color(255, 200, 80));
            g2.drawString(fightStr, cx - fightFm.stringWidth(fightStr) / 2, baseY + fightFs);
    
            int timerY = baseY + fightH + 4;
            String timeStr = "\u23F1 " + formatTime(elapsedSeconds);
            g2.setFont(new Font(Font.MONOSPACED, Font.BOLD, timerFs));
            FontMetrics timerFm = g2.getFontMetrics();
    
            int timerW = timerFm.stringWidth(timeStr) + 20;
            int timerH = timerFs + 8;
            g2.setColor(new Color(0, 0, 0, 160));
            g2.fillRoundRect(cx - timerW / 2, timerY, timerW, timerH, 10, 10);
            g2.setColor(Color.WHITE);
            g2.drawString(timeStr, cx - timerFm.stringWidth(timeStr) / 2, timerY + timerFs);
        }
    
        private void drawTurnLabel(Graphics2D g2, int sw, int sh) {
            String opName = factory.getName(opponentOrder.get(currentOpponentPos));
            String lbl = engine.turnSide == 1 ? "YOUR TURN!\nMake your move!" : opName.toUpperCase() + "\nIS PLANNING...";
            Color color = engine.turnSide == 1 ? new Color(100, 180, 255) : new Color(255, 80, 80);
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
    
        private void drawGameOverOverlay(Graphics2D g2, int sw, int sh) {
            g2.setColor(new Color(0, 0, 0, 160)); g2.fillRect(0, 0, sw, sh);
            int fs = Math.max(36, (int)(sh * 0.09));
            g2.setFont(new Font(Font.MONOSPACED, Font.BOLD, fs));
            g2.setColor(arcadeOver && engine.stats1.hp > 0 ? new Color(80, 255, 80) : Color.YELLOW);
            g2.drawString(winner, sw / 2 - g2.getFontMetrics().stringWidth(winner) / 2, sh / 2);
        }
    
        private String formatTime(long totalSeconds) {
            long m = totalSeconds / 60, s = totalSeconds % 60;
            return String.format("%02d:%02d", m, s);
        }
    
        private void handleSkillClick(int skillNum) {
            if (gameOver || paused || phase != Phase.IDLE || engine.turnSide != 1) return;
            if (engine.isDialogueActive()) return;
            if (!engine.canUseSkill(1, skillNum)) return;
    
            engine.resolveSkill(1, skillNum);
            player.startAttack(skillNum, opponent.getX() + opponent.getWidth() / 2);
            phase = Phase.APPROACHING;
        }
    
        // ── Mouse ─────────────────────────────────────────────────────────────────
        @Override
        public void mouseClicked(MouseEvent e) {
            if (paused) {
                pauseMenu.handleClick(e.getPoint(), pauseCallbacks());
                repaint();
                return;
            }
            if (gameOver || !layoutReady) return;
            if (engine.isDialogueActive()) { engine.advanceDialogue(); repaint(); return; }
            if (phase != Phase.IDLE || engine.turnSide != 1) return;
            Point pt = e.getPoint();
            if (btn1.contains(pt)) handleSkillClick(1);
            else if (btn2.contains(pt)) handleSkillClick(2);
            else if (btn3.contains(pt)) handleSkillClick(3);
        }
    
        @Override
        public void mousePressed(MouseEvent e) {
            if (paused) { pauseMenu.handleMousePressed(e.getPoint()); repaint(); }
        }
    
        @Override
        public void mouseReleased(MouseEvent e) {
            if (paused) pauseMenu.handleRelease();
        }
    
        @Override
        public void mouseMoved(MouseEvent e) {
            mousePos = e.getPoint();
            if (paused) {
                pauseMenu.handleHover(e.getPoint());
                repaint();
                return;
            }
            hoveredSkill = btn1 != null && btn1.getBounds().contains(mousePos) ? 1
                    : btn2 != null && btn2.getBounds().contains(mousePos) ? 2
                    : btn3 != null && btn3.getBounds().contains(mousePos) ? 3 : 0;
            setCursor(hoveredSkill > 0
                    ? Cursor.getPredefinedCursor(Cursor.HAND_CURSOR)
                    : Cursor.getDefaultCursor());
            repaint();
        }
    
        @Override
        public void mouseDragged(MouseEvent e) {
            if (paused) { pauseMenu.handleDrag(e.getPoint()); repaint(); }
        }
    
        // ── Keys ──────────────────────────────────────────────────────────────────
        @Override
        public void keyPressed(KeyEvent e) {
            int k = e.getKeyCode();
            if (k == KeyEvent.VK_ENTER || k == KeyEvent.VK_SPACE) {
                if (paused) return;
                if (engine.isDialogueActive()) { engine.advanceDialogue(); repaint(); }
            } else if (k == KeyEvent.VK_ESCAPE) {
                if (!gameOver) togglePause();
            }
        }
    
        @Override public void mouseEntered(MouseEvent e) {}
        @Override public void mouseExited(MouseEvent e) {}
        @Override public void keyReleased(KeyEvent e) {}
        @Override public void keyTyped(KeyEvent e) {}
    }
package Ui;

import entities.Character;
import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.awt.image.BufferedImage;
import javax.imageio.ImageIO;

public class BattlePanel extends JPanel implements KeyListener {

    private JFrame window;
    private Image  bgImage;

    private Character p1;
    private Character p2;

    private Timer gameTimer;

    private BufferedImage heartSprite;
    private BufferedImage p1HeadImg;
    private BufferedImage p2HeadImg;

    private final int MAX_HP = 5;
    private int p1HP;
    private int p2HP;

    private int  skillTurn;
    private boolean skillUsedThisTurn;
    private boolean damageDealt;

    private boolean gameOver;
    private String winner;

    private boolean p1Left;
    private boolean p1Right;
    private boolean p2Left;
    private boolean p2Right;

    public BattlePanel(Character p1, Character p2,
                       String p1HeadPath, String p2HeadPath,
                       Class<?> loader) {
        this.p1 = p1;
        this.p2 = p2;

        p1HP              = MAX_HP;
        p2HP              = MAX_HP;
        skillTurn         = 1;
        skillUsedThisTurn = false;
        damageDealt       = false;
        gameOver          = false;
        winner            = "";

        p1Left  = false;
        p1Right = false;
        p2Left  = false;
        p2Right = false;

        loadBackground(loader);
        loadHeartSprite(loader);
        loadHeadPortrait(p1HeadPath, true, loader);
        loadHeadPortrait(p2HeadPath, false, loader);

        p1.setImageObserver(this);
        p2.setImageObserver(this);

        setFocusable(true);
        addKeyListener(this);

        gameTimer = new Timer(16, new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                update();
            }
        });
        gameTimer.start();

        setupWindow();

        SwingUtilities.invokeLater(new Runnable() {
            public void run() {
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

    private void loadBackground(Class<?> loader) {
        try {
            bgImage = new ImageIcon(loader.getResource(
                    "/backgrounds/background.png")).getImage();
        } catch (Exception e) {
            System.out.println("Battle background not found");
        }
    }

    private void loadHeartSprite(Class<?> loader) {
        try {
            heartSprite = ImageIO.read(loader.getResource(
                    "/level_assets/heart sprite.png"));
        } catch (Exception e) {
            System.out.println("Heart sprite not found");
        }
    }

    private void loadHeadPortrait(String path, boolean isP1, Class<?> loader) {
        if (path == null) return;

        try {
            BufferedImage portrait = ImageIO.read(loader.getResource(path));
            if (isP1) {
                p1HeadImg = portrait;
            } else {
                p2HeadImg = portrait;
            }
        } catch (Exception e) {
            System.out.println("Head portrait not found: " + path);
        }
    }

    private void setupWindow() {
        window = new JFrame("Human vs Brainrot BATTLE");
        window.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        window.setUndecorated(true);
        window.add(this);

        GraphicsEnvironment ge = GraphicsEnvironment.getLocalGraphicsEnvironment();
        GraphicsDevice gd      = ge.getDefaultScreenDevice();

        if (gd.isFullScreenSupported()) {
            gd.setFullScreenWindow(window);
        } else {
            window.setExtendedState(JFrame.MAXIMIZED_BOTH);
            window.setVisible(true);
        }
    }

    private void update() {
        if (gameOver) return;

        p1.movingRight = p1Right;
        p1.movingLeft  = p1Left;
        p2.movingRight = p2Right;
        p2.movingLeft  = p2Left;

        // Facing direction updated every frame based on movement state
        if (p1Right) p1.facingRight = true;
        if (p1Left)  p1.facingRight = false;
        if (p2Right) p2.facingRight = true;
        if (p2Left)  p2.facingRight = false;

        p1.update();
        p2.update();

        if (skillUsedThisTurn && !damageDealt) {
            boolean p1IsCasting = p1.isCastingSkill1() || p1.isCastingSkill2();
            boolean p2IsCasting = p2.isCastingSkill1() || p2.isCastingSkill2();

            if (skillTurn == 1 && p1IsCasting) {
                p2HP--;
                damageDealt = true;
                checkGameOver();
            }

            if (skillTurn == 2 && p2IsCasting) {
                p1HP--;
                damageDealt = true;
                checkGameOver();
            }
        }

        if (skillUsedThisTurn && damageDealt) {
            boolean p1SkillDone = !p1.isCastingSkill1() && !p1.isCastingSkill2();
            boolean p2SkillDone = !p2.isCastingSkill1() && !p2.isCastingSkill2();

            if (skillTurn == 1 && p1SkillDone) {
                skillTurn         = 2;
                skillUsedThisTurn = false;
                damageDealt       = false;
            } else if (skillTurn == 2 && p2SkillDone) {
                skillTurn         = 1;
                skillUsedThisTurn = false;
                damageDealt       = false;
            }
        }

        p1.setScreenBounds(getWidth());
        p2.setScreenBounds(getWidth());

        repaint();
    }

    private void checkGameOver() {
        if (p1HP <= 0) {
            p1HP     = 0;
            gameOver = true;
            winner   = "PLAYER 2 WINS!";
            gameTimer.stop();
            showGameOverDialog();

        } else if (p2HP <= 0) {
            p2HP     = 0;
            gameOver = true;
            winner   = "PLAYER 1 WINS!";
            gameTimer.stop();
            showGameOverDialog();
        }
    }

    private void showGameOverDialog() {
        repaint();

        Timer endTimer = new Timer(1500, new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                int choice = JOptionPane.showConfirmDialog(
                        window,
                        winner + "\n\nPlay again?",
                        "Game Over",
                        JOptionPane.YES_NO_OPTION,
                        JOptionPane.PLAIN_MESSAGE
                );

                if (choice == JOptionPane.YES_OPTION) {
                    window.dispose();
                    new CharacterSelect();
                } else {
                    System.exit(0);
                }
            }
        });

        endTimer.setRepeats(false);
        endTimer.start();
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2 = (Graphics2D) g;

        if (bgImage != null) {
            g2.drawImage(bgImage, 0, 0, getWidth(), getHeight(), this);
        } else {
            g2.setColor(new Color(20, 40, 20));
            g2.fillRect(0, 0, getWidth(), getHeight());
        }

        p1.draw(g2, this);
        p2.draw(g2, this);

        drawHUD(g2);

        if (gameOver) drawGameOverOverlay(g2);
    }

    private void drawHUD(Graphics2D g2) {
        int headSize  = 64;
        int heartSize = 36;
        int heartGap  = 4;
        int topY      = 16;

        if (p1HeadImg != null) {
            g2.drawImage(p1HeadImg, 10, topY, headSize, headSize, this);
        } else {
            g2.setColor(new Color(100, 200, 255));
            g2.fillRect(10, topY, headSize, headSize);
        }

        int heartStartX = 10 + headSize + 8;
        for (int i = 0; i < MAX_HP; i++) {
            int heartX = heartStartX + i * (heartSize + heartGap);
            int heartY = topY + (headSize - heartSize) / 2;

            if (i < p1HP) {
                if (heartSprite != null) {
                    g2.drawImage(heartSprite, heartX, heartY,
                            heartSize, heartSize, this);
                }
            } else {
                g2.setColor(new Color(60, 0, 0));
                g2.fillRect(heartX, heartY, heartSize, heartSize);
            }
        }

        int p2HeadX = getWidth() - headSize - 10;

        if (p2HeadImg != null) {
            g2.drawImage(p2HeadImg, p2HeadX, topY, headSize, headSize, this);
        } else {
            g2.setColor(new Color(255, 150, 100));
            g2.fillRect(p2HeadX, topY, headSize, headSize);
        }

        int p2HeartsStartX = p2HeadX - 8 - MAX_HP * (heartSize + heartGap);
        for (int i = 0; i < MAX_HP; i++) {
            int heartX = p2HeartsStartX + i * (heartSize + heartGap);
            int heartY = topY + (headSize - heartSize) / 2;

            if (i < p2HP) {
                if (heartSprite != null) {
                    g2.drawImage(heartSprite, heartX, heartY,
                            heartSize, heartSize, this);
                }
            } else {
                g2.setColor(new Color(60, 0, 0));
                g2.fillRect(heartX, heartY, heartSize, heartSize);
            }
        }

        g2.setFont(new Font("Courier New", Font.BOLD, 20));

        String turnText;
        if (skillTurn == 1) {
            turnText = "P1 TURN";
            g2.setColor(new Color(100, 200, 255));
        } else {
            turnText = "P2 TURN";
            g2.setColor(new Color(255, 150, 100));
        }

        int turnX = (getWidth() - g2.getFontMetrics().stringWidth(turnText)) / 2;
        g2.drawString(turnText, turnX, topY + 44);

        g2.setFont(new Font("Courier New", Font.PLAIN, 14));
        g2.setColor(Color.WHITE);
        g2.drawString("P1: A/D move   E/R skill", 20, getHeight() - 16);
        g2.drawString("P2: ← → move   , . skill", getWidth() - 240, getHeight() - 16);
    }

    private void drawGameOverOverlay(Graphics2D g2) {
        g2.setColor(new Color(0, 0, 0, 160));
        g2.fillRect(0, 0, getWidth(), getHeight());

        g2.setFont(new Font("Courier New", Font.BOLD, 72));
        g2.setColor(Color.YELLOW);
        int textX = (getWidth() - g2.getFontMetrics().stringWidth(winner)) / 2;
        g2.drawString(winner, textX, getHeight() / 2);
    }

    @Override
    public void keyPressed(KeyEvent e) {
        if (gameOver) return;

        int key = e.getKeyCode();

        // P1 movement
        if (key == KeyEvent.VK_A) {
            p1Left  = true;
        } else if (key == KeyEvent.VK_D) {
            p1Right = true;

            // P1 skills
        } else if (key == KeyEvent.VK_E) {
            if (skillTurn == 1 && !skillUsedThisTurn
                    && !p1.isAnyCastingSkill() && p1.hasSkill1()) {
                p1.triggerSkill1();
                skillUsedThisTurn = true;
            }
        } else if (key == KeyEvent.VK_R) {
            if (skillTurn == 1 && !skillUsedThisTurn
                    && !p1.isAnyCastingSkill() && p1.hasSkill2()) {
                p1.triggerSkill2();
                skillUsedThisTurn = true;
            }

            // P2 movement
        } else if (key == KeyEvent.VK_LEFT) {
            p2Left  = true;
        } else if (key == KeyEvent.VK_RIGHT) {
            p2Right = true;

            // P2 skills
        } else if (key == KeyEvent.VK_COMMA) {
            if (skillTurn == 2 && !skillUsedThisTurn
                    && !p2.isAnyCastingSkill() && p2.hasSkill1()) {
                p2.triggerSkill1();
                skillUsedThisTurn = true;
            }
        } else if (key == KeyEvent.VK_PERIOD) {
            if (skillTurn == 2 && !skillUsedThisTurn
                    && !p2.isAnyCastingSkill() && p2.hasSkill2()) {
                p2.triggerSkill2();
                skillUsedThisTurn = true;
            }
        }
    }

    @Override
    public void keyReleased(KeyEvent e) {
        int key = e.getKeyCode();

        if (key == KeyEvent.VK_A)     { p1Left  = false; }
        if (key == KeyEvent.VK_D)     { p1Right = false; }
        if (key == KeyEvent.VK_LEFT)  { p2Left  = false; }
        if (key == KeyEvent.VK_RIGHT) { p2Right = false; }
    }

    @Override public void keyTyped(KeyEvent e) {}
}
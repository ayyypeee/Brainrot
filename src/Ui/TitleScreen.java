package Ui;

import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.Clip;
import javax.swing.*;
import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.event.*;
import java.awt.image.BufferedImage;

public class TitleScreen extends JPanel implements KeyListener, MouseListener, MouseMotionListener {

    private JFrame window;
    private BufferedImage bgImage;

    private BufferedImage startImg;
    private BufferedImage aboutImg;
    private BufferedImage exitImg;

    private final int BTN_SRC_X = 32;
    private final int BTN_SRC_Y = 184;
    private final int BTN_SRC_W = 448;
    private final int BTN_SRC_H = 144;

    private Rectangle startRect = new Rectangle();
    private Rectangle aboutRect = new Rectangle();
    private Rectangle exitRect  = new Rectangle();

    private boolean hoverStart = false;
    private boolean hoverAbout = false;
    private boolean hoverExit  = false;

    private int btnW, btnH;
    private int btn1X, btn1Y, btn2Y, btn3Y;
    private boolean layoutReady = false;

    // ── About overlay ─────────────────────────────────────────────────────────
    private final AboutOverlay aboutOverlay = new AboutOverlay();

    // ── Repaint timer for overlay slide-in animation ──────────────────────────
    private Timer repaintTimer;

    // ── Logged-in username forwarded from LoginScreen ─────────────────────────
    private final String loggedInName;

    // ── Constructor accepting username (called from LoginScreen) ──────────────
    public TitleScreen(String loggedInName) {
        this.loggedInName = (loggedInName != null && !loggedInName.isEmpty())
                ? loggedInName : "Player";

        loadImages();
        MusicPlayer.playMenu();

        setFocusable(true);
        addKeyListener(this);
        addMouseListener(this);
        addMouseMotionListener(this);

        window = new JFrame("Guardians of Sanity");
        window.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        window.setUndecorated(true);
        window.add(this);

        GraphicsEnvironment ge = GraphicsEnvironment.getLocalGraphicsEnvironment();
        GraphicsDevice gd = ge.getDefaultScreenDevice();
        if (gd.isFullScreenSupported()) {
            gd.setFullScreenWindow(window);
        } else {
            window.setExtendedState(JFrame.MAXIMIZED_BOTH);
            window.setVisible(true);
        }

        SwingUtilities.invokeLater(() -> {
            calculateLayout();
            layoutReady = true;
            repaint();
        });

        // Drives the overlay slide-in animation at 60fps
        repaintTimer = new Timer(16, e -> {
            if (aboutOverlay.isOpen()) repaint();
        });
        repaintTimer.start();

        requestFocusInWindow();
    }

    // ── Backward-compatible no-arg constructor ────────────────────────────────
    public TitleScreen() {
        this("Player");
    }

    private void loadImages() {
        String[] bgPaths = {
                "/backgrounds/FINAL_TITLE_SCREEN.png",
                "/backgrounds/finalbg.png",
                "/backgrounds/bg.png",
                "/FINAL_TITLE_SCREEN.png",
                "/finalbg.png"
        };
        for (String path : bgPaths) {
            try { bgImage = ImageIO.read(getClass().getResource(path)); break; }
            catch (Exception e) {}
        }

        String[] startPaths = {"/ui/v1_start_button.png", "/v1_start_button.png", "/backgrounds/v1_start_button.png"};
        for (String path : startPaths) {
            try { startImg = ImageIO.read(getClass().getResource(path)); break; }
            catch (Exception e) {}
        }

        String[] aboutPaths = {"/ui/v1_about_button.png", "/v1_about_button.png", "/backgrounds/v1_about_button.png"};
        for (String path : aboutPaths) {
            try { aboutImg = ImageIO.read(getClass().getResource(path)); break; }
            catch (Exception e) {}
        }

        String[] exitPaths = {"/ui/v1_exit_button.png", "/v1_exit_button.png", "/backgrounds/v1_exit_button.png"};
        for (String path : exitPaths) {
            try { exitImg = ImageIO.read(getClass().getResource(path)); break; }
            catch (Exception e) {}
        }
    }

    private void calculateLayout() {
        int sw = getWidth(), sh = getHeight();
        btnH  = (int)(sh * 0.09);
        btnW  = (int)(btnH * (447.0 / 143.0));
        btn1X = (sw - btnW) / 2;
        int gap = (int)(sh * 0.025);
        btn1Y = (int)(sh * 0.55);
        btn2Y = btn1Y + btnH + gap;
        btn3Y = btn2Y + btnH + gap;
        startRect.setBounds(btn1X, btn1Y, btnW, btnH);
        aboutRect.setBounds(btn1X, btn2Y, btnW, btnH);
        exitRect .setBounds(btn1X, btn3Y, btnW, btnH);
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2 = (Graphics2D) g;
        g2.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        if (bgImage != null) {
            g2.drawImage(bgImage, 0, 0, getWidth(), getHeight(), this);
        } else {
            g2.setColor(new Color(8, 28, 16));
            g2.fillRect(0, 0, getWidth(), getHeight());
            g2.setColor(Color.WHITE);
            g2.setFont(new Font(Font.MONOSPACED, Font.BOLD, 20));
            g2.drawString("Background image not found — check console", 40, 40);
        }

        if (layoutReady) {
            drawButton(g2, startImg, startRect, hoverStart);
            drawButton(g2, aboutImg, aboutRect, hoverAbout);
            drawButton(g2, exitImg,  exitRect,  hoverExit);
        }

        // ── About overlay drawn on very top ───────────────────────────────────
        if (aboutOverlay.isOpen()) {
            aboutOverlay.draw(g2, getWidth(), getHeight());
        }
    }

    private void drawButton(Graphics2D g2, BufferedImage img, Rectangle rect, boolean hovered) {
        if (img == null) return;
        int dx = hovered ? (int)(rect.width * 0.04) : 0;
        int dy = hovered ? (int)(rect.height * 0.04) : 0;
        int drawX = rect.x - dx / 2, drawY = rect.y - dy / 2;
        int drawW = rect.width + dx,  drawH = rect.height + dy;

        if (hovered) {
            for (int i = 6; i >= 1; i--) {
                float a = 0.06f * i;
                g2.setColor(new Color(100, 255, 150, (int)(a * 255)));
                g2.fillRoundRect(drawX - i * 3, drawY - i * 2, drawW + i * 6, drawH + i * 4, 20, 20);
            }
        }

        g2.drawImage(img, drawX, drawY, drawX + drawW, drawY + drawH,
                BTN_SRC_X, BTN_SRC_Y, BTN_SRC_X + BTN_SRC_W, BTN_SRC_Y + BTN_SRC_H, this);
    }

    @Override
    public void mouseMoved(MouseEvent e) {
        if (aboutOverlay.isOpen()) {
            aboutOverlay.handleHover(e.getPoint());
            repaint();
            return;
        }
        hoverStart = startRect.contains(e.getPoint());
        hoverAbout = aboutRect.contains(e.getPoint());
        hoverExit  = exitRect.contains(e.getPoint());
        setCursor((hoverStart || hoverAbout || hoverExit)
                ? new Cursor(Cursor.HAND_CURSOR) : new Cursor(Cursor.DEFAULT_CURSOR));
        repaint();
    }

    @Override
    public void mouseClicked(MouseEvent e) {
        if (aboutOverlay.isOpen()) {
            aboutOverlay.handleClick(e.getPoint());
            repaint();
            return;
        }
        if      (startRect.contains(e.getPoint())) handleStart();
        else if (aboutRect.contains(e.getPoint())) handleAbout();
        else if (exitRect.contains(e.getPoint()))  handleExit();
    }

    @Override
    public void keyPressed(KeyEvent e) {
        if (aboutOverlay.isOpen()) {
            aboutOverlay.handleKey(e.getKeyCode());
            repaint();
            return;
        }
        if      (e.getKeyCode() == KeyEvent.VK_ENTER)  handleStart();
        else if (e.getKeyCode() == KeyEvent.VK_ESCAPE) handleExit();
    }

    private void handleStart() {
        repaintTimer.stop();
        window.dispose();

        new LevelSelect(loggedInName);
    }

    private void handleAbout() {
        aboutOverlay.open();
        repaint();
    }

    private void handleExit() {
        MusicPlayer.stop();
        System.exit(0);
    }

    @Override public void mousePressed(MouseEvent e)  {}
    @Override public void mouseReleased(MouseEvent e) {}
    @Override public void mouseEntered(MouseEvent e)  {}
    @Override public void mouseExited(MouseEvent e)   {}
    @Override public void mouseDragged(MouseEvent e)  {}
    @Override public void keyReleased(KeyEvent e)     {}
    @Override public void keyTyped(KeyEvent e)        {}
}
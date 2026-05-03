package Ui;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.awt.image.BufferedImage;
import javax.imageio.ImageIO;

public class LevelSelect extends JPanel implements KeyListener, MouseListener, MouseMotionListener {

    private JFrame window;
    private Image bgImage;

    private BufferedImage computerImg;
    private BufferedImage pvpImg;
    private BufferedImage arcadeImg;

    private BufferedImage goldFrame;
    private BufferedImage hoverFrame;

    private int cardW, cardH, cardY;
    private int card1X, card2X, card3X;
    private int titleFontSize, labelFontSize, descFontSize;
    private boolean layoutReady = false;

    private int selectedCard = 1;

    private Rectangle lbRect = new Rectangle();
    private boolean hoverLB = false;
    private int lbFontSize;


    private final String loggedInName;

    private final LeaderboardOverlay lbOverlay = new LeaderboardOverlay();

    private Timer repaintTimer;

    public LevelSelect(String loggedInName) {
        this.loggedInName = (loggedInName != null && !loggedInName.isEmpty())
                ? loggedInName : "Player";

        loadAssets();
        MusicPlayer.playMenu();
        setFocusable(true);
        addKeyListener(this);
        addMouseListener(this);
        addMouseMotionListener(this);
        setupWindow();
        SwingUtilities.invokeLater(() -> { calculateLayout(); repaint(); requestFocusInWindow(); });

        repaintTimer = new Timer(16, e -> { if (lbOverlay.isOpen()) repaint(); });
        repaintTimer.start();
    }

    public LevelSelect() {
        this("Player");
    }

    private void loadAssets() {
        try { bgImage = new ImageIcon(getClass().getResource("/backgrounds/background.png")).getImage(); }
        catch (Exception e) { System.out.println("Could not find the background image."); }
        try { goldFrame = ImageIO.read(getClass().getResource("/ui/v1gold frame.png")); }
        catch (Exception e) { System.out.println("Could not find the gold frame."); }
        try { hoverFrame = ImageIO.read(getClass().getResource("/ui/v1blueframe.png")); }
        catch (Exception e) { System.out.println("Could not find the blue frame."); }
        try { computerImg = ImageIO.read(getClass().getResource("/ui/computer4.png")); }
        catch (Exception e) { System.out.println("Could not find the computer icon."); }
        try { pvpImg = ImageIO.read(getClass().getResource("/ui/vs.png")); }
        catch (Exception e) { System.out.println("Could not find the versus icon."); }
        try { arcadeImg = ImageIO.read(getClass().getResource("/ui/arcade.png")); }
        catch (Exception e) { System.out.println("Could not find the arcade icon."); }
    }

    private void calculateLayout() {
        int sw = getWidth(), sh = getHeight();

        cardH = (int)(sh * 0.50);
        cardW = (int)(cardH * 0.78);
        titleFontSize = Math.max(24, (int)(sh * 0.055));
        labelFontSize = Math.max(14, (int)(sh * 0.030));
        descFontSize  = Math.max(10, (int)(sh * 0.018));
        lbFontSize    = Math.max(11, (int)(sh * 0.020));

        int gap    = (int)(sw * 0.06);
        int totalW = cardW * 3 + gap * 2;
        int startX = (sw - totalW) / 2;

        card1X = startX;
        card2X = startX + cardW + gap;
        card3X = startX + (cardW + gap) * 2;
        cardY  = (int)(sh * 0.14);

        int lbW = (int)(cardW * 0.85);
        int lbH = Math.max(32, (int)(sh * 0.046));
        int lbX = card3X + (cardW - lbW) / 2;
        int lbY = cardY + cardH + labelFontSize + descFontSize + (int)(sh * 0.048);
        lbRect.setBounds(lbX, lbY, lbW, lbH);

        layoutReady = true;
    }

    private void setupWindow() {
        window = new JFrame("Choose Game Mode");
        window.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        window.setUndecorated(true);
        window.add(this);
        GraphicsDevice gd = GraphicsEnvironment.getLocalGraphicsEnvironment().getDefaultScreenDevice();
        if (gd.isFullScreenSupported()) gd.setFullScreenWindow(window);
        else { window.setExtendedState(JFrame.MAXIMIZED_BOTH); window.setVisible(true); }
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        if (!layoutReady) calculateLayout();
        Graphics2D g2 = (Graphics2D) g;
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g2.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
        g2.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);

        int sw = getWidth(), sh = getHeight();

        if (bgImage != null) g2.drawImage(bgImage, 0, 0, sw, sh, this);
        else { g2.setColor(new Color(10, 30, 15)); g2.fillRect(0, 0, sw, sh); }

        drawTitle(g2, sw, sh);
        drawCard(g2, 1, card1X, cardY, cardW, cardH, computerImg, "Vs Computer",      "Practice against the AI");
        drawCard(g2, 2, card2X, cardY, cardW, cardH, pvpImg,      "Player vs Player", "Fight a friend locally");
        drawCard(g2, 3, card3X, cardY, cardW, cardH, arcadeImg,   "Arcade",           "Defeat all opponents");
        drawLeaderboardButton(g2);

        if (lbOverlay.isOpen()) lbOverlay.draw(g2, sw, sh);
    }

    private void drawTitle(Graphics2D g2, int sw, int sh) {
        String title = "Choose Game Mode";
        g2.setFont(new Font(Font.MONOSPACED, Font.BOLD, titleFontSize));
        int tx = sw / 2 - g2.getFontMetrics().stringWidth(title) / 2;
        int ty = (int)(sh * 0.10);
        g2.setColor(new Color(0, 0, 0, 160));
        g2.drawString(title, tx + 2, ty + 2);
        g2.setColor(Color.WHITE);
        g2.drawString(title, tx, ty);
    }

    private void drawCard(Graphics2D g2, int num, int x, int y, int w, int h,
                          BufferedImage cardImg, String label, String desc) {
        boolean selected = (selectedCard == num);
        int liftY = selected ? -(int)(h * 0.04) : 0;
        y += liftY;

        int imgPad = (int)(w * 0.07);
        if (cardImg != null)
            g2.drawImage(cardImg, x + imgPad, y + imgPad, w - imgPad * 2, h - imgPad * 2, this);

        BufferedImage frame = selected ? hoverFrame : goldFrame;
        if (frame != null) {
            g2.drawImage(frame, x, y, w, h, this);
        } else {
            g2.setColor(selected ? new Color(80, 160, 255) : new Color(200, 160, 50));
            g2.setStroke(new BasicStroke(4));
            g2.drawRect(x + 2, y + 2, w - 4, h - 4);
            g2.setStroke(new BasicStroke(1));
        }

        g2.setFont(new Font(Font.MONOSPACED, Font.BOLD, labelFontSize));
        Color labelColor = selected ? new Color(255, 220, 80) : Color.WHITE;
        int lw     = g2.getFontMetrics().stringWidth(label);
        int labelY = y + h + labelFontSize + 6;
        g2.setColor(new Color(0, 0, 0, 160));
        g2.drawString(label, x + w / 2 - lw / 2 + 2, labelY + 2);
        g2.setColor(labelColor);
        g2.drawString(label, x + w / 2 - lw / 2, labelY);

        g2.setFont(new Font(Font.MONOSPACED, Font.PLAIN, descFontSize));
        g2.setColor(new Color(180, 220, 180));
        int dw = g2.getFontMetrics().stringWidth(desc);
        g2.drawString(desc, x + w / 2 - dw / 2, labelY + descFontSize + 4);
    }

    private void drawLeaderboardButton(Graphics2D g2) {
        int x = lbRect.x, y = lbRect.y, w = lbRect.width, h = lbRect.height;

        Color bg = hoverLB ? new Color(255, 200, 40) : new Color(180, 130, 20);
        g2.setColor(bg);
        g2.fillRoundRect(x, y, w, h, 12, 12);
        g2.setColor(hoverLB ? Color.WHITE : new Color(240, 200, 80));
        g2.setStroke(new BasicStroke(2f));
        g2.drawRoundRect(x, y, w, h, 12, 12);
        g2.setStroke(new BasicStroke(1f));

        String lbText = "\uD83C\uDFC6 Leaderboard";
        g2.setFont(new Font(Font.MONOSPACED, Font.BOLD, lbFontSize));
        FontMetrics fm = g2.getFontMetrics();
        g2.setColor(hoverLB ? new Color(20, 20, 20) : new Color(30, 20, 0));
        int tx = x + (w - fm.stringWidth(lbText)) / 2;
        int ty = y + (h + fm.getAscent() - fm.getDescent()) / 2;
        g2.drawString(lbText, tx, ty);
    }

    @Override
    public void keyPressed(KeyEvent e) {
        if (lbOverlay.isOpen()) {
            lbOverlay.handleKey(e.getKeyCode());
            repaint();
            return;
        }

        int k = e.getKeyCode();
        if (k == KeyEvent.VK_RIGHT || k == KeyEvent.VK_D) {
            if (selectedCard < 3) { selectedCard++; repaint(); }
        } else if (k == KeyEvent.VK_LEFT || k == KeyEvent.VK_A) {
            if (selectedCard > 1) { selectedCard--; repaint(); }
        } else if (k == KeyEvent.VK_ENTER) {
            confirm();
        } else if (k == KeyEvent.VK_ESCAPE) {
            window.dispose();
            new TitleScreen(loggedInName);
        }
    }

    private void confirm() {
        window.dispose();
        switch (selectedCard) {
            // ── KEY CHANGE: pass loggedInName to CharacterSelect for Arcade ──
            case 1: new CharacterSelect(CharacterSelect.Mode.VS_AI,  loggedInName); break;
            case 2: new CharacterSelect(CharacterSelect.Mode.PVP,    loggedInName); break;
            case 3: new CharacterSelect(CharacterSelect.Mode.ARCADE, loggedInName); break;
        }
    }

    @Override
    public void mouseClicked(MouseEvent e) {
        if (lbOverlay.isOpen()) {
            lbOverlay.handleClick(e.getPoint());
            repaint();
            return;
        }

        Point pt = e.getPoint();
        if (lbRect.contains(pt)) {
            lbOverlay.open();
            repaint();
            return;
        }
        int[] xs = { card1X, card2X, card3X };
        for (int i = 0; i < xs.length; i++) {
            if (new Rectangle(xs[i], cardY, cardW, cardH).contains(pt)) {
                selectedCard = i + 1;
                repaint();
                confirm();
                return;
            }
        }
    }

    @Override
    public void mouseMoved(MouseEvent e) {
        if (lbOverlay.isOpen()) {
            lbOverlay.handleHover(e.getPoint());
            setCursor(Cursor.getPredefinedCursor(Cursor.DEFAULT_CURSOR));
            repaint();
            return;
        }

        Point pt = e.getPoint();
        hoverLB = lbRect.contains(pt);
        boolean onCard = false;
        int[] xs = { card1X, card2X, card3X };
        for (int i = 0; i < xs.length; i++) {
            if (new Rectangle(xs[i], cardY, cardW, cardH).contains(pt)) {
                selectedCard = i + 1; onCard = true; break;
            }
        }
        setCursor((hoverLB || onCard)
                ? Cursor.getPredefinedCursor(Cursor.HAND_CURSOR)
                : Cursor.getDefaultCursor());
        repaint();
    }

    @Override public void keyReleased(KeyEvent e)    {}
    @Override public void keyTyped(KeyEvent e)       {}
    @Override public void mousePressed(MouseEvent e) {}
    @Override public void mouseReleased(MouseEvent e){}
    @Override public void mouseEntered(MouseEvent e) {}
    @Override public void mouseExited(MouseEvent e)  {}
    @Override public void mouseDragged(MouseEvent e) {}
}
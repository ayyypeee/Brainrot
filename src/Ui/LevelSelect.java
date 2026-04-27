package Ui;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.awt.image.BufferedImage;
import javax.imageio.ImageIO;

public class LevelSelect extends JPanel implements KeyListener {

    private JFrame  window;
    private Image   bgImage;

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

    public LevelSelect() {
        loadAssets();
        setFocusable(true);
        addKeyListener(this);
        setupWindow();
        SwingUtilities.invokeLater(() -> { calculateLayout(); repaint(); requestFocusInWindow(); });
    }

    private void loadAssets() {
        try { bgImage = new ImageIcon(getClass().getResource("/backgrounds/background.png")).getImage(); }
        catch (Exception e) { System.out.println("BG not found"); }
        try { goldFrame  = ImageIO.read(getClass().getResource("/ui/v1gold frame.png")); }
        catch (Exception e) { System.out.println("Gold frame not found"); }
        try { hoverFrame = ImageIO.read(getClass().getResource("/ui/v1blueframe.png")); }
        catch (Exception e) { System.out.println("Blue frame not found"); }
        try { computerImg = ImageIO.read(getClass().getResource("/ui/computer4.png")); }
        catch (Exception e) { System.out.println("computer4.png not found"); }
        try { pvpImg      = ImageIO.read(getClass().getResource("/ui/vs.png")); }
        catch (Exception e) { System.out.println("vs.png not found"); }
        try { arcadeImg   = ImageIO.read(getClass().getResource("/ui/arcade.png")); }
        catch (Exception e) { System.out.println("arcade.png not found"); }
    }

    private void calculateLayout() {
        int sw = getWidth(), sh = getHeight();

        cardH         = (int)(sh * 0.50);
        cardW         = (int)(cardH * 0.78);
        titleFontSize = Math.max(24, (int)(sh * 0.055));
        labelFontSize = Math.max(14, (int)(sh * 0.030));
        descFontSize  = Math.max(10, (int)(sh * 0.018));

        int gap    = (int)(sw * 0.06);
        int totalW = cardW * 3 + gap * 2;
        int startX = (sw - totalW) / 2;

        card1X = startX;
        card2X = startX + cardW + gap;
        card3X = startX + (cardW + gap) * 2;
        cardY  = (int)(sh * 0.14);

        layoutReady = true;
    }

    private void setupWindow() {
        window = new JFrame("Select Mode");
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
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING,      RenderingHints.VALUE_ANTIALIAS_ON);
        g2.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
        g2.setRenderingHint(RenderingHints.KEY_INTERPOLATION,     RenderingHints.VALUE_INTERPOLATION_BILINEAR);

        int sw = getWidth(), sh = getHeight();

        // Background — no overlay on top
        if (bgImage != null) g2.drawImage(bgImage, 0, 0, sw, sh, this);
        else { g2.setColor(new Color(10, 30, 15)); g2.fillRect(0, 0, sw, sh); }

        drawTitle(g2, sw, sh);
        drawCard(g2, 1, card1X, cardY, cardW, cardH, computerImg, "VS AI",        "BAYOT");
        drawCard(g2, 2, card2X, cardY, cardW, cardH, pvpImg,      "Player vs Player", "SI");
        drawCard(g2, 3, card3X, cardY, cardW, cardH, arcadeImg,   "ARCADE",           "CYBORG");

    }

    private void drawTitle(Graphics2D g2, int sw, int sh) {
        String title = "Select Mode";
        g2.setFont(new Font(Font.MONOSPACED, Font.BOLD, titleFontSize));
        int tx = sw / 2 - g2.getFontMetrics().stringWidth(title) / 2;
        int ty = (int)(sh * 0.10);
        // Subtle shadow only — no big black rect
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
        if (cardImg != null) {
            g2.drawImage(cardImg, x + imgPad, y + imgPad, w - imgPad * 2, h - imgPad * 2, this);
        }


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

    @Override
    public void keyPressed(KeyEvent e) {
        int k = e.getKeyCode();
        if (k == KeyEvent.VK_RIGHT || k == KeyEvent.VK_D) {
            if (selectedCard < 3) { selectedCard++; repaint(); }
        } else if (k == KeyEvent.VK_LEFT || k == KeyEvent.VK_A) {
            if (selectedCard > 1) { selectedCard--; repaint(); }
        } else if (k == KeyEvent.VK_ENTER) {
            confirm();
        } else if (k == KeyEvent.VK_ESCAPE) {
            window.dispose();
            new TitleScreen();
        }
    }

    private void confirm() {
        window.dispose();
        switch (selectedCard) {
            case 1: new CharacterSelect(CharacterSelect.Mode.VS_AI);  break;
            case 2: new CharacterSelect(CharacterSelect.Mode.PVP);    break;
            case 3: new CharacterSelect(CharacterSelect.Mode.ARCADE); break;
        }
    }

    @Override public void keyReleased(KeyEvent e) {}
    @Override public void keyTyped(KeyEvent e)    {}
}
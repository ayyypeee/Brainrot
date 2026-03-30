package Ui;

import entities.Character;
import javax.swing.*;
import java.awt.*;
import java.awt.event.*;

public class CharacterSelect extends JPanel implements KeyListener {

    JFrame window;
    Image bgImage;
    ImageIcon sirKhaiIdle;

    String[] names = {
            "AIP",       "Christian", "Kimmay",
            "Dianne",    "Cyberg",    "Tung Tung",
            "Cappucino", "Ballerina", "Tralalero"
    };

    String[] idleGifPaths = {
            "/characters/idle_gif/v1_aip_moving.gif",
            "/characters/idle_gif/v1_christian_moving_idle.gif",
            "/characters/idle_gif/v1_kimmay_moving_idle.gif",
            "/characters/idle_gif/v1_dianne_moving_idle.gif",
            "/characters/idle_gif/v1_cyberg_moving_idle.gif",
            "/characters/idle_gif/v1_tungtung_moving_idle.gif",
            "/characters/idle_gif/v1_cappucino_moving_idle.gif",
            "/characters/idle_gif/v1_ballerina_moving_idle.gif",
            "/characters/idle_gif/v1_tralalelo_moving_idle.gif"
    };

    String[] walkSheets = {
            "/characters/walk_png/v1_clean_aip_walk.png",
            null,
            "/characters/walk_png/v2_clean_kimwalking.png",
            "/characters/walk_png/v1_clean_diannewalking.png",
            "/characters/walk_png/v1_clean_cyberg_walk.png",
            null, null, null, null
    };

    int[][][] allFrameRegions = {
            {{296,519},{656,871},{1024,1239},{1368,1583}}, // AIP
            null,
            {{40,287},{384,615},{728,967},{1056,1287}},    // Kimmay
            {{48,247},{328,527},{608,823},{904,1103}},     // Dianne
            {{64,271},{408,615},{768,967},{1104,1303}},    // Cyberg
            null, null, null, null
    };

    int[] sheetHeights  = {512,   0, 560, 512, 512, 0, 0, 0, 0};
    int[] bottomPadding = { 72,  72,  48,  40,  32, 0, 0, 0, 0};

    ImageIcon[] idleGifs = new ImageIcon[9];

    final int COLS      = 3;
    final int ROWS      = 3;
    final int CELL_SIZE = 170;
    final int CELL_PAD  = 50;

    int gridX, gridY;
    int selectedIndex = 0;

    public CharacterSelect() {

        try {
            bgImage = new ImageIcon(getClass().getResource("/backgrounds/background.png")).getImage();
        } catch (Exception e) { System.out.println("Background not found"); }

        try {
            sirKhaiIdle = new ImageIcon(getClass().getResource("/characters/idle_gif/v1_sirkhai_moving_idle.gif"));
            sirKhaiIdle.setImageObserver(this);
        } catch (Exception e) { System.out.println("Sir Khai idle not found"); }

        for (int i = 0; i < names.length; i++) {
            try {
                idleGifs[i] = new ImageIcon(getClass().getResource(idleGifPaths[i]));
                idleGifs[i].setImageObserver(this);
            } catch (Exception e) { System.out.println("Missing: " + idleGifPaths[i]); }
        }

        setFocusable(true);
        addKeyListener(this);

        window = new JFrame("Human vs Brainrot - Select Character");
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
            int totalW = COLS * CELL_SIZE + (COLS - 1) * CELL_PAD;
            int totalH = ROWS * CELL_SIZE + (ROWS - 1) * CELL_PAD;
            gridX = (getWidth() / 2) - (totalW / 2) + 130;
            gridY = (getHeight() - totalH) / 2 + 10;
            repaint();
        });

        requestFocusInWindow();
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2 = (Graphics2D) g;
        g2.setRenderingHint(RenderingHints.KEY_INTERPOLATION,
                RenderingHints.VALUE_INTERPOLATION_NEAREST_NEIGHBOR);

        if (bgImage != null)
            g2.drawImage(bgImage, 0, 0, getWidth(), getHeight(), this);
        else {
            g2.setColor(new Color(8, 28, 16));
            g2.fillRect(0, 0, getWidth(), getHeight());
        }

        // Title
        g2.setFont(new Font("Courier New", Font.BOLD, 42));
        g2.setColor(Color.WHITE);
        String title = "Select a Character";
        g2.drawString(title,
                (getWidth() - g2.getFontMetrics().stringWidth(title)) / 2,
                gridY - 24);

        // Sir Khai
        if (sirKhaiIdle != null) {
            int khaiSize   = 300;
            int totalGridH = ROWS * CELL_SIZE + (ROWS - 1) * CELL_PAD;
            int khaiX = gridX - khaiSize - 50;
            int khaiY = gridY + totalGridH / 2 - khaiSize / 2;
            g2.drawImage(sirKhaiIdle.getImage(), khaiX, khaiY, khaiSize, khaiSize, this);
        }

        // 3x3 grid
        for (int i = 0; i < names.length; i++) {
            int col = i % COLS;
            int row = i / COLS;
            int cx  = gridX + col * (CELL_SIZE + CELL_PAD);
            int cy  = gridY + row * (CELL_SIZE + CELL_PAD);

            g2.setColor(new Color(0, 0, 0, 130));
            g2.fillRect(cx, cy, CELL_SIZE, CELL_SIZE);

            g2.setColor(i == selectedIndex ? Color.YELLOW : Color.WHITE);
            g2.setStroke(new BasicStroke(i == selectedIndex ? 4 : 1));
            g2.drawRect(cx, cy, CELL_SIZE, CELL_SIZE);

            if (idleGifs[i] != null) {
                int pad = 8;
                g2.drawImage(idleGifs[i].getImage(),
                        cx + pad, cy + pad,
                        CELL_SIZE - pad * 2, CELL_SIZE - pad * 2, this);
            }

            if (walkSheets[i] == null) {
                g2.setFont(new Font("Courier New", Font.PLAIN, 10));
                g2.setColor(new Color(255, 200, 0));
                g2.drawString("Wlay lakaw", cx + 4, cy + CELL_SIZE - 6);
            }

            g2.setFont(new Font("Courier New", Font.BOLD, 14));
            g2.setColor(i == selectedIndex ? Color.YELLOW : Color.WHITE);
            String n = names[i];
            g2.drawString(n,
                    cx + (CELL_SIZE - g2.getFontMetrics().stringWidth(n)) / 2,
                    cy + CELL_SIZE + 22);
        }

        /*hint
        g2.setFont(new Font("Courier New", Font.PLAIN, 18));
        g2.setColor(Color.WHITE);
        String hint = "Arrow Keys / WASD to navigate     ENTER to confirm";
        g2.drawString(hint,
                (getWidth() - g2.getFontMetrics().stringWidth(hint)) / 2,
                getHeight() - 30);*/
    }

    @Override
    public void keyPressed(KeyEvent e) {
        switch (e.getKeyCode()) {
            case KeyEvent.VK_RIGHT: case KeyEvent.VK_D:
                if (selectedIndex % COLS < COLS - 1) selectedIndex++; break;
            case KeyEvent.VK_LEFT:  case KeyEvent.VK_A:
                if (selectedIndex % COLS > 0) selectedIndex--;         break;
            case KeyEvent.VK_DOWN:  case KeyEvent.VK_S:
                if (selectedIndex + COLS < names.length) selectedIndex += COLS; break;
            case KeyEvent.VK_UP:    case KeyEvent.VK_W:
                if (selectedIndex - COLS >= 0) selectedIndex -= COLS;  break;
            case KeyEvent.VK_ENTER:
                launchGame(); break;
        }
        repaint();
    }

    private void launchGame() {
        window.dispose();

        Character chosen = new Character(
                walkSheets[selectedIndex],
                allFrameRegions[selectedIndex],
                sheetHeights[selectedIndex],
                128, 280,
                bottomPadding[selectedIndex],
                idleGifPaths[selectedIndex],
                getClass()
        );

        new TestWalkPanel(chosen);
    }

    @Override public void keyReleased(KeyEvent e) {}
    @Override public void keyTyped(KeyEvent e) {}
}
package Ui;

import entities.Character;
import entities.CharacterFactory;
import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.util.ArrayList;

public class CharacterSelect extends JPanel implements KeyListener {

    private JFrame window;
    private Image bgImage;
    private ImageIcon sirKhaiIdle;
    private CharacterFactory factory;
    private ArrayList<ImageIcon> idleGifs;

    private int pickingPlayer;
    private int p1Index;
    private int selectedIndex;

    private final int COLS = 3;
    private final int ROWS = 3;

    private int cellSize;
    private int cellPad;
    private int gridX;
    private int gridY;
    private int titleFontSize;
    private int nameFontSize;
    private int hintFontSize;
    private int labelFontSize;
    private int sirKhaiSize;

    public CharacterSelect() {
        factory       = new CharacterFactory();
        pickingPlayer = 1;
        p1Index       = -1;
        selectedIndex = 0;

        loadBackground();
        loadSirKhai();
        loadAllIdleGifs();
        setFocusable(true);
        addKeyListener(this);
        setupWindow();

        SwingUtilities.invokeLater(new Runnable() {
            public void run() {
                calculateDimensions();
                repaint();
            }
        });

        requestFocusInWindow();
    }

    private void calculateDimensions() {
        int sw = getWidth();
        int sh = getHeight();

        cellSize     = (int)(sh * 0.18);
        cellPad      = (int)(sw * 0.03);
        titleFontSize = Math.max(18, (int)(sh * 0.045));
        nameFontSize  = Math.max(10, (int)(sh * 0.018));
        hintFontSize  = Math.max(10, (int)(sh * 0.016));
        labelFontSize = Math.max(7,  (int)(sh * 0.010));
        sirKhaiSize   = (int)(sh * 0.35);

        int totalW = COLS * cellSize + (COLS - 1) * cellPad;
        int totalH = ROWS * cellSize + (ROWS - 1) * cellPad;
        gridX = (sw / 2) - (totalW / 2) + (int)(sw * 0.13);
        gridY = (sh - totalH) / 2 + (int)(sh * 0.01);
    }

    private Font getSafeFont(int style, int size) {
        return new Font(Font.MONOSPACED, style, size);
    }

    private void loadBackground() {
        try {
            bgImage = new ImageIcon(getClass().getResource("/backgrounds/background.png")).getImage();
        } catch (Exception e) { System.out.println("Background not found"); }
    }

    private void loadSirKhai() {
        try {
            sirKhaiIdle = new ImageIcon(getClass().getResource("/characters/idle_gif/v1_sirkhai_moving_idle.gif"));
            sirKhaiIdle.setImageObserver(this);
        } catch (Exception e) { System.out.println("Sir Khai GIF not found"); }
    }

    private void loadAllIdleGifs() {
        idleGifs = new ArrayList<>();
        for (int i = 0; i < factory.getCount(); i++) {
            try {
                ImageIcon gif = new ImageIcon(getClass().getResource(factory.getIdleGifPath(i)));
                gif.setImageObserver(this);
                idleGifs.add(gif);
            } catch (Exception e) {
                idleGifs.add(null);
            }
        }
    }

    private void setupWindow() {
        window = new JFrame("Human vs Brainrot");
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
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2 = (Graphics2D) g;
        g2.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_NEAREST_NEIGHBOR);
        g2.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);

        if (cellSize == 0) calculateDimensions();

        if (bgImage != null) g2.drawImage(bgImage, 0, 0, getWidth(), getHeight(), this);
        else { g2.setColor(new Color(8, 28, 16)); g2.fillRect(0, 0, getWidth(), getHeight()); }

        drawTitle(g2);
        drawSirKhai(g2);
        drawGrid(g2);
        drawHint(g2);
    }

    private void drawTitle(Graphics2D g2) {
        g2.setFont(getSafeFont(Font.BOLD, titleFontSize));
        g2.setColor(pickingPlayer == 1 ? new Color(100, 200, 255) : new Color(255, 150, 100));
        String title = "PLAYER " + pickingPlayer + " - Select Your Character";
        int tx = (getWidth() - g2.getFontMetrics().stringWidth(title)) / 2;
        g2.drawString(title, tx, gridY - (int)(getHeight() * 0.02));
    }

    private void drawSirKhai(Graphics2D g2) {
        if (sirKhaiIdle == null) return;
        int totalGridH = ROWS * cellSize + (ROWS - 1) * cellPad;
        int khaiX = gridX - sirKhaiSize - (int)(getWidth() * 0.03);
        int khaiY = gridY + totalGridH / 2 - sirKhaiSize / 2;
        g2.drawImage(sirKhaiIdle.getImage(), khaiX, khaiY, sirKhaiSize, sirKhaiSize, this);
    }

    private void drawGrid(Graphics2D g2) {
        for (int i = 0; i < factory.getCount(); i++) {
            int col = i % COLS;
            int row = i / COLS;
            int cx  = gridX + col * (cellSize + cellPad);
            int cy  = gridY + row * (cellSize + cellPad);

            g2.setColor(new Color(0, 0, 0, 130));
            g2.fillRect(cx, cy, cellSize, cellSize);
            drawCellBorder(g2, i, cx, cy);

            ImageIcon gif = idleGifs.get(i);
            if (gif != null) {
                int pad = (int)(cellSize * 0.05);
                g2.drawImage(gif.getImage(), cx + pad, cy + pad, cellSize - pad * 2, cellSize - pad * 2, this);
            }

            drawSkillLabels(g2, i, cx, cy);
            drawCharacterName(g2, i, cx, cy);
        }
    }

    private void drawCellBorder(Graphics2D g2, int i, int cx, int cy) {
        float strokeWidth = Math.max(1.5f, getHeight() * 0.004f);
        if (i == p1Index) {
            g2.setColor(new Color(100, 255, 100));
            g2.setStroke(new BasicStroke(strokeWidth * 2));
            g2.drawRect(cx, cy, cellSize, cellSize);
            g2.setFont(getSafeFont(Font.BOLD, labelFontSize + 2));
            g2.drawString("P1", cx + cellSize - (int)(cellSize * 0.15), cy + (int)(cellSize * 0.12));
        } else if (i == selectedIndex) {
            g2.setColor(pickingPlayer == 1 ? new Color(100, 200, 255) : new Color(255, 150, 100));
            g2.setStroke(new BasicStroke(strokeWidth * 2));
            g2.drawRect(cx, cy, cellSize, cellSize);
        } else {
            g2.setColor(Color.WHITE);
            g2.setStroke(new BasicStroke(1));
            g2.drawRect(cx, cy, cellSize, cellSize);
        }
    }

    private void drawSkillLabels(Graphics2D g2, int i, int cx, int cy) {
        g2.setFont(getSafeFont(Font.PLAIN, labelFontSize));
        int labelY = cy + cellSize - (int)(cellSize * 0.08);

        if (factory.getSkill1Sheet(i) != null) {
            g2.setColor(new Color(100, 255, 100));
            g2.drawString("Skill 1", cx + 3, labelY);
        } else {
            g2.setColor(new Color(255, 80, 80));
            g2.drawString("None", cx + 3, labelY);
        }

        if (factory.getSkill2Sheet(i) != null) {
            g2.setColor(new Color(100, 255, 100));
            g2.drawString("Skill 2", cx + 3, labelY + labelFontSize + 2);
        } else {
            g2.setColor(new Color(255, 80, 80));
            g2.drawString("None", cx + 3, labelY + labelFontSize + 2);
        }

        if (factory.getWalkSheet(i) == null) {
            g2.setColor(new Color(255, 200, 0));
            g2.drawString("No Walk", cx + 3, labelY - labelFontSize - 2);
        }
    }

    private void drawCharacterName(Graphics2D g2, int i, int cx, int cy) {
        g2.setFont(getSafeFont(Font.BOLD, nameFontSize));
        g2.setColor(i == selectedIndex ? Color.YELLOW : Color.WHITE);
        String name = factory.getName(i);
        int nx = cx + (cellSize - g2.getFontMetrics().stringWidth(name)) / 2;
        g2.drawString(name, nx, cy + cellSize + nameFontSize + (int)(getHeight() * 0.006));
    }

    private void drawHint(Graphics2D g2) {
        g2.setFont(getSafeFont(Font.PLAIN, hintFontSize));
        g2.setColor(Color.WHITE);
        String hint = "ENTER to confirm";
        int hx = (getWidth() - g2.getFontMetrics().stringWidth(hint)) / 2;
        g2.drawString(hint, hx, getHeight() - (int)(getHeight() * 0.02));
    }

    @Override
    public void keyPressed(KeyEvent e) {
        int key = e.getKeyCode();
        if (key == KeyEvent.VK_RIGHT || key == KeyEvent.VK_D) {
            if (selectedIndex % COLS < COLS - 1) selectedIndex++;
        } else if (key == KeyEvent.VK_LEFT || key == KeyEvent.VK_A) {
            if (selectedIndex % COLS > 0) selectedIndex--;
        } else if (key == KeyEvent.VK_DOWN || key == KeyEvent.VK_S) {
            if (selectedIndex + COLS < factory.getCount()) selectedIndex += COLS;
        } else if (key == KeyEvent.VK_UP || key == KeyEvent.VK_W) {
            if (selectedIndex - COLS >= 0) selectedIndex -= COLS;
        } else if (key == KeyEvent.VK_ENTER) {
            confirmSelection();
        }
        repaint();
    }

    private void confirmSelection() {
        if (pickingPlayer == 1) {
            p1Index       = selectedIndex;
            pickingPlayer = 2;
            selectedIndex = 0;
            if (selectedIndex == p1Index) selectedIndex = 1;
        } else {
            window.dispose();
            int sw = getWidth();
            int sh = getHeight();
            Character p1 = factory.buildCharacter(p1Index, getClass(), sw, sh);
            Character p2 = factory.buildCharacter(selectedIndex, getClass(), sw, sh);
            new BattlePanel(p1, p2, factory.getHeadPath(p1Index), factory.getHeadPath(selectedIndex), getClass());
        }
    }

    @Override public void keyReleased(KeyEvent e) {}
    @Override public void keyTyped(KeyEvent e)   {}
}
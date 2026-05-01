package Ui;

import entities.Character;
import entities.CharacterFactory;
import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import javax.imageio.ImageIO;

public class CharacterSelect extends JPanel implements KeyListener {

    public enum Mode { PVP, VS_AI, ARCADE }

    private final Mode mode;
    private final String loggedInName;

    private int pickingPlayer = 1;
    private int p1Index = -1;
    private int playerIndex = -1;
    private int selectedIndex = 0;

    private JFrame window;
    private Image bgImage;
    private CharacterFactory factory;
    private ArrayList<ImageIcon> idleGifs;
    private BufferedImage goldFrame, blueFrame, redFrame;

    private static final int COLS = 3;
    private static final int ROWS = 3;
    private int cellSize, cellPad, gridX, gridY;
    private int titleFontSize, nameFontSize, hintFontSize, labelFontSize;

    public CharacterSelect(Mode mode, String loggedInName) {
        this.mode = mode;
        this.loggedInName = loggedInName;
        factory = new CharacterFactory();
        loadBackground();
        loadFrames();
        loadAllIdleGifs();
        setFocusable(true);
        addKeyListener(this);
        setupWindow();
        SwingUtilities.invokeLater(() -> { calculateDimensions(); repaint(); });
        requestFocusInWindow();
    }

    public CharacterSelect(Mode mode) { this(mode, "Player"); }
    public CharacterSelect() { this(Mode.PVP, "Player"); }

    private void loadBackground() {
        try { bgImage = new ImageIcon(getClass().getResource("/backgrounds/background.png")).getImage(); }
        catch (Exception e) { System.out.println("Background image could not be loaded."); }
    }

    private void loadFrames() {
        try { goldFrame = ImageIO.read(getClass().getResource("/ui/v1gold frame.png")); }
        catch (Exception e) { System.out.println("Gold frame could not be loaded."); }
        try { blueFrame = ImageIO.read(getClass().getResource("/ui/v1blueframe.png")); }
        catch (Exception e) { System.out.println("Blue frame could not be loaded."); }
        try { redFrame = ImageIO.read(getClass().getResource("/ui/v2redframe.png")); }
        catch (Exception e) { System.out.println("Red frame could not be loaded."); }
    }

    private void loadAllIdleGifs() {
        idleGifs = new ArrayList<>();
        for (int i = 0; i < factory.getCount(); i++) {
            try {
                ImageIcon gif = new ImageIcon(getClass().getResource(factory.getIdleGifPath(i)));
                gif.setImageObserver(this);
                idleGifs.add(gif);
            } catch (Exception e) { idleGifs.add(null); }
        }
    }

    private void calculateDimensions() {
        int sw = getWidth(), sh = getHeight();
        cellSize = (int)(sh * 0.18);
        cellPad = (int)(sw * 0.03);
        titleFontSize = Math.max(18, (int)(sh * 0.045));
        nameFontSize = Math.max(10, (int)(sh * 0.018));
        hintFontSize = Math.max(10, (int)(sh * 0.016));
        labelFontSize = Math.max(7, (int)(sh * 0.010));

        int totalW = COLS * cellSize + (COLS - 1) * cellPad;
        int totalH = ROWS * cellSize + (ROWS - 1) * cellPad;
        gridX = (sw - totalW) / 2;
        gridY = (sh - totalH) / 2 + (int)(sh * 0.04);
    }

    private Font font(int style, int size) { return new Font(Font.MONOSPACED, style, size); }

    private void setupWindow() {
        String title;
        switch (mode) {
            case VS_AI: title = "vs Computer"; break;
            case ARCADE: title = "Arcade"; break;
            default: title = "Player vs Player"; break;
        }
        window = new JFrame(title);
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
        Graphics2D g2 = (Graphics2D) g;
        g2.setRenderingHint(RenderingHints.KEY_INTERPOLATION,
                RenderingHints.VALUE_INTERPOLATION_NEAREST_NEIGHBOR);
        g2.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING,
                RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
        if (cellSize == 0) calculateDimensions();

        int sw = getWidth(), sh = getHeight();

        if (bgImage != null) g2.drawImage(bgImage, 0, 0, sw, sh, this);
        else { g2.setColor(new Color(8, 28, 16)); g2.fillRect(0, 0, sw, sh); }

        drawTitle(g2, sw, sh);
        drawGrid(g2, sh);
    }

    private void drawTitle(Graphics2D g2, int sw, int sh) {
        String line1;
        Color color;

        switch (mode) {
            case PVP:
                line1 = pickingPlayer == 1
                        ? "PLAYER 1: Select Your Character"
                        : "PLAYER 2: Select Your Character";
                color = pickingPlayer == 1
                        ? new Color(100, 200, 255)
                        : new Color(255, 100, 100);
                break;
            case VS_AI:
                if (playerIndex == -1) {
                    line1 = "Select Your Character";
                    color = new Color(100, 200, 255);
                } else {
                    line1 = "CHOOSE YOUR OPPONENT";
                    color = new Color(255, 160, 80);
                }
                break;
            default:
                line1 = "Select Your Fighter";
                color = new Color(255, 200, 40);
                break;
        }

        g2.setFont(font(Font.BOLD, titleFontSize));
        int tx = sw / 2 - g2.getFontMetrics().stringWidth(line1) / 2;
        int ty = gridY - (int)(sh * 0.04);

        g2.setColor(new Color(0, 0, 0, 180));
        g2.drawString(line1, tx + 2, ty + 2);
        g2.setColor(color);
        g2.drawString(line1, tx, ty);
    }

    private void drawGrid(Graphics2D g2, int sh) {
        for (int i = 0; i < factory.getCount(); i++) {
            int col = i % COLS;
            int row = i / COLS;
            int cx = gridX + col * (cellSize + cellPad);
            int cy = gridY + row * (cellSize + cellPad);

            ImageIcon gif = idleGifs.get(i);
            if (gif != null) {
                int pad = (int)(cellSize * 0.05);
                g2.drawImage(gif.getImage(),
                        cx + pad, cy + pad,
                        cellSize - pad * 2, cellSize - pad * 2, this);
            }

            BufferedImage frame = chooseFrame(i);
            if (frame != null) g2.drawImage(frame, cx, cy, cellSize, cellSize, this);

            drawBadge(g2, i, cx, cy);

            g2.setFont(font(Font.BOLD, nameFontSize));
            g2.setColor(i == selectedIndex ? Color.YELLOW : Color.WHITE);
            String name = factory.getName(i);
            int nx = cx + (cellSize - g2.getFontMetrics().stringWidth(name)) / 2;
            int ny = cy + cellSize + nameFontSize + (int)(sh * 0.006);
            g2.setColor(new Color(0, 0, 0, 160));
            g2.drawString(name, nx + 1, ny + 1);
            g2.setColor(i == selectedIndex ? Color.YELLOW : Color.WHITE);
            g2.drawString(name, nx, ny);
        }
    }

    private BufferedImage chooseFrame(int i) {
        switch (mode) {
            case PVP:
                if (i == p1Index) return blueFrame;
                if (i == selectedIndex && pickingPlayer == 1) return blueFrame;
                if (i == selectedIndex && pickingPlayer == 2) return redFrame;
                return goldFrame;
            case VS_AI:
                if (i == playerIndex) return blueFrame;
                if (i == selectedIndex) return (playerIndex == -1) ? blueFrame : redFrame;
                return goldFrame;
            default:
                return (i == selectedIndex) ? blueFrame : goldFrame;
        }
    }

    private void drawBadge(Graphics2D g2, int i, int cx, int cy) {
        String badge = null;
        if (mode == Mode.PVP && i == p1Index) badge = "P1";
        if (mode == Mode.VS_AI && i == playerIndex) badge = "YOU";
        if (badge == null) return;
        g2.setFont(font(Font.BOLD, labelFontSize + 2));
        g2.setColor(Color.WHITE);
        g2.drawString(badge,
                cx + cellSize - g2.getFontMetrics().stringWidth(badge) - 4,
                cy + labelFontSize + 4);
    }

    @Override
    public void keyPressed(KeyEvent e) {
        int key = e.getKeyCode();
        if (key == KeyEvent.VK_RIGHT || key == KeyEvent.VK_D) { if (selectedIndex % COLS < COLS - 1) selectedIndex++; }
        else if (key == KeyEvent.VK_LEFT || key == KeyEvent.VK_A) { if (selectedIndex % COLS > 0) selectedIndex--; }
        else if (key == KeyEvent.VK_DOWN || key == KeyEvent.VK_S) { if (selectedIndex + COLS < factory.getCount()) selectedIndex += COLS; }
        else if (key == KeyEvent.VK_UP || key == KeyEvent.VK_W) { if (selectedIndex - COLS >= 0) selectedIndex -= COLS; }
        else if (key == KeyEvent.VK_ENTER) confirmSelection();
        else if (key == KeyEvent.VK_ESCAPE) { window.dispose(); new LevelSelect(); }
        repaint();
    }

    private void confirmSelection() {
        switch (mode) {
            case PVP: confirmPvP(); break;
            case VS_AI: confirmVsAI(); break;
            case ARCADE: confirmArcade(); break;
        }
    }

    private void confirmPvP() {
        if (pickingPlayer == 1) {
            p1Index = selectedIndex;
            pickingPlayer = 2;
            selectedIndex = 0;
            if (selectedIndex == p1Index) selectedIndex = 1;
            repaint();
        } else {
            if (selectedIndex == p1Index) return;
            launchPvP(p1Index, selectedIndex);
        }
    }

    private void launchPvP(int idx1, int idx2) {
        int sw = getWidth(), sh = getHeight();
        window.dispose();

        Character p1 = factory.buildCharacter(idx1, getClass(), sw, sh);
        Character p2 = factory.buildCharacter(idx2, getClass(), sw, sh);

        String p1Name = factory.getName(idx1);
        String p2Name = factory.getName(idx2);

        new BattlePanel(
                p1, p2,
                factory.getHeadPath(idx1),
                factory.getHeadPath(idx2),
                factory.getSkill1Name(idx1), factory.getSkill2Name(idx1), factory.getSkill3Name(idx1),
                factory.getSkill1Name(idx2), factory.getSkill2Name(idx2), factory.getSkill3Name(idx2),
                factory.getSkill1Icon(idx1), factory.getSkill2Icon(idx1), factory.getSkill3Icon(idx1),
                factory.getSkill1Icon(idx2), factory.getSkill2Icon(idx2), factory.getSkill3Icon(idx2),
                p1Name,
                p2Name,
                getClass()
        );
    }

    private void confirmVsAI() {
        if (playerIndex == -1) {
            playerIndex = selectedIndex;
            selectedIndex = 0;
            if (selectedIndex == playerIndex) selectedIndex = 1;
            repaint();
        } else {
            if (selectedIndex == playerIndex) return;
            launchVsAI(playerIndex, selectedIndex);
        }
    }

    private void launchVsAI(int humanIdx, int aiIdx) {
        int sw = getWidth(), sh = getHeight();
        window.dispose();
        Character humanChar = factory.buildCharacter(humanIdx, getClass(), sw, sh);
        Character aiChar = factory.buildCharacter(aiIdx, getClass(), sw, sh);
        new AIBattlePanel(
                humanChar, aiChar,
                factory.getHeadPath(humanIdx), factory.getHeadPath(aiIdx),
                factory.getSkill1Name(humanIdx), factory.getSkill2Name(humanIdx), factory.getSkill3Name(humanIdx),
                factory.getSkill1Name(aiIdx), factory.getSkill2Name(aiIdx), factory.getSkill3Name(aiIdx),
                factory.getSkill1Icon(humanIdx), factory.getSkill2Icon(humanIdx), factory.getSkill3Icon(humanIdx),
                factory.getSkill1Icon(aiIdx), factory.getSkill2Icon(aiIdx), factory.getSkill3Icon(aiIdx),
                factory.getName(humanIdx),
                factory.getName(aiIdx),
                getClass()
        );
    }

    private void confirmArcade() {
        int playerIdx = selectedIndex;
        int sw = getWidth(), sh = getHeight();
        window.dispose();

        ArrayList<Integer> opponents = new ArrayList<>();
        for (int i = 0; i < factory.getCount(); i++) {
            if (i != playerIdx) opponents.add(i);
        }
        java.util.Collections.shuffle(opponents);

        new ArcadeBattlePanel(playerIdx, opponents, 0, factory, sw, sh, getClass(), loggedInName);
    }


    @Override public void keyReleased(KeyEvent e) {}
    @Override public void keyTyped(KeyEvent e) {}
}
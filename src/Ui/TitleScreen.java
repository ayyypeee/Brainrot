package Ui;

import javax.sound.sampled.*;
import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.io.IOException;

public class TitleScreen extends JPanel implements KeyListener {

    private JFrame window;
    private ImageIcon gifBackground;
    private Image bgImage;
    private Clip bgMusic;

    public TitleScreen() {
        try {
            gifBackground = new ImageIcon(getClass().getResource("/backgrounds/bg.gif"));
            bgImage = gifBackground.getImage();
            gifBackground.setImageObserver(this);
        } catch (Exception e) { System.out.println("GIF not found: " + e.getMessage()); }

        playMusic("/audio/bgMusic2.wav");

        window = new JFrame("Human vs Brainrot");
        window.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        window.setUndecorated(true);
        setFocusable(true);
        addKeyListener(this);
        window.add(this);

        GraphicsEnvironment ge = GraphicsEnvironment.getLocalGraphicsEnvironment();
        GraphicsDevice gd = ge.getDefaultScreenDevice();
        if (gd.isFullScreenSupported()) gd.setFullScreenWindow(window);
        else { window.setExtendedState(JFrame.MAXIMIZED_BOTH); window.setVisible(true); }

        requestFocusInWindow();
    }

    private void playMusic(String path) {
        try {
            AudioInputStream audioStream = AudioSystem.getAudioInputStream(getClass().getResource(path));
            bgMusic = AudioSystem.getClip();
            bgMusic.open(audioStream);
            bgMusic.loop(Clip.LOOP_CONTINUOUSLY);
            bgMusic.start();
        } catch (UnsupportedAudioFileException | IOException | LineUnavailableException e) {
            System.out.println("Music error: " + e.getMessage());
        }
    }

    private void stopMusic() {
        if (bgMusic != null && bgMusic.isRunning()) { bgMusic.stop(); bgMusic.close(); }
    }

    private void exitFullScreen() {
        GraphicsEnvironment ge = GraphicsEnvironment.getLocalGraphicsEnvironment();
        ge.getDefaultScreenDevice().setFullScreenWindow(null);
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        if (bgImage != null) g.drawImage(bgImage, 0, 0, getWidth(), getHeight(), this);
        else { g.setColor(new Color(8, 28, 16)); g.fillRect(0, 0, getWidth(), getHeight()); }

        int pressStartFontSize = Math.max(16, (int)(getHeight() * 0.035));
        g.setFont(new Font(Font.MONOSPACED, Font.BOLD, pressStartFontSize));
        g.setColor(Color.WHITE);
        String msg = "";
        int mx = (getWidth() - g.getFontMetrics().stringWidth(msg)) / 2;
        int my = (int)(getHeight() * 0.72);
        g.drawString(msg, mx, my);
    }

    @Override
    public void keyPressed(KeyEvent e) {
        if (e.getKeyCode() == KeyEvent.VK_ENTER) {
            stopMusic();
            exitFullScreen();
            window.dispose();
            new CharacterSelect();
        }
    }

    @Override public void keyReleased(KeyEvent e) {}
    @Override public void keyTyped(KeyEvent e)   {}
}
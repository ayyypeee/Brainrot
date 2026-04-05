package Ui;

import entities.Character;
import javax.swing.*;
import java.awt.*;
import java.awt.event.*;

public class TestWalkPanel extends JPanel implements KeyListener {

    JFrame window;
    Image bgImage;
    Character character;
    Timer gameTimer;

    public TestWalkPanel(Character character) {
        this.character = character;

        try {
            bgImage = new ImageIcon(getClass().getResource(
                    "/backgrounds/background.png")).getImage();
        } catch (Exception e) { System.out.println("Background not found"); }

        character.setImageObserver(this);
        setFocusable(true);
        addKeyListener(this);

        gameTimer = new Timer(16, e -> {
            character.update();
            character.setScreenBounds(getWidth());
            repaint();
        });
        gameTimer.start();

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

        SwingUtilities.invokeLater(() ->
                character.placeAtBottom(getWidth(), getHeight())
        );

        requestFocusInWindow();
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);

        if (bgImage != null)
            g.drawImage(bgImage, 0, 0, getWidth(), getHeight(), this);
        else {
            g.setColor(new Color(20, 40, 20));
            g.fillRect(0, 0, getWidth(), getHeight());
        }

        character.draw(g, this);

    }

    @Override
    public void keyPressed(KeyEvent e) {
        switch (e.getKeyCode()) {
            case KeyEvent.VK_D: case KeyEvent.VK_RIGHT:
                character.movingRight = true;
                character.facingRight = true;
                break;
            case KeyEvent.VK_A: case KeyEvent.VK_LEFT:
                character.movingLeft  = true;
                character.facingRight = false;
                break;
            case KeyEvent.VK_E:
                character.triggerSkill1();
                break;
            case KeyEvent.VK_R:
                character.triggerSkill2();
                break;
        }
    }

    @Override
    public void keyReleased(KeyEvent e) {
        switch (e.getKeyCode()) {
            case KeyEvent.VK_D: case KeyEvent.VK_RIGHT:
                character.movingRight = false; break;
            case KeyEvent.VK_A: case KeyEvent.VK_LEFT:
                character.movingLeft  = false; break;
        }
    }

    @Override public void keyTyped(KeyEvent e) {}
}
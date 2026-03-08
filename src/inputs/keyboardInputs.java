package inputs;
import Main.GamePanel;

import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;

public class keyboardInputs implements KeyListener {
    private GamePanel gamePanel;
    public keyboardInputs(GamePanel gamePanel){
        this.gamePanel = gamePanel;
    }
    @Override
    public void keyTyped(KeyEvent e) {

    }
    public void keyReleased(KeyEvent e) {

    }

    @Override
    public void keyPressed(KeyEvent e) {

        switch (e.getKeyCode()){
            case KeyEvent.VK_W:
                System.out.println("UP");
                gamePanel.changeYDelta(-5);
                break;
            case KeyEvent.VK_S:
                System.out.println("DOWN");
                gamePanel.changeYDelta(5);
                break;
            case KeyEvent.VK_A:
                System.out.println("LEFT");
                gamePanel.changeXDelta(-5);
                break;
            case KeyEvent.VK_D:
                System.out.println("RIGHT");
                gamePanel.changeXDelta(5);
                break;
        }

    }


}

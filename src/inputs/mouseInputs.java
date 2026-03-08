package inputs;

import Main.GamePanel;

import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;
import java.awt.event.MouseEvent;
public class mouseInputs implements MouseListener, MouseMotionListener {
    private GamePanel gamePanel;
        public mouseInputs(GamePanel gamePanel){
            this.gamePanel = gamePanel;
        }

    public void mouseDragged(MouseEvent e) {

    }
    public void mouseMoved(MouseEvent e) {
        gamePanel.setRectPos(e.getX(), e.getY());
    }

    public void mouseClicked(MouseEvent e) {
        System.out.println("mouse click!");
    }
    public void mousePressed(MouseEvent e) {

    }

    @Override
    public void mouseReleased(MouseEvent e) {

    }

    @Override
    public void mouseEntered(MouseEvent e) {

    }

    @Override
    public void mouseExited(MouseEvent e) {

    }

}

package entities;

import java.awt.Graphics;
import java.awt.image.ImageObserver;

// Interface that forces any class to implement a draw method.
public interface Drawable {
    void draw(Graphics g, ImageObserver observer);
}
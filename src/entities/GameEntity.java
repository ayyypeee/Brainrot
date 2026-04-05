package entities;

import java.awt.Graphics;
import java.awt.image.ImageObserver;

public abstract class GameEntity implements Drawable, Updatable {

    protected int x;
    protected int y;
    protected int width;
    protected int height;
    protected int speed;

    public GameEntity(int x, int y, int width, int height, int speed) {
        this.x      = x;
        this.y      = y;
        this.width  = width;
        this.height = height;
        this.speed  = speed;
    }


    public int getX() { return x; }

    public int getY() { return y; }

    public void setX(int x) { this.x = x; }

    public void setY(int y) { this.y = y; }

    public int getWidth() { return width; }

    public int getHeight() { return height; }

    public void setScreenBounds(int screenWidth) {
        // Don't go past the left edge
        if (x < 0) {
            x = 0;
        }
        // Don't go past the right edge
        if (x > screenWidth - width) {
            x = screenWidth - width;
        }
    }

    @Override
    public abstract void draw(Graphics g, ImageObserver observer);

    @Override
    public abstract void update();
}
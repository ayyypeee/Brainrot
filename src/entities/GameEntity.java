package entities;

import java.awt.Graphics;
import java.awt.image.ImageObserver;


public abstract class GameEntity implements Drawable, Updatable {

    // Position, size, and movement speed — protected so subclasses can access them.
    protected int x;
    protected int y;
    protected int width;
    protected int height;
    protected int speed;

    // Constructor initializes all shared fields for every game entity.
    public GameEntity(int x, int y, int width, int height, int speed) {
        this.x      = x;
        this.y      = y;
        this.width  = width;
        this.height = height;
        this.speed  = speed;
    }

    // Getters and setters for encapsulation.
    public int getX()       {
        return x; }
    public int getY()       {
        return y; }
    public void setX(int x) {
        this.x = x; }
    public void setY(int y) {
        this.y = y; }
    public int getWidth()   {
        return width; }
    public int getHeight()  {
        return height; }

    // Keeps the entity inside the screen horizontally.
    public void setScreenBounds(int screenWidth) {
        if (x < 0) {
            x = 0;
        }
        if (x > screenWidth - width) {
            x = screenWidth - width;
        }
    }

    // Subclasses must provide their own draw and update implementations.
    @Override
    public abstract void draw(Graphics g, ImageObserver observer);

    @Override
    public abstract void update();
}
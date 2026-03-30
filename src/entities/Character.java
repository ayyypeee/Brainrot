package entities;

import java.awt.*;
import java.awt.image.BufferedImage;
import javax.imageio.ImageIO;
import javax.swing.ImageIcon;

public class Character {

    private BufferedImage[] walkFramesRight;
    private BufferedImage[] walkFramesLeft;
    private BufferedImage idleRight;
    private BufferedImage idleLeft;
    private BufferedImage currentFrame;

    // Idle GIF
    private ImageIcon idleGif;

    private int currentFrameIndex = 1;
    private int animTick = 0;
    private boolean animForward = true;
    private final int ANIM_SPEED = 8;

    public int x, y;
    public int speed = 5;
    public int charW, charH;
    public boolean facingRight = true;
    public boolean movingLeft  = false;
    public boolean movingRight = false;

    private int bottomPadding;
    private int sheetHeight;
    private boolean hasPNG = false;

    public Character(String sheetPath, int[][] frameRegions, int sheetHeight,
                     int charW, int charH, int bottomPadding,
                     String idleGifPath, Class<?> loader) {
        this.charW         = charW;
        this.charH         = charH;
        this.bottomPadding = bottomPadding;
        this.sheetHeight   = sheetHeight;

        if (sheetPath != null) {
            loadSprites(sheetPath, frameRegions, loader);
            hasPNG = true;
        }

        if (idleGifPath != null) {
            try {
                idleGif = new ImageIcon(loader.getResource(idleGifPath));
            } catch (Exception e) {
                System.out.println("Idle GIF not found: " + idleGifPath);
            }
        }
    }

    private void loadSprites(String sheetPath, int[][] frameRegions, Class<?> loader) {
        try {
            BufferedImage sheet = ImageIO.read(loader.getResource(sheetPath));
            int n = frameRegions.length;
            walkFramesRight = new BufferedImage[n];
            walkFramesLeft  = new BufferedImage[n];

            for (int i = 0; i < n; i++) {
                int x1 = frameRegions[i][0];
                int x2 = frameRegions[i][1];
                int fw  = x2 - x1 + 1;
                walkFramesRight[i] = sheet.getSubimage(x1, 0, fw, sheetHeight);

                BufferedImage flipped = new BufferedImage(fw, sheetHeight, BufferedImage.TYPE_INT_ARGB);
                Graphics2D g2 = flipped.createGraphics();
                g2.drawImage(walkFramesRight[i], fw, 0, -fw, sheetHeight, null);
                g2.dispose();
                walkFramesLeft[i] = flipped;
            }

            idleRight    = walkFramesRight[0];
            idleLeft     = walkFramesLeft[0];
            currentFrame = idleRight;

        } catch (Exception e) {
            System.out.println("Sprite sheet not found: " + sheetPath);
            hasPNG = false;
        }
    }

    public void setImageObserver(java.awt.image.ImageObserver obs) {
        if (idleGif != null) idleGif.setImageObserver(obs);
    }

    public void placeAtBottom(int screenW, int screenH) {
        x = screenW / 2 - charW / 2;
        y = screenH - charH + (int)(charH * (double) bottomPadding / sheetHeight);
    }

    public void update() {
        if (movingRight) x += speed;
        if (movingLeft)  x -= speed;
        if (hasPNG) updatePNG();
    }

    private void updatePNG() {
        boolean moving = movingLeft || movingRight;
        if (moving) {
            animTick++;
            if (animTick >= ANIM_SPEED) {
                animTick = 0;
                if (animForward) {
                    currentFrameIndex++;
                    if (currentFrameIndex >= walkFramesRight.length - 1) {
                        currentFrameIndex = walkFramesRight.length - 1;
                        animForward = false;
                    }
                } else {
                    currentFrameIndex--;
                    if (currentFrameIndex <= 1) {
                        currentFrameIndex = 1;
                        animForward = true;
                    }
                }
            }
            if (facingRight) {
                currentFrame = walkFramesRight[currentFrameIndex];
            } else {
                int m = Math.max(1, Math.min(
                        (walkFramesRight.length - 1) - currentFrameIndex + 1,
                        walkFramesRight.length - 1));
                currentFrame = walkFramesLeft[m];
            }
        } else {
            animTick = 0;
            currentFrameIndex = 1;
            animForward = true;
            currentFrame = facingRight ? idleRight : idleLeft;
        }
    }

    public void draw(Graphics g, java.awt.image.ImageObserver obs) {
        if (hasPNG && currentFrame != null) {
            g.drawImage(currentFrame, x, y, charW, charH, obs);
        } else {
            // No PNG
            if (idleGif != null) {
                g.drawImage(idleGif.getImage(), x, y, charW, charH, obs);
            }
        }
    }

    public void setScreenBounds(int screenW) {
        if (x < 0) x = 0;
        if (x > screenW - charW) x = screenW - charW;
    }
}
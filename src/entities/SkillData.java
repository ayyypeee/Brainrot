package entities;

import java.awt.image.BufferedImage;
import java.util.ArrayList;

public class SkillData {

    private ArrayList<BufferedImage> frames;

    private int animSpeed;

    private int bottomPadding;

    private int sheetHeight;

    private int currentFrameIndex;

    private int animTick;

    private boolean active;

    private int drawY;

    public SkillData(ArrayList<BufferedImage> frames, int animSpeed,
                     int bottomPadding, int sheetHeight) {
        this.frames        = frames;
        this.animSpeed     = animSpeed;
        this.bottomPadding = bottomPadding;
        this.sheetHeight   = sheetHeight;
        this.currentFrameIndex = 0;
        this.animTick      = 0;
        this.active        = false;
        this.drawY         = 0;
    }

    public void activate() {
        active             = true;
        currentFrameIndex  = 0;
        animTick           = 0;
    }


    public void deactivate() {
        active            = false;
        currentFrameIndex = 0;
        animTick          = 0;
    }


    public boolean tick() {

        if (!active) {
            return false;
        }

        animTick++;

        if (animTick >= animSpeed) {
            animTick = 0;
            currentFrameIndex++;

            if (currentFrameIndex >= frames.size()) {
                deactivate();
                return true;
            }
        }

        return false;
    }

    public BufferedImage getCurrentFrame() {

        int safeIndex = currentFrameIndex;
        if (safeIndex >= frames.size()) {
            safeIndex = frames.size() - 1;
        }
        return frames.get(safeIndex);
    }

    public boolean isActive() { return active; }

    public int getBottomPadding() { return bottomPadding; }

    public int getSheetHeight() { return sheetHeight; }

    public int getFrameCount() { return frames.size(); }

    public void setDrawY(int drawY) { this.drawY = drawY; }

    public int getDrawY() { return drawY; }
}
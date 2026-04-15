package entities;

import java.awt.image.BufferedImage;
import java.util.ArrayList;

public class SkillData {

    private final ArrayList<BufferedImage> frames;
    private final int animSpeed;
    private final int bottomPadding;
    private final int sheetHeight;

    private int  currentFrameIndex;
    private int  animTick;
    private boolean active;
    private int  drawY;

    public SkillData(ArrayList<BufferedImage> frames, int animSpeed,
                     int bottomPadding, int sheetHeight) {
        this.frames        = frames;
        this.animSpeed     = animSpeed;
        this.bottomPadding = bottomPadding;
        this.sheetHeight   = sheetHeight;
    }

    public void activate() {
        active            = true;
        currentFrameIndex = 0;
        animTick          = 0;
    }

    public void deactivate() {
        active            = false;
        currentFrameIndex = 0;
        animTick          = 0;
    }

    public void tick() {
        if (!active) return;
        animTick++;
        if (animTick >= animSpeed) {
            animTick = 0;
            currentFrameIndex++;
            if (currentFrameIndex >= frames.size()) {
                deactivate();
            }
        }
    }

    public BufferedImage getCurrentFrame() {
        int i = Math.min(currentFrameIndex, frames.size() - 1);
        return frames.get(i);
    }

    public boolean isActive()        { return active; }
    public int getBottomPadding()    { return bottomPadding; }
    public int getSheetHeight()      { return sheetHeight; }
    public void setDrawY(int drawY)  { this.drawY = drawY; }
    public int  getDrawY()           { return drawY; }
}
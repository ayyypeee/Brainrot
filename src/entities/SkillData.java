package entities;

import java.awt.image.BufferedImage;
import java.util.ArrayList;


public class SkillData {

    private final ArrayList<BufferedImage> frames;
    private final int animSpeed;

    private int     currentFrameIndex;
    private int     animTick;
    private boolean active;

    private int drawY;

    public SkillData(ArrayList<BufferedImage> frames, int animSpeed) {
        this.frames    = frames;
        this.animSpeed = animSpeed;
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
            if (currentFrameIndex >= frames.size()) deactivate();
        }
    }

    public BufferedImage getCurrentFrame() {
        if (frames.isEmpty()) return null;
        return frames.get(Math.min(currentFrameIndex, frames.size() - 1));
    }

    public boolean isActive()       { return active; }
    public void    setDrawY(int y)  { drawY = y; }
    public int     getDrawY()       { return drawY; }
}
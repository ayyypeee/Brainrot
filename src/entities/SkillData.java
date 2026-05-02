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

    // ── Sound ─────────────────────────────────────────────────────────────────
    // Set by Character right after construction so SkillData knows what to play.
    private String charName  = null;
    private int    skillNum  = 0;

    public SkillData(ArrayList<BufferedImage> frames, int animSpeed) {
        this.frames    = frames;
        this.animSpeed = animSpeed;
    }

    /**
     * Called once by Character after building each SkillData so the sound
     * player knows which clip to fire.
     */
    public void setSoundInfo(String charName, int skillNum) {
        this.charName = charName;
        this.skillNum = skillNum;
    }

    public void activate() {
        active            = true;
        currentFrameIndex = 0;
        animTick          = 0;

        // Play the hit sound the instant the skill animation starts.
        if (charName != null && skillNum > 0) {
            Ui.SkillSoundPlayer.play(charName, skillNum);
        }
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
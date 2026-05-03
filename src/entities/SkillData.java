package entities;

import java.awt.image.BufferedImage;
import java.util.ArrayList;

// Manages animation frames and sound for one character skill.
public class SkillData {

    private ArrayList<BufferedImage> frames;  // animation frames for this skill
    private int animSpeed;                    // how many ticks to show each frame

    private int currentFrameIndex;
    private int animTick;
    private boolean active;

    private int drawY; // vertical draw position on screen

    // Used to play the correct sound when this skill activates.
    private String charName;
    private int skillNum;

    // Constructor receives the list of frames and how fast to animate them.
    public SkillData(ArrayList<BufferedImage> frames, int animSpeed) {
        this.frames    = frames;
        this.animSpeed = animSpeed;
        this.charName  = null;
        this.skillNum  = 0;
    }

    // Called once by Character so this skill knows which sound to play.
    public void setSoundInfo(String charName, int skillNum) {
        this.charName = charName;
        this.skillNum = skillNum;
    }

    // Starts playing this skill animation and its sound.
    public void activate() {
        active            = true;
        currentFrameIndex = 0;
        animTick          = 0;

        if (charName != null && skillNum > 0) {
            Ui.SkillSoundPlayer.play(charName, skillNum);
        }
    }

    // Stops the animation and resets it to the beginning.
    public void deactivate() {
        active            = false;
        currentFrameIndex = 0;
        animTick          = 0;
    }

    // Advances the animation by one game tick.
    public void tick() {
        if (!active) {
            return;
        }

        animTick++;

        if (animTick >= animSpeed) {
            animTick = 0;
            currentFrameIndex++;

            // Deactivate once all frames have been shown.
            if (currentFrameIndex >= frames.size()) {
                deactivate();
            }
        }
    }

    // Returns the frame that should be drawn right now.
    public BufferedImage getCurrentFrame() {
        if (frames.isEmpty()) {
            return null;
        }
        int safeIndex = Math.min(currentFrameIndex, frames.size() - 1);
        return frames.get(safeIndex);
    }

    public boolean isActive()      { return active; }
    public void setDrawY(int y)    { drawY = y; }
    public int getDrawY()          { return drawY; }
}
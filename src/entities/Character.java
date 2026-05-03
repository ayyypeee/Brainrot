package entities;

import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.awt.image.ImageObserver;
import java.util.ArrayList;
import javax.imageio.ImageIO;
import javax.swing.ImageIcon;

// This class represents a playable fighter that handles walking and skill animations.
public class Character extends GameEntity {

    // These images hold the walking animation frames for both directions.
    private BufferedImage[] framesRight;
    private BufferedImage[] framesLeft;
    private BufferedImage currentFrame;
    private boolean hasPNG = false;

    // These objects store the data for the three skill animations.
    private SkillData skill1;
    private SkillData skill2;
    private SkillData skill3;

    // This keeps track of which direction the character is currently facing.
    public boolean facingRight = true;

    // These variables manage the current state of the walking animation cycle.
    private int walkFrameIndex = 1;
    private int walkAnimTick   = 0;
    private static final int WALK_ANIM_SPEED = 4;

    // This is the default resting position where the character stands.
    private int standX;
    private int standY;

    // This defines the different states a character can be in during an attack.
    public enum AttackState { IDLE, WALKING_TO, SKILL, WALKING_BACK }
    private AttackState attackState = AttackState.IDLE;

    private int targetX;
    private int pendingSkill;
    private static final int ATTACK_WALK_SPEED = 36;

    // This image is used when the character is just standing still.
    private ImageIcon idleGif;

    // We save the character name to play the correct skill sounds later.
    private String charName;

    // This constructor sets up the character with all their images and skills.
    public Character(String charName,
                     String[] framePaths,
                     int charW, int charH,
                     String idleGifPath,
                     String[] sk1Paths,
                     String[] sk2Paths,
                     String[] sk3Paths,
                     Class<?> loader) {

        super(0, 0, charW, charH, 5);
        this.charName = charName;

        if (framePaths != null && framePaths.length > 0) {
            loadFrames(framePaths, loader);
        }

        loadIdleGif(idleGifPath, loader);

        if (sk1Paths != null && sk1Paths.length > 0) {
            skill1 = loadSkillFrames(sk1Paths, loader);
            if (skill1 != null) {
                skill1.setSoundInfo(charName, 1);
            }
        }
        if (sk2Paths != null && sk2Paths.length > 0) {
            skill2 = loadSkillFrames(sk2Paths, loader);
            if (skill2 != null) {
                skill2.setSoundInfo(charName, 2);
            }
        }
        if (sk3Paths != null && sk3Paths.length > 0) {
            skill3 = loadSkillFrames(sk3Paths, loader);
            if (skill3 != null) {
                skill3.setSoundInfo(charName, 3);
            }
        }
    }

    // This constructor is kept around for older code that does not use the character name.
    public Character(String[] framePaths,
                     int charW, int charH,
                     String idleGifPath,
                     String[] sk1Paths,
                     String[] sk2Paths,
                     String[] sk3Paths,
                     Class<?> loader) {
        this(null, framePaths, charW, charH, idleGifPath,
                sk1Paths, sk2Paths, sk3Paths, loader);
    }

    // This method loads the walking frames and creates mirrored versions for walking left.
    private void loadFrames(String[] paths, Class<?> loader) {
        framesRight = new BufferedImage[paths.length];
        framesLeft  = new BufferedImage[paths.length];
        int loaded  = 0;

        for (int i = 0; i < paths.length; i++) {
            try {
                BufferedImage img = ImageIO.read(loader.getResource(paths[i]));
                if (img == null) {
                    System.out.println("Could not load image: " + paths[i]);
                    continue;
                }
                framesRight[i] = img;

                // We flip the image horizontally so the character can look left.
                BufferedImage flip = new BufferedImage(img.getWidth(), img.getHeight(),
                        BufferedImage.TYPE_INT_ARGB);
                Graphics2D g2 = flip.createGraphics();
                g2.drawImage(img, img.getWidth(), 0, -img.getWidth(), img.getHeight(), null);
                g2.dispose();
                framesLeft[i] = flip;

                loaded++;
            } catch (Exception e) {
                System.out.println("Could not find frame: " + paths[i]);
            }
        }

        if (loaded > 0) {
            currentFrame = framesRight[0];
            hasPNG = true;
        }
    }

    // This loads the animated image for when the character is resting.
    private void loadIdleGif(String path, Class<?> loader) {
        if (path == null) {
            return;
        }
        try {
            idleGif = new ImageIcon(loader.getResource(path));
        } catch (Exception e) {
            System.out.println("Could not find idle GIF: " + path);
        }
    }

    // This loads the sequence of images needed for a skill animation.
    private SkillData loadSkillFrames(String[] paths, Class<?> loader) {
        ArrayList<BufferedImage> frames = new ArrayList<BufferedImage>();
        for (int i = 0; i < paths.length; i++) {
            try {
                BufferedImage img = ImageIO.read(loader.getResource(paths[i]));
                if (img != null) {
                    frames.add(img);
                }
            } catch (Exception e) {
                System.out.println("Could not find skill frame: " + paths[i]);
            }
        }
        if (frames.isEmpty()) {
            return null;
        }
        return new SkillData(frames, 10);
    }

    // This makes sure animated images update properly on the screen.
    public void setImageObserver(ImageObserver observer) {
        if (idleGif != null) {
            idleGif.setImageObserver(observer);
        }
    }

    // This positions the character so they are standing directly on the platform.
    public void placeOnPlatform(int posX, int platformTopY) {
        standX = posX;
        int overlap = (int)(height * 0.20);
        standY = platformTopY - height + overlap;
        x = standX;
        y = standY;
        setSkillDrawY(standY);
    }

    // We save this spot so the character knows where to return after attacking.
    public void saveHome() {
        standX = x;
        standY = y;
        setSkillDrawY(standY);
    }

    // This updates the vertical drawing position for all the skill animations.
    private void setSkillDrawY(int dy) {
        if (skill1 != null) { skill1.setDrawY(dy); }
        if (skill2 != null) { skill2.setDrawY(dy); }
        if (skill3 != null) { skill3.setDrawY(dy); }
    }

    // This starts the sequence where the character walks towards the enemy.
    public void startAttack(int skillNum, int enemyCentX) {
        if (attackState != AttackState.IDLE) {
            return;
        }
        pendingSkill = skillNum;

        if (facingRight) {
            targetX = enemyCentX - width;
        } else {
            targetX = enemyCentX;
        }

        attackState    = AttackState.WALKING_TO;
        walkFrameIndex = 1;
        walkAnimTick   = 0;
    }

    // These methods help the game check what the character is currently doing.
    public boolean isAnyCastingSkill()  { return attackState != AttackState.IDLE; }
    public boolean isPlayingSkillAnim() { return attackState == AttackState.SKILL; }
    public boolean isWalkingBack()      { return attackState == AttackState.WALKING_BACK; }
    public boolean isAtHome()           { return attackState == AttackState.IDLE; }

    public boolean hasSkill1()          { return skill1 != null; }
    public boolean hasSkill2()          { return skill2 != null; }
    public boolean hasSkill3()          { return skill3 != null; }

    public boolean isCastingSkill1()    { return skill1 != null && skill1.isActive(); }
    public boolean isCastingSkill2()    { return skill2 != null && skill2.isActive(); }
    public boolean isCastingSkill3()    { return skill3 != null && skill3.isActive(); }

    // This updates the character movement and animations every single game frame.
    @Override
    public void update() {
        if (attackState == AttackState.WALKING_TO) {
            facingRight = (targetX >= x);
            int dx = targetX - x;

            if (Math.abs(dx) <= ATTACK_WALK_SPEED) {
                x = targetX;
                fireSkill(pendingSkill);
                attackState    = AttackState.SKILL;
                walkFrameIndex = 0;
            } else {
                if (facingRight) {
                    x += ATTACK_WALK_SPEED;
                } else {
                    x -= ATTACK_WALK_SPEED;
                }
                advanceWalkFrame();
            }

        } else if (attackState == AttackState.SKILL) {
            if (skill1 != null && skill1.isActive()) { skill1.tick(); }
            if (skill2 != null && skill2.isActive()) { skill2.tick(); }
            if (skill3 != null && skill3.isActive()) { skill3.tick(); }

            if (!anySkillActive()) {
                facingRight    = (standX >= x);
                attackState    = AttackState.WALKING_BACK;
                walkFrameIndex = 1;
                walkAnimTick   = 0;
            }

        } else if (attackState == AttackState.WALKING_BACK) {
            facingRight = (standX >= x);
            int dx = standX - x;

            if (Math.abs(dx) <= ATTACK_WALK_SPEED) {
                x              = standX;
                y              = standY;
                attackState    = AttackState.IDLE;
                pendingSkill   = 0;
                walkFrameIndex = 0;
                walkAnimTick   = 0;
            } else {
                if (facingRight) {
                    x += ATTACK_WALK_SPEED;
                } else {
                    x -= ATTACK_WALK_SPEED;
                }
                advanceWalkFrame();
            }
        }

        updateCurrentFrame();
    }

    // This triggers the correct animation based on the chosen skill number.
    private void fireSkill(int num) {
        if (num == 1 && skill1 != null) { skill1.activate(); }
        if (num == 2 && skill2 != null) { skill2.activate(); }
        if (num == 3 && skill3 != null) { skill3.activate(); }
    }

    // This checks if there is any skill animation currently in progress.
    private boolean anySkillActive() {
        if (skill1 != null && skill1.isActive()) { return true; }
        if (skill2 != null && skill2.isActive()) { return true; }
        if (skill3 != null && skill3.isActive()) { return true; }
        return false;
    }

    // This moves the walking animation forward to the next frame.
    private void advanceWalkFrame() {
        if (framesRight == null || framesRight.length < 4) {
            return;
        }
        walkAnimTick++;
        if (walkAnimTick >= WALK_ANIM_SPEED) {
            walkAnimTick = 0;
            walkFrameIndex++;
            if (walkFrameIndex > 3) {
                walkFrameIndex = 1;
            }
        }
    }

    // This updates the current image to show the right walking or resting frame.
    private void updateCurrentFrame() {
        if (!hasPNG || framesRight == null) {
            return;
        }

        int idx;
        if (attackState == AttackState.WALKING_TO || attackState == AttackState.WALKING_BACK) {
            idx = walkFrameIndex;
            if (idx < 1 || idx >= framesRight.length) {
                idx = 1;
            }
        } else {
            idx = 0;
        }

        BufferedImage[] arr;
        if (facingRight) {
            arr = framesRight;
        } else {
            arr = framesLeft;
        }

        if (arr[idx] != null) {
            currentFrame = arr[idx];
        } else if (arr[0] != null) {
            currentFrame = arr[0];
        }
    }

    // This draws the character and prioritizes skill animations over walking.
    @Override
    public void draw(Graphics g, ImageObserver observer) {
        Graphics2D g2 = (Graphics2D) g;

        if (skill1 != null && skill1.isActive()) { drawSkillFrame(g2, skill1, observer); return; }
        if (skill2 != null && skill2.isActive()) { drawSkillFrame(g2, skill2, observer); return; }
        if (skill3 != null && skill3.isActive()) { drawSkillFrame(g2, skill3, observer); return; }

        if (hasPNG && currentFrame != null) {
            g2.drawImage(currentFrame, x, y, width, height, observer);
            return;
        }

        if (idleGif != null) {
            if (facingRight) {
                g2.drawImage(idleGif.getImage(), x, y, width, height, observer);
            } else {
                g2.drawImage(idleGif.getImage(), x + width, y, -width, height, observer);
            }
        }
    }

    // This draws the current skill image and mirrors it if needed.
    private void drawSkillFrame(Graphics2D g2, SkillData skill, ImageObserver observer) {
        BufferedImage frame = skill.getCurrentFrame();
        if (frame == null) {
            return;
        }
        int drawY = skill.getDrawY();

        if (facingRight) {
            g2.drawImage(frame, x, drawY, width, height, observer);
        } else {
            BufferedImage flipped = new BufferedImage(
                    frame.getWidth(), frame.getHeight(), BufferedImage.TYPE_INT_ARGB);
            Graphics2D fg = flipped.createGraphics();
            fg.drawImage(frame, frame.getWidth(), 0, -frame.getWidth(), frame.getHeight(), null);
            fg.dispose();
            g2.drawImage(flipped, x, drawY, width, height, observer);
        }
    }
}
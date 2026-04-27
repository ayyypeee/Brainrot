package entities;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.awt.image.ImageObserver;
import java.util.ArrayList;
import javax.imageio.ImageIO;
import javax.swing.ImageIcon;

public class Character extends GameEntity {

    // ── Walk frames ───────────────────────────────────────────────────────────
    private BufferedImage[] framesRight;
    private BufferedImage[] framesLeft;
    private BufferedImage   currentFrame;
    private boolean         hasPNG = false;

    // ── Skill data ────────────────────────────────────────────────────────────
    private SkillData skill1, skill2, skill3;

    // ── Facing ────────────────────────────────────────────────────────────────
    public boolean facingRight = true;

    // ── Walk animation ────────────────────────────────────────────────────────
    private int  walkFrameIndex = 1;
    private int  walkAnimTick   = 0;
    private static final int WALK_ANIM_SPEED = 4;

    // ── Position ──────────────────────────────────────────────────────────────
    private int standX, standY;

    // ── Attack state ──────────────────────────────────────────────────────────
    public enum AttackState { IDLE, WALKING_TO, SKILL, WALKING_BACK }
    private AttackState attackState = AttackState.IDLE;

    private int targetX;
    private int pendingSkill;
    private static final int ATTACK_WALK_SPEED = 36;

    // ── Fallback GIF ──────────────────────────────────────────────────────────
    private ImageIcon idleGif;

    // ── Constructor ───────────────────────────────────────────────────────────
    public Character(String[] framePaths,
                     int charW, int charH,
                     String idleGifPath,
                     String[] sk1Paths,
                     String[] sk2Paths,
                     String[] sk3Paths,
                     Class<?> loader) {

        super(0, 0, charW, charH, 5);

        if (framePaths != null && framePaths.length > 0)
            loadFrames(framePaths, loader);

        loadIdleGif(idleGifPath, loader);

        if (sk1Paths != null && sk1Paths.length > 0) skill1 = loadSkillFrames(sk1Paths, loader);
        if (sk2Paths != null && sk2Paths.length > 0) skill2 = loadSkillFrames(sk2Paths, loader);
        if (sk3Paths != null && sk3Paths.length > 0) skill3 = loadSkillFrames(sk3Paths, loader);
    }

    // ── Asset loading ─────────────────────────────────────────────────────────
    private void loadFrames(String[] paths, Class<?> loader) {
        framesRight = new BufferedImage[paths.length];
        framesLeft  = new BufferedImage[paths.length];

        int loaded = 0;
        for (int i = 0; i < paths.length; i++) {
            try {
                BufferedImage img = ImageIO.read(loader.getResource(paths[i]));
                if (img == null) { System.out.println("Null image: " + paths[i]); continue; }

                framesRight[i] = img;

                BufferedImage flip = new BufferedImage(img.getWidth(), img.getHeight(),
                        BufferedImage.TYPE_INT_ARGB);
                Graphics2D g2 = flip.createGraphics();
                g2.drawImage(img, img.getWidth(), 0, -img.getWidth(), img.getHeight(), null);
                g2.dispose();
                framesLeft[i] = flip;

                loaded++;
            } catch (Exception e) {
                System.out.println("Frame not found: " + paths[i]);
            }
        }

        if (loaded > 0) {
            currentFrame = framesRight[0];
            hasPNG = true;
        }
    }

    private void loadIdleGif(String path, Class<?> loader) {
        if (path == null) return;
        try { idleGif = new ImageIcon(loader.getResource(path)); }
        catch (Exception e) { System.out.println("Idle GIF not found: " + path); }
    }

    private SkillData loadSkillFrames(String[] paths, Class<?> loader) {
        ArrayList<BufferedImage> frames = new ArrayList<>();
        for (String path : paths) {
            try {
                BufferedImage img = ImageIO.read(loader.getResource(path));
                if (img != null) frames.add(img);
            } catch (Exception e) { System.out.println("Skill frame not found: " + path); }
        }
        return frames.isEmpty() ? null : new SkillData(frames, 10);
    }

    public void setImageObserver(ImageObserver observer) {
        if (idleGif != null) idleGif.setImageObserver(observer);
    }

    // ── Placement ─────────────────────────────────────────────────────────────
    public void placeOnPlatform(int posX, int platformTopY) {
        standX = posX;
        int overlap = (int)(height * 0.20);
        standY = platformTopY - height + overlap;
        x = standX;
        y = standY;
        setSkillDrawY(standY);
    }

    public void saveHome() {
        standX = x;
        standY = y;
        setSkillDrawY(standY);
    }

    private void setSkillDrawY(int dy) {
        if (skill1 != null) skill1.setDrawY(dy);
        if (skill2 != null) skill2.setDrawY(dy);
        if (skill3 != null) skill3.setDrawY(dy);
    }

    // ── Attack API ────────────────────────────────────────────────────────────
    public void startAttack(int skillNum, int enemyCentX) {
        if (attackState != AttackState.IDLE) return;
        pendingSkill = skillNum;
        targetX = facingRight
                ? enemyCentX - width
                : enemyCentX;
        attackState    = AttackState.WALKING_TO;
        walkFrameIndex = 1;
        walkAnimTick   = 0;
    }

    public boolean isAnyCastingSkill() { return attackState != AttackState.IDLE; }
    public boolean hasSkill1()         { return skill1 != null; }
    public boolean hasSkill2()         { return skill2 != null; }
    public boolean hasSkill3()         { return skill3 != null; }
    public boolean isCastingSkill1()   { return skill1 != null && skill1.isActive(); }
    public boolean isCastingSkill2()   { return skill2 != null && skill2.isActive(); }
    public boolean isCastingSkill3()   { return skill3 != null && skill3.isActive(); }

    // ── Update ────────────────────────────────────────────────────────────────
    @Override
    public void update() {
        switch (attackState) {

            case WALKING_TO: {
                facingRight = (targetX >= x);
                int dx = targetX - x;
                if (Math.abs(dx) <= ATTACK_WALK_SPEED) {
                    x = targetX;
                    fireSkill(pendingSkill);
                    attackState = AttackState.SKILL;
                    walkFrameIndex = 0;
                } else {
                    x += facingRight ? ATTACK_WALK_SPEED : -ATTACK_WALK_SPEED;
                    advanceWalkFrame();
                }
                break;
            }

            case SKILL: {
                if (skill1 != null && skill1.isActive()) skill1.tick();
                if (skill2 != null && skill2.isActive()) skill2.tick();
                if (skill3 != null && skill3.isActive()) skill3.tick();
                if (!anySkillActive()) {
                    facingRight    = (standX >= x);
                    attackState    = AttackState.WALKING_BACK;
                    walkFrameIndex = 1;
                    walkAnimTick   = 0;
                }
                break;
            }

            case WALKING_BACK: {
                facingRight = (standX >= x);
                int dx = standX - x;
                if (Math.abs(dx) <= ATTACK_WALK_SPEED) {
                    x = standX;
                    y = standY;
                    attackState    = AttackState.IDLE;
                    pendingSkill   = 0;
                    walkFrameIndex = 0;
                    walkAnimTick   = 0;
                } else {
                    x += facingRight ? ATTACK_WALK_SPEED : -ATTACK_WALK_SPEED;
                    advanceWalkFrame();
                }
                break;
            }

            default: break;
        }

        updateCurrentFrame();
    }

    private void fireSkill(int num) {
        if (num == 1 && skill1 != null) skill1.activate();
        if (num == 2 && skill2 != null) skill2.activate();
        if (num == 3 && skill3 != null) skill3.activate();
    }

    private boolean anySkillActive() {
        return (skill1 != null && skill1.isActive())
                || (skill2 != null && skill2.isActive())
                || (skill3 != null && skill3.isActive());
    }

    private void advanceWalkFrame() {
        if (framesRight == null || framesRight.length < 4) return;
        walkAnimTick++;
        if (walkAnimTick >= WALK_ANIM_SPEED) {
            walkAnimTick = 0;
            walkFrameIndex++;
            if (walkFrameIndex > 3) walkFrameIndex = 1;
        }
    }

    private void updateCurrentFrame() {
        if (!hasPNG || framesRight == null) return;

        int idx;
        if (attackState == AttackState.WALKING_TO || attackState == AttackState.WALKING_BACK) {
            idx = walkFrameIndex;
            if (idx < 1 || idx >= framesRight.length) idx = 1;
        } else {
            idx = 0;
        }

        BufferedImage[] arr = facingRight ? framesRight : framesLeft;
        if (arr[idx] != null)
            currentFrame = arr[idx];
        else if (arr[0] != null)
            currentFrame = arr[0];
    }

    // ── Draw ──────────────────────────────────────────────────────────────────
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
            if (facingRight)
                g2.drawImage(idleGif.getImage(), x, y, width, height, observer);
            else
                g2.drawImage(idleGif.getImage(), x + width, y, -width, height, observer);
        }
    }

    private void drawSkillFrame(Graphics2D g2, SkillData skill, ImageObserver observer) {
        BufferedImage frame = skill.getCurrentFrame();
        if (frame == null) return;
        int drawY = skill.getDrawY();

        if (facingRight) {
            g2.drawImage(frame, x, drawY, width, height, observer);
        } else {
            BufferedImage flipped = new BufferedImage(frame.getWidth(), frame.getHeight(),
                    BufferedImage.TYPE_INT_ARGB);
            Graphics2D fg = flipped.createGraphics();
            fg.drawImage(frame, frame.getWidth(), 0, -frame.getWidth(), frame.getHeight(), null);
            fg.dispose();
            g2.drawImage(flipped, x, drawY, width, height, observer);
        }
    }
}
package entities;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.awt.image.ImageObserver;
import java.util.ArrayList;
import javax.imageio.ImageIO;
import javax.swing.ImageIcon;

public class Character extends GameEntity {

    private ArrayList<BufferedImage> walkFramesRight;

    private BufferedImage idleFrameRight;

    private BufferedImage currentFrame;

    private ImageIcon idleGif;

    private int  walkFrameIndex;
    private int  walkAnimTick;
    private boolean walkForward;
    private final int WALK_ANIM_SPEED = 8;
    private SkillData skill1;
    private SkillData skill2;

    public boolean facingRight;
    public boolean movingLeft;
    public boolean movingRight;

    private int sheetHeight;
    private int bottomPadding;
    private boolean hasPNG;

    private int storedScreenH;

    public Character(String sheetPath, int[][] frameRegions, int sheetHeight,
                     int charW, int charH, int bottomPad,
                     String idleGifPath,
                     String sk1Sheet, int[][] sk1Regions, int sk1H, int sk1Pad,
                     String sk2Sheet, int[][] sk2Regions, int sk2H, int sk2Pad,
                     Class<?> loader) {

        super(0, 0, charW, charH, 5);

        this.sheetHeight   = (sheetHeight > 0) ? sheetHeight : charH;
        this.bottomPadding = bottomPad;
        this.storedScreenH = 600;

        this.walkFramesRight = new ArrayList<>();
        this.walkFrameIndex  = 1;
        this.walkAnimTick    = 0;
        this.walkForward     = true;
        this.hasPNG          = false;

        this.facingRight  = true;
        this.movingLeft   = false;
        this.movingRight  = false;

        if (sheetPath != null) {
            loadWalkSheet(sheetPath, frameRegions, loader);
        }

        loadIdleGif(idleGifPath, loader);

        if (sk1Sheet != null && sk1Regions != null) {
            skill1 = loadSkill(sk1Sheet, sk1Regions, sk1H, sk1Pad, loader);
        }

        if (sk2Sheet != null && sk2Regions != null) {
            skill2 = loadSkill(sk2Sheet, sk2Regions, sk2H, sk2Pad, loader);
        }
    }

    private void loadWalkSheet(String path, int[][] regions, Class<?> loader) {
        try {
            BufferedImage sheet = ImageIO.read(loader.getResource(path));

            for (int i = 0; i < regions.length; i++) {
                int x1         = regions[i][0];
                int x2         = regions[i][1];
                int frameWidth = x2 - x1 + 1;

                BufferedImage rightFrame = sheet.getSubimage(x1, 0, frameWidth, sheetHeight);
                walkFramesRight.add(rightFrame);
            }

            idleFrameRight = walkFramesRight.get(0);
            currentFrame   = idleFrameRight;
            hasPNG         = true;

            System.out.println("Walk sheet loaded: " + path);

        } catch (Exception e) {
            System.out.println("Walk sheet not found: " + path);
            hasPNG = false;
        }
    }

    private void loadIdleGif(String path, Class<?> loader) {
        if (path == null) return;
        try {
            idleGif = new ImageIcon(loader.getResource(path));
        } catch (Exception e) {
            System.out.println("Idle GIF not found: " + path);
        }
    }

    private SkillData loadSkill(String path, int[][] regions,
                                int sheetH, int botPad, Class<?> loader) {
        try {
            BufferedImage sheet = ImageIO.read(loader.getResource(path));

            ArrayList<BufferedImage> frames = new ArrayList<>();

            for (int i = 0; i < regions.length; i++) {
                int x1         = regions[i][0];
                int x2         = regions[i][1];
                int frameWidth = x2 - x1 + 1;
                frames.add(sheet.getSubimage(x1, 0, frameWidth, sheetH));
            }

            System.out.println("Skill sheet loaded: " + path);
            return new SkillData(frames, 10, botPad, sheetH);

        } catch (Exception e) {
            System.out.println("Skill sheet not found: " + path);
            return null;
        }
    }

    public void setImageObserver(ImageObserver observer) {
        if (idleGif != null) {
            idleGif.setImageObserver(observer);
        }
    }

    public void placeAtBottom(int screenW, int screenH) {
        storedScreenH = screenH;

        x = screenW / 2 - width / 2;

        int walkOffset = (int)(height * (double) bottomPadding / sheetHeight);
        y = screenH - height + walkOffset;

        if (skill1 != null) {
            int sk1Offset = (int)(height * (double) skill1.getBottomPadding()
                    / skill1.getSheetHeight());
            skill1.setDrawY(screenH - height + sk1Offset);
        }

        if (skill2 != null) {
            int sk2Offset = (int)(height * (double) skill2.getBottomPadding()
                    / skill2.getSheetHeight());
            skill2.setDrawY(screenH - height + sk2Offset);
        }
    }

    public void triggerSkill1() {
        if (skill1 == null) return;
        if (isAnyCastingSkill()) return;
        skill1.activate();
    }

    public void triggerSkill2() {
        if (skill2 == null) return;
        if (isAnyCastingSkill()) return;
        skill2.activate();
    }

    public boolean isAnyCastingSkill() {
        boolean skill1Active = (skill1 != null && skill1.isActive());
        boolean skill2Active = (skill2 != null && skill2.isActive());
        return skill1Active || skill2Active;
    }

    public boolean hasSkill1() { return skill1 != null; }
    public boolean hasSkill2() { return skill2 != null; }
    public boolean isCastingSkill1() { return skill1 != null && skill1.isActive(); }
    public boolean isCastingSkill2() { return skill2 != null && skill2.isActive(); }

    @Override
    public void update() {
        boolean skillBlocksMove = isAnyCastingSkill();

        if (!skillBlocksMove) {
            if (movingRight) x += speed;
            if (movingLeft)  x -= speed;
        }

        if (skill1 != null && skill1.isActive()) skill1.tick();
        if (skill2 != null && skill2.isActive()) skill2.tick();

        if (hasPNG && !isAnyCastingSkill()) {
            updateWalkAnimation();
        }
    }

    private void updateWalkAnimation() {
        boolean isMoving = movingLeft || movingRight;

        if (isMoving) {
            walkAnimTick++;

            if (walkAnimTick >= WALK_ANIM_SPEED) {
                walkAnimTick = 0;

                if (walkForward) {
                    walkFrameIndex++;
                    if (walkFrameIndex >= walkFramesRight.size() - 1) {
                        walkFrameIndex = walkFramesRight.size() - 1;
                        walkForward    = false;
                    }
                } else {
                    walkFrameIndex--;
                    if (walkFrameIndex <= 1) {
                        walkFrameIndex = 1;
                        walkForward    = true;
                    }
                }
            }

        } else {
            walkAnimTick   = 0;
            walkFrameIndex = 0;
            walkForward    = true;
        }

        // Always use the right-facing frame; draw() handles flipping
        currentFrame = walkFramesRight.get(walkFrameIndex);
    }

    @Override
    public void draw(Graphics g, ImageObserver observer) {
        Graphics2D g2 = (Graphics2D) g;

        // Skills
        if (skill1 != null && skill1.isActive()) {
            drawSkillFrame(g2, skill1, observer);
            return;
        }
        if (skill2 != null && skill2.isActive()) {
            drawSkillFrame(g2, skill2, observer);
            return;
        }

        // PNG walk/idle — always store right-facing frame, flip on draw
        if (hasPNG && currentFrame != null) {
            if (facingRight) {
                g2.drawImage(currentFrame, x, y, width, height, observer);
            } else {
                // Draw mirrored: start at x+width, use negative width to flip
                g2.drawImage(currentFrame, x + width, y, -width, height, observer);
            }
            return;
        }

        // GIF fallback
        if (idleGif != null) {
            if (facingRight) {
                g2.drawImage(idleGif.getImage(), x, y, width, height, observer);
            } else {
                g2.drawImage(idleGif.getImage(), x + width, y, -width, height, observer);
            }
        }
    }

    private void drawSkillFrame(Graphics2D g2, SkillData skill, ImageObserver observer) {
        BufferedImage frame = skill.getCurrentFrame();
        int drawY           = skill.getDrawY();

        if (facingRight) {
            g2.drawImage(frame, x, drawY, width, height, observer);
        } else {
            g2.drawImage(frame, x + width, drawY, -width, height, observer);
        }
    }
}
package entities;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.awt.image.ImageObserver;
import java.util.ArrayList;
import javax.imageio.ImageIO;
import javax.swing.ImageIcon;

public class Character extends GameEntity {

    private BufferedImage idleFrameRight;
    private BufferedImage idleFrameLeft;
    private ImageIcon     idleGif;

    private SkillData skill1;
    private SkillData skill2;

    public boolean facingRight = true;

    private final int sheetHeight;
    private final int bottomPadding;
    private boolean   hasPNG;

    public Character(String sheetPath, int[][] frameRegions, int sheetHeight,
                     int charW, int charH, int bottomPad,
                     String idleGifPath,
                     String sk1Sheet, int[][] sk1Regions, int sk1H, int sk1Pad,
                     String sk2Sheet, int[][] sk2Regions, int sk2H, int sk2Pad,
                     Class<?> loader) {

        super(0, 0, charW, charH, 5);
        this.sheetHeight   = sheetHeight > 0 ? sheetHeight : charH;
        this.bottomPadding = bottomPad;

        if (sheetPath != null) loadIdleFrame(sheetPath, frameRegions, loader);
        loadIdleGif(idleGifPath, loader);
        if (sk1Sheet != null && sk1Regions != null)
            skill1 = loadSkill(sk1Sheet, sk1Regions, sk1H, sk1Pad, loader);
        if (sk2Sheet != null && sk2Regions != null)
            skill2 = loadSkill(sk2Sheet, sk2Regions, sk2H, sk2Pad, loader);
    }

    private void loadIdleFrame(String path, int[][] regions, Class<?> loader) {
        try {
            BufferedImage sheet = ImageIO.read(loader.getResource(path));
            int fw = regions[0][1] - regions[0][0] + 1;
            idleFrameRight = sheet.getSubimage(regions[0][0], 0, fw, sheetHeight);

            idleFrameLeft = new BufferedImage(fw, sheetHeight, BufferedImage.TYPE_INT_ARGB);
            Graphics2D g2 = idleFrameLeft.createGraphics();
            g2.drawImage(idleFrameRight, fw, 0, -fw, sheetHeight, null);
            g2.dispose();

            hasPNG = true;
        } catch (Exception e) {
            System.out.println("Idle frame not found: " + path);
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
            for (int[] region : regions) {
                int fw = region[1] - region[0] + 1;
                frames.add(sheet.getSubimage(region[0], 0, fw, sheetH));
            }
            return new SkillData(frames, 10, botPad, sheetH);
        } catch (Exception e) {
            System.out.println("Skill sheet not found: " + path);
            return null;
        }
    }

    public void setImageObserver(ImageObserver observer) {
        if (idleGif != null) idleGif.setImageObserver(observer);
    }

    public void placeAtBottom(int screenW, int screenH) {
        x = screenW / 2 - width / 2;
        int offset = (int)(height * (double) bottomPadding / sheetHeight);
        y = screenH - height + offset;

        if (skill1 != null) {
            int off = (int)(height * (double) skill1.getBottomPadding() / skill1.getSheetHeight());
            skill1.setDrawY(screenH - height + off);
        }
        if (skill2 != null) {
            int off = (int)(height * (double) skill2.getBottomPadding() / skill2.getSheetHeight());
            skill2.setDrawY(screenH - height + off);
        }
    }

    public void triggerSkill1() {
        if (skill1 == null || isAnyCastingSkill()) return;
        skill1.activate();
    }

    public void triggerSkill2() {
        if (skill2 == null || isAnyCastingSkill()) return;
        skill2.activate();
    }

    public boolean isAnyCastingSkill() {
        return (skill1 != null && skill1.isActive())
                || (skill2 != null && skill2.isActive());
    }

    public boolean hasSkill1()       { return skill1 != null; }
    public boolean hasSkill2()       { return skill2 != null; }
    public boolean isCastingSkill1() { return skill1 != null && skill1.isActive(); }
    public boolean isCastingSkill2() { return skill2 != null && skill2.isActive(); }

    @Override
    public void update() {
        if (skill1 != null && skill1.isActive()) skill1.tick();
        if (skill2 != null && skill2.isActive()) skill2.tick();
    }

    @Override
    public void draw(Graphics g, ImageObserver observer) {
        Graphics2D g2 = (Graphics2D) g;

        if (skill1 != null && skill1.isActive()) { drawSkillFrame(g2, skill1, observer); return; }
        if (skill2 != null && skill2.isActive()) { drawSkillFrame(g2, skill2, observer); return; }

        if (hasPNG) {
            BufferedImage frame = facingRight ? idleFrameRight : idleFrameLeft;
            if (frame != null) { g2.drawImage(frame, x, y, width, height, observer); return; }
        }

        if (idleGif != null) {
            if (facingRight) g2.drawImage(idleGif.getImage(), x, y, width, height, observer);
            else             g2.drawImage(idleGif.getImage(), x + width, y, -width, height, observer);
        }
    }

    private void drawSkillFrame(Graphics2D g2, SkillData skill, ImageObserver observer) {
        BufferedImage frame = skill.getCurrentFrame();
        int drawY = skill.getDrawY();
        if (facingRight) g2.drawImage(frame, x, drawY, width, height, observer);
        else             g2.drawImage(frame, x + width, drawY, -width, height, observer);
    }
}
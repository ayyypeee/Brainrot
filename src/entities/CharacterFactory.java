package entities;

import java.util.ArrayList;

public class CharacterFactory {

    private ArrayList<String>   names;
    private ArrayList<String>   idleGifPaths;
    private ArrayList<String>   headPaths;
    private ArrayList<String>   walkSheets;
    private ArrayList<int[][]>  walkRegions;
    private ArrayList<Integer>  walkHeights;
    private ArrayList<Integer>  walkPaddings;
    private ArrayList<String>   skill1Sheets;
    private ArrayList<int[][]>  skill1Regions;
    private ArrayList<Integer>  skill1Heights;
    private ArrayList<Integer>  skill1Paddings;
    private ArrayList<String>   skill2Sheets;
    private ArrayList<int[][]>  skill2Regions;
    private ArrayList<Integer>  skill2Heights;
    private ArrayList<Integer>  skill2Paddings;

    public CharacterFactory() {
        names          = new ArrayList<>();
        idleGifPaths   = new ArrayList<>();
        headPaths      = new ArrayList<>();
        walkSheets     = new ArrayList<>();
        walkRegions    = new ArrayList<>();
        walkHeights    = new ArrayList<>();
        walkPaddings   = new ArrayList<>();
        skill1Sheets   = new ArrayList<>();
        skill1Regions  = new ArrayList<>();
        skill1Heights  = new ArrayList<>();
        skill1Paddings = new ArrayList<>();
        skill2Sheets   = new ArrayList<>();
        skill2Regions  = new ArrayList<>();
        skill2Heights  = new ArrayList<>();
        skill2Paddings = new ArrayList<>();

        registerAllCharacters();
    }

    private void registerAllCharacters() {

        addCharacter(
                "AIP",
                "/characters/idle_gif/v1_aip_moving.gif",
                "/characters/heads/AIP(Head).png",
                "/characters/walk_png/v1_clean_aip_walk.png",
                new int[][]{{296,519},{656,871},{1024,1239},{1368,1583}}, 512, 72,
                "/characters/skills/skill1/v1 sk1 aip.png",
                new int[][]{{128,359},{704,1031},{1360,1607}}, 512, 40,
                "/characters/skills/skill2/v1 sk2 aip.png",
                new int[][]{{128,503},{640,1151},{1256,1767}}, 512, 40
        );

        addCharacter(
                "Christian",
                "/characters/idle_gif/v1_christian_moving_idle.gif",
                "/characters/heads/christian(Head).png",
                null, null, 0, 72,
                "/characters/skills/skill1/v1 sk1 christian.png",
                new int[][]{{144,359},{592,927},{1072,1271}}, 512, 32,
                null, null, 0, 0
        );

        addCharacter(
                "Kimmay",
                "/characters/idle_gif/v1_kimmay_moving_idle.gif",
                "/characters/heads/kimmay(Head).png",
                "/characters/walk_png/v2_clean_kimwalking.png",
                new int[][]{{40,287},{384,615},{728,967},{1056,1287}}, 560, 48,
                "/characters/skills/skill1/v1 skill1 kimmay (1).png",
                new int[][]{{80,439},{592,1047},{1216,1495},{1504,1583}}, 512, 48,
                "/characters/skills/skill2/v1_kimmay_skill2.png",
                new int[][]{{24,455},{576,1047},{1160,1567}}, 512, 16
        );

        addCharacter(
                "Dianne",
                "/characters/idle_gif/v1_dianne_moving_idle.gif",
                "/characters/heads/dianne (2)(Head).png",
                "/characters/walk_png/v1_clean_diannewalking.png",
                new int[][]{{48,247},{328,527},{608,823},{904,1103}}, 512, 40,
                "/characters/skills/skill1/v1 sk1 dianne.png",
                new int[][]{{128,447},{688,1111},{1392,1687}}, 512, 32,
                "/characters/skills/skill2/v1 sk2 dianne .png",
                new int[][]{{152,367},{480,847},{928,1271}}, 512, 24
        );

        addCharacter(
                "Cyberg",
                "/characters/idle_gif/v1_cyberg_moving_idle.gif",
                "/characters/heads/cyberg(Head).png",
                "/characters/walk_png/v1_clean_cyberg_walk.png",
                new int[][]{{64,271},{408,615},{768,967},{1104,1303}}, 512, 32,
                "/characters/skills/skill1/v1_cyberg_skill1.png",
                new int[][]{{144,367},{504,823},{952,1183}}, 512, 48,
                "/characters/skills/skill2/v1 sk2 cyberg.png",
                new int[][]{{136,343},{432,647},{736,1015},{1136,1287}}, 512, 40
        );

        addCharacter(
                "Tung Tung",
                "/characters/idle_gif/v1_tungtung_moving_idle.gif",
                null,
                "/characters/walk_png/v1_RIGHT_walking_tungtung_SHEET.png",
                new int[][]{{40,199},{264,463},{504,743},{808,983}}, 512, 64,
                "/characters/skills/skill1/v1_tungtung_skill1.png",
                new int[][]{{32,327},{432,863},{912,1247}}, 512, 16,
                "/characters/skills/skill2/v1_tungtung_skill2.png",
                new int[][]{{24,439},{464,879},{920,1311},{1336,1751}}, 512, 24
        );

        addCharacter(
                "Cappucino",
                "/characters/idle_gif/v1_cappucino_moving_idle.gif",
                "/characters/heads/cappucino head.png",
                "/characters/walk_png/v1_cappucino_walking.png",
                new int[][]{{72,335},{400,711},{792,1063}}, 512, 88,
                "/characters/skills/skill1/v1_cappucino_skill1.png",
                new int[][]{{32,311},{360,695}}, 512, 80,
                "/characters/skills/skill2/v1_cappucino_skill2.png",
                new int[][]{{56,247},{312,623},{688,1175}}, 512, 120
        );

        addCharacter(
                "Ballerina",
                "/characters/idle_gif/v1_ballerina_moving_idle.gif",
                null,
                "/characters/walk_png/v1_ballerina_walking.png",
                new int[][]{{16,223},{272,471},{536,735}}, 512, 64,
                "/characters/skills/skill1/ballerina_cappucina_skill1.png",
                new int[][]{{64,295},{352,663},{752,967}}, 512, 8,
                "/characters/skills/skill2/v1_ballerina_skill2.png",
                new int[][]{{40,239},{328,695},{728,999}}, 512, 32
        );

        addCharacter(
                "Tralalelo",
                "/characters/idle_gif/v1_tralalelo_moving_idle.gif",
                null,
                "/characters/walk_png/v1_tralalelo_walking.png",
                new int[][]{{16,391},{448,823},{888,1263}}, 512, 168,
                "/characters/skills/skill1/v1_tralalelo_skill1.png",
                new int[][]{{104,383},{456,1111}}, 512, 64,
                "/characters/skills/skill2/v1_tralalelo_skill2.png",
                new int[][]{{48,335},{368,719},{728,1143}}, 512, 32
        );
    }

    private void addCharacter(
            String name, String idleGif, String headPath,
            String walkSheet, int[][] walkReg, int walkH, int walkPad,
            String sk1Sheet, int[][] sk1Reg, int sk1H, int sk1Pad,
            String sk2Sheet, int[][] sk2Reg, int sk2H, int sk2Pad) {

        names.add(name);
        idleGifPaths.add(idleGif);
        headPaths.add(headPath);
        walkSheets.add(walkSheet);
        walkRegions.add(walkReg);
        walkHeights.add(Integer.valueOf(walkH));
        walkPaddings.add(Integer.valueOf(walkPad));
        skill1Sheets.add(sk1Sheet);
        skill1Regions.add(sk1Reg);
        skill1Heights.add(Integer.valueOf(sk1H));
        skill1Paddings.add(Integer.valueOf(sk1Pad));
        skill2Sheets.add(sk2Sheet);
        skill2Regions.add(sk2Reg);
        skill2Heights.add(Integer.valueOf(sk2H));
        skill2Paddings.add(Integer.valueOf(sk2Pad));
    }

    public int getCount() { return names.size(); }

    public String getName(int index) { return names.get(index); }

    public String getIdleGifPath(int index) { return idleGifPaths.get(index); }

    public String getHeadPath(int index) { return headPaths.get(index); }

    public String getWalkSheet(int index) { return walkSheets.get(index); }

    public String getSkill1Sheet(int index) { return skill1Sheets.get(index); }

    public String getSkill2Sheet(int index) { return skill2Sheets.get(index); }

    public Character buildCharacter(int index, Class<?> loader) {
        return new Character(
                walkSheets.get(index),
                walkRegions.get(index),
                walkHeights.get(index).intValue(),
                128, 280,
                walkPaddings.get(index).intValue(),
                idleGifPaths.get(index),
                skill1Sheets.get(index),
                skill1Regions.get(index),
                skill1Heights.get(index).intValue(),
                skill1Paddings.get(index).intValue(),
                skill2Sheets.get(index),
                skill2Regions.get(index),
                skill2Heights.get(index).intValue(),
                skill2Paddings.get(index).intValue(),
                loader
        );
    }
}
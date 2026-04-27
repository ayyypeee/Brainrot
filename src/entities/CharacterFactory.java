package entities;

import java.util.ArrayList;

public class CharacterFactory {

    private final ArrayList<String>   names          = new ArrayList<>();
    private final ArrayList<String>   idleGifPaths   = new ArrayList<>();
    private final ArrayList<String>   headPaths      = new ArrayList<>();
    private final ArrayList<String[]> walkFramePaths = new ArrayList<>();
    private final ArrayList<String[]> skill1Paths    = new ArrayList<>();
    private final ArrayList<String[]> skill2Paths    = new ArrayList<>();
    private final ArrayList<String[]> skill3Paths    = new ArrayList<>();
    private final ArrayList<String>   skill1Names    = new ArrayList<>();
    private final ArrayList<String>   skill2Names    = new ArrayList<>();
    private final ArrayList<String>   skill3Names    = new ArrayList<>();
    private final ArrayList<int[]>    skill1IconRect = new ArrayList<>();
    private final ArrayList<int[]>    skill2IconRect = new ArrayList<>();
    private final ArrayList<int[]>    skill3IconRect = new ArrayList<>();

    public CharacterFactory() {
        registerAllCharacters();
    }

    // ── Helper ────────────────────────────────────────────────────────────────
    private static String[] walkFrames(String base) {
        return new String[]{
                "/characters/walk_png/" + base + ".png",
                "/characters/walk_png/" + base + " (2).png",
                "/characters/walk_png/" + base + " (3).png",
                "/characters/walk_png/" + base + " (4).png"
        };
    }

    // ── Registration ──────────────────────────────────────────────────────────
    private void registerAllCharacters() {

        // ── AIP ───────────────────────────────────────────────────────────────
        add("AIP",
                "/characters/idle_gif/v1_aip_moving.gif",
                "/characters/heads/AIP(Head).png",
                walkFrames("v1_clean_aip_walk"),
                new String[]{
                        "/characters/skills/skill1/v2_aip skill 1(first skill).png",
                        "/characters/skills/skill1/v2_aip skill 1(second skil).png",
                        "/characters/skills/skill1/v2_aip skill 1(third skill).png"
                }, "Pen Slash", new int[]{130, 500, 115, 115},
                new String[]{
                        "/characters/skills/skill2/v2_aip skill 2(first skill).png",
                        "/characters/skills/skill2/v2_aip skill 2(second skill).png",
                        "/characters/skills/skill2/v2_aip skill 2(third skill).png"
                }, "Salt Bomb", new int[]{245, 500, 115, 115},
                new String[]{
                        "/characters/skills/skill3/v2_aip skill 3(first).png",
                        "/characters/skills/skill3/v2_aip skill 3(secondt).png",
                        "/characters/skills/skill3/v2_aip skill 3(third).png"
                }, "Bag Smash", new int[]{360, 500, 115, 115});

        // ── Christian ─────────────────────────────────────────────────────────
        add("Christian",
                "/characters/idle_gif/v1_christian_moving_idle.gif",
                "/characters/heads/christian(Head).png",
                walkFrames("v1_clean_christian_walk"),
                new String[]{
                        "/characters/skills/skill1/v2_christian skill 1(first skill).png",
                        "/characters/skills/skill1/v2_christian skill1(second skill).png",
                        "/characters/skills/skill1/v2_christian skill1(third skill).png"
                }, "Swift Punch", new int[]{500, 752, 115, 115},
                new String[]{
                        "/characters/skills/skill2/v2_christian skill 2(first skill).png",
                        "/characters/skills/skill2/v2_christian skill 2(second skill).png",
                        "/characters/skills/skill2/v2_christian skill 2(third skill).png"
                }, "Counter", new int[]{615, 752, 115, 115},
                new String[]{
                        "/characters/skills/skill3/v2_christian skill 3(first).png",
                        "/characters/skills/skill3/v2_christian skill 3(second).png",
                        "/characters/skills/skill3/v2_christian skill 3(third).png"
                }, "Eye Beam", new int[]{730, 752, 115, 115});

        // ── Kimmay ────────────────────────────────────────────────────────────
        add("Kimmay",
                "/characters/idle_gif/v1_kimmay_moving_idle.gif",
                "/characters/heads/kimmay(Head).png",
                walkFrames("v2_clean_kimwalking"),
                new String[]{
                        "/characters/skills/skill1/v2_kim skill 1(first skill).png",
                        "/characters/skills/skill1/v2_kim skill1(second skill).png",
                        "/characters/skills/skill1/v2_kim skill 1(third skill).png"
                }, "Music Note", new int[]{500, 500, 115, 115},
                new String[]{
                        "/characters/skills/skill2/v2_kim skill 2(first).png",
                        "/characters/skills/skill2/v2_kim skill 2(second).png",
                        "/characters/skills/skill2/v2_kim skill 2(third).png"
                }, "Star Burst", new int[]{615, 500, 115, 115},
                new String[]{
                        "/characters/skills/skill3/v2_kim skill 3(first).png",
                        "/characters/skills/skill3/v2_kim skill 3(second).png",
                        "/characters/skills/skill3/v2_kim skill 3(third).png"
                }, "Radiance", new int[]{730, 500, 115, 115});

        // ── Dianne ────────────────────────────────────────────────────────────
        add("Dianne",
                "/characters/idle_gif/v1_dianne_moving_idle.gif",
                "/characters/heads/dianne (2)(Head).png",
                walkFrames("v1_clean_diannewalking"),
                new String[]{
                        "/characters/skills/skill1/v2_dianne skill 1(first skill).png",
                        "/characters/skills/skill1/v2_dianne skill 1(second skill).png",
                        "/characters/skills/skill1/v2_dianne skill 1(third skill).png"
                }, "Mystery Fist", new int[]{130, 626, 115, 115},
                new String[]{
                        "/characters/skills/skill2/v2_dianne skill 2(first).png",
                        "/characters/skills/skill2/v2_dianne skill 2(second).png",
                        "/characters/skills/skill2/v2_dianne skill 2(third)).png"
                }, "Trap", new int[]{245, 626, 115, 115},
                new String[]{
                        "/characters/skills/skill3/v2_dianne skill 3(first).png",
                        "/characters/skills/skill3/v2_dianne skill 3(second).png",
                        "/characters/skills/skill3/v2_dianne skill 3(third).png"
                }, "Labyrinth", new int[]{360, 626, 115, 115});

        // ── Cyberg ────────────────────────────────────────────────────────────
        add("Cyberg",
                "/characters/idle_gif/v1_cyberg_moving_idle.gif",
                "/characters/heads/cyberg(Head).png",
                walkFrames("v1_clean_cyberg_walk"),
                new String[]{
                        "/characters/skills/skill1/v2_cyberg skill 1(first skill).png",
                        "/characters/skills/skill1/v2_cyberg skill 1(second skill).png",
                        "/characters/skills/skill1/v2_cyberg skill 1(third skill).png"
                }, "Cyber Punch", new int[]{130, 752, 115, 115},
                new String[]{
                        "/characters/skills/skill2/v2_cyberg skill 2(first skill).png",
                        "/characters/skills/skill2/v2_cyberg skill 2(second).png",
                        "/characters/skills/skill2/v2_cyberg skill 2(third).png",
                        "/characters/skills/skill2/v2_cyberg skill 2(fourth).png"
                }, "Speed Dash", new int[]{245, 752, 115, 115},
                new String[]{
                        "/characters/skills/skill3/v2_cyberg skill 3(first).png",
                        "/characters/skills/skill3/v2_cyberg skill 3(second).png",
                        "/characters/skills/skill3/v2_cyberg skill 3(third).png"
                }, "Shadow Jab", new int[]{360, 752, 115, 115});

        // ── Tung Tung ─────────────────────────────────────────────────────────
        add("Tung Tung",
                "/characters/idle_gif/v1_tungtung_moving_idle.gif",
                "/characters/heads/tungtung(Head).png",
                walkFrames("v1_RIGHT_walking_tungtung_SHEET"),
                new String[]{
                        "/characters/skills/skill1/tungtung_skill1_frame1.png",
                        "/characters/skills/skill1/tungtung_skill1_frame2.png",
                        "/characters/skills/skill1/tungtung_skill1_frame3.png"
                }, "Bat Spin", new int[]{130, 372, 115, 115},
                new String[]{
                        "/characters/skills/skill2/tungtung_sk2_fr1.png",
                        "/characters/skills/skill2/tungtung_sk1_fr2.png",
                        "/characters/skills/skill2/tungtung_sk2_fr3.png",
                        "/characters/skills/skill2/tungtung_sk2_fr4.png",
                        "/characters/skills/skill2/tungtung_sk2_fr5.png",
                        "/characters/skills/skill2/tungtung_sk2_fr6.png"
                }, "Bat Crash", new int[]{245, 372, 115, 115},
                new String[]{
                        "/characters/skills/skill3/tungtung_sk3_fr1.png",
                        "/characters/skills/skill3/tungtung_sk3_fr2.png",
                        "/characters/skills/skill3/tungtung_sk3_fr3.png"
                }, "Lightning", new int[]{360, 372, 115, 115});

        // ── Cappucino ─────────────────────────────────────────────────────────
        add("Cappucino",
                "/characters/idle_gif/v1_cappucino_moving_idle.gif",
                "/characters/heads/cappucino head.png",
                walkFrames("v1_cappucino_walking"),
                new String[]{
                        "/characters/skills/skill1/assasino cappucino_sk1_fr1.png",
                        "/characters/skills/skill1/assasino cappucino_sk1_fr2.png",
                        "/characters/skills/skill1/assasino cappucino_sk1_fr3.png"
                }, "Coffee Slash", new int[]{500, 372, 115, 115},
                new String[]{
                        "/characters/skills/skill2/assasino_sk2_fr1.png",
                        "/characters/skills/skill2/assasino_sk2_fr2.png",
                        "/characters/skills/skill2/assasino_sk2_fr3.png"
                }, "Blade Cross", new int[]{615, 372, 115, 115},
                new String[]{
                        "/characters/skills/skill3/assasino_sk3_fr1.png",
                        "/characters/skills/skill3/assasino_sk3_fr2.png",
                        "/characters/skills/skill3/assasino_sk3_fr3.png"
                }, "Pentagram", new int[]{730, 372, 115, 115});

        // ── Ballerina ─────────────────────────────────────────────────────────
        add("Ballerina",
                "/characters/idle_gif/v1_ballerina_moving_idle.gif",
                "/characters/heads/ballerina(Head).png",
                walkFrames("v1_ballerina_walking"),
                new String[]{
                        "/characters/skills/skill1/ballerina_skill1_frame1.png",
                        "/characters/skills/skill1/ballerina_skil1_frame2.png",
                        "/characters/skills/skill1/ballerina_skill1_frame3.png"
                }, "Leg Sweep", new int[]{500, 245, 115, 115},
                new String[]{
                        "/characters/skills/skill2/ballerina_sk2_fr1.png",
                        "/characters/skills/skill2/ballerina_sk2_fr2.png",
                        "/characters/skills/skill2/ballerina_sk2_fr3.png"
                }, "Heart Beat", new int[]{615, 245, 115, 115},
                new String[]{
                        "/characters/skills/skill3/ballerina_sk3_fr1.png",
                        "/characters/skills/skill3/ballerina_sk3_fr2.png",
                        "/characters/skills/skill3/ballerina_sk3_fr3.png"
                }, "Rose Storm", new int[]{730, 245, 115, 115});

        // ── Tralalelo ─────────────────────────────────────────────────────────
        add("Tralalelo",
                "/characters/idle_gif/v1_tralalelo_moving_idle.gif",
                "/characters/heads/tralalelo(Head).png",
                walkFrames("v1_tralalelo_walking"),
                new String[]{
                        "/characters/skills/skill1/tralalelo_s1_fr1.png",
                        "/characters/skills/skill1/tralalelo_s1_fr2.png",
                        "/characters/skills/skill1/tralalelo_s1_fr3.png"
                }, "Shark Bite", new int[]{130, 372, 115, 115},
                new String[]{
                        "/characters/skills/skill2/tralalelo_sk2_fr1.png",
                        "/characters/skills/skill2/tralalelo_sk2_fr2.png",
                        "/characters/skills/skill2/tralalelo_sk2_fr3.png"
                }, "Crunch", new int[]{245, 372, 115, 115},
                new String[]{
                        "/characters/skills/skill3/tralalelo_sk3_fr1.png",
                        "/characters/skills/skill3/tralalelo_sk3_fr2.png",
                        "/characters/skills/skill3/tralalelo_sk3_fr3.png"
                }, "Explosion", new int[]{360, 372, 115, 115});
    }

    // ── Internal add ──────────────────────────────────────────────────────────
    private void add(String name, String idleGif, String headPath,
                     String[] walkPaths,
                     String[] sk1, String sk1Name, int[] sk1Icon,
                     String[] sk2, String sk2Name, int[] sk2Icon,
                     String[] sk3, String sk3Name, int[] sk3Icon) {
        names.add(name);
        idleGifPaths.add(idleGif);
        headPaths.add(headPath);
        walkFramePaths.add(walkPaths);
        skill1Paths.add(sk1); skill1Names.add(sk1Name); skill1IconRect.add(sk1Icon);
        skill2Paths.add(sk2); skill2Names.add(sk2Name); skill2IconRect.add(sk2Icon);
        skill3Paths.add(sk3); skill3Names.add(sk3Name); skill3IconRect.add(sk3Icon);
    }

    // ── Public getters ────────────────────────────────────────────────────────
    public int      getCount()            { return names.size(); }
    public String   getName(int i)        { return names.get(i); }
    public String   getIdleGifPath(int i) { return idleGifPaths.get(i); }
    public String   getHeadPath(int i)    { return headPaths.get(i); }
    public String   getWalkSheet(int i)   { return walkFramePaths.get(i)[0]; }
    public String   getSkill1Name(int i)  { return skill1Names.get(i); }
    public String   getSkill2Name(int i)  { return skill2Names.get(i); }
    public String   getSkill3Name(int i)  { return skill3Names.get(i); }
    public int[]    getSkill1Icon(int i)  { return skill1IconRect.get(i); }
    public int[]    getSkill2Icon(int i)  { return skill2IconRect.get(i); }
    public int[]    getSkill3Icon(int i)  { return skill3IconRect.get(i); }
    public String[] getSkill1Paths(int i) { return skill1Paths.get(i); }
    public String[] getSkill2Paths(int i) { return skill2Paths.get(i); }
    public String[] getSkill3Paths(int i) { return skill3Paths.get(i); }

    // ── Build ─────────────────────────────────────────────────────────────────
    public Character buildCharacter(int index, Class<?> loader, int screenW, int screenH) {
        int charH = (int)(screenH * 0.28);
        int charW = (int)(charH  * 0.57);

        return new Character(
                walkFramePaths.get(index),
                charW, charH,
                idleGifPaths.get(index),
                skill1Paths.get(index),
                skill2Paths.get(index),
                skill3Paths.get(index),
                loader);
    }
}
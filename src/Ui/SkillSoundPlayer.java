package Ui;

import javax.sound.sampled.*;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;

/**
 * Plays the per-character, per-skill WAV sound effect when a hit lands.
 *
 * Place WAV files under:
 *   src/main/resources/audio/skill_sfx/skill1/
 *   src/main/resources/audio/skill_sfx/skill2/
 *   src/main/resources/audio/skill_sfx/skill3/
 *
 * Expected filenames  (case-sensitive on some OS):
 *   skill1 : Aip_1.wav  Ballerina_1.wav  Assasino_1.wav  Christian_1.wav
 *             Cyberg_1.wav  Dianne_1.wav  Kim_1.wav  Tralalelo_1.wav (or tralalelo_1.wav)  Tungtung_1.wav
 *   skill2 : aip_2.wav  Ballerina_2.wav  assasino_2.wav  Christian_2.wav
 *             Cyberg_2.wav  Dianne_2.wav  Kim_2.wav  Tralalelo_2.wav  TungtungLOOP3x_2.wav
 *   skill3 : aip_3.wav  Ballerina_3.wav  assasino_3.wav  Christian_3.wav
 *             Cyberg_3.wav  Dianne_3.wav  Kim_3.wav  Tralalelo_3.wav  Tungtung_3.wav
 *
 * Clips are cached after the first load so repeated plays are instant.
 * Each call to play() stops any previously playing SFX clip first.
 */
public class SkillSoundPlayer {

    // ── Resource paths ────────────────────────────────────────────────────────
    // key: "CharName_skillNum"  →  value: resource path
    private static final Map<String, String> PATHS = new HashMap<>();

    static {
        // ── Skill 1 ───────────────────────────────────────────────────────────
        PATHS.put("AIP_1",       "/audio/skill_sfx/skill1/Aip.wav");
        PATHS.put("Ballerina_1", "/audio/skill_sfx/skill1/Ballerina.wav");
        PATHS.put("Cappucino_1", "/audio/skill_sfx/skill1/Assasino.wav");
        PATHS.put("Christian_1", "/audio/skill_sfx/skill1/Christian.wav");
        PATHS.put("Cyberg_1",    "/audio/skill_sfx/skill1/Cyberg.wav");
        PATHS.put("Dianne_1",    "/audio/skill_sfx/skill1/Dianne.wav");
        PATHS.put("Kimmay_1",    "/audio/skill_sfx/skill1/Kim.wav");
        PATHS.put("Tralalelo_1", "/audio/skill_sfx/skill1/tralalelo.wav");
        PATHS.put("Tung Tung_1", "/audio/skill_sfx/skill1/Tungtung.wav");

        // ── Skill 2 ───────────────────────────────────────────────────────────
        PATHS.put("AIP_2",       "/audio/skill_sfx/skill2/aip_2.wav");
        PATHS.put("Ballerina_2", "/audio/skill_sfx/skill2/Ballerina_2.wav");
        PATHS.put("Cappucino_2", "/audio/skill_sfx/skill2/assasino_2.wav");
        PATHS.put("Christian_2", "/audio/skill_sfx/skill2/Christian_2.wav");
        PATHS.put("Cyberg_2",    "/audio/skill_sfx/skill2/Cyberg_2.wav");
        PATHS.put("Dianne_2",    "/audio/skill_sfx/skill2/Dianne_2.wav");
        PATHS.put("Kimmay_2",    "/audio/skill_sfx/skill2/Kim_2.wav");
        PATHS.put("Tralalelo_2", "/audio/skill_sfx/skill2/Tralalelo_2.wav");
        PATHS.put("Tung Tung_2", "/audio/skill_sfx/skill2/TungtungLOOP3x.wav");

        // ── Skill 3 ───────────────────────────────────────────────────────────
        PATHS.put("AIP_3",       "/audio/skill_sfx/skill3/aip_3.wav");
        PATHS.put("Ballerina_3", "/audio/skill_sfx/skill3/Ballerina_3.wav");
        PATHS.put("Cappucino_3", "/audio/skill_sfx/skill3/assasino_3.wav");
        PATHS.put("Christian_3", "/audio/skill_sfx/skill3/Christian_3.wav");
        PATHS.put("Cyberg_3",    "/audio/skill_sfx/skill3/Cyberg_3.wav");
        PATHS.put("Dianne_3",    "/audio/skill_sfx/skill3/Dianne_3.wav");
        PATHS.put("Kimmay_3",    "/audio/skill_sfx/skill3/Kim_3.wav");
        PATHS.put("Tralalelo_3", "/audio/skill_sfx/skill3/Tralalelo_3.wav");
        PATHS.put("Tung Tung_3", "/audio/skill_sfx/skill3/Tungtung_3.wav");
    }

    // Clip cache — loaded once, reused on every subsequent play
    private static final Map<String, Clip> CACHE = new HashMap<>();

    // The SFX clip currently playing (so we can stop it before playing a new one)
    private static Clip current = null;

    /**
     * Plays the hit sound for the given attacker character name and skill number.
     * Safe to call from any thread; silently ignores missing resources.
     *
     * @param charName  Exact character name as used in CharSkillDB (e.g. "Tung Tung", "AIP")
     * @param skillNum  1, 2, or 3
     */
    public static void play(String charName, int skillNum) {
        String key  = charName + "_" + skillNum;
        String path = PATHS.get(key);
        if (path == null) return;          // no mapping defined — silent

        // Stop whatever SFX is already playing
        stopCurrent();

        Clip clip = getOrLoad(key, path);
        if (clip == null) return;

        current = clip;
        clip.setFramePosition(0);
        clip.start();
    }

    /** Stops the current SFX clip immediately (called internally before each play). */
    public static void stopCurrent() {
        if (current != null && current.isRunning()) {
            current.stop();
        }
        current = null;
    }

    // ── Internal loader / cache ───────────────────────────────────────────────

    private static Clip getOrLoad(String key, String resourcePath) {
        // Return cached clip if it is still open
        Clip cached = CACHE.get(key);
        if (cached != null && cached.isOpen()) return cached;

        try {
            URL url = SkillSoundPlayer.class.getResource(resourcePath);
            if (url == null) {
                System.out.println("SkillSoundPlayer: resource not found: " + resourcePath);
                CACHE.put(key, null);   // cache the miss so we don't retry every frame
                return null;
            }
            AudioInputStream ais = AudioSystem.getAudioInputStream(url);
            Clip clip = AudioSystem.getClip();
            clip.open(ais);
            CACHE.put(key, clip);
            return clip;
        } catch (Exception e) {
            System.out.println("SkillSoundPlayer: failed to load " + resourcePath
                    + " — " + e.getMessage());
            CACHE.put(key, null);
            return null;
        }
    }
}
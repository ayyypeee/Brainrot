package Ui;

import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.Clip;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;

public class SkillSoundPlayer {


    private static Map<String, String> paths = new HashMap<String, String>();

    static {
        // Skill 1 sounds
        paths.put("AIP_1",       "/audio/skill_sfx/skill1/Aip.wav");
        paths.put("Ballerina_1", "/audio/skill_sfx/skill1/Ballerina.wav");
        paths.put("Cappucino_1", "/audio/skill_sfx/skill1/Assasino.wav");
        paths.put("Christian_1", "/audio/skill_sfx/skill1/Christian.wav");
        paths.put("Cyberg_1",    "/audio/skill_sfx/skill1/Cyberg.wav");
        paths.put("Dianne_1",    "/audio/skill_sfx/skill1/Dianne.wav");
        paths.put("Kimmay_1",    "/audio/skill_sfx/skill1/Kim.wav");
        paths.put("Tralalelo_1", "/audio/skill_sfx/skill1/Tralalelo_1.wav");
        paths.put("Tung Tung_1", "/audio/skill_sfx/skill1/Tungtung.wav");

        // Skill 2 sounds
        paths.put("AIP_2",       "/audio/skill_sfx/skill2/aip_2.wav");
        paths.put("Ballerina_2", "/audio/skill_sfx/skill2/Ballerina_2.wav");
        paths.put("Cappucino_2", "/audio/skill_sfx/skill2/assasino_2.wav");
        paths.put("Christian_2", "/audio/skill_sfx/skill2/Christian_2.wav");
        paths.put("Cyberg_2",    "/audio/skill_sfx/skill2/Cyberg_2.wav");
        paths.put("Dianne_2",    "/audio/skill_sfx/skill2/Dianne_2.wav");
        paths.put("Kimmay_2",    "/audio/skill_sfx/skill2/Kim_2.wav");
        paths.put("Tralalelo_2", "/audio/skill_sfx/skill2/Tralalelo_2.wav");
        paths.put("Tung Tung_2", "/audio/skill_sfx/skill2/Tungtung_2.wav");

        // Skill 3 sounds
        paths.put("AIP_3",       "/audio/skill_sfx/skill3/aip_3.wav");
        paths.put("Ballerina_3", "/audio/skill_sfx/skill3/Ballerina_3.wav");
        paths.put("Cappucino_3", "/audio/skill_sfx/skill3/assasino_3.wav");
        paths.put("Christian_3", "/audio/skill_sfx/skill3/Christian_3.wav");
        paths.put("Cyberg_3",    "/audio/skill_sfx/skill3/Cyberg_3.wav");
        paths.put("Dianne_3",    "/audio/skill_sfx/skill3/Dianne_3.wav");
        paths.put("Kimmay_3",    "/audio/skill_sfx/skill3/Kim_3.wav");
        paths.put("Tralalelo_3", "/audio/skill_sfx/skill3/Tralalelo_3.wav");
        paths.put("Tung Tung_3", "/audio/skill_sfx/skill3/Tungtung_3.wav");
    }

    // Cache so each clip is only loaded from disk once.
    private static Map<String, Clip> cache = new HashMap<String, Clip>();

    // The clip currently playing so we can stop it before playing a new one.
    private static Clip current = null;

    // Plays the sound for the given character name and skill number.
    public static void play(String charName, int skillNum) {
        String key  = charName + "_" + skillNum;
        String path = paths.get(key);
        if (path == null) {
            return; // no sound registered for this skill
        }

        stopCurrent();

        Clip clip = getOrLoad(key, path);
        if (clip == null) {
            return;
        }

        current = clip;
        clip.setFramePosition(0);
        clip.start();
    }

    // Stops the currently playing sound effect.
    public static void stopCurrent() {
        if (current != null && current.isRunning()) {
            current.stop();
        }
        current = null;
    }

    // Returns a cached clip, or loads it from disk if not yet cached.
    private static Clip getOrLoad(String key, String resourcePath) {
        Clip cached = cache.get(key);
        if (cached != null && cached.isOpen()) {
            return cached;
        }

        try {
            URL url = SkillSoundPlayer.class.getResource(resourcePath);
            if (url == null) {
                System.out.println("SkillSoundPlayer: file not found: " + resourcePath);
                cache.put(key, null);
                return null;
            }
            AudioInputStream ais = AudioSystem.getAudioInputStream(url);
            Clip clip = AudioSystem.getClip();
            clip.open(ais);
            cache.put(key, clip);
            return clip;
        } catch (Exception e) {
            System.out.println("SkillSoundPlayer: failed to load " + resourcePath + " — " + e.getMessage());
            cache.put(key, null);
            return null;
        }
    }
}
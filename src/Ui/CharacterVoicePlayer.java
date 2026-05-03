package Ui;

import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.Clip;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;

// Plays a short voice clip when a character is selected on the character screen.
public class CharacterVoicePlayer {

    // Maps each character name to the path of their voice WAV file.
    private static Map<String, String> voicePaths = new HashMap<String, String>();

    // Static block fills the map once when the class is first used.
    static {
        voicePaths.put("AIP",       "/characters/voices/AIP.wav");
        voicePaths.put("Ballerina", "/characters/voices/Ballerina.wav");
        voicePaths.put("Cappucino", "/characters/voices/Cappucino.wav");
        voicePaths.put("Christian", "/characters/voices/Christian.wav");
        voicePaths.put("Cyberg",    "/characters/voices/Cyberg.wav");
        voicePaths.put("Dianne",    "/characters/voices/Dianne.wav");
        voicePaths.put("Kimmay",    "/characters/voices/Kimmay.wav");
        voicePaths.put("Tralalelo", "/characters/voices/Tralalelo.wav");
        voicePaths.put("Tung Tung", "/characters/voices/TungTung.wav");
    }

    // Cache so each clip is only loaded from disk once.
    private static Map<String, Clip> clipCache = new HashMap<String, Clip>();

    // The clip that is currently playing (kept so we can stop it).
    private static Clip currentClip = null;

    // Plays the voice clip for the given character name.
    public static void play(String characterName) {
        String path = voicePaths.get(characterName);
        if (path == null) {
            return; // no voice registered for this character
        }

        stopCurrent();

        Clip clip = getClip(characterName, path);
        if (clip == null) {
            return;
        }

        currentClip = clip;
        clip.setFramePosition(0);
        clip.start();
    }

    // Stops the currently playing voice clip.
    public static void stopCurrent() {
        if (currentClip != null && currentClip.isRunning()) {
            currentClip.stop();
        }
        currentClip = null;
    }

    // Returns a cached clip, or loads it from disk if not yet cached.
    private static Clip getClip(String name, String resourcePath) {
        Clip cached = clipCache.get(name);
        if (cached != null && cached.isOpen()) {
            return cached;
        }

        try {
            URL url = CharacterVoicePlayer.class.getResource(resourcePath);
            if (url == null) {
                System.out.println("CharacterVoicePlayer: file not found: " + resourcePath);
                return null;
            }
            AudioInputStream ais = AudioSystem.getAudioInputStream(url);
            Clip clip = AudioSystem.getClip();
            clip.open(ais);
            clipCache.put(name, clip);
            return clip;
        } catch (Exception e) {
            System.out.println("CharacterVoicePlayer: could not load " + resourcePath + " — " + e.getMessage());
            return null;
        }
    }
}
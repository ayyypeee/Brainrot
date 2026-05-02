package Ui;

import javax.sound.sampled.*;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;

/**
 * Plays the character name voice clip (WAV) when a character is LOCKED IN
 * on the selection screen.
 *
 * Place WAV files in:  src/main/resources/characters/voices/
 *   AIP.wav, Ballerina.wav, Cappucino.wav, Christian.wav,
 *   Cyberg.wav, Dianne.wav, Kimmay.wav, Tralalelo.wav, TungTung.wav
 */
public class CharacterVoicePlayer {

    private static final Map<String, String> VOICE_PATHS = new HashMap<>();

    static {
        VOICE_PATHS.put("AIP",       "/characters/voices/AIP.wav");
        VOICE_PATHS.put("Ballerina", "/characters/voices/Ballerina.wav");
        VOICE_PATHS.put("Cappucino", "/characters/voices/Cappucino.wav");
        VOICE_PATHS.put("Christian", "/characters/voices/Christian.wav");
        VOICE_PATHS.put("Cyberg",    "/characters/voices/Cyberg.wav");
        VOICE_PATHS.put("Dianne",    "/characters/voices/Dianne.wav");
        VOICE_PATHS.put("Kimmay",    "/characters/voices/Kimmay.wav");
        VOICE_PATHS.put("Tralalelo", "/characters/voices/Tralalelo.wav");
        VOICE_PATHS.put("Tung Tung", "/characters/voices/TungTung.wav");
    }

    private static final Map<String, Clip> CLIP_CACHE = new HashMap<>();
    private static Clip currentClip = null;

    /** Plays the voice clip for the given character name. Call on lock-in only. */
    public static void play(String characterName) {
        String path = VOICE_PATHS.get(characterName);
        if (path == null) return;

        stopCurrent();

        Clip clip = getClip(characterName, path);
        if (clip == null) return;

        currentClip = clip;
        clip.setFramePosition(0);
        clip.start();
    }

    /** Stops the currently playing clip, if any. */
    public static void stopCurrent() {
        if (currentClip != null && currentClip.isRunning()) {
            currentClip.stop();
        }
        currentClip = null;
    }

    private static Clip getClip(String name, String resourcePath) {
        Clip cached = CLIP_CACHE.get(name);
        if (cached != null && cached.isOpen()) return cached;

        try {
            URL url = CharacterVoicePlayer.class.getResource(resourcePath);
            if (url == null) {
                System.out.println("CharacterVoicePlayer: resource not found: " + resourcePath);
                return null;
            }
            AudioInputStream ais = AudioSystem.getAudioInputStream(url);
            Clip clip = AudioSystem.getClip();
            clip.open(ais);
            CLIP_CACHE.put(name, clip);
            return clip;
        } catch (Exception e) {
            System.out.println("CharacterVoicePlayer: could not load " + resourcePath + " — " + e.getMessage());
            return null;
        }
    }
}
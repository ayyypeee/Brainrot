package Ui;

import javax.sound.sampled.*;
import java.net.URL;

public class MusicPlayer {

    private static final String PATH_MENU        = "/audio/bgMusic2.wav";
    private static final String PATH_CHAR_SELECT = "/audio/Selection.wav";
    private static final String PATH_INGAME      = "/audio/bgMusic_Ingame.wav";

    private static final float CHAR_SELECT_VOLUME = 0.35f;

    private static Clip   clip        = null;
    private static String currentPath = null;

    public static void playMenu()       { play(PATH_MENU,        1.0f); }
    public static void playCharSelect() { play(PATH_CHAR_SELECT, CHAR_SELECT_VOLUME); }
    public static void playIngame()     { play(PATH_INGAME,      1.0f); }

    public static void stop() {
        if (clip != null) {
            if (clip.isRunning()) clip.stop();
            clip.close();
            clip = null;
        }
        currentPath = null;
    }

    /** Adjusts live volume of the currently playing clip. volume = 0.0–1.0 */
    public static void setVolume(float volume) {
        if (clip != null && clip.isOpen()) setVolume(clip, volume);
    }

    private static void play(String resourcePath, float volume) {
        if (clip != null && clip.isRunning() && resourcePath.equals(currentPath)) return;
        stop();
        try {
            URL url = MusicPlayer.class.getResource(resourcePath);
            if (url == null) {
                System.out.println("MusicPlayer: resource not found: " + resourcePath);
                return;
            }
            AudioInputStream ais = AudioSystem.getAudioInputStream(url);
            clip = AudioSystem.getClip();
            clip.open(ais);
            setVolume(clip, volume);
            clip.loop(Clip.LOOP_CONTINUOUSLY);
            clip.start();
            currentPath = resourcePath;
        } catch (Exception e) {
            System.out.println("MusicPlayer: could not load " + resourcePath + " — " + e.getMessage());
        }
    }

    private static void setVolume(Clip c, float volume) {
        if (!c.isControlSupported(FloatControl.Type.MASTER_GAIN)) return;
        FloatControl gain = (FloatControl) c.getControl(FloatControl.Type.MASTER_GAIN);
        float dB = volume <= 0f
                ? gain.getMinimum()
                : 20f * (float) Math.log10(volume);
        dB = Math.max(gain.getMinimum(), Math.min(gain.getMaximum(), dB));
        gain.setValue(dB);
    }
}
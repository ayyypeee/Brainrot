    package Ui;
    
    import javax.sound.sampled.AudioInputStream;
    import javax.sound.sampled.AudioSystem;
    import javax.sound.sampled.Clip;
    import javax.sound.sampled.FloatControl;
    import java.net.URL;
    
    // Plays and manages background music (one track at a time, loops continuously).
    public class MusicPlayer {
    
        private static final String PATH_MENU        = "/audio/OPENING.wav";
        private static final String PATH_CHAR_SELECT = "/audio/Selection.wav";
        private static final String PATH_INGAME      = "/audio/bgMusic_Ingame.wav";
    
        private static final float CHAR_SELECT_VOLUME = 0.02f; // quieter on the selection screen
    
        private static Clip   clip        = null;  // the currently loaded audio clip
        private static String currentPath = null;  // path of the track currently playing

        // Convenience methods for each game screen's music.
        public static void playMenu()       { play(PATH_MENU,        0.02f); }
        public static void playCharSelect() { play(PATH_CHAR_SELECT, CHAR_SELECT_VOLUME); }
        public static void playIngame()     { play(PATH_INGAME,      0.05f); }
    
        // Stops and closes the current clip immediately.
        public static void stop() {
            if (clip != null) {
                if (clip.isRunning()) {
                    clip.stop();
                }
                clip.close();
                clip = null;
            }
            currentPath = null;
        }
    
        // Changes the volume of the currently playing clip (0.0 = silent, 1.0 = full).
        public static void setVolume(float volume) {
            if (clip != null && clip.isOpen()) {
                applyVolume(clip, volume);
            }
        }
    
        // Loads and loops a new music track (skips loading if it is already playing).
        private static void play(String resourcePath, float volume) {
            if (clip != null && clip.isRunning() && resourcePath.equals(currentPath)) {
                return; // already playing this track, do nothing
            }
            stop();
    
            try {
                URL url = MusicPlayer.class.getResource(resourcePath);
                if (url == null) {
                    System.out.println("MusicPlayer: file not found: " + resourcePath);
                    return;
                }
                AudioInputStream ais = AudioSystem.getAudioInputStream(url);
                clip = AudioSystem.getClip();
                clip.open(ais);
                applyVolume(clip, volume);
                clip.loop(Clip.LOOP_CONTINUOUSLY);
                clip.start();
                currentPath = resourcePath;
            } catch (Exception e) {
                System.out.println("MusicPlayer: could not load " + resourcePath + " — " + e.getMessage());
            }
        }
    
        // Converts a 0–1 float volume to decibels and applies it to the clip.
        private static void applyVolume(Clip c, float volume) {
            if (!c.isControlSupported(FloatControl.Type.MASTER_GAIN)) {
                return;
            }
            FloatControl gain = (FloatControl) c.getControl(FloatControl.Type.MASTER_GAIN);
            float dB;
            if (volume <= 0f) {
                dB = gain.getMinimum();
            } else {
                dB = 20f * (float) Math.log10(volume);
            }
            if (dB < gain.getMinimum()) { dB = gain.getMinimum(); }
            if (dB > gain.getMaximum()) { dB = gain.getMaximum(); }
            gain.setValue(dB);
        }
    }
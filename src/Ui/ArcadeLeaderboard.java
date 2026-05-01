package Ui;

import java.io.*;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

public class ArcadeLeaderboard {

    private static final String FILE = "arcade_leaderboard.txt";

    public static class Entry {
        public final String playerName;
        public final String characterName;
        public final long   seconds;

        public Entry(String playerName, String characterName, long seconds) {
            this.playerName    = playerName;
            this.characterName = characterName;
            this.seconds       = seconds;
        }
    }

    public static void addEntry(String playerName, String characterName, long seconds) {
        try (BufferedWriter w = new BufferedWriter(new FileWriter(FILE, true))) {
            w.write(playerName + "," + characterName + "," + seconds);
            w.newLine();
        } catch (IOException e) { e.printStackTrace(); }
    }

    public static List<Entry> getEntries() {
        List<Entry> list = new ArrayList<>();
        try (BufferedReader r = new BufferedReader(new FileReader(FILE))) {
            String line;
            while ((line = r.readLine()) != null) {
                String[] parts = line.split(",", 3);
                if (parts.length == 3) {
                    try {
                        list.add(new Entry(parts[0], parts[1], Long.parseLong(parts[2])));
                    } catch (NumberFormatException ignored) {}
                }
            }
        } catch (IOException ignored) {}
        // Sort fastest first
        list.sort(Comparator.comparingLong(e -> e.seconds));
        return list;
    }
}
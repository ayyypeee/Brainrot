package Ui;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;


// Scores are sorted fastest-first so the best runs appear at the top.
public class ArcadeLeaderboard {

    private static final String FILE = "arcade_leaderboard.txt";

    // One leaderboard entry: who played, which character, and how fast they finished.
    public static class Entry {
        public String playerName;
        public String characterName;
        public long   seconds;        // total time taken in seconds

        public Entry(String playerName, String characterName, long seconds) {
            this.playerName    = playerName;
            this.characterName = characterName;
            this.seconds       = seconds;
        }
    }


    public static void addEntry(String playerName, String characterName, long seconds) {
        try {
            BufferedWriter writer = new BufferedWriter(new FileWriter(FILE, true));
            writer.write(playerName + "," + characterName + "," + seconds);
            writer.newLine();
            writer.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }


    public static List<Entry> getEntries() {
        List<Entry> list = new ArrayList<Entry>();

        try {
            BufferedReader reader = new BufferedReader(new FileReader(FILE));
            String line = reader.readLine();

            while (line != null) {
                String[] parts = line.split(",", 3);
                if (parts.length == 3) {
                    try {
                        long secs = Long.parseLong(parts[2]);
                        list.add(new Entry(parts[0], parts[1], secs));
                    } catch (NumberFormatException e) {
                        // Skip malformed lines.
                    }
                }
                line = reader.readLine();
            }
            reader.close();
        } catch (IOException e) {

        }

        // Sort ascending by time so the fastest run is first.
        list.sort(new Comparator<Entry>() {
            public int compare(Entry a, Entry b) {
                return Long.compare(a.seconds, b.seconds);
            }
        });

        return list;
    }
}
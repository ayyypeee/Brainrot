package Ui;

import java.io.*;

public class FileHandler {

    private static final String FILE_NAME = "users.txt";

    public static boolean registerUser(String username, String password) {
        if (userExists(username)) return false;
        try (BufferedWriter w = new BufferedWriter(new FileWriter(FILE_NAME, true))) {
            w.write(username + "," + password);
            w.newLine();
            return true;
        } catch (IOException e) {
            e.printStackTrace();
            return false;
        }
    }

    public static boolean loginUser(String username, String password) {
        try (BufferedReader r = new BufferedReader(new FileReader(FILE_NAME))) {
            String line;
            while ((line = r.readLine()) != null) {
                String[] parts = line.split(",", 2);
                if (parts.length == 2
                        && parts[0].equals(username)
                        && parts[1].equals(password)) return true;
            }
        } catch (IOException ignored) {}
        return false;
    }

    private static boolean userExists(String username) {
        try (BufferedReader r = new BufferedReader(new FileReader(FILE_NAME))) {
            String line;
            while ((line = r.readLine()) != null) {
                String[] parts = line.split(",", 2);
                if (parts.length >= 1 && parts[0].equals(username)) return true;
            }
        } catch (IOException ignored) {}
        return false;
    }
}
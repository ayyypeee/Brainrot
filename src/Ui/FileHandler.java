package Ui;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;

public class FileHandler {

    // The file that stores registered accounts.
    private static final String FILE_NAME = "users.txt";

    // CREATE — saves a new user if the username is not already taken.
    public static boolean registerUser(String username, String password) {
        if (userExists(username)) {
            return false; // username already taken
        }
        try {
            BufferedWriter writer = new BufferedWriter(new FileWriter(FILE_NAME, true));
            writer.write(username + "," + password);
            writer.newLine();
            writer.close();
            return true;
        } catch (IOException e) {
            e.printStackTrace();
            return false;
        }
    }

    // READ — checks the file for a matching username and password.
    public static boolean loginUser(String username, String password) {
        try {
            BufferedReader reader = new BufferedReader(new FileReader(FILE_NAME));
            String line = reader.readLine();

            while (line != null) {
                String[] parts = line.split(",", 2);
                if (parts.length == 2) {
                    if (parts[0].equals(username) && parts[1].equals(password)) {
                        reader.close();
                        return true;
                    }
                }
                line = reader.readLine();
            }
            reader.close();
        } catch (IOException e) {

        }
        return false;
    }

    // READ — returns true if a username already exists in the file.
    private static boolean userExists(String username) {
        try {
            BufferedReader reader = new BufferedReader(new FileReader(FILE_NAME));
            String line = reader.readLine();

            while (line != null) {
                String[] parts = line.split(",", 2);
                if (parts.length >= 1 && parts[0].equals(username)) {
                    reader.close();
                    return true;
                }
                line = reader.readLine();
            }
            reader.close();
        } catch (IOException e) {
            // File does not exist yet.
        }
        return false;
    }
}
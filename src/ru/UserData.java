package ru;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;

public class UserData {

    private static final String FILE_PATH = "C:\\Astral Client\\assets\\skins\\us.data";
    public static int getUid() {
        return Integer.parseInt(getDataFromFile("uid:"));
    }
    public static String getLogin() {
        return getDataFromFile("login:");
    }
    public static String getSub() {
        return getDataFromFile("expired:");
    }
    private static String getDataFromFile(String key) {
        File file = new File(FILE_PATH);
        String value = "";
        try (BufferedReader reader = new BufferedReader(new FileReader(file))) {
            String line;
            while ((line = reader.readLine()) != null) {
                if (line.startsWith(key)) {
                    value = line.split(":")[1].trim();
                    break;
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return value;
    }
}

package ru;

import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.Date;

public class InfinityGuardChecker {
    private static final String pros = "https://atral-client-default-rtdb.firebaseio.com/truesror.json";

    public static boolean hasAccess(String nickname) {
        try {
            URL url = new URL(pros);
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            connection.setRequestMethod("GET");
            BufferedReader reader = new BufferedReader(new InputStreamReader(connection.getInputStream()));
            StringBuilder response = new StringBuilder();
            String line;
            while ((line = reader.readLine()) != null) {
                response.append(line);
            }
            reader.close();

            JSONObject jsonResponse = new JSONObject(response.toString());
            for (String key : jsonResponse.keySet()) {
                JSONObject item = jsonResponse.getJSONObject(key);
                if (item.has("message") && item.getString("message").equals(nickname)) {
                    if (isExpired(item.getString("timestamp"))) {
                        deleteObject(key);
                        return false; 
                    }
                    return true; 
                }
            }
            return false; 
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    private static boolean isExpired(String timestamp) {
        try {
            SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm");
            Date storedDate = dateFormat.parse(timestamp);
            long diff = new Date().getTime() - storedDate.getTime();
            return diff > 5 * 60 * 1000; 
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    private static void deleteObject(String objectKey) {
        try {
            URL deleteUrl = new URL(pros.replace(".json", "/" + objectKey + ".json"));
            HttpURLConnection connection = (HttpURLConnection) deleteUrl.openConnection();
            connection.setRequestMethod("DELETE");
            connection.getResponseCode(); 
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}

package com.example.timetracker;

import android.util.Log;

import org.json.JSONObject;

import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;

public class WorkTrackingService {
    private static final String BASE_URL = "http://10.0.2.2:5000"; // Emulator: Use "10.0.2.2" instead of "127.0.0.1"

    public static boolean startWork(int userId) {
        try {
            URL url = new URL(BASE_URL + "/start_work");
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("POST");
            conn.setRequestProperty("Content-Type", "application/json");
            conn.setDoOutput(true);

            JSONObject json = new JSONObject();
            json.put("UserId", userId);

            try (OutputStream os = conn.getOutputStream()) {
                os.write(json.toString().getBytes(StandardCharsets.UTF_8));
            }

            return conn.getResponseCode() == 201;
        } catch (Exception e) {
            Log.e("WorkTrackingService", "Error starting work", e);
            return false;
        }
    }

    public static boolean endWork(int userId, String description, long endTime) {
        try {
            URL url = new URL(BASE_URL + "/end_work");
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("PUT");
            conn.setRequestProperty("Content-Type", "application/json");
            conn.setDoOutput(true);

            JSONObject json = new JSONObject();
            json.put("UserId", userId);
            json.put("Description", description);
            json.put("EndTime", endTime);

            String jsonString = json.toString();
            Log.d("WorkTrackingService", "Sending to /end_work: " + jsonString); // Log the JSON

            try (OutputStream os = conn.getOutputStream()) {
                os.write(jsonString.getBytes(StandardCharsets.UTF_8));
            }

            int responseCode = conn.getResponseCode();
            Log.d("WorkTrackingService", "/end_work response code: " + responseCode); // Log the response code
            return responseCode == 200;

        } catch (Exception e) {
            Log.e("WorkTrackingService", "Error ending work", e);
            return false;
        }
    }
}
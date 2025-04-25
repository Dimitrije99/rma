package com.example.timetracker;

import okhttp3.*;

import org.json.JSONException;
import org.json.JSONObject;
import java.io.IOException;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Scanner;

public class LoginService {
    private static final String BASE_URL = "http://10.0.2.2:5000/login";

    public static Integer loginUser(String username, String password) {
        try {
            URL url = new URL(BASE_URL);
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("POST");
            conn.setRequestProperty("Content-Type", "application/json");
            conn.setDoOutput(true);

            // Create JSON request body
            JSONObject json = new JSONObject();
            json.put("Username", username);
            json.put("Password", password);

            OutputStream os = conn.getOutputStream();
            os.write(json.toString().getBytes());
            os.flush();
            os.close();

            // Read response
            int responseCode = conn.getResponseCode();
            if (responseCode == 200) { // Success
                Scanner scanner = new Scanner(conn.getInputStream());
                String response = scanner.useDelimiter("\\A").next();
                scanner.close();

                // Parse response JSON
                JSONObject jsonResponse = new JSONObject(response);
                return jsonResponse.getInt("UserId"); // Extract userId
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null; // Return null if login fails
    }

    public static boolean registerUser(String username, String password) {
        try {
            URL url = new URL("http://10.0.2.2:5000/register");  // Make sure the URL is correct
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("POST");
            conn.setRequestProperty("Content-Type", "application/json; utf-8");
            conn.setDoOutput(true);

            JSONObject json = new JSONObject();
            json.put("username", username);
            json.put("password", password);

            try (OutputStream os = conn.getOutputStream()) {
                byte[] input = json.toString().getBytes("utf-8");
                os.write(input, 0, input.length);
            }

            int responseCode = conn.getResponseCode();
            return responseCode == HttpURLConnection.HTTP_OK;  // 200 means success

        } catch (IOException | JSONException e) {
            e.printStackTrace();
            return false;
        }
    }

}

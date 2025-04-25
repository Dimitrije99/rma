package com.example.timetracker;

import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Scanner;

public class WorkHistoryService {
    public static String getWorkHistory(int userId) {
        try {
            URL url = new URL("http://10.0.2.2:5000/work_history?UserId=" + userId);
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("GET");

            Scanner scanner = new Scanner(conn.getInputStream());
            String response = scanner.useDelimiter("\\A").next();
            scanner.close();

            return response;
        } catch (Exception e) {
            return "[]";
        }
    }
}

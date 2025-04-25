package com.example.timetracker;

public class WorkHistoryItem {
    private String date;
    private String startTime;
    private String endTime;
    private String description;

    public WorkHistoryItem(String date, String startTime, String endTime, String description) {
        this.date = date;
        this.startTime = startTime;
        this.endTime = endTime;
        this.description = description;
    }

    public String getDate() {
        return date;
    }

    public String getStartTime() {
        return startTime;
    }

    public String getEndTime() {
        return endTime;
    }

    public String getDescription() {
        return description;
    }
}
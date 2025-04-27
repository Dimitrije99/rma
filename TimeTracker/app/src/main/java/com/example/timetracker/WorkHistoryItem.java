package com.example.timetracker;

public class WorkHistoryItem {

    private int id;
    private String date;
    private String startTime;
    private String endTime;
    private String description;

    public WorkHistoryItem(int id,String date, String startTime, String endTime, String description) {
        this.id = id;
        this.date = date;
        this.startTime = startTime;
        this.endTime = endTime;
        this.description = description;
    }

    public int getId() {
        return id;
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

    public void setDescription(String description) {
        this.description = description;
    }
}
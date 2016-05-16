package com.law.blueinnofora;


public class ScheduleListItem {
    private String time;
    private String message;

    public ScheduleListItem() {

    }

    public ScheduleListItem(String inTime, String inMessage) {
        time = inTime;
        message = inMessage;
    }

    public String getTime() {
        return time;
    }

    public void setTime(String time) {
        this.time = time;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

}
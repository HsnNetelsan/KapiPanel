package com.netelsan.ipinterkompanel.model;

import java.io.Serializable;

public class DateTimeObject implements Serializable {

    private static final long serialVersionUID = 4L;

    public String time;
    public String date;

    public String getTime() {
        return time;
    }

    public void setTime(String time) {
        this.time = time;
    }

    public String getDate() {
        return date;
    }

    public void setDate(String date) {
        this.date = date;
    }
}
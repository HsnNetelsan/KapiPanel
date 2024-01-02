package com.netelsan.ipinterkompanel.model;

import java.io.Serializable;

public class DateTimeClass  implements Serializable {

    private static final long serialVersionUID = 4L;



    public int id;
    public int hour;
    public int minute;
    public int dayofMonth;
    public int month;
    public int year;



    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public int getHour() {
        return hour;
    }

    public void setHour(int hour) {
        this.hour = hour;
    }


    public int getMinute() {
        return minute;
    }

    public void setMinute(int minute) {
        this.minute = minute;
    }


    public int getDayofMonth() {
        return dayofMonth;
    }

    public void setDayofMonth(int dayofMonth) {
        this.dayofMonth = dayofMonth;
    }


    public int getMonth() {
        return month;
    }

    public void setMonth(int month) {
        this.month = month;
    }


    public int getYear() {
        return year;
    }

    public void setYear(int year) {
        this.year = year;
    }







}

package com.netelsan.ipinterkompanel.model;

import java.io.Serializable;


public class DaireTalep implements Serializable {

    private static final long serialVersionUID = 4L;

    public int id;

    public String time;
    public String date;

    public String ownerIP;

    public int type;
    public int deviceType;
    public int adet;

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

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

    public String getOwnerIP() {
        return ownerIP;
    }

    public void setOwnerIP(String ownerIP) {
        this.ownerIP = ownerIP;
    }

    public int getType() {
        return type;
    }

    public void setType(int type) {
        this.type = type;
    }

    public int getDeviceType() {
        return deviceType;
    }

    public void setDeviceType(int deviceType) {
        this.deviceType = deviceType;
    }

    public int getAdet() {
        return adet;
    }

    public void setAdet(int adet) {
        this.adet = adet;
    }
}
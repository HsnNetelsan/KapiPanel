package com.netelsan.ipinterkompanel.model;

import java.io.Serializable;


public class Guvenlik implements Serializable {

    private static final long serialVersionUID = 4L;

    public int id;

    public String deviceName;
    public String ip;
    public int guvenlikNo;

    boolean isSelected = false;

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getDeviceName() {
        return deviceName;
    }

    public void setDeviceName(String deviceName) {
        this.deviceName = deviceName;
    }

    public String getIp() {
        return ip;
    }

    public void setIp(String ip) {
        this.ip = ip;
    }

    public int getGuvenlikNo() {
        return guvenlikNo;
    }

    public void setGuvenlikNo(int guvenlikNo) {
        this.guvenlikNo = guvenlikNo;
    }

    public boolean isSelected() {
        return isSelected;
    }

    public void setSelected(boolean selected) {
        isSelected = selected;
    }
}
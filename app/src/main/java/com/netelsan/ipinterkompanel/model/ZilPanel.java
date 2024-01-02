package com.netelsan.ipinterkompanel.model;

import java.io.Serializable;


public class ZilPanel implements Serializable {

    private static final long serialVersionUID = 4L;

    public int id;

    public String ip;
    public String deviceName;

    public int blok;
    public int kapiNo;

    public String getIp() {
        return ip;
    }

    public void setIp(String ip) {
        this.ip = ip;
    }

    public String getDeviceName() {
        return deviceName;
    }

    public void setDeviceName(String deviceName) {
        this.deviceName = deviceName;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public int getBlok() {
        return blok;
    }

    public void setBlok(int blok) {
        this.blok = blok;
    }

    public int getKapiNo() {
        return kapiNo;
    }

    public void setKapiNo(int kapiNo) {
        this.kapiNo = kapiNo;
    }

}
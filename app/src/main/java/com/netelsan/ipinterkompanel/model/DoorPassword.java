package com.netelsan.ipinterkompanel.model;

import java.io.Serializable;

public class DoorPassword implements Serializable {

    private static final long serialVersionUID = 4L;

    public int id;

    public String ip;
    public String door;
    public String rfid;

    boolean isActive;

    boolean isSelected = false;

    String passwordLabel = "";

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getIp() {
        return ip;
    }

    public void setIp(String ip) {
        this.ip = ip;
    }

    public String getDoor() {
        return door;
    }

    public void setDoor(String door) {
        this.door = door;
    }

    public String getRfid() {
        return rfid;
    }

    public void setRfid(String rfid) {
        this.rfid = rfid;
    }

    public boolean isActive() {
        return isActive;
    }

    public void setActive(boolean active) {
        isActive = active;
    }

    public boolean isSelected() {
        return isSelected;
    }

    public void setSelected(boolean selected) {
        isSelected = selected;
    }

    public String getPasswordLabel() {
        return passwordLabel;
    }

    public void setPasswordLabel(String passwordLabel) {
        this.passwordLabel = passwordLabel;
    }

}

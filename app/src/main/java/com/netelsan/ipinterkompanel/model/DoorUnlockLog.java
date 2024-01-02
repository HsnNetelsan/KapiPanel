package com.netelsan.ipinterkompanel.model;

import java.io.Serializable;

public class DoorUnlockLog implements Serializable {

    private static final long serialVersionUID = 4L;

    int id;

    String rfid;
    String doorPassword;

    String passwordOwnerIP;
    String passwordLabel;

    String datetime;

    int unlockType;

    boolean isSelected;

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public int getUnlockType() {
        return unlockType;
    }

    public void setUnlockType(int unlockType) {
        this.unlockType = unlockType;
    }

    public String getRfid() {
        return rfid;
    }

    public void setRfid(String rfid) {
        this.rfid = rfid;
    }

    public String getDoorPassword() {
        return doorPassword;
    }

    public void setDoorPassword(String doorPassword) {
        this.doorPassword = doorPassword;
    }

    public String getDatetime() {
        return datetime;
    }

    public void setDatetime(String datetime) {
        this.datetime = datetime;
    }

    public String getPasswordOwnerIP() {
        return passwordOwnerIP;
    }

    public void setPasswordOwnerIP(String passwordOwnerIP) {
        this.passwordOwnerIP = passwordOwnerIP;
    }

    public String getPasswordLabel() {
        return passwordLabel;
    }

    public void setPasswordLabel(String passwordLabel) {
        this.passwordLabel = passwordLabel;
    }

    public boolean isSelected() {
        return isSelected;
    }

    public void setSelected(boolean selected) {
        isSelected = selected;
    }
}

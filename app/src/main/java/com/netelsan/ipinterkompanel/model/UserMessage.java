package com.netelsan.ipinterkompanel.model;

import java.io.Serializable;

public class UserMessage implements Serializable {

    private static final long serialVersionUID = 4L;

    public UserMessage(String uniqueID) {
        this.uniqueID = uniqueID;
    }

    int id;

    String text;
    String imagePath;
    String datetime;

    int senderId;
    int senderType;

    boolean isRead;

    String uniqueID;
    boolean isReadByReceiver;

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
    }

    public String getImagePath() {
        return imagePath;
    }

    public void setImagePath(String imagePath) {
        this.imagePath = imagePath;
    }

    public String getDatetime() {
        return datetime;
    }

    public void setDatetime(String datetime) {
        this.datetime = datetime;
    }

    public int getSenderId() {
        return senderId;
    }

    public void setSenderId(int senderId) {
        this.senderId = senderId;
    }

    public int getSenderType() {
        return senderType;
    }

    public void setSenderType(int senderType) {
        this.senderType = senderType;
    }

    public boolean isRead() {
        return isRead;
    }

    public void setRead(boolean read) {
        isRead = read;
    }

    public String getUniqueID() {
        return uniqueID;
    }

    public void setUniqueID(String uniqueID) {
        this.uniqueID = uniqueID;
    }

    public boolean isReadByReceiver() {
        return isReadByReceiver;
    }

    public void setReadByReceiver(boolean readByReceiver) {
        isReadByReceiver = readByReceiver;
    }

}

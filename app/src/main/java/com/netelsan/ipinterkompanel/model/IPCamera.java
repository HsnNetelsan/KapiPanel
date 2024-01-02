package com.netelsan.ipinterkompanel.model;

import java.io.Serializable;


public class IPCamera implements Serializable {

    private static final long serialVersionUID = 4L;

    public int id;

    public String name;
    public String ip;
    int port;

    String streamSuffix;

    String workForIP;// özel bir cihaz için eklendiyse onunla görüşürken izletme için. ÖRN güvenlik için eklenen kamera

    String lastSSFilePath;

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getIp() {
        return ip;
    }

    public void setIp(String ip) {
        this.ip = ip;
    }

    public int getPort() {
        return port;
    }

    public void setPort(int port) {
        this.port = port;
    }

    public String getStreamSuffix() {
        return streamSuffix;
    }

    public void setStreamSuffix(String streamSuffix) {
        this.streamSuffix = streamSuffix;
    }

    public String getWorkForIP() {
        return workForIP;
    }

    public void setWorkForIP(String workForIP) {
        this.workForIP = workForIP;
    }

    public String getLastSSFilePath() {
        return lastSSFilePath;
    }

    public void setLastSSFilePath(String lastSSFilePath) {
        this.lastSSFilePath = lastSSFilePath;
    }
}
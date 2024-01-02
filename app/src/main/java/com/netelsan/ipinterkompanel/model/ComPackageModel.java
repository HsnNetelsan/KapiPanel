package com.netelsan.ipinterkompanel.model;

import java.io.Serializable;
import java.util.ArrayList;

public class ComPackageModel implements Serializable {

    private static final long serialVersionUID = 4L;

    int ope_type;

    boolean isNeedResponse;

    ZilPanel zilPanel;
    ZilPanelSite zilPanelSite;
    Daire daire;
    Guvenlik guvenlik;
    IPCamera ipCamera;

    ArrayList<ZilPanel> zilPanels = new ArrayList<>();
    ArrayList<ZilPanelSite> zilPanelSites = new ArrayList<>();
    ArrayList<Daire> daires = new ArrayList<>();
    ArrayList<Guvenlik> guvenliks = new ArrayList<>();
    ArrayList<IPCamera> ipCameras = new ArrayList<>();

    UserMessage userMessage = null;
    DoorPassword doorPassword = null;

    ArrayList<CallSnapshotObject> callSnapshotObjects;
    ArrayList<String> callSnapshotImageIds;

    ArrayList<DoorPassword> doorPasswords = new ArrayList<>();

    DaireTalep daireTalep;

    String dataString = "";
    boolean dataBoolean;
    long dataLong;
    int dataInt;


    String serverControlHour;
    String serverControlMinute;

    public int getOpe_type() {
        return ope_type;
    }

    public void setOpe_type(int ope_type) {
        this.ope_type = ope_type;
    }

    public String getServerControlHour() {
        return serverControlHour;
    }

    public void setServerControlHour(String serverControlHour) {
        this.serverControlHour = serverControlHour;
    }
    public String getServerControlMinute() {
        return serverControlMinute;
    }

    public void setServerControlMinute(String serverControlMinute) {
        this.serverControlMinute = serverControlMinute;
    }

    public boolean isNeedResponse() {
        return isNeedResponse;
    }

    public void setNeedResponse(boolean needResponse) {
        isNeedResponse = needResponse;
    }

    public boolean getDataBoolean() {
        return dataBoolean;
    }

    public void setDataBoolean(boolean dataBoolean) {
        this.dataBoolean = dataBoolean;
    }

    public int getDataInt() {
        return dataInt;
    }

    public void setDataInt(int dataInt) {
        this.dataInt = dataInt;
    }

    public ZilPanel getZilPanel() {
        return zilPanel;
    }

    public void setZilPanel(ZilPanel zilPanel) {
        this.zilPanel = zilPanel;
    }

    public ZilPanelSite getZilPanelSite() {
        return zilPanelSite;
    }

    public void setZilPanelSite(ZilPanelSite zilPanelSite) {
        this.zilPanelSite = zilPanelSite;
    }

    public Daire getDaire() {
        return daire;
    }

    public void setDaire(Daire daire) {
        this.daire = daire;
    }

    public Guvenlik getGuvenlik() {
        return guvenlik;
    }

    public void setGuvenlik(Guvenlik guvenlik) {
        this.guvenlik = guvenlik;
    }

    public IPCamera getIpCamera() {
        return ipCamera;
    }

    public void setIpCamera(IPCamera ipCamera) {
        this.ipCamera = ipCamera;
    }

    public long getDataLong() {
        return dataLong;
    }

    public void setDataLong(long dataLong) {
        this.dataLong = dataLong;
    }

    public ArrayList<ZilPanel> getZilPanels() {
        return zilPanels;
    }

    public void setZilPanels(ArrayList<ZilPanel> zilPanels) {
        this.zilPanels = zilPanels;
    }

    public ArrayList<ZilPanelSite> getZilPanelSites() {
        return zilPanelSites;
    }

    public void setZilPanelSites(ArrayList<ZilPanelSite> zilPanelSites) {
        this.zilPanelSites = zilPanelSites;
    }

    public ArrayList<Daire> getDaires() {
        return daires;
    }

    public void setDaires(ArrayList<Daire> daires) {
        this.daires = daires;
    }

    public ArrayList<Guvenlik> getGuvenliks() {
        return guvenliks;
    }

    public void setGuvenliks(ArrayList<Guvenlik> guvenliks) {
        this.guvenliks = guvenliks;
    }

    public ArrayList<IPCamera> getIpCameras() {
        return ipCameras;
    }

    public void setIpCameras(ArrayList<IPCamera> ipCameras) {
        this.ipCameras = ipCameras;
    }

    public UserMessage getUserMessage() {
        return userMessage;
    }

    public void setUserMessage(UserMessage userMessage) {
        this.userMessage = userMessage;
    }

    public DoorPassword getDoorPassword() {
        return doorPassword;
    }

    public void setDoorPassword(DoorPassword doorPassword) {
        this.doorPassword = doorPassword;
    }

    public DaireTalep getDaireTalep() {
        return daireTalep;
    }

    public void setDaireTalep(DaireTalep daireTalep) {
        this.daireTalep = daireTalep;
    }

    public String getDataString() {
        return dataString;
    }

    public void setDataString(String dataString) {
        this.dataString = dataString;
    }

    public ArrayList<CallSnapshotObject> getCallSnapshotObjects() {
        return callSnapshotObjects;
    }

    public void setCallSnapshotObjects(ArrayList<CallSnapshotObject> callSnapshotObjects) {
        this.callSnapshotObjects = callSnapshotObjects;
    }

    public ArrayList<String> getCallSnapshotImageIds() {
        return callSnapshotImageIds;
    }

    public void setCallSnapshotImageIds(ArrayList<String> callSnapshotImageIds) {
        this.callSnapshotImageIds = callSnapshotImageIds;
    }

    public ArrayList<DoorPassword> getDoorPasswords() {
        return doorPasswords;
    }

    public void setDoorPasswords(ArrayList<DoorPassword> doorPasswords) {
        this.doorPasswords = doorPasswords;
    }

}

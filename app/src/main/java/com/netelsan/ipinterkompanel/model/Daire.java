package com.netelsan.ipinterkompanel.model;

import java.io.Serializable;
import java.util.ArrayList;


public class Daire implements Serializable {

    private static final long serialVersionUID = 4L;

    public int id;

    public String isim;
    public String soyisim;
    public String ip;
    public int blok;
    public int daireNo;
    public String katNo;

    boolean isSelected = false;

    boolean isGorevli = false;
    ArrayList<String> gorevliOlduguBinalar = new ArrayList<>();

    public String getIsim() {
        return isim;
    }

    public void setIsim(String isim) {
        this.isim = isim;
    }

    public String getSoyisim() {
        return soyisim;
    }

    public void setSoyisim(String soyisim) {
        this.soyisim = soyisim;
    }

    public String getIp() {
        return ip;
    }

    public void setIp(String ip) {
        this.ip = ip;
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

    public int getDaireNo() {
        return daireNo;
    }

    public void setDaireNo(int daireNo) {
        this.daireNo = daireNo;
    }

    public String getKatNo() {
        return katNo;
    }

    public void setKatNo(String katNo) {
        this.katNo = katNo;
    }

    public boolean isSelected() {
        return isSelected;
    }

    public void setSelected(boolean selected) {
        isSelected = selected;
    }

    public boolean isGorevli() {
        return isGorevli;
    }

    public void setGorevli(boolean gorevli) {
        isGorevli = gorevli;
    }

    public ArrayList<String> getGorevliOlduguBinalar() {
        return gorevliOlduguBinalar;
    }

    public void setGorevliOlduguBinalar(ArrayList<String> gorevliOlduguBinalar) {
        this.gorevliOlduguBinalar = gorevliOlduguBinalar;
    }
}
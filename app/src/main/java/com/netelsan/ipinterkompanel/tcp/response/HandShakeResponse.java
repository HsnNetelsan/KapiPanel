package com.netelsan.ipinterkompanel.tcp.response;

import com.netelsan.ipinterkompanel.model.Daire;
import com.netelsan.ipinterkompanel.model.ZilPanel;

import java.io.Serializable;

public class HandShakeResponse implements Serializable {

    ZilPanel zilPanel = null;
    Daire daire = null;

    long timeMillis;

    public ZilPanel getZilPanel() {
        return zilPanel;
    }

    public void setZilPanel(ZilPanel zilPanel) {
        this.zilPanel = zilPanel;
    }

    public Daire getDaire() {
        return daire;
    }

    public void setDaire(Daire daire) {
        this.daire = daire;
    }

    public long getTimeMillis() {
        return timeMillis;
    }

    public void setTimeMillis(long timeMillis) {
        this.timeMillis = timeMillis;
    }
}

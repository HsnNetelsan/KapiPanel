package com.netelsan.ipinterkompanel.tcp.request;

import com.netelsan.ipinterkompanel.model.Daire;
import com.netelsan.ipinterkompanel.model.ZilPanel;

import java.io.Serializable;

public class HandShakeRequest implements Serializable {

    Daire daire;
    ZilPanel zilPanel;

    public Daire getDaire() {
        return daire;
    }

    public void setDaire(Daire daire) {
        this.daire = daire;
    }

    public ZilPanel getZilPanel() {
        return zilPanel;
    }

    public void setZilPanel(ZilPanel zilPanel) {
        this.zilPanel = zilPanel;
    }
}

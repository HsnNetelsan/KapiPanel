package com.netelsan.ipinterkompanel.tcp.request;

import com.netelsan.ipinterkompanel.model.Daire;

import java.io.Serializable;

public class DateTimeRequest implements Serializable {

    Daire daire;

    public Daire getDaire() {
        return daire;
    }

    public void setDaire(Daire daire) {
        this.daire = daire;
    }
}

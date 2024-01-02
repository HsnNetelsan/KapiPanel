package com.netelsan.ipinterkompanel.listener;

public interface KeyPadListener {

    void onKeyPressed(String keyCode);

    void onRFIDDetected(String rfid);

}

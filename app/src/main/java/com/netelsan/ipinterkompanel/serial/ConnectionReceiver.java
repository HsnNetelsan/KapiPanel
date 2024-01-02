package com.netelsan.ipinterkompanel.serial;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

import com.netelsan.ipinterkompanel.Constants;

public class ConnectionReceiver extends BroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent) {

        Log.d(Constants.LOG_TAG, "" + intent.getAction());

        if(intent.getAction().equals("com.netelsan.ip.USER_HANG_UP")) {
            Application.commandClient.SendVideoCallEnd(Application.remoteDevice.address);
        } else if(intent.getAction().equals("com.netelsan.ip.DOOR_UNLOCK")) {
            Application.commandClient.SendDoorUnlock(Application.remoteDevice.address);
        }

    }

}

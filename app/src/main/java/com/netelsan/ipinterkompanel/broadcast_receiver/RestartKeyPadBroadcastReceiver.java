package com.netelsan.ipinterkompanel.broadcast_receiver;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import com.netelsan.ipinterkompanel.service.KeyPadService;
import com.netelsan.ipinterkompanel.tcp.Server;

public class RestartKeyPadBroadcastReceiver extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {

        context.startService(new Intent(context, KeyPadService.class));
    }

}

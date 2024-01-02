package com.netelsan.ipinterkompanel.tcp;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

import com.netelsan.ipinterkompanel.Constants;
import com.netelsan.ipinterkompanel.Helper;
import com.netelsan.ipinterkompanel.activity.SplashSelectorActivity;

public class RestartBroadcastReceiver extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {

        Log.i(Constants.LOG_TAG, "RestartBroadcastReceiver onReceive");

        Server server = new Server(context);

        if(! Helper.isMyServiceRunning(context, server.getClass())) {
            Intent serverIntent = new Intent(context, server.getClass());
            context.startService(serverIntent);
        }

//        context.startService(new Intent(context, Server.class));
    }

}

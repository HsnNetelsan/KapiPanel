package com.netelsan.ipinterkompanel.broadcast_receiver;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

import com.netelsan.ipinterkompanel.Constants;
import com.netelsan.ipinterkompanel.Helper;
import com.netelsan.ipinterkompanel.service.SystemControlService;

public class PeriodicAlarmBroadcastReceiver extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {

        Log.d(Constants.LOG_TAG, "PeriodicAlarmBroadcastReceiver onReceive=");
        long nextAlarmTimeMillis = Helper.getNextAlarmTimeMillis();
        Helper.setNextAlarm(context, nextAlarmTimeMillis);

        // burada sıradaki alarm ı kurmalısın ve gerekli kontrollerini bir servis ile yapabilirsin

        context.startService(new Intent(context, SystemControlService.class));

    }

}

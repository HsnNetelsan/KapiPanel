package com.netelsan.ipinterkompanel.activity.guvenlik_sistemi;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.view.View;

import androidx.annotation.Nullable;

import com.netelsan.ipinterkompanel.Constants;
import com.netelsan.ipinterkompanel.Helper;
import com.netelsan.ipinterkompanel.R;
import com.netelsan.ipinterkompanel.database.DatabaseHelper;
import com.netelsan.ipinterkompanel.model.ComPackageModel;
import com.netelsan.ipinterkompanel.model.Daire;
import com.netelsan.ipinterkompanel.model.Guvenlik;
import com.netelsan.ipinterkompanel.model.ZilPanel;
import com.netelsan.ipinterkompanel.tcp.RestartBroadcastReceiver;
import com.netelsan.ipinterkompanel.tcp.TCPHelper;
import com.netelsan.ipinterkompanel.tcp.Utils;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.Timer;
import java.util.TimerTask;

public class DeviceDisplacementAlarmActivity extends BaseGuvenlikSistemiActivity implements View.OnClickListener {

    public static boolean isActive = false;

    MediaPlayer mediaPlayerAlarm;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_device_displacement_alarm);

        isActive = true;

        startAlarmSequence();
    }

    private void startAlarmSequence() {

        setDeviceVolumeToMax();
        playAlarmSound();
        startLeds();
        setLayouts();
        sendEmergencyDataToGuvenlik();
        sendEmergencyDataToGorevliIfNeeded();
        registerCloseAlarmFromGuvenlikReceiver();

    }

    boolean isCloseAlarmFromGuvenlikRegistered = false;

    private void registerCloseAlarmFromGuvenlikReceiver() {

        isCloseAlarmFromGuvenlikRegistered = true;
        IntentFilter filter = new IntentFilter();
        filter.addAction("com.netelsan.CLOSE_ALARM_FROM_GUVENLIK");
        registerReceiver(closeEvGuvenlikFromGuvenlikBroadcastReceiver, filter);


    }

    private void sendEmergencyDataToGuvenlik() {

        DatabaseHelper databaseHelper = new DatabaseHelper(getApplicationContext());
        ZilPanel zilPanelSelf = databaseHelper.getZilPanelByIP(Utils.getIPAddress(true));
        ArrayList<Guvenlik> guvenliks = databaseHelper.getGuvenlikler();

        Iterator<Guvenlik> iterator = guvenliks.iterator();
        while (iterator.hasNext()) {
            Guvenlik guvenlik = iterator.next();
            String destinationIP = guvenlik.getIp();

            ComPackageModel model = new ComPackageModel();
            model.setZilPanel(zilPanelSelf);
            model.setOpe_type(Constants.OPERATION_ALARM_TRIGGERED);
            model.setDataInt(Constants.GUVENLIK_SISTEMI_ALARM_DISPLACEMENT);

            TCPHelper.sendMessageToIP(getApplicationContext(), destinationIP, model);
        }

    }

    private void sendEmergencyDataToGorevliIfNeeded() {

        DatabaseHelper databaseHelper = new DatabaseHelper(getApplicationContext());
        ZilPanel zilPanelSelf = databaseHelper.getZilPanelByIP(Utils.getIPAddress(true));
        ArrayList<Daire> gorevliler = databaseHelper.getGorevliDaireler();

        Iterator<Daire> iterator = gorevliler.iterator();
        while (iterator.hasNext()) {
            Daire daireGorevli = iterator.next();
            String destinationIP = daireGorevli.getIp();

            ComPackageModel model = new ComPackageModel();
            model.setZilPanel(zilPanelSelf);
            model.setOpe_type(Constants.OPERATION_ALARM_TRIGGERED);
            model.setDataInt(Constants.GUVENLIK_SISTEMI_ALARM_DISPLACEMENT);

            TCPHelper.sendMessageToIP(getApplicationContext(), destinationIP, model);
        }

    }

    private void setLayouts() {

    }

    boolean isLedOn = false;
    Timer timerForLeds = null;

    private void startLeds() {

//        Log.d(Constants.LOG_TAG, "startLeds timerForLeds= " + timerForLeds);

        if (timerForLeds == null) {
            timerForLeds = new Timer();
        } else {
            return;
        }

        int ledPortNumber = Helper.getPINFromGPIO("PE17");
        Process process = Helper.getProcess();

        timerForLeds.schedule(new TimerTask() {
            @Override
            public void run() {

//                Log.d(Constants.LOG_TAG, "startLeds timerForLeds run");
                setLedState(process, ledPortNumber, isLedOn);

                isLedOn = !isLedOn;
            }
        }, 0, 100);

    }

    private void setLedState(Process process, int ledPortNumber, boolean isLedOn) {
        Helper.setGPIO(process, Constants.GPIO_EXPORT, ledPortNumber, true, isLedOn);
        Helper.setGPIO(process, Constants.GPIO_DIRECTION, ledPortNumber, true, isLedOn);
        Helper.setGPIO(process, Constants.GPIO_VALUE, ledPortNumber, true, isLedOn);
    }

    private void playAlarmSound() {

        mediaPlayerAlarm = MediaPlayer.create(getApplicationContext(), R.raw.guvenlik_alarm);
        mediaPlayerAlarm.start();

        mediaPlayerAlarm.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
            @Override
            public void onCompletion(MediaPlayer mp) {
                mediaPlayerAlarm.start();
            }
        });

    }

    private void setDeviceVolumeToMax() {
        AudioManager audioManager = (AudioManager) getSystemService(AUDIO_SERVICE);
        int volume_level = 15;
        audioManager.setStreamVolume(AudioManager.STREAM_MUSIC, volume_level, 0);
    }

//    private void processIntent(Intent intent) {
//        if(intent == null) {
//            return;
//        }
//        Bundle bundle = intent.getExtras();
//        if(bundle == null) {
//            return;
//        }
//
//
//    }

//    private void sendDeviceDisplacementToSecondaryDevices(boolean isActivated) {
//
//
//        DatabaseHelper databaseHelper = new DatabaseHelper(getApplicationContext());
//        Daire daireSelf = databaseHelper.getDaireByIP(Utils.getIPAddress(true));
//
//        boolean isMainDevice = Helper.isMainDeviceInHome(getApplicationContext(), Utils.getIPAddress(true));
//
//        if(isMainDevice) {
//
//            ArrayList<Daire> dairesSecondary = databaseHelper.getDairelerSecondary();
//            Iterator<Daire> iterator = dairesSecondary.iterator();
//            while(iterator.hasNext()) {
//                Daire daire = iterator.next();
//                String destinationIP = daire.getIp();
//
//                ComPackageModel model = new ComPackageModel();
//                model.setOpe_type(Constants.OPERATION_EV_GUVENLIK_ALARM_STATE);
//                model.setDaire(daireSelf);
////                model.setDataInt(guvenlikAlarmType);
//                model.setDataBoolean(isActivated);
//
//                TCPHelper.sendMessageToIP(getApplicationContext(), destinationIP, model);
//
//            }
//
//        } else {
//
//            Daire daireMain = Helper.getMainDeviceInHome(getApplicationContext());
//            if(daireMain != null) {
//
//                String destinationIP = daireMain.getIp();
//
//                ComPackageModel model = new ComPackageModel();
//                model.setOpe_type(Constants.OPERATION_EV_GUVENLIK_ALARM_STATE);
//                model.setDaire(daireSelf);
////                model.setDataInt(guvenlikAlarmType);
//                model.setDataBoolean(isActivated);
//
//                TCPHelper.sendMessageToIP(getApplicationContext(), destinationIP, model);
//
//            }
//
//        }
//
//    }

    @Override
    public void onClick(View view) {

        switch (view.getId()) {
            default:
                break;
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        isActive = false;

        if (mediaPlayerAlarm != null) {
            mediaPlayerAlarm.stop();
            mediaPlayerAlarm = null;
        }

        if (timerForLeds != null) {
            timerForLeds.purge();
            timerForLeds.cancel();
            timerForLeds = null;
        }

        if (isCloseAlarmFromGuvenlikRegistered) {
            unregisterReceiver(closeEvGuvenlikFromGuvenlikBroadcastReceiver);
        }

        int ledPortNumber = Helper.getPINFromGPIO("PE17");
        Process process = Helper.getProcess();
        setLedState(process, ledPortNumber, false);

    }

    BroadcastReceiver closeEvGuvenlikFromGuvenlikBroadcastReceiver = new RestartBroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            DeviceDisplacementAlarmActivity.this.finish();
        }
    };

}

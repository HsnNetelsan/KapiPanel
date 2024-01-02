package com.netelsan.ipinterkompanel.service;

import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Binder;
import android.os.Handler;
import android.os.IBinder;
import android.util.Log;

import androidx.annotation.Nullable;

import com.netelsan.ipinterkompanel.Constants;
import com.netelsan.ipinterkompanel.Helper;
import com.netelsan.ipinterkompanel.activity.SplashSelectorActivity;
import com.netelsan.ipinterkompanel.database.DatabaseHelper;
import com.netelsan.ipinterkompanel.model.ComPackageModel;
import com.netelsan.ipinterkompanel.model.DateTimeClass;
import com.netelsan.ipinterkompanel.model.Guvenlik;
import com.netelsan.ipinterkompanel.model.ZilPanel;
import com.netelsan.ipinterkompanel.model.ZilPanelSite;
import com.netelsan.ipinterkompanel.tcp.Server;
import com.netelsan.ipinterkompanel.tcp.TCPHelper;
import com.netelsan.ipinterkompanel.tcp.Utils;
import com.stericson.RootShell.execution.Command;
import com.stericson.RootTools.RootTools;

import java.io.File;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Timer;
import java.util.TimerTask;

public class SystemControlService extends Service {

    private final IBinder mBinder = new MyBinder();

    public SystemControlService() {

    }

    public SystemControlService(Context context) {
        super();
    }


    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
//        Bundle extras = intent.getExtras();

        return mBinder;
    }

    public class MyBinder extends Binder {
    }

    @Override
    public void onCreate() {
        super.onCreate();
//        Log.i(Constants.LOG_TAG, "onCreate()");

    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        super.onStartCommand(intent, flags, startId);
//        Log.i(Constants.LOG_TAG, "onStartCommand()");

        testTCPService();

        return START_NOT_STICKY;
    }

    Timer timerForTest;
    BroadcastReceiver broadcastReceiverTestTCPService;

    private void testTCPService() {

        timerForTest = new Timer();
        timerForTest.schedule(new TimerTask() {
            @Override
            public void run() {
                restartApplication();
            }
        }, 5000);

        broadcastReceiverTestTCPService = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {

                if (timerForTest != null) {
                    timerForTest.purge();
                    timerForTest.cancel();
                    timerForTest = null;
                }


                //feyy
                requestTimeIfNeeded();




                restartServicesIfNeeded();
                startBackupDBProcess();

//                verileri burada iste. eğer site içinse zil panelinden isteyeceksin
                reguestDevicesFromZilPaneliIfNeeded();

                unregisterReceiver(broadcastReceiverTestTCPService);
                broadcastReceiverTestTCPService = null;

                stopSelf();

            }
        };

        IntentFilter filter = new IntentFilter();
        filter.addAction("com.netelsan.TEST_TCP_SERVICE");
        registerReceiver(broadcastReceiverTestTCPService, filter);

        ComPackageModel comPackageModel = new ComPackageModel();
        comPackageModel.setOpe_type(Constants.OPERATION_TEST_TCP_SERVICE);
        TCPHelper.sendMessageToIP(getApplicationContext(), Utils.getIPAddress(true), comPackageModel);

    }

    private void requestTimeIfNeeded() {
        String currentYear = Helper.getCurrentYear();
        int currentYearInt = Integer.parseInt(currentYear);
        if (currentYearInt < 2000) {
            requestDateAndTimeFromZilPanels();
        } else {
            boolean isTimeSuitable = isTimeSuitable();
            if (isTimeSuitable) {
                requestDateAndTimeFromZilPanels();
            }
        }


    }

    private void requestDateAndTimeFromZilPanels() {

        String destinationIP;

        DatabaseHelper databaseHelper = new DatabaseHelper(getApplicationContext());

        boolean isZilForSite = Helper.isZilForSite(getApplicationContext());

        Log.d(Constants.LOG_TAG, "feyy SystemControlService requestDateAndTimeFromZilPanels  isZilForSite="+isZilForSite);

        ZilPanel zilPanelSelf = databaseHelper.getZilPanelByIP(Utils.getIPAddress(true));
        ZilPanelSite zilPanelSiteSelf = databaseHelper.getSiteZilPanelByIP(Utils.getIPAddress(true));


       // Guvenlik guvenlikSelf = databaseHelper.getGuvenlikByIP(Utils.getIPAddress(true));


        boolean isCenterUnitZilPanel = Helper.isCenterUnitZilPanel(getApplicationContext());
        Log.d(Constants.LOG_TAG, "feyy SystemControlService requestDateAndTimeFromZilPanels  isCenterUnitZilPanel="+isCenterUnitZilPanel);
        if (isCenterUnitZilPanel) {
          return;
        } else {
            ArrayList<Guvenlik> arrayListGuvenlikler = databaseHelper.getGuvenlikler();
            if (arrayListGuvenlikler.size() == 0) {
                return;
            }
            Guvenlik guvenlik = arrayListGuvenlikler.get(0);
            destinationIP = guvenlik.getIp();
        }

        ComPackageModel model = new ComPackageModel();
        Log.d(Constants.LOG_TAG, "feyy SystemControlService requestDateAndTimeFromZilPanels  OPERATION_GET_DATE_TIME");
        model.setOpe_type(Constants.OPERATION_GET_DATE_TIME);
        if (isZilForSite) {
            model.setZilPanelSite(zilPanelSiteSelf);
        } else {
            model.setZilPanel(zilPanelSelf);
        }

        TCPHelper.sendMessageToIP(getApplicationContext(), destinationIP, model);
        Log.d(Constants.LOG_TAG, "feyy SystemControlService requestDateAndTimeFromZilPanels  gitti");



/*
        boolean isCenterUnitZilPanel = Helper.isCenterUnitZilPanel(getApplicationContext());
        if (isCenterUnitZilPanel) {
            ArrayList<ZilPanel> arrayListZilPaneller = databaseHelper.getZilPanelleri();
            if (arrayListZilPaneller.size() == 0) {
                return;
            }
            ZilPanel zilPanel = arrayListZilPaneller.get(0);
            destinationIP = zilPanel.getIp();
        } else {
            return;
        }

        ComPackageModel model = new ComPackageModel();
        model.setOpe_type(Constants.OPERATION_GET_DATE_TIME);
        model.setGuvenlik(guvenlikSelf);

        TCPHelper.sendMessageToIP(getApplicationContext(), destinationIP, model, false);*/

    }

    private void restartServicesIfNeeded() {

        boolean isGlobalTouchRunning = Helper.isServiceRunning(getApplicationContext(), KeyPadService.class);
        if (!isGlobalTouchRunning) {
            KeyPadService keyPadService = new KeyPadService(getApplicationContext());
            Intent mServiceIntent = new Intent(SystemControlService.this, keyPadService.getClass());
            startService(mServiceIntent);
        }

    }


    private void reguestDevicesFromZilPaneliIfNeeded() {

        boolean isZilForSite = Helper.isZilForSite(getApplicationContext());
        if (isZilForSite) {
            boolean isTimeSuitable = isTimeSuitable();
            if (isTimeSuitable) {
                requestForDevicesFromZilPanel();
            }
        }
    }

    private void requestForDevicesFromZilPanel() {

        DatabaseHelper databaseHelper = new DatabaseHelper(getApplicationContext());
        ZilPanelSite zilPanelSite = databaseHelper.getSiteZilPanelByIP(Utils.getIPAddress(true));

        ArrayList<ZilPanel> zilPanels = databaseHelper.getZilPanelleri(true);
        if (zilPanels.size() == 0) {
            return;
        }

        ZilPanel zilPanel = zilPanels.get(0);

        ComPackageModel model = new ComPackageModel();
        model.setOpe_type(Constants.OPERATION_REQUEST_DEVICE_INFOS);
        model.setZilPanelSite(zilPanelSite);

        String destinationIp = zilPanel.getIp();
        TCPHelper.sendMessageToIP(getApplicationContext(), destinationIp, model);

    }

    private boolean isTimeSuitable() {

        Calendar calendar = Calendar.getInstance();

        int currentHour = calendar.get(Calendar.HOUR_OF_DAY);
        int currentMinute = calendar.get(Calendar.MINUTE);



        int targetHour = 4;

        if (currentHour == targetHour) {

            if (currentMinute > 10) {
                reguestDevicesFromZilPaneliIfNeeded();
                return true;
            }
        }
        //burayı kapatıyoruz
        /*
        if(currentHour==2 && 30 >=currentMinute && currentMinute >= 10)
        {
            rebootDevice();
        }*/
        return false;
    }
    private void rebootDevice() {
        try {
            Process process = Runtime.getRuntime().exec("adb reboot");
            process.waitFor();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void restartApplication() {
//        burada uygulamaya restart atmak yerine cachi boşalt ve tcp server ı baştan başlat
        Helper.deleteCache(getApplicationContext());
        boolean isServerRunning = Helper.isServiceRunning(getApplicationContext(), Server.class);
        if (isServerRunning) {
            Intent mServiceIntent = new Intent(SystemControlService.this, Server.class);
            stopService(mServiceIntent);
        }

        startServerService();

    }

    private void startServerService() {

        Server serverService = new Server(SystemControlService.this);
        Intent mServiceIntent = new Intent(SystemControlService.this, serverService.getClass());
        boolean isServiceRunning = Helper.isServiceRunning(getApplicationContext(), serverService.getClass());
        if (!isServiceRunning) {
            startService(mServiceIntent);
        }

    }

    private void startBackupDBProcess() {

        if (!Helper.isSDCardMounted()) {
            return;
        }

        Helper.backupDBToSDCard();

    }


    @Override
    public void onDestroy() {

        if (timerForTest != null) {
            timerForTest.purge();
            timerForTest.cancel();
            timerForTest = null;
        }

        if (broadcastReceiverTestTCPService != null) {
            unregisterReceiver(broadcastReceiverTestTCPService);
        }

    }

    @Override
    public void onTaskRemoved(Intent rootIntent) {
        Log.i(Constants.LOG_TAG, "onTaskRemoved()");

        super.onTaskRemoved(rootIntent);

    }

    @Override
    public void onLowMemory() {
        super.onLowMemory();
    }

}

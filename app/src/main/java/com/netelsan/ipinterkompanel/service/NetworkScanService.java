package com.netelsan.ipinterkompanel.service;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Binder;
import android.os.Bundle;
import android.os.IBinder;
import android.util.Log;

import androidx.annotation.Nullable;

import com.netelsan.ipinterkompanel.Constants;
import com.netelsan.ipinterkompanel.Helper;
import com.netelsan.ipinterkompanel.database.DatabaseHelper;
import com.netelsan.ipinterkompanel.listener.NetworkScanListener;
import com.netelsan.ipinterkompanel.model.ZilPanel;
import com.netelsan.ipinterkompanel.model.ZilPanelSite;
import com.netelsan.ipinterkompanel.tcp.Server;

import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.SocketAddress;
import java.util.ArrayList;

public class NetworkScanService extends Service {

    private final IBinder mBinder = new NetworkScanServiceBinder();

    String selfIP = "";

    NetworkScanListener networkScanListener;

    public NetworkScanService() {

    }

    public NetworkScanService(Context context) {
        super();
        Log.i(Constants.LOG_TAG, "NetworkScanService class");
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        Bundle extras = intent.getExtras();

        return mBinder;
    }

    public class NetworkScanServiceBinder extends Binder {
        public NetworkScanService getService() {
            return NetworkScanService.this;
        }
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

        return START_NOT_STICKY;
    }

    @Override
    public void onDestroy() {
        Log.i(Constants.LOG_TAG, "serviceOnDestroy()");

        super.onDestroy();

    }

    @Override
    public void onTaskRemoved(Intent rootIntent) {
        Log.i(Constants.LOG_TAG, "onTaskRemoved()");

        super.onTaskRemoved(rootIntent);

    }

    @Override
    public void onLowMemory() {
        super.onLowMemory();
        Log.i(Constants.LOG_TAG, "onLowMemory()");
    }

    public void startNetworkScanWithListener(NetworkScanListener networkScanListener, String selfIPAddress) {

        this.networkScanListener = networkScanListener;

        boolean isCenterUnitZilPanel = Helper.isCenterUnitZilPanel(getApplicationContext());
        boolean isZilForSite = Helper.isZilForSite(getApplicationContext());

        DatabaseHelper databaseHelper = new DatabaseHelper(getApplicationContext());
        if(isZilForSite) {
            if(isCenterUnitZilPanel) {
                ZilPanelSite zilPanelSite = databaseHelper.getSiteZilPanelByIP(selfIPAddress);
                selfIP = zilPanelSite.getIp();
                new ScanTask(0, isZilForSite).executeOnExecutor(Constants.DEFAULT_EXECUTOR);
            } else {// merkezde güvenlik olacak şekilde taratsın
                new ScanTaskForGuvenliks(isZilForSite).executeOnExecutor(Constants.DEFAULT_EXECUTOR);
            }
        } else {

            if(isCenterUnitZilPanel) {// zil paneli merkezde olacak şekilde çalışsın
                ZilPanel zilPanel = databaseHelper.getZilPanelByIP(selfIPAddress);
                selfIP = zilPanel.getIp();
                new ScanTask(zilPanel.getBlok(), isZilForSite).executeOnExecutor(Constants.DEFAULT_EXECUTOR);
            } else {// burada sadece güvenlikleri ara
                new ScanTaskForGuvenliks(isZilForSite).executeOnExecutor(Constants.DEFAULT_EXECUTOR);
            }

        }

    }

    class ScanTaskForGuvenliks extends AsyncTask<Void, Void, ArrayList<String>> {

        boolean isZilForSite;

        public ScanTaskForGuvenliks(boolean isZilForSite) {
            this.isZilForSite = isZilForSite;
        }

        protected void onPreExecute() {
            super.onPreExecute();
        }

        protected ArrayList<String> doInBackground(Void... arg0) {
            ArrayList<String> arrayListTotal = new ArrayList<>();

            ArrayList<String> arrayListGuvenlikler = scanGuvenlikler();
            arrayListTotal.addAll(arrayListGuvenlikler);

            return arrayListTotal;
        }

        protected void onPostExecute(ArrayList<String> result) {
            super.onPostExecute(result);

            networkScanListener.onFinished(result);

            stopSelf();
        }

    }

    class ScanTask extends AsyncTask<Void, Void, ArrayList<String>> {

        int blokNo;
        boolean isZilForSite;

        public ScanTask(int blokNo, boolean isZilForSite) {
            this.blokNo = blokNo;
            this.isZilForSite = isZilForSite;
        }

        protected void onPreExecute() {
            super.onPreExecute();
        }

        protected ArrayList<String> doInBackground(Void... arg0) {
            ArrayList<String> arrayListTotal = new ArrayList<>();

            ArrayList<String> arrayListZilPaneller = scanZilPanelleri();
            arrayListTotal.addAll(arrayListZilPaneller);

            if(!isZilForSite) {

                ArrayList<String> arrayListDaireler = scanDairesInSameBuilding(blokNo);
                ArrayList<String> arrayListGuvenlikler = scanGuvenlikler();

                arrayListTotal.addAll(arrayListDaireler);
                arrayListTotal.addAll(arrayListGuvenlikler);
            }

            return arrayListTotal;
        }

        protected void onPostExecute(ArrayList<String> result) {
            super.onPostExecute(result);

            networkScanListener.onFinished(result);

            stopSelf();
        }

    }

    private ArrayList<String> scanDairesInSameBuilding(int blokNo) {

        ArrayList<String> arrayList = new ArrayList<String>();

        int octet2 = blokNo;
        int octet3 = 0;
        int octet4 = 0;

        int octet3TryCount = 0;
        int octet4TryCount = 0;

        outerloop:
        for(octet3 = 0; octet3 <= 244; octet3++) {

            if(octet3TryCount > 3) {
                break outerloop;
            }

            innerloop:
            for(octet4 = 0; octet4 < (256 / Constants.DAIRE_ICI_MAX_DEVICE_NUMBER); octet4++) {

                String ipAddress = "172." + octet2 + "." + octet3 + "." + (octet4 * Constants.DAIRE_ICI_MAX_DEVICE_NUMBER);
                if(ipAddress.equals(selfIP)) {
                    octet3TryCount = 0;
                    octet4TryCount = 0;
                    continue innerloop;
                }

                if(octet4TryCount > 5) {
                    octet4TryCount = 0;
                    break innerloop;
                }

//                Log.d(Constants.LOG_TAG, "1907 scanDairesInSameBuilding Trying: " + ipAddress);
                try {
//                    Log.d(Constants.LOG_TAG, "1907 scanDairesInSameBuilding octet3TryCount: " + octet3TryCount + " octet4TryCount=" + octet4TryCount);
                    boolean isReachableByTcp = isReachableByTcp(ipAddress);
                    if(isReachableByTcp) {
                        arrayList.add(ipAddress);
                        octet3TryCount = 0;
                        octet4TryCount = 0;
                        Log.d(Constants.LOG_TAG, "1907 scanDairesInSameBuilding reacheable = " + ipAddress);
                    } else {
                        octet4TryCount = octet4TryCount + 1;
                    }

                } catch(Exception e) {
                    e.printStackTrace();
                }

            }

            octet3TryCount = octet3TryCount + 1;

        }

        return arrayList;
    }

    private ArrayList<String> scanZilPanelleri() {

        ArrayList<String> arrayList = new ArrayList<String>();

        int octet2 = 0;
        int octet4 = 0;

        int octet2TryCount = 0;
        int octet4TryCount = 0;

        outerloop:
        for(octet2 = 0; octet2 <= 244; octet2++) {

            if(octet2TryCount > 3) {
                break outerloop;
            }

            innerloop:
            for(octet4 = 0; octet4 <= 255; octet4++) {

                String ipAddress = "172." + octet2 + "." + 255 + "." + octet4;
                if(ipAddress.equals(selfIP)) {
                    octet2TryCount = 0;
                    octet4TryCount = 0;
                    continue innerloop;
                }

                if(octet4TryCount > 5) {
                    octet4TryCount = 0;
                    break innerloop;
                }

//                Log.d(Constants.LOG_TAG, "1907 scanZilPanelleri Trying: " + ipAddress);
                try {
                    boolean isReachableByTcp = isReachableByTcp(ipAddress);
                    if(isReachableByTcp) {
                        arrayList.add(ipAddress);
                        octet2TryCount = 0;
                        octet4TryCount = 0;
                        Log.d(Constants.LOG_TAG, "1907 scanZilPanelleri reacheable = " + ipAddress);
                    } else {
                        octet4TryCount = octet4TryCount + 1;
                    }

                } catch(Exception e) {
                    e.printStackTrace();
                }

            }
            octet2TryCount = octet2TryCount + 1;
        }

        return arrayList;
    }

    private ArrayList<String> scanGuvenlikler() {

        ArrayList<String> arrayList = new ArrayList<String>();

        int octet2 = 255;
        int octet3 = 0;
        int octet4 = 0;

        int octet3TryCount = 0;
        int octet4TryCount = 0;

        outerloop:
        for(octet3 = 0; octet3 <= 255; octet3++) {

            if(octet3TryCount > 3) {
                break outerloop;
            }

            innerloop:
            for(octet4 = 0; octet4 <= 255; octet4++) {

                String ipAddress = "172." + octet2 + "." + octet3 + "." + octet4;
                if(ipAddress.equals(selfIP)) {
                    octet3TryCount = 0;
                    octet4TryCount = 0;
                    continue innerloop;
                }

                if(octet4TryCount > 5) {
                    octet4TryCount = 0;
                    break innerloop;
                }

//                Log.d(Constants.LOG_TAG, "1907 scanGuvenlikler Trying: " + ipAddress);
                try {
                    boolean isReachableByTcp = isReachableByTcp(ipAddress);
                    if(isReachableByTcp) {
                        arrayList.add(ipAddress);
                        octet3TryCount = 0;
                        octet4TryCount = 0;
                        Log.d(Constants.LOG_TAG, "1907 scanGuvenlikler reacheable = " + ipAddress);
                    } else {
                        octet4TryCount = octet4TryCount + 1;
                    }

                } catch(Exception e) {
                    e.printStackTrace();
                }

            }

            octet3TryCount = octet3TryCount + 1;
        }

        return arrayList;
    }

    private synchronized boolean isReachableByTcp(String host) {
        try {
            Socket socket = new Socket();
            SocketAddress socketAddress = new InetSocketAddress(host, Server.SERVERPORT);
            socket.connect(socketAddress, 50);
            socket.close();
            return true;
        } catch(Exception e) {
//            e.printStackTrace();
            return false;
        }
    }

}
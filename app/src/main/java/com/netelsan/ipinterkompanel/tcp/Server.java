package com.netelsan.ipinterkompanel.tcp;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.AsyncTask;
import android.os.IBinder;
import android.os.SystemClock;
import android.provider.Settings;
import android.util.Log;

import androidx.annotation.Nullable;

import com.netelsan.ipinterkompanel.Constants;
import com.netelsan.ipinterkompanel.Helper;
import com.netelsan.ipinterkompanel.R;
import com.netelsan.ipinterkompanel.activity.MainActivity;
import com.netelsan.ipinterkompanel.database.DatabaseHelper;
import com.netelsan.ipinterkompanel.model.ComPackageModel;
import com.netelsan.ipinterkompanel.model.Daire;
import com.netelsan.ipinterkompanel.model.DoorPassword;
import com.netelsan.ipinterkompanel.model.DoorUnlockLog;
import com.netelsan.ipinterkompanel.model.Guvenlik;
import com.netelsan.ipinterkompanel.model.IPCamera;
import com.netelsan.ipinterkompanel.model.ZilPanel;
import com.netelsan.ipinterkompanel.model.ZilPanelSite;
import com.netelsan.ipinterkompanel.service.KeyPadService;
import com.netelsan.ipinterkompanel.webrtc.activity.CallActivity;
import com.scottyab.aescrypt.AESCrypt;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.ServerSocket;
import java.net.Socket;
import java.security.GeneralSecurityException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Iterator;

public class Server extends Service implements ServiceConnection {


    private ServerSocket serverSocket;
    //    Handler messageProcessHandler;
    Thread serverThread = null;
    public static final int SERVERPORT = 5432;
    public static ArrayList<Integer> blokKontrolList = new ArrayList<Integer>();
    public static String serverGeciciHour = "00";
    public static String serverGeciciMinute = "00";


    private int counter = 0;

    public Server() {

    }

    public Server(Context context) {
        super();
        Log.i(Constants.LOG_TAG, "Server class");
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        Log.i(Constants.LOG_TAG, "onCreate()");

    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        super.onStartCommand(intent, flags, startId);
        Log.i(Constants.LOG_TAG, "onStartCommand()");

        this.serverThread = new Thread(new ServerThread(), "ServerThread");
        this.serverThread.start();

        return START_STICKY;
    }

    @Override
    public void onDestroy() {
        Log.i(Constants.LOG_TAG, "serviceOnDestroy()");

        super.onDestroy();

        try {
            serverThread.interrupt();
        } catch (Exception e) {
            e.printStackTrace();
        }

        try {
            if (serverSocket != null) {
                serverSocket.close();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

        Intent broadcastIntent = new Intent("com.netelsan.panel.RestartServiceSensor");
        sendBroadcast(broadcastIntent);

    }

    class ServerThread implements Runnable {

        public void run() {

            Socket socket = null;
            try {
                serverSocket = new ServerSocket(SERVERPORT);
            } catch (IOException e) {
                stopSelf();
                e.printStackTrace();
            }
            while (!Thread.currentThread().isInterrupted()) {

                try {

                    if (serverSocket.isClosed()) {
                        return;
                    }

                    socket = serverSocket.accept();

                    new SocketServerReplyTask(socket).executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);

                } catch (IOException e) {
                    Thread.currentThread().interrupt();
                    stopSelf();
                    e.printStackTrace();
                }
            }
        }
    }

    private class SocketServerReplyTask extends AsyncTask<String, String, String> {

        String dataString = "";

        private Socket hostThreadSocket;

        private BufferedReader bufferedReader;

        public SocketServerReplyTask(Socket hostThreadSocket) {
            this.hostThreadSocket = hostThreadSocket;
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
//            Log.d(Constants.LOG_TAG, "SocketServerReplyTask onPreExecute for " + hostThreadSocket.getInetAddress().getHostAddress() + " isConnected=" + hostThreadSocket.isConnected());
        }

        @Override
        protected String doInBackground(String... strings) {
//            Log.d(Constants.LOG_TAG, "SocketServerReplyTask doInBackground for " + hostThreadSocket.getInetAddress().getHostAddress() + " isConnected=" + hostThreadSocket.isConnected());

            try {

                InputStream inputStream = this.hostThreadSocket.getInputStream();
                InputStreamReader inputStreamReader = new InputStreamReader(inputStream);
                this.bufferedReader = new BufferedReader(inputStreamReader);

                String line = "";
                while ((line = bufferedReader.readLine()) != null) {
                    dataString = line;
                }

                String requestIP = hostThreadSocket.getInetAddress().getHostAddress();
//                Log.d(Constants.LOG_TAG, "messageReceived requestIP=" + requestIP + " request=" + dataString);

                inputStream.close();

                bufferedReader.close();

                hostThreadSocket.close();

                processRequest(dataString, requestIP);

            } catch (Exception e) {
                stopSelf();
                e.printStackTrace();
            }

            return null;
        }

        @Override
        protected void onPostExecute(String s) {

//            Log.d(Constants.LOG_TAG, "SocketServerReplyTask onPostExecute for " + hostThreadSocket.getInetAddress().getHostAddress() + " isConnected=" + hostThreadSocket.isConnected());
            super.onPostExecute(s);
        }

    }

    private void processRequest(String encryptedMsg, String requestIP) throws Exception {

        String decryptedMsg = null;
        try {
            decryptedMsg = AESCrypt.decrypt(Constants.ENCRYPTION_PASSWORD, encryptedMsg);
        } catch (Exception e) {
            //handle error
            decryptedMsg = encryptedMsg;
        }

        Log.d(Constants.LOG_TAG, "processRequest decryptedMsg=" + encryptedMsg);
        if (decryptedMsg == null) {
            return;
        }

        DatabaseHelper databaseHelper = new DatabaseHelper(getApplicationContext());

        ComPackageModel comPackageModel = Helper.getComPackageModelFromJsonString(decryptedMsg);
        int operationType = comPackageModel.getOpe_type();

        makeRequestResponseOperation(operationType, comPackageModel, databaseHelper, requestIP);


    }

    private void makeRequestResponseOperation(int operationType, ComPackageModel comPackageModel, DatabaseHelper databaseHelper, String requestIP) {

        switch (operationType) {

            case Constants.OPERATION_HANDSHAKE_DAIRE_RESPONSE: {
                ArrayList<ZilPanel> zilPanels = comPackageModel.getZilPanels();
                ArrayList<Daire> daires = comPackageModel.getDaires();

                saveZilPanelsToDB(databaseHelper, zilPanels);
                saveDairesToDB(databaseHelper, daires);
                break;
            }

            case Constants.OPERATION_HANDSHAKE_ZIL_PANEL_RESPONSE: {

                Guvenlik guvenlik = comPackageModel.getGuvenlik();
                if (guvenlik != null) {

                    Guvenlik guvenlikLocal = databaseHelper.getGuvenlikByIP(guvenlik.getIp());
                    if (guvenlikLocal == null) {
                        databaseHelper.insertGuvenlik(guvenlik);
                    } else {
                        databaseHelper.updateGuvenlik(guvenlik);
                    }
                }

                Daire daire = comPackageModel.getDaire();
                if (daire != null) {

                    Daire daireLocal = databaseHelper.getDaireByIP(daire.getIp());
                    if (daireLocal == null) {
                        databaseHelper.insertDaire(daire);
                    } else {
                        databaseHelper.updateDaire(daire);
                    }
                }

                ZilPanel zilPanel = comPackageModel.getZilPanel();
                if (zilPanel != null) {

                    ZilPanel zilPanelLocal = databaseHelper.getZilPanelByIP(zilPanel.getIp());
                    if (zilPanelLocal == null) {
                        databaseHelper.insertZilPanel(zilPanel);
                    } else {
                        databaseHelper.updateZilPanel(zilPanel);
                    }
                }

//                boolean isPanelsAddedBefore = isPanelsAddedBefore(databaseHelper);
//                boolean isDairesAddedBefore = isDairesAddedBefore(databaseHelper);

//                if(! isPanelsAddedBefore) {
//                    ArrayList<ZilPanel> zilPanels = comPackageModel.getZilPanels();
//                    saveZilPanelsToDB(databaseHelper, zilPanels);
//                }

//                if(! isDairesAddedBefore) {
                ArrayList<Daire> daires = comPackageModel.getDaires();
                saveDairesToDB(databaseHelper, daires);
//                }

                boolean isCenterUnitZilPanel = Helper.isCenterUnitZilPanel(getApplicationContext());
                if (!isCenterUnitZilPanel) {
                    long timeMillis = comPackageModel.getDataLong();
                    Helper.setDeviceTime(getApplicationContext(), timeMillis);
                }
                break;
            }

            case Constants.OPERATION_ARAMA_REQUEST_RESPONSE: {
                boolean isCallRequestAccepted = comPackageModel.getDataBoolean();
                int messageId = comPackageModel.getDataInt();
                String rejectMessage = Helper.getComMessageById(getApplicationContext(), messageId);

                Log.d("HK","HK rejected.................................");
                Intent rejectIntent = new Intent("com.netelsan.CALL_REQUEST_RESPONSE");
                rejectIntent.putExtra("message", rejectMessage);
                rejectIntent.putExtra("isAccepted", isCallRequestAccepted);
                sendBroadcast(rejectIntent);
                break;



              /*  Log.d(Constants.LOG_TAG, "feyy feyy  noHostDeviceDialogCloseText5");
                boolean isCallAccepted = comPackageModel.getDataBoolean();

                ZilPanel zilPanel = comPackageModel.getZilPanel();
                ZilPanelSite zilPanelSite = comPackageModel.getZilPanelSite();
                Daire daire = comPackageModel.getDaire();
                Guvenlik guvenlik = comPackageModel.getGuvenlik();

                int callerType;
                if (zilPanel != null) {
                    callerType = Constants.CALLER_ZIL;
                } else if (zilPanelSite != null) {
                    callerType = Constants.CALLER_ZIL_SITE;
                } else if (daire != null) {
                    callerType = Constants.CALLER_SUBE;
                } else {
                    callerType = Constants.CALLER_GUVENLIK;
                }

                if (isCallAccepted) {

                    if (zilPanel != null) {//zil paneli aramayı kabul etti demek

                        Intent intentStartCall = new Intent(getApplicationContext(), CallActivity.class);
                        intentStartCall.putExtra(Constants.PARAM_CALL_RECEIVING, false);

                        intentStartCall.putExtra(Constants.PARAM_CALL_IS_YONLENDIRME, false);
                        intentStartCall.putExtra(Constants.PARAM_CALL_SS_IDS, new ArrayList<String>());
                        intentStartCall.putExtra(Constants.PARAM_IS_MULTI_CALL_ACCEPT, false);
                        intentStartCall.putExtra(Constants.PARAM_CALL_IS_NEED_VIDEO, true);
                        intentStartCall.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                        startActivity(intentStartCall);

                    } else if (zilPanelSite != null) {//zil paneli site aramayı kabul etti demek

                        Intent intentStartCall = new Intent(getApplicationContext(), CallActivity.class);
                        intentStartCall.putExtra(Constants.PARAM_CALL_RECEIVING, false);
                        intentStartCall.putExtra(Constants.PARAM_CALL_IS_YONLENDIRME, false);
                        intentStartCall.putExtra(Constants.PARAM_CALL_SS_IDS, new ArrayList<String>());
                        intentStartCall.putExtra(Constants.PARAM_IS_MULTI_CALL_ACCEPT, false);
                        intentStartCall.putExtra(Constants.PARAM_CALL_IS_NEED_VIDEO, true);
                        intentStartCall.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                        startActivity(intentStartCall);

                    } else {//daire aramayı kabul etti demek

                        boolean isCallRequestAccepted = comPackageModel.getDataBoolean();

                        int messageId = comPackageModel.getDataInt();
                        String message = Helper.getComMessageById(getApplicationContext(), messageId);
                        Intent rejectIntent = new Intent("com.netelsan.CALL_REQUEST_RESPONSE");
                        rejectIntent.putExtra("message", message);
                        rejectIntent.putExtra("isAccepted", isCallRequestAccepted);
                        sendBroadcast(rejectIntent);

                    }

                } else {

                    //Bu mesajı ekranda göster
                    int messageId = comPackageModel.getDataInt();
                    String rejectMessage = Helper.getComMessageById(getApplicationContext(), messageId);
                    boolean isCallRequestAccepted = comPackageModel.getDataBoolean();

                    if (zilPanel != null) {

                        Handler handler = new Handler(Looper.getMainLooper());
                        handler.post(new Runnable() {

                            @Override
                            public void run() {
//                                Toast.makeText(getApplicationContext(), rejectMessage, Toast.LENGTH_SHORT).show();
                               *//* MDToast mdToast = MDToast.makeText(getApplicationContext(), rejectMessage, MDToast.LENGTH_SHORT, MDToast.TYPE_ERROR);
                                mdToast.show();*//*
                                Helper.getComMessageById(getApplicationContext(), messageId);
                            }
                        });
                    } else if (zilPanelSite != null) {

                        Handler handler = new Handler(Looper.getMainLooper());
                        handler.post(new Runnable() {

                            @Override
                            public void run() {
//                                Toast.makeText(getApplicationContext(), rejectMessage, Toast.LENGTH_SHORT).show();
                               *//* MDToast mdToast = MDToast.makeText(getApplicationContext(), rejectMessage, MDToast.LENGTH_SHORT, MDToast.TYPE_ERROR);
                                mdToast.show();*//*
                                Helper.getComMessageById(getApplicationContext(), messageId);
                            }
                        });
                    } else {
                        Intent broadcastIntent = new Intent("com.netelsan.CALL_REQUEST_RESPONSE");
                        broadcastIntent.putExtra("message", rejectMessage);
                        broadcastIntent.putExtra("isAccepted", isCallRequestAccepted);
                        sendBroadcast(broadcastIntent);


                    }

                }
                break;*/
            }

            case Constants.OPERATION_REQUEST_YONLENDIRME_RESPONSE: {

                String yonlendirmeDeviceIP = comPackageModel.getDataString();
                Intent intent = new Intent("com.netelsan.YONLENDIRME_STATE");
                intent.putExtra("isReachable", true);
                if (yonlendirmeDeviceIP == null || yonlendirmeDeviceIP.equals("")) {// yönlendirme yok demek
                    intent.putExtra("isNeedYonlendirme", false);
                    int totalSecondarDeviceCount = comPackageModel.getDataInt();
                    if (totalSecondarDeviceCount > 1) {
                        boolean isBusy = comPackageModel.getDataBoolean();
                        intent.putExtra("isBusy", isBusy);
                    }
                    intent.putExtra("totalDeviceCount", totalSecondarDeviceCount);
                } else {//yönlendirme yapılacak
                    intent.putExtra("isNeedYonlendirme", true);
                    intent.putExtra("deviceIP", yonlendirmeDeviceIP);
                }
                sendBroadcast(intent);
                break;
            }

            case Constants.OPERATION_TEST_TCP_SERVICE: {
                Intent broadcastIntent = new Intent("com.netelsan.TEST_TCP_SERVICE");
                sendBroadcast(broadcastIntent);
                break;
            }

            case Constants.OPERATION_MULTI_CALL_REJECT: {
                Intent broadcastIntent = new Intent("com.netelsan.MULTI_CALL_RECEIVER");
                broadcastIntent.putExtra("ope_type", operationType);
                sendBroadcast(broadcastIntent);
                break;
            }

            case Constants.OPERATION_MULTI_CALL_ACCEPTED: {
                Daire daire = comPackageModel.getDaire();

                Intent broadcastIntent = new Intent("com.netelsan.MULTI_CALL_RECEIVER");
                broadcastIntent.putExtra("ope_type", operationType);
                broadcastIntent.putExtra("object", daire);
                sendBroadcast(broadcastIntent);
                break;
            }

            case Constants.OPERATION_NEW_IP_CAMERA_ADDED: {
                IPCamera ipCamera = comPackageModel.getIpCamera();

                IPCamera ipCameraLocal = databaseHelper.getCameraByIP(ipCamera.getIp());
                if (ipCameraLocal == null) {
                    databaseHelper.insertIPCamera(ipCamera);
                } else {
                    databaseHelper.updateIPCamera(ipCamera);
                }

                sendNewCameraAddedToDairesInSameBuilding(databaseHelper, ipCamera);
                break;
            }

            case Constants.OPERATION_NEW_IP_CAMERA_REMOVED: {
                IPCamera ipCamera = comPackageModel.getIpCamera();

                databaseHelper.deleteIPCamera(ipCamera);

                sendNewCameraRemovedToDairesInSameBuilding(databaseHelper, ipCamera);
                break;
            }

            case Constants.OPERATION_EDIT_PASSWORD: {
                DoorPassword doorPassword = comPackageModel.getDoorPassword();
                databaseHelper.updatePassword(doorPassword);
                break;
            }

            case Constants.OPERATION_HANDSHAKE_DAIRE: {
                Daire newDaire = comPackageModel.getDaire();

                Daire daireLocal = databaseHelper.getDaireByIP(newDaire.getIp());
                if (daireLocal == null) {
                    databaseHelper.insertDaire(newDaire);
                } else {
                    databaseHelper.updateDaire(newDaire);
                }

                ZilPanel zilPanelSelf = databaseHelper.getZilPanelByIP(Utils.getIPAddress(true));
                ArrayList<Daire> daires = databaseHelper.getDairelerInSameApartment(zilPanelSelf.getBlok());
//                ArrayList<ZilPanel> zilPanels = databaseHelper.getZilPanelleri(true);
//                ArrayList<Daire> daires = databaseHelper.getDaireler(true);
                ArrayList<Guvenlik> guvenlikler = databaseHelper.getGuvenlikler();
                ArrayList<IPCamera> ipCameras = databaseHelper.getIPCameras();
                ArrayList<ZilPanelSite> siteZilPanels = databaseHelper.getSiteZilPanelleri(true);

                ComPackageModel model = new ComPackageModel();
                model.setOpe_type(Constants.OPERATION_HANDSHAKE_DAIRE_RESPONSE);
                model.setZilPanel(zilPanelSelf);
                model.setDaires(daires);
                model.setGuvenliks(guvenlikler);
                //feyhsn
                if(newDaire.getBlok()==zilPanelSelf.getBlok())
                model.setIpCameras(ipCameras);
                model.setZilPanelSites(siteZilPanels);

                model.setDataLong(System.currentTimeMillis());

                sendResponseMessage(requestIP, model);

                sendNewDaireAddedToOtherDairesInSameBuilding(databaseHelper, newDaire);

                sendNewDaireAddedToGuvenlikler(newDaire, guvenlikler);
                break;

            }

            case Constants.OPERATION_HANDSHAKE_ZIL_PANEL: {
                ZilPanel newZilPanel = comPackageModel.getZilPanel();

                ZilPanel zilPanelLocal = databaseHelper.getZilPanelByIP(newZilPanel.getIp());
                if (zilPanelLocal == null) {
                    databaseHelper.insertZilPanel(newZilPanel);
                } else {
                    databaseHelper.updateZilPanel(newZilPanel);
                }

                ZilPanel zilPanelSelf = databaseHelper.getZilPanelByIP(Utils.getIPAddress(true));
                ArrayList<Daire> daires = databaseHelper.getDairelerInSameApartment(zilPanelSelf.getBlok());
//                ArrayList<ZilPanel> zilPanels = databaseHelper.getZilPanelleri(true);
//                ArrayList<Daire> daires = databaseHelper.getDaireler(true);

                ComPackageModel model = new ComPackageModel();
                model.setOpe_type(Constants.OPERATION_HANDSHAKE_ZIL_PANEL_RESPONSE);
                model.setZilPanel(zilPanelSelf);
                model.setDaires(daires);

                sendResponseMessage(requestIP, model);

//            kendi dairelerine bilgi gönder kaydetsin
                sendNewZilPanelAddedToOtherDaires(databaseHelper, newZilPanel);
                break;
            }

            case Constants.OPERATION_HANDSHAKE_GUVENLIK: {

                boolean isCenterUnitZilPanel = Helper.isCenterUnitZilPanel(getApplicationContext());
                if (isCenterUnitZilPanel) {

                    ArrayList<Guvenlik> guvenliks = databaseHelper.getGuvenlikler();

                    Guvenlik guvenlikNew = comPackageModel.getGuvenlik();

                    Guvenlik guvenlikLocal = databaseHelper.getGuvenlikByIP(guvenlikNew.getIp());
                    if (guvenlikLocal == null) {
                        databaseHelper.insertGuvenlik(guvenlikNew);
                    } else {
                        databaseHelper.updateGuvenlik(guvenlikNew);
                    }

                    ZilPanel zilPanelSelf = databaseHelper.getZilPanelByIP(Utils.getIPAddress(true));

//                ArrayList<ZilPanel> zilpanels = databaseHelper.getZilPanelleri(true);
                    ArrayList<Daire> arrayListDaires = databaseHelper.getDairelerInSameApartment(zilPanelSelf.getBlok());
                    ArrayList<ZilPanelSite> siteZilPanels = databaseHelper.getSiteZilPanelleri(true);
                    ArrayList<IPCamera> ipCameras = databaseHelper.getIPCameras();

                    ComPackageModel model = new ComPackageModel();
                    model.setOpe_type(Constants.OPERATION_HANDSHAKE_GUVENLIK_RESPONSE);
                    model.setZilPanel(zilPanelSelf);
                    model.setDaires(arrayListDaires);// binadaki dairelerin datası
                    model.setGuvenliks(guvenliks);// zil panelindeki guvenlikler
                    model.setZilPanelSites(siteZilPanels);
                    model.setIpCameras(ipCameras);
                    model.setDataLong(System.currentTimeMillis());//güvenlik konsolları için saat datası

                    sendResponseMessage(requestIP, model);

                    sendNewGuvenlikAddedToDaires(guvenlikNew, arrayListDaires);
                    sendNewGuvenlikAddedToGuvenliks(guvenlikNew, guvenliks);
                    sendNewGuvenlikAddedToSiteZilPanels(guvenlikNew, siteZilPanels);

                } else {
                    Guvenlik guvenlikNew = comPackageModel.getGuvenlik();

                    Guvenlik guvenlikLocal = databaseHelper.getGuvenlikByIP(guvenlikNew.getIp());
                    if (guvenlikLocal == null) {
                        databaseHelper.insertGuvenlik(guvenlikNew);
                    } else {
                        databaseHelper.updateGuvenlik(guvenlikNew);
                    }

                    ComPackageModel model = new ComPackageModel();
                    boolean isZilForSite = Helper.isZilForSite(getApplicationContext());
                    if (isZilForSite) {
                        ZilPanelSite zilPanelSiteSelf = databaseHelper.getSiteZilPanelByIP(Utils.getIPAddress(true));
                        model.setZilPanelSite(zilPanelSiteSelf);
                    } else {
                        ZilPanel zilPanelSelf = databaseHelper.getZilPanelByIP(Utils.getIPAddress(true));
                        model.setZilPanel(zilPanelSelf);
                    }
                    model.setOpe_type(Constants.OPERATION_HANDSHAKE_GUVENLIK_RESPONSE);

                    TCPHelper.sendMessageToIP(getApplicationContext(), requestIP, model);
                }

                break;
            }

            case Constants.OPERATION_GET_DATE_TIME: {
                ComPackageModel model = new ComPackageModel();
                model.setOpe_type(Constants.OPERATION_GET_DATE_TIME_RESPONSE);
                model.setDataLong(System.currentTimeMillis());

                sendResponseMessage(requestIP, model);
                break;
            }

            case Constants.OPERATION_GET_DATE_TIME_RESPONSE: {
                long zilPanelTimeMillis = comPackageModel.getDataLong();
                Helper.setDeviceTime(getApplicationContext(), zilPanelTimeMillis);

                Helper.setNextAlarm(getApplicationContext(), Helper.getNextAlarmTimeMillis());
                break;
            }

            case Constants.OPERATION_ARAMA_REQUEST: {
                Daire daireRequestCall = comPackageModel.getDaire();
                Guvenlik guvenlikRequestCall = comPackageModel.getGuvenlik();

                boolean isCallerDaire = false;
                if (daireRequestCall != null) {
                    isCallerDaire = true;
                }

                boolean isZilForSite = Helper.isZilForSite(getApplicationContext());

                ZilPanel zilPanelSelf = databaseHelper.getZilPanelByIP(Utils.getIPAddress(true));
                ZilPanelSite zilPanelSiteSelf = databaseHelper.getSiteZilPanelByIP(Utils.getIPAddress(true));
                Log.d(Constants.LOG_TAG, "feyy feyy  noHostDeviceDialogCloseText13");
                if (CallActivity.activityRunning) {
                    //mesgul bilgisi göndermelisin

                    ComPackageModel model = new ComPackageModel();
                    model.setOpe_type(Constants.OPERATION_ARAMA_REQUEST_RESPONSE);
                    if (isZilForSite) {
                        model.setZilPanelSite(zilPanelSiteSelf);
                    } else {
                        model.setZilPanel(zilPanelSelf);
                    }
                    model.setDataBoolean(false);
                    model.setDataInt(Constants.COM_MESSAGE_BUSY);

                    String destinationIP;
                    if (isCallerDaire) {
                        destinationIP = daireRequestCall.getIp();
                    } else {
                        destinationIP = guvenlikRequestCall.getIp();
                    }
                    TCPHelper.sendMessageToIP(getApplicationContext(), destinationIP, model);

                    return;
                }

                MainActivity.lastUserInteractionTime = 0;
                Intent broadcastIntent = new Intent("com.netelsan.panel.KeyPadDayDream");
                sendBroadcast(broadcastIntent);

                ComPackageModel model = new ComPackageModel();
                model.setOpe_type(Constants.OPERATION_ARAMA_REQUEST_RESPONSE);
                model.setDataBoolean(true);
                if (isZilForSite) {
                    model.setZilPanelSite(zilPanelSiteSelf);
                } else {
                    model.setZilPanel(zilPanelSelf);
                }

                sendResponseMessage(requestIP, model);

//            Intent intentStartCall = new Intent(getApplicationContext(), ConnectActivity.class);
                Intent intentStartCall = new Intent(getApplicationContext(), CallActivity.class);
                intentStartCall.putExtra(Constants.PARAM_CALL_RECEIVING, true);
                intentStartCall.putExtra(Constants.PARAM_CALL_IS_YONLENDIRME, false);
                intentStartCall.putExtra(Constants.PARAM_CALL_SS_IDS, new ArrayList<>());
                intentStartCall.putExtra(Constants.PARAM_IS_MULTI_CALL_ACCEPT, false);
                if (isCallerDaire) {
                    intentStartCall.putExtra(Constants.PARAM_CALL_IS_NEED_VIDEO, true);
                    intentStartCall.putExtra("object", daireRequestCall);
                } else {
                    intentStartCall.putExtra(Constants.PARAM_CALL_IS_NEED_VIDEO, false);
                    intentStartCall.putExtra("object", guvenlikRequestCall);
                }

                intentStartCall.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                startActivity(intentStartCall);
                break;
            }

            case Constants.OPERATION_DOOR_UNLOCK: {

                Log.d("Hk","HK door open.....");

                Helper.setDoorLock(getApplicationContext(),true);

                enmDoorUnlock = comPackageModel.getDataInt();

                Intent serviceConnIntent = new Intent(getApplicationContext(), KeyPadService.class);
                bindService(serviceConnIntent, this, Context.BIND_AUTO_CREATE);
                doorUnlockOwnerIP = requestIP;
                break;
            }

            case Constants.OPERATION_ADD_NEW_PASSWORD: {

                DoorPassword doorPasswordNew = comPackageModel.getDoorPassword();

                boolean isRFID = false;
                if (doorPasswordNew.getRfid() != null && !doorPasswordNew.getRfid().equals("")) {
                    isRFID = true;
                }

                DoorPassword doorPasswordControl;
                if (isRFID) {
                    doorPasswordControl = databaseHelper.getPasswordByRFID(doorPasswordNew.getRfid());
                } else {
                    doorPasswordControl = databaseHelper.getPasswordByDoorPassword(doorPasswordNew.getDoor());
                }

                if (doorPasswordControl == null) {
                    databaseHelper.insertPassword(doorPasswordNew);
                } else {
                    databaseHelper.updatePassword(doorPasswordNew);
                }

                break;
            }

            case Constants.OPERATION_DELETE_PASSWORD: {
//                burada gelen veri rfid de olabilir
                DoorPassword doorPasswordForDelete = comPackageModel.getDoorPassword();
                String doorPassword = doorPasswordForDelete.getDoor();
                if (doorPassword == null || doorPassword.isEmpty()) {
                    databaseHelper.deletePasswordByIPAndDoorPassword(doorPasswordForDelete);
                } else {
                    databaseHelper.deletePasswordByIPAndDoorRFID(doorPasswordForDelete);
                }
                break;
            }

            case Constants.OPERATION_REQUEST_DOOR_PASSWORDS: {
                ArrayList<DoorPassword> doorPasswords = databaseHelper.getPasswordsByIP(requestIP);

                ComPackageModel model = new ComPackageModel();
                model.setOpe_type(Constants.OPERATION_REQUEST_DOOR_PASSWORDS_RESPONSE);
                model.setZilPanel(databaseHelper.getZilPanelByIP(Utils.getIPAddress(true)));
                model.setDoorPasswords(doorPasswords);

                sendResponseMessage(requestIP, model);
                break;
            }

            case Constants.OPERATION_IS_DOOR_PASSWORD_SUITABLE: {
                DoorPassword doorPassword = comPackageModel.getDoorPassword();

                DoorPassword doorPasswordTemp = databaseHelper.getPasswordByDoorPassword(doorPassword.getDoor());

                ComPackageModel model = new ComPackageModel();
                model.setOpe_type(Constants.OPERATION_IS_DOOR_PASSWORD_SUITABLE_RESPONSE);
                model.setZilPanel(databaseHelper.getZilPanelByIP(Utils.getIPAddress(true)));

                if (doorPasswordTemp == null) {//suitable
                    model.setDataBoolean(true);
                } else {
                    model.setDataBoolean(false);
                }

                sendResponseMessage(requestIP, model);
                break;
            }

            case Constants.OPERATION_REQUEST_DEVICE_INFOS: {
                ZilPanel zilPanelSelf = databaseHelper.getZilPanelByIP(Utils.getIPAddress(true));

                Daire daire = comPackageModel.getDaire();
                Guvenlik guvenlik = comPackageModel.getGuvenlik();
                ZilPanelSite zilPanelSite = comPackageModel.getZilPanelSite();

                ArrayList<ZilPanel> zilPanels = databaseHelper.getZilPanelleri(true);
                ArrayList<Daire> daires = databaseHelper.getDaireler();
                ArrayList<Guvenlik> guvenliks = databaseHelper.getGuvenlikler();

                ArrayList<IPCamera> ipCameras = databaseHelper.getIPCameras();
                ArrayList<ZilPanelSite> siteZilPanels = databaseHelper.getSiteZilPanelleri(true);

                String destinationIP = null;
                if (daire != null) {// sadece daireye cevap dön

                    ArrayList<Daire> dairesForRequest = new ArrayList<>();
                    dairesForRequest.add(daire);
                    Log.d(Constants.LOG_TAG, "fey yeni gelen daire saveDairesToDBsaveDairesToDB3");
                    saveDairesToDB(databaseHelper, dairesForRequest);

                    destinationIP = daire.getIp();
                }

                if (guvenlik != null) {//sadece güvenliğe cevap dön

                    ArrayList<Guvenlik> guvenliksForRequest = new ArrayList<>();
                    guvenliksForRequest.add(guvenlik);
                    saveGuvenliksToDB(databaseHelper, guvenliksForRequest);

                    destinationIP = guvenlik.getIp();
                }

                if (zilPanelSite != null) {//sadece site paneline cevap dön

                    ZilPanelSite zilPanelSiteLocal = databaseHelper.getSiteZilPanelByIP(zilPanelSite.getIp());
                    if (zilPanelSiteLocal == null) {
                        databaseHelper.insertSiteZilPanel(zilPanelSite);
                    } else {
                        databaseHelper.updateSiteZilPanel(zilPanelSite);
                    }

                    destinationIP = zilPanelSite.getIp();
                }

//                buradaki mantığı düzelt

                if (destinationIP == null) {
                    return;
                }

                ComPackageModel model = new ComPackageModel();
                model.setOpe_type(Constants.OPERATION_REQUEST_DEVICE_INFOS_RESPONSE);
                model.setZilPanel(zilPanelSelf);
                model.setZilPanels(zilPanels);
                model.setDaires(daires);
                model.setGuvenliks(guvenliks);
                //feyhsn
                if(daire.getBlok()==zilPanelSelf.getBlok())
                model.setIpCameras(ipCameras);
                model.setZilPanelSites(siteZilPanels);

                TCPHelper.sendMessageToIP(getApplicationContext(), destinationIP, model);

//                ArrayList<Daire> dairesInSameBuilding = databaseHelper.getDairelerInSameApartment(zilPanelSelf.getBlok());
//                Iterator<Daire> iterator = dairesInSameBuilding.iterator();
//                while (iterator.hasNext()) {
//                    Daire daireForInfo = iterator.next();
//                    String destinationIP = daireForInfo.getIp();
//
//                    ComPackageModel model = new ComPackageModel();
//                    model.setOpe_type(Constants.OPERATION_REQUEST_DEVICE_INFOS_RESPONSE);
//                    model.setZilPanel(zilPanelSelf);
//                    model.setZilPanels(zilPanels);
//                    model.setDaires(daires);
//                    model.setGuvenliks(guvenliks);
//                    model.setIpCameras(ipCameras);
//                    model.setZilPanelSites(siteZilPanels);
//
//                    TCPHelper.sendMessageToIP(getApplicationContext(), destinationIP, model);
//
//                }

//                ArrayList<Guvenlik> guvenliksForUpdate = databaseHelper.getGuvenlikler();
//                Iterator<Guvenlik> iteratorGuvenlik = guvenliksForUpdate.iterator();
//                while (iteratorGuvenlik.hasNext()) {
//                    Guvenlik guvenlikTemp = iteratorGuvenlik.next();
//                    String destinationIP = guvenlikTemp.getIp();
//
//                    ComPackageModel model = new ComPackageModel();
//                    model.setOpe_type(Constants.OPERATION_REQUEST_DEVICE_INFOS_RESPONSE);
//                    model.setZilPanel(zilPanelSelf);
//                    model.setZilPanels(zilPanels);
//                    model.setDaires(daires);
//                    model.setGuvenliks(guvenliks);
//                    model.setIpCameras(ipCameras);
//                    model.setZilPanelSites(siteZilPanels);
//
//                    TCPHelper.sendMessageToIP(getApplicationContext(), destinationIP, model);
//                }

//                ArrayList<ZilPanelSite> siteZilPanelsForUpdate = databaseHelper.getSiteZilPanelleri(true);
//                Iterator<ZilPanelSite> iteratorzilPanelSite = siteZilPanelsForUpdate.iterator();
//                while (iteratorzilPanelSite.hasNext()) {
//                    ZilPanelSite zilPanelSiteTemp = iteratorzilPanelSite.next();
//                    String destinationIP = zilPanelSiteTemp.getIp();
//
//                    ComPackageModel model = new ComPackageModel();
//                    model.setOpe_type(Constants.OPERATION_REQUEST_DEVICE_INFOS_RESPONSE);
//                    model.setZilPanel(zilPanelSelf);
//                    model.setZilPanels(zilPanels);
//                    model.setDaires(daires);
//                    model.setGuvenliks(guvenliks);
//                    model.setIpCameras(ipCameras);
//                    model.setZilPanelSites(siteZilPanels);
//
//                    TCPHelper.sendMessageToIP(getApplicationContext(), destinationIP, model);
//                }

                break;
            }

            case Constants.OPERATION_REQUEST_DEVICE_INFOS_RESPONSE: {
                ArrayList<ZilPanel> zilPanels = comPackageModel.getZilPanels();
                ArrayList<Daire> daires = comPackageModel.getDaires();
                ArrayList<Guvenlik> guvenliks = comPackageModel.getGuvenliks();
                ArrayList<ZilPanelSite> siteZilPanels = comPackageModel.getZilPanelSites();

                saveZilPanelsToDB(databaseHelper, zilPanels);
                Log.d(Constants.LOG_TAG, "fey yeni gelen daire saveDairesToDBsaveDairesToDB4");
                saveDairesToDB(databaseHelper, daires);
                saveGuvenliksToDB(databaseHelper, guvenliks);


                break;
            }

            case Constants.OPERATION_ADB_STATE: {
                boolean isADBEnabled = comPackageModel.getDataBoolean();
                try {
                    if (isADBEnabled) {
                        Settings.Secure.putInt(getContentResolver(), Settings.Secure.ADB_ENABLED, 1);
                    } else {
                        Settings.Secure.putInt(getContentResolver(), Settings.Secure.ADB_ENABLED, 0);
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
                break;
            }

            case Constants.OPERATION_HANDSHAKE_ZIL_PANEL_SITE: {

                ZilPanelSite zilPanelSite = comPackageModel.getZilPanelSite();

                ZilPanelSite zilPanelSiteLocal = databaseHelper.getSiteZilPanelByIP(zilPanelSite.getIp());
                if (zilPanelSiteLocal == null) {
                    databaseHelper.insertSiteZilPanel(zilPanelSite);
                } else {
                    databaseHelper.updateSiteZilPanel(zilPanelSite);
                }

                ArrayList<ZilPanel> zilPanels = databaseHelper.getZilPanelleri(true);


                //feyyaz
                ZilPanel zilPanelSelf = databaseHelper.getZilPanelByIP(Utils.getIPAddress(true));
                int blokNo = zilPanelSelf.getBlok();


                ArrayList<Daire> daires = databaseHelper.getDairelerForBlok(blokNo);

                //buraya kadar


                ArrayList<Guvenlik> guvenliks = databaseHelper.getGuvenlikler();

                ComPackageModel model = new ComPackageModel();
                model.setOpe_type(Constants.OPERATION_HANDSHAKE_ZIL_PANEL_SITE_RESPONSE);
                model.setZilPanels(zilPanels);
                model.setDaires(daires);
                model.setGuvenliks(guvenliks);
                model.setDataLong(System.currentTimeMillis());

//                rfid kapi şifrelerini de koy içine
                ArrayList<DoorPassword> doorPasswordsRFID = databaseHelper.getPasswords(true);
                model.setDoorPasswords(doorPasswordsRFID);

                //burada site zil panelinde kullanılacak şifreyi belirleyip gönder
                String blokPasswordForSiteZilPanel = getBlokPasswordForSiteZilPanel(databaseHelper);
                model.setDataString(blokPasswordForSiteZilPanel);

                TCPHelper.sendMessageToIP(getApplicationContext(), requestIP, model);

                sendNewZilPanelSiteAddedToDairesInSameBuilding(databaseHelper, zilPanelSite);
                sendNewZilPanelSiteAddedToGuvenliks(databaseHelper, zilPanelSite);

                break;
            }

            case Constants.OPERATION_HANDSHAKE_ZIL_PANEL_SITE_RESPONSE: {
                ArrayList<ZilPanel> zilPanels = comPackageModel.getZilPanels();
                ArrayList<Daire> daires = comPackageModel.getDaires();
                ArrayList<Guvenlik> guvenliks = comPackageModel.getGuvenliks();
                ArrayList<DoorPassword> doorPasswordsRFID = comPackageModel.getDoorPasswords();

                saveZilPanelsToDB(databaseHelper, zilPanels);

                ZilPanel zilPanel = databaseHelper.getZilPanelByIP(requestIP);
                if (blokKontrolList.indexOf(zilPanel.getBlok()) == -1) {
                    Log.d(Constants.LOG_TAG, "fey yeni gelen daire saveDairesToDBsaveDairesToDB5");
                    blokKontrolList.add(zilPanel.getBlok());
                    saveDairesToDB(databaseHelper, daires);
                }

                saveGuvenliksToDB(databaseHelper, guvenliks);
                saveDoorPasswordsToDB(databaseHelper, doorPasswordsRFID);

                long timeMillis = comPackageModel.getDataLong();
                Helper.setDeviceTime(getApplicationContext(), timeMillis);

                Helper.setNextAlarm(getApplicationContext(), Helper.getNextAlarmTimeMillis());

                String passwordForSiteZil = comPackageModel.getDataString();
                addKapiPasswordIfNeeded(databaseHelper, passwordForSiteZil, requestIP);
                break;
            }

            case Constants.OPERATION_NEW_GUVENLIK_ADDED: {
                Guvenlik guvenlikNew = comPackageModel.getGuvenlik();

                Guvenlik guvenlikLocal = databaseHelper.getGuvenlikByIP(guvenlikNew.getIp());
                if (guvenlikLocal == null) {
                    databaseHelper.insertGuvenlik(guvenlikNew);
                } else {
                    databaseHelper.updateGuvenlik(guvenlikNew);
                }
                break;
            }

            case Constants.OPERATION_TEKNIK_PERSONEL_SIFRE_STATE: {
                boolean isTeknikPersonelSifreEnabled = comPackageModel.getDataBoolean();
                Helper.setTeknikPersonelSifreEnabled(getApplicationContext(), isTeknikPersonelSifreEnabled);
                Log.d("HK","HK isteknik: " + isTeknikPersonelSifreEnabled);
                break;
            }

            case Constants.OPERATION_CLOSE_TRIGGERED_ALARM: {
                Intent broadcastIntent = new Intent("com.netelsan.CLOSE_ALARM_FROM_GUVENLIK");
                sendBroadcast(broadcastIntent);
                break;
            }
      /*     //sinan beyin yapöış olduğu bağlantı kontrol yapısı
            case Constants.OPERATION_CHECK_CONNECTION: {
                ZilPanel zilPanelSelf = databaseHelper.getZilPanelByIP(Utils.getIPAddress(true));
                ComPackageModel model = new ComPackageModel();
                model.setOpe_type(Constants.OPERATION_CHECK_CONNECTION_RESPONSE);
                model.setDataBoolean(true);
                model.setZilPanel(zilPanelSelf);
                TCPHelper.sendMessageToIP(getApplicationContext(), requestIP, model);
                break;
            }*/

            case Constants.OPERATION_DAIRELER_RESETLENDI: {
                int blokNo = comPackageModel.getDataInt();
                databaseHelper.deleteDairesWithBlokNo(blokNo);
                break;
            }
            case Constants.OPERATION_CHECK_CONNECTION: {

                Daire daireRequest = comPackageModel.getDaire();
                Guvenlik guvenlikRequest=comPackageModel.getGuvenlik();

                ComPackageModel model = new ComPackageModel();

                String destinationIP="";
                if (daireRequest != null) {
                    destinationIP = daireRequest.getIp();
                }
                if (guvenlikRequest != null) {
                    destinationIP = guvenlikRequest.getIp();
                }

                model.setOpe_type(Constants.OPERATION_CHECK_CONNECTION_RESPONSE);
                model.setServerControlHour(comPackageModel.getServerControlHour());
                model.setServerControlMinute(comPackageModel.getServerControlMinute());


                if(!destinationIP.equals("")) {
                    TCPHelper.sendMessageToIP(getApplicationContext(), destinationIP, model);
                }

                break;
            }
            case Constants.OPERATION_CHECK_CONNECTION_RESPONSE: {

                serverGeciciHour = comPackageModel.getServerControlHour();
                serverGeciciMinute = comPackageModel.getServerControlMinute();
                break;
            }



            default:
                break;
        }

    }

    private void saveDoorPasswordsToDB(DatabaseHelper databaseHelper, ArrayList<DoorPassword> doorPasswordsRFID) {

        Iterator<DoorPassword> iterator = doorPasswordsRFID.iterator();
        while (iterator.hasNext()) {
            DoorPassword doorPassword = iterator.next();

            DoorPassword doorPasswordLocal = databaseHelper.getPasswordByRFID(doorPassword.getRfid());
            if (doorPasswordLocal == null) {
                databaseHelper.insertPassword(doorPassword);
            } else {
                databaseHelper.updatePassword(doorPassword);
            }
        }

    }

    private void addKapiPasswordIfNeeded(DatabaseHelper databaseHelper, String passwordForSiteZil, String requestIP) {

//        Log.d(Constants.LOG_TAG, "addKapiPasswordIfNeeded passwordForSiteZil=" + passwordForSiteZil);

        ZilPanel zilPanel = databaseHelper.getZilPanelByIP(requestIP);

        String passwordLabel = "";
        if (zilPanel != null) {
            passwordLabel = getDoorPasswordLabel(zilPanel);
        }

        DoorPassword doorPassword = databaseHelper.getPasswordByDoorPassword(passwordForSiteZil);

        if (doorPassword == null) {
            doorPassword = new DoorPassword();
            doorPassword.setDoor(passwordForSiteZil);
            doorPassword.setActive(true);
            doorPassword.setIp(requestIP);
            doorPassword.setPasswordLabel(passwordLabel);

            databaseHelper.insertPassword(doorPassword);
        }

    }

    private String getDoorPasswordLabel(ZilPanel zilPanel) {
        return getString(R.string.blok) + " " + zilPanel.getBlok() + "  " + getString(R.string.kapi) + " " + zilPanel.getKapiNo();
    }

    private String getBlokPasswordForSiteZilPanel(DatabaseHelper databaseHelper) {

//        String passwordForSiteZil = selfIP.replaceAll("\\.", "");
////        Log.d(Constants.LOG_TAG, "getBlokPasswordForSiteZilPanel passwordForSiteZil=" + passwordForSiteZil);
//        int totalLength = passwordForSiteZil.length();
//        passwordForSiteZil = passwordForSiteZil.substring(totalLength - 4, totalLength);
//        Log.d(Constants.LOG_TAG, "getBlokPasswordForSiteZilPanel passwordForSiteZil 2=" + passwordForSiteZil);

        String selfIP = Utils.getIPAddress(true);
        ZilPanel zilPanelSelf = databaseHelper.getZilPanelByIP(selfIP);

        int blokNo = zilPanelSelf.getBlok();// 3 haneli olabilir
        int kapiNo = zilPanelSelf.getKapiNo();// 3 haneli olabilir

        String passwordForSiteZil = blokNo + "" + kapiNo;
        int totalLength = passwordForSiteZil.length();

        if (totalLength > 4) {
            passwordForSiteZil = passwordForSiteZil.substring(totalLength - 4, totalLength);
        } else {
            if (totalLength == 3) {
                passwordForSiteZil = passwordForSiteZil + "" + kapiNo;
            } else if (totalLength == 2) {
                passwordForSiteZil = blokNo + "" + kapiNo + "" + kapiNo + "" + blokNo;
            }

        }

        return passwordForSiteZil;

        // 4 haneli bir şifre oluşturmalısın
        // ip nin son 4 rakamını şifre olarak belirledik

    }

    private void sendNewZilPanelSiteAddedToGuvenliks(DatabaseHelper databaseHelper, ZilPanelSite zilPanelSite) {

        ArrayList<Guvenlik> guvenliks = databaseHelper.getGuvenlikler();
        Iterator<Guvenlik> iterator = guvenliks.iterator();
        while (iterator.hasNext()) {
            Guvenlik guvenlik = iterator.next();
            String destinationIP = guvenlik.getIp();

            if (destinationIP == null || destinationIP.isEmpty()) {
                continue;
            }
            ComPackageModel model = new ComPackageModel();
            model.setOpe_type(Constants.OPERATION_HANDSHAKE_ZIL_PANEL_SITE);
            model.setZilPanelSite(zilPanelSite);

            TCPHelper.sendMessageToIP(getApplicationContext(), destinationIP, model);

        }

    }

    private void sendNewZilPanelSiteAddedToDairesInSameBuilding(DatabaseHelper databaseHelper, ZilPanelSite zilPanelSite) {

        ZilPanel zilPanelSelf = databaseHelper.getZilPanelByIP(Utils.getIPAddress(true));

        ArrayList<Daire> daires = databaseHelper.getDairelerInSameApartment(zilPanelSelf.getBlok());
        Iterator<Daire> iterator = daires.iterator();
        while (iterator.hasNext()) {
            Daire daire = iterator.next();
            String destinationIP = daire.getIp();

            ComPackageModel model = new ComPackageModel();
            model.setOpe_type(Constants.OPERATION_HANDSHAKE_ZIL_PANEL_SITE);
            model.setZilPanelSite(zilPanelSite);

            TCPHelper.sendMessageToIP(getApplicationContext(), destinationIP, model);
        }

    }

    private void sendNewCameraAddedToDairesInSameBuilding(DatabaseHelper databaseHelper, IPCamera newIPCamera) {

        ZilPanel zilPanelSelf = databaseHelper.getZilPanelByIP(Utils.getIPAddress(true));

        ArrayList<Daire> daires = databaseHelper.getDairelerInSameApartment(zilPanelSelf.getBlok());

        Iterator<Daire> iterator = daires.iterator();
        while (iterator.hasNext()) {
            Daire daire = iterator.next();
            String destinationIP = daire.getIp();

            ComPackageModel model = new ComPackageModel();
            model.setOpe_type(Constants.OPERATION_NEW_IP_CAMERA_ADDED);
            model.setIpCamera(newIPCamera);

            TCPHelper.sendMessageToIP(getApplicationContext(), destinationIP, model);
        }

    }

    private void sendNewCameraRemovedToDairesInSameBuilding(DatabaseHelper databaseHelper, IPCamera newIPCamera) {

        ZilPanel zilPanelSelf = databaseHelper.getZilPanelByIP(Utils.getIPAddress(true));

        ArrayList<Daire> daires = databaseHelper.getDairelerInSameApartment(zilPanelSelf.getBlok());

        Iterator<Daire> iterator = daires.iterator();
        while (iterator.hasNext()) {
            Daire daire = iterator.next();
            String destinationIP = daire.getIp();

            ComPackageModel model = new ComPackageModel();
            model.setOpe_type(Constants.OPERATION_NEW_IP_CAMERA_REMOVED);
            model.setIpCamera(newIPCamera);

            TCPHelper.sendMessageToIP(getApplicationContext(), destinationIP, model);
        }

    }

    private void sendNewDaireAddedToGuvenlikler(Daire newDaire, ArrayList<Guvenlik> guvenlikler) {

        Iterator<Guvenlik> iterator = guvenlikler.iterator();
        while (iterator.hasNext()) {
            Guvenlik guvenlik = iterator.next();
            String destinationIP = guvenlik.getIp();

            ComPackageModel model = new ComPackageModel();
            model.setDaire(newDaire);
            model.setOpe_type(Constants.OPERATION_ADD_NEW_DAIRE);

            TCPHelper.sendMessageToIP(getApplicationContext(), destinationIP, model);
        }

    }

    private void sendNewGuvenlikAddedToSiteZilPanels(Guvenlik newGuvenlik, ArrayList<ZilPanelSite> arrayListSiteZilPanels) {

        Iterator<ZilPanelSite> iterator = arrayListSiteZilPanels.iterator();
        while (iterator.hasNext()) {
            ZilPanelSite zilPanelSite = iterator.next();
            String destinationIp = zilPanelSite.getIp();

            ComPackageModel model = new ComPackageModel();
            model.setOpe_type(Constants.OPERATION_NEW_GUVENLIK_ADDED);
            model.setGuvenlik(newGuvenlik);

            TCPHelper.sendMessageToIP(getApplicationContext(), destinationIp, model);

        }

    }

    private void sendNewGuvenlikAddedToGuvenliks(Guvenlik newGuvenlik, ArrayList<Guvenlik> arrayListGuvenliks) {

        Iterator<Guvenlik> iterator = arrayListGuvenliks.iterator();
        while (iterator.hasNext()) {
            Guvenlik guvenlik = iterator.next();
            String destinationIp = guvenlik.getIp();

            ComPackageModel model = new ComPackageModel();
            model.setOpe_type(Constants.OPERATION_NEW_GUVENLIK_ADDED);
            model.setGuvenlik(newGuvenlik);

            TCPHelper.sendMessageToIP(getApplicationContext(), destinationIp, model);

        }

    }

    private void sendNewGuvenlikAddedToDaires(Guvenlik guvenlik, ArrayList<Daire> arrayListDaires) {

        Iterator<Daire> iterator = arrayListDaires.iterator();
        while (iterator.hasNext()) {
            Daire daire = iterator.next();
            String destinationIp = daire.getIp();

            ComPackageModel model = new ComPackageModel();
            model.setOpe_type(Constants.OPERATION_NEW_GUVENLIK_ADDED);
            model.setGuvenlik(guvenlik);

            TCPHelper.sendMessageToIP(getApplicationContext(), destinationIp, model);

        }

    }

    public KeyPadService keyPadService;
    String doorUnlockOwnerIP = null;
    int enmDoorUnlock;

    @Override
    public void onServiceConnected(ComponentName name, IBinder service) {

        if (doorUnlockOwnerIP != null) {
            DatabaseHelper databaseHelper = new DatabaseHelper(getApplicationContext());

            Calendar calendar = Calendar.getInstance();
            SimpleDateFormat dateFormat = new SimpleDateFormat("dd.MM.yyyy HH:mm:ss");
            String formattedDate = dateFormat.format(calendar.getTime());

            DoorUnlockLog doorUnlockLog = new DoorUnlockLog();
            doorUnlockLog.setUnlockType(Constants.DOOR_UNLOCK_DAIRE);
            doorUnlockLog.setDatetime(formattedDate);
            doorUnlockLog.setRfid("");
            doorUnlockLog.setDoorPassword("");
            doorUnlockLog.setPasswordLabel(getString(R.string.daire_tarafindan));

            doorUnlockLog.setPasswordOwnerIP(doorUnlockOwnerIP);
            databaseHelper.insertDoorLog(doorUnlockLog);
            doorUnlockOwnerIP = null;
        }

        keyPadService = ((KeyPadService.MyBinder) service).getService();
        keyPadService.sendDoorUnlock(enmDoorUnlock);
//        if (isForMainDoor){
//            keyPadService.sendDoorUnlock(Constants.ENUM_DOOR_UNLOCK_MAIN);
//        }else{
//            keyPadService.sendDoorUnlock(Constants.ENUM_DOOR_UNLOCK_PEDESTRAIN);
//        }

        unbindService(this);

    }

    @Override
    public void onServiceDisconnected(ComponentName name) {
        keyPadService = null;
    }

//    private boolean isPanelsAddedBefore(DatabaseHelper databaseHelper) {
//
//        ArrayList<ZilPanel> zilPanelsLocal = databaseHelper.getZilPanelleri(false);
//
//        if(zilPanelsLocal.size() == 0) {
//            return false;
//        } else {
//            return true;
//        }
//
//    }

//    private boolean isDairesAddedBefore(DatabaseHelper databaseHelper) {
//
//        ArrayList<Daire> dairesLocal = databaseHelper.getDaireler(false);
//
//        if(dairesLocal.size() == 0) {
//            return false;
//        } else {
//            return true;
//        }
//
//    }


    private void saveZilPanelsToDB(DatabaseHelper databaseHelper, ArrayList<ZilPanel> zilPanels) {

        Iterator<ZilPanel> iterator = zilPanels.iterator();
        while (iterator.hasNext()) {
            ZilPanel zilPanel = iterator.next();

            ZilPanel zilPanelLocal = databaseHelper.getZilPanelByIP(zilPanel.getIp());
            if (zilPanelLocal == null) {
                databaseHelper.insertZilPanel(zilPanel);
            } else {
                databaseHelper.updateZilPanel(zilPanel);
            }
        }

    }
    //           saveZilpanelsToDB
    //       saveDairesToDB
    //                                  saveGuvenliksToDB
    //       saveIPCamerasToDB
    //         saveSiteZilPanelsToDB

    public static int saveDairesToDBKontrol = 0;

    private void saveDairesToDB(DatabaseHelper databaseHelper, ArrayList<Daire> daires) {

        if (saveDairesToDBKontrol > 0) {
            return;
        }

        Iterator<Daire> iterator = daires.iterator();
        while (iterator.hasNext()) {
            Daire daire = iterator.next();


            Daire daireLocal = databaseHelper.getDaireByIP(daire.getIp());
            if (daireLocal == null) {

                //feyyyyy
                /*try {
                    Thread.sleep(500);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }*/
                Boolean selectedDaireKontrol = databaseHelper.daireKontrolByIP(daire.getIp());
                if (selectedDaireKontrol == false) {
                    Log.d(Constants.LOG_TAG, "fey yeni gelen daire saveDairesToDBKontrol=" + saveDairesToDBKontrol + "daire.getIp()=" + daire.getIp());
                    databaseHelper.insertDaire(daire);
                }
            } else {
                databaseHelper.updateDaire(daire);
            }


        }
        saveDairesToDBKontrol++;

    }

    private void saveGuvenliksToDB(DatabaseHelper databaseHelper, ArrayList<Guvenlik> guvenliks) {

        Iterator<Guvenlik> iterator = guvenliks.iterator();
        while (iterator.hasNext()) {
            Guvenlik guvenlik = iterator.next();

            Guvenlik guvenlikLocal = databaseHelper.getGuvenlikByIP(guvenlik.getIp());
            if (guvenlikLocal == null) {
                databaseHelper.insertGuvenlik(guvenlik);
            } else {
                databaseHelper.updateGuvenlik(guvenlik);
            }
        }

    }

    private void sendResponseMessage(String destinationIP, ComPackageModel model) {
        TCPHelper.sendMessageToIP(getApplicationContext(), destinationIP, model);
    }

    private void sendNewZilPanelAddedToOtherDaires(DatabaseHelper databaseHelper, ZilPanel newZilPanel) {

        ZilPanel zilPanelSelf = databaseHelper.getZilPanelByIP(Utils.getIPAddress(true));
        int blokNo = zilPanelSelf.getBlok();

        ArrayList<Daire> arrayListDaireler = databaseHelper.getDairelerInSameApartment(blokNo);

        sendNewZilPanelAddedRecursive(arrayListDaireler, newZilPanel);

    }

    private void sendNewZilPanelAddedRecursive(ArrayList<Daire> arrayList, ZilPanel newZilPanel) {

        Iterator<Daire> iterator = arrayList.iterator();
        while (iterator.hasNext()) {
            Daire daire = iterator.next();
            String destinationIP = daire.getIp();

            ComPackageModel model = new ComPackageModel();
            model.setOpe_type(Constants.OPERATION_HANDSHAKE_ZIL_PANEL);
            model.setZilPanel(newZilPanel);
            model.setNeedResponse(false);

            TCPHelper.sendMessageToIP(getApplicationContext(), destinationIP, model);
        }

    }

    private void sendNewDaireAddedToOtherDairesInSameBuilding(DatabaseHelper databaseHelper, Daire newDaire) {

        ZilPanel zilPanelSelf = databaseHelper.getZilPanelByIP(Utils.getIPAddress(true));

        int blokNo = zilPanelSelf.getBlok();
        ArrayList<Daire> arrayListDaireler = databaseHelper.getDairelerInSameApartment(blokNo);
        Log.d(Constants.LOG_TAG, "1907 sendNewDaireAddedToOtherDaires arrayListDaireler=" + arrayListDaireler.size());

        Iterator<Daire> iterator = arrayListDaireler.iterator();
        while (iterator.hasNext()) {
            Daire daire = iterator.next();

            Log.d(Constants.LOG_TAG, "1907 sendNewDaireAddedRecursive daire=" + daire.getIp());

            if (daire.getIp().equals(newDaire.getIp())) {
                continue;
//                sendNewDaireAddedRecursive(arrayList, arrayIndex + 1, newDaire, databaseHelper, isNeedGlobalBroadcast);
            }

            String destinationIP = daire.getIp();

            ComPackageModel model = new ComPackageModel();
            model.setOpe_type(Constants.OPERATION_ADD_NEW_DAIRE);
            model.setDaire(newDaire);

            TCPHelper.sendMessageToIP(getApplicationContext(), destinationIP, model);

        }

    }

    @Override
    public void onTaskRemoved(Intent rootIntent) {
        Log.i(Constants.LOG_TAG, "onTaskRemoved()");

        // workaround for kitkat: set an alarm service to trigger service again
        Intent intent = new Intent(getApplicationContext(), Server.class);
        PendingIntent pendingIntent = PendingIntent.getService(this, 1, intent, PendingIntent.FLAG_ONE_SHOT);
        AlarmManager alarmManager = (AlarmManager) getSystemService(Context.ALARM_SERVICE);
        alarmManager.set(AlarmManager.RTC_WAKEUP, SystemClock.elapsedRealtime() + 5000, pendingIntent);

        super.onTaskRemoved(rootIntent);

    }

    @Override
    public void onLowMemory() {
        super.onLowMemory();
        Log.i(Constants.LOG_TAG, "onLowMemory()");
    }


}
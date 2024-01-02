package com.netelsan.ipinterkompanel.tcp;

import android.app.AlarmManager;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

import com.google.gson.Gson;
import com.netelsan.ipinterkompanel.Constants;
import com.netelsan.ipinterkompanel.Helper;
import com.netelsan.ipinterkompanel.R;
import com.netelsan.ipinterkompanel.database.DatabaseHelper;
import com.netelsan.ipinterkompanel.dialog_activity.NoHostDeviceActivity;
import com.netelsan.ipinterkompanel.listener.TCPIPListener;
import com.netelsan.ipinterkompanel.model.ComPackageModel;
import com.netelsan.ipinterkompanel.model.Daire;
import com.netelsan.ipinterkompanel.model.ZilPanel;
import com.scottyab.aescrypt.AESCrypt;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.net.ConnectException;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.SocketAddress;
import java.net.SocketTimeoutException;
import java.security.GeneralSecurityException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Iterator;

import static com.netelsan.ipinterkompanel.tcp.Server.SERVERPORT;

public class TCPHelper {

    public static void sendMessageToIP(Context context, String serverIP, ComPackageModel model) {

        ClientThread clientThread = new ClientThread(context, serverIP, model);
        new Thread(clientThread).start();

    }

    static class ClientThread implements Runnable {

        String serverIP;
        ComPackageModel model;

        Context context;

        public ClientThread(Context context, String serverIP, ComPackageModel model) {
            this.context = context;
            this.serverIP = serverIP;
            this.model = model;
        }

        @Override
        public void run() {

            try {
                InetAddress serverAddr = InetAddress.getByName(serverIP);

                Socket socket = new Socket(serverAddr, SERVERPORT);

                Gson gson = new Gson();
                String decryptedMsg = gson.toJson(model);

                String encryptedMsg = null;
                try {
                    encryptedMsg = AESCrypt.encrypt(Constants.ENCRYPTION_PASSWORD, decryptedMsg);
                } catch(GeneralSecurityException e) {
                    //handle error
                }

//                Log.d(Constants.LOG_TAG, "ClientThread encryptedMsg=" + encryptedMsg);
                if(encryptedMsg == null) {
                    return;
                }

                OutputStream outputStream = socket.getOutputStream();
                OutputStreamWriter outputStreamWriter = new OutputStreamWriter(outputStream);
                BufferedWriter bufferedWriter = new BufferedWriter(outputStreamWriter);
                PrintWriter out = new PrintWriter(bufferedWriter, true);
                out.println(encryptedMsg);

                bufferedWriter.flush();
                bufferedWriter.close();

                out.flush();
                out.close();

                socket.close();

            } catch(Exception e) {

                if(e instanceof ConnectException) {
                    showNoHostDeviceDialog(context, model);
                }

//                e.printStackTrace();
            }

        }

    }

    private static void showNoHostDeviceDialog(Context context, ComPackageModel model) {

        Intent intentYonlendirme = new Intent("com.netelsan.YONLENDIRME_STATE");
        intentYonlendirme.putExtra("isReachable", false);
        context.sendBroadcast(intentYonlendirme);

        int opeType = model.getOpe_type();

        String title = "";
        String subtitle = "";

//        Log.d(Constants.LOG_TAG, "showNoHostDeviceDialog opeType=" + opeType);

        if(opeType == Constants.OPERATION_REQUEST_YONLENDIRME) {
            title = context.getString(R.string.aradiginiz_cihaz_ulasilmaz_durumda);
            subtitle = context.getString(R.string.daha_sonra_tekrar_deneyin);
        } else if(opeType == Constants.OPERATION_MESSAGE) {
            title = context.getString(R.string.mesaj_cihaz_ulasilmaz_durumda);
            subtitle = context.getString(R.string.daha_sonra_tekrar_deneyin);
        } else {
            return;
        }

        Intent intent = new Intent(context, NoHostDeviceActivity.class);
        intent.putExtra("title", title);
        intent.putExtra("subtitle", subtitle);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_EXCLUDE_FROM_RECENTS);
        context.startActivity(intent);

    }

    private static boolean isPanelsAddedBefore(DatabaseHelper databaseHelper) {

        ArrayList<ZilPanel> zilPanelsLocal = databaseHelper.getZilPanelleri(false);

        if(zilPanelsLocal.size() == 0) {
            return false;
        } else {
            return true;
        }

    }


//    private static boolean isDairesAddedBefore(DatabaseHelper databaseHelper) {
//
//        ArrayList<Daire> dairesLocal = databaseHelper.getDaireler();
//
//        if(dairesLocal.size() == 0) {
//            return false;
//        } else {
//            return true;
//        }
//
//    }

//    private static void saveZilPanelsToDB(DatabaseHelper databaseHelper, ArrayList<ZilPanel> zilPanels) {
//
//        Iterator<ZilPanel> iterator = zilPanels.iterator();
//        while(iterator.hasNext()) {
//            ZilPanel zilPanel = iterator.next();
//
//            ZilPanel zilPanelLocal = databaseHelper.getZilPanelByIP(zilPanel.getIp());
//            if(zilPanelLocal == null) {
//                databaseHelper.insertZilPanel(zilPanel);
//            } else {
//                databaseHelper.updateZilPanel(zilPanel);
//            }
//        }
//
//    }

    private static void saveDairesToDB(DatabaseHelper databaseHelper, ArrayList<Daire> daires) {

        Iterator<Daire> iterator = daires.iterator();
        while(iterator.hasNext()) {
            Daire daire = iterator.next();

            Daire daireLocal = databaseHelper.getDaireByIP(daire.getIp());
            if(daireLocal == null) {
                databaseHelper.insertDaire(daire);
            } else {
                databaseHelper.updateDaire(daire);
            }
        }

    }

    private static void setDeviceTime(Context context, long zilPanelTimeMillis) {

        Calendar calendar = Calendar.getInstance();
        calendar.setTimeInMillis(zilPanelTimeMillis);

        AlarmManager am = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        am.setTimeZone("Europe/Istanbul");

        setDeviceTime(calendar);
    }

    private static void setDeviceTime(Calendar calendar) {

        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyyMMdd.HHmmss");
        String formattedDate = dateFormat.format(calendar.getTime());
//        Log.d(Constants.LOG_TAG, "formattedDate=" + formattedDate);

        try {
//            Process process = Runtime.getRuntime().exec("su 0 toolbox date -s 20191015.084000");//YYYYMMDD.HHmmss
            Process process = Runtime.getRuntime().exec("su 0 toolbox date -s " + formattedDate);//YYYYMMDD.HHmmss
            BufferedReader in = new BufferedReader(new InputStreamReader(process.getInputStream()));

//            refreshDateAndTime(calendar);

        } catch(Exception e) {
            e.printStackTrace();
        }

    }

    public static boolean isSocketAlive() {

        String hostName = Utils.getIPAddress(true);
        int port = Server.SERVERPORT;

        boolean isAlive = false;

        // Creates a socket address from a hostname and a port number
        SocketAddress socketAddress = new InetSocketAddress(hostName, port);
        Socket socket = new Socket();

        // Timeout required - it's in milliseconds
        int timeout = 2000;

        Log.d(Constants.LOG_TAG, "hostName: " + hostName + ", port: " + port);
        try {
            socket.connect(socketAddress, timeout);
            socket.close();
            isAlive = true;
        } catch(SocketTimeoutException exception) {
            System.out.println("SocketTimeoutException " + hostName + ":" + port + ". " + exception.getMessage());
        } catch(IOException exception) {
            System.out.println("IOException - Unable to connect to " + hostName + ":" + port + ". " + exception.getMessage());
        }

        return isAlive;
    }

}


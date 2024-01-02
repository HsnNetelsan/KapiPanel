package com.netelsan.ipinterkompanel.activity;

import android.app.ActivityManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Point;
import android.media.MediaCodec;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.Display;
import android.view.View;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.netelsan.ipinterkompanel.Constants;
import com.netelsan.ipinterkompanel.Helper;

import com.netelsan.ipinterkompanel.database.DatabaseHelper;
import com.netelsan.ipinterkompanel.model.ComPackageModel;
import com.netelsan.ipinterkompanel.model.Daire;
import com.netelsan.ipinterkompanel.model.Guvenlik;
import com.netelsan.ipinterkompanel.model.ZilPanel;
import com.netelsan.ipinterkompanel.model.ZilPanelSite;
import com.netelsan.ipinterkompanel.tcp.Server;
import com.netelsan.ipinterkompanel.tcp.TCPHelper;
import com.netelsan.ipinterkompanel.tcp.Utils;
import com.stericson.RootShell.execution.Command;
import com.stericson.RootTools.RootTools;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.RandomAccessFile;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.Random;
import java.util.regex.Matcher;
import java.util.regex.Pattern;


public class BaseActivity extends AppCompatActivity {

    private int currentApiVersion;

    CpuStat nesne = new CpuStat();
    private Runnable runnable;
    private Handler handler;
    private int sayac = 0;


    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        currentApiVersion = android.os.Build.VERSION.SDK_INT;
        final int flags = View.SYSTEM_UI_FLAG_LAYOUT_STABLE | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION | View.SYSTEM_UI_FLAG_FULLSCREEN | View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY;
        getWindow().getDecorView().setSystemUiVisibility(flags);

        final View decorView = getWindow().getDecorView();
        decorView.setOnSystemUiVisibilityChangeListener(new View.OnSystemUiVisibilityChangeListener() {

            @Override
            public void onSystemUiVisibilityChange(int visibility) {
                if ((visibility & View.SYSTEM_UI_FLAG_FULLSCREEN) == 0) {
                    decorView.setSystemUiVisibility(flags);
                }
            }
        });
        startTimeTickReceiver();

     /*  getTotalRam();

        ActivityManager.MemoryInfo mi = new ActivityManager.MemoryInfo();
        ActivityManager activityManager = (ActivityManager) getSystemService(ACTIVITY_SERVICE);
        activityManager.getMemoryInfo(mi);
        double availableMegs = mi.availMem / 0x100000L;

//Percentage can be calculated for API 16+

        //hsn cpu zorla
        //buralar test ediliyor

        double percentAvail = mi.availMem / (double)mi.totalMem * 100.0;

        Log.d(Constants.LOG_TAG, "fey fey baseactivity create percentavail=" + percentAvail);

        handler = new Handler();
        runnable = new Runnable() {
            @Override
            public void run() {
                nesne.toString();
                getPerformans();
                handler.postDelayed(runnable,1000);
                sayac++;

                if(sayac == 5)
                {
                    Toast.makeText(getApplicationContext(), "cpu zorlaa", Toast.LENGTH_SHORT).show();
                    Log.i( "deneme","CPU total= baseactivitye geldi" );
                }
                if(sayac > 24000)
                {
                    handler.removeCallbacks(runnable);
                }
            }
        };
        handler.post(runnable);*/

    }

    BroadcastReceiver timeTickReceiver;


    private void startTimeTickReceiver() {
        timeTickReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                if (intent.getAction().compareTo(Intent.ACTION_TIME_TICK) == 0) {
                    String time = Helper.getCurrentHour() + " : " + Helper.getCurrentMinute();
                    if (time.equals("01 : 03")) {
                        ScreensaverActivity2.zilRebootKontrol = true;
                    }
                    if (time.equals("06 : 03")) {
                        ScreensaverActivity2.zilRebootKontrol = true;
                    }
                    if (time.equals("10 : 56")) {
                        ScreensaverActivity2.zilRebootKontrol = true;
                    }
                    if (time.equals("16 : 27")) {
                        ScreensaverActivity2.zilRebootKontrol = true;
                    }
                    if (time.equals("18 : 37")) {
                        ScreensaverActivity2.zilRebootKontrol = true;
                    }
                    if (time.equals("21 : 27")) {
                        ScreensaverActivity2.zilRebootKontrol = true;
                    }

                    serverControlSayac++;

                    if (time.equals("03 : 15")) {
                        Log.d("HK", "HK sistem kontrol");
                        systemControl();
                    }

//                      if (serverControlSayac % 10 == 1) {
//                          systemControl();
//                     }


//                    if (serverControlSayac % 10 == randomControlMinute) {
//                        Log.d(Constants.LOG_TAG, "fey fey baseactivity systemControl methoduna geldi" + randomControlMinute);
//                        systemControl();
//
//                    }


                }

            }
        };
        registerReceiver(timeTickReceiver, new IntentFilter(Intent.ACTION_TIME_TICK));
    }

    public static int serverControlSayac = 0;
    public static int randomControlMinute = 3;
    public static String geciciHour = "00";
    public static String geciciMinute = "00";

    public void systemControl() {
        serverControlSayac = 0;
        DatabaseHelper databaseHelper = new DatabaseHelper(getApplicationContext());


        boolean isZilForSite = Helper.isZilForSite(getApplicationContext());
        if (isZilForSite) {
            ArrayList<Daire> dairelerListesi = databaseHelper.getDaireler();
            if (dairelerListesi.size() < 1) {
                return;
            }
        } else {
            ZilPanel zilPanelSelf = databaseHelper.getZilPanelByIP(Utils.getIPAddress(true));
            ArrayList<Daire> daireIps = databaseHelper.getDairelerForBlok(zilPanelSelf.getBlok());
            if (daireIps.size() < 1) {
                return;
            }

        }

        randomControlMinute = new Random().nextInt(8) + 2;
        String testHour = Helper.getCurrentHour();
        String testMinute = Helper.getCurrentMinute();

        if (isZilForSite) {
            //Site Zil paneli için

            if (geciciHour.equals("00") && geciciMinute.equals("00")) {
                geciciHour = testHour;
                geciciMinute = testMinute;
                ArrayList<Guvenlik> guvelikIps = databaseHelper.getGuvenlikler();
                if (guvelikIps.size() > 0) {

                    // String selfIPAddress = Utils.getIPAddress(true);
                    // ZilPanelSite
                    ZilPanelSite zilPanelSiteSelf = databaseHelper.getSiteZilPanelByIP(Utils.getIPAddress(true));
                    Iterator<Guvenlik> iteratorGuvenlik = guvelikIps.iterator();
                    if (iteratorGuvenlik.hasNext()) {
                        while (iteratorGuvenlik.hasNext()) {
                            Guvenlik guvenlik = iteratorGuvenlik.next();
                            String destinationIp = guvenlik.getIp();
                            ComPackageModel model = new ComPackageModel();
                            model.setOpe_type(Constants.OPERATION_CHECK_CONNECTION);
                            model.setServerControlHour(geciciHour);
                            model.setServerControlMinute(geciciMinute);
                            model.setZilPanelSite(zilPanelSiteSelf);
                            TCPHelper.sendMessageToIP(getApplicationContext(), destinationIp, model);
                            Log.d(Constants.LOG_TAG, "fey fey baseactivity systemControl basarili");
                            break;

                        }
                    }
                }


                ArrayList<Daire> daireIps = databaseHelper.getDaireler();

                if (daireIps.size() > 0) {
                    ZilPanelSite zilPanelSiteSelf = databaseHelper.getSiteZilPanelByIP(Utils.getIPAddress(true));
                    Iterator<Daire> iterator = daireIps.iterator();
                    int daireSayaci = 0;
                    while (iterator.hasNext()) {
                        Daire iteratorDaire = iterator.next();
                        String destinationIp = iteratorDaire.getIp();
                        ComPackageModel model = new ComPackageModel();
                        model.setOpe_type(Constants.OPERATION_CHECK_CONNECTION);
                        model.setServerControlHour(geciciHour);
                        model.setServerControlMinute(geciciMinute);
                        model.setZilPanelSite(zilPanelSiteSelf);
                        TCPHelper.sendMessageToIP(getApplicationContext(), destinationIp, model);
                        daireSayaci++;
                        Log.d(Constants.LOG_TAG, "fey fey baseactivity systemControl iteratorDaire basarili=" + daireSayaci);
                        if (daireSayaci > 3) {
                            break;
                        }

                    }
                }

                Log.d(Constants.LOG_TAG, "fey SiteZil devam ediyor1");
                return;
            }

            if (geciciHour.equals(Server.serverGeciciHour) && geciciMinute.equals(Server.serverGeciciMinute)) {

                geciciHour = testHour;
                geciciMinute = testMinute;
                ArrayList<Guvenlik> guvelikIps = databaseHelper.getGuvenlikler();
                if (guvelikIps.size() > 0) {
                    ZilPanelSite zilPanelSiteSelf = databaseHelper.getSiteZilPanelByIP(Utils.getIPAddress(true));
                    Iterator<Guvenlik> iteratorGuvenlik = guvelikIps.iterator();
                    if (iteratorGuvenlik.hasNext()) {
                        while (iteratorGuvenlik.hasNext()) {
                            Guvenlik guvenlik = iteratorGuvenlik.next();
                            String destinationIp = guvenlik.getIp();
                            ComPackageModel model = new ComPackageModel();
                            model.setOpe_type(Constants.OPERATION_CHECK_CONNECTION);
                            model.setServerControlHour(geciciHour);
                            model.setServerControlMinute(geciciMinute);
                            model.setZilPanelSite(zilPanelSiteSelf);
                            TCPHelper.sendMessageToIP(getApplicationContext(), destinationIp, model);
                            Log.d(Constants.LOG_TAG, "fey fey baseactivity systemControl basarili");
                            break;

                        }
                    }
                }


                ArrayList<Daire> daireIps = databaseHelper.getDaireler();

                if (daireIps.size() > 0) {
                    ZilPanelSite zilPanelSiteSelf = databaseHelper.getSiteZilPanelByIP(Utils.getIPAddress(true));
                    Iterator<Daire> iterator = daireIps.iterator();
                    int daireSayaci = 0;
                    while (iterator.hasNext()) {
                        Daire iteratorDaire = iterator.next();
                        String destinationIp = iteratorDaire.getIp();
                        ComPackageModel model = new ComPackageModel();
                        model.setOpe_type(Constants.OPERATION_CHECK_CONNECTION);
                        model.setServerControlHour(geciciHour);
                        model.setServerControlMinute(geciciMinute);
                        model.setZilPanelSite(zilPanelSiteSelf);
                        TCPHelper.sendMessageToIP(getApplicationContext(), destinationIp, model);
                        daireSayaci++;
                        Log.d(Constants.LOG_TAG, "fey fey baseactivity systemControl iteratorDaire basarili=" + daireSayaci);
                        if (daireSayaci > 3) {
                            break;
                        }

                    }
                }

                return;
            } else {
                rebootDevice();
            }
        } else {
            //Blok Zil panelleri için


            geciciHour = testHour;
            geciciMinute = testMinute;
            ArrayList<Guvenlik> guvelikIps = databaseHelper.getGuvenlikler();
            ZilPanel zilPanelSelf = databaseHelper.getZilPanelByIP(Utils.getIPAddress(true));
            if (guvelikIps.size() > 0) {
                Iterator<Guvenlik> iteratorGuvenlik = guvelikIps.iterator();
                if (iteratorGuvenlik.hasNext()) {
                    while (iteratorGuvenlik.hasNext()) {
                        Guvenlik guvenlik = iteratorGuvenlik.next();
                        String destinationIp = guvenlik.getIp();
                        ComPackageModel model = new ComPackageModel();
                        model.setOpe_type(Constants.OPERATION_CHECK_CONNECTION);
                        model.setServerControlHour(geciciHour);
                        model.setServerControlMinute(geciciMinute);
                        model.setZilPanel(zilPanelSelf);
                        TCPHelper.sendMessageToIP(getApplicationContext(), destinationIp, model);
                        Log.d(Constants.LOG_TAG, "fey fey zilPanelSelf baseactivity systemControl basarili");
                        break;

                    }
                }
            }


            ArrayList<Daire> daireIps = databaseHelper.getDairelerForBlok(zilPanelSelf.getBlok());

            if (daireIps.size() > 0) {
                Iterator<Daire> iterator = daireIps.iterator();
                int daireSayaci = 0;
                while (iterator.hasNext()) {
                    Daire iteratorDaire = iterator.next();
                    String destinationIp = iteratorDaire.getIp();
                    ComPackageModel model = new ComPackageModel();
                    model.setOpe_type(Constants.OPERATION_CHECK_CONNECTION);
                    model.setServerControlHour(geciciHour);
                    model.setServerControlMinute(geciciMinute);
                    model.setZilPanel(zilPanelSelf);
                    TCPHelper.sendMessageToIP(getApplicationContext(), destinationIp, model);
                    daireSayaci++;
                    Log.d("HK", "HK zilPanelSelf baseactivity systemControl ip:" + destinationIp);
                    if (daireSayaci > 3) {
                        break;
                    }

                }
            }

            Log.d(Constants.LOG_TAG, "fey SiteZil devam ediyor1");
            return;


//            if (geciciHour.equals(Server.serverGeciciHour) && geciciMinute.equals(Server.serverGeciciMinute)) {
//
//                geciciHour = testHour;
//                geciciMinute = testMinute;
//                ArrayList<Guvenlik> guvelikIps = databaseHelper.getGuvenlikler();
//                ZilPanel zilPanelSelf = databaseHelper.getZilPanelByIP(Utils.getIPAddress(true));
//                if (guvelikIps.size() > 0) {
//                    Iterator<Guvenlik> iteratorGuvenlik = guvelikIps.iterator();
//                    if (iteratorGuvenlik.hasNext()) {
//                        while (iteratorGuvenlik.hasNext()) {
//                            Guvenlik guvenlik = iteratorGuvenlik.next();
//                            String destinationIp = guvenlik.getIp();
//                            ComPackageModel model = new ComPackageModel();
//                            model.setOpe_type(Constants.OPERATION_CHECK_CONNECTION);
//                            model.setServerControlHour(geciciHour);
//                            model.setServerControlMinute(geciciMinute);
//                            model.setZilPanel(zilPanelSelf);
//                            TCPHelper.sendMessageToIP(getApplicationContext(), destinationIp, model);
//                            Log.d(Constants.LOG_TAG, "fey fey zilPanelSelf baseactivity systemControl devam basarili");
//                            break;
//
//                        }
//                    }
//                }
//
//
//                ArrayList<Daire> daireIps = databaseHelper.getDairelerForBlok(zilPanelSelf.getBlok());
//
//                if (daireIps.size() > 0) {
//                    Iterator<Daire> iterator = daireIps.iterator();
//                    int daireSayaci = 0;
//                    while (iterator.hasNext()) {
//                        Daire iteratorDaire = iterator.next();
//                        String destinationIp = iteratorDaire.getIp();
//                        ComPackageModel model = new ComPackageModel();
//                        model.setOpe_type(Constants.OPERATION_CHECK_CONNECTION);
//                        model.setServerControlHour(geciciHour);
//                        model.setServerControlMinute(geciciMinute);
//                        model.setZilPanel(zilPanelSelf);
//                        TCPHelper.sendMessageToIP(getApplicationContext(), destinationIp, model);
//                        daireSayaci++;
//                        Log.d(Constants.LOG_TAG, "fey fey zilPanelSelf baseactivity systemControl devam basarili2=" + daireSayaci);
//                        if (daireSayaci > 3) {
//                            break;
//                        }
//
//                    }
//                }
//
//                Log.d(Constants.LOG_TAG, "fey SiteZil devam ediyor1");
//                return;
//            } else {
//                Log.d(Constants.LOG_TAG, "fey SiteZil reset oldu");
//                rebootDevice();
//            }


        }


    }


    private void rebootDevice() {
        try {
            Command command = new Command(0, "reboot");
            RootTools.getShell(true).add(command).getExitCode();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    @Override
    public void onWindowFocusChanged(boolean hasFocus) {
        super.onWindowFocusChanged(hasFocus);
        if (currentApiVersion >= Build.VERSION_CODES.KITKAT && hasFocus) {
            getWindow().getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_LAYOUT_STABLE | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION | View.SYSTEM_UI_FLAG_FULLSCREEN | View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY);
        }
    }

    public int getScreenWidth() {
        Display display = getWindowManager().getDefaultDisplay();
        Point size = new Point();
        display.getSize(size);
        int width = size.x;

        return width;

    }

    public int getScreenHeight() {
        Display display = getWindowManager().getDefaultDisplay();
        Point size = new Point();
        display.getSize(size);
        int height = size.y;

        return height;
    }

    public Point getLocationOnScreen(View view) {
        int[] location = new int[2];
        view.getLocationOnScreen(location);
        return new Point(location[0], location[1]);
    }


    @Override
    protected void onDestroy() {
        super.onDestroy();
        try {
            unregisterReceiver(timeTickReceiver);
        } catch (Exception e) {

        }

    }

    public class CpuStat {
        private static final String TAG = "CpuUsage";
        private RandomAccessFile statFile;
        private CpuInfo mCpuInfoTotal;
        private ArrayList<CpuInfo> mCpuInfoList;

        public CpuStat() {
        }

        public void update() {
            try {
                createFile();
                parseFile();
                closeFile();
            } catch (FileNotFoundException e) {
                statFile = null;
                Log.e(TAG, "cannot open /proc/stat: " + e);
            } catch (IOException e) {
                Log.e(TAG, "cannot close /proc/stat: " + e);
            }
        }

        private void createFile() throws FileNotFoundException {
            statFile = new RandomAccessFile("/proc/stat", "r");
        }

        public void closeFile() throws IOException {
            if (statFile != null)
                statFile.close();
        }

        private void parseFile() {
            if (statFile != null) {
                try {
                    statFile.seek(0);
                    String cpuLine = "";
                    int cpuId = -1;
                    do {
                        cpuLine = statFile.readLine();
                        parseCpuLine(cpuId, cpuLine);
                        cpuId++;
                    } while (cpuLine != null);
                } catch (IOException e) {
                    Log.e(TAG, "Ops: " + e);
                }
            }
        }

        private void parseCpuLine(int cpuId, String cpuLine) {
            if (cpuLine != null && cpuLine.length() > 0) {
                String[] parts = cpuLine.split("[ ]+");
                String cpuLabel = "cpu";
                if (parts[0].indexOf(cpuLabel) != -1) {
                    createCpuInfo(cpuId, parts);
                }
            } else {
                Log.e(TAG, "unable to get cpu line");
            }
        }

        private void createCpuInfo(int cpuId, String[] parts) {
            if (cpuId == -1) {
                if (mCpuInfoTotal == null)
                    mCpuInfoTotal = new CpuInfo();
                mCpuInfoTotal.update(parts);
            } else {
                if (mCpuInfoList == null)
                    mCpuInfoList = new ArrayList<CpuInfo>();
                if (cpuId < mCpuInfoList.size())
                    mCpuInfoList.get(cpuId).update(parts);
                else {
                    CpuInfo info = new CpuInfo();
                    info.update(parts);
                    mCpuInfoList.add(info);
                }
            }
        }

        public int getCpuUsage(int cpuId) {
            update();
            int usage = 0;
            if (mCpuInfoList != null) {
                int cpuCount = mCpuInfoList.size();
                if (cpuCount > 0) {
                    cpuCount--;
                    if (cpuId == cpuCount) { // -1 total cpu usage
                        usage = mCpuInfoList.get(0).getUsage();
                    } else {
                        if (cpuId <= cpuCount)
                            usage = mCpuInfoList.get(cpuId).getUsage();
                        else
                            usage = -1;
                    }
                }
            }
            return usage;
        }


        public int getTotalCpuUsage() {
            update();
            int usage = 0;
            if (mCpuInfoTotal != null)
                usage = mCpuInfoTotal.getUsage();
            return usage;
        }


        public String toString() {
            update();
            StringBuffer buf = new StringBuffer();
            if (mCpuInfoTotal != null) {
                buf.append("Cpu Total : ");
                buf.append(mCpuInfoTotal.getUsage());
                buf.append("%");
            }
            if (mCpuInfoList != null) {
                for (int i = 0; i < mCpuInfoList.size(); i++) {
                    CpuInfo info = mCpuInfoList.get(i);
                    buf.append(" Cpu Core(" + i + ") : ");
                    buf.append(info.getUsage());
                    buf.append("%");
                    info.getUsage();
                }
            }
            return buf.toString();
        }

        public class CpuInfo {
            private int mUsage;
            private long mLastTotal;
            private long mLastIdle;

            public CpuInfo() {
                mUsage = 0;
                mLastTotal = 0;
                mLastIdle = 0;
            }

            private int getUsage() {
                return mUsage;
            }

            public void update(String[] parts) {
                // the columns are:
                //
                // 0 "cpu": the string "cpu" that identifies the line
                // 1 user: normal processes executing in user mode
                // 2 nice: niced processes executing in user mode
                // 3 system: processes executing in kernel mode
                // 4 idle: twiddling thumbs
                // 5 iowait: waiting for I/O to complete
                // 6 irq: servicing interrupts
                // 7 softirq: servicing softirqs
                //
                long idle = Long.parseLong(parts[4], 10);
                long total = 0;
                boolean head = true;
                for (String part : parts) {
                    if (head) {
                        head = false;
                        continue;
                    }
                    total += Long.parseLong(part, 10);
                }
                long diffIdle = idle - mLastIdle;
                long diffTotal = total - mLastTotal;
                mUsage = (int) ((float) (diffTotal - diffIdle) / diffTotal * 100);
                mLastTotal = total;
                mLastIdle = idle;
                Log.i(TAG, "CPU total=" + total + "; idle=" + idle + "; usage=%" + mUsage);
            }
        }
    }

    public void getTotalRam() {
        RandomAccessFile reader = null;
        String load = null;
        DecimalFormat twoDecimal = new DecimalFormat("#.##");
        double totRam = 0;
        String lastvalue = "";
        try {
            reader = new RandomAccessFile("/proc/meminfo", "r");
            load = reader.readLine();

            Pattern p = Pattern.compile("(\\d+)");
            Matcher m = p.matcher(load);
            String value = "";
            while (m.find()) {
                value = m.group(1);


            }


            reader.close();
            totRam = Double.parseDouble(value);

            double mb = totRam / 1024.0;
            double gb = totRam / 1048576.0;
            double tb = totRam / 1073741824.0;

            if (tb > 1) {
                lastvalue = twoDecimal.format(tb).concat(" TB");
            } else if (gb > 1) {
                lastvalue = twoDecimal.format(gb).concat(" GB");
            } else if (mb > 1) {
                lastvalue = twoDecimal.format(mb).concat(" MB");
            } else {
                lastvalue = twoDecimal.format(totRam).concat(" KB");
            }


        } catch (Exception e) {
            e.printStackTrace();
        }

        Log.d("Ram", "Ram value: " + lastvalue);
    }


    private void getPerformans() {
        ActivityManager activityManager = (ActivityManager) getApplicationContext().getSystemService(Context.ACTIVITY_SERVICE);
        ActivityManager.MemoryInfo memoryInfo = new ActivityManager.MemoryInfo();
        assert activityManager != null;
        activityManager.getMemoryInfo(memoryInfo);
        long totalmemory = memoryInfo.totalMem;
        long availmemory = memoryInfo.availMem;
        long usedmemory = totalmemory - availmemory;
        float precentlong = ((float) (usedmemory / totalmemory) * 100);
        Log.d("hsn hsn memory", "totalmemory: " + totalmemory + "     usedmemory: " + usedmemory + "         yuzdelik: %" + precentlong);
    }

}



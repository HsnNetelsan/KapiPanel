package com.netelsan.ipinterkompanel.activity.device_test;


import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.TextView;

import com.netelsan.ipinterkompanel.Constants;
import com.netelsan.ipinterkompanel.Helper;
import com.netelsan.ipinterkompanel.R;
import com.netelsan.ipinterkompanel.tcp.Utils;
import com.stericson.RootShell.execution.Command;
import com.stericson.RootTools.RootTools;

import java.net.InetAddress;
import java.util.Timer;
import java.util.TimerTask;

public class TestEthernetActivity extends BaseTestActivity implements View.OnClickListener {

    TextView veriTestiMessage;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_test_ethernet);

        veriTestiMessage = findViewById(R.id.veriTestiMessage);

        boolean isNeedRebootForRoot = isNeedRebootForRoot();
        if (isNeedRebootForRoot) {
            rebootDevice();
            return;
        }

        startEthernetTest();

    }

    private void rebootDevice() {
        try {
            Helper.showWhiteScreen(TestEthernetActivity.this);
            Process process = Runtime.getRuntime().exec("adb reboot");
            process.waitFor();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private boolean isNeedRebootForRoot() {

        boolean isRebootedForRoot = Helper.isRebootedForRoot(getApplicationContext());
        if (isRebootedForRoot) {
            return false;
        }
        Helper.setRebootedForRoot(getApplicationContext(), true);
        return true;
    }

    private void startEthernetTest() {

        String selfIP = Utils.getIPAddress(true);
//        Log.d(Constants.LOG_TAG, "1907 startEthernetTest selfIP=" + selfIP);

        if (selfIP.equals(Constants.IP_FOR_TEST)) {// data gönder al işlerini yap
            new ScanSelfDevice(selfIP).executeOnExecutor(Constants.DEFAULT_EXECUTOR);
        } else {
//            try count ile 3 defa denerse hata mesajı verdir
            int ethernetTestTryCount = Helper.getEthernetTestTryCount(getApplicationContext());
            if (ethernetTestTryCount > 3) {
                showVeriTestiResult(false);
            } else {
                Helper.setEthernetTestTryCount(getApplicationContext(), ethernetTestTryCount + 1);
                Helper.showWhiteScreen(TestEthernetActivity.this);
                setTestIP(Constants.IP_FOR_TEST);
            }
        }

    }

    class ScanSelfDevice extends AsyncTask<Void, Void, Boolean> {

        String selfIP;

        public ScanSelfDevice(String selfIP) {
            this.selfIP = selfIP;
        }

        protected void onPreExecute() {
            super.onPreExecute();
        }

        protected Boolean doInBackground(Void... arg0) {
            boolean isReachable = isDeviceReachable(selfIP);
            Log.d(Constants.LOG_TAG, "1907 ScanSelfDevice isReachable=" + isReachable);
            return isReachable;
        }

        protected void onPostExecute(Boolean result) {
            super.onPostExecute(result);
            showVeriTestiResult(result);
        }

    }

    private void showVeriTestiResult(boolean isSuccess) {
        if (isSuccess) {
            veriTestiMessage.setText(getString(R.string.veri_testi_basarili));
            startTimerForReboot();
        } else {
            veriTestiMessage.setText(getString(R.string.veri_testi_basarili_degil));
        }
    }

    private void startTimerForReboot() {
        Log.d(Constants.LOG_TAG, "1907 startTimerForReboot");
        setTestIP("");
        Helper.setEthernetTestFinished(getApplicationContext(), true);

        Timer timer = new Timer();
        timer.schedule(new TimerTask() {
            @Override
            public void run() {
                try {

//                    Log.d(Constants.LOG_TAG, "1907 startTimerForReboot runs");
                    Helper.showWhiteScreen(TestEthernetActivity.this);
                    Command command = new Command(0, "reboot");
                    RootTools.getShell(true).add(command).getExitCode();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }, 10000);
    }

    private boolean isDeviceReachable(String selfIP) {

        InetAddress inetAddress = null;
        try {
            inetAddress = InetAddress.getByName(selfIP);
            if (inetAddress.isReachable(50)) {
                return true;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        return false;

    }

    private void setTestIP(String generatedIP) {

        try {

            Command command = new Command(0,
                    "sqlite3 /data/data/com.android.providers.settings/databases/settings.db \"UPDATE global SET value = '0' WHERE name = 'eth_mode';\"",
                    "sqlite3 /data/data/com.android.providers.settings/databases/settings.db \"UPDATE global SET value = '" + generatedIP + "' WHERE name = 'eth_ip';\"",
                    "sqlite3 /data/data/com.android.providers.settings/databases/settings.db \"UPDATE global SET value = '255.0.0.0' WHERE name = 'eth_netmask';\"",
//                    "sqlite3 /data/data/com.android.providers.settings/databases/settings.db \"UPDATE global SET value = '192.168.1.207' WHERE name = 'eth_route';\"",
                    "sqlite3 /data/data/com.android.providers.settings/databases/settings.db \"UPDATE global SET value = '8.8.8.8' WHERE name = 'eth_dns';\"",
                    "reboot");
            RootTools.getShell(true).add(command).getExitCode();

        } catch (Exception e) {
            e.printStackTrace();
        }

    }

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

    }

}
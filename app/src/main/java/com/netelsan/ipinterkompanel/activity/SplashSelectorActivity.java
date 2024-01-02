package com.netelsan.ipinterkompanel.activity;

import android.app.AlertDialog;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.ServiceConnection;
import android.media.AudioManager;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.provider.Settings;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.Nullable;

import com.netelsan.ipinterkompanel.Constants;
import com.netelsan.ipinterkompanel.Helper;
import com.netelsan.ipinterkompanel.R;
import com.netelsan.ipinterkompanel.activity.device_test.DeviceTestActivity;
import com.netelsan.ipinterkompanel.activity.device_test.TestEthernetActivity;
import com.netelsan.ipinterkompanel.database.DatabaseHelper;
import com.netelsan.ipinterkompanel.dialog_activity.NoZilPanelCountDownActivity;
import com.netelsan.ipinterkompanel.listener.NetworkScanListener;
import com.netelsan.ipinterkompanel.model.ComPackageModel;
import com.netelsan.ipinterkompanel.model.ZilPanel;
import com.netelsan.ipinterkompanel.model.ZilPanelSite;
import com.netelsan.ipinterkompanel.service.KeyPadService;
import com.netelsan.ipinterkompanel.service.NetworkScanService;
import com.netelsan.ipinterkompanel.tcp.Server;
import com.netelsan.ipinterkompanel.tcp.TCPHelper;
import com.netelsan.ipinterkompanel.tcp.Utils;
import com.stericson.RootShell.execution.Command;
import com.stericson.RootTools.RootTools;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.Timer;
import java.util.TimerTask;

public class SplashSelectorActivity extends BaseActivity implements ServiceConnection {

//    public static boolean isActivityAlive = false;

    TextView initializeScanDevicesText;

    ImageView initializeBrandLogo;
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_selector);

        initializeScanDevicesText = findViewById(R.id.initializeScanDevicesText);
        initializeBrandLogo = findViewById(R.id.splashBrandLogo);

        setBrandLogo();
//        setDeviceLanguage();

        startKeyPadService();


        try {
            Helper.runRestartCameraShellCommand();
        } catch (Throwable e) {
            e.printStackTrace();
        }

//        AudioManager audioManager = (AudioManager) getSystemService(AUDIO_SERVICE);
//        int maxVolume = audioManager.getStreamMaxVolume(AudioManager.STREAM_MUSIC);
//        audioManager.setStreamVolume(AudioManager.STREAM_MUSIC, 5, 0);

        initializeScanDevicesText.setVisibility(View.GONE);

        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        onCreatePostOperations();
                    }
                });
            }
        }, 2500);


        //feyyaz yorum satırı yaptı
        //   hideSystemUi();

        setLedState(false);



    }

    private void onCreatePostOperations() {
        boolean isRooted = isRooted();
        if (isRooted) {

//            disableAutoSyncTime();

            boolean isInitializeFinished = Helper.isInitializeFinished(getApplicationContext());

            if (isInitializeFinished) {
                boolean isHandshakeFinished = Helper.isHandshakeFinished(getApplicationContext());
                if (isHandshakeFinished) {
                    boolean isCriticalErrorExist = isCriticalErrorExist();
                    if (isCriticalErrorExist) {
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {

                                showCriticalErrorDialog();
                            }
                        });
                    } else {
                        startBackgroundTCPServer();
                    }
                } else {
                    initializeScanDevicesText.setVisibility(View.VISIBLE);
                    //handshake işlemlerini yap.

                    startNetworkScanForHandshake();

                    new Handler().postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    onCreatePostOperations();
                                }
                            });
                        }
                    }, 30000);
                }
            } else {
                boolean isDeviceTestFinished = Helper.isDeviceTestFinished(getApplicationContext());
                if (isDeviceTestFinished) {
                    startInitialize();
                } else {
                    startDeviceTest();
                }
            }
//feyyaz koydu
            hideSystemUi();
        } else {
            showRootNeededScreen();
        }
    }

    private void disableAutoSyncTime() {
        try {
            Process process = Runtime.getRuntime().exec("adb shell settings put global auto_time 0");
            process.waitFor();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void startDeviceTest() {

//        burada öncelikle data testi yap. ip set et ve restart at.

        boolean isEthernetTestFinished = Helper.isEthernetTestFinished(getApplicationContext());
        if (isEthernetTestFinished) {
            Intent intent = new Intent(SplashSelectorActivity.this, DeviceTestActivity.class);
            intent.putExtra("isNeedInitializeScreen", true);
            startActivity(intent);
            SplashSelectorActivity.this.finish();
        } else {
            showVeriTestiScreen();
        }

    }

    private void showVeriTestiScreen() {
        Intent intent = new Intent(SplashSelectorActivity.this, TestEthernetActivity.class);
        startActivity(intent);
    }

    private void setLedState(boolean isActive) {

        int ledPortNumber = Helper.getPINFromGPIO("PE17");

        Process process = Helper.getProcess();
        Helper.setGPIO(process, Constants.GPIO_EXPORT, ledPortNumber, true, isActive);
        Helper.setGPIO(process, Constants.GPIO_DIRECTION, ledPortNumber, true, isActive);
        Helper.setGPIO(process, Constants.GPIO_VALUE, ledPortNumber, true, isActive);

    }

    private void setBrandLogo() {

        if (Constants.IS_DEVICE_NAUFFEN) {
            initializeBrandLogo.setImageResource(R.drawable.nauffen_logo_big);
        } else {
            initializeBrandLogo.setImageResource(R.drawable.netelsan_logo);
        }
    }

    private void hideSystemUi() {
        try {
            Build.VERSION_CODES vc = new Build.VERSION_CODES();
            Build.VERSION vr = new Build.VERSION();
            String ProcID = "79"; //HONEYCOMB AND OLDER

            //v.RELEASE  //4.0.3
            if (vr.SDK_INT >= vc.ICE_CREAM_SANDWICH) {
                ProcID = "42"; //ICS AND NEWER
            }
            Process proc = Runtime.getRuntime().exec(new String[]{
                    "su", "-c", "service call activity " + ProcID + " s16 com.android.systemui"});

            proc.waitFor();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void showCriticalErrorDialog() {

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setCancelable(false);
        builder.setTitle("");

        View customLayout = getLayoutInflater().inflate(R.layout.custom_critical_error_dialog, null);
        builder.setView(customLayout);


        AlertDialog alertDialog = builder.create();
        alertDialog.show();

        Timer timer = new Timer();
        timer.schedule(new TimerTask() {
            @Override
            public void run() {
                try {
                    Helper.showWhiteScreen(SplashSelectorActivity.this);
                    Command command = new Command(0, "reboot");
                    RootTools.getShell(true).add(command).getExitCode();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }, 7500);

    }

    private boolean isCriticalErrorExist() {

        String selfIP = Utils.getIPAddress(true);
        Log.d(Constants.LOG_TAG, "isCriticalErrorExist selfIP=" + selfIP);

        if (selfIP == null) {
            return true;
        }

        if (selfIP.equals("")) {
            return true;
        }

        return false;

    }

//    private void setDeviceLanguage() {
//
//        String languageCodeText = "";
//        int selectedLanguageCode = Helper.getSelectedLanguageCode(getApplicationContext());
//        Log.d(Constants.LOG_TAG, "setDeviceLanguage selectedLanguageCode=" + selectedLanguageCode);
//        if(selectedLanguageCode == Constants.LANGUAGE_TR) {
//            languageCodeText = "tr";
//        } else if(selectedLanguageCode == Constants.LANGUAGE_EN) {
//            languageCodeText = "en";
//        } else if(selectedLanguageCode == Constants.LANGUAGE_DE) {
//            languageCodeText = "de";
//        }
//
////        Log.d(Constants.LOG_TAG, "application language sets to=" + languageCodeText);
//
//        Locale locale = new Locale(languageCodeText);
//        Locale.setDefault(locale);
//        Configuration config = new Configuration();
//        config.locale = locale;
//        getBaseContext().getResources().updateConfiguration(config, getBaseContext().getResources().getDisplayMetrics());
//
//    }

    private void startNetworkScanForHandshake() {

        NetworkScanService networkScanService = new NetworkScanService(SplashSelectorActivity.this);
        Intent serverIntent = new Intent(SplashSelectorActivity.this, networkScanService.getClass());
        startService(serverIntent);

        setListenerToService();
    }

    boolean isServiceBind = false;
    NetworkScanService networkScanService;

    @Override
    public void onServiceConnected(ComponentName name, IBinder service) {
        isServiceBind = true;
        networkScanService = ((NetworkScanService.NetworkScanServiceBinder) service).getService();

        tryCount = 1;
        tryNetworkScan();

    }

    private void tryNetworkScan() {

        String selfIPAddress = Utils.getIPAddress(true);
        Log.d(Constants.LOG_TAG, "tryNetworkScan selfIPAddress=" + selfIPAddress);
        if (selfIPAddress.equals("")) {
            if (tryCount >= 4) {
//                cihazı sıfırla baştan kurulum yapsın

                Helper.setHandshakeFinished(getApplicationContext(), false);
                Helper.setInitializeFinished(getApplicationContext(), false);
                resetDeviceIP();

                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        showKurulumHatasiDialog();
                    }
                });
                return;
            }
            tryNetworkScanIn5Seconds();
            return;
        }
        startNetworkScan(selfIPAddress);
    }

    int tryCount = 1;

    private void tryNetworkScanIn5Seconds() {
        Timer timer = new Timer();
        timer.schedule(new TimerTask() {
            @Override
            public void run() {
                tryCount = tryCount + 1;
                tryNetworkScan();
            }
        }, 5000);
    }

    private void startNetworkScan(String selfIPAddress) {
        if (networkScanService == null) {
            return;
        }

        NetworkScanListener networkScanListener = new NetworkScanListener() {
            @Override
            public void onFinished(ArrayList<String> arrayList) {

//                Log.d(Constants.LOG_TAG, "1907 onFinished=");
                startServerService();

                //önlem oalrak 1500 ms beklet
                sendHandshakeToAllDevicesIn2Seconds(arrayList);

            }
        };
        networkScanService.startNetworkScanWithListener(networkScanListener, selfIPAddress);
    }

    private void sendHandshakeToAllDevicesIn2Seconds(ArrayList<String> arrayList) {

//        SERVER SERVİCE İN AÇILMASINI BEKLEMEK İÇİN eklendi
        Timer timer = new Timer();
        timer.schedule(new TimerTask() {
            @Override
            public void run() {
                sendHandshakeToAllDevices(arrayList);
            }
        }, 1500);

    }

    private void sendHandshakeToAllDevices(ArrayList<String> result) {

        Log.d(Constants.LOG_TAG, "1907 sendHandshakeToAllDevices arraySize=" + result.size());

        DatabaseHelper databaseHelper = new DatabaseHelper(getApplicationContext());

        boolean isZilForSite = Helper.isZilForSite(getApplicationContext());

        ZilPanel zilPanel = null;
        ZilPanelSite zilPanelSite = null;
        int operationType;
        if (isZilForSite) {

            if (result.size() == 0) {

                Helper.setHandshakeFinished(getApplicationContext(), false);
                Helper.setInitializeFinished(getApplicationContext(), false);
                resetDeviceIP();

                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        showNoZilPaneliDialog();
                    }
                });
                return;
            }
            operationType = Constants.OPERATION_HANDSHAKE_ZIL_PANEL_SITE;
            zilPanelSite = databaseHelper.getSiteZilPanelByIP(Utils.getIPAddress(true));
        } else {
            operationType = Constants.OPERATION_HANDSHAKE_ZIL_PANEL;
            zilPanel = databaseHelper.getZilPanelByIP(Utils.getIPAddress(true));
        }

        Iterator<String> iterator = result.iterator();
        while (iterator.hasNext()) {
            String reachableIP = iterator.next();
            Log.d(Constants.LOG_TAG, "1907 sendHandshakeToAllDevices reachableIP=" + reachableIP);

            ComPackageModel model = new ComPackageModel();
            model.setOpe_type(operationType);
            if (isZilForSite) {
                model.setZilPanelSite(zilPanelSite);
            } else {
                model.setZilPanel(zilPanel);
            }
            model.setNeedResponse(true);

            TCPHelper.sendMessageToIP(getApplicationContext(), reachableIP, model);
        }

        Helper.setHandshakeFinished(getApplicationContext(), true);

        boolean isCenterUnitZilPanel = Helper.isCenterUnitZilPanel(getApplicationContext());

        Log.d(Constants.LOG_TAG, "feyy SplashSelectorActivity requestDeviceDateAndTime  saaat için geldi 222 isCenterUnitZilPanel= "+ isCenterUnitZilPanel);
        if (isCenterUnitZilPanel) {
            startMainActivity();
        } else {
            requestDeviceDateAndTime();
        }

    }

    private void requestDeviceDateAndTime() {


        Log.d(Constants.LOG_TAG, "feyy SplashSelectorActivity requestDeviceDateAndTime  saaat için geldi 222");
        DatabaseHelper databaseHelper = new DatabaseHelper(getApplicationContext());
        ArrayList<ZilPanel> arrayListZilPaneller = databaseHelper.getZilPanelleri(true);



        if (arrayListZilPaneller.size() == 0) {
            Log.d(Constants.LOG_TAG, "hiç zil paneli yok. 5 saniye sonra tekrar dene");
            tryAgainIn5Seconds();
            return;
        }

        //burada koyduğumuz 5 saniyelik bekleme zil panelinin açılışını beklemek için. tarih ve saat oradan geldiği için henüz açılmadan istemeye çalışıyoruz.
        // Bu sebeple 5 saniye bekletip istiyoruz
        // bildiği bütün zil panellerine saat için mesaj atıyor. hangisi cevap dönerse eşitliyor
        Timer timer = new Timer();
        timer.schedule(new TimerTask() {
            @Override
            public void run() {



                Log.d(Constants.LOG_TAG, "feyy SplashSelectorActivity requestDeviceDateAndTime  saaat için geldi 222");

                Iterator<ZilPanel> iterator = arrayListZilPaneller.iterator();
                while (iterator.hasNext()) {
                    ZilPanel zilPanel = iterator.next();

                    String destinationIP = zilPanel.getIp();

//                    Guvenlik guvenlik = databaseHelper.getZilPanelByIP(Utils.getIPAddress(true));

                    ComPackageModel model = new ComPackageModel();
                    model.setOpe_type(Constants.OPERATION_GET_DATE_TIME);
//                    model.setGuvenlik(guvenlik);

                    TCPHelper.sendMessageToIP(getApplicationContext(), destinationIP, model);

                }

                startMainActivity();

            }
        }, 5000);

    }

    private void tryAgainIn5Seconds() {

        Timer timer = new Timer();
        timer.schedule(new TimerTask() {
            @Override
            public void run() {
                Log.d(Constants.LOG_TAG, "5 saniye sonra deneme başladı");
                requestDeviceDateAndTime();
            }
        }, 5000);

    }

    //    int countDownTimerForNoZilPanel = 5;
    private void showNoZilPaneliDialog() {

        Intent intent = new Intent(SplashSelectorActivity.this, NoZilPanelCountDownActivity.class);
        startActivity(intent);

        AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(this);
        alertDialogBuilder.setCancelable(false);

        alertDialogBuilder.setTitle(getString(R.string.zil_paneli_bulunamadi)).setMessage(getString(R.string.reset_device_message));
        alertDialogBuilder.setPositiveButton(getString(R.string.tekrar_kurulum), new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int which) {

                try {
                    Helper.showWhiteScreen(SplashSelectorActivity.this);
                    Command command = new Command(0, "reboot");
                    RootTools.getShell(true).add(command).getExitCode();
                } catch (Exception e) {
                    e.printStackTrace();
                }

            }
        }).show();

    }

    @Override
    public void onServiceDisconnected(ComponentName name) {

        isServiceBind = false;
        networkScanService = null;
    }

    AlertDialog countDownTimerDialog;
    int timeRemaining = 5;
    Timer timerCountDown;

    private void showKurulumHatasiDialog() {

        timeRemaining = 5;

        AlertDialog.Builder builder = new AlertDialog.Builder(SplashSelectorActivity.this);
        builder.setTitle("");

        View customLayout = getLayoutInflater().inflate(R.layout.kurulum_hatasi_dialog, null);
        builder.setView(customLayout);

        TextView kurulumHatasiDialogRemainingTime = customLayout.findViewById(R.id.kurulumHatasiDialogRemainingTime);
        kurulumHatasiDialogRemainingTime.setText("5 ");

        builder.setOnDismissListener(new DialogInterface.OnDismissListener() {
            @Override
            public void onDismiss(DialogInterface dialog) {
                countDownTimerDialog = null;
//                isCountDownDialogRunning = false;
            }
        });
        countDownTimerDialog = builder.create();
        countDownTimerDialog.show();
//        isCountDownDialogRunning = true;

        timerCountDown = new Timer();
        timerCountDown.schedule(new TimerTask() {
            @Override
            public void run() {

                if (timeRemaining == 0) {
                    timerCountDown.cancel();
                    timerCountDown.purge();
                    timerCountDown = null;

                    countDownTimerDialog.dismiss();

                    try {

                        Helper.showWhiteScreen(SplashSelectorActivity.this);
                        Command command = new Command(0, "reboot");
                        RootTools.getShell(true).add(command).getExitCode();
                    } catch (Exception e) {
                        e.printStackTrace();
                    }

                    return;
                }
                timeRemaining = timeRemaining - 1;

                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        kurulumHatasiDialogRemainingTime.setText(timeRemaining + " ");
                    }
                });

            }
        }, 0, 1000);

    }

    private void resetDeviceIP() {

        try {

            Command command = new Command(0, "sqlite3 /data/data/com.android.providers.settings/databases/settings.db \"UPDATE global SET value = '0' WHERE name = 'eth_mode';\"",
                    "sqlite3 /data/data/com.android.providers.settings/databases/settings.db \"UPDATE global SET value = '" + "1.2.3.4" + "' WHERE name = 'eth_ip';\"",
                    "sqlite3 /data/data/com.android.providers.settings/databases/settings.db \"UPDATE global SET value = '255.255.255.255' WHERE name = 'eth_netmask';\"",
//                    "sqlite3 /data/data/com.android.providers.settings/databases/settings.db \"UPDATE global SET value = '192.168.1.207' WHERE name = 'eth_route';\"",
                    "sqlite3 /data/data/com.android.providers.settings/databases/settings.db \"UPDATE global SET value = '8.8.8.8' WHERE name = 'eth_dns';\"");
            RootTools.getShell(true).add(command).getExitCode();

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void setListenerToService() {

        Intent serviceConnIntent = new Intent(getApplicationContext(), NetworkScanService.class);
        bindService(serviceConnIntent, this, Context.BIND_AUTO_CREATE);

    }

    private void showRootNeededScreen() {

        Intent intent = new Intent(SplashSelectorActivity.this, RootNeededActivity.class);
        startActivity(intent);
        SplashSelectorActivity.this.finish();

    }

    public boolean isRooted() {
        try {
            Process process = Runtime.getRuntime().exec("su -v");
            process.waitFor();
//            Log.d(Constants.LOG_TAG, "isRooted result=" + result);

            BufferedReader in = new BufferedReader(new InputStreamReader(process.getInputStream()));
            String result = in.readLine();
//            Log.d(Constants.LOG_TAG, "isRooted result=" + result);

            if (result == null || result.equals("null")) {
                return false;
            }

            if (result.equals("1.61:SUPERSU")) {
                return true;
            } else {
                return false;
            }
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }

    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
//        isActivityAlive = false;

        if (isServiceBind) {
            unbindService(this);
        }

    }

    private void startInitialize() {
        Intent intent = new Intent(SplashSelectorActivity.this, SelectDeviceTypeActivity.class);
        startActivity(intent);
        SplashSelectorActivity.this.finish();
    }

    private void startBackgroundTCPServer() {

        stopServerService();

        startServerIn3Seconds();

    }

    private void startServerIn3Seconds() {

        Timer timer = new Timer();
        timer.schedule(new TimerTask() {
            @Override
            public void run() {
                startServerService();
                startMainActivity();
            }
        }, 3000);

    }

    private void startServerService() {

        Server server = new Server(SplashSelectorActivity.this);
        Intent serverIntent = new Intent(SplashSelectorActivity.this, server.getClass());
        if (!Helper.isMyServiceRunning(getApplicationContext(), server.getClass())) {
            startService(serverIntent);
        }

    }

    private void stopServerService() {
        Server server = new Server(SplashSelectorActivity.this);
        Intent serverIntent = new Intent(SplashSelectorActivity.this, server.getClass());
        stopService(serverIntent);
    }

    private void startKeyPadService() {

//        Thread t = new Thread() {
//            public void run() {
//                KeyPadService keyPadService = new KeyPadService(SplashSelectorActivity.this);
//                Intent keypadServiceIntent = new Intent(SplashSelectorActivity.this, keyPadService.getClass());
//                if (!Helper.isMyServiceRunning(getApplicationContext(), keyPadService.getClass())) {
//                    startService(keypadServiceIntent);
//                }
//            }
//        };
//        t.start();

//        new StartKeyPadServiceTask().executeOnExecutor(Constants.DEFAULT_EXECUTOR);

        KeyPadService keyPadService = new KeyPadService(SplashSelectorActivity.this);
        Intent keypadServiceIntent = new Intent(SplashSelectorActivity.this, keyPadService.getClass());
        if (!Helper.isMyServiceRunning(getApplicationContext(), keyPadService.getClass())) {
            startService(keypadServiceIntent);
        }

    }

    class StartKeyPadServiceTask extends AsyncTask<Void, Void, Void> {

        protected void onPreExecute() {
            super.onPreExecute();
        }

        protected Void doInBackground(Void... arg0) {
            KeyPadService keyPadService = new KeyPadService(SplashSelectorActivity.this);
            Intent keypadServiceIntent = new Intent(SplashSelectorActivity.this, keyPadService.getClass());
            if (!Helper.isMyServiceRunning(getApplicationContext(), keyPadService.getClass())) {
                startService(keypadServiceIntent);
            }
            return null;
        }

        protected void onPostExecute(Void aVoid) {
            super.onPostExecute(aVoid);
        }

    }


    private void startMainActivity() {

        Helper.setNextAlarm(getApplicationContext(), Helper.getNextAlarmTimeMillis());

        Intent intent = new Intent(SplashSelectorActivity.this, MainActivity.class);
        startActivity(intent);
        SplashSelectorActivity.this.finish();

    }

}

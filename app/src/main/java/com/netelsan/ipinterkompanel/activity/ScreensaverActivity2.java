package com.netelsan.ipinterkompanel.activity;

import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.os.Build;
import android.os.Handler;
import android.os.Bundle;
import android.os.IBinder;
import android.util.Log;
import android.view.View;
import android.view.ViewTreeObserver;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.netelsan.ipinterkompanel.Constants;
import com.netelsan.ipinterkompanel.Helper;
import com.netelsan.ipinterkompanel.R;
import com.netelsan.ipinterkompanel.database.DatabaseHelper;
import com.netelsan.ipinterkompanel.listener.KeyPadListener;
import com.netelsan.ipinterkompanel.model.Daire;
import com.netelsan.ipinterkompanel.model.DateTimeClass;
import com.netelsan.ipinterkompanel.model.DoorPassword;
import com.netelsan.ipinterkompanel.model.DoorUnlockLog;
import com.netelsan.ipinterkompanel.model.ZilPanel;
import com.netelsan.ipinterkompanel.model.ZilPanelSite;
import com.netelsan.ipinterkompanel.service.KeyPadService;
import com.netelsan.ipinterkompanel.tcp.Utils;
import com.stericson.RootShell.exceptions.RootDeniedException;
import com.stericson.RootShell.execution.Command;
import com.stericson.RootTools.RootTools;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Random;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.TimeoutException;

import static java.lang.Thread.sleep;


public class ScreensaverActivity2 extends BaseActivity implements ServiceConnection {

    RelativeLayout dayDreamContainer;
    RelativeLayout altAciklama;

    RelativeLayout dayDreamTimeContainer;
    RelativeLayout dayDreamAnimationContainer;

    TextView dayDreamDeviceName;
    TextView dayDreamHour;

    DatabaseHelper databaseHelper;


    boolean anindegiskontrol = false;


    public KeyPadService keyPadService;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_screensaver2);
        Intent serviceConnIntent = new Intent(this, KeyPadService.class);
        bindService(serviceConnIntent, this, Context.BIND_AUTO_CREATE);

        /*if (keyPadService != null) {
            keyPadService.addListener(ScreensaverActivity2.this, ScreensaverActivity2.class.getName());
        }*/
        lastUserInteractionTime2 = 0;
        dayDreamContainer = findViewById(R.id.dayDreamContainer2);
        dayDreamAnimationContainer = findViewById(R.id.dayDreamAnimationContainer2);
        dayDreamTimeContainer = findViewById(R.id.dayDreamTimeContainer2);
        dayDreamDeviceName = findViewById(R.id.dayDreamDeviceName2);
        dayDreamHour = findViewById(R.id.dayDreamHour2);
        altAciklama = findViewById(R.id.altAciklama2);
        anindegiskontrol = false;
        setTimeLayouts();
        changeLocationOfTime();

        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                try {
                    Command commandForDelete = new Command(0, "rm -r /storage/emulated/legacy/call_images");
                    int exitCodeForDelete = RootTools.getShell(true).add(commandForDelete).getExitCode();
                } catch (IOException e) {
                    e.printStackTrace();
                } catch (TimeoutException e) {
                    e.printStackTrace();
                } catch (RootDeniedException e) {
                    e.printStackTrace();
                }

//
            }
        }, 10000);


    }

    static Timer timer;
    public static long lastUserInteractionTime2 = 0;

    private void startTimer() {

        if (timer == null) {
            timer = new Timer();
        } else {
            return;
        }

        timer.schedule(new TimerTask() {
            @Override
            public void run() {
                setTimeLayouts();

                if (lastUserInteractionTime2 % 12 == 2) {
                    changeLocationOfTime();

                    if (zilRebootKontrol) {
                        rebootDevice();
                    }
                }
                lastUserInteractionTime2 = lastUserInteractionTime2 + 1;

                if (lastUserInteractionTime2 > 500) {
                    lastUserInteractionTime2 = 0;
                    Log.d(Constants.LOG_TAG, "HK screenSaver lastUserInteractionTime2 > 500");
                 /* Intent intent = new Intent(ScreensaverActivity2.this, MainActivity.class)
                    startActivity(intent);
                  //  ScreensaverActivity2.this.finish();
                    // rebootDevice();*/
                }

            }
        }, 1000, 1000);

    }

    private void rebootDevice() {
        try {
            Command command = new Command(0, "reboot");
            RootTools.getShell(true).add(command).getExitCode();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static boolean zilRebootKontrol = false;

    public void setTimeLayouts() {
        final String deviceName;
        databaseHelper = new DatabaseHelper(getApplicationContext());
        boolean isZilForSite = Helper.isZilForSite(getApplicationContext());
        if (isZilForSite) {
            ZilPanelSite zilPanelSiteSelf = databaseHelper.getSiteZilPanelByIP(Utils.getIPAddress(true));
            deviceName = zilPanelSiteSelf.getDeviceName();
        } else {
            ZilPanel zilPanelSelf = databaseHelper.getZilPanelByIP(Utils.getIPAddress(true));
            deviceName = zilPanelSelf.getDeviceName();
        }
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                String time = Helper.getCurrentHour() + " : " + Helper.getCurrentMinute();
                String hour = Helper.getCurrentHour();

                dayDreamDeviceName.setText(deviceName);
                dayDreamHour.setText(time);
            }
        });

    }

    Thread t;
    boolean threadStop = false;

    private void changeLocationOfTime() {

        dayDreamTimeContainer.getViewTreeObserver().addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
            @Override
            public void onGlobalLayout() {

                dayDreamTimeContainer.getViewTreeObserver().removeOnGlobalLayoutListener(this);

                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {

                        int min = 0;

                        int randomX = new Random().nextInt((101)) + min;
                        int randomY = new Random().nextInt((36)) + min;

                        //  Log.d(Constants.LOG_TAG, "1903 fey randomX=" + randomX + " randomY=" + randomY);

                        dayDreamTimeContainer.setX(randomX);
                        dayDreamTimeContainer.setY(randomY);


                        if (!anindegiskontrol) {

                            // final Handler h = new Handler();

                            final ImageView scroll = (ImageView) findViewById(R.id.image2);

                            t = new Thread() {
                                public void run() {
                                    int y = scroll.getScrollY() + 10;
                                    int x = -510; //scroll.getScrollX();


                                    while (true) {
//                                        final int X = x;
//                                        final int Y = y;
//
//                                        scroll.scrollTo(X, Y);
                                        scroll.scrollTo(x, y);

                                        x++;
                                        // if in içerisinden mainctivitye giden kod yaz
                                        if (x > 2025)
                                            x = -510;
                                        try {
                                            sleep(1000 / 80);
                                            //  sleep(2000 / 80);

                                        } catch (InterruptedException e) {
                                        }
                                        if (threadStop) {
                                            break;
                                        }

                                    }
                                }
                            };


                            t.start();


                            anindegiskontrol = true;
                        }
                    }
                });

            }
        });

    }


    @Override
    public void onServiceConnected(ComponentName name, IBinder service) {
   /*     keyPadService = ((KeyPadService.MyBinder) service).getService();
        keyPadService.addListener(ScreensaverActivity2.this, ScreensaverActivity2.class.getName());*/

    }

    @Override
    public void onServiceDisconnected(ComponentName name) {
        Log.d(Constants.LOG_TAG, "HK screenSaver onServiceDisconnected: " + name.toString());
        keyPadService = null;
    }


 /*   @Override
    public void onKeyPressed(String keyCode) {

        Log.d(Constants.LOG_TAG, "HK screenSaver onkeypressed: " + keyCode);
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                keyPressed(keyCode);
            }
        });

    }*/

    public static String rfScreenValue = "";
    public static boolean rfBooleanValue = false;

    /*@Override
    public void onRFIDDetected(String rfid) {

        rfBooleanValue = true;
        rfScreenValue = rfid;
        Intent intent = new Intent(ScreensaverActivity2.this, MainActivity.class);
        startActivity(intent);
        ScreensaverActivity2.this.finish();


        // hsn hsn hsn ------------------------------

//        DatabaseHelper databaseHelper = new DatabaseHelper(getApplicationContext());
//        DoorPassword doorPassword = databaseHelper.getPasswordByRFID(rfid);
//
//        Log.d(Constants.LOG_TAG, "processRFIDCode kapıyı " + doorPassword + " açtı");
//        if (doorPassword == null) {
//            String text = getString(R.string.gecersiz);
//            Helper.showTopMessageBanner((AppCompatActivity) ScreensaverActivity2.this, text, false);
//
//            return;  // bunlar kalkmalı bence
//        }
//
//        if (!doorPassword.isActive()) {
//            String text = getString(R.string.sifre_aktif_degil);
//            Helper.showTopMessageBanner((AppCompatActivity) ScreensaverActivity2.this, text, false);
//
//            return;  // bunlar kalkmalı bence
//        }
//
//        Log.d(Constants.LOG_TAG, "processRFIDCode kapıyı " + doorPassword.getPasswordLabel() + " açtı");
//
//        Calendar calendar = Calendar.getInstance();
//        SimpleDateFormat dateFormat = new SimpleDateFormat("dd.MM.yyyy HH:mm:ss");
//        String formattedDate = dateFormat.format(calendar.getTime());
//
//        DoorUnlockLog doorUnlockLog = new DoorUnlockLog();
//        doorUnlockLog.setUnlockType(Constants.DOOR_UNLOCK_RFID);
//        doorUnlockLog.setDatetime(formattedDate);
//        doorUnlockLog.setRfid(doorPassword.getRfid());
//        doorUnlockLog.setDoorPassword("");
//        doorUnlockLog.setPasswordOwnerIP(doorPassword.getIp());
//        doorUnlockLog.setPasswordLabel(doorPassword.getPasswordLabel());
//        databaseHelper.insertDoorLog(doorUnlockLog);
//
//        keyPadService.sendDoorUnlock(Constants.ENUM_DOOR_UNLOCK_MAIN);
//
//        Daire daire = databaseHelper.getDaireByIP(doorPassword.getIp());
//
//        String text = getString(R.string.hosgeldiniz) + "  " + daire.getIsim() + " " + daire.getSoyisim() + " (" + doorPassword.getPasswordLabel() + ")";
//        Helper.showTopMessageBanner((AppCompatActivity) ScreensaverActivity2.this, text, true);

        // hsn hsn -----------------------------
    }*/

    private BroadcastReceiver closeReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            if("com.netelsan.ipinterkompanel.CLOSE_SCREESAVER_ACTIVITY".equals(intent.getAction()))
            {
                Log.d("HK", "HK broadcast kapanma ");
                Intent intent2;
                intent2 = new Intent(getApplicationContext(), MainActivity.class);
                intent2.putExtra("keyPadLock", false);
                ScreensaverActivity2.lastUserInteractionTime2 = 0;
                startActivity(intent2);
                ScreensaverActivity2.this.finish();

            }
        }
    };


    public void keyPressed(String keyCode) {

        try {
            Log.d("HK", "HK pressed keycode: " + keyCode);
            Intent intent;

             intent = new Intent(getApplicationContext(), MainActivity.class);

            if (keyCode.equals(Constants.KEYPAD_LOCK)) {
                intent = new Intent(ScreensaverActivity2.this, MainActivity.class);
                intent.putExtra("keyPadLock", true);

            } else {
              //  intent = new Intent(ScreensaverActivity2.this, MainActivity.class);
                intent.putExtra("keyPadLock", false);
            }

            if (intent == null) {
                Log.d("HK", "HK intent null: ");
                return;
            }

            ScreensaverActivity2.lastUserInteractionTime2 = 0;

            startActivity(intent);
            this.overridePendingTransition(0, 0);

            ScreensaverActivity2.this.finish();
        } catch (Exception e) {
            e.printStackTrace();
        }


    }

    @Override
    protected void onPause() {
        super.onPause();
        if (timer != null) {
            timer.purge();
            timer.cancel();
            timer = null;
        }

        Log.d(Constants.LOG_TAG, "HK screenSaver onPause ");

        //  ScreensaverActivity2.this.finish();
    }

    @Override
    protected void onResume() {
        super.onResume();
        startTimer();
        Intent serviceConnIntent = new Intent(this, KeyPadService.class);
        this.bindService(serviceConnIntent, this, Context.BIND_AUTO_CREATE);
        Log.d(Constants.LOG_TAG, "HK screenSaver onResume ");
        IntentFilter filter = new IntentFilter("com.netelsan.ipinterkompanel.CLOSE_SCREESAVER_ACTIVITY");
        registerReceiver(closeReceiver, filter);

    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
       /* if (keyPadService != null) {
            keyPadService.removeListener(ScreensaverActivity2.class.getName());
        }*/

        Log.d(Constants.LOG_TAG, "HK screenSaver  pre onDestroy ");
        if (timer != null) {
            timer.purge();
            timer.cancel();
            timer = null;
        }

        threadStop = true;
        // unbindService(this);
        unregisterReceiver(closeReceiver);
        Log.d(Constants.LOG_TAG, "HK screenSaver onDestroy ");
        //ScreensaverActivity2.this.finish();
    }


}
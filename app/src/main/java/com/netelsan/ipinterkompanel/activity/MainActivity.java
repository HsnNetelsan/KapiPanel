package com.netelsan.ipinterkompanel.activity;

import android.app.ActivityManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.util.Log;
import android.view.View;
import android.view.ViewTreeObserver;
import android.view.animation.TranslateAnimation;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;

import com.netelsan.ipinterkompanel.Constants;
import com.netelsan.ipinterkompanel.Helper;
import com.netelsan.ipinterkompanel.R;
import com.netelsan.ipinterkompanel.ZilPanelApplication;
import com.netelsan.ipinterkompanel.database.DatabaseHelper;
import com.netelsan.ipinterkompanel.fragments.AyarlarFragment;
import com.netelsan.ipinterkompanel.fragments.DaireAraSiteFragment;
import com.netelsan.ipinterkompanel.fragments.GorevliFragment;
import com.netelsan.ipinterkompanel.fragments.GuvenlikFragment;
import com.netelsan.ipinterkompanel.fragments.IdleMenuFragment;
import com.netelsan.ipinterkompanel.fragments.KapiAcFragment;
import com.netelsan.ipinterkompanel.fragments.OnboardingFragment;
import com.netelsan.ipinterkompanel.fragments.RehberFragment;
import com.netelsan.ipinterkompanel.fragments.RehberSiteFragment;
import com.netelsan.ipinterkompanel.fragments.ScreensaverFragment;
import com.netelsan.ipinterkompanel.fragments.settings.AyarlarDoorLogsFragment;
import com.netelsan.ipinterkompanel.fragments.settings.AyarlarTeknikPersonelFragment;
import com.netelsan.ipinterkompanel.fragments.settings.AyarlarYoneticiFragment;
import com.netelsan.ipinterkompanel.listener.KeyPadListener;
import com.netelsan.ipinterkompanel.model.ComPackageModel;
import com.netelsan.ipinterkompanel.model.Daire;
import com.netelsan.ipinterkompanel.model.DateTimeClass;
import com.netelsan.ipinterkompanel.model.Guvenlik;
import com.netelsan.ipinterkompanel.model.ZilPanel;
import com.netelsan.ipinterkompanel.model.ZilPanelSite;
import com.netelsan.ipinterkompanel.service.KeyPadService;
import com.netelsan.ipinterkompanel.service.NetworkScanService;
import com.netelsan.ipinterkompanel.tcp.TCPHelper;
import com.netelsan.ipinterkompanel.tcp.Utils;
import com.stericson.RootShell.execution.Command;
import com.stericson.RootTools.RootTools;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Iterator;
import java.util.Random;
import java.util.Timer;
import java.util.TimerTask;

public class MainActivity extends BaseActivity implements ServiceConnection {

    RelativeLayout containerForMessageBanner;




    LinearLayout mainContainer;


    TextView topMenuHour;
    TextView topMenuDate;

    LinearLayout mainNetelsanRedLogo;
    TextView mainNetelsanLogo;
    LinearLayout mainNetelsanRegisteredLogo;
    ImageView mainNauffenLogo;
    boolean keyPadLock;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        containerForMessageBanner = findViewById(R.id.containerForMessageBanner);
        mainContainer = findViewById(R.id.mainContainer);

        topMenuHour = findViewById(R.id.topMenuHour);
        topMenuDate = findViewById(R.id.topMenuDate);
        mainNetelsanLogo = findViewById(R.id.mainNetelsanLogo);
        mainNetelsanRedLogo = findViewById(R.id.mainNetelsanRedLogo);
        mainNetelsanRegisteredLogo = findViewById(R.id.mainNetelsanRegisteredLogo);

        mainNauffenLogo = findViewById(R.id.mainNauffenLogo);

        setBrandLogo();

        setTimeLayouts();

        displayView(Constants.SCREEN_ONBOARDING, null);

        //burasıda test için kaldırıldı.
        // IntentFilter filter = new IntentFilter();
        // filter.addAction("com.netelsan.panel.KeyPadDayDream");
        // registerReceiver(keypadDayDreamBroadcastReceiver, filter);



        processIntent(getIntent());
    }

    private void processIntent(Intent intent) {

        if (intent == null) {
            return;
        }
        Bundle bundle = intent.getExtras();
        if (bundle == null) {
            return;
        }

        keyPadLock = bundle.getBoolean("keyPadLock");
        if (keyPadLock) {
            displayView(Constants.SCREEN_KAPI_AC, null);
        }

    }

    private void setBrandLogo() {

        if (Constants.IS_DEVICE_NAUFFEN) {
            mainNetelsanLogo.setVisibility(View.GONE);
            mainNetelsanRedLogo.setVisibility(View.GONE);
            mainNetelsanRegisteredLogo.setVisibility(View.GONE);
            mainNauffenLogo.setVisibility(View.VISIBLE);
        } else {
            mainNetelsanLogo.setVisibility(View.VISIBLE);
            mainNetelsanRedLogo.setVisibility(View.VISIBLE);
            mainNetelsanRegisteredLogo.setVisibility(View.VISIBLE);
            mainNauffenLogo.setVisibility(View.GONE);
        }


        boolean isCenterUnitZilPanel = Helper.isCenterUnitZilPanel(getApplicationContext());
        boolean isZilForSite = Helper.isZilForSite(getApplicationContext());

        databaseHelper = new DatabaseHelper(getApplicationContext());
        if (!isZilForSite) {
            if (isCenterUnitZilPanel) {
                String year = Helper.getCurrentYear();
                if (year.equals("1970")) {
                    new Handler().postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            databaseHelper = new DatabaseHelper(getApplicationContext());
                            DateTimeClass zilDateTime = new DateTimeClass();
                            try {
                                databaseHelper.createZilDateTime();
                            } catch (Exception e) {

                            }
                            zilDateTime = databaseHelper.getZilDateTimeById(1);

                            if (zilDateTime != null) {
                                if (zilDateTime.getYear() != 1970) {
                                    setDeviceSettingsAndRestart(zilDateTime);
                                }
                            }
                        }
                    }, 30000);


                }
            }
        }


    }

    @Override
    public void onServiceConnected(ComponentName name, IBinder service) {

    }

    @Override
    public void onServiceDisconnected(ComponentName name) {

    }

    static Timer timer;
    public static long lastUserInteractionTime = 0;


    private void startTimer() {

//        start();

        if (timer == null) {
            timer = new Timer();
        } else {
            return;
        }

        timer.schedule(new TimerTask() {
            @Override
            public void run() {
                lastUserInteractionTime = lastUserInteractionTime + 1;

                if (lastUserInteractionTime < Constants.IDLE_TIME) {
                    setTimeLayouts();
                    return;
                }

                setDayDreamScreen();

            }
        }, 1000, 1000);

    }

    @Override
    protected void onPause() {
        super.onPause();


        if (timer != null) {
            timer.purge();
            timer.cancel();
            timer = null;
        }
    }



    @Override
    protected void onResume() {
        super.onResume();

        startTimer();
    }


    //burası komple kaldırılıyor
   /* BroadcastReceiver keypadDayDreamBroadcastReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            setDayDreamScreen();
        }
    };*/

    @Override
    protected void onDestroy() {
        super.onDestroy();

        if (timer != null) {
            timer.purge();
            timer.cancel();
            timer = null;
        }


       /* try {
            if (keypadDayDreamBroadcastReceiver != null) {
                unregisterReceiver(keypadDayDreamBroadcastReceiver);
            }
        } catch (Exception e) {

        }*/


//        stop();

//        isActivityAlive = false;
    }

    int sayac = 0;

    public void setTimeLayouts() {

        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                String time = Helper.getCurrentHour() + " : " + Helper.getCurrentMinute();
                String date = Helper.getCurrentDayOfMonth() + " " + Helper.getCurrentMonthName() + " " + Helper.getCurrentYear();

                topMenuHour.setText(time);
                topMenuDate.setText(date);


                boolean isCenterUnitZilPanel = Helper.isCenterUnitZilPanel(getApplicationContext());
                boolean isZilForSite = Helper.isZilForSite(getApplicationContext());


                if (!isZilForSite) {
                    if (isCenterUnitZilPanel) {
                        sayac++;
                        if (sayac % 1 == 30) {
                            sayac = 0;
                            setDeviceTimeSave();

                        }
                    }
                }

            }
        });

    }

    private void setDeviceTimeSave() {
        Calendar calendar = Calendar.getInstance();

        int currentYear = calendar.get(Calendar.YEAR);

        if (currentYear != 1970) {

            DatabaseHelper databaseHelper = new DatabaseHelper(getApplicationContext());

            try {
                databaseHelper.createZilDateTime();
            } catch (Exception e) {

            }
            DateTimeClass zilDateTime2 = new DateTimeClass();
            zilDateTime2 = databaseHelper.getZilDateTimeById(1);
            calendar = Calendar.getInstance();
            int currentHour = calendar.get(Calendar.HOUR_OF_DAY);
            int currentMinute = calendar.get(Calendar.MINUTE);
            currentYear = calendar.get(Calendar.YEAR);
            int currentDay = calendar.get(Calendar.DAY_OF_MONTH);
            int currentMonth = calendar.get(Calendar.MONTH);

            if (zilDateTime2 == null) {
                zilDateTime2 = new DateTimeClass();
                zilDateTime2.setMinute(currentMinute);
                zilDateTime2.setHour(currentHour);
                zilDateTime2.setDayofMonth(currentDay);
                zilDateTime2.setMonth(currentMonth);
                zilDateTime2.setYear(currentYear);
                databaseHelper.insertZilDateTime(zilDateTime2);
            } else {
                zilDateTime2.setId(1);
                zilDateTime2.setMinute(currentMinute);
                zilDateTime2.setHour(currentHour);
                zilDateTime2.setDayofMonth(currentDay);
                zilDateTime2.setMonth(currentMonth);
                zilDateTime2.setYear(currentYear);
                databaseHelper.updateZilDateTime(zilDateTime2);
            }


        }

    }


    DatabaseHelper databaseHelper;

    private void setDeviceSettingsAndRestart(DateTimeClass zilDateTime) {

        long newTimeMillis = setDeviceTime(zilDateTime);

        boolean isForSite = Helper.isZilForSite(getApplicationContext());
        if (!isForSite) {

            //burada kendi blogundaki cihazlara zaman değişti mesajı gönder
            databaseHelper = new DatabaseHelper(getApplicationContext());
            ZilPanel zilPanel = databaseHelper.getZilPanelByIP(Utils.getIPAddress(true));
            ArrayList<Daire> arrayList = databaseHelper.getDairelerForBlok(zilPanel.getBlok());
            if (arrayList.size() != 0) {

                Iterator<Daire> iterator = arrayList.iterator();
                while (iterator.hasNext()) {
                    Daire daire = iterator.next();

                    String destinationIP = daire.getIp();

                    ComPackageModel model = new ComPackageModel();
                    model.setOpe_type(Constants.OPERATION_GET_DATE_TIME_RESPONSE);
                    model.setDataLong(newTimeMillis);

                    TCPHelper.sendMessageToIP(getApplicationContext(), destinationIP, model);

                }

            }

            ArrayList<Guvenlik> guvenlikler = databaseHelper.getGuvenlikler();
            if (guvenlikler.size() != 0) {
                Iterator<Guvenlik> iterator = guvenlikler.iterator();
                while (iterator.hasNext()) {
                    Guvenlik guvenlik = iterator.next();
                    String destinationIP = guvenlik.getIp();

                    ComPackageModel model = new ComPackageModel();
                    model.setOpe_type(Constants.OPERATION_GET_DATE_TIME_RESPONSE);
                    model.setDataLong(newTimeMillis);
                    TCPHelper.sendMessageToIP(getApplicationContext(), destinationIP, model);
                }
            }

        }

        //1 saniyelik bir bekleme yap. TCP mesajlarının gittiğinden emin olalım
        Timer timer = new Timer();
        timer.schedule(new TimerTask() {
            @Override
            public void run() {
                //  rebootDevice();
            }
        }, 1000);

    }

    private long setDeviceTime(DateTimeClass zilDateTime) {

        Calendar calendar = Calendar.getInstance();
        calendar.set(Calendar.YEAR, zilDateTime.getYear());
        calendar.set(Calendar.MONTH, zilDateTime.getMonth());
        calendar.set(Calendar.DAY_OF_MONTH, zilDateTime.getDayofMonth());
        calendar.set(Calendar.MINUTE, zilDateTime.getMinute());
        calendar.set(Calendar.HOUR_OF_DAY, zilDateTime.getHour());
        calendar.set(Calendar.SECOND, 0);


        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyyMMdd.HHmmss");
        String formattedDate = dateFormat.format(calendar.getTime());
//        Log.d(Constants.LOG_TAG, "formattedDate=" + formattedDate);

        try {
//            Process process = Runtime.getRuntime().exec("su 0 toolbox date -s 20191015.084000");//YYYYMMDD.HHmmss
            Process process = Runtime.getRuntime().exec("su 0 toolbox date -s " + formattedDate);//YYYYMMDD.HHmmss
            BufferedReader in = new BufferedReader(new InputStreamReader(process.getInputStream()));

//            refreshDateAndTime(calendar);

        } catch (Exception e) {
            e.printStackTrace();
        }

        return calendar.getTimeInMillis();
    }


    private void rebootDevice() {
        try {
            Helper.showWhiteScreen((AppCompatActivity) getApplicationContext());
            Command command = new Command(0, "reboot");
            RootTools.getShell(true).add(command).getExitCode();

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void setDayDreamScreen() {

        Intent intent = new Intent(MainActivity.this, ScreensaverActivity2.class);
        startActivity(intent);
        this.overridePendingTransition(0, 0);
        MainActivity.this.finish();
    }

    Fragment selectedFragment = null;

    public void displayView(int screenId, Bundle bundle) {

        boolean isZilForSite = Helper.isZilForSite(getApplicationContext());

        Fragment fragment = null;

        switch (screenId) {

            case Constants.SCREEN_IDLE_MENU:
                fragment = new IdleMenuFragment();
                break;

            case Constants.SCREEN_ONBOARDING:
                fragment = new OnboardingFragment();
                break;

            case Constants.SCREEN_REHBER:
                if (isZilForSite) {
                    fragment = new RehberSiteFragment();
                } else {
                    fragment = new RehberFragment();
                }
                break;

            case Constants.SCREEN_GOREVLI:
                fragment = new GorevliFragment();
                break;

            case Constants.SCREEN_GUVENLIK:
                fragment = new GuvenlikFragment();
                break;

            case Constants.SCREEN_DAIRE_ARA:
                fragment = new DaireAraSiteFragment();
//                if(isZilForSite){
//                }else{
//                    fragment = new DaireAraFragment();
//                }
                break;

            case Constants.SCREEN_KAPI_AC:
                fragment = new KapiAcFragment();
                break;

            case Constants.SCREEN_AYARLAR:
                fragment = new AyarlarFragment();
                break;

            case Constants.SCREEN_AYARLAR_YONETICI:
                fragment = new AyarlarYoneticiFragment();
                break;

            case Constants.SCREEN_AYARLAR_TEKNIK_PERSONEL:
                fragment = new AyarlarTeknikPersonelFragment();
                break;

            case Constants.SCREEN_AYARLAR_DOOR_LOGS:
                fragment = new AyarlarDoorLogsFragment();
                break;

            default:
                break;
        }

        if (fragment != null) {

            // MainActivity.lastUserInteractionTime = 0;

            FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();

//            Bundle bundle = new Bundle();
//            bundle.putBoolean("isNeedTitle", true);
            if (bundle != null) {
                fragment.setArguments(bundle);
            }

            selectedFragment = fragment;
            transaction.replace(R.id.frameLayout, fragment);
            transaction.commitAllowingStateLoss();
        }

    }


}

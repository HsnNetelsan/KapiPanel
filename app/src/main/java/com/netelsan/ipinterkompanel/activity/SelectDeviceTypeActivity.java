package com.netelsan.ipinterkompanel.activity;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Build;
import android.os.Bundle;
import android.os.IBinder;
import android.view.View;
import android.widget.ImageView;

import androidx.annotation.Nullable;

import com.netelsan.ipinterkompanel.Constants;
import com.netelsan.ipinterkompanel.Helper;
import com.netelsan.ipinterkompanel.R;
import com.netelsan.ipinterkompanel.listener.KeyPadListener;
import com.netelsan.ipinterkompanel.service.KeyPadService;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.text.SimpleDateFormat;
import java.util.Calendar;

public class SelectDeviceTypeActivity extends BaseActivity implements KeyPadListener, ServiceConnection {

    ImageView setZilPaneliArrow;
    ImageView setSiteZilPanelArrow;

    int selectedMenuId = Constants.SELECT_DEVICE_MENU_ZIL;

    KeyPadService keyPadService;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_select_device_type);
//
        setZilPaneliArrow = findViewById(R.id.setZilPaneliArrow);
        setSiteZilPanelArrow = findViewById(R.id.setSiteZilPanelArrow);

        Intent serviceConnIntent = new Intent(this, KeyPadService.class);
        bindService(serviceConnIntent, this, Context.BIND_AUTO_CREATE);

        if (keyPadService != null) {
            keyPadService.addListener(SelectDeviceTypeActivity.this, SelectDeviceTypeActivity.class.getName());
        }

        hideSystemUi();

//        boolean isNeedRebootForRoot = isNeedRebootForRoot();
//        if (isNeedRebootForRoot) {
//            rebootDevice();
//            return;
//        }

    }

    private void rebootDevice() {
        try {
            Helper.showWhiteScreen(SelectDeviceTypeActivity.this);
            Process process = Runtime.getRuntime().exec("adb reboot");
            process.waitFor();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

//    private boolean isNeedRebootForRoot() {
//
//        boolean isRebootedForRoot = Helper.isRebootedForRoot(getApplicationContext());
//        if (isRebootedForRoot) {
//            return false;
//        }
//        Helper.setRebootedForRoot(getApplicationContext(), true);
//        return true;
//    }

    private void hideSystemUi() {
        try {
            Build.VERSION_CODES vc = new Build.VERSION_CODES();
            Build.VERSION vr = new Build.VERSION();
            String ProcID = "79"; //HONEYCOMB AND OLDER

            //v.RELEASE  //4.0.3
            if (vr.SDK_INT >= vc.ICE_CREAM_SANDWICH) {
                ProcID = "42"; //ICS AND NEWER
            }
            Process proc = Runtime.getRuntime().exec(new String[]{"su", "-c", "service call activity " + ProcID + " s16 com.android.systemui"});

            proc.waitFor();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onServiceConnected(ComponentName name, IBinder binder) {
        keyPadService = ((KeyPadService.MyBinder) binder).getService();
        keyPadService.addListener(SelectDeviceTypeActivity.this, SelectDeviceTypeActivity.class.getName());
    }

    @Override
    public void onServiceDisconnected(ComponentName name) {
        keyPadService = null;
    }

    @Override
    public void onKeyPressed(String keyCode) {

        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                keyPressed(keyCode);
            }
        });
    }

    @Override
    public void onRFIDDetected(String rfid) {

    }

    private void keyPressed(String keyCode) {

        if (keyCode.equals(Constants.KEYPAD_BACK)) {
            menuBackPressed();
        } else if (keyCode.equals(Constants.KEYPAD_UP)) {
            menuArrowClicked(true);
        } else if (keyCode.equals(Constants.KEYPAD_DOWN)) {
            menuArrowClicked(false);
        } else if (keyCode.equals(Constants.KEYPAD_HOME)) {
            menuOkayClicked();
        } else if (keyCode.equals(Constants.KEYPAD_CALL)) {

        } else if (keyCode.equals(Constants.KEYPAD_LOCK)) {

        } else {

        }

    }

    private void menuBackPressed() {

    }

    private void menuOkayClicked() {


        Intent intent = null;

        if (selectedMenuId == Constants.SELECT_DEVICE_MENU_ZIL) {
            intent = new Intent(SelectDeviceTypeActivity.this, SelectSystemCenterDeviceActivity.class);
            intent.putExtra("isSite", false);
        } else if (selectedMenuId == Constants.SELECT_DEVICE_MENU_SITE) {
            intent = new Intent(SelectDeviceTypeActivity.this, SelectSystemCenterDeviceActivity.class);
            intent.putExtra("isSite", true);
        }

        if (intent == null) {
            return;
        }

        startActivity(intent);
        this.overridePendingTransition(0, 0);
        SelectDeviceTypeActivity.this.finish();

    }

    private void menuArrowClicked(boolean isUp) {

        moveMenuArrow(isUp);

    }

    private void moveMenuArrow(boolean isUp) {

        hideAllArrows();

        if (isUp) {

            if (selectedMenuId == Constants.SELECT_DEVICE_MENU_ZIL) {
                selectedMenuId = Constants.SELECT_DEVICE_MENU_SITE;
            } else {
                selectedMenuId = selectedMenuId - 1;
            }

        } else {

            if (selectedMenuId == Constants.SELECT_DEVICE_MENU_SITE) {
                selectedMenuId = Constants.SELECT_DEVICE_MENU_ZIL;
            } else {
                selectedMenuId = selectedMenuId + 1;
            }
        }

        setMenuArrow();

    }

    private void setMenuArrow() {

        if (selectedMenuId == Constants.SELECT_DEVICE_MENU_ZIL) {
            setZilPaneliArrow.setVisibility(View.VISIBLE);
        } else if (selectedMenuId == Constants.SELECT_DEVICE_MENU_SITE) {
            setSiteZilPanelArrow.setVisibility(View.VISIBLE);
        }

    }

    private void hideAllArrows() {

        setZilPaneliArrow.setVisibility(View.INVISIBLE);
        setSiteZilPanelArrow.setVisibility(View.INVISIBLE);
    }

    private void setDeviceTime(int hour, int minute, int dayofMonth, int month, int year) {

        Calendar calendar = Calendar.getInstance();
        calendar.set(Calendar.YEAR, year);
        calendar.set(Calendar.MONTH, month);
        calendar.set(Calendar.DAY_OF_MONTH, dayofMonth);
        calendar.set(Calendar.MINUTE, minute);
        calendar.set(Calendar.HOUR_OF_DAY, hour);
        calendar.set(Calendar.SECOND, 30);

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

    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
//        isActivityAlive = false;

        if (keyPadService != null) {
            keyPadService.removeListener(SelectDeviceTypeActivity.class.getName());
        }

        unbindService(this);

    }
}

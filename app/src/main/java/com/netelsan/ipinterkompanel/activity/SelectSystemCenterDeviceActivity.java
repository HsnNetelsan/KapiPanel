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
import android.widget.LinearLayout;

import androidx.annotation.Nullable;

import com.netelsan.ipinterkompanel.Constants;
import com.netelsan.ipinterkompanel.R;
import com.netelsan.ipinterkompanel.listener.KeyPadListener;
import com.netelsan.ipinterkompanel.service.KeyPadService;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.text.SimpleDateFormat;
import java.util.Calendar;

public class SelectSystemCenterDeviceActivity extends BaseActivity implements KeyPadListener, ServiceConnection {

    LinearLayout selectSystemCenterZilContainer;
    LinearLayout selectSystemCenterGuvenlikContainer;

    ImageView selectSystemCenterZilArrow;
    ImageView selectSystemCenterGuvenlikArrow;

    int selectedMenuId = Constants.SELECT_CENTER_DEVICE_MENU_ZIL;

    KeyPadService keyPadService;

    boolean isSite;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_select_system_center);

        selectSystemCenterZilContainer = findViewById(R.id.selectSystemCenterZilContainer);
        selectSystemCenterGuvenlikContainer = findViewById(R.id.selectSystemCenterGuvenlikContainer);

        selectSystemCenterZilArrow = findViewById(R.id.selectSystemCenterZilArrow);
        selectSystemCenterGuvenlikArrow = findViewById(R.id.selectSystemCenterGuvenlikArrow);

        Intent serviceConnIntent = new Intent(this, KeyPadService.class);
        bindService(serviceConnIntent, this, Context.BIND_AUTO_CREATE);

        if(keyPadService != null) {
            keyPadService.addListener(SelectSystemCenterDeviceActivity.this, SelectSystemCenterDeviceActivity.class.getName());
        }

        processIntent(getIntent());

    }

    private void processIntent(Intent intent) {

        if(intent == null) {
            return;
        }
        Bundle bundle = intent.getExtras();
        if(bundle == null) {
            return;
        }

        isSite = bundle.getBoolean("isSite");

    }

    @Override
    public void onServiceConnected(ComponentName name, IBinder binder) {
        keyPadService = ((KeyPadService.MyBinder) binder).getService();
        keyPadService.addListener(SelectSystemCenterDeviceActivity.this, SelectSystemCenterDeviceActivity.class.getName());
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

        if(keyCode.equals(Constants.KEYPAD_BACK)) {
            menuBackPressed();
        } else if(keyCode.equals(Constants.KEYPAD_UP)) {
            menuArrowClicked(true);
        } else if(keyCode.equals(Constants.KEYPAD_DOWN)) {
            menuArrowClicked(false);
        } else if(keyCode.equals(Constants.KEYPAD_HOME)) {
            menuOkayClicked();
        } else if(keyCode.equals(Constants.KEYPAD_CALL)) {

        } else if(keyCode.equals(Constants.KEYPAD_LOCK)) {

        } else {

        }

    }

    private void menuBackPressed() {

        Intent intent = new Intent(SelectSystemCenterDeviceActivity.this, SelectDeviceTypeActivity.class);
        startActivity(intent);
        SelectSystemCenterDeviceActivity.this.finish();
    }

    private void menuOkayClicked() {

        Intent intent = null;
        if(isSite) {
            intent = new Intent(SelectSystemCenterDeviceActivity.this, InitializeSiteActivity.class);
        } else {
            intent = new Intent(SelectSystemCenterDeviceActivity.this, InitializeActivity.class);
        }

        if(selectedMenuId == Constants.SELECT_CENTER_DEVICE_MENU_ZIL) {
            intent.putExtra("isCenterDeviceZil", true);
        } else {
            intent.putExtra("isCenterDeviceZil", false);
        }

        intent.putExtra("isSite", isSite);

        if(intent == null) {
            return;
        }

        startActivity(intent);
        this.overridePendingTransition(0, 0);
        SelectSystemCenterDeviceActivity.this.finish();

    }

    private void menuArrowClicked(boolean isUp) {

        moveMenuArrow(isUp);

    }

    private void moveMenuArrow(boolean isUp) {

        hideAllArrows();

        if(isUp) {

            if(selectedMenuId == Constants.SELECT_CENTER_DEVICE_MENU_ZIL) {
                selectedMenuId = Constants.SELECT_DEVICE_MENU_SITE;
            } else {
                selectedMenuId = selectedMenuId - 1;
            }

        } else {

            if(selectedMenuId == Constants.SELECT_DEVICE_MENU_SITE) {
                selectedMenuId = Constants.SELECT_CENTER_DEVICE_MENU_ZIL;
            } else {
                selectedMenuId = selectedMenuId + 1;
            }
        }

        setMenuArrow();

    }

    private void setMenuArrow() {

        if(selectedMenuId == Constants.SELECT_CENTER_DEVICE_MENU_ZIL) {
            selectSystemCenterZilArrow.setVisibility(View.VISIBLE);
            selectSystemCenterZilContainer.setBackgroundResource(R.drawable.black_bordered_btn_bg);
        } else if(selectedMenuId == Constants.SELECT_DEVICE_MENU_SITE) {
            selectSystemCenterGuvenlikArrow.setVisibility(View.VISIBLE);
            selectSystemCenterGuvenlikContainer.setBackgroundResource(R.drawable.black_bordered_btn_bg);
        }

    }

    private void hideAllArrows() {

        selectSystemCenterZilContainer.setBackgroundResource(0);
        selectSystemCenterGuvenlikContainer.setBackgroundResource(0);

        selectSystemCenterZilArrow.setVisibility(View.INVISIBLE);
        selectSystemCenterGuvenlikArrow.setVisibility(View.INVISIBLE);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

        if(keyPadService != null) {
            keyPadService.removeListener(SelectSystemCenterDeviceActivity.class.getName());
        }

        unbindService(this);

    }
}

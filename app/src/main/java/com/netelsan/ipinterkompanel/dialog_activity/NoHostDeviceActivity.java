package com.netelsan.ipinterkompanel.dialog_activity;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.graphics.Point;
import android.os.Bundle;
import android.os.IBinder;
import android.util.Log;
import android.view.Display;
import android.view.View;
import android.view.Window;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.Nullable;

import com.netelsan.ipinterkompanel.Constants;
import com.netelsan.ipinterkompanel.R;
import com.netelsan.ipinterkompanel.listener.KeyPadListener;
import com.netelsan.ipinterkompanel.service.KeyPadService;

import java.sql.Time;
import java.util.Timer;
import java.util.TimerTask;

public class NoHostDeviceActivity extends BaseDialogActivity implements ServiceConnection, KeyPadListener {

    TextView noHostDeviceDialogCloseText;

    TextView noHostDeviceTitle;
    TextView noHostDeviceSubtitle;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        requestWindowFeature(Window.FEATURE_NO_TITLE);

        setContentView(R.layout.activity_no_host_device);

        noHostDeviceDialogCloseText = findViewById(R.id.noHostDeviceDialogCloseText);
        noHostDeviceTitle = findViewById(R.id.noHostDeviceTitle);
        noHostDeviceSubtitle = findViewById(R.id.noHostDeviceSubtitle);

        setFinishOnTouchOutside(false);

        Display display = getWindowManager().getDefaultDisplay();
        Point size = new Point();
        display.getSize(size);
        int width = size.x;
        int height = size.y;

        getWindow().setLayout(((int) (width * Constants.CUSTOM_DIALOG_WIDTH)), ((int) (height * Constants.CUSTOM_DIALOG_HEIGHT)));

        Intent serviceConnIntent = new Intent(this, KeyPadService.class);
        bindService(serviceConnIntent, this, Context.BIND_AUTO_CREATE);

        processIntent(getIntent());

    }

    KeyPadService keyPadService;

    @Override
    public void onServiceConnected(ComponentName name, IBinder binder) {
        keyPadService = ((KeyPadService.MyBinder) binder).getService();
        keyPadService.addListener(NoHostDeviceActivity.this, NoHostDeviceActivity.class.getName());
    }

    @Override
    public void onServiceDisconnected(ComponentName name) {

    }

    @Override
    public void onKeyPressed(String keyCode) {
        if(keyCode.equals(Constants.KEYPAD_BACK)) {
            NoHostDeviceActivity.this.finish();
        }
    }

    @Override
    public void onRFIDDetected(String rfid) {

    }

    private void processIntent(Intent intent) {

        if(intent == null) {
            return;
        }

        Bundle bundle = intent.getExtras();
        if(bundle == null) {
            return;
        }

        String title = bundle.getString("title");
        String subtitle = bundle.getString("subtitle");

        setLayouts(title, subtitle);

    }

    private void setLayouts(String title, String subtitle) {

        noHostDeviceTitle.setText(title);
        noHostDeviceSubtitle.setText(subtitle);

        startTimer();

    }

    Timer timerForDissmiss;
    int timeForDissmis = 5;

    private void startTimer() {

        timeForDissmis = 5;

        timerForDissmiss = new Timer();
        timerForDissmiss.schedule(new TimerTask() {
            @Override
            public void run() {

                if(timeForDissmis == 0) {

                    if(timerForDissmiss != null) {
                        timerForDissmiss.purge();
                        timerForDissmiss.cancel();
                        timerForDissmiss = null;
                    }

                    NoHostDeviceActivity.this.finish();

                }

                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        Log.d(Constants.LOG_TAG, "feyy feyy  noHostDeviceDialogCloseText1");
                        noHostDeviceDialogCloseText.setText(getString(R.string.saniye_sonra_kapanacak_1) + " " + timeForDissmis + " " + getString(R.string.saniye_sonra_kapanacak_2));
                    }
                });

                timeForDissmis = timeForDissmis - 1;

            }
        }, 0, 1000);

    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

        if(timerForDissmiss != null) {
            timerForDissmiss.purge();
            timerForDissmiss.cancel();
            timerForDissmiss = null;
        }

        if(keyPadService != null) {
            keyPadService.removeListener(NoHostDeviceActivity.class.getName());
        }

        unbindService(this);

    }

}

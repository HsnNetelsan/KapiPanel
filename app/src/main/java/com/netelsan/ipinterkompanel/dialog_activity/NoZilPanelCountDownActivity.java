package com.netelsan.ipinterkompanel.dialog_activity;

import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.graphics.Point;
import android.os.Bundle;
import android.os.IBinder;
import android.util.Log;
import android.view.Display;
import android.view.Window;
import android.widget.TextView;

import androidx.annotation.Nullable;

import com.netelsan.ipinterkompanel.Constants;
import com.netelsan.ipinterkompanel.Helper;
import com.netelsan.ipinterkompanel.R;
import com.netelsan.ipinterkompanel.activity.SplashSelectorActivity;
import com.netelsan.ipinterkompanel.listener.KeyPadListener;
import com.netelsan.ipinterkompanel.model.Daire;
import com.netelsan.ipinterkompanel.model.Guvenlik;
import com.netelsan.ipinterkompanel.service.CapturePhotoService;
import com.netelsan.ipinterkompanel.service.KeyPadService;
import com.stericson.RootShell.execution.Command;
import com.stericson.RootTools.RootTools;

import java.util.ArrayList;
import java.util.Timer;
import java.util.TimerTask;

public class NoZilPanelCountDownActivity extends BaseDialogActivity implements KeyPadListener {

    TextView countdownDialogRemainingTime;

    int timeRemaining = 4;
    Timer timerCountDown;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        requestWindowFeature(Window.FEATURE_NO_TITLE);

        setContentView(R.layout.activity_no_zil_panel_count_down);

        countdownDialogRemainingTime = findViewById(R.id.countdownDialogRemainingTime);

        setFinishOnTouchOutside(false);

        Display display = getWindowManager().getDefaultDisplay();
        Point size = new Point();
        display.getSize(size);
        int width = size.x;
        int height = size.y;

        getWindow().setLayout(((int) (width * Constants.CUSTOM_DIALOG_WIDTH)), ((int) (height * Constants.CUSTOM_DIALOG_HEIGHT)));

        timeRemaining = 5;

        setLayouts();

        countdownDialogRemainingTime.setText("5 ");
    }

    @Override
    public void onKeyPressed(String keyCode) {
        if(keyCode.equals(Constants.KEYPAD_BACK)) {
        }
    }

    @Override
    public void onRFIDDetected(String rfid) {

    }

    private void setLayouts() {

        timerCountDown = new Timer();
        timerCountDown.schedule(new TimerTask() {
            @Override
            public void run() {

                if(timeRemaining == 0) {
                    timerCountDown.cancel();
                    timerCountDown.purge();
                    timerCountDown = null;

                    try {
                        Helper.showWhiteScreen(NoZilPanelCountDownActivity.this);
                        Command command = new Command(0, "reboot");
                        RootTools.getShell(true).add(command).getExitCode();
                    } catch(Exception e) {
                        e.printStackTrace();
                    }

                    return;
                }

                timeRemaining = timeRemaining - 1;

                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        countdownDialogRemainingTime.setText(timeRemaining + " ");
                    }
                });

            }
        }, 0, 1000);

    }

    @Override
    protected void onDestroy() {

        if(timerCountDown != null) {
            timerCountDown.purge();
            timerCountDown.cancel();
            timerCountDown = null;
        }

        super.onDestroy();

    }

}

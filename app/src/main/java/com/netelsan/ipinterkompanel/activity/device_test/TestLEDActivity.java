package com.netelsan.ipinterkompanel.activity.device_test;

import android.os.Bundle;
import android.view.View;
import android.widget.LinearLayout;

import com.netelsan.ipinterkompanel.Constants;
import com.netelsan.ipinterkompanel.Helper;
import com.netelsan.ipinterkompanel.R;
import com.netelsan.ipinterkompanel.model.DeviceTest;

import java.util.Timer;
import java.util.TimerTask;

public class TestLEDActivity extends BaseTestActivity implements View.OnClickListener {

    LinearLayout ledTestiNegative;
    LinearLayout ledTestiPositive;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_test_led);

        ledTestiNegative = findViewById(R.id.ledTestiNegative);
        ledTestiPositive = findViewById(R.id.ledTestiPositive);

        startLedLights();

    }

    @Override
    public void onKeyPressed(String keyCode) {
        if (keyCode.equals(Constants.KEYPAD_BACK)) {
            resultClicked(false);
        } else if (keyCode.equals(Constants.KEYPAD_UP)) {

        } else if (keyCode.equals(Constants.KEYPAD_DOWN)) {

        } else if (keyCode.equals(Constants.KEYPAD_HOME)) {
            resultClicked(true);
        } else if (keyCode.equals(Constants.KEYPAD_CALL)) {

        } else if (keyCode.equals(Constants.KEYPAD_LOCK)) {

        } else {

        }
    }

    boolean isLedOn = false;
    Timer timerForLEDs = null;

    private void startLedLights() {

        if(timerForLEDs == null) {
            timerForLEDs = new Timer();
        }
        timerForLEDs.schedule(new TimerTask() {
            @Override
            public void run() {

                setLedState(isLedOn);

                isLedOn = ! isLedOn;
            }
        }, 0, 300);

    }

    @Override
    public void onClick(View view) {

        switch(view.getId()) {

            default:
                break;

        }
    }

    private void resultClicked(boolean isPositive) {

        DeviceTest deviceTest = Helper.getDeviceTest(getApplicationContext());
        deviceTest.setLEDTestiOkay(isPositive);
        Helper.setDeviceTest(getApplicationContext(), deviceTest);
        TestLEDActivity.this.finish();

    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

        if(timerForLEDs != null) {
            timerForLEDs.purge();
            timerForLEDs.cancel();
            timerForLEDs = null;
        }

        setLedState(false);
    }

    private void setLedState(boolean isActive) {

        int ledPortNumber = Helper.getPINFromGPIO("PE17");

        Process process = Helper.getProcess();
        Helper.setGPIO(process, Constants.GPIO_EXPORT, ledPortNumber, true, isActive);
        Helper.setGPIO(process, Constants.GPIO_DIRECTION, ledPortNumber, true, isActive);
        Helper.setGPIO(process, Constants.GPIO_VALUE, ledPortNumber, true, isActive);

    }

}
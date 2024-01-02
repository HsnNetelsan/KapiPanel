package com.netelsan.ipinterkompanel.activity.device_test;


import android.os.Bundle;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.netelsan.ipinterkompanel.Constants;
import com.netelsan.ipinterkompanel.Helper;
import com.netelsan.ipinterkompanel.R;
import com.netelsan.ipinterkompanel.model.DeviceTest;

import java.util.Timer;
import java.util.TimerTask;

public class TestScreenActivity extends BaseTestActivity implements View.OnClickListener {

    RelativeLayout mainContainer;
    LinearLayout resultButtonsContainer;

    LinearLayout screenTestiNegative;
    LinearLayout screenTestiPositive;

    TextView testScreenColorName;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_test_screen);

        mainContainer = findViewById(R.id.mainContainer);
        resultButtonsContainer = findViewById(R.id.resultButtonsContainer);
        screenTestiNegative = findViewById(R.id.screenTestiNegative);
        screenTestiPositive = findViewById(R.id.screenTestiPositive);
        testScreenColorName = findViewById(R.id.testScreenColorName);

        setLayouts();

    }

    private void setLayouts() {

        mainContainer.setVisibility(View.VISIBLE);
        resultButtonsContainer.setVisibility(View.GONE);

        startTimerForColors();

    }

    @Override
    public void onKeyPressed(String keyCode) {
        if (keyCode.equals(Constants.KEYPAD_BACK)) {
            bottomMenuBackClicked();
        } else if (keyCode.equals(Constants.KEYPAD_UP)) {

        } else if (keyCode.equals(Constants.KEYPAD_DOWN)) {

        } else if (keyCode.equals(Constants.KEYPAD_HOME)) {
            bottomMenuOkayClicked();
        } else if (keyCode.equals(Constants.KEYPAD_CALL)) {

        } else if (keyCode.equals(Constants.KEYPAD_LOCK)) {

        } else {

        }
    }

    private void bottomMenuBackClicked(){
        resultClicked(false);
    }

    private void bottomMenuOkayClicked(){
        resultClicked(true);
    }

    Timer timer;
    int colorId = 1;

    int totalLoopCount = 1;

    private void startTimerForColors() {

        colorId = 1;

        if (timer == null) {
            timer = new Timer();
        }

        timer.schedule(new TimerTask() {
            @Override
            public void run() {

                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {

                        if (totalLoopCount == 3) {
                            stopTimerAndShowResultButtons();
                        }

                        setColors(colorId);

                        colorId = colorId + 1;

                        if (colorId == 4) {
                            colorId = 1;

                            totalLoopCount = totalLoopCount + 1;
                        }

                    }
                });

            }
        }, 0, 2000);
    }

    private void stopTimerAndShowResultButtons() {

        if (timer != null) {
            timer.purge();
            timer.cancel();
            timer = null;
        }

        mainContainer.setVisibility(View.GONE);
        resultButtonsContainer.setVisibility(View.VISIBLE);

    }

    private void setColors(int colorId) {

        if (colorId == 1) {
            mainContainer.setBackgroundColor(0xffff0000);
            testScreenColorName.setText(getString(R.string.kirmizi));
            testScreenColorName.setTextColor(0xffffffff);
        } else if (colorId == 2) {
            mainContainer.setBackgroundColor(0xff00ff00);
            testScreenColorName.setText(getString(R.string.yesil));
            testScreenColorName.setTextColor(0xffffffff);
        } else if (colorId == 3) {
            mainContainer.setBackgroundColor(0xff0000ff);
            testScreenColorName.setText(getString(R.string.mavi));
            testScreenColorName.setTextColor(0xffffffff);
        }

    }

    @Override
    public void onClick(View view) {

        switch (view.getId()) {

            default:
                break;

        }
    }

    private void resultClicked(boolean isPositive) {

        DeviceTest deviceTest = Helper.getDeviceTest(getApplicationContext());

        deviceTest.setScreenTestiOkay(isPositive);

        Helper.setDeviceTest(getApplicationContext(), deviceTest);

        TestScreenActivity.this.finish();

    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

        if (timer != null) {
            timer.purge();
            timer.cancel();
            timer = null;
        }

    }

}
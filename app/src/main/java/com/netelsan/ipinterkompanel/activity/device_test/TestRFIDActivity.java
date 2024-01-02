package com.netelsan.ipinterkompanel.activity.device_test;

import android.os.Bundle;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.netelsan.ipinterkompanel.Constants;
import com.netelsan.ipinterkompanel.Helper;
import com.netelsan.ipinterkompanel.R;
import com.netelsan.ipinterkompanel.model.DeviceTest;


public class TestRFIDActivity extends BaseTestActivity implements View.OnClickListener {

    LinearLayout testRFIDNegative;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_test_rfid);

        testRFIDNegative = findViewById(R.id.testRFIDNegative);

    }

    @Override
    public void onKeyPressed(String keyCode) {
        if (keyCode.equals(Constants.KEYPAD_BACK)) {
            resultClicked(false);
        } else if (keyCode.equals(Constants.KEYPAD_UP)) {

        } else if (keyCode.equals(Constants.KEYPAD_DOWN)) {

        } else if (keyCode.equals(Constants.KEYPAD_HOME)) {
        } else if (keyCode.equals(Constants.KEYPAD_CALL)) {

        } else if (keyCode.equals(Constants.KEYPAD_LOCK)) {

        } else {

        }
    }

    @Override
    public void onRFIDDetected(String rfid) {
        resultClicked(true);
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
        deviceTest.setRFIDTestiOkay(isPositive);
        Helper.setDeviceTest(getApplicationContext(), deviceTest);
        TestRFIDActivity.this.finish();

    }

    @Override
    protected void onDestroy() {
        super.onDestroy();


    }


}
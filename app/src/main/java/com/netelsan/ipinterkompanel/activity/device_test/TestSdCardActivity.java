package com.netelsan.ipinterkompanel.activity.device_test;

import android.os.Bundle;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.netelsan.ipinterkompanel.Constants;
import com.netelsan.ipinterkompanel.Helper;
import com.netelsan.ipinterkompanel.R;
import com.netelsan.ipinterkompanel.model.DeviceTest;


public class TestSdCardActivity extends BaseTestActivity implements View.OnClickListener {

    LinearLayout testScCardNegative;

    LinearLayout testScCardTestEt;
    TextView testScCardTestResult;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_test_sd_card);

        testScCardNegative = findViewById(R.id.testScCardNegative);
        testScCardTestEt = findViewById(R.id.testScCardTestEt);
        testScCardTestResult = findViewById(R.id.testScCardTestResult);

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
            bottomMenuLockClicked();
        } else {

        }
    }

    private void bottomMenuLockClicked(){
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                testEtClicked();
            }
        });
    }

    @Override
    public void onClick(View view) {

        switch (view.getId()) {

            default:
                break;

        }
    }

    private void testEtClicked() {

        boolean isSDCardMounted = Helper.isSDCardMounted();
        if (isSDCardMounted) {
            resultClicked(true);
        } else {
            testScCardTestResult.setText(getString(R.string.hafiza_karti_bulunamadi));
        }

    }

    private void resultClicked(boolean isPositive) {

        DeviceTest deviceTest = Helper.getDeviceTest(getApplicationContext());
        deviceTest.setSdCardTestiOkay(isPositive);
        Helper.setDeviceTest(getApplicationContext(), deviceTest);
        TestSdCardActivity.this.finish();

    }

    @Override
    protected void onDestroy() {
        super.onDestroy();


    }

}
package com.netelsan.ipinterkompanel.activity.device_test;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.Nullable;

import com.netelsan.ipinterkompanel.Constants;
import com.netelsan.ipinterkompanel.Helper;
import com.netelsan.ipinterkompanel.R;
import com.netelsan.ipinterkompanel.activity.InitializeActivity;
import com.netelsan.ipinterkompanel.activity.SelectDeviceTypeActivity;
import com.netelsan.ipinterkompanel.activity.SplashSelectorActivity;
import com.netelsan.ipinterkompanel.model.DeviceTest;
import com.netelsan.ipinterkompanel.tcp.Utils;


public class DeviceTestActivity extends BaseTestActivity implements View.OnClickListener {

    TextView testDeviceTitle;

    LinearLayout testCompletedContainer;
    LinearLayout testMainContainer;
    LinearLayout testDeviceTekrarTest;
    LinearLayout testDeviceBaslat;

    ImageView screenTestiCheck;
    LinearLayout screenTestiSina;

    ImageView kameraTestiCheck;
    LinearLayout kameraTestiSina;

    ImageView sesTestiCheck;
    LinearLayout sesTestiSina;

    ImageView ledTestiCheck;
    LinearLayout ledTestiSina;

    ImageView micTestiCheck;
    LinearLayout micTestiSina;

    ImageView videoTestiCheck;
    LinearLayout videoTestiSina;

    ImageView sdcardTestCheck;
    LinearLayout sdcardTestSina;

    ImageView rfidTestCheck;
    LinearLayout rfidTestSina;

    ImageView relayTestCheck;
    LinearLayout relayTestSina;

    ImageView relayTestCheck2;
    LinearLayout relayTestSina2;

    TextView testDeviceBaslatText;

    boolean isNeedInitializeScreen;

    boolean isAllTestsPassed = false;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_device_test);

        testDeviceTitle = findViewById(R.id.testDeviceTitle);

        testCompletedContainer = findViewById(R.id.testCompletedContainer);
        testMainContainer = findViewById(R.id.testMainContainer);
        testDeviceTekrarTest = findViewById(R.id.testDeviceTekrarTest);
        testDeviceBaslat = findViewById(R.id.testDeviceBaslat);

        screenTestiCheck = findViewById(R.id.screenTestiCheck);
        screenTestiSina = findViewById(R.id.screenTestiSina);

        kameraTestiCheck = findViewById(R.id.kameraTestiCheck);
        kameraTestiSina = findViewById(R.id.kameraTestiSina);

        sesTestiCheck = findViewById(R.id.sesTestiCheck);
        sesTestiSina = findViewById(R.id.sesTestiSina);

        ledTestiCheck = findViewById(R.id.ledTestiCheck);
        ledTestiSina = findViewById(R.id.ledTestiSina);

        micTestiCheck = findViewById(R.id.micTestiCheck);
        micTestiSina = findViewById(R.id.micTestiSina);

        videoTestiCheck = findViewById(R.id.videoTestiCheck);
        videoTestiSina = findViewById(R.id.videoTestiSina);

        sdcardTestCheck = findViewById(R.id.sdcardTestCheck);
        sdcardTestSina = findViewById(R.id.sdcardTestSina);

        rfidTestCheck = findViewById(R.id.rfidTestCheck);
        rfidTestSina = findViewById(R.id.rfidTestSina);

        relayTestCheck = findViewById(R.id.relayTestCheck);
        relayTestSina = findViewById(R.id.relayTestSina);

        relayTestCheck2 = findViewById(R.id.relayTestCheck2);
        relayTestSina2 = findViewById(R.id.relayTestSina2);


        testDeviceBaslatText = findViewById(R.id.testDeviceBaslatText);

        processIntent(getIntent());

        String selfIP = Utils.getIPAddress(true);
        Log.d(Constants.LOG_TAG, "DeviceTestActivity selfIP=" + selfIP);
    }

    private void processIntent(Intent intent) {
        if (intent == null) {
            return;
        }
        Bundle bundle = intent.getExtras();
        if (bundle == null) {
            return;
        }

        isNeedInitializeScreen = bundle.getBoolean("isNeedInitializeScreen");

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
            bottomNumbersClicked(keyCode);
        }
    }

    private void bottomMenuBackClicked() {
        if (!isAllTestsPassed) {
            return;
        }
        restartTestSequence();
    }

    private void bottomMenuOkayClicked() {
        if (!isAllTestsPassed) {
            return;
        }
        finishTestSquence();
    }

    private void bottomNumbersClicked(String keycode) {

        if (keycode.equals(Constants.KEYPAD_1)) {
            showScreenTestiScreen();
        } else if (keycode.equals(Constants.KEYPAD_2)) {
            showKameraTestiScreen();
        } else if (keycode.equals(Constants.KEYPAD_3)) {
            showSesTestiScreen();
        } else if (keycode.equals(Constants.KEYPAD_4)) {
            showLEDTestiScreen();
        } else if (keycode.equals(Constants.KEYPAD_5)) {
            showMicTestiScreen();
        } else if (keycode.equals(Constants.KEYPAD_6)) {
            showVideoTestiScreen();
        } else if (keycode.equals(Constants.KEYPAD_7)) {
            showSdCardScreen();
        } else if (keycode.equals(Constants.KEYPAD_8)) {
            showRFIDScreen();
        } else if (keycode.equals(Constants.KEYPAD_9)) {
            showRelayScreen();
        } else if (keycode.equals(Constants.KEYPAD_0)) {
            showRelayScreen2();
        }
    }


    @Override
    public void onResume() {
        super.onResume();

        setLayouts();

    }

    private void setLayouts() {

        if (isNeedInitializeScreen) {
            testDeviceBaslatText.setText(getString(R.string.sistemi_baslat));
        } else {
            testDeviceBaslatText.setText(getString(R.string.testi_bitir));
        }

        boolean isAllPassed = true;

        DeviceTest deviceTest = Helper.getDeviceTest(getApplicationContext());

        boolean isScreenTestiOkay = deviceTest.isScreenTestiOkay();
        if (isScreenTestiOkay) {
            screenTestiCheck.setVisibility(View.VISIBLE);
            screenTestiSina.setVisibility(View.GONE);
        } else {
            isAllPassed = false;
            screenTestiCheck.setVisibility(View.GONE);
            screenTestiSina.setVisibility(View.VISIBLE);
        }

//        boolean isVeriTestiOkay = deviceTest.isVeriTestiOkay();
//        if(isVeriTestiOkay) {
//            veriTestiCheck.setVisibility(View.VISIBLE);
//            veriTestiSina.setVisibility(View.GONE);
//        } else {
//            isAllPassed = false;
//            veriTestiCheck.setVisibility(View.GONE);
//            veriTestiSina.setVisibility(View.VISIBLE);
//        }

        boolean isKameraTestiOkay = deviceTest.isKameraTestiOkay();
        if (isKameraTestiOkay) {
            kameraTestiCheck.setVisibility(View.VISIBLE);
            kameraTestiSina.setVisibility(View.GONE);
        } else {
            isAllPassed = false;
            kameraTestiCheck.setVisibility(View.GONE);
            kameraTestiSina.setVisibility(View.VISIBLE);
        }

        boolean isSesTestiOkay = deviceTest.isSesTestiOkay();
        if (isSesTestiOkay) {
            sesTestiCheck.setVisibility(View.VISIBLE);
            sesTestiSina.setVisibility(View.GONE);
        } else {
            isAllPassed = false;
            sesTestiCheck.setVisibility(View.GONE);
            sesTestiSina.setVisibility(View.VISIBLE);
        }

        boolean isLEDTestiOkay = deviceTest.isLEDTestiOkay();
        if (isLEDTestiOkay) {
            ledTestiCheck.setVisibility(View.VISIBLE);
            ledTestiSina.setVisibility(View.GONE);
        } else {
            isAllPassed = false;
            ledTestiCheck.setVisibility(View.GONE);
            ledTestiSina.setVisibility(View.VISIBLE);
        }

        boolean isMicTestiOkay = deviceTest.isMicTestiOkay();
        if (isMicTestiOkay) {
            micTestiCheck.setVisibility(View.VISIBLE);
            micTestiSina.setVisibility(View.GONE);
        } else {
            isAllPassed = false;
            micTestiCheck.setVisibility(View.GONE);
            micTestiSina.setVisibility(View.VISIBLE);
        }

        boolean isVideoTestiOkay = deviceTest.isVideoTestiOkay();
        if (isVideoTestiOkay) {
            videoTestiCheck.setVisibility(View.VISIBLE);
            videoTestiSina.setVisibility(View.GONE);
        } else {
            isAllPassed = false;
            videoTestiCheck.setVisibility(View.GONE);
            videoTestiSina.setVisibility(View.VISIBLE);
        }

       // deviceTest.setSdCardTestiOkay(true);
        boolean isSdCardTestiOkay = deviceTest.isSdCardTestiOkay();
        if (isSdCardTestiOkay) {
            sdcardTestCheck.setVisibility(View.VISIBLE);
            sdcardTestSina.setVisibility(View.GONE);
        } else {
            isAllPassed = false;
            sdcardTestCheck.setVisibility(View.GONE);
            sdcardTestSina.setVisibility(View.VISIBLE);
        }

       // deviceTest.setRFIDTestiOkay(true);
        boolean isRFIDTestiOkay = deviceTest.isRFIDTestiOkay();
        if (isRFIDTestiOkay) {
            rfidTestCheck.setVisibility(View.VISIBLE);
            rfidTestSina.setVisibility(View.GONE);
        } else {
            isAllPassed = false;
            rfidTestCheck.setVisibility(View.GONE);
            rfidTestSina.setVisibility(View.VISIBLE);
        }

        boolean isRelayTestiOkay = deviceTest.isRelayTestiOkay();
        if (isRelayTestiOkay) {
            relayTestCheck.setVisibility(View.VISIBLE);
            relayTestSina.setVisibility(View.GONE);
        } else {
            isAllPassed = false;
            relayTestCheck.setVisibility(View.GONE);
            relayTestSina.setVisibility(View.VISIBLE);
        }

        boolean isRelayTestiOkay2 = deviceTest.isRelayTestiOkay2();
        if (isRelayTestiOkay2) {
            relayTestCheck2.setVisibility(View.VISIBLE);
            relayTestSina2.setVisibility(View.GONE);
        } else {
            isAllPassed = false;
            relayTestCheck2.setVisibility(View.GONE);
            relayTestSina2.setVisibility(View.VISIBLE);
        }

        isAllTestsPassed = isAllPassed;
        if (isAllPassed) {

            Helper.setDeviceTestFinished(getApplicationContext(), true);

            testCompletedContainer.setVisibility(View.VISIBLE);
            testDeviceTitle.setVisibility(View.GONE);
            testMainContainer.setVisibility(View.GONE);
        } else {
            testCompletedContainer.setVisibility(View.GONE);
            testDeviceTitle.setVisibility(View.VISIBLE);
            testMainContainer.setVisibility(View.VISIBLE);
        }

    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }

    @Override
    public void onClick(View view) {

        switch (view.getId()) {

            default:
                break;

        }
    }

    private void restartTestSequence() {

        Helper.setDeviceTestFinished(getApplicationContext(), false);
        Helper.setDeviceTest(getApplicationContext(), null);

        setLayouts();

    }

    private void finishTestSquence() {

        Helper.setDeviceTestFinished(getApplicationContext(), true);

        try {
            Thread.sleep(100);
        } catch (Exception e) {
            e.printStackTrace();
        }

        if (isNeedInitializeScreen) {

            Intent intent = new Intent(DeviceTestActivity.this, SelectDeviceTypeActivity.class);
            startActivity(intent);

//            Intent intent = new Intent(DeviceTestActivity.this, InitializeActivity.class);
//            startActivity(intent);
        }

        DeviceTestActivity.this.finish();

    }

    private void showScreenTestiScreen() {
        Intent intent = new Intent(DeviceTestActivity.this, TestScreenActivity.class);
        startActivity(intent);
    }

    private void showKameraTestiScreen() {
        Intent intent = new Intent(DeviceTestActivity.this, TestCameraActivity.class);
        startActivity(intent);
    }

    private void showSesTestiScreen() {
        Intent intent = new Intent(DeviceTestActivity.this, TestSesActivity.class);
        startActivity(intent);
    }

    private void showLEDTestiScreen() {
        Intent intent = new Intent(DeviceTestActivity.this, TestLEDActivity.class);
        startActivity(intent);
    }

    private void showMicTestiScreen() {
        Intent intent = new Intent(DeviceTestActivity.this, TestMicActivity.class);
        startActivity(intent);
    }

    private void showVideoTestiScreen() {
        Intent intent = new Intent(DeviceTestActivity.this, TestVideoActivity.class);
        startActivity(intent);
    }

    private void showSdCardScreen() {
        Intent intent = new Intent(DeviceTestActivity.this, TestSdCardActivity.class);
        startActivity(intent);
    }

    private void showRFIDScreen() {
        try {
            Intent intent = new Intent(DeviceTestActivity.this, TestRFIDActivity.class);
            startActivity(intent);
        } catch (Exception e) {
            Log.e("HK", "HK  " + e);
        }

    }

    private void showRelayScreen() {
        try {
            Intent intent = new Intent(DeviceTestActivity.this, TestRelayActivity.class);
            startActivity(intent);
        } catch (Exception e) {
            Log.e("HK", "HK  " + e);
        }


    }

    private void showRelayScreen2() {
        try {
            Intent intent = new Intent(DeviceTestActivity.this, TestRelayActivity2.class);
            startActivity(intent);
        } catch (Exception e) {
            Log.e("HK", "HK  22" + e);
        }


    }


}

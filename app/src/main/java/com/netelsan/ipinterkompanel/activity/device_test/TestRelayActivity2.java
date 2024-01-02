package com.netelsan.ipinterkompanel.activity.device_test;

import androidx.appcompat.app.AppCompatActivity;

import android.media.AudioManager;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.util.Log;
import android.view.View;

import com.netelsan.ipinterkompanel.Constants;
import com.netelsan.ipinterkompanel.Helper;
import com.netelsan.ipinterkompanel.R;
import com.netelsan.ipinterkompanel.model.DeviceTest;

public class TestRelayActivity2 extends BaseTestActivity implements View.OnClickListener {

    boolean value = true;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_test_relay2);
    }

    @Override
    protected void onStart() {
        Log.d("HK", "HK onStart..");
        Thread myThread = new Thread(new MyThread2());
        myThread.start();
        super.onStart();
    }

    @Override
    public void onClick(View view) {

    }

    @Override
    public void onKeyPressed(String keyCode) {
        Log.d("HK", "HK pressed: " + keyCode);
        if (keyCode.equals(Constants.KEYPAD_BACK)) {
            keypadBack();
        } else if (keyCode.equals(Constants.KEYPAD_LOCK)) {
            // lockPressed();
        } else if (keyCode.equals(Constants.KEYPAD_HOME)) {
            okayPressed();
        }


    }

    private void okayPressed() {
        DeviceTest deviceTest = Helper.getDeviceTest(getApplicationContext());
        deviceTest.setRelayTestiOkay2(true);
        Helper.setDeviceTest(getApplicationContext(), deviceTest);
        TestRelayActivity2.this.finish();


    }

    private void lockPressed() {
        Thread t = new Thread(new Mythread());
        t.start();


    }

    private void keypadBack() {
        DeviceTest deviceTest = Helper.getDeviceTest(getApplicationContext());
        deviceTest.setRelayTestiOkay2(false);
        Helper.setDeviceTest(getApplicationContext(), deviceTest);
        TestRelayActivity2.this.finish();
    }


    @Override
    protected void onDestroy() {
        value = false;
        super.onDestroy();
    }

    public class Mythread implements Runnable {

        @Override
        public void run() {
            while (value) {
                keyPadService.sendDoorUnlock(Constants.DOOR_UNLOCK_FOR_TEST2);

                try {
                    Thread.sleep(3000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }
    }


    public class MyThread2 implements Runnable {

        @Override
        public void run() {
            try {
                Thread.sleep(1000);
                lockPressed();

            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }
}
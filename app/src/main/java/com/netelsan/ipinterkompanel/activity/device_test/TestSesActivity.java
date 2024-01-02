package com.netelsan.ipinterkompanel.activity.device_test;

import android.media.AudioManager;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.netelsan.ipinterkompanel.Constants;
import com.netelsan.ipinterkompanel.Helper;
import com.netelsan.ipinterkompanel.R;
import com.netelsan.ipinterkompanel.model.DeviceTest;

public class TestSesActivity extends BaseTestActivity implements View.OnClickListener {

    LinearLayout sesTestiNegative;
    LinearLayout sesTestiPositive;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_test_ses);

        sesTestiNegative = findViewById(R.id.sesTestiNegative);
        sesTestiPositive = findViewById(R.id.sesTestiPositive);

        playSound();

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

    MediaPlayer mediaPlayer;

    private void playSound() {

        mediaPlayer = MediaPlayer.create(getApplicationContext(), R.raw.phone_ringing_tone);
        mediaPlayer.start();

        mediaPlayer.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
            @Override
            public void onCompletion(MediaPlayer mp) {
                try {
                    mediaPlayer.start();
                } catch(Exception e) {
                    e.printStackTrace();
                }
            }
        });

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
        deviceTest.setSesTestiOkay(isPositive);
        Helper.setDeviceTest(getApplicationContext(), deviceTest);
        TestSesActivity.this.finish();

    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

        if(mediaPlayer != null) {
            mediaPlayer.stop();
            mediaPlayer = null;
        }

        setVolumeToDefault();

    }

    private void setVolumeToDefault() {

        AudioManager audioManager = (AudioManager) getSystemService(AUDIO_SERVICE);
        int volume_level = 11;

        audioManager.setStreamVolume(AudioManager.STREAM_MUSIC, volume_level, 0);

    }

}
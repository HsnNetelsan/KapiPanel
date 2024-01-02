package com.netelsan.ipinterkompanel.activity.device_test;

import android.media.MediaRecorder;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.github.piasy.rxandroidaudio.AudioRecorder;
import com.github.piasy.rxandroidaudio.PlayConfig;
import com.github.piasy.rxandroidaudio.RxAudioPlayer;
import com.netelsan.ipinterkompanel.Constants;
import com.netelsan.ipinterkompanel.Helper;
import com.netelsan.ipinterkompanel.R;
import com.netelsan.ipinterkompanel.model.DeviceTest;

import java.io.File;
import java.util.Timer;
import java.util.TimerTask;

import io.reactivex.Observer;
import io.reactivex.disposables.Disposable;
import io.reactivex.schedulers.Schedulers;

public class TestMicActivity extends BaseTestActivity implements View.OnClickListener {

    LinearLayout micTestiNegative;
    LinearLayout micTestiPositive;

    LinearLayout micTestiRecordContainer;

    ImageView micTestiIcon;
    TextView micTestiDuration;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_test_mic);

        micTestiNegative = findViewById(R.id.micTestiNegative);
        micTestiPositive = findViewById(R.id.micTestiPositive);
        micTestiRecordContainer = findViewById(R.id.micTestiRecordContainer);
        micTestiIcon = findViewById(R.id.micTestiIcon);
        micTestiDuration = findViewById(R.id.micTestiDuration);

        setRecordSound();

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
            bottomMenuLockClicked();
        } else {

        }
    }

    private void bottomMenuLockClicked() {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                micIconClicked();
            }
        });
    }

    AudioRecorder mAudioRecorder;
    File mAudioFile;
    RxAudioPlayer rxAudioPlayer;

    private void setRecordSound() {

        mAudioRecorder = AudioRecorder.getInstance();
        mAudioFile = new File(Environment.getExternalStorageDirectory().getAbsolutePath() + File.separator + System.nanoTime() + "_file.m4a");
        mAudioRecorder.prepareRecord(MediaRecorder.AudioSource.MIC, MediaRecorder.OutputFormat.MPEG_4, MediaRecorder.AudioEncoder.AAC, mAudioFile);
        rxAudioPlayer = RxAudioPlayer.getInstance();

    }

    @Override
    public void onClick(View view) {

        switch (view.getId()) {

            default:
                break;

        }

    }

    private void micIconClicked() {
        Log.d(Constants.LOG_TAG, "micIconClicked");
        if (mAudioRecorder.isStarted()) {
            stopRecording();
        } else {
            startRecording();
        }
    }

    private void startRecording() {
        micTestiIcon.setImageResource(R.drawable.icon_stop_record);
        mAudioRecorder.startRecord();
        startTimerForRecord();
    }

    private void stopRecording() {
        micTestiIcon.setImageResource(R.drawable.icon_mic_enabled);
        mAudioRecorder.stopRecord();
        playLastRecordedSoundInHalfSecond();
        stopTimerForRecord();
    }

    Timer timerForRecord = null;
    int totalDuration = 0;

    private void startTimerForRecord() {

        if (timerForRecord == null) {
            timerForRecord = new Timer();
        }

        timerForRecord.schedule(new TimerTask() {
            @Override
            public void run() {

                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {

                        if (totalDuration >= 5) {
                            stopRecording();
                        }
                        micTestiDuration.setText("00:0" + totalDuration);
                        totalDuration = totalDuration + 1;
                    }
                });

            }
        }, 0, 1000);
    }

    private void stopTimerForRecord() {
        if (timerForRecord != null) {
            timerForRecord.cancel();
            timerForRecord.purge();
            timerForRecord = null;
        }
    }

    private void playLastRecordedSoundInHalfSecond() {

        Timer timer = new Timer();
        timer.schedule(new TimerTask() {
            @Override
            public void run() {


                rxAudioPlayer.play(PlayConfig.file(mAudioFile).looping(true).leftVolume(15f).rightVolume(15f).build())
                        .subscribeOn(Schedulers.io())
                        .subscribe(new Observer<Boolean>() {
                            @Override
                            public void onSubscribe(final Disposable disposable) {

                            }

                            @Override
                            public void onNext(final Boolean aBoolean) {
                                // prepared
                            }

                            @Override
                            public void onError(final Throwable throwable) {

                            }

                            @Override
                            public void onComplete() {
                                // play finished
                                // NOTE: if looping, the Observable will never finish, you need stop playing
                                // onDestroy, otherwise, memory leak will happen!
                            }
                        });


            }
        }, 500);

    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

        rxAudioPlayer.stopPlay();
        stopTimerForRecord();
    }

    private void resultClicked(boolean isPositive) {

        DeviceTest deviceTest = Helper.getDeviceTest(getApplicationContext());

        deviceTest.setMicTestiOkay(isPositive);

        Helper.setDeviceTest(getApplicationContext(), deviceTest);

        TestMicActivity.this.finish();

    }

}
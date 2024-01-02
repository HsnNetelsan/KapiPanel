package com.netelsan.ipinterkompanel.activity.device_test;

import android.media.AudioManager;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.MediaController;
import android.widget.TextView;
import android.widget.VideoView;

import com.netelsan.ipinterkompanel.Constants;
import com.netelsan.ipinterkompanel.Helper;
import com.netelsan.ipinterkompanel.R;
import com.netelsan.ipinterkompanel.model.DeviceTest;

import java.io.File;

public class TestVideoActivity extends BaseTestActivity implements View.OnClickListener {

    LinearLayout videoTestiNegative;
    LinearLayout videoTestiPositive;

    VideoView videoTestVideo;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_test_video);

        videoTestiNegative = findViewById(R.id.videoTestiNegative);
        videoTestiPositive = findViewById(R.id.videoTestiPositive);
        videoTestVideo = findViewById(R.id.videoTestVideo);

        playVideo();

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

    private void playVideo() {

//        String videoPath = "file:///android_asset/test_video.mp4";
        String videoPath = "android.resource://" + getPackageName() + File.separator + R.raw.test_video;
//        File file = new File(videoPath);

        Uri yourUri = Uri.parse(videoPath);

        final MediaController mediacontroller = new MediaController(this);

        mediacontroller.setAnchorView(videoTestVideo);

        videoTestVideo.setMediaController(mediacontroller);
        videoTestVideo.setVideoURI(yourUri);
        videoTestVideo.requestFocus();

        videoTestVideo.setOnPreparedListener(new MediaPlayer.OnPreparedListener() {
            @Override
            public void onPrepared(MediaPlayer mp) {
                videoTestVideo.start();
            }
        });

        videoTestVideo.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
            @Override
            public void onCompletion(MediaPlayer mp) {
                videoTestVideo.start();
            }
        });

        videoTestVideo.setOnErrorListener(new MediaPlayer.OnErrorListener() {
            @Override
            public boolean onError(MediaPlayer mp, int what, int extra) {
                TestVideoActivity.this.finish();
                return false;
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
        deviceTest.setVideoTestiOkay(isPositive);
        Helper.setDeviceTest(getApplicationContext(), deviceTest);
        TestVideoActivity.this.finish();

    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

        setVolumeToDefault();

        if(videoTestVideo.isPlaying()) {
            videoTestVideo.pause();
        }

    }

    private void setVolumeToDefault() {

        AudioManager audioManager = (AudioManager) getSystemService(AUDIO_SERVICE);
        int volume_level = 11;

        audioManager.setStreamVolume(AudioManager.STREAM_MUSIC, volume_level, 0);

    }

}
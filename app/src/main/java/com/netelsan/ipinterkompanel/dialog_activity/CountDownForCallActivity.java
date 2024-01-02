package com.netelsan.ipinterkompanel.dialog_activity;

import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.content.SharedPreferences;
import android.graphics.Point;
import android.media.AudioManager;
import android.media.MediaPlayer;
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
import com.netelsan.ipinterkompanel.activity.call.MultipleDeviceCallRequestActivity;
import com.netelsan.ipinterkompanel.listener.KeyPadListener;
import com.netelsan.ipinterkompanel.model.Daire;
import com.netelsan.ipinterkompanel.model.Guvenlik;
import com.netelsan.ipinterkompanel.service.CapturePhotoService;
import com.netelsan.ipinterkompanel.service.KeyPadService;

import java.util.ArrayList;
import java.util.Timer;
import java.util.TimerTask;

public class CountDownForCallActivity extends BaseDialogActivity implements ServiceConnection, KeyPadListener {

    TextView countdownDialogDaireName;
    TextView countdownDialogRemainingTime;
    boolean iscallingDaire = true;
    Daire selectedDaire;
    Guvenlik selectedGuvenlik;

    int timeRemaining = 4;
    Timer timerCountDown;

    AudioManager audioManager;

    MediaPlayer mediaPlayerBuzzer;


    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        requestWindowFeature(Window.FEATURE_NO_TITLE);

        setContentView(R.layout.activity_count_down_for_call);

        countdownDialogDaireName = findViewById(R.id.countdownDialogDaireName);
        countdownDialogRemainingTime = findViewById(R.id.countdownDialogRemainingTime);

        setFinishOnTouchOutside(false);

        Display display = getWindowManager().getDefaultDisplay();
        Point size = new Point();
        display.getSize(size);
        int width = size.x;
        int height = size.y;

        getWindow().setLayout(((int) (width * Constants.CUSTOM_DIALOG_WIDTH)), ((int) (height * Constants.CUSTOM_DIALOG_HEIGHT)));

        Intent serviceConnIntent = new Intent(this, KeyPadService.class);
        bindService(serviceConnIntent, this, Context.BIND_AUTO_CREATE);

        processIntent(getIntent());

        IntentFilter filterPhotoCapture = new IntentFilter();
        filterPhotoCapture.addAction("com.netelsan.PHOTO_CAPTURE");
        registerReceiver(photoCaptureBroadcastReceiver, filterPhotoCapture);

        Intent capturePhotoService = new Intent(CountDownForCallActivity.this, CapturePhotoService.class);
        SharedPreferences sharedPreferences = getApplicationContext().getSharedPreferences(Constants.SHARED_PREFS, 0);
        boolean isEnabled = sharedPreferences.getBoolean(Constants.KEY_TUS_SESI, false);
        Log.d("tus", "geri sayma kontrol:" + isEnabled);
        if (isEnabled) {
            if (iscallingDaire) {

                if (selectedDaire.getGorevliOlduguBinalar().size() > 0) {
                    mediaPlayerBuzzer = MediaPlayer.create(getApplicationContext(), R.raw.gorevli_araniyor);
                    mediaPlayerBuzzer.setAudioStreamType(AudioManager.STREAM_MUSIC);
                    mediaPlayerBuzzer.start();
                } else {

                    //daire arama seslendirmesi
                    mediaPlayerBuzzer = MediaPlayer.create(getApplicationContext(), R.raw.daire_araniyor);
                    mediaPlayerBuzzer.setAudioStreamType(AudioManager.STREAM_MUSIC);
                    mediaPlayerBuzzer.start();
                }


                capturePhotoService.putExtra("object", selectedDaire);
            } else {

                ///Güvenlik arama seslendirmesi
                mediaPlayerBuzzer = MediaPlayer.create(getApplicationContext(), R.raw.guvenlik_araniyor);
                mediaPlayerBuzzer.setAudioStreamType(AudioManager.STREAM_MUSIC);
                mediaPlayerBuzzer.start();

            }
            if (iscallingDaire)
                capturePhotoService.putExtra("object", selectedDaire);
            else
                capturePhotoService.putExtra("object", selectedGuvenlik);

        } else {
            if (iscallingDaire)
                capturePhotoService.putExtra("object", selectedDaire);
            else
                capturePhotoService.putExtra("object", selectedGuvenlik);
        }


        startService(capturePhotoService);

    }

    KeyPadService keyPadService;

    @Override
    public void onServiceConnected(ComponentName componentName, IBinder binder) {
        String className = componentName.getClassName();

        if (className.equals(KeyPadService.class.getName())) {
            keyPadService = ((KeyPadService.MyBinder) binder).getService();
            keyPadService.addListener(CountDownForCallActivity.this, CountDownForCallActivity.class.getName());
        }

    }

    @Override
    public void onServiceDisconnected(ComponentName name) {
        if (keyPadService != null) {
            keyPadService.removeListener(CountDownForCallActivity.class.getName());
        }
        keyPadService = null;
    }

    @Override
    public void onKeyPressed(String keyCode) {

        Log.d(Constants.LOG_TAG, "CountDownForCallActivity onKeyPressed keyCode=" + keyCode);

        if (keyCode.equals(Constants.KEYPAD_BACK)) {
            sendBroadcastForCountDown(false);
        }
    }

    @Override
    public void onRFIDDetected(String rfid) {

    }

    private void processIntent(Intent intent) {
        if (intent == null) {
            return;
        }
        Bundle bundle = intent.getExtras();
        if (bundle == null) {
            return;
        }

        try {
            selectedDaire = (Daire) bundle.getSerializable("object");
            iscallingDaire = true;
        } catch (Exception e) {
            selectedGuvenlik = (Guvenlik) bundle.getSerializable("object");
            iscallingDaire = false;
        }

        timeRemaining = 4;

        setLayouts();

        if (iscallingDaire) {
            countdownDialogDaireName.setText(selectedDaire.getIsim() + " " + selectedDaire.getSoyisim());
        } else {
            countdownDialogDaireName.setText(selectedGuvenlik.getDeviceName());
        }
        countdownDialogRemainingTime.setText("4 ");

    }

    private void setLayouts() {

//        mediaPlayerBuzzer = MediaPlayer.create(getApplicationContext(), R.raw.doorman_calling);
//        mediaPlayerBuzzer.setAudioStreamType(AudioManager.STREAM_MUSIC);
//        mediaPlayerBuzzer.start();
        timerCountDown = new Timer();
        timerCountDown.schedule(new TimerTask() {
            @Override
            public void run() {



                if (timeRemaining == 0) {
                    timerCountDown.cancel();
                    timerCountDown.purge();
                    timerCountDown = null;
                    if (mediaPlayerBuzzer != null) {
                        mediaPlayerBuzzer.stop();
                        mediaPlayerBuzzer.release();
                        mediaPlayerBuzzer = null;
                    }

                    sendBroadcastForCountDown(true);

                    //hsn hsn

//                    Intent intent = new Intent(CountDownForCallActivity.this, CountDownForCallActivity.class);
//                    int totalDeviceCount = intent.getIntExtra("totalDeviceCount", 2);
//                    if (totalDeviceCount > 1) {
//                        Intent intentMultiDevice = new Intent(CountDownForCallActivity.this, MultipleDeviceCallRequestActivity.class);
//                        intentMultiDevice.putExtra("object", selectedDaire);
//                        startActivity(intentMultiDevice);
//                    }

                    //hsn hsn

                    return;
                }

                timeRemaining = timeRemaining - 1;

                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        Log.d("HK", "HK timeRemaning: " + timeRemaining);
                        countdownDialogRemainingTime.setText(timeRemaining + " ");
                    }
                });

            }
        }, 0, 1000);

    }

    ArrayList<String> bitmapFilePaths = new ArrayList<>();

    private void sendBroadcastForCountDown(boolean isPositive) {

        Log.d(Constants.LOG_TAG, "1907 sendBroadcastForCountDown isPositive= " + isPositive);

        Log.d(Constants.LOG_TAG, "1907 sendBroadcastForCountDown bitmapFilePaths= " + bitmapFilePaths.size());

        try {
            Intent intent = new Intent("com.netelsan.COUNT_DOWN");
            intent.putExtra("isPositive", isPositive);
            if (isPositive) {
                if (bitmapFilePaths == null) {
                    bitmapFilePaths = new ArrayList<>();
                }
                Log.d(Constants.LOG_TAG, "1907 sendBroadcastForCountDown bitmapFilePaths 2= " + bitmapFilePaths.size());
                intent.putExtra("bitmapFiles", bitmapFilePaths);
            }
            sendBroadcast(intent);

            CountDownForCallActivity.this.finish();
        } catch (Exception e) {
            Log.e("HK", "HK error " + e);
        }

    }

    @Override
    protected void onDestroy() {

        try {
            Helper.runRestartCameraShellCommand();
        } catch (Throwable e) {
            e.printStackTrace();
        }

        if (timerCountDown != null) {
            timerCountDown.purge();
            timerCountDown.cancel();
            timerCountDown = null;
        }

        if (keyPadService != null) {
            keyPadService.removeListener(CountDownForCallActivity.class.getName());
        }

        unbindService(this);
        unregisterReceiver(photoCaptureBroadcastReceiver);

        super.onDestroy();

    }

    BroadcastReceiver photoCaptureBroadcastReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {

            String bitmapPath = intent.getStringExtra("bitmapPath");
//            Log.d(Constants.LOG_TAG, "1907 photoCaptureBroadcastReceiver onReceive bitmapPath= " + bitmapPath);
//            Log.d(Constants.LOG_TAG, "1907 photoCaptureBroadcastReceiver onReceive size= " + bitmapFilePaths.size());
            if (bitmapFilePaths == null) {
                bitmapFilePaths = new ArrayList<>();
            }

            bitmapFilePaths.add(bitmapPath);
//            Log.d(Constants.LOG_TAG, "1907 photoCaptureBroadcastReceiver onReceive size 2= " + bitmapFilePaths.size());
        }

    };

}

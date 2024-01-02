package com.netelsan.ipinterkompanel.activity.call;

import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.hardware.Camera;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.util.Log;
import android.widget.FrameLayout;
import android.widget.TextView;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.netelsan.ipinterkompanel.Constants;
import com.netelsan.ipinterkompanel.Helper;
import com.netelsan.ipinterkompanel.R;
import com.netelsan.ipinterkompanel.database.DatabaseHelper;
import com.netelsan.ipinterkompanel.listener.KeyPadListener;
import com.netelsan.ipinterkompanel.model.ComPackageModel;
import com.netelsan.ipinterkompanel.model.Daire;
import com.netelsan.ipinterkompanel.model.ZilPanel;
import com.netelsan.ipinterkompanel.model.ZilPanelSite;
import com.netelsan.ipinterkompanel.multi_call.sound_stream.MediaStreamClient;
import com.netelsan.ipinterkompanel.multi_call.sound_stream.MediaStreamServer;
import com.netelsan.ipinterkompanel.service.KeyPadService;
import com.netelsan.ipinterkompanel.tcp.TCPHelper;
import com.netelsan.ipinterkompanel.tcp.Utils;
import com.netelsan.ipinterkompanel.multi_call.video_stream.MyCameraView;
import com.netelsan.ipinterkompanel.multi_call.video_stream.MyServerThread;
import com.netelsan.ipinterkompanel.webrtc.activity.CallActivity;

import java.net.Inet4Address;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.ServerSocket;
import java.net.SocketException;
import java.util.Enumeration;
import java.util.Timer;
import java.util.TimerTask;

public class MultipleDeviceCallRequestActivity extends AppCompatActivity implements KeyPadListener, ServiceConnection {

    public static boolean activityRunning;

    Daire daireCalling;

    ZilPanel zilPanelSelf;
    ZilPanelSite zilPanelSiteSelf;

    private Camera mCamera;
    public MyCameraView mPreview;
    public static String SERVERIP = "localhost";
    public static final int SERVERPORT = 9191;
    private Handler handler = new Handler();
    ServerSocket serverSocket = null;
    MyServerThread myServerThread;

    boolean isServiceBind = false;
    public KeyPadService keyPadService;

    TextView callingInfoName;
    TextView callingInfoTimeRemaining;

    boolean isForSite;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_multi_device_call_request);
        activityRunning = true;

        callingInfoName = findViewById(R.id.callingInfoName);
        callingInfoTimeRemaining = findViewById(R.id.callingInfoTimeRemaining);

        DatabaseHelper databaseHelper = new DatabaseHelper(getApplicationContext());

        isForSite = Helper.isZilForSite(getApplicationContext());
        if(isForSite) {
            zilPanelSelf = databaseHelper.getZilPanelByIP(Utils.getIPAddress(true));
        } else {
            zilPanelSiteSelf = databaseHelper.getSiteZilPanelByIP(Utils.getIPAddress(true));
        }

        processIntent(getIntent());

        registerMultiCallReceiver();

        setLedState(true);

    }

    @Override
    protected void onResume() {
        super.onResume();

        Intent serviceConnIntent = new Intent(MultipleDeviceCallRequestActivity.this, KeyPadService.class);
        bindService(serviceConnIntent, this, Context.BIND_AUTO_CREATE);

        if(keyPadService != null) {
            keyPadService.addListener(MultipleDeviceCallRequestActivity.this, MultipleDeviceCallRequestActivity.class.getName());
        }
    }

    @Override
    public void onPause() {
        super.onPause();
        if(keyPadService != null) {
            keyPadService.removeListener(MultipleDeviceCallRequestActivity.class.getName());
        }
    }

    @Override
    public void onServiceConnected(ComponentName name, IBinder service) {
        isServiceBind = true;
        keyPadService = ((KeyPadService.MyBinder) service).getService();
        keyPadService.addListener(MultipleDeviceCallRequestActivity.this, MultipleDeviceCallRequestActivity.class.getName());
    }

    @Override
    public void onServiceDisconnected(ComponentName name) {
        isServiceBind = false;
        keyPadService = null;
    }

    @Override
    public void onKeyPressed(String keyCode) {

        if(keyCode.equals(Constants.KEYPAD_BACK)) {
            bottomMenuBackClicked();
        } else if(keyCode.equals(Constants.KEYPAD_UP)) {

        } else if(keyCode.equals(Constants.KEYPAD_DOWN)) {

        } else if(keyCode.equals(Constants.KEYPAD_HOME)) {

        } else if(keyCode.equals(Constants.KEYPAD_CALL)) {

        } else if(keyCode.equals(Constants.KEYPAD_LOCK)) {

        } else {

        }

    }

    private void bottomMenuBackClicked() {
        sendMultiCallCancelledToReceiver();
        setLedState(false);
        MultipleDeviceCallRequestActivity.this.finish();
    }

    private void sendMultiCallCancelledToReceiver() {

        String destinationIP = daireCalling.getIp();

        ComPackageModel model = new ComPackageModel();

        if(isForSite) {
            model.setZilPanelSite(zilPanelSiteSelf);
        } else {
            model.setZilPanel(zilPanelSelf);
        }
        model.setOpe_type(Constants.OPERATION_MULTI_CALL_CANCELLED_BY_CALLER);
        Log.d("ServerThread", "fey feyTCPHelper.sendMessageToIP(getApplicationContext(), destinationIP, model)" + model.getOpe_type());
        TCPHelper.sendMessageToIP(getApplicationContext(), destinationIP, model);

    }

    @Override
    public void onRFIDDetected(String rfid) {

    }

    private void registerMultiCallReceiver() {

        IntentFilter filter = new IntentFilter();
        filter.addAction("com.netelsan.MULTI_CALL_RECEIVER");
        registerReceiver(multiCallRequestBroadcastReceiver, filter);

    }

    private void processIntent(Intent intent) {

        if(intent == null) {
            return;
        }
        Bundle bundle = intent.getExtras();
        if(bundle == null) {
            return;
        }

        daireCalling = (Daire) bundle.getSerializable("object");

        setLayouts();

        SERVERIP = getLocalIpAddress();
        mCamera = getCameraInstance();
        mPreview = new MyCameraView(this, mCamera);
        FrameLayout preview = findViewById(R.id.camera_preview);
        preview.addView(mPreview);

        try {
            serverSocket = new ServerSocket(SERVERPORT);

            myServerThread = new MyServerThread(this, serverSocket, SERVERIP, handler);
            Thread thread = new Thread(myServerThread);
            thread.start();

            startTimeOutTimer();

        } catch(Exception e) {
            Log.d("ServerThread", "run: error");
        }

    }

    private void setLayouts() {

        callingInfoName.setText(daireCalling.getIsim() + " " + daireCalling.getSoyisim());
        startPhoneCallingSound();
    }

    MediaPlayer mediaPlayer;

    private void startPhoneCallingSound() {

        mediaPlayer = MediaPlayer.create(getApplicationContext(), R.raw.phone_ringing_tone);
        mediaPlayer.start();

        mediaPlayer.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
            @Override
            public void onCompletion(MediaPlayer mp) {
                if(mediaPlayer != null) {
                    mediaPlayer.start();
                }
            }
        });

    }

    Timer timerForTimeout;
    int countDownTime = 30;

    private void startTimeOutTimer() {

        if(timerForTimeout == null) {
            timerForTimeout = new Timer();
        } else {
            return;
        }

        countDownTime = 30;
        timerForTimeout.schedule(new TimerTask() {
            @Override
            public void run() {

                if(countDownTime <= 0) {

                    bottomMenuBackClicked();
                    return;
                }
                setCountDownTimeLayouts();

                countDownTime = countDownTime - 1;
            }
        }, 0, 1000);

    }

    private void setCountDownTimeLayouts() {

        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                Log.d(Constants.LOG_TAG, "feyy feyy  noHostDeviceDialogCloseText2");
                callingInfoTimeRemaining.setText(getString(R.string.saniye_sonra_kapanacak_1) + " " + countDownTime + " " + getString(R.string.saniye_sonra_kapanacak_2));
            }
        });

    }

    private String getLocalIpAddress() {
        try {
            for(Enumeration<NetworkInterface> en = NetworkInterface.getNetworkInterfaces(); en.hasMoreElements(); ) {
                NetworkInterface intf = en.nextElement();
                for(Enumeration<InetAddress> enumIpAddr = intf.getInetAddresses(); enumIpAddr.hasMoreElements(); ) {
                    InetAddress inetAddress = enumIpAddr.nextElement();
                    if(! inetAddress.isLoopbackAddress() && inetAddress instanceof Inet4Address) {
                        return inetAddress.getHostAddress().toString();
                    }
                }
            }
        } catch(SocketException ex) {
            ex.printStackTrace();
        }
        return null;
    }


    public static Camera getCameraInstance() {
        Camera camera = null;
        try {
            camera = Camera.open();
//            Camera.Parameters parameters = camera.getParameters();
//            parameters.setPictureSize(320, 240);
//            parameters.setPreviewSize(320, 240);
//            parameters.setPreviewFrameRate(25);
//            camera.setParameters(parameters);

        } catch(Exception e) {
            e.printStackTrace();
        }
        return camera;
    }


    @Override
    protected void onDestroy() {
        super.onDestroy();
        activityRunning = false;

        unregisterReceiver(multiCallRequestBroadcastReceiver);

        try {
            serverSocket.close();
        } catch(Exception e) {
            e.printStackTrace();
        }

        myServerThread.isDestroyed = true;

        if(isServiceBind) {
            unbindService(this);
        }

        if(timerForTimeout != null) {
            timerForTimeout.purge();
            timerForTimeout.cancel();
            timerForTimeout = null;
        }

        if(mediaPlayer != null) {
            mediaPlayer.stop();
            mediaPlayer.setOnCompletionListener(null);
            mediaPlayer = null;
        }

        setLedState(false);
    }

    BroadcastReceiver multiCallRequestBroadcastReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {

            int operationType = intent.getIntExtra("ope_type", - 1);
            if(operationType == Constants.OPERATION_MULTI_CALL_REJECT) {

                MultipleDeviceCallRequestActivity.this.finish();

            } else if(operationType == Constants.OPERATION_MULTI_CALL_ACCEPTED) {
                Log.d(Constants.LOG_TAG, "feyy feyy normal geliyor diğeri gelmiyor");
                if(mediaPlayer != null) {
                    mediaPlayer.stop();
                    mediaPlayer.setOnCompletionListener(null);
                    mediaPlayer = null;
                }

                if(timerForTimeout != null) {
                    timerForTimeout.purge();
                    timerForTimeout.cancel();
                    timerForTimeout = null;
                }

//                startAudioStreamServer();
//                startAudioStreamClient();

                Daire daire = (Daire) intent.getSerializableExtra("object");

                Intent intentCallActivity = new Intent(MultipleDeviceCallRequestActivity.this, CallActivity.class);
                intentCallActivity.putExtra(Constants.PARAM_CALL_RECEIVING, false);
                intentCallActivity.putExtra(Constants.PARAM_CALL_IS_YONLENDIRME, false);
                intentCallActivity.putExtra(Constants.PARAM_CALL_IS_NEED_VIDEO, true);
                intentCallActivity.putExtra(Constants.PARAM_IS_MULTI_CALL_ACCEPT, true);

                // TODO: 20.01.2020 bu noktada arama resim id lerini gönder ki kaydedebilsin
//                intentCallActivity.putExtra(Constants.PARAM_CALL_SS_IDS, callSnapshotImageIds);

                intentCallActivity.putExtra("object", daire);
                startActivity(intentCallActivity);

                MultipleDeviceCallRequestActivity.this.finish();

            }

        }
    };

    MediaStreamServer mediaStreamServer;

    private void startAudioStreamServer() {
        mediaStreamServer = new MediaStreamServer(MultipleDeviceCallRequestActivity.this, 9635);
    }

    MediaStreamClient mediaStreamClient;

    private void startAudioStreamClient() {
        Timer timer = new Timer();
        timer.schedule(new TimerTask() {
            @Override
            public void run() {
                mediaStreamClient = new MediaStreamClient(MultipleDeviceCallRequestActivity.this, daireCalling.getIp(), 9630);
            }
        }, 3000);
    }

    private void setLedState(boolean isActive) {

        int ledPortNumber = Helper.getPINFromGPIO("PE17");

        Process process = Helper.getProcess();
        Helper.setGPIO(process, Constants.GPIO_EXPORT, ledPortNumber, true, isActive);
        Helper.setGPIO(process, Constants.GPIO_DIRECTION, ledPortNumber, true, isActive);
        Helper.setGPIO(process, Constants.GPIO_VALUE, ledPortNumber, true, isActive);

    }

}

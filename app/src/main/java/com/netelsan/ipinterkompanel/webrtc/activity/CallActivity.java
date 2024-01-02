/*
 *  Copyright 2015 The WebRTC Project Authors. All rights reserved.
 *
 *  Use of this source code is governed by a BSD-style license
 *  that can be found in the LICENSE file in the root of the source
 *  tree. An additional intellectual property rights grant can be found
 *  in the file PATENTS.  All contributing project authors may
 *  be found in the AUTHORS file in the root of the source tree.
 */

package com.netelsan.ipinterkompanel.webrtc.activity;

import android.annotation.TargetApi;
import android.app.Activity;
import android.app.FragmentTransaction;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.hardware.Camera;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.media.MediaRecorder;
import android.media.projection.MediaProjection;
import android.media.projection.MediaProjectionManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.IBinder;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.view.WindowManager.LayoutParams;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.coremedia.iso.boxes.Container;
import com.googlecode.mp4parser.authoring.Movie;
import com.googlecode.mp4parser.authoring.Track;
import com.googlecode.mp4parser.authoring.builder.DefaultMp4Builder;
import com.googlecode.mp4parser.authoring.container.mp4.MovieCreator;
import com.netelsan.ipinterkompanel.Constants;
import com.netelsan.ipinterkompanel.Helper;
import com.netelsan.ipinterkompanel.R;
import com.netelsan.ipinterkompanel.activity.MainActivity;
import com.netelsan.ipinterkompanel.activity.ScreensaverActivity2;
import com.netelsan.ipinterkompanel.database.DatabaseHelper;
import com.netelsan.ipinterkompanel.fragments.settings.AyarlarYoneticiFragment;
import com.netelsan.ipinterkompanel.listener.KeyPadListener;
import com.netelsan.ipinterkompanel.model.ComPackageModel;
import com.netelsan.ipinterkompanel.model.Daire;
import com.netelsan.ipinterkompanel.model.DoorPassword;
import com.netelsan.ipinterkompanel.model.DoorUnlockLog;
import com.netelsan.ipinterkompanel.model.Guvenlik;
import com.netelsan.ipinterkompanel.model.ZilPanel;
import com.netelsan.ipinterkompanel.model.ZilPanelSite;
import com.netelsan.ipinterkompanel.multi_call.video_stream.MyCameraView;
import com.netelsan.ipinterkompanel.multi_call.video_stream.MyServerThread;
import com.netelsan.ipinterkompanel.service.KeyPadService;
import com.netelsan.ipinterkompanel.tcp.TCPHelper;
import com.netelsan.ipinterkompanel.tcp.Utils;
import com.netelsan.ipinterkompanel.webrtc.AppRTCAudioManager;
import com.netelsan.ipinterkompanel.webrtc.AppRTCClient;
import com.netelsan.ipinterkompanel.webrtc.FileEncoder;
import com.netelsan.ipinterkompanel.webrtc.PeerConnectionClient;
import com.netelsan.ipinterkompanel.webrtc.fragment.CallFragment;
import com.netelsan.ipinterkompanel.webrtc.fragment.HudFragment;
import com.netelsan.ipinterkompanel.webrtc.socket_server.DirectRTCClient;

import org.webrtc.Camera1Enumerator;
import org.webrtc.Camera2Enumerator;
import org.webrtc.CameraEnumerator;
import org.webrtc.EglBase;
import org.webrtc.IceCandidate;
import org.webrtc.Logging;
import org.webrtc.PeerConnectionFactory;
import org.webrtc.RendererCommon.ScalingType;
import org.webrtc.ScreenCapturerAndroid;
import org.webrtc.SessionDescription;
import org.webrtc.StatsReport;
import org.webrtc.SurfaceViewRenderer;
import org.webrtc.VideoCapturer;
import org.webrtc.VideoFileRenderer;
import org.webrtc.VideoFrame;
import org.webrtc.VideoSink;

import java.io.File;
import java.io.FileOutputStream;
import java.net.Inet4Address;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.ServerSocket;
import java.net.SocketException;
import java.nio.channels.FileChannel;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Enumeration;
import java.util.List;
import java.util.Set;
import java.util.Timer;
import java.util.TimerTask;

public class CallActivity extends AppCompatActivity implements AppRTCClient.SignalingEvents, PeerConnectionClient.PeerConnectionEvents, CallFragment.OnCallEvents, KeyPadListener, ServiceConnection {
    private static final String TAG = Constants.LOG_TAG;

    private static final int CAPTURE_PERMISSION_REQUEST_CODE = 1;

    // Peer connection statistics callback period in ms.
    private static final int STAT_CALLBACK_PERIOD = 1000;

    public static boolean door = false;

    private static class ProxyVideoSink implements VideoSink {
        private VideoSink target;

        @Override
        synchronized public void onFrame(VideoFrame frame) {

            if (target == null) {
                Logging.d(TAG, "Dropping frame in proxy because target is null.");
                return;
            }
            Logging.d(TAG, "ProxyVideoSink width=" + frame.getRotatedWidth() + " height=" + frame.getRotatedHeight());

//            Log.d(Constants.LOG_TAG, "ProxyVideoSink rotation=" + frame.getRotation() + " height=" + frame.getRotatedHeight() + " width=" + frame.getRotatedWidth());
            target.onFrame(frame);
        }

        synchronized public void setTarget(VideoSink target) {
            this.target = target;
        }
    }

    private final ProxyVideoSink remoteProxyRenderer = new ProxyVideoSink();
    private final ProxyVideoSink localProxyVideoSink = new ProxyVideoSink();
    @Nullable
    private PeerConnectionClient peerConnectionClient;
    @Nullable
    private AppRTCClient appRtcClient;
    @Nullable
    private AppRTCClient.SignalingParameters signalingParameters;
    @Nullable
    private AppRTCAudioManager audioManager;
    @Nullable
    private SurfaceViewRenderer pipRenderer;
    @Nullable
    private SurfaceViewRenderer fullscreenRenderer;
    @Nullable
    private RelativeLayout callingInfoContainerForZilPanel;
    @Nullable
    private RelativeLayout multiDeviceCallingContainer;
    @Nullable
    private VideoFileRenderer videoFileRenderer;
    private final List<VideoSink> remoteSinks = new ArrayList<>();
    private boolean commandLineRun;
    public static boolean activityRunning;
    private AppRTCClient.RoomConnectionParameters roomConnectionParameters;
    @Nullable
    private PeerConnectionClient.PeerConnectionParameters peerConnectionParameters;
    private boolean connected;
    private boolean isError;
    private boolean callControlFragmentVisible = true;
    private long callStartedTimeMs;
    private boolean micEnabled = true;
    private boolean screencaptureEnabled;
    private static Intent mediaProjectionPermissionResultData;
    private static int mediaProjectionPermissionResultCode;
    // True if local view is in the fullscreen renderer.
    private boolean isSwappedFeeds;

    // Controls
//    private CallFragment callFragment;
    private HudFragment hudFragment;
//    private CpuMonitor cpuMonitor;

    TextView callingInfoName;
    TextView callingInfoTimeRemaining;
    TextView callingInfoDevamEdiyor;

    TextView callingInfoDoorUnlock;
    LinearLayout callingInfoAraniyorContainer;

    LinearLayout yonlendirmeContainer;

    FileEncoder recording;
    int volume_level;
    float volumeLevel;

    @Override
    // LayoutParams.FLAG_SHOW_WHEN_LOCKED are deprecated.
    @SuppressWarnings("deprecation")
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
//        Thread.setDefaultUncaughtExceptionHandler(new UnhandledExceptionHandler(this));
       /* mediaPlayer = MediaPlayer.create(getApplicationContext(), R.raw.phone_ringing_tone);
        mediaPlayer.setVolume(volumeLevel, volumeLevel);
        mediaPlayer.start();
        AudioManager audioManager = (AudioManager) getSystemService(AUDIO_SERVICE);
        volume_level = audioManager.getStreamVolume(AudioManager.STREAM_MUSIC);
        maxVolume = audioManager.getStreamMaxVolume(AudioManager.STREAM_MUSIC);
        volumeLevel = ((float) volume_level) / ((float) maxVolume);
        Log.d(Constants.LOG_TAG, "CallActivity fey volume_level=" + volume_level);
        mediaPlayer.stop();*/
        Helper.setDoorLock(getApplicationContext(), false);
        Log.d(Constants.LOG_TAG, "feyy feyy  noHostDeviceDialogCloseText11");
        AudioManager audioManager = (AudioManager) getSystemService(AUDIO_SERVICE);
        volume_level = audioManager.getStreamVolume(AudioManager.STREAM_MUSIC);
        maxVolume = audioManager.getStreamMaxVolume(AudioManager.STREAM_MUSIC);
        volumeLevel = ((float) volume_level) / ((float) maxVolume);
        volumeLevel = volumeLevel * 0.3f;
        //volumeLevel = volumeLevel * 1.0f;
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().addFlags(LayoutParams.FLAG_FULLSCREEN | LayoutParams.FLAG_KEEP_SCREEN_ON | LayoutParams.FLAG_SHOW_WHEN_LOCKED | LayoutParams.FLAG_TURN_SCREEN_ON);
        getWindow().getDecorView().setSystemUiVisibility(getSystemUiVisibility());
        setContentView(R.layout.activity_call);


        String roomId = processIntent(getIntent());

        if (roomId == null) {
            return;
        }

        connected = false;
        signalingParameters = null;

        // Create UI controls.

        yonlendirmeContainer = findViewById(R.id.yonlendirmeContainer);
        callingInfoName = findViewById(R.id.callingInfoName);
        callingInfoDevamEdiyor = findViewById(R.id.callingInfoDevamEdiyor);
        callingInfoTimeRemaining = findViewById(R.id.callingInfoTimeRemaining);
        callingInfoAraniyorContainer = findViewById(R.id.callingInfoAraniyorContainer);

        callingInfoDoorUnlock = findViewById(R.id.callingTextDoorUnlock);

        pipRenderer = findViewById(R.id.pip_video_view);
        fullscreenRenderer = findViewById(R.id.fullscreen_video_view);

        callingInfoContainerForZilPanel = findViewById(R.id.callingInfoContainerForZilPanel);

        fullscreenRenderer.setVisibility(View.GONE);
        pipRenderer.setVisibility(View.GONE);
        callingInfoContainerForZilPanel.setVisibility(View.VISIBLE);

        multiDeviceCallingContainer = findViewById(R.id.multiDeviceCallingContainer);


        callingInfoDoorUnlock.setVisibility(View.INVISIBLE);

//        callFragment = new CallFragment();
        hudFragment = new HudFragment();

        // Show/hide call control fragment on view click.
        View.OnClickListener listener = new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                toggleCallControlFragmentVisibility();
            }
        };

        // Swap feeds on pip view click.
        pipRenderer.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                setSwappedFeeds(!isSwappedFeeds);
            }
        });

        fullscreenRenderer.setOnClickListener(listener);
        remoteSinks.add(remoteProxyRenderer);

        final Intent intent = getIntent();
        final EglBase eglBase = EglBase.create();

        // Create video renderers.
        pipRenderer.init(eglBase.getEglBaseContext(), null);
        pipRenderer.setScalingType(ScalingType.SCALE_ASPECT_FIT);

        fullscreenRenderer.init(eglBase.getEglBaseContext(), null);
        fullscreenRenderer.setScalingType(ScalingType.SCALE_ASPECT_FILL);

        pipRenderer.setZOrderMediaOverlay(true);
        pipRenderer.setEnableHardwareScaler(true /* enabled */);
        fullscreenRenderer.setEnableHardwareScaler(false /* enabled */);
        // Start with local feed in fullscreen and swap it to the pip when the call is connected.
        setSwappedFeeds(true /* isSwappedFeeds */);

        boolean tracing = false;

        int videoWidth = 0;
        int videoHeight = 0;

        screencaptureEnabled = false;
        // If capturing format is not specified for screencapture, use screen resolution.
        if (screencaptureEnabled && videoWidth == 0 && videoHeight == 0) {
            DisplayMetrics displayMetrics = getDisplayMetrics();
            videoWidth = displayMetrics.widthPixels;
            videoHeight = displayMetrics.heightPixels;
        }
        PeerConnectionClient.DataChannelParameters dataChannelParameters = null;
//        if(intent.getBooleanExtra(EXTRA_DATA_CHANNEL_ENABLED, false)) {
//            dataChannelParameters = new PeerConnectionClient.DataChannelParameters(intent.getBooleanExtra(EXTRA_ORDERED, true), intent.getIntExtra(EXTRA_MAX_RETRANSMITS_MS, - 1), intent.getIntExtra(EXTRA_MAX_RETRANSMITS, - 1), intent.getStringExtra(EXTRA_PROTOCOL), intent.getBooleanExtra(EXTRA_NEGOTIATED, false), intent.getIntExtra(EXTRA_ID, - 1));
//        }

        peerConnectionParameters = new PeerConnectionClient.PeerConnectionParameters(
                isNeedVideo,
                tracing,
                videoWidth,
                videoHeight,
                0,
                0,
                "VP8",
                true,
                false,
                0,
                "OPUS",
                false,
                false,
                false,
                false,
                false,
                false,
                false,
                false,
                true,
                dataChannelParameters);

        commandLineRun = false;
        int runTimeMs = 0;

        appRtcClient = new DirectRTCClient(this);

        String urlParameters = "";
        roomConnectionParameters = new AppRTCClient.RoomConnectionParameters(roomId, urlParameters);

        hudFragment.setArguments(intent.getExtras());
        FragmentTransaction fragmentTransaction = getFragmentManager().beginTransaction();

//        if(Helper.isDeviceSube(getApplicationContext())) {
////            fragmentTransaction.add(R.id.call_fragment_container, callFragment);
//            fragmentTransaction.add(R.id.hud_fragment_container, hudFragment);
//            fragmentTransaction.commit();
//        }

        if (commandLineRun && runTimeMs > 0) {
            (new Handler()).postDelayed(new Runnable() {
                @Override
                public void run() {
                    disconnect();
                }
            }, runTimeMs);
        }

        peerConnectionClient = new PeerConnectionClient(getApplicationContext(), eglBase, peerConnectionParameters, CallActivity.this);
        PeerConnectionFactory.Options options = new PeerConnectionFactory.Options();

        peerConnectionClient.createPeerConnectionFactory(options);

        if (screencaptureEnabled) {
            startScreenCapture();
        } else {
            startCall();
        }


        setLedState(true);

        bindKaypadService();

        if (!isCallReceiving) {// arama yapılıyor demek

            if (isMultiCallAccept) {
                showConnectingScreens();
            } else {

                //startPhoneCallingSound();
                setAraniyorViews(30, true);
                setGorusmeStateText(true);
                startTimerForTimeOutDuration();
                sendCallRequestToPeer();
                //burası değişmeli


                new Handler().postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        startPhoneCallingSound();
                    }
                }, 2000);


            }
            registerCallRequestListener();

        } else {// gelen arama demek
            setAraniyorViews(60, false);
            setGorusmeStateText(false);
            startTimerForTotalCallDuration();
            startSoundIn1Second();

        }


        registerDoorUnlockListener();

        setYonlendirmeViewsIfNeeded();

        setRecordingsIfNeeded(eglBase);

        setAnimations();

        setVideoSenderIfNeeded();


    }

    private void showConnectingScreens() {
        callingInfoContainerForZilPanel.setVisibility(View.GONE);
        multiDeviceCallingContainer.setVisibility(View.VISIBLE);
    }

    private void startSoundIn1Second() {

        Log.d(Constants.LOG_TAG, "startSoundIn1Second 1");

        Timer timer = new Timer();
        timer.schedule(new TimerTask() {
            @Override
            public void run() {

                Log.d(Constants.LOG_TAG, "startSoundIn1Second 2");
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {

                        peerConnectionClient.setAudioEnabled(true);

                        Log.d(Constants.LOG_TAG, "startSoundIn1Second 3");
                        if (audioManager != null) {
                            //Log.d(Constants.LOG_TAG, "startSoundIn1Second 4");
                            audioManager.start(new AppRTCAudioManager.AudioManagerEvents() {
                                @Override
                                public void onAudioDeviceChanged(AppRTCAudioManager.AudioDevice audioDevice, Set<AppRTCAudioManager.AudioDevice> availableAudioDevices) {
                                    Log.d(Constants.LOG_TAG, "startSoundIn1Second 5");
                                    onAudioManagerDevicesChanged(audioDevice, availableAudioDevices);
                                }
                            });
                        }

                    }
                });
            }
        }, 1000);

    }

    private void setAnimations() {

        Animation animationLeftToRight = AnimationUtils.loadAnimation(this, R.anim.slide_left_to_right);
        Animation animationRightToLeft = AnimationUtils.loadAnimation(this, R.anim.slide_right_to_left);

        ImageView callingArrowAnimation = findViewById(R.id.callingArrowAnimation);

        if (isCallReceiving) {
            callingArrowAnimation.setImageResource(R.drawable.icon_arrow_left_black);
            callingArrowAnimation.startAnimation(animationRightToLeft);

            animationRightToLeft.setAnimationListener(new Animation.AnimationListener() {
                @Override
                public void onAnimationStart(Animation animation) {

                }

                @Override
                public void onAnimationEnd(Animation animation) {
                    animation.start();
                }

                @Override
                public void onAnimationRepeat(Animation animation) {

                }
            });

        } else {
            callingArrowAnimation.setImageResource(R.drawable.icon_arrow_right_black);
            callingArrowAnimation.startAnimation(animationLeftToRight);

            animationLeftToRight.setAnimationListener(new Animation.AnimationListener() {
                @Override
                public void onAnimationStart(Animation animation) {

                }

                @Override
                public void onAnimationEnd(Animation animation) {
                    animation.start();
                }

                @Override
                public void onAnimationRepeat(Animation animation) {

                }
            });

        }

        ImageView callingDestinationDeviceIcon = findViewById(R.id.callingDestinationDeviceIcon);
        callingDestinationDeviceIcon.setImageResource(R.drawable.icon_user);

        DatabaseHelper databaseHelper = new DatabaseHelper(getApplicationContext());

        TextView callingSelfName = findViewById(R.id.callingSelfName);
        boolean isZilForSite = Helper.isZilForSite(getApplicationContext());
        if (isZilForSite) {
            ZilPanelSite zilPanelSite = databaseHelper.getSiteZilPanelByIP(Utils.getIPAddress(true));
            callingSelfName.setText(zilPanelSite.getDeviceName());
        } else {
            ZilPanel zilPanel = databaseHelper.getZilPanelByIP(Utils.getIPAddress(true));
            callingSelfName.setText(zilPanel.getDeviceName());
        }

    }

    String recordSoundFileName = null;

    private void setRecordingsIfNeeded(EglBase eglBase) {

        boolean isCallVideoRecordEnabled = Helper.isCallVideoRecordEnabled(getApplicationContext());
        Log.d(Constants.LOG_TAG, "setRecordingsIfNeeded isCallVideoRecordEnabled=" + isCallVideoRecordEnabled);
        if (!isCallVideoRecordEnabled) {
            return;
        }

        boolean isSDCardMounted = Helper.isSDCardMounted();
        Log.d(Constants.LOG_TAG, "setRecordingsIfNeeded isSDCardMounted=" + isSDCardMounted);
        if (!isSDCardMounted) {
            return;
        }

        try {

            SimpleDateFormat dateFormat = new SimpleDateFormat("ddMMyy_HHmmss");
            String fileName = dateFormat.format(Calendar.getInstance().getTime());
            recordSoundFileName = fileName;

            // 03.10.2023 tarihinde ses ve görüşme kaydı kapatıldı
            //  setVideoRecording(eglBase, fileName);
            //   setSoundRecording(fileName);
        } catch (Exception e) {

        }

    }

    MediaRecorder mediaRecorderMic;
    String recordSoundFilePath = null;

    private void setSoundRecording(String fileName) throws Exception {

        String root = Environment.getExternalStorageDirectory().toString();
        File myDir = new File(root + "/saved_call_videos/sounds");
        myDir.mkdirs();

        String filePath = myDir.getAbsolutePath() + File.separator + fileName + ".mp3";
        recordSoundFilePath = filePath;

        mediaRecorderMic = new MediaRecorder();
        mediaRecorderMic.setAudioSource(MediaRecorder.AudioSource.VOICE_CALL);
        mediaRecorderMic.setOutputFormat(MediaRecorder.OutputFormat.THREE_GPP);
        mediaRecorderMic.setAudioEncoder(MediaRecorder.AudioEncoder.AMR_NB);
        mediaRecorderMic.setOutputFile(filePath);
        mediaRecorderMic.prepare();
        mediaRecorderMic.start();

    }

    String recordVideoFilePath = null;

    private void setVideoRecording(EglBase eglBase, String fileName) {

        String root = Environment.getExternalStorageDirectory().toString();
        File myDir = new File(root + "/saved_call_videos/videos");
        myDir.mkdirs();

        String filePath = myDir.getAbsolutePath() + File.separator + fileName + ".mp4";
        recordVideoFilePath = filePath;

        try {
            recording = new FileEncoder(filePath, eglBase.getEglBaseContext());
            remoteSinks.add(recording);
        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    private void setYonlendirmeViewsIfNeeded() {
        if (isCallYonlendirme) {
            yonlendirmeContainer.setVisibility(View.VISIBLE);
        } else {
            yonlendirmeContainer.setVisibility(View.GONE);
        }
    }

    private void setAraniyorViews(int timeRemaining, boolean isNeedAraniyor) {


        if (isNeedAraniyor) {
            callingInfoAraniyorContainer.setVisibility(View.VISIBLE);
        } else {
            callingInfoAraniyorContainer.setVisibility(View.GONE);
        }



        callingInfoTimeRemaining.setText(getString(R.string.saniye_sonra_kapanacak_1) + " " + timeRemaining + " " + getString(R.string.saniye_sonra_kapanacak_2));

        if (isDeviceSube) {
            String isim = daire.getIsim();
            String soyisim = daire.getSoyisim();

            callingInfoName.setText(isim + " " + soyisim);
        } else {
            callingInfoName.setText(guvenlik.getDeviceName());
        }

    }


    private void setGorusmeStateText(boolean isCevapBekleniyor) {

        if (isCevapBekleniyor) {
            callingInfoDevamEdiyor.setText(getString(R.string.aramanin_cevaplanmasi_bekleniyor));
        } else {
            callingInfoDevamEdiyor.setText(getString(R.string.gorusme_devam_ediyor));
        }

    }

    private void sendCallRequestToPeer() {

        String receiverIP;
        if (isDeviceSube) {
            receiverIP = daire.getIp();
        } else {
            receiverIP = guvenlik.getIp();
        }
        String selfIP = Utils.getIPAddress(true);

        if (receiverIP.equals(selfIP)) {
            Log.d(Constants.LOG_TAG, "OPERATION_ARAMA_REQUEST Kendinizi arayamazsın");
            return;
        }

        // arayacağın cihazı da bilgilendir ki o cihaz da client olarak bu cihaza bağlanacak

        boolean isZilForSite = Helper.isZilForSite(getApplicationContext());

        ComPackageModel model = new ComPackageModel();

        DatabaseHelper databaseHelper = new DatabaseHelper(CallActivity.this);

        if (isZilForSite) {
            ZilPanelSite zilPanelSite = databaseHelper.getSiteZilPanelByIP(Utils.getIPAddress(true));
            model.setZilPanelSite(zilPanelSite);
        } else {
            ZilPanel zilPanel = databaseHelper.getZilPanelByIP(Utils.getIPAddress(true));
            model.setZilPanel(zilPanel);
        }

        model.setOpe_type(Constants.OPERATION_ARAMA_REQUEST);
        model.setCallSnapshotImageIds(callSnapshotImageIds);
        model.setDataBoolean(false);//acil arama olmadığını belirtiyoruz

        TCPHelper.sendMessageToIP(CallActivity.this, receiverIP, model);

    }

    private void registerCallRequestListener() {

        IntentFilter filter = new IntentFilter();
        filter.addAction("com.netelsan.CALL_REQUEST_RESPONSE");
        registerReceiver(callRequestBroadcastReceiver, filter);

    }

    private void registerDoorUnlockListener() {

        IntentFilter filter = new IntentFilter();
        filter.addAction("com.netelsan.ipinterkompanel.doorunlock");
        registerReceiver(doorUnlockBroadcastReceiver, filter);

    }

    BroadcastReceiver callRequestBroadcastReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {

            Bundle bundle = intent.getExtras();
            boolean isAccepted = bundle.getBoolean("isAccepted");
            String rejectMessage = bundle.getString("message");

            Log.d(Constants.LOG_TAG, "callRequestBroadcastReceiver isAccepted=" + isAccepted + "   rejectMessage=" + rejectMessage);
            if (isAccepted) {

                if (mediaPlayer != null) {
                    mediaPlayer.stop();
                    mediaPlayer = null;
                }

//
                peerConnectionClient.setAudioEnabled(true);

                if (audioManager != null) {
                    audioManager.start(new AppRTCAudioManager.AudioManagerEvents() {
                        @Override
                        public void onAudioDeviceChanged(AppRTCAudioManager.AudioDevice audioDevice, Set<AppRTCAudioManager.AudioDevice> availableAudioDevices) {
                            onAudioManagerDevicesChanged(audioDevice, availableAudioDevices);
                        }
                    });
                }

                if (timerForTimeOut != null) {
                    timerForTimeOut.cancel();
                    timerForTimeOut.purge();
                    timerForTimeOut = null;
                }

                startTimerForTotalCallDuration();

                if (isMultiCallAccept) {
                    multiDeviceCallingContainer.setVisibility(View.GONE);
                    callingInfoContainerForZilPanel.setVisibility(View.VISIBLE);
                }
            } else {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        Helper.showTopMessageBanner(CallActivity.this, rejectMessage, false);
                    }
                });

                Timer timerForDisconnect = new Timer();
                timerForDisconnect.schedule(new TimerTask() {
                    @Override
                    public void run() {
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                disconnect();
                            }
                        });
                    }
                }, 1500);
            }

        }
    };

    BroadcastReceiver doorUnlockBroadcastReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {

            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    String message = getString(R.string.kapi_acildi);
                    Helper.showTopMessageBanner(CallActivity.this, message, true);

                    boolean df = Helper.getDoorLock(getApplicationContext());
                    Log.d("HK", "Door: " + df);
                    if (df) {
                        callingInfoDoorUnlock.setVisibility(View.VISIBLE);

                    } else {
                        callingInfoDoorUnlock.setVisibility(View.INVISIBLE);
                    }
//                    MediaPlayer mediaPlayerBuzzer = MediaPlayer.create(getApplicationContext(), R.raw.number_okay_tone);
//                    mediaPlayerBuzzer.setAudioStreamType(AudioManager.STREAM_MUSIC);
//                    mediaPlayerBuzzer.start();

                }
            });
        }
    };

    MediaPlayer mediaPlayer;
    int maxVolume;

    private void startPhoneCallingSound() {

        mediaPlayer = MediaPlayer.create(getApplicationContext(), R.raw.phone_ringing_tone);
        mediaPlayer.setVolume(volumeLevel, volumeLevel);
        //mediaPlayer.start();

        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {

                mediaPlayer.start();

            }
        }, 2000);
        mediaPlayer.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
            @Override
            public void onCompletion(MediaPlayer mp) {
                if (mediaPlayer != null) {
                    mediaPlayer.start();
                    Log.d(Constants.LOG_TAG, "CallActivity fey volume_level4=" + volume_level);
                }
            }
        });

        Log.d(Constants.LOG_TAG, "CallActivity fey volume_level5=" + volume_level);

    }

    boolean isDeviceSube = true;
    Daire daire;
    Guvenlik guvenlik;

    boolean isCallReceiving;
    boolean isCallYonlendirme;
    ArrayList<String> callSnapshotImageIds = new ArrayList<>();
    boolean isNeedVideo = true;
    boolean isMultiCallAccept = false;

    private String processIntent(Intent intent) {

        if (intent == null) {
            CallActivity.this.finish();
            return null;
        }
        Bundle bundle = intent.getExtras();
        if (bundle == null) {
            CallActivity.this.finish();
            return null;
        }

        isCallReceiving = bundle.getBoolean(Constants.PARAM_CALL_RECEIVING);

        try {
            daire = (Daire) bundle.getSerializable("object");
            isDeviceSube = true;
        } catch (Exception e) {
            guvenlik = (Guvenlik) bundle.getSerializable("object");
            isDeviceSube = false;
        }

        isCallYonlendirme = bundle.getBoolean(Constants.PARAM_CALL_IS_YONLENDIRME);

        callSnapshotImageIds = (ArrayList<String>) bundle.getSerializable(Constants.PARAM_CALL_SS_IDS);
        isNeedVideo = bundle.getBoolean(Constants.PARAM_CALL_IS_NEED_VIDEO);

        isMultiCallAccept = bundle.getBoolean(Constants.PARAM_IS_MULTI_CALL_ACCEPT);

        String ipForCall = "";
        if (isCallReceiving) {
            if (isDeviceSube) {
                ipForCall = daire.getIp();
            } else {
                ipForCall = guvenlik.getIp();
            }
        } else { //burada kendi ip ni kullanacaksın
            ipForCall = Utils.getIPAddress(true);
        }

        Log.d(Constants.LOG_TAG, "CallActivity processIntent ipForCall=" + ipForCall);
//        return ipForCall;
        return ipForCall;
    }

    Timer timer = null;
    int totalDurationTime = 0;

    private void startTimerForTotalCallDuration() {

        if (timer == null) {

            timer = new Timer();
            AudioManager audioManager = (AudioManager) getSystemService(AUDIO_SERVICE);
            /*int volume_level2 = audioManager.getStreamVolume(AppRTCAudioManager.SELECTED_SOUND_CHANNEL);
            int maxVolume2 = audioManager.getStreamMaxVolume(AppRTCAudioManager.SELECTED_SOUND_CHANNEL);

            Log.d(Constants.LOG_TAG, "fey fey ses max volume_level="+volume_level2+" maksimum maxVolume="+maxVolume2);*/

            int gelen_volume = AyarlarYoneticiFragment.volume_konusma_level;
            //Log.d(Constants.LOG_TAG, "fey fey ses max gelen_volume="+gelen_volume);

            audioManager.setStreamVolume(AppRTCAudioManager.SELECTED_SOUND_CHANNEL, 1, 0);


        }

        timer.schedule(new TimerTask() {
            @Override
            public void run() {
                totalDurationTime = totalDurationTime + 1;

                // burada kod fazlalığı var incelenecek
                MainActivity.lastUserInteractionTime = 0;
                ScreensaverActivity2.lastUserInteractionTime2 = 0;
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        setAraniyorViews((60 - totalDurationTime), false);
                        setGorusmeStateText(false);
                    }
                });
                if (totalDurationTime >= Constants.MAX_DURATION_TIME) {

                    timer.cancel();
                    timer.purge();
                    timer = null;
                    //burada aramayı bitir ve uyarı mesajı göster

                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            logAndToast(getString(R.string.gorusme_sonlandirildi), false);
                            disconnect();
                        }
                    });
                }

            }
        }, 0, 1000);

    }

    Timer timerForTimeOut = null;
    int totalDurationForTimeOut = 0;

    private void startTimerForTimeOutDuration() {

        if (timerForTimeOut == null) {
            timerForTimeOut = new Timer();
        }

        timerForTimeOut.schedule(new TimerTask() {
            @Override
            public void run() {
                totalDurationForTimeOut = totalDurationForTimeOut + 1;

                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        setAraniyorViews((Constants.MAX_TIMEOUT_DURATION_TIME - totalDurationForTimeOut), true);
                        setGorusmeStateText(true);
                    }
                });
                if (totalDurationForTimeOut >= Constants.MAX_TIMEOUT_DURATION_TIME) {

                    timerForTimeOut.cancel();
                    timerForTimeOut.purge();
                    timerForTimeOut = null;
                    //burada aramayı bitir ve uyarı mesajı göster

                    setLedState(false);
                    disconnect();
                }

            }
        }, 0, 1500);

    }

    KeyPadService keyPadService;

    private void bindKaypadService() {

        Intent serviceConnIntent = new Intent(this, KeyPadService.class);
        bindService(serviceConnIntent, this, Context.BIND_AUTO_CREATE);

        if (keyPadService != null) {
            keyPadService.addListener(CallActivity.this, CallActivity.class.getName());
        }

    }

    private void unbindKeypadService() {

        unbindService(this);
        keyPadService = null;

    }

    @Override
    public void onServiceConnected(ComponentName name, IBinder service) {
        keyPadService = ((KeyPadService.MyBinder) service).getService();
        keyPadService.addListener(CallActivity.this, CallActivity.class.getName());
    }

    @Override
    public void onServiceDisconnected(ComponentName name) {
        keyPadService = null;
    }

    @Override
    public void onKeyPressed(String keyCode) {

        Log.d(Constants.LOG_TAG, "CallActivity onKeyPressed keyCode=" + keyCode);

        if (keyCode.equals(Constants.KEYPAD_BACK)) {
            bottomMenuBackClicked();
        } else if (keyCode.equals(Constants.KEYPAD_UP)) {
//            menuNavigationClicked(Constants.MENU_NAVIGATION_UP);
        } else if (keyCode.equals(Constants.KEYPAD_DOWN)) {
//            menuNavigationClicked(Constants.MENU_NAVIGATION_DOWN);
        } else if (keyCode.equals(Constants.KEYPAD_HOME)) {
//            bottomMenuOkayClicked();
        } else if (keyCode.equals(Constants.KEYPAD_CALL)) {

        } else if (keyCode.equals(Constants.KEYPAD_LOCK)) {
//            bottomLockClicked();
        } else {
//            bottomNumbersClicked(keyCode);
        }
    }

    @Override
    public void onRFIDDetected(String rfid) {
        DatabaseHelper databaseHelper = new DatabaseHelper(getApplicationContext());
        DoorPassword doorPassword = databaseHelper.getPasswordByRFID(rfid);
        if (doorPassword == null) {
            return;
        }

        if (!doorPassword.isActive()) {
            return;
        }


        Calendar calendar = Calendar.getInstance();
        SimpleDateFormat dateFormat = new SimpleDateFormat("dd.MM.yyyy HH:mm:ss");
        String formattedDate = dateFormat.format(calendar.getTime());

        DoorUnlockLog doorUnlockLog = new DoorUnlockLog();
        doorUnlockLog.setUnlockType(Constants.DOOR_UNLOCK_RFID);
        doorUnlockLog.setDatetime(formattedDate);
        doorUnlockLog.setRfid(rfid);
        doorUnlockLog.setDoorPassword("");
        doorUnlockLog.setPasswordLabel(doorPassword.getPasswordLabel());

        doorUnlockLog.setPasswordOwnerIP(doorPassword.getIp());
        databaseHelper.insertDoorLog(doorUnlockLog);

        keyPadService.sendDoorUnlock(Constants.ENUM_DOOR_UNLOCK_MAIN);
    }

    private void bottomMenuBackClicked() {

        if (!isConnected) {
            return;
        }

        setLedState(false);
        CallActivity.this.finish();

    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
//        Thread.setDefaultUncaughtExceptionHandler(null);

        door = false;

        disconnect();

        try {
            Helper.runRestartCameraShellCommand();
        } catch (Throwable e) {
            e.printStackTrace();
        }

        activityRunning = false;

        setLedState(false);

        if (keyPadService != null) {
            keyPadService.removeListener(CallActivity.class.getName());
        }

        if (timer != null) {
            timer.cancel();
            timer.purge();
            timer = null;
        }

        try {
            if (mediaPlayer != null) {
                mediaPlayer.stop();
                mediaPlayer = null;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        try {
            remoteSinks.remove(recording);
            if (recording != null) {
                recording.release();
            }

            if (mediaRecorderMic != null) {
                try {
                    mediaRecorderMic.stop();
                    mediaRecorderMic.reset();
                    mediaRecorderMic.release();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }

            //03.10.2023 ses ve görüşme kaydı kapatıldı
            //mergeVideoAndSoundFiles();

        } catch (Exception e) {
            e.printStackTrace();
        }

        try {
            if (!isCallReceiving) {
                unregisterReceiver(callRequestBroadcastReceiver);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        try {
            unregisterReceiver(doorUnlockBroadcastReceiver);
        } catch (Exception e) {
            e.printStackTrace();
        }

        unbindKeypadService();

        try {
            if (serverSocket != null) {
                serverSocket.close();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        if (myServerThread != null) {
            myServerThread.isDestroyed = true;
        }
        CallActivity.this.finish();
    }

    private void mergeVideoAndSoundFiles() {

        if ((recordSoundFilePath != null && !recordSoundFilePath.equals("")) &&
                (recordVideoFilePath != null && !recordVideoFilePath.equals("")) &&
                (recordSoundFileName != null && !recordSoundFileName.equals(""))) {

            String root = Environment.getExternalStorageDirectory().toString();
            File myDir = new File(root + "/saved_call_videos/final");
            myDir.mkdirs();

            String filePath = myDir.getAbsolutePath() + File.separator + recordSoundFileName + ".mp4";

            try {
                Movie video;
                video = MovieCreator.build(recordVideoFilePath);
                Movie audio;
                audio = MovieCreator.build(recordSoundFilePath);
                Track audioTrack = audio.getTracks().get(0);
                video.addTrack(audioTrack);
                Container out = new DefaultMp4Builder().build(video);
                FileChannel fileChannel = new FileOutputStream(new File(filePath)).getChannel();
                out.writeContainer(fileChannel);
                fileChannel.close();

                Thread.sleep(300);
                File fileVideo = new File(recordVideoFilePath);
                File fileSound = new File(recordSoundFileName);

                fileVideo.delete();
                fileSound.delete();

            } catch (Exception e) {
                e.printStackTrace();
            }

            recordSoundFilePath = null;
            recordVideoFilePath = null;
            recordSoundFileName = null;

        }

    }

    private void setLedState(boolean isActive) {

        int ledPortNumber = Helper.getPINFromGPIO("PE17");

        Process process = Helper.getProcess();
        Helper.setGPIO(process, Constants.GPIO_EXPORT, ledPortNumber, true, isActive);
        Helper.setGPIO(process, Constants.GPIO_DIRECTION, ledPortNumber, true, isActive);
        Helper.setGPIO(process, Constants.GPIO_VALUE, ledPortNumber, true, isActive);

    }

    @TargetApi(17)
    private DisplayMetrics getDisplayMetrics() {
        DisplayMetrics displayMetrics = new DisplayMetrics();
        WindowManager windowManager = (WindowManager) getApplication().getSystemService(Context.WINDOW_SERVICE);
        windowManager.getDefaultDisplay().getRealMetrics(displayMetrics);
        return displayMetrics;
    }

    @TargetApi(19)
    private static int getSystemUiVisibility() {
        int flags = View.SYSTEM_UI_FLAG_HIDE_NAVIGATION | View.SYSTEM_UI_FLAG_FULLSCREEN;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            flags |= View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY;
        }
        return flags;
    }

    @TargetApi(21)
    private void startScreenCapture() {
        MediaProjectionManager mediaProjectionManager = (MediaProjectionManager) getApplication().getSystemService(Context.MEDIA_PROJECTION_SERVICE);
        startActivityForResult(mediaProjectionManager.createScreenCaptureIntent(), CAPTURE_PERMISSION_REQUEST_CODE);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode != CAPTURE_PERMISSION_REQUEST_CODE)
            return;
        mediaProjectionPermissionResultCode = resultCode;
        mediaProjectionPermissionResultData = data;
        startCall();
    }

    private boolean useCamera2() {
//        return Camera2Enumerator.isSupported(this) && getIntent().getBooleanExtra(EXTRA_CAMERA2, true);
        return Camera2Enumerator.isSupported(this);
    }

    private boolean captureToTexture() {
//        return getIntent().getBooleanExtra(EXTRA_CAPTURETOTEXTURE_ENABLED, false);
        return false;
    }

    private @Nullable
    VideoCapturer createCameraCapturer(CameraEnumerator enumerator) {
        final String[] deviceNames = enumerator.getDeviceNames();

        // First, try to find front facing camera
        Logging.d(TAG, "Looking for front facing cameras.");
        for (String deviceName : deviceNames) {
            if (enumerator.isFrontFacing(deviceName)) {
                Logging.d(TAG, "Creating front facing camera capturer.");
                VideoCapturer videoCapturer = enumerator.createCapturer(deviceName, null);

                if (videoCapturer != null) {
                    return videoCapturer;
                }
            }
        }

        // Front facing camera not found, try something else
        Logging.d(TAG, "Looking for other cameras.");
        for (String deviceName : deviceNames) {
            if (!enumerator.isFrontFacing(deviceName)) {
                Logging.d(TAG, "Creating other camera capturer.");
                VideoCapturer videoCapturer = enumerator.createCapturer(deviceName, null);

                if (videoCapturer != null) {
                    return videoCapturer;
                }
            }
        }

        return null;
    }

    @TargetApi(21)
    private @Nullable
    VideoCapturer createScreenCapturer() {
        if (mediaProjectionPermissionResultCode != Activity.RESULT_OK) {
            reportError("User didn't give permission to capture the screen.");
            return null;
        }
        return new ScreenCapturerAndroid(mediaProjectionPermissionResultData, new MediaProjection.Callback() {
            @Override
            public void onStop() {
                reportError("User revoked permission to capture the screen.");
            }
        });
    }

    // Activity interfaces
    @Override
    public void onStop() {
        super.onStop();
        disconnect();
        activityRunning = false;
        // Don't stop the video when using screencapture to allow user to show other apps to the remote
        // end.
        if (peerConnectionClient != null && !screencaptureEnabled) {
            peerConnectionClient.stopVideoSource();
        }
//        if(cpuMonitor != null) {
//            cpuMonitor.pause();
//        }
    }

    @Override
    public void onStart() {
        super.onStart();
        activityRunning = true;
        // Video is not paused for screencapture. See onPause.
        if (peerConnectionClient != null && !screencaptureEnabled) {
            peerConnectionClient.startVideoSource();
        }
//        if(cpuMonitor != null) {
//            cpuMonitor.resume();
//        }
    }

    // CallFragment.OnCallEvents interface implementation.
    @Override
    public void onCallHangUp() {
        disconnect();
    }

    @Override
    public void onCameraSwitch() {
        if (peerConnectionClient != null) {
            peerConnectionClient.switchCamera();
        }
    }

    @Override
    public void onVideoScalingSwitch(ScalingType scalingType) {
        fullscreenRenderer.setScalingType(scalingType);
    }

    @Override
    public void onCaptureFormatChange(int width, int height, int framerate) {
        if (peerConnectionClient != null) {
            peerConnectionClient.changeCaptureFormat(width, height, framerate);

            Log.d(TAG, "Looking for front facing cameras." + width + " " + height + "/ pixel /" + framerate);
        }
    }

    @Override
    public boolean onToggleMic() {
        if (peerConnectionClient != null) {
            micEnabled = !micEnabled;
            peerConnectionClient.setAudioEnabled(micEnabled);
        }
        return micEnabled;
    }

    // Helper functions.
    private void toggleCallControlFragmentVisibility() {
//        if(! connected || ! callFragment.isAdded()) {
//            return;
//        }
//        // Show/hide call control fragment
//        callControlFragmentVisible = ! callControlFragmentVisible;
//        FragmentTransaction ft = getFragmentManager().beginTransaction();
//        if(callControlFragmentVisible) {
//            ft.show(callFragment);
//            ft.show(hudFragment);
//        } else {
//            ft.hide(callFragment);
//            ft.hide(hudFragment);
//        }
//        ft.setTransition(FragmentTransaction.TRANSIT_FRAGMENT_FADE);
//        ft.commit();
    }

    private void startCall() {
        if (appRtcClient == null) {
            Log.e(TAG, "AppRTC client is not allocated for a call.");
            return;
        }
        callStartedTimeMs = System.currentTimeMillis();

        // Start room connection.
//    logAndToast(getString(R.string.connecting_to, roomConnectionParameters.roomUrl));
        appRtcClient.connectToRoom(roomConnectionParameters);

        // Create and audio manager that will take care of audio routing,
        // audio modes, audio device enumeration etc.
        audioManager = AppRTCAudioManager.create(getApplicationContext());


        // Store existing audio settings and change audio mode to
        // MODE_IN_COMMUNICATION for best possible VoIP performance.
//        Log.d(TAG, "Starting the audio manager...");

        //Önce request i cevaplaması gerekiyor. bu yüzden sesi ve mikrofonu kapalı başlatıyoruz. ama zil paneli aranırken açık olarak başlatman gerekiyor.
        peerConnectionClient.setAudioEnabled(false);
//        audioManager.start(new AppRTCAudioManager.AudioManagerEvents() {
//            @Override
//            public void onAudioDeviceChanged(AppRTCAudioManager.AudioDevice audioDevice, Set<AppRTCAudioManager.AudioDevice> availableAudioDevices) {
//                onAudioManagerDevicesChanged(audioDevice, availableAudioDevices);
//            }
//        });

    }

    // Should be called from UI thread
    private void callConnected() {
        final long delta = System.currentTimeMillis() - callStartedTimeMs;
        Log.i(TAG, "Call connected: delay=" + delta + "ms");
        if (peerConnectionClient == null || isError) {
            Log.w(TAG, "Call is connected in closed or error state");
            return;
        }
        // Enable statistics callback.
        peerConnectionClient.enableStatsEvents(true, STAT_CALLBACK_PERIOD);
        setSwappedFeeds(false /* isSwappedFeeds */);
    }

    // This method is called when the audio manager reports audio device change,
    // e.g. from wired headset to speakerphone.
    private void onAudioManagerDevicesChanged(final AppRTCAudioManager.AudioDevice device, final Set<AppRTCAudioManager.AudioDevice> availableDevices) {
        Log.d(TAG, "onAudioManagerDevicesChanged: " + availableDevices + ", " + "selected: " + device);
    }

    // Disconnect from remote resources, dispose of local resources, and exit.
    private void disconnect() {
        try {
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    if (mediaPlayer != null) {
                        mediaPlayer.stop();
                        mediaPlayer = null;
                    }
                }
            });

        } catch (Exception e) {
            e.printStackTrace();
        }
        try {
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    if (audioManager != null) {
                        audioManager.stop();
                        audioManager = null;
                    }
                }
            });
        } catch (Exception e) {
            e.printStackTrace();
        }
        try {
            activityRunning = false;
            remoteProxyRenderer.setTarget(null);
            localProxyVideoSink.setTarget(null);
            if (appRtcClient != null) {
                appRtcClient.disconnectFromRoom();
                appRtcClient = null;
            }
            if (pipRenderer != null) {
                pipRenderer.release();
                pipRenderer = null;
            }
            if (videoFileRenderer != null) {
                videoFileRenderer.release();
                videoFileRenderer = null;
            }
            if (fullscreenRenderer != null) {
                fullscreenRenderer.release();
                fullscreenRenderer = null;
            }
            if (peerConnectionClient != null) {
                peerConnectionClient.close();
                peerConnectionClient = null;
            }

        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            Intent intent = new Intent(CallActivity.this, MainActivity.class);
            startActivity(intent);
            CallActivity.this.finish();
        }


    }

//    private void disconnectWithErrorMessage(final String errorMessage) {
//        if(commandLineRun || ! activityRunning) {
//            Log.e(TAG, "Critical error: " + errorMessage);
//            disconnect();
//        } else {
//            new AlertDialog.Builder(this).setTitle(getText(R.string.channel_error_title)).setMessage(errorMessage).setCancelable(false).setNeutralButton(R.string.ok, new DialogInterface.OnClickListener() {
//                @Override
//                public void onClick(DialogInterface dialog, int id) {
//                    dialog.cancel();
//                    disconnect();
//                }
//            }).create().show();
//        }
//    }

    boolean isLogEnabled = false;

    private void logAndToast(String msg, boolean isSuccess) {
        Log.d(Constants.LOG_TAG, msg);
        if (isLogEnabled) {
            Helper.showTopMessageBanner(CallActivity.this, msg, isSuccess);
        }
    }

    private void reportError(final String description) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                if (!isError) {
                    isError = true;
//                    disconnectWithErrorMessage(description);
                }
            }
        });
    }

    private @Nullable
    VideoCapturer createVideoCapturer() {
        final VideoCapturer videoCapturer;
//        String videoFileAsCamera = getIntent().getStringExtra(EXTRA_VIDEO_FILE_AS_CAMERA);
//        if(videoFileAsCamera != null) {
//            try {
//                videoCapturer = new FileVideoCapturer(videoFileAsCamera);
//            } catch(IOException e) {
//                reportError("Failed to open video file for emulated camera");
//                return null;
//            }
//        } else
        if (screencaptureEnabled) {
            return createScreenCapturer();
        } else if (useCamera2()) {
            if (!captureToTexture()) {
//                reportError(getString(R.string.camera2_texture_only_error));
                return null;
            }

            Logging.d(TAG, "Creating capturer using camera2 API.");
            videoCapturer = createCameraCapturer(new Camera2Enumerator(this));
        } else {
            Logging.d(TAG, "Creating capturer using camera1 API.");
            videoCapturer = createCameraCapturer(new Camera1Enumerator(captureToTexture()));
        }
        if (videoCapturer == null) {
            reportError("Failed to open camera");
            return null;
        }
        return videoCapturer;
    }

    private void setSwappedFeeds(boolean isSwappedFeeds) {
        Logging.d(TAG, "setSwappedFeeds: " + isSwappedFeeds);
        this.isSwappedFeeds = isSwappedFeeds;
        localProxyVideoSink.setTarget(isSwappedFeeds ? fullscreenRenderer : pipRenderer);
        remoteProxyRenderer.setTarget(isSwappedFeeds ? pipRenderer : fullscreenRenderer);
        fullscreenRenderer.setMirror(isSwappedFeeds);
        pipRenderer.setMirror(!isSwappedFeeds);
    }

    // -----Implementation of AppRTCClient.AppRTCSignalingEvents ---------------
    // All callbacks are invoked from websocket signaling looper thread and
    // are routed to UI thread.
    private void onConnectedToRoomInternal(final AppRTCClient.SignalingParameters params) {
        final long delta = System.currentTimeMillis() - callStartedTimeMs;

        signalingParameters = params;
        logAndToast("Creating peer connection, delay=" + delta + "ms", false);
        VideoCapturer videoCapturer = null;
        if (peerConnectionParameters.videoCallEnabled) {
            videoCapturer = createVideoCapturer();
        }
        peerConnectionClient.createPeerConnection(localProxyVideoSink, remoteSinks, videoCapturer, signalingParameters);
        if (signalingParameters.initiator) {
            logAndToast("Creating OFFER...", false);
            // Create offer. Offer SDP will be sent to answering client in
            // PeerConnectionEvents.onLocalDescription event.
            peerConnectionClient.createOffer();
        } else {
            if (params.offerSdp != null) {
                peerConnectionClient.setRemoteDescription(params.offerSdp);
                logAndToast("Creating ANSWER...", false);
                // Create answer. Answer SDP will be sent to offering client in
                // PeerConnectionEvents.onLocalDescription event.
                peerConnectionClient.createAnswer();
            }
            if (params.iceCandidates != null) {
                // Add remote ICE candidates from room.
                for (IceCandidate iceCandidate : params.iceCandidates) {
                    peerConnectionClient.addRemoteIceCandidate(iceCandidate);
                }
            }
        }

    }

    @Override
    public void onConnectedToRoom(final AppRTCClient.SignalingParameters params) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                onConnectedToRoomInternal(params);
            }
        });
    }

    @Override
    public void onRemoteDescription(final SessionDescription sdp) {
        final long delta = System.currentTimeMillis() - callStartedTimeMs;
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                if (peerConnectionClient == null) {
                    Log.e(TAG, "Received remote SDP for non-initilized peer connection.");
                    return;
                }
                logAndToast("Received remote " + sdp.type + ", delay=" + delta + "ms", false);
                peerConnectionClient.setRemoteDescription(sdp);
                if (!signalingParameters.initiator) {
                    logAndToast("Creating ANSWER...", false);
                    // Create answer. Answer SDP will be sent to offering client in
                    // PeerConnectionEvents.onLocalDescription event.
                    peerConnectionClient.createAnswer();
                }
            }
        });
    }

    @Override
    public void onRemoteIceCandidate(final IceCandidate candidate) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                if (peerConnectionClient == null) {
                    Log.e(TAG, "Received ICE candidate for a non-initialized peer connection.");
                    return;
                }
                peerConnectionClient.addRemoteIceCandidate(candidate);
            }
        });
    }

    @Override
    public void onRemoteIceCandidatesRemoved(final IceCandidate[] candidates) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                if (peerConnectionClient == null) {
                    Log.e(TAG, "Received ICE candidate removals for a non-initialized peer connection.");
                    return;
                }
                peerConnectionClient.removeRemoteIceCandidates(candidates);
            }
        });
    }

    @Override
    public void onChannelClose() {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                logAndToast("Remote end hung up; dropping PeerConnection", false);
                disconnect();
            }
        });
    }

    @Override
    public void onChannelError(final String description) {
        reportError(description);
    }

    // -----Implementation of PeerConnectionClient.PeerConnectionEvents.---------
    // Send local peer connection SDP and ICE candidates to remote party.
    // All callbacks are invoked from peer connection client looper thread and
    // are routed to UI thread.
    @Override
    public void onLocalDescription(final SessionDescription sdp) {
        final long delta = System.currentTimeMillis() - callStartedTimeMs;
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                if (appRtcClient != null) {
                    logAndToast("Sending " + sdp.type + ", delay=" + delta + "ms", false);
                    if (signalingParameters.initiator) {
                        appRtcClient.sendOfferSdp(sdp);
                    } else {
                        appRtcClient.sendAnswerSdp(sdp);
                    }
                }
                if (peerConnectionParameters.videoMaxBitrate > 0) {
                    Log.d(TAG, "Set video maximum bitrate: " + peerConnectionParameters.videoMaxBitrate);
                    peerConnectionClient.setVideoMaxBitrate(peerConnectionParameters.videoMaxBitrate);
                }
            }
        });
    }

    @Override
    public void onIceCandidate(final IceCandidate candidate) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                if (appRtcClient != null) {
                    appRtcClient.sendLocalIceCandidate(candidate);
                }
            }
        });
    }

    @Override
    public void onIceCandidatesRemoved(final IceCandidate[] candidates) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                if (appRtcClient != null) {
                    appRtcClient.sendLocalIceCandidateRemovals(candidates);
                }
            }
        });
    }

    boolean isConnected = false;

    @Override
    public void onIceConnected() {
        isConnected = true;
        final long delta = System.currentTimeMillis() - callStartedTimeMs;
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                logAndToast("ICE connected, delay=" + delta + "ms", false);
            }
        });
    }

    @Override
    public void onIceDisconnected() {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                logAndToast("ICE disconnected", false);
            }
        });
    }

    @Override
    public void onConnected() {
        final long delta = System.currentTimeMillis() - callStartedTimeMs;
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                logAndToast("DTLS connected, delay=" + delta + "ms", false);
                connected = true;
                callConnected();
            }
        });
    }

    @Override
    public void onDisconnected() {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                logAndToast("DTLS disconnected", false);
                connected = false;
                disconnect();
            }
        });
    }

    @Override
    public void onPeerConnectionClosed() {
    }

    @Override
    public void onPeerConnectionStatsReady(final StatsReport[] reports) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                if (!isError && connected) {
                    hudFragment.updateEncoderStatistics(reports);
                }
            }
        });
    }

    @Override
    public void onPeerConnectionError(final String description) {
        reportError(description);
    }


    private Camera mCamera;
    public static String SERVERIP = "localhost";
    public MyCameraView mPreview;
    public static final int SERVERPORT = 9191;
    private Handler handler = new Handler();
    ServerSocket serverSocket = null;
    MyServerThread myServerThread;

    private void setVideoSenderIfNeeded() {

        if (isDeviceSube) {
            return;
        }

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

        } catch (Exception e) {
            e.printStackTrace();
            Log.d("ServerThread", "run: erro");
        }

    }

    private Camera getCameraInstance() {
        Camera camera = null;
        try {
            camera = Camera.open();
//            Camera.Parameters parameters = camera.getParameters();
//            parameters.setPictureSize(320, 240);
//            parameters.setPreviewSize(320, 240);
//            parameters.setPreviewFrameRate(25);
//            camera.setParameters(parameters);

        } catch (Exception e) {
            e.printStackTrace();
        }
        return camera;
    }

    private String getLocalIpAddress() {
        try {
            for (Enumeration<NetworkInterface> en = NetworkInterface.getNetworkInterfaces(); en.hasMoreElements(); ) {
                NetworkInterface intf = en.nextElement();
                for (Enumeration<InetAddress> enumIpAddr = intf.getInetAddresses(); enumIpAddr.hasMoreElements(); ) {
                    InetAddress inetAddress = enumIpAddr.nextElement();
                    if (!inetAddress.isLoopbackAddress() && inetAddress instanceof Inet4Address) {
                        return inetAddress.getHostAddress().toString();
                    }
                }
            }
        } catch (SocketException ex) {
            ex.printStackTrace();
        }
        return null;
    }

}

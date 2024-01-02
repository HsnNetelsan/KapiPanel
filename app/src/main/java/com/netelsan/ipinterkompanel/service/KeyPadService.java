package com.netelsan.ipinterkompanel.service;

import android.app.ActivityManager;
import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.os.Binder;
import android.os.IBinder;
import android.util.Log;

import androidx.annotation.Nullable;

import com.coremedia.iso.Hex;
import com.netelsan.ipinterkompanel.Constants;
import com.netelsan.ipinterkompanel.Helper;
import com.netelsan.ipinterkompanel.R;
import com.netelsan.ipinterkompanel.activity.MainActivity;
import com.netelsan.ipinterkompanel.activity.ScreensaverActivity2;
import com.netelsan.ipinterkompanel.activity.guvenlik_sistemi.DeviceDisplacementAlarmActivity;
import com.netelsan.ipinterkompanel.listener.KeyPadListener;
import com.netelsan.ipinterkompanel.serial.SerialPort;
import com.netelsan.ipinterkompanel.tcp.Server;
import com.netelsan.ipinterkompanel.webrtc.AppRTCAudioManager;
import com.netelsan.ipinterkompanel.webrtc.activity.CallActivity;

import java.io.DataInputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.SocketAddress;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.List;
import java.util.stream.Collectors;

public class KeyPadService extends Service {

    private final IBinder mBinder = new MyBinder();

    SerialPort mSerialPort;
    private OutputStream mOutputStream;
    private InputStream mInputStream;
    DataInputStream dataInputStream;

    ScreensaverActivity2 screensaverActivity2 = new ScreensaverActivity2();

    private boolean bShutDown;
    byte[] buffer = new byte[64];
    private static final char[] HEX_ARRAY = "0123456789ABCDEF".toCharArray();

    Hashtable<String, KeyPadListener> listeners = new Hashtable<>();

    MediaPlayer mediaPlayerBuzzer;

    public KeyPadService() {

    }

    public KeyPadService(Context context) {
        super();
        Log.i(Constants.LOG_TAG, "KeyPadService class");
    }


    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
//        Bundle extras = intent.getExtras();
//        Log.d("service", "onBind");
        // Get messager from the Activity

        return mBinder;
    }

    public class MyBinder extends Binder {
        public KeyPadService getService() {
            return KeyPadService.this;
        }
    }

    @Override
    public void onCreate() {
        super.onCreate();
//        Log.i(Constants.LOG_TAG, "onCreate()");

    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        super.onStartCommand(intent, flags, startId);

        Helper.setDeviceDisplacementEnabled(getApplicationContext(), false);// cihaz açıldığında öncelikle sadece kullanıcının set ettiği değeri false a çekiyoruz

        try {
            mSerialPort = new SerialPort(new File("/dev/ttyS1"), 19200, 0);
            mOutputStream = mSerialPort.getOutputStream();
            mInputStream = mSerialPort.getInputStream();
            dataInputStream = new DataInputStream(mInputStream);

            Thread serialReadThread = new Thread(new KeyPadThread(), "Serial Read Thread");
            serialReadThread.start();

        } catch (Exception e) {
            e.printStackTrace();
        }
        return START_STICKY;
    }

    public void addListener(KeyPadListener keyPadListener, String className) {

        KeyPadListener keyPadListenerTemp = listeners.get(className);
        if (keyPadListenerTemp == null) {
            listeners.put(className, keyPadListener);
        }

    }

    public void removeListener(String className) {
        KeyPadListener keyPadListener = listeners.get(className);
        if (keyPadListener != null) {
            listeners.remove(className);
        }
    }


    public void sendDoorUnlock(int enmDoorUnlock) {
        if (mSerialPort == null || mOutputStream == null)
            return;

        Intent broadcastIntent = new Intent("com.netelsan.ipinterkompanel.doorunlock");
        sendBroadcast(broadcastIntent);
        byte[] byteArray2 = "[DU]".getBytes(StandardCharsets.US_ASCII);
        int relayTime = Helper.getRelayTime(getApplicationContext());

        Log.d("HK", "HK get byte: " + relayTime);

        if (enmDoorUnlock == Constants.ENUM_DOOR_UNLOCK_MAIN) {
            try {
                for (int i = 0; i <= 1; i++) {
                    mOutputStream.write("[T".getBytes());
                    mOutputStream.write(relayTime);
                    mOutputStream.write("]".getBytes());
                    mOutputStream.flush();


                    mOutputStream.write("[DU]".getBytes());
                    mOutputStream.flush();
                    Log.d("HK", "HK unlock main");
                }

//                mOutputStream.write("[DU]".getBytes());
//                mOutputStream.flush();
            } catch (IOException e) {
                e.printStackTrace();
            }
        } else if (enmDoorUnlock == Constants.ENUM_DOOR_UNLOCK_PEDESTRAIN) {
            try {
                for (int i = 0; i <= 1; i++) {
                    mOutputStream.write("[T".getBytes());
                    mOutputStream.write(relayTime);
                    mOutputStream.write("]".getBytes());
                    mOutputStream.flush();

                    mOutputStream.write("[PU]".getBytes());
                    Log.d("HK", "HK unlock main2");
                    mOutputStream.flush();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        } else if (enmDoorUnlock == Constants.ENUM_DOOR_UNLOCK_LONG) {
            try {
                mOutputStream.write("[LU]".getBytes());
                mOutputStream.flush();
            } catch (IOException e) {
                e.printStackTrace();
            }
        } else if (enmDoorUnlock == Constants.ENUM_DOOR_UNLOCK_REVERSE_LONG) {
            try {
                mOutputStream.write("[AU]".getBytes());
                mOutputStream.flush();
            } catch (IOException e) {
                e.printStackTrace();
            }
        } else if (enmDoorUnlock == Constants.DOOR_UNLOCK_FOR_TEST) {
            try {

                for (int i = 0; i <= 1; i++) {
                    mOutputStream.write("[T".getBytes());
                    mOutputStream.write(relayTime);
                    mOutputStream.write("]".getBytes());
                    mOutputStream.flush();


                    mOutputStream.write("[DU]".getBytes());
                    mOutputStream.flush();
                    Log.d("HK", "HK unlock main");
                }

            } catch (IOException e) {
                e.printStackTrace();
            }
        } else if (enmDoorUnlock == Constants.DOOR_UNLOCK_FOR_TEST2) {
            try {

                for (int i = 0; i <= 1; i++) {
                    mOutputStream.write("[T".getBytes());
                    mOutputStream.write(relayTime);
                    mOutputStream.write("]".getBytes());
                    mOutputStream.flush();


                    mOutputStream.write("[PU]".getBytes());
                    mOutputStream.flush();
                    Log.d("HK", "HK unlock main");


                }

            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        if (enmDoorUnlock != Constants.DOOR_UNLOCK_FOR_TEST && enmDoorUnlock != Constants.DOOR_UNLOCK_FOR_TEST2)
            playBuzzerSoundForOpenDoor();

    }

    private void playBuzzerSoundForOpenDoor() {
        try {
            MediaPlayer mediaPlayerBuzzer = MediaPlayer.create(getApplicationContext(), R.raw.unlock_door);
            mediaPlayerBuzzer.setAudioStreamType(AudioManager.STREAM_MUSIC);
            mediaPlayerBuzzer.start();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void unlockDoorStateVoice(String stateVoice) {

        SharedPreferences sharedPreferences = getApplicationContext().getSharedPreferences(Constants.SHARED_PREFS, 0);
        boolean isEnabled = sharedPreferences.getBoolean(Constants.KEY_TUS_SESI, false);

        if (isEnabled) {
            mediaPlayerBuzzer.release();
            if (stateVoice.equals("invalid")) {
                mediaPlayerBuzzer = MediaPlayer.create(getApplicationContext(), R.raw.invalid_password);
            } else if (stateVoice.equals("invalid_RF")) {
                mediaPlayerBuzzer = MediaPlayer.create(getApplicationContext(), R.raw.invalid);
            } else {
                mediaPlayerBuzzer = MediaPlayer.create(getApplicationContext(), R.raw.unlock_door);
            }


            mediaPlayerBuzzer.start();
        }

    }

    @Override
    public void onDestroy() {
        Log.i(Constants.LOG_TAG, "serviceOnDestroy()");

        super.onDestroy();

        Intent broadcastIntent = new Intent("com.netelsan.ipinterkompanel.RestartKeyPadService");
        sendBroadcast(broadcastIntent);

        if (mediaPlayerBuzzer != null) {
            mediaPlayerBuzzer.stop();
            mediaPlayerBuzzer = null;
        }

    }

    public static String bytesToHex(byte[] bytes) {
        char[] hexChars = new char[bytes.length * 2];
        for (int j = 0; j < bytes.length; j++) {
            int v = bytes[j] & 0xFF;
            hexChars[j * 2] = HEX_ARRAY[v >>> 4];
            hexChars[j * 2 + 1] = HEX_ARRAY[v & 0x0F];
        }
        return new String(hexChars);
    }

    class KeyPadThread implements Runnable {

        public void run() {
            int size;
//            String strMessage;

            while (!bShutDown) {

                try {
                    if (mInputStream == null)
                        return;
                    size = mInputStream.read(buffer);

//                    size = 0;
//                    String receivedKeyCode = "";
//                    int totalSize = dataInputStream.read(buffer, 0, buffer.length);
//                    for(int index = 0; index < totalSize; index++) {
//                        int unsignedByte = dataInputStream.readUnsignedByte();
//                        char pressedKeyChar = (char) unsignedByte;
//
//                        receivedKeyCode = receivedKeyCode + pressedKeyChar + "";
//
//                    }
//
//                    Log.d(Constants.LOG_TAG, "KeyPad pressedKeyChar: " + receivedKeyCode);
//                    receivedKeyCode = "";

                    if (size > 0) {

                        String detectedCode = "";
                        String getCode = "";
                        for (int index = 0; index < buffer.length; index++) {
                            detectedCode = detectedCode + buffer[index];
                        }
                        Log.d(Constants.LOG_TAG, "KeyPad detectedCode: " + detectedCode);

                        Intent closeIntent = new Intent("com.netelsan.ipinterkompanel.CLOSE_SCREESAVER_ACTIVITY");


                        //   screensaverActivity2.keyPressed(detectedCode);
                        // Log.d(Constants.LOG_TAG, "KeyPad detectedCode 2: " + new String(buffer, 1, size - 1));

                        String hex1 = bytesToHex(buffer).substring(2, 4);
                        String hex2 = bytesToHex(buffer).substring(4, 6);
                        String hex3 = bytesToHex(buffer).substring(6, 8);
                        String code = hex3 + hex2 + hex1;
                        String decimalPlus = "";
                        char[] Temp_Char = code.toCharArray();

                        int decimalValue = Integer.parseInt(code, 16);
//                        for (int i = 0; i < code.length(); i += 2) {
//                            String str = code.substring(i, i + 2);
//                            int decimalValue = Integer.parseInt(str, 16);
//                            Log.d("HK", "str: " + str + "  value: " + decimalValue);
//                            decimalPlus = decimalPlus + decimalValue;
//
//                        }
                        Log.d("HK", "HK RFID Code: " + decimalValue);


                        if (buffer[0] == '#') {

                            char pressedKeyChar = (char) buffer[1];
                            String pressedKeyString = pressedKeyChar + "";
                            Log.d(Constants.LOG_TAG, "Keypad Detected: " + pressedKeyString);
                            if (pressedKeyString.equals(Constants.KEYPAD_CIHAZ_SOKME) || pressedKeyString.equals(Constants.KEYPAD_CIHAZ_TAKMA)) {
                                sokmeTakmaOperations(pressedKeyString);
                            } else {
                                sendKeyPressedToAllListeners(pressedKeyString, false);
                                sendBroadcast(closeIntent);

                            }

                        } else if (buffer[0] == '*') {


                            // RF Buffer çalışması
//                            String strMessage = new String(buffer, 1, size - 1);
//                            for (int i=0;i<10;i++)
//                            {
//                                Log.d(Constants.LOG_TAG, " genel RFID Detected1: " + buffer[i]);
//                            }

                            detectedCode = removeLastZeroCharsIfNeeded(detectedCode);
                            String strMessage = detectedCode;

                            Log.d(Constants.LOG_TAG, " genel RFID Detected2: " + strMessage);
                            sendKeyPressedToAllListeners(strMessage, true);
                            sendBroadcast(closeIntent);

                        }

                    } else {
                        Log.d(Constants.LOG_TAG, "NO DATA !!!");
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                    return;
                }
            }

            Log.w(Constants.LOG_TAG, "Shutting down main loop !");

            try {
                mInputStream.close();
                mOutputStream.close();
            } catch (IOException e) {
                e.printStackTrace();
            }

            // close serial port
            if (mSerialPort != null) {
                mSerialPort.close();
                mSerialPort = null;
            }
        }
    }

    boolean isCihazSokmeAlgilandi = false;
    //    int cihazSokmeAlarmCounter = 1;
    boolean isFirstRead = true;
    String lastValueOfAlarmSwitch = Constants.KEYPAD_CIHAZ_TAKMA;

    private void sokmeTakmaOperations(String pressedKeyString) {

        boolean isDeviceDisplacementEnabled;
        if (isFirstRead) {
            isFirstRead = false;
            if (pressedKeyString.equals(Constants.KEYPAD_CIHAZ_SOKME)) { // cihaz yerinden sökülmüş olarak başlatıldı. alarmı devre dışı bırak
                Helper.setDeviceDisplacementEnabled(getApplicationContext(), true);
            } else { // cihaz yerine takılı olarak başlatıldı. alarmı devre devreye girsin
                Helper.setDeviceDisplacementEnabled(getApplicationContext(), false);
            }
        } else {
            isDeviceDisplacementEnabled = Helper.isDeviceDisplacementEnabled(getApplicationContext());
            if (isDeviceDisplacementEnabled) {

                boolean isDeviceDisplacementStateChange = isDeviceDisplacementStateChange(pressedKeyString, lastValueOfAlarmSwitch);
                if (isDeviceDisplacementStateChange) {
                    if (lastValueOfAlarmSwitch.equals(Constants.KEYPAD_CIHAZ_TAKMA)) {

                    } else {
                        Helper.setDeviceDisplacementEnabled(getApplicationContext(), false);
                        return;
                    }
                }

            } else { // bu noktada alarm devrede
                if (lastValueOfAlarmSwitch.equals(Constants.KEYPAD_CIHAZ_TAKMA)) {// cihaz bir öncekinde yerine takılıydı
                    if (pressedKeyString.equals(Constants.KEYPAD_CIHAZ_SOKME)) {// alarm verdir
                        Helper.setDeviceDisplacementEnabled(getApplicationContext(), false);
                    }
                } else {// cihaz bir öncekinde yerine takılı değildi
                    if (pressedKeyString.equals(Constants.KEYPAD_CIHAZ_TAKMA)) {
                        Helper.setDeviceDisplacementEnabled(getApplicationContext(), false);
                    }
                }
            }

        }

        lastValueOfAlarmSwitch = pressedKeyString;

        isDeviceDisplacementEnabled = Helper.isDeviceDisplacementEnabled(getApplicationContext());
//        Log.d(Constants.LOG_TAG, "isDeviceDisplacementEnabled=" + isDeviceDisplacementEnabled + "    alarmSwitchValue=" + alarmSwitchValue);
        if (isDeviceDisplacementEnabled) { //eğer cihaz sökme aktifse alarm verdirme
            return;
        }

        if (pressedKeyString.equals(Constants.KEYPAD_CIHAZ_SOKME)) {// cihaz yerinden çıkartıldı demektir.

//            cihazSokmeAlarmCounter = cihazSokmeAlarmCounter + 1;
//            if (cihazSokmeAlarmCounter < 3) {
//                return;
//            }

            if (isCihazSokmeAlgilandi) {
                return;
            }
            isCihazSokmeAlgilandi = true;

            if (DeviceDisplacementAlarmActivity.isActive) {
                return;
            }

            startCihazSokulduScreen();

        } else {
            isCihazSokmeAlgilandi = false;
//            cihazSokmeAlarmCounter = 1;
        }

    }

    private void startCihazSokulduScreen() {

        Intent intent = new Intent(getApplicationContext(), DeviceDisplacementAlarmActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(intent);

    }

    private boolean isDeviceDisplacementStateChange(String currentValue, String lastValue) {

        if (!currentValue.equals(lastValue)) {
            return true;
        }

        return false;
    }

    private String removeLastZeroCharsIfNeeded(String detectedCode) {
        detectedCode = detectedCode.replaceAll("-", "");
        int counter = 0;
        while (true) {

            if (counter >= 75) {
                break;
            }
            String lastChar = detectedCode.substring(detectedCode.length() - 1);
            //  Log.d(Constants.LOG_TAG, "removeLastZeroCharsIfNeeded lastChar=" + lastChar);
            if (lastChar.equals("0")) {
                detectedCode = removeLastChar(detectedCode);
                //  Log.d(Constants.LOG_TAG, "removeLastZeroCharsIfNeeded detectedCode=" + detectedCode);
            } else {
                break;
            }

            counter = counter + 1;
        }

        return detectedCode;
    }

    private String removeLastChar(String str) {
        return str.substring(0, str.length() - 1);
    }

    private void sendKeyPressedToAllListeners(String pressedKey, boolean isRFID) {

        if (!isRFID) {
            playBuzzerSound(pressedKey);
        }

        if (MainActivity.lastUserInteractionTime > Constants.IDLE_TIME) {

            MainActivity.lastUserInteractionTime = 0;
            ScreensaverActivity2.lastUserInteractionTime2 = 0;

            sendBroadcastForMainActivity(pressedKey);

            if (isRFID) {
                sendRFIDDetectedForScreenSaveState(pressedKey);
            }

            return;
        }
        if (ScreensaverActivity2.lastUserInteractionTime2 > Constants.IDLE_TIME) {

            MainActivity.lastUserInteractionTime = 0;
            ScreensaverActivity2.lastUserInteractionTime2 = 0;

            sendBroadcastForMainActivity(pressedKey);

            if (isRFID) {
                sendRFIDDetectedForScreenSaveState(pressedKey);
            }

            return;
        }

        MainActivity.lastUserInteractionTime = 0;
        ScreensaverActivity2.lastUserInteractionTime2 = 0;

        if (listeners == null || listeners.size() == 0) {
            return;
        }

        Enumeration<String> enumeration = listeners.keys();
        while (enumeration.hasMoreElements()) {
            String classNameForListener = enumeration.nextElement();

            KeyPadListener keyPadListener = listeners.get(classNameForListener);

            if (isRFID) {
                keyPadListener.onRFIDDetected(pressedKey);
            } else {
                keyPadListener.onKeyPressed(pressedKey);
            }

        }

//        if (!isRFID) {
//            playBuzzerSound(pressedKey);
//        }

    }

    private void sendRFIDDetectedForScreenSaveState(String RFIDCode) {
        if (listeners == null || listeners.size() == 0) {
            return;
        }

        Enumeration<String> enumeration = listeners.keys();
        while (enumeration.hasMoreElements()) {
            String classNameForListener = enumeration.nextElement();

            KeyPadListener keyPadListener = listeners.get(classNameForListener);

            keyPadListener.onRFIDDetected(RFIDCode);
        }

    }

    AudioManager audioManager;
    int volume_level;

    private void playBuzzerSound(String pressedKey) {

        boolean isTusSesiEnabled = Helper.isTusSesiEnabled(getApplicationContext());
        if (!isTusSesiEnabled) {
            return;
        }

        try {

//            ActivityManager am = (ActivityManager) this.getSystemService(ACTIVITY_SERVICE);
//            List<ActivityManager.RunningTaskInfo> taskInfo = am.getRunningTasks(1);
//            Log.d("topActivity", "CURRENT Activity ::" + taskInfo.get(0).topActivity.getClassName());


            SharedPreferences sharedPreferences = getApplicationContext().getSharedPreferences(Constants.SHARED_PREFS, 0);
            boolean isEnabled = sharedPreferences.getBoolean(Constants.KEY_TUS_SESI, false);

            int volume = sharedPreferences.getInt(Constants.KEY_VOLUME, 8);

            if (isEnabled) {
                if (mediaPlayerBuzzer != null) {
                    mediaPlayerBuzzer.stop();
                    mediaPlayerBuzzer.release();
                    mediaPlayerBuzzer = null;
                    Log.d("feyhsnbuffer", "feyhsnbuffer mediaPlayerBuzzer nulllll değilmiş patladiiiiiiiiiiiiiii");
                } else {
                    Log.d("feyhsnbuffer", "feyhsnbuffer mediaPlayerBuzzer nulll mışşşş patladiiiiiiiiiiiiiii");
                }

                if (pressedKey.equals(Constants.KEYPAD_0)) {
                    mediaPlayerBuzzer = MediaPlayer.create(getApplicationContext(), R.raw.number_0_tone);
                } else if (pressedKey.equals(Constants.KEYPAD_1)) {
                    mediaPlayerBuzzer = MediaPlayer.create(getApplicationContext(), R.raw.number_1_tone);
                } else if (pressedKey.equals(Constants.KEYPAD_2)) {
                    mediaPlayerBuzzer = MediaPlayer.create(getApplicationContext(), R.raw.number_2_tone);
                } else if (pressedKey.equals(Constants.KEYPAD_3)) {
                    mediaPlayerBuzzer = MediaPlayer.create(getApplicationContext(), R.raw.number_3_tone);
                } else if (pressedKey.equals(Constants.KEYPAD_4)) {
                    mediaPlayerBuzzer = MediaPlayer.create(getApplicationContext(), R.raw.number_4_tone);
                } else if (pressedKey.equals(Constants.KEYPAD_5)) {
                    mediaPlayerBuzzer = MediaPlayer.create(getApplicationContext(), R.raw.number_5_tone);
                } else if (pressedKey.equals(Constants.KEYPAD_6)) {
                    mediaPlayerBuzzer = MediaPlayer.create(getApplicationContext(), R.raw.number_6_tone);
                } else if (pressedKey.equals(Constants.KEYPAD_7)) {
                    mediaPlayerBuzzer = MediaPlayer.create(getApplicationContext(), R.raw.number_7_tone);
                } else if (pressedKey.equals(Constants.KEYPAD_8)) {
                    mediaPlayerBuzzer = MediaPlayer.create(getApplicationContext(), R.raw.number_8_tone);
                } else if (pressedKey.equals(Constants.KEYPAD_9)) {
                    mediaPlayerBuzzer = MediaPlayer.create(getApplicationContext(), R.raw.number_9_tone);
                } else if (pressedKey.equals(Constants.KEYPAD_LOCK)) {
                    mediaPlayerBuzzer = MediaPlayer.create(getApplicationContext(), R.raw.number_star_tone);
                } else if (pressedKey.equals(Constants.KEYPAD_CALL)) {
                    mediaPlayerBuzzer = MediaPlayer.create(getApplicationContext(), R.raw.number_diyez_tone);
                } else if (pressedKey.equals(Constants.KEYPAD_BACK)) {
                    mediaPlayerBuzzer = MediaPlayer.create(getApplicationContext(), R.raw.number_back_tone);
                } else if (pressedKey.equals(Constants.KEYPAD_UP)) {
                    mediaPlayerBuzzer = MediaPlayer.create(getApplicationContext(), R.raw.number_up_tone);
                } else if (pressedKey.equals(Constants.KEYPAD_DOWN)) {
                    mediaPlayerBuzzer = MediaPlayer.create(getApplicationContext(), R.raw.number_down_tone);
                } else if (pressedKey.equals(Constants.KEYPAD_HOME)) {
                    mediaPlayerBuzzer = MediaPlayer.create(getApplicationContext(), R.raw.number_okay_tone);
                    // mediaPlayerBuzzer = MediaPlayer.create(getApplicationContext(), R.raw.doorman_calling);
                }

                audioManager = (AudioManager) getSystemService(AUDIO_SERVICE);
//            volume_level = audioManager.getStreamVolume(AppRTCAudioManager.SELECTED_SOUND_CHANNEL);
//            audioManager.setStreamVolume(AudioManager.STREAM_MUSIC, volume_level, 0);
                //    Log.d("voice", "playBuzzerSound: " + audioManager.getStreamMaxVolume(AudioManager.STREAM_MUSIC));
                audioManager.setStreamVolume(AudioManager.STREAM_MUSIC, audioManager.getStreamMaxVolume(AudioManager.STREAM_MUSIC) - volume, 0);
                mediaPlayerBuzzer.setAudioStreamType(AudioManager.STREAM_MUSIC);
                // mediaPlayerBuzzer.setVolume(1.0f, 1.0f);
                mediaPlayerBuzzer.start();
            }

        } catch (Exception e) {

            Log.d("feyhsnbuffer", "feyhsnbuffer mediaPlayerBuzzer patladiiiiiiiiiiiiiii");
            e.printStackTrace();
        }

    }

    private void sendBroadcastForMainActivity(String keyCode) {
        Intent broadcastIntent = new Intent("com.netelsan.panel.KeyPadDayDream");
        sendBroadcast(broadcastIntent);
    }

    @Override
    public void onTaskRemoved(Intent rootIntent) {
        Log.i(Constants.LOG_TAG, "onTaskRemoved()");

        // workaround for kitkat: set an alarm service to trigger service again
//        Intent intent = new Intent(getApplicationContext(), KeyPadService.class);
//        PendingIntent pendingIntent = PendingIntent.getService(this, 1, intent, PendingIntent.FLAG_ONE_SHOT);
//        AlarmManager alarmManager = (AlarmManager) getSystemService(Context.ALARM_SERVICE);
//        alarmManager.set(AlarmManager.RTC_WAKEUP, SystemClock.elapsedRealtime() + 5000, pendingIntent);

        super.onTaskRemoved(rootIntent);

    }

    @Override
    public void onLowMemory() {
        super.onLowMemory();
        Log.i(Constants.LOG_TAG, "onLowMemory()");
    }


}
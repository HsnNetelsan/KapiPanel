//package com.netelsan.ipinterkompanel.serial;
//
//import android.util.Log;
//
//import androidx.appcompat.app.AppCompatActivity;
//
//import com.netelsan.ipinterkompanel.Constants;
//import com.netelsan.ipinterkompanel.activity.MainActivity;
//import com.netelsan.ipinterkompanel.listener.KeyPadListener;
//
//import java.io.File;
//import java.io.IOException;
//import java.io.InputStream;
//import java.io.OutputStream;
//import java.util.Enumeration;
//import java.util.Hashtable;
//
//
//public class KeypadAccess implements Runnable {
//
//    SerialPort mSerialPort;
//    private OutputStream mOutputStream;
//    private InputStream mInputStream;
//
//    private boolean bShutDown;
//    byte[] buffer = new byte[64];
//
//    AppCompatActivity baseActivity;
//
//    Hashtable<String, KeyPadListener> listeners = new Hashtable<>();
//
//    public KeypadAccess(AppCompatActivity baseActivity) {
//
//        this.baseActivity = baseActivity;
//
//        try {
//            /* Open the serial port */
//
//            mSerialPort = new SerialPort(new File("/dev/ttyS1"), 19200, 0);
//            mOutputStream = mSerialPort.getOutputStream();
//            mInputStream = mSerialPort.getInputStream();
//
//            Thread serialReadThread = new Thread(this, "Serial Read Thread");
//            serialReadThread.start();
//
//        } catch(Exception e) {
//            e.printStackTrace();
//        }
//    }
//
//    public void addListener(KeyPadListener keyPadListener, String className) {
//
//        KeyPadListener keyPadListenerTemp = listeners.get(className);
//        if(keyPadListenerTemp == null) {
//            listeners.put(className, keyPadListener);
//        }
//
//    }
//
//    public void removeListener(String className) {
//        KeyPadListener keyPadListener = listeners.get(className);
//        if(keyPadListener != null) {
//            listeners.remove(className);
//        }
//    }
//
//    public void shutdown() {
//        Log.i(Constants.LOG_TAG, "Shutting itself down !");
//        this.bShutDown = true;
//    }
//
//    public void sendDoorUnlock() {
//        if(mSerialPort == null || mOutputStream == null)
//            return;
//
//        try {
//            mOutputStream.write("[DU]".getBytes());
//            mOutputStream.flush();
//        } catch(IOException e) {
//            // TODO Auto-generated catch block
//            e.printStackTrace();
//        }
//        Log.i(Constants.LOG_TAG, "sendDoorUnlock");
//    }
//
//    @Override
//    public void run() {
//
//        int size;
//        String strMessage;
//
//        while(bShutDown == false) {
//
//            try {
//                if(mInputStream == null)
//                    return;
//                size = mInputStream.read(buffer);
//                if(size > 0) {
//                    if(buffer[0] == '#') {
//
//                        char pressedKeyChar = (char) buffer[1];
//                        String pressedKeyString = pressedKeyChar + "";
//                        sendKeyPressedToAllListeners(pressedKeyString);
//
//                    } else if(buffer[0] == '*') {
//                        strMessage = new String(buffer, 1, size - 1);
//                        Log.d(Constants.LOG_TAG, "TAG: " + strMessage);
//                    }
//
//                } else
//                    Log.d(Constants.LOG_TAG, "NO DATA !!!");
//            } catch(IOException e) {
//                e.printStackTrace();
//                return;
//            }
//        }
//
//        Log.w(Constants.LOG_TAG, "Shutting down main loop !");
//
//        try {
//            mInputStream.close();
//            mOutputStream.close();
//        } catch(IOException e) {
//            // TODO Auto-generated catch block
//            e.printStackTrace();
//        }
//
//        // close serial port
//        if(mSerialPort != null) {
//            mSerialPort.close();
//            mSerialPort = null;
//        }
//
//    }
//
//    private void sendKeyPressedToAllListeners(String pressedKey) {
//
//        if(baseActivity instanceof MainActivity) {
//            if(MainActivity.lastUserInteractionTime > Constants.IDLE_TIME) {
//
//                MainActivity.lastUserInteractionTime = 0;
//
//                MainActivity mainActivity = (MainActivity) baseActivity;
//                mainActivity.setDayDreamScreen(false);
//
//                return;
//            }
//        }
//
//        MainActivity.lastUserInteractionTime = 0;
//
//        if(listeners == null || listeners.size() == 0) {
//            return;
//        }
//
//        Enumeration<String> enumeration = listeners.keys();
//        while(enumeration.hasMoreElements()) {
//            String classNameForListener = enumeration.nextElement();
//
//            KeyPadListener keyPadListener = listeners.get(classNameForListener);
//
//            keyPadListener.onKeyPressed(pressedKey);
//
//        }
//
//    }
//
//}

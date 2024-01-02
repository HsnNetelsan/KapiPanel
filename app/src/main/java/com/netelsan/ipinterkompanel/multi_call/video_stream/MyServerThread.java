package com.netelsan.ipinterkompanel.multi_call.video_stream;

import android.content.Context;
import android.os.Handler;
import android.util.Log;

import androidx.appcompat.app.AppCompatActivity;

import com.netelsan.ipinterkompanel.activity.MainActivity;
import com.netelsan.ipinterkompanel.activity.ScreensaverActivity2;
import com.netelsan.ipinterkompanel.activity.call.MultipleDeviceCallRequestActivity;
import com.netelsan.ipinterkompanel.webrtc.activity.CallActivity;

import java.io.DataOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.net.ServerSocket;
import java.net.Socket;

public class MyServerThread implements Runnable {
    private String mServerIP;
    private Context mContext;
    private Handler mHandler;
    private MultipleDeviceCallRequestActivity multipleDeviceCallRequestActivity;
    private CallActivity callActivity;
    ServerSocket serverSocket;

    public boolean isDestroyed = false;

    public MyServerThread(AppCompatActivity appCompatActivity, ServerSocket serverSocket, String serverip, Handler handler) {
        super();
        mContext = appCompatActivity;
        mServerIP = serverip;
        this.serverSocket = serverSocket;
        mHandler = handler;

        try {
            multipleDeviceCallRequestActivity = (MultipleDeviceCallRequestActivity) appCompatActivity;
        } catch (Exception e) {
            callActivity = (CallActivity) appCompatActivity;
        }

    }

    public void run() {
        try {
//            mHandler.post(new Runnable() {
//                @Override
//                public void run() {
//                    mActivityInstance.serverStatus.setText("Listening on IP: " + mServerIP);
//                }
//            });
            while (true) {
//                Log.d(Constants.LOG_TAG, "1907 MyServerThread run while");

                if (isDestroyed) {
                    break;
                }

                Socket socket = serverSocket.accept();
                //socketList.add(serverSocket);
                Thread thread = new Thread(new ServerSocketThread(socket));
                thread.start();

            }
        } catch (Exception e) {
            Log.d("ServerThread", "run: erro");
        }
    }

    public class ServerSocketThread implements Runnable {
        Socket socket = null;
        // BufferedReader br = null;
        //BufferedWriter bw = null;
        OutputStream os = null;

        public ServerSocketThread(Socket socket) throws IOException {
            this.socket = socket;
            //br = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            //bw = new BufferedWriter(new OutputStreamWriter(socket.getOutputStream()));
        }

        @Override
        public void run() {

            if (socket != null) {
                String clientIp = socket.getInetAddress().toString().replace("/", "");
                int clientPort = socket.getPort();
                System.out.println("====client ip=====" + clientIp);
                System.out.println("====client port=====" + clientPort);
                try {

                    socket.setKeepAlive(true);
                    os = socket.getOutputStream();
                    while (true) {

                        if (isDestroyed) {
                            break;
                        }
//                        Log.d(Constants.LOG_TAG, "1907 ServerSocketThread run while");

                        MainActivity.lastUserInteractionTime = 0; // screensave e girmemesi için gerekli
                        ScreensaverActivity2.lastUserInteractionTime2 = 0;

                        DataOutputStream dos = new DataOutputStream(os);
                        dos.writeInt(4);
                        dos.writeUTF("#@@#");
                        if (multipleDeviceCallRequestActivity != null) {
                            dos.writeInt(multipleDeviceCallRequestActivity.mPreview.mFrameBuffer.size());
                        } else {
                            dos.writeInt(callActivity.mPreview.mFrameBuffer.size());
                        }
                        dos.writeUTF("-@@-");
                        dos.flush();
//                        System.out.println(mActivityInstance.mPreview.mFrameBuffer.size());

                        if (multipleDeviceCallRequestActivity != null) {
                            dos.write(multipleDeviceCallRequestActivity.mPreview.mFrameBuffer.toByteArray());
                        } else {
                            dos.write(callActivity.mPreview.mFrameBuffer.toByteArray());
                        }

                        //System.out.println("outlength"+mPreview.mFrameBuffer.length);
                        dos.flush();
                        Thread.sleep(1000 / 15);
                    }

                } catch (Exception e) {
                    e.printStackTrace();
                    try {
                        if (os != null)
                            os.close();

                    } catch (Exception e2) {
                        e.printStackTrace();
                    }

                }


            } else {
                System.out.println("socket is null");

            }
        }

    }
}
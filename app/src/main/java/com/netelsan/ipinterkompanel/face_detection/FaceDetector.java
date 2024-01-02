package com.netelsan.ipinterkompanel.face_detection;

import android.Manifest;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.pm.PackageManager;
import android.content.res.Configuration;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Matrix;
import android.graphics.Point;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.IBinder;
import android.util.Log;
import android.view.Display;
import android.view.Menu;
import android.view.WindowManager;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.StringRes;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.DialogFragment;

import com.google.android.cameraview.CameraView;
import com.netelsan.ipinterkompanel.Constants;
import com.netelsan.ipinterkompanel.Helper;
import com.netelsan.ipinterkompanel.R;
import com.netelsan.ipinterkompanel.activity.MainActivity;
import com.netelsan.ipinterkompanel.listener.KeyPadListener;
import com.netelsan.ipinterkompanel.service.KeyPadService;

import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.InetAddress;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;
import java.util.regex.Pattern;


public class FaceDetector extends AppCompatActivity implements ActivityCompat.OnRequestPermissionsResultCallback, ServiceConnection, KeyPadListener {


    @Override
    public void onRFIDDetected(String rfid) {

    }

    @Override
    public void onKeyPressed(String keyCode) {
        this.runOnUiThread(new Runnable() {
            @Override
            public void run() {

                //Log.d(com.netelsan.ipinterkompanel.Constants.LOG_TAG, "DaireAraFragment onKeyPressed keyCode=" + keyCode);

                if (keyCode.equals(com.netelsan.ipinterkompanel.Constants.KEYPAD_BACK)) {
                    bottomMenuBackClicked();
                } else if (keyCode.equals(com.netelsan.ipinterkompanel.Constants.KEYPAD_UP)) {

                } else if (keyCode.equals(com.netelsan.ipinterkompanel.Constants.KEYPAD_DOWN)) {

                } else if (keyCode.equals(com.netelsan.ipinterkompanel.Constants.KEYPAD_HOME)) {
                    bottomMenuOkayClicked();
                } else if (keyCode.equals(com.netelsan.ipinterkompanel.Constants.KEYPAD_CALL)) {

                } else if (keyCode.equals(com.netelsan.ipinterkompanel.Constants.KEYPAD_LOCK)) {
//                    bottomLockClicked();
                } else {
                    bottomNumbersClicked(keyCode);
                }

            }
        });
    }

    private void bottomLockClicked() {

        if (mCameraView != null) {
            mCameraView.takePicture(); //
        }
    }

    private void bottomNumbersClicked(String keycode) {
    }

    private void bottomMenuOkayClicked() {
//        setLedState(false);
//        Intent i = new Intent(FaceDetector.this, MainActivity.class);
//        startActivity(i);
        FaceDetector.this.finish();
        //finish();
    }

    private void bottomMenuBackClicked() {
    }

    @Override
    public void onServiceConnected(ComponentName name, IBinder service) {
        keyPadService = ((KeyPadService.MyBinder) service).getService();
        keyPadService.addListener(FaceDetector.this, FaceDetector.class.getName());
    }

    @Override
    public void onServiceDisconnected(ComponentName name) {
        keyPadService = null;
    }

    public KeyPadService keyPadService;
    private static final String TAG = "MainActivity";
    private static final int INPUT_SIZE = 480;

    /*private static final int[] FLASH_OPTIONS = {
            CameraView.FLASH_AUTO,
            CameraView.FLASH_OFF,
            CameraView.FLASH_ON,
    };

    private static final int[] FLASH_ICONS = {
            R.drawable.ic_flash_auto,
            R.drawable.ic_flash_off,
            R.drawable.ic_flash_on,
    };

    private static final int[] FLASH_TITLES = {
            R.string.flash_auto,
            R.string.flash_off,
            R.string.flash_on,
    };

    private int mCurrentFlash;
    */
    private CameraView mCameraView;

    private Handler mBackgroundHandler;

    /*private View.OnClickListener mOnClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            switch (v.getId()) {
                case R.id.take_picture:
                    if (mCameraView != null) {
                        mCameraView.takePicture();
                    }
                    break;
                case R.id.add_person:
                    Intent i = new Intent(main.this, AddPerson.class);
                    startActivity(i);
                    finish();
                    break;
            }
        }
    };*/

    void resumeCreate() {
        setContentView(R.layout.activity_face_datector);

        mCameraView = (CameraView) findViewById(R.id.camera);
        if (mCameraView != null) {
            mCameraView.addCallback(mCallback);
        }

    }


    private void setLedState(boolean isActive) {

        int ledPortNumber;
        /*boolean isDeviceSube = Helper.isDeviceSube(getApplicationContext());
        //Log.d(com.netelsan.ipinterkompanel.Constants.LOG_TAG, "setLedState isDeviceSube=" + isDeviceSube);
        if(isDeviceSube) {
            ledPortNumber = Helper.getPINFromGPIO("PE16");
        } else {*/
        ledPortNumber = Helper.getPINFromGPIO("PE17");
        //}

        Process process = Helper.getProcess();
        Helper.setGPIO(process, com.netelsan.ipinterkompanel.Constants.GPIO_EXPORT, ledPortNumber, true, isActive);
        Helper.setGPIO(process, com.netelsan.ipinterkompanel.Constants.GPIO_DIRECTION, ledPortNumber, true, isActive);
        Helper.setGPIO(process, com.netelsan.ipinterkompanel.Constants.GPIO_VALUE, ledPortNumber, true, isActive);

    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        Log.d(TAG, "onCreate called");
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_face_datector);
        checkPermissions();

        mCameraView = findViewById(R.id.camera);
        mCameraView.setAdjustViewBounds(true);
        mCameraView.setRotation(180f);

        if (mCameraView != null) {
            mCameraView.addCallback(mCallback);
        }

//        Timer timer = new Timer();
//        timer.schedule(new TimerTask() {
//            @Override
//            public void run() {
//                bottomLockClicked();
//            }
//        }, 1000);

    }

//    private FaceRec mFaceRec;// = MainActivity.mFaceRec; //Eski cihazda yüz okuma yaparkenden kalan değişken

    private void changeProgressDialogMessage(final ProgressDialog pd, final String msg) {
        Runnable changeMessage = new Runnable() {
            @Override
            public void run() {
                pd.setMessage(msg);
            }
        };
        runOnUiThread(changeMessage);
    }


    String[] permissions = new String[]{
            Manifest.permission.CAMERA,
            Manifest.permission.WRITE_EXTERNAL_STORAGE,
    };

    private boolean checkPermissions() {
        int result;
        List<String> listPermissionsNeeded = new ArrayList<>();
        for (String p : permissions) {
            result = ContextCompat.checkSelfPermission(this, p);
            if (result != PackageManager.PERMISSION_GRANTED) {
                listPermissionsNeeded.add(p);
            }
        }
        if (!listPermissionsNeeded.isEmpty()) {
            ActivityCompat.requestPermissions(this, listPermissionsNeeded.toArray(new String[listPermissionsNeeded.size()]), 100);
            return false;
        }
        return true;
    }


    @Override
    protected void onResume() {
        Log.d(TAG, "onResume called");
        super.onResume();

        Intent serviceConnIntent = new Intent(this, KeyPadService.class);
        this.bindService(serviceConnIntent, this, Context.BIND_AUTO_CREATE);

        if (keyPadService != null) {
            keyPadService.addListener(FaceDetector.this, FaceDetector.class.getName());
        }

        if (mCameraView != null) {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA)
                    == PackageManager.PERMISSION_GRANTED) {
                Display getOrient = ((WindowManager) getSystemService(Context.WINDOW_SERVICE)).getDefaultDisplay();

                Point point = new Point();
                getOrient.getSize(point);
                Log.d(TAG, String.format("screen size (%d,%d)", point.x, point.y));
                mCameraView.start();
            }
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE)
                    == PackageManager.PERMISSION_GRANTED) {
                //new initRecAsync().execute();
            }
        } else {
            resumeCreate();
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA)
                    == PackageManager.PERMISSION_GRANTED) {
                mCameraView.start();
            }
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE)
                    == PackageManager.PERMISSION_GRANTED) {
                //new initRecAsync().execute();
            }
        }
    }

    @Override
    protected void onPause() {
        Log.d(TAG, "onPause called");
        mCameraView.stop();
        if (keyPadService != null) {
            keyPadService.removeListener(FaceDetector.class.getName());
        }
        super.onPause();
    }

    @Override
    protected void onDestroy() {
        Log.d(TAG, "onDestroy called");
        super.onDestroy();

        setLedState(false);
        if (keyPadService != null) {
            keyPadService.removeListener(FaceDetector.class.getName());
            unbindService(this);
        }
//        if (mFaceRec != null) {
//            mFaceRec.release();
//        }
        if (mBackgroundHandler != null) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR2) {
                mBackgroundHandler.getLooper().quitSafely();
            } else {
                mBackgroundHandler.getLooper().quit();
            }
            mBackgroundHandler = null;
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        if (requestCode == 100) {
            if (grantResults.length > 0
                    && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                // do something
            }
            return;
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
//        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }


    private Handler getBackgroundHandler() {
        if (mBackgroundHandler == null) {
            HandlerThread thread = new HandlerThread("background");
            thread.start();
            mBackgroundHandler = new Handler(thread.getLooper());
        }
        return mBackgroundHandler;
    }

    private String getResultMessage(ArrayList<String> names) {
        String msg = new String();
        if (names.isEmpty()) {
            msg = "Tanınan Bir Yüz Bulunamadı";

        } else {
            for (int i = 0; i < names.size(); i++) {
                msg += names.get(i).split(Pattern.quote("."))[0];
                if (i != names.size() - 1) msg += ", ";
            }
            msg = "Tanınan Yüz Bulundu \n " + msg;
            Log.d("face","names: "+ names);
//            keyPadService.sendDoorUnlock(true);
            keyPadService.sendDoorUnlock(Constants.ENUM_DOOR_UNLOCK_MAIN);
        }
        return msg;
    }

    void SendCropperBitmap() {

        Thread thread = new Thread(new Runnable() {

            @Override
            public void run() {

            }
        });

        thread.start();


        ///BEGIN

        //long endTime = System.currentTimeMillis();
        //Log.d(TAG, "Time cost: " + String.valueOf((endTime - startTime) / 1000f) + " sec");


        ///END
    }

    private class recognizeAsync extends AsyncTask<Bitmap, Void, ArrayList<String>> { //yüz tanıma için tcp üzerinden sunucuya gönderme
        ProgressDialog dialog = new ProgressDialog(FaceDetector.this);
        private int mScreenRotation = 1;
        private boolean mIsComputing = false;
        private Bitmap mCroppedBitmap = Bitmap.createBitmap(INPUT_SIZE, INPUT_SIZE, Bitmap.Config.ARGB_8888);

        @Override
        protected void onPreExecute() {
           /* dialog.setMessage("Yüzler taranıyor... Lütfen Sabit Durun");
            dialog.setCancelable(false);
            dialog.show();*/
            super.onPreExecute();
        }

        String getName(byte[] input) {
            byte temp[] = new byte[input.length - 1];
            System.arraycopy(input, 1, temp, input.length, input.length - 1);
            return new String(temp);
        }

        int getApart(byte[] input) {
            int D = 0;
            for (int i = 1; i < 6; i++) {
                Log.d("input","input: " + input[i]);
                if (input[i] > 47) {
                    Log.d("input","input -48: " + input[i]);
                    D *= 10;
                    D += input[i] - 48;
                } else
                    break;
            }
            Log.d("input","input: " + D);
            return D;
        }

        protected ArrayList<String> doInBackground(Bitmap... bp) {

            drawResizedBitmap(bp[0], mCroppedBitmap);

            //Log.d(TAG, "byte to bitmap");


            Socket socket = null;
            Log.d(TAG, "SocketPrep");
            final int SERVERPORT = Constants.IP_FOR_FACE_DETECTOR_PORT_DETECTION;
            final String SERVER_IP = Constants.IP_FOR_FACE_DETECTOR;

            ArrayList<String> names = new ArrayList<>();
            try {
                InetAddress serverAddr = InetAddress.getByName(SERVER_IP);
                socket = new Socket(serverAddr, SERVERPORT);


                Bitmap bmp = mCroppedBitmap; //String str = et.getText().toString();
                byte[] inArray = new byte[10];

                InputStream in = socket.getInputStream();
                DataInputStream din = new DataInputStream(in);
                din.read(inArray);
                //Log.d(TAG,"Inarray :"+ Arrays.toString(inArray));
                ByteArrayOutputStream bos = new ByteArrayOutputStream();
                bmp.compress(Bitmap.CompressFormat.JPEG, 50, bos);
                byte[] array = bos.toByteArray();
                inArray = new byte[30];
                OutputStream out = socket.getOutputStream();
                DataOutputStream dos = new DataOutputStream(out);
                //dos.writeInt(array.length);
                dos.write(array, 0, array.length);
                din.read(inArray);
                Log.d(TAG,"Inarray :"+inArray[0]+".."+inArray[1]+".."+inArray[2]);
                String response = new String(inArray, "UTF-8");

                int D = getApart(inArray);
                //String name = getName(inArray);
                if (inArray[0] == 84) {
                    //names.add(name);
                    names.add("Daire " + D);
                }

                /*if(inArray[0]==(byte)0x54)
                    dialog.setMessage("Yüz Tanıma Başarılı.");
                else
                    dialog.setMessage("Yetkili Yüz Bulunamadı.");
                dialog.setCancelable(false);
                dialog.show();*/
                //result = in.readLine();
            } catch (Exception e) {
                e.printStackTrace();

            }
            Log.d(TAG, "SocketSent");

//            try {
//                socket.close();
//            } catch (Exception ex) {
//            }

            //long startTime = System.currentTimeMillis();
//            List<VisionDetRet> results;
            //results = mFaceRec.recognize(mCroppedBitmap);


            /*for(VisionDetRet n:results) {
                names.add(n.getLabel());
            }*/
            return names;
        }

        protected void onPostExecute(ArrayList<String> names) {
            AlertDialog.Builder builder1 = new AlertDialog.Builder(FaceDetector.this);
            builder1.setMessage(getResultMessage(names));
            builder1.setCancelable(true);
            AlertDialog alertDialog = builder1.create();
            alertDialog.show();

            Timer timer = new Timer();
            timer.schedule(new TimerTask() {
                @Override
                public void run() {
                    alertDialog.dismiss();
                    bottomMenuOkayClicked();
                    //feyhsn 3000
                }
            }, 1000);

          /*  if(dialog != null && dialog.isShowing()){
                dialog.dismiss();
                AlertDialog.Builder builder1 = new AlertDialog.Builder(main.this);
                builder1.setMessage(getResultMessage(names));
                builder1.setCancelable(true);
                AlertDialog alert11 = builder1.create();
                alert11.show();
            }*/
        }

        private void drawResizedBitmap(final Bitmap src, final Bitmap dst) {
            Display getOrient = ((WindowManager) getSystemService(Context.WINDOW_SERVICE)).getDefaultDisplay();
            int orientation = Configuration.ORIENTATION_UNDEFINED;
            Point point = new Point();
            getOrient.getSize(point);
            int screen_width = point.x;
            int screen_height = point.y;
            //Log.d(TAG, String.format("screen size (%d,%d)", screen_width, screen_height));
            if (screen_width < screen_height) {
                orientation = Configuration.ORIENTATION_PORTRAIT;
                mScreenRotation = 0;
            } else {
                orientation = Configuration.ORIENTATION_LANDSCAPE;
                mScreenRotation = 1;
            }

            //Assert.assertEquals(dst.getWidth(), dst.getHeight());
            final float minDim = Math.min(src.getWidth(), src.getHeight());

            final Matrix matrix = new Matrix();

            //matrix.preScale(-1.0f,1.0f);
            // We only want the center square out of the original rectangle.
            final float translateX = -Math.max(0, (src.getWidth() - minDim) / 2);
            final float translateY = -Math.max(0, (src.getHeight() - minDim) / 2);
            matrix.preTranslate(translateX, translateY);

            final float scaleFactor = dst.getHeight() / minDim;
            matrix.postScale(scaleFactor, scaleFactor);

            // Rotate around the center if necessary.
            /*if (mScreenRotation != 0) {
                matrix.postTranslate(-dst.getWidth() / 2.0f, -dst.getHeight() / 2.0f);
                matrix.postRotate(0);
                matrix.postTranslate(dst.getWidth() / 2.0f, dst.getHeight() / 2.0f);
            }*/
            //matrix.postRotate(180,dst.getWidth() / 2.0f, dst.getHeight() / 2.0f);
            final Canvas canvas = new Canvas(dst);
            canvas.drawBitmap(src, matrix, null);
        }
    }


    private CameraView.Callback mCallback = new CameraView.Callback() {

        @Override
        public void onCameraOpened(CameraView cameraView) {
            Log.d(TAG, "onCameraOpened");

            setLedState(true);

            new Handler().postDelayed(new Runnable() {
                @Override
                public void run() {
                    bottomLockClicked();
                }
            }, 1500);
        }

        @Override
        public void onCameraClosed(CameraView cameraView) {
            Log.d(TAG, "onCameraClosed");
        }

        @Override
        public void onPictureTaken(CameraView cameraView, final byte[] data) {
            Log.d(TAG, "onPictureTaken " + data.length);
            //feyhsn
            /*Toast.makeText(cameraView.getContext(), data.length, Toast.LENGTH_SHORT)
                    .show();*/
            Bitmap bp = BitmapFactory.decodeByteArray(data, 0, data.length);
            new recognizeAsync().execute(bp);

        }

    };

    public static class ConfirmationDialogFragment extends DialogFragment {

        private static final String ARG_MESSAGE = "message";
        private static final String ARG_PERMISSIONS = "permissions";
        private static final String ARG_REQUEST_CODE = "request_code";
        private static final String ARG_NOT_GRANTED_MESSAGE = "not_granted_message";

        public static ConfirmationDialogFragment newInstance(@StringRes int message,
                                                             String[] permissions, int requestCode, @StringRes int notGrantedMessage) {
            ConfirmationDialogFragment fragment = new ConfirmationDialogFragment();
            Bundle args = new Bundle();
            args.putInt(ARG_MESSAGE, message);
            args.putStringArray(ARG_PERMISSIONS, permissions);
            args.putInt(ARG_REQUEST_CODE, requestCode);
            args.putInt(ARG_NOT_GRANTED_MESSAGE, notGrantedMessage);
            fragment.setArguments(args);
            return fragment;
        }

        @NonNull
        @Override
        public Dialog onCreateDialog(Bundle savedInstanceState) {
            final Bundle args = getArguments();
            return new AlertDialog.Builder(getActivity())
                    .setMessage(args.getInt(ARG_MESSAGE))
                    .setPositiveButton(android.R.string.ok,
                            new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    String[] permissions = args.getStringArray(ARG_PERMISSIONS);
                                    if (permissions == null) {
                                        throw new IllegalArgumentException();
                                    }
                                    ActivityCompat.requestPermissions(getActivity(),
                                            permissions, args.getInt(ARG_REQUEST_CODE));
                                }
                            })
                    .setNegativeButton(android.R.string.cancel,
                            new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    Toast.makeText(getActivity(),
                                            args.getInt(ARG_NOT_GRANTED_MESSAGE),
                                            Toast.LENGTH_SHORT).show();
                                }
                            })
                    .create();
        }

    }

}
//package com.netelsan.ipinterkompanel.dialog_activity;
//
//import android.content.BroadcastReceiver;
//import android.content.ComponentName;
//import android.content.Context;
//import android.content.Intent;
//import android.content.IntentFilter;
//import android.content.ServiceConnection;
//import android.graphics.Bitmap;
//import android.graphics.Point;
//import android.os.Bundle;
//import android.os.IBinder;
//import android.util.Log;
//import android.view.Display;
//import android.view.Window;
//import android.widget.TextView;
//
//import androidx.annotation.Nullable;
//
//import com.netelsan.ipinterkompanel.Constants;
//import com.netelsan.ipinterkompanel.Helper;
//import com.netelsan.ipinterkompanel.R;
//import com.netelsan.ipinterkompanel.listener.KeyPadListener;
//import com.netelsan.ipinterkompanel.model.Daire;
//import com.netelsan.ipinterkompanel.service.CapturePhotoService;
//import com.netelsan.ipinterkompanel.service.KeyPadService;
//
//import java.text.SimpleDateFormat;
//import java.util.ArrayList;
//import java.util.Calendar;
//import java.util.Timer;
//import java.util.TimerTask;
//
//public class CountDownForCallActivityOLD extends BaseDialogActivity implements ServiceConnection, KeyPadListener {
//
//    TextView countdownDialogDaireName;
//    TextView countdownDialogRemainingTime;
//
//    Daire selectedDaire;
//
//    int timeRemaining = 4;
//    Timer timerCountDown;
//
////    CameraKitView cameraKitView;
//
//    @Override
//    protected void onCreate(@Nullable Bundle savedInstanceState) {
//        super.onCreate(savedInstanceState);
//
//        requestWindowFeature(Window.FEATURE_NO_TITLE);
//
//        setContentView(R.layout.activity_count_down_for_call);
//
//        countdownDialogDaireName = findViewById(R.id.countdownDialogDaireName);
//        countdownDialogRemainingTime = findViewById(R.id.countdownDialogRemainingTime);
//
////        cameraKitView = findViewById(R.id.cameraKitView);
//
//        setFinishOnTouchOutside(false);
//
//        Display display = getWindowManager().getDefaultDisplay();
//        Point size = new Point();
//        display.getSize(size);
//        int width = size.x;
//        int height = size.y;
//
//        getWindow().setLayout(((int) (width * Constants.CUSTOM_DIALOG_WIDTH)), ((int) (height * Constants.CUSTOM_DIALOG_HEIGHT)));
//
//        Intent serviceConnIntent = new Intent(this, KeyPadService.class);
//        bindService(serviceConnIntent, this, Context.BIND_AUTO_CREATE);
//
//        processIntent(getIntent());
//
//
//        IntentFilter filterPhotoCapture = new IntentFilter();
//        filterPhotoCapture.addAction("com.netelsan.PHOTO_CAPTURE");
//        registerReceiver(photoCaptureBroadcastReceiver, filterPhotoCapture);
//
//        Intent front_translucent = new Intent(CountDownForCallActivityOLD.this, CapturePhotoService.class);
//        front_translucent.putExtra("object", selectedDaire);
//        startService(front_translucent);
//
//    }
//
//    @Override
//    protected void onStart() {
//        super.onStart();
////        try {
////            cameraKitView.onStart();
////        } catch(Exception e) {
////            sendBroadcastForCountDown(false);
////            e.printStackTrace();
////        }
//    }
//
//    @Override
//    public void onResume() {
//        super.onResume();
////        try {
////            cameraKitView.onResume();
////        } catch(Exception e) {
////            sendBroadcastForCountDown(false);
////            e.printStackTrace();
////        }
//    }
//
//    @Override
//    public void onPause() {
////        try {
////            cameraKitView.onPause();
////        } catch(Exception e) {
////            sendBroadcastForCountDown(false);
////            e.printStackTrace();
////        }
//        super.onPause();
//    }
//
//    @Override
//    protected void onStop() {
////        try {
////            cameraKitView.onStop();
////        } catch(Exception e) {
////            sendBroadcastForCountDown(false);
////            e.printStackTrace();
////        }
//        super.onStop();
//    }
//
////    @Override
////    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
////        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
////        cameraKitView.onRequestPermissionsResult(requestCode, permissions, grantResults);
////    }
//
//    KeyPadService keyPadService;
//
//    @Override
//    public void onServiceConnected(ComponentName componentName, IBinder binder) {
//        String className = componentName.getClassName();
//
//        if(className.equals(KeyPadService.class.getName())) {
//            keyPadService = ((KeyPadService.MyBinder) binder).getService();
//            keyPadService.addListener(CountDownForCallActivityOLD.this, CountDownForCallActivityOLD.class.getName());
//        }
//
//    }
//
//    @Override
//    public void onServiceDisconnected(ComponentName name) {
//        keyPadService = null;
//    }
//
//    @Override
//    public void onKeyPressed(String keyCode) {
//
//        Log.d(Constants.LOG_TAG, "CountDownForCallActivity onKeyPressed keyCode=" + keyCode);
//
//        if(keyCode.equals(Constants.KEYPAD_BACK)) {
//            sendBroadcastForCountDown(false);
//        }
//    }
//
//    @Override
//    public void onRFIDDetected(String rfid) {
//
//    }
//
//    private void processIntent(Intent intent) {
//        if(intent == null) {
//            return;
//        }
//        Bundle bundle = intent.getExtras();
//        if(bundle == null) {
//            return;
//        }
//
//        selectedDaire = (Daire) bundle.getSerializable("object");
//
//        timeRemaining = 4;
//
//        startCountDownWhenCameraOpened();
//
//        countdownDialogDaireName.setText(selectedDaire.getIsim() + " " + selectedDaire.getSoyisim());
//        countdownDialogRemainingTime.setText("4 ");
//
//    }
//
//    private void startCountDownWhenCameraOpened() {
//
////        cameraKitView.setErrorListener(new CameraKitView.ErrorListener() {
////            @Override
////            public void onError(CameraKitView cameraKitView, CameraKitView.CameraException e) {
////                sendBroadcastForCountDown(false);
////            }
////        });
//
////        cameraKitView.setCameraListener(new CameraKitView.CameraListener() {
////            @Override
////            public void onOpened() {
////
////                new android.os.Handler().postDelayed(new Runnable() {
////                    @Override
////                    public void run() {
////                        setLayouts();
////                    }
////                }, 500L);
////
////            }
////
////            @Override
////            public void onClosed() {
////                sendBroadcastForCountDown(false);
////            }
////        });
//
//    }
//
//    private void setLayouts() {
//
//        timerCountDown = new Timer();
//        timerCountDown.schedule(new TimerTask() {
//            @Override
//            public void run() {
//
//                if(timeRemaining == 0) {
//                    timerCountDown.cancel();
//                    timerCountDown.purge();
//                    timerCountDown = null;
//
//                    sendBroadcastForCountDown(true);
//
//                    return;
//                }
//
//                timeRemaining = timeRemaining - 1;
//
//                runOnUiThread(new Runnable() {
//                    @Override
//                    public void run() {
//                        countdownDialogRemainingTime.setText(timeRemaining + " ");
//
////                        Log.d(Constants.LOG_TAG, "snapshot proccessing timeRemaining=" + timeRemaining);
//                        if(timeRemaining <= 2 && timeRemaining > 0) {
//                            getSnapshotFromCamera();
//                        }
//
//                    }
//                });
//
//            }
//        }, 0, 1000);
//
//    }
//
//    ArrayList<String> bitmapFilePaths = new ArrayList<>();
//
//    private void getSnapshotFromCamera() {
//
////        cameraKitView.captureImage(new CameraKitView.ImageCallback() {
////            @Override
////            public void onImage(CameraKitView view, final byte[] photo) {
////                new Thread(new Runnable() {
////                    @Override
////                    public void run() {
////
//////                        Log.d(Constants.LOG_TAG, "snapshot proccessing");
////                        //burada resmi kaydet. daire klasörü ile birlikte
////                        try {
//////                            Log.d(Constants.LOG_TAG, "snapshot proccessing size=" + photo.length);
////                            File saveFolderFile = Helper.createDaireCallImageFolderIfNeeded(selectedDaire);
//////                            Log.d(Constants.LOG_TAG, "snapshot proccessing path=" + saveFolderFile.getAbsolutePath());
////
////                            String bitmapFileName = generateImageId();
////                            File bitmapfile = new File(saveFolderFile.getAbsoluteFile(), bitmapFileName + ".jpg");
////                            bitmapFilePaths.add(bitmapfile.getAbsolutePath());
////
////                            FileOutputStream fileOutputStream = new FileOutputStream(bitmapfile);
////                            Bitmap bitmap = BitmapFactory.decodeByteArray(photo, 0, photo.length);
////                            Bitmap bitmapReduced = getResizedBitmap(bitmap, bitmap.getWidth() / 5, bitmap.getHeight() / 5);
////
////                            Log.d(Constants.LOG_TAG, "getSnapshotFromCamera bitmap width=" + bitmapReduced.getWidth() + " height=" + bitmapReduced.getHeight());
////                            bitmapReduced.compress(Bitmap.CompressFormat.JPEG, 80, fileOutputStream);
////                        } catch(Exception e) {
////                            e.printStackTrace();
////                        }
////
//////                        imageViewTemp.setImageBitmap(bitmap);
////
////                    }
////                }).start();
////            }
////        });
//
//    }
//
//    private String generateImageId() {
//
////        SecureRandom random = new SecureRandom();
//
////        String generatedImageId = new BigInteger(130, random).toString(32);
//
//        String generatedImageId = getDateSuffix();
////        generatedImageId = generatedImageId + "_" + getDateSuffix();
//        Log.d(Constants.LOG_TAG, "generatedImageId=" + generatedImageId);
//        return generatedImageId;
//
//    }
//
//    public Bitmap getResizedBitmap(Bitmap image, int bitmapWidth, int bitmapHeight) {
//        return Bitmap.createScaledBitmap(image, bitmapWidth, bitmapHeight, true);
//    }
//
//    private String getDateSuffix() {
//
//        SimpleDateFormat dateFormat = new SimpleDateFormat("HHmmss_ddMMyyyy");
//        String formattedDate = dateFormat.format(Calendar.getInstance().getTime());
//
//        return formattedDate;
//    }
//
//    private void sendBroadcastForCountDown(boolean isPositive) {
//
//        Intent intent = new Intent("com.netelsan.COUNT_DOWN");
//        intent.putExtra("isPositive", isPositive);
//        if(isPositive) {
//            if(bitmapFilePaths == null) {
//                bitmapFilePaths = new ArrayList<>();
//            }
//
//            intent.putExtra("bitmapFiles", bitmapFilePaths);
//        }
//        sendBroadcast(intent);
//
//        CountDownForCallActivityOLD.this.finish();
//    }
//
//    @Override
//    protected void onDestroy() {
//
//        try {
//            Helper.runRestartCameraShellCommand();
//        } catch(Throwable e) {
//            e.printStackTrace();
//        }
//
//        if(timerCountDown != null) {
//            timerCountDown.purge();
//            timerCountDown.cancel();
//            timerCountDown = null;
//        }
//
//        if(keyPadService != null) {
//            keyPadService.removeListener(CountDownForCallActivityOLD.class.getName());
//        }
//
//        unbindService(this);
//
//        unregisterReceiver(photoCaptureBroadcastReceiver);
//
//        super.onDestroy();
//
//    }
//
//    BroadcastReceiver photoCaptureBroadcastReceiver = new BroadcastReceiver() {
//        @Override
//        public void onReceive(Context context, Intent intent) {
//
//            String bitmapPath = intent.getStringExtra("bitmapPath");
//            Log.d(Constants.LOG_TAG, "photoCaptured bitmapPath=" + bitmapPath);
//            bitmapFilePaths.add(bitmapPath);
//        }
//
//    };
//
//}

package com.netelsan.ipinterkompanel.service;

import android.app.Service;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.graphics.PixelFormat;
import android.hardware.Camera;
import android.media.MediaScannerConnection;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Environment;
import android.os.Handler;
import android.os.IBinder;
import android.util.Log;
import android.view.Gravity;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.WindowManager;

import com.netelsan.ipinterkompanel.BuildConfig;
import com.netelsan.ipinterkompanel.Constants;
import com.netelsan.ipinterkompanel.Helper;
import com.netelsan.ipinterkompanel.error_handling.ErrorHandlingUtils;
import com.netelsan.ipinterkompanel.model.Daire;
import com.netelsan.ipinterkompanel.model.Guvenlik;
import com.netelsan.ipinterkompanel.tcp.Utils;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.io.Writer;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Timer;
import java.util.TimerTask;

public class CapturePhotoService extends Service implements SurfaceHolder.Callback {

    private Camera mCamera;
    private Camera.Parameters parameters;
    private Bitmap bmp;
    FileOutputStream fileOutputStream;
    private Camera.Size pictureSize;
    SurfaceView surfaceView;
    private SurfaceHolder surfaceHolder;
    private WindowManager windowManager;
    WindowManager.LayoutParams params;

    boolean isDeviceSube = true;
    Daire selectedDaire;
    Guvenlik selectedGuvenlik;

    @Override
    public void onCreate() {
        super.onCreate();
    }

    private void setBesttPictureResolution() {

        pictureSize = getBiggesttPictureSize(parameters);
        parameters.setPictureSize(pictureSize.width, pictureSize.height);

        int width = pictureSize.width / 3;
        int height = pictureSize.height / 3;

        parameters.setPictureSize(width, height);

    }

    private Camera.Size getBiggesttPictureSize(Camera.Parameters parameters) {
        Camera.Size result = null;

        for (Camera.Size size : parameters.getSupportedPictureSizes()) {
            if (result == null) {
                result = size;
            } else {
                int resultArea = result.width * result.height;
                int newArea = size.width * size.height;

                if (newArea > resultArea) {
                    result = size;
                }
            }
        }

        return (result);
    }

    Handler handler = new Handler();

    private class TakeImageTask extends AsyncTask<Intent, Void, Void> {

        @Override
        protected Void doInBackground(Intent... params) {
            try {
//                Log.d(Constants.LOG_TAG, "1907 CapturePhotoService TakeImageTask");
                takeImage();
            } catch (Exception e) {
                e.printStackTrace();
            }
            return null;
        }

        @Override
        protected void onPostExecute(Void result) {
        }

    }

    int tryCount = 0;

    private synchronized void takeImage() {

//        Log.d(Constants.LOG_TAG, "1907 takeImage mCamera=" + mCamera);
        if (mCamera != null) {

            try {
                mCamera.stopPreview();
                mCamera.release();
//                belki burada ufak bir bekleme koyulursa sorun çözülür
                mCamera = Camera.open();
            } catch (Exception exception) {
                exception.printStackTrace();

                Throwable throwable = exception.getCause();
                File fileErrorLogs;
                if (throwable == null) {
                    fileErrorLogs = ErrorHandlingUtils.getErrorLogsTextFile(true);
                } else {
                    fileErrorLogs = ErrorHandlingUtils.getErrorLogsTextFile(false);
                }

                String currentErrorLogs = getErrorLogs(throwable);
                ErrorHandlingUtils.writeToFile(currentErrorLogs, fileErrorLogs);

                stopSelf();

            }

        } else {
            mCamera = getCameraInstance();
        }

//        Log.d(Constants.LOG_TAG, "1907 takeImage tryCount=" + tryCount);
        if (tryCount == 2) {
            stopSelf();
            return;
        }

        if (mCamera == null) {
            takeImage();
        } else {
            Timer timer = new Timer();
            timer.schedule(new TimerTask() {
                @Override
                public void run() {
                    takeImageSecondStep();
                }
            }, 200);

        }

        tryCount++;


    }

    private void takeImageSecondStep() {

//        Log.d(Constants.LOG_TAG, "1907 takeImageSecondStep mCamera=" + mCamera);
        try {
            if (mCamera != null) {
                mCamera.setPreviewDisplay(surfaceView.getHolder());
                parameters = mCamera.getParameters();

                parameters.setFlashMode("off");
                // set biggest picture
                setBesttPictureResolution();
                // log quality and image format
                Log.d(Constants.LOG_TAG, parameters.getJpegQuality() + "");
                Log.d(Constants.LOG_TAG, parameters.getPictureFormat() + "");

                // set camera parameters
                mCamera.setParameters(parameters);
                mCamera.startPreview();
                Log.d(Constants.LOG_TAG, "OnTake()");
                mCamera.takePicture(null, null, mCall);
            } else {
                handler.post(new Runnable() {
                    @Override
                    public void run() {
                        Log.e(Constants.LOG_TAG, "Camera is unavailable ");
                    }
                });

            }

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private String getErrorLogs(Throwable throwable) {

        String errorLog = "\n\n" + "******************** " +
                Helper.getCurrentDayOfMonth() + "." + Helper.getCurrentMonthName() + "." + Helper.getCurrentYear() + "   " + Helper.getCurrentHour() + ":" + Helper.getCurrentMinute() +
                " ********************" + "\n";

        int versionCode = BuildConfig.VERSION_CODE;
        String versionName = BuildConfig.VERSION_NAME;

        errorLog = errorLog + "App Version -------> " + versionCode + "\n";
        errorLog = errorLog + "App Version Name --> " + versionName + "\n";
        errorLog = errorLog + "Device IP ---------> " + Utils.getIPAddress(true) + "\n\n";

        if (throwable != null) {

            Writer writer = new StringWriter();
            throwable.printStackTrace(new PrintWriter(writer));
            String errorLogStack = writer.toString();

            errorLog = errorLog + errorLogStack + "\n";

        } else {
            errorLog = errorLog + " onLowMemory\n";
        }

        errorLog = errorLog + "******************** END ********************\n\n";

        return errorLog;
    }


    @SuppressWarnings("deprecation")
    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {

        try {
            selectedDaire = (Daire) intent.getSerializableExtra("object");
            isDeviceSube = true;
        } catch (Exception e) {
            selectedGuvenlik = (Guvenlik) intent.getSerializableExtra("object");
            isDeviceSube = false;
        }

        windowManager = (WindowManager) getSystemService(WINDOW_SERVICE);

        params = new WindowManager.LayoutParams(
                WindowManager.LayoutParams.WRAP_CONTENT,
                WindowManager.LayoutParams.WRAP_CONTENT,
                WindowManager.LayoutParams.TYPE_PHONE,
                WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE,
                PixelFormat.TRANSLUCENT);

        params.gravity = Gravity.TOP | Gravity.LEFT;
        params.width = 1;
        params.height = 1;
        params.x = 0;
        params.y = 0;
        surfaceView = new SurfaceView(getApplicationContext());

        windowManager.addView(surfaceView, params);
        surfaceHolder = surfaceView.getHolder();
        surfaceHolder.setType(SurfaceHolder.SURFACE_TYPE_PUSH_BUFFERS);
        surfaceHolder.addCallback(this);

        return START_NOT_STICKY;
    }

    Camera.PictureCallback mCall = new Camera.PictureCallback() {

        @Override
        public void onPictureTaken(byte[] data, Camera camera) {
            // decode the data obtained by the camera into a Bitmap
            Log.d(Constants.LOG_TAG, "Done");
            if (bmp != null) {
                bmp.recycle();
            }
            System.gc();
            bmp = decodeBitmap(data);

            Matrix matrix = new Matrix();
//            matrix.postRotate(180);
            bmp = Bitmap.createBitmap(bmp, 0, 0, bmp.getWidth(), bmp.getHeight(), matrix, true);
            ByteArrayOutputStream bytes = new ByteArrayOutputStream();
            bmp.compress(Bitmap.CompressFormat.JPEG, 60, bytes);

            File imagesFolder;
            if (isDeviceSube) {

                imagesFolder = Helper.createDaireCallImageFolderIfNeeded(selectedDaire);
            } else {
                imagesFolder = Helper.createGuvenlikCallImageFolderIfNeeded(getApplicationContext(), selectedGuvenlik);
            }

            String bitmapFileName = generateImageId();
            File image = new File(imagesFolder.getAbsoluteFile(), bitmapFileName + ".jpg");

            Log.d(Constants.LOG_TAG, "onPictureTaken imageFilePath=" + image.getAbsolutePath());

            try {
                fileOutputStream = new FileOutputStream(image);
            } catch (FileNotFoundException e) {
                e.printStackTrace();
            }
            try {
                fileOutputStream.write(bytes.toByteArray());
            } catch (IOException e) {
                e.printStackTrace();
            }

//            kaydettikten 1 sn sonra gönder
            broadcastRecordedImageIn1Second(image.getAbsolutePath());


            try {
                fileOutputStream.close();

                MediaScannerConnection.scanFile(getApplicationContext(), new String[]{image.toString()}, null, new MediaScannerConnection.OnScanCompletedListener() {
                    public void onScanCompleted(String path, Uri uri) {
                        Log.i(Constants.LOG_TAG, "Scanned " + path + ":");
                        Log.i(Constants.LOG_TAG, "-> uri=" + uri);
                    }
                });

            } catch (IOException e) {
                e.printStackTrace();
            }

            try {

                if (mCamera != null) {
                    mCamera.stopPreview();
                    mCamera.release();
                    mCamera = null;
                }

                if (bmp != null) {
                    bmp.recycle();
                    bmp = null;
                    System.gc();
                }

            } catch (Exception e) {
                e.printStackTrace();
            }

            mCamera = null;

            if (isNeedStopSelf) {

                stopSelf();
            }

            String root = Environment.getExternalStorageDirectory().toString();
            File myDir = new File(root + "/call_images/");
            try {
                myDir.delete();
                Log.d("HK", "HK deleted call_images");
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    };

    synchronized private void broadcastRecordedImageIn1Second(String imagePath) {

        Intent intent = new Intent("com.netelsan.PHOTO_CAPTURE");
        intent.putExtra("bitmapPath", imagePath);
        sendBroadcast(intent);
    }

    private String generateImageId() {
        String generatedImageId = getDateSuffix();
        return generatedImageId;
    }

    private String getDateSuffix() {
        SimpleDateFormat dateFormat = new SimpleDateFormat("HHmmss_ddMMyyyy");
        String formattedDate = dateFormat.format(Calendar.getInstance().getTime());
        return formattedDate;
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    public static Camera getCameraInstance() {
        Camera camera = null;
        try {
            camera = Camera.open();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return camera;
    }

    @Override
    public void onDestroy() {
        try {
            if (mCamera != null) {
                mCamera.stopPreview();
                mCamera.release();
                mCamera = null;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        if (surfaceView != null) {
            windowManager.removeView(surfaceView);
        }

        super.onDestroy();
    }

    @Override
    public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {

    }

    boolean isNeedStopSelf;

    @Override
    public void surfaceCreated(SurfaceHolder holder) {

        tryCount = 0;

        isNeedStopSelf = false;
        new TakeImageTask().executeOnExecutor(Constants.DEFAULT_EXECUTOR);

//        Timer timer = new Timer();
//        timer.schedule(new TimerTask() {
//            @Override
//            public void run() {
//                isNeedStopSelf = true;
//                new TakeImageTask().executeOnExecutor(Constants.DEFAULT_EXECUTOR);
//            }
//        }, 750);

    }

    @Override
    public void surfaceDestroyed(SurfaceHolder holder) {
        if (mCamera != null) {
            mCamera.stopPreview();
            mCamera.release();
            mCamera = null;
        }
    }

    public static Bitmap decodeBitmap(byte[] data) {

        Bitmap bitmap = null;
        BitmapFactory.Options bfOptions = new BitmapFactory.Options();
        bfOptions.inDither = false;
        bfOptions.inPurgeable = true;
        bfOptions.inInputShareable = true;
        bfOptions.inTempStorage = new byte[32 * 1024];

        if (data != null) {
            bitmap = BitmapFactory.decodeByteArray(data, 0, data.length, bfOptions);
        }

        return bitmap;
    }

}
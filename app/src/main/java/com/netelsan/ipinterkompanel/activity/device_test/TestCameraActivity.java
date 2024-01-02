package com.netelsan.ipinterkompanel.activity.device_test;


import android.graphics.ImageFormat;
import android.graphics.SurfaceTexture;
import android.hardware.Camera;
import android.os.Bundle;
import android.view.TextureView;
import android.view.View;
import android.widget.LinearLayout;

import com.netelsan.ipinterkompanel.Constants;
import com.netelsan.ipinterkompanel.Helper;
import com.netelsan.ipinterkompanel.R;
import com.netelsan.ipinterkompanel.model.DeviceTest;

import java.io.IOException;

public class TestCameraActivity extends BaseTestActivity implements View.OnClickListener, TextureView.SurfaceTextureListener {

    TextureView kameraTestiKamera;

    LinearLayout kameraTestiNegative;
    LinearLayout kameraTestiPositive;

    Camera mCamera;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_test_camera);

        kameraTestiKamera = findViewById(R.id.kameraTestiKamera);
        kameraTestiNegative = findViewById(R.id.kameraTestiNegative);
        kameraTestiPositive = findViewById(R.id.kameraTestiPositive);

        kameraTestiKamera.setSurfaceTextureListener(this);

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

    @Override
    public void onSurfaceTextureAvailable(SurfaceTexture surface, int width, int height) {
        openCamera(surface);
    }

    @Override
    public void onSurfaceTextureSizeChanged(SurfaceTexture surface, int width, int height) {

    }

    @Override
    public boolean onSurfaceTextureDestroyed(SurfaceTexture surface) {
        if (mCamera != null) {
            mCamera.stopPreview();
            mCamera.release();
        }

        return true;
    }

    @Override
    public void onSurfaceTextureUpdated(SurfaceTexture surface) {

    }

    @Override
    public void onClick(View view) {

        switch (view.getId()) {

            default:
                break;

        }
    }

    private void resultClicked(boolean isPositive) {

        DeviceTest deviceTest = Helper.getDeviceTest(getApplicationContext());

        deviceTest.setKameraTestiOkay(isPositive);

        Helper.setDeviceTest(getApplicationContext(), deviceTest);

        TestCameraActivity.this.finish();

    }

    @Override
    protected void onStart() {
        super.onStart();
    }

    @Override
    public void onResume() {
        super.onResume();

        if (kameraTestiKamera.isAvailable()) {
            openCamera(kameraTestiKamera.getSurfaceTexture());
        } else {
            kameraTestiKamera.setSurfaceTextureListener(this);
        }

    }

    private void openCamera(SurfaceTexture surface) {

        mCamera = Camera.open(0);
        try {
            mCamera.setPreviewTexture(surface);

            Camera.Parameters parameters = mCamera.getParameters();
            parameters.setPreviewSize(640, 480);
            parameters.setPreviewFormat(ImageFormat.NV21);

            mCamera.setParameters(parameters);
            mCamera.startPreview();

        } catch (IOException ioe) {
        }
    }

    @Override
    public void onPause() {
        super.onPause();
    }

    @Override
    protected void onStop() {
        super.onStop();
    }


//    private View.OnClickListener photoOnClickListener = new View.OnClickListener() {
//        @Override
//        public void onClick(View v) {
//            Log.d(Constants.LOG_TAG, "photoOnClickListener onClick");
//            cameraView.captureImage(new CameraKitView.ImageCallback() {
//                @Override
//                public void onImage(CameraKitView view, final byte[] photo) {
//                    new Thread(new Runnable() {
//                        @Override
//                        public void run() {
//
//                            Log.d(Constants.LOG_TAG, "photoOnClickListener onImage photo=" + photo.length);
//                            runOnUiThread(new Runnable() {
//                                @Override
//                                public void run() {
//                                    Bitmap bitmap = BitmapFactory.decodeByteArray(photo, 0, photo.length);
//                                    imageViewTemp.setImageBitmap(bitmap);
//                                }
//                            });
//
//                        }
//                    }).start();
//                }
//            });
//        }
//    };

}
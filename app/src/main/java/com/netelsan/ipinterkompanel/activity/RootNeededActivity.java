package com.netelsan.ipinterkompanel.activity;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.res.AssetManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.Nullable;

import com.netelsan.ipinterkompanel.Constants;
import com.netelsan.ipinterkompanel.Helper;
import com.netelsan.ipinterkompanel.R;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Arrays;
import java.util.Timer;
import java.util.TimerTask;

public class RootNeededActivity extends BaseActivity {

    private int currentApiVersion;

    ImageView rootNeededImage;

    TextView rootNeededRootingText;
    TextView rootNeededRootFailed;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_root_needed);

        rootNeededImage = findViewById(R.id.rootNeededImage);

        rootNeededRootingText = findViewById(R.id.rootNeededRootingText);
        rootNeededRootFailed = findViewById(R.id.rootNeededRootFailed);

        currentApiVersion = android.os.Build.VERSION.SDK_INT;
        final int flags = View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                | View.SYSTEM_UI_FLAG_FULLSCREEN
                | View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY;
        getWindow().getDecorView().setSystemUiVisibility(flags);

        final View decorView = getWindow().getDecorView();
        decorView.setOnSystemUiVisibilityChangeListener(new View.OnSystemUiVisibilityChangeListener() {

            @Override
            public void onSystemUiVisibilityChange(int visibility) {
                if ((visibility & View.SYSTEM_UI_FLAG_FULLSCREEN) == 0) {
                    decorView.setSystemUiVisibility(flags);
                }
            }
        });

        startCopyOperations();

    }

    private static final String PATH_SDCARD = "sdcard";
    private static final String PREF_FIRST_RUN = "first_run";
    private File mSdCard;

    private void startCopyOperations() {

        rootNeededRootingText.setVisibility(View.VISIBLE);

        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(RootNeededActivity.this);
        if (prefs.getBoolean(PREF_FIRST_RUN, false)) {
            Log.v(Constants.LOG_TAG, "Not copying assets - already ran.");
            return;
        }
        mSdCard = Environment.getExternalStorageDirectory();
        if (mSdCard == null) {
            Log.e(Constants.LOG_TAG, "Could not get access to sdcard.");
        }
        copyAssets(RootNeededActivity.this);
        prefs.edit().putBoolean(PREF_FIRST_RUN, true);
        prefs.edit().commit();

//        startCheckRootFilesTimer();
    }

    private void copyAssets(Context context) {
        // We must use a subdirectory because Android implicitly adds
        // extra directories when using an empty string
        copyDirectory(PATH_SDCARD, context);
    }

    private void copyDirectory(String root, Context context) {
        // Get a handle to all the assets included.
        AssetManager assetManager = context.getAssets();
        String[] files = null;
        try {
            files = assetManager.list(root);
        } catch (IOException e) {
            Log.e(Constants.LOG_TAG, "Failed to get asset file list.", e);
        }
        Log.d(Constants.LOG_TAG, "************");
        Log.d(Constants.LOG_TAG, "Files to copy in " + root + ": " + Arrays.toString(files));
        for (String fileName : files) {
            String filePath = root + "/" + fileName;
            InputStream inputStream = null;
            OutputStream outputStream = null;
            try {
                inputStream = assetManager.open(filePath);
            } catch (IOException e) {
                // Asset could not be opened, is likely a directory.
                Log.w(Constants.LOG_TAG, "Could not open: " + filePath);
                // Treat the file as a directory and attempt to copy it's contents
                copyDirectory(filePath, context);
            }
            // If input was not set, then continue to the next found file.
            if (inputStream == null) {
                continue;
            }
            try {
                // If we are here then we have an actual file, lets copy it.
                filePath = trimSdcard(filePath);
                File file = new File(mSdCard + filePath);
                Log.d(Constants.LOG_TAG, "Copying file: " + file.getAbsolutePath());
                outputStream = new FileOutputStream(file);
                copyFile(inputStream, outputStream);
                inputStream.close();
                inputStream = null;
                outputStream.flush();
                outputStream.close();
                outputStream = null;
            } catch (Exception e) {
                // Huh... guess something bad happened...
                e.printStackTrace();

                SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
                prefs.edit().putBoolean(PREF_FIRST_RUN, false);
                prefs.edit().commit();
            }
        }

        startTimerForReboot();

    }

    Timer timerForReboot = null;

    private void startTimerForReboot() {
        if (timerForReboot != null) {
            return;
        }

        timerForReboot = new Timer();
        timerForReboot.schedule(new TimerTask() {
            @Override
            public void run() {
                Helper.showWhiteScreen(RootNeededActivity.this);
                rebootDevice();
            }
        }, 10000);

    }

    private String trimSdcard(String string) {
        if (string.startsWith(PATH_SDCARD)) {
            return string.substring(PATH_SDCARD.length());
        }
        return string;
    }

    private void copyFile(InputStream in, OutputStream out) throws IOException {
        byte[] buffer = new byte[1024];
        int read;
        while ((read = in.read(buffer)) != -1) {
            out.write(buffer, 0, read);
        }
    }

    @Override
    public void onWindowFocusChanged(boolean hasFocus) {
        super.onWindowFocusChanged(hasFocus);
        if (currentApiVersion >= Build.VERSION_CODES.KITKAT && hasFocus) {
            getWindow().getDecorView().setSystemUiVisibility(
                    View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                            | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                            | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                            | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                            | View.SYSTEM_UI_FLAG_FULLSCREEN
                            | View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY);
        }
    }

    private void rebootDevice() {
        try {

            Process process = Runtime.getRuntime().exec("adb reboot");
            process.waitFor();

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }

}

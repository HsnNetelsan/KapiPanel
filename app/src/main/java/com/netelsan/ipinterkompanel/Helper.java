package com.netelsan.ipinterkompanel;

import android.app.Activity;
import android.app.ActivityManager;
import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.Typeface;
import android.os.Environment;
import android.util.Base64;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.androidadvance.topsnackbar.TSnackbar;
import com.google.gson.Gson;
import com.kaopiz.kprogresshud.KProgressHUD;
import com.netelsan.ipinterkompanel.activity.DummyWhiteActivity;
import com.netelsan.ipinterkompanel.activity.InitializeActivity;
import com.netelsan.ipinterkompanel.activity.InitializeSiteActivity;
import com.netelsan.ipinterkompanel.activity.MainActivity;
import com.netelsan.ipinterkompanel.broadcast_receiver.PeriodicAlarmBroadcastReceiver;
import com.netelsan.ipinterkompanel.model.ComPackageModel;
import com.netelsan.ipinterkompanel.model.Daire;
import com.netelsan.ipinterkompanel.model.DeviceTest;
import com.netelsan.ipinterkompanel.model.Guvenlik;
import com.netelsan.ipinterkompanel.webrtc.activity.CallActivity;
import com.stericson.RootShell.execution.Command;
import com.stericson.RootTools.RootTools;

import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.Iterator;
import java.util.Locale;
import java.util.Timer;
import java.util.TimerTask;

import static android.content.Context.ALARM_SERVICE;

public class Helper {

    public static long getFileSizeInKB(File file) {

        long fileSizeInBytes = file.length();
        long fileSizeInKB = fileSizeInBytes / 1024;

        return fileSizeInKB;
    }

    static KProgressHUD progressHUD = null;
    private static Timer timerForProgress = null;
    private static int timerCount = 0;
    public static boolean aramaMesgul = false;

    public boolean door = false;
    public static boolean voice = false;


    public static void showProgressDialog(AppCompatActivity appCompatActivity, String title, String subtitle, boolean isNeedLongTimer) {

        dismissProgressDialog();

//        mutlaka bir timer koy ve timer sonunda olumsuz ise kapat
        progressHUD = KProgressHUD.create(appCompatActivity)
                .setStyle(KProgressHUD.Style.SPIN_INDETERMINATE)
                .setLabel(title)
                .setDetailsLabel(subtitle)
                .setCancellable(false)
                .setAnimationSpeed(2)
                .setDimAmount(0.6f)
                .show();

        if (timerForProgress == null) {
            timerForProgress = new Timer();
            timerCount = 0;
        } else {
            return;
        }

        timerForProgress.schedule(new TimerTask() {
            @Override
            public void run() {
                if (isNeedLongTimer) {
                    if (timerCount == 60) {
                        appCompatActivity.runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                timerForProgress = null;
                                dismissProgressDialog();
                            }
                        });
                        return;
                    }
                } else {
                    if (timerCount == 5) {
                        appCompatActivity.runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                timerForProgress = null;
                                dismissProgressDialog();
                            }
                        });
                        return;
                    }
                }
                timerCount = timerCount + 1;
            }
        }, 0, 1000);

    }

    public static void dismissProgressDialog() {
        if (progressHUD != null) {
            progressHUD.dismiss();
            progressHUD = null;
        }

        if (timerForProgress != null) {
            try {
                timerForProgress.purge();
                timerForProgress.cancel();
                timerForProgress = null;
            } catch (Exception e) {
                timerForProgress = null;
                e.printStackTrace();
            }
        }
    }


    public static DeviceTest getDeviceTest(Context context) {
        SharedPreferences sharedPreferences = context.getSharedPreferences(Constants.SHARED_PREFS, 0);
        String objectString = sharedPreferences.getString(Constants.KEY_DEVICE_TEST, null);
        if (objectString == null) {
            return new DeviceTest();
        }

        Gson gson = new Gson();
        try {
            return gson.fromJson(objectString, DeviceTest.class);
        } catch (Exception e) {
            return new DeviceTest();
        }
    }

    public static void setDeviceTest(Context context, DeviceTest deviceTest) {
        SharedPreferences sharedPreferences = context.getSharedPreferences(Constants.SHARED_PREFS, 0);
        SharedPreferences.Editor editor = sharedPreferences.edit();

        if (deviceTest == null) {
            editor.putString(Constants.KEY_DEVICE_TEST, null);
            editor.commit();
            return;
        }
        Gson gson = new Gson();
        String objectString = gson.toJson(deviceTest);

        editor.putString(Constants.KEY_DEVICE_TEST, objectString);
        editor.commit();
    }

    public static int getEthernetTestTryCount(Context context) {
        SharedPreferences sharedPreferences = context.getSharedPreferences(Constants.SHARED_PREFS, 0);
        int tryCount = sharedPreferences.getInt(Constants.KEY_ETHERNET_TEST_TRY_COUNT, 1);
        return tryCount;
    }

    public static void setEthernetTestTryCount(Context context, int tryCount) {
        SharedPreferences sharedPreferences = context.getSharedPreferences(Constants.SHARED_PREFS, 0);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putInt(Constants.KEY_ETHERNET_TEST_TRY_COUNT, tryCount);
        editor.commit();
    }

    public static boolean isEthernetTestFinished(Context context) {
        SharedPreferences sharedPreferences = context.getSharedPreferences(Constants.SHARED_PREFS, 0);
        boolean isRunning = sharedPreferences.getBoolean(Constants.KEY_ETHERNET_TEST_FINISHED, false);
        return isRunning;
    }

    public static void setEthernetTestFinished(Context context, boolean isFinished) {
        SharedPreferences sharedPreferences = context.getSharedPreferences(Constants.SHARED_PREFS, 0);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putBoolean(Constants.KEY_ETHERNET_TEST_FINISHED, isFinished);
        editor.commit();
    }

    public static boolean isDeviceTestFinished(Context context) {
        SharedPreferences sharedPreferences = context.getSharedPreferences(Constants.SHARED_PREFS, 0);
        boolean isRunning = sharedPreferences.getBoolean(Constants.KEY_DEVICE_TEST_FINISHED, false);
        return isRunning;
    }

    public static void setDeviceTestFinished(Context context, boolean isFinished) {
        SharedPreferences sharedPreferences = context.getSharedPreferences(Constants.SHARED_PREFS, 0);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putBoolean(Constants.KEY_DEVICE_TEST_FINISHED, isFinished);
        editor.commit();
    }

    public static boolean isDeviceDisplacementEnabled(Context context) {
        SharedPreferences sharedPreferences = context.getSharedPreferences(Constants.SHARED_PREFS, 0);
        boolean isEnable = sharedPreferences.getBoolean(Constants.KEY_DEVICE_DISPLACEMENT, false);
        return isEnable;
    }

    public static void setDeviceDisplacementEnabled(Context context, boolean isEnable) {
        SharedPreferences sharedPreferences = context.getSharedPreferences(Constants.SHARED_PREFS, 0);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putBoolean(Constants.KEY_DEVICE_DISPLACEMENT, isEnable);
        editor.commit();
    }

    public static boolean isTeknikPersonelSifreEnabled(Context context) {
        SharedPreferences sharedPreferences = context.getSharedPreferences(Constants.SHARED_PREFS, 0);
        boolean isEnabled = sharedPreferences.getBoolean(Constants.KEY_TEKNIK_PERS_SIFRE, true);
        return isEnabled;
    }

    public static int getRelayTime(Context context) {
        SharedPreferences sharedPreferences = context.getSharedPreferences(Constants.SHARED_PREFS, 0);
        int relayTime = sharedPreferences.getInt(Constants.KEY_RELAY_TIME, 0);
        return relayTime;
    }

    public static void setRelayTime(Context context, int relayTime) {
        SharedPreferences sharedPreferences = context.getSharedPreferences(Constants.SHARED_PREFS, 0);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putInt(Constants.KEY_RELAY_TIME, relayTime);
        editor.commit();
    }

    public static void setTeknikPersonelSifreEnabled(Context context, boolean isEnabled) {
        SharedPreferences sharedPreferences = context.getSharedPreferences(Constants.SHARED_PREFS, 0);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putBoolean(Constants.KEY_TEKNIK_PERS_SIFRE, isEnabled);
        editor.commit();
    }

    public static boolean isRebootedForRoot(Context context) {
        SharedPreferences sharedPreferences = context.getSharedPreferences(Constants.SHARED_PREFS, 0);
        boolean isRebooted = sharedPreferences.getBoolean(Constants.KEY_IS_REBOOTED_FOR_ROOT, false);
        return isRebooted;
    }

    public static void setRebootedForRoot(Context context, boolean isRebooted) {
        SharedPreferences sharedPreferences = context.getSharedPreferences(Constants.SHARED_PREFS, 0);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putBoolean(Constants.KEY_IS_REBOOTED_FOR_ROOT, isRebooted);
        editor.commit();

    }

    public static boolean isCenterUnitZilPanel(Context context) {
        SharedPreferences sharedPreferences = context.getSharedPreferences(Constants.SHARED_PREFS, 0);
        boolean isZilPanel = sharedPreferences.getBoolean(Constants.KEY_CENTER_UNIT, true);
        return isZilPanel;
    }

    public static void setCenterUnitZilPanel(Context context, boolean isZilPanel) {
        SharedPreferences sharedPreferences = context.getSharedPreferences(Constants.SHARED_PREFS, 0);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putBoolean(Constants.KEY_CENTER_UNIT, isZilPanel);
        editor.commit();

    }

    public static void backupDBToSDCard() {
        try {
            String newFolderName = getBackupNewFolderName();

//            Log.d(Constants.LOG_TAG, "backupDBToSDCard newFolderName=" + newFolderName);
            File file = new File("/data/data/com.netelsan.ipinterkompanel/databases/db_sube");
//            Log.d(Constants.LOG_TAG, "backupDBToSDCard file=" + file.getAbsolutePath());
            boolean isFileExist = file.exists();
//            Log.d(Constants.LOG_TAG, "backupDBToSDCard isFileExist=" + isFileExist);
            if (!isFileExist) {
                return;
            }

            Command commandForDelete = new Command(0, "rm -r /storage/extsd/db_backup/");
            int exitCodeForDelete = RootTools.getShell(true).add(commandForDelete).getExitCode();
            Log.d(Constants.LOG_TAG, "backupDBToSDCard commandForDelete=" + exitCodeForDelete);

            Command commandForCreate = new Command(0, "mkdir /storage/extsd/db_backup/");
            int exitCodeForCreate = RootTools.getShell(true).add(commandForCreate).getExitCode();
            Log.d(Constants.LOG_TAG, "backupDBToSDCard exitCodeForCreate=" + exitCodeForCreate);

            Command commandForCreate2 = new Command(0, "mkdir /storage/extsd/db_backup/" + newFolderName,
                    "adb push /data/data/com.netelsan.ipinterkompanel/databases/db_sube /storage/extsd/db_backup/" + newFolderName);
            int exitCodeForCreate2 = RootTools.getShell(true).add(commandForCreate2).getExitCode();
            Log.d(Constants.LOG_TAG, "backupDBToSDCard exitCodeForCreate2=" + exitCodeForCreate2);

        } catch (Exception e) {
        }

    }

    private static String getBackupNewFolderName() {

        Calendar calendar = Calendar.getInstance();
        String formattedDate = android.text.format.DateFormat.format("dd_MM_yyyy_HH_mm_ss", calendar.getTime()).toString();

        return formattedDate;

    }

    public static void setDeviceTime(Context context, long zilPanelTimeMillis) {

        Calendar calendar = Calendar.getInstance();
        calendar.setTimeInMillis(zilPanelTimeMillis);

        AlarmManager am = (AlarmManager) context.getSystemService(ALARM_SERVICE);
        am.setTimeZone("Europe/Istanbul");

        setDeviceTime(calendar);
    }

    private static void setDeviceTime(Calendar calendar) {

        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyyMMdd.HHmmss");
        String formattedDate = dateFormat.format(calendar.getTime());

        try {
            Process process = Runtime.getRuntime().exec("su 0 toolbox date -s " + formattedDate);//YYYYMMDD.HHmmss
            BufferedReader in = new BufferedReader(new InputStreamReader(process.getInputStream()));
        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    public static boolean isZilForSite(Context context) {
        SharedPreferences sharedPreferences = context.getSharedPreferences(Constants.SHARED_PREFS, 0);
        boolean isEnabled = sharedPreferences.getBoolean(Constants.KEY_ZIL_FOR_SITE, false);
        return isEnabled;
    }

    public static void setZilForSite(Context context, boolean isEnabled) {
        SharedPreferences sharedPreferences = context.getSharedPreferences(Constants.SHARED_PREFS, 0);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putBoolean(Constants.KEY_ZIL_FOR_SITE, isEnabled);
        editor.commit();
    }

    public static void setIPInfoView(Context context, View view) {

        boolean isIPInfoEnabled = isIPInfoEnabled(context);
        if (isIPInfoEnabled) {
            view.setVisibility(View.VISIBLE);
        } else {
            view.setVisibility(View.GONE);
        }

    }

    public static boolean isIPInfoEnabled(Context context) {
        SharedPreferences sharedPreferences = context.getSharedPreferences(Constants.SHARED_PREFS, 0);
        boolean isEnabled = sharedPreferences.getBoolean(Constants.KEY_IP_INFO, false);
        return isEnabled;
    }

    public static void setIPInfoEnabled(Context context, boolean isEnabled) {
        SharedPreferences sharedPreferences = context.getSharedPreferences(Constants.SHARED_PREFS, 0);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putBoolean(Constants.KEY_IP_INFO, isEnabled);
        editor.commit();
    }

    public static boolean isServiceRunning(Context context, Class<?> serviceClass) {
        ActivityManager manager = (ActivityManager) context.getSystemService(Context.ACTIVITY_SERVICE);

        for (ActivityManager.RunningServiceInfo service : manager.getRunningServices(Integer.MAX_VALUE)) {
            if (serviceClass.getName().equals(service.service.getClassName())) {
//                Log.i(Constants.LOG_TAG, "isServiceRunning? " + true + "");
                return true;
            }
        }

//        Log.i(Constants.LOG_TAG, "isServiceRunning? " + false + "");
        return false;
    }

    public static String getOnboardingText1(Context context) {
        SharedPreferences sharedPreferences = context.getSharedPreferences(Constants.SHARED_PREFS, 0);
        String sifre = sharedPreferences.getString(Constants.KEY_ONBOARDING_NAME_1, null);
        return sifre;
    }

    public static void setOnboardingText1(Context context, String onboardingText) {
        SharedPreferences sharedPreferences = context.getSharedPreferences(Constants.SHARED_PREFS, 0);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putString(Constants.KEY_ONBOARDING_NAME_1, onboardingText);
        editor.commit();
    }

    public static String getOnboardingText2(Context context) {
        SharedPreferences sharedPreferences = context.getSharedPreferences(Constants.SHARED_PREFS, 0);
        String sifre = sharedPreferences.getString(Constants.KEY_ONBOARDING_NAME_2, null);
        return sifre;
    }

    public static void setOnboardingText2(Context context, String onboardingText) {
        SharedPreferences sharedPreferences = context.getSharedPreferences(Constants.SHARED_PREFS, 0);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putString(Constants.KEY_ONBOARDING_NAME_2, onboardingText);
        editor.commit();
    }

    public static void runRestartCameraShellCommand() throws Throwable {
        Process process = Runtime.getRuntime().exec("su");
        DataOutputStream dataOutputStream = null;
        try {
            dataOutputStream = new DataOutputStream(process.getOutputStream());
            final String command = createKillCommandForSystemBin("media", "mediaserver");// + createKillCommandForSystemBin("camera", "cameraserver");
//            Log.d(Constants.LOG_TAG, "runRestartCameraShellCommand command=" + command);

            dataOutputStream.writeBytes(command + "\n");
        } finally {
            if (dataOutputStream != null) {
                try {
                    dataOutputStream.writeBytes("exit\n");
                } catch (Throwable t) {
                    t.printStackTrace();
                }
                try {
                    dataOutputStream.flush();
                } catch (Throwable t) {
                    t.printStackTrace();
                }
                try {
                    dataOutputStream.close();
                } catch (Throwable t) {
                    t.printStackTrace();
                }
                try {
                    process.waitFor();
                } catch (Throwable t) {
                    t.printStackTrace();
                }
                try {
                    process.destroy();
                } catch (Throwable t) {
                    t.printStackTrace();
                }
            }
        }
    }

    private static String createKillCommandForSystemBin(final String _user, final String _fileName) {
        return "kill $(ps|grep ^" + _user + ".*[/]system[/]bin[/]" + _fileName + "$|tr -s ' '|cut -d ' ' -f2);";
    }

    public static boolean isSDCardMounted() {

        try {
            File newFile = new File("/mnt/extsd/");
            if (newFile.length() > 0) {
//                Log.d(Constants.LOG_TAG, "1907 newFile size=" + newFile.length());
                return true;
            }
            return false;
        } catch (Exception e) {
            return false;
        }

    }

    public static void killCamera() {

        if (true) {
            return;
        }

        String text;

        String output;

        output = execute("ps");
        if (output.startsWith("Error")) {
            Log.d(Constants.LOG_TAG, "killCamera output=" + output);
            return;
        }

        String[] ps_output = output.split(System.getProperty("line.separator"));
        for (String line : ps_output) {
            if (line.contains("camera")) {
                if (line.contains("killcamera")) {
                    continue; //Watch out don't kill yourself!
                }
                String[] columns = line.split("\\s+");
                if (columns.length >= 2) {
                    execute("kill " + columns[1]);
                    Log.d(Constants.LOG_TAG, "killCamera " + "Killed " + columns[columns.length - 1] + " PID: " + columns[1]);
                }
            }
        }
    }

    private static final String execute(String command) {
        boolean retval = false;
        String error = "";
        DataInputStream osRes;
        Process suProcess;
        StringBuilder output = null;

        try {
            if (null != command && command.length() > 0) {
                suProcess = Runtime.getRuntime().exec("su");

                DataOutputStream os = new DataOutputStream(suProcess.getOutputStream());
                osRes = new DataInputStream(suProcess.getInputStream());

                // Execute commands that require root access
                os.writeBytes(command + "\n");
                os.flush();

                os.writeBytes("exit\n");
                os.flush();

                try {
                    int suProcessRetval = suProcess.waitFor();
                    if (255 != suProcessRetval) {
                        // Root access granted
                        retval = true;
                        BufferedReader r = new BufferedReader(new InputStreamReader(osRes));
                        output = new StringBuilder();
                        String line = "";
                        while ((line = r.readLine()) != null) {
                            output.append(line).append('\n');
                        }
                    } else {
                        // Root access denied
                        retval = false;
                    }
                } catch (Exception ex) {
                    error = "Error executing root action";
                    Log.e("ROOT", error, ex);
                }
            }
        } catch (IOException ex) {
            error = "Error: Can't get root access";
            Log.w("ROOT", error, ex);
        } catch (SecurityException ex) {
            error = "Error: Can't get root access";
            Log.w("ROOT", error, ex);
        } catch (Exception ex) {
            error = "Error executing internal operation";
            Log.w("ROOT", error, ex);
        }

        if (!retval) {
            return error;
        }

        return output.toString();
    }

    public static String convertBitmapToString(Bitmap bitmap) {
        ByteArrayOutputStream byteArrayOS = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.JPEG, 100, byteArrayOS);
        return Base64.encodeToString(byteArrayOS.toByteArray(), Base64.DEFAULT);
    }

    public static boolean isTusSesiEnabled(Context context) {
        SharedPreferences sharedPreferences = context.getSharedPreferences(Constants.SHARED_PREFS, 0);
        boolean isEnabled = sharedPreferences.getBoolean(Constants.KEY_TUS_SESI, false);
        return isEnabled;
    }

    public static void setTusSesiEnabled(Context context, boolean isEnabled) {
        SharedPreferences sharedPreferences = context.getSharedPreferences(Constants.SHARED_PREFS, 0);
        SharedPreferences.Editor editor = sharedPreferences.edit();

        editor.putBoolean(Constants.KEY_TUS_SESI, isEnabled);
        editor.commit();
    }

    public static void setVolume(Context context, int volume) {
        SharedPreferences sharedPreferences = context.getSharedPreferences(Constants.SHARED_PREFS, 0);
        SharedPreferences.Editor editor = sharedPreferences.edit();

        editor.putInt(Constants.KEY_VOLUME, volume);
        editor.commit();
    }

    public static void setDoorLock(Context context, boolean volume) {
        SharedPreferences sharedPreferences = context.getSharedPreferences(Constants.SHARED_PREFS, 0);
        SharedPreferences.Editor editor = sharedPreferences.edit();

        editor.putBoolean(Constants.KEY_DOOR, volume);
        editor.commit();
    }

    public static boolean getDoorLock(Context context) {
        SharedPreferences sharedPreferences = context.getSharedPreferences(Constants.SHARED_PREFS, 0);

        boolean doorInlock = sharedPreferences.getBoolean(Constants.KEY_DOOR, false);
        return doorInlock;
    }


    public static boolean isCallVideoRecordEnabled(Context context) {
        SharedPreferences sharedPreferences = context.getSharedPreferences(Constants.SHARED_PREFS, 0);
        boolean isEnabled = sharedPreferences.getBoolean(Constants.KEY_CALL_VIDEO_RECORD, false);
        return isEnabled;
    }

    public static void setCallVideoRecordEnabled(Context context, boolean isEnabled) {
        SharedPreferences sharedPreferences = context.getSharedPreferences(Constants.SHARED_PREFS, 0);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putBoolean(Constants.KEY_CALL_VIDEO_RECORD, isEnabled);
        editor.commit();
    }

    public static void showWhiteScreen(AppCompatActivity activity) {
        Intent intent = new Intent(activity, DummyWhiteActivity.class);
        activity.startActivity(intent);
    }

    public static void showWhiteScreen(Activity activity) {
        Intent intent = new Intent(activity, DummyWhiteActivity.class);
        activity.startActivity(intent);
    }

    public static File createDaireCallImageFolderIfNeeded(Daire selectedDaire) {

        String daireFolderName = selectedDaire.getBlok() + "_" + selectedDaire.getDaireNo();

        String root = Environment.getExternalStorageDirectory().toString();

        File myDir = new File(root + "/call_images/" + daireFolderName);

        if (!myDir.exists()) {
            myDir.mkdirs();
        }

        Log.d("HK", "HK direxist() :" + myDir.exists());
        return myDir;

    }

    public static File createGuvenlikCallImageFolderIfNeeded(Context context, Guvenlik selectedGuvenlik) {

        String daireFolderName = context.getString(R.string.guvenlik) + "_" + selectedGuvenlik.getGuvenlikNo();

        String root = Environment.getExternalStorageDirectory().toString();
        File myDir = new File(root + "/call_images/" + daireFolderName);
        if (!myDir.exists()) {
            myDir.mkdirs();
        }

        return myDir;

    }

    public static void showTopMessageBanner(AppCompatActivity appCompatActivity, String text, boolean isSuccess) {

        View containerView = null;
        if (appCompatActivity instanceof MainActivity) {
            MainActivity mainActivity = (MainActivity) appCompatActivity;
            containerView = mainActivity.findViewById(R.id.containerForMessageBanner);
        } else if (appCompatActivity instanceof CallActivity) {
            CallActivity callActivity = (CallActivity) appCompatActivity;
            containerView = callActivity.findViewById(R.id.idForBannerMessage);
        } else if (appCompatActivity instanceof InitializeActivity) {
            InitializeActivity initializeActivity = (InitializeActivity) appCompatActivity;
            containerView = initializeActivity.findViewById(R.id.idForBannerMessage);
        } else if (appCompatActivity instanceof InitializeSiteActivity) {
            InitializeSiteActivity initializeSiteActivity = (InitializeSiteActivity) appCompatActivity;
            containerView = initializeSiteActivity.findViewById(R.id.idForBannerMessage);
        } else {
            return;
        }

        if (containerView == null) {
            return;
        }

        TSnackbar snackbar = TSnackbar.make(containerView, text, TSnackbar.LENGTH_LONG);
        snackbar.setActionTextColor(Color.WHITE);
        View snackbarView = snackbar.getView();
        if (isSuccess) {
            snackbarView.setBackgroundColor(Color.parseColor("#99cc00"));
        } else {
            snackbarView.setBackgroundColor(Color.parseColor("#ff4444"));
        }
        TextView textView = snackbarView.findViewById(com.androidadvance.topsnackbar.R.id.snackbar_text);
        textView.setTextColor(Color.WHITE);
        textView.setGravity(Gravity.CENTER);
        Typeface face = Typeface.createFromAsset(appCompatActivity.getAssets(), "bold.ttf");
        textView.setTypeface(face);
        textView.setTextSize(20);
        snackbar.show();

    }

    public static String getComMessageById(Context context, int messageId) {

        if (messageId == Constants.COM_MESSAGE_NOT_RESPONDING) {
            aramaMesgul = false;
            return context.getString(R.string.cevap_vermiyor);
        } else if (messageId == Constants.COM_MESSAGE_REJECT) {
            aramaMesgul = false;
            return context.getString(R.string.arama_reddedildi);
        } else if (messageId == Constants.COM_MESSAGE_ACCEPT) {
            aramaMesgul = false;
            return context.getString(R.string.arama_kabul);
        } else if (messageId == Constants.COM_MESSAGE_CANCELED) {
            aramaMesgul = false;
            return context.getString(R.string.arama_iptal);
        } else if (messageId == Constants.COM_MESSAGE_BUSY) {
            aramaMesgul = true;
            return context.getString(R.string.arama_mesgul);
        }

        aramaMesgul = false;
        return "";

    }

    public static void copyFile(String inputPath, String inputFile, String outputPath) {

        InputStream in = null;
        OutputStream out = null;
        try {

            //create output directory if it doesn't exist
            File dir = new File(outputPath);
            if (!dir.exists()) {
                dir.mkdirs();
            }


            in = new FileInputStream(inputPath + inputFile);
            out = new FileOutputStream(outputPath + inputFile);

            byte[] buffer = new byte[1024];
            int read;
            while ((read = in.read(buffer)) != -1) {
                out.write(buffer, 0, read);
            }
            in.close();
            in = null;

            // write the output file (You have now copied the file)
            out.flush();
            out.close();
            out = null;

        } catch (Exception fnfe1) {
            Log.e(Constants.LOG_TAG, fnfe1.getMessage());
        }

    }


    public static void setNextAlarm(Context context, long alarmTimeMillis) {

        Intent intent = new Intent(context, PeriodicAlarmBroadcastReceiver.class);
        PendingIntent pendingIntent = PendingIntent.getBroadcast(context, 234324243, intent, 0);
        AlarmManager alarmManager = (AlarmManager) context.getSystemService(ALARM_SERVICE);
        alarmManager.set(AlarmManager.RTC_WAKEUP, alarmTimeMillis, pendingIntent);

    }

    public static long getNextAlarmTimeMillis() {

        Calendar calendar = Calendar.getInstance();

        int currentMinute = calendar.get(Calendar.MINUTE);

        if (currentMinute >= 0 && currentMinute < 15) {
            calendar.set(Calendar.MINUTE, 15);
        } else if (currentMinute >= 15 && currentMinute < 30) {
            calendar.set(Calendar.MINUTE, 30);
        } else if (currentMinute >= 30 && currentMinute < 45) {
            calendar.set(Calendar.MINUTE, 45);
        } else {
            calendar.set(Calendar.MINUTE, 0);
            calendar.add(Calendar.HOUR_OF_DAY, 1);
        }

        calendar.set(Calendar.SECOND, 0);
        calendar.set(Calendar.MILLISECOND, 0);

        long nextAlarmTimeMillis = calendar.getTimeInMillis();
        Log.d(Constants.LOG_TAG, "nextAlarmTimeMillis=" + nextAlarmTimeMillis);

        return nextAlarmTimeMillis;

    }

    public static String getFormattedDate(int selectedDayOfMonth, int selectedMonth, int selectedYear) {

        Calendar calendar = Calendar.getInstance();
        calendar.set(Calendar.DAY_OF_MONTH, selectedDayOfMonth);
        calendar.set(Calendar.MONTH, selectedMonth);
        calendar.set(Calendar.YEAR, selectedYear);

        String formattedDate = android.text.format.DateFormat.format("dd MM yyyy", calendar.getTime()).toString();
        return formattedDate;

    }

    public static String getFormattedTime(int selectedHour, int selectedMinute) {

        Calendar calendar = Calendar.getInstance();
        calendar.set(Calendar.HOUR_OF_DAY, selectedHour);
        calendar.set(Calendar.MINUTE, selectedMinute);

        String formattedDate = android.text.format.DateFormat.format("HH:mm", calendar.getTime()).toString();
        return formattedDate;

    }

    public static int getSelectedLanguageCode(Context context) {
        SharedPreferences sharedPreferences = context.getSharedPreferences(Constants.SHARED_PREFS, 0);
        int languageCode = sharedPreferences.getInt(Constants.KEY_LANGUAGE, Constants.LANGUAGE_TR);
        return languageCode;
    }

    public static void setSelectedLanguageCode(Context context, int languageCode) {
        SharedPreferences sharedPreferences = context.getSharedPreferences(Constants.SHARED_PREFS, 0);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putInt(Constants.KEY_LANGUAGE, languageCode);
        editor.commit();
    }

    public static int getSystemMonthId(int selectedMonth) {

        switch (selectedMonth) {

            case 1:
                return Calendar.JANUARY;

            case 2:
                return Calendar.FEBRUARY;

            case 3:
                return Calendar.MARCH;

            case 4:
                return Calendar.APRIL;

            case 5:
                return Calendar.MAY;

            case 6:
                return Calendar.JUNE;

            case 7:
                return Calendar.JULY;

            case 8:
                return Calendar.AUGUST;

            case 9:
                return Calendar.SEPTEMBER;

            case 10:
                return Calendar.OCTOBER;

            case 11:
                return Calendar.NOVEMBER;

            case 12:
                return Calendar.DECEMBER;

            default:
                break;
        }
        return Calendar.JANUARY;

    }

    public static boolean isMyServiceRunning(Context context, Class<?> serviceClass) {
        ActivityManager manager = (ActivityManager) context.getSystemService(Context.ACTIVITY_SERVICE);

        for (ActivityManager.RunningServiceInfo service : manager.getRunningServices(Integer.MAX_VALUE)) {
            if (serviceClass.getName().equals(service.service.getClassName())) {
                Log.i(Constants.LOG_TAG, "isMyServiceRunning? " + true + "");
                return true;
            }
        }

        Log.i(Constants.LOG_TAG, "isMyServiceRunning? " + false + "");
        return false;
    }

    public static void deleteCache(Context context) {
        try {
            File dir = context.getCacheDir();
            deleteDir(dir);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static boolean deleteDir(File dir) {
        if (dir != null && dir.isDirectory()) {
            String[] children = dir.list();
            for (int i = 0; i < children.length; i++) {
                boolean success = deleteDir(new File(dir, children[i]));
                if (!success) {
                    return false;
                }
            }
            return dir.delete();
        } else if (dir != null && dir.isFile()) {
            return dir.delete();
        } else {
            return false;
        }
    }

    public static Process getProcess() {

        try {
            Process process = Runtime.getRuntime().exec("su");
            return process;

        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }

    }

    public static void setGPIO(Process process, int processType, ArrayList<Integer> ports, boolean isDırectionOut, boolean isHigh) {

        try {

            Iterator<Integer> iterator = ports.iterator();
            while (iterator.hasNext()) {
                int port = iterator.next();

                DataOutputStream dataOutputStream = new DataOutputStream(process.getOutputStream());

                String basePath = "/sys/class/gpio/";

                if (processType == Constants.GPIO_EXPORT) {
                    dataOutputStream.writeBytes("echo " + port + " > " + basePath + "export\n");
                }
                if (processType == Constants.GPIO_DIRECTION) {
                    if (isDırectionOut) {
                        dataOutputStream.writeBytes("echo out > " + basePath + "gpio" + port + "/direction\n");
                    } else {
                        dataOutputStream.writeBytes("echo in > " + basePath + "gpio" + port + "/direction\n");
                    }
                }
                if (processType == Constants.GPIO_VALUE) {
                    if (isHigh) {
                        dataOutputStream.writeBytes("echo 1 > " + basePath + "gpio" + port + "/value\n");
                    } else {
                        dataOutputStream.writeBytes("echo 0 > " + basePath + "gpio" + port + "/value\n");
                    }
                }
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void setGPIO(Process process, int processType, int port, boolean isDırectionOut, boolean isHigh) {

        try {

//            Log.d(Constants.LOG_TAG, "setGPIO port=" + port + " Direction=Out=" + isDırectionOut + " isHigh=" + isHigh);

            DataOutputStream dataOutputStream = new DataOutputStream(process.getOutputStream());

            String basePath = "/sys/class/gpio/";

            if (processType == Constants.GPIO_EXPORT) {
                dataOutputStream.writeBytes("echo " + port + " > " + basePath + "export\n");
            }
            if (processType == Constants.GPIO_DIRECTION) {
                if (isDırectionOut) {
                    dataOutputStream.writeBytes("echo out > " + basePath + "gpio" + port + "/direction\n");
                } else {
                    dataOutputStream.writeBytes("echo in > " + basePath + "gpio" + port + "/direction\n");
                }
            }
            if (processType == Constants.GPIO_VALUE) {
                if (isHigh) {
                    dataOutputStream.writeBytes("echo 1 > " + basePath + "gpio" + port + "/value\n");
                } else {
                    dataOutputStream.writeBytes("echo 0 > " + basePath + "gpio" + port + "/value\n");
                }
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static int getPINFromGPIO(String gpioPin) {

        if (gpioPin.length() != 4) {
            return -1;
        }

        char[] ch = gpioPin.toCharArray();

        String pinText = ch[1] + "";

        int alphabetIndex = getAlphabetIndex(pinText);
        Log.d(Constants.LOG_TAG, "getPINFromGPIO alphabetIndex=" + alphabetIndex);

        String pinNumber = ch[2] + "" + ch[3];
        Log.d(Constants.LOG_TAG, "getPINFromGPIO pinNumber=" + pinNumber);

        int intPinNumber = Integer.parseInt(pinNumber);

        return ((alphabetIndex - 1) * 32) + intPinNumber;

    }

    private static int getAlphabetIndex(String text) {

        text = text.toLowerCase();

        char[] ch = text.toCharArray();
        for (char c : ch) {
            int temp = (int) c;
            int temp_integer = 96; //for lower case
            if (temp <= 122 & temp >= 97)
                return temp - temp_integer;
//                System.out.print(temp - temp_integer);
        }

        return -1;
    }

    public static ComPackageModel getComPackageModelFromJsonString(String objectString) {

        try {
            Gson gson = new Gson();
            ComPackageModel comPackageModel = gson.fromJson(objectString, ComPackageModel.class);
            return comPackageModel;
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }

    }

    private static final String DATE_PATTERN = "(0?[1-9]|1[012]) [/.-] (0?[1-9]|[12][0-9]|3[01]) [/.-] ((19|20)\\d\\d)";

    public static boolean isDateValid(final String date) {

        DateFormat format = new SimpleDateFormat("dd.MM.yyyy");

        format.setLenient(false);

//        String date = "29/18/2017";
        try {
            format.parse(date);
            return true;
        } catch (ParseException e) {
//            System.out.println("Date " + date + " is not valid according to " + ((SimpleDateFormat) format).toPattern() + " pattern.");
            return false;
        }

    }

    public static long getWrongAttemptTimeMillis(Context context) {
        SharedPreferences sharedPreferences = context.getSharedPreferences(Constants.SHARED_PREFS, 0);
        long count = sharedPreferences.getLong(Constants.KEY_WRONG_ATTEMPT_TIME, 0);
        return count;
    }

    public static void setWrongAttemptTimeMillis(Context context, long count) {
        SharedPreferences sharedPreferences = context.getSharedPreferences(Constants.SHARED_PREFS, 0);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putLong(Constants.KEY_WRONG_ATTEMPT_TIME, count);
        editor.commit();
    }

    public static int getWrongAttemptCount(Context context) {
        SharedPreferences sharedPreferences = context.getSharedPreferences(Constants.SHARED_PREFS, 0);
        int count = sharedPreferences.getInt(Constants.KEY_WRONG_ATTEMPT, 0);
        return count;
    }

    public static void setWrongAttemptCount(Context context, int count) {
        SharedPreferences sharedPreferences = context.getSharedPreferences(Constants.SHARED_PREFS, 0);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putInt(Constants.KEY_WRONG_ATTEMPT, count);
        editor.commit();
    }

    public static boolean isHandshakeFinished(Context context) {
        SharedPreferences sharedPreferences = context.getSharedPreferences(Constants.SHARED_PREFS, 0);
        boolean isRunning = sharedPreferences.getBoolean(Constants.KEY_HANDSHAKE_FINISHED, false);
        return isRunning;
    }

    public static void setHandshakeFinished(Context context, boolean isFinished) {
        SharedPreferences sharedPreferences = context.getSharedPreferences(Constants.SHARED_PREFS, 0);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putBoolean(Constants.KEY_HANDSHAKE_FINISHED, isFinished);
        editor.commit();
    }

    public static boolean isInitializeFinished(Context context) {
        SharedPreferences sharedPreferences = context.getSharedPreferences(Constants.SHARED_PREFS, 0);
        boolean isRunning = sharedPreferences.getBoolean(Constants.KEY_INITIALIZE_FINISHED, false);
        return isRunning;
    }

    public static void setInitializeFinished(Context context, boolean isFinished) {
        SharedPreferences sharedPreferences = context.getSharedPreferences(Constants.SHARED_PREFS, 0);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putBoolean(Constants.KEY_INITIALIZE_FINISHED, isFinished);
        editor.commit();
    }

    public static String getDateStringForError() {

        Calendar calendar = Calendar.getInstance();

        SimpleDateFormat dateFormat = new SimpleDateFormat("dd_MM_yyyy__HH_mm_ss");
        String formattedDate = dateFormat.format(calendar.getTime());

        return formattedDate;
    }

    public static String getCurrentHour() {

        SimpleDateFormat sdf = new SimpleDateFormat("HH", Locale.getDefault());
        String currentDate = sdf.format(new Date());

        return currentDate;
    }

    public static String getCurrentMinute() {

        SimpleDateFormat sdf = new SimpleDateFormat("mm", Locale.getDefault());
        String currentDate = sdf.format(new Date());

        return currentDate;
    }

    public static String getCurrentDayName() {

        SimpleDateFormat sdf = new SimpleDateFormat("EEEE", Locale.getDefault());
        String currentDate = sdf.format(new Date());

        return currentDate;
    }

    public static String getCurrentDayOfMonth() {

        SimpleDateFormat sdf = new SimpleDateFormat("dd", Locale.getDefault());
        String currentDate = sdf.format(new Date());

        return currentDate;
    }

    public static String getCurrentMonthName() {

        SimpleDateFormat sdf = new SimpleDateFormat("MMMM", Locale.getDefault());
        String currentDate = sdf.format(new Date());

        return currentDate;
    }

    public static String getCurrentMonth() {

        SimpleDateFormat sdf = new SimpleDateFormat("MM", Locale.getDefault());
        String currentDate = sdf.format(new Date());

        return currentDate;
    }

    public static String getCurrentYear() {

        SimpleDateFormat sdf = new SimpleDateFormat("yyyy", Locale.getDefault());
        String currentDate = sdf.format(new Date());

        return currentDate;
    }

}

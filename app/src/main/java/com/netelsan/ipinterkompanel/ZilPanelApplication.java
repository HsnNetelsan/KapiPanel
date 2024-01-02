package com.netelsan.ipinterkompanel;

import android.app.AlarmManager;
import android.app.Application;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

import androidx.multidex.MultiDex;

import com.netelsan.ipinterkompanel.activity.SplashSelectorActivity;
import com.netelsan.ipinterkompanel.error_handling.ErrorHandlingUtils;
import com.netelsan.ipinterkompanel.tcp.Utils;

import java.io.File;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.io.Writer;

public class ZilPanelApplication extends Application {

    @Override
    public void onCreate() {
        super.onCreate();

        try {
            Thread.setDefaultUncaughtExceptionHandler(new Thread.UncaughtExceptionHandler() {
                @Override
                public void uncaughtException(Thread thread, Throwable throwable) {
                    handleUncaughtException(throwable);
                }
            });
        } catch(SecurityException e) {
            Log.e(Constants.LOG_TAG, "Could not set the Default Uncaught Exception Handler:" + e.getStackTrace());
        }

    }

    private void handleUncaughtException(Throwable throwable) {
//        e.printStackTrace();

        File fileErrorLogs;
        if(throwable == null) {
            fileErrorLogs = ErrorHandlingUtils.getErrorLogsTextFile(true);
        } else {
            fileErrorLogs = ErrorHandlingUtils.getErrorLogsTextFile(false);
        }
//        String previousErrorLogs = ErrorHandlingUtils.readFromFile(fileErrorLogs);

        String currentErrorLogs = getErrorLogs(throwable);

//        previousErrorLogs = previousErrorLogs + currentErrorLogs;
        ErrorHandlingUtils.writeToFile(currentErrorLogs, fileErrorLogs);

        restartApplication();

    }

    public void restartApplication(){

        Intent mStartActivity = new Intent(ZilPanelApplication.this, SplashSelectorActivity.class);
        mStartActivity.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        int mPendingIntentId = 135;
        PendingIntent mPendingIntent = PendingIntent.getActivity(ZilPanelApplication.this, mPendingIntentId, mStartActivity, PendingIntent.FLAG_CANCEL_CURRENT);
        AlarmManager mgr = (AlarmManager) getSystemService(Context.ALARM_SERVICE);
        mgr.set(AlarmManager.RTC, System.currentTimeMillis() + 100, mPendingIntent);
        System.exit(0);
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

        if(throwable != null) {

            Writer writer = new StringWriter();
            throwable.printStackTrace(new PrintWriter(writer));
            String errorLogStack = writer.toString();

            errorLog = errorLog + errorLogStack + "\n";

//            errorLog = errorLog + throwable.getMessage() + "\n\n";
//            errorLog = errorLog + errorLogStack + "\n\n";
//
//            StackTraceElement[] stackTraceElements = throwable.getStackTrace();
//            for(int index = 0; index < stackTraceElements.length; index++) {
//                StackTraceElement stackTraceElement = stackTraceElements[index];
//                String stack = stackTraceElement.toString();
//                int lineNumber = stackTraceElement.getLineNumber();
//                String className = stackTraceElement.getClassName();
//                String methodName = stackTraceElement.getMethodName();
//                errorLog = errorLog + stack + "  CLASS:" + className  + "  METHOD:" + methodName + "  LINE:" + lineNumber + "\n";
//            }
//
//            errorLog = errorLog + throwable.getCause() + "\n\n";
        } else {
            errorLog = errorLog + " onLowMemory\n";
        }

        errorLog = errorLog + "******************** END ********************\n\n";

        return errorLog;
    }

    @Override
    protected void attachBaseContext(Context base) {
        super.attachBaseContext(base);
        MultiDex.install(this);
    }

    @Override
    public void onLowMemory() {
        super.onLowMemory();

        handleUncaughtException(null);

    }
}

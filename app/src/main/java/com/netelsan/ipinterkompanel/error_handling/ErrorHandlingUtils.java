package com.netelsan.ipinterkompanel.error_handling;

import android.os.Environment;

import com.netelsan.ipinterkompanel.Helper;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;

public class ErrorHandlingUtils {

    public static File getErrorLogsTextFile(boolean isLowMemory) {

        String root = Environment.getExternalStorageDirectory().toString();
        File myDir = new File(root + "/error_logs");
        if(! myDir.exists()) {
            myDir.mkdirs();
        }

        String errorFileName = Helper.getDateStringForError();
        if(isLowMemory) {
            errorFileName = errorFileName + "_low_memory";
        }
        String fileName = errorFileName + ".txt";
        File file = new File(myDir, fileName);

        return file;

    }

    public static void writeToFile(String errorText, File file) {

        try {
            FileOutputStream fileOutputStream = new FileOutputStream(file);
            fileOutputStream.write(errorText.getBytes());
            fileOutputStream.close();
        } catch(Exception e) {
            e.printStackTrace();
        }

    }

    public static String readFromFile(File file) {

        int length = (int) file.length();

        byte[] bytes = new byte[length];

        try {
            FileInputStream fileInputStream = new FileInputStream(file);
            fileInputStream.read(bytes);
            fileInputStream.close();
        } catch(Exception e) {
            e.printStackTrace();
        }
        String contents = new String(bytes);

        return contents;
    }

}

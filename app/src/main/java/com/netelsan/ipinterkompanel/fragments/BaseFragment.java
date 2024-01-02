package com.netelsan.ipinterkompanel.fragments;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.os.Handler;
import android.util.Base64;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;

import com.netelsan.ipinterkompanel.Constants;
import com.netelsan.ipinterkompanel.Helper;
import com.netelsan.ipinterkompanel.R;
import com.netelsan.ipinterkompanel.activity.InitializeActivity;
import com.netelsan.ipinterkompanel.activity.MainActivity;
import com.netelsan.ipinterkompanel.activity.RootNeededActivity;
import com.netelsan.ipinterkompanel.activity.ScreensaverActivity2;
import com.netelsan.ipinterkompanel.activity.SplashSelectorActivity;
import com.netelsan.ipinterkompanel.database.DatabaseHelper;
import com.netelsan.ipinterkompanel.model.CallSnapshotObject;
import com.netelsan.ipinterkompanel.model.Daire;
import com.netelsan.ipinterkompanel.model.DoorPassword;
import com.netelsan.ipinterkompanel.model.DoorUnlockLog;
import com.netelsan.ipinterkompanel.service.KeyPadService;
import com.stericson.RootShell.execution.Command;
import com.stericson.RootTools.RootTools;

import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Iterator;

import static org.webrtc.ContextUtils.getApplicationContext;

public class BaseFragment extends Fragment {

    public KeyPadService keyPadService;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {


        return super.onCreateView(inflater, container, savedInstanceState);

    }

    public ArrayList<CallSnapshotObject> getCallSnapshotObjects(ArrayList<String> bitmapFilePaths) {

        ArrayList<CallSnapshotObject> callSnapshotObjects = new ArrayList<>();

        Iterator<String> iterator = bitmapFilePaths.iterator();
        while (iterator.hasNext()) {
            String imagePath = iterator.next();
            File file = new File(imagePath);

            Bitmap bitmap = BitmapFactory.decodeFile(file.getAbsolutePath());
            String objectString = Helper.convertBitmapToString(bitmap);

            CallSnapshotObject callSnapshotObject = new CallSnapshotObject();
            callSnapshotObject.setObjectString(objectString);

            String generatedImageIdWithSuffix = file.getName();// bu image isimlerini arama isteğinde de göndermeyi unutma
            Log.d(Constants.LOG_TAG, "generatedImageIdWithSuffix=" + generatedImageIdWithSuffix);
            String imageId = generatedImageIdWithSuffix.split("\\.")[0];
            Log.d(Constants.LOG_TAG, "imageId=" + imageId);
            callSnapshotObject.setImageId(imageId);

            callSnapshotObjects.add(callSnapshotObject);
        }

        return callSnapshotObjects;

    }


    public ArrayList<String> getCallSnapshotIds(ArrayList<CallSnapshotObject> callSnapshotObjects) {

        ArrayList<String> arrayList = new ArrayList<>();

        Iterator<CallSnapshotObject> iterator = callSnapshotObjects.iterator();
        while (iterator.hasNext()) {
            CallSnapshotObject callSnapshotObject = iterator.next();

            String imageId = callSnapshotObject.getImageId();

            arrayList.add(imageId);

        }

        return arrayList;

    }

    public void processRFIDCode(String rfid, KeyPadService keyPadService) {

        Log.d(Constants.LOG_TAG, "fey hsn processRFIDCode kapıyı " + rfid + " değeri geldi");
//
      /* if (rfid.length() > 22) {
            if (keyPadService != null) {
                keyPadService.removeListener(BaseFragment.class.getName());
            }
//            rebootDevice();

           String text = getString(R.string.gecersiz);
           Helper.showTopMessageBanner((AppCompatActivity) getActivity(), text, false);
           return;
        }*/

// Intent intent = new Intent(ScreensaverActivity2.this, MainActivity.class);
//        startActivity(intent);

        DatabaseHelper databaseHelper = new DatabaseHelper(getActivity());
        DoorPassword doorPassword = databaseHelper.getPasswordByRFID(rfid);
        if (doorPassword == null) {
            Log.d("test","rf geçersiz");
            keyPadService.unlockDoorStateVoice("invalid_RF");
            String text = getString(R.string.gecersiz);
            Helper.showTopMessageBanner((AppCompatActivity) getActivity(), text, false);
//            getActivity().runOnUiThread(new Runnable() {
//                @Override
//                public void run() {
//                    new Handler().postDelayed(new Runnable() {
//                        @Override
//                        public void run() {
//                            try {
//                                Intent intent = new Intent(getActivity(), MainActivity.class);
//                                startActivity(intent);
////                   // getActivity().finish();
//                            } catch (Exception e) {
//
//                            }
//                        }
//                    }, 1500);
//                }
//            });
          //  Log.d(Constants.LOG_TAG, "processRFIDCode kapıyı " + doorPassword + " açtı");

            return;
        }


        if (!doorPassword.isActive()) {
            Intent intent = new Intent(this.getActivity(), MainActivity.class);
            startActivity(intent);
            String text = getString(R.string.sifre_aktif_degil);
            Helper.showTopMessageBanner((AppCompatActivity) getActivity(), text, false);
//            getActivity().runOnUiThread(new Runnable() {
//                @Override
//                public void run() {
//                    new Handler().postDelayed(new Runnable() {
//                        @Override
//                        public void run() {
//                            try {
//                                Intent intent = new Intent(getActivity(), MainActivity.class);
//                                startActivity(intent);
////                   // getActivity().finish();
//                            } catch (Exception e) {
//
//                            }
//                        }
//                    }, 1500);
//                }
//            });
            return;
        }

        if (rfid.length() <100) {
            Calendar calendar = Calendar.getInstance();
            SimpleDateFormat dateFormat = new SimpleDateFormat("dd.MM.yyyy HH:mm:ss");
            String formattedDate = dateFormat.format(calendar.getTime());
            Log.d(Constants.LOG_TAG, "fey processRFIDCode kapıyı   if (rfid.length() <100)= " + rfid + " açtı");
            DoorUnlockLog doorUnlockLog = new DoorUnlockLog();
            doorUnlockLog.setUnlockType(Constants.DOOR_UNLOCK_RFID);
            doorUnlockLog.setDatetime(formattedDate);
            doorUnlockLog.setRfid(doorPassword.getRfid());
            doorUnlockLog.setDoorPassword("");
            doorUnlockLog.setPasswordOwnerIP(doorPassword.getIp());
            doorUnlockLog.setPasswordLabel(doorPassword.getPasswordLabel());
            databaseHelper.insertDoorLog(doorUnlockLog);

            keyPadService.sendDoorUnlock(Constants.ENUM_DOOR_UNLOCK_MAIN);

            Daire daire = databaseHelper.getDaireByIP(doorPassword.getIp());

//            Intent intent = new Intent(getActivity(), MainActivity.class);
//            startActivity(intent);
//


            String text = getString(R.string.hosgeldiniz) + "  " + daire.getIsim() + " " + daire.getSoyisim() + " (" + doorPassword.getPasswordLabel() + ")";
            Helper.showTopMessageBanner((AppCompatActivity) getActivity(), text, true);

//            new Handler().postDelayed(new Runnable() {
//                @Override
//                public void run() {
////                Intent intent = new Intent(getActivity(), MainActivity.class);
////                    startActivity(intent);
//                   // getActivity().finish();
//                }
//            }, 3000);


//            getActivity().runOnUiThread(new Runnable() {
//                @Override
//                public void run() {
//                    new Handler().postDelayed(new Runnable() {
//                        @Override
//                        public void run() {
//                            try {
//                                Intent intent = new Intent(getActivity(), MainActivity.class);
//                                startActivity(intent);
////                   // getActivity().finish();
//                            } catch (Exception e) {
//
//                            }
//                        }
//                    }, 1500);
//                }
//            });
        }

    }

    private void rebootDevice() {
        try {
            Command command = new Command(0, "reboot");
            RootTools.getShell(true).add(command).getExitCode();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }


}

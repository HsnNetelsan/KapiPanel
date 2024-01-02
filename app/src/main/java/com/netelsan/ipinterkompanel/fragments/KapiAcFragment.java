package com.netelsan.ipinterkompanel.fragments;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.IBinder;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.netelsan.ipinterkompanel.Constants;
import com.netelsan.ipinterkompanel.Helper;
import com.netelsan.ipinterkompanel.R;
import com.netelsan.ipinterkompanel.activity.MainActivity;
import com.netelsan.ipinterkompanel.database.DatabaseHelper;
import com.netelsan.ipinterkompanel.face_detection.FaceDetector;
import com.netelsan.ipinterkompanel.listener.KeyPadListener;
import com.netelsan.ipinterkompanel.model.Daire;
import com.netelsan.ipinterkompanel.model.DoorPassword;
import com.netelsan.ipinterkompanel.model.DoorUnlockLog;
import com.netelsan.ipinterkompanel.model.ZilPanel;
import com.netelsan.ipinterkompanel.service.KeyPadService;

import java.io.DataInputStream;
import java.io.InputStream;
import java.net.InetAddress;
import java.net.Socket;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;

public class KapiAcFragment extends BaseFragment implements KeyPadListener, ServiceConnection {

    LinearLayout kapiAcText1Container;
    TextView kapiAcText1;

    LinearLayout kapiAcText2Container;
    TextView kapiAcText2;

    LinearLayout kapiAcText3Container;
    TextView kapiAcText3;

    LinearLayout kapiAcText4Container;
    TextView kapiAcText4;

    Boolean alreadyFalseSound = false;
    int selectedSifrePart = 1;// 1 = text1, 2 = text2, 3 = text3, 4 = text4

    TextView yuzTanimaText;

    public KapiAcFragment() {
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {

        View rootview = inflater.inflate(R.layout.fragment_kapi_ac, null);

        kapiAcText1Container = rootview.findViewById(R.id.kapiAcText1Container);
        kapiAcText1 = rootview.findViewById(R.id.kapiAcText1);
        kapiAcText2Container = rootview.findViewById(R.id.kapiAcText2Container);
        kapiAcText2 = rootview.findViewById(R.id.kapiAcText2);
        kapiAcText3Container = rootview.findViewById(R.id.kapiAcText3Container);
        kapiAcText3 = rootview.findViewById(R.id.kapiAcText3);
        kapiAcText4Container = rootview.findViewById(R.id.kapiAcText4Container);
        kapiAcText4 = rootview.findViewById(R.id.kapiAcText4);
        yuzTanimaText = rootview.findViewById(R.id.yuz_tanima_text);

        setLayouts();

        return rootview;
    }

    @Override
    public void onServiceConnected(ComponentName name, IBinder service) {
        keyPadService = ((KeyPadService.MyBinder) service).getService();
        keyPadService.addListener(KapiAcFragment.this, KapiAcFragment.class.getName());
    }

    @Override
    public void onServiceDisconnected(ComponentName name) {
        keyPadService = null;
    }

    private void setLayouts() {

        //PH6 backlight pini
//        getPINFromGPIO
//        Process process = getProcess();
//        setGPIO(process, Constants.GPIO_EXPORT, 145, true, ! isHigh);
//        setGPIO(process, Constants.GPIO_DIRECTION, 145, true, ! isHigh);
//        setGPIO(process, Constants.GPIO_VALUE, 145, true, ! isHigh);

    }

    @Override
    public void onResume() {
        super.onResume();

        yuzTanimaText.setVisibility(View.INVISIBLE);
        new checkServerTask().execute();

        Intent serviceConnIntent = new Intent(getActivity(), KeyPadService.class);
        getActivity().bindService(serviceConnIntent, this, Context.BIND_AUTO_CREATE);

        if (keyPadService != null) {
            keyPadService.addListener(KapiAcFragment.this, KapiAcFragment.class.getName());
        }
    }

    class checkServerTask extends AsyncTask<Void, Void, Void> {

        private Exception exception;

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
        }

        protected Void doInBackground(Void... args) {
            checkServer();
            return null;
        }

        protected void onPostExecute(Void result) {
        }
    }

    public void checkServer() {
        Socket socket;
        final int SERVERPORT = Constants.IP_FOR_FACE_DETECTOR_PORT_CONNECTION_CHECK;
        final String SERVER_IP = Constants.IP_FOR_FACE_DETECTOR;

        ArrayList<String> names = new ArrayList<>();
        try {
            InetAddress serverAddr = InetAddress.getByName(SERVER_IP);
            socket = new Socket(serverAddr, SERVERPORT);
            byte[] inArray = new byte[10];

            InputStream in = socket.getInputStream();
            DataInputStream din = new DataInputStream(in);
            din.read(inArray);

            if (inArray[0] == 83 || inArray[0] == 84) {
                getActivity().runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        yuzTanimaText.setVisibility(View.VISIBLE);
                    }
                });
//                yuzTanimaText.setVisibility(View.VISIBLE);
            }
//
            socket.close();
            Log.d(Constants.LOG_TAG, "inArray[0] : " + inArray[0]);

        } catch (java.net.UnknownHostException e) {
            e.printStackTrace();
        } catch (java.io.IOException e) {
            e.printStackTrace();
        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    @Override
    public void onPause() {
        super.onPause();
        if (keyPadService != null) {
            keyPadService.removeListener(KapiAcFragment.class.getName());
        }
    }


    @Override
    public void onStart() {
        super.onStart();

        SharedPreferences sharedPreferences = getActivity().getApplicationContext().getSharedPreferences(Constants.SHARED_PREFS, 0);
        boolean isEnabled = sharedPreferences.getBoolean(Constants.KEY_TUS_SESI, false);

        if (isEnabled) {
            Helper.setTusSesiEnabled(getActivity().getApplicationContext(), false);
        } else {
            alreadyFalseSound = true;
        }

    }

    @Override
    public void onDestroy() {
        super.onDestroy();

        if (!alreadyFalseSound)
            Helper.setTusSesiEnabled(getActivity().getApplicationContext(), true);
    }

    @Override
    public void onKeyPressed(String keyCode) {

        getActivity().runOnUiThread(new Runnable() {
            @Override
            public void run() {

//                Log.d(Constants.LOG_TAG, "KapiAcFragment onKeyPressed keyCode=" + keyCode);

                if (keyCode.equals(Constants.KEYPAD_BACK)) {
                    bottomMenuBackClicked();
                } else if (keyCode.equals(Constants.KEYPAD_UP)) {
                    menuNavigationClicked(Constants.MENU_NAVIGATION_UP, true);
                } else if (keyCode.equals(Constants.KEYPAD_DOWN)) {
                    menuNavigationClicked(Constants.MENU_NAVIGATION_DOWN, true);
                } else if (keyCode.equals(Constants.KEYPAD_HOME)) {
                    bottomMenuOkayClicked();
                } else if (keyCode.equals(Constants.KEYPAD_CALL)) {

                } else if (keyCode.equals(Constants.KEYPAD_LOCK)) {
                    bottomLockClicked();
                } else {
                    bottomNumbersClicked(keyCode);
                }

            }
        });

    }

    @Override
    public void onRFIDDetected(String rfid) {
        processRFIDCode(rfid, keyPadService);
    }

    private void bottomLockClicked() {
        attempKeyUnlock();
    }

    private void bottomMenuOkayClicked() {

        attempKeyUnlock();
    }

    private void attempKeyUnlock() {

        if (!alreadyFalseSound)
            Helper.setTusSesiEnabled(getActivity().getApplicationContext(), true);

        boolean isAllPartsFilled = isAllPartsFilled();

        if (isAllPartsFilled) {

            boolean isPasswordCorrect = isPasswordCorrect();
            if (isPasswordCorrect) {

                if (!isPasswordActive()) {
                    Helper.showTopMessageBanner((AppCompatActivity) getActivity(), getString(R.string.sifre_aktif_degil), false);
                    return;
                }
                keyPadService.unlockDoorStateVoice("valid");
                keyPadService.sendDoorUnlock(Constants.ENUM_DOOR_UNLOCK_MAIN);
                showOnboardingScreen();
            } else {
                keyPadService.unlockDoorStateVoice("invalid");
                Helper.showTopMessageBanner((AppCompatActivity) getActivity(), getString(R.string.hatali_sifre), false);
                kapiAcText1.setTag("-1");
                kapiAcText2.setTag("-1");
                kapiAcText3.setTag("-1");
                kapiAcText4.setTag("-1");
                kapiAcText1.setText("-");
                kapiAcText2.setText("-");
                kapiAcText3.setText("-");
                kapiAcText4.setText("-");

                selectedSifrePart = 4;
                menuNavigationClicked(Constants.MENU_NAVIGATION_UP, false);
            }
        } else {
            if (isPartiallyFilled())
                Helper.showTopMessageBanner((AppCompatActivity) getActivity(), getString(R.string.sifre_alani_eksik), false);
            else {
                if (yuzTanimaText.getVisibility() == View.VISIBLE) {
                    Intent intent = new Intent(getActivity(), FaceDetector.class);
                    startActivity(intent);
                }
            }

//            Helper.showTopMessageBanner((AppCompatActivity) getActivity(), getString(R.string.sifre_alani_eksik), false);
        }
        Helper.setTusSesiEnabled(getActivity().getApplicationContext(), false);

    }

    private boolean isPartiallyFilled() {

        String userPassword = getPasswordString2();

        if (userPassword.equals("----")) {
            return false;
        }

        return true;

    }

    private String getPasswordString2() {

        if (kapiAcText1.getTag() == null && kapiAcText2.getTag() == null && kapiAcText3.getTag() == null && kapiAcText4.getTag() == null) {
            return "----";
        } else

            return "";
    }

    private void showWelcomeMessage(DoorPassword doorPassword) {

        String ownerIP = doorPassword.getIp();

        DatabaseHelper databaseHelper = new DatabaseHelper(getActivity());
        Daire daire = databaseHelper.getDaireByIP(ownerIP);

        String text = "";

        if (daire != null) {
            text = getString(R.string.hosgeldiniz) + "  " + daire.getIsim() + " " + daire.getSoyisim() + " (" + doorPassword.getPasswordLabel() + ")";
        } else {
            ZilPanel zilPanel = databaseHelper.getZilPanelByIP(ownerIP);
            if (zilPanel != null) {
                text = getString(R.string.hosgeldiniz) + "  " + zilPanel.getDeviceName() + " (" + doorPassword.getPasswordLabel() + ")";
            }
        }

        Helper.showTopMessageBanner((AppCompatActivity) getActivity(), text, true);
    }

    private void showOnboardingScreen() {

        MainActivity mainActivity = (MainActivity) getActivity();
        mainActivity.displayView(Constants.SCREEN_ONBOARDING, null);

    }

    private boolean isPasswordCorrect() {

        String userPassword = getPasswordString();

        DatabaseHelper databaseHelper = new DatabaseHelper(getActivity());
        DoorPassword doorPassword = databaseHelper.getPasswordByDoorPassword(userPassword);
//        Log.d(Constants.LOG_TAG, "isPasswordCorrect doorPassword=" + doorPassword + " userPassword="  + userPassword);
        if (doorPassword == null) {
            return false;
        }
//        Log.d(Constants.LOG_TAG, "doorPassword" + doorPassword.getIp());
        //burada giriş yapanların kaydını tut
//        Log.d(Constants.LOG_TAG, "şifre ile kapıyı " + doorPassword.getPasswordLabel() + " açtı");

        showWelcomeMessage(doorPassword);
        return true;

    }

    private boolean isPasswordActive() {

        String userPassword = getPasswordString();

        DatabaseHelper databaseHelper = new DatabaseHelper(getActivity());
        DoorPassword doorPassword = databaseHelper.getPasswordByDoorPassword(userPassword);

        boolean isActive = doorPassword.isActive();
        if (isActive) {
            Calendar calendar = Calendar.getInstance();
            SimpleDateFormat dateFormat = new SimpleDateFormat("dd.MM.yyyy HH:mm:ss");
            String formattedDate = dateFormat.format(calendar.getTime());

            DoorUnlockLog doorUnlockLog = new DoorUnlockLog();
            doorUnlockLog.setUnlockType(Constants.DOOR_UNLOCK_PASSWORD);
            doorUnlockLog.setDatetime(formattedDate);
            doorUnlockLog.setRfid("");
            doorUnlockLog.setDoorPassword(userPassword);
            doorUnlockLog.setPasswordLabel(doorPassword.getPasswordLabel());

            doorUnlockLog.setPasswordOwnerIP(doorPassword.getIp());
            databaseHelper.insertDoorLog(doorUnlockLog);
        }

        return isActive;

    }

    private String getPasswordString() {

        if (kapiAcText1.getTag() == null || kapiAcText2.getTag() == null || kapiAcText3.getTag() == null || kapiAcText4.getTag() == null) {
            return "----";
        }

        String text1 = kapiAcText1.getTag().toString();
        String text2 = kapiAcText2.getTag().toString();
        String text3 = kapiAcText3.getTag().toString();
        String text4 = kapiAcText4.getTag().toString();

        String userPassword = text1 + text2 + text3 + text4;

        return userPassword;
    }

    private boolean isAllPartsFilled() {

        String userPassword = getPasswordString();
        if (userPassword.contains("-")) {
            return false;
        }

        return true;

    }

    private void bottomNumbersClicked(String keycode) {

        if (selectedSifrePart == 1) {
            kapiAcText1.setText("*");
            kapiAcText1.setTag(keycode);
            menuNavigationClicked(Constants.MENU_NAVIGATION_UP, false);
        } else if (selectedSifrePart == 2) {
            kapiAcText2.setText("*");
            kapiAcText2.setTag(keycode);
            menuNavigationClicked(Constants.MENU_NAVIGATION_UP, false);
        } else if (selectedSifrePart == 3) {
            kapiAcText3.setText("*");
            kapiAcText3.setTag(keycode);
            menuNavigationClicked(Constants.MENU_NAVIGATION_UP, false);
        } else if (selectedSifrePart == 4) {
            kapiAcText4.setText("*");
            kapiAcText4.setTag(keycode);
            boolean isAllPartsFilled = isAllPartsFilled();
            if (isAllPartsFilled) {
                attempKeyUnlock();
            }
        }

    }

    private void menuNavigationClicked(int direction, boolean isFromNumberButton) {

        if (direction == Constants.MENU_NAVIGATION_UP) {

            if (isFromNumberButton) {
                if (selectedSifrePart == 4) {
                    selectedSifrePart = 1;
                } else {
                    selectedSifrePart = selectedSifrePart + 1;
                }
            } else {
                if (selectedSifrePart == 4) {
                    String sifreText1 = kapiAcText1.getText().toString();
                    String sifreText2 = kapiAcText2.getText().toString();
                    String sifreText3 = kapiAcText3.getText().toString();

                    if (sifreText1.equals("-")) {
                        selectedSifrePart = 1;
                    } else if (sifreText2.equals("-")) {
                        selectedSifrePart = 2;
                    } else if (sifreText3.equals("-")) {
                        selectedSifrePart = 3;
                    }
                } else {
                    selectedSifrePart = selectedSifrePart + 1;
                }
            }

        } else {

            if (selectedSifrePart == 1) {
                selectedSifrePart = 4;
            } else {
                selectedSifrePart = selectedSifrePart - 1;
            }

        }

        kapiAcText1Container.setBackgroundResource(0);
        kapiAcText1.setTextColor(getResources().getColor(R.color.white));
        kapiAcText2Container.setBackgroundResource(0);
        kapiAcText2.setTextColor(getResources().getColor(R.color.white));
        kapiAcText3Container.setBackgroundResource(0);
        kapiAcText3.setTextColor(getResources().getColor(R.color.white));
        kapiAcText4Container.setBackgroundResource(0);
        kapiAcText4.setTextColor(getResources().getColor(R.color.white));

        if (selectedSifrePart == 1) {
            kapiAcText1Container.setBackgroundResource(R.drawable.black_bordered_btn_bg);
            kapiAcText1.setTextColor(getResources().getColor(R.color.black));
        } else if (selectedSifrePart == 2) {
            kapiAcText2Container.setBackgroundResource(R.drawable.black_bordered_btn_bg);
            kapiAcText2.setTextColor(getResources().getColor(R.color.black));
        } else if (selectedSifrePart == 3) {
            kapiAcText3Container.setBackgroundResource(R.drawable.black_bordered_btn_bg);
            kapiAcText3.setTextColor(getResources().getColor(R.color.black));
        } else if (selectedSifrePart == 4) {
            kapiAcText4Container.setBackgroundResource(R.drawable.black_bordered_btn_bg);
            kapiAcText4.setTextColor(getResources().getColor(R.color.black));
        }

    }

    private void bottomMenuBackClicked() {
        MainActivity mainActivity = (MainActivity) getActivity();
        mainActivity.displayView(Constants.SCREEN_IDLE_MENU, null);
    }

}

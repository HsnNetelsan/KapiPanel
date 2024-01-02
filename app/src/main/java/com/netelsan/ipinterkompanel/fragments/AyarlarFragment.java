package com.netelsan.ipinterkompanel.fragments;

import android.app.AlertDialog;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
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
import com.netelsan.ipinterkompanel.listener.KeyPadListener;
import com.netelsan.ipinterkompanel.model.ZilPanel;
import com.netelsan.ipinterkompanel.model.ZilPanelSite;
import com.netelsan.ipinterkompanel.service.KeyPadService;
import com.netelsan.ipinterkompanel.tcp.Utils;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Hashtable;

public class AyarlarFragment extends BaseFragment implements KeyPadListener, ServiceConnection {

    LinearLayout ayarlarYoneticiContainer;
    TextView ayarlarYoneticiItemText;

    LinearLayout ayarlarTeknikPersonelContainer;
    TextView ayarlarTeknikPersonelText;

    LinearLayout ayarlarDoorLogsContainer;
    TextView ayarlarDoorLogsText;

    int selectedMenuId = Constants.AYARLAR_MENU_YONETICI;

    public AyarlarFragment() {
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {

        View rootview = inflater.inflate(R.layout.fragment_ayarlar, null);

        ayarlarYoneticiContainer = rootview.findViewById(R.id.ayarlarYoneticiContainer);
        ayarlarYoneticiItemText = rootview.findViewById(R.id.ayarlarYoneticiItemText);

        ayarlarTeknikPersonelContainer = rootview.findViewById(R.id.ayarlarTeknikPersonelContainer);
        ayarlarTeknikPersonelText = rootview.findViewById(R.id.ayarlarTeknikPersonelText);

        ayarlarDoorLogsContainer = rootview.findViewById(R.id.ayarlarDoorLogsContainer);
        ayarlarDoorLogsText = rootview.findViewById(R.id.ayarlarDoorLogsText);

        setLayouts(rootview);

        return rootview;
    }

    @Override
    public void onServiceConnected(ComponentName name, IBinder service) {
        keyPadService = ((KeyPadService.MyBinder) service).getService();
        keyPadService.addListener(AyarlarFragment.this, AyarlarFragment.class.getName());
    }

    @Override
    public void onServiceDisconnected(ComponentName name) {
        keyPadService = null;
    }

    private void setLayouts(View rootview) {
        TextView ayarlarAppVersion = rootview.findViewById(R.id.ayarlarAppVersion);

        try {
            PackageInfo pInfo = getActivity().getPackageManager().getPackageInfo(getActivity().getPackageName(), 0);
            String version = pInfo.versionName;
            int verCode = pInfo.versionCode;

            ayarlarAppVersion.setText("v" + verCode + " (" + version + ")");
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        }

    }

    @Override
    public void onResume() {
        super.onResume();

        Intent serviceConnIntent = new Intent(getActivity(), KeyPadService.class);
        getActivity().bindService(serviceConnIntent, this, Context.BIND_AUTO_CREATE);

        if (keyPadService != null) {
            keyPadService.addListener(AyarlarFragment.this, AyarlarFragment.class.getName());
        }
    }

    @Override
    public void onPause() {
        super.onPause();
        if (sifreDialog != null) {
            sifreDialog.dismiss();
            sifreDialog = null;
        }
        if (keyPadService != null) {
            keyPadService.removeListener(AyarlarFragment.class.getName());
        }
    }

    @Override
    public void onKeyPressed(String keyCode) {

        Log.d(Constants.LOG_TAG, "AyarlarFragment onKeyPressed 1 keyCode=" + keyCode);
        if (!isAdded()) {
            return;
        }

        getActivity().runOnUiThread(new Runnable() {
            @Override
            public void run() {

                Log.d(Constants.LOG_TAG, "AyarlarFragment onKeyPressed 2 keyCode=" + keyCode);

                if (keyCode.equals(Constants.KEYPAD_BACK)) {
                    bottomMenuBackClicked();
                } else if (keyCode.equals(Constants.KEYPAD_UP)) {
                    menuNavigationClicked(Constants.MENU_NAVIGATION_UP);
                } else if (keyCode.equals(Constants.KEYPAD_DOWN)) {
                    menuNavigationClicked(Constants.MENU_NAVIGATION_DOWN);
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
        MainActivity mainActivity = (MainActivity) getActivity();
        mainActivity.displayView(Constants.SCREEN_KAPI_AC, null);
    }

    private void bottomNumbersClicked(String keycode) {

        if (isAdminDialogRunning) {
            if (selectedSifrePart == 1) {
                sifreDialogText1.setText("*");
                sifreDialogText1.setTag(keycode);
            } else if (selectedSifrePart == 2) {
                sifreDialogText2.setText("*");
                sifreDialogText2.setTag(keycode);
            } else if (selectedSifrePart == 3) {
                sifreDialogText3.setText("*");
                sifreDialogText3.setTag(keycode);
            } else if (selectedSifrePart == 4) {
                sifreDialogText4.setText("*");
                sifreDialogText4.setTag(keycode);
            } else if (selectedSifrePart == 5) {
                sifreDialogText5.setText("*");
                sifreDialogText5.setTag(keycode);
            } else if (selectedSifrePart == 6) {
                sifreDialogText6.setText("*");
                sifreDialogText6.setTag(keycode);
            }
            sifreDialogNavigate(Constants.MENU_NAVIGATION_UP, false);
            return;
        }
    }

    boolean isAdminDialogRunning = false;

    private void bottomMenuOkayClicked() {

        if (selectedMenuId == Constants.AYARLAR_MENU_YONETICI) {
            if (isAdminDialogRunning) {
                String userPasswordText = getUserPasswordText();
                if (userPasswordText.length() != 6) {
                    Helper.showTopMessageBanner((AppCompatActivity) getActivity(), getString(R.string.sifre_yanlis_veya_eksik), false);
                    return;
                }
                attemptLogin(true, userPasswordText);
            } else {
                showPasswordDialog(true);
            }
        } else if (selectedMenuId == Constants.AYARLAR_MENU_TEKNIK_PERSONEL) {

            boolean isTeknikPersonelSifreEnabled = Helper.isTeknikPersonelSifreEnabled(getActivity());
            if (!isTeknikPersonelSifreEnabled) {
                Helper.setWrongAttemptCount(getActivity(), 0);
                Helper.setWrongAttemptTimeMillis(getActivity(), 0);
                showSuitableScreen(false);
                return;
            }
            if (isAdminDialogRunning) {
                String userPasswordText = getUserPasswordText();
                if (userPasswordText.length() != 6) {
                    Helper.showTopMessageBanner((AppCompatActivity) getActivity(), getString(R.string.sifre_yanlis_veya_eksik), false);
                    return;
                }
                attemptLogin(false, userPasswordText);
            } else {
                showPasswordDialog(false);
            }
        } else if (selectedMenuId == Constants.AYARLAR_MENU_DOOR_LOGS) {
            if (isAdminDialogRunning) {
                String userPasswordText = getUserPasswordText();
                if (userPasswordText.length() != 6) {
                    Helper.showTopMessageBanner((AppCompatActivity) getActivity(), getString(R.string.sifre_yanlis_veya_eksik), false);
                    return;
                }
                attemptLogin(true, userPasswordText);
            } else {
                showPasswordDialog(true);
            }
        }

    }

    private void attemptLogin(boolean isYonetici, String userPasswordText) {

        int totalWrongAttempt = Helper.getWrongAttemptCount(getActivity());
        Log.d(Constants.LOG_TAG, "totalWrongAttempt= " + totalWrongAttempt);
        if (totalWrongAttempt > 2) {

            long wrongAttempTimeMillis = Helper.getWrongAttemptTimeMillis(getActivity());
            long now = System.currentTimeMillis();
            long diff = now - wrongAttempTimeMillis;
            Log.d(Constants.LOG_TAG, "diff= " + diff);
            if (diff < (1000 * 60 * 5)) {
                Helper.showTopMessageBanner((AppCompatActivity) getActivity(), getString(R.string.hatali_sifre_hakkiniz_doldu), false);
                if (isAdminDialogRunning) {
                    resetSifreDialogTexts();
                }
                return;
            } else {
                totalWrongAttempt = 0;
                Helper.setWrongAttemptCount(getActivity(), 0);
                Helper.setWrongAttemptTimeMillis(getActivity(), 0);
            }

        }

        boolean isPasswordOkay = isPasswordOkay(isYonetici, userPasswordText);

        if (isPasswordOkay) {
            Helper.setWrongAttemptCount(getActivity(), 0);
            Helper.setWrongAttemptTimeMillis(getActivity(), 0);
            sifreDialog.dismiss();
            showSuitableScreen(isYonetici);
        } else {
            Helper.showTopMessageBanner((AppCompatActivity) getActivity(), getString(R.string.sifre_yanlis) + " " + (2 - totalWrongAttempt) + " " + getString(R.string.deneme_hakkiniz_kaldi), false);
            if (isAdminDialogRunning) {
                resetSifreDialogTexts();
            }
            Helper.setWrongAttemptTimeMillis(getActivity(), System.currentTimeMillis());
            Helper.setWrongAttemptCount(getActivity(), totalWrongAttempt + 1);
        }

    }

    private void resetSifreDialogTexts() {

        selectedSifrePart = 1;

        sifreDialogText1Container.setBackgroundResource(0);
        sifreDialogText2Container.setBackgroundResource(0);
        sifreDialogText3Container.setBackgroundResource(0);
        sifreDialogText4Container.setBackgroundResource(0);
        sifreDialogText5Container.setBackgroundResource(0);
        sifreDialogText6Container.setBackgroundResource(0);

        if (selectedSifrePart == 1) {
            sifreDialogText1Container.setBackgroundResource(R.drawable.black_bordered_btn_bg);
        } else if (selectedSifrePart == 2) {
            sifreDialogText2Container.setBackgroundResource(R.drawable.black_bordered_btn_bg);
        } else if (selectedSifrePart == 3) {
            sifreDialogText3Container.setBackgroundResource(R.drawable.black_bordered_btn_bg);
        } else if (selectedSifrePart == 4) {
            sifreDialogText4Container.setBackgroundResource(R.drawable.black_bordered_btn_bg);
        } else if (selectedSifrePart == 5) {
            sifreDialogText5Container.setBackgroundResource(R.drawable.black_bordered_btn_bg);
        } else if (selectedSifrePart == 6) {
            sifreDialogText6Container.setBackgroundResource(R.drawable.black_bordered_btn_bg);
        }

        sifreDialogText1.setText("-");
        sifreDialogText1.setTag(null);

        sifreDialogText2.setText("-");
        sifreDialogText2.setTag(null);

        sifreDialogText3.setText("-");
        sifreDialogText3.setTag(null);

        sifreDialogText4.setText("-");
        sifreDialogText4.setTag(null);

        sifreDialogText5.setText("-");
        sifreDialogText5.setTag(null);

        sifreDialogText6.setText("-");
        sifreDialogText6.setTag(null);

    }

    private void showSuitableScreen(boolean isYonetici) {

        MainActivity mainActivity = (MainActivity) getActivity();
        if (isYonetici) {
            if (selectedMenuId == Constants.AYARLAR_MENU_YONETICI) {
                mainActivity.displayView(Constants.SCREEN_AYARLAR_YONETICI, null);
            } else {
                mainActivity.displayView(Constants.SCREEN_AYARLAR_DOOR_LOGS, null);
            }
        } else {
            mainActivity.displayView(Constants.SCREEN_AYARLAR_TEKNIK_PERSONEL, null);
        }

    }

    private boolean isPasswordOkay(boolean isYonetici, String userPasswordText) {

        String controlText = "";
        if (isYonetici) {
            controlText = getYoneticiPassword();
        } else {
            controlText = getTeknikPersonelPassword();
        }

        if (userPasswordText.equals(controlText)) {
            return true;
        }

        return false;
    }

    private String getTeknikPersonelPassword() {

        Hashtable<Integer, String> hashtable = new Hashtable<>();

        DatabaseHelper databaseHelper = new DatabaseHelper(getActivity());

        int blokNo = 0;
        int kapiNo = 0;

        boolean isForSite = Helper.isZilForSite(getActivity());
        if (isForSite) {
            ZilPanelSite zilPanelSite = databaseHelper.getSiteZilPanelByIP(Utils.getIPAddress(true));
            blokNo = 0;
            kapiNo = zilPanelSite.getKapiNo();
        } else {
            ZilPanel zilPanel = databaseHelper.getZilPanelByIP(Utils.getIPAddress(true));
            blokNo = zilPanel.getBlok();
            kapiNo = zilPanel.getKapiNo();
        }

        Calendar calendar = Calendar.getInstance();

        SimpleDateFormat dateFormat = new SimpleDateFormat("ddMMyy");
        String formattedDate = dateFormat.format(calendar.getTime());

        int counter = 0;
        for (char ch : formattedDate.toCharArray()) {
            hashtable.put(counter, ch + "");

            counter = counter + 1;
        }

        String generatedPassword = hashtable.get(4) + "" + hashtable.get(0) + "" + hashtable.get(2) + "" + hashtable.get(1) + "" + hashtable.get(5) + hashtable.get(3);

        int generatedPasswordInt = Integer.parseInt(generatedPassword);
        generatedPasswordInt = generatedPasswordInt + blokNo + kapiNo;

        generatedPassword = generatedPasswordInt + "";
//        Log.d(Constants.LOG_TAG, "getTeknikPersonelPassword generatedPassword=" + generatedPassword);

        return generatedPassword;
    }


    private String getYoneticiPassword() {

        Hashtable<Integer, String> hashtable = new Hashtable<>();

        DatabaseHelper databaseHelper = new DatabaseHelper(getActivity());

        int blokNo = 0;
        int kapiNo = 0;

        boolean isForSite = Helper.isZilForSite(getActivity());
        if (isForSite) {
            ZilPanelSite zilPanelSite = databaseHelper.getSiteZilPanelByIP(Utils.getIPAddress(true));
            blokNo = 0;
            kapiNo = zilPanelSite.getKapiNo();
        } else {
            ZilPanel zilPanel = databaseHelper.getZilPanelByIP(Utils.getIPAddress(true));
            blokNo = zilPanel.getBlok();
            kapiNo = zilPanel.getKapiNo();
        }

        Calendar calendar = Calendar.getInstance();

        SimpleDateFormat dateFormat = new SimpleDateFormat("ddMMyy");
        String formattedDate = dateFormat.format(calendar.getTime());

        int counter = 0;
        for (char ch : formattedDate.toCharArray()) {
            hashtable.put(counter, ch + "");

            counter = counter + 1;
        }

        String generatedPassword = hashtable.get(0) + "" + hashtable.get(2) + "" + hashtable.get(1) + "" + hashtable.get(4) + "" + hashtable.get(3) + hashtable.get(5);

        int generatedPasswordInt = Integer.parseInt(generatedPassword);
        generatedPasswordInt = generatedPasswordInt + blokNo + kapiNo;

        generatedPassword = generatedPasswordInt + "";

        while (true) {
            int totalLength = generatedPassword.length();
            if (totalLength < 6) {
                generatedPassword = "0" + generatedPassword;
            } else {
                break;
            }
        }

        return generatedPassword;
    }

    private String getUserPasswordText() {

        String userText = "";

        if (sifreDialogText1.getTag() != null) {
            userText = userText + sifreDialogText1.getTag().toString();
        }

        if (sifreDialogText2.getTag() != null) {
            userText = userText + sifreDialogText2.getTag().toString();
        }

        if (sifreDialogText3.getTag() != null) {
            userText = userText + sifreDialogText3.getTag().toString();
        }

        if (sifreDialogText4.getTag() != null) {
            userText = userText + sifreDialogText4.getTag().toString();
        }

        if (sifreDialogText5.getTag() != null) {
            userText = userText + sifreDialogText5.getTag().toString();
        }

        if (sifreDialogText6.getTag() != null) {
            userText = userText + sifreDialogText6.getTag().toString();
        }

        return userText;
    }

    AlertDialog sifreDialog;
    int selectedSifrePart = 1;// 1 = text1, 2 = text2, 3 = text3, 4 = text4, 5 = text5, 6 = text6
    LinearLayout sifreDialogText1Container;
    LinearLayout sifreDialogText2Container;
    LinearLayout sifreDialogText3Container;
    LinearLayout sifreDialogText4Container;
    LinearLayout sifreDialogText5Container;
    LinearLayout sifreDialogText6Container;
    TextView sifreDialogText1;
    TextView sifreDialogText2;
    TextView sifreDialogText3;
    TextView sifreDialogText4;
    TextView sifreDialogText5;
    TextView sifreDialogText6;

    private void showPasswordDialog(boolean isYonetici) {
        Boolean preTusVolume = Helper.isTusSesiEnabled(getActivity().getApplicationContext());

        if (preTusVolume)
            Helper.setTusSesiEnabled(getActivity().getApplicationContext(), false);
        selectedSifrePart = 1;

        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setTitle("");

        View customLayout = getLayoutInflater().inflate(R.layout.custom_password_6_dialog, null);
        builder.setView(customLayout);

        TextView customPasswordTitle = customLayout.findViewById(R.id.customPasswordTitle);
        customPasswordTitle.setText(getString(R.string.teknik_personel_sifresini_girin));
        if (isYonetici) {
            customPasswordTitle.setText(getString(R.string.yonetici_sifresini_giriniz));
        }

        sifreDialogText1Container = customLayout.findViewById(R.id.sifreDialogText1Container);
        sifreDialogText1 = customLayout.findViewById(R.id.sifreDialogText1);

        sifreDialogText2Container = customLayout.findViewById(R.id.sifreDialogText2Container);
        sifreDialogText2 = customLayout.findViewById(R.id.sifreDialogText2);

        sifreDialogText3Container = customLayout.findViewById(R.id.sifreDialogText3Container);
        sifreDialogText3 = customLayout.findViewById(R.id.sifreDialogText3);

        sifreDialogText4Container = customLayout.findViewById(R.id.sifreDialogText4Container);
        sifreDialogText4 = customLayout.findViewById(R.id.sifreDialogText4);

        sifreDialogText5Container = customLayout.findViewById(R.id.sifreDialogText5Container);
        sifreDialogText5 = customLayout.findViewById(R.id.sifreDialogText5);

        sifreDialogText6Container = customLayout.findViewById(R.id.sifreDialogText6Container);
        sifreDialogText6 = customLayout.findViewById(R.id.sifreDialogText6);

        builder.setOnDismissListener(new DialogInterface.OnDismissListener() {
            @Override
            public void onDismiss(DialogInterface dialog) {
                sifreDialog = null;
                isAdminDialogRunning = false;

                Helper.setTusSesiEnabled(getActivity().getApplicationContext(), preTusVolume);
            }
        });
        sifreDialog = builder.create();
        sifreDialog.show();
        isAdminDialogRunning = true;

    }

    private void sifreDialogNavigate(int direction, boolean isFromButton) {

        if (direction == Constants.MENU_NAVIGATION_UP) {

            if (isFromButton) {
                if (selectedSifrePart == 6) {
                    selectedSifrePart = 1;
                } else {
                    selectedSifrePart = selectedSifrePart + 1;
                }
            } else {
                if (selectedSifrePart == 6) {
                    String sifreText1 = sifreDialogText1.getText().toString();
                    String sifreText2 = sifreDialogText2.getText().toString();
                    String sifreText3 = sifreDialogText3.getText().toString();
                    String sifreText4 = sifreDialogText4.getText().toString();
                    String sifreText5 = sifreDialogText5.getText().toString();

                    if (sifreText1.equals("-")) {
                        selectedSifrePart = 1;
                    } else if (sifreText2.equals("-")) {
                        selectedSifrePart = 2;
                    } else if (sifreText3.equals("-")) {
                        selectedSifrePart = 3;
                    } else if (sifreText4.equals("-")) {
                        selectedSifrePart = 4;
                    } else if (sifreText5.equals("-")) {
                        selectedSifrePart = 5;
                    }
                } else {
                    selectedSifrePart = selectedSifrePart + 1;
                }
            }

        } else {

            if (selectedSifrePart == 1) {
                selectedSifrePart = 6;
            } else {
                selectedSifrePart = selectedSifrePart - 1;
            }

        }

        sifreDialogText1Container.setBackgroundResource(0);
        sifreDialogText2Container.setBackgroundResource(0);
        sifreDialogText3Container.setBackgroundResource(0);
        sifreDialogText4Container.setBackgroundResource(0);
        sifreDialogText5Container.setBackgroundResource(0);
        sifreDialogText6Container.setBackgroundResource(0);

        if (selectedSifrePart == 1) {
            sifreDialogText1Container.setBackgroundResource(R.drawable.black_bordered_btn_bg);
        } else if (selectedSifrePart == 2) {
            sifreDialogText2Container.setBackgroundResource(R.drawable.black_bordered_btn_bg);
        } else if (selectedSifrePart == 3) {
            sifreDialogText3Container.setBackgroundResource(R.drawable.black_bordered_btn_bg);
        } else if (selectedSifrePart == 4) {
            sifreDialogText4Container.setBackgroundResource(R.drawable.black_bordered_btn_bg);
        } else if (selectedSifrePart == 5) {
            sifreDialogText5Container.setBackgroundResource(R.drawable.black_bordered_btn_bg);
        } else if (selectedSifrePart == 6) {
            sifreDialogText6Container.setBackgroundResource(R.drawable.black_bordered_btn_bg);
        }

    }

    private void menuNavigationClicked(int direction) {

        if (isAdminDialogRunning) {
            sifreDialogNavigate(direction, true);
            return;
        }

        if (direction == Constants.MENU_NAVIGATION_UP) {

            if (selectedMenuId == Constants.AYARLAR_MENU_YONETICI) {
                selectedMenuId = Constants.AYARLAR_MENU_DOOR_LOGS;
            } else {
                selectedMenuId = selectedMenuId - 1;
            }

        } else if (direction == Constants.MENU_NAVIGATION_DOWN) {

            if (selectedMenuId == Constants.AYARLAR_MENU_DOOR_LOGS) {
                selectedMenuId = Constants.AYARLAR_MENU_YONETICI;
            } else {
                selectedMenuId = selectedMenuId + 1;
            }
        }

        selectMenu(selectedMenuId);

    }

    private void selectMenu(int menuId) {

        unselectAllMenus();

        if (menuId == Constants.AYARLAR_MENU_YONETICI) {
            ayarlarYoneticiContainer.setBackgroundResource(R.drawable.menu_item_selected);
            ayarlarYoneticiItemText.setTextColor(getResources().getColor(R.color.black));
        } else if (menuId == Constants.AYARLAR_MENU_TEKNIK_PERSONEL) {
            ayarlarTeknikPersonelContainer.setBackgroundResource(R.drawable.menu_item_selected);
            ayarlarTeknikPersonelText.setTextColor(getResources().getColor(R.color.black));
        } else if (menuId == Constants.AYARLAR_MENU_DOOR_LOGS) {
            ayarlarDoorLogsContainer.setBackgroundResource(R.drawable.menu_item_selected);
            ayarlarDoorLogsText.setTextColor(getResources().getColor(R.color.black));
        }

    }

    private void unselectAllMenus() {

        ayarlarYoneticiContainer.setBackgroundResource(R.drawable.menu_item_unselected);
        ayarlarYoneticiItemText.setTextColor(getResources().getColor(R.color.white));
        ayarlarTeknikPersonelContainer.setBackgroundResource(R.drawable.menu_item_unselected);
        ayarlarTeknikPersonelText.setTextColor(getResources().getColor(R.color.white));
        ayarlarDoorLogsContainer.setBackgroundResource(R.drawable.menu_item_unselected);
        ayarlarDoorLogsText.setTextColor(getResources().getColor(R.color.white));

    }

    private void bottomMenuBackClicked() {

        if (isAdminDialogRunning) {
            if (sifreDialog != null) {
                sifreDialog.dismiss();
                sifreDialog = null;
            }
            return;
        }

        MainActivity mainActivity = (MainActivity) getActivity();
        mainActivity.displayView(Constants.SCREEN_IDLE_MENU, null);
    }

}

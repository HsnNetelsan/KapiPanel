package com.netelsan.ipinterkompanel.fragments.settings;

import android.app.AlertDialog;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.graphics.Point;
import android.media.AudioManager;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.IBinder;
import android.util.Log;
import android.view.Display;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.netelsan.ipinterkompanel.Constants;
import com.netelsan.ipinterkompanel.Helper;
import com.netelsan.ipinterkompanel.R;
import com.netelsan.ipinterkompanel.activity.MainActivity;
import com.netelsan.ipinterkompanel.adapter.ApkListAdapter;
import com.netelsan.ipinterkompanel.adapter.SelectDaireAdapter;
import com.netelsan.ipinterkompanel.adapter.SelectPasswordAdapter;
import com.netelsan.ipinterkompanel.database.DatabaseHelper;
import com.netelsan.ipinterkompanel.fragments.BaseFragment;
import com.netelsan.ipinterkompanel.listener.KeyPadListener;
import com.netelsan.ipinterkompanel.model.APKListItem;
import com.netelsan.ipinterkompanel.model.ComPackageModel;
import com.netelsan.ipinterkompanel.model.Daire;
import com.netelsan.ipinterkompanel.model.DoorPassword;
import com.netelsan.ipinterkompanel.model.Guvenlik;
import com.netelsan.ipinterkompanel.model.ZilPanel;
import com.netelsan.ipinterkompanel.model.ZilPanelSite;
import com.netelsan.ipinterkompanel.service.KeyPadService;
import com.netelsan.ipinterkompanel.tcp.TCPHelper;
import com.netelsan.ipinterkompanel.tcp.Utils;
import com.netelsan.ipinterkompanel.webrtc.AppRTCAudioManager;
import com.stericson.RootShell.execution.Command;
import com.stericson.RootTools.RootTools;

import java.io.File;
import java.util.ArrayList;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

import static android.content.Context.AUDIO_SERVICE;

public class AyarlarYoneticiFragment extends BaseFragment implements KeyPadListener, ServiceConnection {

    LinearLayout yoneticiLanguageContainer;
    TextView yoneticiLanguageText;

    LinearLayout yoneticiChangeNameContainer;
    TextView yoneticiChangeNameText;

    LinearLayout yoneticiChangeOnboardingNameContainer;
    TextView yoneticiChangeOnboardingNameText;

    LinearLayout yoneticiVolumeContainer;
    TextView yoneticiVolumeNameText;
    LinearLayout yoneticiKonusmaVolumeContainer;
    TextView yoneticiKonusmaVolumeNameText;

    LinearLayout yoneticiAddPasswordContainer;
    TextView yoneticiAddPasswordText;

    LinearLayout yoneticiRemovePasswordContainer;
    TextView yoneticiRemovePasswordText;

    LinearLayout yoneticiAddRFIDContainer;
    TextView yoneticiAddRFIDText;

    LinearLayout yoneticiRemoveRFIDContainer;
    TextView yoneticiRemoveRFIDText;

    LinearLayout yoneticiRemoveRFIDViaTouchContainer;
    TextView yoneticiRemoveRFIDViaTouchText;

//    LinearLayout yoneticiVideoKayitContainer;
//    TextView yoneticiVideoKayitText;
//    ImageView yoneticiVideoKayitImage;

    LinearLayout yoneticiTusSesiContainer;
    TextView yoneticiTusSesiText;
    ImageView yoneticiTusSesiImage;

    ScrollView ayarlarScrollView;

    int selectedMenuId = Constants.YONETICI_MENU_LANGUAGE;

    public AyarlarYoneticiFragment() {
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View rootview = inflater.inflate(R.layout.fragment_ayarlar_yonetici, null);

        yoneticiLanguageContainer = rootview.findViewById(R.id.yoneticiLanguageContainer);
        yoneticiLanguageText = rootview.findViewById(R.id.yoneticiLanguageText);

        yoneticiChangeNameContainer = rootview.findViewById(R.id.yoneticiChangeNameContainer);
        yoneticiChangeNameText = rootview.findViewById(R.id.yoneticiChangeNameText);

        yoneticiChangeOnboardingNameContainer = rootview.findViewById(R.id.yoneticiChangeOnboardingNameContainer);
        yoneticiChangeOnboardingNameText = rootview.findViewById(R.id.yoneticiChangeOnboardingNameText);

        yoneticiVolumeContainer = rootview.findViewById(R.id.yoneticiVolumeContainer);
        yoneticiVolumeNameText = rootview.findViewById(R.id.yoneticiVolumeText);

        yoneticiKonusmaVolumeContainer = rootview.findViewById(R.id.yoneticiKonusmaVolumeContainer);
        yoneticiKonusmaVolumeNameText = rootview.findViewById(R.id.yoneticiKonusmaVolumeNameText);

        yoneticiAddPasswordContainer = rootview.findViewById(R.id.yoneticiAddPasswordContainer);
        yoneticiAddPasswordText = rootview.findViewById(R.id.yoneticiAddPasswordText);

        yoneticiRemovePasswordContainer = rootview.findViewById(R.id.yoneticiRemovePasswordContainer);
        yoneticiRemovePasswordText = rootview.findViewById(R.id.yoneticiRemovePasswordText);

        yoneticiAddRFIDContainer = rootview.findViewById(R.id.yoneticiAddRFIDContainer);
        yoneticiAddRFIDText = rootview.findViewById(R.id.yoneticiAddRFIDText);

        yoneticiRemoveRFIDContainer = rootview.findViewById(R.id.yoneticiRemoveRFIDContainer);
        yoneticiRemoveRFIDText = rootview.findViewById(R.id.yoneticiRemoveRFIDText);

        yoneticiRemoveRFIDViaTouchContainer = rootview.findViewById(R.id.yoneticiRemoveRFIDViaTouchContainer);
        yoneticiRemoveRFIDViaTouchText = rootview.findViewById(R.id.yoneticiRemoveRFIDViaTouchText);

//        yoneticiVideoKayitContainer = rootview.findViewById(R.id.yoneticiVideoKayitContainer);
//        yoneticiVideoKayitText = rootview.findViewById(R.id.yoneticiVideoKayitText);
//        yoneticiVideoKayitImage = rootview.findViewById(R.id.yoneticiVideoKayitImage);

        yoneticiTusSesiContainer = rootview.findViewById(R.id.yoneticiTusSesiContainer);
        yoneticiTusSesiText = rootview.findViewById(R.id.yoneticiTusSesiText);
        yoneticiTusSesiImage = rootview.findViewById(R.id.yoneticiTusSesiImage);

        ayarlarScrollView = rootview.findViewById(R.id.ayarlarScrollView);

        setLayoutVisibilities();

        setLayouts();

        return rootview;
    }

    private void setLayoutVisibilities() {

        boolean isZilForSite = Helper.isZilForSite(getActivity());
        if (isZilForSite) {
            yoneticiAddPasswordContainer.setVisibility(View.GONE);
            yoneticiRemovePasswordContainer.setVisibility(View.GONE);
            yoneticiAddRFIDContainer.setVisibility(View.GONE);
            yoneticiRemoveRFIDContainer.setVisibility(View.GONE);
            yoneticiRemoveRFIDViaTouchContainer.setVisibility(View.GONE);
//            yoneticiVideoKayitContainer.setVisibility(View.GONE);
        } else {
            yoneticiAddPasswordContainer.setVisibility(View.VISIBLE);
            yoneticiRemovePasswordContainer.setVisibility(View.VISIBLE);
            yoneticiAddRFIDContainer.setVisibility(View.VISIBLE);
            yoneticiRemoveRFIDContainer.setVisibility(View.VISIBLE);
            yoneticiRemoveRFIDViaTouchContainer.setVisibility(View.VISIBLE);
//            yoneticiVideoKayitContainer.setVisibility(View.VISIBLE);
        }

    }

    private void setLayouts() {

//        if (selectedMenuId == Constants.YONETICI_MENU_VIDEO_KAYIT) {
//            if (Helper.isCallVideoRecordEnabled(getActivity())) {
//                yoneticiVideoKayitImage.setImageResource(R.drawable.icon_checkbox_checked_black);
//            } else {
//                yoneticiVideoKayitImage.setImageResource(R.drawable.icon_checkbox_unchecked_black);
//            }
//        } else
        if (selectedMenuId == Constants.YONETICI_MENU_TUS_SESI) {

            if (Helper.isTusSesiEnabled(getActivity())) {
                yoneticiTusSesiImage.setImageResource(R.drawable.icon_checkbox_checked_black);
            } else {
                yoneticiTusSesiImage.setImageResource(R.drawable.icon_checkbox_unchecked_black);
            }
        } else {

//            if (Helper.isCallVideoRecordEnabled(getActivity())) {
//                yoneticiVideoKayitImage.setImageResource(R.drawable.icon_checkbox_checked);
//            } else {
//                yoneticiVideoKayitImage.setImageResource(R.drawable.icon_checkbox_unchecked);
//            }

            if (Helper.isTusSesiEnabled(getActivity())) {
                yoneticiTusSesiImage.setImageResource(R.drawable.icon_checkbox_checked);
            } else {
                yoneticiTusSesiImage.setImageResource(R.drawable.icon_checkbox_unchecked);
            }

        }


    }

    @Override
    public void onServiceConnected(ComponentName name, IBinder service) {
        keyPadService = ((KeyPadService.MyBinder) service).getService();
        keyPadService.addListener(AyarlarYoneticiFragment.this, AyarlarYoneticiFragment.class.getName());
    }

    @Override
    public void onServiceDisconnected(ComponentName name) {
        keyPadService = null;
    }

    @Override
    public void onResume() {
        super.onResume();

        Intent serviceConnIntent = new Intent(getActivity(), KeyPadService.class);
        getActivity().bindService(serviceConnIntent, this, Context.BIND_AUTO_CREATE);

        if (keyPadService != null) {
            keyPadService.addListener(AyarlarYoneticiFragment.this, AyarlarYoneticiFragment.class.getName());
        }
    }

    @Override
    public void onPause() {
        super.onPause();

        if (selectLanguageDialog != null) {
            selectLanguageDialog.dismiss();
            selectLanguageDialog = null;
        }

        if (selectVolumeDialog != null) {
            selectVolumeDialog.dismiss();
            selectVolumeDialog = null;
        }

        if (selectKonusmaVolumeDialog != null) {
            selectKonusmaVolumeDialog.dismiss();
            selectKonusmaVolumeDialog = null;
        }

        if (selectDaireDialog != null) {
            selectDaireDialog.dismiss();
            selectDaireDialog = null;
        }

        if (sifreDialog != null) {
            sifreDialog.dismiss();
            sifreDialog = null;
            return;
        }

        if (selectPassworDialog != null) {
            selectPassworDialog.dismiss();
            selectPassworDialog = null;
            return;
        }

        if (changeNameDialog != null) {
            changeNameDialog.dismiss();
            changeNameDialog = null;
            return;
        }

        if (changeOnboardingNameDialog != null) {
            changeOnboardingNameDialog.dismiss();
            changeOnboardingNameDialog = null;
            return;
        }

        if (addLabelToPasswordDialog != null) {
            addLabelToPasswordDialog.dismiss();
            addLabelToPasswordDialog = null;
            return;
        }

        if (keyPadService != null) {
            keyPadService.removeListener(AyarlarYoneticiFragment.class.getName());
        }
    }

    @Override
    public void onKeyPressed(String keyCode) {

        if (!isAdded()) {
            return;
        }

        getActivity().runOnUiThread(new Runnable() {
            @Override
            public void run() {

//                Log.d(Constants.LOG_TAG, "KapiAcFragment onKeyPressed keyCode=" + keyCode);

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
//                    bottomLockClicked();
                } else {
                    bottomNumbersClicked(keyCode);
                }

            }
        });

    }

    @Override
    public void onRFIDDetected(String rfid) {
//        ekleme mi silme mi?
        Intent broadcastIntent = null;
        if (isRFIDRemoveCountDownDialogRunning) {
            broadcastIntent = new Intent("com.netelsan.panel.RemoveRFID");
        } else {
            broadcastIntent = new Intent("com.netelsan.panel.AddNewRFID");
        }
        broadcastIntent.putExtra("rfid", rfid);
        getActivity().sendBroadcast(broadcastIntent);
    }

    private void bottomNumbersClicked(String keyCode) {

        if (isPasswordAddDialogRunning) {
            if (selectedSifrePart == 1) {
                sifreDialogText1.setText("*");
                sifreDialogText1.setTag(keyCode);
            } else if (selectedSifrePart == 2) {
                sifreDialogText2.setText("*");
                sifreDialogText2.setTag(keyCode);
            } else if (selectedSifrePart == 3) {
                sifreDialogText3.setText("*");
                sifreDialogText3.setTag(keyCode);
            } else if (selectedSifrePart == 4) {
                sifreDialogText4.setText("*");
                sifreDialogText4.setTag(keyCode);
            }
            if (selectedSifrePart != 4) {
                passwordAddNavigate(Constants.MENU_NAVIGATION_DOWN);
            }
            return;
        }

        if (isChangeNameDialogRunning) {
            setChangeNameSuitableText(keyCode);
            return;
        }

        if (isChangeOnboardingNameDialogRunning) {
            setChangeOnboardingNameSuitableText(keyCode);
            return;
        }

        if (isAddLabelToPasswordDialogRunning) {
            setAddLabelSuitableText(keyCode);
            return;
        }

    }

    private void setAddLabelSuitableText(String keyCode) {

        TextView selectedTextview = changeNameTextViews.get(selectedChangeNameItemIndex);

        String currentText = selectedTextview.getText().toString();

        selectedTextview.setText(getSuitableChar(currentText, keyCode));

    }

    private void setChangeNameSuitableText(String keyCode) {

        TextView selectedTextview = changeNameTextViews.get(selectedChangeNameItemIndex);

        String currentText = selectedTextview.getText().toString();

        selectedTextview.setText(getSuitableChar(currentText, keyCode));

    }

    private void setChangeOnboardingNameSuitableText(String keyCode) {

        TextView selectedTextview = changeOnboardingNameTextViews.get(selectedChangeOnboardingNameItemIndex);

        String currentText = selectedTextview.getText().toString();

        selectedTextview.setText(getSuitableChar(currentText, keyCode));

    }


    private String getSuitableChar(String currentText, String keycode) {

        currentText = currentText.toLowerCase();

        String text = "";

        if (keycode.equals(Constants.KEYPAD_1)) {
            text = "1";
        } else if (keycode.equals(Constants.KEYPAD_2)) {
            if (currentText.equals("a")) {
                text = "b";
            } else if (currentText.equals("b")) {
                text = "c";
            } else if (currentText.equals("c")) {
                text = "ç";
            } else if (currentText.equals("ç")) {
                text = "2";
            } else {
                text = "a";
            }
        } else if (keycode.equals(Constants.KEYPAD_3)) {
            if (currentText.equals("d")) {
                text = "e";
            } else if (currentText.equals("e")) {
                text = "f";
            } else if (currentText.equals("f")) {
                text = "3";
            } else if (currentText.equals("3")) {
                text = "d";
            } else {
                text = "d";
            }
        } else if (keycode.equals(Constants.KEYPAD_4)) {
            if (currentText.equals("g")) {
                text = "ğ";
            } else if (currentText.equals("ğ")) {
                text = "h";
            } else if (currentText.equals("h")) {
                text = "ı";
            } else if (currentText.equals("ı")) {
                text = "i";
            } else if (currentText.equals("i")) {
                text = "4";
            } else if (currentText.equals("4")) {
                text = "g";
            } else {
                text = "g";
            }
        } else if (keycode.equals(Constants.KEYPAD_5)) {
            if (currentText.equals("j")) {
                text = "k";
            } else if (currentText.equals("k")) {
                text = "l";
            } else if (currentText.equals("l")) {
                text = "5";
            } else if (currentText.equals("5")) {
                text = "j";
            } else {
                text = "j";
            }
        } else if (keycode.equals(Constants.KEYPAD_6)) {
            if (currentText.equals("m")) {
                text = "n";
            } else if (currentText.equals("n")) {
                text = "o";
            } else if (currentText.equals("o")) {
                text = "ö";
            } else if (currentText.equals("ö")) {
                text = "6";
            } else if (currentText.equals("6")) {
                text = "m";
            } else {
                text = "m";
            }
        } else if (keycode.equals(Constants.KEYPAD_7)) {
            if (currentText.equals("p")) {
                text = "q";
            } else if (currentText.equals("q")) {
                text = "r";
            } else if (currentText.equals("r")) {
                text = "s";
            } else if (currentText.equals("s")) {
                text = "ş";
            } else if (currentText.equals("ş")) {
                text = "7";
            } else if (currentText.equals("7")) {
                text = "p";
            } else {
                text = "p";
            }
        } else if (keycode.equals(Constants.KEYPAD_8)) {
            if (currentText.equals("t")) {
                text = "u";
            } else if (currentText.equals("u")) {
                text = "ü";
            } else if (currentText.equals("ü")) {
                text = "v";
            } else if (currentText.equals("v")) {
                text = "8";
            } else if (currentText.equals("8")) {
                text = "t";
            } else {
                text = "t";
            }
        } else if (keycode.equals(Constants.KEYPAD_9)) {
            if (currentText.equals("w")) {
                text = "x";
            } else if (currentText.equals("x")) {
                text = "y";
            } else if (currentText.equals("y")) {
                text = "z";
            } else if (currentText.equals("z")) {
                text = "9";
            } else if (currentText.equals("9")) {
                text = "w";
            } else {
                text = "w";
            }
        } else if (keycode.equals(Constants.KEYPAD_0)) {
            if (currentText.equals("0")) {
                text = "";
            } else {
                text = "0";
            }
        }


        return text.toUpperCase();
    }

    private void menuNavigationClicked(int direction) {

        if (isSelectDaireDialogRunning) {
            navigateSifreMenu(direction);
            return;
        }

        if (isSelectPasswordDialogRunning) {
            navigateSelectPasswordMenu(direction);
            return;
        }
        if (isPasswordAddDialogRunning) {
            passwordAddNavigate(direction);
            return;
        }

        if (isSelectLanguageDialogRunning) {
            selectLanguageNavigate(direction);
            return;
        }

        if (isSelectVolumeDialogRunning) {
            selectVolumeNavigate(direction);
            return;
        }
        if (isSelectKonusmaVolumeDialogRunning) {
            selectKonusmaVolumeNavigate(direction);
            return;
        }

        if (isChangeNameDialogRunning) {
            changeNameNavigate(direction);
            return;
        }

        if (isChangeOnboardingNameDialogRunning) {
            changeOnboardingNameNavigate(direction);
            return;
        }

        if (isAddLabelToPasswordDialogRunning) {
            addLabelNavigate(direction);
            return;
        }

        if (isRFIDCountDownDialogRunning) {
            return;
        }

        if (isRFIDRemoveCountDownDialogRunning) {
            return;
        }

        boolean isZilForSite = Helper.isZilForSite(getActivity());

        boolean isDirectionUp = true;
        if (direction == Constants.MENU_NAVIGATION_UP) {
            isDirectionUp = true;
            if (selectedMenuId == Constants.YONETICI_MENU_LANGUAGE) {
                isDirectionUp = false;
                if (isZilForSite) {
                    selectedMenuId = Constants.YONETICI_MENU_VOLUME;
                } else {
                    selectedMenuId = Constants.YONETICI_MENU_KONUSMA_VOLUME;
                }
            } else {
                selectedMenuId = selectedMenuId - 1;
            }
        } else if (direction == Constants.MENU_NAVIGATION_DOWN) {
            isDirectionUp = false;
            if (isZilForSite) {
                if (selectedMenuId == Constants.YONETICI_MENU_VOLUME) {
                    isDirectionUp = true;
                    selectedMenuId = Constants.YONETICI_MENU_LANGUAGE;
                } else {
                    selectedMenuId = selectedMenuId + 1;
                }
            } else {
                if (selectedMenuId == Constants.YONETICI_MENU_KONUSMA_VOLUME) {
                    isDirectionUp = true;
                    selectedMenuId = Constants.YONETICI_MENU_LANGUAGE;
                } else {
                    selectedMenuId = selectedMenuId + 1;
                }
            }
        }

        selectMenu(selectedMenuId, isDirectionUp);

    }


    private void addLabelNavigate(int direction) {

        if (direction == Constants.MENU_NAVIGATION_UP) {
            if (selectedChangeNameItemIndex == 0) {
                selectedChangeNameItemIndex = 14;
            } else {
                selectedChangeNameItemIndex = selectedChangeNameItemIndex - 1;
            }
        } else if (direction == Constants.MENU_NAVIGATION_DOWN) {
            if (selectedChangeNameItemIndex == 14) {
                selectedChangeNameItemIndex = 0;
            } else {
                selectedChangeNameItemIndex = selectedChangeNameItemIndex + 1;
            }
        }

        Iterator<LinearLayout> iterator = changeNameContainerViews.iterator();
        while (iterator.hasNext()) {
            LinearLayout linearLayout = iterator.next();
            linearLayout.setBackgroundResource(0);
        }

        Iterator<TextView> iterator2 = changeNameTextViews.iterator();
        while (iterator2.hasNext()) {
            TextView textView = iterator2.next();
        }

        changeNameContainerViews.get(selectedChangeNameItemIndex).setBackgroundResource(R.drawable.black_bordered_btn_unclicked_bg);

    }

    private void changeNameNavigate(int direction) {

        if (direction == Constants.MENU_NAVIGATION_UP) {
            if (selectedChangeNameItemIndex == 0) {
                selectedChangeNameItemIndex = 14;
            } else {
                selectedChangeNameItemIndex = selectedChangeNameItemIndex - 1;
            }
        } else if (direction == Constants.MENU_NAVIGATION_DOWN) {
            if (selectedChangeNameItemIndex == 14) {
                selectedChangeNameItemIndex = 0;
            } else {
                selectedChangeNameItemIndex = selectedChangeNameItemIndex + 1;
            }
        }

        Iterator<LinearLayout> iterator = changeNameContainerViews.iterator();
        while (iterator.hasNext()) {
            LinearLayout linearLayout = iterator.next();
            linearLayout.setBackgroundResource(0);
        }

        Iterator<TextView> iterator2 = changeNameTextViews.iterator();
        while (iterator2.hasNext()) {
            TextView textView = iterator2.next();
        }

        changeNameContainerViews.get(selectedChangeNameItemIndex).setBackgroundResource(R.drawable.black_bordered_btn_unclicked_bg);

    }

    private void changeOnboardingNameNavigate(int direction) {

        if (direction == Constants.MENU_NAVIGATION_UP) {
            if (selectedChangeOnboardingNameItemIndex == 0) {
                selectedChangeOnboardingNameItemIndex = 49;
            } else {
                selectedChangeOnboardingNameItemIndex = selectedChangeOnboardingNameItemIndex - 1;
            }
        } else if (direction == Constants.MENU_NAVIGATION_DOWN) {
            if (selectedChangeOnboardingNameItemIndex == 49) {
                selectedChangeOnboardingNameItemIndex = 0;
            } else {
                selectedChangeOnboardingNameItemIndex = selectedChangeOnboardingNameItemIndex + 1;
            }
        }

        Iterator<LinearLayout> iterator = changeOnboardingNameContainerViews.iterator();
        while (iterator.hasNext()) {
            LinearLayout linearLayout = iterator.next();
            linearLayout.setBackgroundResource(0);
        }

        Iterator<TextView> iterator2 = changeOnboardingNameTextViews.iterator();
        while (iterator2.hasNext()) {
            TextView textView = iterator2.next();
        }

        changeOnboardingNameContainerViews.get(selectedChangeOnboardingNameItemIndex).setBackgroundResource(R.drawable.black_bordered_btn_unclicked_bg);

    }

    private void selectVolumeNavigate(int direction) {
        if (direction == Constants.MENU_NAVIGATION_UP) {
            setSoundLevel(true);
        } else if (direction == Constants.MENU_NAVIGATION_DOWN) {
            setSoundLevel(false);
        }
    }



    private void selectKonusmaVolumeNavigate(int direction) {
        if (direction == Constants.MENU_NAVIGATION_UP) {
            setKonusmaSoundLevel(true);
        } else if (direction == Constants.MENU_NAVIGATION_DOWN) {
            setKonusmaSoundLevel(false);
        }
    }

    private void setSoundLevel(boolean isIncrease) {

        AudioManager audioManager = (AudioManager) getActivity().getSystemService(AUDIO_SERVICE);
        int volume_level = audioManager.getStreamVolume(AppRTCAudioManager.SELECTED_SOUND_CHANNEL);
        int maxVolume = audioManager.getStreamMaxVolume(AppRTCAudioManager.SELECTED_SOUND_CHANNEL);

        if (isIncrease) {
            if (volume_level == maxVolume) {
                return;
            }

            volume_level = volume_level + 1;

        } else {

            if (volume_level == 1) {
                return;
            }

            volume_level = volume_level - 1;
        }

        audioManager.setStreamVolume(AppRTCAudioManager.SELECTED_SOUND_CHANNEL, volume_level, 0);

        soundLevelText.setText(volume_level + " / " + maxVolume);

        Log.d("HK","volume level:" + volume_level);

        switch (volume_level) {
            case 1:
                Helper.setVolume(getActivity().getApplicationContext(), 12);
                break;
            case 2:
                Helper.setVolume(getActivity().getApplicationContext(), 10);
                break;
            case 3:
                Helper.setVolume(getActivity().getApplicationContext(), 8);
                break;
            case 4:
                Helper.setVolume(getActivity().getApplicationContext(), 6);
                break;
            case 5:
                Helper.setVolume(getActivity().getApplicationContext(), 4);
                break;
            default:
                Helper.setVolume(getActivity().getApplicationContext(), 9);
                break;

        }

    }
    public static int volume_konusma_level=2;
    private void setKonusmaSoundLevel(boolean isIncrease) {

        AudioManager audioManager = (AudioManager) getActivity().getSystemService(AUDIO_SERVICE);
        int volume_level = volume_konusma_level;
        int maxVolume = audioManager.getStreamMaxVolume(AppRTCAudioManager.SELECTED_SOUND_CHANNEL);

        if (isIncrease) {
            if (volume_level == maxVolume) {
                return;
            }

            volume_level = volume_level + 1;

        } else {

            if (volume_level == 1) {
                return;
            }

            volume_level = volume_level - 1;
        }

        //audioManager.setStreamVolume(AppRTCAudioManager.SELECTED_SOUND_CHANNEL, volume_level, 0);

        volume_konusma_level=volume_level;
        soundKonusmaLevelText.setText(volume_level + " / " + maxVolume);

    }

    private void selectLanguageNavigate(int direction) {

        if (direction == Constants.MENU_NAVIGATION_UP) {
            if (selectedSelectLanguageItemIndex == Constants.LANGUAGE_TR) {
                selectedSelectLanguageItemIndex = Constants.LANGUAGE_DE;
            } else {
                selectedSelectLanguageItemIndex = selectedSelectLanguageItemIndex - 1;
            }
        } else if (direction == Constants.MENU_NAVIGATION_DOWN) {
            if (selectedSelectLanguageItemIndex == Constants.LANGUAGE_DE) {
                selectedSelectLanguageItemIndex = Constants.LANGUAGE_TR;
            } else {
                selectedSelectLanguageItemIndex = selectedSelectLanguageItemIndex + 1;
            }
        }

        customLanguageTurkceContainer.setBackgroundResource(0);
        customLanguageTurkceText.setTextColor(getResources().getColor(R.color.white));
        customLanguageEnglishContainer.setBackgroundResource(0);
        customLanguageEnglishText.setTextColor(getResources().getColor(R.color.white));
        customLanguageDeutschContainer.setBackgroundResource(0);
        customLanguageDeutschText.setTextColor(getResources().getColor(R.color.white));

        if (selectedSelectLanguageItemIndex == Constants.LANGUAGE_TR) {
            customLanguageTurkceContainer.setBackgroundResource(R.drawable.menu_item_selected);
            customLanguageTurkceText.setTextColor(getResources().getColor(R.color.black));
        } else if (selectedSelectLanguageItemIndex == Constants.LANGUAGE_EN) {
            customLanguageEnglishContainer.setBackgroundResource(R.drawable.menu_item_selected);
            customLanguageEnglishText.setTextColor(getResources().getColor(R.color.black));
        } else if (selectedSelectLanguageItemIndex == Constants.LANGUAGE_DE) {
            customLanguageDeutschContainer.setBackgroundResource(R.drawable.menu_item_selected);
            customLanguageDeutschText.setTextColor(getResources().getColor(R.color.black));
        }

    }

    private void passwordAddNavigate(int direction) {

        if (direction == Constants.MENU_NAVIGATION_UP) {
            if (selectedSifrePart == 1) {
                selectedSifrePart = 4;
            } else {
                selectedSifrePart = selectedSifrePart - 1;
            }
        } else if (direction == Constants.MENU_NAVIGATION_DOWN) {
            if (selectedSifrePart == 4) {
                selectedSifrePart = 1;
            } else {
                selectedSifrePart = selectedSifrePart + 1;
            }
        }

        sifreDialogText1Container.setBackgroundResource(0);
        sifreDialogText2Container.setBackgroundResource(0);
        sifreDialogText3Container.setBackgroundResource(0);
        sifreDialogText4Container.setBackgroundResource(0);

        if (selectedSifrePart == 1) {
            sifreDialogText1Container.setBackgroundResource(R.drawable.black_bordered_btn_bg);
        } else if (selectedSifrePart == 2) {
            sifreDialogText2Container.setBackgroundResource(R.drawable.black_bordered_btn_bg);
        } else if (selectedSifrePart == 3) {
            sifreDialogText3Container.setBackgroundResource(R.drawable.black_bordered_btn_bg);
        } else if (selectedSifrePart == 4) {
            sifreDialogText4Container.setBackgroundResource(R.drawable.black_bordered_btn_bg);
        }

    }

    private void selectMenu(int menuId, boolean isDirectionUp) {

        unselectAllMenus();

        boolean isZilForSite = Helper.isZilForSite(getActivity());
        if (!isZilForSite) {

            if (isDirectionUp) {
                if (menuId <= Constants.YONETICI_MENU_TUS_SESI) {
                    ayarlarScrollView.fullScroll(ayarlarScrollView.FOCUS_UP);
                }
            } else {
                if (menuId >= Constants.YONETICI_MENU_REMOVE_PASSWORD) {
                    ayarlarScrollView.fullScroll(ayarlarScrollView.FOCUS_DOWN);
                }
            }
        }

        if (menuId == Constants.YONETICI_MENU_LANGUAGE) {
            yoneticiLanguageContainer.setBackgroundResource(R.drawable.menu_item_selected);
            yoneticiLanguageText.setTextColor(getResources().getColor(R.color.black));
        } else if (menuId == Constants.YONETICI_MENU_CHANGE_NAME) {
            yoneticiChangeNameContainer.setBackgroundResource(R.drawable.menu_item_selected);
            yoneticiChangeNameText.setTextColor(getResources().getColor(R.color.black));
        } else if (menuId == Constants.YONETICI_MENU_CHANGE_ONBOARDING_NAME) {
            yoneticiChangeOnboardingNameContainer.setBackgroundResource(R.drawable.menu_item_selected);
            yoneticiChangeOnboardingNameText.setTextColor(getResources().getColor(R.color.black));
        } else if (menuId == Constants.YONETICI_MENU_VOLUME) {
            yoneticiVolumeContainer.setBackgroundResource(R.drawable.menu_item_selected);
            yoneticiVolumeNameText.setTextColor(getResources().getColor(R.color.black));
        } else if (menuId == Constants.YONETICI_MENU_KONUSMA_VOLUME) {
            yoneticiKonusmaVolumeContainer.setBackgroundResource(R.drawable.menu_item_selected);
            yoneticiKonusmaVolumeNameText.setTextColor(getResources().getColor(R.color.black));
        } else if (menuId == Constants.YONETICI_MENU_TUS_SESI) {
            yoneticiTusSesiContainer.setBackgroundResource(R.drawable.menu_item_selected);
            yoneticiTusSesiText.setTextColor(getResources().getColor(R.color.black));

            if (Helper.isTusSesiEnabled(getActivity())) {
                yoneticiTusSesiImage.setImageResource(R.drawable.icon_checkbox_checked_black);
            } else {
                yoneticiTusSesiImage.setImageResource(R.drawable.icon_checkbox_unchecked_black);
            }
        } else if (menuId == Constants.YONETICI_MENU_ADD_PASSWORD) {
            yoneticiAddPasswordContainer.setBackgroundResource(R.drawable.menu_item_selected);
            yoneticiAddPasswordText.setTextColor(getResources().getColor(R.color.black));
        } else if (menuId == Constants.YONETICI_MENU_REMOVE_PASSWORD) {
            yoneticiRemovePasswordContainer.setBackgroundResource(R.drawable.menu_item_selected);
            yoneticiRemovePasswordText.setTextColor(getResources().getColor(R.color.black));
        } else if (menuId == Constants.YONETICI_MENU_ADD_RFID) {
            yoneticiAddRFIDContainer.setBackgroundResource(R.drawable.menu_item_selected);
            yoneticiAddRFIDText.setTextColor(getResources().getColor(R.color.black));
        } else if (menuId == Constants.YONETICI_MENU_REMOVE_RFID) {
            yoneticiRemoveRFIDContainer.setBackgroundResource(R.drawable.menu_item_selected);
            yoneticiRemoveRFIDText.setTextColor(getResources().getColor(R.color.black));
        } else if (menuId == Constants.YONETICI_MENU_REMOVE_RFID_VIA_TOUCH) {
            yoneticiRemoveRFIDViaTouchContainer.setBackgroundResource(R.drawable.menu_item_selected);
            yoneticiRemoveRFIDViaTouchText.setTextColor(getResources().getColor(R.color.black));
        }
//        else if (menuId == Constants.YONETICI_MENU_VIDEO_KAYIT) {
//            yoneticiVideoKayitContainer.setBackgroundResource(R.drawable.menu_item_selected);
//            yoneticiVideoKayitText.setTextColor(getResources().getColor(R.color.black));
//
//            if (Helper.isCallVideoRecordEnabled(getActivity())) {
//                yoneticiVideoKayitImage.setImageResource(R.drawable.icon_checkbox_checked_black);
//            } else {
//                yoneticiVideoKayitImage.setImageResource(R.drawable.icon_checkbox_unchecked_black);
//            }
//        }

    }

    private void unselectAllMenus() {

        yoneticiLanguageContainer.setBackgroundResource(R.drawable.menu_item_unselected);
        yoneticiLanguageText.setTextColor(getResources().getColor(R.color.white));

        yoneticiChangeNameContainer.setBackgroundResource(R.drawable.menu_item_unselected);
        yoneticiChangeNameText.setTextColor(getResources().getColor(R.color.white));

        yoneticiChangeOnboardingNameContainer.setBackgroundResource(R.drawable.menu_item_unselected);
        yoneticiChangeOnboardingNameText.setTextColor(getResources().getColor(R.color.white));

        yoneticiVolumeContainer.setBackgroundResource(R.drawable.menu_item_unselected);
        yoneticiVolumeNameText.setTextColor(getResources().getColor(R.color.white));

        yoneticiKonusmaVolumeContainer.setBackgroundResource(R.drawable.menu_item_unselected);
        yoneticiKonusmaVolumeNameText.setTextColor(getResources().getColor(R.color.white));


        yoneticiAddPasswordContainer.setBackgroundResource(R.drawable.menu_item_unselected);
        yoneticiAddPasswordText.setTextColor(getResources().getColor(R.color.white));

        yoneticiRemovePasswordContainer.setBackgroundResource(R.drawable.menu_item_unselected);
        yoneticiRemovePasswordText.setTextColor(getResources().getColor(R.color.white));

        yoneticiAddRFIDContainer.setBackgroundResource(R.drawable.menu_item_unselected);
        yoneticiAddRFIDText.setTextColor(getResources().getColor(R.color.white));

        yoneticiRemoveRFIDContainer.setBackgroundResource(R.drawable.menu_item_unselected);
        yoneticiRemoveRFIDText.setTextColor(getResources().getColor(R.color.white));

        yoneticiRemoveRFIDViaTouchContainer.setBackgroundResource(R.drawable.menu_item_unselected);
        yoneticiRemoveRFIDViaTouchText.setTextColor(getResources().getColor(R.color.white));

//        yoneticiVideoKayitContainer.setBackgroundResource(R.drawable.menu_item_unselected);
//        yoneticiVideoKayitText.setTextColor(getResources().getColor(R.color.white));

        yoneticiTusSesiContainer.setBackgroundResource(R.drawable.menu_item_unselected);
        yoneticiTusSesiText.setTextColor(getResources().getColor(R.color.white));

//        if (Helper.isCallVideoRecordEnabled(getActivity())) {
////            yoneticiVideoKayitImage.setImageResource(R.drawable.icon_checkbox_checked);
////        } else {
////            yoneticiVideoKayitImage.setImageResource(R.drawable.icon_checkbox_unchecked);
////        }

        if (Helper.isTusSesiEnabled(getActivity())) {
            yoneticiTusSesiImage.setImageResource(R.drawable.icon_checkbox_checked);
        } else {
            yoneticiTusSesiImage.setImageResource(R.drawable.icon_checkbox_unchecked);
        }

    }

    private void bottomMenuOkayClicked() {

        if (isSelectDaireDialogRunning) {
            selectDaireOkayButtonClicked();
            return;
        }

        if (isSelectPasswordDialogRunning) {
            selectPasswordOkayButtonClicked();
            return;
        }

        if (isPasswordAddDialogRunning) {
            passwordAddOkayButtonClicked();
            return;
        }

        if (isSelectLanguageDialogRunning) {
            selectLanguageOkayClicked();
            return;
        }

        if (isSelectVolumeDialogRunning) {
            selectVolumeOkayClicked();
            return;
        }
        if (isSelectKonusmaVolumeDialogRunning) {
            selectKonusmaVolumeOkayClicked();
            return;
        }

        if (isChangeNameDialogRunning) {
            changeNameOkayClicked();
            return;
        }

        if (isChangeOnboardingNameDialogRunning) {
            changeOnboardingNameOkayClicked();
            return;
        }

        if (isAddLabelToPasswordDialogRunning) {
            addLabelOkayClicked();
            return;
        }

        if (isRFIDCountDownDialogRunning) {
            return;
        }

        if (selectedMenuId == Constants.YONETICI_MENU_LANGUAGE) {
            showSelectLanguageDialog();
        } else if (selectedMenuId == Constants.YONETICI_MENU_CHANGE_NAME) {
            showChangeNameDialog();
        } else if (selectedMenuId == Constants.YONETICI_MENU_CHANGE_ONBOARDING_NAME) {
            showChangeOnboardingNameDialog();
        } else if (selectedMenuId == Constants.YONETICI_MENU_VOLUME) {
            showVolumeDialog();
        } else if (selectedMenuId == Constants.YONETICI_MENU_KONUSMA_VOLUME) {
            showKonusmaVolumeDialog();
        } else if (selectedMenuId == Constants.YONETICI_MENU_ADD_PASSWORD) {
            showSelectDaireDialog(true, true);
        } else if (selectedMenuId == Constants.YONETICI_MENU_REMOVE_PASSWORD) {
            showSelectDaireDialog(false, true);
        } else if (selectedMenuId == Constants.YONETICI_MENU_ADD_RFID) {
            showSelectDaireDialog(true, false);
        } else if (selectedMenuId == Constants.YONETICI_MENU_REMOVE_RFID) {
            showSelectDaireDialog(false, false);
        } else if (selectedMenuId == Constants.YONETICI_MENU_REMOVE_RFID_VIA_TOUCH) {
            showRFIDTouchForRemoveDialog();
        } else if (selectedMenuId == Constants.YONETICI_MENU_TUS_SESI) {
            Helper.setTusSesiEnabled(getActivity(), !Helper.isTusSesiEnabled(getActivity()));
            setLayouts();
        }
//        else if (selectedMenuId == Constants.YONETICI_MENU_VIDEO_KAYIT) {
//            Helper.setCallVideoRecordEnabled(getActivity(), !Helper.isCallVideoRecordEnabled(getActivity()));
//            setLayouts();
//        }


    }


    boolean isRFIDRemoveCountDownDialogRunning = false;
    AlertDialog RFIDRemoveCountDownTimerDialog;
    int timeRemainingForRemove = 11;
    Timer timerCountDownForRemove;

    private void showRFIDTouchForRemoveDialog() {

        timeRemainingForRemove = 11;

        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setTitle("");

        View customLayout = getLayoutInflater().inflate(R.layout.custom_rfid_remove_countdown_dialog, null);
        builder.setView(customLayout);

        TextView rfidCountdownDialogRemainingTime = customLayout.findViewById(R.id.rfidCountdownDialogRemainingTime);
        rfidCountdownDialogRemainingTime.setText("10");

        builder.setOnDismissListener(new DialogInterface.OnDismissListener() {
            @Override
            public void onDismiss(DialogInterface dialog) {
                RFIDRemoveCountDownTimerDialog = null;
                isRFIDRemoveCountDownDialogRunning = false;
            }
        });
        RFIDRemoveCountDownTimerDialog = builder.create();
        RFIDRemoveCountDownTimerDialog.show();
        isRFIDRemoveCountDownDialogRunning = true;

        IntentFilter filter = new IntentFilter();
        filter.addAction("com.netelsan.panel.RemoveRFID");
        getActivity().registerReceiver(removeRFIDBroadcastReceiver, filter);

        timerCountDownForRemove = new Timer();
        timerCountDownForRemove.schedule(new TimerTask() {
            @Override
            public void run() {

                if (timeRemainingForRemove == 0) {

                    if (removeRFIDBroadcastReceiver != null) {
                        getActivity().unregisterReceiver(removeRFIDBroadcastReceiver);
//                        removeRFIDBroadcastReceiver = null;
                    }

                    timerCountDownForRemove.cancel();
                    timerCountDownForRemove.purge();
                    timerCountDownForRemove = null;

                    RFIDRemoveCountDownTimerDialog.dismiss();

                    getActivity().runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            Helper.showTopMessageBanner((AppCompatActivity) getActivity(), getString(R.string.islem_iptal_edildi), false);
                        }
                    });

                    return;
                }
                timeRemainingForRemove = timeRemainingForRemove - 1;

                getActivity().runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        rfidCountdownDialogRemainingTime.setText(timeRemainingForRemove + "");
                    }
                });

            }
        }, 0, 1000);

    }

    BroadcastReceiver removeRFIDBroadcastReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            Log.d(Constants.LOG_TAG, "removeRFIDBroadcastReceiver onReceive");

            if (intent == null) {
                return;
            }
            Bundle bundle = intent.getExtras();
            if (bundle == null) {
                return;
            }

            String RFIDKey = bundle.getString("rfid");
            Log.d(Constants.LOG_TAG, "removeRFIDBroadcastReceiver onReceive RFIDKey=" + RFIDKey);

            DatabaseHelper databaseHelper = new DatabaseHelper(getActivity());
            DoorPassword doorPassword = databaseHelper.getPasswordByRFID(RFIDKey);
            if (doorPassword == null) {
                keyPadService.unlockDoorStateVoice("invalid_RF");
                Helper.showTopMessageBanner((AppCompatActivity) getActivity(), getString(R.string.gecersiz), false);
                return;
            }

            if (removeRFIDBroadcastReceiver != null) {
                getActivity().unregisterReceiver(removeRFIDBroadcastReceiver);
//                removeRFIDBroadcastReceiver = null;
            }

            timerCountDownForRemove.cancel();
            timerCountDownForRemove.purge();
            timerCountDownForRemove = null;

            RFIDRemoveCountDownTimerDialog.dismiss();

            sendPasswordDeletedToOtherZilPanels(databaseHelper, doorPassword);
            databaseHelper.deletePasswordByIPAndDoorRFID(doorPassword);

            RFIDRemoveCountDownTimerDialog.dismiss();
            RFIDRemoveCountDownTimerDialog = null;

            isRFIDRemoveCountDownDialogRunning = false;

            Helper.showTopMessageBanner((AppCompatActivity) getActivity(), getString(R.string.kart_basari_ile_silindi), true);

        }
    };

    private void addLabelOkayClicked() {

        String finalText = "";
        Iterator<TextView> iterator = changeNameTextViews.iterator();
        while (iterator.hasNext()) {
            TextView textView = iterator.next();
            String text = textView.getText().toString();

            if (text.equals("")) {
                text = " ";
            }

            finalText = finalText + text;

        }

        finalText = removeLastBlankChars(finalText);

        Log.d(Constants.LOG_TAG, "label for password=" + finalText);

        DatabaseHelper databaseHelper = new DatabaseHelper(getActivity());
        doorPasswordForAddLabel.setPasswordLabel(finalText);
        databaseHelper.updatePassword(doorPasswordForAddLabel);

        sendNewPasswordToOtherZilPanels(databaseHelper, doorPasswordForAddLabel);

        addLabelToPasswordDialog.dismiss();
        addLabelToPasswordDialog = null;
        isAddLabelToPasswordDialogRunning = false;

        Helper.showTopMessageBanner((AppCompatActivity) getActivity(), getString(R.string.sifre_icin_isim_belirlendi), true);

    }

    private void changeNameOkayClicked() {

        String finalText = "";
        Iterator<TextView> iterator = changeNameTextViews.iterator();
        while (iterator.hasNext()) {
            TextView textView = iterator.next();
            String text = textView.getText().toString();

            if (text.equals("")) {
                text = " ";
            }

            finalText = finalText + text;

        }

        finalText = removeLastBlankChars(finalText);

        DatabaseHelper databaseHelper = new DatabaseHelper(getActivity());

        boolean isZilForSite = Helper.isZilForSite(getActivity());

        if (isZilForSite) {

            ZilPanelSite zilPanelSite = databaseHelper.getSiteZilPanelByIP(Utils.getIPAddress(true));
            zilPanelSite.setDeviceName(finalText);
            databaseHelper.updateSiteZilPanel(zilPanelSite);

            ArrayList<ZilPanel> zilPanels = databaseHelper.getZilPanelleri(true);
            Iterator<ZilPanel> iteratorZilPanels = zilPanels.iterator();
            while (iteratorZilPanels.hasNext()) {
                ZilPanel zilPanel = iteratorZilPanels.next();
                String destinationIP = zilPanel.getIp();
                ComPackageModel model = new ComPackageModel();
                model.setOpe_type(Constants.OPERATION_HANDSHAKE_ZIL_PANEL_SITE);
                model.setZilPanelSite(zilPanelSite);
                model.setNeedResponse(false);

                TCPHelper.sendMessageToIP(getActivity(), destinationIP, model);
            }

        } else {

            ZilPanel zilPanelSelf = databaseHelper.getZilPanelByIP(Utils.getIPAddress(true));
            zilPanelSelf.setDeviceName(finalText);

            databaseHelper.updateZilPanel(zilPanelSelf);

            ArrayList<ZilPanel> arrayListZilPanels = databaseHelper.getZilPanelleri(false);


            Iterator<ZilPanel> iterator2 = arrayListZilPanels.iterator();
            while (iterator2.hasNext()) {
                ZilPanel zilPanel = iterator2.next();

                String destinationIP = zilPanel.getIp();
                ComPackageModel model = new ComPackageModel();
                model.setOpe_type(Constants.OPERATION_HANDSHAKE_ZIL_PANEL);
                model.setZilPanel(zilPanelSelf);
                model.setNeedResponse(false);

                TCPHelper.sendMessageToIP(getActivity(), destinationIP, model);
            }

            ArrayList<Guvenlik> arrayListGuvenliks = databaseHelper.getGuvenlikler();
            Iterator<Guvenlik> iteratorGuvenlik = arrayListGuvenliks.iterator();
            while (iteratorGuvenlik.hasNext()) {
                Guvenlik guvenlik = iteratorGuvenlik.next();

                String destinationIP = guvenlik.getIp();
                ComPackageModel model = new ComPackageModel();
                model.setOpe_type(Constants.OPERATION_HANDSHAKE_ZIL_PANEL);
                model.setZilPanel(zilPanelSelf);
                model.setNeedResponse(false);

                TCPHelper.sendMessageToIP(getActivity(), destinationIP, model);
            }


            ArrayList<Daire> arrayListDaires = databaseHelper.getDairelerForBlok(zilPanelSelf.getBlok());
            Iterator<Daire> iteratorSubeler = arrayListDaires.iterator();
            while (iteratorSubeler.hasNext()) {
                Daire daire = iteratorSubeler.next();

                String destinationIP = daire.getIp();
                ComPackageModel model = new ComPackageModel();
                model.setOpe_type(Constants.OPERATION_HANDSHAKE_ZIL_PANEL);
                model.setZilPanel(zilPanelSelf);
                model.setNeedResponse(false);

                TCPHelper.sendMessageToIP(getActivity(), destinationIP, model);

            }
        }

        changeNameDialog.dismiss();
        changeNameDialog = null;
        isChangeNameDialogRunning = false;

        Helper.showTopMessageBanner((AppCompatActivity) getActivity(), getString(R.string.cihaz_ismi_basariyla_degistirildi), true);

    }

    private void changeOnboardingNameOkayClicked() {

        List<TextView> textViewsFirstLine = changeOnboardingNameTextViews.subList(0, 24);
        String finalTextFirstLine = "";
        Iterator<TextView> iterator = textViewsFirstLine.iterator();
        while (iterator.hasNext()) {
            TextView textView = iterator.next();
            String text = textView.getText().toString();

            if (text.equals("")) {
                text = " ";
            }

            finalTextFirstLine = finalTextFirstLine + text;

        }
        finalTextFirstLine = removeLastBlankChars(finalTextFirstLine);
        Helper.setOnboardingText1(getActivity(), finalTextFirstLine);

        List<TextView> textViewsSecondLine = changeOnboardingNameTextViews.subList(25, 49);
        String finalTextSecondLine = "";
        Iterator<TextView> iteratorSecond = textViewsSecondLine.iterator();
        while (iteratorSecond.hasNext()) {
            TextView textView = iteratorSecond.next();
            String text = textView.getText().toString();

            if (text.equals("")) {
                text = " ";
            }

            finalTextSecondLine = finalTextSecondLine + text;

        }
        finalTextSecondLine = removeLastBlankChars(finalTextSecondLine);
        Helper.setOnboardingText2(getActivity(), finalTextSecondLine);

        changeOnboardingNameDialog.dismiss();
        changeOnboardingNameDialog = null;
        isChangeOnboardingNameDialogRunning = false;

        Helper.showTopMessageBanner((AppCompatActivity) getActivity(), getString(R.string.cihaz_ismi_basariyla_degistirildi), true);

    }

    private String removeLastBlankChars(String finalText) {

        Hashtable<Integer, String> hashtableTemp = new Hashtable<>();

        char[] charArray = finalText.toCharArray();

        boolean isCharDetected = false;
        for (int index = (finalText.length() - 1); index >= 0; index--) {

            char charText = charArray[index];

            if ((charText + "").equals(" ")) {
                if (isCharDetected) {
                    hashtableTemp.put(index, (charText + ""));
                } else {
                    hashtableTemp.put(index, "");
                }
            } else {
                isCharDetected = true;
                hashtableTemp.put(index, (charText + ""));
            }

        }

        String textTemp = "";
        for (int index = 0; index < finalText.length(); index++) {
            String charText = hashtableTemp.get(index);
            textTemp = textTemp + charText;
        }

//        Enumeration<Integer> enumeration = hashtableTemp.keys();
//        while(enumeration.hasMoreElements()) {
//            int index = enumeration.nextElement();
//            String charText = hashtableTemp.get(index);
//            textTemp = textTemp + charText;
//        }

        return textTemp;

    }

    AlertDialog changeNameDialog;
    boolean isChangeNameDialogRunning;
    int selectedChangeNameItemIndex = 0;

    LinearLayout changeName1Container;
    LinearLayout changeName2Container;
    LinearLayout changeName3Container;
    LinearLayout changeName4Container;
    LinearLayout changeName5Container;
    LinearLayout changeName6Container;
    LinearLayout changeName7Container;
    LinearLayout changeName8Container;
    LinearLayout changeName9Container;
    LinearLayout changeName10Container;
    LinearLayout changeName11Container;
    LinearLayout changeName12Container;
    LinearLayout changeName13Container;
    LinearLayout changeName14Container;
    LinearLayout changeName15Container;
    ArrayList<LinearLayout> changeNameContainerViews = new ArrayList<>();

    TextView changeName1Text;
    TextView changeName2Text;
    TextView changeName3Text;
    TextView changeName4Text;
    TextView changeName5Text;
    TextView changeName6Text;
    TextView changeName7Text;
    TextView changeName8Text;
    TextView changeName9Text;
    TextView changeName10Text;
    TextView changeName11Text;
    TextView changeName12Text;
    TextView changeName13Text;
    TextView changeName14Text;
    TextView changeName15Text;
    ArrayList<TextView> changeNameTextViews = new ArrayList<>();

    private void showChangeNameDialog() {

        DatabaseHelper databaseHelper = new DatabaseHelper(getActivity());

        ZilPanel zilPanelSelf = null;
        ZilPanelSite zilPanelSiteSelf = null;
        boolean isZilForSite = Helper.isZilForSite(getActivity());
        if (isZilForSite) {
            zilPanelSiteSelf = databaseHelper.getSiteZilPanelByIP(Utils.getIPAddress(true));
        } else {
            zilPanelSelf = databaseHelper.getZilPanelByIP(Utils.getIPAddress(true));
        }

        selectedChangeNameItemIndex = 0;

        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setTitle("");

        View customLayout = getLayoutInflater().inflate(R.layout.custom_change_name_dialog, null);
        builder.setView(customLayout);

        setChangeNameLayouts(customLayout);

        if (isZilForSite) {
            setDeviceName(zilPanelSiteSelf);
        } else {
            setDeviceName(zilPanelSelf);
        }

        builder.setOnDismissListener(new DialogInterface.OnDismissListener() {
            @Override
            public void onDismiss(DialogInterface dialog) {
                changeNameDialog = null;
                isChangeNameDialogRunning = false;
            }
        });

        changeNameDialog = builder.create();
        changeNameDialog.show();
        isChangeNameDialogRunning = true;

        WindowManager.LayoutParams layoutParams = new WindowManager.LayoutParams();

        Display display = getActivity().getWindowManager().getDefaultDisplay();
        Point size = new Point();
        display.getSize(size);
        int width = size.x;

        layoutParams.copyFrom(changeNameDialog.getWindow().getAttributes());
        layoutParams.width = (int) (width * 0.90);
        changeNameDialog.getWindow().setAttributes(layoutParams);

    }

    AlertDialog changeOnboardingNameDialog;
    boolean isChangeOnboardingNameDialogRunning;
    int selectedChangeOnboardingNameItemIndex = 0;

    LinearLayout changeOnboardingName1Container;
    LinearLayout changeOnboardingName2Container;
    LinearLayout changeOnboardingName3Container;
    LinearLayout changeOnboardingName4Container;
    LinearLayout changeOnboardingName5Container;
    LinearLayout changeOnboardingName6Container;
    LinearLayout changeOnboardingName7Container;
    LinearLayout changeOnboardingName8Container;
    LinearLayout changeOnboardingName9Container;
    LinearLayout changeOnboardingName10Container;
    LinearLayout changeOnboardingName11Container;
    LinearLayout changeOnboardingName12Container;
    LinearLayout changeOnboardingName13Container;
    LinearLayout changeOnboardingName14Container;
    LinearLayout changeOnboardingName15Container;
    LinearLayout changeOnboardingName16Container;
    LinearLayout changeOnboardingName17Container;
    LinearLayout changeOnboardingName18Container;
    LinearLayout changeOnboardingName19Container;
    LinearLayout changeOnboardingName20Container;
    LinearLayout changeOnboardingName21Container;
    LinearLayout changeOnboardingName22Container;
    LinearLayout changeOnboardingName23Container;
    LinearLayout changeOnboardingName24Container;
    LinearLayout changeOnboardingName25Container;

    LinearLayout changeOnboardingName1Container2;
    LinearLayout changeOnboardingName2Container2;
    LinearLayout changeOnboardingName3Container2;
    LinearLayout changeOnboardingName4Container2;
    LinearLayout changeOnboardingName5Container2;
    LinearLayout changeOnboardingName6Container2;
    LinearLayout changeOnboardingName7Container2;
    LinearLayout changeOnboardingName8Container2;
    LinearLayout changeOnboardingName9Container2;
    LinearLayout changeOnboardingName10Container2;
    LinearLayout changeOnboardingName11Container2;
    LinearLayout changeOnboardingName12Container2;
    LinearLayout changeOnboardingName13Container2;
    LinearLayout changeOnboardingName14Container2;
    LinearLayout changeOnboardingName15Container2;
    LinearLayout changeOnboardingName16Container2;
    LinearLayout changeOnboardingName17Container2;
    LinearLayout changeOnboardingName18Container2;
    LinearLayout changeOnboardingName19Container2;
    LinearLayout changeOnboardingName20Container2;
    LinearLayout changeOnboardingName21Container2;
    LinearLayout changeOnboardingName22Container2;
    LinearLayout changeOnboardingName23Container2;
    LinearLayout changeOnboardingName24Container2;
    LinearLayout changeOnboardingName25Container2;

    ArrayList<LinearLayout> changeOnboardingNameContainerViews = new ArrayList<>();

    TextView changeOnboardingName1Text;
    TextView changeOnboardingName2Text;
    TextView changeOnboardingName3Text;
    TextView changeOnboardingName4Text;
    TextView changeOnboardingName5Text;
    TextView changeOnboardingName6Text;
    TextView changeOnboardingName7Text;
    TextView changeOnboardingName8Text;
    TextView changeOnboardingName9Text;
    TextView changeOnboardingName10Text;
    TextView changeOnboardingName11Text;
    TextView changeOnboardingName12Text;
    TextView changeOnboardingName13Text;
    TextView changeOnboardingName14Text;
    TextView changeOnboardingName15Text;
    TextView changeOnboardingName16Text;
    TextView changeOnboardingName17Text;
    TextView changeOnboardingName18Text;
    TextView changeOnboardingName19Text;
    TextView changeOnboardingName20Text;
    TextView changeOnboardingName21Text;
    TextView changeOnboardingName22Text;
    TextView changeOnboardingName23Text;
    TextView changeOnboardingName24Text;
    TextView changeOnboardingName25Text;

    TextView changeOnboardingName1Text2;
    TextView changeOnboardingName2Text2;
    TextView changeOnboardingName3Text2;
    TextView changeOnboardingName4Text2;
    TextView changeOnboardingName5Text2;
    TextView changeOnboardingName6Text2;
    TextView changeOnboardingName7Text2;
    TextView changeOnboardingName8Text2;
    TextView changeOnboardingName9Text2;
    TextView changeOnboardingName10Text2;
    TextView changeOnboardingName11Text2;
    TextView changeOnboardingName12Text2;
    TextView changeOnboardingName13Text2;
    TextView changeOnboardingName14Text2;
    TextView changeOnboardingName15Text2;
    TextView changeOnboardingName16Text2;
    TextView changeOnboardingName17Text2;
    TextView changeOnboardingName18Text2;
    TextView changeOnboardingName19Text2;
    TextView changeOnboardingName20Text2;
    TextView changeOnboardingName21Text2;
    TextView changeOnboardingName22Text2;
    TextView changeOnboardingName23Text2;
    TextView changeOnboardingName24Text2;
    TextView changeOnboardingName25Text2;

    ArrayList<TextView> changeOnboardingNameTextViews = new ArrayList<>();

    private void showChangeOnboardingNameDialog() {

        selectedChangeOnboardingNameItemIndex = 0;

        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setTitle("");

        View customLayout = getLayoutInflater().inflate(R.layout.custom_change_onboarding_name_dialog, null);
        builder.setView(customLayout);

        setChangeOnboardingNameLayouts(customLayout);
        setOnboardingName();

        builder.setOnDismissListener(new DialogInterface.OnDismissListener() {
            @Override
            public void onDismiss(DialogInterface dialog) {
                changeOnboardingNameDialog = null;
                isChangeOnboardingNameDialogRunning = false;
            }
        });

        changeOnboardingNameDialog = builder.create();
        changeOnboardingNameDialog.show();
        isChangeOnboardingNameDialogRunning = true;

        WindowManager.LayoutParams layoutParams = new WindowManager.LayoutParams();

        Display display = getActivity().getWindowManager().getDefaultDisplay();
        Point size = new Point();
        display.getSize(size);
        int width = size.x;

        layoutParams.copyFrom(changeOnboardingNameDialog.getWindow().getAttributes());
        layoutParams.width = width;//(int) (width * 0.95);
        changeOnboardingNameDialog.getWindow().setAttributes(layoutParams);

    }

    private void setOnboardingName() {
        String onboardingText1 = Helper.getOnboardingText1(getActivity());
        String onboardingText2 = Helper.getOnboardingText2(getActivity());

        if (onboardingText1 == null) {
            onboardingText1 = "";
        }
        if (onboardingText2 == null) {
            onboardingText2 = "";
        }

        List<TextView> firstPart = changeOnboardingNameTextViews.subList(0, 24);
        int index1 = 0;
        for (char ch : onboardingText1.toCharArray()) {

            TextView textView = firstPart.get(index1);
            textView.setText(ch + "");
            index1 = index1 + 1;
        }


        List<TextView> secondPart = changeOnboardingNameTextViews.subList(25, 49);
        int index2 = 0;
        for (char ch : onboardingText2.toCharArray()) {

            TextView textView = secondPart.get(index2);
            textView.setText(ch + "");
            index2 = index2 + 1;
        }

    }

    private void setDeviceName(ZilPanel zilPanelSelf) {
        String deviceName = zilPanelSelf.getDeviceName();

        int index = 0;
        for (char ch : deviceName.toCharArray()) {

            TextView textView = changeNameTextViews.get(index);
            textView.setText(ch + "");
            index = index + 1;
        }

    }

    private void setDeviceName(ZilPanelSite zilPanelSiteSelf) {
        String deviceName = zilPanelSiteSelf.getDeviceName();

        int index = 0;
        for (char ch : deviceName.toCharArray()) {

            TextView textView = changeNameTextViews.get(index);
            textView.setText(ch + "");
            index = index + 1;
        }

    }

    private void setChangeNameLayouts(View rootView) {

        changeNameContainerViews = new ArrayList<>();
        changeNameTextViews = new ArrayList<>();

        changeName1Container = rootView.findViewById(R.id.changeName1Container);
        changeName2Container = rootView.findViewById(R.id.changeName2Container);
        changeName3Container = rootView.findViewById(R.id.changeName3Container);
        changeName4Container = rootView.findViewById(R.id.changeName4Container);
        changeName5Container = rootView.findViewById(R.id.changeName5Container);
        changeName6Container = rootView.findViewById(R.id.changeName6Container);
        changeName7Container = rootView.findViewById(R.id.changeName7Container);
        changeName8Container = rootView.findViewById(R.id.changeName8Container);
        changeName9Container = rootView.findViewById(R.id.changeName9Container);
        changeName10Container = rootView.findViewById(R.id.changeName10Container);
        changeName11Container = rootView.findViewById(R.id.changeName11Container);
        changeName12Container = rootView.findViewById(R.id.changeName12Container);
        changeName13Container = rootView.findViewById(R.id.changeName13Container);
        changeName14Container = rootView.findViewById(R.id.changeName14Container);
        changeName15Container = rootView.findViewById(R.id.changeName15Container);

        changeNameContainerViews.add(changeName1Container);
        changeNameContainerViews.add(changeName2Container);
        changeNameContainerViews.add(changeName3Container);
        changeNameContainerViews.add(changeName4Container);
        changeNameContainerViews.add(changeName5Container);
        changeNameContainerViews.add(changeName6Container);
        changeNameContainerViews.add(changeName7Container);
        changeNameContainerViews.add(changeName8Container);
        changeNameContainerViews.add(changeName9Container);
        changeNameContainerViews.add(changeName10Container);
        changeNameContainerViews.add(changeName11Container);
        changeNameContainerViews.add(changeName12Container);
        changeNameContainerViews.add(changeName13Container);
        changeNameContainerViews.add(changeName14Container);
        changeNameContainerViews.add(changeName15Container);

        changeName1Text = rootView.findViewById(R.id.changeName1Text);
        changeName2Text = rootView.findViewById(R.id.changeName2Text);
        changeName3Text = rootView.findViewById(R.id.changeName3Text);
        changeName4Text = rootView.findViewById(R.id.changeName4Text);
        changeName5Text = rootView.findViewById(R.id.changeName5Text);
        changeName6Text = rootView.findViewById(R.id.changeName6Text);
        changeName7Text = rootView.findViewById(R.id.changeName7Text);
        changeName8Text = rootView.findViewById(R.id.changeName8Text);
        changeName9Text = rootView.findViewById(R.id.changeName9Text);
        changeName10Text = rootView.findViewById(R.id.changeName10Text);
        changeName11Text = rootView.findViewById(R.id.changeName11Text);
        changeName12Text = rootView.findViewById(R.id.changeName12Text);
        changeName13Text = rootView.findViewById(R.id.changeName13Text);
        changeName14Text = rootView.findViewById(R.id.changeName14Text);
        changeName15Text = rootView.findViewById(R.id.changeName15Text);

        changeNameTextViews.add(changeName1Text);
        changeNameTextViews.add(changeName2Text);
        changeNameTextViews.add(changeName3Text);
        changeNameTextViews.add(changeName4Text);
        changeNameTextViews.add(changeName5Text);
        changeNameTextViews.add(changeName6Text);
        changeNameTextViews.add(changeName7Text);
        changeNameTextViews.add(changeName8Text);
        changeNameTextViews.add(changeName9Text);
        changeNameTextViews.add(changeName10Text);
        changeNameTextViews.add(changeName11Text);
        changeNameTextViews.add(changeName12Text);
        changeNameTextViews.add(changeName13Text);
        changeNameTextViews.add(changeName14Text);
        changeNameTextViews.add(changeName15Text);

    }

    private void setChangeOnboardingNameLayouts(View rootView) {

        changeOnboardingNameContainerViews = new ArrayList<>();
        changeOnboardingNameTextViews = new ArrayList<>();

        changeOnboardingName1Container = rootView.findViewById(R.id.changeOnboardingName1Container);
        changeOnboardingName2Container = rootView.findViewById(R.id.changeOnboardingName2Container);
        changeOnboardingName3Container = rootView.findViewById(R.id.changeOnboardingName3Container);
        changeOnboardingName4Container = rootView.findViewById(R.id.changeOnboardingName4Container);
        changeOnboardingName5Container = rootView.findViewById(R.id.changeOnboardingName5Container);
        changeOnboardingName6Container = rootView.findViewById(R.id.changeOnboardingName6Container);
        changeOnboardingName7Container = rootView.findViewById(R.id.changeOnboardingName7Container);
        changeOnboardingName8Container = rootView.findViewById(R.id.changeOnboardingName8Container);
        changeOnboardingName9Container = rootView.findViewById(R.id.changeOnboardingName9Container);
        changeOnboardingName10Container = rootView.findViewById(R.id.changeOnboardingName10Container);
        changeOnboardingName11Container = rootView.findViewById(R.id.changeOnboardingName11Container);
        changeOnboardingName12Container = rootView.findViewById(R.id.changeOnboardingName12Container);
        changeOnboardingName13Container = rootView.findViewById(R.id.changeOnboardingName13Container);
        changeOnboardingName14Container = rootView.findViewById(R.id.changeOnboardingName14Container);
        changeOnboardingName15Container = rootView.findViewById(R.id.changeOnboardingName15Container);
        changeOnboardingName16Container = rootView.findViewById(R.id.changeOnboardingName16Container);
        changeOnboardingName17Container = rootView.findViewById(R.id.changeOnboardingName17Container);
        changeOnboardingName18Container = rootView.findViewById(R.id.changeOnboardingName18Container);
        changeOnboardingName19Container = rootView.findViewById(R.id.changeOnboardingName19Container);
        changeOnboardingName20Container = rootView.findViewById(R.id.changeOnboardingName20Container);
        changeOnboardingName21Container = rootView.findViewById(R.id.changeOnboardingName21Container);
        changeOnboardingName22Container = rootView.findViewById(R.id.changeOnboardingName22Container);
        changeOnboardingName23Container = rootView.findViewById(R.id.changeOnboardingName23Container);
        changeOnboardingName24Container = rootView.findViewById(R.id.changeOnboardingName24Container);
        changeOnboardingName25Container = rootView.findViewById(R.id.changeOnboardingName25Container);

        changeOnboardingName1Container2 = rootView.findViewById(R.id.changeOnboardingName1Container2);
        changeOnboardingName2Container2 = rootView.findViewById(R.id.changeOnboardingName2Container2);
        changeOnboardingName3Container2 = rootView.findViewById(R.id.changeOnboardingName3Container2);
        changeOnboardingName4Container2 = rootView.findViewById(R.id.changeOnboardingName4Container2);
        changeOnboardingName5Container2 = rootView.findViewById(R.id.changeOnboardingName5Container2);
        changeOnboardingName6Container2 = rootView.findViewById(R.id.changeOnboardingName6Container2);
        changeOnboardingName7Container2 = rootView.findViewById(R.id.changeOnboardingName7Container2);
        changeOnboardingName8Container2 = rootView.findViewById(R.id.changeOnboardingName8Container2);
        changeOnboardingName9Container2 = rootView.findViewById(R.id.changeOnboardingName9Container2);
        changeOnboardingName10Container2 = rootView.findViewById(R.id.changeOnboardingName10Container2);
        changeOnboardingName11Container2 = rootView.findViewById(R.id.changeOnboardingName11Container2);
        changeOnboardingName12Container2 = rootView.findViewById(R.id.changeOnboardingName12Container2);
        changeOnboardingName13Container2 = rootView.findViewById(R.id.changeOnboardingName13Container2);
        changeOnboardingName14Container2 = rootView.findViewById(R.id.changeOnboardingName14Container2);
        changeOnboardingName15Container2 = rootView.findViewById(R.id.changeOnboardingName15Container2);
        changeOnboardingName16Container2 = rootView.findViewById(R.id.changeOnboardingName16Container2);
        changeOnboardingName17Container2 = rootView.findViewById(R.id.changeOnboardingName17Container2);
        changeOnboardingName18Container2 = rootView.findViewById(R.id.changeOnboardingName18Container2);
        changeOnboardingName19Container2 = rootView.findViewById(R.id.changeOnboardingName19Container2);
        changeOnboardingName20Container2 = rootView.findViewById(R.id.changeOnboardingName20Container2);
        changeOnboardingName21Container2 = rootView.findViewById(R.id.changeOnboardingName21Container2);
        changeOnboardingName22Container2 = rootView.findViewById(R.id.changeOnboardingName22Container2);
        changeOnboardingName23Container2 = rootView.findViewById(R.id.changeOnboardingName23Container2);
        changeOnboardingName24Container2 = rootView.findViewById(R.id.changeOnboardingName24Container2);
        changeOnboardingName25Container2 = rootView.findViewById(R.id.changeOnboardingName25Container2);


        changeOnboardingNameContainerViews.add(changeOnboardingName1Container);
        changeOnboardingNameContainerViews.add(changeOnboardingName2Container);
        changeOnboardingNameContainerViews.add(changeOnboardingName3Container);
        changeOnboardingNameContainerViews.add(changeOnboardingName4Container);
        changeOnboardingNameContainerViews.add(changeOnboardingName5Container);
        changeOnboardingNameContainerViews.add(changeOnboardingName6Container);
        changeOnboardingNameContainerViews.add(changeOnboardingName7Container);
        changeOnboardingNameContainerViews.add(changeOnboardingName8Container);
        changeOnboardingNameContainerViews.add(changeOnboardingName9Container);
        changeOnboardingNameContainerViews.add(changeOnboardingName10Container);
        changeOnboardingNameContainerViews.add(changeOnboardingName11Container);
        changeOnboardingNameContainerViews.add(changeOnboardingName12Container);
        changeOnboardingNameContainerViews.add(changeOnboardingName13Container);
        changeOnboardingNameContainerViews.add(changeOnboardingName14Container);
        changeOnboardingNameContainerViews.add(changeOnboardingName15Container);
        changeOnboardingNameContainerViews.add(changeOnboardingName16Container);
        changeOnboardingNameContainerViews.add(changeOnboardingName17Container);
        changeOnboardingNameContainerViews.add(changeOnboardingName18Container);
        changeOnboardingNameContainerViews.add(changeOnboardingName19Container);
        changeOnboardingNameContainerViews.add(changeOnboardingName20Container);
        changeOnboardingNameContainerViews.add(changeOnboardingName21Container);
        changeOnboardingNameContainerViews.add(changeOnboardingName22Container);
        changeOnboardingNameContainerViews.add(changeOnboardingName23Container);
        changeOnboardingNameContainerViews.add(changeOnboardingName24Container);
        changeOnboardingNameContainerViews.add(changeOnboardingName25Container);

        changeOnboardingNameContainerViews.add(changeOnboardingName1Container2);
        changeOnboardingNameContainerViews.add(changeOnboardingName2Container2);
        changeOnboardingNameContainerViews.add(changeOnboardingName3Container2);
        changeOnboardingNameContainerViews.add(changeOnboardingName4Container2);
        changeOnboardingNameContainerViews.add(changeOnboardingName5Container2);
        changeOnboardingNameContainerViews.add(changeOnboardingName6Container2);
        changeOnboardingNameContainerViews.add(changeOnboardingName7Container2);
        changeOnboardingNameContainerViews.add(changeOnboardingName8Container2);
        changeOnboardingNameContainerViews.add(changeOnboardingName9Container2);
        changeOnboardingNameContainerViews.add(changeOnboardingName10Container2);
        changeOnboardingNameContainerViews.add(changeOnboardingName11Container2);
        changeOnboardingNameContainerViews.add(changeOnboardingName12Container2);
        changeOnboardingNameContainerViews.add(changeOnboardingName13Container2);
        changeOnboardingNameContainerViews.add(changeOnboardingName14Container2);
        changeOnboardingNameContainerViews.add(changeOnboardingName15Container2);
        changeOnboardingNameContainerViews.add(changeOnboardingName16Container2);
        changeOnboardingNameContainerViews.add(changeOnboardingName17Container2);
        changeOnboardingNameContainerViews.add(changeOnboardingName18Container2);
        changeOnboardingNameContainerViews.add(changeOnboardingName19Container2);
        changeOnboardingNameContainerViews.add(changeOnboardingName20Container2);
        changeOnboardingNameContainerViews.add(changeOnboardingName21Container2);
        changeOnboardingNameContainerViews.add(changeOnboardingName22Container2);
        changeOnboardingNameContainerViews.add(changeOnboardingName23Container2);
        changeOnboardingNameContainerViews.add(changeOnboardingName24Container2);
        changeOnboardingNameContainerViews.add(changeOnboardingName25Container2);

        changeOnboardingName1Text = rootView.findViewById(R.id.changeOnboardingName1Text);
        changeOnboardingName2Text = rootView.findViewById(R.id.changeOnboardingName2Text);
        changeOnboardingName3Text = rootView.findViewById(R.id.changeOnboardingName3Text);
        changeOnboardingName4Text = rootView.findViewById(R.id.changeOnboardingName4Text);
        changeOnboardingName5Text = rootView.findViewById(R.id.changeOnboardingName5Text);
        changeOnboardingName6Text = rootView.findViewById(R.id.changeOnboardingName6Text);
        changeOnboardingName7Text = rootView.findViewById(R.id.changeOnboardingName7Text);
        changeOnboardingName8Text = rootView.findViewById(R.id.changeOnboardingName8Text);
        changeOnboardingName9Text = rootView.findViewById(R.id.changeOnboardingName9Text);
        changeOnboardingName10Text = rootView.findViewById(R.id.changeOnboardingName10Text);
        changeOnboardingName11Text = rootView.findViewById(R.id.changeOnboardingName11Text);
        changeOnboardingName12Text = rootView.findViewById(R.id.changeOnboardingName12Text);
        changeOnboardingName13Text = rootView.findViewById(R.id.changeOnboardingName13Text);
        changeOnboardingName14Text = rootView.findViewById(R.id.changeOnboardingName14Text);
        changeOnboardingName15Text = rootView.findViewById(R.id.changeOnboardingName15Text);
        changeOnboardingName16Text = rootView.findViewById(R.id.changeOnboardingName16Text);
        changeOnboardingName17Text = rootView.findViewById(R.id.changeOnboardingName17Text);
        changeOnboardingName18Text = rootView.findViewById(R.id.changeOnboardingName18Text);
        changeOnboardingName19Text = rootView.findViewById(R.id.changeOnboardingName19Text);
        changeOnboardingName20Text = rootView.findViewById(R.id.changeOnboardingName20Text);
        changeOnboardingName21Text = rootView.findViewById(R.id.changeOnboardingName21Text);
        changeOnboardingName22Text = rootView.findViewById(R.id.changeOnboardingName22Text);
        changeOnboardingName23Text = rootView.findViewById(R.id.changeOnboardingName23Text);
        changeOnboardingName24Text = rootView.findViewById(R.id.changeOnboardingName24Text);
        changeOnboardingName25Text = rootView.findViewById(R.id.changeOnboardingName25Text);

        changeOnboardingName1Text2 = rootView.findViewById(R.id.changeOnboardingName1Text2);
        changeOnboardingName2Text2 = rootView.findViewById(R.id.changeOnboardingName2Text2);
        changeOnboardingName3Text2 = rootView.findViewById(R.id.changeOnboardingName3Text2);
        changeOnboardingName4Text2 = rootView.findViewById(R.id.changeOnboardingName4Text2);
        changeOnboardingName5Text2 = rootView.findViewById(R.id.changeOnboardingName5Text2);
        changeOnboardingName6Text2 = rootView.findViewById(R.id.changeOnboardingName6Text2);
        changeOnboardingName7Text2 = rootView.findViewById(R.id.changeOnboardingName7Text2);
        changeOnboardingName8Text2 = rootView.findViewById(R.id.changeOnboardingName8Text2);
        changeOnboardingName9Text2 = rootView.findViewById(R.id.changeOnboardingName9Text2);
        changeOnboardingName10Text2 = rootView.findViewById(R.id.changeOnboardingName10Text2);
        changeOnboardingName11Text2 = rootView.findViewById(R.id.changeOnboardingName11Text2);
        changeOnboardingName12Text2 = rootView.findViewById(R.id.changeOnboardingName12Text2);
        changeOnboardingName13Text2 = rootView.findViewById(R.id.changeOnboardingName13Text2);
        changeOnboardingName14Text2 = rootView.findViewById(R.id.changeOnboardingName14Text2);
        changeOnboardingName15Text2 = rootView.findViewById(R.id.changeOnboardingName15Text2);
        changeOnboardingName16Text2 = rootView.findViewById(R.id.changeOnboardingName16Text2);
        changeOnboardingName17Text2 = rootView.findViewById(R.id.changeOnboardingName17Text2);
        changeOnboardingName18Text2 = rootView.findViewById(R.id.changeOnboardingName18Text2);
        changeOnboardingName19Text2 = rootView.findViewById(R.id.changeOnboardingName19Text2);
        changeOnboardingName20Text2 = rootView.findViewById(R.id.changeOnboardingName20Text2);
        changeOnboardingName21Text2 = rootView.findViewById(R.id.changeOnboardingName21Text2);
        changeOnboardingName22Text2 = rootView.findViewById(R.id.changeOnboardingName22Text2);
        changeOnboardingName23Text2 = rootView.findViewById(R.id.changeOnboardingName23Text2);
        changeOnboardingName24Text2 = rootView.findViewById(R.id.changeOnboardingName24Text2);
        changeOnboardingName25Text2 = rootView.findViewById(R.id.changeOnboardingName25Text2);

        changeOnboardingNameTextViews.add(changeOnboardingName1Text);
        changeOnboardingNameTextViews.add(changeOnboardingName2Text);
        changeOnboardingNameTextViews.add(changeOnboardingName3Text);
        changeOnboardingNameTextViews.add(changeOnboardingName4Text);
        changeOnboardingNameTextViews.add(changeOnboardingName5Text);
        changeOnboardingNameTextViews.add(changeOnboardingName6Text);
        changeOnboardingNameTextViews.add(changeOnboardingName7Text);
        changeOnboardingNameTextViews.add(changeOnboardingName8Text);
        changeOnboardingNameTextViews.add(changeOnboardingName9Text);
        changeOnboardingNameTextViews.add(changeOnboardingName10Text);
        changeOnboardingNameTextViews.add(changeOnboardingName11Text);
        changeOnboardingNameTextViews.add(changeOnboardingName12Text);
        changeOnboardingNameTextViews.add(changeOnboardingName13Text);
        changeOnboardingNameTextViews.add(changeOnboardingName14Text);
        changeOnboardingNameTextViews.add(changeOnboardingName15Text);
        changeOnboardingNameTextViews.add(changeOnboardingName16Text);
        changeOnboardingNameTextViews.add(changeOnboardingName17Text);
        changeOnboardingNameTextViews.add(changeOnboardingName18Text);
        changeOnboardingNameTextViews.add(changeOnboardingName19Text);
        changeOnboardingNameTextViews.add(changeOnboardingName20Text);
        changeOnboardingNameTextViews.add(changeOnboardingName21Text);
        changeOnboardingNameTextViews.add(changeOnboardingName22Text);
        changeOnboardingNameTextViews.add(changeOnboardingName23Text);
        changeOnboardingNameTextViews.add(changeOnboardingName24Text);
        changeOnboardingNameTextViews.add(changeOnboardingName25Text);

        changeOnboardingNameTextViews.add(changeOnboardingName1Text2);
        changeOnboardingNameTextViews.add(changeOnboardingName2Text2);
        changeOnboardingNameTextViews.add(changeOnboardingName3Text2);
        changeOnboardingNameTextViews.add(changeOnboardingName4Text2);
        changeOnboardingNameTextViews.add(changeOnboardingName5Text2);
        changeOnboardingNameTextViews.add(changeOnboardingName6Text2);
        changeOnboardingNameTextViews.add(changeOnboardingName7Text2);
        changeOnboardingNameTextViews.add(changeOnboardingName8Text2);
        changeOnboardingNameTextViews.add(changeOnboardingName9Text2);
        changeOnboardingNameTextViews.add(changeOnboardingName10Text2);
        changeOnboardingNameTextViews.add(changeOnboardingName11Text2);
        changeOnboardingNameTextViews.add(changeOnboardingName12Text2);
        changeOnboardingNameTextViews.add(changeOnboardingName13Text2);
        changeOnboardingNameTextViews.add(changeOnboardingName14Text2);
        changeOnboardingNameTextViews.add(changeOnboardingName15Text2);
        changeOnboardingNameTextViews.add(changeOnboardingName16Text2);
        changeOnboardingNameTextViews.add(changeOnboardingName17Text2);
        changeOnboardingNameTextViews.add(changeOnboardingName18Text2);
        changeOnboardingNameTextViews.add(changeOnboardingName19Text2);
        changeOnboardingNameTextViews.add(changeOnboardingName20Text2);
        changeOnboardingNameTextViews.add(changeOnboardingName21Text2);
        changeOnboardingNameTextViews.add(changeOnboardingName22Text2);
        changeOnboardingNameTextViews.add(changeOnboardingName23Text2);
        changeOnboardingNameTextViews.add(changeOnboardingName24Text2);
        changeOnboardingNameTextViews.add(changeOnboardingName25Text2);

    }

    private void selectLanguageOkayClicked() {

        setSelectedLanguageAsDefault();

    }

    private void selectVolumeOkayClicked() {

        selectVolumeDialog.dismiss();
        selectVolumeDialog = null;
        isSelectVolumeDialogRunning = false;
    }
    private void selectKonusmaVolumeOkayClicked() {

        selectKonusmaVolumeDialog.dismiss();
        selectKonusmaVolumeDialog = null;
        isSelectKonusmaVolumeDialogRunning = false;

    }


    private void setSelectedLanguageAsDefault() {

        if (selectedSelectLanguageItemIndex == Constants.LANGUAGE_TR) {
            Helper.setSelectedLanguageCode(getActivity(), Constants.LANGUAGE_TR);
        } else if (selectedSelectLanguageItemIndex == Constants.LANGUAGE_EN) {
            Helper.setSelectedLanguageCode(getActivity(), Constants.LANGUAGE_EN);
        } else if (selectedSelectLanguageItemIndex == Constants.LANGUAGE_DE) {
            Helper.setSelectedLanguageCode(getActivity(), Constants.LANGUAGE_DE);
        }

        selectLanguageDialog.dismiss();
        selectLanguageDialog = null;
        isSelectLanguageDialogRunning = false;

//        restartApplication();
        setLanguageAndRebootApplication(selectedSelectLanguageItemIndex);
    }

    private void setLanguageAndRebootApplication(int selectedLanguageTemp) {

        String commandText = null;
        if (selectedLanguageTemp == Constants.LANGUAGE_TR) {
            commandText = "setprop persist.sys.language tr; setprop persist.sys.country TR; setprop ctl.restart zygote";
        } else if (selectedLanguageTemp == Constants.LANGUAGE_EN) {
            commandText = "setprop persist.sys.language en; setprop persist.sys.country TR; setprop ctl.restart zygote";
        } else if (selectedLanguageTemp == Constants.LANGUAGE_DE) {
            commandText = "setprop persist.sys.language de; setprop persist.sys.country TR; setprop ctl.restart zygote";
        }

        if (commandText != null) {

            try {
                Command command = new Command(0, commandText);
                RootTools.getShell(true).add(command).getExitCode();
            } catch (Exception e) {
                e.printStackTrace();
            }

        }

    }


//    private void restartApplication() {
//        Intent mStartActivity = new Intent(getActivity(), SplashSelectorActivity.class);
//        int mPendingIntentId = 135;
//        PendingIntent mPendingIntent = PendingIntent.getActivity(getActivity(), mPendingIntentId, mStartActivity, PendingIntent.FLAG_CANCEL_CURRENT);
//        AlarmManager mgr = (AlarmManager) getActivity().getSystemService(Context.ALARM_SERVICE);
//        mgr.set(AlarmManager.RTC, System.currentTimeMillis() + 100, mPendingIntent);
//        System.exit(0);
//    }

    AlertDialog selectLanguageDialog;
    boolean isSelectLanguageDialogRunning;
    int selectedSelectLanguageItemIndex = 0;
    LinearLayout customLanguageTurkceContainer;
    LinearLayout customLanguageEnglishContainer;
    LinearLayout customLanguageDeutschContainer;
    TextView customLanguageTurkceText;
    TextView customLanguageEnglishText;
    TextView customLanguageDeutschText;

    private void showSelectLanguageDialog() {

        int selectedLanguageCode = Helper.getSelectedLanguageCode(getActivity());
        if (selectedLanguageCode == Constants.LANGUAGE_TR) {
            selectedSelectLanguageItemIndex = Constants.LANGUAGE_TR;
        } else if (selectedLanguageCode == Constants.LANGUAGE_EN) {
            selectedSelectLanguageItemIndex = Constants.LANGUAGE_EN;
        } else if (selectedLanguageCode == Constants.LANGUAGE_DE) {
            selectedSelectLanguageItemIndex = Constants.LANGUAGE_DE;
        }

        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setTitle("");

        View customLayout = getLayoutInflater().inflate(R.layout.custom_select_language_dialog, null);
        builder.setView(customLayout);

        customLanguageTurkceContainer = customLayout.findViewById(R.id.customLanguageTurkceContainer);
        customLanguageTurkceText = customLayout.findViewById(R.id.customLanguageTurkceText);

        customLanguageEnglishContainer = customLayout.findViewById(R.id.customLanguageEnglishContainer);
        customLanguageEnglishText = customLayout.findViewById(R.id.customLanguageEnglishText);

        customLanguageDeutschContainer = customLayout.findViewById(R.id.customLanguageDeutschContainer);
        customLanguageDeutschText = customLayout.findViewById(R.id.customLanguageDeutschText);

        customLanguageTurkceContainer.setBackgroundResource(0);
        customLanguageTurkceText.setTextColor(getResources().getColor(R.color.white));
        customLanguageEnglishContainer.setBackgroundResource(0);
        customLanguageEnglishText.setTextColor(getResources().getColor(R.color.white));
        customLanguageDeutschContainer.setBackgroundResource(0);
        customLanguageDeutschText.setTextColor(getResources().getColor(R.color.white));

        if (selectedLanguageCode == Constants.LANGUAGE_TR) {
            customLanguageTurkceContainer.setBackgroundResource(R.drawable.menu_item_selected);
            customLanguageTurkceText.setTextColor(getResources().getColor(R.color.black));
        } else if (selectedLanguageCode == Constants.LANGUAGE_EN) {
            customLanguageEnglishContainer.setBackgroundResource(R.drawable.menu_item_selected);
            customLanguageEnglishText.setTextColor(getResources().getColor(R.color.black));
        } else if (selectedLanguageCode == Constants.LANGUAGE_DE) {
            customLanguageDeutschContainer.setBackgroundResource(R.drawable.menu_item_selected);
            customLanguageDeutschText.setTextColor(getResources().getColor(R.color.black));
        }

        builder.setOnDismissListener(new DialogInterface.OnDismissListener() {
            @Override
            public void onDismiss(DialogInterface dialog) {
                selectLanguageDialog = null;
                isSelectLanguageDialogRunning = false;
            }
        });
        selectLanguageDialog = builder.create();
        selectLanguageDialog.show();
        isSelectLanguageDialogRunning = true;

    }

    AlertDialog selectVolumeDialog;
    boolean isSelectVolumeDialogRunning;
    boolean isSelectKonusmaVolumeDialogRunning;
    TextView soundLevelText;

    private void showVolumeDialog() {

        AudioManager audioManager = (AudioManager) getActivity().getSystemService(AUDIO_SERVICE);
        int volume_level = audioManager.getStreamVolume(AppRTCAudioManager.SELECTED_SOUND_CHANNEL);
        int maxVolume = audioManager.getStreamMaxVolume(AppRTCAudioManager.SELECTED_SOUND_CHANNEL);

        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setTitle("");

        View customLayout = getLayoutInflater().inflate(R.layout.custom_select_volume_dialog, null);
        builder.setView(customLayout);

        soundLevelText = customLayout.findViewById(R.id.soundLevelText);
        soundLevelText.setText(volume_level + " / " + maxVolume);

        builder.setOnDismissListener(new DialogInterface.OnDismissListener() {
            @Override
            public void onDismiss(DialogInterface dialog) {
                selectVolumeDialog = null;
                isSelectVolumeDialogRunning = false;
            }
        });
        selectVolumeDialog = builder.create();
        selectVolumeDialog.show();
        isSelectVolumeDialogRunning = true;
    }

    //showKonusmaVolumeDialog burada güncellenecek

    AlertDialog selectKonusmaVolumeDialog;
    TextView soundKonusmaLevelText;
    private void showKonusmaVolumeDialog() {

        AudioManager audioManager = (AudioManager) getActivity().getSystemService(AUDIO_SERVICE);
        int volume_level = volume_konusma_level;
        int maxVolume = audioManager.getStreamMaxVolume(AppRTCAudioManager.SELECTED_SOUND_CHANNEL);
        //Log.d(Constants.LOG_TAG, "fey fey ses maxses volume_level=" + volume_level + " maksimum maxVolume=" + maxVolume);

        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setTitle("");

        View customLayout = getLayoutInflater().inflate(R.layout.custom_select_konusma_volume_dialog, null);
        builder.setView(customLayout);

        soundKonusmaLevelText = customLayout.findViewById(R.id.soundKonusmaLevelText);
        soundKonusmaLevelText.setText(volume_level + " / " + maxVolume);

        builder.setOnDismissListener(new DialogInterface.OnDismissListener() {
            @Override
            public void onDismiss(DialogInterface dialog) {
                selectKonusmaVolumeDialog = null;
                isSelectKonusmaVolumeDialogRunning = false;

            }
        });
        selectKonusmaVolumeDialog = builder.create();
        selectKonusmaVolumeDialog.show();
        isSelectKonusmaVolumeDialogRunning = true;
    }


    private void passwordAddOkayButtonClicked() {

        String userPasswordText = getUserPasswordText();

        if (userPasswordText.length() != 4) {
            Helper.showTopMessageBanner((AppCompatActivity) getActivity(), getString(R.string.lutfen_butun_alanlari_doldurun), false);
            return;
        }

        addNewPasswordIfNeeded(userPasswordText);

    }

    private void addNewPasswordIfNeeded(String userpassword) {

        DatabaseHelper databaseHelper = new DatabaseHelper(getActivity());
        DoorPassword doorPassword = databaseHelper.getPasswordByDoorPassword(userpassword);
        if (doorPassword != null) {
            Helper.showTopMessageBanner((AppCompatActivity) getActivity(), getString(R.string.bu_sifre_zaten_bu_daire_icin_kullanimda), false);
            return;
        }

        DoorPassword doorPasswordNew = new DoorPassword();
        doorPasswordNew.setActive(true);
        doorPasswordNew.setDoor(userpassword);
        doorPasswordNew.setIp(daireSelectedForPasswordAdd.getIp());
        doorPasswordNew.setRfid("");

        sendNewPasswordToOtherZilPanels(databaseHelper, doorPasswordNew);

        //burada bu kart şifresini diğer kapılara da gönder
        int newId = databaseHelper.insertPassword(doorPasswordNew);
        doorPasswordNew.setId(newId);
        sifreDialog.dismiss();
        sifreDialog = null;

        isPasswordAddDialogRunning = false;

        Helper.showTopMessageBanner((AppCompatActivity) getActivity(), getString(R.string.sifre_basari_ile_eklendi), true);

//        label ekleme için dialog aç
        showAddLabelToPasswordDialog(doorPasswordNew);

    }

    boolean isAddLabelToPasswordDialogRunning = false;
    AlertDialog addLabelToPasswordDialog;
    DoorPassword doorPasswordForAddLabel;

    private void showAddLabelToPasswordDialog(DoorPassword doorPassword) {

        doorPasswordForAddLabel = doorPassword;

        selectedChangeNameItemIndex = 0;

        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setTitle("");

        View customLayout = getLayoutInflater().inflate(R.layout.custom_add_label_dialog, null);
        builder.setView(customLayout);

        setChangeNameLayouts(customLayout);

        builder.setOnDismissListener(new DialogInterface.OnDismissListener() {
            @Override
            public void onDismiss(DialogInterface dialog) {
                addLabelToPasswordDialog = null;
                isAddLabelToPasswordDialogRunning = false;
            }
        });
        addLabelToPasswordDialog = builder.create();
        addLabelToPasswordDialog.show();
        isAddLabelToPasswordDialogRunning = true;

    }

    private void sendNewPasswordToOtherZilPanels(DatabaseHelper databaseHelper, DoorPassword doorPasswordNew) {

        ZilPanel zilPanelSelf = databaseHelper.getZilPanelByIP(Utils.getIPAddress(true));
        ArrayList<ZilPanel> arrayListZilPanels = databaseHelper.getOtherZilPanelleriByBlokNo(zilPanelSelf.getBlok());

        Iterator<ZilPanel> iterator = arrayListZilPanels.iterator();
        while (iterator.hasNext()) {
            ZilPanel zilPanel = iterator.next();

            String destinationIP = zilPanel.getIp();
            ComPackageModel model = new ComPackageModel();
            model.setOpe_type(Constants.OPERATION_ADD_NEW_PASSWORD);
            model.setDoorPassword(doorPasswordNew);

            TCPHelper.sendMessageToIP(getActivity(), destinationIP, model);
        }

        sendPasswordAddedToSiteZilPaneller(databaseHelper, doorPasswordNew);
    }

    private void sendNewPasswordToOtherSiteZilPanels(DatabaseHelper databaseHelper, DoorPassword doorPasswordNew) {

//        ZilPanel zilPanelSelf = databaseHelper.getZilPanelByIP(Utils.getIPAddress(true));
        ArrayList<ZilPanelSite> arrayListZilPanels = databaseHelper.getSiteZilPanelleri(false);

        Iterator<ZilPanelSite> iterator = arrayListZilPanels.iterator();
        while (iterator.hasNext()) {
            ZilPanelSite zilPanelSite = iterator.next();

            String destinationIP = zilPanelSite.getIp();
            ComPackageModel model = new ComPackageModel();
            model.setOpe_type(Constants.OPERATION_ADD_NEW_PASSWORD);
            model.setDoorPassword(doorPasswordNew);

            TCPHelper.sendMessageToIP(getActivity(), destinationIP, model);
        }

        sendPasswordAddedToSiteZilPaneller(databaseHelper, doorPasswordNew);
    }

    private void sendPasswordDeletedToOtherZilPanels(DatabaseHelper databaseHelper, DoorPassword doorPasswordForDelete) {

        ZilPanel zilPanelSelf = databaseHelper.getZilPanelByIP(Utils.getIPAddress(true));
        ArrayList<ZilPanel> arrayListZilPanels = databaseHelper.getOtherZilPanelleriByBlokNo(zilPanelSelf.getBlok());

        Iterator<ZilPanel> iterator = arrayListZilPanels.iterator();
        while (iterator.hasNext()) {
            ZilPanel zilPanel = iterator.next();

            String destinationIP = zilPanel.getIp();
            ComPackageModel model = new ComPackageModel();
            model.setOpe_type(Constants.OPERATION_DELETE_PASSWORD);
            model.setDoorPassword(doorPasswordForDelete);

            TCPHelper.sendMessageToIP(getActivity(), destinationIP, model);
        }

        sendPasswordDeletedToSiteZilPanel(databaseHelper, doorPasswordForDelete);

    }

//    private void sendPasswordDeletedToOtherSiteZilPanels(DatabaseHelper databaseHelper, DoorPassword doorPasswordForDelete) {
//
////        ZilPanel zilPanelSelf = databaseHelper.getZilPanelByIP(Utils.getIPAddress(true));
//        ArrayList<ZilPanelSite> arrayListZilPanels = databaseHelper.getSiteZilPanelleri(false);
//
//        Iterator<ZilPanelSite> iterator = arrayListZilPanels.iterator();
//        while (iterator.hasNext()) {
//            ZilPanelSite zilPanelSite = iterator.next();
//
//            String destinationIP = zilPanelSite.getIp();
//            ComPackageModel model = new ComPackageModel();
//            model.setOpe_type(Constants.OPERATION_DELETE_PASSWORD);
//            model.setDoorPassword(doorPasswordForDelete);
//
//            TCPHelper.sendMessageToIP(getActivity(), destinationIP, model);
//        }
//
//        sendPasswordDeletedToSiteZilPanel(databaseHelper, doorPasswordForDelete);
//
//    }

    private void sendPasswordAddedToSiteZilPaneller(DatabaseHelper databaseHelper, DoorPassword doorPasswordNew) {

        ArrayList<ZilPanelSite> zilPanelSites = databaseHelper.getSiteZilPanelleri(true);
        Iterator<ZilPanelSite> iterator = zilPanelSites.iterator();
        while (iterator.hasNext()) {
            ZilPanelSite zilPanelSite = iterator.next();
            String destinationIP = zilPanelSite.getIp();

            ComPackageModel model = new ComPackageModel();
            model.setOpe_type(Constants.OPERATION_ADD_NEW_PASSWORD);
            model.setDoorPassword(doorPasswordNew);

            TCPHelper.sendMessageToIP(getActivity(), destinationIP, model);
        }

    }

    private void sendPasswordDeletedToSiteZilPanel(DatabaseHelper databaseHelper, DoorPassword doorPasswordForDelete) {

        ArrayList<ZilPanelSite> zilPanelSites = databaseHelper.getSiteZilPanelleri(true);
        Iterator<ZilPanelSite> iterator = zilPanelSites.iterator();
        while (iterator.hasNext()) {
            ZilPanelSite zilPanelSite = iterator.next();
            String destinationIP = zilPanelSite.getIp();

            ComPackageModel model = new ComPackageModel();
            model.setOpe_type(Constants.OPERATION_DELETE_PASSWORD);
            model.setDoorPassword(doorPasswordForDelete);

            TCPHelper.sendMessageToIP(getActivity(), destinationIP, model);
        }

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
        return userText;
    }

    private void selectPasswordOkayButtonClicked() {

        ArrayList<DoorPassword> arrayListPasswords = selectPasswordAdapter.getArrayList();
        DoorPassword passwordSelected = arrayListPasswords.get(selectedSelectPasswordItemIndex);

        if (passwordSelected == null) {
            return;
        }

        boolean isRFID = true;
        String rfid = passwordSelected.getRfid();
        if (rfid == null || rfid.isEmpty()) {
            isRFID = false;
        }
        DatabaseHelper databaseHelper = new DatabaseHelper(getActivity());

        //bu noktada diğer zil panellerine de haber ver
        sendPasswordDeletedToOtherZilPanels(databaseHelper, passwordSelected);

        if (isRFID) {
            databaseHelper.deletePasswordByIPAndDoorRFID(passwordSelected);
        } else {
            databaseHelper.deletePasswordByIPAndDoorPassword(passwordSelected);
        }

        isSelectPasswordDialogRunning = false;
        selectPassworDialog.dismiss();
        selectDaireDialog = null;

        Helper.showTopMessageBanner((AppCompatActivity) getActivity(), getString(R.string.basari_ile_silindi), true);

    }

    private void selectDaireOkayButtonClicked() {

        ArrayList<Daire> arrayListDaires = selectDaireAdapter.getArrayList();
        Daire daireSelected = arrayListDaires.get(selectedSelectDaireItemIndex);

        if (daireSelected == null) {
            return;
        }

        isSelectDaireDialogRunning = false;
        selectDaireDialog.dismiss();
        selectDaireDialog = null;
        daireSelectedForPasswordAdd = daireSelected;

        if (!isForDoorPassword) {// burada rfid işlemlerini yapmak için ayrılıyoruz

            if (isPasswordForAdd) {
                showRFIDAddDialogForDaire();
            } else {
                showPasswordRemoveForDaireDialog();
            }
            return;
        }

        if (isPasswordForAdd) {
//            eklenecek şifreyi al ve kaydet
            showPasswordAddForDaireDialog();
        } else {
//            hangi şifreyi sileceğini göster
            showPasswordRemoveForDaireDialog();
        }

    }

    boolean isRFIDCountDownDialogRunning = false;
    AlertDialog RFIDcountDownTimerDialog;
    int timeRemaining = 11;
    Timer timerCountDown;

    private void showRFIDAddDialogForDaire() {

        timeRemaining = 11;

        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setTitle("");

        View customLayout = getLayoutInflater().inflate(R.layout.custom_rfid_add_countdown_dialog, null);
        builder.setView(customLayout);

        TextView rfidCountdownDialogDaireName = customLayout.findViewById(R.id.rfidCountdownDialogDaireName);
        rfidCountdownDialogDaireName.setText(daireSelectedForPasswordAdd.getIsim() + " " + daireSelectedForPasswordAdd.getSoyisim());

        TextView rfidCountdownDialogRemainingTime = customLayout.findViewById(R.id.rfidCountdownDialogRemainingTime);
        rfidCountdownDialogRemainingTime.setText("10");

        builder.setOnDismissListener(new DialogInterface.OnDismissListener() {
            @Override
            public void onDismiss(DialogInterface dialog) {
                RFIDcountDownTimerDialog = null;
                isRFIDCountDownDialogRunning = false;
            }
        });
        RFIDcountDownTimerDialog = builder.create();
        RFIDcountDownTimerDialog.show();
        isRFIDCountDownDialogRunning = true;

        IntentFilter filter = new IntentFilter();
        filter.addAction("com.netelsan.panel.AddNewRFID");
        getActivity().registerReceiver(addNewRFIDBroadcastReceiver, filter);

        timerCountDown = new Timer();
        timerCountDown.schedule(new TimerTask() {
            @Override
            public void run() {

                if (timeRemaining == 0) {

                    if (addNewRFIDBroadcastReceiver != null) {
                        getActivity().unregisterReceiver(addNewRFIDBroadcastReceiver);
                    }
                    timerCountDown.cancel();
                    timerCountDown.purge();
                    timerCountDown = null;

                    RFIDcountDownTimerDialog.dismiss();

                    getActivity().runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            Helper.showTopMessageBanner((AppCompatActivity) getActivity(), getString(R.string.islem_iptal_edildi), false);
                        }
                    });

                    return;
                }
                timeRemaining = timeRemaining - 1;

                getActivity().runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        rfidCountdownDialogRemainingTime.setText(timeRemaining + "");
                    }
                });

            }
        }, 0, 1000);

    }

    BroadcastReceiver addNewRFIDBroadcastReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            Log.d(Constants.LOG_TAG, "addNewRFIDBroadcastReceiver onReceive");

            if (intent == null) {
                return;
            }
            Bundle bundle = intent.getExtras();
            if (bundle == null) {
                return;
            }

            String RFIDKey = bundle.getString("rfid");
            Log.d(Constants.LOG_TAG, "addNewRFIDBroadcastReceiver onReceive RFIDKey=" + RFIDKey);

            if (addNewRFIDBroadcastReceiver != null) {
                getActivity().unregisterReceiver(addNewRFIDBroadcastReceiver);
            }
            timerCountDown.cancel();
            timerCountDown.purge();
            timerCountDown = null;

            RFIDcountDownTimerDialog.dismiss();

            DatabaseHelper databaseHelper = new DatabaseHelper(getActivity());
            DoorPassword doorPassword = databaseHelper.getPasswordByRFID(RFIDKey);
            if (doorPassword != null) {
                Helper.showTopMessageBanner((AppCompatActivity) getActivity(), getString(R.string.bu_kart_daha_önce_kullanilmis), false);
                return;
            }

            DoorPassword doorPasswordNew = new DoorPassword();
            doorPasswordNew.setActive(true);
            doorPasswordNew.setDoor("");
            doorPasswordNew.setIp(daireSelectedForPasswordAdd.getIp());
            doorPasswordNew.setRfid(RFIDKey);

            sendNewPasswordToOtherZilPanels(databaseHelper, doorPasswordNew);
            sendNewPasswordToOtherSiteZilPanels(databaseHelper, doorPasswordNew);

            int newId = databaseHelper.insertPassword(doorPasswordNew);
            doorPasswordNew.setId(newId);

            RFIDcountDownTimerDialog.dismiss();
            RFIDcountDownTimerDialog = null;

            isRFIDCountDownDialogRunning = false;

            Helper.showTopMessageBanner((AppCompatActivity) getActivity(), getString(R.string.kart_basari_ile_atandi), true);

//        label ekleme için dialog aç
            showAddLabelToPasswordDialog(doorPasswordNew);

        }
    };

    AlertDialog sifreDialog;
    boolean isPasswordAddDialogRunning;
    int selectedSifrePart = 1;// 1 = text1, 2 = text2, 3 = text3, 4 = text4
    Daire daireSelectedForPasswordAdd = null;
    LinearLayout sifreDialogText1Container;
    LinearLayout sifreDialogText2Container;
    LinearLayout sifreDialogText3Container;
    LinearLayout sifreDialogText4Container;
    TextView sifreDialogText1;
    TextView sifreDialogText2;
    TextView sifreDialogText3;
    TextView sifreDialogText4;

    private void showPasswordAddForDaireDialog() {

        selectedSifrePart = 1;

        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setTitle("");

        View customLayout = getLayoutInflater().inflate(R.layout.custom_password_4_dialog, null);
        builder.setView(customLayout);

        TextView customPasswordTitle = customLayout.findViewById(R.id.customPasswordTitle);
        customPasswordTitle.setText(daireSelectedForPasswordAdd.getIsim() + " " + daireSelectedForPasswordAdd.getSoyisim() + " için yeni şifre girin");

        sifreDialogText1Container = customLayout.findViewById(R.id.sifreDialogText1Container);
        sifreDialogText1 = customLayout.findViewById(R.id.sifreDialogText1);

        sifreDialogText2Container = customLayout.findViewById(R.id.sifreDialogText2Container);
        sifreDialogText2 = customLayout.findViewById(R.id.sifreDialogText2);

        sifreDialogText3Container = customLayout.findViewById(R.id.sifreDialogText3Container);
        sifreDialogText3 = customLayout.findViewById(R.id.sifreDialogText3);

        sifreDialogText4Container = customLayout.findViewById(R.id.sifreDialogText4Container);
        sifreDialogText4 = customLayout.findViewById(R.id.sifreDialogText4);

        builder.setOnDismissListener(new DialogInterface.OnDismissListener() {
            @Override
            public void onDismiss(DialogInterface dialog) {
                sifreDialog = null;
                isPasswordAddDialogRunning = false;
            }
        });
        sifreDialog = builder.create();
        sifreDialog.show();
        isPasswordAddDialogRunning = true;

    }

    private void showPasswordRemoveForDaireDialog() {
//        burada daire seçilmiş oluyor. o dairenin şifreleri getir ve listele

        DatabaseHelper databaseHelper = new DatabaseHelper(getActivity());
        ArrayList<DoorPassword> arrayListDoorPasswords = databaseHelper.getPasswordsByIP(daireSelectedForPasswordAdd.getIp(), isForDoorPassword);

        if (arrayListDoorPasswords.size() == 0) {
            Helper.showTopMessageBanner((AppCompatActivity) getActivity(), getString(R.string.henuz_sifre_atanmamis), false);
            return;
        }
        showAllPasswordsForDelete(arrayListDoorPasswords);
//        Log.d(Constants.LOG_TAG, "showPasswordRemoveForDaireDialog size=" + arrayListDoorPasswords.size());

    }

    AlertDialog selectPassworDialog;
    boolean isSelectPasswordDialogRunning;
    int selectedSelectPasswordItemIndex = 0;
    SelectPasswordAdapter selectPasswordAdapter;
    RecyclerView recyclerViewSelectPassword;

    private void showAllPasswordsForDelete(ArrayList<DoorPassword> arrayListDoorPasswords) {
        selectedSelectPasswordItemIndex = 0;

        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setTitle("");

        View customLayout = getLayoutInflater().inflate(R.layout.custom_select_password_dialog, null);
        builder.setView(customLayout);

        if (arrayListDoorPasswords.size() > 0) {
            DoorPassword doorPassword = arrayListDoorPasswords.get(0);

            doorPassword.setSelected(true);
            arrayListDoorPasswords.set(0, doorPassword);

        }

        selectPasswordAdapter = new SelectPasswordAdapter(getActivity(), arrayListDoorPasswords, isForDoorPassword);
        recyclerViewSelectPassword = customLayout.findViewById(R.id.recyclerViewSelectPassword);
        recyclerViewSelectPassword.setAdapter(selectPasswordAdapter);

        builder.setOnDismissListener(new DialogInterface.OnDismissListener() {
            @Override
            public void onDismiss(DialogInterface dialog) {
                selectPassworDialog = null;
                isSelectPasswordDialogRunning = false;
            }
        });
        selectPassworDialog = builder.create();
        selectPassworDialog.show();
        isSelectPasswordDialogRunning = true;
    }


    AlertDialog selectDaireDialog;
    boolean isSelectDaireDialogRunning;
    int selectedSelectDaireItemIndex = 0;
    boolean isPasswordForAdd;
    boolean isForDoorPassword;
    SelectDaireAdapter selectDaireAdapter;
    RecyclerView recyclerViewSelectDaire;

    private void showSelectDaireDialog(boolean isAdd, boolean isForDoorPassword) {

        isPasswordForAdd = isAdd;
        this.isForDoorPassword = isForDoorPassword;
        selectedSelectDaireItemIndex = 0;

        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setTitle("");

        View customLayout = getLayoutInflater().inflate(R.layout.custom_select_daire_dialog, null);
        builder.setView(customLayout);

        DatabaseHelper databaseHelper = new DatabaseHelper(getActivity());
        ZilPanel zilPanelSelf = databaseHelper.getZilPanelByIP(Utils.getIPAddress(true));
        ArrayList<Daire> arrayListDaires = databaseHelper.getDairelerForBlok(zilPanelSelf.getBlok());

        if (arrayListDaires.size() > 0) {
            Daire daire = arrayListDaires.get(0);

            daire.setSelected(true);
            arrayListDaires.set(0, daire);
        }

        selectDaireAdapter = new SelectDaireAdapter(getActivity(), arrayListDaires);
        recyclerViewSelectDaire = customLayout.findViewById(R.id.recyclerViewSelectDaire);
        recyclerViewSelectDaire.setAdapter(selectDaireAdapter);

        builder.setOnDismissListener(new DialogInterface.OnDismissListener() {
            @Override
            public void onDismiss(DialogInterface dialog) {
                selectDaireDialog = null;
                isSelectDaireDialogRunning = false;
            }
        });
        selectDaireDialog = builder.create();
        selectDaireDialog.show();
        isSelectDaireDialogRunning = true;

    }

    private void navigateSelectPasswordMenu(int direction) {

        ArrayList<DoorPassword> arrayListPasswords = selectPasswordAdapter.getArrayList();
        int totalSize = arrayListPasswords.size();
        if (totalSize == 0) {
            return;
        }

        int postSelectedIndex = selectedSelectPasswordItemIndex;

        if (direction == Constants.MENU_NAVIGATION_UP) {
            if (selectedSelectPasswordItemIndex == 0) {
                selectedSelectPasswordItemIndex = totalSize - 1;
            } else {
                selectedSelectPasswordItemIndex = selectedSelectPasswordItemIndex - 1;
            }
        } else if (direction == Constants.MENU_NAVIGATION_DOWN) {
            if (selectedSelectPasswordItemIndex == (totalSize - 1)) {
                selectedSelectPasswordItemIndex = 0;
            } else {
                selectedSelectPasswordItemIndex = selectedSelectPasswordItemIndex + 1;
            }
        }

        DoorPassword passwordPost = arrayListPasswords.get(postSelectedIndex);
        passwordPost.setSelected(false);
        arrayListPasswords.set(postSelectedIndex, passwordPost);

        DoorPassword passwordSelected = arrayListPasswords.get(selectedSelectPasswordItemIndex);
        passwordSelected.setSelected(true);
        arrayListPasswords.set(selectedSelectPasswordItemIndex, passwordSelected);

        selectPasswordAdapter.setArrayList(arrayListPasswords);
        selectPasswordAdapter.notifyItemChanged(postSelectedIndex);
        selectPasswordAdapter.notifyItemChanged(selectedSelectPasswordItemIndex);

        recyclerViewSelectPassword.scrollToPosition(selectedSelectPasswordItemIndex);
    }


    private void navigateSifreMenu(int direction) {

        ArrayList<Daire> arrayListDaires = selectDaireAdapter.getArrayList();
        int totalSize = arrayListDaires.size();
        if (totalSize == 0) {
            return;
        }

        int postSelectedIndex = selectedSelectDaireItemIndex;

        if (direction == Constants.MENU_NAVIGATION_UP) {
            if (selectedSelectDaireItemIndex == 0) {
                selectedSelectDaireItemIndex = totalSize - 1;
            } else {
                selectedSelectDaireItemIndex = selectedSelectDaireItemIndex - 1;
            }
        } else if (direction == Constants.MENU_NAVIGATION_DOWN) {
            if (selectedSelectDaireItemIndex == (totalSize - 1)) {
                selectedSelectDaireItemIndex = 0;
            } else {
                selectedSelectDaireItemIndex = selectedSelectDaireItemIndex + 1;
            }
        }

        Daire dairePost = arrayListDaires.get(postSelectedIndex);
        dairePost.setSelected(false);
        arrayListDaires.set(postSelectedIndex, dairePost);

        Daire daireNewSelected = arrayListDaires.get(selectedSelectDaireItemIndex);
        daireNewSelected.setSelected(true);
        arrayListDaires.set(selectedSelectDaireItemIndex, daireNewSelected);

        selectDaireAdapter.setArrayList(arrayListDaires);
        selectDaireAdapter.notifyItemChanged(postSelectedIndex);
        selectDaireAdapter.notifyItemChanged(selectedSelectDaireItemIndex);

        recyclerViewSelectDaire.scrollToPosition(selectedSelectDaireItemIndex);
    }

    private void bottomMenuBackClicked() {

        if (isSelectDaireDialogRunning) {
            selectDaireDialog.dismiss();
            selectDaireDialog = null;
            return;
        }

        if (isPasswordAddDialogRunning) {
            sifreDialog.dismiss();
            sifreDialog = null;
            return;
        }

        if (isSelectPasswordDialogRunning) {
            selectPassworDialog.dismiss();
            selectPassworDialog = null;
            return;
        }

        if (isSelectLanguageDialogRunning) {
            selectLanguageDialog.dismiss();
            selectLanguageDialog = null;
            return;
        }

        if (isSelectVolumeDialogRunning) {
            selectVolumeDialog.dismiss();
            selectVolumeDialog = null;
            return;
        }
        if (isSelectKonusmaVolumeDialogRunning) {
            selectKonusmaVolumeDialog.dismiss();
            selectKonusmaVolumeDialog = null;
            return;
        }

        if (isChangeNameDialogRunning) {
            changeNameDialog.dismiss();
            changeNameDialog = null;
            return;
        }

        if (isChangeOnboardingNameDialogRunning) {
            changeOnboardingNameDialog.dismiss();
            changeOnboardingNameDialog = null;
            return;
        }

        if (isAddLabelToPasswordDialogRunning) {
            addLabelToPasswordDialog.dismiss();
            addLabelToPasswordDialog = null;
            return;
        }

        if (isRFIDCountDownDialogRunning) {

            RFIDcountDownTimerDialog.dismiss();
            RFIDcountDownTimerDialog = null;

            if (addNewRFIDBroadcastReceiver != null) {
                getActivity().unregisterReceiver(addNewRFIDBroadcastReceiver);
            }

            timerCountDown.cancel();
            timerCountDown.purge();
            timerCountDown = null;

            return;
        }

        if (isRFIDRemoveCountDownDialogRunning) {

            RFIDRemoveCountDownTimerDialog.dismiss();
            RFIDRemoveCountDownTimerDialog = null;

            if (removeRFIDBroadcastReceiver != null) {
                getActivity().unregisterReceiver(removeRFIDBroadcastReceiver);
//                removeRFIDBroadcastReceiver = null;
            }

            timerCountDownForRemove.cancel();
            timerCountDownForRemove.purge();
            timerCountDownForRemove = null;

            return;
        }

        MainActivity mainActivity = (MainActivity) getActivity();
        mainActivity.displayView(Constants.SCREEN_AYARLAR, null);
    }

}

package com.netelsan.ipinterkompanel.fragments;

import static android.content.Context.AUDIO_SERVICE;
import static androidx.core.content.ContextCompat.getSystemService;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.SharedPreferences;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.netelsan.ipinterkompanel.Constants;
import com.netelsan.ipinterkompanel.Helper;
import com.netelsan.ipinterkompanel.R;
import com.netelsan.ipinterkompanel.activity.InitializeActivity;
import com.netelsan.ipinterkompanel.activity.MainActivity;
import com.netelsan.ipinterkompanel.listener.KeyPadListener;
import com.netelsan.ipinterkompanel.service.KeyPadService;

public class IdleMenuFragment extends BaseFragment implements KeyPadListener, ServiceConnection {

    LinearLayout menuItemRehber;
    TextView menuItemRehberText;

    LinearLayout menuItemGorevliyiAra;
    TextView menuItemGorevliyiAraText;

    LinearLayout menuItemGuvenlikAra;
    TextView menuItemGuvenlikAraText;

    LinearLayout menuItemDaireAra;
    TextView menuItemDaireAraText;

    LinearLayout menuItemKapiAc;
    TextView menuItemKapiAcText;

    LinearLayout menuItemAyarlar;
    TextView menuItemAyarlarText;

    ScrollView idleMenuScrollView;

    MediaPlayer mediaPlayerBuzzer;
    AudioManager mAudioManager;

    Helper helper;

    AudioManager audioManager;
    private static int selectedMenuId = Constants.MENU_DAIRE_ARA;

    public IdleMenuFragment() {
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {

        View rootview = inflater.inflate(R.layout.fragment_idle_menu, null);
        if (mediaPlayerBuzzer != null) {
            mediaPlayerBuzzer.stop();
            mediaPlayerBuzzer.release();
            mediaPlayerBuzzer = null;
        }


       // audioManager.setStreamVolume(AudioManager.STREAM_MUSIC, audioManager.getStreamMaxVolume(AudioManager.STREAM_MUSIC) - 12, 0);//8
        mediaPlayerBuzzer = MediaPlayer.create(getActivity(), R.raw.daire_araniyor);
        mediaPlayerBuzzer.setAudioStreamType(AudioManager.STREAM_MUSIC);
        mediaPlayerBuzzer.start();

        menuItemRehber = rootview.findViewById(R.id.menuItemRehber);
        menuItemRehberText = rootview.findViewById(R.id.menuItemRehberText);

        menuItemGorevliyiAra = rootview.findViewById(R.id.menuItemGorevliyiAra);
        menuItemGorevliyiAraText = rootview.findViewById(R.id.menuItemGorevliyiAraText);

        menuItemGuvenlikAra = rootview.findViewById(R.id.menuItemGuvenlikAra);
        menuItemGuvenlikAraText = rootview.findViewById(R.id.menuItemGuvenlikAraText);

        menuItemDaireAra = rootview.findViewById(R.id.menuItemDaireAra);
        menuItemDaireAraText = rootview.findViewById(R.id.menuItemDaireAraText);

        menuItemKapiAc = rootview.findViewById(R.id.menuItemKapiAc);
        menuItemKapiAcText = rootview.findViewById(R.id.menuItemKapiAcText);

        menuItemAyarlar = rootview.findViewById(R.id.menuItemAyarlar);
        menuItemAyarlarText = rootview.findViewById(R.id.menuItemAyarlarText);

        idleMenuScrollView = rootview.findViewById(R.id.idleMenuScrollView);

        selectMenu(selectedMenuId);

//        setLayoutVisibilities();

        return rootview;
    }

//    private void setLayoutVisibilities() {
//
//        boolean isZilForSite = Helper.isZilForSite(getActivity());
//        if(isZilForSite) {
//            menuItemAyarlar.setVisibility(View.GONE);
//        } else {
//            menuItemAyarlar.setVisibility(View.VISIBLE);
//        }
//
//    }

    @Override
    public void onServiceConnected(ComponentName name, IBinder service) {
        keyPadService = ((KeyPadService.MyBinder) service).getService();
        keyPadService.addListener(IdleMenuFragment.this, IdleMenuFragment.class.getName());
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
            keyPadService.addListener(IdleMenuFragment.this, IdleMenuFragment.class.getName());
        }
    }

    @Override
    public void onPause() {
        super.onPause();

        if (keyPadService != null) {
            keyPadService.removeListener(IdleMenuFragment.class.getName());
        }
    }

    @Override
    public void onKeyPressed(String keyCode) {

        getActivity().runOnUiThread(new Runnable() {
            @Override
            public void run() {

                Log.d(Constants.LOG_TAG, "IdleMenuFragment onKeyPressed keyCode=" + keyCode);

                if (keyCode.equals(Constants.KEYPAD_BACK)) {
                    bottomMenuBackClicked();
                } else if (keyCode.equals(Constants.KEYPAD_UP)) {
                    menuNavigationClicked(Constants.MENU_NAVIGATION_UP);
                } else if (keyCode.equals(Constants.KEYPAD_DOWN)) {
                    menuNavigationClicked(Constants.MENU_NAVIGATION_DOWN);
                } else if (keyCode.equals(Constants.KEYPAD_HOME)) {
                    bottomMenuOkayClicked();
                } else if (keyCode.equals(Constants.KEYPAD_CALL)) {
                    bottomNumbersClicked(null);
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

    private void bottomNumbersClicked(String keyCode) {

        Bundle bundle = null;
        if (keyCode != null) {
            bundle = new Bundle();
            bundle.putString("keyCode", keyCode);
        }

        MainActivity mainActivity = (MainActivity) getActivity();
        mainActivity.displayView(Constants.SCREEN_DAIRE_ARA, bundle);
    }

    private void bottomLockClicked() {
        MainActivity mainActivity = (MainActivity) getActivity();
        mainActivity.displayView(Constants.SCREEN_KAPI_AC, null);
    }

    private void bottomMenuOkayClicked() {

        showSelectedMenuScreen();

    }

    private void showSelectedMenuScreen() {

        MainActivity mainActivity = (MainActivity) getActivity();

        if (selectedMenuId == Constants.MENU_DAIRE_ARA) {
            mainActivity.displayView(Constants.SCREEN_DAIRE_ARA, null);
        } else if (selectedMenuId == Constants.MENU_REHBER) {
            mainActivity.displayView(Constants.SCREEN_REHBER, null);
        } else if (selectedMenuId == Constants.MENU_GOREVLIYI_ARA) {
            mainActivity.displayView(Constants.SCREEN_GOREVLI, null);
        } else if (selectedMenuId == Constants.MENU_GUVENLIK_ARA) {
            mainActivity.displayView(Constants.SCREEN_GUVENLIK, null);
        } else if (selectedMenuId == Constants.MENU_KAPI_AC) {
            mainActivity.displayView(Constants.SCREEN_KAPI_AC, null);
        } else if (selectedMenuId == Constants.MENU_AYARLAR) {
            mainActivity.displayView(Constants.SCREEN_AYARLAR, null);
        }

    }

    private void menuNavigationClicked(int direction) {

        if (direction == Constants.MENU_NAVIGATION_UP) {

            if (selectedMenuId == Constants.MENU_DAIRE_ARA) {
                selectedMenuId = Constants.MENU_AYARLAR;
            } else {
                selectedMenuId = selectedMenuId - 1;
            }

        } else if (direction == Constants.MENU_NAVIGATION_DOWN) {

            if (selectedMenuId == Constants.MENU_AYARLAR) {
                selectedMenuId = Constants.MENU_DAIRE_ARA;
            } else {
                selectedMenuId = selectedMenuId + 1;
            }

        }

        selectMenu(selectedMenuId);

    }

    private void selectMenu(int menuId) {

        unselectAllMenus();
        SharedPreferences sharedPreferences = getActivity().getSharedPreferences(Constants.SHARED_PREFS, 0);
        boolean isEnabled = sharedPreferences.getBoolean(Constants.KEY_TUS_SESI, false);
        Log.d("tus","tus sesi: "+ isEnabled);


        if (menuId == Constants.MENU_DAIRE_ARA) {
            if (mediaPlayerBuzzer != null) {
                mediaPlayerBuzzer.stop();
                mediaPlayerBuzzer.release();
                mediaPlayerBuzzer = null;
            }
            if(isEnabled)
            {
                mediaPlayerBuzzer = MediaPlayer.create(getActivity(), R.raw.menu_1_tone);
                mediaPlayerBuzzer.setAudioStreamType(AudioManager.STREAM_MUSIC);
                mediaPlayerBuzzer.start();
            }

            idleMenuScrollView.fullScroll(ScrollView.FOCUS_UP);
            menuItemDaireAra.setBackgroundResource(R.drawable.menu_item_selected);
            menuItemDaireAraText.setTextColor(getResources().getColor(R.color.black));
        } else if (menuId == Constants.MENU_REHBER) {
            if (mediaPlayerBuzzer != null) {
                mediaPlayerBuzzer.stop();
                mediaPlayerBuzzer.release();
                mediaPlayerBuzzer = null;
            }
            if(isEnabled)
            {
                mediaPlayerBuzzer = MediaPlayer.create(getActivity(), R.raw.menu_2_tone);
                mediaPlayerBuzzer.setAudioStreamType(AudioManager.STREAM_MUSIC);
                mediaPlayerBuzzer.start();
            }

            menuItemRehber.setBackgroundResource(R.drawable.menu_item_selected);
            menuItemRehberText.setTextColor(getResources().getColor(R.color.black));
        } else if (menuId == Constants.MENU_GOREVLIYI_ARA) {
            if (mediaPlayerBuzzer != null) {
                mediaPlayerBuzzer.stop();
                mediaPlayerBuzzer.release();
                mediaPlayerBuzzer = null;
            }
            if(isEnabled)
            {
                mediaPlayerBuzzer = MediaPlayer.create(getActivity(), R.raw.menu_3_tone);
                mediaPlayerBuzzer.setAudioStreamType(AudioManager.STREAM_MUSIC);
                mediaPlayerBuzzer.start();
            }

            menuItemGorevliyiAra.setBackgroundResource(R.drawable.menu_item_selected);
            menuItemGorevliyiAraText.setTextColor(getResources().getColor(R.color.black));
        } else if (menuId == Constants.MENU_GUVENLIK_ARA) {
            if (mediaPlayerBuzzer != null) {
                mediaPlayerBuzzer.stop();
                mediaPlayerBuzzer.release();
                mediaPlayerBuzzer = null;
            }
            if(isEnabled)
            {
                mediaPlayerBuzzer = MediaPlayer.create(getActivity(), R.raw.menu_4_tone);
                mediaPlayerBuzzer.setAudioStreamType(AudioManager.STREAM_MUSIC);
                mediaPlayerBuzzer.start();
            }

            menuItemGuvenlikAra.setBackgroundResource(R.drawable.menu_item_selected);
            menuItemGuvenlikAraText.setTextColor(getResources().getColor(R.color.black));
        } else if (menuId == Constants.MENU_KAPI_AC) {
            if (mediaPlayerBuzzer != null) {
                mediaPlayerBuzzer.stop();
                mediaPlayerBuzzer.release();
                mediaPlayerBuzzer = null;
            }
            if(isEnabled)
            {
                mediaPlayerBuzzer = MediaPlayer.create(getActivity(), R.raw.menu_5_tone);
                mediaPlayerBuzzer.setAudioStreamType(AudioManager.STREAM_MUSIC);
                mediaPlayerBuzzer.start();
            }

            menuItemKapiAc.setBackgroundResource(R.drawable.menu_item_selected);
            menuItemKapiAcText.setTextColor(getResources().getColor(R.color.black));
        } else if (menuId == Constants.MENU_AYARLAR) {
            if (mediaPlayerBuzzer != null) {
                mediaPlayerBuzzer.stop();
                mediaPlayerBuzzer.release();
                mediaPlayerBuzzer = null;
            }
            if(isEnabled)
            {
                mediaPlayerBuzzer = MediaPlayer.create(getActivity(), R.raw.menu_6_tone);
                mediaPlayerBuzzer.setAudioStreamType(AudioManager.STREAM_MUSIC);
                mediaPlayerBuzzer.start();
            }

            idleMenuScrollView.fullScroll(ScrollView.FOCUS_DOWN);
            menuItemAyarlar.setBackgroundResource(R.drawable.menu_item_selected);
            menuItemAyarlarText.setTextColor(getResources().getColor(R.color.black));
        }

    }

    private void unselectAllMenus() {

        menuItemRehber.setBackgroundResource(R.drawable.menu_item_unselected);
        menuItemRehberText.setTextColor(getResources().getColor(R.color.white));
        menuItemGorevliyiAra.setBackgroundResource(R.drawable.menu_item_unselected);
        menuItemGorevliyiAraText.setTextColor(getResources().getColor(R.color.white));
        menuItemGuvenlikAra.setBackgroundResource(R.drawable.menu_item_unselected);
        menuItemGuvenlikAraText.setTextColor(getResources().getColor(R.color.white));
        menuItemDaireAra.setBackgroundResource(R.drawable.menu_item_unselected);
        menuItemDaireAraText.setTextColor(getResources().getColor(R.color.white));
        menuItemKapiAc.setBackgroundResource(R.drawable.menu_item_unselected);
        menuItemKapiAcText.setTextColor(getResources().getColor(R.color.white));
        menuItemAyarlar.setBackgroundResource(R.drawable.menu_item_unselected);
        menuItemAyarlarText.setTextColor(getResources().getColor(R.color.white));

    }

    private void bottomMenuBackClicked() {
        selectedMenuId = Constants.MENU_DAIRE_ARA;
        MainActivity mainActivity = (MainActivity) getActivity();
        mainActivity.displayView(Constants.SCREEN_ONBOARDING, null);
    }

}

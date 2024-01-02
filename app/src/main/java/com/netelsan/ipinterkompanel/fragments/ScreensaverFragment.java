package com.netelsan.ipinterkompanel.fragments;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.netelsan.ipinterkompanel.activity.MainActivity;
import android.os.IBinder;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.netelsan.ipinterkompanel.Constants;
import com.netelsan.ipinterkompanel.R;
import com.netelsan.ipinterkompanel.ZilPanelApplication;
import com.netelsan.ipinterkompanel.listener.KeyPadListener;
import com.netelsan.ipinterkompanel.service.KeyPadService;

import org.jetbrains.annotations.NotNull;




public class ScreensaverFragment extends  BaseFragment implements KeyPadListener, ServiceConnection {


    Context mContext;
    public ScreensaverFragment() {
        // Required empty public constructor
    }



    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {

        View rootview = inflater.inflate(R.layout.fragment_screensaver, null);


        return rootview;
    }


    @Override
    public void onServiceConnected(ComponentName name, IBinder service) {
        keyPadService = ((KeyPadService.MyBinder) service).getService();
        keyPadService.addListener(ScreensaverFragment.this, ScreensaverFragment.class.getName());
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

        if(keyPadService != null) {
            keyPadService.addListener(ScreensaverFragment.this, ScreensaverFragment.class.getName());
        }
    }

    @Override
    public void onPause() {
        super.onPause();
        if(keyPadService != null) {
            keyPadService.removeListener(OnboardingFragment.class.getName());
        }
    }

    @Override
    public void onKeyPressed(@NotNull String keyCode) {

        //burada herhangi bir tuş geldiğinde idle menu ekranını aç
        Log.d(Constants.LOG_TAG, "fey 1903 OnboardingFragment onKeyPressed mainActivity=" + mainActivity + " isAdded()=" + isAdded());
        if(keyCode.equals(Constants.KEYPAD_LOCK)) {
            bottomLockClicked();
            return;
        }

        if(keyCode.equals(Constants.KEYPAD_BACK)) {
            return;
        }

        MainActivity mainActivity = (MainActivity) getActivity();
        Log.d(Constants.LOG_TAG, "OnboardingFragment onKeyPressed mainActivity=" + mainActivity + " isAdded()=" + isAdded());
        if(isAdded()) {

            if(keyCode.equals(Constants.KEYPAD_BACK) ||
                    keyCode.equals(Constants.KEYPAD_UP) ||
                    keyCode.equals(Constants.KEYPAD_DOWN) ||
                    keyCode.equals(Constants.KEYPAD_HOME) ||
                    keyCode.equals(Constants.KEYPAD_CALL) ||
                    keyCode.equals(Constants.KEYPAD_LOCK)) {
                mainActivity.displayView(Constants.SCREEN_IDLE_MENU, null);
            } else {
                Bundle bundle = null;
                if(keyCode != null) {
                    bundle = new Bundle();
                    bundle.putString("keyCode", keyCode);
                }
                mainActivity.displayView(Constants.SCREEN_DAIRE_ARA, bundle);
            }

        } else {

            if(mainActivity == null) {
                System.exit(0);
            } else {
                ZilPanelApplication zilPanelApplication = (ZilPanelApplication) mainActivity.getApplication();
                zilPanelApplication.restartApplication();
            }

        }

//        Log.d(Constants.LOG_TAG, "OnboardingFragment onKeyPressed keyCode=" + keyCode);

    }

    MainActivity mainActivity;

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        mainActivity = (MainActivity) getActivity();
    }

    @Override
    public void onRFIDDetected(String rfid) {
        processRFIDCode(rfid, keyPadService);
    }

    private void bottomLockClicked() {
        MainActivity mainActivity = (MainActivity) getActivity();
        mainActivity.displayView(Constants.SCREEN_KAPI_AC, null);
    }


}
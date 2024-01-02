package com.netelsan.ipinterkompanel.fragments;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.netelsan.ipinterkompanel.Constants;
import com.netelsan.ipinterkompanel.Helper;
import com.netelsan.ipinterkompanel.R;
import com.netelsan.ipinterkompanel.ZilPanelApplication;

import com.netelsan.ipinterkompanel.activity.MainActivity;
import com.netelsan.ipinterkompanel.activity.ScreensaverActivity2;
import com.netelsan.ipinterkompanel.activity.SplashSelectorActivity;
import com.netelsan.ipinterkompanel.listener.KeyPadListener;
import com.netelsan.ipinterkompanel.service.KeyPadService;
import com.stericson.RootShell.exceptions.RootDeniedException;
import com.stericson.RootShell.execution.Command;
import com.stericson.RootTools.RootTools;

import org.jetbrains.annotations.NotNull;
import org.w3c.dom.Text;

import java.io.IOException;
import java.util.concurrent.TimeoutException;

public class OnboardingFragment extends BaseFragment implements KeyPadListener, ServiceConnection {

    Context mContext;

    TextView onboardingText1;
    TextView onboardingText2;

    public OnboardingFragment() {
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {

        View rootview = inflater.inflate(R.layout.fragment_onboarding, null);

        onboardingText1 = rootview.findViewById(R.id.onboardingText1);
        onboardingText2 = rootview.findViewById(R.id.onboardingText2);

        setLayouts();

        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                try {
                    if(ScreensaverActivity2.rfBooleanValue==true)
                    {
                        if(ScreensaverActivity2.rfScreenValue != null)
                        {
                            processRFIDCode(ScreensaverActivity2.rfScreenValue, keyPadService);
                        }
                        ScreensaverActivity2.rfScreenValue="";
                        ScreensaverActivity2.rfBooleanValue=false;
                    }
                } catch (Exception e) {

                }

            }
        }, 100);


        return rootview;
    }

    private void setLayouts() {

        String onboardingText1 = Helper.getOnboardingText1(getActivity());
        if (onboardingText1 == null || onboardingText1.isEmpty()) {
            this.onboardingText1.setVisibility(View.GONE);
        } else {
            this.onboardingText1.setVisibility(View.VISIBLE);
            this.onboardingText1.setText(onboardingText1);
        }

        String onboardingText2 = Helper.getOnboardingText2(getActivity());
        if (onboardingText2 == null || onboardingText2.isEmpty()) {
            this.onboardingText2.setVisibility(View.GONE);
        } else {
            this.onboardingText2.setVisibility(View.VISIBLE);
            this.onboardingText2.setText(onboardingText2);
        }

    }

    @Override
    public void onServiceConnected(ComponentName name, IBinder service) {
        keyPadService = ((KeyPadService.MyBinder) service).getService();
        keyPadService.addListener(OnboardingFragment.this, OnboardingFragment.class.getName());
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
            keyPadService.addListener(OnboardingFragment.this, OnboardingFragment.class.getName());
        }
    }

    @Override
    public void onPause() {
        super.onPause();
        if (keyPadService != null) {
            keyPadService.removeListener(OnboardingFragment.class.getName());
        }
    }

    @Override
    public void onKeyPressed(@NotNull String keyCode) {
        if (keyCode.equals(Constants.KEYPAD_LOCK)) {
            bottomLockClicked();
            return;
        }

        if (keyCode.equals(Constants.KEYPAD_BACK)) {
            return;
        }

        MainActivity mainActivity = (MainActivity) getActivity();
        if (isAdded()) {

            if (keyCode.equals(Constants.KEYPAD_BACK) ||
                    keyCode.equals(Constants.KEYPAD_UP) ||
                    keyCode.equals(Constants.KEYPAD_DOWN) ||
                    keyCode.equals(Constants.KEYPAD_HOME) ||
                    keyCode.equals(Constants.KEYPAD_CALL) ||
                    keyCode.equals(Constants.KEYPAD_LOCK)) {
                mainActivity.displayView(Constants.SCREEN_IDLE_MENU, null);
            } else {
                Bundle bundle = null;
                if (keyCode != null) {
                    bundle = new Bundle();
                    bundle.putString("keyCode", keyCode);
                }
                mainActivity.displayView(Constants.SCREEN_DAIRE_ARA, bundle);
            }

        } else {

            if (mainActivity == null) {
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

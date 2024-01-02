package com.netelsan.ipinterkompanel.fragments.settings;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.IBinder;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.recyclerview.widget.RecyclerView;

import com.netelsan.ipinterkompanel.Constants;
import com.netelsan.ipinterkompanel.R;
import com.netelsan.ipinterkompanel.activity.MainActivity;
import com.netelsan.ipinterkompanel.adapter.DaireRehberAdapter;
import com.netelsan.ipinterkompanel.adapter.DoorLogsAdapter;
import com.netelsan.ipinterkompanel.database.DatabaseHelper;
import com.netelsan.ipinterkompanel.fragments.BaseFragment;
import com.netelsan.ipinterkompanel.listener.KeyPadListener;
import com.netelsan.ipinterkompanel.model.Daire;
import com.netelsan.ipinterkompanel.model.DoorUnlockLog;
import com.netelsan.ipinterkompanel.service.KeyPadService;

import java.util.ArrayList;
import java.util.Iterator;

public class AyarlarDoorLogsFragment extends BaseFragment implements KeyPadListener, ServiceConnection {

    RecyclerView doorLogsRecyclerView;
    DoorLogsAdapter doorLogsAdapter;

    public AyarlarDoorLogsFragment() {
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View rootview = inflater.inflate(R.layout.fragment_ayarlar_door_logs, null);

        doorLogsRecyclerView = rootview.findViewById(R.id.doorLogsRecyclerView);

        setLayouts();

        return rootview;
    }

    private void setLayouts() {

        DatabaseHelper databaseHelper = new DatabaseHelper(getActivity());
        ArrayList<DoorUnlockLog> doorUnlockLogs = databaseHelper.getDoorLogs();

        if(doorUnlockLogs.size() > 0) {
            DoorUnlockLog doorUnlockLog = doorUnlockLogs.get(0);

            doorUnlockLog.setSelected(true);
            doorUnlockLogs.set(0, doorUnlockLog);
        }

        doorLogsAdapter = new DoorLogsAdapter(getActivity(), doorUnlockLogs);
        doorLogsRecyclerView.setAdapter(doorLogsAdapter);

    }

    @Override
    public void onServiceConnected(ComponentName name, IBinder service) {
        keyPadService = ((KeyPadService.MyBinder) service).getService();
        keyPadService.addListener(AyarlarDoorLogsFragment.this, AyarlarDoorLogsFragment.class.getName());
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
            keyPadService.addListener(AyarlarDoorLogsFragment.this, AyarlarDoorLogsFragment.class.getName());
        }

    }

    @Override
    public void onPause() {
        super.onPause();

        if(keyPadService != null) {
            keyPadService.removeListener(AyarlarDoorLogsFragment.class.getName());
        }
    }

    @Override
    public void onKeyPressed(String keyCode) {

        if(! isAdded()) {
            return;
        }
        getActivity().runOnUiThread(new Runnable() {
            @Override
            public void run() {

//                Log.d(Constants.LOG_TAG, "KapiAcFragment onKeyPressed keyCode=" + keyCode);

                if(keyCode.equals(Constants.KEYPAD_BACK)) {
                    bottomMenuBackClicked();
                } else if(keyCode.equals(Constants.KEYPAD_UP)) {
                    menuNavigationClicked(Constants.MENU_NAVIGATION_UP);
                } else if(keyCode.equals(Constants.KEYPAD_DOWN)) {
                    menuNavigationClicked(Constants.MENU_NAVIGATION_DOWN);
                } else if(keyCode.equals(Constants.KEYPAD_HOME)) {
                    bottomMenuOkayClicked();
                } else if(keyCode.equals(Constants.KEYPAD_CALL)) {

                } else if(keyCode.equals(Constants.KEYPAD_LOCK)) {
//                    bottomLockClicked();
                } else {
                    bottomNumbersClicked(keyCode);
                }

            }
        });

    }

    @Override
    public void onRFIDDetected(String rfid) {

    }

    private void bottomNumbersClicked(String keyCode) {

    }

    private void menuNavigationClicked(int direction) {


        ArrayList<DoorUnlockLog> doorUnlockLogs = doorLogsAdapter.getArrayList();
        int totalSize = doorUnlockLogs.size();
        if(totalSize == 1) {
            return;
        }

        int selectedIndex = getSelectedIndex(doorUnlockLogs);
        if(selectedIndex == - 1) {
            return;
        }

        int oldIndex = selectedIndex;

        DoorUnlockLog oldDoorUnlockLog = doorUnlockLogs.get(selectedIndex);
        oldDoorUnlockLog.setSelected(false);
        doorUnlockLogs.set(selectedIndex, oldDoorUnlockLog);

        if(direction == Constants.MENU_NAVIGATION_UP) {

            if(selectedIndex == 0) {
                selectedIndex = totalSize - 1;
            } else {
                selectedIndex = selectedIndex - 1;
            }

        } else {

            if(totalSize == (selectedIndex + 1)) {
                selectedIndex = 0;
            } else {
                selectedIndex = selectedIndex + 1;
            }
        }

        DoorUnlockLog newDoorUnlockLog = doorUnlockLogs.get(selectedIndex);
        newDoorUnlockLog.setSelected(true);
        doorUnlockLogs.set(selectedIndex, newDoorUnlockLog);

        doorLogsAdapter.setArrayList(doorUnlockLogs);
        doorLogsAdapter.notifyItemChanged(oldIndex);
        doorLogsAdapter.notifyItemChanged(selectedIndex);

        doorLogsRecyclerView.scrollToPosition(selectedIndex);

    }

    private int getSelectedIndex(ArrayList<DoorUnlockLog> doorUnlockLogs) {

        int selectedIndex = 0;

        Iterator<DoorUnlockLog> iterator = doorUnlockLogs.iterator();
        while(iterator.hasNext()) {
            DoorUnlockLog doorUnlockLog = iterator.next();
            if(doorUnlockLog.isSelected()) {
                return selectedIndex;
            }
            selectedIndex = selectedIndex + 1;
        }

        return - 1;
    }

    private void bottomMenuOkayClicked() {

    }

    private void bottomMenuBackClicked() {
        MainActivity mainActivity = (MainActivity) getActivity();
        mainActivity.displayView(Constants.SCREEN_AYARLAR, null);
    }

}

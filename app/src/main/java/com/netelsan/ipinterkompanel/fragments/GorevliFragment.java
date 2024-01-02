package com.netelsan.ipinterkompanel.fragments;

import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.IBinder;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.RecyclerView;

import com.esotericsoftware.kryo.util.Util;
import com.netelsan.ipinterkompanel.Constants;
import com.netelsan.ipinterkompanel.Helper;
import com.netelsan.ipinterkompanel.R;
import com.netelsan.ipinterkompanel.activity.MainActivity;
import com.netelsan.ipinterkompanel.activity.call.MultipleDeviceCallRequestActivity;
import com.netelsan.ipinterkompanel.adapter.DaireRehberAdapter;
import com.netelsan.ipinterkompanel.database.DatabaseHelper;
import com.netelsan.ipinterkompanel.dialog_activity.CountDownForCallActivity;
import com.netelsan.ipinterkompanel.listener.KeyPadListener;
import com.netelsan.ipinterkompanel.model.CallSnapshotObject;
import com.netelsan.ipinterkompanel.model.ComPackageModel;
import com.netelsan.ipinterkompanel.model.Daire;
import com.netelsan.ipinterkompanel.model.DoorPassword;
import com.netelsan.ipinterkompanel.model.ZilPanel;
import com.netelsan.ipinterkompanel.model.ZilPanelSite;
import com.netelsan.ipinterkompanel.service.KeyPadService;
import com.netelsan.ipinterkompanel.tcp.TCPHelper;
import com.netelsan.ipinterkompanel.tcp.Utils;
import com.netelsan.ipinterkompanel.webrtc.activity.CallActivity;

import java.util.ArrayList;
import java.util.Iterator;

public class GorevliFragment extends BaseFragment implements KeyPadListener, ServiceConnection {

    DaireRehberAdapter daireRehberAdapter;
    RecyclerView gorevliRecyclerView;

    public GorevliFragment() {
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {

        View rootview = inflater.inflate(R.layout.fragment_gorevli, null);

        setLayouts(rootview);

        return rootview;
    }

    @Override
    public void onServiceConnected(ComponentName name, IBinder service) {
        keyPadService = ((KeyPadService.MyBinder) service).getService();
        keyPadService.addListener(GorevliFragment.this, GorevliFragment.class.getName());
    }

    @Override
    public void onServiceDisconnected(ComponentName name) {
        keyPadService.removeListener(GorevliFragment.class.getName());
        keyPadService = null;
    }

    private void setLayouts(View rootview) {

        DatabaseHelper databaseHelper = new DatabaseHelper(getActivity());
        ZilPanel zilPanel = databaseHelper.getZilPanelByIP(Utils.getIPAddress(true));

        ArrayList<Daire> gorevlilerInSameBuilding = databaseHelper.getGorevliDairelerInSameBlok(zilPanel.getBlok());
        ArrayList<Daire> gorevlilerInOtherBuilding = databaseHelper.getGorevliDairelerInOtherBlocks(zilPanel.getBlok());

        ArrayList<Daire> arrayList = new ArrayList<>();
        arrayList.addAll(gorevlilerInSameBuilding);
        arrayList.addAll(gorevlilerInOtherBuilding);

        if(arrayList.size() > 0) {
            Daire daire = arrayList.get(0);

            daire.setSelected(true);
            arrayList.set(0, daire);
        }

        gorevliRecyclerView = rootview.findViewById(R.id.gorevliRecyclerView);
        daireRehberAdapter = new DaireRehberAdapter(getActivity(), arrayList);
        gorevliRecyclerView.setAdapter(daireRehberAdapter);

        ArrayList<DoorPassword> doorPasswords = databaseHelper.getPasswords(true);
        ArrayList<DoorPassword> doorPasswords2 = databaseHelper.getPasswords(false);
        doorPasswords.addAll(doorPasswords2);
        Iterator<DoorPassword> iterator = doorPasswords.iterator();
        while (iterator.hasNext()){
            DoorPassword doorPassword = iterator.next();
            Log.d(Constants.LOG_TAG,"setLayouts label=" + doorPassword.getPasswordLabel() + " password=" + doorPassword.getDoor() + " RFID=" + doorPassword.getRfid() + " id=" + doorPassword.getId());

        }
    }

    @Override
    public void onResume() {
        super.onResume();

        Intent serviceConnIntent = new Intent(getActivity(), KeyPadService.class);
        getActivity().bindService(serviceConnIntent, this, Context.BIND_AUTO_CREATE);

        if(keyPadService != null) {
            keyPadService.addListener(GorevliFragment.this, GorevliFragment.class.getName());
        }
    }

    @Override
    public void onPause() {
        super.onPause();
        if(keyPadService != null) {
            keyPadService.removeListener(GorevliFragment.class.getName());
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

                Log.d(Constants.LOG_TAG, "RehberFragment onKeyPressed keyCode=" + keyCode);

                if(keyCode.equals(Constants.KEYPAD_BACK)) {
                    bottomMenuBackClicked();
                } else if(keyCode.equals(Constants.KEYPAD_UP)) {
                    menuNavigationClicked(Constants.MENU_NAVIGATION_UP);
                } else if(keyCode.equals(Constants.KEYPAD_DOWN)) {
                    menuNavigationClicked(Constants.MENU_NAVIGATION_DOWN);
                } else if(keyCode.equals(Constants.KEYPAD_HOME)) {
                    bottomMenuOkayClicked();
                } else if(keyCode.equals(Constants.KEYPAD_CALL)) {
                    bottomMenuOkayClicked();

                } else if(keyCode.equals(Constants.KEYPAD_LOCK)) {
                    bottomLockClicked();
                } else {
//            showKeypadScreen(keyCode);
                }

            }
        });

    }

    @Override
    public void onRFIDDetected(String rfid) {
        processRFIDCode(rfid, keyPadService);
    }

    private void bottomMenuOkayClicked() {

        if(isCountDownDialogRunning) {
            return;
        }

        ArrayList<Daire> arrayListDaires = daireRehberAdapter.getArrayList();
        if(arrayListDaires.size() == 0){
            return;
        }

        int selectedIndex = getSelectedIndex(arrayListDaires);
        Daire selectedDaire = arrayListDaires.get(selectedIndex);

        showCountDownTimer(selectedDaire);

    }

    boolean isCountDownDialogRunning = false;

    BroadcastReceiver broadcastReceiverCountDown = null;

    private void showCountDownTimer(Daire selectedDaire) {

        broadcastReceiverCountDown = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {

                isCountDownDialogRunning = false;
                boolean isPositive = intent.getBooleanExtra("isPositive", false);
                Log.d(Constants.LOG_TAG, "broadcastReceiverCountDown isPositive=" + isPositive);

                if(isPositive) {
                    ArrayList<String> bitmapFilePaths = (ArrayList<String>) intent.getSerializableExtra("bitmapFiles");

                    ArrayList<CallSnapshotObject> callSnapshotObjects = getCallSnapshotObjects(bitmapFilePaths);
                    Log.d(Constants.LOG_TAG, "callSnapshotObjects size=" + callSnapshotObjects.size());
//                    burada objeyi oluştur ve resim isimlerini oluştur ve arama isteğinde bu isimleri de gönder.
//                    bu isimlerle yönlendirme isteğinde karşı tarafkayıt edecek.
//                    arama isteğinde ise callRecord a kaydedecek

                    showCallingActivity(selectedDaire, callSnapshotObjects);

                }

                getActivity().unregisterReceiver(broadcastReceiverCountDown);
                broadcastReceiverCountDown = null;
            }
        };

        IntentFilter filter = new IntentFilter();
        filter.addAction("com.netelsan.COUNT_DOWN");
        getActivity().registerReceiver(broadcastReceiverCountDown, filter);

        isCountDownDialogRunning = true;

        try {
            Helper.runRestartCameraShellCommand();
        } catch(Throwable e) {
            e.printStackTrace();
        }

        Intent intent = new Intent(getActivity(), CountDownForCallActivity.class);
        intent.putExtra("object", selectedDaire);
        startActivity(intent);

    }

    BroadcastReceiver broadcastReceiverYonlendirme = null;

    private void showCallingActivity(Daire selectedDaire, ArrayList<CallSnapshotObject> callSnapshotObjects) {

        DatabaseHelper databaseHelper = new DatabaseHelper(getActivity());

        broadcastReceiverYonlendirme = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {

                boolean isReachable = intent.getBooleanExtra("isReachable", false);
                if(! isReachable) {
                    getActivity().unregisterReceiver(broadcastReceiverYonlendirme);
                    broadcastReceiverYonlendirme = null;
                    return;
                }

                boolean isNeedYonlendirme = intent.getBooleanExtra("isNeedYonlendirme", false);
                ArrayList<String> callSnapshotImageIds = getCallSnapshotIds(callSnapshotObjects);
                if(isNeedYonlendirme) {
                    String yonlendirmeDeviceIp = intent.getStringExtra("deviceIP");
                    Daire daireYonlendirme = databaseHelper.getDaireByIP(yonlendirmeDeviceIp);
//                    startCallingActivity(daireYonlendirme, isNeedYonlendirme, callSnapshotImageIds);

                    showCallingActivity(daireYonlendirme, callSnapshotObjects);

                } else {

                    int totalDeviceCount = intent.getIntExtra("totalDeviceCount", 1);
                    if(totalDeviceCount > 1) {
                        boolean isBusy = intent.getBooleanExtra("isBusy", false);
                        if(isBusy){
                            Log.d("HK","HK busy.................................");
                            Helper.showTopMessageBanner((AppCompatActivity) getActivity(), getString(R.string.arama_mesgul), false);
                        }else{
                            Intent intentMultiDevice = new Intent(getActivity(), MultipleDeviceCallRequestActivity.class);
                            intentMultiDevice.putExtra("object", selectedDaire);
                            startActivity(intentMultiDevice);
                        }
                    } else {
                        startCallingActivity(selectedDaire, isNeedYonlendirme, callSnapshotImageIds);
                    }

                }

                getActivity().unregisterReceiver(broadcastReceiverYonlendirme);
                broadcastReceiverYonlendirme = null;

            }
        };

        IntentFilter filter = new IntentFilter();
        filter.addAction("com.netelsan.YONLENDIRME_STATE");
        getActivity().registerReceiver(broadcastReceiverYonlendirme, filter);

        ComPackageModel model = new ComPackageModel();
        boolean isZilForSite = Helper.isZilForSite(getActivity());
        if(isZilForSite){
            ZilPanelSite zilPanelSiteSelf = databaseHelper.getSiteZilPanelByIP(Utils.getIPAddress(true));
            model.setZilPanelSite(zilPanelSiteSelf);
        }else{
            ZilPanel zilPanelSelf = databaseHelper.getZilPanelByIP(Utils.getIPAddress(true));
            model.setZilPanel(zilPanelSelf);
        }

        model.setOpe_type(Constants.OPERATION_REQUEST_YONLENDIRME);
        model.setCallSnapshotObjects(callSnapshotObjects);

        String destinationIp = selectedDaire.getIp();
        TCPHelper.sendMessageToIP(getActivity(), destinationIp, model);

    }

    private void startCallingActivity(Daire selectedDaire, boolean isYonlendirme, ArrayList<String> callSnapshotImageIds) {
        Intent intent = new Intent(getActivity(), CallActivity.class);
        intent.putExtra(Constants.PARAM_CALL_RECEIVING, false);
        intent.putExtra(Constants.PARAM_CALL_IS_YONLENDIRME, isYonlendirme);
        intent.putExtra(Constants.PARAM_CALL_SS_IDS, callSnapshotImageIds);
        intent.putExtra(Constants.PARAM_CALL_IS_NEED_VIDEO, true);
        intent.putExtra(Constants.PARAM_IS_MULTI_CALL_ACCEPT, false);

        // TODO: 10.02.2020 multi device olabilir. kontrol et

        intent.putExtra("object", selectedDaire);
        startActivity(intent);
    }

    private void menuNavigationClicked(int direction) {

        if(isCountDownDialogRunning) {
            return;
        }

        ArrayList<Daire> arrayListDaires = daireRehberAdapter.getArrayList();
        int totalSize = arrayListDaires.size();
        if(totalSize == 1) {
            return;
        }

        int selectedIndex = getSelectedIndex(arrayListDaires);
        if(selectedIndex == - 1) {
            return;
        }

        int oldIndex = selectedIndex;

        Daire oldSelectedDaire = arrayListDaires.get(selectedIndex);
        oldSelectedDaire.setSelected(false);
        arrayListDaires.set(selectedIndex, oldSelectedDaire);

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

        Daire newSelectedDaire = arrayListDaires.get(selectedIndex);
        newSelectedDaire.setSelected(true);
        arrayListDaires.set(selectedIndex, newSelectedDaire);

        daireRehberAdapter.setArrayList(arrayListDaires);
        daireRehberAdapter.notifyItemChanged(oldIndex);
        daireRehberAdapter.notifyItemChanged(selectedIndex);

        gorevliRecyclerView.scrollToPosition(selectedIndex);

    }

    private int getSelectedIndex(ArrayList<Daire> arrayListDaires) {

        int selectedIndex = 0;

        Iterator<Daire> iterator = arrayListDaires.iterator();
        while(iterator.hasNext()) {
            Daire daire = iterator.next();
            if(daire.isSelected()) {
                return selectedIndex;
            }
            selectedIndex = selectedIndex + 1;
        }

        return - 1;
    }

    private void bottomLockClicked() {
        MainActivity mainActivity = (MainActivity) getActivity();
        mainActivity.displayView(Constants.SCREEN_KAPI_AC, null);
    }

    private void bottomMenuBackClicked() {

//        if(isCountDownDialogRunning) {
//
//            countDownTimerDialog.dismiss();
//            countDownTimerDialog = null;
//
//            timerCountDown.cancel();
//            timerCountDown.purge();
//            timerCountDown = null;
//
//            return;
//        }

        MainActivity mainActivity = (MainActivity) getActivity();
        mainActivity.displayView(Constants.SCREEN_IDLE_MENU, null);
    }

}

package com.netelsan.ipinterkompanel.fragments;

import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.os.IBinder;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.recyclerview.widget.RecyclerView;

import com.netelsan.ipinterkompanel.Constants;
import com.netelsan.ipinterkompanel.Helper;
import com.netelsan.ipinterkompanel.R;
import com.netelsan.ipinterkompanel.activity.MainActivity;
import com.netelsan.ipinterkompanel.activity.call.MultipleDeviceCallRequestActivity;
import com.netelsan.ipinterkompanel.adapter.GuvenlikRehberAdapter;
import com.netelsan.ipinterkompanel.database.DatabaseHelper;
import com.netelsan.ipinterkompanel.dialog_activity.CountDownForCallActivity;
import com.netelsan.ipinterkompanel.listener.KeyPadListener;
import com.netelsan.ipinterkompanel.model.CallSnapshotObject;
import com.netelsan.ipinterkompanel.model.ComPackageModel;
import com.netelsan.ipinterkompanel.model.Guvenlik;
import com.netelsan.ipinterkompanel.model.ZilPanel;
import com.netelsan.ipinterkompanel.model.ZilPanelSite;
import com.netelsan.ipinterkompanel.service.KeyPadService;
import com.netelsan.ipinterkompanel.tcp.TCPHelper;
import com.netelsan.ipinterkompanel.tcp.Utils;
import com.netelsan.ipinterkompanel.webrtc.activity.CallActivity;

import java.util.ArrayList;
import java.util.Iterator;

public class GuvenlikFragment extends BaseFragment implements KeyPadListener, ServiceConnection {

    GuvenlikRehberAdapter guvenlikRehberAdapter;
    RecyclerView gorevliRecyclerView;



    public GuvenlikFragment() {
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
        keyPadService.addListener(GuvenlikFragment.this, GuvenlikFragment.class.getName());
    }

    @Override
    public void onServiceDisconnected(ComponentName name) {
        keyPadService.removeListener(GuvenlikFragment.class.getName());
        keyPadService = null;
    }

    private void setLayouts(View rootview) {

        DatabaseHelper databaseHelper = new DatabaseHelper(getActivity());

        ArrayList<Guvenlik> guvenliks = databaseHelper.getGuvenlikler();

        if(guvenliks.size() > 0) {
            Guvenlik guvenlik = guvenliks.get(0);

            guvenlik.setSelected(true);
            guvenliks.set(0, guvenlik);

        } else {
            return;
        }

        gorevliRecyclerView = rootview.findViewById(R.id.gorevliRecyclerView);
        guvenlikRehberAdapter = new GuvenlikRehberAdapter(getActivity(), guvenliks);
        gorevliRecyclerView.setAdapter(guvenlikRehberAdapter);

    }

    @Override
    public void onResume() {
        super.onResume();

        Intent serviceConnIntent = new Intent(getActivity(), KeyPadService.class);
        getActivity().bindService(serviceConnIntent, this, Context.BIND_AUTO_CREATE);

        if(keyPadService != null) {
            keyPadService.addListener(GuvenlikFragment.this, GuvenlikFragment.class.getName());
        }
    }

    @Override
    public void onPause() {
        super.onPause();
        if(keyPadService != null) {
            keyPadService.removeListener(GuvenlikFragment.class.getName());
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

        if (guvenlikRehberAdapter == null){
            return;
        }

        ArrayList<Guvenlik> arrayListDaires = guvenlikRehberAdapter.getArrayList();
        if(arrayListDaires.size() == 0) {
            return;
        }

        int selectedIndex = getSelectedIndex(arrayListDaires);
        Guvenlik selectedGuvenlik = arrayListDaires.get(selectedIndex);

        showCountDownTimer(selectedGuvenlik);
        Log.d(Constants.LOG_TAG, "aranacak daire=" + selectedGuvenlik.getIp());

    }

    boolean isCountDownDialogRunning = false;

    BroadcastReceiver broadcastReceiverCountDown = null;

    private void showCountDownTimer(Guvenlik selectedGuvenlik) {

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

                    showCallingActivity(selectedGuvenlik, callSnapshotObjects);

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
        intent.putExtra("object", selectedGuvenlik);
        startActivity(intent);

    }

    private void showCallingActivity(Guvenlik selectedGuvenlik, ArrayList<CallSnapshotObject> callSnapshotObjects) {

        DatabaseHelper databaseHelper = new DatabaseHelper(getActivity());

        BroadcastReceiver broadcastReceiverYonlendirme = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {

                boolean isReachable = intent.getBooleanExtra("isReachable", false);
                if(! isReachable) {
                    getActivity().unregisterReceiver(this);
                    return;
                }

                boolean isNeedYonlendirme = intent.getBooleanExtra("isNeedYonlendirme", false);
                ArrayList<String> callSnapshotImageIds = getCallSnapshotIds(callSnapshotObjects);
                if(isNeedYonlendirme) {
                    String yonlendirmeDeviceIp = intent.getStringExtra("deviceIP");
                    Guvenlik guvenlikYonlendirme = databaseHelper.getGuvenlikByIP(yonlendirmeDeviceIp);
//                    startCallingActivity(daireYonlendirme, isNeedYonlendirme, callSnapshotImageIds);

                    showCallingActivity(guvenlikYonlendirme, callSnapshotObjects);

                } else {

                    int totalDeviceCount = intent.getIntExtra("totalDeviceCount", 1);
                    if(totalDeviceCount > 1) {
                        Intent intentMultiDevice = new Intent(getActivity(), MultipleDeviceCallRequestActivity.class);
                        intentMultiDevice.putExtra("object", selectedGuvenlik);
                        startActivity(intentMultiDevice);
                    } else {
                        startCallingActivity(selectedGuvenlik, isNeedYonlendirme, callSnapshotImageIds);
                    }

                }

                getActivity().unregisterReceiver(this);

            }
        };

        IntentFilter filter = new IntentFilter();
        filter.addAction("com.netelsan.YONLENDIRME_STATE");
        getActivity().registerReceiver(broadcastReceiverYonlendirme, filter);

        ComPackageModel model = new ComPackageModel();
        boolean isZilForSite = Helper.isZilForSite(getActivity());
        if(isZilForSite) {
            ZilPanelSite zilPanelSiteSelf = databaseHelper.getSiteZilPanelByIP(Utils.getIPAddress(true));
            model.setZilPanelSite(zilPanelSiteSelf);
        } else {
            ZilPanel zilPanelSelf = databaseHelper.getZilPanelByIP(Utils.getIPAddress(true));
            model.setZilPanel(zilPanelSelf);
        }

        model.setOpe_type(Constants.OPERATION_REQUEST_YONLENDIRME);
        model.setCallSnapshotObjects(callSnapshotObjects);

        String destinationIp = selectedGuvenlik.getIp();
        TCPHelper.sendMessageToIP(getActivity(), destinationIp, model);

    }

    private void startCallingActivity(Guvenlik selectedGuvenlik, boolean isYonlendirme, ArrayList<String> callSnapshotImageIds) {
        Intent intent = new Intent(getActivity(), CallActivity.class);
        intent.putExtra(Constants.PARAM_CALL_RECEIVING, false);
        intent.putExtra(Constants.PARAM_CALL_IS_YONLENDIRME, isYonlendirme);
        intent.putExtra(Constants.PARAM_CALL_SS_IDS, callSnapshotImageIds);
        intent.putExtra(Constants.PARAM_CALL_IS_NEED_VIDEO, false);
        intent.putExtra(Constants.PARAM_IS_MULTI_CALL_ACCEPT, false);
        intent.putExtra("object", selectedGuvenlik);
        startActivity(intent);
    }

    private void menuNavigationClicked(int direction) {

        if(isCountDownDialogRunning) {
            return;
        }

        if (guvenlikRehberAdapter == null){
            return;
        }

        ArrayList<Guvenlik> arrayListGuvenliks = guvenlikRehberAdapter.getArrayList();
        int totalSize = arrayListGuvenliks.size();
        if(totalSize == 1) {
            return;
        }

        int selectedIndex = getSelectedIndex(arrayListGuvenliks);
        if(selectedIndex == - 1) {
            return;
        }

        int oldIndex = selectedIndex;

        Guvenlik oldSelectedGuvenlik = arrayListGuvenliks.get(selectedIndex);
        oldSelectedGuvenlik.setSelected(false);
        arrayListGuvenliks.set(selectedIndex, oldSelectedGuvenlik);

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

        Guvenlik newSelectedGuvenlik = arrayListGuvenliks.get(selectedIndex);
        newSelectedGuvenlik.setSelected(true);
        arrayListGuvenliks.set(selectedIndex, newSelectedGuvenlik);

        guvenlikRehberAdapter.setArrayList(arrayListGuvenliks);
        guvenlikRehberAdapter.notifyItemChanged(oldIndex);
        guvenlikRehberAdapter.notifyItemChanged(selectedIndex);

        gorevliRecyclerView.scrollToPosition(selectedIndex);

    }

    private int getSelectedIndex(ArrayList<Guvenlik> arrayListGuvenliks) {

        int selectedIndex = 0;

        Iterator<Guvenlik> iterator = arrayListGuvenliks.iterator();
        while(iterator.hasNext()) {
            Guvenlik guvenlik = iterator.next();
            if(guvenlik.isSelected()) {
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

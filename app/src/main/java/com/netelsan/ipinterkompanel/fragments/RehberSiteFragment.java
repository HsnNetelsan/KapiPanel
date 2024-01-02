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
import android.widget.LinearLayout;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.RecyclerView;

import com.netelsan.ipinterkompanel.Constants;
import com.netelsan.ipinterkompanel.Helper;
import com.netelsan.ipinterkompanel.R;
import com.netelsan.ipinterkompanel.activity.MainActivity;
import com.netelsan.ipinterkompanel.activity.call.MultipleDeviceCallRequestActivity;
import com.netelsan.ipinterkompanel.adapter.DaireRehberAdapter;
import com.netelsan.ipinterkompanel.adapter.RehberBlokNoAdapter;
import com.netelsan.ipinterkompanel.database.DatabaseHelper;
import com.netelsan.ipinterkompanel.dialog_activity.CountDownForCallActivity;
import com.netelsan.ipinterkompanel.listener.KeyPadListener;
import com.netelsan.ipinterkompanel.model.CallSnapshotObject;
import com.netelsan.ipinterkompanel.model.ComPackageModel;
import com.netelsan.ipinterkompanel.model.Daire;
import com.netelsan.ipinterkompanel.model.ZilPanel;
import com.netelsan.ipinterkompanel.model.ZilPanelSite;
import com.netelsan.ipinterkompanel.service.KeyPadService;
import com.netelsan.ipinterkompanel.tcp.TCPHelper;
import com.netelsan.ipinterkompanel.tcp.Utils;
import com.netelsan.ipinterkompanel.webrtc.activity.CallActivity;

import java.util.ArrayList;
import java.util.Iterator;

public class RehberSiteFragment extends BaseFragment implements KeyPadListener, ServiceConnection {

    LinearLayout rehberSiteBlokContainer;
    LinearLayout rehberSiteDaireContainer;

    RecyclerView rehberSiteBlokRecyclerView;
    RecyclerView rehberSiteDaireRecyclerView;

    DatabaseHelper databaseHelper;

    RehberBlokNoAdapter rehberBlokNoAdapter;
    DaireRehberAdapter daireRehberAdapter;

    boolean isBlokNoSelected = true;

    int selectedBlokNo;

    public RehberSiteFragment() {
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {

        View rootview = inflater.inflate(R.layout.fragment_rehber_site, null);

        databaseHelper = new DatabaseHelper(getActivity());

        rehberSiteBlokContainer = rootview.findViewById(R.id.rehberSiteBlokContainer);
        rehberSiteDaireContainer = rootview.findViewById(R.id.rehberSiteDaireContainer);

        rehberSiteBlokRecyclerView = rootview.findViewById(R.id.rehberSiteBlokRecyclerView);
        rehberSiteDaireRecyclerView = rootview.findViewById(R.id.rehberSiteDaireRecyclerView);

        setLayouts(true);

        return rootview;
    }

    @Override
    public void onServiceConnected(ComponentName name, IBinder service) {
        keyPadService = ((KeyPadService.MyBinder) service).getService();
        keyPadService.addListener(RehberSiteFragment.this, RehberSiteFragment.class.getName());
    }

    @Override
    public void onServiceDisconnected(ComponentName name) {
        keyPadService.removeListener(RehberSiteFragment.class.getName());
        keyPadService = null;
    }

    private void setLayouts(boolean isForBlok) {

        isBlokNoSelected = isForBlok;

        if (isForBlok) {
            ArrayList<Integer> blokNos = databaseHelper.getDairelerGroupByBlok();
            Log.d(Constants.LOG_TAG, "blokNos=" + blokNos.size());

            ArrayList<Daire> arrayList = new ArrayList<>();

            int counter = 0;
            Iterator<Integer> iterator = blokNos.iterator();
            while (iterator.hasNext()) {
                int blokNo = iterator.next();

                Daire daire = new Daire();
                daire.setBlok(blokNo);

                if (counter == 0) {
                    daire.setSelected(true);
                    selectedBlokNo = blokNo;
                }
                //    ArrayList<Daire> daires = databaseHelper.getDaireler();
                //  if (daires.indexOf(daire) == -1) {
                arrayList.add(daire);
                //  }

                counter = counter + 1;
            }

            rehberBlokNoAdapter = new RehberBlokNoAdapter(getActivity(), arrayList);
            rehberSiteBlokRecyclerView.setAdapter(rehberBlokNoAdapter);

            rehberSiteBlokContainer.setVisibility(View.VISIBLE);
            rehberSiteDaireContainer.setVisibility(View.GONE);

        } else {

            ArrayList<Daire> arrayListDaireler = databaseHelper.getDairelerForBlok(selectedBlokNo);

            if (arrayListDaireler.size() > 0) {
                Daire daire = arrayListDaireler.get(0);

                daire.setSelected(true);
                arrayListDaireler.set(0, daire);


            }

            daireRehberAdapter = new DaireRehberAdapter(getActivity(), arrayListDaireler);
            rehberSiteDaireRecyclerView.setAdapter(daireRehberAdapter);

            rehberSiteBlokContainer.setVisibility(View.GONE);
            rehberSiteDaireContainer.setVisibility(View.VISIBLE);
        }

    }


    @Override
    public void onResume() {
        super.onResume();

        Intent serviceConnIntent = new Intent(getActivity(), KeyPadService.class);
        getActivity().bindService(serviceConnIntent, this, Context.BIND_AUTO_CREATE);

        if (keyPadService != null) {
            keyPadService.addListener(RehberSiteFragment.this, RehberSiteFragment.class.getName());
        }
    }

    @Override
    public void onPause() {
        super.onPause();
        if (keyPadService != null) {
            keyPadService.removeListener(RehberSiteFragment.class.getName());
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

                Log.d(Constants.LOG_TAG, "RehberFragment onKeyPressed keyCode=" + keyCode);

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

        if (isBlokNoSelected) {
            setLayouts(false);
            return;
        }

        if (isCountDownDialogRunning) {
            return;
        }

        ArrayList<Daire> arrayListDaires = daireRehberAdapter.getArrayList();
        if (arrayListDaires.size() == 0) {
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

                if (isPositive) {
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
        } catch (Throwable e) {
            e.printStackTrace();
        }

        Intent intent = new Intent(getActivity(), CountDownForCallActivity.class);
        intent.putExtra("object", selectedDaire);
        startActivity(intent);

    }

    private void showCallingActivity(Daire selectedDaire, ArrayList<CallSnapshotObject> callSnapshotObjects) {

        DatabaseHelper databaseHelper = new DatabaseHelper(getActivity());

        BroadcastReceiver broadcastReceiverYonlendirme = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {

                boolean isReachable = intent.getBooleanExtra("isReachable", false);
                if (!isReachable) {
                    getActivity().unregisterReceiver(this);
                    return;
                }

                boolean isNeedYonlendirme = intent.getBooleanExtra("isNeedYonlendirme", false);
                ArrayList<String> callSnapshotImageIds = getCallSnapshotIds(callSnapshotObjects);
                if (isNeedYonlendirme) {
                    String yonlendirmeDeviceIp = intent.getStringExtra("deviceIP");
                    Daire daireYonlendirme = databaseHelper.getDaireByIP(yonlendirmeDeviceIp);
//                    startCallingActivity(daireYonlendirme, isNeedYonlendirme, callSnapshotImageIds);

                    showCallingActivity(daireYonlendirme, callSnapshotObjects);

                } else {

                    int totalDeviceCount = intent.getIntExtra("totalDeviceCount", 1);
                    if (totalDeviceCount > 1) {
                        boolean isBusy = intent.getBooleanExtra("isBusy", false);
                        if (isBusy) {
                            Log.d("HK","HK rehber busy35.................................");
                            Helper.showTopMessageBanner((AppCompatActivity) getActivity(), getString(R.string.arama_mesgul), false);
                        } else {
                            Intent intentMultiDevice = new Intent(getActivity(), MultipleDeviceCallRequestActivity.class);
                            intentMultiDevice.putExtra("object", selectedDaire);
                            startActivity(intentMultiDevice);
                        }
                    } else {
                        startCallingActivity(selectedDaire, isNeedYonlendirme, callSnapshotImageIds);
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
        if (isZilForSite) {
            ZilPanelSite zilPanelSiteSelf = databaseHelper.getSiteZilPanelByIP(Utils.getIPAddress(true));
            model.setZilPanelSite(zilPanelSiteSelf);
        } else {
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
        intent.putExtra("object", selectedDaire);
        startActivity(intent);
    }

    private void menuNavigationClicked(int direction) {

        if (isCountDownDialogRunning) {
            return;
        }

        ArrayList<Daire> arrayListDaires;
        if (isBlokNoSelected) {
            arrayListDaires = rehberBlokNoAdapter.getArrayList();
        } else {
            arrayListDaires = daireRehberAdapter.getArrayList();
        }

        int totalSize = arrayListDaires.size();
        if (totalSize == 1) {
            return;
        }

        int selectedIndex = getSelectedIndex(arrayListDaires);
        if (selectedIndex == -1) {
            return;
        }

        int oldIndex = selectedIndex;

        Daire oldSelectedDaire = arrayListDaires.get(selectedIndex);
        oldSelectedDaire.setSelected(false);
        arrayListDaires.set(selectedIndex, oldSelectedDaire);

        if (direction == Constants.MENU_NAVIGATION_UP) {

            if (selectedIndex == 0) {
                selectedIndex = totalSize - 1;
            } else {
                selectedIndex = selectedIndex - 1;
            }

        } else {

            if (totalSize == (selectedIndex + 1)) {
                selectedIndex = 0;
            } else {
                selectedIndex = selectedIndex + 1;
            }
        }

        Daire newSelectedDaire = arrayListDaires.get(selectedIndex);
        newSelectedDaire.setSelected(true);
        arrayListDaires.set(selectedIndex, newSelectedDaire);

        if (isBlokNoSelected) {

            selectedBlokNo = newSelectedDaire.getBlok();

            rehberBlokNoAdapter.setArrayList(arrayListDaires);
            rehberBlokNoAdapter.notifyItemChanged(oldIndex);
            rehberBlokNoAdapter.notifyItemChanged(selectedIndex);
            rehberSiteBlokRecyclerView.scrollToPosition(selectedIndex);
        } else {
            daireRehberAdapter.setArrayList(arrayListDaires);
            daireRehberAdapter.notifyItemChanged(oldIndex);
            daireRehberAdapter.notifyItemChanged(selectedIndex);
            rehberSiteDaireRecyclerView.scrollToPosition(selectedIndex);
        }

    }

    private int getSelectedIndex(ArrayList<Daire> arrayListDaires) {

        int selectedIndex = 0;

        Iterator<Daire> iterator = arrayListDaires.iterator();
        while (iterator.hasNext()) {
            Daire daire = iterator.next();
            if (daire.isSelected()) {
                return selectedIndex;
            }
            selectedIndex = selectedIndex + 1;
        }

        return -1;
    }

    private void bottomLockClicked() {
        MainActivity mainActivity = (MainActivity) getActivity();
        mainActivity.displayView(Constants.SCREEN_KAPI_AC, null);
    }

    private void bottomMenuBackClicked() {

        if (!isBlokNoSelected) {
            setLayouts(true);
            return;
        }
        MainActivity mainActivity = (MainActivity) getActivity();
        mainActivity.displayView(Constants.SCREEN_IDLE_MENU, null);
    }

}

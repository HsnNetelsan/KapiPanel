//package com.netelsan.ipinterkompanel.fragments;
//
//import android.content.BroadcastReceiver;
//import android.content.ComponentName;
//import android.content.Context;
//import android.content.Intent;
//import android.content.IntentFilter;
//import android.content.ServiceConnection;
//import android.os.Bundle;
//import android.os.IBinder;
//import android.util.Log;
//import android.view.LayoutInflater;
//import android.view.View;
//import android.view.ViewGroup;
//import android.widget.TextView;
//import android.widget.Toast;
//
//import androidx.annotation.NonNull;
//import androidx.annotation.Nullable;
//import androidx.appcompat.app.AppCompatActivity;
//
//import com.netelsan.ipinterkompanel.Constants;
//import com.netelsan.ipinterkompanel.Helper;
//import com.netelsan.ipinterkompanel.R;
//import com.netelsan.ipinterkompanel.activity.MainActivity;
//import com.netelsan.ipinterkompanel.activity.call.MultipleDeviceCallRequestActivity;
//import com.netelsan.ipinterkompanel.database.DatabaseHelper;
//import com.netelsan.ipinterkompanel.dialog_activity.CountDownForCallActivity;
//import com.netelsan.ipinterkompanel.listener.KeyPadListener;
//import com.netelsan.ipinterkompanel.model.CallSnapshotObject;
//import com.netelsan.ipinterkompanel.model.ComPackageModel;
//import com.netelsan.ipinterkompanel.model.Daire;
//import com.netelsan.ipinterkompanel.model.ZilPanel;
//import com.netelsan.ipinterkompanel.model.ZilPanelSite;
//import com.netelsan.ipinterkompanel.service.KeyPadService;
//import com.netelsan.ipinterkompanel.tcp.TCPHelper;
//import com.netelsan.ipinterkompanel.tcp.Utils;
//import com.netelsan.ipinterkompanel.webrtc.activity.CallActivity;
//
//import java.io.DataOutputStream;
//import java.util.ArrayList;
//
//public class DaireAraFragment extends BaseFragment implements KeyPadListener, ServiceConnection {
//
//    TextView daireAraText;
//
//    public DaireAraFragment() {
//    }
//
//    @Nullable
//    @Override
//    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
//
//        View rootview = inflater.inflate(R.layout.fragment_daire_ara, null);
//
//        daireAraText = rootview.findViewById(R.id.daireAraText);
//
//        setLayouts(getArguments());
//
//        return rootview;
//    }
//
//    @Override
//    public void onServiceConnected(ComponentName name, IBinder service) {
//        keyPadService = ((KeyPadService.MyBinder) service).getService();
//        keyPadService.addListener(DaireAraFragment.this, DaireAraFragment.class.getName());
//    }
//
//    @Override
//    public void onServiceDisconnected(ComponentName name) {
//        keyPadService = null;
//    }
//
//    private void setLayouts(Bundle bundle) {
//
//        String clickedKey = "";
//        if(bundle != null) {
//            clickedKey = bundle.getString("keyCode");
//        }
//
//        if(! clickedKey.isEmpty()) {
//            daireAraText.setText(clickedKey);
//        }
//    }
//
//    @Override
//    public void onResume() {
//        super.onResume();
//
//        Intent serviceConnIntent = new Intent(getActivity(), KeyPadService.class);
//        getActivity().bindService(serviceConnIntent, this, Context.BIND_AUTO_CREATE);
//
//        if(keyPadService != null) {
//            keyPadService.addListener(DaireAraFragment.this, DaireAraFragment.class.getName());
//        }
//    }
//
//    @Override
//    public void onPause() {
//        super.onPause();
//        if(keyPadService != null) {
//            keyPadService.removeListener(DaireAraFragment.class.getName());
//        }
//    }
//
//    @Override
//    public void onKeyPressed(String keyCode) {
//
//        getActivity().runOnUiThread(new Runnable() {
//            @Override
//            public void run() {
//
//                Log.d(Constants.LOG_TAG, "DaireAraFragment onKeyPressed keyCode=" + keyCode);
//
//                if(keyCode.equals(Constants.KEYPAD_BACK)) {
//                    bottomMenuBackClicked();
//                } else if(keyCode.equals(Constants.KEYPAD_UP)) {
//
//                } else if(keyCode.equals(Constants.KEYPAD_DOWN)) {
//
//                } else if(keyCode.equals(Constants.KEYPAD_HOME)) {
//                    bottomMenuOkayClicked();
//                } else if(keyCode.equals(Constants.KEYPAD_CALL)) {
//                    bottomMenuOkayClicked();
//                } else if(keyCode.equals(Constants.KEYPAD_LOCK)) {
//                    bottomLockClicked();
//                } else {
//                    bottomNumbersClicked(keyCode);
//                }
//
//            }
//        });
//
//    }
//
//    @Override
//    public void onRFIDDetected(String rfid) {
//        processRFIDCode(rfid, keyPadService);
//    }
//
//    private void bottomMenuOkayClicked() {
//
//        String userText = daireAraText.getText().toString();
//
//        if(userText.equals("- - -")) {
//            Helper.showTopMessageBanner((AppCompatActivity) getActivity(), getString(R.string.daire_numarasi_eksik), false);
//            return;
//        }
//
//        try {
//            int daireNo = Integer.parseInt(userText);
//            daireNo = daireNo * Constants.DAIRE_ICI_MAX_DEVICE_NUMBER;
//
//            DatabaseHelper databaseHelper = new DatabaseHelper(getActivity());
//            ZilPanel zilPanel = databaseHelper.getZilPanelByIP(Utils.getIPAddress(true));
//
//            int maxDaireInBlokPart = (256 / Constants.DAIRE_ICI_MAX_DEVICE_NUMBER);
//
//            int blokPart = daireNo / maxDaireInBlokPart;
//            int tempDaireNo = daireNo - (maxDaireInBlokPart * blokPart);
//
//            String daireIp = "172." + zilPanel.getBlok() + "." + blokPart + "." + tempDaireNo;
//            Daire selectedDaire = databaseHelper.getDaireByIP(daireIp);
//
//            Log.d(Constants.LOG_TAG, "daireIp=" + daireIp);
//            if(selectedDaire == null) {
//                Helper.showTopMessageBanner((AppCompatActivity) getActivity(), getString(R.string.daire_kayitli_degil), false);
//                return;
//            }
//
//            daireAraText.setText("- - -");
//            showCountDownTimer(selectedDaire);
//
//        } catch(Exception e) {
//            e.printStackTrace();
//        }
//    }
//
//    boolean isCountDownDialogRunning = false;
//
//    BroadcastReceiver broadcastReceiverCountDown = null;
//
//    private void showCountDownTimer(Daire selectedDaire) {
//
//        broadcastReceiverCountDown = new BroadcastReceiver() {
//            @Override
//            public void onReceive(Context context, Intent intent) {
//
//                isCountDownDialogRunning = false;
//                boolean isPositive = intent.getBooleanExtra("isPositive", false);
//                Log.d(Constants.LOG_TAG, "broadcastReceiverCountDown isPositive=" + isPositive);
//
//                if(isPositive) {
//                    ArrayList<String> bitmapFilePaths = (ArrayList<String>) intent.getSerializableExtra("bitmapFiles");
//
//                    ArrayList<CallSnapshotObject> callSnapshotObjects = getCallSnapshotObjects(bitmapFilePaths);
//                    Log.d(Constants.LOG_TAG, "callSnapshotObjects size=" + callSnapshotObjects.size());
////                    burada objeyi oluştur ve resim isimlerini oluştur ve arama isteğinde bu isimleri de gönder.
////                    bu isimlerle yönlendirme isteğinde karşı tarafkayıt edecek.
////                    arama isteğinde ise callRecord a kaydedecek
//
//                    showCallingActivity(selectedDaire, callSnapshotObjects);
//
//                }
//
//                getActivity().unregisterReceiver(broadcastReceiverCountDown);
//                broadcastReceiverCountDown = null;
//            }
//        };
//
//        IntentFilter filter = new IntentFilter();
//        filter.addAction("com.netelsan.COUNT_DOWN");
//        getActivity().registerReceiver(broadcastReceiverCountDown, filter);
//
//        isCountDownDialogRunning = true;
//
//        try {
//            Helper.runRestartCameraShellCommand();
//        } catch(Throwable e) {
//            e.printStackTrace();
//        }
//
//        Intent intent = new Intent(getActivity(), CountDownForCallActivity.class);
//        intent.putExtra("object", selectedDaire);
//        startActivity(intent);
//
//    }
//
//    BroadcastReceiver broadcastReceiverYonlendirme = null;
//
//    private void showCallingActivity(Daire selectedDaire, ArrayList<CallSnapshotObject> callSnapshotObjects) {
//
//        DatabaseHelper databaseHelper = new DatabaseHelper(getActivity());
//
//        broadcastReceiverYonlendirme = new BroadcastReceiver() {
//            @Override
//            public void onReceive(Context context, Intent intent) {
//
//                boolean isReachable = intent.getBooleanExtra("isReachable", false);
//                if(! isReachable) {
//                    getActivity().unregisterReceiver(broadcastReceiverYonlendirme);
//                    broadcastReceiverYonlendirme = null;
//                    return;
//                }
//
//                boolean isNeedYonlendirme = intent.getBooleanExtra("isNeedYonlendirme", false);
//
//                ArrayList<String> callSnapshotImageIds = getCallSnapshotIds(callSnapshotObjects);
//                if(isNeedYonlendirme) {
//                    String yonlendirmeDeviceIp = intent.getStringExtra("deviceIP");
//                    Daire daireYonlendirme = databaseHelper.getDaireByIP(yonlendirmeDeviceIp);
////                    startCallingActivity(daireYonlendirme, isNeedYonlendirme, callSnapshotImageIds);
//
//                    showCallingActivity(daireYonlendirme, callSnapshotObjects);
//
//                } else {
//                    int totalDeviceCount = intent.getIntExtra("totalDeviceCount", 1);
//                    if(totalDeviceCount > 1) {
//                        boolean isBusy = intent.getBooleanExtra("isBusy", false);
//                        if(isBusy){
//                            Helper.showTopMessageBanner((AppCompatActivity) getActivity(), getString(R.string.arama_mesgul), false);
//                        }else{
//                            Intent intentMultiDevice = new Intent(getActivity(), MultipleDeviceCallRequestActivity.class);
//                            intentMultiDevice.putExtra("object", selectedDaire);
//                            startActivity(intentMultiDevice);
//                        }
//                    } else {
//                        startCallingActivity(selectedDaire, isNeedYonlendirme, callSnapshotImageIds);
//                    }
//                    showMenuFragment();
//                }
//
//                getActivity().unregisterReceiver(broadcastReceiverYonlendirme);
//                broadcastReceiverYonlendirme = null;
//
//            }
//        };
//
//        IntentFilter filter = new IntentFilter();
//        filter.addAction("com.netelsan.YONLENDIRME_STATE");
//        getActivity().registerReceiver(broadcastReceiverYonlendirme, filter);
//
//        ComPackageModel model = new ComPackageModel();
//        boolean isZilForSite = Helper.isZilForSite(getActivity());
//        if(isZilForSite){
//            ZilPanelSite zilPanelSiteSelf = databaseHelper.getSiteZilPanelByIP(Utils.getIPAddress(true));
//            model.setZilPanelSite(zilPanelSiteSelf);
//        }else{
//            ZilPanel zilPanelSelf = databaseHelper.getZilPanelByIP(Utils.getIPAddress(true));
//            model.setZilPanel(zilPanelSelf);
//        }
//
//        model.setOpe_type(Constants.OPERATION_REQUEST_YONLENDIRME);
//        model.setCallSnapshotObjects(callSnapshotObjects);
//
//        String destinationIp = selectedDaire.getIp();
//        TCPHelper.sendMessageToIP(getActivity(), destinationIp, model);
//
//    }
//
//    private void showMenuFragment(){
//
//        MainActivity mainActivity = (MainActivity) getActivity();
//        mainActivity.displayView(Constants.SCREEN_ONBOARDING, null);
//
//    }
//
//    private void startCallingActivity(Daire selectedDaire, boolean isYonlendirme, ArrayList<String> callSnapshotImageIds) {
//        Intent intent = new Intent(getActivity(), CallActivity.class);
//        intent.putExtra(Constants.PARAM_CALL_RECEIVING, false);
//        intent.putExtra(Constants.PARAM_CALL_IS_YONLENDIRME, isYonlendirme);
//        intent.putExtra(Constants.PARAM_CALL_SS_IDS, callSnapshotImageIds);
//        intent.putExtra(Constants.PARAM_CALL_IS_NEED_VIDEO, true);
//        intent.putExtra(Constants.PARAM_IS_MULTI_CALL_ACCEPT, false);
//        intent.putExtra("object", selectedDaire);
//        startActivity(intent);
//    }
//
//    private void bottomNumbersClicked(String keyCode) {
//
//        if(isCountDownDialogRunning) {
//            return;
//        }
//
//        String stringOnTextView = daireAraText.getText().toString();
//
//        if(stringOnTextView.equals("- - -")) {
//            stringOnTextView = "";
//        }
//
//        if(stringOnTextView.length() >= 3) {
//            return;
//        }
//
//        stringOnTextView = stringOnTextView + keyCode;
//        daireAraText.setText(stringOnTextView);
//
//    }
//
//    private void bottomLockClicked() {
//
//        if(isCountDownDialogRunning) {
//            return;
//        }
//
//        MainActivity mainActivity = (MainActivity) getActivity();
//        mainActivity.displayView(Constants.SCREEN_KAPI_AC, null);
//    }
//
//    private void bottomMenuBackClicked() {
//
//        if(isCountDownDialogRunning) {
//            return;
//        }
//
//        String stringOnTextView = daireAraText.getText().toString();
//
//        if(stringOnTextView.isEmpty() || stringOnTextView.equals("- - -")) {
//            MainActivity mainActivity = (MainActivity) getActivity();
//            mainActivity.displayView(Constants.SCREEN_IDLE_MENU, null);
//            return;
//        }
//
//        int totalLength = stringOnTextView.length();
//
//        String finalText = stringOnTextView.substring(0, totalLength - 1);
//
//        if(finalText.isEmpty()) {
//            daireAraText.setText("- - -");
//        } else {
//            daireAraText.setText(finalText);
//        }
//
//    }
//
//}

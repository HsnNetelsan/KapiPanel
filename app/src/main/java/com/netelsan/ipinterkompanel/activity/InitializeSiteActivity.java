package com.netelsan.ipinterkompanel.activity;

import android.app.AlertDialog;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.IBinder;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;

import androidx.annotation.Nullable;

import com.netelsan.ipinterkompanel.Constants;
import com.netelsan.ipinterkompanel.Helper;
import com.netelsan.ipinterkompanel.R;
import com.netelsan.ipinterkompanel.database.DatabaseHelper;
import com.netelsan.ipinterkompanel.listener.KeyPadListener;
import com.netelsan.ipinterkompanel.model.ZilPanel;
import com.netelsan.ipinterkompanel.model.ZilPanelSite;
import com.netelsan.ipinterkompanel.service.KeyPadService;
import com.stericson.RootShell.execution.Command;
import com.stericson.RootTools.RootTools;

public class InitializeSiteActivity extends BaseActivity implements KeyPadListener, ServiceConnection {

    TextView initializeTitle;

    EditText initializeSiteKapiNo;

    KeyPadService keyPadService;

    boolean isSite;
    boolean isCenterDeviceZil;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_initialize_site);
//
        initializeTitle = findViewById(R.id.initializeTitle);
        initializeSiteKapiNo = findViewById(R.id.initializeSiteKapiNo);

        Intent serviceConnIntent = new Intent(this, KeyPadService.class);
        bindService(serviceConnIntent, this, Context.BIND_AUTO_CREATE);

        if(keyPadService != null) {
            keyPadService.addListener(InitializeSiteActivity.this, InitializeSiteActivity.class.getName());
        }

        processIntent(getIntent());

    }

    private void processIntent(Intent intent) {
        if(intent == null) {
            return;
        }
        Bundle bundle = intent.getExtras();
        if(bundle == null) {
            return;
        }

        isSite = bundle.getBoolean("isSite");
        isCenterDeviceZil = bundle.getBoolean("isCenterDeviceZil");

        if(isSite) {
            initializeTitle.setText(getString(R.string.initialize_title_1) + " " + getString(R.string.site_zil_paneli) + " " + getString(R.string.initialize_title_2));
        } else {
            initializeTitle.setText(getString(R.string.initialize_title_1) + " " + getString(R.string.zil_paneli) + " " + getString(R.string.initialize_title_2));
        }

    }

    @Override
    public void onServiceConnected(ComponentName name, IBinder binder) {
        keyPadService = ((KeyPadService.MyBinder) binder).getService();
        keyPadService.addListener(InitializeSiteActivity.this, InitializeSiteActivity.class.getName());
    }

    @Override
    public void onServiceDisconnected(ComponentName name) {
        keyPadService = null;
    }

    @Override
    public void onKeyPressed(String keyCode) {

        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                keyPressed(keyCode);
            }
        });
    }

    @Override
    public void onRFIDDetected(String rfid) {

    }

    private void keyPressed(String keyCode) {

        if(keyCode.equals(Constants.KEYPAD_BACK)) {
            menuBackPressed();
        } else if(keyCode.equals(Constants.KEYPAD_UP)) {
            menuArrowClicked(true);
        } else if(keyCode.equals(Constants.KEYPAD_DOWN)) {
            menuArrowClicked(false);
        } else if(keyCode.equals(Constants.KEYPAD_HOME)) {
            menuOkayClicked();
        } else if(keyCode.equals(Constants.KEYPAD_CALL)) {

        } else if(keyCode.equals(Constants.KEYPAD_LOCK)) {

        } else {
            setDeviceNumbers(keyCode);
        }

    }

    private void menuBackPressed() {

        if(isSummaryDialogRunning) {
            if(summaryDialog != null) {
                summaryDialog.dismiss();
                summaryDialog = null;
            }
            return;
        }

        String currentText = initializeSiteKapiNo.getText().toString();
        if(currentText.isEmpty()) {
            finishCurrentActivity();
        } else {
            int totalLength = currentText.length();
            currentText = currentText.substring(0, totalLength - 1);
            initializeSiteKapiNo.setText(currentText);
        }

    }

    private void finishCurrentActivity() {
        Intent intent = new Intent(InitializeSiteActivity.this, SelectDeviceTypeActivity.class);
        startActivity(intent);
        this.overridePendingTransition(0, 0);
        InitializeSiteActivity.this.finish();
    }

    private void menuOkayClicked() {

        if(isSummaryDialogRunning) {
            setDeviceSettingsAndReboot();
            return;
        }

        String currentText = initializeSiteKapiNo.getText().toString();
        if(currentText.isEmpty()) {
            Helper.showTopMessageBanner(InitializeSiteActivity.this, getString(R.string.lutfen_kapi_no_girin), false);
            return;
        }

        try {
            int intKapiNo = Integer.parseInt(currentText);

            if(intKapiNo == 0) {
                Helper.showTopMessageBanner(InitializeSiteActivity.this, getString(R.string.kapi_no_sifir_olamaz), false);
                return;
            }

            showSummaryDialog(intKapiNo);

        } catch(Exception e) {
            return;
        }

    }

    private void setDeviceSettingsAndReboot() {

        String currentText = initializeSiteKapiNo.getText().toString();
        int intKapiNo = Integer.parseInt(currentText);

        String generatedIP = generateSuitableIP(intKapiNo);

        setDeviceInfoInDB(intKapiNo, generatedIP);

        setDeviceIP(generatedIP);

    }


    private void setDeviceIP(String generatedIP) {

        Helper.showWhiteScreen(InitializeSiteActivity.this);

        Helper.setZilForSite(getApplicationContext(), true);

        Helper.setCenterUnitZilPanel(getApplicationContext(), isCenterDeviceZil);
        setDeviceIPWithSU(generatedIP);

    }

    public void setDeviceIPWithSU(String generatedIP) {

        try {

            Helper.setInitializeFinished(getApplicationContext(), true);

            Command command = new Command(0, "sqlite3 /data/data/com.android.providers.settings/databases/settings.db \"UPDATE global SET value = '0' WHERE name = 'eth_mode';\"",
                    "sqlite3 /data/data/com.android.providers.settings/databases/settings.db \"UPDATE global SET value = '" + generatedIP + "' WHERE name = 'eth_ip';\"",
                    "sqlite3 /data/data/com.android.providers.settings/databases/settings.db \"UPDATE global SET value = '255.0.0.0' WHERE name = 'eth_netmask';\"",
//                    "sqlite3 /data/data/com.android.providers.settings/databases/settings.db \"UPDATE global SET value = '172.1.255.1' WHERE name = 'eth_route';\"",
                    "sqlite3 /data/data/com.android.providers.settings/databases/settings.db \"UPDATE global SET value = '8.8.8.8' WHERE name = 'eth_dns';\"",
                    "reboot");
            RootTools.getShell(true).add(command).getExitCode();

        } catch(Exception e) {
            e.printStackTrace();
        }

    }

    private void setDeviceInfoInDB(int kapiNo, String generatedIP) {


        ZilPanelSite zilPanelSite = new ZilPanelSite();
        zilPanelSite.setIp(generatedIP);
        zilPanelSite.setDeviceName(getString(R.string.site_kapisi).toUpperCase() + " " + kapiNo);
        zilPanelSite.setKapiNo(kapiNo);

        DatabaseHelper databaseHelper = new DatabaseHelper(getApplicationContext());
        databaseHelper.insertSiteZilPanel(zilPanelSite);

    }

    private String generateSuitableIP(int kapiNo) {

        int blokPart = 0;
        if(kapiNo > 255) {
            blokPart = kapiNo / 256;
            kapiNo = kapiNo - (256 * blokPart);
        }
        String generatedIP = "172.252." + blokPart + "." + kapiNo;
//        String generatedIP = "192.168." + intValueForBlokNo + "." + kapiNo;
        Log.d(Constants.LOG_TAG, "generatedIP=" + generatedIP);
        return generatedIP;
//        return "172.1.255.1";
    }

    AlertDialog summaryDialog;
    boolean isSummaryDialogRunning = false;

    private void showSummaryDialog(int kapiNo) {

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("");

        View customLayout = getLayoutInflater().inflate(R.layout.custom_summary_site_dialog, null);
        builder.setView(customLayout);

        TextView summaryKapiNo = customLayout.findViewById(R.id.summaryKapiNo);
        summaryKapiNo.setText(kapiNo + "");

        builder.setOnDismissListener(new DialogInterface.OnDismissListener() {
            @Override
            public void onDismiss(DialogInterface dialog) {
                isSummaryDialogRunning = false;
            }
        });
        summaryDialog = builder.create();
        summaryDialog.show();
        isSummaryDialogRunning = true;

    }

    private void setDeviceNumbers(String keycode) {

        String currentText = initializeSiteKapiNo.getText().toString();

        initializeSiteKapiNo.setText(currentText + keycode);

    }

    private void menuArrowClicked(boolean isUp) {

        moveMenuArrow(isUp);

    }

    private void moveMenuArrow(boolean isUp) {

    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

        if(keyPadService != null) {
            keyPadService.removeListener(InitializeSiteActivity.class.getName());
        }

        unbindService(this);

    }
}

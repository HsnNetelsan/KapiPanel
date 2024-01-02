package com.netelsan.ipinterkompanel.activity;

import android.app.AlertDialog;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.IBinder;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.Nullable;

import com.netelsan.ipinterkompanel.Constants;
import com.netelsan.ipinterkompanel.Helper;
import com.netelsan.ipinterkompanel.R;
import com.netelsan.ipinterkompanel.database.DatabaseHelper;
import com.netelsan.ipinterkompanel.listener.KeyPadListener;
import com.netelsan.ipinterkompanel.model.ZilPanel;
import com.netelsan.ipinterkompanel.service.KeyPadService;
import com.stericson.RootShell.execution.Command;
import com.stericson.RootTools.RootTools;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.text.SimpleDateFormat;
import java.util.Calendar;

public class InitializeActivity extends BaseActivity implements KeyPadListener, ServiceConnection {

    TextView initializeTitle;

    ImageView setTimeArrow;
    ImageView setDeviceArrow;

    TextView saatResult;
    TextView tarihResult;
    TextView deviceBlokResult;
    TextView deviceKapiNoResult;

    int selectedMenuId = Constants.INIT_MENU_ZAMAN;

    KeyPadService keyPadService;

    boolean isSite;
    boolean isCenterDeviceZil;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_initialize);
//
        initializeTitle = findViewById(R.id.initializeTitle);

        setTimeArrow = findViewById(R.id.setTimeArrow);
        setDeviceArrow = findViewById(R.id.setDeviceArrow);

        saatResult = findViewById(R.id.saatResult);
        tarihResult = findViewById(R.id.tarihResult);
        deviceBlokResult = findViewById(R.id.deviceBlokResult);
        deviceKapiNoResult = findViewById(R.id.deviceKapiNoResult);

        Intent serviceConnIntent = new Intent(this, KeyPadService.class);
        bindService(serviceConnIntent, this, Context.BIND_AUTO_CREATE);

        if(keyPadService != null) {
            keyPadService.addListener(InitializeActivity.this, InitializeActivity.class.getName());
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
        keyPadService.addListener(InitializeActivity.this, InitializeActivity.class.getName());
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
            }
            return;
        }
        if(selectedMenuId == Constants.INIT_MENU_ZAMAN) {

            if(isSetZamanActive) {
                if(selectedZamanPart == 2) {
                    selectedZamanPart = 1;
                    zamanDialogSaatArrow.setVisibility(View.VISIBLE);
                    zamanDialogDakikaArrow.setVisibility(View.INVISIBLE);
                } else {
                    if(zamanDialog != null) {
                        zamanDialog.dismiss();
                    }
                }
            } else if(isSetTarihActive) {
                if(selectedTarihPart == 3) {
                    selectedTarihPart = 2;

                    tarihDialogGunArrow.setVisibility(View.INVISIBLE);
                    tarihDialogAyArrow.setVisibility(View.VISIBLE);
                    tarihDialogYilArrow.setVisibility(View.INVISIBLE);
                } else if(selectedTarihPart == 2) {
                    selectedTarihPart = 1;

                    tarihDialogGunArrow.setVisibility(View.VISIBLE);
                    tarihDialogAyArrow.setVisibility(View.INVISIBLE);
                    tarihDialogYilArrow.setVisibility(View.INVISIBLE);
                } else {
                    if(tarihDialog != null) {
                        tarihDialog.dismiss();
                    }
                }
            } else {
                finishCurrentActivity();
            }

        } else if(selectedMenuId == Constants.INIT_MENU_CIHAZ) {

            if(isSetDeviceActive) {

                if(selectedDevicePart == 1) {

                    String blokNo = deviceDialogBlokNo.getText().toString();
                    if(blokNo.equals("-")) {
                        isSetDeviceActive = false;
                        selectedDevicePart = 1;
                        deviceDialog.dismiss();
                        return;
                    }
                    if(blokNo.length() == 1) {
                        blokNo = "-";
                    }
                    if(blokNo.length() == 2) {
                        blokNo = blokNo.substring(0, 1);
                    }
                    if(blokNo.length() == 3) {
                        blokNo = blokNo.substring(0, 2);
                    }
                    deviceDialogBlokNo.setText(blokNo);

                } else if(selectedDevicePart == 2) {

                    String kapiNo = deviceDialogKapiNo.getText().toString();
                    if(kapiNo.equals("-")) {
                        selectedDevicePart = 1;
                        deviceDialogBlokArrow.setVisibility(View.VISIBLE);
                        deviceDialogKapiArrow.setVisibility(View.INVISIBLE);
                        return;
                    }
                    if(kapiNo.length() == 1) {
                        kapiNo = "-";
                    }
                    if(kapiNo.length() == 2) {
                        kapiNo = kapiNo.substring(0, 1);
                    }
                    if(kapiNo.length() == 3) {
                        kapiNo = kapiNo.substring(0, 2);
                    }
                    deviceDialogKapiNo.setText(kapiNo);

                }

            } else {
                finishCurrentActivity();
            }

        }

    }

    private void finishCurrentActivity() {
        Intent intent = new Intent(InitializeActivity.this, SelectDeviceTypeActivity.class);
        startActivity(intent);
        this.overridePendingTransition(0, 0);
        InitializeActivity.this.finish();
    }

    int selectedHour = - 1;
    int selectedMinute = - 1;
    int selectedDayofMonth = - 1;
    int selectedMonth = - 1;
    int selectedYear = - 1;

    String selectedBlokNo = "";
    String selectedKapiNo = "";

    private void menuOkayClicked() {

        if(isSummaryDialogRunning) {
            setDeviceSettingsAndRestart();
            return;
        }

        if(selectedMenuId == Constants.INIT_MENU_ZAMAN) {

            if(isSetZamanActive) {
//                burada seçili olan saat mi dakikamı diye kontrol et
                if(selectedZamanPart == 1) {
                    selectedZamanPart = 2;
                    zamanDialogSaatArrow.setVisibility(View.INVISIBLE);
                    zamanDialogDakikaArrow.setVisibility(View.VISIBLE);
                } else if(selectedZamanPart == 2) {

                    String selectedSaat = zamanDialogSaat.getText().toString();
                    String selectedDakika = zamanDialogDakika.getText().toString();

                    selectedHour = Integer.parseInt(selectedSaat);
                    selectedMinute = Integer.parseInt(selectedDakika);

                    String selectedZaman = selectedSaat + ":" + selectedDakika;
                    saatResult.setText(selectedZaman);
                    zamanDialog.dismiss();
                    isSetZamanActive = false;

                    selectedTarihPart = 1;
                    showTarihDialog();
//                    burada saati kaydet ve tarih dialog u aç
                }
            } else if(isSetTarihActive) {

                if(selectedTarihPart == 1) {
                    selectedTarihPart = 2;
                    tarihDialogGunArrow.setVisibility(View.INVISIBLE);
                    tarihDialogAyArrow.setVisibility(View.VISIBLE);
                    tarihDialogYilArrow.setVisibility(View.INVISIBLE);
                } else if(selectedTarihPart == 2) {
                    selectedTarihPart = 3;
                    tarihDialogGunArrow.setVisibility(View.INVISIBLE);
                    tarihDialogAyArrow.setVisibility(View.INVISIBLE);
                    tarihDialogYilArrow.setVisibility(View.VISIBLE);
                } else if(selectedTarihPart == 3) {

                    String selectedGun = tarihDialogGun.getText().toString();
                    String selectedAy = tarihDialogAy.getText().toString();
                    String selectedYil = tarihDialogYil.getText().toString();

                    selectedDayofMonth = Integer.parseInt(selectedGun);

//                    burada ay değerini android sistemine göre ata
                    selectedMonth = Helper.getSystemMonthId(Integer.parseInt(selectedAy));
                    selectedYear = Integer.parseInt(selectedYil);

                    String selectedTarih = selectedGun + "." + selectedAy + "." + selectedYil;

                    boolean isDateValid = Helper.isDateValid(selectedTarih);
                    if(! isDateValid) {
                        Helper.showTopMessageBanner(InitializeActivity.this, getString(R.string.hatali_tarih), false);
                        return;
                    }

                    tarihResult.setText(selectedTarih);
                    tarihDialog.dismiss();
                    isSetTarihActive = false;

                    showSummaryScreenIfNeeded();

                }

            } else {
                selectedZamanPart = 1;
                showZamanDialog();
            }

        } else if(selectedMenuId == Constants.INIT_MENU_CIHAZ) {

            if(isSetDeviceActive) {

                if(selectedDevicePart == 1) {
                    selectedDevicePart = 2;
                    deviceDialogBlokArrow.setVisibility(View.INVISIBLE);
                    deviceDialogKapiArrow.setVisibility(View.VISIBLE);
                } else if(selectedDevicePart == 2) {

                    String blokNo = deviceDialogBlokNo.getText().toString();
                    if(blokNo.equals("-")) {
                        Helper.showTopMessageBanner(InitializeActivity.this, getString(R.string.lutfen_blok_no_girin), false);
                        return;
                    }
                    String kapiNo = deviceDialogKapiNo.getText().toString();
                    if(kapiNo.equals("-")) {
                        Helper.showTopMessageBanner(InitializeActivity.this, getString(R.string.lutfen_kapi_no_girin), false);
                        return;
                    }

                    int kapiNoInt = Integer.parseInt(kapiNo);
                    if(kapiNoInt == 0) {
                        Helper.showTopMessageBanner(InitializeActivity.this, getString(R.string.kapi_no_sifir_olamaz), false);
                        return;
                    }

                    selectedBlokNo = blokNo;
                    selectedKapiNo = kapiNo;

                    deviceBlokResult.setText(getString(R.string.blok_no_two_dots) + blokNo);
                    deviceKapiNoResult.setText(getString(R.string.kapi_no_two_dots) + kapiNo);
                    deviceDialog.dismiss();
                    isSetDeviceActive = false;

                    showSummaryScreenIfNeeded();

//                    tarih ve saat değişikliklerini kaydet
//                   burada blok numarası ve kapı numarasına göre ip yi set et
                }

            } else {
                selectedDevicePart = 1;
                showSetDeviceSettings();
            }
        }

    }


    private void setDeviceSettingsAndRestart() {

        setDeviceInfoInDB();

        if(selectedHour == 24) {
            selectedHour = 0;
        }

        setDeviceTime(selectedHour, selectedMinute, selectedDayofMonth, selectedMonth, selectedYear);
        setDeviceIP();
//        rebootDevice();

    }

    private void setDeviceInfoInDB() {

        String generatedIP = generateSuitableIP(selectedBlokNo, selectedKapiNo);

        ZilPanel zilPanel = new ZilPanel();
        zilPanel.setIp(generatedIP);
        zilPanel.setDeviceName(getString(R.string.blok).toUpperCase() + " " + selectedBlokNo + " " + getString(R.string.kapi).toUpperCase() + " " + selectedKapiNo);
        zilPanel.setBlok(Integer.parseInt(selectedBlokNo));
        zilPanel.setKapiNo(Integer.parseInt(selectedKapiNo));

        DatabaseHelper databaseHelper = new DatabaseHelper(getApplicationContext());
        databaseHelper.insertZilPanel(zilPanel);

    }

    private void setDeviceIP() {

//        String blokNo = deviceBlokResult.getText().toString();
//        String kapiNo = deviceKapiNoResult.getText().toString();

        String generatedIP = generateSuitableIP(selectedBlokNo, selectedKapiNo);

        Helper.showWhiteScreen(InitializeActivity.this);

        Helper.setZilForSite(getApplicationContext(), false);

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

    private String generateSuitableIP(String blokNo, String kapiNo) {

        String generatedIP = "172." + blokNo + ".255." + kapiNo;
//        String generatedIP = "192.168." + intValueForBlokNo + "." + kapiNo;
//        Log.d(Constants.LOG_TAG, "generatedIP=" + generatedIP);
        return generatedIP;
//        return "172.1.255.1";
    }

    private void showSummaryScreenIfNeeded() {

        String blokNo = deviceBlokResult.getText().toString();
        String kapiNo = deviceKapiNoResult.getText().toString();

        if(selectedHour != - 1 && selectedMinute != - 1 && selectedDayofMonth != - 1 && selectedMonth != - 1 && selectedYear != - 1 && ! blokNo.isEmpty() && ! kapiNo.isEmpty()) {
            showSummaryScreen();
        } else {
            if(selectedMenuId == Constants.INIT_MENU_ZAMAN) {
                moveMenuArrow(false);
                showSetDeviceSettings();
            } else if(selectedMenuId == Constants.INIT_MENU_CIHAZ) {
                moveMenuArrow(true);
                selectedZamanPart = 1;
                showZamanDialog();
            }
        }
    }

    AlertDialog summaryDialog;
    boolean isSummaryDialogRunning = false;

    private void showSummaryScreen() {

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("");

        View customLayout = getLayoutInflater().inflate(R.layout.custom_summary_dialog, null);
        builder.setView(customLayout);

        String timeFormatted = Helper.getFormattedTime(selectedHour, selectedMinute);
        TextView summarySaat = customLayout.findViewById(R.id.summarySaat);
        summarySaat.setText(timeFormatted);

        String dateFormatted = Helper.getFormattedDate(selectedDayofMonth, selectedMonth, selectedYear);
        TextView summaryTarih = customLayout.findViewById(R.id.summaryTarih);
        summaryTarih.setText(dateFormatted);

        TextView summaryBlokNo = customLayout.findViewById(R.id.summaryBlokNo);
        summaryBlokNo.setText(deviceBlokResult.getText().toString());

        TextView summaryKapiNo = customLayout.findViewById(R.id.summaryKapiNo);
        summaryKapiNo.setText(deviceKapiNoResult.getText().toString());

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


    boolean isSetDeviceActive = false;
    AlertDialog deviceDialog;
    int selectedDevicePart = 1;//1=Blok, 2=Kapı
    TextView deviceDialogBlokNo;
    TextView deviceDialogKapiNo;
    ImageView deviceDialogBlokArrow;
    ImageView deviceDialogKapiArrow;

    private void showSetDeviceSettings() {

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("");

        View customLayout = getLayoutInflater().inflate(R.layout.custom_device_dialog, null);
        builder.setView(customLayout);

        deviceDialogBlokNo = customLayout.findViewById(R.id.deviceDialogBlokNo);
        deviceDialogKapiNo = customLayout.findViewById(R.id.deviceDialogKapiNo);

        deviceDialogBlokArrow = customLayout.findViewById(R.id.deviceDialogBlokArrow);
        deviceDialogKapiArrow = customLayout.findViewById(R.id.deviceDialogKapiArrow);

        builder.setOnDismissListener(new DialogInterface.OnDismissListener() {
            @Override
            public void onDismiss(DialogInterface dialog) {
                isSetDeviceActive = false;
            }
        });
        deviceDialog = builder.create();
        deviceDialog.show();
        isSetDeviceActive = true;

    }

    boolean isSetTarihActive = false;
    AlertDialog tarihDialog;
    int selectedTarihPart = 1;//1=Gun, 2=Ay, 3=Yıl
    TextView tarihDialogGun;
    TextView tarihDialogAy;
    TextView tarihDialogYil;
    ImageView tarihDialogGunArrow;
    ImageView tarihDialogAyArrow;
    ImageView tarihDialogYilArrow;

    private void showTarihDialog() {

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("");

        View customLayout = getLayoutInflater().inflate(R.layout.custom_tarih_dialog, null);
        builder.setView(customLayout);

        tarihDialogGun = customLayout.findViewById(R.id.tarihDialogGun);
        tarihDialogAy = customLayout.findViewById(R.id.tarihDialogAy);
        tarihDialogYil = customLayout.findViewById(R.id.tarihDialogYil);

        tarihDialogGunArrow = customLayout.findViewById(R.id.tarihDialogGunArrow);
        tarihDialogAyArrow = customLayout.findViewById(R.id.tarihDialogAyArrow);
        tarihDialogYilArrow = customLayout.findViewById(R.id.tarihDialogYilArrow);

        builder.setOnDismissListener(new DialogInterface.OnDismissListener() {
            @Override
            public void onDismiss(DialogInterface dialog) {
                isSetTarihActive = false;
            }
        });
        tarihDialog = builder.create();
        tarihDialog.show();
        isSetTarihActive = true;

    }

    boolean isSetZamanActive = false;
    AlertDialog zamanDialog;
    int selectedZamanPart = 1;//1=Saat, 2=Dakika
    TextView zamanDialogSaat;
    TextView zamanDialogDakika;
    ImageView zamanDialogSaatArrow;
    ImageView zamanDialogDakikaArrow;

    private void showZamanDialog() {

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("");

        View customLayout = getLayoutInflater().inflate(R.layout.custom_zaman_dialog, null);
        builder.setView(customLayout);

        zamanDialogSaat = customLayout.findViewById(R.id.zamanDialogSaat);
        zamanDialogDakika = customLayout.findViewById(R.id.zamanDialogDakika);

        zamanDialogSaatArrow = customLayout.findViewById(R.id.zamanDialogSaatArrow);
        zamanDialogDakikaArrow = customLayout.findViewById(R.id.zamanDialogDakikaArrow);

        builder.setOnDismissListener(new DialogInterface.OnDismissListener() {
            @Override
            public void onDismiss(DialogInterface dialog) {
                isSetZamanActive = false;
            }
        });
        zamanDialog = builder.create();
        zamanDialog.show();
        isSetZamanActive = true;

    }

    private void setDeviceNumbers(String keycode) {

        if(isSetDeviceActive) {

            if(selectedDevicePart == 1) {//burada ip organizasyonuna göre blok numarası en fazla 244 olabilir.

                String blokNo = deviceDialogBlokNo.getText().toString();

                if(blokNo.length() == 3) {
                    Helper.showTopMessageBanner(InitializeActivity.this, getString(R.string.en_fazla_3_hane_olabilir), false);
                    return;
                }
                if(blokNo.equals("-")) {
                    blokNo = "";
                }

                blokNo = blokNo + keycode;
                int intBlokNo = Integer.parseInt(blokNo);
                if(intBlokNo > 244) {
                    Helper.showTopMessageBanner(InitializeActivity.this, getString(R.string.blok_no_0_244_arasi), false);
                    return;
                }
                deviceDialogBlokNo.setText(blokNo);

            } else if(selectedDevicePart == 2) {

                String kapiNo = deviceDialogKapiNo.getText().toString();

                if(kapiNo.length() == 3) {
                    Helper.showTopMessageBanner(InitializeActivity.this, getString(R.string.en_fazla_3_hane_olabilir), false);
                    return;
                }
                if(kapiNo.equals("-")) {
                    kapiNo = "";
                }

                kapiNo = kapiNo + keycode;
                int intKapiNo = Integer.parseInt(kapiNo);
                if(intKapiNo > 255) {
                    Helper.showTopMessageBanner(InitializeActivity.this, getString(R.string.kapi_no_0_255_arasi), false);
                    return;
                }
                deviceDialogKapiNo.setText(kapiNo);
            }

        } else if(isSetZamanActive) {

            if(selectedZamanPart == 1) {

                int keyCodeInt = Integer.parseInt(keycode);

                String currentSaat = zamanDialogSaat.getText().toString();
                int totalLength = currentSaat.length();
                int currentSaatInt = Integer.parseInt(currentSaat);

                if(totalLength == 2) {
                    zamanDialogSaat.setText(keycode);
                } else if(totalLength == 1) {
                    if(currentSaatInt > 2) {
                        zamanDialogSaat.setText(keycode);
                    } else if(currentSaatInt == 2) {
                        if(keyCodeInt <= 4) {
                            zamanDialogSaat.setText(currentSaat + "" + keycode);
                        }
                    } else {
                        zamanDialogSaat.setText(currentSaat + "" + keycode);
                    }
                }

            } else if(selectedZamanPart == 2) {

                int keyCodeInt = Integer.parseInt(keycode);

                String currentDakika = zamanDialogDakika.getText().toString();
                int totalLength = currentDakika.length();
                int currentDakikaInt = Integer.parseInt(currentDakika);

                if(totalLength == 2) {
                    zamanDialogDakika.setText(keycode);
                } else if(totalLength == 1) {

                    if(currentDakikaInt >= 0 && currentDakikaInt <= 5) {
                        zamanDialogDakika.setText(currentDakika + "" + keycode);
                    } else if(currentDakikaInt == 6) {
                        if(keyCodeInt == 0) {
                            zamanDialogDakika.setText(currentDakika + "" + keycode);
                        }
                    }

                }

            }

        } else if(isSetTarihActive) {

            int keyCodeInt = Integer.parseInt(keycode);

            if(selectedTarihPart == 1) {

                String currentGun = tarihDialogGun.getText().toString();
                int totalLength = currentGun.length();
                int currentGunInt = Integer.parseInt(currentGun);

                if(totalLength == 2) {
                    tarihDialogGun.setText(keycode);
                } else if(totalLength == 1) {
                    if(currentGunInt == 1 || currentGunInt == 2) {
                        tarihDialogGun.setText(currentGun + "" + keycode);
                    } else if(currentGunInt == 3) {
                        if(keyCodeInt <= 1) {
                            tarihDialogGun.setText(currentGun + "" + keycode);
                        }
                    } else {
                        tarihDialogGun.setText(keycode);
                    }
                }

            } else if(selectedTarihPart == 2) {

                String currentAy = tarihDialogAy.getText().toString();
                int totalLength = currentAy.length();
                int currentAyInt = Integer.parseInt(currentAy);

                if(totalLength == 2) {
                    tarihDialogAy.setText(keycode);
                } else if(totalLength == 1) {
                    if(currentAyInt == 1) {
                        if(keyCodeInt <= 2) {
                            tarihDialogAy.setText(currentAy + "" + keycode);
                        } else {
                            tarihDialogAy.setText(keycode);
                        }
                    } else {
                        tarihDialogAy.setText(keycode);
                    }
                }

            } else if(selectedTarihPart == 3) {

                String currentYil = tarihDialogYil.getText().toString();
                int totalLength = currentYil.length();
                int currentYilInt = Integer.parseInt(currentYil);

                if(totalLength == 4) {
                    if(keyCodeInt == 2) {
                        tarihDialogYil.setText(keycode);
                    }
                } else if(totalLength == 1) {
                    if(keyCodeInt == 0) {
                        tarihDialogYil.setText(currentYil + "" + keycode);
                    }
                } else if(totalLength == 2) {
                    if(keyCodeInt == 1 || keyCodeInt == 2) {
                        tarihDialogYil.setText(currentYil + "" + keycode);
                    }
                } else if(totalLength == 3) {
                    tarihDialogYil.setText(currentYil + "" + keycode);
                }

            }

        }

    }

    private void menuArrowClicked(boolean isUp) {

        if(isSetZamanActive) {

//                burada seçili olan saat mi dakikamı diye kontrol et

            if(selectedZamanPart == 1) {
                selectedZamanPart = 2;
                zamanDialogSaatArrow.setVisibility(View.INVISIBLE);
                zamanDialogDakikaArrow.setVisibility(View.VISIBLE);
            } else if(selectedZamanPart == 2) {
                selectedZamanPart = 1;
                zamanDialogSaatArrow.setVisibility(View.VISIBLE);
                zamanDialogDakikaArrow.setVisibility(View.INVISIBLE);
            }

//            increaseZamanParts(isUp);
        } else if(isSetTarihActive) {

            if(selectedTarihPart == 1) {
                if(isUp) {
                    selectedTarihPart = 2;
                    tarihDialogGunArrow.setVisibility(View.INVISIBLE);
                    tarihDialogAyArrow.setVisibility(View.VISIBLE);
                    tarihDialogYilArrow.setVisibility(View.INVISIBLE);
                } else {
                    selectedTarihPart = 3;
                    tarihDialogGunArrow.setVisibility(View.INVISIBLE);
                    tarihDialogAyArrow.setVisibility(View.INVISIBLE);
                    tarihDialogYilArrow.setVisibility(View.VISIBLE);
                }
            } else if(selectedTarihPart == 2) {
                if(isUp) {
                    selectedTarihPart = 3;
                    tarihDialogGunArrow.setVisibility(View.INVISIBLE);
                    tarihDialogAyArrow.setVisibility(View.INVISIBLE);
                    tarihDialogYilArrow.setVisibility(View.VISIBLE);
                } else {
                    selectedTarihPart = 1;
                    tarihDialogGunArrow.setVisibility(View.VISIBLE);
                    tarihDialogAyArrow.setVisibility(View.INVISIBLE);
                    tarihDialogYilArrow.setVisibility(View.INVISIBLE);
                }
            } else if(selectedTarihPart == 3) {
                if(isUp) {
                    selectedTarihPart = 1;
                    tarihDialogGunArrow.setVisibility(View.VISIBLE);
                    tarihDialogAyArrow.setVisibility(View.INVISIBLE);
                    tarihDialogYilArrow.setVisibility(View.INVISIBLE);
                } else {
                    selectedTarihPart = 2;
                    tarihDialogGunArrow.setVisibility(View.INVISIBLE);
                    tarihDialogAyArrow.setVisibility(View.VISIBLE);
                    tarihDialogYilArrow.setVisibility(View.INVISIBLE);
                }
            }

//            increaseTarihParts(isUp);
        } else if(isSetDeviceActive) {

        } else {
            moveMenuArrow(isUp);
        }
    }

    private void increaseTarihParts(boolean isUp) {

        if(selectedTarihPart == 1) {

            String currentGun = tarihDialogGun.getText().toString();
            int value = Integer.parseInt(currentGun);
            if(isUp) {
                if(value == 31) {
                    value = 1;
                } else {
                    value = value + 1;
                }
            } else {
                if(value == 1) {
                    value = 31;
                } else {
                    value = value - 1;
                }
            }

            tarihDialogGun.setText(value + "");

        } else if(selectedTarihPart == 2) {

            String currentAy = tarihDialogAy.getText().toString();
            int value = Integer.parseInt(currentAy);
            if(isUp) {
                if(value == 12) {
                    value = 1;
                } else {
                    value = value + 1;
                }
            } else {
                if(value == 1) {
                    value = 12;
                } else {
                    value = value - 1;
                }
            }

            tarihDialogAy.setText(value + "");

        } else {
            String currentYil = tarihDialogYil.getText().toString();
            int value = Integer.parseInt(currentYil);

            if(isUp) {
                value = value + 1;
            } else {
                if(value == 2019) {
                    value = 2019;
                } else {
                    value = value - 1;
                }
            }

            tarihDialogYil.setText(value + "");

        }

    }

    private void increaseZamanParts(boolean isUp) {

        if(selectedZamanPart == 1) {
            String currentSaat = zamanDialogSaat.getText().toString();
            int value = Integer.parseInt(currentSaat);
            if(isUp) {
                if(value == 23) {
                    value = 00;
                } else {
                    value = value + 1;
                }
            } else {
                if(value == 0) {
                    value = 23;
                } else {
                    value = value - 1;
                }
            }

            zamanDialogSaat.setText(value + "");
        } else {
            String currentDakika = zamanDialogDakika.getText().toString();
            int value = Integer.parseInt(currentDakika);

            if(isUp) {
                if(value == 59) {
                    value = 00;
                } else {
                    value = value + 1;
                }
            } else {
                if(value == 0) {
                    value = 59;
                } else {
                    value = value - 1;
                }
            }

            zamanDialogDakika.setText(value + "");

        }

    }

    private void moveMenuArrow(boolean isUp) {

        hideAllArrows();

        if(isUp) {

            if(selectedMenuId == Constants.INIT_MENU_ZAMAN) {
                selectedMenuId = Constants.INIT_MENU_CIHAZ;
            } else {
                selectedMenuId = selectedMenuId - 1;
            }

        } else {

            if(selectedMenuId == Constants.INIT_MENU_CIHAZ) {
                selectedMenuId = Constants.INIT_MENU_ZAMAN;
            } else {
                selectedMenuId = selectedMenuId + 1;
            }
        }

        setMenuArrow();

    }

    private void setMenuArrow() {

        if(selectedMenuId == Constants.INIT_MENU_ZAMAN) {
            setTimeArrow.setVisibility(View.VISIBLE);
        } else if(selectedMenuId == Constants.INIT_MENU_CIHAZ) {
            setDeviceArrow.setVisibility(View.VISIBLE);
        }

    }

    private void hideAllArrows() {

        setTimeArrow.setVisibility(View.INVISIBLE);
        setDeviceArrow.setVisibility(View.INVISIBLE);
    }

    private void setDeviceTime(int hour, int minute, int dayofMonth, int month, int year) {

        Calendar calendar = Calendar.getInstance();
        calendar.set(Calendar.YEAR, year);
        calendar.set(Calendar.MONTH, month);
        calendar.set(Calendar.DAY_OF_MONTH, dayofMonth);
        calendar.set(Calendar.MINUTE, minute);
        calendar.set(Calendar.HOUR_OF_DAY, hour);
        calendar.set(Calendar.SECOND, 30);

        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyyMMdd.HHmmss");
        String formattedDate = dateFormat.format(calendar.getTime());
//        Log.d(Constants.LOG_TAG, "formattedDate=" + formattedDate);

        try {
//            Process process = Runtime.getRuntime().exec("su 0 toolbox date -s 20191015.084000");//YYYYMMDD.HHmmss
            Process process = Runtime.getRuntime().exec("su 0 toolbox date -s " + formattedDate);//YYYYMMDD.HHmmss
            BufferedReader in = new BufferedReader(new InputStreamReader(process.getInputStream()));

//            refreshDateAndTime(calendar);

        } catch(Exception e) {
            e.printStackTrace();
        }

    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
//        isActivityAlive = false;

        if(keyPadService != null) {
            keyPadService.removeListener(InitializeActivity.class.getName());
        }

    }
}

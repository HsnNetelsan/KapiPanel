package com.netelsan.ipinterkompanel.fragments.settings;

import android.app.AlertDialog;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.IBinder;
import android.provider.CalendarContract;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.netelsan.ipinterkompanel.Constants;
import com.netelsan.ipinterkompanel.Helper;
import com.netelsan.ipinterkompanel.R;
import com.netelsan.ipinterkompanel.activity.MainActivity;
import com.netelsan.ipinterkompanel.adapter.ApkListAdapter;
import com.netelsan.ipinterkompanel.database.DatabaseHelper;
import com.netelsan.ipinterkompanel.fragments.BaseFragment;
import com.netelsan.ipinterkompanel.fragments.RelayTimeFragment;
import com.netelsan.ipinterkompanel.listener.KeyPadListener;
import com.netelsan.ipinterkompanel.model.APKListItem;
import com.netelsan.ipinterkompanel.model.ComPackageModel;
import com.netelsan.ipinterkompanel.model.Daire;
import com.netelsan.ipinterkompanel.model.Guvenlik;
import com.netelsan.ipinterkompanel.model.IPCamera;
import com.netelsan.ipinterkompanel.model.ZilPanel;
import com.netelsan.ipinterkompanel.model.ZilPanelSite;
import com.netelsan.ipinterkompanel.serial.Utils;
import com.netelsan.ipinterkompanel.service.KeyPadService;
import com.netelsan.ipinterkompanel.tcp.TCPHelper;
import com.stericson.RootShell.execution.Command;
import com.stericson.RootTools.RootTools;

import java.io.BufferedReader;
import java.io.File;
import java.io.InputStreamReader;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Iterator;
import java.util.Timer;
import java.util.TimerTask;

public class AyarlarTeknikPersonelFragment extends BaseFragment implements KeyPadListener, ServiceConnection {

    LinearLayout teknikPersonelTarihSaatContainer;
    TextView teknikPersonelTarihSaatText;

    LinearLayout teknikPersonelSifirlaContainer;
    TextView teknikPersonelSifirlaText;

    LinearLayout yoneticiGuncelleContainer;
    TextView yoneticiGuncelleText;

    LinearLayout teknikPersonelRehberiGuncelleContainer;
    TextView teknikPersonelRehberiGuncelleText;

    LinearLayout teknikPersonelRebootContainer;
    TextView teknikPersonelRebootText;

    LinearLayout teknikPersonelIPInfoContainer;
    TextView teknikPersonelIPInfoText;
    ImageView teknikPersonelIPInfoImage;


    LinearLayout teknikPersonelBackupContainer;
    TextView teknikPersonelBackupText;

    LinearLayout teknikPersonelRecoveryContainer;
    TextView teknikPersonelRecoveryText;

    LinearLayout teknikPersonelRelayTimeContainer;
    TextView teknikPersonelRelayTimeText;

    //Relay Time fragment


    ScrollView ayarlarTeknikPersonelScrollView;

    int selectedMenuId = Constants.TEKNIK_PERS_MENU_TARIH_SAAT;

    public AyarlarTeknikPersonelFragment() {
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {

        View rootview = inflater.inflate(R.layout.fragment_ayarlar_teknik_personel, null);

        teknikPersonelTarihSaatContainer = rootview.findViewById(R.id.teknikPersonelTarihSaatContainer);
        teknikPersonelTarihSaatText = rootview.findViewById(R.id.teknikPersonelTarihSaatText);

        teknikPersonelSifirlaContainer = rootview.findViewById(R.id.teknikPersonelSifirlaContainer);
        teknikPersonelSifirlaText = rootview.findViewById(R.id.teknikPersonelSifirlaText);

        yoneticiGuncelleContainer = rootview.findViewById(R.id.yoneticiGuncelleContainer);
        yoneticiGuncelleText = rootview.findViewById(R.id.yoneticiGuncelleText);

        teknikPersonelIPInfoContainer = rootview.findViewById(R.id.teknikPersonelIPInfoContainer);
        teknikPersonelIPInfoText = rootview.findViewById(R.id.teknikPersonelIPInfoText);
        teknikPersonelIPInfoImage = rootview.findViewById(R.id.teknikPersonelIPInfoImage);

        teknikPersonelRehberiGuncelleContainer = rootview.findViewById(R.id.teknikPersonelRehberiGuncelleContainer);
        teknikPersonelRehberiGuncelleText = rootview.findViewById(R.id.teknikPersonelRehberiGuncelleText);

        teknikPersonelRelayTimeContainer = rootview.findViewById(R.id.teknikPersonelRelayTimeContainer);
        teknikPersonelRelayTimeText = rootview.findViewById(R.id.teknikPersonelRelayTimeText);

        teknikPersonelBackupContainer = rootview.findViewById(R.id.teknikPersonelBackupContainer);
        teknikPersonelBackupText = rootview.findViewById(R.id.teknikPersonelBackupText);

        teknikPersonelRecoveryContainer = rootview.findViewById(R.id.teknikPersonelRecoveryContainer);
        teknikPersonelRecoveryText = rootview.findViewById(R.id.teknikPersonelRecoveryText);

        teknikPersonelRebootContainer = rootview.findViewById(R.id.teknikPersonelRebootContainer);
        teknikPersonelRebootText = rootview.findViewById(R.id.teknikPersonelRebootText);

        ayarlarTeknikPersonelScrollView = rootview.findViewById(R.id.ayarlarTeknikPersonelScrollView);


        return rootview;
    }


    @Override
    public void onServiceConnected(ComponentName name, IBinder service) {
        keyPadService = ((KeyPadService.MyBinder) service).getService();
        keyPadService.addListener(AyarlarTeknikPersonelFragment.this, AyarlarTeknikPersonelFragment.class.getName());
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
            keyPadService.addListener(AyarlarTeknikPersonelFragment.this, AyarlarTeknikPersonelFragment.class.getName());
        }
    }

    @Override
    public void onPause() {
        super.onPause();

        if (zamanDialog != null) {
            zamanDialog.dismiss();
            zamanDialog = null;
        }

        if (sifirlaDialog != null) {
            sifirlaDialog.dismiss();
            sifirlaDialog = null;
        }

        if (updateAppDialog != null) {
            updateAppDialog.dismiss();
            updateAppDialog = null;
        }

        if (tarihDialog != null) {
            tarihDialog.dismiss();
            tarihDialog = null;
        }

        if (summaryDialog != null) {
            summaryDialog.dismiss();
            summaryDialog = null;
        }

        if (keyPadService != null) {
            keyPadService.removeListener(AyarlarTeknikPersonelFragment.class.getName());
        }
    }

    @Override
    public void onKeyPressed(String keyCode) {

        if (isWaitDialogActive) {
            return;
        }

        getActivity().runOnUiThread(new Runnable() {
            @Override
            public void run() {

//                Log.d(Constants.LOG_TAG, "KapiAcFragment onKeyPressed keyCode=" + keyCode);

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

    private void bottomNumbersClicked(String keycode) {
        if (isSetZamanActive) {

            if (selectedZamanPart == 1) {

                int keyCodeInt = Integer.parseInt(keycode);

                String currentSaat = zamanDialogSaat.getText().toString();
                int totalLength = currentSaat.length();
                int currentSaatInt = Integer.parseInt(currentSaat);

                if (totalLength == 2) {
                    zamanDialogSaat.setText(keycode);
                } else if (totalLength == 1) {
                    if (currentSaatInt > 2) {
                        zamanDialogSaat.setText(keycode);
                    } else if (currentSaatInt == 2) {
                        if (keyCodeInt <= 4) {
                            zamanDialogSaat.setText(currentSaat + "" + keycode);
                        }
                    } else {
                        zamanDialogSaat.setText(currentSaat + "" + keycode);
                    }
                }

            } else if (selectedZamanPart == 2) {

                int keyCodeInt = Integer.parseInt(keycode);

                String currentDakika = zamanDialogDakika.getText().toString();
                int totalLength = currentDakika.length();
                int currentDakikaInt = Integer.parseInt(currentDakika);

                if (totalLength == 2) {
                    zamanDialogDakika.setText(keycode);
                } else if (totalLength == 1) {

                    if (currentDakikaInt >= 0 && currentDakikaInt <= 5) {
                        zamanDialogDakika.setText(currentDakika + "" + keycode);
                    } else if (currentDakikaInt == 6) {
                        if (keyCodeInt == 0) {
                            zamanDialogDakika.setText(currentDakika + "" + keycode);
                        }
                    }

                }

            }

        } else if (isSetTarihActive) {

            int keyCodeInt = Integer.parseInt(keycode);

            if (selectedTarihPart == 1) {

                String currentGun = tarihDialogGun.getText().toString();
                int totalLength = currentGun.length();
                int currentGunInt = Integer.parseInt(currentGun);

                if (totalLength == 2) {
                    tarihDialogGun.setText(keycode);
                } else if (totalLength == 1) {
                    if (currentGunInt == 1 || currentGunInt == 2) {
                        tarihDialogGun.setText(currentGun + "" + keycode);
                    } else if (currentGunInt == 3) {
                        if (keyCodeInt <= 1) {
                            tarihDialogGun.setText(currentGun + "" + keycode);
                        }
                    } else {
                        tarihDialogGun.setText(keycode);
                    }
                }

            } else if (selectedTarihPart == 2) {

                String currentAy = tarihDialogAy.getText().toString();
                int totalLength = currentAy.length();
                int currentAyInt = Integer.parseInt(currentAy);

                if (totalLength == 2) {
                    tarihDialogAy.setText(keycode);
                } else if (totalLength == 1) {
                    if (currentAyInt == 1) {
                        if (keyCodeInt <= 2) {
                            tarihDialogAy.setText(currentAy + "" + keycode);
                        } else {
                            tarihDialogAy.setText(keycode);
                        }
                    } else {
                        tarihDialogAy.setText(keycode);
                    }
                }

            } else if (selectedTarihPart == 3) {

                String currentYil = tarihDialogYil.getText().toString();
                int totalLength = currentYil.length();
                int currentYilInt = Integer.parseInt(currentYil);

                if (totalLength == 4) {
                    if (keyCodeInt == 2) {
                        tarihDialogYil.setText(keycode);
                    }
                } else if (totalLength == 1) {
                    if (keyCodeInt == 0) {
                        tarihDialogYil.setText(currentYil + "" + keycode);
                    }
                } else if (totalLength == 2) {
                    if (keyCodeInt == 1 || keyCodeInt == 2) {
                        tarihDialogYil.setText(currentYil + "" + keycode);
                    }
                } else if (totalLength == 3) {
                    tarihDialogYil.setText(currentYil + "" + keycode);
                }

            }

        }
    }

    int selectedHour = -1;
    int selectedMinute = -1;
    int selectedDayofMonth = -1;
    int selectedMonth = -1;
    int selectedYear = -1;

    private void bottomMenuOkayClicked() {

        if (isCountDownDialogRunning) {
            return;
        }

        if (isSummaryDialogRunning) {
            setDeviceSettingsAndRestart();
            return;
        }

        if (isSetZamanActive) {
            setZamanDialogOkay();
            return;
        }

        if (isSifirlaActive) {
            sifirlaOkay();
            return;
        }

        if (isUpdateAppDialogRunning) {
            updateAppOkayClicked();
            return;
        }

        if (isSetTarihActive) {
            setTarihDialogOkay();
            return;
        }

        if (isRelayTimerunning) {
            relayTimeUpdate(relaySelectMenuid);
            return;
        }


        if (selectedMenuId == Constants.TEKNIK_PERS_MENU_TARIH_SAAT) {

            selectedHour = -1;
            selectedMinute = -1;
            selectedDayofMonth = -1;
            selectedMonth = -1;
            selectedYear = -1;

            showZamanDialog();

        } else if (selectedMenuId == Constants.TEKNIK_PERS_MENU_SIFIRLA) {
            showSifirlaDialog();
        } else if (selectedMenuId == Constants.TEKNIK_PERS_MENU_UPDATE_APP) {
            showUpdateDialog();
        } else if (selectedMenuId == Constants.TEKNIK_PERS_MENU_IP_INFO) {
            ipInfoClicked();
        } else if (selectedMenuId == Constants.TEKNIK_PERS_MENU_REHBER_GUNCELLE) {
            rehberiGuncelleOperations();
        } else if (selectedMenuId == Constants.TEKNIK_PERS_MENU_RELAY_TIME) {
            getrelaytimeFragment();
        } else if (selectedMenuId == Constants.TEKNIK_PERS_MENU_BACKUP) {
            backupDBClicked();
        } else if (selectedMenuId == Constants.TEKNIK_PERS_MENU_RECOVERY) {
            recoveryDBClicked();
        } else if (selectedMenuId == Constants.TEKNIK_PERS_MENU_REBOOT) {
            showRebootTimerDialog();
        }

    }


    private void relayTimeUpdate(int selectedId) {
        zeroPointFiveCheck.setVisibility(View.INVISIBLE);
        oneCheck.setVisibility(View.INVISIBLE);
        onePointFiveCheck.setVisibility(View.INVISIBLE);
        twoCheck.setVisibility(View.INVISIBLE);
        twoPointFiveCheck.setVisibility(View.INVISIBLE);
        threeCheck.setVisibility(View.INVISIBLE);
        threePointFiveCheck.setVisibility(View.INVISIBLE);
        fourCheck.setVisibility(View.INVISIBLE);
        fourPointFiveCheck.setVisibility(View.INVISIBLE);
        fiveCheck.setVisibility(View.INVISIBLE);
        fivePointFiveCheck.setVisibility(View.INVISIBLE);
        sixCheck.setVisibility(View.INVISIBLE);
        sixPointFiveCheck.setVisibility(View.INVISIBLE);
        sevenCheck.setVisibility(View.INVISIBLE);
        sevenPointFiveCheck.setVisibility(View.INVISIBLE);
        eightCheck.setVisibility(View.INVISIBLE);
        eightPointFiveCheck.setVisibility(View.INVISIBLE);
        nineCheck.setVisibility(View.INVISIBLE);
        ninePointFiveCheck.setVisibility(View.INVISIBLE);
        tenCheck.setVisibility(View.INVISIBLE);


        switch (selectedId) {
            case 0:
                zeroPointFiveCheck.setVisibility(View.VISIBLE);
                Helper.setRelayTime(getActivity().getApplicationContext(), 0);
                break;
            case 1:
                oneCheck.setVisibility(View.VISIBLE);
                Helper.setRelayTime(getActivity().getApplicationContext(), 1);
                break;
            case 2:
                onePointFiveCheck.setVisibility(View.VISIBLE);
                Helper.setRelayTime(getActivity().getApplicationContext(), 2);
                break;
            case 3:
                twoCheck.setVisibility(View.VISIBLE);
                Helper.setRelayTime(getActivity().getApplicationContext(), 3);
                break;
            case 4:
                twoPointFiveCheck.setVisibility(View.VISIBLE);
                Helper.setRelayTime(getActivity().getApplicationContext(), 4);
                break;
            case 5:
                threeCheck.setVisibility(View.VISIBLE);
                Helper.setRelayTime(getActivity().getApplicationContext(), 5);
                break;

            case 6:
                threePointFiveCheck.setVisibility(View.VISIBLE);
                Helper.setRelayTime(getActivity().getApplicationContext(), 6);
                break;
            case 7:
                fourCheck.setVisibility(View.VISIBLE);
                Helper.setRelayTime(getActivity().getApplicationContext(), 7);
                break;
            case 8:
                fourPointFiveCheck.setVisibility(View.VISIBLE);
                Helper.setRelayTime(getActivity().getApplicationContext(), 8);
                break;
            case 9:
                fiveCheck.setVisibility(View.VISIBLE);
                Helper.setRelayTime(getActivity().getApplicationContext(), 9);
                break;
            case 10:
                fivePointFiveCheck.setVisibility(View.VISIBLE);
                Helper.setRelayTime(getActivity().getApplicationContext(), 10);
                break;
            case 11:
                sixCheck.setVisibility(View.VISIBLE);
                Helper.setRelayTime(getActivity().getApplicationContext(), 11);
                break;
            case 12:
                sixPointFiveCheck.setVisibility(View.VISIBLE);
                Helper.setRelayTime(getActivity().getApplicationContext(), 12);
                break;

            case 13:
                sevenCheck.setVisibility(View.VISIBLE);
                Helper.setRelayTime(getActivity().getApplicationContext(), 13);
                break;

            case 14:
                sevenPointFiveCheck.setVisibility(View.VISIBLE);
                Helper.setRelayTime(getActivity().getApplicationContext(), 14);
                break;
            case 15:
                eightCheck.setVisibility(View.VISIBLE);
                Helper.setRelayTime(getActivity().getApplicationContext(), 15);
                break;
            case 16:
                eightPointFiveCheck.setVisibility(View.VISIBLE);
                Helper.setRelayTime(getActivity().getApplicationContext(), 16);
                break;
            case 17:
                nineCheck.setVisibility(View.VISIBLE);
                Helper.setRelayTime(getActivity().getApplicationContext(), 17);
                break;
            case 18:
                ninePointFiveCheck.setVisibility(View.VISIBLE);
                Helper.setRelayTime(getActivity().getApplicationContext(), 18);
                break;

            case 19:
                tenCheck.setVisibility(View.VISIBLE);
                Helper.setRelayTime(getActivity().getApplicationContext(), 19);
                break;


        }

    }


    boolean isRelayTimerunning = false;

    AlertDialog relayDialog;
    LinearLayout zeroPointFiveBack, oneBack, onePointFiveBack, twoBack, twoPointFiveBack, threeBack, threePointFiveBack, fourBack,
            fourPointFiveBack, fiveBack, fivePointFiveBack, sixBack, sixPointFiveBack, sevenBack, sevenPointFiveBack, eightBack, eightPointFiveBack,
            nineBack, ninePointFiveBack, tenBack;

    TextView zeroPointFive, one, onePointFive, two, twoPointFive, three, threePointFive, four, fourPointFive, five, fivePointFive, six, sixPointFive,
            seven, sevenPointFive, eight, eightPointFive, nine, ninePointFive, ten;

    ImageView zeroPointFiveCheck, oneCheck, onePointFiveCheck, twoCheck, twoPointFiveCheck, threeCheck, threePointFiveCheck, fourCheck, fourPointFiveCheck,
            fiveCheck, fivePointFiveCheck, sixCheck, sixPointFiveCheck, sevenCheck, sevenPointFiveCheck, eightCheck, eightPointFiveCheck, nineCheck, ninePointFiveCheck,
            tenCheck;

    ScrollView scrollViewRelayTime;

    int relaySelectMenuid = 0;


    private void getrelaytimeFragment() {


        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setTitle("");

        View customLayout = getLayoutInflater().inflate(R.layout.fragment_relay, null);
        builder.setView(customLayout);

        zeroPointFiveBack = customLayout.findViewById(R.id.zeroPointFiveBack);
        zeroPointFive = customLayout.findViewById(R.id.zeroPointFive);
        zeroPointFiveCheck = customLayout.findViewById(R.id.zeroPointFiveCheck);

        oneBack = customLayout.findViewById(R.id.oneBack);
        one = customLayout.findViewById(R.id.one);
        oneCheck = customLayout.findViewById(R.id.oneCheck);

        onePointFiveBack = customLayout.findViewById(R.id.onePointFiveBack);
        onePointFive = customLayout.findViewById(R.id.onePointFive);
        onePointFiveCheck = customLayout.findViewById(R.id.onePointFiveCheck);

        twoBack = customLayout.findViewById(R.id.twoBack);
        two = customLayout.findViewById(R.id.two);
        twoCheck = customLayout.findViewById(R.id.twoCheck);

        twoPointFiveBack = customLayout.findViewById(R.id.twoPointFiveBack);
        twoPointFive = customLayout.findViewById(R.id.twoPointFive);
        twoPointFiveCheck = customLayout.findViewById(R.id.twoPointFiveCheck);

        threeBack = customLayout.findViewById(R.id.threeBack);
        three = customLayout.findViewById(R.id.three);
        threeCheck = customLayout.findViewById(R.id.threeCheck);


        threePointFiveBack = customLayout.findViewById(R.id.threePointFiveBack);
        threePointFive = customLayout.findViewById(R.id.threePointFive);
        threePointFiveCheck = customLayout.findViewById(R.id.threePointFiveCheck);

        fourBack = customLayout.findViewById(R.id.fourBack);
        four = customLayout.findViewById(R.id.four);
        fourCheck = customLayout.findViewById(R.id.fourCheck);

        fourPointFiveBack = customLayout.findViewById(R.id.fourPointFiveBack);
        fourPointFive = customLayout.findViewById(R.id.fourPointFive);
        fourPointFiveCheck = customLayout.findViewById(R.id.fourPointFiveCheck);

        fiveBack = customLayout.findViewById(R.id.fiveBack);
        five = customLayout.findViewById(R.id.five);
        fiveCheck = customLayout.findViewById(R.id.fiveCheck);

        fivePointFiveBack = customLayout.findViewById(R.id.fivePointFiveBack);
        fivePointFive = customLayout.findViewById(R.id.fivePointFive);
        fivePointFiveCheck = customLayout.findViewById(R.id.fivePointFiveCheck);

        sixBack = customLayout.findViewById(R.id.sixBack);
        six = customLayout.findViewById(R.id.six);
        sixCheck = customLayout.findViewById(R.id.sixCheck);

        sixPointFiveBack = customLayout.findViewById(R.id.sixPointFiveBack);
        sixPointFive = customLayout.findViewById(R.id.sixPointFive);
        sixPointFiveCheck = customLayout.findViewById(R.id.sixPointFiveCheck);

        sevenBack = customLayout.findViewById(R.id.sevenBack);
        seven = customLayout.findViewById(R.id.seven);
        sevenCheck = customLayout.findViewById(R.id.sevenCheck);

        sevenPointFiveBack = customLayout.findViewById(R.id.sevenPointFiveBack);
        sevenPointFive = customLayout.findViewById(R.id.sevenPointFive);
        sevenPointFiveCheck = customLayout.findViewById(R.id.sevenPointFiveCheck);

        eightBack = customLayout.findViewById(R.id.eightBack);
        eight = customLayout.findViewById(R.id.eight);
        eightCheck = customLayout.findViewById(R.id.eightCheck);

        eightPointFiveBack = customLayout.findViewById(R.id.eightPointFiveBack);
        eightPointFive = customLayout.findViewById(R.id.eightPointFive);
        eightPointFiveCheck = customLayout.findViewById(R.id.eightPointFiveCheck);

        nineBack = customLayout.findViewById(R.id.nineBack);
        nine = customLayout.findViewById(R.id.nine);
        nineCheck = customLayout.findViewById(R.id.nineCheck);

        ninePointFiveBack = customLayout.findViewById(R.id.ninePointFiveBack);
        ninePointFive = customLayout.findViewById(R.id.ninePointFive);
        ninePointFiveCheck = customLayout.findViewById(R.id.ninePointFiveCheck);

        tenBack = customLayout.findViewById(R.id.tenBack);
        ten = customLayout.findViewById(R.id.ten);
        tenCheck = customLayout.findViewById(R.id.tenCheck);

        scrollViewRelayTime = customLayout.findViewById(R.id.relayTimeScrolview);

// hasan
        int a = Helper.getRelayTime(getActivity().getApplicationContext());
        switch (a) {
            case 0:
                zeroPointFiveCheck.setVisibility(View.VISIBLE);
                Helper.setRelayTime(getActivity().getApplicationContext(), 0);
                break;
            case 1:
                oneCheck.setVisibility(View.VISIBLE);
                Helper.setRelayTime(getActivity().getApplicationContext(), 1);
                break;
            case 2:
                onePointFiveCheck.setVisibility(View.VISIBLE);
                Helper.setRelayTime(getActivity().getApplicationContext(), 2);
                break;
            case 3:
                twoCheck.setVisibility(View.VISIBLE);
                Helper.setRelayTime(getActivity().getApplicationContext(), 3);
                break;
            case 4:
                twoPointFiveCheck.setVisibility(View.VISIBLE);
                Helper.setRelayTime(getActivity().getApplicationContext(), 4);
                break;
            case 5:
                threeCheck.setVisibility(View.VISIBLE);
                Helper.setRelayTime(getActivity().getApplicationContext(), 5);
                break;

            case 6:
                threePointFiveCheck.setVisibility(View.VISIBLE);
                Helper.setRelayTime(getActivity().getApplicationContext(), 6);
                break;
            case 7:
                fourCheck.setVisibility(View.VISIBLE);
                Helper.setRelayTime(getActivity().getApplicationContext(), 7);
                break;
            case 8:
                fourPointFiveCheck.setVisibility(View.VISIBLE);
                Helper.setRelayTime(getActivity().getApplicationContext(), 8);
                break;
            case 9:
                fiveCheck.setVisibility(View.VISIBLE);
                Helper.setRelayTime(getActivity().getApplicationContext(), 9);
                break;
            case 10:
                fivePointFiveCheck.setVisibility(View.VISIBLE);
                Helper.setRelayTime(getActivity().getApplicationContext(), 10);
                break;
            case 11:
                sixCheck.setVisibility(View.VISIBLE);
                Helper.setRelayTime(getActivity().getApplicationContext(), 11);
                break;
            case 12:
                sixPointFiveCheck.setVisibility(View.VISIBLE);
                Helper.setRelayTime(getActivity().getApplicationContext(), 12);
                break;

            case 13:
                sevenCheck.setVisibility(View.VISIBLE);
                Helper.setRelayTime(getActivity().getApplicationContext(), 13);
                break;

            case 14:
                sevenPointFiveCheck.setVisibility(View.VISIBLE);
                Helper.setRelayTime(getActivity().getApplicationContext(), 14);
                break;
            case 15:
                eightCheck.setVisibility(View.VISIBLE);
                Helper.setRelayTime(getActivity().getApplicationContext(), 15);
                break;
            case 16:
                eightPointFiveCheck.setVisibility(View.VISIBLE);
                Helper.setRelayTime(getActivity().getApplicationContext(), 16);
                break;
            case 17:
                nineCheck.setVisibility(View.VISIBLE);
                Helper.setRelayTime(getActivity().getApplicationContext(), 17);
                break;
            case 18:
                ninePointFiveCheck.setVisibility(View.VISIBLE);
                Helper.setRelayTime(getActivity().getApplicationContext(), 18);
                break;

            case 19:
                tenCheck.setVisibility(View.VISIBLE);
                Helper.setRelayTime(getActivity().getApplicationContext(), 19);
                break;


        }


        builder.setOnDismissListener(new DialogInterface.OnDismissListener() {
            @Override
            public void onDismiss(DialogInterface dialog) {

                relaySelectMenuid = 0;
                isRelayTimerunning = false;
            }
        });

        isRelayTimerunning = true;

        relayDialog = builder.create();
        relayDialog.show();

//        try {
//            MainActivity mainActivity = (MainActivity) getActivity();
//
//            Fragment fragment = null;
//            fragment = new RelayTimeFragment();
//
//            RelayTimeFragment relayTimeFragment = new RelayTimeFragment();
//            getActivity().getSupportFragmentManager().beginTransaction()
//                    .replace(R.id.relayTimeScrolview, relayTimeFragment, "RelayTime")
//                    .addToBackStack(null)
//                    .commit();
//        } catch (Exception e) {
//            Log.e("HK", "Fragment error: " + e);
//        }


    }

    private void updateAppNavigate(int direction) {

        int selectedIndex = 0;
        int counter = 0;

        ArrayList<APKListItem> arrayListTemp = new ArrayList<>();

        ArrayList<APKListItem> arrayList = apkListAdapter.getArrayList();
        Iterator<APKListItem> iterator = arrayList.iterator();
        while (iterator.hasNext()) {
            APKListItem apkListItem = iterator.next();
            if (apkListItem.isSelected()) {
                selectedIndex = counter;
            }

            apkListItem.setSelected(false);
            arrayListTemp.add(apkListItem);

            counter++;

        }

        if (direction == Constants.MENU_NAVIGATION_UP) {
            if (selectedIndex == 0) {
                selectedIndex = arrayList.size() - 1;
            } else {
                selectedIndex--;
            }
        } else if (direction == Constants.MENU_NAVIGATION_DOWN) {
            if (selectedIndex == arrayList.size() - 1) {
                selectedIndex = 0;
            } else {
                selectedIndex++;
            }
        }
        APKListItem apkListItem = arrayListTemp.get(selectedIndex);
        apkListItem.setSelected(true);
        selectedFileUpdateApp = apkListItem.getFile();
        arrayListTemp.set(selectedIndex, apkListItem);

        apkListAdapter.setArrayList(arrayListTemp);
        apkListAdapter.notifyDataSetChanged();

        updateAppDialogRecyclerView.scrollToPosition(selectedIndex);
    }

    AlertDialog updateAppDialog;
    boolean isUpdateAppDialogRunning;

    private void showUpdateDialog() {

        boolean isSDCardMounted = Helper.isSDCardMounted();
        if (!isSDCardMounted) {
            Helper.showTopMessageBanner((AppCompatActivity) getActivity(), getString(R.string.hafiza_karti_gerekiyor), false);
            return;
        }

        new ScanSDCardTaskForSuitableAPK().executeOnExecutor(Constants.DEFAULT_EXECUTOR);

    }

    boolean isWaitDialogActive = false;

    class ScanSDCardTaskForSuitableAPK extends AsyncTask<Void, Void, ArrayList<File>> {

        protected void onPreExecute() {
            isWaitDialogActive = true;
            Helper.showProgressDialog((AppCompatActivity) getActivity(), getString(R.string.lutfen_bekleyin), getString(R.string.islem_yapiliyor), false);
            super.onPreExecute();
        }

        protected ArrayList<File> doInBackground(Void... arg0) {
            ArrayList<File> arrayListFiles = scanListOfFiles();
            return arrayListFiles;
        }

        protected void onPostExecute(ArrayList<File> result) {
            super.onPostExecute(result);
            isWaitDialogActive = false;
            Helper.dismissProgressDialog();
            if (result != null && result.size() > 0) {
                showSelectAPKForUpdateDialog(result);
            } else {
                Helper.showTopMessageBanner((AppCompatActivity) getActivity(), getString(R.string.gosterilecek_dosya_bulunamadi), false);
            }
        }

    }

    RecyclerView updateAppDialogRecyclerView;
    ApkListAdapter apkListAdapter;
    File selectedFileUpdateApp = null;

    private void showSelectAPKForUpdateDialog(ArrayList<File> result) {

        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setTitle("");

        View customLayout = getLayoutInflater().inflate(R.layout.custom_update_app_dialog, null);
        builder.setView(customLayout);

        updateAppDialogRecyclerView = customLayout.findViewById(R.id.updateAppDialogRecyclerView);

        RecyclerView.LayoutManager layoutManager = new LinearLayoutManager(getActivity(), RecyclerView.VERTICAL, false);
        updateAppDialogRecyclerView.setLayoutManager(layoutManager);

        int counter = 0;
        ArrayList<APKListItem> arrayList = new ArrayList<>();
        Iterator<File> iterator = result.iterator();
        while (iterator.hasNext()) {
            File file = iterator.next();

            APKListItem apkListItem = new APKListItem();
            apkListItem.setFile(file);
            if (counter == 0) {
                apkListItem.setSelected(true);
                selectedFileUpdateApp = file;
            } else {
                apkListItem.setSelected(false);
            }

            arrayList.add(apkListItem);

            counter++;

        }

        apkListAdapter = new ApkListAdapter(getActivity(), arrayList);
        updateAppDialogRecyclerView.setAdapter(apkListAdapter);

        builder.setOnDismissListener(new DialogInterface.OnDismissListener() {
            @Override
            public void onDismiss(DialogInterface dialog) {
                updateAppDialog = null;
                isUpdateAppDialogRunning = false;
            }
        });
        updateAppDialog = builder.create();
        updateAppDialog.show();
        isUpdateAppDialogRunning = true;
    }

    private ArrayList<File> scanListOfFiles() {

        ArrayList<File> arrayListFiles = new ArrayList<>();

        File directory = new File("/storage/extsd/");
        File[] files = directory.listFiles();
        if (files == null) {
            return null;
        }
//        Log.d(Constants.LOG_TAG, "Size: " + files.length);

        for (int i = 0; i < files.length; i++) {
            File file = files[i];

            ArrayList<File> arrayListTemp = getFilesInDirectory(file);
            arrayListFiles.addAll(arrayListTemp);

        }

        return arrayListFiles;

    }

    private ArrayList<File> getFilesInDirectory(File rootDirectory) {

        ArrayList<File> arrayListFiles = new ArrayList<>();

        if (rootDirectory.isDirectory()) {
            File[] list = rootDirectory.listFiles();
            if (list == null) {
                return arrayListFiles;
            }
            for (File file : list) {
                if (file.isDirectory()) {
//                    Log.d(Constants.LOG_TAG, "getFilesInDirectory Dir: " + file.getAbsoluteFile());
                    ArrayList<File> arrayList = getFilesInDirectory(file);
                    arrayListFiles.addAll(arrayList);
                } else {
                    String fileName = file.getName();
                    if (isFileSuitable(fileName)) {
                        arrayListFiles.add(file);
                    }
                }
            }
        } else {
            String fileName = rootDirectory.getName();
            if (isFileSuitable(fileName)) {
                long fileSizeInKB = Helper.getFileSizeInKB(rootDirectory);
                if (fileSizeInKB > 0 && fileSizeInKB <= Constants.MAX_FILE_SIZE_IN_KB) {
                    arrayListFiles.add(rootDirectory);
                }
            }
        }

        return arrayListFiles;

    }

    private boolean isFileSuitable(String fileName) {
        if (fileName.endsWith(".apk")) {
            return true;
        }
        return false;
    }

    private void updateAppOkayClicked() {

        Helper.showProgressDialog((AppCompatActivity) getActivity(), getString(R.string.lutfen_bekleyin), getString(R.string.islem_yapiliyor), true);
        isWaitDialogActive = true;

        updateAppDialog.dismiss();
        updateAppDialog = null;
        isUpdateAppDialogRunning = false;

        if (selectedFileUpdateApp != null) {

            String absolutePath = selectedFileUpdateApp.getAbsolutePath();
            try {
                Command command = new Command(0, "adb install -r " + absolutePath);
                RootTools.getShell(true).add(command).getExitCode();
            } catch (Exception e) {
                e.printStackTrace();
            }

            start5MinutesTimerForPrecaution();

        }

    }

    private void start5MinutesTimerForPrecaution() {
        Timer timer = new Timer();
        timer.schedule(new TimerTask() {
            @Override
            public void run() {

                getActivity().runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        isWaitDialogActive = false;
                        Toast.makeText(getActivity(), getString(R.string.hata_daha_sonra_tekrar_deneyin), Toast.LENGTH_SHORT).show();
                        Helper.dismissProgressDialog();
                    }
                });

            }
        }, 1000 * 60 * 5);
    }

    private void rehberiGuncelleOperations() {

        boolean isZilForSite = Helper.isZilForSite(getActivity());
        if (!isZilForSite) {

            boolean isCenterUnitZilPanel = Helper.isCenterUnitZilPanel(getActivity());
            if (isCenterUnitZilPanel) {

                DatabaseHelper databaseHelper = new DatabaseHelper(getActivity());

                ZilPanel zilPanelSelf = databaseHelper.getZilPanelByIP(Utils.getIPAddress(true));

                ArrayList<ZilPanel> zilPanels = databaseHelper.getZilPanelleri(true);
                ArrayList<Daire> daires = databaseHelper.getDaireler();
                ArrayList<Guvenlik> guvenliks = databaseHelper.getGuvenlikler();
                ArrayList<IPCamera> ipCameras = databaseHelper.getIPCameras();
                ArrayList<ZilPanelSite> siteZilPanels = databaseHelper.getSiteZilPanelleri(true);

                ArrayList<Daire> dairesInSameBuilding = databaseHelper.getDairelerInSameApartment(zilPanelSelf.getBlok());
                Iterator<Daire> iterator = dairesInSameBuilding.iterator();
                while (iterator.hasNext()) {
                    Daire daireForInfo = iterator.next();
                    String destinationIP = daireForInfo.getIp();

                    ComPackageModel model = new ComPackageModel();
                    model.setOpe_type(Constants.OPERATION_REQUEST_DEVICE_INFOS_RESPONSE);
                    model.setZilPanel(zilPanelSelf);
                    model.setZilPanels(zilPanels);
                    model.setDaires(daires);
                    model.setGuvenliks(guvenliks);
                    model.setIpCameras(ipCameras);
                    model.setZilPanelSites(siteZilPanels);

                    TCPHelper.sendMessageToIP(getActivity(), destinationIP, model);

                }


                Helper.showTopMessageBanner((AppCompatActivity) getActivity(), getString(R.string.guncelleme_basladi_otomatik_yapilacaktir), true);
            }

        }
    }

    private void ipInfoClicked() {
        boolean isIPInfoEnabled = Helper.isIPInfoEnabled(getActivity());

        Helper.setIPInfoEnabled(getActivity(), !isIPInfoEnabled);

        if (!isIPInfoEnabled) {
            teknikPersonelIPInfoImage.setImageResource(R.drawable.icon_checkbox_checked_black);
        } else {
            teknikPersonelIPInfoImage.setImageResource(R.drawable.icon_checkbox_unchecked_black);
        }

    }

    private void sifirlaOkay() {
        SharedPreferences sharedPreferences = getActivity().getSharedPreferences(Constants.SHARED_PREFS, 0);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.clear();
        editor.commit();

        Helper.setEthernetTestFinished(getActivity(), true);
        Helper.setDeviceTestFinished(getActivity(), true);

        DatabaseHelper databaseHelper = new DatabaseHelper(getActivity());
        databaseHelper.deleteEverything();

        Helper.showWhiteScreen((AppCompatActivity) getActivity());
        resetDeviceIPAndReboot();
    }

    private void resetDeviceIPAndReboot() {

        try {

            Command command = new Command(0, "sqlite3 /data/data/com.android.providers.settings/databases/settings.db \"UPDATE global SET value = '0' WHERE name = 'eth_mode';\"",
                    "sqlite3 /data/data/com.android.providers.settings/databases/settings.db \"UPDATE global SET value = '" + "1.2.3.4" + "' WHERE name = 'eth_ip';\"",
                    "sqlite3 /data/data/com.android.providers.settings/databases/settings.db \"UPDATE global SET value = '255.255.255.255' WHERE name = 'eth_netmask';\"",
//                    "sqlite3 /data/data/com.android.providers.settings/databases/settings.db \"UPDATE global SET value = '192.168.1.207' WHERE name = 'eth_route';\"",
                    "sqlite3 /data/data/com.android.providers.settings/databases/settings.db \"UPDATE global SET value = '8.8.8.8' WHERE name = 'eth_dns';\"",
                    "reboot");
            RootTools.getShell(true).add(command).getExitCode();

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    boolean isCountDownDialogRunning = false;
    AlertDialog countDownTimerDialog;
    int timeRemaining = 10;
    Timer timerCountDown;

    private void showRebootTimerDialog() {

        timeRemaining = 10;

        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setTitle("");

        View customLayout = getLayoutInflater().inflate(R.layout.reboot_countdown_dialog, null);
        builder.setView(customLayout);

        TextView rebootDialogRemainingTime = customLayout.findViewById(R.id.rebootDialogRemainingTime);
        rebootDialogRemainingTime.setText("10 ");

        builder.setOnDismissListener(new DialogInterface.OnDismissListener() {
            @Override
            public void onDismiss(DialogInterface dialog) {
                countDownTimerDialog = null;
                isCountDownDialogRunning = false;
            }
        });
        countDownTimerDialog = builder.create();
        countDownTimerDialog.show();
        isCountDownDialogRunning = true;

        timerCountDown = new Timer();
        timerCountDown.schedule(new TimerTask() {
            @Override
            public void run() {

                if (timeRemaining == 0) {
                    timerCountDown.cancel();
                    timerCountDown.purge();
                    timerCountDown = null;

                    countDownTimerDialog.dismiss();

                    rebootDevice();

                    return;
                }
                timeRemaining = timeRemaining - 1;

                getActivity().runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        rebootDialogRemainingTime.setText(timeRemaining + " ");
                    }
                });

            }
        }, 0, 1000);

    }

    private void setTarihDialogOkay() {
        if (selectedTarihPart == 1) {
            selectedTarihPart = 2;
            tarihDialogGunArrow.setVisibility(View.INVISIBLE);
            tarihDialogAyArrow.setVisibility(View.VISIBLE);
            tarihDialogYilArrow.setVisibility(View.INVISIBLE);
        } else if (selectedTarihPart == 2) {
            selectedTarihPart = 3;
            tarihDialogGunArrow.setVisibility(View.INVISIBLE);
            tarihDialogAyArrow.setVisibility(View.INVISIBLE);
            tarihDialogYilArrow.setVisibility(View.VISIBLE);
        } else if (selectedTarihPart == 3) {

            String selectedGun = tarihDialogGun.getText().toString();
            String selectedAy = tarihDialogAy.getText().toString();
            String selectedYil = tarihDialogYil.getText().toString();

            selectedDayofMonth = Integer.parseInt(selectedGun);

//                    burada ay değerini android sistemine göre ata
            selectedMonth = Helper.getSystemMonthId(Integer.parseInt(selectedAy));
            selectedYear = Integer.parseInt(selectedYil);

            String selectedTarih = selectedGun + "." + selectedAy + "." + selectedYil;

            boolean isDateValid = Helper.isDateValid(selectedTarih);
            if (!isDateValid) {

                Helper.showTopMessageBanner((AppCompatActivity) getActivity(), getString(R.string.hatali_tarih), false);
                return;
            }

//                tarihResult.setText(selectedTarih);
            tarihDialog.dismiss();
            isSetTarihActive = false;

            showSummaryScreenIfNeeded();

        }

    }

    private void setZamanDialogOkay() {

        if (selectedZamanPart == 1) {
            selectedZamanPart = 2;
            zamanDialogSaatArrow.setVisibility(View.INVISIBLE);
            zamanDialogDakikaArrow.setVisibility(View.VISIBLE);
        } else if (selectedZamanPart == 2) {

            String selectedSaat = zamanDialogSaat.getText().toString();
            String selectedDakika = zamanDialogDakika.getText().toString();

            selectedHour = Integer.parseInt(selectedSaat);
            selectedMinute = Integer.parseInt(selectedDakika);

            String selectedZaman = selectedSaat + ":" + selectedDakika;
//                    saatResult.setText(selectedZaman);
            zamanDialog.dismiss();
            isSetZamanActive = false;

            selectedTarihPart = 1;
            showTarihDialog();
//                    burada saati kaydet ve tarih dialog u aç
        }

    }

    private void setDeviceSettingsAndRestart() {

        if (selectedHour == 24) {
            selectedHour = 0;
        }

        long newTimeMillis = setDeviceTime(selectedHour, selectedMinute, selectedDayofMonth, selectedMonth, selectedYear);

        boolean isForSite = Helper.isZilForSite(getActivity());
        if (!isForSite) {

            //burada kendi blogundaki cihazlara zaman değişti mesajı gönder
            DatabaseHelper databaseHelper = new DatabaseHelper(getActivity());
            ZilPanel zilPanel = databaseHelper.getZilPanelByIP(Utils.getIPAddress(true));
            ArrayList<Daire> arrayList = databaseHelper.getDairelerForBlok(zilPanel.getBlok());
            if (arrayList.size() != 0) {

                Iterator<Daire> iterator = arrayList.iterator();
                while (iterator.hasNext()) {
                    Daire daire = iterator.next();

                    String destinationIP = daire.getIp();

                    ComPackageModel model = new ComPackageModel();
                    model.setOpe_type(Constants.OPERATION_GET_DATE_TIME_RESPONSE);
                    model.setDataLong(newTimeMillis);

                    TCPHelper.sendMessageToIP(getActivity(), destinationIP, model);

                }

            }

        }

        //1 saniyelik bir bekleme yap. TCP mesajlarının gittiğinden emin olalım
        Timer timer = new Timer();
        timer.schedule(new TimerTask() {
            @Override
            public void run() {
                rebootDevice();
            }
        }, 1000);

    }

    private void rebootDevice() {
        try {

            Helper.showWhiteScreen((AppCompatActivity) getActivity());
            Command command = new Command(0, "reboot");
            RootTools.getShell(true).add(command).getExitCode();

        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    private long setDeviceTime(int hour, int minute, int dayofMonth, int month, int year) {

        Calendar calendar = Calendar.getInstance();
        calendar.set(Calendar.YEAR, year);
        calendar.set(Calendar.MONTH, month);
        calendar.set(Calendar.DAY_OF_MONTH, dayofMonth);
        calendar.set(Calendar.MINUTE, minute);
        calendar.set(Calendar.HOUR_OF_DAY, hour);
        calendar.set(Calendar.SECOND, 0);

        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyyMMdd.HHmmss");
        String formattedDate = dateFormat.format(calendar.getTime());
//        Log.d(Constants.LOG_TAG, "formattedDate=" + formattedDate);

        try {
//            Process process = Runtime.getRuntime().exec("su 0 toolbox date -s 20191015.084000");//YYYYMMDD.HHmmss
            Process process = Runtime.getRuntime().exec("su 0 toolbox date -s " + formattedDate);//YYYYMMDD.HHmmss
            BufferedReader in = new BufferedReader(new InputStreamReader(process.getInputStream()));

//            refreshDateAndTime(calendar);

        } catch (Exception e) {
            e.printStackTrace();
        }

        return calendar.getTimeInMillis();
    }

    private void showSummaryScreenIfNeeded() {

        if (selectedHour != -1 && selectedMinute != -1 && selectedDayofMonth != -1 && selectedMonth != -1 && selectedYear != -1) {
            showSummaryScreen();
        }

    }

    AlertDialog summaryDialog;
    boolean isSummaryDialogRunning = false;

    private void showSummaryScreen() {

        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setTitle("");

        View customLayout = getLayoutInflater().inflate(R.layout.custom_summary_dialog, null);
        builder.setView(customLayout);

        String timeFormatted = Helper.getFormattedTime(selectedHour, selectedMinute);
        TextView summarySaat = customLayout.findViewById(R.id.summarySaat);
        summarySaat.setText(timeFormatted);

        String dateFormatted = Helper.getFormattedDate(selectedDayofMonth, selectedMonth, selectedYear);
        TextView summaryTarih = customLayout.findViewById(R.id.summaryTarih);
        summaryTarih.setText(dateFormatted);

        TextView summaryBlokNoText = customLayout.findViewById(R.id.summaryBlokNoText);
        TextView summaryBlokNo = customLayout.findViewById(R.id.summaryBlokNo);
        TextView summaryKapiNoText = customLayout.findViewById(R.id.summaryKapiNoText);
        TextView summaryKapiNo = customLayout.findViewById(R.id.summaryKapiNo);

        summaryBlokNoText.setVisibility(View.GONE);
        summaryBlokNo.setVisibility(View.GONE);
        summaryKapiNoText.setVisibility(View.GONE);
        summaryKapiNo.setVisibility(View.GONE);

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

        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setTitle("");

        View customLayout = getLayoutInflater().inflate(R.layout.custom_tarih_dialog, null);
        builder.setView(customLayout);

        tarihDialogGun = customLayout.findViewById(R.id.tarihDialogGun);
        tarihDialogAy = customLayout.findViewById(R.id.tarihDialogAy);
        tarihDialogYil = customLayout.findViewById(R.id.tarihDialogYil);

        String currentdayOfMonth = Helper.getCurrentDayOfMonth();
        tarihDialogGun.setText(currentdayOfMonth);
        String currentMonth = Helper.getCurrentMonth();
        tarihDialogAy.setText(currentMonth);
        String currentYear = Helper.getCurrentYear();
        tarihDialogYil.setText(currentYear);

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

        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setTitle("");

        View customLayout = getLayoutInflater().inflate(R.layout.custom_zaman_dialog, null);
        builder.setView(customLayout);

        zamanDialogSaat = customLayout.findViewById(R.id.zamanDialogSaat);
        zamanDialogDakika = customLayout.findViewById(R.id.zamanDialogDakika);

        String currentHour = Helper.getCurrentHour();
        zamanDialogSaat.setText(currentHour);
        String currentMinute = Helper.getCurrentMinute();
        zamanDialogDakika.setText(currentMinute);

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

    boolean isSifirlaActive = false;
    AlertDialog sifirlaDialog;

    private void showSifirlaDialog() {

        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setTitle("");

        View customLayout = getLayoutInflater().inflate(R.layout.custom_sifirla_dialog, null);
        builder.setView(customLayout);

        builder.setOnDismissListener(new DialogInterface.OnDismissListener() {
            @Override
            public void onDismiss(DialogInterface dialog) {
                isSifirlaActive = false;
            }
        });
        sifirlaDialog = builder.create();
        sifirlaDialog.show();
        isSifirlaActive = true;

    }

    private void menuNavigationClicked(int direction) {

        if (isCountDownDialogRunning) {
            return;
        }

        if (isSetZamanActive) {
            navigateZamanDialog();
            return;
        }
        if (isSifirlaActive) {
            return;
        }

        if (isUpdateAppDialogRunning) {
            updateAppNavigate(direction);
            return;
        }

        if (isSetTarihActive) {
            navigateTarihDialog(direction);
            return;
        }
        if (isRelayTimerunning) {

            setRelayTime(direction);
            return;
        }

        boolean isDirectionUp = true;


        if (direction == Constants.MENU_NAVIGATION_UP) {
            if (selectedMenuId == Constants.TEKNIK_PERS_MENU_TARIH_SAAT) {
                selectedMenuId = Constants.TEKNIK_PERS_MENU_REBOOT;
            } else {
                selectedMenuId = selectedMenuId - 1;
            }
        } else if (direction == Constants.MENU_NAVIGATION_DOWN) {
            if (selectedMenuId == Constants.TEKNIK_PERS_MENU_REBOOT) {
                selectedMenuId = Constants.TEKNIK_PERS_MENU_TARIH_SAAT;
            } else {
                selectedMenuId = selectedMenuId + 1;
            }
        }

        selectMenu(selectedMenuId);

    }

    private void setRelayTime(int direction) {

        if (direction == Constants.MENU_NAVIGATION_UP) {

            relaySelectMenuid--;

//            if (relaySelectMenuid == 2) {
//                scrollViewRelayTime.fullScroll(ScrollView.FOCUS_UP);
//            }

            if (relaySelectMenuid == 5) {
                scrollViewRelayTime.fullScroll(ScrollView.FOCUS_UP);
            }

            if (relaySelectMenuid == 11) {
                scrollViewRelayTime.scrollTo(0, 225);
            }

            if (relaySelectMenuid == 13) {
                scrollViewRelayTime.scrollTo(0, 490);
            }

            if (relaySelectMenuid == -1) {
                relaySelectMenuid = 19;
                scrollViewRelayTime.fullScroll(ScrollView.FOCUS_DOWN);
            }


        }
        if (direction == Constants.MENU_NAVIGATION_DOWN) {

            relaySelectMenuid++;
            if (relaySelectMenuid == 6) {
                scrollViewRelayTime.scrollTo(0, 225);

            }

            if (relaySelectMenuid == 12) {
                scrollViewRelayTime.scrollTo(0, 490);
            }
            if (relaySelectMenuid == 18) {
                scrollViewRelayTime.fullScroll(View.FOCUS_DOWN);
            }

            if (relaySelectMenuid == 20) {
                scrollViewRelayTime.fullScroll(ScrollView.FOCUS_UP);
                relaySelectMenuid = 0;
            }

        }
        Log.d("HK", "HK direction: " + relaySelectMenuid);

        zeroPointFiveBack.setBackgroundColor(Color.parseColor("#ffffff"));
        zeroPointFive.setTextColor(Color.parseColor("#000000"));
        oneBack.setBackgroundColor(Color.parseColor("#ffffff"));
        one.setTextColor(Color.parseColor("#000000"));
        onePointFiveBack.setBackgroundColor(Color.parseColor("#ffffff"));
        onePointFive.setTextColor(Color.parseColor("#000000"));
        twoBack.setBackgroundColor(Color.parseColor("#ffffff"));
        two.setTextColor(Color.parseColor("#000000"));
        twoPointFiveBack.setBackgroundColor(Color.parseColor("#ffffff"));
        twoPointFive.setTextColor(Color.parseColor("#000000"));
        threeBack.setBackgroundColor(Color.parseColor("#ffffff"));
        three.setTextColor(Color.parseColor("#000000"));
        threePointFiveBack.setBackgroundColor(Color.parseColor("#ffffff"));
        threePointFive.setTextColor(Color.parseColor("#000000"));
        ////////////////////////////////////////////////////////////////////////////
        fourBack.setBackgroundColor(Color.parseColor("#ffffff"));
        four.setTextColor(Color.parseColor("#000000"));
        fourPointFiveBack.setBackgroundColor(Color.parseColor("#ffffff"));
        fourPointFive.setTextColor(Color.parseColor("#000000"));
        fiveBack.setBackgroundColor(Color.parseColor("#ffffff"));
        five.setTextColor(Color.parseColor("#000000"));
        fivePointFiveBack.setBackgroundColor(Color.parseColor("#ffffff"));
        fivePointFive.setTextColor(Color.parseColor("#000000"));
        sixBack.setBackgroundColor(Color.parseColor("#ffffff"));
        six.setTextColor(Color.parseColor("#000000"));
        sixPointFiveBack.setBackgroundColor(Color.parseColor("#ffffff"));
        sixPointFive.setTextColor(Color.parseColor("#000000"));
        sevenBack.setBackgroundColor(Color.parseColor("#ffffff"));
        seven.setTextColor(Color.parseColor("#000000"));
        //////////////////////////////////////////////////////
        sevenPointFiveBack.setBackgroundColor(Color.parseColor("#ffffff"));
        sevenPointFive.setTextColor(Color.parseColor("#000000"));
        eightBack.setBackgroundColor(Color.parseColor("#ffffff"));
        eight.setTextColor(Color.parseColor("#000000"));
        eightPointFiveBack.setBackgroundColor(Color.parseColor("#ffffff"));
        eightPointFive.setTextColor(Color.parseColor("#000000"));
        nineBack.setBackgroundColor(Color.parseColor("#ffffff"));
        nine.setTextColor(Color.parseColor("#000000"));

        ninePointFiveBack.setBackgroundColor(Color.parseColor("#ffffff"));
        ninePointFive.setTextColor(Color.parseColor("#000000"));
        tenBack.setBackgroundColor(Color.parseColor("#ffffff"));
        ten.setTextColor(Color.parseColor("#000000"));
        switch (relaySelectMenuid) {
            case 0:
                zeroPointFiveBack.setBackgroundColor(Color.parseColor("#000000"));
                zeroPointFive.setTextColor(Color.parseColor("#ffffff"));
                break;
            case 1:
                oneBack.setBackgroundColor(Color.parseColor("#000000"));
                one.setTextColor(Color.parseColor("#ffffff"));
                break;
            case 2:
                onePointFiveBack.setBackgroundColor(Color.parseColor("#000000"));
                onePointFive.setTextColor(Color.parseColor("#ffffff"));
                break;
            case 3:
                twoBack.setBackgroundColor(Color.parseColor("#000000"));
                two.setTextColor(Color.parseColor("#ffffff"));
                break;

            case 4:
                twoPointFiveBack.setBackgroundColor(Color.parseColor("#000000"));
                twoPointFive.setTextColor(Color.parseColor("#ffffff"));
                break;

            case 5:
                threeBack.setBackgroundColor(Color.parseColor("#000000"));
                three.setTextColor(Color.parseColor("#ffffff"));
                break;

            case 6:
                threePointFiveBack.setBackgroundColor(Color.parseColor("#000000"));
                threePointFive.setTextColor(Color.parseColor("#ffffff"));
                break;
            case 7:
                fourBack.setBackgroundColor(Color.parseColor("#000000"));
                four.setTextColor(Color.parseColor("#ffffff"));
                break;
            case 8:
                fourPointFiveBack.setBackgroundColor(Color.parseColor("#000000"));
                fourPointFive.setTextColor(Color.parseColor("#ffffff"));
                break;
            case 9:
                fiveBack.setBackgroundColor(Color.parseColor("#000000"));
                five.setTextColor(Color.parseColor("#ffffff"));
                break;
            case 10:
                fivePointFiveBack.setBackgroundColor(Color.parseColor("#000000"));
                fivePointFive.setTextColor(Color.parseColor("#ffffff"));
                break;

            case 11:
                sixBack.setBackgroundColor(Color.parseColor("#000000"));
                six.setTextColor(Color.parseColor("#ffffff"));
                break;

            case 12:
                sixPointFiveBack.setBackgroundColor(Color.parseColor("#000000"));
                sixPointFive.setTextColor(Color.parseColor("#ffffff"));
                break;

            case 13:
                sevenBack.setBackgroundColor(Color.parseColor("#000000"));
                seven.setTextColor(Color.parseColor("#ffffff"));
                break;

            case 14:
                sevenPointFiveBack.setBackgroundColor(Color.parseColor("#000000"));
                sevenPointFive.setTextColor(Color.parseColor("#ffffff"));
                break;
            case 15:
                eightBack.setBackgroundColor(Color.parseColor("#000000"));
                eight.setTextColor(Color.parseColor("#ffffff"));
                break;
            case 16:
                eightPointFiveBack.setBackgroundColor(Color.parseColor("#000000"));
                eightPointFive.setTextColor(Color.parseColor("#ffffff"));
                break;
            case 17:
                nineBack.setBackgroundColor(Color.parseColor("#000000"));
                nine.setTextColor(Color.parseColor("#ffffff"));
                break;

            case 18:
                ninePointFiveBack.setBackgroundColor(Color.parseColor("#000000"));
                ninePointFive.setTextColor(Color.parseColor("#ffffff"));
                break;

            case 19:
                tenBack.setBackgroundColor(Color.parseColor("#000000"));
                ten.setTextColor(Color.parseColor("#ffffff"));
                break;


        }
    }

    private void navigateTarihDialog(int direction) {

        boolean isUp = false;
        if (direction == Constants.MENU_NAVIGATION_UP) {
            isUp = true;
        }

        if (selectedTarihPart == 1) {
            if (isUp) {
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
        } else if (selectedTarihPart == 2) {
            if (isUp) {
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
        } else if (selectedTarihPart == 3) {
            if (isUp) {
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

    }

    private void navigateZamanDialog() {
        if (selectedZamanPart == 1) {
            selectedZamanPart = 2;
            zamanDialogSaatArrow.setVisibility(View.INVISIBLE);
            zamanDialogDakikaArrow.setVisibility(View.VISIBLE);
        } else if (selectedZamanPart == 2) {
            selectedZamanPart = 1;
            zamanDialogSaatArrow.setVisibility(View.VISIBLE);
            zamanDialogDakikaArrow.setVisibility(View.INVISIBLE);
        }
    }

    private void selectMenu(int menuId) {

        unselectAllMenus();

        if (menuId == Constants.TEKNIK_PERS_MENU_TARIH_SAAT) {

            ayarlarTeknikPersonelScrollView.fullScroll(ScrollView.FOCUS_UP);
            teknikPersonelTarihSaatContainer.setBackgroundResource(R.drawable.menu_item_selected);
            teknikPersonelTarihSaatText.setTextColor(getResources().getColor(R.color.black));
        } else if (menuId == Constants.TEKNIK_PERS_MENU_SIFIRLA) {
            teknikPersonelSifirlaContainer.setBackgroundResource(R.drawable.menu_item_selected);
            teknikPersonelSifirlaText.setTextColor(getResources().getColor(R.color.black));
        } else if (menuId == Constants.TEKNIK_PERS_MENU_UPDATE_APP) {
            ayarlarTeknikPersonelScrollView.fullScroll(ScrollView.FOCUS_UP);
            yoneticiGuncelleContainer.setBackgroundResource(R.drawable.menu_item_selected);
            yoneticiGuncelleText.setTextColor(getResources().getColor(R.color.black));
        } else if (menuId == Constants.TEKNIK_PERS_MENU_IP_INFO) {
            teknikPersonelIPInfoContainer.setBackgroundResource(R.drawable.menu_item_selected);
            teknikPersonelIPInfoText.setTextColor(getResources().getColor(R.color.black));

            if (Helper.isIPInfoEnabled(getActivity())) {
                teknikPersonelIPInfoImage.setImageResource(R.drawable.icon_checkbox_checked_black);
            } else {
                teknikPersonelIPInfoImage.setImageResource(R.drawable.icon_checkbox_unchecked_black);
            }
        } else if (menuId == Constants.TEKNIK_PERS_MENU_REHBER_GUNCELLE) {
            teknikPersonelRehberiGuncelleContainer.setBackgroundResource(R.drawable.menu_item_selected);
            teknikPersonelRehberiGuncelleText.setTextColor(getResources().getColor(R.color.black));
        } else if (menuId == Constants.TEKNIK_PERS_MENU_RELAY_TIME) {
            teknikPersonelRelayTimeContainer.setBackgroundResource(R.drawable.menu_item_selected);
            teknikPersonelRelayTimeText.setTextColor(getResources().getColor(R.color.black));
        } else if (menuId == Constants.TEKNIK_PERS_MENU_BACKUP) {
            ayarlarTeknikPersonelScrollView.fullScroll(ScrollView.FOCUS_DOWN);
            teknikPersonelBackupContainer.setBackgroundResource(R.drawable.menu_item_selected);
            teknikPersonelBackupText.setTextColor(getResources().getColor(R.color.black));
        } else if (menuId == Constants.TEKNIK_PERS_MENU_RECOVERY) {
            teknikPersonelRecoveryContainer.setBackgroundResource(R.drawable.menu_item_selected);
            teknikPersonelRecoveryText.setTextColor(getResources().getColor(R.color.black));
        } else if (menuId == Constants.TEKNIK_PERS_MENU_REBOOT) {
            ayarlarTeknikPersonelScrollView.fullScroll(ScrollView.FOCUS_DOWN);
            teknikPersonelRebootContainer.setBackgroundResource(R.drawable.menu_item_selected);
            teknikPersonelRebootText.setTextColor(getResources().getColor(R.color.black));
        }

    }

    private void unselectAllMenus() {

        teknikPersonelTarihSaatContainer.setBackgroundResource(R.drawable.menu_item_unselected);
        teknikPersonelTarihSaatText.setTextColor(getResources().getColor(R.color.white));

        teknikPersonelSifirlaContainer.setBackgroundResource(R.drawable.menu_item_unselected);
        teknikPersonelSifirlaText.setTextColor(getResources().getColor(R.color.white));

        yoneticiGuncelleContainer.setBackgroundResource(R.drawable.menu_item_unselected);
        yoneticiGuncelleText.setTextColor(getResources().getColor(R.color.white));

        teknikPersonelIPInfoContainer.setBackgroundResource(R.drawable.menu_item_unselected);
        teknikPersonelIPInfoText.setTextColor(getResources().getColor(R.color.white));
        if (Helper.isIPInfoEnabled(getActivity())) {
            teknikPersonelIPInfoImage.setImageResource(R.drawable.icon_checkbox_checked);
        } else {
            teknikPersonelIPInfoImage.setImageResource(R.drawable.icon_checkbox_unchecked);
        }

        teknikPersonelRebootContainer.setBackgroundResource(R.drawable.menu_item_unselected);
        teknikPersonelRebootText.setTextColor(getResources().getColor(R.color.white));

        teknikPersonelRelayTimeContainer.setBackgroundResource(R.drawable.menu_item_unselected);
        teknikPersonelRelayTimeText.setTextColor(getResources().getColor(R.color.white));


        teknikPersonelRehberiGuncelleContainer.setBackgroundResource(R.drawable.menu_item_unselected);
        teknikPersonelRehberiGuncelleText.setTextColor(getResources().getColor(R.color.white));

        teknikPersonelBackupContainer.setBackgroundResource(R.drawable.menu_item_unselected);
        teknikPersonelBackupText.setTextColor(getResources().getColor(R.color.white));

        teknikPersonelRecoveryContainer.setBackgroundResource(R.drawable.menu_item_unselected);
        teknikPersonelRecoveryText.setTextColor(getResources().getColor(R.color.white));

    }

    private void backupDBClicked() {
        boolean isSDCardMounted = Helper.isSDCardMounted();
        if (!isSDCardMounted) {
            Helper.showTopMessageBanner((AppCompatActivity) getActivity(), getString(R.string.hafiza_karti_gerekiyor), false);
            return;
        }
        backupDBToSDCard();
        Helper.showTopMessageBanner((AppCompatActivity) getActivity(), getString(R.string.isleminiz_basari_ile_tamamlandi), true);
    }

    private void recoveryDBClicked() {
        boolean isSDCardMounted = Helper.isSDCardMounted();
        if (!isSDCardMounted) {
            Helper.showTopMessageBanner((AppCompatActivity) getActivity(), getString(R.string.hafiza_karti_gerekiyor), false);
            return;
        }
        recoveryDBToSDCard();
        Helper.showTopMessageBanner((AppCompatActivity) getActivity(), getString(R.string.isleminiz_basari_ile_tamamlandi), true);
    }


    public static void backupDBToSDCard() {
        try {

//            Log.d(Constants.LOG_TAG, "backupDBToSDCard newFolderName=" + newFolderName);
            File file = new File("/data/data/com.netelsan.ipinterkompanel/databases/db_zil");
            Log.d(Constants.LOG_TAG, "backupDBToSDCard file=" + file.getAbsolutePath());
            boolean isFileExist = file.exists();
            Log.d(Constants.LOG_TAG, "backupDBToSDCard isFileExist=" + isFileExist);
            if (!isFileExist) {
                return;
            }

            Command commandForDelete = new Command(0, "rm -r /storage/extsd/db_backup/");

            int exitCodeForDelete = RootTools.getShell(true).add(commandForDelete).getExitCode();
            Log.d(Constants.LOG_TAG, "backupDBToSDCard commandForDelete=" + exitCodeForDelete);

            Command commandForCreate = new Command(0, "mkdir /storage/extsd/db_backup/");
            int exitCodeForCreate = RootTools.getShell(true).add(commandForCreate).getExitCode();
            Log.d(Constants.LOG_TAG, "backupDBToSDCard exitCodeForCreate=" + exitCodeForCreate);

            Command commandForCreate2 = new Command(0, "mkdir /storage/extsd/db_backup/",
                    "adb push /data/data/com.netelsan.ipinterkompanel/databases/db_zil /storage/extsd/db_backup/");
            int exitCodeForCreate2 = RootTools.getShell(true).add(commandForCreate2).getExitCode();
            Log.d(Constants.LOG_TAG, "backupDBToSDCard exitCodeForCreate2=" + exitCodeForCreate2);

        } catch (Exception e) {
        }

    }


    public static void recoveryDBToSDCard() {
        try {

//            Log.d(Constants.LOG_TAG, "backupDBToSDCard newFolderName=" + newFolderName);
            File file = new File("/data/data/com.netelsan.ipinterkompanel/databases/db_zil");
//            Log.d(Constants.LOG_TAG, "backupDBToSDCard file=" + file.getAbsolutePath());
            boolean isFileExist = file.exists();
//            Log.d(Constants.LOG_TAG, "backupDBToSDCard isFileExist=" + isFileExist);
            if (!isFileExist) {
                return;
            }

            Command commandForDelete = new Command(0, "rm -r /data/data/com.netelsan.ipinterkompanel/databases/db_zil");
            int exitCodeForDelete = RootTools.getShell(true).add(commandForDelete).getExitCode();
            Log.d(Constants.LOG_TAG, "recoveryDBToSDCard exitCode: " + exitCodeForDelete);

//            Command commandForCreate = new Command(0, "mkdir /storage/extsd/db_backup/");
//            int exitCodeForCreate = RootTools.getShell(true).add(commandForCreate).getExitCode();
//            Log.d(Constants.LOG_TAG, "recoveryDBToSDCard exitCodeForCreate=" + exitCodeForCreate);

            Command commandForCreate2 = new Command(0, "adb push /storage/extsd/db_backup/db_zil /data/data/com.netelsan.ipinterkompanel/databases/");
            int exitCodeForCreate2 = RootTools.getShell(true).add(commandForCreate2).getExitCode();
            Log.d(Constants.LOG_TAG, "recoveryDBToSDCard adb push /storage/extsd/db_backup/db_zil   oluşmadı2 " + exitCodeForCreate2);

        } catch (Exception e) {
            Log.e("HK","recoveryDBToSDCard" + e);
        }

    }


    private void bottomMenuBackClicked() {

        if (isCountDownDialogRunning) {

            countDownTimerDialog.dismiss();
            countDownTimerDialog = null;

            timerCountDown.cancel();
            timerCountDown.purge();
            timerCountDown = null;

            return;
        }


        if (isSummaryDialogRunning) {
            if (summaryDialog != null) {
                summaryDialog.dismiss();
            }
            return;
        }

        if (isSetZamanActive) {
            zamanDialog.dismiss();
            zamanDialog = null;
            return;
        }

        if (isSifirlaActive) {
            sifirlaDialog.dismiss();
            sifirlaDialog = null;
            return;
        }

        if (isUpdateAppDialogRunning) {
            updateAppDialog.dismiss();
            updateAppDialog = null;
            return;
        }

        if (isSetTarihActive) {
            tarihDialog.dismiss();
            tarihDialog = null;
            return;
        }

        if (isRelayTimerunning) {
            relayDialog.dismiss();
            relayDialog = null;
            return;
        }

        MainActivity mainActivity = (MainActivity) getActivity();
        mainActivity.displayView(Constants.SCREEN_AYARLAR, null);
    }

}

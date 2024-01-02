package com.netelsan.ipinterkompanel;

import android.media.AudioManager;
import android.os.AsyncTask;

import java.util.concurrent.Executor;

public class Constants {

    public static Executor DEFAULT_EXECUTOR = AsyncTask.SERIAL_EXECUTOR;

    public static final String ENCRYPTION_PASSWORD = "asvp26px";

    public static final int MAX_DURATION_TIME = 60;//in seconds
    public static final int MAX_TIMEOUT_DURATION_TIME = 30;//in seconds

    public static final String LOG_TAG = "Kapi Panel";

    public static final String IP_FOR_TEST = "172.253.253.1";

    public static final String IP_FOR_FACE_DETECTOR = "172.249.1.1";
    public static final int IP_FOR_FACE_DETECTOR_PORT_DETECTION = 8888;
    public static final int IP_FOR_FACE_DETECTOR_PORT_CONNECTION_CHECK = 8899;
    public static final int IP_FOR_FACE_DETECTOR_PORT_IMAGE_RECORD = 8877;

    public static final int MAX_FILE_SIZE_IN_KB = 3072;

    public static String SHARED_PREFS = "panel_shared_pref";
    public static String KEY_INITIALIZE_FINISHED = "KEY_INITIALIZE_FINISHED";
    public static String KEY_HANDSHAKE_FINISHED = "KEY_HANDSHAKE_FINISHED";
    public static String KEY_WRONG_ATTEMPT = "KEY_WRONG_ATTEMPT";
    public static String KEY_WRONG_ATTEMPT_TIME = "KEY_WRONG_ATTEMPT_TIME";
    public static String KEY_LANGUAGE = "KEY_LANGUAGE";
    public static String KEY_CALL_VIDEO_RECORD = "KEY_CALL_VIDEO_RECORD";
    public static String KEY_ONBOARDING_NAME_1 = "KEY_ONBOARDING_NAME_1";
    public static String KEY_ONBOARDING_NAME_2 = "KEY_ONBOARDING_NAME_2";
    public static String KEY_TUS_SESI = "KEY_TUS_SESI";
    public static String KEY_VOLUME = "KEY_VOLUME";
    public static String KEY_DOOR = "KEY_DOOR";
    public static String KEY_IP_INFO = "KEY_IP_INFO";
    public static String KEY_ZIL_FOR_SITE = "KEY_ZIL_FOR_SITE";
    public static String KEY_CENTER_UNIT = "KEY_CENTER_UNIT";
    public static String KEY_IS_REBOOTED_FOR_ROOT = "KEY_IS_REBOOTED_FOR_ROOT";
    public static String KEY_TEKNIK_PERS_SIFRE = "KEY_TEKNIK_PERS_SIFRE";
    public static String KEY_DEVICE_DISPLACEMENT = "KEY_DEVICE_DISPLACEMENT";
    public static String KEY_DEVICE_TEST_FINISHED = "KEY_DEVICE_TEST_FINISHED";
    public static String KEY_ETHERNET_TEST_FINISHED = "KEY_ETHERNET_TEST_FINISHED";
    public static String KEY_ETHERNET_TEST_TRY_COUNT = "KEY_ETHERNET_TEST_TRY_COUNT";
    public static String KEY_DEVICE_TEST = "KEY_DEVICE_TEST";
    public static String KEY_RELAY_TIME = "KEY_RELAY_TIME";
    public static String KEY_SELECTED_GENEL_ARAMA_ID = "KEY_SELECTED_GENEL_ARAMA_ID";

    public static final int IDLE_TIME = 60;//in seconds burası değişecek

    public static final int MENU_NAVIGATION_UP = 1;
    public static final int MENU_NAVIGATION_DOWN = 2;

    public static final int SCREEN_IDLE_MENU = 0;
    public static final int SCREEN_ONBOARDING = 1;
    public static final int SCREEN_DAIRE_ARA = 2;
    public static final int SCREEN_REHBER = 3;
    public static final int SCREEN_GOREVLI = 4;
    public static final int SCREEN_GUVENLIK = 5;
    public static final int SCREEN_KAPI_AC = 6;
    public static final int SCREEN_AYARLAR = 7;
    public static final int SCREEN_AYARLAR_YONETICI = 8;
    public static final int SCREEN_AYARLAR_TEKNIK_PERSONEL = 9;
    public static final int SCREEN_AYARLAR_DOOR_LOGS = 10;

    public static final int MENU_DAIRE_ARA = 1;
    public static final int MENU_REHBER = 2;
    public static final int MENU_GOREVLIYI_ARA = 3;
    public static final int MENU_GUVENLIK_ARA = 4;
    public static final int MENU_KAPI_AC = 5;
    public static final int MENU_AYARLAR = 6;

    public static final int AYARLAR_MENU_YONETICI = 1;
    public static final int AYARLAR_MENU_TEKNIK_PERSONEL = 2;
    public static final int AYARLAR_MENU_DOOR_LOGS = 3;

    public static final int YONETICI_MENU_LANGUAGE = 1;
    public static final int YONETICI_MENU_CHANGE_NAME = 2;
    public static final int YONETICI_MENU_CHANGE_ONBOARDING_NAME = 3;
    public static final int YONETICI_MENU_VOLUME = 4;
    public static final int YONETICI_MENU_TUS_SESI = 5;
    public static final int YONETICI_MENU_ADD_PASSWORD = 6;
    public static final int YONETICI_MENU_REMOVE_PASSWORD = 7;
    public static final int YONETICI_MENU_ADD_RFID = 8;
    public static final int YONETICI_MENU_REMOVE_RFID = 9;
    public static final int YONETICI_MENU_REMOVE_RFID_VIA_TOUCH = 10;
    public static final int YONETICI_MENU_KONUSMA_VOLUME = 11;
//    public static final int YONETICI_MENU_VIDEO_KAYIT = 11;

    public static final int TEKNIK_PERS_MENU_TARIH_SAAT = 1;
    public static final int TEKNIK_PERS_MENU_SIFIRLA = 2;
    public static final int TEKNIK_PERS_MENU_UPDATE_APP = 3;
    public static final int TEKNIK_PERS_MENU_IP_INFO = 4;
    public static final int TEKNIK_PERS_MENU_REHBER_GUNCELLE = 5;
    public static final int TEKNIK_PERS_MENU_RELAY_TIME = 6;
    public static final int TEKNIK_PERS_MENU_BACKUP = 7;
    public static final int TEKNIK_PERS_MENU_RECOVERY = 8;
    public static final int TEKNIK_PERS_MENU_REBOOT = 9;


    public static final int INIT_MENU_ZAMAN = 1;
    public static final int INIT_MENU_CIHAZ = 2;

    public static final int ENUM_DOOR_UNLOCK_MAIN = 1;
    public static final int ENUM_DOOR_UNLOCK_PEDESTRAIN = 2;
    public static final int ENUM_DOOR_UNLOCK_LONG = 3;
    public static final int ENUM_DOOR_UNLOCK_REVERSE_LONG = 4;
    public static  final int DOOR_UNLOCK_FOR_TEST = 5;
    public static  final int DOOR_UNLOCK_FOR_TEST2 = 6;

    public static final int SELECT_DEVICE_MENU_ZIL = 1;
    public static final int SELECT_DEVICE_MENU_SITE = 2;

    public static final int SELECT_CENTER_DEVICE_MENU_ZIL = 1;
    public static final int SELECT_CENTER_DEVICE_MENU_GUVENLIK = 2;

    public static final String KEYPAD_0 = "0";
    public static final String KEYPAD_1 = "1";
    public static final String KEYPAD_2 = "2";
    public static final String KEYPAD_3 = "3";
    public static final String KEYPAD_4 = "4";
    public static final String KEYPAD_5 = "5";
    public static final String KEYPAD_6 = "6";
    public static final String KEYPAD_7 = "7";
    public static final String KEYPAD_8 = "8";
    public static final String KEYPAD_9 = "9";

    public static final String KEYPAD_CALL = "c";
    public static final String KEYPAD_LOCK = "k";

    public static final String KEYPAD_HOME = "h";
    public static final String KEYPAD_DOWN = "d";
    public static final String KEYPAD_UP = "u";
    public static final String KEYPAD_BACK = "s";

    public static final String KEYPAD_CIHAZ_SOKME = "f";
    public static final String KEYPAD_CIHAZ_TAKMA = "t";

    public static final int OPERATION_PARSE_ERROR = -1;
    public static final int OPERATION_HANDSHAKE_DAIRE = 1;
    public static final int OPERATION_HANDSHAKE_DAIRE_RESPONSE = 2;
    public static final int OPERATION_HANDSHAKE_ZIL_PANEL = 3;
    public static final int OPERATION_HANDSHAKE_ZIL_PANEL_RESPONSE = 4;
    public static final int OPERATION_ADD_NEW_DAIRE = 5;
    public static final int OPERATION_ADD_NEW_DAIRE_RESPONSE = 6;
    public static final int OPERATION_GET_DATE_TIME = 7;
    public static final int OPERATION_GET_DATE_TIME_RESPONSE = 8;
    public static final int OPERATION_ARAMA_REQUEST = 9;
    public static final int OPERATION_ARAMA_REQUEST_RESPONSE = 10;
    public static final int OPERATION_ARAMA_REQUEST_CANCEL = 11;
    public static final int OPERATION_DOOR_UNLOCK = 12;
    public static final int OPERATION_MESSAGE = 13;
    public static final int OPERATION_ADD_NEW_PASSWORD = 14;
    public static final int OPERATION_DELETE_PASSWORD = 15;
    public static final int OPERATION_REQUEST_YONLENDIRME = 16;
    public static final int OPERATION_REQUEST_YONLENDIRME_RESPONSE = 17;
    public static final int OPERATION_REQUEST_DOOR_PASSWORDS = 18;
    public static final int OPERATION_REQUEST_DOOR_PASSWORDS_RESPONSE = 19;
    public static final int OPERATION_IS_DOOR_PASSWORD_SUITABLE = 20;
    public static final int OPERATION_IS_DOOR_PASSWORD_SUITABLE_RESPONSE = 21;
    public static final int OPERATION_HANDSHAKE_DAIRE_SECONDARY = 22;
    public static final int OPERATION_HANDSHAKE_DAIRE_SECONDARY_RESPONSE = 23;
    public static final int OPERATION_ADD_NEW_DAIRE_FOR_SECONDARY = 24;
    public static final int OPERATION_HANDSHAKE_ZIL_PANEL_FOR_SECONDARY = 25;
    public static final int OPERATION_REQUEST_DEVICE_INFOS = 26;
    public static final int OPERATION_REQUEST_DEVICE_INFOS_RESPONSE = 27;
    public static final int OPERATION_GUVENLIK_PASSWORD_CHANGE = 28;
    public static final int OPERATION_DEVICE_NAME_CHANGE = 29;
    public static final int OPERATION_EV_GUVENLIK_STATE = 30;
    public static final int OPERATION_YONLENDIRME_STATE = 31;
    public static final int OPERATION_RAHATSIZ_ETME_STATE = 32;
    public static final int OPERATION_EV_GUVENLIK_ALARM_STATE = 33;
    public static final int OPERATION_TEST_TCP_SERVICE = 34;
    public static final int OPERATION_ADB_STATE = 35;
    public static final int OPERATION_MESSAGE_READ_BY_RECEIVER = 36;
    public static final int OPERATION_NOTIFY_CALL_FOR_SECONDARY_DEVICES = 37;
    public static final int OPERATION_MULTI_CALL_REJECT = 38;
    public static final int OPERATION_MULTI_CALL_CANCELLED_BY_CALLER = 39;
    public static final int OPERATION_MULTI_CALL_ACCEPT = 40;
    public static final int OPERATION_MULTI_CALL_ACCEPTED = 41;
    public static final int OPERATION_HANDSHAKE_GUVENLIK = 42;
    public static final int OPERATION_HANDSHAKE_GUVENLIK_RESPONSE = 43;
    public static final int OPERATION_NEW_GUVENLIK_ADDED = 44;
    public static final int OPERATION_NEW_IP_CAMERA_ADDED = 45;
    public static final int OPERATION_NEW_IP_CAMERA_REMOVED = 46;
    public static final int OPERATION_ALARM_TRIGGERED = 47;
    public static final int OPERATION_ANOTHER_DEVICE_CALL_WARNING = 48;
    public static final int OPERATION_GOREVLI_STATE = 49;
    public static final int OPERATION_KAPI_ZILI_STATE = 50;
    public static final int OPERATION_EDIT_PASSWORD = 51;
    public static final int OPERATION_CLOSE_TRIGGERED_ALARM = 52;
    public static final int OPERATION_ALARM_TRIGGERED_FOR_SECONDARY = 53;
    public static final int OPERATION_MISSING_DATA = 54;
    public static final int OPERATION_MISSING_DATA_REQUEST = 55;
    public static final int OPERATION_HANDSHAKE_ZIL_PANEL_SITE = 56;
    public static final int OPERATION_HANDSHAKE_ZIL_PANEL_SITE_RESPONSE = 57;
    public static final int OPERATION_RESET_EV_GUVENLIK = 58;
    public static final int OPERATION_GUVENLIK_ALARM = 59;
    public static final int OPERATION_REQUEST_TO_CLERK = 60;
    public static final int OPERATION_RESET_ALL_SYSTEM = 61;
    public static final int OPERATION_NOTIFY_BREAD_REQUEST_FOR_SECONDARY_DEVICES = 62;
    public static final int OPERATION_TEKNIK_PERSONEL_SIFRE_STATE = 63;
    public static final int OPERATION_VIDEO_CALL_PERMISSION = 64;
    public static final int OPERATION_CHECK_CONNECTION = 65;
    public static final int OPERATION_CHECK_CONNECTION_RESPONSE = 66;
    public static final int OPERATION_DAIRELER_RESETLENDI = 67;
    public static final int OPERATION_GENEL_ARAMA = 68;

    public static final int TCP_ERROR_UNKNOWN_HOST = 1;
    public static final int TCP_ERROR_IO_EXCEPTION = 2;

    public static final int GPIO_EXPORT = 1;
    public static final int GPIO_DIRECTION = 2;
    public static final int GPIO_VALUE = 3;

    public static String PARAM_CALL_RECEIVING = "PARAM_CALL_RECEIVING";
    public static String PARAM_CALL_IS_YONLENDIRME = "PARAM_CALL_IS_YONLENDIRME";
    public static String PARAM_CALL_SS_IDS = "PARAM_CALL_SS_IDS";
    public static String PARAM_CALL_IS_NEED_VIDEO = "PARAM_CALL_IS_NEED_VIDEO";
    public static String PARAM_IS_MULTI_CALL_ACCEPT = "PARAM_IS_MULTI_CALL_ACCEPT";

    public static final int LANGUAGE_TR = 1;
    public static final int LANGUAGE_EN = 2;
    public static final int LANGUAGE_DE = 3;

    public static final int DOOR_UNLOCK_PASSWORD = 1;
    public static final int DOOR_UNLOCK_RFID = 2;
    public static final int DOOR_UNLOCK_DAIRE = 3;

    public static final int COM_MESSAGE_NOT_RESPONDING = 1;
    public static final int COM_MESSAGE_REJECT = 2;
    public static final int COM_MESSAGE_ACCEPT = 3;
    public static final int COM_MESSAGE_CANCELED = 4;
    public static final int COM_MESSAGE_BUSY = 5;

    public static final int CALLER_SUBE = 1;
    public static final int CALLER_GUVENLIK = 2;
    public static final int CALLER_ZIL = 3;
    public static final int CALLER_ZIL_SITE = 4;


    public static final float CUSTOM_DIALOG_WIDTH = 0.80f;
    public static final float CUSTOM_DIALOG_HEIGHT = 0.80f;

    public static final int DAIRE_ICI_MAX_DEVICE_NUMBER = 4;

    public static final boolean IS_DEVICE_NAUFFEN = false;

    public static final int GUVENLIK_SISTEMI_ALARM_PIR = 1;
    public static final int GUVENLIK_SISTEMI_ALARM_MANYETIK = 2;
    public static final int GUVENLIK_SISTEMI_ALARM_SU_BASMA = 3;
    public static final int GUVENLIK_SISTEMI_ALARM_YANGIN = 4;
    public static final int GUVENLIK_SISTEMI_ALARM_GAZ = 5;
    public static final int GUVENLIK_SISTEMI_ALARM_DISPLACEMENT = 6;
    public static final int GUVENLIK_SISTEMI_ALARM_GAZ_VANA = 7;
    public static final int GUVENLIK_SISTEMI_ALARM_SU_VANA = 8;

}

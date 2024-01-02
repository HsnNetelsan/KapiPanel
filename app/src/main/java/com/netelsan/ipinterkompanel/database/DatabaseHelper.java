package com.netelsan.ipinterkompanel.database;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.os.Build;
import android.util.Log;

import androidx.appcompat.app.AppCompatActivity;

import com.google.gson.Gson;
import com.netelsan.ipinterkompanel.Constants;
import com.netelsan.ipinterkompanel.Helper;
import com.netelsan.ipinterkompanel.R;
import com.netelsan.ipinterkompanel.model.Daire;
import com.netelsan.ipinterkompanel.model.DateTimeClass;
import com.netelsan.ipinterkompanel.model.DoorPassword;
import com.netelsan.ipinterkompanel.model.DoorUnlockLog;
import com.netelsan.ipinterkompanel.model.Guvenlik;
import com.netelsan.ipinterkompanel.model.IPCamera;
import com.netelsan.ipinterkompanel.model.ZilPanel;
import com.netelsan.ipinterkompanel.model.ZilPanelSite;
import com.netelsan.ipinterkompanel.tcp.Utils;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.function.Predicate;

public class DatabaseHelper extends SQLiteOpenHelper {

    private static final int DATABASE_VERSION = 1;

    private static final String DATABASE_NAME = "db_zil";//database adı

    private static final String TABLE_NAME_DAIRE = "daire";
    private static String DAIRE_ID = "id";
    private static String DAIRE_ISIM = "isim";
    private static String DAIRE_SOYISIM = "soyisim";
    private static String DAIRE_IP = "ip";
    private static String DAIRE_BLOK = "blok";
    private static String DAIRE_DAIRE_NO = "daire_no";
    private static String DAIRE_REAL_DAIRE_NO = "real_daire_no";
    private static String DAIRE_KAT_NO = "kat_no";
    private static String DAIRE_IS_GOREVLI = "is_gorevli";
    private static String DAIRE_GOREVLI_OLDUGU_BINALAR = "binalar";

    private static final String TABLE_NAME_ZIL = "zil";
    private static String ZIL_ID = "id";
    private static String ZIL_IP = "ip";
    private static String ZIL_DEVICE_NAME = "name";
    private static String ZIL_BLOK = "blok";
    private static String ZIL_KAPI_NO = "kapi_no";


    private static final String TABLE_NAME_ZIL_SITE = "zil_site";
    private static String ZIL_SITE_ID = "id";
    private static String ZIL_SITE_IP = "ip";
    private static String ZIL_SITE_DEVICE_NAME = "name";
    private static String ZIL_SITE_KAPI_NO = "kapi_no";

    private static final String TABLE_NAME_GUVENLIK = "guvenlik";
    private static String GUVENLIK_ID = "id";
    private static String GUVENLIK_NAME = "name";
    private static String GUVENLIK_IP = "ip";
    private static String GUVENLIK_GUVENLIK_NO = "guvenlik_no";

    private static final String TABLE_NAME_PASSWORD = "password";
    private static String PASSWORD_ID = "id";
    private static String PASSWORD_IP = "ip";
    private static String PASSWORD_DOOR = "door";
    private static String PASSWORD_RFID = "rfid";
    private static String PASSWORD_IS_ACTIVE = "is_active";
    private static String PASSWORD_LABEL = "label";

    private static final String TABLE_NAME_DOOR_LOG = "door_log";
    private static String DOOR_LOG_ID = "id";
    private static String DOOR_LOG_RFID = "rfid";
    private static String DOOR_LOG_PASSWORD = "password";
    private static String DOOR_LOG_USER_IP = "user_ip";
    private static String DOOR_LOG_DATETIME = "datetime";
    private static String DOOR_LOG_UNLOCK_TYPE = "unlock_type";
    private static String DOOR_LOG_LABEL = "label";

    private static final String TABLE_NAME_IP_CAMERA = "ip_camera";
    private static String IP_CAMERA_ID = "id";
    private static String IP_CAMERA_NAME = "name";
    private static String IP_CAMERA_IP = "ip";
    private static String IP_CAMERA_PORT = "port";
    private static String IP_CAMERA_SUFFIX = "suffix";
    private static String IP_CAMERA_WORK_FOR_IP = "work_for";
    private static String IP_CAMERA_LAST_SS = "LAST_SS";

    private static final String TABLE_NAME_ZIL_DATE_TIME = "date_time";
    private static String ZIL_HOUR_ID = "id";
    private static String ZIL_HOUR = "hour";
    private static String ZIL_MINUTE = "minute";
    private static String ZIL_DAY_OF_MONTH = "day";
    private static String ZIL_MONTH = "month";
    private static String ZIL_YEAR = "year";

    public DatabaseHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {

        db.execSQL(CREATE_TABLE_DAIRE);
        db.execSQL(CREATE_TABLE_ZIL);
        db.execSQL(CREATE_TABLE_ZIL_SITE);
        db.execSQL(CREATE_TABLE_GUVENLIK);
        db.execSQL(CREATE_TABLE_PASSWORD);
        db.execSQL(CREATE_TABLE_DOOR_LOG);
        db.execSQL(CREATE_TABLE_IP_CAMERA);
        db.execSQL(CREATE_TABLE_ZIL_DATE_TIME);

    }

    @Override
    public void onUpgrade(SQLiteDatabase arg0, int arg1, int arg2) {

    }

    //********************** DAIRE **********************//
    public void deleteDaire(Daire daire) { //id si belli olan row u silmek için

        SQLiteDatabase db = this.getWritableDatabase();
        db.delete(TABLE_NAME_DAIRE, DAIRE_ID + " = ?", new String[]{String.valueOf(daire.getId() + "")});
        db.close();
    }

    public void insertDaire(Daire daire) {
            Boolean selectedDaireKontrol = daireKontrolByIP(daire.getIp());


        String deg=daire.getSoyisim();
        if(selectedDaireKontrol==false) {
            SQLiteDatabase db = this.getWritableDatabase();
            ContentValues values = new ContentValues();
            values.put(DAIRE_ISIM, daire.getIsim());
            values.put(DAIRE_SOYISIM, daire.getSoyisim());
            values.put(DAIRE_IP, daire.getIp());
            values.put(DAIRE_BLOK, daire.getBlok());
            values.put(DAIRE_DAIRE_NO, daire.getDaireNo());
            values.put(DAIRE_KAT_NO, daire.getKatNo() + "");
            values.put(DAIRE_REAL_DAIRE_NO, Integer.parseInt(deg.substring(6,deg.length())));

            values.put(DAIRE_IS_GOREVLI, convertBooleanToInt(daire.isGorevli()));
            Gson gson = new Gson();
            String objectString = gson.toJson(daire.getGorevliOlduguBinalar());
            values.put(DAIRE_GOREVLI_OLDUGU_BINALAR, objectString);

            db.insert(TABLE_NAME_DAIRE, null, values);
            db.close();
        }


    }

    public void updateDaire(Daire daire) {

        String deg=daire.getSoyisim();
        SQLiteDatabase db = this.getWritableDatabase();

        ContentValues values = new ContentValues();
        values.put(DAIRE_ISIM, daire.getIsim());
        values.put(DAIRE_SOYISIM, daire.getSoyisim());
        values.put(DAIRE_IP, daire.getIp());
        values.put(DAIRE_BLOK, daire.getBlok());
        values.put(DAIRE_DAIRE_NO, daire.getDaireNo());
        values.put(DAIRE_KAT_NO, daire.getKatNo() + "");
        values.put(DAIRE_REAL_DAIRE_NO, Integer.parseInt(deg.substring(6,deg.length())));
        values.put(DAIRE_IS_GOREVLI, convertBooleanToInt(daire.isGorevli()));

        Gson gson = new Gson();
        String objectString = gson.toJson(daire.getGorevliOlduguBinalar());
        values.put(DAIRE_GOREVLI_OLDUGU_BINALAR, objectString);

        db.update(TABLE_NAME_DAIRE, values, DAIRE_IP + " = ?", new String[]{daire.getIp()});
        db.close();
    }

    public ArrayList<Daire> getDairelerForBlok(int blokNo) {

        SQLiteDatabase db = this.getReadableDatabase();
        String selectQuery = "SELECT * FROM " + TABLE_NAME_DAIRE + " WHERE " + DAIRE_BLOK + " = " + blokNo + " ORDER BY " + DAIRE_REAL_DAIRE_NO + " ASC";
        Cursor cursor = db.rawQuery(selectQuery, null);
        ArrayList<Daire> arrayList = new ArrayList<>();

        if (cursor.moveToFirst()) {
            do {

                Daire daire = new Daire();

                daire.setId(cursor.getInt(cursor.getColumnIndex(DAIRE_ID)));
                daire.setIsim(cursor.getString(cursor.getColumnIndex(DAIRE_ISIM)));
                daire.setSoyisim(cursor.getString(cursor.getColumnIndex(DAIRE_SOYISIM)));
                daire.setIp(cursor.getString(cursor.getColumnIndex(DAIRE_IP)));
                daire.setBlok(cursor.getInt(cursor.getColumnIndex(DAIRE_BLOK)));
                daire.setDaireNo(cursor.getInt(cursor.getColumnIndex(DAIRE_DAIRE_NO)));
                daire.setKatNo(cursor.getString(cursor.getColumnIndex(DAIRE_KAT_NO)));

                daire.setGorevli(convertIntToBoolean(cursor.getInt(cursor.getColumnIndex(DAIRE_IS_GOREVLI))));

                String objectString = cursor.getString(cursor.getColumnIndex(DAIRE_GOREVLI_OLDUGU_BINALAR));
                Gson gson = new Gson();
                ArrayList<String> arrayListBinalar = gson.fromJson(objectString, ArrayList.class);
                daire.setGorevliOlduguBinalar(arrayListBinalar);

                // arrayList.indexOf(daire);

                //   Log.d(Constants.LOG_TAG, "feyyaz site zil daire geliyor =   arrayList.indexOf(daire)" + arrayList.indexOf(daire));
                if (arrayList.indexOf(daire.getDaireNo()) == -1) {
                    arrayList.add(daire);
                }
            } while (cursor.moveToNext());
        }
        db.close();

        return arrayList;
    }

    public ArrayList<Daire> getGorevliDaireler() {

        SQLiteDatabase db = this.getReadableDatabase();
        String selectQuery = "SELECT * FROM " + TABLE_NAME_DAIRE + " WHERE " + DAIRE_IS_GOREVLI + " LIKE " + "1" + " ORDER BY " + DAIRE_REAL_DAIRE_NO + " ASC";
        Cursor cursor = db.rawQuery(selectQuery, null);
        ArrayList<Daire> arrayList = new ArrayList<>();

        if (cursor.moveToFirst()) {
            do {

                Daire daire = new Daire();

                daire.setId(cursor.getInt(cursor.getColumnIndex(DAIRE_ID)));
                daire.setIsim(cursor.getString(cursor.getColumnIndex(DAIRE_ISIM)));
                daire.setSoyisim(cursor.getString(cursor.getColumnIndex(DAIRE_SOYISIM)));
                daire.setIp(cursor.getString(cursor.getColumnIndex(DAIRE_IP)));
                daire.setBlok(cursor.getInt(cursor.getColumnIndex(DAIRE_BLOK)));
                daire.setDaireNo(cursor.getInt(cursor.getColumnIndex(DAIRE_DAIRE_NO)));
                daire.setKatNo(cursor.getString(cursor.getColumnIndex(DAIRE_KAT_NO)));

                daire.setGorevli(convertIntToBoolean(cursor.getInt(cursor.getColumnIndex(DAIRE_IS_GOREVLI))));

                String objectString = cursor.getString(cursor.getColumnIndex(DAIRE_GOREVLI_OLDUGU_BINALAR));
                Gson gson = new Gson();
                ArrayList<String> arrayListBinalar = gson.fromJson(objectString, ArrayList.class);
                daire.setGorevliOlduguBinalar(arrayListBinalar);

                arrayList.add(daire);
            } while (cursor.moveToNext());
        }
        db.close();

        return arrayList;
    }

    public ArrayList<Daire> getGorevliDairelerInSameBlok(int blokNo) {

        SQLiteDatabase db = this.getReadableDatabase();
        String selectQuery = "SELECT * FROM " + TABLE_NAME_DAIRE + " WHERE " + DAIRE_IS_GOREVLI + " LIKE " + "1 AND " + DAIRE_BLOK + " LIKE " + blokNo + " ORDER BY " + DAIRE_REAL_DAIRE_NO + " ASC";
//        Log.d(Constants.LOG_TAG, "getGorevliDairelerInSameBlok selectQuery=" + selectQuery);

        Cursor cursor = db.rawQuery(selectQuery, null);
        ArrayList<Daire> arrayList = new ArrayList<>();

        if (cursor.moveToFirst()) {
            do {

                Daire daire = new Daire();

                daire.setId(cursor.getInt(cursor.getColumnIndex(DAIRE_ID)));
                daire.setIsim(cursor.getString(cursor.getColumnIndex(DAIRE_ISIM)));
                daire.setSoyisim(cursor.getString(cursor.getColumnIndex(DAIRE_SOYISIM)));
                daire.setIp(cursor.getString(cursor.getColumnIndex(DAIRE_IP)));
                daire.setBlok(cursor.getInt(cursor.getColumnIndex(DAIRE_BLOK)));
                daire.setDaireNo(cursor.getInt(cursor.getColumnIndex(DAIRE_DAIRE_NO)));
                daire.setKatNo(cursor.getString(cursor.getColumnIndex(DAIRE_KAT_NO)));

                daire.setGorevli(convertIntToBoolean(cursor.getInt(cursor.getColumnIndex(DAIRE_IS_GOREVLI))));

                String objectString = cursor.getString(cursor.getColumnIndex(DAIRE_GOREVLI_OLDUGU_BINALAR));
                Gson gson = new Gson();
                ArrayList<String> arrayListBinalar = gson.fromJson(objectString, ArrayList.class);
                daire.setGorevliOlduguBinalar(arrayListBinalar);

                arrayList.add(daire);
            } while (cursor.moveToNext());
        }
        db.close();

        return arrayList;
    }

    public ArrayList<Daire> getGorevliDairelerInOtherBlocks(int currentBlokNo) {

        SQLiteDatabase db = this.getReadableDatabase();
        String selectQuery = "SELECT * FROM " + TABLE_NAME_DAIRE + " WHERE " + DAIRE_IS_GOREVLI + " LIKE " + "1 AND " + DAIRE_BLOK + " NOT LIKE " + currentBlokNo + " ORDER BY " + DAIRE_BLOK + " ASC, " + DAIRE_REAL_DAIRE_NO + " ASC";
//        Log.d(Constants.LOG_TAG, "getGorevliDairelerInOtherBlocks selectQuery=" + selectQuery);
        Cursor cursor = db.rawQuery(selectQuery, null);
        ArrayList<Daire> arrayList = new ArrayList<>();

        if (cursor.moveToFirst()) {
            do {

                Daire daire = new Daire();

                daire.setId(cursor.getInt(cursor.getColumnIndex(DAIRE_ID)));
                daire.setIsim(cursor.getString(cursor.getColumnIndex(DAIRE_ISIM)));
                daire.setSoyisim(cursor.getString(cursor.getColumnIndex(DAIRE_SOYISIM)));
                daire.setIp(cursor.getString(cursor.getColumnIndex(DAIRE_IP)));
                daire.setBlok(cursor.getInt(cursor.getColumnIndex(DAIRE_BLOK)));
                daire.setDaireNo(cursor.getInt(cursor.getColumnIndex(DAIRE_DAIRE_NO)));
                daire.setKatNo(cursor.getString(cursor.getColumnIndex(DAIRE_KAT_NO)));

                daire.setGorevli(convertIntToBoolean(cursor.getInt(cursor.getColumnIndex(DAIRE_IS_GOREVLI))));

                String objectString = cursor.getString(cursor.getColumnIndex(DAIRE_GOREVLI_OLDUGU_BINALAR));
                Gson gson = new Gson();
                ArrayList<String> arrayListBinalar = gson.fromJson(objectString, ArrayList.class);
                daire.setGorevliOlduguBinalar(arrayListBinalar);

                arrayList.add(daire);
            } while (cursor.moveToNext());
        }
        db.close();

        return arrayList;
    }

    public ArrayList<Daire> getDaireler() {

        SQLiteDatabase db = this.getReadableDatabase();

        String selectQuery = "SELECT * FROM " + TABLE_NAME_DAIRE + " ORDER BY " + DAIRE_REAL_DAIRE_NO + " ASC";
        Cursor cursor = db.rawQuery(selectQuery, null);

        ArrayList<Daire> arrayList = new ArrayList<>();

        if (cursor.moveToFirst()) {
            do {

                Daire daire = new Daire();

                daire.setId(cursor.getInt(cursor.getColumnIndex(DAIRE_ID)));
                daire.setIsim(cursor.getString(cursor.getColumnIndex(DAIRE_ISIM)));
                daire.setSoyisim(cursor.getString(cursor.getColumnIndex(DAIRE_SOYISIM)));
                daire.setIp(cursor.getString(cursor.getColumnIndex(DAIRE_IP)));
                daire.setBlok(cursor.getInt(cursor.getColumnIndex(DAIRE_BLOK)));
                daire.setDaireNo(cursor.getInt(cursor.getColumnIndex(DAIRE_DAIRE_NO)));
                daire.setKatNo(cursor.getString(cursor.getColumnIndex(DAIRE_KAT_NO)));

                daire.setGorevli(convertIntToBoolean(cursor.getInt(cursor.getColumnIndex(DAIRE_IS_GOREVLI))));

                String objectString = cursor.getString(cursor.getColumnIndex(DAIRE_GOREVLI_OLDUGU_BINALAR));
                Gson gson = new Gson();
                ArrayList<String> arrayListBinalar = gson.fromJson(objectString, ArrayList.class);
                daire.setGorevliOlduguBinalar(arrayListBinalar);

                arrayList.add(daire);
            } while (cursor.moveToNext());
        }
        db.close();

        return arrayList;
    }

    public ArrayList<Integer> getDairelerGroupByBlok() {

        SQLiteDatabase db = this.getReadableDatabase();

        String selectQuery = "SELECT " + DAIRE_BLOK + " FROM " + TABLE_NAME_DAIRE + " GROUP BY " + DAIRE_BLOK;
        Cursor cursor = db.rawQuery(selectQuery, new String[]{});

        ArrayList<Integer> arrayList = new ArrayList<>();

        if (cursor.moveToFirst()) {
            do {
                int blokNo = cursor.getInt(cursor.getColumnIndex(DAIRE_BLOK));
                arrayList.add(blokNo);
            } while (cursor.moveToNext());
        }
        db.close();

        return arrayList;
    }

    public ArrayList<Daire> getDairelerInSameApartment(int blokNo) {

        SQLiteDatabase db = this.getReadableDatabase();
        String selectQuery = "SELECT * FROM " + TABLE_NAME_DAIRE + " WHERE " + DAIRE_IP + " != ? AND " + DAIRE_BLOK + " = ?";
        Cursor cursor = db.rawQuery(selectQuery, new String[]{Utils.getIPAddress(true), (blokNo + "")});
        ArrayList<Daire> arrayList = new ArrayList<>();

        if (cursor.moveToFirst()) {
            do {

                Daire daire = new Daire();

                daire.setId(cursor.getInt(cursor.getColumnIndex(DAIRE_ID)));
                daire.setIsim(cursor.getString(cursor.getColumnIndex(DAIRE_ISIM)));
                daire.setSoyisim(cursor.getString(cursor.getColumnIndex(DAIRE_SOYISIM)));
                daire.setIp(cursor.getString(cursor.getColumnIndex(DAIRE_IP)));
                daire.setBlok(cursor.getInt(cursor.getColumnIndex(DAIRE_BLOK)));
                daire.setDaireNo(cursor.getInt(cursor.getColumnIndex(DAIRE_DAIRE_NO)));
                daire.setKatNo(cursor.getString(cursor.getColumnIndex(DAIRE_KAT_NO)));

                daire.setGorevli(convertIntToBoolean(cursor.getInt(cursor.getColumnIndex(DAIRE_IS_GOREVLI))));

                String objectString = cursor.getString(cursor.getColumnIndex(DAIRE_GOREVLI_OLDUGU_BINALAR));
                Gson gson = new Gson();
                ArrayList<String> arrayListBinalar = gson.fromJson(objectString, ArrayList.class);
                daire.setGorevliOlduguBinalar(arrayListBinalar);

                arrayList.add(daire);
            } while (cursor.moveToNext());
        }
        db.close();

        return arrayList;
    }

    public Daire getDaireByIP(String ip) {

        Daire daire = null;
        String selectQuery = "SELECT * FROM " + TABLE_NAME_DAIRE + " WHERE " + DAIRE_IP + "='" + ip + "'";

        SQLiteDatabase db = this.getReadableDatabase();

        Cursor cursor = db.rawQuery(selectQuery, null);

        // Move to first row
        cursor.moveToFirst();

        if (cursor.getCount() > 0) {
            daire = new Daire();
            daire.setId(cursor.getInt(cursor.getColumnIndex(DAIRE_ID)));
            daire.setIsim(cursor.getString(cursor.getColumnIndex(DAIRE_ISIM)));
            daire.setSoyisim(cursor.getString(cursor.getColumnIndex(DAIRE_SOYISIM)));
            daire.setIp(cursor.getString(cursor.getColumnIndex(DAIRE_IP)));
            daire.setBlok(cursor.getInt(cursor.getColumnIndex(DAIRE_BLOK)));
            daire.setDaireNo(cursor.getInt(cursor.getColumnIndex(DAIRE_DAIRE_NO)));
            daire.setKatNo(cursor.getString(cursor.getColumnIndex(DAIRE_KAT_NO)));

            daire.setGorevli(convertIntToBoolean(cursor.getInt(cursor.getColumnIndex(DAIRE_IS_GOREVLI))));

            String objectString = cursor.getString(cursor.getColumnIndex(DAIRE_GOREVLI_OLDUGU_BINALAR));
            Gson gson = new Gson();
            ArrayList<String> arrayListBinalar = gson.fromJson(objectString, ArrayList.class);
            daire.setGorevliOlduguBinalar(arrayListBinalar);

        }
        cursor.close();
        db.close();

        return daire;
    }


    public Boolean daireKontrolByIP(String ip) {

        String selectQuery = "SELECT * FROM " + TABLE_NAME_DAIRE + " WHERE " + DAIRE_IP + "='" + ip + "'";
        Boolean selectedDaireKontrol = false;
        SQLiteDatabase db = this.getReadableDatabase();

        //fey
        Log.d(Constants.LOG_TAG, "fey getDaireByIP normal");
        Cursor cursor = db.rawQuery(selectQuery, null);
        Log.d(Constants.LOG_TAG, "fey getDaireByIP =" + cursor.moveToFirst() + "cursor.getCount()=" + cursor.getCount());
        // Move to first row
        selectedDaireKontrol = cursor.moveToFirst();
        cursor.close();
        db.close();

        return selectedDaireKontrol;

    }


    public void deleteAllDaires() {

        SQLiteDatabase db = this.getWritableDatabase();

        db.execSQL("DROP TABLE IF EXISTS " + TABLE_NAME_DAIRE);

        db.execSQL(CREATE_TABLE_DAIRE);

        db.close();
    }

    public void deleteDairesWithBlokNo(int blokNo) {

        SQLiteDatabase db = this.getWritableDatabase();
        db.execSQL("DELETE FROM " + TABLE_NAME_DAIRE + " WHERE " + DAIRE_BLOK + " LIKE '" + blokNo + "'");
        db.close();
    }

    //********************** DAIRE END **********************//


    //********************** ZIL PANEL **********************//
    public void deleteZilPanel(ZilPanel zilPanel) { //id si belli olan row u silmek için

        SQLiteDatabase db = this.getWritableDatabase();
        db.delete(TABLE_NAME_ZIL, ZIL_ID + " = ?", new String[]{String.valueOf(zilPanel.getId() + "")});
        db.close();
    }

    public void insertZilPanel(ZilPanel zilPanel) {

        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(ZIL_IP, zilPanel.getIp());
        values.put(ZIL_DEVICE_NAME, zilPanel.getDeviceName());
        values.put(ZIL_BLOK, zilPanel.getBlok());
        values.put(ZIL_KAPI_NO, zilPanel.getKapiNo());

        db.insert(TABLE_NAME_ZIL, null, values);
        db.close();

    }

    public void updateZilPanel(ZilPanel zilPanel) {

        SQLiteDatabase db = this.getWritableDatabase();

        ContentValues values = new ContentValues();
        values.put(ZIL_IP, zilPanel.getIp());
        values.put(ZIL_DEVICE_NAME, zilPanel.getDeviceName());
        values.put(ZIL_BLOK, zilPanel.getBlok());
        values.put(ZIL_KAPI_NO, zilPanel.getKapiNo());

        db.update(TABLE_NAME_ZIL, values, ZIL_IP + " = ?", new String[]{zilPanel.getIp()});
        db.close();
    }

    public ArrayList<ZilPanel> getZilPanelleri(boolean isNeedSelf) {

        SQLiteDatabase db = this.getReadableDatabase();

        String selectQuery;
        Cursor cursor;
        if (isNeedSelf) {
            selectQuery = "SELECT * FROM " + TABLE_NAME_ZIL;
            cursor = db.rawQuery(selectQuery, null);
        } else {
            selectQuery = "SELECT * FROM " + TABLE_NAME_ZIL + " WHERE " + ZIL_IP + " != ? ";
            cursor = db.rawQuery(selectQuery, new String[]{Utils.getIPAddress(true)});
        }

        ArrayList<ZilPanel> arrayList = new ArrayList<>();

        if (cursor.moveToFirst()) {
            do {

                ZilPanel zilPanel = new ZilPanel();

                zilPanel.setId(cursor.getInt(cursor.getColumnIndex(ZIL_ID)));
                zilPanel.setIp(cursor.getString(cursor.getColumnIndex(ZIL_IP)));
                zilPanel.setDeviceName(cursor.getString(cursor.getColumnIndex(ZIL_DEVICE_NAME)));
                zilPanel.setBlok(cursor.getInt(cursor.getColumnIndex(ZIL_BLOK)));
                zilPanel.setKapiNo(cursor.getInt(cursor.getColumnIndex(ZIL_KAPI_NO)));

                arrayList.add(zilPanel);
            } while (cursor.moveToNext());
        }
        db.close();

        return arrayList;
    }

    public ArrayList<ZilPanel> getOtherZilPanelleriByBlokNo(int blokNo) {

        SQLiteDatabase db = this.getReadableDatabase();

        String selectQuery = "SELECT * FROM " + TABLE_NAME_ZIL + " WHERE " + ZIL_IP + " != ? AND " + ZIL_BLOK + " =?";
        Cursor cursor = db.rawQuery(selectQuery, new String[]{Utils.getIPAddress(true), (blokNo + "")});

        ArrayList<ZilPanel> arrayList = new ArrayList<>();

        if (cursor.moveToFirst()) {
            do {

                ZilPanel zilPanel = new ZilPanel();

                zilPanel.setId(cursor.getInt(cursor.getColumnIndex(ZIL_ID)));
                zilPanel.setIp(cursor.getString(cursor.getColumnIndex(ZIL_IP)));
                zilPanel.setDeviceName(cursor.getString(cursor.getColumnIndex(ZIL_DEVICE_NAME)));
                zilPanel.setBlok(cursor.getInt(cursor.getColumnIndex(ZIL_BLOK)));
                zilPanel.setKapiNo(cursor.getInt(cursor.getColumnIndex(ZIL_KAPI_NO)));

                arrayList.add(zilPanel);
            } while (cursor.moveToNext());
        }
        db.close();

        return arrayList;
    }

    public ZilPanel getZilPanelByIP(String ip) {

        ZilPanel zilPanel = null;
        String selectQuery = "SELECT * FROM " + TABLE_NAME_ZIL + " WHERE " + ZIL_IP + "='" + ip + "'";

        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery(selectQuery, null);
        // Move to first row
        cursor.moveToFirst();
        if (cursor.getCount() > 0) {

            zilPanel = new ZilPanel();
            zilPanel.setId(cursor.getInt(cursor.getColumnIndex(ZIL_ID)));
            zilPanel.setIp(cursor.getString(cursor.getColumnIndex(ZIL_IP)));
            zilPanel.setDeviceName(cursor.getString(cursor.getColumnIndex(ZIL_DEVICE_NAME)));
            zilPanel.setBlok(cursor.getInt(cursor.getColumnIndex(ZIL_BLOK)));
            zilPanel.setKapiNo(cursor.getInt(cursor.getColumnIndex(ZIL_KAPI_NO)));

        }
        cursor.close();
        db.close();

        return zilPanel;
    }

    public void deleteAllZilPanelleri() {

        SQLiteDatabase db = this.getWritableDatabase();

        db.execSQL("DROP TABLE IF EXISTS " + TABLE_NAME_ZIL);

        db.execSQL(CREATE_TABLE_ZIL);

        db.close();
    }
    //********************** ZIL PANEL END **********************//

    //********************** ZIL PANEL SITE **********************//
    public void deleteSiteZilPanel(ZilPanelSite zilPanelSite) {

        SQLiteDatabase db = this.getWritableDatabase();
        db.delete(TABLE_NAME_ZIL_SITE, ZIL_ID + " = ?", new String[]{String.valueOf(zilPanelSite.getId() + "")});
        db.close();
    }

    public void insertSiteZilPanel(ZilPanelSite zilPanelSite) {

        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(ZIL_SITE_IP, zilPanelSite.getIp());
        values.put(ZIL_SITE_DEVICE_NAME, zilPanelSite.getDeviceName());
        values.put(ZIL_SITE_KAPI_NO, zilPanelSite.getKapiNo());

        db.insert(TABLE_NAME_ZIL_SITE, null, values);
        db.close();

    }

    public void updateSiteZilPanel(ZilPanelSite zilPanelSite) {

        SQLiteDatabase db = this.getWritableDatabase();

        ContentValues values = new ContentValues();
        values.put(ZIL_SITE_IP, zilPanelSite.getIp());
        values.put(ZIL_SITE_DEVICE_NAME, zilPanelSite.getDeviceName());
        values.put(ZIL_SITE_KAPI_NO, zilPanelSite.getKapiNo());

        db.update(TABLE_NAME_ZIL_SITE, values, ZIL_SITE_IP + " = ?", new String[]{zilPanelSite.getIp()});
        db.close();
    }

    public ArrayList<ZilPanelSite> getSiteZilPanelleri(boolean isNeedSelf) {

        SQLiteDatabase db = this.getReadableDatabase();

        String selectQuery;
        Cursor cursor;
        if (isNeedSelf) {
            selectQuery = "SELECT * FROM " + TABLE_NAME_ZIL_SITE;
            cursor = db.rawQuery(selectQuery, null);
        } else {
            selectQuery = "SELECT * FROM " + TABLE_NAME_ZIL_SITE + " WHERE " + ZIL_SITE_IP + " != ? ";
            cursor = db.rawQuery(selectQuery, new String[]{Utils.getIPAddress(true)});
        }

        ArrayList<ZilPanelSite> arrayList = new ArrayList<>();

        if (cursor.moveToFirst()) {
            do {

                ZilPanelSite zilPanelSite = new ZilPanelSite();

                zilPanelSite = new ZilPanelSite();
                zilPanelSite.setId(cursor.getInt(cursor.getColumnIndex(ZIL_SITE_ID)));
                zilPanelSite.setIp(cursor.getString(cursor.getColumnIndex(ZIL_SITE_IP)));
                zilPanelSite.setDeviceName(cursor.getString(cursor.getColumnIndex(ZIL_SITE_DEVICE_NAME)));
                zilPanelSite.setKapiNo(cursor.getInt(cursor.getColumnIndex(ZIL_SITE_KAPI_NO)));

                arrayList.add(zilPanelSite);
            } while (cursor.moveToNext());
        }
        db.close();

        return arrayList;
    }

    public ZilPanelSite getSiteZilPanelByIP(String ip) {

        ZilPanelSite zilPanelSite = null;
        String selectQuery = "SELECT * FROM " + TABLE_NAME_ZIL_SITE + " WHERE " + ZIL_SITE_IP + "='" + ip + "'";

        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery(selectQuery, null);
        // Move to first row
        cursor.moveToFirst();
        if (cursor.getCount() > 0) {

            zilPanelSite = new ZilPanelSite();
            zilPanelSite.setId(cursor.getInt(cursor.getColumnIndex(ZIL_SITE_ID)));
            zilPanelSite.setIp(cursor.getString(cursor.getColumnIndex(ZIL_SITE_IP)));
            zilPanelSite.setDeviceName(cursor.getString(cursor.getColumnIndex(ZIL_SITE_DEVICE_NAME)));
            zilPanelSite.setKapiNo(cursor.getInt(cursor.getColumnIndex(ZIL_SITE_KAPI_NO)));

        }
        cursor.close();
        db.close();

        return zilPanelSite;
    }

    public void deleteAllSiteZilPanelleri() {

        SQLiteDatabase db = this.getWritableDatabase();

        db.execSQL("DROP TABLE IF EXISTS " + TABLE_NAME_ZIL_SITE);

        db.execSQL(CREATE_TABLE_ZIL_SITE);

        db.close();
    }
    //********************** ZIL PANEL SITE END **********************//

    //********************** GUVENLIK **********************//
    public void deleteGuvenlik(Guvenlik guvenlik) { //id si belli olan row u silmek için

        SQLiteDatabase db = this.getWritableDatabase();
        db.delete(TABLE_NAME_GUVENLIK, GUVENLIK_ID + " = ?", new String[]{String.valueOf(guvenlik.getId() + "")});
        db.close();
    }

    public void insertGuvenlik(Guvenlik guvenlik) {

        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(GUVENLIK_NAME, guvenlik.getDeviceName());
        values.put(GUVENLIK_IP, guvenlik.getIp());
        values.put(GUVENLIK_GUVENLIK_NO, guvenlik.getGuvenlikNo());

        db.insert(TABLE_NAME_GUVENLIK, null, values);
        db.close();

    }

    public void updateGuvenlik(Guvenlik guvenlik) {

        SQLiteDatabase db = this.getWritableDatabase();

        ContentValues values = new ContentValues();
        values.put(GUVENLIK_NAME, guvenlik.getDeviceName());
        values.put(GUVENLIK_IP, guvenlik.getIp());
        values.put(GUVENLIK_GUVENLIK_NO, guvenlik.getGuvenlikNo());

        db.update(TABLE_NAME_GUVENLIK, values, GUVENLIK_IP + " = ?", new String[]{guvenlik.getIp()});
        db.close();
    }

    public ArrayList<Guvenlik> getGuvenlikler() {

        SQLiteDatabase db = this.getReadableDatabase();

        String selectQuery = "SELECT * FROM " + TABLE_NAME_GUVENLIK + " GROUP BY " + GUVENLIK_IP;
        Cursor cursor = db.rawQuery(selectQuery, null);

        ArrayList<Guvenlik> arrayList = new ArrayList<>();

        if (cursor.moveToFirst()) {
            do {

                Guvenlik guvenlik = new Guvenlik();

                guvenlik.setId(cursor.getInt(cursor.getColumnIndex(GUVENLIK_ID)));
                guvenlik.setDeviceName(cursor.getString(cursor.getColumnIndex(GUVENLIK_NAME)));
                guvenlik.setIp(cursor.getString(cursor.getColumnIndex(GUVENLIK_IP)));
                guvenlik.setGuvenlikNo(cursor.getInt(cursor.getColumnIndex(GUVENLIK_GUVENLIK_NO)));

                arrayList.add(guvenlik);
            } while (cursor.moveToNext());
        }
        db.close();

        return arrayList;
    }

    public Guvenlik getGuvenlikByIP(String ip) {

        Guvenlik guvenlik = null;
        String selectQuery = "SELECT * FROM " + TABLE_NAME_GUVENLIK + " WHERE " + GUVENLIK_IP + "='" + ip + "'";

        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery(selectQuery, null);
        // Move to first row
        cursor.moveToFirst();
        if (cursor.getCount() > 0) {

            guvenlik = new Guvenlik();
            guvenlik.setId(cursor.getInt(cursor.getColumnIndex(GUVENLIK_ID)));
            guvenlik.setDeviceName(cursor.getString(cursor.getColumnIndex(GUVENLIK_NAME)));
            guvenlik.setIp(cursor.getString(cursor.getColumnIndex(GUVENLIK_IP)));
            guvenlik.setGuvenlikNo(cursor.getInt(cursor.getColumnIndex(GUVENLIK_GUVENLIK_NO)));

        }

        cursor.close();
        db.close();

        return guvenlik;
    }

    public void deleteAllGuvenlikler() {

        SQLiteDatabase db = this.getWritableDatabase();

        db.execSQL("DROP TABLE IF EXISTS " + TABLE_NAME_GUVENLIK);

        db.execSQL(CREATE_TABLE_GUVENLIK);

        db.close();
    }
    //********************** GUVENLIK END **********************//

    //********************** PASSWORD **********************//
    public void deletePasswordByIPAndDoorPassword(DoorPassword doorPassword) {
        SQLiteDatabase db = this.getWritableDatabase();
        db.delete(TABLE_NAME_PASSWORD, PASSWORD_IP + " = ? AND " + PASSWORD_DOOR + " = ?", new String[]{doorPassword.getIp(), doorPassword.getDoor()});
        db.close();
    }

    public void deletePasswordByIPAndDoorRFID(DoorPassword doorPassword) {
        SQLiteDatabase db = this.getWritableDatabase();
        Log.d(Constants.LOG_TAG, "deletePasswordByIPAndDoorRFID ip=" + doorPassword.getIp() + " rfid=" + doorPassword.getRfid());
        db.delete(TABLE_NAME_PASSWORD, PASSWORD_IP + " = ? AND " + PASSWORD_RFID + " = ?", new String[]{doorPassword.getIp(), doorPassword.getRfid()});
        db.close();
    }

    public int insertPassword(DoorPassword doorPassword) {

        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(PASSWORD_IP, doorPassword.getIp());
        values.put(PASSWORD_DOOR, doorPassword.getDoor());
        values.put(PASSWORD_RFID, doorPassword.getRfid());
        values.put(PASSWORD_IS_ACTIVE, convertBooleanToInt(doorPassword.isActive()));
        values.put(PASSWORD_LABEL, doorPassword.getPasswordLabel());

        long newId = db.insert(TABLE_NAME_PASSWORD, null, values);
        db.close();

        Log.d(Constants.LOG_TAG, "password added=" + newId);

        return (int) newId;

    }

    public void updatePassword(DoorPassword doorPassword) {

        SQLiteDatabase db = this.getWritableDatabase();

        ContentValues values = new ContentValues();
        values.put(PASSWORD_IP, doorPassword.getIp());
        values.put(PASSWORD_DOOR, doorPassword.getDoor());
        values.put(PASSWORD_RFID, doorPassword.getRfid());
        values.put(PASSWORD_IS_ACTIVE, convertBooleanToInt(doorPassword.isActive()));
        values.put(PASSWORD_LABEL, doorPassword.getPasswordLabel());

        db.update(TABLE_NAME_PASSWORD, values, PASSWORD_ID + " = ?", new String[]{doorPassword.getId() + ""});
        db.close();
    }

    public ArrayList<DoorPassword> getPasswords(boolean isRFID) {

        SQLiteDatabase db = this.getReadableDatabase();

        String selectQuery = "";
        if (isRFID) {
            selectQuery = "SELECT * FROM " + TABLE_NAME_PASSWORD + " WHERE " + PASSWORD_RFID + " != ''";
        } else {
            selectQuery = "SELECT * FROM " + TABLE_NAME_PASSWORD + " WHERE " + PASSWORD_DOOR + " != ''";
        }

        Cursor cursor = db.rawQuery(selectQuery, null);

        ArrayList<DoorPassword> arrayList = new ArrayList<>();

        if (cursor.moveToFirst()) {
            do {

                DoorPassword doorPassword = new DoorPassword();

                doorPassword.setId(cursor.getInt(cursor.getColumnIndex(PASSWORD_ID)));
                doorPassword.setIp(cursor.getString(cursor.getColumnIndex(PASSWORD_IP)));
                doorPassword.setDoor(cursor.getString(cursor.getColumnIndex(PASSWORD_DOOR)));
                doorPassword.setRfid(cursor.getString(cursor.getColumnIndex(PASSWORD_RFID)));
                doorPassword.setActive(convertIntToBoolean(cursor.getInt(cursor.getColumnIndex(PASSWORD_IS_ACTIVE))));
                doorPassword.setPasswordLabel(cursor.getString(cursor.getColumnIndex(PASSWORD_LABEL)));

                arrayList.add(doorPassword);
            } while (cursor.moveToNext());
        }
        db.close();

        return arrayList;
    }

    public ArrayList<DoorPassword> getPasswordsByIP(String ip, boolean isForDoorPassword) {

        SQLiteDatabase db = this.getReadableDatabase();

        String selectQuery = "";
        Cursor cursor;

        if (isForDoorPassword) {
            selectQuery = "SELECT * FROM " + TABLE_NAME_PASSWORD + " WHERE " + PASSWORD_IP + " = ? AND " + PASSWORD_DOOR + " !=? ";
            cursor = db.rawQuery(selectQuery, new String[]{ip, ""});
        } else {
            selectQuery = "SELECT * FROM " + TABLE_NAME_PASSWORD + " WHERE " + PASSWORD_IP + " = ? AND " + PASSWORD_RFID + " !=? ";
            cursor = db.rawQuery(selectQuery, new String[]{ip, ""});
        }

        ArrayList<DoorPassword> arrayList = new ArrayList<>();

        if (cursor.moveToFirst()) {
            do {

                DoorPassword doorPassword = new DoorPassword();

                doorPassword.setId(cursor.getInt(cursor.getColumnIndex(PASSWORD_ID)));
                doorPassword.setIp(cursor.getString(cursor.getColumnIndex(PASSWORD_IP)));
                doorPassword.setDoor(cursor.getString(cursor.getColumnIndex(PASSWORD_DOOR)));
                doorPassword.setRfid(cursor.getString(cursor.getColumnIndex(PASSWORD_RFID)));
                doorPassword.setActive(convertIntToBoolean(cursor.getInt(cursor.getColumnIndex(PASSWORD_IS_ACTIVE))));
                doorPassword.setPasswordLabel(cursor.getString(cursor.getColumnIndex(PASSWORD_LABEL)));

                arrayList.add(doorPassword);
            } while (cursor.moveToNext());
        }
        db.close();

        return arrayList;
    }

    public ArrayList<DoorPassword> getPasswordsByIP(String ip) {

        SQLiteDatabase db = this.getReadableDatabase();

        String selectQuery = "SELECT * FROM " + TABLE_NAME_PASSWORD + " WHERE " + PASSWORD_IP + " = ?";
        Cursor cursor = db.rawQuery(selectQuery, new String[]{ip});

        ArrayList<DoorPassword> arrayList = new ArrayList<>();

        if (cursor.moveToFirst()) {
            do {

                DoorPassword doorPassword = new DoorPassword();

                doorPassword.setId(cursor.getInt(cursor.getColumnIndex(PASSWORD_ID)));
                doorPassword.setIp(cursor.getString(cursor.getColumnIndex(PASSWORD_IP)));
                doorPassword.setDoor(cursor.getString(cursor.getColumnIndex(PASSWORD_DOOR)));
                doorPassword.setRfid(cursor.getString(cursor.getColumnIndex(PASSWORD_RFID)));
                doorPassword.setActive(convertIntToBoolean(cursor.getInt(cursor.getColumnIndex(PASSWORD_IS_ACTIVE))));
                doorPassword.setPasswordLabel(cursor.getString(cursor.getColumnIndex(PASSWORD_LABEL)));

                arrayList.add(doorPassword);
            } while (cursor.moveToNext());
        }
        db.close();

        return arrayList;
    }

    public DoorPassword getPasswordByRFID(String rfid) {

        DoorPassword doorPassword = null;
        String selectQuery = "SELECT * FROM " + TABLE_NAME_PASSWORD + " WHERE " + PASSWORD_RFID + " LIKE '" + rfid + "'";

        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery(selectQuery, null);
        // Move to first row
        cursor.moveToFirst();
        if (cursor.getCount() > 0) {

            doorPassword = new DoorPassword();

            doorPassword.setId(cursor.getInt(cursor.getColumnIndex(PASSWORD_ID)));
            doorPassword.setIp(cursor.getString(cursor.getColumnIndex(PASSWORD_IP)));
            doorPassword.setDoor(cursor.getString(cursor.getColumnIndex(PASSWORD_DOOR)));
            doorPassword.setRfid(cursor.getString(cursor.getColumnIndex(PASSWORD_RFID)));
            doorPassword.setActive(convertIntToBoolean(cursor.getInt(cursor.getColumnIndex(PASSWORD_IS_ACTIVE))));
            doorPassword.setPasswordLabel(cursor.getString(cursor.getColumnIndex(PASSWORD_LABEL)));

        }
        cursor.close();
        db.close();

        return doorPassword;
    }

    public DoorPassword getPasswordByDoorPassword(String password) {

        DoorPassword doorPassword = null;
        String selectQuery = "SELECT * FROM " + TABLE_NAME_PASSWORD + " WHERE " + PASSWORD_DOOR + "='" + password + "'";

        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery(selectQuery, null);
        // Move to first row
        cursor.moveToFirst();
        if (cursor.getCount() > 0) {

            doorPassword = new DoorPassword();

            doorPassword.setId(cursor.getInt(cursor.getColumnIndex(PASSWORD_ID)));
            doorPassword.setIp(cursor.getString(cursor.getColumnIndex(PASSWORD_IP)));
            doorPassword.setDoor(cursor.getString(cursor.getColumnIndex(PASSWORD_DOOR)));
            doorPassword.setRfid(cursor.getString(cursor.getColumnIndex(PASSWORD_RFID)));
            doorPassword.setActive(convertIntToBoolean(cursor.getInt(cursor.getColumnIndex(PASSWORD_IS_ACTIVE))));
            doorPassword.setPasswordLabel(cursor.getString(cursor.getColumnIndex(PASSWORD_LABEL)));

        }
        cursor.close();
        db.close();

        return doorPassword;
    }

    public void deleteAllPasswords() {

        SQLiteDatabase db = this.getWritableDatabase();

        db.execSQL("DROP TABLE IF EXISTS " + TABLE_NAME_PASSWORD);

        db.execSQL(CREATE_TABLE_PASSWORD);

        db.close();
    }
    //********************** PASSWORD END **********************//

    //********************** DOOR LOG **********************//

    public void deleteDoorLog(DoorUnlockLog doorUnlockLog) {
        SQLiteDatabase db = this.getWritableDatabase();
        db.delete(TABLE_NAME_DOOR_LOG, DOOR_LOG_ID + " = ? ", new String[]{doorUnlockLog.getId() + ""});
        db.close();
    }

    public int insertDoorLog(DoorUnlockLog doorUnlockLog) {

        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(DOOR_LOG_RFID, doorUnlockLog.getRfid());
        values.put(DOOR_LOG_PASSWORD, doorUnlockLog.getDoorPassword());
        values.put(DOOR_LOG_USER_IP, doorUnlockLog.getPasswordOwnerIP());
        values.put(DOOR_LOG_DATETIME, doorUnlockLog.getDatetime());
        values.put(DOOR_LOG_UNLOCK_TYPE, doorUnlockLog.getUnlockType());
        values.put(DOOR_LOG_LABEL, doorUnlockLog.getPasswordLabel());

        long newId = db.insert(TABLE_NAME_DOOR_LOG, null, values);
        db.close();

        Log.d(Constants.LOG_TAG, "insertDoorLog added=" + newId);

        return (int) newId;

    }

    public void updateDoorLog(DoorUnlockLog doorUnlockLog) {

        SQLiteDatabase db = this.getWritableDatabase();

        ContentValues values = new ContentValues();
        values.put(DOOR_LOG_RFID, doorUnlockLog.getRfid());
        values.put(DOOR_LOG_PASSWORD, doorUnlockLog.getDoorPassword());
        values.put(DOOR_LOG_USER_IP, doorUnlockLog.getPasswordOwnerIP());
        values.put(DOOR_LOG_DATETIME, doorUnlockLog.getDatetime());
        values.put(DOOR_LOG_UNLOCK_TYPE, doorUnlockLog.getUnlockType());
        values.put(DOOR_LOG_LABEL, doorUnlockLog.getPasswordLabel());

        db.update(TABLE_NAME_DOOR_LOG, values, DOOR_LOG_ID + " = ?", new String[]{doorUnlockLog.getId() + ""});
        db.close();
    }

    public ArrayList<DoorUnlockLog> getDoorLogs() {

        SQLiteDatabase db = this.getReadableDatabase();

        String selectQuery = "SELECT * FROM " + TABLE_NAME_DOOR_LOG;
        Cursor cursor = db.rawQuery(selectQuery, null);

        ArrayList<DoorUnlockLog> arrayList = new ArrayList<>();

        if (cursor.moveToFirst()) {
            do {

                DoorUnlockLog doorUnlockLog = new DoorUnlockLog();

                doorUnlockLog.setId(cursor.getInt(cursor.getColumnIndex(DOOR_LOG_ID)));
                doorUnlockLog.setRfid(cursor.getString(cursor.getColumnIndex(DOOR_LOG_RFID)));
                doorUnlockLog.setDoorPassword(cursor.getString(cursor.getColumnIndex(DOOR_LOG_PASSWORD)));
                doorUnlockLog.setPasswordOwnerIP(cursor.getString(cursor.getColumnIndex(DOOR_LOG_USER_IP)));
                doorUnlockLog.setDatetime(cursor.getString(cursor.getColumnIndex(DOOR_LOG_DATETIME)));
                doorUnlockLog.setUnlockType(cursor.getInt(cursor.getColumnIndex(DOOR_LOG_UNLOCK_TYPE)));
                doorUnlockLog.setPasswordLabel(cursor.getString(cursor.getColumnIndex(DOOR_LOG_LABEL)));

                arrayList.add(doorUnlockLog);
            } while (cursor.moveToNext());
        }
        db.close();

        return arrayList;
    }

    public ArrayList<DoorUnlockLog> getDoorLogsByIP(String ip) {

        SQLiteDatabase db = this.getReadableDatabase();

        String selectQuery = "SELECT * FROM " + TABLE_NAME_DOOR_LOG + " WHERE " + DOOR_LOG_USER_IP + " = ? ";
        Cursor cursor = db.rawQuery(selectQuery, new String[]{ip, ""});

        ArrayList<DoorUnlockLog> arrayList = new ArrayList<>();

        if (cursor.moveToFirst()) {
            do {

                DoorUnlockLog doorUnlockLog = new DoorUnlockLog();

                doorUnlockLog.setId(cursor.getInt(cursor.getColumnIndex(DOOR_LOG_ID)));
                doorUnlockLog.setRfid(cursor.getString(cursor.getColumnIndex(DOOR_LOG_RFID)));
                doorUnlockLog.setDoorPassword(cursor.getString(cursor.getColumnIndex(DOOR_LOG_PASSWORD)));
                doorUnlockLog.setPasswordOwnerIP(cursor.getString(cursor.getColumnIndex(DOOR_LOG_USER_IP)));
                doorUnlockLog.setDatetime(cursor.getString(cursor.getColumnIndex(DOOR_LOG_DATETIME)));
                doorUnlockLog.setUnlockType(cursor.getInt(cursor.getColumnIndex(DOOR_LOG_UNLOCK_TYPE)));
                doorUnlockLog.setPasswordLabel(cursor.getString(cursor.getColumnIndex(DOOR_LOG_LABEL)));

                arrayList.add(doorUnlockLog);
            } while (cursor.moveToNext());
        }
        db.close();

        return arrayList;
    }

    public DoorUnlockLog getDoorLogsByPassword(String doorPassword) {

        DoorUnlockLog doorUnlockLog = null;
        String selectQuery = "SELECT * FROM " + TABLE_NAME_DOOR_LOG + " WHERE " + DOOR_LOG_PASSWORD + "='" + doorPassword + "'";

        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery(selectQuery, null);
        // Move to first row
        cursor.moveToFirst();
        if (cursor.getCount() > 0) {

            doorUnlockLog = new DoorUnlockLog();

            doorUnlockLog.setId(cursor.getInt(cursor.getColumnIndex(DOOR_LOG_ID)));
            doorUnlockLog.setRfid(cursor.getString(cursor.getColumnIndex(DOOR_LOG_RFID)));
            doorUnlockLog.setDoorPassword(cursor.getString(cursor.getColumnIndex(DOOR_LOG_PASSWORD)));
            doorUnlockLog.setPasswordOwnerIP(cursor.getString(cursor.getColumnIndex(DOOR_LOG_USER_IP)));
            doorUnlockLog.setDatetime(cursor.getString(cursor.getColumnIndex(DOOR_LOG_DATETIME)));
            doorUnlockLog.setUnlockType(cursor.getInt(cursor.getColumnIndex(DOOR_LOG_UNLOCK_TYPE)));
            doorUnlockLog.setPasswordLabel(cursor.getString(cursor.getColumnIndex(DOOR_LOG_LABEL)));

        }
        cursor.close();
        db.close();

        return doorUnlockLog;
    }


    public DoorUnlockLog getDoorLogsByEFID(String doorRFID) {

        DoorUnlockLog doorUnlockLog = null;
        String selectQuery = "SELECT * FROM " + TABLE_NAME_DOOR_LOG + " WHERE " + DOOR_LOG_RFID + "='" + doorRFID + "'";

        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery(selectQuery, null);
        // Move to first row
        cursor.moveToFirst();
        if (cursor.getCount() > 0) {

            doorUnlockLog = new DoorUnlockLog();

            doorUnlockLog.setId(cursor.getInt(cursor.getColumnIndex(DOOR_LOG_ID)));
            doorUnlockLog.setRfid(cursor.getString(cursor.getColumnIndex(DOOR_LOG_RFID)));
            doorUnlockLog.setDoorPassword(cursor.getString(cursor.getColumnIndex(DOOR_LOG_PASSWORD)));
            doorUnlockLog.setPasswordOwnerIP(cursor.getString(cursor.getColumnIndex(DOOR_LOG_USER_IP)));
            doorUnlockLog.setDatetime(cursor.getString(cursor.getColumnIndex(DOOR_LOG_DATETIME)));
            doorUnlockLog.setUnlockType(cursor.getInt(cursor.getColumnIndex(DOOR_LOG_UNLOCK_TYPE)));
            doorUnlockLog.setPasswordLabel(cursor.getString(cursor.getColumnIndex(DOOR_LOG_LABEL)));

        }
        cursor.close();
        db.close();

        return doorUnlockLog;
    }

    public DoorUnlockLog getDoorLogsByType(int unlockType) {

        DoorUnlockLog doorUnlockLog = null;
        String selectQuery = "SELECT * FROM " + TABLE_NAME_DOOR_LOG + " WHERE " + DOOR_LOG_UNLOCK_TYPE + "='" + unlockType + "'";

        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery(selectQuery, null);
        // Move to first row
        cursor.moveToFirst();
        if (cursor.getCount() > 0) {

            doorUnlockLog = new DoorUnlockLog();

            doorUnlockLog.setId(cursor.getInt(cursor.getColumnIndex(DOOR_LOG_ID)));
            doorUnlockLog.setRfid(cursor.getString(cursor.getColumnIndex(DOOR_LOG_RFID)));
            doorUnlockLog.setDoorPassword(cursor.getString(cursor.getColumnIndex(DOOR_LOG_PASSWORD)));
            doorUnlockLog.setPasswordOwnerIP(cursor.getString(cursor.getColumnIndex(DOOR_LOG_USER_IP)));
            doorUnlockLog.setDatetime(cursor.getString(cursor.getColumnIndex(DOOR_LOG_DATETIME)));
            doorUnlockLog.setUnlockType(cursor.getInt(cursor.getColumnIndex(DOOR_LOG_UNLOCK_TYPE)));
            doorUnlockLog.setPasswordLabel(cursor.getString(cursor.getColumnIndex(DOOR_LOG_LABEL)));

        }
        cursor.close();
        db.close();

        return doorUnlockLog;
    }

    public void deleteAllDoorLogs() {

        SQLiteDatabase db = this.getWritableDatabase();

        db.execSQL("DROP TABLE IF EXISTS " + TABLE_NAME_DOOR_LOG);

        db.execSQL(CREATE_TABLE_DOOR_LOG);

        db.close();
    }
    //********************** DOOR LOG END **********************//

    //********************** IP CAMERA **********************//
    public void deleteIPCamera(IPCamera ipCamera) { //id si belli olan row u silmek için
        SQLiteDatabase db = this.getWritableDatabase();
        db.delete(TABLE_NAME_IP_CAMERA, IP_CAMERA_IP + " = ?", new String[]{ipCamera.getIp() + ""});
        db.close();
    }

    public int insertIPCamera(IPCamera ipCamera) {

        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();

        values.put(IP_CAMERA_IP, ipCamera.getIp());
        values.put(IP_CAMERA_NAME, ipCamera.getName());
        values.put(IP_CAMERA_PORT, ipCamera.getPort());
        values.put(IP_CAMERA_SUFFIX, ipCamera.getStreamSuffix());
        values.put(IP_CAMERA_WORK_FOR_IP, ipCamera.getWorkForIP());
        values.put(IP_CAMERA_LAST_SS, ipCamera.getLastSSFilePath());

        long newId = db.insert(TABLE_NAME_IP_CAMERA, null, values);
        db.close();

        return (int) newId;

    }

    public void updateIPCamera(IPCamera ipCamera) {

        SQLiteDatabase db = this.getWritableDatabase();

        ContentValues values = new ContentValues();
        values.put(IP_CAMERA_IP, ipCamera.getIp());
        values.put(IP_CAMERA_NAME, ipCamera.getName());
        values.put(IP_CAMERA_PORT, ipCamera.getPort());
        values.put(IP_CAMERA_SUFFIX, ipCamera.getStreamSuffix());
        values.put(IP_CAMERA_WORK_FOR_IP, ipCamera.getWorkForIP());
        values.put(IP_CAMERA_LAST_SS, ipCamera.getLastSSFilePath());

        db.update(TABLE_NAME_IP_CAMERA, values, IP_CAMERA_IP + " = ?", new String[]{(ipCamera.getIp() + "")});
        db.close();
    }

    public ArrayList<IPCamera> getIPCameras() {

        SQLiteDatabase db = this.getReadableDatabase();

        String selectQuery = "SELECT * FROM " + TABLE_NAME_IP_CAMERA;
        Cursor cursor = db.rawQuery(selectQuery, null);

        ArrayList<IPCamera> arrayList = new ArrayList<>();

        if (cursor.moveToFirst()) {
            do {

                IPCamera ipCamera = new IPCamera();

                ipCamera.setId(cursor.getInt(cursor.getColumnIndex(IP_CAMERA_ID)));
                ipCamera.setIp(cursor.getString(cursor.getColumnIndex(IP_CAMERA_IP)));
                ipCamera.setName(cursor.getString(cursor.getColumnIndex(IP_CAMERA_NAME)));
                ipCamera.setPort(cursor.getInt(cursor.getColumnIndex(IP_CAMERA_PORT)));
                ipCamera.setStreamSuffix(cursor.getString(cursor.getColumnIndex(IP_CAMERA_SUFFIX)));
                ipCamera.setWorkForIP(cursor.getString(cursor.getColumnIndex(IP_CAMERA_WORK_FOR_IP)));
                ipCamera.setLastSSFilePath(cursor.getString(cursor.getColumnIndex(IP_CAMERA_LAST_SS)));

                arrayList.add(ipCamera);
            } while (cursor.moveToNext());
        }
        db.close();

        return arrayList;
    }

    public IPCamera getCameraByIP(String ip) {

        IPCamera ipCamera = null;
        String selectQuery = "SELECT * FROM " + TABLE_NAME_IP_CAMERA + " WHERE " + IP_CAMERA_IP + "='" + ip + "'";

        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery(selectQuery, null);
        // Move to first row
        cursor.moveToFirst();
        if (cursor.getCount() > 0) {

            ipCamera = new IPCamera();

            ipCamera.setId(cursor.getInt(cursor.getColumnIndex(IP_CAMERA_ID)));
            ipCamera.setIp(cursor.getString(cursor.getColumnIndex(IP_CAMERA_IP)));
            ipCamera.setName(cursor.getString(cursor.getColumnIndex(IP_CAMERA_NAME)));
            ipCamera.setPort(cursor.getInt(cursor.getColumnIndex(IP_CAMERA_PORT)));
            ipCamera.setStreamSuffix(cursor.getString(cursor.getColumnIndex(IP_CAMERA_SUFFIX)));
            ipCamera.setWorkForIP(cursor.getString(cursor.getColumnIndex(IP_CAMERA_WORK_FOR_IP)));
            ipCamera.setLastSSFilePath(cursor.getString(cursor.getColumnIndex(IP_CAMERA_LAST_SS)));

        }

        cursor.close();
        db.close();

        return ipCamera;
    }

    public void deleteAllIPCameras() {

        SQLiteDatabase db = this.getWritableDatabase();

        db.execSQL("DROP TABLE IF EXISTS " + TABLE_NAME_IP_CAMERA);

        db.execSQL(CREATE_TABLE_IP_CAMERA);

        db.close();
    }
    //********************** IP CAMERA END **********************//


    //********************** ZIL DATE TIME **********************//

    public void insertZilDateTime(DateTimeClass dateTimeClass) {


        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(ZIL_HOUR, dateTimeClass.getHour());
        values.put(ZIL_MINUTE, dateTimeClass.getMinute());
        values.put(ZIL_DAY_OF_MONTH, dateTimeClass.getDayofMonth());
        values.put(ZIL_MONTH, dateTimeClass.getMonth());
        values.put(ZIL_YEAR, dateTimeClass.getYear());


        db.insert(TABLE_NAME_ZIL_DATE_TIME, null, values);
        db.close();

    }

    public void updateZilDateTime(DateTimeClass dateTimeClass) {

        SQLiteDatabase db = this.getWritableDatabase();

        ContentValues values = new ContentValues();
        values.put(ZIL_HOUR, dateTimeClass.getHour());
        values.put(ZIL_MINUTE, dateTimeClass.getMinute());
        values.put(ZIL_DAY_OF_MONTH, dateTimeClass.getDayofMonth());
        values.put(ZIL_MONTH, dateTimeClass.getMonth());
        values.put(ZIL_YEAR, dateTimeClass.getYear());

        db.update(TABLE_NAME_ZIL_DATE_TIME, values, ZIL_HOUR_ID + " = ?", new String[]{dateTimeClass.getId() + ""});
        db.close();
    }

    public DateTimeClass getZilDateTimeById(int id) {

        DateTimeClass dateTimeClass = null;
        String selectQuery = "SELECT * FROM " + TABLE_NAME_ZIL_DATE_TIME + " WHERE " + ZIL_HOUR_ID + "='" + id + "'";

        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery(selectQuery, null);
        // Move to first row
        cursor.moveToFirst();
        if (cursor.getCount() > 0) {

            dateTimeClass = new DateTimeClass();

            dateTimeClass.setId(cursor.getInt(cursor.getColumnIndex(ZIL_HOUR_ID)));
            dateTimeClass.setHour(cursor.getInt(cursor.getColumnIndex(ZIL_HOUR)));
            dateTimeClass.setMinute(cursor.getInt(cursor.getColumnIndex(ZIL_MINUTE)));
            dateTimeClass.setDayofMonth(cursor.getInt(cursor.getColumnIndex(ZIL_DAY_OF_MONTH)));
            dateTimeClass.setMonth(cursor.getInt(cursor.getColumnIndex(ZIL_MONTH)));
            dateTimeClass.setYear(cursor.getInt(cursor.getColumnIndex(ZIL_YEAR)));

        }

        cursor.close();
        db.close();

        return dateTimeClass;
    }

    public void createZilDateTime() {
        SQLiteDatabase db = this.getWritableDatabase();
        db.execSQL(CREATE_TABLE_ZIL_DATE_TIME);
        db.close();

    }


    //********************** ZIL DATE TIME END **********************//

    public void deleteEverything() {

        SQLiteDatabase db = this.getWritableDatabase();

        db.execSQL("DROP TABLE IF EXISTS " + TABLE_NAME_DAIRE);
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_NAME_ZIL);
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_NAME_ZIL_SITE);
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_NAME_GUVENLIK);
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_NAME_PASSWORD);
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_NAME_DOOR_LOG);
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_NAME_IP_CAMERA);
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_NAME_ZIL_DATE_TIME);


        db.execSQL(CREATE_TABLE_DAIRE);
        db.execSQL(CREATE_TABLE_ZIL);
        db.execSQL(CREATE_TABLE_ZIL_SITE);
        db.execSQL(CREATE_TABLE_GUVENLIK);
        db.execSQL(CREATE_TABLE_PASSWORD);
        db.execSQL(CREATE_TABLE_DOOR_LOG);
        db.execSQL(CREATE_TABLE_IP_CAMERA);
        db.execSQL(CREATE_TABLE_ZIL_DATE_TIME);

        db.close();
    }

    public int getRowCount(String tableName) {

        String countQuery = "SELECT  * FROM " + tableName;
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery(countQuery, null);
        int rowCount = cursor.getCount();
        db.close();
        cursor.close();
        // return row count
        return rowCount;
    }

    private int convertBooleanToInt(boolean value) {

        if (value) {
            return 1;
        }
        return 0;
    }

    private boolean convertIntToBoolean(int value) {
        if (value == 1) {
            return true;
        }

        return false;
    }

    String CREATE_TABLE_DAIRE = "CREATE TABLE " + TABLE_NAME_DAIRE + "("
            + DAIRE_ID + " INTEGER PRIMARY KEY AUTOINCREMENT,"
            + DAIRE_ISIM + " TEXT,"
            + DAIRE_SOYISIM + " TEXT,"
            + DAIRE_IP + " TEXT,"
            + DAIRE_BLOK + " INTEGER,"
            + DAIRE_IS_GOREVLI + " INTEGER,"
            + DAIRE_GOREVLI_OLDUGU_BINALAR + " TEXT,"
            + DAIRE_KAT_NO + " TEXT,"
            + DAIRE_REAL_DAIRE_NO + " INTEGER,"
            + DAIRE_DAIRE_NO + " INTEGER" + ")";

    String CREATE_TABLE_ZIL = "CREATE TABLE " + TABLE_NAME_ZIL + "("
            + ZIL_ID + " INTEGER PRIMARY KEY AUTOINCREMENT,"
            + ZIL_IP + " TEXT,"
            + ZIL_DEVICE_NAME + " TEXT,"
            + ZIL_BLOK + " INTEGER,"
            + ZIL_KAPI_NO + " INTEGER" + ")";

    String CREATE_TABLE_ZIL_SITE = "CREATE TABLE " + TABLE_NAME_ZIL_SITE + "("
            + ZIL_SITE_ID + " INTEGER PRIMARY KEY AUTOINCREMENT,"
            + ZIL_SITE_IP + " TEXT,"
            + ZIL_SITE_DEVICE_NAME + " TEXT,"
            + ZIL_SITE_KAPI_NO + " INTEGER" + ")";

    String CREATE_TABLE_GUVENLIK = "CREATE TABLE " + TABLE_NAME_GUVENLIK + "("
            + GUVENLIK_ID + " INTEGER PRIMARY KEY AUTOINCREMENT,"
            + GUVENLIK_NAME + " TEXT,"
            + GUVENLIK_IP + " TEXT,"
            + GUVENLIK_GUVENLIK_NO + " INTEGER" + ")";

    String CREATE_TABLE_PASSWORD = "CREATE TABLE " + TABLE_NAME_PASSWORD + "("
            + PASSWORD_ID + " INTEGER PRIMARY KEY AUTOINCREMENT,"
            + PASSWORD_IP + " TEXT,"
            + PASSWORD_DOOR + " TEXT,"
            + PASSWORD_RFID + " TEXT,"
            + PASSWORD_LABEL + " TEXT,"
            + PASSWORD_IS_ACTIVE + " INTEGER" + ")";

    String CREATE_TABLE_DOOR_LOG = "CREATE TABLE " + TABLE_NAME_DOOR_LOG + "("
            + DOOR_LOG_ID + " INTEGER PRIMARY KEY AUTOINCREMENT,"
            + DOOR_LOG_RFID + " TEXT,"
            + DOOR_LOG_PASSWORD + " TEXT,"
            + DOOR_LOG_USER_IP + " TEXT,"
            + DOOR_LOG_DATETIME + " TEXT,"
            + DOOR_LOG_LABEL + " TEXT,"
            + DOOR_LOG_UNLOCK_TYPE + " INTEGER" + ")";

    String CREATE_TABLE_IP_CAMERA = "CREATE TABLE " + TABLE_NAME_IP_CAMERA + "("
            + IP_CAMERA_ID + " INTEGER PRIMARY KEY AUTOINCREMENT,"
            + IP_CAMERA_NAME + " TEXT,"
            + IP_CAMERA_IP + " TEXT,"
            + IP_CAMERA_PORT + " INTEGER,"
            + IP_CAMERA_SUFFIX + " TEXT,"
            + IP_CAMERA_WORK_FOR_IP + " TEXT,"
            + IP_CAMERA_LAST_SS + " TEXT" + ")";


    String CREATE_TABLE_ZIL_DATE_TIME = "CREATE TABLE " + TABLE_NAME_ZIL_DATE_TIME + "("
            + ZIL_HOUR_ID + " INTEGER PRIMARY KEY AUTOINCREMENT,"
            + ZIL_HOUR + " INTEGER,"
            + ZIL_MINUTE + " INTEGER,"
            + ZIL_DAY_OF_MONTH + " INTEGER,"
            + ZIL_MONTH + " INTEGER,"
            + ZIL_YEAR + " INTEGER" + ")";


}
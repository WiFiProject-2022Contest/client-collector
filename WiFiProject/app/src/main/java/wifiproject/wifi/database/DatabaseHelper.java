package wifilocation.wifi.database;

import android.annotation.SuppressLint;
import android.app.ProgressDialog;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.os.AsyncTask;
import android.util.Log;
import android.widget.Toast;

import androidx.annotation.Nullable;

import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import wifilocation.wifi.MainActivity;
import wifilocation.wifi.barcode.Barcode;
import wifilocation.wifi.estimate.EstimatedResult;
import wifilocation.wifi.model.WiFiItem;
import wifilocation.wifi.serverconnection.RetrofitAPI;
import wifilocation.wifi.serverconnection.RetrofitClient;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

/**
 * example)
 * DatabaseHelper dbHelper = new DatabaseHelper(context);
 * dbHelper.insertIntoWiFiInfo(parameters);
 * List<WiFiItem> items = dbHelper.searchFromWiFiInfo(parameters);
 */
public class DatabaseHelper extends SQLiteOpenHelper {
    Context context;
    public static final String DBNAME = "wifilocation1.db";
    public static int VERSION = 1;

    public static final String TABLE_WIFIINFO = "wifiinfo";
    public static final String POS_X = "pos_x";
    public static final String POS_Y = "pos_y";
    public static final String SSID = "SSID";
    public static final String BSSID = "BSSID";
    public static final String FREQUENCY = "frequency";
    public static final String LEVEL = "level";
    public static final String DATE = "date";
    public static final String UUID = "uuid";
    public static final String BUILDING = "building";
    public static final String METHOD = "method";
    public static final String NEW = "new";

    public static final String TABLE_FINGERPRINT = "fingerprint";
    // POS_X
    // POS_Y
    // UUID
    // DATE
    public static final String EST_X = "est_x";
    public static final String EST_Y = "est_y";
    public static final String K = "k";
    public static final String THRESHOLD = "threshold";
    // BUILDING
    // SSID
    public static final String ALGORITHM_VERSION = "algorithmVersion";
    // METHOD
    // NEW

    public static final String TABLE_BARCODE = "barcode";
    public static final String BARCODE_SERIAL = "barcode_serial";
    // BUILDING
    // POS_X
    // POS_Y
    // DATE
    // NEW

    public DatabaseHelper(@Nullable Context context) {
        super(context, DBNAME, null, VERSION);
        this.context = context;
    }

    @Override
    public void onCreate(SQLiteDatabase sqLiteDatabase) {
        // wifiinfo ????????? ??????
        sqLiteDatabase.execSQL("create table if not exists " + TABLE_WIFIINFO + " (" +
                "id integer PRIMARY KEY autoincrement, " +
                POS_X + " real, " +
                POS_Y + " real, " +
                SSID + " text, " +
                BSSID + " text, " +
                FREQUENCY + " integer, " +
                LEVEL + " integer, " +
                DATE + " integer, " +
                UUID + " text, " +
                BUILDING + " text, " +
                METHOD + " text, " +
                NEW + " integer default 0)");

        // fingerprint ????????? ??????
        sqLiteDatabase.execSQL("create table if not exists " + TABLE_FINGERPRINT + " (" +
                "id integer PRIMARY KEY autoincrement, " +
                POS_X + " real, " +
                POS_Y + " real, " +
                UUID + " text, " +
                DATE + " integer, " +
                EST_X + " real, " +
                EST_Y + " real, " +
                K + " integer, " +
                THRESHOLD + " integer, " +
                BUILDING + " text, " +
                SSID + " text, " +
                ALGORITHM_VERSION + " integer, " +
                METHOD + " text, " +
                NEW + " integer default 0)");

        sqLiteDatabase.execSQL("create table if not exists " + TABLE_BARCODE + " (" +
                "id integer primary key autoincrement, " +
                BARCODE_SERIAL + " text, " +
                BUILDING + " text, " +
                POS_X + " real, " +
                POS_Y + " real, " +
                DATE + " date, " +
                NEW + " integer default 0)");
    }

    @Override
    public void onOpen(SQLiteDatabase db) {
        super.onOpen(db);
    }

    @Override
    public void onUpgrade(SQLiteDatabase sqLiteDatabase, int oldVersion, int newVersion) {
        if (newVersion > 1) {
            sqLiteDatabase.execSQL("DROP TABLE IF EXISTS " + TABLE_WIFIINFO);
            sqLiteDatabase.execSQL("DROP TABLE IF EXISTS " + TABLE_FINGERPRINT);
            sqLiteDatabase.execSQL("DROP TABLE IF EXISTS " + TABLE_BARCODE);
            onCreate(sqLiteDatabase);
        }
    }

    /**
     * wifiinfo ???????????? ????????? ??????
     *
     * @param items ????????? WiFiItem ??????, DB??? ??????
     * @param _new  ????????? ??????????????? 1, ???????????? 0 ??????
     */
    public void insertIntoWiFiInfo(List<WiFiItem> items, int _new) {
        if (items.size() == 0) {
            return;
        }
        SQLiteDatabase db = getWritableDatabase();
        StringBuilder sql = new StringBuilder("insert into " + TABLE_WIFIINFO + String.format(" (%s, %s, %s, %s, %s, %s, %s, %s, %s, %s, %s) ", POS_X, POS_Y, SSID, BSSID, FREQUENCY, LEVEL, DATE, UUID, BUILDING, METHOD, NEW) + " values ");
        int sqlLength = sql.length();
        for (int i = 1; i <= items.size(); i++) {
            WiFiItem item = items.get(i - 1);
            sql.append(String.format(" (%f, %f, '%s', '%s', %d, %d, %d, '%s', '%s', '%s', %d), ",
                    item.getX(), item.getY(), item.getSSID().replace("'", "''"), item.getBSSID(), item.getFrequency(), item.getRSSI(), item.getDate().getTime(), item.getUuid(), item.getBuilding(), item.getMethod(), _new));

            if (i % 500 == 0) {
                try {
                    db.execSQL(sql.substring(0, sql.length() - 2));
                } catch (Exception e) {
                    e.printStackTrace();
                } finally {
                    sql.setLength(sqlLength);
                }
            }
        }
        if (items.size() % 500 != 0) {
            db.execSQL(sql.substring(0, sql.length() - 2));
        }
    }

    /**
     * wifiinfo ?????????????????? ????????? ??????
     *
     * @param building building??? ???????????? row?????? ??????
     * @param ssid     ssid??? ???????????? row?????? ??????
     * @param x        x - 1 < pos_x < x + 1 ??? row?????? ??????
     * @param y        y - 1 < pos_y < y + 1 ??? row?????? ??????, x??? y ?????? null ?????? ??? ????????? ?????? ??????
     * @param from     from ????????? ????????? row?????? ??????, null ?????? ??? ?????? ??????
     * @param to       to ????????? ????????? row?????? ??????, null ?????? ??? ?????? ??????
     * @param _new     ???????????? ?????? ?????? DB?????? ?????? ???????????? ??????????????? null??? ?????? ??? ??????
     * @return sql??? ?????? ????????? ?????? row?????? List<WiFiItem> ?????? ???????????? ??????
     */
    @SuppressLint("Range")
    public List<WiFiItem> searchFromWiFiInfo(String building, String ssid, Float x, Float y, String from, String to, Integer _new) {
        StringBuilder sql = new StringBuilder("select " + String.format("%s, %s, %s, %s, %s, %s, %s, %s, %s, %s", POS_X, POS_Y, SSID, BSSID, LEVEL, FREQUENCY, UUID, BUILDING, METHOD, DATE) + " from " + TABLE_WIFIINFO);
        List<String> conditions = new ArrayList<String>();
        // ?????? ?????? ??????
        if (building != null) {
            conditions.add(String.format(" (%s = '%s') ", BUILDING, building));
        }
        // SSID ?????? ??????
        if (ssid != null) {
            conditions.add(String.format(" (%s = '%s') ", SSID, ssid));
        }
        // ?????? ?????? ??????
        if (from != null || to != null) {
            try {
                SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMdd");
                long timestampFrom, timestampTo;
                timestampFrom = sdf.parse(from == null ? "20020202" : from).getTime();
                timestampTo = sdf.parse(to == null ? "20300303" : to).getTime();
                conditions.add(String.format(" (%s between %d and %d) ", DATE, timestampFrom, timestampTo));
            } catch (ParseException e) {
                e.printStackTrace();
            }
        }
        // ?????? ?????? ??????
        if (x != null && y != null) {
            conditions.add(String.format(" ((%s between %f and %f) and (%s between %f and %f)) ", POS_X, x - 1, x + 1, POS_Y, y - 1, y + 1));
        }
        // ???????????? ?????? ????????? ??????????????? ?????? ??????
        if (_new != null) {
            conditions.add(String.format(" (%s = 1) ", NEW));
        }
        if (conditions.size() != 0) {
            sql.append(" where ");
        }
        for (int i = 0; i < conditions.size(); i++) {
            sql.append(conditions.get(i));
            if (i != conditions.size() - 1) {
                sql.append(" and ");
            }
        }

        SQLiteDatabase db = getReadableDatabase();
        Cursor cursor = db.rawQuery(sql.toString(), null);

        List<WiFiItem> result = new ArrayList<WiFiItem>();

        int count = cursor.getCount();
        for (int i = 0; i < count; i++) {
            cursor.moveToNext();
            result.add(new WiFiItem(cursor.getFloat(cursor.getColumnIndex(POS_X)),
                    cursor.getFloat(cursor.getColumnIndex(POS_Y)),
                    cursor.getString(cursor.getColumnIndex(SSID)),
                    cursor.getString(cursor.getColumnIndex(BSSID)),
                    cursor.getInt(cursor.getColumnIndex(LEVEL)),
                    cursor.getInt(cursor.getColumnIndex(FREQUENCY)),
                    cursor.getString(cursor.getColumnIndex(UUID)),
                    cursor.getString(cursor.getColumnIndex(BUILDING)),
                    cursor.getString(cursor.getColumnIndex(METHOD)),
                    cursor.getLong(cursor.getColumnIndex(DATE))));
        }
        return result;
    }

    public void logAllWiFiInfo() {
        String sql = "select " + String.format("%s, %s, %s, %s, %s, %s, %s, %s, %s", POS_X, POS_Y, SSID, BSSID, LEVEL, FREQUENCY, UUID, BUILDING, METHOD) + " from " + TABLE_WIFIINFO;
        SQLiteDatabase readableDatabase = getReadableDatabase();
        Cursor cursor = readableDatabase.rawQuery(sql, null);
        int count = cursor.getCount();
        for (int i = 0; i < count; i++) {
            cursor.moveToNext();
            WiFiItem wiFiItem = new WiFiItem(cursor.getFloat(0),
                    cursor.getFloat(1),
                    cursor.getString(2),
                    cursor.getString(3),
                    cursor.getInt(4),
                    cursor.getInt(5),
                    cursor.getString(6),
                    cursor.getString(7),
                    cursor.getString(8));
            Log.d(getClass().getName(), wiFiItem.toString());
        }
    }

    /**
     * fingerprint ???????????? ????????? ??????
     *
     * @param items ????????? ?????? ?????? ????????? ??????
     * @param _new  ????????? ??????????????? 1, ???????????? 0 ??????
     */
    public void insertIntoFingerprint(List<EstimatedResult> items, int _new) {
        if (items.size() == 0) {
            return;
        }
        SQLiteDatabase db = getWritableDatabase();
        StringBuilder sql = new StringBuilder("insert into " + TABLE_FINGERPRINT + String.format(" (%s, %s, %s, %s, %s, %s, %s, %s, %s, %s, %s, %s, %s)", POS_X, POS_Y, UUID, DATE, EST_X, EST_Y, K, THRESHOLD, BUILDING, SSID, ALGORITHM_VERSION, METHOD, NEW) + " values ");
        int sqlLength = sql.length();
        for (int i = 1; i <= items.size(); i++) {
            EstimatedResult item = items.get(i - 1);
            sql.append(String.format(" (%f, %f, '%s', %d, %f, %f, %d, %d, '%s', '%s', %d, '%s', %d), ",
                    item.getPositionRealX(), item.getPositionRealY(), item.getUuid(), item.getDate().getTime(), item.getPositionEstimatedX(), item.getPositionEstimatedY(),
                    item.getK(), item.getThreshold(), item.getBuilding(), item.getSsid().replace("'", "''"), item.getAlgorithmVersion(), item.getMethod(), _new));

            if (i % 500 == 0) {
                try {
                    db.execSQL(sql.substring(0, sql.length() - 2));
                } catch (Exception e) {
                    e.printStackTrace();
                } finally {
                    sql.setLength(sqlLength);
                }
            }
        }
        if (items.size() % 500 != 0) {
            db.execSQL(sql.substring(0, sql.length() - 2));
        }
    }

    /**
     * fingerprint ??????????????? ????????? ??????
     *
     * @param _new ???????????? ?????? ?????? DB?????? ?????? ???????????? ??????????????? null??? ?????? ??? ??????
     * @return sql??? ?????? ????????? ?????? row?????? List<EstimateResult> ??? ????????? ??????
     */
    @SuppressLint("Range")
    public List<EstimatedResult> searchFromFingerprint(Integer _new) {
        StringBuilder sql = new StringBuilder("select " + String.format(" %s, %s, %s, %s, %s, %s, %s, %s, %s, %s, %s, %s ", POS_X, POS_Y, UUID, DATE, EST_X, EST_Y, K, THRESHOLD, BUILDING, SSID, ALGORITHM_VERSION, METHOD) +
                " from " + TABLE_FINGERPRINT);
        List<String> conditions = new ArrayList<String>();
        // ???????????? ?????? ???????????? ??????????????? ?????? ?????? ??????
        if (_new != null) {
            conditions.add(String.format(" (%s = 1) ", NEW));
        }
        if (conditions.size() != 0) {
            sql.append(" where ");
        }
        for (int i = 0; i < conditions.size(); i++) {
            sql.append(conditions.get(i));
            if (i != conditions.size() - 1) {
                sql.append(" and ");
            }
        }


        SQLiteDatabase db = getReadableDatabase();
        Cursor cursor = db.rawQuery(sql.toString(), null);

        List<EstimatedResult> result = new ArrayList<EstimatedResult>();
        int count = cursor.getCount();
        for (int i = 0; i < count; i++) {
            cursor.moveToNext();
            result.add(new EstimatedResult(cursor.getString(cursor.getColumnIndex(BUILDING)),
                    cursor.getString(cursor.getColumnIndex(SSID)),
                    cursor.getFloat(cursor.getColumnIndex(POS_X)),
                    cursor.getFloat(cursor.getColumnIndex(POS_Y)),
                    cursor.getFloat(cursor.getColumnIndex(EST_X)),
                    cursor.getFloat(cursor.getColumnIndex(EST_Y)),
                    cursor.getString(cursor.getColumnIndex(UUID)),
                    cursor.getString(cursor.getColumnIndex(METHOD)),
                    cursor.getInt(cursor.getColumnIndex(K)),
                    cursor.getInt(cursor.getColumnIndex(THRESHOLD)),
                    cursor.getInt(cursor.getColumnIndex(ALGORITHM_VERSION)),
                    cursor.getLong(cursor.getColumnIndex(DATE))));
        }
        return result;
    }

    /**
     * barcode ???????????? ????????? ??????
     * @param items
     * @param _new ????????? ??????????????? 1, ???????????? 0 ??????
     */
    public void insertIntoBarcode(List<Barcode> items, int _new) {
        if (items.size() == 0) {
            return;
        }
        SQLiteDatabase db = getWritableDatabase();
        StringBuilder sql = new StringBuilder("insert into " + TABLE_BARCODE + String.format(" (%s, %s, %s, %s, %s, %s)", BARCODE_SERIAL, BUILDING, POS_X, POS_Y, DATE, NEW) + " values ");
        int sqlLength = sql.length();
        for (int i = 1; i <= items.size(); i++) {
            Barcode item = items.get(i - 1);
            sql.append(String.format(" ('%s', '%s', %f, %f, %d, %d), ", item.getSerial(), MainActivity.building, item.getPosX(), item.getPosY(), item.getDate().getTime(), _new));
            if (i % 500 == 0) {
                try {
                    db.execSQL(sql.substring(0, sql.length() - 2));
                } catch (Exception e) {
                    e.printStackTrace();
                } finally {
                    sql.setLength(sqlLength);
                }
            }
        }
        if (items.size() % 500 != 0) {
            db.execSQL(sql.substring(0, sql.length() - 2));
        }
    }

    /**
     * barcode ??????????????? ????????? ??????
     * @param _new ???????????? ?????? ?????? DB?????? ?????? ???????????? ??????????????? null??? ?????? ??? ??????
     * @return ?????? ????????? ?????? row?????? List<Barcode>??? ??????
     */
    @SuppressLint("Range")
    public List<Barcode> searchFromBarcode(Integer _new) {
        StringBuilder sql = new StringBuilder("select " + String.format(" %s, %s, %s, %s ", BARCODE_SERIAL, POS_X, POS_Y, DATE) + " from " + TABLE_BARCODE);
        List<String> conditions = new ArrayList<String>();
        // ???????????? ?????? ???????????? ??????????????? ?????? ?????? ??????
        if (_new != null) {
            conditions.add(String.format(" (%s = 1) ", NEW));
        }
        // ?????? ?????? ??????
        conditions.add(String.format(" (%s = '%s') ", BUILDING, MainActivity.building));
        if (conditions.size() != 0) {
            sql.append(" where ");
        }
        for (int i = 0; i < conditions.size(); i++) {
            sql.append(conditions.get(i));
            if (i != conditions.size() - 1) {
                sql.append(" and ");
            }
        }


        SQLiteDatabase db = getReadableDatabase();
        Cursor cursor = db.rawQuery(sql.toString(), null);

        List<Barcode> result = new ArrayList<>();
        int count = cursor.getCount();
        for (int i = 0; i < count; i++) {
            cursor.moveToNext();
            result.add(new Barcode(cursor.getString(cursor.getColumnIndex(BARCODE_SERIAL)),
                    cursor.getFloat(cursor.getColumnIndex(POS_X)),
                    cursor.getFloat(cursor.getColumnIndex(POS_Y)),
                    cursor.getLong(cursor.getColumnIndex(DATE))));
        }
        return result;
    }

    /**
     * ????????? DB ?????????
     */
    public void synchronize() {
        SynchronizeTask synchronizeTask = new SynchronizeTask();
        synchronizeTask.execute();
    }

    private class SynchronizeTask extends AsyncTask<String, String, String> {

        ProgressDialog progressDialog = new ProgressDialog(context);

        @Override
        protected void onPreExecute() {
            progressDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
            progressDialog.setCancelable(false);
            progressDialog.show();
            super.onPreExecute();
        }

        @Override
        protected String doInBackground(String... strings) {
            // ????????? new??? 1??? ????????? ?????????
            publishProgress("????????? ?????? ????????? ????????? ???...");
            pushRemoteNewData();
            // ????????? ?????? ????????? ??????
            publishProgress("?????? DB??? ????????? ?????? ???...");
            deleteAllLocal();
            // ??????????????? ????????? ????????????
            publishProgress("??????????????? ???????????? ???????????? ???...");
            getAllFromRemote();
            return "????????? ??????";
        }

        @Override
        protected void onProgressUpdate(String... progress) {
            if (progress.length > 0) {
                progressDialog.setMessage(progress[0]);
            }
            super.onProgressUpdate(progress);
        }

        @Override
        protected void onPostExecute(String result) {
            progressDialog.dismiss();
            Toast.makeText(context, result, Toast.LENGTH_SHORT).show();
            super.onPostExecute(result);
        }

        private void pushRemoteNewData() {
            List<WiFiItem> wiFiItems = searchFromWiFiInfo(null, null, null, null, null, null, 1);
            List<EstimatedResult> estimatedResults = searchFromFingerprint(1);
            List<Barcode> barcodes = searchFromBarcode(1);
            RetrofitAPI retrofitAPI = RetrofitClient.getRetrofitAPI();
            try {
                retrofitAPI.postDataWiFiItem(wiFiItems).execute();
                retrofitAPI.postDataEstimatedResult(estimatedResults).execute();
                retrofitAPI.postDataBarcode(barcodes).execute();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        private void deleteAllLocal() {
            SQLiteDatabase db = getWritableDatabase();
            db.execSQL("delete from " + TABLE_WIFIINFO);
            db.execSQL("delete from " + TABLE_FINGERPRINT);
            db.execSQL("delete from " + TABLE_BARCODE);
        }

        private void getAllFromRemote() {
            RetrofitAPI retrofitAPI = RetrofitClient.getRetrofitAPI();
            try {
                List<WiFiItem> wiFiItems = retrofitAPI.getDataWiFiItem(null, null, null, null, null, null).execute().body();
                List<EstimatedResult> estimatedResults = retrofitAPI.getDataEstimateResult(null, null).execute().body();
                List<Barcode> barcodes = retrofitAPI.getDataBarcode().execute().body();
                insertIntoWiFiInfo(wiFiItems, 0);
                insertIntoFingerprint(estimatedResults, 0);
                insertIntoBarcode(barcodes, 0);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
}

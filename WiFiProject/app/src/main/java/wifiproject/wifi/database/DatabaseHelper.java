package wifilocation.wifi.database;

import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

import androidx.annotation.Nullable;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;

import wifilocation.wifi.estimate.EstimatedResult;
import wifilocation.wifi.model.WiFiItem;

/**
 * example)
 * DatabaseHelper dbHelper = new DatabaseHelper(context);
 * dbHelper.insertIntoWiFiInfo(parameters);
 * List<WiFiItem> items = dbHelper.searchFromWiFiInfo(parameters);
 *
 */
public class DatabaseHelper extends SQLiteOpenHelper {
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

    public DatabaseHelper(@Nullable Context context) {
        super(context, DBNAME, null, VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase sqLiteDatabase) {
        // wifiinfo 테이블 생성
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
                METHOD + " text)");
        
        // fingerprint 테이블 생성
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
                METHOD + " text)");
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
            onCreate(sqLiteDatabase);
        }
    }

    /**
     * wifiinfo 테이블에 데이터 추가
     *
     * @param items 스캔한 WiFiItem 전달, DB에 저장
     */
    public void insertIntoWiFiInfo(List<WiFiItem> items) {
        if (items.size() == 0) {
            return;
        }
        long currentTimeMillis = System.currentTimeMillis();
        String sql = "insert into " + TABLE_WIFIINFO + String.format(" (%s, %s, %s, %s, %s, %s, %s, %s, %s, %s) ", POS_X, POS_Y, SSID, BSSID, FREQUENCY, LEVEL, DATE, UUID, BUILDING, METHOD) + " values ";
        for (WiFiItem item : items) {
            sql += String.format("(%f, %f, '%s', '%s', %d, %d, %d, '%s', '%s', '%s'), ",
                    item.getX(), item.getY(), item.getSSID(), item.getBSSID(), item.getFrequency(), item.getRSSI(), currentTimeMillis, item.getUuid(), item.getBuilding(), item.getMethod());
        }
        sql = sql.substring(0, sql.length() - 2); // 끝에 붙은 ,<공백> 제거
        SQLiteDatabase db = getWritableDatabase();
        db.execSQL(sql);
    }

    /**
     * wifiinfo 테이블로부터 데이터 조회
     *
     * @param building building이 일치하는 row들만 조회
     * @param ssid ssid가 일치하는 row들만 조회
     * @param x x - 1 < pos_x < x + 1 인 row들만 조회
     * @param y y - 1 < pos_y < y + 1 인 row들만 조회, x와 y 모두 null 전달 시 위치에 제한 없음
     * @param from from 잉후에 등록된 row들만 조회, null 전달 시 제한 없음
     * @param to to 이전에 등록된 row들만 조회, null 전달 시 제한 없음
     * @return sql문 실행 결과로 나온 row들을 List<WiFiItem> 으로 변환하여 반환
     */
    public List<WiFiItem> searchFromWiFiInfo(String building, String ssid, Float x, Float y, String from, String to) {
        String sql = "select " + String.format("%s, %s, %s, %s, %s, %s, %s, %s, %s", POS_X, POS_Y, SSID, BSSID, LEVEL, FREQUENCY, UUID, BUILDING, METHOD) + " from " + TABLE_WIFIINFO;
        // 빌딩 조건 추가
        if (building != null) {
            sql += String.format(" where (%s = '%s')", BUILDING, building);
        }
        // SSID 조건 추가
        if (ssid != null) {
            sql += String.format(" and (%s = '%s')", SSID, ssid);
        }
        // 날짜 조건 추가
        if (from != null || to != null) {
            try {
                SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMdd");
                long timestampFrom, timestampTo;
                timestampFrom = sdf.parse(from == null ? "20020202" : from).getTime();
                timestampTo = sdf.parse(to == null ? "20300303" : to).getTime();
                sql += String.format(" and (%s between %d and %d)", DATE, timestampFrom, timestampTo);
            } catch (ParseException e) {
                e.printStackTrace();
            }
        }
        // 위치 조건 추가
        if (x != null && y != null) {
            sql += String.format(" and ((%s between %f and %f) and (%s between %f and %f))", POS_X, x - 1, x + 1, POS_Y, y - 1, y + 1);
        }


        SQLiteDatabase db = getReadableDatabase();
        Cursor cursor = db.rawQuery(sql, null);

        List<WiFiItem> result = new ArrayList<WiFiItem>();

        int count = cursor.getCount();
        for (int i = 0; i < count; i++) {
            cursor.moveToNext();
            result.add(new WiFiItem(cursor.getFloat(0),
                    cursor.getFloat(1),
                    cursor.getString(2),
                    cursor.getString(3),
                    cursor.getInt(4),
                    cursor.getInt(5),
                    cursor.getString(6),
                    cursor.getString(7),
                    cursor.getString(8)));
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
     * fingerprint 테이블에 데이터 추가
     * 
     * @param items 추정된 위치 정보 리스트 전달
     */
    public void insertIntoFingerprint(List<EstimatedResult> items) {
        if (items.size() == 0) {
            return;
        }
        long currentTimeMillis = System.currentTimeMillis();
        String sql = "insert into " + TABLE_FINGERPRINT + String.format(" (%s, %s, %s, %s, %s, %s, %s, %s, %s, %s, %s, %s)", POS_X, POS_Y, UUID, DATE, EST_X, EST_Y, K, THRESHOLD, BUILDING, SSID, ALGORITHM_VERSION, METHOD) + " values ";
        for (EstimatedResult item : items) {
            sql += String.format("(%f, %f, '%s', %d, %f, %f, %d, %d, '%s', '%s', %d, '%s'), ",
                    item.getPositionRealX(), item.getPositionRealY(), item.getUuid(), currentTimeMillis, item.getPositionEstimatedX(), item.getPositionEstimatedY(),
                    item.getK(), item.getThreshold(), item.getBuilding(), item.getSsid(), item.getAlgorithmVersion(), item.getMethod());
        }
        sql = sql.substring(0, sql.length() - 2);

        SQLiteDatabase db = getWritableDatabase();
        db.execSQL(sql);
    }
}

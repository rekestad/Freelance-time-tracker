package a9.iprogmob.a9.utils;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import java.util.ArrayList;
import java.util.List;

import a9.iprogmob.a9.models.TimeLog;
import a9.iprogmob.a9.models.Workplace;

/**
 * Används för att kommunciera med SQLite-databasen
 */

public class DatabaseHelper extends SQLiteOpenHelper {

    private static final int DATABASE_VERSION = 1;
    private static final String DATABASE_NAME = "worktime";

    // Tabeller
    private static final String TABLE_WORKPLACE = "workplace";
    private static final String TABLE_TIME_LOG = "timeLog";

    // Gemensamma kolumner
    private static final String ID = "id";

    // Kolumner för workplace-tabellen
    private static final String WORKPLACE_NAME = "name";
    private static final String WORKPLACE_CHARGE_PER_HOUR = "chargePerHour";
    private static final String WORKPLACE_CURRENCY = "currency";

    // Kolumner för timeLog-tabellen
    private static final String TIME_LOG_WORKPLACE_ID = "workplaceId";
    private static final String TIME_LOG_ACTIVE = "active";
    private static final String TIME_LOG_START_TIME = "startTime";
    private static final String TIME_LOG_END_TIME = "endTime";
    private static final String TIME_LOG_TOTAL_MIN = "totalMinutes";
    private static final String TIME_LOG_COMMENT = "comment";

    // Query för att skapa workplace-tabellen
    private static final String CREATE_TABLE_WORKPLACE = "CREATE TABLE "
            + TABLE_WORKPLACE + " ("
            + ID + " INTEGER NOT NULL PRIMARY KEY AUTOINCREMENT, "
            + WORKPLACE_NAME + "	TEXT NOT NULL, "
            + WORKPLACE_CHARGE_PER_HOUR + "	REAL NOT NULL, "
            + WORKPLACE_CURRENCY + "	TEXT NOT NULL );";

    // Query för att skapa timeLog-tabellen
    private static final String CREATE_TABLE_TIME_LOG = "CREATE TABLE "
            + TABLE_TIME_LOG + " ("
            + ID + "	INTEGER NOT NULL PRIMARY KEY AUTOINCREMENT, "
            + TIME_LOG_WORKPLACE_ID + "	INTEGER NOT NULL, "
            + TIME_LOG_ACTIVE + "	INTEGER NOT NULL, "
            + TIME_LOG_START_TIME + "	TEXT NOT NULL, "
            + TIME_LOG_END_TIME + "	TEXT, "
            + TIME_LOG_TOTAL_MIN + "	INTEGER, "
            + TIME_LOG_COMMENT + "	TEXT, "
            + "FOREIGN KEY(" + TIME_LOG_WORKPLACE_ID + ") REFERENCES " + TABLE_WORKPLACE + "(" + ID + ") ON DELETE CASCADE );";

    /**
     * Konstruktor
     */
    public DatabaseHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    /**
     * Called when the database is created for the first time
     * https://developer.android.com/reference/android/database/sqlite/SQLiteOpenHelper#oncreate
     */
    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL(CREATE_TABLE_WORKPLACE);
        db.execSQL(CREATE_TABLE_TIME_LOG);
    }

    /**
     * Called when the database needs to be upgraded. The implementation should use this method to
     * drop tables, add tables, or do anything else it needs to upgrade to the new schema version.
     * https://developer.android.com/reference/android/database/sqlite/SQLiteOpenHelper#onupgrade
     */
    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_WORKPLACE);
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_TIME_LOG);
        onCreate(db);
    }

    // #######################################
    // ## WORKPLACE
    // #######################################

    /**
     * Skapa
     */
    public void insertWorkplace(Workplace wp) {
        SQLiteDatabase db = this.getWritableDatabase();

        ContentValues values = new ContentValues();
        values.put(WORKPLACE_NAME, wp.getName());
        values.put(WORKPLACE_CHARGE_PER_HOUR, wp.getChargePerHour());
        values.put(WORKPLACE_CURRENCY, wp.getCurrency());

        db.insert(TABLE_WORKPLACE, null, values);
    }

    /**
     * Hämta en(1)
     */
    public Workplace getWorkplace(int id) {
        SQLiteDatabase db = this.getReadableDatabase();

        String sql = "SELECT * FROM " + TABLE_WORKPLACE + " WHERE " + ID + " = " + id;

        Cursor c = db.rawQuery(sql, null);

        if (c != null) {
            c.moveToFirst();
            Workplace wp = populateWorkplaceObject(c);
            c.close();
            return wp;
        }

        return null;

    }

    /**
     * Validering
     */
    public boolean checkIfWorkplaceNameExist(String name) {
        boolean exists = false;

        SQLiteDatabase db = this.getReadableDatabase();
        String sql = "SELECT * FROM " + TABLE_WORKPLACE + " WHERE " + WORKPLACE_NAME + " LIKE '" + name + "'";

        Cursor c = db.rawQuery(sql, null);

        if (c != null && c.getCount() > 0) {
            exists = true;
            c.close();
        }

        return exists;
    }

    /**
     * Hämta alla
     */
    public List<Workplace> getAllWorkplace() {
        List<Workplace> wps = new ArrayList<>();
        String sql = "SELECT * FROM " + TABLE_WORKPLACE + " ORDER BY " + WORKPLACE_NAME;

        SQLiteDatabase db = this.getReadableDatabase();
        Cursor c = db.rawQuery(sql, null);

        if (c.moveToFirst()) {
            do {
                wps.add(populateWorkplaceObject(c));
            } while (c.moveToNext());

            c.close();
        }

        return wps;
    }

    /**
     * Populera objekt med DB-data
     */
    private Workplace populateWorkplaceObject(Cursor c) {
        Workplace wp = new Workplace();
        wp.setId(c.getInt(c.getColumnIndex(ID)));
        wp.setName(c.getString(c.getColumnIndex(WORKPLACE_NAME)));
        wp.setChargePerHour(c.getDouble(c.getColumnIndex(WORKPLACE_CHARGE_PER_HOUR)));
        wp.setCurrency(c.getString(c.getColumnIndex(WORKPLACE_CURRENCY)));
        return wp;
    }

    /**
     * Uppdatera
     */
    public void updateWorkplace(Workplace wp) {
        SQLiteDatabase db = this.getWritableDatabase();

        ContentValues values = new ContentValues();
        values.put(WORKPLACE_NAME, wp.getName());
        values.put(WORKPLACE_CHARGE_PER_HOUR, wp.getChargePerHour());
        values.put(WORKPLACE_CURRENCY, wp.getCurrency());

        db.update(TABLE_WORKPLACE, values, ID + " = ?",
                new String[]{String.valueOf(wp.getId())});

    }

    /**
     * Ta bort
     */
    public void deleteWorkplace(int id) {
        SQLiteDatabase db = this.getWritableDatabase();
        db.delete(TABLE_WORKPLACE, ID + " = ?",
                new String[]{String.valueOf(id)});
    }

    // #######################################
    // ## TIMELOG
    // #######################################

    /**
     * Skapa
     */
    public void insertTimeLog(TimeLog tl) {
        SQLiteDatabase db = this.getWritableDatabase();

        ContentValues values = new ContentValues();
        values.put(TIME_LOG_WORKPLACE_ID, tl.getWorkplaceId());
        values.put(TIME_LOG_ACTIVE, tl.getActive());
        values.put(TIME_LOG_START_TIME, tl.getStartTime());
        values.put(TIME_LOG_END_TIME, tl.getEndTime());
        values.put(TIME_LOG_TOTAL_MIN, tl.getTotalMinutes());
        values.put(TIME_LOG_COMMENT, tl.getComment());
        db.insert(TABLE_TIME_LOG, null, values);
    }

    /**
     * Hämta en(1)
     */
    public TimeLog getTimeLog(int id) {
        SQLiteDatabase db = this.getReadableDatabase();

        String sql = "SELECT * FROM " + TABLE_TIME_LOG + " WHERE " + ID + " = " + id;

        Cursor c = db.rawQuery(sql, null);

        if (c != null) {
            c.moveToFirst();
            TimeLog tl = populateTimeLogObject(c);
            c.close();
            return tl;
        }

        return null;
    }

    /**
     * Uppdatera
     */
    public void updateTimeLog(TimeLog tl) {
        SQLiteDatabase db = this.getWritableDatabase();

        ContentValues values = new ContentValues();
        values.put(TIME_LOG_ACTIVE, tl.getActive());
        values.put(TIME_LOG_START_TIME, tl.getStartTime());
        values.put(TIME_LOG_END_TIME, tl.getEndTime());
        values.put(TIME_LOG_TOTAL_MIN, tl.getTotalMinutes());
        values.put(TIME_LOG_COMMENT, tl.getComment());

        db.update(TABLE_TIME_LOG, values, ID + " = ?",
                new String[]{String.valueOf(tl.getId())});
    }

    /**
     * Ta bort
     */
    public void deleteTimeLog(int id) {
        SQLiteDatabase db = this.getWritableDatabase();
        db.delete(TABLE_TIME_LOG, ID + " = ?",
                new String[]{String.valueOf(id)});
    }

    /**
     * Hämta alla
     */
    public List<TimeLog> getAllTimeLog(int workplaceId) {
        List<TimeLog> tls = new ArrayList<>();
        String sql = "SELECT * FROM " + TABLE_TIME_LOG + " WHERE " + TIME_LOG_WORKPLACE_ID + " = " + workplaceId + " AND " + TIME_LOG_ACTIVE + " = 0 ORDER BY " + TIME_LOG_START_TIME + " DESC";

        SQLiteDatabase db = this.getReadableDatabase();
        Cursor c = db.rawQuery(sql, null);

        if (c.moveToFirst()) {
            do {
                tls.add(populateTimeLogObject(c));
            } while (c.moveToNext());

            c.close();
        }

        return tls;
    }

    /**
     * Hämta aktiv just nu
     */
    public TimeLog getActiveTimeLog(int workplaceId) {
        SQLiteDatabase db = this.getReadableDatabase();

        String sql = "SELECT * FROM " + TABLE_TIME_LOG + " WHERE " + TIME_LOG_ACTIVE + " = 1 AND " + TIME_LOG_WORKPLACE_ID + " = " + workplaceId;

        Cursor c = db.rawQuery(sql, null);

        if (c != null && c.getCount() > 0) {
            c.moveToFirst();
            TimeLog tl = populateTimeLogObject(c);
            c.close();
            return tl;
        }

        return null;
    }

    /**
     * Populera objekt
     */
    private TimeLog populateTimeLogObject(Cursor c) {
        TimeLog tl = new TimeLog();
        tl.setId(c.getInt(c.getColumnIndex(ID)));
        tl.setWorkplaceId(c.getInt(c.getColumnIndex(TIME_LOG_WORKPLACE_ID)));
        tl.setActive(c.getInt(c.getColumnIndex(TIME_LOG_ACTIVE)));
        tl.setStartTime(c.getString(c.getColumnIndex(TIME_LOG_START_TIME)));
        tl.setEndTime(c.getString(c.getColumnIndex(TIME_LOG_END_TIME)));
        tl.setTotalMinutes(c.getInt(c.getColumnIndex(TIME_LOG_TOTAL_MIN)));
        tl.setComment(c.getString(c.getColumnIndex(TIME_LOG_COMMENT)));
        return tl;
    }

    /**
     * Returnera total arbetstid för en workplace
     */
    public int sumTime(int workplaceId) {
        SQLiteDatabase db = this.getReadableDatabase();

        String sql = "SELECT SUM(" + TIME_LOG_TOTAL_MIN + ") AS totalTime FROM " + TABLE_TIME_LOG + " WHERE " + TIME_LOG_WORKPLACE_ID + " = " + workplaceId;

        Cursor c = db.rawQuery(sql, null);

        if (c != null) {
            c.moveToFirst();
            int total = c.getInt(c.getColumnIndex("totalTime"));
            c.close();
            return total;
        }

        return 0;
    }
}


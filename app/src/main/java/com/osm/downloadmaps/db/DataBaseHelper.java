package com.osm.downloadmaps.db;

import android.annotation.TargetApi;
import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.os.Build;

public class DataBaseHelper extends SQLiteOpenHelper {

    private static DataBaseHelper sInstance;
    private static final String TAG = DataBaseHelper.class.getSimpleName();

    public static final String DB_NAME = "regions.db";
    private static final int DB_VERSION = 1;

    protected static final String TABLE_COUNTRY = "country";
    protected static final String TABLE_CONTINENT = "continent";
    protected static final String TABLE_CITY = "city";

    protected static final String COLUMN_ID = "id";
    protected static final String COLUMN_NAME = "name";
    protected static final String COLUMN_LOADPATH = "loadpath";
    protected static final String COLUMN_ISLOADMAP = "isloadmap";
    protected static final String COUNTRY_CONTINENT_ID = "continent_id";
    protected static final String CITY_COUNTRY_ID = "country_id";

    private static final String CREATE_TABLE_CONTINENT = "CREATE TABLE IF NOT EXISTS " + TABLE_CONTINENT + "("
            + COLUMN_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, "
            + COLUMN_NAME + " TEXT )";

    private static final String CREATE_TABLE_COUNTRY = "CREATE TABLE IF NOT EXISTS " + TABLE_COUNTRY + "("
            + COLUMN_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, "
            + COLUMN_NAME + " TEXT, "
            + COLUMN_LOADPATH + " TEXT, "
            + COLUMN_ISLOADMAP + " INTEGER, "
            + COUNTRY_CONTINENT_ID + " INT, "
            + "FOREIGN KEY(" + COUNTRY_CONTINENT_ID + ") REFERENCES "
            + TABLE_CONTINENT + "(id) " + ")";

    private static final String CREATE_TABLE_CITY = "CREATE TABLE IF NOT EXISTS " + TABLE_CITY + "("
            + COLUMN_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, "
            + COLUMN_NAME + " TEXT, "
            + COLUMN_LOADPATH + " TEXT, "
            + COLUMN_ISLOADMAP + " INTEGER, "
            + CITY_COUNTRY_ID + " INT, "
            + "FOREIGN KEY(" + CITY_COUNTRY_ID + ") REFERENCES "
            + TABLE_COUNTRY + "(id) " + ")";

    public static synchronized DataBaseHelper getHelper(Context context) {
        if (sInstance == null) {
            sInstance = new DataBaseHelper(context.getApplicationContext());
        }
        return sInstance;
    }

    private DataBaseHelper(Context context) {
        super(context, DB_NAME, null, DB_VERSION);
    }

    @Override
    public void onOpen(SQLiteDatabase db) {
        super.onOpen(db);
        if (!db.isReadOnly()) {
            setForeignKeyConstraintsEnabled(db);
        }
        /*
        super.onOpen(db);
        if (!db.isReadOnly()) {
            // Enable foreign key constraints
            db.execSQL("PRAGMA foreign_keys=ON;");
        }
        */
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL(CREATE_TABLE_CONTINENT);
        db.execSQL(CREATE_TABLE_COUNTRY);
        db.execSQL(CREATE_TABLE_CITY);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {

    }

    private void setForeignKeyConstraintsEnabled(SQLiteDatabase db) {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.JELLY_BEAN) {
            setForeignKeyConstraintsEnabledPreJellyBean(db);
        } else {
            setForeignKeyConstraintsEnabledPostJellyBean(db);
        }
    }

    private void setForeignKeyConstraintsEnabledPreJellyBean(SQLiteDatabase db) {
        db.execSQL("PRAGMA foreign_keys=ON;");
    }

    @TargetApi(Build.VERSION_CODES.JELLY_BEAN)
    private void setForeignKeyConstraintsEnabledPostJellyBean(SQLiteDatabase db) {
        db.setForeignKeyConstraintsEnabled(true);
    }

}

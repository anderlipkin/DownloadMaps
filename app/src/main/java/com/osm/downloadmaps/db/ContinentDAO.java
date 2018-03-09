package com.osm.downloadmaps.db;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteStatement;
import android.util.Log;

import com.osm.downloadmaps.model.Continent;

import java.util.ArrayList;
import java.util.List;

public class ContinentDAO extends GeneralDBDAO{

    private static final String TAG = ContinentDAO.class.getSimpleName();

    private static final String WHERE_ID_EQUALS = DataBaseHelper.COLUMN_ID + " =?";

    public ContinentDAO(Context context) {
        super(context);
    }

    public long save(Continent continent) {
        ContentValues values = new ContentValues();
        values.put(DataBaseHelper.COLUMN_NAME, continent.getName());

        Log.d(TAG, "Save continent=" + continent.getName());
        return database.insert(DataBaseHelper.TABLE_CONTINENT, null, values);
    }

    public void saveAllContinent(List<Continent> continents) {
        String sql = "INSERT INTO " + DataBaseHelper.TABLE_CONTINENT + " VALUES (?);";
        SQLiteStatement statement = database.compileStatement(sql);
        database.beginTransaction();

        try {
            for (Continent continent : continents) {
                statement.clearBindings();
                statement.bindString(1, continent.getName());
                statement.execute();
            }

            database.setTransactionSuccessful();
            Log.d(TAG, "Save all continents to database");
        } catch (Exception e) {
            Log.d(TAG, "Error while trying to save all continent to database");
        } finally {
            database.endTransaction();
        }
    }

    public long update(Continent continent) {
        ContentValues values = new ContentValues();
        values.put(DataBaseHelper.COLUMN_NAME, continent.getName());

        long result = database.update(DataBaseHelper.TABLE_CONTINENT, values,
                WHERE_ID_EQUALS,
                new String[] { String.valueOf(continent.getId()) });
        Log.d(TAG, "Update continent=" + continent.getName());
        return result;

    }

    public int deleteContinent(Continent continent) {
        Log.d(TAG, "Delete continent=" + continent.getName());
        return database.delete(DataBaseHelper.TABLE_CONTINENT,
                WHERE_ID_EQUALS, new String[] {String.valueOf(continent.getId())});
    }

    public Continent getContinent(int id) {
        String[] selectionArgs = {String.valueOf(id)};

        String selectQuery = String.format("SELECT * FROM %s WHERE %s = ?",
                DataBaseHelper.TABLE_CONTINENT, DataBaseHelper.COLUMN_ID);

        Cursor cursor = database.rawQuery(selectQuery, selectionArgs);

        if (cursor.getCount() < 1) {
            cursor.close();
            return null;
        }

        cursor.moveToFirst();
        Continent continent = new Continent();
        continent.setId(cursor.getInt(0));
        continent.setName(cursor.getString(1));

        cursor.close();
        return continent;
    }

    public Continent getContinentByName(String name) {
        String[] selectionArgs = {name};

        String selectQuery = String.format("SELECT * FROM %s WHERE %s = ?",
                DataBaseHelper.TABLE_CONTINENT, DataBaseHelper.COLUMN_NAME);

        Cursor cursor = database.rawQuery(selectQuery, selectionArgs);

        if (cursor.getCount() < 1) {
            cursor.close();
            return null;
        }

        cursor.moveToFirst();
        Continent continent = new Continent();
        continent.setId(cursor.getInt(0));
        continent.setName(cursor.getString(1));

        cursor.close();
        return continent;
    }

    public List<Continent> getContinents() {
        List<Continent> continents = new ArrayList<Continent>();
        Cursor cursor = database.query(DataBaseHelper.TABLE_CONTINENT,
                new String[] { DataBaseHelper.COLUMN_ID,
                        DataBaseHelper.COLUMN_NAME },
                null, null, null, null, DataBaseHelper.COLUMN_NAME + " ASC");

        while (cursor.moveToNext()) {
            Continent continent = new Continent();
            continent.setId(cursor.getInt(0));
            continent.setName(cursor.getString(1));
            continents.add(continent);
        }
        cursor.close();
        return continents;
    }
}

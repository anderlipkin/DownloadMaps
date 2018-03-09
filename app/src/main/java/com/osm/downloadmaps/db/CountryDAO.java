package com.osm.downloadmaps.db;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteQueryBuilder;
import android.database.sqlite.SQLiteStatement;
import android.util.Log;

import com.osm.downloadmaps.model.Continent;
import com.osm.downloadmaps.model.Country;

import java.util.ArrayList;
import java.util.List;

public class CountryDAO extends GeneralDBDAO {
    private static final String TAG = CountryDAO.class.getSimpleName();

    private static final String WHERE_ID_EQUALS = DataBaseHelper.COLUMN_ID + " =?";

    public CountryDAO(Context context) {
        super(context);
    }

    public long save(Country country) {
        ContentValues values = new ContentValues();
        values.put(DataBaseHelper.COLUMN_NAME, country.getName());
        values.put(DataBaseHelper.COLUMN_LOADPATH, country.getLoadPath());
        values.put(DataBaseHelper.COLUMN_ISLOADMAP, country.getIsLoadMap());
        values.put(DataBaseHelper.COUNTRY_CONTINENT_ID, country.getContinent().getId());

        Log.d(TAG, "Save country to database: " + country.getName());
        return database.insert(DataBaseHelper.TABLE_COUNTRY, null, values);
    }

    public void saveAllCountry(List<Country> countries) {
        String sql = "INSERT INTO " + DataBaseHelper.TABLE_COUNTRY + " VALUES (?,?,?,?);";
        SQLiteStatement statement = database.compileStatement(sql);
        database.beginTransaction();

        try {
            for (Country country: countries) {
                statement.clearBindings();
                statement.bindString(1, country.getName());
                statement.bindString(2, country.getLoadPath());
                statement.bindLong(3, country.getIsLoadMap());
                statement.bindLong(4, country.getContinent().getId());
                statement.execute();
            }

            database.setTransactionSuccessful();
            Log.d(TAG, "Save all countries to database");
        } catch (Exception e) {
            Log.d(TAG, "Error while trying to save all countries to database");
        } finally {
            database.endTransaction();
        }
    }

    public long update(Country country) {
        ContentValues values = new ContentValues();
        values.put(DataBaseHelper.COLUMN_NAME, country.getName());
        values.put(DataBaseHelper.COLUMN_LOADPATH, country.getLoadPath());
        values.put(DataBaseHelper.COLUMN_ISLOADMAP, country.getIsLoadMap());
        values.put(DataBaseHelper.COUNTRY_CONTINENT_ID, country.getContinent().getId());

        long result = database.update(DataBaseHelper.TABLE_COUNTRY, values,
                WHERE_ID_EQUALS,
                new String[] { String.valueOf(country.getId()) });
        Log.d(TAG, "Update country=" + country.getName());
        return result;

    }

    public int deleteCountry(Country country) {
        Log.d(TAG, "Delete country=" + country.getName());
        return database.delete(DataBaseHelper.TABLE_COUNTRY,
                WHERE_ID_EQUALS, new String[] { String.valueOf(country.getId()) });
    }

    public Country getCountry(int id) {
        SQLiteQueryBuilder queryBuilder = new SQLiteQueryBuilder();
        queryBuilder
                .setTables(DataBaseHelper.TABLE_COUNTRY
                        + " INNER JOIN "
                        + DataBaseHelper.TABLE_CONTINENT
                        + " ON "
                        + DataBaseHelper.COUNTRY_CONTINENT_ID
                        + " = "
                        + (DataBaseHelper.TABLE_CONTINENT + "." + DataBaseHelper.COLUMN_ID));
        queryBuilder.appendWhere(DataBaseHelper.COLUMN_ID + "=" + id);

        Cursor cursor = queryBuilder.query(database, new String[] {
                        DataBaseHelper.TABLE_COUNTRY + "." + DataBaseHelper.COLUMN_ID,
                        DataBaseHelper.TABLE_COUNTRY + "." + DataBaseHelper.COLUMN_NAME,
                        DataBaseHelper.COLUMN_LOADPATH,
                        DataBaseHelper.COLUMN_ISLOADMAP,
                        DataBaseHelper.COUNTRY_CONTINENT_ID,
                        DataBaseHelper.TABLE_CONTINENT + "." + DataBaseHelper.COLUMN_NAME},
                null, null, null, null, null);

        if (cursor.getCount() < 1) {
            cursor.close();
            return null;
        }

        cursor.moveToFirst();
        Country country = new Country();
        country.setId(cursor.getInt(0));
        country.setName(cursor.getString(1));
        country.setLoadPath(cursor.getString(2));
        country.setIsLoadMap(cursor.getInt(3));
        country.setContinent(new Continent(cursor.getInt(4), cursor.getString(5)));

        cursor.close();
        return country;
    }


    public Country getCountryByName(String name) {
        SQLiteQueryBuilder queryBuilder = new SQLiteQueryBuilder();
        queryBuilder
                .setTables(DataBaseHelper.TABLE_COUNTRY
                        + " INNER JOIN "
                        + DataBaseHelper.TABLE_CONTINENT
                        + " ON "
                        + DataBaseHelper.COUNTRY_CONTINENT_ID
                        + " = "
                        + (DataBaseHelper.TABLE_CONTINENT + "." + DataBaseHelper.COLUMN_ID));
        queryBuilder.appendWhere(DataBaseHelper.TABLE_COUNTRY + "." + DataBaseHelper.COLUMN_NAME + "='" + name +"'");

        Cursor cursor = queryBuilder.query(database, new String[] {
                        DataBaseHelper.TABLE_COUNTRY + "." + DataBaseHelper.COLUMN_ID,
                        DataBaseHelper.TABLE_COUNTRY + "." + DataBaseHelper.COLUMN_NAME,
                        DataBaseHelper.COLUMN_LOADPATH,
                        DataBaseHelper.COLUMN_ISLOADMAP,
                        DataBaseHelper.COUNTRY_CONTINENT_ID,
                        DataBaseHelper.TABLE_CONTINENT + "." + DataBaseHelper.COLUMN_NAME},
                null, null, null, null, null);

        if (cursor.getCount() < 1) {
            cursor.close();
            return null;
        }

        cursor.moveToFirst();
        Country country = new Country();
        country.setId(cursor.getInt(0));
        country.setName(cursor.getString(1));
        country.setLoadPath(cursor.getString(2));
        country.setIsLoadMap(cursor.getInt(3));
        country.setContinent(new Continent(cursor.getInt(4), cursor.getString(5)));

        cursor.close();
        return country;
    }


    public List<Country> getCountries() {
        List<Country> countries = new ArrayList<Country>();
        SQLiteQueryBuilder queryBuilder = new SQLiteQueryBuilder();
        queryBuilder
                .setTables(DataBaseHelper.TABLE_COUNTRY
                        + " INNER JOIN "
                        + DataBaseHelper.TABLE_CONTINENT
                        + " ON "
                        + DataBaseHelper.COUNTRY_CONTINENT_ID
                        + " = "
                        + (DataBaseHelper.TABLE_CONTINENT + "." + DataBaseHelper.COLUMN_ID));

        Cursor cursor = queryBuilder.query(database, new String[] {
                DataBaseHelper.TABLE_COUNTRY + "." + DataBaseHelper.COLUMN_ID,
                DataBaseHelper.TABLE_COUNTRY + "." + DataBaseHelper.COLUMN_NAME,
                DataBaseHelper.COLUMN_LOADPATH,
                DataBaseHelper.COLUMN_ISLOADMAP,
                DataBaseHelper.COUNTRY_CONTINENT_ID,
                DataBaseHelper.TABLE_CONTINENT + "." + DataBaseHelper.COLUMN_NAME},
        null, null, null, null, null);

        while (cursor.moveToNext()) {
            Country country = new Country();
            country.setId(cursor.getInt(0));
            country.setName(cursor.getString(1));
            country.setLoadPath(cursor.getString(2));
            country.setIsLoadMap(cursor.getInt(3));
            country.setContinent(new Continent(cursor.getInt(4), cursor.getString(5)));
            countries.add(country);
        }

        cursor.close();
        return countries;
    }

    public List<Country> getCountriesByContinentId(int idContinent) {
        List<Country> countries = new ArrayList<Country>();
        SQLiteQueryBuilder queryBuilder = new SQLiteQueryBuilder();
        queryBuilder
                .setTables(DataBaseHelper.TABLE_COUNTRY
                        + " INNER JOIN "
                        + DataBaseHelper.TABLE_CONTINENT
                        + " ON "
                        + DataBaseHelper.COUNTRY_CONTINENT_ID
                        + " = "
                        + (DataBaseHelper.TABLE_CONTINENT + "." + DataBaseHelper.COLUMN_ID));
        queryBuilder.appendWhere(DataBaseHelper.COUNTRY_CONTINENT_ID + "=" + idContinent);

        Cursor cursor = queryBuilder.query(database, new String[] {
                        DataBaseHelper.TABLE_COUNTRY + "." + DataBaseHelper.COLUMN_ID,
                        DataBaseHelper.TABLE_COUNTRY + "." + DataBaseHelper.COLUMN_NAME,
                        DataBaseHelper.COLUMN_LOADPATH,
                        DataBaseHelper.COLUMN_ISLOADMAP,
                        DataBaseHelper.COUNTRY_CONTINENT_ID,
                        DataBaseHelper.TABLE_CONTINENT + "." + DataBaseHelper.COLUMN_NAME},
                null, null, null, null,
                DataBaseHelper.TABLE_COUNTRY + "." + DataBaseHelper.COLUMN_NAME + " ASC");

        if (cursor.getCount() < 1) {
            cursor.close();
            return null;
        }

        while (cursor.moveToNext()) {
            Country country = new Country();
            country.setId(cursor.getInt(0));
            country.setName(cursor.getString(1));
            country.setLoadPath(cursor.getString(2));
            country.setIsLoadMap(cursor.getInt(3));
            country.setContinent(new Continent(cursor.getInt(4), cursor.getString(5)));
            countries.add(country);
        }

        cursor.close();
        return countries;
    }

}

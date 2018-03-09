package com.osm.downloadmaps.db;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteQueryBuilder;
import android.database.sqlite.SQLiteStatement;
import android.util.Log;

import com.osm.downloadmaps.model.City;
import com.osm.downloadmaps.model.Country;

import java.util.ArrayList;
import java.util.List;

public class CityDAO extends GeneralDBDAO {

    private static final String TAG = CityDAO.class.getSimpleName();

    private static final String WHERE_ID_EQUALS = DataBaseHelper.COLUMN_ID + " =?";

    public CityDAO(Context context) {
        super(context);
    }

    public long save(City city) {
        ContentValues values = new ContentValues();
        values.put(DataBaseHelper.COLUMN_NAME, city.getName());
        values.put(DataBaseHelper.COLUMN_LOADPATH, city.getLoadPath());
        values.put(DataBaseHelper.COLUMN_ISLOADMAP, city.getIsLoadMap());
        values.put(DataBaseHelper.CITY_COUNTRY_ID, city.getCountry().getId());

        Log.d(TAG, "Save city to database: " + city.getName());
        return database.insert(DataBaseHelper.TABLE_CITY, null, values);
    }

    public void saveAllCity(List<City> cities) {
        String sql = "INSERT INTO " + DataBaseHelper.TABLE_CITY + " VALUES (?,?,?,?);";
        SQLiteStatement statement = database.compileStatement(sql);
        database.beginTransaction();

        try {
            for (City city: cities) {
                statement.clearBindings();
                statement.bindString(1, city.getName());
                statement.bindString(2, city.getLoadPath());
                statement.bindLong(3, city.getIsLoadMap());
                statement.bindLong(4, city.getCountry().getId());
                statement.execute();
            }

            database.setTransactionSuccessful();
            Log.d(TAG, "Save all cities to database");
        } catch (Exception e) {
            Log.d(TAG, "Error while trying to save all cities to database");
        } finally {
            database.endTransaction();
        }
    }

    public long update(City city) {
        ContentValues values = new ContentValues();
        values.put(DataBaseHelper.COLUMN_NAME, city.getName());
        values.put(DataBaseHelper.COLUMN_LOADPATH, city.getLoadPath());
        values.put(DataBaseHelper.COLUMN_ISLOADMAP, city.getIsLoadMap());
        values.put(DataBaseHelper.CITY_COUNTRY_ID, city.getCountry().getId());

        long result = database.update(DataBaseHelper.TABLE_CITY, values,
                WHERE_ID_EQUALS,
                new String[] { String.valueOf(city.getId()) });
        Log.d(TAG, "Update city=" + city.getName());
        return result;

    }

    public int deleteCity(City city) {
        Log.d(TAG, "Delete country=" + city.getName());
        return database.delete(DataBaseHelper.TABLE_CITY,
                WHERE_ID_EQUALS, new String[] {String.valueOf(city.getId())});
    }

    public City getCity(int id) {
        SQLiteQueryBuilder queryBuilder = new SQLiteQueryBuilder();
        queryBuilder
                .setTables(DataBaseHelper.TABLE_CITY
                        + " INNER JOIN "
                        + DataBaseHelper.TABLE_COUNTRY
                        + " ON "
                        + DataBaseHelper.CITY_COUNTRY_ID
                        + " = "
                        + (DataBaseHelper.TABLE_COUNTRY + "." + DataBaseHelper.COLUMN_ID));
        queryBuilder.appendWhere(DataBaseHelper.COLUMN_ID + "=" + id);

        Cursor cursor = queryBuilder.query(database, new String[] {
                        DataBaseHelper.TABLE_CITY + "." + DataBaseHelper.COLUMN_ID,
                        DataBaseHelper.TABLE_CITY + "." + DataBaseHelper.COLUMN_NAME,
                        DataBaseHelper.COLUMN_LOADPATH,
                        DataBaseHelper.COLUMN_ISLOADMAP,
                        DataBaseHelper.CITY_COUNTRY_ID,
                        DataBaseHelper.TABLE_COUNTRY + "." + DataBaseHelper.COLUMN_NAME},
                null, null, null, null, null);

        if (cursor.getCount() < 1) {
            cursor.close();
            return null;
        }

        cursor.moveToFirst();
        City city = new City();
        city.setId(cursor.getInt(0));
        city.setName(cursor.getString(1));
        city.setLoadPath(cursor.getString(2));
        city.setIsLoadMap(cursor.getInt(3));
        city.setCountry(new Country(cursor.getInt(4), cursor.getString(5)));

        cursor.close();
        return city;
    }

    public List<City> getCities() {
        List<City> cities = new ArrayList<City>();
        SQLiteQueryBuilder queryBuilder = new SQLiteQueryBuilder();
        queryBuilder
                .setTables(DataBaseHelper.TABLE_CITY
                        + " INNER JOIN "
                        + DataBaseHelper.TABLE_COUNTRY
                        + " ON "
                        + DataBaseHelper.CITY_COUNTRY_ID
                        + " = "
                        + (DataBaseHelper.TABLE_COUNTRY + "." + DataBaseHelper.COLUMN_ID));

        Cursor cursor = queryBuilder.query(database, new String[] {
                        DataBaseHelper.TABLE_CITY + "." + DataBaseHelper.COLUMN_ID,
                        DataBaseHelper.TABLE_CITY + "." + DataBaseHelper.COLUMN_NAME,
                        DataBaseHelper.COLUMN_LOADPATH,
                        DataBaseHelper.COLUMN_ISLOADMAP,
                        DataBaseHelper.CITY_COUNTRY_ID,
                        DataBaseHelper.TABLE_COUNTRY + "." + DataBaseHelper.COLUMN_NAME},
                null, null, null, null, null);

        while (cursor.moveToNext()) {
            City city = new City();
            city.setId(cursor.getInt(0));
            city.setName(cursor.getString(1));
            city.setLoadPath(cursor.getString(2));
            city.setIsLoadMap(cursor.getInt(3));
            city.setCountry(new Country(cursor.getInt(4), cursor.getString(5)));
            cities.add(city);
        }

        cursor.close();
        return cities;
    }

    public List<City> getCitiesByCountryId(int idCountry) {
        List<City> cities = new ArrayList<City>();
        SQLiteQueryBuilder queryBuilder = new SQLiteQueryBuilder();
        queryBuilder
                .setTables(DataBaseHelper.TABLE_CITY
                        + " INNER JOIN "
                        + DataBaseHelper.TABLE_COUNTRY
                        + " ON "
                        + DataBaseHelper.CITY_COUNTRY_ID
                        + " = "
                        + (DataBaseHelper.TABLE_COUNTRY + "." + DataBaseHelper.COLUMN_ID));
        queryBuilder.appendWhere(DataBaseHelper.CITY_COUNTRY_ID + "=" + idCountry);

        Cursor cursor = queryBuilder.query(database, new String[] {
                        DataBaseHelper.TABLE_CITY + "." + DataBaseHelper.COLUMN_ID,
                        DataBaseHelper.TABLE_CITY + "." + DataBaseHelper.COLUMN_NAME,
                        DataBaseHelper.TABLE_CITY + "." + DataBaseHelper.COLUMN_LOADPATH,
                        DataBaseHelper.TABLE_CITY + "." + DataBaseHelper.COLUMN_ISLOADMAP,
                        DataBaseHelper.CITY_COUNTRY_ID,
                        DataBaseHelper.TABLE_COUNTRY + "." + DataBaseHelper.COLUMN_NAME,
                        DataBaseHelper.TABLE_COUNTRY + "." + DataBaseHelper.COLUMN_LOADPATH},
                null, null, null, null,
                DataBaseHelper.TABLE_CITY + "." + DataBaseHelper.COLUMN_NAME + " ASC");

        if (cursor.getCount() < 1) {
            cursor.close();
            return null;
        }

        while (cursor.moveToNext()) {
            City city = new City();
            city.setId(cursor.getInt(0));
            city.setName(cursor.getString(1));
            city.setLoadPath(cursor.getString(2));
            city.setIsLoadMap(cursor.getInt(3));
            city.setCountry(new Country(cursor.getInt(4), cursor.getString(5)));
            cities.add(city);
        }

        cursor.close();
        return cities;
    }

}

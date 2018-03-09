package com.osm.downloadmaps.activity;

import android.app.DownloadManager;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteException;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.osm.downloadmaps.MapDownloadManager;
import com.osm.downloadmaps.R;
import com.osm.downloadmaps.adapters.MapsAdapter;
import com.osm.downloadmaps.db.ContinentDAO;
import com.osm.downloadmaps.interfaces.Region;
import com.osm.downloadmaps.model.Continent;
import com.osm.downloadmaps.utils.MemoryUtil;
import com.osm.downloadmaps.utils.RegionsXmlParser;

import org.xmlpull.v1.XmlPullParserException;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.lang.ref.WeakReference;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity {
    private static final String EXTRA_LIST_CONTINENT = "list_continent";

    private static List<Region> continents;

    private DownloadReceiver downloadReceiver;
    private static GetContinentsTask continentsTask;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        initialProgressBarMemory();

        if (savedInstanceState != null) {
            continents = savedInstanceState.getParcelableArrayList(EXTRA_LIST_CONTINENT);
        }

        CreateDBRegionsTask createDBRegionsTask = null;

        if (!checkDataBase()) {
            ContinentDAO continentDAO = new ContinentDAO(MainActivity.this);
            createDBRegionsTask = new CreateDBRegionsTask(MainActivity.this, continentDAO);
            createDBRegionsTask.execute();
        }

        if (continents == null) {
            if (createDBRegionsTask == null && continentsTask == null) {
                ContinentDAO continentDAO = new ContinentDAO(MainActivity.this);
                continentsTask = new GetContinentsTask(MainActivity.this, continentDAO);
                continentsTask.execute();
            }
        } else {
            buildAdapter(MainActivity.this);
        }

        MapDownloadManager mapDownloadManager = MapDownloadManager.getDownloadManager(getApplicationContext());
        IntentFilter downloadCompleteIntent = new IntentFilter(DownloadManager.ACTION_DOWNLOAD_COMPLETE);
        downloadReceiver = new DownloadReceiver(getApplicationContext(), mapDownloadManager);

        registerReceiver(downloadReceiver, downloadCompleteIntent);
    }

    @Override
    public void onPause() {
        super.onPause();
        unregisterReceiver(downloadReceiver);
    }

    @Override
    public void onSaveInstanceState(Bundle savedInstanceState) {
        super.onSaveInstanceState(savedInstanceState);
        savedInstanceState.putParcelableArrayList(EXTRA_LIST_CONTINENT, (ArrayList<Region>) continents);
    }


    private boolean checkDataBase() {

        SQLiteDatabase checkDB = null;

        try {
            File database = new File("/data/data/" + getPackageName() + "/databases/regions.db");

            if (database.exists()) {
                Log.i("Database", "Found");
                String myPath = database.getPath();
                Log.i("Database Path", myPath);
                checkDB = SQLiteDatabase.openDatabase(myPath, null, SQLiteDatabase.OPEN_READONLY);

            }
            else {
                Log.i("Database", "Not Found");
            }

        } catch(SQLiteException e) {
            Log.i("Database", "Not Found");
        } finally {

            if(checkDB != null) {
                checkDB.close();
            }
        }

        return checkDB != null;
    }


    private void initialProgressBarMemory() {
        MemoryUtil memoryUtil = new MemoryUtil(MainActivity.this);
        ProgressBar progressBarMemory = (ProgressBar) findViewById(R.id.progress_memory);
        TextView txtFreeMemory = (TextView) findViewById(R.id.txtFreeMemory);

        long freeMemoryGb = memoryUtil.getAvailableExternalMemorySize();
        String freeMemory = String.valueOf(
                new BigDecimal((double) freeMemoryGb/(1024*1024*1024))
                        .setScale(2, RoundingMode.HALF_UP)
                        .doubleValue());
        txtFreeMemory.setText(getResources().getString(R.string.free_memory, freeMemory));

        long totalMemory = memoryUtil.getTotalExternalMemorySize();
        int percentUsableMemory = (int) (((double) (totalMemory - freeMemoryGb)/totalMemory) * 100);
        progressBarMemory.setProgress(percentUsableMemory);
    }

    private static class CreateDBRegionsTask extends AsyncTask<Void, Void, Void> {

        private WeakReference<Context> mContext;
        private ContinentDAO continentDAO;

        CreateDBRegionsTask(Context context, ContinentDAO continentDAO) {
            mContext = new WeakReference<>(context);
            this.continentDAO = continentDAO;
        }

        @Override
        protected Void doInBackground(Void... arg0) {
            try {
                InputStream inputStream = mContext.get().getApplicationContext()
                        .getAssets().open("regions.xml");
                new RegionsXmlParser(mContext.get(), continentDAO).parse(inputStream);
                inputStream.close();
            } catch (XmlPullParserException | IOException e) {
                e.printStackTrace();
            }
            return null;
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            continentsTask = new GetContinentsTask(mContext.get(), continentDAO);
            continentsTask.execute();
        }

    }

    private static class GetContinentsTask extends AsyncTask<Void, Void, Void> {

        private WeakReference<Context> mContext;
        private ContinentDAO continentDAO;

        GetContinentsTask(Context context, ContinentDAO continentDAO) {
            mContext = new WeakReference<>(context);
            this.continentDAO = continentDAO;
        }

        @SuppressWarnings("unchecked")
        @Override
        protected Void doInBackground(Void... arg0) {
            continents = (List<Region>) ((List<?>)continentDAO.getContinents());
            return null;
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            buildAdapter(mContext.get());
        }

    }

    private static void buildAdapter(final Context context) {
        ListView listContinent = (ListView) ((MainActivity) context).findViewById(R.id.lvContinent);
        MapsAdapter mapsAdapter = new MapsAdapter(context, continents, true);
        listContinent.setAdapter(mapsAdapter);
        listContinent.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Intent intent = new Intent(context, RegionActivity.class);
                intent.putExtra("id_continent",
                        ((Continent) parent.getItemAtPosition(position)).getId());
                context.startActivity(intent);
            }
        });
    }

}

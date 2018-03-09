package com.osm.downloadmaps.activity;

import android.app.DownloadManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.os.AsyncTask;
import android.support.v4.app.FragmentActivity;
import android.util.Log;
import android.util.LongSparseArray;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.osm.downloadmaps.MapDownloadManager;
import com.osm.downloadmaps.R;
import com.osm.downloadmaps.db.CityDAO;
import com.osm.downloadmaps.db.CountryDAO;
import com.osm.downloadmaps.interfaces.Region;
import com.osm.downloadmaps.model.City;
import com.osm.downloadmaps.model.Country;

import java.lang.ref.WeakReference;

public class DownloadReceiver extends BroadcastReceiver {

    private static MapDownloadManager mapDownloadManager;
    private WeakReference<Context> mContext;

    DownloadReceiver(Context context, MapDownloadManager manager) {
        this.mContext = new WeakReference<>(context);
        mapDownloadManager = manager;
    }


    @Override
    public void onReceive(Context context, Intent intent) {
        if (DownloadManager.ACTION_DOWNLOAD_COMPLETE.equals(intent.getAction())) {

            long downloadId = intent.getLongExtra(
                    DownloadManager.EXTRA_DOWNLOAD_ID, 0);

            DownloadManager.Query query = new DownloadManager.Query();
            int runningDownloads = mapDownloadManager
                    .query(query.setFilterByStatus(DownloadManager.STATUS_PAUSED
                            | DownloadManager.STATUS_PENDING
                            | DownloadManager.STATUS_RUNNING))
                    .getCount();

            query = new DownloadManager.Query();
            Cursor cursor = mapDownloadManager.query(query.setFilterById(downloadId));

            FragmentActivity fragmentActivity = null;

            if (mContext.get() instanceof RegionActivity) {
                fragmentActivity = ((RegionActivity) mContext.get()).getHostFragment().getActivity();
            }

            // Progress bar visible gone if cancel download latest
            if (cursor == null || cursor.getCount() == 0) {
                Log.d(RegionsFragment.class.getName(), "Not more downloading");
                if (mContext.get() != null && mContext.get() instanceof RegionActivity) {
                    fragmentActivity.findViewById(R.id.linearLoad).setVisibility(View.GONE);
                }
                return;
            }

            cursor.moveToFirst();
            int status = cursor.getInt(cursor.getColumnIndex(DownloadManager.COLUMN_STATUS));

            if (runningDownloads == 0 && mContext.get() instanceof RegionActivity &&
                    status == DownloadManager.STATUS_SUCCESSFUL)
            {
                ProgressBar progressBarTotal = (ProgressBar)fragmentActivity.findViewById(R.id.progress_loading);
                progressBarTotal.setProgress(100);

                ((TextView) fragmentActivity.findViewById(R.id.txtDownloadindPercent)).setText(
                        mContext.get().getResources().getString(R.string.percent, 100));
            }

            String title = cursor.getString(cursor.getColumnIndex(DownloadManager.COLUMN_TITLE));
            cursor.close();
            Toast.makeText(context, "Completed download Region = " + title, Toast.LENGTH_SHORT).show();

            LongSparseArray<Region> mapRegions = mapDownloadManager.getMapRegions();
            if (status == DownloadManager.STATUS_SUCCESSFUL &&
                    mapRegions != null && mapRegions.size() > 0) {

                Region region = mapDownloadManager.getRegionByLoadId(downloadId);

                if (region.getCountry() == null) {
                    Country country = ((Country) region);
                    country.setIsLoadMap(1);
                    new UpdateRegionTask(context).execute(country);
                }
                else {
                    City city = ((City) region);
                    city.setIsLoadMap(1);
                    new UpdateRegionTask(context).execute(city);
                }

                Log.d(DownloadReceiver.class.getName(), "Change isLoadMap=" + region.getIsLoadMap());
                mapDownloadManager.updateMapsAdapter();
                mapRegions.delete(downloadId);
            }
        }
    }

    private static class UpdateRegionTask extends AsyncTask<Region, Void, Void> {

        private WeakReference<Context> mContext;

        UpdateRegionTask(Context context) {
            mContext = new WeakReference<>(context);
        }

        @Override
        protected Void doInBackground(Region... args) {
            if (args[0] instanceof Country) {
                CountryDAO countryDAO = new CountryDAO(mContext.get());
                countryDAO.update((Country) args[0]);
            }
            else if (args[0] instanceof City){
                CityDAO cityDAO = new CityDAO(mContext.get());
                cityDAO.update((City) args[0]);
            }

            return null;
        }

    }
}

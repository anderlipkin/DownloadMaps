package com.osm.downloadmaps;

import android.app.DownloadManager;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.os.Environment;
import android.os.Handler;
import android.support.v4.app.FragmentActivity;
import android.util.Log;
import android.util.LongSparseArray;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.osm.downloadmaps.activity.HostFragment;
import com.osm.downloadmaps.activity.RegionActivity;
import com.osm.downloadmaps.interfaces.Region;
import com.osm.downloadmaps.utils.RegionUtil;

import java.io.File;
import java.lang.ref.WeakReference;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public final class MapDownloadManager {

    private static final int PROGRESS_DELAY = 1000;
    private Handler handler = new Handler();
    private boolean isProgressCheckerRunning = false;
    private static int globalPercentProgress;
    private static String nameDownloadingMap;

    private static volatile MapDownloadManager mapDownloadManager;

    private static DownloadManager manager;
    private static WeakReference<Context> mContext;
    private ArrayAdapter<Region> mapAdapter;
    private List<? extends Region> listOfRegion;
    private LongSparseArray<Region> mapRegionsForReceiver;

    private MapDownloadManager(Context context, LongSparseArray<Region> mapRegions) {
        mContext = new WeakReference<>(context);
        manager = (DownloadManager) context.getApplicationContext().getSystemService(Context.DOWNLOAD_SERVICE);
        if (mapRegions != null) {
            mapRegionsForReceiver = mapRegions;
        } else {
            mapRegionsForReceiver = new LongSparseArray<>();
        }
    }

    public static MapDownloadManager getDownloadManager(Context context) {
        LongSparseArray<Region> map = null;
        if (mapDownloadManager == null || !context.equals(mContext.get())) {
            synchronized (MapDownloadManager.class) {
                if (mapDownloadManager == null || !context.equals(mContext.get())) {
                    if (mapDownloadManager != null) {
                        map = mapDownloadManager.mapRegionsForReceiver;

                    }
                    mapDownloadManager = new MapDownloadManager(context, map);
                }
            }
        }
        return mapDownloadManager;
    }

    public LongSparseArray<Region> getMapRegions() {
        return mapRegionsForReceiver;
    }

    public Cursor query(DownloadManager.Query q) {
        return manager.query(q);
    }

    public long startDownload(Region region, String url) {
        DownloadManager.Request request = new DownloadManager.Request(Uri.parse(url));

        request.setAllowedNetworkTypes(DownloadManager.Request.NETWORK_WIFI | DownloadManager.Request.NETWORK_MOBILE);
        request.setTitle(region.getName());
        request.setVisibleInDownloadsUi(false);
        request.setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE); //TODO: change to HIDDEN
        request.setDestinationInExternalFilesDir(mContext.get(),
                Environment.DIRECTORY_DOWNLOADS,
                File.separator + "maps" + File.separator + region.getName());

        long idLoad = manager.enqueue(request);
        mapRegionsForReceiver.append(idLoad, region);

        if (!isProgressCheckerRunning) {
            startProgressChecker();
        }

        Log.d(MapDownloadManager.class.getName(), "Count downloading = " + mapRegionsForReceiver.size());

        return idLoad;
    }


    public void removeDownloadById(long downloadId) {
        mapRegionsForReceiver.delete(downloadId);
        manager.remove(downloadId);
    }


    private void checkProgress() {
        int countDownloading = 0;
        Map<String, UpdateHolder> map = new HashMap<String, UpdateHolder>();
        nameDownloadingMap = "";

        DownloadManager.Query q = new DownloadManager.Query();

        q.setFilterByStatus(~(DownloadManager.STATUS_FAILED | DownloadManager.STATUS_SUCCESSFUL));
        Cursor cursor = manager.query(q);

        if (!cursor.moveToFirst()) {
            cursor.close();
            stopProgressChecker();
            return;
        }

        long globalDownloaded = 0L;
        long globalTotal = 0L;

        do {
            countDownloading++;
            int downloadId = cursor.getInt(cursor.getColumnIndex(DownloadManager.COLUMN_ID));
            String title = cursor.getString(cursor.getColumnIndex(DownloadManager.COLUMN_TITLE));
            int status = cursor.getInt(cursor.getColumnIndex(DownloadManager.COLUMN_STATUS));
            long downloaded = cursor.getInt(cursor.getColumnIndex(DownloadManager.COLUMN_BYTES_DOWNLOADED_SO_FAR));
            long total = cursor.getInt(cursor.getColumnIndex(DownloadManager.COLUMN_TOTAL_SIZE_BYTES));
            int percentDownload = (int) ((downloaded * 100L) / total);

            globalDownloaded += downloaded;
            globalTotal += total;

            UpdateHolder holder = new UpdateHolder();
            holder.downloadId = downloadId;
            holder.progress = percentDownload;
            holder.status = status;
            map.put(title, holder);

            if (countDownloading > 1 && !nameDownloadingMap.equals("maps")) {
                nameDownloadingMap = "maps";
            } else if (nameDownloadingMap.isEmpty()){
                nameDownloadingMap = RegionUtil.getNormalName(title);
            }

        } while (cursor.moveToNext());

        cursor.close();

        globalPercentProgress = (int) ((globalDownloaded * 100L) / globalTotal);

        if (mContext.get() instanceof RegionActivity) {

            ((RegionActivity) mContext.get()).runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    HostFragment hostFragment = ((RegionActivity) mContext.get()).getHostFragment();

                    if (hostFragment != null) {
                        FragmentActivity hostActivity = hostFragment.getActivity();

                        if (hostActivity.findViewById(R.id.linearLoad).getVisibility() == View.GONE) {
                            hostActivity.findViewById(R.id.linearLoad).setVisibility(View.VISIBLE);
                        }
                        ProgressBar progressBarTotal = (ProgressBar) hostActivity.findViewById(R.id.progress_loading);
                        progressBarTotal.setProgress(globalPercentProgress);

                        ((TextView) hostActivity.findViewById(R.id.txtDownloadingMap)).setText(
                                hostActivity
                                        .getResources()
                                        .getString(R.string.downloading_name_map, nameDownloadingMap));

                        ((TextView) hostActivity.findViewById(R.id.txtDownloadindPercent)).setText(
                                hostActivity
                                        .getResources()
                                        .getString(R.string.percent, globalPercentProgress));

                    }
                }
            });
        }

        // TODO: progressBar for each list download
        for (Region region : listOfRegion) {
            UpdateHolder holder = map.get(region.getName());

            if (holder != null) {
                if (region.downloadProgress != holder.progress) {
                    region.downloadProgress = holder.progress;
                }
                if (region.downloadStatus != holder.status) {
                    region.downloadStatus = holder.status;
                }
                if (region.downloadId != holder.downloadId) {
                    region.downloadId = holder.downloadId;
                }
            }
            else {
                if (region.downloadProgress != 0) {
                    region.downloadProgress = 0;
                }
                if (region.downloadStatus != -1) {
                    region.downloadStatus = -1;
                }
                if (region.downloadId != -1L) {
                    region.downloadId = -1L;
                }
            }
        }

        updateMapsAdapter();
    }

    public Region getRegionByLoadId(long downloadId) {
        for (Region region: listOfRegion) {
            if (region.downloadId == downloadId) {
                return region;
            }
        }
        return null;
    }

    public void updateMapsAdapter() {
        if (mContext.get() instanceof RegionActivity) {
            ((RegionActivity) mContext.get()).runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    mapAdapter.notifyDataSetChanged();
                }
            });
        }
    }

    private void startProgressChecker() {
        if (!isProgressCheckerRunning) {
            progressChecker.run();
            isProgressCheckerRunning = true;
        }
    }

    public void startProgressChecker(List<? extends Region> regions, ArrayAdapter<Region> mapAdapter) {
        listOfRegion = regions;
        this.mapAdapter = mapAdapter;
        if (!isProgressCheckerRunning) {
            progressChecker.run();
            isProgressCheckerRunning = true;
        }
    }

    public void stopProgressChecker() {
        handler.removeCallbacks(progressChecker);
        isProgressCheckerRunning = false;
    }

    private Runnable progressChecker = new Runnable() {
        @Override
        public void run() {
            try {
                checkProgress();
            } finally {
                handler.postDelayed(progressChecker, PROGRESS_DELAY);
            }
        }
    };

    private class UpdateHolder {
        int status;
        int progress;
        long downloadId;
    }

}

package com.osm.downloadmaps.activity;

import android.app.DownloadManager;
import android.content.Context;
import android.content.IntentFilter;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.Parcelable;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ListView;

import com.osm.downloadmaps.MapDownloadManager;
import com.osm.downloadmaps.R;
import com.osm.downloadmaps.adapters.MapsAdapter;
import com.osm.downloadmaps.db.CityDAO;
import com.osm.downloadmaps.interfaces.Region;
import com.osm.downloadmaps.model.City;
import com.osm.downloadmaps.utils.RegionUtil;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.List;

public class RegionsFragment extends Fragment {

    private static final String EXTRA_IS_COUNTRY ="is_country";
    private static final String EXTRA_LIST_COUNTRY ="list_country";
    private static final String EXTRA_LIST_REGIONS ="list_regions";

    private static boolean isCountry;
    private static List<Region> regions;
    private static MapDownloadManager mapDownloadManager;
    private DownloadReceiver downloadReceiver;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        isCountry = getArguments().getBoolean(EXTRA_IS_COUNTRY);

        if (isCountry) {
            regions = getArguments().getParcelableArrayList(EXTRA_LIST_COUNTRY);
        } else {
            regions = getArguments().getParcelableArrayList(EXTRA_LIST_REGIONS);
        }

        //mapDownloadManager = MapDownloadManager.getDownloadManager(getActivity(), regions);
        mapDownloadManager = MapDownloadManager.getDownloadManager(getActivity());
        IntentFilter downloadCompleteIntent = new IntentFilter(DownloadManager.ACTION_DOWNLOAD_COMPLETE);
        downloadReceiver = new DownloadReceiver(getActivity(), mapDownloadManager);

        getActivity().registerReceiver(downloadReceiver, downloadCompleteIntent);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.maps_list, container, false);

        final MapsAdapter mapsAdapter = new MapsAdapter(getActivity(), regions, false);

        ListView lv = (ListView) v.findViewById(R.id.lvMaps);
        lv.setAdapter(mapsAdapter);

        mapDownloadManager.startProgressChecker(regions, mapsAdapter);

        if (isCountry) {
            lv.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                @Override
                public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                    Region region = (Region) parent.getItemAtPosition(position);
                    Log.d(RegionsFragment.class.getName(),"Click country=" + region.getName() +"_" + region.getLoadPath());

                    if (region.getLoadPath() == null || region.getLoadPath().isEmpty()) {
                        new GetCityTask(getActivity()).execute(region.getId());
                    }
                    else if (region.getIsLoadMap() == 0 && region.downloadStatus != DownloadManager.STATUS_RUNNING) {

                        String url = RegionUtil.getLoadPathForCounty(region.getLoadPath());
                        region.downloadId = mapDownloadManager.startDownload(region, url);

                        Log.d(RegionsFragment.class.getName(),"Click country ulr=" + url + ", idLoad=");
                        new Handler().postDelayed(new Runnable() {
                            @Override
                            public void run() {
                                mapsAdapter.notifyDataSetChanged();
                            }
                        }, 1000);
                    }
                }
            });
        } else {
            lv.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                @Override
                public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                    Region region = (Region) parent.getItemAtPosition(position);

                    if (region.getIsLoadMap() == 0 && region.downloadStatus != DownloadManager.STATUS_RUNNING
                            && region.getLoadPath() != null && !region.getLoadPath().isEmpty()) {

                        String url = RegionUtil.getLoadPathForCity(region);
                        Log.d(RegionsFragment.class.getName(),"Click city ulr=" + url);
                        region.downloadId = mapDownloadManager.startDownload(region, url);

                        new Handler().postDelayed(new Runnable() {
                            @Override
                            public void run() {
                                mapsAdapter.notifyDataSetChanged();
                            }
                        }, 1000);
                    }
                }
            });
        }
        return v;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        mapDownloadManager.stopProgressChecker();
        getActivity().unregisterReceiver(downloadReceiver);
    }


    public static RegionsFragment newInstance(Bundle args) {
        RegionsFragment fragment = new RegionsFragment();
        fragment.setArguments(args);
        return fragment;
    }


    private static class GetCityTask extends AsyncTask<Integer, Void, List<City>> {

        private WeakReference<Context> mContext;

        GetCityTask(Context context) {
            mContext = new WeakReference<>(context);
        }

        @Override
        protected List<City> doInBackground(Integer... args) {
            CityDAO cityDAO = new CityDAO(mContext.get());
            return cityDAO.getCitiesByCountryId(args[0]);
        }

        @Override
        protected void onPostExecute(List<City> cities) {
            super.onPostExecute(cities);
            if (cities == null) {
                return;
            }

            Bundle args = new Bundle();
            args.putParcelableArrayList(EXTRA_LIST_REGIONS, (ArrayList<? extends Parcelable>) cities);
            args.putBoolean(EXTRA_IS_COUNTRY, false);

            ((RegionActivity) mContext.get()).openNewContentFragment(
                    RegionsFragment.newInstance(args), "regions");
        }
    }

/*
    private BroadcastReceiver downloadReceiver = new BroadcastReceiver() {
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

                if (cursor == null || cursor.getCount() == 0) {
                    Log.d(RegionsFragment.class.getName(), "Not more downloading");
                    getActivity().findViewById(R.id.linearLoad).setVisibility(View.GONE);
                    return;
                }

                cursor.moveToFirst();

                if (runningDownloads == 0 &&
                        cursor.getInt(cursor.getColumnIndex(DownloadManager.COLUMN_STATUS)) == DownloadManager.STATUS_SUCCESSFUL)
                {
                    ProgressBar progressBarTotal = (ProgressBar) getActivity().findViewById(R.id.progress_loading);
                    progressBarTotal.setProgress(100);

                    ((TextView) getActivity().findViewById(R.id.txtDownloadindPercent)).setText(
                            getResources().getString(R.string.percent, 100));
                }

                String title = cursor.getString(cursor.getColumnIndex(DownloadManager.COLUMN_TITLE));
                cursor.close();

                for (Region region: regions) {
                    if (region.downloadId == downloadId) {
                        if (region.getCountry() == null) {
                            Country country = ((Country) region);
                            country.setIsLoadMap(1);
                            new UpdateRegionTask(getActivity()).execute(country);
                        }
                        else {
                            City city = ((City) region);
                            city.setIsLoadMap(1);
                            new UpdateRegionTask(getActivity()).execute(city);
                        }
                    }
                }

                Toast.makeText(getContext(), "Completed download Region = " + title, Toast.LENGTH_SHORT).show();

            }
        }
    };
*/
}

package com.osm.downloadmaps.activity;

import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Parcelable;
import android.support.v4.app.Fragment;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.MenuItem;

import com.osm.downloadmaps.R;
import com.osm.downloadmaps.adapters.CustomPagerAdapter;
import com.osm.downloadmaps.db.CountryDAO;
import com.osm.downloadmaps.model.Country;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.List;

public class RegionActivity extends AppCompatActivity {

    private static HostFragment hostFragment;
    private static final String EXTRA_ID_CONTINENT = "id_continent";
    private static final String EXTRA_LIST_COUNTRY = "list_country";
    private static List<Country> countries;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_pager);

        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }

        ViewPager viewPager = (ViewPager) findViewById(R.id.viewpager);
        viewPager.setOffscreenPageLimit(1);

        int idContinent = getIntent().getIntExtra(EXTRA_ID_CONTINENT, 0);

        new GetCountryTask(RegionActivity.this, viewPager).execute(idContinent);
    }


    private static class GetCountryTask extends AsyncTask<Integer, Void, Void> {

        private WeakReference<Context> mContext;
        private WeakReference<ViewPager> viewPager;

        GetCountryTask(Context context, ViewPager viewPager) {
            mContext = new WeakReference<>(context);
            this.viewPager = new WeakReference<>(viewPager);
        }

        @Override
        protected Void doInBackground(Integer... args) {
            CountryDAO countryDAO = new CountryDAO(mContext.get());
            countries = countryDAO.getCountriesByContinentId(args[0]);
            return null;
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            Bundle args = new Bundle();
            args.putBoolean("is_country", true);
            args.putParcelableArrayList(EXTRA_LIST_COUNTRY, (ArrayList<? extends Parcelable>) countries);

            CustomPagerAdapter customPagerAdapter = new CustomPagerAdapter(
                    ((RegionActivity) mContext.get()).getSupportFragmentManager());

            customPagerAdapter.initializeTabs(RegionsFragment.newInstance(args));
            viewPager.get().setAdapter(customPagerAdapter);
            hostFragment = (HostFragment) customPagerAdapter.getItem(viewPager.get().getCurrentItem());

            Log.d(RegionActivity.class.getName(), "RegionActivity = "+ mContext.get().hashCode());
        }
    }

    @Override
    public void onBackPressed() {
        Fragment host = hostFragment.getFragmentManager().findFragmentByTag("host");
        Fragment regions = hostFragment.getFragmentManager().findFragmentByTag("regions");

        if (host != null && host.isVisible()) {
            startActivityAfterCleanup(MainActivity.class);
        }
        else if (regions != null && regions.isVisible()) {
            hostFragment.replaceFragment(hostFragment.getBackTraceFragment(), "host");
        }
        else {
            super.onBackPressed();
        }
    }

    public void openNewContentFragment(RegionsFragment fragment, String tag) {;
        hostFragment.replaceFragment(fragment, tag);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem menuItem) {
        switch (menuItem.getItemId()) {
            case android.R.id.home:
                onBackPressed();
                return true;

            default:
                return (super.onOptionsItemSelected(menuItem));
        }
    }


    private void startActivityAfterCleanup(Class<?> cls) {
        Intent intent = new Intent(getApplicationContext(), cls);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        startActivity(intent);
    }

    public HostFragment getHostFragment() {
        return hostFragment;
    }

}

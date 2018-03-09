package com.osm.downloadmaps.activity;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.osm.downloadmaps.R;


public class HostFragment extends Fragment {
    private Fragment fragment;
    private static Fragment backTraceFragment;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        super.onCreateView(inflater, container, savedInstanceState);
        View view = inflater.inflate(R.layout.fragment_region, container, false);
        if (fragment != null) {
            replaceFragment(fragment, "host");
        }

        return view;
    }

    public void replaceFragment(Fragment fragment, String tag) {
        backTraceFragment = this.fragment;
        getFragmentManager().beginTransaction().replace(R.id.hosted_fragment, fragment, tag).commit();
    }

    public static HostFragment newInstance(Fragment fragment) {
        HostFragment hostFragment = new HostFragment();
        hostFragment.fragment = fragment;
        return hostFragment;
    }

    public Fragment getBackTraceFragment() {
        return backTraceFragment;
    }

}

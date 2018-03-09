package com.osm.downloadmaps.interfaces;

import android.os.Parcelable;

import com.osm.downloadmaps.model.Country;

public abstract class Region implements Parcelable {
    public long downloadId = -1L;
    public int downloadProgress = 0;
    public int downloadStatus = -1;

    public abstract int getId();

    public abstract String getName();

    public String getLoadPath() {
        return null;
    }

    public int getIsLoadMap() {
        return 0;
    }

    public Country getCountry() {
        return null;
    }

}

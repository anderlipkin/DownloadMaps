package com.osm.downloadmaps.utils;


import android.content.Context;
import android.os.Environment;

import java.io.File;

public class MemoryUtil {
    private Context mContext;

    public MemoryUtil(Context context) {
        mContext = context;
    }

    public long getTotalExternalMemorySize() {
        long result = 0;
        if (externalMemoryAvailable()) {
            result = new File(mContext.getExternalFilesDir(null).toString()).getTotalSpace();
        }
        return result;
    }

    public long getAvailableExternalMemorySize() {
        long result = 0;
        if (externalMemoryAvailable()) {
            result = new File(mContext.getExternalFilesDir(null).toString()).getFreeSpace();
        }
        return result;
    }

    private boolean externalMemoryAvailable() {
        return Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED);
    }

}

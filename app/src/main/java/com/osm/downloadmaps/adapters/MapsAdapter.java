package com.osm.downloadmaps.adapters;

import android.app.DownloadManager;
import android.content.Context;
import android.content.res.Resources;
import android.graphics.Color;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.osm.downloadmaps.MapDownloadManager;
import com.osm.downloadmaps.R;
import com.osm.downloadmaps.interfaces.Region;
import com.osm.downloadmaps.utils.RegionUtil;

import java.util.List;

public class MapsAdapter extends ArrayAdapter<Region> {
    private boolean isContinent;
    private List<Region> mRegions;
    private Context mContext;

    public static class ViewHolder{
        ImageView imgMap;
        TextView txtName;
        ImageView imgLoad;
        ProgressBar downloadProgressBar; // TODO: add for each Map below
    }

    public MapsAdapter(@NonNull Context context, @NonNull List<Region> regions, boolean isContinent) {
        super(context, 0, regions);
        this.isContinent = isContinent;
        mRegions = regions;
        mContext = context;
    }

    @NonNull
    @Override
    public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
        Resources res = getContext().getResources();
        final ViewHolder viewHolder;
        final Region region = getItem(position);

        if (convertView == null) {
            viewHolder = new ViewHolder();
            convertView = LayoutInflater.from(getContext())
                    .inflate(R.layout.list_item, parent, false);

            viewHolder.imgMap = (ImageView) convertView.findViewById(R.id.imgMap);
            viewHolder.txtName = (TextView) convertView.findViewById(R.id.txtName);
            viewHolder.imgLoad = (ImageView) convertView.findViewById(R.id.imgLoad);
            viewHolder.downloadProgressBar = (ProgressBar) convertView.findViewById(R.id.progress_item_download);

            // Cache the viewHolder object inside the fresh view
            convertView.setTag(viewHolder);
        } else {
            viewHolder = (ViewHolder) convertView.getTag();
        }

        if (isContinent) {
            viewHolder.imgMap.setImageResource(R.drawable.ic_world_globe_dark);
        } else {
            viewHolder.imgMap.setImageResource(R.drawable.ic_map);
        }

        viewHolder.txtName.setText(RegionUtil.getNormalName(region.getName()));

        if (region.getLoadPath() == null || region.getLoadPath().isEmpty()) {
            viewHolder.imgLoad.setVisibility(View.GONE);
        }
        else if (region.getIsLoadMap() != 0) {
            viewHolder.imgLoad.setImageResource(R.drawable.ic_action_import);
            viewHolder.imgLoad.setColorFilter(Color.GREEN);
            viewHolder.imgLoad.setVisibility(View.VISIBLE);
            viewHolder.imgLoad.setTag(Integer.valueOf(R.drawable.ic_action_import));
            viewHolder.downloadProgressBar.setVisibility(View.GONE);
        }
        else if (region.downloadStatus == DownloadManager.STATUS_RUNNING ||
                region.downloadStatus == DownloadManager.STATUS_PENDING) {

            viewHolder.imgLoad.setImageResource(R.drawable.ic_action_remove_dark);
            viewHolder.imgLoad.setTag(Integer.valueOf(R.drawable.ic_action_remove_dark));
            viewHolder.imgLoad.setVisibility(View.VISIBLE);
            viewHolder.downloadProgressBar.setProgress(region.downloadProgress);
            viewHolder.downloadProgressBar.setVisibility(View.VISIBLE);
        }
        else {
            viewHolder.imgLoad.setImageResource(R.drawable.ic_action_import);
            viewHolder.imgLoad.setColorFilter(null);
            viewHolder.imgLoad.setTag(Integer.valueOf(R.drawable.ic_action_import));
            viewHolder.imgLoad.setVisibility(View.VISIBLE);
            viewHolder.downloadProgressBar.setVisibility(View.GONE);
        }

        if (viewHolder.imgLoad.getTag() != null &&
                (Integer) viewHolder.imgLoad.getTag() == R.drawable.ic_action_remove_dark) {

            viewHolder.imgLoad.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    MapDownloadManager.getDownloadManager(mContext).removeDownloadById(region.downloadId);
                    region.downloadStatus = -1;
                    viewHolder.imgLoad.setImageResource(R.drawable.ic_action_import);
                    viewHolder.imgLoad.setTag(Integer.valueOf(R.drawable.ic_action_import));
                    viewHolder.downloadProgressBar.setVisibility(View.GONE);
                    notifyDataSetChanged();
                }
            });
        }

        return convertView;
    }

    public List<Region> getObjects() {
        return mRegions;
    }

}

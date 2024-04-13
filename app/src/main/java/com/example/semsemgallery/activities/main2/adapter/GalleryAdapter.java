package com.example.semsemgallery.activities.main2.adapter;

import static androidx.core.content.ContextCompat.startActivity;

import android.content.Context;
import android.content.Intent;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.LongDef;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import androidx.recyclerview.widget.StaggeredGridLayoutManager;

import com.bumptech.glide.Glide;
import com.example.semsemgallery.R;
import com.example.semsemgallery.activities.base.GridMode;
import com.example.semsemgallery.activities.base.ObservableGridMode;
import com.example.semsemgallery.activities.main2.viewholder.GalleryItem;
import com.example.semsemgallery.activities.main2.viewholder.GalleryItemViewHolder;
import com.example.semsemgallery.activities.pictureview.PictureViewActivity;
import com.example.semsemgallery.models.Picture;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.TreeSet;

public class GalleryAdapter extends RecyclerView.Adapter<GalleryItemViewHolder> {
    private ObservableGridMode<GalleryItem> observableGridMode;
    private Context context;
    //private ArrayList<Picture> dataList;

    private static int findIndexOf(Picture pic, ArrayList<Picture> list) {
        int low = 0;
        int high = list.size() - 1;
        while (low <= high) {
            int mid = (low + high) / 2;
            Picture dataMid = list.get(mid);
            if (dataMid.getPictureId() == pic.getPictureId()) {
                return mid;
            } else if (dataMid.compareTo(pic) < 0) {
                high = mid - 1;
            } else {
                low = mid + 1;
            }
        }

        return -1;
    }

    public GalleryAdapter(Context context, ObservableGridMode<GalleryItem> data) {
        this.context = context;
        observableGridMode = data;
    }

    @NonNull
    @Override
    public GalleryItemViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view;
        if (viewType == GalleryItem.THUMBNAIL) {
            view = LayoutInflater.from(parent.getContext()).inflate(R.layout.component_picture_item, parent, false);
            return new GalleryItemViewHolder(view, observableGridMode, viewType) {
                @Override
                public void clickOnNormalMode(View v) {
                    Intent intent = new Intent(context, PictureViewActivity.class);
                    //intent.putParcelableArrayListExtra("pictureList", dataList);
                    ObservableGridMode<GalleryItem>.DataItem item = observableGridMode.getDataAt(getAbsoluteAdapterPosition());
                    Picture data = (Picture) item.data.getData();
                    intent.putExtra("selectingPic", data);
                    Log.d("Pictures", "ID = " + data.getPictureId());
//                    int pos = findIndexOf(data, dataList);
//                    intent.putExtra("position", pos);
                    startActivity(context, intent, null);
                }
            };
        } else if (viewType == GalleryItem.GROUPDATE) {
            view = LayoutInflater.from(parent.getContext()).inflate(R.layout.component_header_item, parent, false);
            StaggeredGridLayoutManager.LayoutParams layoutParams = (StaggeredGridLayoutManager.LayoutParams) view.getLayoutParams();
            layoutParams.setFullSpan(true);

            return new GalleryItemViewHolder(view, observableGridMode, viewType) {
                @Override
                public void clickOnNormalMode(View v) {
                    return;
                }
            };
        }
        return null;
    }


    @Override
    public void onBindViewHolder(@NonNull GalleryItemViewHolder holder, int position) {
        GalleryItem item = observableGridMode.getDataAt(position).data;
        holder.selector.setChecked(observableGridMode.getDataAt(position).isSelected);
        if (observableGridMode.getCurrentMode() == GridMode.SELECTING) {
            if (item.getType() == GalleryItem.GROUPDATE)
                holder.selector.setVisibility(View.INVISIBLE);
            else holder.selector.setVisibility(View.VISIBLE);
        }
        if (item.getType() == GalleryItem.GROUPDATE) {
            holder.groupDisplayText.setText(item.getDateFormatted());
        } else {
            Glide.with(context).load(((Picture) item.getData()).getPath()).into(holder.thumnail);
            holder.isFav.setVisibility(((Picture) item.getData()).isFav() ? View.VISIBLE : View.INVISIBLE);
        }
    }

    @Override
    public int getItemViewType(int position) {
        return observableGridMode.getDataAt(position).data.getType();
    }

    @Override
    public int getItemCount() {
        return observableGridMode.getDataSize();
    }
}

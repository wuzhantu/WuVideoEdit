package com.example.wuvideoedit;

import android.content.Context;
import android.graphics.Bitmap;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

public class VideoRecyclerViewAdapter extends RecyclerView.Adapter {

    private Context context;
    public VideoRecyclerViewAdapter(Context context) {
        this.context = context;
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.video_timeline_cell, parent, false);
        VideoViewHolder viewHolder = new VideoViewHolder(view);
        return viewHolder;
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        VideoViewHolder viewHolder = (VideoViewHolder) holder;
        EditActivity activity = (EditActivity) context;
        Bitmap bitmap = activity.getBitmap(position);
        viewHolder.imgView.setImageBitmap(bitmap);
    }

    @Override
    public int getItemCount() {
        return 120;
    }

    class VideoViewHolder extends RecyclerView.ViewHolder {

        private ImageView imgView;

        public VideoViewHolder(@NonNull View itemView) {
            super(itemView);
            imgView = itemView.findViewById(R.id.timelineImgView);
        }
    }
}

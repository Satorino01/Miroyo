package com.example.kobayashi_satoru.miroyo.adapter;

import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.DataSource;
import com.bumptech.glide.load.engine.GlideException;
import com.bumptech.glide.request.RequestListener;
import com.bumptech.glide.request.RequestOptions;
import com.bumptech.glide.request.target.Target;
import com.example.kobayashi_satoru.miroyo.R;
import com.example.kobayashi_satoru.miroyo.SquareImageView;
import com.example.kobayashi_satoru.miroyo.listener.OnRecyclerListener;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;


public class videoAdapter extends RecyclerView.Adapter {

    private String PREF_FILE_NAME = "com.example.kobayashi_satoru.miroyo.SendMovieActivity";
    private LayoutInflater mInflater;
    private List<String> videoIDs;
    private HashMap videoMaps;
    private Context mContext;
    private OnRecyclerListener mListener;

    public videoAdapter(Context context, List<String> VideoIDs, HashMap VideoMaps, OnRecyclerListener listener){
        mInflater = LayoutInflater.from(context);
        mContext = context;
        videoIDs = VideoIDs;
        videoMaps = VideoMaps;
        mListener = listener;
    }
    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {
        return new ViewHolder(mInflater.inflate(R.layout.video_item, viewGroup, false));
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder viewHolder, final int position) {
        final ViewHolder videoHolder = (ViewHolder) viewHolder;
        // データ表示
        if (videoIDs != null && videoIDs.size() > position && videoIDs.get(position) != null) {

            String videoThumbnailURL = "https://firebasestorage.googleapis.com/v0/b/miroyo.appspot.com/o/errorvideothumbnail.png?alt=media&token=2a82f01a-5472-473b-97dc-1c6bc2d9476b";
            //Firestoreで アップロードした動画一覧をとってくる
            try{
                String videoName = ((HashMap)videoMaps.get(videoIDs.get(position))).get("VideoName").toString();
                videoHolder.videoNameTxt.setText(videoName);
                String videoPlayTime = ((HashMap)videoMaps.get(videoIDs.get(position))).get("PlayTime").toString();
                videoHolder.videoPlayTimeTxt.setText(videoPlayTime);
                videoThumbnailURL = ((HashMap)videoMaps.get(videoIDs.get(position))).get("ThumbnailURL").toString();
            } catch (NullPointerException e){
                Log.d("",String.valueOf(position));
                Log.d("",String.valueOf(videoIDs.get(position)));
                Log.d("",String.valueOf(videoMaps.get(videoIDs.get(position))));
            }
            Log.d(" onBindViewHolder",String.valueOf(position));
            // DataBean detabean = DataBean.getdata(position);
            RequestOptions options = new RequestOptions()
                    .error(R.drawable.samplemoviethumbnail)//エラー時に読み込む画像のIDやURL
                    .placeholder(R.drawable.samplemoviethumbnail)//ロード開始時に読み込むIDやURL
                    .override(300,300);
            Glide.with(mContext).load(videoThumbnailURL)
                    .apply(options)
                    .listener(createLoggerListener("video_thumbnail"))
                    .into(videoHolder.videoThumbnail);
            //TODO filted　画像の読み込み失敗時　 if(e != null) e.printtrack();
            //TODO ReadyResouce 画像の読み込みが終わって表示される直前によばれるメソッド　imageview.setvisiblity(Visible)
        }

        // クリック処理
        videoHolder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                SharedPreferences sharedPref = mContext.getSharedPreferences(PREF_FILE_NAME, Context.MODE_PRIVATE);
                SharedPreferences.Editor editor =  sharedPref.edit();
                editor.putString("setVideoIDSendMovieActivity", videoIDs.get(position));
                editor.apply();
                mListener.onRecyclerClicked(view, position);
            }
        });
    }

    private RequestListener<Drawable> createLoggerListener(final String match_image) {
        return new RequestListener<Drawable>(){

            @Override
            public boolean onLoadFailed(@Nullable GlideException e, Object model, Target target, boolean isFirstResource) {
                return false;
            }

            @Override
            public boolean onResourceReady(Drawable resource, Object model, Target target, DataSource dataSource, boolean isFirstResource) {
                if (resource instanceof BitmapDrawable) {
                    Bitmap bitmap = ((BitmapDrawable) resource).getBitmap();
                    Log.d("GlideApp",
                            String.format("Ready %s bitmap %,d bytes, size: %d x %d",
                                    match_image,
                                    bitmap.getByteCount(),
                                    bitmap.getWidth(),
                                    bitmap.getHeight()));
                }
                return false;
            }
        };
    }

    @Override
    public int getItemCount() {
        if (videoIDs != null) {
            return videoIDs.size();
        } else {
            return 0;
        }
    }

    @Override
    public void onViewRecycled(@NonNull RecyclerView.ViewHolder holder) {
        super.onViewRecycled(holder);
        //Glide.with(context).clear(videoHolder.iconImageView);
    }

    public class ViewHolder extends RecyclerView.ViewHolder {

        SquareImageView videoThumbnail;
        TextView videoNameTxt;
        TextView videoPlayTimeTxt;


        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            videoThumbnail=itemView.findViewById(R.id.videoThumbnail);
            videoNameTxt=itemView.findViewById(R.id.videoNameTxt);
            videoPlayTimeTxt=itemView.findViewById(R.id.videoPlayTimeTxt);
        }
    }

    public void addItem(int position, String videoID, HashMap videoMap) {
        videoIDs.add(videoID);
        videoMaps.put(videoID,videoMap);
        notifyItemInserted(position);
    }

    public void remove(int position) {
        videoIDs.remove(position);
        notifyItemRemoved(position);
    }
}

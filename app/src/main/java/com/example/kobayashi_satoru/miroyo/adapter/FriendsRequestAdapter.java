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
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.DataSource;
import com.bumptech.glide.load.engine.GlideException;
import com.bumptech.glide.request.RequestListener;
import com.bumptech.glide.request.RequestOptions;
import com.bumptech.glide.request.target.Target;
import com.example.kobayashi_satoru.miroyo.R;
import com.example.kobayashi_satoru.miroyo.SquareImageView;
import com.example.kobayashi_satoru.miroyo.listener.OnRecyclerListener;

import java.util.HashMap;
import java.util.List;


public class FriendsRequestAdapter extends RecyclerView.Adapter {

    private String PREF_FILE_NAME = "com.example.kobayashi_satoru.miroyo.SendMovieActivity";
    private LayoutInflater mInflater;
    private List<String> friendsRequestIDs;
    private HashMap friendsRequestMaps;
    private Context mContext;
    private OnRecyclerListener mListener;

    public FriendsRequestAdapter(Context context, List<String> FriendsRequestIDs, HashMap FriendsRequestMaps, OnRecyclerListener listener){
        mInflater = LayoutInflater.from(context);
        mContext = context;
        friendsRequestIDs = FriendsRequestIDs;
        friendsRequestMaps = FriendsRequestMaps;
        mListener = listener;
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {
        return new ViewHolder(mInflater.inflate(R.layout.friend_item, viewGroup, false));
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder viewHolder, final int position) {
        final ViewHolder friendsRequestHolder = (ViewHolder) viewHolder;
        // データ表示
        if (friendsRequestIDs != null && friendsRequestIDs.size() > position && friendsRequestIDs.get(position) != null) {

            String friendsRequestThumbnailURL = "https://firebasestorage.googleapis.com/v0/b/miroyo.appspot.com/o/errorfriendsRequestthumbnail.png?alt=media&token=2a82f01a-5472-473b-97dc-1c6bc2d9476b";
            //Firestoreで アップロードした動画一覧をとってくる
            try{
                String friendsRequestName = ((HashMap)friendsRequestMaps.get(friendsRequestIDs.get(position))).get("UserName").toString();
                friendsRequestHolder.friendsRequestNameTxt.setText(friendsRequestName);
                String friendsRequestEmailAdress = ((HashMap)friendsRequestMaps.get(friendsRequestIDs.get(position))).get("EmailAdress").toString();
                friendsRequestHolder.friendsRequestEmailAdressTxt.setText(friendsRequestEmailAdress);
                friendsRequestThumbnailURL = ((HashMap)friendsRequestMaps.get(friendsRequestIDs.get(position))).get("ThumbnailURL").toString();

            } catch (NullPointerException e){
                Log.d("",String.valueOf(position));
                Log.d("",String.valueOf(friendsRequestIDs.get(position)));
                Log.d("",String.valueOf(friendsRequestMaps.get(friendsRequestIDs.get(position))));
            }
            Log.d(" onBindViewHolder",String.valueOf(position));
            // DataBean detabean = DataBean.getdata(position);
            RequestOptions options = new RequestOptions()
                    .error(R.drawable.sampleusericon)//エラー時に読み込む画像のIDやURL
                    .placeholder(R.drawable.sampleusericon)//ロード開始時に読み込むIDやURL
                    .override(300,300);
            Glide.with(mContext).load(friendsRequestThumbnailURL)
                    .apply(options)
                    .listener(createLoggerListener("friendsRequest_thumbnail"))
                    .into(friendsRequestHolder.friendsRequestThumbnail);
        }

        // クリック処理
        friendsRequestHolder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
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
        if (friendsRequestIDs != null) {
            return friendsRequestIDs.size();
        } else {
            return 0;
        }
    }

    @Override
    public void onViewRecycled(@NonNull RecyclerView.ViewHolder holder) {
        super.onViewRecycled(holder);
    }

    public class ViewHolder extends RecyclerView.ViewHolder {

        SquareImageView friendsRequestThumbnail;
        TextView friendsRequestNameTxt;
        TextView friendsRequestEmailAdressTxt;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            friendsRequestThumbnail=itemView.findViewById(R.id.friendThumbnail);
            friendsRequestNameTxt=itemView.findViewById(R.id.friendNameTxt);
            friendsRequestEmailAdressTxt =itemView.findViewById(R.id.friendEmailAdressTxt);
        }
    }

    public void addItem(int position, String friendsRequestID, HashMap friendsRequestMap) {
        friendsRequestMaps.put(friendsRequestID,friendsRequestMap);
        friendsRequestIDs.add(friendsRequestID);
        notifyItemInserted(position);
    }

    public void moved(int fromPos, int toPos) {
        String moveID = friendsRequestIDs.get(fromPos);
        friendsRequestIDs.set(fromPos,friendsRequestIDs.get(toPos));
        friendsRequestIDs.set(toPos,moveID);
        notifyItemMoved(fromPos,toPos);
    }

    public void remove(int position) {
        friendsRequestMaps.remove(friendsRequestIDs.get(position));
        friendsRequestIDs.remove(position);
        notifyItemRemoved(position);
    }
}

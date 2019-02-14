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


public class FriendAdapter extends RecyclerView.Adapter {

    private String PREF_FILE_NAME = "com.example.kobayashi_satoru.miroyo.SendMovieActivity";
    private LayoutInflater mInflater;
    private List<String> friendIDs;
    private HashMap friendMaps;
    private Context mContext;
    private OnRecyclerListener mListener;

    public FriendAdapter(Context context, List<String> FriendIDs, HashMap FriendMaps, OnRecyclerListener listener){
        mInflater = LayoutInflater.from(context);
        mContext = context;
        friendIDs = FriendIDs;
        friendMaps = FriendMaps;
        mListener = listener;
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {
        return new ViewHolder(mInflater.inflate(R.layout.friend_item, viewGroup, false));
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder viewHolder, final int position) {
        final ViewHolder friendHolder = (ViewHolder) viewHolder;
        // データ表示
        if (friendIDs != null && friendIDs.size() > position && friendIDs.get(position) != null) {

            String friendThumbnailURL = "https://firebasestorage.googleapis.com/v0/b/miroyo.appspot.com/o/errorfriendthumbnail.png?alt=media&token=2a82f01a-5472-473b-97dc-1c6bc2d9476b";
            //Firestoreで アップロードした動画一覧をとってくる
            try{
                String friendName = ((HashMap)friendMaps.get(friendIDs.get(position))).get("UserName").toString();
                friendHolder.friendNameTxt.setText(friendName);
                String friendEmailAdress = ((HashMap)friendMaps.get(friendIDs.get(position))).get("EmailAdress").toString();
                friendHolder.friendEmailAdressTxt.setText(friendEmailAdress);
                friendThumbnailURL = ((HashMap)friendMaps.get(friendIDs.get(position))).get("ThumbnailURL").toString();

            } catch (NullPointerException e){
                Log.d("",String.valueOf(position));
                Log.d("",String.valueOf(friendIDs.get(position)));
                Log.d("",String.valueOf(friendMaps.get(friendIDs.get(position))));
            }
            Log.d(" onBindViewHolder",String.valueOf(position));
            // DataBean detabean = DataBean.getdata(position);
            RequestOptions options = new RequestOptions()
                    .error(R.drawable.sampleusericon)//エラー時に読み込む画像のIDやURL
                    .placeholder(R.drawable.sampleusericon)//ロード開始時に読み込むIDやURL
                    .override(300,300);
            Glide.with(mContext).load(friendThumbnailURL)
                    .apply(options)
                    .listener(createLoggerListener("friend_thumbnail"))
                    .into(friendHolder.friendThumbnail);
        }

        // クリック処理
        friendHolder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                SharedPreferences sharedPref = mContext.getSharedPreferences(PREF_FILE_NAME, Context.MODE_PRIVATE);
                SharedPreferences.Editor editor =  sharedPref.edit();
                editor.putString("setFriendIDSendVideoActivity", friendIDs.get(position));
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
        if (friendIDs != null) {
            return friendIDs.size();
        } else {
            return 0;
        }
    }

    @Override
    public void onViewRecycled(@NonNull RecyclerView.ViewHolder holder) {
        super.onViewRecycled(holder);
        //Glide.with(context).clear(friendHolder.iconImageView);
    }

    public class ViewHolder extends RecyclerView.ViewHolder {

        SquareImageView friendThumbnail;
        TextView friendNameTxt;
        TextView friendEmailAdressTxt;


        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            friendThumbnail=itemView.findViewById(R.id.friendThumbnail);
            friendNameTxt=itemView.findViewById(R.id.friendNameTxt);
            friendEmailAdressTxt =itemView.findViewById(R.id.friendEmailAdressTxt);
        }
    }

    public void addItem(int position, String friendID, HashMap friendMap) {
        friendMaps.put(friendID,friendMap);
        friendIDs.add(friendID);
        notifyItemInserted(position);
    }

    public void moved(int fromPos, int toPos) {
        String moveID = friendIDs.get(fromPos);
        friendIDs.set(fromPos,friendIDs.get(toPos));
        friendIDs.set(toPos,moveID);
        notifyItemMoved(fromPos,toPos);
    }

    public void remove(int position) {
        friendMaps.remove(friendIDs.get(position));
        friendIDs.remove(position);
        notifyItemRemoved(position);
    }
}

package com.example.kobayashi_satoru.miroyo;

import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.net.ConnectivityManager;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.NavigationView;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.DataSource;
import com.bumptech.glide.load.engine.GlideException;
import com.bumptech.glide.request.RequestListener;
import com.bumptech.glide.request.RequestOptions;
import com.bumptech.glide.request.target.Target;
import com.example.kobayashi_satoru.miroyo.receiver.NetworkReceiver;
import com.facebook.login.LoginManager;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.messaging.FirebaseMessaging;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class SendVideoActivity extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener, NetworkReceiver.OnNetworkStateChangedListener{

    private final String PREF_FILE_NAME = "com.example.kobayashi_satoru.miroyo.SendMovieActivity";
    private String TAG = "SendMovieActivity";
    private String myUserID;
    private String myUserName;
    private String videoURL;

    private String responseUserID;
    private FirebaseAuth firebaseAuth;
    private NavigationView navigationView;

    private NetworkReceiver mReceiver; //ネットワークの状態監視
    private AlertDialog alertNetworkDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // ここで1秒間スリープし、スプラッシュを表示させたままにする。ちゃんとNoActionbarなThemeにしないと落ちる。
        try {
            Thread.sleep(1000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        setTheme(R.style.AppTheme_NoActionBar);
        setContentView(R.layout.send_video_activity);
    }

    @Override
    public void onStart() {
        super.onStart();
        firebaseAuth = FirebaseAuth.getInstance();
        FirebaseUser currentUser = firebaseAuth.getCurrentUser();
        if (checkLogin(currentUser)) {
            myUserID = currentUser.getUid();
            myUserName = currentUser.getDisplayName();

            //TODO ↓自分のID名以外の全てのトピックを削除したい
            FirebaseMessaging.getInstance().subscribeToTopic(myUserID);//自分のID名でFirebaseMessagingのトピック起動
            setUI(currentUser);
        } else {
            Intent intent = new Intent(this, LoginActivity.class);
            startActivity(intent);//startActivityForResult(intent);
        }
    }

    @Override
    public void onRestart() {
        super.onRestart();
    }

    @Override
    public void onResume() {
        super.onResume();
        // Registers BroadcastReceiver to track network connection changes.
        IntentFilter filter = new IntentFilter(ConnectivityManager.CONNECTIVITY_ACTION);
        mReceiver = new NetworkReceiver(this);
        registerReceiver(mReceiver, filter);
    }

    @Override
    public void onPause() {
        super.onPause();
        if(mReceiver != null) {
            unregisterReceiver(mReceiver);
        }
    }

    @Override
    public void changedToWifi() {
        if(alertNetworkDialog != null){
            alertNetworkDialog.dismiss();
            alertNetworkDialog = null;
        }
    }

    @Override
    public void changedToMobile() {
        if(alertNetworkDialog != null){
            alertNetworkDialog.dismiss();
            alertNetworkDialog = null;
        }
    }

    @Override
    public void changedToOffline() {
        alertNetworkDialog = new AlertDialog.Builder(this)
                .setIcon(R.drawable.ic_signal_cellular_off_black_24dp)//.setIcon(R.drawable.ic_signal_wifi_off_black_24dp)
                .setTitle("OFLINE")
                .setMessage("ネットワークに接続してください")
                .show();
        alertNetworkDialog.setCanceledOnTouchOutside(false);
    }

    public void setUI(FirebaseUser currentUser){
        responseUserID = null;
        videoURL = null;

        // 設定ファイルを開きます。
        SharedPreferences sharedPref = getSharedPreferences(PREF_FILE_NAME, Context.MODE_PRIVATE);

        //TODO VideoオブジェクトとUserオブジェクトでセット
        //動画アイテムのセット
        String videoID = sharedPref.getString("setVideoIDSendMovieActivity", "noSetVideoStatus");
        Log.d("setVideoID",videoID);
        if(videoID.equals("noSetVideoStatus")) {
            videoURL = null;
            final HashMap resultMap = new HashMap();
            resultMap.put("VideoName", "動画を選択してください");
            resultMap.put("PlayTimeMilliSecond", "");
            resultMap.put("VideoByte", "");
            resultMap.put("ThumbnailURL", "Default");
            resultMap.put("VideoURL", "Default");
            setVideoButton(resultMap);
        }else{
            List<String> getFieldList = Arrays.asList("ThumbnailURL", "PlayTimeMilliSecond", "VideoName", "VideoURL", "VideoByte");
            fetchValueFirestore("videos", videoID, getFieldList);
        }

        //フレンドアイテムのセット
        String friendID = sharedPref.getString("setFriendIDSendVideoActivity", "noSetFriendStatus");
        Log.d("setFriendID",friendID);
        if(friendID.equals("noSetFriendStatus")) {
            responseUserID = null;
            final HashMap resultMap = new HashMap();
            resultMap.put("UserName", "友達を選択してください");
            resultMap.put("EmailAdress", "");
            resultMap.put("ThumbnailURL", "Default");
            setFriendButton(resultMap, responseUserID);
        }else{
            List<String> getFieldList = Arrays.asList("ThumbnailURL", "EmailAdress", "UserName");
            fetchValueFirestore("friends", friendID, getFieldList);
        }

        //NavigationDrawerのセット
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        DrawerLayout drawer = findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.addDrawerListener(toggle);
        toggle.syncState();
        navigationView = findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);

        Uri myThumbnailURL = currentUser.getPhotoUrl();
        ImageView myThumbnailImage = navigationView.getHeaderView(0).findViewById(R.id.myThumbnailImage);
        RequestOptions options = new RequestOptions()
                .error(R.drawable.samplevideothumbnail)//エラー時に読み込む画像のIDやURL
                .placeholder(R.drawable.samplevideothumbnail)//ロード開始時に読み込むIDやURL
                .circleCrop()
                .override(200,200);
        Glide.with(this).load(myThumbnailURL)
                .apply(options)
                .listener(createLoggerListener("video_thumbnail"))
                .into(myThumbnailImage);

        TextView myUserNameTxt = navigationView.getHeaderView(0).findViewById(R.id.myNameTxt);
        myUserNameTxt.setText(myUserName);
        TextView myUserEmailAddressTxt = navigationView.getHeaderView(0).findViewById(R.id.myEmailAddressTxt);
        myUserEmailAddressTxt.setText(currentUser.getEmail());
    }


    public void fetchValueFirestore(final String collectionPath, final String ID, final List<String> getFieldList) {
        final HashMap resultMap = new HashMap();
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        DocumentReference docRef = db.collection("users").document(myUserID).collection(collectionPath).document(ID);
        docRef.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                if (task.isSuccessful()) {
                    DocumentSnapshot document = task.getResult();
                    if (document != null) {
                        Log.d("document.getId:",document.getId());
                        Log.d("document.toString():",document.toString());
                        for (String getKey : getFieldList){
                            if(getKey.equals("PlayTimeMilliSecond")||getKey.equals("VideoByte")){//PlayTimeMilliSecondとVideoByteはlong型だから
                                resultMap.put(getKey,document.get(getKey));
                            }else{
                                resultMap.put(getKey,document.getString(getKey));
                            }
                        }
                        if(collectionPath.equals("videos")){
                            setVideoButton(resultMap);
                        }else if(collectionPath.equals("friends")){
                            setFriendButton(resultMap, ID);
                        }
                    } else {
                        Log.d("LOGGER", "No such document");
                    }
                } else {
                    Log.d("LOGGER", "get failed with ", task.getException());
                }
            }
        });
    }

    public void setVideoButton(HashMap videoMap){
        Log.d("HashMap videoMap:",videoMap.toString());
        try {
            ImageView sendVideoThumbnail = findViewById(R.id.sendVideoThumbnail);
            if (videoMap.get("ThumbnailURL").toString().equals("Default")){
                sendVideoThumbnail.setImageResource(R.drawable.samplevideothumbnail);
            }else{
                RequestOptions options = new RequestOptions()
                        .error(R.drawable.samplevideothumbnail)//エラー時に読み込む画像のIDやURL
                        .placeholder(R.drawable.samplevideothumbnail)//ロード開始時に読み込むIDやURL
                        .override(300, 300);
                Glide.with(this).load(videoMap.get("ThumbnailURL").toString())
                        .apply(options)
                        .listener(createLoggerListener("video_thumbnail"))
                        .into(sendVideoThumbnail);
            }
            //動画テキストのセット
            TextView videoNameTxt = findViewById(R.id.sendVideoNameTxt);
            videoNameTxt.setText(videoMap.get("VideoName").toString());
            TextView videoPlayTimeTxt = findViewById(R.id.sendVideoPlayTimeTxt);
            TextView videoByteTxt = findViewById(R.id.sendVideoByteTxt);

            if(videoMap.get("PlayTimeMilliSecond").getClass().toString().equals("class java.lang.String")){
                String playTime = videoMap.get("PlayTimeMilliSecond").toString();
                videoPlayTimeTxt.setText(playTime);
            } else {
                String playTime = milliSecond.toTimeColonFormat(Integer.parseInt(videoMap.get("PlayTimeMilliSecond").toString()));
                videoPlayTimeTxt.setText(playTime);
            }

            if(videoMap.get("VideoByte").getClass().toString().equals("class java.lang.String")){
                String videoMegaByte = videoMap.get("VideoByte").toString();
                videoByteTxt.setText(videoMegaByte);
            }else{
                String videoMegaByte = fileByte.toStringMegaByte(Integer.parseInt(videoMap.get("VideoByte").toString()));
                videoByteTxt.setText(videoMegaByte);
            }
            //videoURLのセット
            videoURL = videoMap.get("VideoURL").toString();
        } catch (NullPointerException e){
            Log.d("setVideoButton","VideoItem削除済み",e);
        }
    }

    public void setFriendButton(HashMap friendMap, String friendID){
        Log.d("HashMap friendMap:",friendMap.toString());
        try {
            ImageView sendFriendThumbnail = findViewById(R.id.sendFriendThumbnail);
            if (friendMap.get("ThumbnailURL").toString().equals("Default")){
                sendFriendThumbnail.setImageResource(R.drawable.sampleusericon);
            }else{
                RequestOptions options = new RequestOptions()
                        .error(R.drawable.sampleusericon)//エラー時に読み込む画像のIDやURL
                        .placeholder(R.drawable.sampleusericon)//ロード開始時に読み込むIDやURL
                        .override(300, 300);
                Glide.with(this).load(friendMap.get("ThumbnailURL").toString())
                        .apply(options)
                        .listener(createLoggerListener("video_thumbnail"))
                        .into(sendFriendThumbnail);
            }
            //動画テキストのセット
            TextView friendNameTxt = findViewById(R.id.sendFriendNameTxt);
            friendNameTxt.setText(friendMap.get("UserName").toString());
            TextView friendEmailAdressTxt = findViewById(R.id.sendFriendEmailAdressTxt);
            friendEmailAdressTxt.setText(friendMap.get("EmailAdress").toString());
            //responseUserIDのセット
            responseUserID = friendID;
        } catch (NullPointerException e){
            Log.d("setFriendButton","FriendItem削除済み",e);
        }

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

    public boolean checkLogin(FirebaseUser currentUser){
        if (currentUser == null) {
            return false;
        } else {
            return true;
        }
    }


    public void onClickSetMovieButton(View view) {
        Intent intent = new Intent(this, SetVideoActivity.class);
        //startActivityForResult(intent);
        intent.putExtra("myUserID",myUserID);
        startActivity(intent);
    }


    public void onClickSetUserButton(View view) {
        Intent intent = new Intent(this, SetFriendActivity.class);
        intent.putExtra("myUserID",myUserID);
        startActivity(intent);
    }

    public void onClickSendVideoButton(View view) {
        if(responseUserID!=null&&videoURL!=null) {
            if (!isClickEvent()) return;
            Map<String, Object> request = new HashMap<>();
            request.put("RequestUserName", myUserName);
            request.put("RequestUserID", myUserID);
            request.put("ResponseUserID", responseUserID);
            request.put("VideoURL", videoURL);

            //Firebaseの通信準備
            FirebaseFirestore db = FirebaseFirestore.getInstance();
            db.collection("requests").add(request).addOnSuccessListener(new OnSuccessListener<DocumentReference>() {
                @Override
                public void onSuccess(DocumentReference documentReference) {
                    sendVideoRequest(documentReference.getId());
                }
            }).addOnFailureListener(new OnFailureListener() {
                @Override
                public void onFailure(@NonNull Exception e) {
                    Log.w(TAG, "動画の送信に失敗：Error adding document", e);
                }
            });
        }
        else{
            Context context = getApplicationContext();
            Toast.makeText(context , "動画と友達の両方を選択してください", Toast.LENGTH_LONG).show();
        }
    }


    public void sendVideoRequest(final String requestID){
        final String baseUrl = "https://fcm.googleapis.com/fcm/send";
        final HashMap pushStringData = new HashMap() {
            {
                put("id", "1");
                put("label", "Dataテストタイトル");
                put("text", requestID);
            }
        };
        final HashMap data = new HashMap() {
            {
                put("to", "/topics/"+responseUserID);
                put("priority", "high");
                put("data", pushStringData);
            }
        };
        final HashMap mapData = new HashMap(){
            {
                put("baseUrl", baseUrl);
                put("data", data);
            }
        };
        HttpRequestTask httpTask = new HttpRequestTask(getString(R.string.FireStoreAPIkey));
        httpTask.execute(mapData);
        Context context = getApplicationContext();
        Toast.makeText(context , "送信しました", Toast.LENGTH_LONG).show();
    }
    /**
     * クリックイベントが実行可能か判断する。
     * @return クリックイベントの実行可否 (true:可, false:否)
     */
    public boolean isClickEvent() {
        // 現在時間を取得する
        long time = System.currentTimeMillis();
        // クリック連打制御時間(ミリ秒)
        long CLICK_DELAY = 2000;
        // 前回のクリックイベント実行時間
        long mOldClickTime = 0;
        // 一定時間経過していなければクリックイベント実行不可
        if (time - mOldClickTime < CLICK_DELAY) {
            Context context = getApplicationContext();
            Toast.makeText(context , "連続では送信できません", Toast.LENGTH_LONG).show();
            return false;
        }
        // 一定時間経過したらクリックイベント実行可能
        mOldClickTime = time;
        return true;
    }

    public void signOut() {
        firebaseAuth.signOut();
        LoginManager.getInstance().logOut();
        //TODO 他のアカウントでLoginしたときID情報が残っているとやべえし、時間が経ってログアウトして別垢でログインしなおしたらバグる
        SharedPreferences sharedPref = getSharedPreferences(PREF_FILE_NAME, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor =  sharedPref.edit();
        editor.clear().commit();

        FirebaseMessaging.getInstance().unsubscribeFromTopic(myUserID);

        Intent intent = new Intent(this, LoginActivity.class);
        startActivity(intent);
    }

    @Override
    public void onBackPressed() {
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }
    }

    @SuppressWarnings("StatementWithEmptyBody")
    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        // Handle navigation view item clicks here.
        int id = item.getItemId();

        if (id == R.id.nav_camera) {

        }else if (id == R.id.logout) {
            signOut();
        }

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }
}

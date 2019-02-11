package com.example.kobayashi_satoru.miroyo;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.NavigationView;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
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

public class SendVideoActivity extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener{

    private final String PREF_FILE_NAME = "com.example.kobayashi_satoru.miroyo.SendMovieActivity";
    private String TAG = "SendMovieActivity";
    private String myUserID;
    private String myUserName;

    private String videoURL;

    private String responseUserID;
    private FirebaseAuth firebaseAuth;
    private NavigationView navigationView;

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
        setContentView(R.layout.send_movie_activity);
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
            //自分のID名でFirebaseMessagingのトピック起動
            FirebaseMessaging.getInstance().subscribeToTopic(myUserID);
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

    public void setUI(FirebaseUser currentUser){
        responseUserID=null;
        videoURL=null;

        // 設定ファイルを開きます。
        SharedPreferences sharedPref = getSharedPreferences(PREF_FILE_NAME, Context.MODE_PRIVATE);

        //TODO VideoオブジェクトとUserオブジェクトでセット
        //動画アイテムのセット
        String videoID = sharedPref.getString("setVideoIDSendMovieActivity", "noSetVideoStatus");
        Log.d("setVideoID",videoID);
        if(videoID.equals("noSetVideoStatus")) {
            videoURL = null;
            final HashMap resultMap = new HashMap();
            resultMap.put("VideoName", "動画ファイルを選択してください");
            resultMap.put("PlayTimeMilliSecond", "");
            resultMap.put("ThumbnailURL", "Default");
            resultMap.put("VideoURL", "Default");
            setVideoButton(resultMap);
        }else{
            List<String> getFieldList = Arrays.asList("ThumbnailURL", "PlayTimeMilliSecond", "VideoName", "VideoURL");
            fetchValueFirestore("videos", videoID, getFieldList);
        }

        //フレンドアイテムのセット
        String userID = sharedPref.getString("setUserIDSendMovieActivity", "noSetUserStatus");
        String userImageConfig = sharedPref.getString("setUserIconSendMovieActivity", "noSetUserStatus");
        if(userID != "noSetUserStatus") {
            responseUserID = userID;
            //List<String> getFieldList = Arrays.asList("ThumbnailURL", "UserID", "UserName");
            //fireBaseRead("users", userID, getFieldList);
        }
        TextView userName = findViewById(R.id.setUserName);
        TextView emailAddress = findViewById(R.id.setEmailAddress);
        ImageView imageView = findViewById(R.id.userThumbnail);
        if(userImageConfig.equals("satoruicon")){
            imageView.setImageResource(R.drawable.satoruicon);
            userName.setText("小林 慧");
            emailAddress.setText("satorino0821@gmail.com");
        }else if(userImageConfig.equals("sampleusericon")){
            imageView.setImageResource(R.drawable.sampleusericon);
            userName.setText("小林 さとり");
            emailAddress.setText("satorino0821@yahoo.co.jp");
        }else{
            imageView.setImageResource(R.drawable.sampleusericon);
            userName.setText("送信する友達を選択してください");
            emailAddress.setText("");
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
                .error(R.drawable.samplemoviethumbnail)//エラー時に読み込む画像のIDやURL
                .placeholder(R.drawable.samplemoviethumbnail)//ロード開始時に読み込むIDやURL
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


    public void fetchValueFirestore(final String collectionPath, String ID, final List<String> getFieldList) {
        final HashMap resultMap = new HashMap();
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        DocumentReference docRef = db.collection(collectionPath).document(ID);
        docRef.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                if (task.isSuccessful()) {
                    DocumentSnapshot document = task.getResult();
                    if (document != null) {
                        for (String getKey : getFieldList){
                            resultMap.put(getKey,document.getString(getKey));
                            Log.d(TAG,"AAAAAAAAA"+getKey+":"+document.getString(getKey));
                        }
                        if(collectionPath.equals("videos")){
                            setVideoButton(resultMap);
                        }else if(collectionPath.equals("users")){
                            setUserButton(resultMap);
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
                sendVideoThumbnail.setImageResource(R.drawable.samplemoviethumbnail);
            }else{
                RequestOptions options = new RequestOptions()
                        .error(R.drawable.samplemoviethumbnail)//エラー時に読み込む画像のIDやURL
                        .placeholder(R.drawable.samplemoviethumbnail)//ロード開始時に読み込むIDやURL
                        .override(300, 300);
                Glide.with(this).load(videoMap.get("ThumbnailURL").toString())
                        .apply(options)
                        .listener(createLoggerListener("video_thumbnail"))
                        .into(sendVideoThumbnail);
            }
            //動画テキストのセット
            TextView videoNameTxt = findViewById(R.id.sendVideoName);
            videoNameTxt.setText(videoMap.get("VideoName").toString());
            TextView videoPlayTimeTxt = findViewById(R.id.sendVideoPlayTime);
            if(videoMap.get("PlayTimeMilliSecond").getClass().toString().equals("class java.lang.String")){
                String PlayTime = videoMap.get("PlayTimeMilliSecond").toString();
                videoPlayTimeTxt.setText(PlayTime);
            } else {
                String PlayTime = milliSecond.toTimeColonFormat((int)videoMap.get("PlayTimeMilliSecond"));
                videoPlayTimeTxt.setText(PlayTime);
            }

            //videoURLのセット
            videoURL = videoMap.get("VideoURL").toString();
        } catch (NullPointerException e){
            Log.d("setVideoButton","VideoItem削除済み",e);
        }
    }

    public void setUserButton(HashMap userMap){

        //responseUserIDのセット
        //アイコン画像のセットを書く
//            task = new DownloadTask();
//            task.setListener(createListener());
//            task.execute("https://storage.googleapis.com/miroyo.appspot.com/01.png");

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
        Intent intent = new Intent(this, SetUserActivity.class);
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
        Intent intent = new Intent(this, LoginActivity.class);
        //startActivityForResult(intent);
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

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
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.NavigationView;
import android.support.design.widget.Snackbar;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
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
import com.example.kobayashi_satoru.miroyo.ui.sendmovie.SendMovieViewModel;
import com.facebook.login.LoginManager;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.UserProfileChangeRequest;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.messaging.FirebaseMessaging;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class SendMovieActivity extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener{

    private String TAG = "SendMovieActivity";
    private String myUserID = "UnknownID";
    private String myUserName = "UnknownName";
    private String responseUserID;
    private String videoURL;
    private SendMovieViewModel sendMovieViewModel;
    private FirebaseAuth firebaseAuth;
    private NavigationView navigationView;

    private static final String PREF_FILE_NAME = "com.example.kobayashi_satoru.miroyo.SendMovieActivity";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //FirebaseAuthの初期設定
        firebaseAuth = FirebaseAuth.getInstance();
        //signOut();
        // ここで1秒間スリープし、スプラッシュを表示させたままにする。
//        setTheme(R.style.SplashTheme);
//        try {
//            Thread.sleep(1000);
//        } catch (InterruptedException e) {
//            e.printStackTrace();
//        }
//        setTheme(R.style.AppTheme);
        setContentView(R.layout.send_movie_activity);

        //NavigationDrawer

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG).setAction("Action", null).show();
            }
        });

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.addDrawerListener(toggle);
        toggle.syncState();

        navigationView = (NavigationView) findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);
    }


    @Override
    public void onStart() {
        super.onStart();
        setUI();
    }


    @Override
    public void onRestart() {
        super.onRestart();
        setUI();
    }

    public void setUI(){
        responseUserID=null;
        videoURL=null;
        // Check if user is signed in (non-null) and update UI accordingly.
        FirebaseUser currentUser = firebaseAuth.getCurrentUser();
        checkLogin(currentUser);

        // 設定ファイルを開きます。
        SharedPreferences sharedPref = getSharedPreferences(PREF_FILE_NAME, Context.MODE_PRIVATE);

        //アイコンや文字列のセット
        String videoID = sharedPref.getString("setVideoIDSendMovieActivity", "noSetVideoStatus");
        //videoID = "3C37gFJYWl3EsCkUw8fV";
        if(videoID != "noSetVideoStatus") {
            List<String> getFieldList = Arrays.asList("ThumbnailURL", "PlayTime", "VideoName", "VideoURL");
            fireBaseRead("videos", videoID, getFieldList);
        }

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
    }


    public void fireBaseRead(final String collectionPath, String ID, final List<String> getFieldList) {
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
        //アイコン画像のセット
        ImageView sendVideoThumbnail = findViewById(R.id.sendVideoThumbnail);
        RequestOptions options = new RequestOptions()
                .error(R.drawable.samplemoviethumbnail)//エラー時に読み込む画像のIDやURL
                .placeholder(R.drawable.samplemoviethumbnail)//ロード開始時に読み込むIDやURL
                .override(300,300);
        Glide.with(this).load(videoMap.get("ThumbnailURL").toString())
                .apply(options)
                .listener(createLoggerListener("video_thumbnail"))
                .into(sendVideoThumbnail);
        //動画テキストのセット
        TextView videoNameTxt = findViewById(R.id.sendVideoName);
        videoNameTxt.setText(videoMap.get("VideoName").toString());
        TextView videoPlayTimeTxt = findViewById(R.id.sendVideoPlayTime);
        videoPlayTimeTxt.setText(videoMap.get("PlayTime").toString());
        //videoURLのセット
        videoURL = videoMap.get("VideoURL").toString();

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


    public void setUserButton(HashMap userMap){

        //responseUserIDのセット
        //アイコン画像のセットを書く
//            task = new DownloadTask();
//            task.setListener(createListener());
//            task.execute("https://storage.googleapis.com/miroyo.appspot.com/01.png");

    }


    public void checkLogin(FirebaseUser currentUser){
        if (currentUser == null) {
            Intent intent = new Intent(this, LoginActivity.class);
            //startActivityForResult(intent);
            startActivity(intent);
        } else {
            myUserID = currentUser.getUid();
            myUserName = currentUser.getDisplayName();
            Uri myThumbnailURL = currentUser.getPhotoUrl();

//            UserProfileChangeRequest userProfileChangeRequest = new UserProfileChangeRequest.Builder().setPhotoUri(Uri.parse("https://scontent-nrt1-1.xx.fbcdn.net/v/t1.0-1/c0.0.319.319a/49899532_107467780342810_9021168769015218176_n.jpg?_nc_cat=108&_nc_ht=scontent-nrt1-1.xx&oh=2085d9420100b9d174c43b90f76a3c0e&oe=5CB786E2")).build();
//            currentUser.updateProfile(userProfileChangeRequest);

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

            //TODO ↓その他全てのトピックを削除したい
            //自分のID名でFirebaseMessagingのトピック起動
            FirebaseMessaging.getInstance().subscribeToTopic(myUserID);
        }
    }


    public void onClickSetMovieButton(View view) {
        Intent intent = new Intent(this, SetMovieActivity.class);
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
        HttpRequestTask httpTask =new HttpRequestTask();
        httpTask.execute(mapData);
        Context context = getApplicationContext();
        Toast.makeText(context , "送信しました", Toast.LENGTH_LONG).show();
    }

    /** クリック連打制御時間(ミリ秒) */
    private static final long CLICK_DELAY = 2000;
    /** 前回のクリックイベント実行時間 */
    private static long mOldClickTime;

    /**
     * クリックイベントが実行可能か判断する。
     * @return クリックイベントの実行可否 (true:可, false:否)
     */
    public boolean isClickEvent() {
        // 現在時間を取得する
        long time = System.currentTimeMillis();

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

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.navigation_drawer, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @SuppressWarnings("StatementWithEmptyBody")
    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        // Handle navigation view item clicks here.
        int id = item.getItemId();

        if (id == R.id.nav_camera) {
            // Handle the camera action
        } else if (id == R.id.nav_gallery) {

        } else if (id == R.id.nav_slideshow) {
            signOut();
        } else if (id == R.id.nav_manage) {

        } else if (id == R.id.nav_share) {

        } else if (id == R.id.nav_send) {

        }

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }
}

package com.example.kobayashi_satoru.miroyo;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.net.Uri;
import android.provider.DocumentsContract;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.example.kobayashi_satoru.miroyo.ui.sendmovie.SendMovieViewModel;
import com.facebook.login.Login;
import com.facebook.login.LoginManager;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.messaging.FirebaseMessaging;

import java.net.URL;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class SendMovieActivity extends AppCompatActivity {

    private String TAG = "SendMovieActivity";
    private String myUserID = "UnknownID";
    private String myUserName = "UnknownName";
    private String responseUserID = "wvJVcJftx3QMwdnF4Uzf4kTlNKd2";
    private String videoURL;
    private SendMovieViewModel sendMovieViewModel;
    private FirebaseAuth firebaseAuth;
    private DownloadTask downloadTask;

    private static final String PREF_FILE_NAME = "com.example.kobayashi_satoru.miroyo.SendMovieActivity";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //FirebaseAuthの初期設定
        firebaseAuth = FirebaseAuth.getInstance();
        //signOut();
        // ここで1秒間スリープし、スプラッシュを表示させたままにする。
        setTheme(R.style.SplashTheme);
        try {
            Thread.sleep(1000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        setTheme(R.style.AppTheme);
        setContentView(R.layout.send_movie_activity);
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
            fireBaseRead("video", videoID, getFieldList);
        }

        String userID = sharedPref.getString("setUserIDSendMovieActivity", "noSetUserStatus");
        if(userID != "noSetUserStatus") {
            List<String> getFieldList = Arrays.asList("ThumbnailURL", "UserID", "UserName");
            fireBaseRead("users", userID, getFieldList);
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
                        if(collectionPath.equals("video")){
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
        downloadTask = new DownloadTask();
        downloadTask.setListener(createListener());
        downloadTask.execute(videoMap.get("ThumbnailURL").toString());
        //動画テキストのセット
        TextView videoName = findViewById(R.id.videoName);
        videoName.setText(videoMap.get("VideoName").toString());
        TextView videoPlayTime = findViewById(R.id.videoPlayTime);
        videoPlayTime.setText(videoMap.get("PlayTime").toString());
        //videoURLのセット
        videoURL = videoMap.get("VideoURL").toString();

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
            TextView textID=findViewById(R.id.text_id);
            textID.setText("MyID:"+currentUser.getUid());
            TextView textEmailAdressView=findViewById(R.id.text_emailadress);
            textEmailAdressView.setText("MyEmail:"+currentUser.getEmail());
            TextView textUserNameView=findViewById(R.id.text_user_name);
            textUserNameView.setText("MyUserName:"+myUserName);
            //↓その他全てのトピックを削除したい
            FirebaseMessaging.getInstance().unsubscribeFromTopic(responseUserID);
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
        if (!isClickEvent()) return;
        Map<String, Object> request = new HashMap<>();
        request.put("RequestUserName", myUserName);
        request.put("RequestUserID", myUserID);
        request.put("ResponseUserID", responseUserID);
        request.put("VideoURL", videoURL);

        //Firebaseの通信準備
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        db.collection("request")
        .add(request)
        .addOnSuccessListener(new OnSuccessListener<DocumentReference>() {
            @Override
            public void onSuccess(DocumentReference documentReference) {
                sendVideoRequest(documentReference.getId());
            }
        })
        .addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Log.w(TAG, "動画の送信に失敗：Error adding document", e);
            }
        });
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
    }

    /** クリック連打制御時間(ミリ秒) */
    private static final long CLICK_DELAY = 2000;
    /** 前回のクリックイベント実行時間 */
    private static long mOldClickTime;

    /**
     * クリックイベントが実行可能か判断する。
     * @return クリックイベントの実行可否 (true:可, false:否)
     */
    public static boolean isClickEvent() {
        // 現在時間を取得する
        long time = System.currentTimeMillis();

        // 一定時間経過していなければクリックイベント実行不可
        if (time - mOldClickTime < CLICK_DELAY) {
            return false;
        }
        // 一定時間経過したらクリックイベント実行可能
        mOldClickTime = time;
        return true;
    }

    private DownloadTask.Listener createListener() {
        return new DownloadTask.Listener() {
            @Override
            public void onSuccess(Bitmap bmp) {
                SquareImageView videoThumbnailView = findViewById(R.id.videoThumbnail);
                videoThumbnailView.setImageBitmap(bmp);
            }
        };
    }


    public void signOut() {
        firebaseAuth.signOut();
        LoginManager.getInstance().logOut();
    }
}

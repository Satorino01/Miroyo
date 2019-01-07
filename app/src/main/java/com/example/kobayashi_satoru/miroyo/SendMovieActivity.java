package com.example.kobayashi_satoru.miroyo;

import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;

import com.example.kobayashi_satoru.miroyo.ui.sendmovie.SendMovieViewModel;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.messaging.FirebaseMessaging;
import java.util.HashMap;
import java.util.Map;

public class SendMovieActivity extends AppCompatActivity {

    private String TAG = "satoru";
    private String myUserID;
    private String myUserName;
    private String responseUserID;
    private SendMovieViewModel sendMovieViewModel;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //myUserIDとmyUserNameの取得
        myUserID = "pIABvBhFpcWZsck0Z2q4";
        myUserName = "小林 慧";
        responseUserID = "4iyGUtycDoaJkghp3KeX";
        //↓その他全てのトピックを削除したい
        FirebaseMessaging.getInstance().unsubscribeFromTopic(responseUserID);
        //自分のID名でFirebaseMessagingのトピック起動
        FirebaseMessaging.getInstance().subscribeToTopic(myUserID);
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


    public void onClickSetMovieButton(View view) {
        Intent intent = new Intent(this, SetMovieActivity.class);
        //startActivityForResult(intent);
        startActivity(intent);
    }


    public void onClickSetUserButton(View view) {
        Intent intent = new Intent(this, SetUserActivity.class);
        startActivity(intent);
    }


    public void onClickSendVideoButton(View view) {
        Map<String, Object> request = new HashMap<>();
        request.put("RequestUserName", myUserName);
        request.put("RequestUserID", myUserID);
        request.put("ResponseUserID", responseUserID);
        request.put("VideoURL", "https://storage.googleapis.com/miroyo.appspot.com/VID_20190107_072029.mp4");

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
}

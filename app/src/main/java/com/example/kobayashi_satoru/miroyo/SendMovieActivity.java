package com.example.kobayashi_satoru.miroyo;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;

import com.example.kobayashi_satoru.miroyo.ui.sendmovie.SendMovieViewModel;
import com.google.firebase.messaging.FirebaseMessaging;

import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.PrintStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.ProtocolException;
import java.net.URL;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

public class SendMovieActivity extends AppCompatActivity {

    private String TAG = "satoru";
    private SendMovieViewModel sendMovieViewModel;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        Log.d(TAG, "SendMovieActivity起動！！");

        //FirebaseMessagingのトピック起動
        FirebaseMessaging.getInstance().subscribeToTopic("test");
        // ここで1秒間スリープし、スプラッシュを表示させたままにする。
        setTheme(R.style.SplashTheme);
        try {
            Thread.sleep(1000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        setTheme(R.style.AppTheme);
        setContentView(R.layout.send_movie_activity);
//
//        FirebaseFirestore db = FirebaseFirestore.getInstance();
//
//        // Create a new user with a first, middle, and last name
//        Map<String, Object> user = new HashMap<>();
//        user.put("EmailAddress", "asasa@mail");
//        user.put("UserName", "Mathison");
//        user.put("OAuthTokenFacebook", "rqefd3");
//        user.put("OAuthTokenGoogle", "saa323");
//
//        // Add a new document with a generated ID
//        db.collection("users")
//                .add(user)
//                .addOnSuccessListener(new OnSuccessListener<DocumentReference>() {
//                    @Override
//                    public void onSuccess(DocumentReference documentReference) {
//                        Log.d(TAG, "成功しましたあああDocumentSnapshot added with ID: " + documentReference.getId());
//                    }
//                })
//                .addOnFailureListener(new OnFailureListener() {
//                    @Override
//                    public void onFailure(@NonNull Exception e) {
//                        Log.w(TAG, "Error adding document", e);
//                    }
//                });
//
//        // Create a new user with a first and last name
//        db.collection("users")
//                .get()
//                .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
//                    @Override
//                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
//                        if (task.isSuccessful()) {
//                            for (QueryDocumentSnapshot document : task.getResult()) {
//                                Log.d(TAG, document.getId() + " 成功 => " + document.getData());
//                            }
//                        } else {
//                            Log.w(TAG, "AAAAAAAAAAAAAError getting documents.", task.getException());
//                        }
//                    }
//                });


        if (savedInstanceState == null) {
            //getSupportFragmentManager().beginTransaction().replace(R.id.container, SendMovieFragment.newInstance()).commitNow();
        }
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

        final String apiKey = "AAAAbHcEneY:APA91bEGxZ7oiHiF4KFeD9LgFkL_MHPoipGcdylbx7Rz6vF8PXpEQGnpkrJlhiyApF-2CWei1-QvtTk5ahquTlxH-tBMB5n9p_NYCj5i2l8AIZ6SQAWmGAjUY05rDeGxxgn5Osc3hDKv";
        final String baseUrl = "https://fcm.googleapis.com/fcm/send";
        final HashMap pushStringData = new HashMap() {
            {
                put("id", "1");
                put("label", "Dataテストタイトル");
                put("text", "Dataテスト本文");
            }
        };
        final HashMap data = new HashMap() {
            {
                put("to", "/topics/test");
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
//        final String header= "Content-Type:application/json"+"\r\n"+"Authorization:key=" + apiKey;
//        final HashMap httpData = new HashMap() {
//            {
//                put("method","POST");
//                put("header",header);
//                put("content",new JSONObject(data));
//            }
//        };
//        HashMap context = new HashMap() {
//            {
//                put("http",httpData);
//            }
//        };
        HttpRequestTask httpTask =new HttpRequestTask();
        httpTask.execute(mapData);
    }
}

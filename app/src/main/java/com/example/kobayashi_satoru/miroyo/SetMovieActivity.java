package com.example.kobayashi_satoru.miroyo;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.example.kobayashi_satoru.miroyo.adapter.videoAdapter;
import com.example.kobayashi_satoru.miroyo.listener.OnRecyclerListener;
import com.example.kobayashi_satoru.miroyo.ui.setmovie.SetMovieFragment;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

public class SetMovieActivity extends AppCompatActivity implements OnRecyclerListener {

    private static final String PREF_FILE_NAME = "com.example.kobayashi_satoru.miroyo.SendMovieActivity";

    private int buttonFormCounter = 0;
    private List videoIDs;
    private HashMap videoMap;
    private ArrayList videoMapArray = new ArrayList<HashMap>();

    private videoAdapter videoAdapter;
    private RecyclerView videoRecyclerView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.set_movie_activity);

        videoRecyclerView = findViewById(R.id.videoRecyclerView);// RecyclerViewの参照を取得
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(this);// レイアウトマネージャを設定(ここで縦方向の標準リストであることを指定)
        videoRecyclerView.setLayoutManager(linearLayoutManager);

        Intent intent = getIntent();
        String myUserID = intent.getStringExtra("myUserID");
        String[] getFieldArray = {"UserName", "EmailAdress" , "FriendIDs" , "VideoIDs" , "videos"};
        final List<String> getFieldList = Arrays.asList(getFieldArray);

        FirebaseFirestore db = FirebaseFirestore.getInstance();
        DocumentReference docRef = db.collection("users").document(myUserID);
        final Context context = this;
        docRef.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                if (task.isSuccessful()) {
                    DocumentSnapshot document = task.getResult();
                    if (document != null) {
                        List<Object> VideoIDs = new ArrayList<>();
                        HashMap VideoMaps = new HashMap<>();
                        for (String getKey : getFieldList) {
                            if(getKey=="VideoIDs" || getKey=="FriendIDs"){
                                VideoIDs = (List) document.get(getKey);
                            } else if(getKey=="videos"){
                                VideoMaps = (HashMap) document.get(getKey);
                            } else {
                                //document.getString(getKey);
                            }
                        }
                        videoAdapter = new videoAdapter(context , VideoIDs, VideoMaps, (OnRecyclerListener) context);
                        videoRecyclerView.setAdapter(videoAdapter);
                        Log.d("LOGGER", "No such document");
                    }
                } else {
                    Log.d("LOGGER", "get failed with ", task.getException());
                }
            }
        });

    }
    @Override
    public void onStart() {
        super.onStart();
        //setUI();
    }

    public void setUI(){

        Intent intent = getIntent();
        String myUserID = intent.getStringExtra("myUserID");
        String[] getFieldArray = {"UserName", "EmailAdress" , "FriendIDs" , "VideoIDs" , "videos"};
        List<String> getFieldList = Arrays.asList(getFieldArray);
        HashMap userMap = new HashMap();//TODO mapからUSER型に変換
        try {
            userMap = fetchValueFireStore.fetchMap("users", myUserID ,getFieldList);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        List<Object> VideoIDs = Arrays.asList(userMap.get("VideoIDs"));

    }

    public void onClickReturnButton(View v){
        finish();
    }

    @Override
    public void onRecyclerClicked(View v, int position) {
        finish();
    }

}

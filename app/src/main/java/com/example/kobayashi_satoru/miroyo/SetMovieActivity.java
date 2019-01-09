package com.example.kobayashi_satoru.miroyo;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

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

public class SetMovieActivity extends AppCompatActivity {

    private static final String PREF_FILE_NAME = "com.example.kobayashi_satoru.miroyo.SendMovieActivity";

    private DownloadTask downloadTask;
    private int buttonFormCounter = 0;
    private List videoIDs;
    private HashMap videoMap;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.set_movie_activity);
        if (savedInstanceState == null) {
            //getSupportFragmentManager().beginTransaction().replace(R.id.container, SetMovieFragment.newInstance()).commitNow();
        }
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
        Intent intent = getIntent();
        String myUserID = intent.getStringExtra("myUserID");
        List<String> getFieldList = Arrays.asList("VideoIDs");
        fireBaseRead("users", myUserID ,getFieldList);
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
                        if(collectionPath.equals("users")){
                            videoIDs = (List)document.get(getFieldList.get(0));
                            for(int i = 0 ; i < 2; i++){
                                List<String> getFieldList = Arrays.asList("ThumbnailURL", "PlayTime", "VideoName", "VideoURL");
                                try {
                                    fireBaseRead("video", videoIDs.get(i).toString(), getFieldList);
                                }catch(Exception e){
                                    Log.d("Erroraaaaaaaaaa:",videoIDs.get(i).getClass().toString());
                                }
                            }
                        }else if(collectionPath.equals("video")){
                            for (String getKey : getFieldList) {
                                resultMap.put(getKey, document.getString(getKey));
                            }
                            videoMap = resultMap;
                            setVideoButton();
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

    public void setVideoButton(){

        //アイコン画像のセット
        downloadTask = new DownloadTask();
        downloadTask.setListener(createListener());
        downloadTask.execute(videoMap.get("ThumbnailURL").toString());
    }

    private DownloadTask.Listener createListener() {
        return new DownloadTask.Listener() {
            @Override
            public void onSuccess(Bitmap bmp) {
                switch (buttonFormCounter){
                    case 0:
                        //アイコンのセット
                        SquareImageView videoThumbnailView0 = findViewById(R.id.videoThumbnail0);
                        videoThumbnailView0.setImageBitmap(bmp);
                        //文字列のセット
                        TextView videoName0 = findViewById(R.id.videoName0);
                        videoName0.setText(videoMap.get("VideoName").toString());
                        TextView videoPlayTime0 = findViewById(R.id.videoPlayTime0);
                        videoPlayTime0.setText(videoMap.get("PlayTime").toString());
                        break;
                    case 1:
                        //アイコンのセット
                        SquareImageView videoThumbnailView1 = findViewById(R.id.videoThumbnail1);
                        videoThumbnailView1.setImageBitmap(bmp);
                        //文字列のセット
                        TextView videoName1 = findViewById(R.id.videoName1);
                        videoName1.setText(videoMap.get("VideoName").toString());
                        TextView videoPlayTime1 = findViewById(R.id.videoPlayTime1);
                        videoPlayTime1.setText(videoMap.get("PlayTime").toString());
                        break;
                    case 2:
                        break;
                    case 3:
                        break;
                    case 4:
                        break;
                    case 5:
                        break;
                }
                buttonFormCounter++;
            }
        };
    }
    public void onClickVideo0(View v){
        SharedPreferences sharedPref = getSharedPreferences(PREF_FILE_NAME, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor =  sharedPref.edit();
        //アイコンや文字列のセット
        editor.putString("setVideoIDSendMovieActivity", videoIDs.get(0).toString());
        editor.apply();
        finish();
    }
    public void onClickVideo1(View v){
        SharedPreferences sharedPref = getSharedPreferences(PREF_FILE_NAME, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor =  sharedPref.edit();
        //アイコンや文字列のセット
        editor.putString("setVideoIDSendMovieActivity", videoIDs.get(1).toString());
        editor.apply();
        finish();
    }
    public void onClickVideo2(View v){
        finish();
    }
    public void onClickVideo3(View v){
        finish();
    }
    public void onClickVideo4(View v){
        finish();
    }
    public void onClickVideo5(View v){
        finish();
    }

    public void onClickReturnButton(View v){
        finish();
    }
}

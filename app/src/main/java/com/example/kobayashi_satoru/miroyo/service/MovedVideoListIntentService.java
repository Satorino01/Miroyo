package com.example.kobayashi_satoru.miroyo.service;

import android.app.IntentService;
import android.content.Intent;
import android.util.Log;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.List;
import java.util.concurrent.CountDownLatch;

public class MovedVideoListIntentService extends IntentService {
    // TODO: アクションの名前を変更し、そのタスクを説明するアクション名を選択してください。
    private final String ACTION_MovedVideo = "com.example.kobayashi_satoru.miroyo.action.MovedVideo";
    private List<String> videoIDs;
    CountDownLatch finishCountDownLatch = new CountDownLatch(1);

    public MovedVideoListIntentService() {
        super("MovedVideoListIntentService");
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        Log.d("onHandleIntent","起動おおおおおおおおおおおおおおおおおおおおおおおおおおお");
        if (intent != null) {
            final String action = intent.getAction();
            if (ACTION_MovedVideo.equals(action)) {
                videoIDs = intent.getStringArrayListExtra("videoIDs");
                try {
                    handleActionMovedVideo();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }
    }
    private void handleActionMovedVideo() throws InterruptedException {
        MovedVideoIDsOfUsers();
    }
    public void MovedVideoIDsOfUsers() throws InterruptedException {
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        FirebaseAuth firebaseAuth = FirebaseAuth.getInstance();
        FirebaseUser currentUser = firebaseAuth.getCurrentUser();
        String myUserID = currentUser.getUid();
        CollectionReference myVideosCollectionReference = db.collection("users").document(myUserID).collection("videos");
        myVideosCollectionReference
                .document("VideosData")
                .update("VideoIDs",videoIDs)
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        finishCountDownLatch.countDown();
                        Log.d("VideoIDs","videoIDsの順番変更成功");
                    }
                });
        finishCountDownLatch.await();
        Log.d("awaitFinishCountDown","サービス終了：" + String.valueOf(finishCountDownLatch.getCount()));
        stopSelf();
    }
}

package com.example.kobayashi_satoru.miroyo.service;

import android.app.IntentService;
import android.content.Intent;
import android.content.Context;
import android.graphics.Bitmap;
import android.media.MediaMetadataRetriever;
import android.media.ThumbnailUtils;
import android.net.Uri;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.util.Log;
import android.widget.Toast;

import com.google.android.gms.tasks.Continuation;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CountDownLatch;

public class DeleteVideoFileIntentService extends IntentService {
    // TODO: アクションの名前を変更し、そのタスクを説明するアクション名を選択してください。
    private final String ACTION_DeleteVideo = "com.example.kobayashi_satoru.miroyo.action.DeleteVideo";
    private final CountDownLatch DeleteCountDownLatch = new CountDownLatch(5);
    private List<String> videoIDs;

    public DeleteVideoFileIntentService() {
        super("DeleteVideoFileIntentService");
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        Log.d("onHandleIntent","起動おおおおおおおおおおおおおおおおおおおおおおおおおおお");
        if (intent != null) {
            final String action = intent.getAction();
            if (ACTION_DeleteVideo.equals(action)) {
                String videoID = intent.getStringExtra("videoID");
                String videoName = intent.getStringExtra("VideoName");
                videoIDs = intent.getStringArrayListExtra("videoIDs");
                handleActionDeleteVideo(videoID ,videoName);
            }
        }
    }
    private void handleActionDeleteVideo(String videoID, String videoName) {
        FetchVideosMetaData(videoID, videoName);
    }

    public void FetchVideosMetaData(String videoID, String videoName){
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        FirebaseStorage firebaseStorage = FirebaseStorage.getInstance();
        StorageReference storageRef = firebaseStorage.getReference();
        Context context = getApplicationContext();
        //Toast.makeText(context , "\"" + videoName + "\"\nの削除開始", Toast.LENGTH_LONG).show();
        
        DeleteVideoThumbnailFirebaseStorage(storageRef, videoID, videoName);
        DeleteVideoFirebaseStorage(storageRef, videoID, videoName);
        DeleteVideosFireStore(db, videoID, videoName);
        DeleteVideosOfUsersFireStore(db, videoID, videoName);
        try {
            DeleteCountDownLatch.await();//動画ファイルと動画のサムネファイルのアップロードが終わるまで待機
            Log.d("onHandleIntent","非同期処理の統合成功！！");
        } catch (InterruptedException e) {
            Log.d("onHandleIntent","非同期処理の待ち合わせ失敗！！");
            e.printStackTrace();
        }
        //Toast.makeText(context , "\"" + videoName + "\"\nの削除を完了しました。", Toast.LENGTH_LONG).show();
    }

    public void DeleteVideoThumbnailFirebaseStorage(StorageReference storageRef, String videoID, String videoName){
        Log.d("onHandleIntent","Videoサムネ画像のアップロード開始");
        String thumbnailFileName = null;
        if (videoName.endsWith(".mp4")) {
            thumbnailFileName = videoName.replaceAll(".mp4", ".jpg");//ファイル名の.mp4を.jpgに変換 TODO webmなどにも対応
        } else if (videoName.endsWith(".webm")){
            thumbnailFileName = videoName.replaceAll(".webm", ".jpg");
        }
        
        final StorageReference videoThumbnailsRef = storageRef
                .child("videoThumbnails/" + videoID + thumbnailFileName);//ランダム変数追加

        //ビデオサムネ画像ファイルの削除
        videoThumbnailsRef.delete().addOnSuccessListener(new OnSuccessListener<Void>() {
            @Override
            public void onSuccess(Void aVoid) {
                DeleteCountDownLatch.countDown();
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception exception) {
                DeleteCountDownLatch.countDown();
            }
        });
    }

    public void DeleteVideoFirebaseStorage(StorageReference storageRef, String videoID, String videoFileName) {
        Log.d("onHandleIntent","Videoファイルの削除開始");
        StorageReference videosRef = storageRef.child("videos/" + videoID + videoFileName);
        //ビデオファイルの削除
        videosRef.delete().addOnSuccessListener(new OnSuccessListener<Void>() {
            @Override
            public void onSuccess(Void aVoid) {
                DeleteCountDownLatch.countDown();
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception exception) {
                DeleteCountDownLatch.countDown();
            }
        });
    }

    public void DeleteVideosFireStore(final FirebaseFirestore db, String videoID, String videoName) {
        db.collection("videos")
                .document(videoID)
                .delete()
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        DeleteCountDownLatch.countDown();
                        Log.d("DeleteVideosFire","myVideosの削除成功");
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        DeleteCountDownLatch.countDown();
                        Log.d("DeleteVideosFire","myVideosの削除成功");
                    }
                });
    }
    public void DeleteVideosOfUsersFireStore(FirebaseFirestore db, String videoID, final String videoName){
        FirebaseAuth firebaseAuth = FirebaseAuth.getInstance();
        FirebaseUser currentUser = firebaseAuth.getCurrentUser();
        String myUserID = currentUser.getUid();
        CollectionReference myVideosCollectionReference = db.collection("users").document(myUserID).collection("videos");
        myVideosCollectionReference
                .document(videoID)
                .delete()
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        DeleteCountDownLatch.countDown();
                        Log.d("DeleteVideosOfUsersFire","myVideosの削除成功");
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        DeleteCountDownLatch.countDown();
                        Log.d("DeleteVideosOfUsersFire","myVideosの削除失敗",e);
                    }
                });

        //TODO videoIDsを既存のものから受け取る
        videoIDs.remove(videoIDs.indexOf(videoID));
        myVideosCollectionReference
                .document("VideosData")
                .update("VideoIDs",videoIDs)
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        DeleteCountDownLatch.countDown();
                        Log.d("VideoIDs","videoIDs追加成功");
                    }
                });
    }
}

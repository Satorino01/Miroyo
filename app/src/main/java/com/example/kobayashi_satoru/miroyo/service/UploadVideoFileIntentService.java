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
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CountDownLatch;

public class UploadVideoFileIntentService extends IntentService {
    // TODO: アクションの名前を変更し、そのタスクを説明するアクション名を選択してください。
    private final String ACTION_UploadVideo = "com.example.kobayashi_satoru.miroyo.action.UploadVideo";
    private final CountDownLatch uploadCountDownLatch = new CountDownLatch(3);
    private String videoID;
    private String videoURL;
    private String videoThumbnailURL;
    private List<String> videoIDs;
    private int videoPlayTime;

    public UploadVideoFileIntentService() {
        super("UploadVideoFileIntentService");
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        Log.d("onHandleIntent","起動おおおおおおおおおおおおおおおおおおおおおおおおおおお");
        if (intent != null) {
            final String action = intent.getAction();
            if (ACTION_UploadVideo.equals(action)) {
                final String[] filesPass = intent.getStringArrayExtra("filesPass");
                videoIDs = intent.getStringArrayListExtra("videoIDs");
                handleActionUploadVideo(filesPass);
            }
        }
    }

    private void handleActionUploadVideo(String[] filesPass) {
        Context context = getApplicationContext();
        Toast.makeText(context , "動画ファイルのアップロードを開始しました。", Toast.LENGTH_LONG).show();
        FetchVideosID(filesPass);
    }

    public void FetchVideosID(String[] filesPass){
        final FirebaseFirestore db = FirebaseFirestore.getInstance();

        for (final String filePass : filesPass){
            final File videoFile = new File(filePass);
            int videoByte = (int) videoFile.length();
            final Uri videoFileUri = Uri.fromFile(videoFile);
            final String videoFileName = videoFileUri.getLastPathSegment();

            Map<String, Object> video = new HashMap();
            video.put("VideoName", videoFileName);
            db.collection("videos").add(video)
                    .addOnSuccessListener(new OnSuccessListener<DocumentReference>(){
                        @Override
                        public void onSuccess(DocumentReference documentReference) {

                            FirebaseStorage firebaseStorage = FirebaseStorage.getInstance();
                            final StorageReference storageRef = firebaseStorage.getReference();
                            videoID = documentReference.getId();

                            PutVideoThumbnailFirebaseStorage(storageRef, videoFileName, filePass, videoID);
                            PutVideoFirebaseStorage(storageRef, videoFileName, videoFileUri, filePass, videoID);
                            videoPlayTime = fetchPlayTime(videoFileUri);//動画の再生時間を取得
                            Log.d("onHandleIntent","PlayTime:"+String.valueOf(videoPlayTime));
                            uploadCountDownLatch.countDown();
                        }
                    })
                    .addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            Log.d("FetchVideosID","videokeyの取得エラー",e);
                            //progressDialog.hide();
                        }
                    });
            try {
                uploadCountDownLatch.await();//動画ファイルと動画のサムネファイルのアップロードが終わるまで待機
                Log.d("onHandleIntent","非同期処理の統合成功！！");
                Log.d("onHandleIntent","VideoURL"+videoURL);
                Log.d("onHandleIntent","VideoThumbnailURL"+videoThumbnailURL);

                WriteVideosFireStore(db, videoID, videoFileName, videoURL, videoThumbnailURL, videoPlayTime, videoByte);
            } catch (InterruptedException e) {
                Log.d("onHandleIntent","非同期処理の待ち合わせ失敗！！");
                e.printStackTrace();
            }
        }
    }

    public void PutVideoThumbnailFirebaseStorage(StorageReference storageRef, String videoFileName, final String filePass, final String videoID){
        Log.d("onHandleIntent","Videoサムネ画像のアップロード開始");
        //動画のサムネ画像を Firebase ストレージ に保存して URL を取得
        Bitmap videoThumbnail = ThumbnailUtils.createVideoThumbnail(filePass, MediaStore.Images.Thumbnails.MINI_KIND);//(512×384)
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        videoThumbnail.compress(Bitmap.CompressFormat.JPEG, 100, baos);
        byte[] data = baos.toByteArray();

        String thumbnailFileName = null;
        if (filePass.endsWith(".mp4")) {
            thumbnailFileName = videoFileName.replaceAll(".mp4", ".jpg");//ファイル名の.mp4を.jpgに変換 TODO webmなどにも対応
        } else if (filePass.endsWith(".webm")){
            thumbnailFileName = videoFileName.replaceAll(".webm", ".jpg");
        } else {
            //TODO 例外処理
        }

        final StorageReference videoThumbnailsRef = storageRef
                .child("videoThumbnails/" + videoID + thumbnailFileName);//ランダム変数追加

        //ビデオサムネ画像のアップロード
        UploadTask uploadTask = videoThumbnailsRef.putBytes(data);
        uploadTask.continueWithTask(new Continuation<UploadTask.TaskSnapshot, Task<Uri>>() {
            @Override
            public Task<Uri> then(@NonNull Task<UploadTask.TaskSnapshot> task) throws Exception {
                if (!task.isSuccessful()) {
                    throw task.getException();
                }
                return videoThumbnailsRef.getDownloadUrl();
            }
        }).addOnCompleteListener(new OnCompleteListener<Uri>() {
            @Override
            public void onComplete(@NonNull Task<Uri> task) {
                if (task.isSuccessful()) {
                    videoThumbnailURL = task.getResult().toString();
                    Log.d("onHandleIntent","Videoサムネ画像のアップロード完了");
                    uploadCountDownLatch.countDown();
                }else{
                    Log.d("onHandleIntent","Videoサムネ画像のアップロード失敗いいいいいいいい");
                }
            }
        });
    }

    public void PutVideoFirebaseStorage(StorageReference storageRef, String videoFileName, Uri videoFileUri, String filePass, final String videoID) {
        Log.d("onHandleIntent","Videoファイルのアップロード開始");
        final StorageReference videosRef = storageRef.child("videos/" + videoID + videoFileName);
        UploadTask uploadVideoTask = videosRef.putFile(videoFileUri);
        uploadVideoTask.continueWithTask(new Continuation<UploadTask.TaskSnapshot, Task<Uri>>() {
            @Override
            public Task<Uri> then(@NonNull Task<UploadTask.TaskSnapshot> task) throws Exception {
                if (!task.isSuccessful()) {
                    throw task.getException();
                }
                return videosRef.getDownloadUrl();
            }
        }).addOnCompleteListener(new OnCompleteListener<Uri>() {
            @Override
            public void onComplete(@NonNull Task<Uri> task) {
                if (task.isSuccessful()) {
                    videoURL = task.getResult().toString();
                    Log.d("onHandleIntent","Videoファイルのアップロード完了");
                    uploadCountDownLatch.countDown();
                }else{
                    Log.d("onHandleIntent","Videoファイルのアップロード失敗いいいいいいいいいい");
                }
            }
        });
    }

    public void WriteVideosFireStore(final FirebaseFirestore db,
                                     final String videoID,
                                     String videoName,
                                     String videoURL,
                                     String videoThumbnailURL,
                                     int playTimeMilliSecond,
                                     int videoByte) {
        // Create a new user with a first and last name
        final Map<String, Object> video = new HashMap<>();
        //TODO videoオブジェクトに変換
        video.put("VideoName", videoName);
        video.put("VideoURL", videoURL);
        video.put("PlayTimeMilliSecond", playTimeMilliSecond);
        video.put("ThumbnailURL", videoThumbnailURL);
        video.put("VideoByte", videoByte);

        db.collection("videos")
                .document(videoID)
                .update(video)
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        WriteVideosOfUsersFireStore(db ,videoID, video);
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                    }
                });
    }

    public void WriteVideosOfUsersFireStore(FirebaseFirestore db , String videoID , final Map video){
        FirebaseAuth firebaseAuth = FirebaseAuth.getInstance();
        FirebaseUser currentUser = firebaseAuth.getCurrentUser();
        String myUserID = currentUser.getUid();
        CollectionReference myVideosCollectionReference = db.collection("users").document(myUserID).collection("videos");
        myVideosCollectionReference
                .document(videoID)
                .set(video)
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        Log.d("WriteVideosOfUsersFire","myVideosに追加成功");
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Log.d("WriteVideosOfUsersFire","myVideosに追加失敗いい",e);
                    }
                });

        //TODO videoIDsを既存のものから受け取る
        videoIDs.add(videoID);
        myVideosCollectionReference
                .document("VideosData")
                .update("VideoIDs",videoIDs)
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        Log.d("VideoIDs","videoIDs追加成功");
                    }
                });
        Context context = getApplicationContext();
        Toast.makeText(context , "動画ファイル\n\""+ video.get("VideoName").toString()+"\"\nのアップロードを完了しました。", Toast.LENGTH_LONG).show();
    }

    private int fetchPlayTime(Uri videoURI) {
        // メディアメタデータにアクセスするクラスをインスタンス化する。
        MediaMetadataRetriever mediaMetadataRetriever = new MediaMetadataRetriever();
        mediaMetadataRetriever.setDataSource(getApplicationContext(), videoURI);
        int msPlayTime = Integer.parseInt(mediaMetadataRetriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_DURATION));
        mediaMetadataRetriever.release();
        return msPlayTime;
    }
}

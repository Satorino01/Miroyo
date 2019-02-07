package com.example.kobayashi_satoru.miroyo;

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

import com.google.android.gms.tasks.Continuation;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
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

public class UploadVideoFileIntentService extends IntentService {
    // TODO: アクションの名前を変更し、そのタスクを説明するアクション名を選択してください。
    private static final String ACTION_UploadVideo = "com.example.kobayashi_satoru.miroyo.action.UploadVideo";

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
                handleActionUploadVideo(filesPass);
            }
        }
    }

    private void handleActionUploadVideo(String[] filesPass) {
        FetchVideosID(filesPass);
    }

    public void FetchVideosID(String[] filesPass){
        final FirebaseFirestore db = FirebaseFirestore.getInstance();

        for (final String filePass : filesPass){
            Uri videoFileUri = Uri.fromFile(new File(filePass));
            String videoFileName = videoFileUri.getLastPathSegment();

            Map<String, Object> video = new HashMap();
            video.put("VideoName", videoFileName);

            db.collection("videos").add(video)
                    .addOnSuccessListener(new OnSuccessListener<DocumentReference>(){
                        @Override
                        public void onSuccess(DocumentReference documentReference) {
                            PutVideoThumbnailFirebaseStorage(db, filePass , documentReference.getId());
                        }
                    })
                    .addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            Log.d("FetchVideosID","videokeyの取得エラー",e);
                            //progressDialog.hide();
                        }
                    });
        }
    }

    public void PutVideoThumbnailFirebaseStorage(final FirebaseFirestore db, final String filePass, final String videoID){
        //動画のサムネ画像を Firebase ストレージ に保存して URL を取得
        Bitmap videoThumbnail = ThumbnailUtils.createVideoThumbnail(filePass, MediaStore.Images.Thumbnails.MINI_KIND);//(512×384)
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        videoThumbnail.compress(Bitmap.CompressFormat.JPEG, 100, baos);
        byte[] data = baos.toByteArray();

        Uri videoFile = Uri.fromFile(new File(filePass));
        String thumbnailFileName = null;
        if (filePass.endsWith(".mp4")) {
            thumbnailFileName = videoFile.getLastPathSegment().replaceAll(".mp4", ".jpg");//ファイル名の.mp4を.jpgに変換 TODO webmなどにも対応
        } else if (filePass.endsWith(".webm")){
            thumbnailFileName = videoFile.getLastPathSegment().replaceAll(".webm", ".jpg");
        } else {
            //TODO 例外処理
        }

        FirebaseStorage firebaseStorage = FirebaseStorage.getInstance();
        final StorageReference storageRef = firebaseStorage.getReference();

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
                    Uri downloadUri = task.getResult();
                    Log.d("ダウンロードに使えるURL",task.getResult().toString());
                    PutVideoFirebaseStorage(db, storageRef, filePass, task.getResult().toString(), videoID);
                } else {

                }
            }
        });
    }

    public void PutVideoFirebaseStorage(final FirebaseFirestore db, StorageReference storageRef, String filePass, final String videoThumbnailURL, final String videoID) {
        final Uri videoFile = Uri.fromFile(new File(filePass));
        final StorageReference videosRef = storageRef.child("videos/" + videoID + videoFile.getLastPathSegment());
        final String videoName = videoFile.getLastPathSegment();
        UploadTask uploadVideoTask = videosRef.putFile(videoFile);
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
                    final int playTime = fetchPlayTime(videoFile);//動画の再生時間を取得
                    Log.d("動画のダウンロードURL",task.getResult().toString());
                    UploadTask.TaskSnapshot taskSnapshot = null;
                    WriteVideosFireStore(db, videoID, taskSnapshot, videoName,task.getResult().toString(), videoThumbnailURL, playTime);
                } else {

                }
            }
        });
    }

    public void WriteVideosFireStore(final FirebaseFirestore db, final String videoID, UploadTask.TaskSnapshot taskSnapshot, String videoName, String videoURL, String videoThumbnailURL, int playTime) {
        // Create a new user with a first and last name
        final Map<String, Object> video = new HashMap<>();
        //TODO videoオブジェクトに変換
        video.put("VideoName", videoName);
        video.put("VideoURL", videoURL);
        video.put("PlayTime", String.valueOf(playTime));
        video.put("ThumbnailURL", videoThumbnailURL);
        //video.put("VideoByte", taskSnapshot.getMetadata().getSizeBytes());

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
    public void WriteVideosOfUsersFireStore(FirebaseFirestore db ,String videoID , Map video){
        FirebaseAuth firebaseAuth = FirebaseAuth.getInstance();
        FirebaseUser currentUser = firebaseAuth.getCurrentUser();
        String myUserID = currentUser.getUid();
        db.collection("users")
                .document(myUserID)
                .collection("videos")
                .document(videoID)
                .set(video)
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        //progressDialog.hide();
                        Log.d("WriteVideosOfUsersFire","onSuccess");
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        //progressDialog.hide();
                        Log.d("WriteVideosOfUsersFire","Failure",e);
                    }
                });

        List<String> videoIDs = new ArrayList<>();

        videoIDs.add(videoID);
        db.collection("users")
                .document(myUserID)
                .collection("videos")
                .document("VideosData")
                .update("VideoIDs",videoIDs)
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        //progressDialog.hide();
                        Log.d("VideoIDs","videoIDs追加成功");
                    }
                });
    }

    private int fetchPlayTime(Uri videoURI) {
        // メディアメタデータにアクセスするクラスをインスタンス化する。
        MediaMetadataRetriever mediaMetadataRetriever = new MediaMetadataRetriever();
        mediaMetadataRetriever.setDataSource(getApplicationContext(), videoURI);
        int secondsPlayTime = Integer.parseInt(mediaMetadataRetriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_DURATION))/ 1000;
        mediaMetadataRetriever.release();
        return secondsPlayTime;
    }
}

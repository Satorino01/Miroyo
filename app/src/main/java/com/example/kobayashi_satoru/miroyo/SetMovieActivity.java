package com.example.kobayashi_satoru.miroyo;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.media.MediaMetadataRetriever;
import android.media.ThumbnailUtils;
import android.net.Uri;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import com.example.kobayashi_satoru.miroyo.adapter.videoAdapter;
import com.example.kobayashi_satoru.miroyo.listener.OnRecyclerListener;
import com.github.angads25.filepicker.controller.DialogSelectionListener;
import com.github.angads25.filepicker.model.DialogConfigs;
import com.github.angads25.filepicker.model.DialogProperties;
import com.github.angads25.filepicker.view.FilePickerDialog;
import com.google.android.gms.tasks.Continuation;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class SetMovieActivity extends AppCompatActivity implements OnRecyclerListener{

    private final String PREF_FILE_NAME = "com.example.kobayashi_satoru.miroyo.SendMovieActivity";

    private videoAdapter videoAdapter;
    private RecyclerView videoRecyclerView;

    private FilePickerDialog filePickerDialog;

    private String myUserID;

    ProgressDialog progressDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.set_movie_activity);

        videoRecyclerView = findViewById(R.id.videoRecyclerView);// RecyclerViewの参照を取得
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(this);// レイアウトマネージャを設定(ここで縦方向の標準リストであることを指定)
        videoRecyclerView.setLayoutManager(linearLayoutManager);

        Intent intent = getIntent();
        myUserID = intent.getStringExtra("myUserID");
        String[] getFieldArray = {"UserName", "EmailAdress" , "FriendIDs" , "VideoIDs" , "videos"};
        final List<String> getFieldList = Arrays.asList(getFieldArray);

        FirebaseFirestore db = FirebaseFirestore.getInstance();

        //TODO サブコレクション使った取得方法
        final Context context = this;
        db.collection("users").document(myUserID).collection("videos").get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
            @Override
            public void onComplete(@NonNull Task<QuerySnapshot> task) {
                if (task.isSuccessful()){
                    QuerySnapshot querySnapshot = task.getResult();
                    List DocumentList = querySnapshot.getDocuments();
                    HashMap videoMaps = new HashMap<>();
                    List videoIDs = new ArrayList();
                    for(Object doc : DocumentList){
                        DocumentSnapshot documentSnapshot = (DocumentSnapshot)doc;
                        if(documentSnapshot.getId().equals("VideosData")){
                            //List<Object> videoIDs = (List) documentSnapshot.getData().get("VideoIDs");
                        }else{
                            videoMaps.put(documentSnapshot.getId(),documentSnapshot.getData());
                            videoIDs.add(documentSnapshot.getId());
                        }
                    }
                    videoAdapter = new videoAdapter(context, videoIDs, videoMaps, (OnRecyclerListener) context);
                    videoRecyclerView.setAdapter(videoAdapter);
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

    public void onClickCreateVideoItemButton(View view){
        progressDialog = new ProgressDialog(this);
        progressDialog.setTitle("アップロード中");
        progressDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
        progressDialog.setCancelable(false);

        //ファイル選択ダイアログ
        DialogProperties properties = new DialogProperties();
        properties.selection_mode = DialogConfigs.SINGLE_MODE;
        properties.selection_type = DialogConfigs.FILE_SELECT;
        properties.root = new File(DialogConfigs.DEFAULT_DIR);
        properties.error_dir = new File(DialogConfigs.DEFAULT_DIR);
        properties.offset = new File(DialogConfigs.DEFAULT_DIR);
        properties.extensions = null;

        filePickerDialog = new FilePickerDialog(this,properties);
        filePickerDialog.setTitle("Select .mp4　or .webm File");
        final Context context = this;

        filePickerDialog.setDialogSelectionListener(new DialogSelectionListener() {
            @Override
            public void onSelectedFilePaths(String[] files) {
                if(checkFileFormat(files)){
                    FetchVideosID(files);
                }else{
                    progressDialog.hide();
                    Toast.makeText(context,"対応しているのはmp4,webm,のみです。",Toast.LENGTH_SHORT).show();
                }
            }
        });
        filePickerDialog.show();
    }

    public boolean checkFileFormat(String[] filesPass){
        for (String filePass : filesPass){
            if(filePass.endsWith(".mp4")){
                return true;
            }else if(filePass.endsWith(".webm")){
                return true;
            }
        }
        return false;
    }

    public void FetchVideosID(String[] filesPass){
        progressDialog.setMessage("アップロード開始");
        progressDialog.show();// TODO　エラーの原因 E/WindowManager: android.view.WindowLeaked: Activity com.example.kobayashi_satoru.miroyo.SetMovieActivity has leaked window DecorView@ea59cd1[アップロード中] that was originally added hereat android.view.ViewRootImpl.<init>(ViewRootImpl.java:511)
        final FirebaseFirestore db = FirebaseFirestore.getInstance();
        for (final String filePass : filesPass){
            Uri videoFileUri = Uri.fromFile(new File(filePass));
            String videoFileName = videoFileUri.getLastPathSegment();

            Map <String, Object> video = new HashMap();
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
                            progressDialog.hide();
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
        db.collection("users")
                .document(myUserID)
                .collection("videos")
                .document(videoID)
                .set(video)
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        progressDialog.hide();
                        Log.d("WriteVideosOfUsersFire","onSuccess");
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        progressDialog.hide();
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
                        progressDialog.hide();
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

    //Android 6.0以上では権限の要求が必要。必要な権限がアプリに付与されたときにDialogを表示する。
    @Override
    public void onRequestPermissionsResult(int requestCode,@NonNull String permissions[],@NonNull int[] grantResults) {
        switch (requestCode) {
            case FilePickerDialog.EXTERNAL_READ_PERMISSION_GRANT: {
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    if(filePickerDialog!=null)
                    {   //読み取り権限が付与されている場合はダイアログを表示します
                        filePickerDialog.show();
                    }
                }
                else {
                    //許可が与えられていません。 ユーザーに通知してください
                    Toast.makeText(this,"ファイルのリストを取得するには権限が必要です",Toast.LENGTH_SHORT).show();
                }
            }
        }
    }
}

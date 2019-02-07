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
                    progressDialog = new ProgressDialog(context);
                    progressDialog.setTitle("アップロード中");
                    progressDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
                    progressDialog.setCancelable(false);
                    progressDialog.setMessage("アップロード開始");
                    //progressDialog.show();// TODO　エラーの原因 E/WindowManager: android.view.WindowLeaked: Activity com.example.kobayashi_satoru.miroyo.SetMovieActivity has leaked window DecorView@ea59cd1[アップロード中] that was originally added hereat android.view.ViewRootImpl.<init>(ViewRootImpl.java:511)
                    startActionUploadVideo(context,files);
                }else{
                    progressDialog.hide();
                    Toast.makeText(context,"対応しているのはmp4,webm,のみです。",Toast.LENGTH_SHORT).show();
                }
            }
        });
        filePickerDialog.show();
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

    public static void startActionUploadVideo(Context context, String[] filesPass) {
        final String ACTION_UploadVideo = "com.example.kobayashi_satoru.miroyo.action.UploadVideo";
        Intent intent = new Intent(context, UploadVideoFileIntentService.class);
        intent.setAction(ACTION_UploadVideo);
        intent.putExtra("filesPass", filesPass);
        context.startService(intent);
    }
}

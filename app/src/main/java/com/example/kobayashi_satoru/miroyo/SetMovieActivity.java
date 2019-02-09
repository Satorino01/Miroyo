package com.example.kobayashi_satoru.miroyo;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.helper.ItemTouchHelper;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import com.example.kobayashi_satoru.miroyo.adapter.videoAdapter;
import com.example.kobayashi_satoru.miroyo.listener.OnRecyclerListener;
import com.github.angads25.filepicker.controller.DialogSelectionListener;
import com.github.angads25.filepicker.model.DialogConfigs;
import com.github.angads25.filepicker.model.DialogProperties;
import com.github.angads25.filepicker.view.FilePickerDialog;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentChange;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.protobuf.StringValue;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CountDownLatch;

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

        final Context context = this;
        final HashMap<String, Map<String, Object>> videoMaps = new HashMap<>();
        final List<String> videoIDs = new ArrayList<>();

        FirebaseFirestore db = FirebaseFirestore.getInstance();
        final CollectionReference myCollectionReference = db.collection("users").document(myUserID).collection("videos");
        myCollectionReference.get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
            @Override
            public void onComplete(@NonNull Task<QuerySnapshot> task) {
                if (task.isSuccessful()){
                    QuerySnapshot querySnapshot = task.getResult();
                    List<DocumentSnapshot> DocumentList = querySnapshot.getDocuments();
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

                    ItemTouchHelper itemTouchHelper  = new ItemTouchHelper(
                            new ItemTouchHelper.SimpleCallback(ItemTouchHelper.UP |
                                    ItemTouchHelper.DOWN, ItemTouchHelper.LEFT) {
                                @Override
                                public boolean onMove(@NonNull RecyclerView recyclerView, @NonNull RecyclerView.ViewHolder viewHolder, @NonNull RecyclerView.ViewHolder target) {
                                    final int fromPos = viewHolder.getAdapterPosition();
                                    final int toPos = target.getAdapterPosition();
                                    Log.d("onMoved","fromPos:" + String.valueOf(fromPos) +"toPos:" + String.valueOf(toPos));
                                    videoAdapter.notifyItemMoved(fromPos, toPos);
                                    return true;// true if moved, false otherwise
                                }

                                @Override
                                public void onSwiped(@NonNull RecyclerView.ViewHolder viewHolder, int direction) {
                                    final int fromPos = viewHolder.getAdapterPosition();
                                    // TODO potionのデータセットの削除
                                    Log.d("onSwiped","fromPos:" + String.valueOf(fromPos));
                                    //videoAdapter.notifyItemRemoved(fromPos);
                                }
                            });
                    itemTouchHelper.attachToRecyclerView(videoRecyclerView);
                    Log.d("onComplete","videoIDsの要素数:"+String.valueOf(videoIDs.size()));
                    Log.d("onComplete","videoMapsの要素数:"+String.valueOf(videoMaps.size()));
                    Log.d("onComplete","videoIDsの中身:"+videoIDs.toString());
                    onDataChanged(myCollectionReference, context, videoIDs, videoMaps);
                }
            }
        });
    }

    public void onDataChanged(CollectionReference myCollectionReference, final Context context, final List videoIDs, final HashMap videoMaps){
        myCollectionReference.addSnapshotListener(new EventListener<QuerySnapshot>() {
            @Override
            public void onEvent(@Nullable QuerySnapshot snapshots, @Nullable FirebaseFirestoreException e) {
                if (e != null) {
                    Log.w("DataChanged", "listen:error", e);
                    return;
                }
                for (DocumentChange documentChange : snapshots.getDocumentChanges()) {
                    switch (documentChange.getType()) {
                        case ADDED://データの追加
                            if(!(documentChange.getDocument().getId().equals("VideosData") || videoIDs.contains(documentChange.getDocument().getId()))) {
                                Log.d("ADDEDonDataChangedID：",documentChange.getDocument().getId());
                                Log.d("ADDEDonDataChangedMet：",documentChange.getDocument().getData().toString());
                                Log.d("ADDEDonDataChanged","videoIDsの要素数:"+String.valueOf(videoIDs.size()));
                                Log.d("ADDEDonDataChanged","videoMapsの要素数:"+String.valueOf(videoMaps.size()));
                                Log.d("ADDEDonDataChanged","videoIDsの中身:"+videoIDs.toString());
                                String newVideoID = documentChange.getDocument().getId();
                                int newPosition = videoMaps.size();
                                Log.d("newPosition",String.valueOf(newPosition));
                                videoAdapter.addItem(newPosition,newVideoID,(HashMap)documentChange.getDocument().getData());
                                videoAdapter.notifyItemInserted(newPosition);
                            }
                            break;
                        case MODIFIED://データの変更
                            if(!documentChange.getDocument().getId().equals("VideosData")) {
//                                String newVideoID = documentChange.getDocument().getId();
//                                videoMaps.put(newVideoID, documentChange.getDocument().getData());
//                                videoAdapter newVideoAdapter = new videoAdapter(context, videoIDs, videoMaps, (OnRecyclerListener) context);
//                                videoRecyclerView.setAdapter(newVideoAdapter);
                            }
                            break;
                        case REMOVED:
                            if(documentChange.getDocument().getId().equals("VideosData")) {
//                                String newVideoID = documentChange.getDocument().getId();
//                                videoMaps.remove(newVideoID);
//                                videoIDs.remove(videoIDs.indexOf(newVideoID));
//                                videoAdapter newVideoAdapter = new videoAdapter(context, videoIDs, videoMaps, (OnRecyclerListener) context);
//                                videoRecyclerView.setAdapter(newVideoAdapter);
                            }
                            break;
                    }
                }

            }
        });
    }

    @Override
    public void onStart() {
        super.onStart();
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
        filePickerDialog.setTitle("Select .mp4 or .webm File");
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

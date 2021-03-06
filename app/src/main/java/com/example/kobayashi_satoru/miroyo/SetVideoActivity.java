package com.example.kobayashi_satoru.miroyo;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.net.ConnectivityManager;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.helper.ItemTouchHelper;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import com.example.kobayashi_satoru.miroyo.adapter.VideoAdapter;
import com.example.kobayashi_satoru.miroyo.listener.OnRecyclerListener;
import com.example.kobayashi_satoru.miroyo.receiver.NetworkReceiver;
import com.example.kobayashi_satoru.miroyo.service.DeleteVideoFileIntentService;
import com.example.kobayashi_satoru.miroyo.service.MovedVideoListIntentService;
import com.example.kobayashi_satoru.miroyo.service.UploadVideoFileIntentService;
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

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class SetVideoActivity extends AppCompatActivity implements OnRecyclerListener, NetworkReceiver.OnNetworkStateChangedListener{

    private VideoAdapter VideoAdapter;
    private RecyclerView videoRecyclerView;
    private FilePickerDialog filePickerDialog;

    private Context context;
    private static HashMap<String, Map<String, Object>> videoMaps;
    private static ArrayList<String> videoIDs;

    private String myUserID;

    private NetworkReceiver mReceiver; //ネットワークの状態監視
    private AlertDialog alertNetworkDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.set_video_activity);

        videoRecyclerView = findViewById(R.id.videoRecyclerView);// RecyclerViewの参照を取得
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(this);// レイアウトマネージャを設定(ここで縦方向の標準リストであることを指定)
        videoRecyclerView.setLayoutManager(linearLayoutManager);

        Intent intent = getIntent();
        myUserID = intent.getStringExtra("myUserID");

        context = this;
        videoMaps = new HashMap<>();
        videoIDs = new ArrayList<>();

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
                            videoIDs = (ArrayList<String>) documentSnapshot.getData().get("VideoIDs");
                            Log.d("videoIDs",videoIDs.toString());
                        }else{
                            videoMaps.put(documentSnapshot.getId(),documentSnapshot.getData());
                        }
                    }
                    VideoAdapter = new VideoAdapter(context, videoIDs, videoMaps, (OnRecyclerListener) context);
                    videoRecyclerView.setAdapter(VideoAdapter);

                    ItemTouchHelper itemTouchHelper  = new ItemTouchHelper(
                            new ItemTouchHelper.SimpleCallback(ItemTouchHelper.UP |
                                    ItemTouchHelper.DOWN, ItemTouchHelper.LEFT) {
                                @Override
                                public boolean onMove(@NonNull RecyclerView recyclerView, @NonNull RecyclerView.ViewHolder viewHolder, @NonNull RecyclerView.ViewHolder target) {
                                    final int fromPos = viewHolder.getAdapterPosition();
                                    final int toPos = target.getAdapterPosition();
                                    Log.d("onMoved","fromPos:" + String.valueOf(fromPos) +"toPos:" + String.valueOf(toPos));
                                    VideoAdapter.moved(fromPos, toPos);//videoIDs内の値交換
                                    startActionMovedVideo(context);
                                    return true;// true if moved, false otherwise
                                }

                                @Override
                                public void onSwiped(@NonNull RecyclerView.ViewHolder viewHolder, int direction) {
                                    final int fromPos = viewHolder.getAdapterPosition();
                                    Log.d("onSwiped","fromPos:" + String.valueOf(fromPos));
                                    String deleteVideoID = videoIDs.get(fromPos);
                                    startActionDeleteVideo(deleteVideoID);
                                    CheckSetVideoID(deleteVideoID);
                                    VideoAdapter.remove(fromPos);
                                }
                            });
                    itemTouchHelper.attachToRecyclerView(videoRecyclerView);
                    Log.d("onComplete","videoIDsの要素数:"+String.valueOf(videoIDs.size()));
                    Log.d("onComplete","videoMapsの要素数:"+String.valueOf(videoMaps.size()));
                    Log.d("onComplete","videoIDsの中身:" + videoIDs.toString());
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
                                Log.d("ADDEDonDataChanged","videoIDsの中身:" + videoIDs.toString());
                                String newVideoID = documentChange.getDocument().getId();
                                int newPosition = videoMaps.size();
                                Log.d("newPosition",String.valueOf(newPosition));
                                VideoAdapter.addItem(newPosition,newVideoID,(HashMap)documentChange.getDocument().getData());
                            }
                            break;
                        case MODIFIED://データの変更
                            if(documentChange.getDocument().getId().equals("VideosData")) {
                            }
                            break;
                        case REMOVED:
                            if(!documentChange.getDocument().getId().equals("VideosData")) {
                                String deleteVideoID = documentChange.getDocument().getId();
                                Log.d("REMOVEDonDataChanged","削除したvideoID:" + deleteVideoID);
                                Log.d("REMOVEDonDataChanged","削除したvideoIDがvideoIDsに含まれているかどうか(うまくいっているなら最後はFalseのはず):" + String.valueOf(videoIDs.contains(deleteVideoID)));
                            }else{
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

    @Override
    public void onResume() {
        super.onResume();
        // Registers BroadcastReceiver to track network connection changes.
        IntentFilter filter = new IntentFilter(ConnectivityManager.CONNECTIVITY_ACTION);
        mReceiver = new NetworkReceiver(this);
        registerReceiver(mReceiver, filter);
    }

    @Override
    public void onPause() {
        super.onPause();
        if(mReceiver != null) {
            unregisterReceiver(mReceiver);
        }
    }

    @Override
    public void changedToWifi() {
        if(alertNetworkDialog != null){
            alertNetworkDialog.dismiss();
            alertNetworkDialog = null;
        }
    }

    @Override
    public void changedToMobile() {
        if(alertNetworkDialog != null){
            alertNetworkDialog.dismiss();
            alertNetworkDialog = null;
        }
    }

    @Override
    public void changedToOffline() {
        alertNetworkDialog = new AlertDialog.Builder(this)
                .setIcon(R.drawable.ic_signal_cellular_off_black_24dp)//.setIcon(R.drawable.ic_signal_wifi_off_black_24dp)
                .setTitle("OFLINE")
                .setMessage("ネットワークに接続してください")
                .show();
        alertNetworkDialog.setCanceledOnTouchOutside(false);
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
                    startActionUploadVideo(context,files);
                }else{
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

    public void startActionUploadVideo(Context context, String[] filesPass) {
        Context applicationContext = getApplicationContext();
        Toast.makeText(applicationContext , "アップロードを開始しました", Toast.LENGTH_LONG).show();

        final String ACTION_UploadVideo = "com.example.kobayashi_satoru.miroyo.action.UploadVideo";
        Intent intent = new Intent(context, UploadVideoFileIntentService.class);
        intent.setAction(ACTION_UploadVideo);
        intent.putExtra("filesPass", filesPass);
        intent.putStringArrayListExtra("videoIDs", videoIDs);
        context.startService(intent);
    }

    //クライアント側で操作した結果（videoIDs）を渡すのみ
    public void startActionMovedVideo(Context context) {
        final String ACTION_MovedVideo = "com.example.kobayashi_satoru.miroyo.action.MovedVideo";
        Intent intent = new Intent(context, MovedVideoListIntentService.class);
        intent.setAction(ACTION_MovedVideo);
        intent.putStringArrayListExtra("videoIDs", videoIDs);
        context.startService(intent);
    }

    //VideoIDで削除するのは確実性が高いから。videoのpositionはリアルタイムで変更される可能性がある。
    public void startActionDeleteVideo(String deleteVideoID) {
        final String ACTION_DeleteVideo = "com.example.kobayashi_satoru.miroyo.action.DeleteVideo";
        Intent intent = new Intent(context, DeleteVideoFileIntentService.class);
        intent.setAction(ACTION_DeleteVideo);
        intent.putExtra("videoID", deleteVideoID);
        intent.putExtra("VideoName",videoMaps.get(deleteVideoID).get("VideoName").toString());
        intent.putStringArrayListExtra("videoIDs",videoIDs);
        context.startService(intent);
    }

    public void CheckSetVideoID(String deleteVideoID){
        String PREF_FILE_NAME = "com.example.kobayashi_satoru.miroyo.SendMovieActivity";
        SharedPreferences sharedPref = context.getSharedPreferences(PREF_FILE_NAME, Context.MODE_PRIVATE);
        String setID = sharedPref.getString("setVideoIDSendMovieActivity","noSetVideoStatus");
        if(setID.equals(deleteVideoID)){
            SharedPreferences.Editor editor = sharedPref.edit();
            editor.putString("setVideoIDSendMovieActivity", "noSetVideoStatus");
            editor.apply();
        }
    }
}

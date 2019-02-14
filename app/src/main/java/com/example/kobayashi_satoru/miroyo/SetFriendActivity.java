package com.example.kobayashi_satoru.miroyo;

import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
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

import com.example.kobayashi_satoru.miroyo.adapter.FriendAdapter;
import com.example.kobayashi_satoru.miroyo.listener.OnRecyclerListener;
import com.example.kobayashi_satoru.miroyo.receiver.NetworkReceiver;
import com.example.kobayashi_satoru.miroyo.service.DeleteFriendIntentService;
import com.example.kobayashi_satoru.miroyo.service.MovedFriendListIntentService;
//import com.example.kobayashi_satoru.miroyo.service.AddFriendIntentService;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentChange;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class SetFriendActivity extends AppCompatActivity implements OnRecyclerListener, NetworkReceiver.OnNetworkStateChangedListener{

    private com.example.kobayashi_satoru.miroyo.adapter.FriendAdapter friendAdapter;
    private RecyclerView friendRecyclerView;

    private Context context;
    private static HashMap<String, Map<String, Object>> friendMaps;
    private static ArrayList<String> friendIDs;

    private String myUserID;

    private NetworkReceiver mReceiver; //ネットワークの状態監視
    private AlertDialog alertNetworkDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.set_friend_activity);

        friendRecyclerView = findViewById(R.id.friendRecyclerView);// RecyclerViewの参照を取得
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(this);// レイアウトマネージャを設定(ここで縦方向の標準リストであることを指定)
        friendRecyclerView.setLayoutManager(linearLayoutManager);

        Intent intent = getIntent();
        myUserID = intent.getStringExtra("myUserID");

        context = this;
        friendMaps = new HashMap<>();
        friendIDs = new ArrayList<>();

        FirebaseFirestore db = FirebaseFirestore.getInstance();
        final CollectionReference myCollectionReference = db.collection("users").document(myUserID).collection("friends");
        myCollectionReference.get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
            @Override
            public void onComplete(@NonNull Task<QuerySnapshot> task) {
                if (task.isSuccessful()){
                    QuerySnapshot querySnapshot = task.getResult();
                    List<DocumentSnapshot> DocumentList = querySnapshot.getDocuments();
                    for(Object doc : DocumentList){
                        DocumentSnapshot documentSnapshot = (DocumentSnapshot)doc;
                        if(documentSnapshot.getId().equals("VideosData")){
                            friendIDs = (ArrayList<String>) documentSnapshot.getData().get("FriendIDs");
                            Log.d("friendIDs",friendIDs.toString());
                        }else{
                            friendMaps.put(documentSnapshot.getId(),documentSnapshot.getData());
                        }
                    }
                    friendAdapter = new FriendAdapter(context, friendIDs, friendMaps, (OnRecyclerListener) context);
                    friendRecyclerView.setAdapter(friendAdapter);

                    ItemTouchHelper itemTouchHelper  = new ItemTouchHelper(
                            new ItemTouchHelper.SimpleCallback(ItemTouchHelper.UP |
                                    ItemTouchHelper.DOWN, ItemTouchHelper.LEFT) {
                                @Override
                                public boolean onMove(@NonNull RecyclerView recyclerView, @NonNull RecyclerView.ViewHolder viewHolder, @NonNull RecyclerView.ViewHolder target) {
                                    final int fromPos = viewHolder.getAdapterPosition();
                                    final int toPos = target.getAdapterPosition();
                                    Log.d("onMoved","fromPos:" + String.valueOf(fromPos) +"toPos:" + String.valueOf(toPos));
                                    friendAdapter.moved(fromPos, toPos);//friendIDs内の値交換
                                    startActionMovedFriend(context);
                                    return true;// true if moved, false otherwise
                                }

                                @Override
                                public void onSwiped(@NonNull RecyclerView.ViewHolder viewHolder, int direction) {
                                    final int fromPos = viewHolder.getAdapterPosition();
                                    Log.d("onSwiped","fromPos:" + String.valueOf(fromPos));
                                    String deleteFriendID = friendIDs.get(fromPos);
                                    startActionDeleteFriend(deleteFriendID);
                                    CheckSetFriendID(deleteFriendID);
                                    friendAdapter.remove(fromPos);
                                }
                            });
                    itemTouchHelper.attachToRecyclerView(friendRecyclerView);
                    Log.d("onComplete","friendIDsの要素数:"+String.valueOf(friendIDs.size()));
                    Log.d("onComplete","friendMapsの要素数:"+String.valueOf(friendMaps.size()));
                    Log.d("onComplete","friendIDsの中身:" + friendIDs.toString());
                    onDataChanged(myCollectionReference, context, friendIDs, friendMaps);
                }
            }
        });
    }

    public void onDataChanged(CollectionReference myCollectionReference, final Context context, final List friendIDs, final HashMap friendMaps){
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
                            if(!(documentChange.getDocument().getId().equals("FriendsData") || friendIDs.contains(documentChange.getDocument().getId()))) {
                                Log.d("ADDEDonDataChangedID：",documentChange.getDocument().getId());
                                Log.d("ADDEDonDataChangedMet：",documentChange.getDocument().getData().toString());
                                Log.d("ADDEDonDataChanged","friendIDsの要素数:"+String.valueOf(friendIDs.size()));
                                Log.d("ADDEDonDataChanged","friendMapsの要素数:"+String.valueOf(friendMaps.size()));
                                Log.d("ADDEDonDataChanged","friendIDsの中身:" + friendIDs.toString());
                                String newFriendID = documentChange.getDocument().getId();
                                int newPosition = friendMaps.size();
                                Log.d("newPosition",String.valueOf(newPosition));
                                friendAdapter.addItem(newPosition,newFriendID,(HashMap)documentChange.getDocument().getData());
                                //friendAdapter.notifyItemInserted(newPosition);
                            }
                            break;
                        case MODIFIED://データの変更
                            if(documentChange.getDocument().getId().equals("FriendsData")) {
//                                friendMaps.put(newFriendID, documentChange.getDocument().getData());
//                                friendAdapter newFriendAdapter = new friendAdapter(context, friendIDs, friendMaps, (OnRecyclerListener) context);
//                                friendRecyclerView.setAdapter(newFriendAdapter);
                            }
                            break;
                        case REMOVED:
                            if(!documentChange.getDocument().getId().equals("FriendsData")) {
                                String deleteFriendID = documentChange.getDocument().getId();
                                Log.d("REMOVEDonDataChanged","削除したfriendID:" + deleteFriendID);
                                Log.d("REMOVEDonDataChanged","削除したfriendIDがfriendIDsに含まれているかどうか(うまくいっているならFalseのはず):" + String.valueOf(friendIDs.contains(deleteFriendID)));
//                                friendMaps.remove(newFriendID);
//                                friendIDs.remove(friendIDs.indexOf(newFriendID));
//                                friendAdapter newFriendAdapter = new friendAdapter(context, friendIDs, friendMaps, (OnRecyclerListener) context);
//                                friendRecyclerView.setAdapter(newFriendAdapter);
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

//    public void startActionAddFriend(Context context, String[] filesPass) {
//        Context applicationContext = getApplicationContext();
//        Toast.makeText(applicationContext , "アップロードを開始しました", Toast.LENGTH_LONG).show();
//
//        final String ACTION_UploadFriend = "com.example.kobayashi_satoru.miroyo.action.UploadFriend";
//        Intent intent = new Intent(context, AddFriendIntentService.class);
//        intent.setAction(ACTION_UploadFriend);
//        intent.putExtra("filesPass", filesPass);
//        intent.putStringArrayListExtra("friendIDs", friendIDs);
//        context.startService(intent);
//    }

    //クライアント側で操作した結果（friendIDs）を渡すのみ
    public void startActionMovedFriend(Context context) {
        final String ACTION_MovedFriend = "com.example.kobayashi_satoru.miroyo.action.MovedFriend";
        Intent intent = new Intent(context, MovedFriendListIntentService.class);
        intent.setAction(ACTION_MovedFriend);
        intent.putStringArrayListExtra("friendIDs", friendIDs);
        context.startService(intent);
    }

    //FriendIDで削除するのは確実性が高いから。friendのpositionはリアルタイムで変更される可能性がある。
    public void startActionDeleteFriend(String deleteFriendID) {
        final String ACTION_DeleteFriend = "com.example.kobayashi_satoru.miroyo.action.DeleteFriend";
        Intent intent = new Intent(context, DeleteFriendIntentService.class);
        intent.setAction(ACTION_DeleteFriend);
        intent.putExtra("friendID", deleteFriendID);
        intent.putExtra("FriendName",friendMaps.get(deleteFriendID).get("FriendName").toString());
        intent.putStringArrayListExtra("friendIDs",friendIDs);
        context.startService(intent);
    }

    public void CheckSetFriendID(String deleteFriendID){
        String PREF_FILE_NAME = "com.example.kobayashi_satoru.miroyo.SendMovieActivity";
        SharedPreferences sharedPref = context.getSharedPreferences(PREF_FILE_NAME, Context.MODE_PRIVATE);
        String setID = sharedPref.getString("setFriendIDSendMovieActivity","noSetFriendStatus");
        if(setID.equals(deleteFriendID)){
            SharedPreferences.Editor editor = sharedPref.edit();
            editor.putString("setFriendIDSendMovieActivity", "noSetFriendStatus");
            editor.apply();
        }
    }

    public void onClickAddFriendButton(View view){

    }
}

package com.example.kobayashi_satoru.miroyo;

import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
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

import com.example.kobayashi_satoru.miroyo.adapter.FriendsRequestAdapter;
import com.example.kobayashi_satoru.miroyo.listener.OnRecyclerListener;
import com.example.kobayashi_satoru.miroyo.receiver.NetworkReceiver;
import com.example.kobayashi_satoru.miroyo.service.DeleteFriendIntentService;
import com.example.kobayashi_satoru.miroyo.service.MovedFriendListIntentService;
//import com.example.kobayashi_satoru.miroyo.service.AddFriendIntentService;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class AddFriendActivity extends AppCompatActivity implements OnRecyclerListener, NetworkReceiver.OnNetworkStateChangedListener{

    private com.example.kobayashi_satoru.miroyo.adapter.FriendsRequestAdapter friendsRequestAdapter;
    private RecyclerView friendsRequestRecyclerView;

    private Context context;
    private static HashMap<String, Map<String, Object>> friendsRequestMaps;
    private static ArrayList<String> friendsRequestIDs;

    private String myUserID;

    private NetworkReceiver mReceiver; //ネットワークの状態監視
    private AlertDialog alertNetworkDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.add_friend_activity);

        friendsRequestRecyclerView = findViewById(R.id.friendsRequestRecyclerView);// RecyclerViewの参照を取得
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(this);// レイアウトマネージャを設定(ここで縦方向の標準リストであることを指定)
        friendsRequestRecyclerView.setLayoutManager(linearLayoutManager);

        Intent intent = getIntent();
        myUserID = intent.getStringExtra("myUserID");

        context = this;
        friendsRequestMaps = new HashMap<>();
        friendsRequestIDs = new ArrayList<>();

        FirebaseFirestore db = FirebaseFirestore.getInstance();
        final DocumentReference myDocumentReference = db.collection("friendsRequest").document(myUserID);
        myDocumentReference.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                if (task.isSuccessful()){
                    DocumentSnapshot documentSnapshot = task.getResult();
                    Map documentMap = documentSnapshot.getData();
                    //friendsRequestIDs = new ArrayList<>(documentMap.keySet());
                    Log.d("AddFriendActivityMap",documentMap.toString());
                    friendsRequestIDs = (ArrayList<String>) documentMap.get("friendsRequestIDs");
                    for(String key : friendsRequestIDs){ ;
                        friendsRequestMaps.put(key,(HashMap)documentMap.get(key));
                    }
                    friendsRequestAdapter = new FriendsRequestAdapter(context, friendsRequestIDs, friendsRequestMaps, (OnRecyclerListener) context);
                    friendsRequestRecyclerView.setAdapter(friendsRequestAdapter);

                    ItemTouchHelper itemTouchHelper  = new ItemTouchHelper(
                            new ItemTouchHelper.SimpleCallback(ItemTouchHelper.UP |
                                    ItemTouchHelper.DOWN, ItemTouchHelper.LEFT) {
                                @Override
                                public boolean onMove(@NonNull RecyclerView recyclerView, @NonNull RecyclerView.ViewHolder viewHolder, @NonNull RecyclerView.ViewHolder target) {
                                    final int fromPos = viewHolder.getAdapterPosition();
                                    final int toPos = target.getAdapterPosition();
                                    Log.d("onMoved","fromPos:" + String.valueOf(fromPos) +"toPos:" + String.valueOf(toPos));
                                    friendsRequestAdapter.moved(fromPos, toPos);//friendsRequestIDs内の値交換
                                    startActionMovedFriend(context);
                                    return true;// true if moved, false otherwise
                                }

                                @Override
                                public void onSwiped(@NonNull RecyclerView.ViewHolder viewHolder, int direction) {
                                    final int fromPos = viewHolder.getAdapterPosition();
                                    Log.d("onSwiped","fromPos:" + String.valueOf(fromPos));
                                    String deleteFriendID = friendsRequestIDs.get(fromPos);
                                    startActionDeleteFriend(deleteFriendID);
                                    friendsRequestAdapter.remove(fromPos);
                                }
                            });
                    itemTouchHelper.attachToRecyclerView(friendsRequestRecyclerView);
                    Log.d("onComplete","friendsRequestIDsの要素数:"+String.valueOf(friendsRequestIDs.size()));
                    Log.d("onComplete","friendMapsの要素数:"+String.valueOf(friendsRequestMaps.size()));
                    Log.d("onComplete","friendsRequestIDsの中身:" + friendsRequestIDs.toString());
                    onDataChanged(myDocumentReference, context, friendsRequestIDs, friendsRequestMaps);
                }
            }
        });
    }

    public void onDataChanged(DocumentReference myDocumentReference, final Context context, final List friendsRequestIDs, final HashMap friendsRequestMaps){
        myDocumentReference.addSnapshotListener(new EventListener<DocumentSnapshot>() {
            @Override
            public void onEvent(@Nullable DocumentSnapshot snapshots, @Nullable FirebaseFirestoreException e) {
                if (e != null) {
                    Log.w("DataChanged", "listen:error", e);
                    return;
                }
                Map snapshotsMap = snapshots.getData();
                ArrayList snapshotsIDs = new ArrayList<>(snapshotsMap.keySet());
                //Listの数が増えてたら更新する処
                if(friendsRequestIDs.size() < snapshotsIDs.size() - 1) {//カラム friendRequestIDsの分を引く
                    Log.d("ADDEDonDataChanged","friendsRequestIDsの要素数:"+String.valueOf(friendsRequestIDs.size()));
                    Log.d("ADDEDonDataChanged","snapshotsIDsの中身:"+snapshotsIDs.toString());
                    Log.d("ADDEDonDataChanged","friendsRequestIDsの要素数:"+String.valueOf(friendsRequestIDs.size()));
                    Log.d("ADDEDonDataChanged","friendsRequestIDsの中身:" + friendsRequestIDs.toString());
                    boolean isRetain = snapshotsIDs.retainAll(friendsRequestIDs);
                    Log.d("isRetain",String.valueOf(isRetain));
                    for(Object snapshotsID : snapshotsIDs){
                        String newFriendRequestID = snapshotsID.toString();
                        int newPosition = snapshotsMap.size();
                        Log.d("newPosition",String.valueOf(newPosition));
                        friendsRequestAdapter.addItem(newPosition,newFriendRequestID,(HashMap)snapshotsMap.get(newFriendRequestID));
                    }
                    //friendsRequestAdapter.notifyItemInserted(newPosition);
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

    //クライアント側で操作した結果（friendsRequestIDs）を渡すのみ
    public void startActionMovedFriend(Context context) {
        final String ACTION_MovedFriend = "com.example.kobayashi_satoru.miroyo.action.MovedFriend";
        Intent intent = new Intent(context, MovedFriendListIntentService.class);
        intent.setAction(ACTION_MovedFriend);
        intent.putStringArrayListExtra("friendsRequestIDs", friendsRequestIDs);
        context.startService(intent);
    }

    //FriendIDで削除するのは確実性が高いから。friendのpositionはリアルタイムで変更される可能性がある。
    public void startActionDeleteFriend(String deleteFriendID) {
        final String ACTION_DeleteFriend = "com.example.kobayashi_satoru.miroyo.action.DeleteFriend";
        Intent intent = new Intent(context, DeleteFriendIntentService.class);
        intent.setAction(ACTION_DeleteFriend);
        intent.putExtra("friendsRequestID", deleteFriendID);
        intent.putExtra("FriendName",friendsRequestMaps.get(deleteFriendID).get("FriendName").toString());
        intent.putStringArrayListExtra("friendsRequestIDs",friendsRequestIDs);
        context.startService(intent);
    }
}

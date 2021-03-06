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

public class MovedFriendListIntentService extends IntentService {
    // TODO: アクションの名前を変更し、そのタスクを説明するアクション名を選択してください。
    private final String ACTION_MovedFriend = "com.example.kobayashi_satoru.miroyo.action.MovedFriend";
    private List<String> friendIDs;
    private final CountDownLatch finishCountDownLatch = new CountDownLatch(1);

    public MovedFriendListIntentService() {
        super("MovedFriendListIntentService");
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        Log.d("onHandleIntent","起動おおおおおおおおおおおおおおおおおおおおおおおおおおお");
        if (intent != null) {
            final String action = intent.getAction();
            if (ACTION_MovedFriend.equals(action)) {
                friendIDs = intent.getStringArrayListExtra("friendIDs");
                try {
                    handleActionMovedFriend();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }
    }
    private void handleActionMovedFriend() throws InterruptedException {
        MovedFriendIDsOfUsers();
    }
    public void MovedFriendIDsOfUsers() throws InterruptedException {
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        FirebaseAuth firebaseAuth = FirebaseAuth.getInstance();
        FirebaseUser currentUser = firebaseAuth.getCurrentUser();
        String myUserID = currentUser.getUid();
        CollectionReference myFriendsCollectionReference = db.collection("users").document(myUserID).collection("friends");
        myFriendsCollectionReference
                .document("FriendsData")
                .update("FriendIDs",friendIDs)
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        Log.d("FriendIDs","friendIDsの順番変更成功");
                    }
                });
        finishCountDownLatch.await();
        Log.d("awaitFinishCountDown","サービス終了：" + String.valueOf(finishCountDownLatch.getCount()));
        stopSelf();
    }
}

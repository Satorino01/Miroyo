package com.example.kobayashi_satoru.miroyo.service;

import android.app.IntentService;
import android.content.Intent;
import android.support.annotation.NonNull;
import android.util.Log;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.List;
import java.util.concurrent.CountDownLatch;

public class DeleteFriendIntentService extends IntentService {
    private final String ACTION_DeleteFriend = "com.example.kobayashi_satoru.miroyo.action.DeleteFriend";
    private final CountDownLatch deleteCountDownLatch = new CountDownLatch(2);
    private List<String> friendIDs;

    public DeleteFriendIntentService() {
        super("DeleteFriendFileIntentService");
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        Log.d("onHandleIntent","起動おおおおおおおおおおおおおおおおおおおおおおおおおおお");
        if (intent != null) {
            final String action = intent.getAction();
            if (ACTION_DeleteFriend.equals(action)) {
                String friendID = intent.getStringExtra("friendID");
                String friendName = intent.getStringExtra("FriendName");
                friendIDs = intent.getStringArrayListExtra("friendIDs");
                try {
                    handleActionDeleteFriend(friendID ,friendName);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }
    }
    private void handleActionDeleteFriend(String friendID, String friendName) throws InterruptedException {
        DeleteFriendsOfUsers(friendID, friendName);
    }

    public void DeleteFriendsOfUsers(String friendID, final String friendName) throws InterruptedException {
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        FirebaseAuth firebaseAuth = FirebaseAuth.getInstance();
        FirebaseUser currentUser = firebaseAuth.getCurrentUser();
        String myUserID = currentUser.getUid();
        CollectionReference myFriendsCollectionReference = db.collection("users").document(myUserID).collection("friends");
        myFriendsCollectionReference
                .document(friendID)
                .delete()
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        deleteCountDownLatch.countDown();
                        Log.d("DeleteFriendsOfUsers","myFriendsの削除成功");
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        deleteCountDownLatch.countDown();
                        Log.d("DeleteFriendsOfUsers","myFriendsの削除失敗",e);
                    }
                });

        //TODO friendIDsを既存のものから受け取る
        friendIDs.remove(friendIDs.indexOf(friendID));
        myFriendsCollectionReference
                .document("FriendsData")
                .update("FriendIDs",friendIDs)
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        deleteCountDownLatch.countDown();
                        Log.d("FriendIDs","friendIDs追加成功");
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        deleteCountDownLatch.countDown();
                        Log.d("DeleteFriendsOfUsers","myFriendsの削除失敗",e);
                    }
                });
        deleteCountDownLatch.await();
        Log.d("deleteCountDownLatch","サービス終了：" + String.valueOf(deleteCountDownLatch.getCount()));
        stopSelf();
    }
}

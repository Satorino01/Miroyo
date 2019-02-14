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
    // TODO: アクションの名前を変更し、そのタスクを説明するアクション名を選択してください。
    private final String ACTION_DeleteFriend = "com.example.kobayashi_satoru.miroyo.action.DeleteFriend";
    private final CountDownLatch DeleteCountDownLatch = new CountDownLatch(5);
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
                handleActionDeleteFriend(friendID ,friendName);
            }
        }
    }
    private void handleActionDeleteFriend(String friendID, String friendName) {
        DeleteFriendsOfUsersFireStore(friendID, friendName);
    }

    public void DeleteFriendsOfUsersFireStore(String friendID, final String friendName){
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
                        DeleteCountDownLatch.countDown();
                        Log.d("DeleteFriendsOfUsers","myFriendsの削除成功");
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        DeleteCountDownLatch.countDown();
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
                        DeleteCountDownLatch.countDown();
                        Log.d("FriendIDs","friendIDs追加成功");
                    }
                });
    }
}

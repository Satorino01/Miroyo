package com.example.kobayashi_satoru.miroyo.service;

import android.app.IntentService;
import android.content.Intent;
import android.content.Context;
import android.support.annotation.NonNull;
import android.util.Log;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CountDownLatch;

public class DeleteFriendsRequestIntentService extends IntentService {
    private final String ACTION_DeleteFriendsRequest = "com.example.kobayashi_satoru.miroyo.action.DeleteFriendsRequest";
    private final CountDownLatch DeleteCountDownLatch = new CountDownLatch(5);
    private List<String> friendsRequestIDs;

    public DeleteFriendsRequestIntentService() {
        super("DeleteFriendsRequestIntentService");
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        Log.d("onHandleIntent","起動おおおおおおおおおおおおおおおおおおおおおおおおおおお");
        if (intent != null) {
            final String action = intent.getAction();
            if (ACTION_DeleteFriendsRequest.equals(action)) {
                String friendsRequestID = intent.getStringExtra("friendsRequestID");
                friendsRequestIDs = intent.getStringArrayListExtra("friendsRequestIDs");
                handleActionDeleteFriend(friendsRequestID);
            }
        }
    }
    private void handleActionDeleteFriend(String friendsRequestID) {
        DeleteFriendsRequestID(friendsRequestID);
    }

    public void DeleteFriendsRequestID(String friendsRequestID){
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        FirebaseAuth firebaseAuth = FirebaseAuth.getInstance();
        FirebaseUser currentUser = firebaseAuth.getCurrentUser();
        String myUserID = currentUser.getUid();
        Map<String,Object> deleteUpdates = new HashMap<>();
        deleteUpdates.put(friendsRequestID, FieldValue.delete());
        DocumentReference myFriendsRequestsDocumentReference = db.collection("friendsRequest").document(myUserID);
        myFriendsRequestsDocumentReference
                .update(deleteUpdates)
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
        friendsRequestIDs.remove(friendsRequestIDs.indexOf(friendsRequestID));
        myFriendsRequestsDocumentReference
                .update("FriendsRequestIDs",friendsRequestIDs)
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        DeleteCountDownLatch.countDown();
                        Log.d("FriendIDs","friendIDs追加成功");
                    }
                });
    }
}

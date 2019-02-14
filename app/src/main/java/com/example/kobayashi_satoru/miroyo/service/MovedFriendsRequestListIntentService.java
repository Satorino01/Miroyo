package com.example.kobayashi_satoru.miroyo.service;

import android.app.IntentService;
import android.content.Intent;
import android.util.Log;

import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;

import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.List;

public class MovedFriendsRequestListIntentService extends IntentService {
    // TODO: アクションの名前を変更し、そのタスクを説明するアクション名を選択してください。
    private final String ACTION_MovedFriend = "com.example.kobayashi_satoru.miroyo.action.MovedFriendsRequest";

    public MovedFriendsRequestListIntentService() {
        super("MovedFriendsRequestListIntentService");
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        Log.d("onHandleIntent","起動おおおおおおおおおおおおおおおおおおおおおおおおおおお");
        if (intent != null) {
            final String action = intent.getAction();
            if (ACTION_MovedFriend.equals(action)) {
                ArrayList<String> friendsRequestIDs = intent.getStringArrayListExtra("FriendsRequestIDs");
                handleActionMovedFriend(friendsRequestIDs);
            }
        }
    }
    private void handleActionMovedFriend(ArrayList<String> friendsRequestIDs) {
        MovedFriendIDsOfUsers(friendsRequestIDs);
    }
    public void MovedFriendIDsOfUsers(ArrayList<String> friendsRequestIDs){
        Log.d("MovedFriendIDsOfUsers","friendsRequestIDs:" + friendsRequestIDs.toString());
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        FirebaseAuth firebaseAuth = FirebaseAuth.getInstance();
        FirebaseUser currentUser = firebaseAuth.getCurrentUser();
        String myUserID = currentUser.getUid();
        DocumentReference myDocumentReference = db.collection("friendsRequest").document(myUserID);
        myDocumentReference
                .update("FriendsRequestIDs",friendsRequestIDs)
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        Log.d("FriendsRequestIDs","friendsRequestIDsの順番変更成功");
                    }
                });
    }
}

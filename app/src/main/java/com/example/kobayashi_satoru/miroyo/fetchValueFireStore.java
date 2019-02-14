package com.example.kobayashi_satoru.miroyo;

import android.support.annotation.NonNull;
import android.util.Log;

import com.example.kobayashi_satoru.miroyo.data.User;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class fetchValueFireStore implements OnCompleteListener{

    public fetchValueFireStore() {

    }

    public static HashMap fetchMap(final String collectionPath, String ID, final List<String> getFieldList) throws InterruptedException {
        final HashMap resultMap = new HashMap();

        FirebaseFirestore db = FirebaseFirestore.getInstance();
        DocumentReference docRef = db.collection(collectionPath).document(ID);
        docRef.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                if (task.isSuccessful()) {
                    DocumentSnapshot document = task.getResult();
                    if (document != null) {
                        for (String getKey : getFieldList) {
                            if(getKey=="VideoIDs" || getKey=="FriendIDs"){
                                resultMap.put(getKey, (List) document.get(getKey));
                            } else if(getKey=="videos"){
                                //resultMap.put(getKey, (Map) document.get(getKey);
                            } else {
                                resultMap.put(getKey, document.getString(getKey));
                            }
                        }
                        Log.d("LOGGER", "No such document");
                    }
                } else {
                    Log.d("LOGGER", "get failed with ", task.getException());
                }
            }
        });
        return resultMap;
    }

    public User fetchUser(final String collectionPath, final String ID) {

        final User[] user = new User[1];
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        DocumentReference docRef = db.collection(collectionPath).document(ID);
        docRef.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                if (task.isSuccessful()) {
                    DocumentSnapshot document = task.getResult();
                    if (document != null) {
                        user[0] = new User(ID,document.getString("UserName"));
                    }
                } else {
                    Log.d("LOGGER", "get failed with ", task.getException());
                }
            }
        });
        return user[0];
    }

    @Override
    public void onComplete(@NonNull Task task) {

    }
}

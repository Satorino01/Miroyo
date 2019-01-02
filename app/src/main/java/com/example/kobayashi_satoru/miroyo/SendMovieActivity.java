package com.example.kobayashi_satoru.miroyo;

import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;

import com.example.kobayashi_satoru.miroyo.ui.sendmovie.SendMovieFragment;
import com.example.kobayashi_satoru.miroyo.ui.sendmovie.SendMovieViewModel;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.HashMap;
import java.util.Map;

public class SendMovieActivity extends AppCompatActivity {

    private String TAG = "satoru";
    private SendMovieViewModel sendMovieViewModel;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.d(TAG,"AAAAAAAAAAAAA");
        // ここで1秒間スリープし、スプラッシュを表示させたままにする。
        try {
            Thread.sleep(1000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        // スプラッシュthemeを通常themeに変更する
        setTheme(R.style.AppTheme);
        setContentView(R.layout.send_movie_activity);

        FirebaseFirestore db = FirebaseFirestore.getInstance();

        // Create a new user with a first, middle, and last name
        Map<String, Object> user = new HashMap<>();
        user.put("EmailAddress", "asasa@mail");
        user.put("UserName", "Mathison");
        user.put("OAuthTokenFacebook", "rqefd3");
        user.put("OAuthTokenGoogle", "saa323");

        // Add a new document with a generated ID
        db.collection("users")
                .add(user)
                .addOnSuccessListener(new OnSuccessListener<DocumentReference>() {
                    @Override
                    public void onSuccess(DocumentReference documentReference) {
                        Log.d(TAG, "成功しましたあああDocumentSnapshot added with ID: " + documentReference.getId());
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Log.w(TAG, "Error adding document", e);
                    }
                });

        // Create a new user with a first and last name
        db.collection("users")
                .get()
                .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                        if (task.isSuccessful()) {
                            for (QueryDocumentSnapshot document : task.getResult()) {
                                Log.d(TAG, document.getId() + " 成功 => " + document.getData());
                            }
                        } else {
                            Log.w(TAG, "AAAAAAAAAAAAAError getting documents.", task.getException());
                        }
                    }
                });



        if (savedInstanceState == null) {
            //getSupportFragmentManager().beginTransaction().replace(R.id.container, SendMovieFragment.newInstance()).commitNow();
        }
    }
    public void onClickSetMovieButton(View view){
        Intent intent = new Intent(this, SetMovieActivity.class);
        //startActivityForResult(intent);
        startActivity(intent);
    }
    public void onClickSetUserButton(View view){
        Intent intent = new Intent(this, SetUserActivity.class);
        startActivity(intent);
    }

}

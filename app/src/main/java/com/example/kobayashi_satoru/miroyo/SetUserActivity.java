package com.example.kobayashi_satoru.miroyo;

import android.content.Context;
import android.content.SharedPreferences;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;

import com.example.kobayashi_satoru.miroyo.ui.setuser.SetUserFragment;

public class SetUserActivity extends AppCompatActivity {

    private final String PREF_FILE_NAME = "com.example.kobayashi_satoru.miroyo.SendMovieActivity";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.set_user_activity);
        if (savedInstanceState == null) {
        }
    }
    public void onClickUser0(View v){
        SharedPreferences sharedPref = getSharedPreferences(PREF_FILE_NAME, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor =  sharedPref.edit();
        //アイコンや文字列のセット
        editor.putString("setUserIDSendMovieActivity", "wvJVcJftx3QMwdnF4Uzf4kTlNKd2");
        editor.putString("setUserIconSendMovieActivity", "sampleusericon");
        editor.apply();
        finish();
    }
    public void onClickUser1(View v){
        SharedPreferences sharedPref = getSharedPreferences(PREF_FILE_NAME, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor =  sharedPref.edit();
        //アイコンや文字列のセット
        editor.putString("setUserIDSendMovieActivity", "GW2ht4RNQzWfmtEGP4DaO4qsLGG3");
        editor.putString("setUserIconSendMovieActivity", "satoruicon");
        editor.apply();
        finish();
    }
    public void onClickReturnButton(View v){
        finish();
    }
}

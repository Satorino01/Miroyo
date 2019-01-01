package com.example.kobayashi_satoru.miroyo;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;

import com.example.kobayashi_satoru.miroyo.ui.setuser.SetUserFragment;

public class SetUserActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.set_user_activity);
        if (savedInstanceState == null) {
        }
    }
    public void onClickReturnButton(View v){
        finish();
    }
}

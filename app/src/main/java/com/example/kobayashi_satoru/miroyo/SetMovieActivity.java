package com.example.kobayashi_satoru.miroyo;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;

import com.example.kobayashi_satoru.miroyo.ui.setmovie.SetMovieFragment;

public class SetMovieActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.set_movie_activity);
        if (savedInstanceState == null) {
            //getSupportFragmentManager().beginTransaction().replace(R.id.container, SetMovieFragment.newInstance()).commitNow();
        }
    }
    public void onClickReturnButton(View v){
        finish();
    }
}

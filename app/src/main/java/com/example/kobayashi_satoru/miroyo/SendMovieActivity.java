package com.example.kobayashi_satoru.miroyo;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;

import com.example.kobayashi_satoru.miroyo.ui.sendmovie.SendMovieFragment;

public class SendMovieActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.send_movie_activity);
        if (savedInstanceState == null) {
            getSupportFragmentManager().beginTransaction().replace(R.id.container, SendMovieFragment.newInstance()).commitNow();
        }
    }
}

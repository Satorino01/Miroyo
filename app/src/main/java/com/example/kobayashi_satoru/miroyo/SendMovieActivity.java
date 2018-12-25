package com.example.kobayashi_satoru.miroyo;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;

import com.example.kobayashi_satoru.miroyo.ui.sendmovie.SendMovieFragment;

public class SendMovieActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // ここで1秒間スリープし、スプラッシュを表示させたままにする。
        try {
            Thread.sleep(1000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        setTheme(R.style.AppTheme);// スプラッシュthemeを通常themeに変更する
        setContentView(R.layout.send_movie_activity);

        if (savedInstanceState == null) {
            getSupportFragmentManager().beginTransaction().replace(R.id.container, SendMovieFragment.newInstance()).commitNow();
        }
    }
}

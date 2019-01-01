package com.example.kobayashi_satoru.miroyo;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;

import com.example.kobayashi_satoru.miroyo.ui.sendmovie.SendMovieFragment;
import com.example.kobayashi_satoru.miroyo.ui.sendmovie.SendMovieViewModel;

public class SendMovieActivity extends AppCompatActivity {

    private SendMovieViewModel sendMovieViewModel;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // ここで1秒間スリープし、スプラッシュを表示させたままにする。
        try {
            Thread.sleep(1000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        // スプラッシュthemeを通常themeに変更する
        setTheme(R.style.AppTheme);
        setContentView(R.layout.send_movie_activity);

        if (savedInstanceState == null) {
            //getSupportFragmentManager().beginTransaction().replace(R.id.container, SendMovieFragment.newInstance()).commitNow();
        }
    }
    public void onClickSetMovieButton(View view){
        Intent intent = new Intent(this, SetMovieActivity.class);
        //startActivityForResult(intent);
        startActivity(intent);
    }
}

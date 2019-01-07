package com.example.kobayashi_satoru.miroyo;

import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;

import android.widget.VideoView;
import android.net.Uri;
import android.view.View;
import android.media.MediaPlayer;

public class PlayVideoActivity extends AppCompatActivity {

    VideoView videoView;
    ProgressDialog progressDialog;

    String video_name = "https://storage.googleapis.com/miroyo.appspot.com/VID_20181227_204054.mp4";
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.play_video_activity);

        videoView = findViewById(R.id.videoView);
        videoView.setVideoURI(Uri.parse(video_name));

        progressDialog = new ProgressDialog(this);
        progressDialog.setTitle("動画のダウンロード中");
        progressDialog.setMessage("小林慧さんからのリクエストです");
        progressDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
        progressDialog.setButton("受信拒否", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                progressDialog.dismiss();
                finishAndRemoveTask();
            }
        });
        progressDialog.show();

        //videoが再生可能になったら呼ばれるリスナー
        videoView.setOnPreparedListener(new MediaPlayer.OnPreparedListener() {
            @Override
            public void onPrepared(MediaPlayer mediaPlayer) {
                progressDialog.dismiss();
                videoView.start();
            }
        });

        //videoの再生開始時に呼ばれるリスナー
        videoView.setOnInfoListener(new MediaPlayer.OnInfoListener(){
            @Override
            public boolean onInfo(MediaPlayer mediaPlayer, int what, int extra) {
                return false;
            }
        });

        //videoの再生完了時に呼ばれるリスナー
        videoView.setOnCompletionListener(new MediaPlayer.OnCompletionListener(){
            @Override
            public void onCompletion(MediaPlayer mediaPlayer) {
                if(mediaPlayer!=null) {
                    if(mediaPlayer.isPlaying())
                        mediaPlayer.stop();
                    mediaPlayer.reset();
                    mediaPlayer.release();
                }
                finishAndRemoveTask();
            }
        });

        //videoの準備がエラーを起こした時に呼ばれるリスナー
        videoView.setOnErrorListener(new MediaPlayer.OnErrorListener(){
            @Override
            public boolean onError(MediaPlayer mediaPlayer, int what, int extra) {
                progressDialog.dismiss();
                if(mediaPlayer!=null) {
                    if(mediaPlayer.isPlaying())
                        mediaPlayer.stop();
                    mediaPlayer.reset();
                    mediaPlayer.release();
                }
                finishAndRemoveTask();
                return false;
            }
        });

    }

    @Override
    public void onDestroy() {
        super.onDestroy();
    }
    private void repeatVideo(){
        //リピートする場合
        // 先頭に戻す
        videoView.seekTo(0);
        // 再生開始
        videoView.start();
    }

    private void stopVideo(){
        videoView.setVisibility(View.GONE);
        videoView.stopPlayback();
    }
    //videoViewを表示
    //videoViewを非表示
    //videoView.setVisibility(View.INVISIBLE);
}

package com.example.kobayashi_satoru.miroyo;

import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;

import android.widget.VideoView;
import android.net.Uri;
import android.view.View;
import android.media.MediaPlayer;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

public class PlayVideoActivity extends AppCompatActivity {

    VideoView videoView;
    ProgressDialog progressDialog;

    String videoName = "https://storage.googleapis.com/miroyo.appspot.com/failure_cat.mp4";

    String requestID;
    String requestUserName;

    FirebaseFirestore db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.play_video_activity);
        Intent intent = getIntent();
        requestID = intent.getStringExtra("requestID");

        videoView = findViewById(R.id.videoView);
        db = FirebaseFirestore.getInstance();
        db.collection("requests").document(requestID).get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                requestUserName = task.getResult().get("RequestUserName").toString();
                videoName = task.getResult().get("VideoURL").toString();
                videoView.setVideoURI(Uri.parse(videoName));
                showProgressDialog();

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

                        db.collection("requests").document(requestID).delete();

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
                        db.collection("requests").document(requestID).delete();
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
        });
    }

    void showProgressDialog(){
        progressDialog = new ProgressDialog(this);
        progressDialog.setTitle("動画のダウンロード中");
        progressDialog.setMessage(requestUserName + "からのリクエストです");
        progressDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
        progressDialog.setButton("受信拒否", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                db.collection("requests").document(requestID).delete();
                progressDialog.dismiss();
                finishAndRemoveTask();
            }
        });
        progressDialog.show();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        videoView.stopPlayback();
        finishAndRemoveTask();
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
}

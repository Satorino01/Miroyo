package com.example.kobayashi_satoru.miroyo;
import android.content.Intent;
import android.util.Log;

import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;

import java.util.Map;

public class ReceiveVideoFirebaseMessagingService extends FirebaseMessagingService {
    @Override
    public void onMessageReceived(RemoteMessage remoteMessage) {
        // データの受信
        Map<String, String> data = remoteMessage.getData();
        Log.d("プッシュ通知のID：", data.get("id"));
        Log.d("プッシュ通知のLABEL：", data.get("label"));
        Log.d("プッシュ通知のTEXT：", data.get("text"));

        Intent intent = new Intent(this, PlayVideoActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        String requestID = data.get("text");
        intent.putExtra("requestID", requestID);
        getApplication().startActivity(intent);
    }
}

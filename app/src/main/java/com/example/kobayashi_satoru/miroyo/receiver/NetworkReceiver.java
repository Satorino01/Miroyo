package com.example.kobayashi_satoru.miroyo.receiver;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.util.Log;

public class NetworkReceiver extends BroadcastReceiver {

    private OnNetworkStateChangedListener mOnNetworkStateChangedListener;

    public NetworkReceiver(OnNetworkStateChangedListener onNetworkStateChangedListener) {
        Log.d("NetworkReceiver","NetworkReceiverあああああああああああああああああああああああ");
        mOnNetworkStateChangedListener = onNetworkStateChangedListener;
    }

    public interface OnNetworkStateChangedListener {
        void changedToWifi();
        void changedToMobile();
        void changedToOffline();
    }

    String TAG = "NetworkReceiver";
    @Override
    public void onReceive(Context context, Intent intent) {
        ConnectivityManager conn = (ConnectivityManager)
                context.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo networkInfo = conn.getActiveNetworkInfo();

        if (networkInfo != null && networkInfo.getType() == ConnectivityManager.TYPE_WIFI) {
            if(mOnNetworkStateChangedListener!=null) mOnNetworkStateChangedListener.changedToWifi();
            Log.d(TAG, "Network state has changed to WIFI");
        } else if (networkInfo != null) {
            if(mOnNetworkStateChangedListener!=null) mOnNetworkStateChangedListener.changedToMobile();
            Log.d(TAG, "Network state has changed to MOBILE");
        } else {
            if(mOnNetworkStateChangedListener!=null) mOnNetworkStateChangedListener.changedToOffline();
            Log.d(TAG, "I have no idea...");
        }
    }
}

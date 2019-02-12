package com.example.kobayashi_satoru.miroyo;

import android.content.Context;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.net.ConnectivityManager;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;

import com.example.kobayashi_satoru.miroyo.ui.setuser.SetUserFragment;

public class SetUserActivity extends AppCompatActivity implements NetworkReceiver.OnNetworkStateChangedListener{

    private final String PREF_FILE_NAME = "com.example.kobayashi_satoru.miroyo.SendMovieActivity";

    private NetworkReceiver mReceiver; //ネットワークの状態監視
    private AlertDialog alertNetworkDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.set_user_activity);
        if (savedInstanceState == null) {
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        // Registers BroadcastReceiver to track network connection changes.
        IntentFilter filter = new IntentFilter(ConnectivityManager.CONNECTIVITY_ACTION);
        mReceiver = new NetworkReceiver(this);
        registerReceiver(mReceiver, filter);
    }

    @Override
    public void onPause() {
        super.onPause();
        if(mReceiver != null) {
            unregisterReceiver(mReceiver);
        }
    }

    @Override
    public void changedToWifi() {
        if(alertNetworkDialog != null){
            alertNetworkDialog.dismiss();
            alertNetworkDialog = null;
        }
    }

    @Override
    public void changedToMobile() {
        if(alertNetworkDialog != null){
            alertNetworkDialog.dismiss();
            alertNetworkDialog = null;
        }
    }

    @Override
    public void changedToOffline() {
        alertNetworkDialog = new AlertDialog.Builder(this)
                .setIcon(R.drawable.ic_signal_cellular_off_black_24dp)//.setIcon(R.drawable.ic_signal_wifi_off_black_24dp)
                .setTitle("OFLINE")
                .setMessage("ネットワークに接続してください")
                .show();
        alertNetworkDialog.setCanceledOnTouchOutside(false);
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

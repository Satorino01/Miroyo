package com.example.kobayashi_satoru.miroyo.ui;

import android.app.Application;

import com.beardedhen.androidbootstrap.TypefaceProvider;

public class Bootstrap extends Application {
    // Applicationを継承
    @Override public void onCreate() {
        super.onCreate();
        TypefaceProvider.registerDefaultIconSets();
    }
}

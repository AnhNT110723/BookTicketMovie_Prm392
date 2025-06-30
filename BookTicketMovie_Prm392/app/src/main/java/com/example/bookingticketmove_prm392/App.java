package com.example.bookingticketmove_prm392;

import android.app.Application;
import android.util.Log;

import com.jakewharton.threetenabp.AndroidThreeTen;

public class App extends Application {
    private static final String TAG = "App";

    @Override
    public void onCreate() {
        super.onCreate();
        try {
            AndroidThreeTen.init(this); // Khởi tạo ThreeTenABP
            Log.d(TAG, "ThreeTenABP initialized successfully");
        } catch (Exception e) {
            Log.e(TAG, "Error initializing ThreeTenABP: " + e.getMessage(), e);
        }
    }
}
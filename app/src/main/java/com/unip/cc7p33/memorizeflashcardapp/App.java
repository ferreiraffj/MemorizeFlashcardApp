package com.unip.cc7p33.memorizeflashcardapp;

import android.app.Application;
import android.content.Context;

public class App extends Application {
    private static Context context;

    public void onCreate() {
        super.onCreate();
        App.context = getApplicationContext();
    }

    public static Context getContext() {
        return App.context;
    }
}

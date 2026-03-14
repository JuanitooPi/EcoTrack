package com.example.ecotrack;

import android.app.Application;

public class EcoTrackApplication extends Application {
    @Override
    public void onCreate() {
        super.onCreate();
        SessionManager sessionManager = new SessionManager(this);
        sessionManager.applyTheme();
    }
}
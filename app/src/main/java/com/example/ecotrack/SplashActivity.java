package com.example.ecotrack;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import androidx.appcompat.app.AppCompatActivity;

public class SplashActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);

        // Duración de la pantalla de presentación (3 segundos)
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                // Verificar si hay sesión activa
                SessionManager sessionManager = new SessionManager(SplashActivity.this);

                Intent intent;
                if (sessionManager.isLoggedIn()) {
                    // Si ya hay sesión, ir al dashboard
                    intent = new Intent(SplashActivity.this, MainActivity.class);
                } else {
                    // Si no hay sesión, ir al login
                    intent = new Intent(SplashActivity.this, LoginActivity.class);
                }

                startActivity(intent);
                finish(); // Cerrar SplashActivity
            }
        }, 3000); // 3000 milisegundos = 3 segundos
    }
}
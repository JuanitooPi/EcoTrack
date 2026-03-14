package com.example.ecotrack;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

public class LoginActivity extends AppCompatActivity {

    private EditText etUsuario;
    private EditText etPassword;
    private Button btnLogin;
    private TextView tvMensaje;
    private TextView tvRegistrarse;
    private SessionManager sessionManager;
    private DatabaseHelper dbHelper;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        initViews();
        initDatabase();

        // Verificar si ya hay sesión activa
        if (sessionManager.isLoggedIn()) {
            irAlDashboard();
        }
    }

    private void initViews() {
        etUsuario = findViewById(R.id.etUsuario);
        etPassword = findViewById(R.id.etPassword);
        btnLogin = findViewById(R.id.btnLogin);
        tvMensaje = findViewById(R.id.tvMensaje);
        tvRegistrarse = findViewById(R.id.tvRegistrarse);

        btnLogin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                iniciarSesion();
            }
        });

        tvRegistrarse.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(LoginActivity.this, RegisterActivity.class);
                startActivity(intent);
            }
        });
    }

    private void initDatabase() {
        dbHelper = new DatabaseHelper(this);
        sessionManager = new SessionManager(this);
    }

    private void iniciarSesion() {
        String usuario = etUsuario.getText().toString().trim();
        String password = etPassword.getText().toString().trim();

        // Validar campos vacíos
        if (TextUtils.isEmpty(usuario)) {
            etUsuario.setError(getString(R.string.error_usuario_vacio));
            return;
        }

        if (TextUtils.isEmpty(password)) {
            etPassword.setError(getString(R.string.error_password_vacio));
            return;
        }

        // Mostrar progreso
        btnLogin.setEnabled(false);
        btnLogin.setText(R.string.iniciando_sesion);

        // Simular proceso de autenticación
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                // CONSULTAR EN LA BASE DE DATOS
                Usuario user = dbHelper.obtenerUsuarioPorCredenciales(usuario, password);

                if (user != null) {
                    // Login exitoso
                    sessionManager.crearSesion(user);

                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            Toast.makeText(LoginActivity.this,
                                    getString(R.string.bienvenido, user.getNombre()),
                                    Toast.LENGTH_SHORT).show();
                        }
                    });
                    irAlDashboard();
                } else {
                    // Login fallido
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            tvMensaje.setVisibility(View.VISIBLE);
                            tvMensaje.setText(R.string.error_credenciales);
                            btnLogin.setEnabled(true);
                            btnLogin.setText(R.string.iniciar_sesion);
                        }
                    });
                }
            }
        }, 1500);
    }

    private void irAlDashboard() {
        Intent intent = new Intent(LoginActivity.this, MainActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish();
    }
}
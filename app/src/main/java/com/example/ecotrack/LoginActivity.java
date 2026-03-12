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

    private EditText etUsuario, etPassword;
    private Button btnLogin;
    private TextView tvMensaje;
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

        btnLogin.setOnClickListener(v -> iniciarSesion());
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
            etUsuario.setError("Ingrese usuario");
            return;
        }

        if (TextUtils.isEmpty(password)) {
            etPassword.setError("Ingrese contraseña");
            return;
        }

        // Mostrar progreso
        btnLogin.setEnabled(false);
        btnLogin.setText("Iniciando sesión...");

        // Simular validación (en un caso real sería consulta a BD)
        new Handler().postDelayed(() -> {
            // Para este ejemplo, usamos credenciales fijas
            // En un caso real, usarías dbHelper.obtenerUsuarioPorCredenciales(usuario, password)
            if (usuario.equals("ecolim") && password.equals("123456")) {
                // Crear objeto usuario
                Usuario user = new Usuario();
                user.setId(1);
                user.setNombre("Operario ECOLIM");
                user.setEmail("operario@ecolim.com");
                user.setRol("Recolector");

                // Guardar sesión
                sessionManager.crearSesion(user);

                Toast.makeText(LoginActivity.this, "Bienvenido " + user.getNombre(), Toast.LENGTH_SHORT).show();
                irAlDashboard();
            } else {
                tvMensaje.setVisibility(View.VISIBLE);
                tvMensaje.setText("Usuario o contraseña incorrectos");
                btnLogin.setEnabled(true);
                btnLogin.setText("Iniciar Sesión");
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
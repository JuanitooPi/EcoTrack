package com.example.ecotrack;

import android.os.Bundle;
import android.os.Handler;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

public class RegisterActivity extends AppCompatActivity {

    private EditText etNombre, etEmail, etUsuario, etPassword, etConfirmPassword;
    private Button btnRegistrar;
    private TextView tvIniciarSesion;
    private ProgressBar progressBar;

    private DatabaseHelper dbHelper;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        initViews();
        initDatabase();
        setupListeners();
    }

    private void initViews() {
        etNombre = findViewById(R.id.etNombre);
        etEmail = findViewById(R.id.etEmail);
        etUsuario = findViewById(R.id.etUsuario);
        etPassword = findViewById(R.id.etPassword);
        etConfirmPassword = findViewById(R.id.etConfirmPassword);
        btnRegistrar = findViewById(R.id.btnRegistrar);
        tvIniciarSesion = findViewById(R.id.tvIniciarSesion);
        progressBar = findViewById(R.id.progressBar);
    }

    private void initDatabase() {
        dbHelper = new DatabaseHelper(this);
    }

    private void setupListeners() {
        btnRegistrar.setOnClickListener(v -> registrarUsuario());

        tvIniciarSesion.setOnClickListener(v -> {
            finish(); // Volver al login
        });
    }

    private void registrarUsuario() {
        String nombre = etNombre.getText().toString().trim();
        String email = etEmail.getText().toString().trim();
        String usuario = etUsuario.getText().toString().trim();
        String password = etPassword.getText().toString().trim();
        String confirmPassword = etConfirmPassword.getText().toString().trim();

        // Validaciones
        if (TextUtils.isEmpty(nombre)) {
            etNombre.setError("Ingrese su nombre completo");
            return;
        }

        if (TextUtils.isEmpty(email)) {
            etEmail.setError("Ingrese su correo electrónico");
            return;
        }

        if (!android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            etEmail.setError("Ingrese un correo válido");
            return;
        }

        if (TextUtils.isEmpty(usuario)) {
            etUsuario.setError("Ingrese un nombre de usuario");
            return;
        }

        if (usuario.length() < 4) {
            etUsuario.setError("El usuario debe tener al menos 4 caracteres");
            return;
        }

        if (TextUtils.isEmpty(password)) {
            etPassword.setError("Ingrese una contraseña");
            return;
        }

        if (password.length() < 6) {
            etPassword.setError("La contraseña debe tener al menos 6 caracteres");
            return;
        }

        if (!password.equals(confirmPassword)) {
            etConfirmPassword.setError("Las contraseñas no coinciden");
            return;
        }

        // Verificar si el usuario o email ya existen
        if (dbHelper.usuarioExiste(usuario, email)) {
            Toast.makeText(this, "El usuario o email ya están registrados", Toast.LENGTH_LONG).show();
            return;
        }

        // Mostrar progreso
        progressBar.setVisibility(View.VISIBLE);
        btnRegistrar.setEnabled(false);
        btnRegistrar.setText("Registrando...");

        // Simular proceso de registro (en un caso real sería instantáneo)
        new Handler().postDelayed(() -> {
            // Crear nuevo usuario
            Usuario nuevoUsuario = new Usuario();
            nuevoUsuario.setNombre(nombre);
            nuevoUsuario.setEmail(email);
            nuevoUsuario.setRol("Operario"); // Rol por defecto
            nuevoUsuario.setPassword(password);
            // La foto se puede agregar después

            long id = dbHelper.insertarUsuario(nuevoUsuario);

            progressBar.setVisibility(View.GONE);
            btnRegistrar.setEnabled(true);
            btnRegistrar.setText("Registrarse");

            if (id != -1) {
                Toast.makeText(RegisterActivity.this,
                        "Registro exitoso. Ya puede iniciar sesión",
                        Toast.LENGTH_LONG).show();
                finish(); // Volver al login
            } else {
                Toast.makeText(RegisterActivity.this,
                        "Error al registrar. Intente nuevamente",
                        Toast.LENGTH_SHORT).show();
            }
        }, 1500);
    }
}
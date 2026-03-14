package com.example.ecotrack;

import android.app.AlertDialog;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.fragment.app.Fragment;

public class PerfilFragment extends Fragment {

    private TextView tvNombre, tvRol, tvEmail, tvTotalRegistros, tvTotalKg;
    private Button btnEditarPerfil, btnCambiarPassword, btnCerrarSesion;
    private TextView btnAyuda, btnPrivacidad; 
    private ImageView imagenPerfil;
    private RadioGroup rgTema;
    private RadioButton rbSistema, rbClaro, rbOscuro;
    private SessionManager sessionManager;
    private DatabaseHelper dbHelper;

    private final ActivityResultLauncher<String> galleryLauncher = registerForActivityResult(
            new ActivityResultContracts.GetContent(),
            uri -> {
                if (uri != null) {
                    actualizarFoto(uri);
                }
            }
    );

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        try {
            View view = inflater.inflate(R.layout.fragment_perfil, container, false);
            
            // 1. Inicializar base de datos y sesión PRIMERO
            initDatabase();
            
            // 2. Inicializar vistas
            initViews(view);
            
            // 3. Configurar datos y eventos
            cargarDatosPerfil();
            setupThemeSelection();
            setupListeners();
            
            return view;
        } catch (Exception e) {
            android.util.Log.e("PerfilFragment", "Error al inflar la vista de perfil", e);
            Toast.makeText(getContext(), "Error al cargar el perfil", Toast.LENGTH_LONG).show();
            return new View(getContext()); // Retorna vista vacía para evitar crash
        }
    }

    private void initViews(View view) {
        imagenPerfil = view.findViewById(R.id.imagenPerfil);
        tvNombre = view.findViewById(R.id.tvNombre);
        tvRol = view.findViewById(R.id.tvRol);
        tvEmail = view.findViewById(R.id.tvEmail);
        tvTotalRegistros = view.findViewById(R.id.tvTotalRegistros);
        tvTotalKg = view.findViewById(R.id.tvTotalKg);
        btnEditarPerfil = view.findViewById(R.id.btnEditarPerfil);
        btnCambiarPassword = view.findViewById(R.id.btnCambiarPassword);
        btnCerrarSesion = view.findViewById(R.id.btnCerrarSesion);
        btnAyuda = view.findViewById(R.id.btnAyuda);
        btnPrivacidad = view.findViewById(R.id.btnPrivacidad);
        rgTema = view.findViewById(R.id.rgTema);
        rbSistema = view.findViewById(R.id.rbSistema);
        rbClaro = view.findViewById(R.id.rbClaro);
        rbOscuro = view.findViewById(R.id.rbOscuro);
    }

    private void initDatabase() {
        if (getContext() != null) {
            sessionManager = new SessionManager(requireContext());
            dbHelper = new DatabaseHelper(requireContext());
        }
    }

    private void setupThemeSelection() {
        if (rgTema == null || sessionManager == null) return;

        int currentMode = sessionManager.getThemeMode();
        if (currentMode == AppCompatDelegate.MODE_NIGHT_YES) {
            rbOscuro.setChecked(true);
        } else if (currentMode == AppCompatDelegate.MODE_NIGHT_NO) {
            rbClaro.setChecked(true);
        } else {
            rbSistema.setChecked(true);
        }

        rgTema.setOnCheckedChangeListener((group, checkedId) -> {
            int mode;
            if (checkedId == R.id.rbClaro) {
                mode = AppCompatDelegate.MODE_NIGHT_NO;
            } else if (checkedId == R.id.rbOscuro) {
                mode = AppCompatDelegate.MODE_NIGHT_YES;
            } else {
                mode = AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM;
            }
            sessionManager.setThemeMode(mode);
            AppCompatDelegate.setDefaultNightMode(mode);
        });
    }

    private void setupListeners() {
        if (imagenPerfil != null) imagenPerfil.setOnClickListener(v -> mostrarOpcionesFoto());
        if (btnEditarPerfil != null) btnEditarPerfil.setOnClickListener(v -> mostrarDialogoEditarPerfil());
        if (btnCambiarPassword != null) btnCambiarPassword.setOnClickListener(v -> mostrarDialogoCambiarPassword());
        if (btnCerrarSesion != null) btnCerrarSesion.setOnClickListener(v -> cerrarSesion());

        if (btnAyuda != null) {
            btnAyuda.setOnClickListener(v -> 
                Toast.makeText(getContext(), "Redirigiendo al Centro de Ayuda...", Toast.LENGTH_SHORT).show()
            );
        }

        if (btnPrivacidad != null) {
            btnPrivacidad.setOnClickListener(v -> 
                Toast.makeText(getContext(), "Cargando Política de Privacidad...", Toast.LENGTH_SHORT).show()
            );
        }
    }

    private void cargarDatosPerfil() {
        if (sessionManager == null) return;
        Usuario usuario = sessionManager.getUsuario();
        if (usuario == null) return;

        if (tvNombre != null) tvNombre.setText(usuario.getNombre());
        if (tvRol != null) tvRol.setText(usuario.getRol());
        if (tvEmail != null) tvEmail.setText(usuario.getEmail());

        if (imagenPerfil != null) {
            if (usuario.getFoto() != null && !usuario.getFoto().isEmpty()) {
                try {
                    imagenPerfil.setImageURI(Uri.parse(usuario.getFoto()));
                } catch (Exception e) {
                    imagenPerfil.setImageResource(R.drawable.perfil);
                }
            } else {
                imagenPerfil.setImageResource(R.drawable.perfil);
            }
        }

        if (dbHelper != null) {
            int total = dbHelper.contarResiduosPorUsuario(usuario.getId());
            if (tvTotalRegistros != null) tvTotalRegistros.setText(String.valueOf(total));
        }
        if (tvTotalKg != null) tvTotalKg.setText("0.0 kg"); 
    }

    private void mostrarOpcionesFoto() {
        if (getContext() == null) return;
        String[] opciones = {"Cambiar foto", "Eliminar foto", "Cancelar"};
        AlertDialog.Builder builder = new AlertDialog.Builder(requireContext());
        builder.setTitle("Foto de perfil");
        builder.setItems(opciones, (dialog, which) -> {
            if (which == 0) {
                galleryLauncher.launch("image/*");
            } else if (which == 1) {
                eliminarFoto();
            }
        });
        builder.show();
    }

    private void actualizarFoto(Uri uri) {
        if (getContext() == null || sessionManager == null || dbHelper == null) return;
        try {
            requireContext().getContentResolver().takePersistableUriPermission(uri, Intent.FLAG_GRANT_READ_URI_PERMISSION);
        } catch (Exception e) {
            android.util.Log.e("PerfilFragment", "Error permisos URI", e);
        }

        String fotoStr = uri.toString();
        int usuarioId = sessionManager.getUsuarioId();

        if (dbHelper.actualizarFotoUsuario(usuarioId, fotoStr)) {
            sessionManager.actualizarFoto(fotoStr);
            if (imagenPerfil != null) imagenPerfil.setImageURI(uri);
            Toast.makeText(getContext(), "Foto actualizada", Toast.LENGTH_SHORT).show();
        }
    }

    private void eliminarFoto() {
        if (sessionManager == null || dbHelper == null) return;
        int usuarioId = sessionManager.getUsuarioId();
        if (dbHelper.actualizarFotoUsuario(usuarioId, null)) {
            sessionManager.actualizarFoto(null);
            if (imagenPerfil != null) imagenPerfil.setImageResource(R.drawable.perfil);
            Toast.makeText(getContext(), "Foto eliminada", Toast.LENGTH_SHORT).show();
        }
    }

    private void mostrarDialogoEditarPerfil() {
        if (getContext() == null || sessionManager == null) return;
        AlertDialog.Builder builder = new AlertDialog.Builder(requireContext());
        View dialogView = getLayoutInflater().inflate(R.layout.dialog_editar_perfil, null);
        builder.setView(dialogView);

        EditText etNombre = dialogView.findViewById(R.id.etNombre);
        EditText etEmail = dialogView.findViewById(R.id.etEmail);
        EditText etRol = dialogView.findViewById(R.id.etRol);
        Button btnCancelar = dialogView.findViewById(R.id.btnCancelar);
        Button btnGuardar = dialogView.findViewById(R.id.btnGuardar);

        Usuario usuario = sessionManager.getUsuario();
        if (usuario != null) {
            etNombre.setText(usuario.getNombre());
            etEmail.setText(usuario.getEmail());
            etRol.setText(usuario.getRol());
        }

        AlertDialog dialog = builder.create();
        btnCancelar.setOnClickListener(v -> dialog.dismiss());
        btnGuardar.setOnClickListener(v -> {
            String nuevoNombre = etNombre.getText().toString().trim();
            String nuevoEmail = etEmail.getText().toString().trim();
            String nuevoRol = etRol.getText().toString().trim();

            if (!nuevoNombre.isEmpty()) {
                sessionManager.actualizarPerfil(nuevoNombre, nuevoEmail, nuevoRol);
                if (tvNombre != null) tvNombre.setText(nuevoNombre);
                if (tvEmail != null) tvEmail.setText(nuevoEmail);
                if (tvRol != null) tvRol.setText(nuevoRol);
                Toast.makeText(getContext(), "Perfil actualizado", Toast.LENGTH_SHORT).show();
                dialog.dismiss();
            }
        });
        dialog.show();
    }

    private void mostrarDialogoCambiarPassword() {
        if (getContext() == null || sessionManager == null || dbHelper == null) return;
        AlertDialog.Builder builder = new AlertDialog.Builder(requireContext());
        View dialogView = getLayoutInflater().inflate(R.layout.dialog_cambiar_password, null);
        builder.setView(dialogView);

        EditText etPasswordActual = dialogView.findViewById(R.id.etPasswordActual);
        EditText etPasswordNueva = dialogView.findViewById(R.id.etPasswordNueva);
        EditText etConfirmarPassword = dialogView.findViewById(R.id.etConfirmarPassword);
        Button btnCancelar = dialogView.findViewById(R.id.btnCancelar);
        Button btnGuardar = dialogView.findViewById(R.id.btnGuardar);

        AlertDialog dialog = builder.create();
        btnCancelar.setOnClickListener(v -> dialog.dismiss());
        btnGuardar.setOnClickListener(v -> {
            String actual = etPasswordActual.getText().toString();
            String nueva = etPasswordNueva.getText().toString();
            String confirmar = etConfirmarPassword.getText().toString();

            if (!nueva.equals(confirmar)) {
                etConfirmarPassword.setError("Las contraseñas no coinciden");
                return;
            }

            if (dbHelper.cambiarPassword(sessionManager.getUsuarioId(), actual, nueva)) {
                Toast.makeText(getContext(), "Contraseña actualizada", Toast.LENGTH_SHORT).show();
                dialog.dismiss();
            } else {
                etPasswordActual.setError("Contraseña incorrecta");
            }
        });
        dialog.show();
    }

    private void cerrarSesion() {
        if (sessionManager != null) {
            sessionManager.cerrarSesion();
            requireActivity().finish();
            startActivity(new Intent(getContext(), LoginActivity.class));
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        cargarDatosPerfil();
    }
}

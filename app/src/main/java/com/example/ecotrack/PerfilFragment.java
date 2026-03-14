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
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_perfil, container, false);

        initViews(view);
        initDatabase();
        cargarDatosPerfil();
        setupThemeSelection();
        setupListeners();

        return view;
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
        sessionManager = new SessionManager(requireContext());
        dbHelper = new DatabaseHelper(requireContext());
    }

    private void setupThemeSelection() {
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
        imagenPerfil.setOnClickListener(v -> mostrarOpcionesFoto());
        btnEditarPerfil.setOnClickListener(v -> mostrarDialogoEditarPerfil());
        btnCambiarPassword.setOnClickListener(v -> mostrarDialogoCambiarPassword());
        btnCerrarSesion.setOnClickListener(v -> cerrarSesion());

        btnAyuda.setOnClickListener(v -> 
            Toast.makeText(requireContext(), "Redirigiendo al Centro de Ayuda...", Toast.LENGTH_SHORT).show()
        );

        btnPrivacidad.setOnClickListener(v -> 
            Toast.makeText(requireContext(), "Cargando Política de Privacidad...", Toast.LENGTH_SHORT).show()
        );
    }

    private void cargarDatosPerfil() {
        Usuario usuario = sessionManager.getUsuario();

        tvNombre.setText(usuario.getNombre());
        tvRol.setText(usuario.getRol());
        tvEmail.setText(usuario.getEmail());

        if (usuario.getFoto() != null && !usuario.getFoto().isEmpty()) {
            try {
                imagenPerfil.setImageURI(Uri.parse(usuario.getFoto()));
            } catch (Exception e) {
                imagenPerfil.setImageResource(R.drawable.perfil);
            }
        } else {
            imagenPerfil.setImageResource(R.drawable.perfil);
        }

        int total = dbHelper.contarResiduosPorUsuario(usuario.getId());
        tvTotalRegistros.setText(String.valueOf(total));
        
        // Simulación de carga de Kg totales (esto debería venir de ResiduoDAO)
        tvTotalKg.setText(String.format("%.1f", 12.5)); 
    }

    private void mostrarOpcionesFoto() {
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
        try {
            requireContext().getContentResolver().takePersistableUriPermission(uri, Intent.FLAG_GRANT_READ_URI_PERMISSION);
        } catch (Exception e) {
            e.printStackTrace();
        }

        String fotoStr = uri.toString();
        int usuarioId = sessionManager.getUsuarioId();

        if (dbHelper.actualizarFotoUsuario(usuarioId, fotoStr)) {
            sessionManager.actualizarFoto(fotoStr);
            imagenPerfil.setImageURI(uri);
            Toast.makeText(requireContext(), "Foto actualizada", Toast.LENGTH_SHORT).show();
        }
    }

    private void eliminarFoto() {
        int usuarioId = sessionManager.getUsuarioId();
        if (dbHelper.actualizarFotoUsuario(usuarioId, null)) {
            sessionManager.actualizarFoto(null);
            imagenPerfil.setImageResource(R.drawable.perfil);
            Toast.makeText(requireContext(), "Foto eliminada", Toast.LENGTH_SHORT).show();
        }
    }

    private void mostrarDialogoEditarPerfil() {
        AlertDialog.Builder builder = new AlertDialog.Builder(requireContext());
        View dialogView = getLayoutInflater().inflate(R.layout.dialog_editar_perfil, null);
        builder.setView(dialogView);

        EditText etNombre = dialogView.findViewById(R.id.etNombre);
        EditText etEmail = dialogView.findViewById(R.id.etEmail);
        EditText etRol = dialogView.findViewById(R.id.etRol);
        Button btnCancelar = dialogView.findViewById(R.id.btnCancelar);
        Button btnGuardar = dialogView.findViewById(R.id.btnGuardar);

        Usuario usuario = sessionManager.getUsuario();
        etNombre.setText(usuario.getNombre());
        etEmail.setText(usuario.getEmail());
        etRol.setText(usuario.getRol());

        AlertDialog dialog = builder.create();
        btnCancelar.setOnClickListener(v -> dialog.dismiss());
        btnGuardar.setOnClickListener(v -> {
            String nuevoNombre = etNombre.getText().toString().trim();
            String nuevoEmail = etEmail.getText().toString().trim();
            String nuevoRol = etRol.getText().toString().trim();

            if (!nuevoNombre.isEmpty()) {
                sessionManager.actualizarPerfil(nuevoNombre, nuevoEmail, nuevoRol);
                tvNombre.setText(nuevoNombre);
                tvEmail.setText(nuevoEmail);
                tvRol.setText(nuevoRol);
                Toast.makeText(requireContext(), "Perfil actualizado", Toast.LENGTH_SHORT).show();
                dialog.dismiss();
            }
        });
        dialog.show();
    }

    private void mostrarDialogoCambiarPassword() {
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
                Toast.makeText(requireContext(), "Contraseña actualizada", Toast.LENGTH_SHORT).show();
                dialog.dismiss();
            } else {
                etPasswordActual.setError("Contraseña incorrecta");
            }
        });
        dialog.show();
    }

    private void cerrarSesion() {
        sessionManager.cerrarSesion();
        requireActivity().finish();
        startActivity(new Intent(requireContext(), LoginActivity.class));
    }

    @Override
    public void onResume() {
        super.onResume();
        cargarDatosPerfil();
    }
}

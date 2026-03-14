package com.example.ecotrack;

import android.app.AlertDialog;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatDelegate;
import androidx.fragment.app.Fragment;

public class PerfilFragment extends Fragment {

    private TextView tvNombre, tvRol, tvEmail, tvTotalRegistros, tvTotalKg;
    private Button btnEditarPerfil, btnCambiarPassword, btnCerrarSesion;
    private RadioGroup rgTema;
    private RadioButton rbSistema, rbClaro, rbOscuro;
    private SessionManager sessionManager;
    private DatabaseHelper dbHelper;
    private ResiduoDAO residuoDAO;

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
        tvNombre = view.findViewById(R.id.tvNombre);
        tvRol = view.findViewById(R.id.tvRol);
        tvEmail = view.findViewById(R.id.tvEmail);
        tvTotalRegistros = view.findViewById(R.id.tvTotalRegistros);
        tvTotalKg = view.findViewById(R.id.tvTotalKg);
        btnEditarPerfil = view.findViewById(R.id.btnEditarPerfil);
        btnCambiarPassword = view.findViewById(R.id.btnCambiarPassword);
        btnCerrarSesion = view.findViewById(R.id.btnCerrarSesion);
        rgTema = view.findViewById(R.id.rgTema);
        rbSistema = view.findViewById(R.id.rbSistema);
        rbClaro = view.findViewById(R.id.rbClaro);
        rbOscuro = view.findViewById(R.id.rbOscuro);
    }

    private void initDatabase() {
        sessionManager = new SessionManager(requireContext());
        dbHelper = new DatabaseHelper(requireContext());
        residuoDAO = new ResiduoDAO(dbHelper);
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
        btnEditarPerfil.setOnClickListener(v -> mostrarDialogoEditarPerfil());
        btnCambiarPassword.setOnClickListener(v -> mostrarDialogoCambiarPassword());
        btnCerrarSesion.setOnClickListener(v -> cerrarSesion());
    }

    private void cargarDatosPerfil() {
        Usuario usuario = sessionManager.getUsuario();

        tvNombre.setText(usuario.getNombre());
        tvRol.setText(usuario.getRol());
        tvEmail.setText(usuario.getEmail());

        // Contar registros totales del usuario
        int total = dbHelper.contarResiduosPorUsuario(usuario.getId());
        tvTotalRegistros.setText(String.valueOf(total));

        // Calcular total de kg (simplificado)
        double totalKg = 0;
        tvTotalKg.setText(String.format("%.1f kg", totalKg));
    }

    private void mostrarDialogoEditarPerfil() {
        AlertDialog.Builder builder = new AlertDialog.Builder(requireContext());
        LayoutInflater inflater = getLayoutInflater();
        View dialogView = inflater.inflate(R.layout.dialog_editar_perfil, null);
        builder.setView(dialogView);

        EditText etNombre = dialogView.findViewById(R.id.etNombre);
        EditText etEmail = dialogView.findViewById(R.id.etEmail);
        EditText etRol = dialogView.findViewById(R.id.etRol);
        Button btnCancelar = dialogView.findViewById(R.id.btnCancelar);
        Button btnGuardar = dialogView.findViewById(R.id.btnGuardar);

        // Cargar datos actuales
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

            if (nuevoNombre.isEmpty()) {
                etNombre.setError("Ingrese nombre");
                return;
            }

            // Actualizar en SharedPreferences
            sessionManager.actualizarPerfil(nuevoNombre, nuevoEmail, nuevoRol);

            // Actualizar vistas
            tvNombre.setText(nuevoNombre);
            tvEmail.setText(nuevoEmail);
            tvRol.setText(nuevoRol);

            Toast.makeText(requireContext(), "Perfil actualizado", Toast.LENGTH_SHORT).show();
            dialog.dismiss();
        });

        dialog.show();
    }

    private void mostrarDialogoCambiarPassword() {
        AlertDialog.Builder builder = new AlertDialog.Builder(requireContext());
        LayoutInflater inflater = getLayoutInflater();
        View dialogView = inflater.inflate(R.layout.dialog_cambiar_password, null);
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

            if (actual.isEmpty()) {
                etPasswordActual.setError("Ingrese contraseña actual");
                return;
            }

            if (nueva.isEmpty()) {
                etPasswordNueva.setError("Ingrese nueva contraseña");
                return;
            }

            if (!nueva.equals(confirmar)) {
                etConfirmarPassword.setError("Las contraseñas no coinciden");
                return;
            }

            // Actualizar en BD
            Usuario usuario = sessionManager.getUsuario();
            boolean exito = dbHelper.cambiarPassword(usuario.getId(), actual, nueva);

            if (exito) {
                Toast.makeText(requireContext(), "Contraseña actualizada correctamente", Toast.LENGTH_SHORT).show();
                dialog.dismiss();
            } else {
                etPasswordActual.setError("La contraseña actual es incorrecta");
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
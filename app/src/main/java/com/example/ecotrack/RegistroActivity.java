package com.example.ecotrack;

import android.app.DatePickerDialog;
import android.app.TimePickerDialog;
import android.os.Bundle;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.textfield.TextInputLayout;

import java.util.Calendar;

public class RegistroActivity extends AppCompatActivity {

    private DatabaseHelper dbHelper;
    private ResiduoDAO residuoDAO;
    private SessionManager sessionManager;

    private AutoCompleteTextView actTipo, actUnidad;
    private EditText etCantidad, etFecha, etHora, etUbicacion, etOtroTipo;
    private TextInputLayout tilOtroTipo;
    private Button btnGuardar, btnCancelar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        sessionManager = new SessionManager(this);
        sessionManager.applyTheme();
        
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_registro);

        initViews();
        initDatabase();
        setupSpinners();
        setupDateTimePickers();
        setupButtons();

        // Cargar fecha y hora actual
        etFecha.setText(DateUtils.formatearFechaMostrar(DateUtils.getFechaActual()));
        etHora.setText(DateUtils.getHoraActual());
    }

    private void initViews() {
        actTipo = findViewById(R.id.actTipo);
        actUnidad = findViewById(R.id.actUnidad);
        etCantidad = findViewById(R.id.etCantidad);
        etFecha = findViewById(R.id.etFecha);
        etHora = findViewById(R.id.etHora);
        etUbicacion = findViewById(R.id.etUbicacion);
        etOtroTipo = findViewById(R.id.etOtroTipo);
        tilOtroTipo = findViewById(R.id.tilOtroTipo);
        btnGuardar = findViewById(R.id.btnGuardar);
        btnCancelar = findViewById(R.id.btnCancelar);
    }

    private void initDatabase() {
        dbHelper = new DatabaseHelper(this);
        residuoDAO = new ResiduoDAO(dbHelper);
    }

    private void setupSpinners() {
        ArrayAdapter<String> tipoAdapter = new ArrayAdapter<>(
                this,
                android.R.layout.simple_dropdown_item_1line,
                Residuo.TIPOS_RESIDUOS
        );
        actTipo.setAdapter(tipoAdapter);

        // Listener para detectar si elige "Otro..."
        actTipo.setOnItemClickListener((parent, view, position, id) -> {
            String seleccion = (String) parent.getItemAtPosition(position);
            if ("Otro...".equals(seleccion)) {
                tilOtroTipo.setVisibility(View.VISIBLE);
                etOtroTipo.requestFocus();
            } else {
                tilOtroTipo.setVisibility(View.GONE);
            }
        });

        actTipo.setOnClickListener(v -> actTipo.showDropDown());

        ArrayAdapter<String> unidadAdapter = new ArrayAdapter<>(
                this,
                android.R.layout.simple_dropdown_item_1line,
                Residuo.UNIDADES
        );
        actUnidad.setAdapter(unidadAdapter);
        actUnidad.setOnClickListener(v -> actUnidad.showDropDown());
    }

    private void setupDateTimePickers() {
        etFecha.setOnClickListener(v -> mostrarDatePicker());
        etHora.setOnClickListener(v -> mostrarTimePicker());
    }

    private void mostrarDatePicker() {
        Calendar cal = Calendar.getInstance();
        DatePickerDialog datePickerDialog = new DatePickerDialog(this,
                (view, year, month, dayOfMonth) -> {
                    String fecha = String.format("%d-%02d-%02d", year, month + 1, dayOfMonth);
                    etFecha.setText(DateUtils.formatearFechaMostrar(fecha));
                },
                cal.get(Calendar.YEAR),
                cal.get(Calendar.MONTH),
                cal.get(Calendar.DAY_OF_MONTH));
        datePickerDialog.show();
    }

    private void mostrarTimePicker() {
        Calendar cal = Calendar.getInstance();
        TimePickerDialog timePickerDialog = new TimePickerDialog(this,
                (view, hourOfDay, minute) -> {
                    String hora = String.format("%02d:%02d:00", hourOfDay, minute);
                    etHora.setText(hora);
                },
                cal.get(Calendar.HOUR_OF_DAY),
                cal.get(Calendar.MINUTE),
                true);
        timePickerDialog.show();
    }

    private void setupButtons() {
        btnGuardar.setOnClickListener(v -> guardarRegistro());
        btnCancelar.setOnClickListener(v -> finish());
    }

    private void guardarRegistro() {
        String tipoFinal = actTipo.getText().toString();
        String unidad = actUnidad.getText().toString();
        String cantidadStr = etCantidad.getText().toString();
        String fecha = etFecha.getText().toString();
        String hora = etHora.getText().toString();
        String ubicacion = etUbicacion.getText().toString();

        // Si eligió "Otro...", el tipo real es lo que escribió en etOtroTipo
        if ("Otro...".equals(tipoFinal)) {
            tipoFinal = etOtroTipo.getText().toString().trim();
            if (tipoFinal.isEmpty()) {
                etOtroTipo.setError("Especifique el tipo de residuo");
                return;
            }
        }

        if (tipoFinal.isEmpty() || "Seleccione tipo".equals(tipoFinal)) {
            Toast.makeText(this, "Seleccione un tipo de residuo", Toast.LENGTH_SHORT).show();
            return;
        }

        if (cantidadStr.isEmpty()) {
            etCantidad.setError("Ingrese cantidad");
            return;
        }

        double cantidad = Double.parseDouble(cantidadStr);
        String fechaBD = DateUtils.formatearFechaBD(fecha);

        Residuo residuo = new Residuo(tipoFinal, cantidad, unidad, fechaBD, hora, ubicacion);
        
        // Usar el ID del usuario actual de la sesión
        int usuarioId = sessionManager.getUsuarioId();
        long id = dbHelper.insertarResiduo(residuo, usuarioId);

        if (id != -1) {
            Toast.makeText(this, "Registro guardado exitosamente", Toast.LENGTH_SHORT).show();
            finish();
        } else {
            Toast.makeText(this, "Error al guardar el registro", Toast.LENGTH_SHORT).show();
        }
    }
}

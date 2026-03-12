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
import java.util.Calendar;

public class RegistroActivity extends AppCompatActivity {

    private DatabaseHelper dbHelper;
    private ResiduoDAO residuoDAO;

    private AutoCompleteTextView actTipo, actUnidad;
    private EditText etCantidad, etFecha, etHora, etUbicacion;
    private Button btnGuardar, btnCancelar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_registro);

        initViews();
        initDatabase();
        setupSpinners();  // IMPORTANTE: Configurar los spinners
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
        btnGuardar = findViewById(R.id.btnGuardar);
        btnCancelar = findViewById(R.id.btnCancelar);
    }

    private void initDatabase() {
        dbHelper = new DatabaseHelper(this);
        residuoDAO = new ResiduoDAO(dbHelper);
    }

    private void setupSpinners() {
        // Configurar spinner de tipos de residuos
        ArrayAdapter<String> tipoAdapter = new ArrayAdapter<>(
                this,
                android.R.layout.simple_dropdown_item_1line,
                Residuo.TIPOS_RESIDUOS
        );
        actTipo.setAdapter(tipoAdapter);

        // Hacer que el dropdown se muestre inmediatamente al hacer clic
        actTipo.setOnClickListener(v -> {
            actTipo.showDropDown();
        });

        // También al enfocar
        actTipo.setOnFocusChangeListener((v, hasFocus) -> {
            if (hasFocus) {
                actTipo.showDropDown();
            }
        });

        // Configurar spinner de unidades de medida
        ArrayAdapter<String> unidadAdapter = new ArrayAdapter<>(
                this,
                android.R.layout.simple_dropdown_item_1line,
                Residuo.UNIDADES
        );
        actUnidad.setAdapter(unidadAdapter);

        actUnidad.setOnClickListener(v -> {
            actUnidad.showDropDown();
        });

        actUnidad.setOnFocusChangeListener((v, hasFocus) -> {
            if (hasFocus) {
                actUnidad.showDropDown();
            }
        });
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
        // Validar campos
        String tipo = actTipo.getText().toString();
        String unidad = actUnidad.getText().toString();
        String cantidadStr = etCantidad.getText().toString();
        String fecha = etFecha.getText().toString();
        String hora = etHora.getText().toString();
        String ubicacion = etUbicacion.getText().toString();

        if (tipo.isEmpty()) {
            actTipo.setError("Seleccione un tipo");
            actTipo.requestFocus();
            return;
        }

        if (unidad.isEmpty()) {
            actUnidad.setError("Seleccione una unidad");
            actUnidad.requestFocus();
            return;
        }

        if (cantidadStr.isEmpty()) {
            etCantidad.setError("Ingrese cantidad");
            etCantidad.requestFocus();
            return;
        }

        double cantidad;
        try {
            cantidad = Double.parseDouble(cantidadStr);
            if (cantidad <= 0) {
                etCantidad.setError("La cantidad debe ser mayor a 0");
                etCantidad.requestFocus();
                return;
            }
        } catch (NumberFormatException e) {
            etCantidad.setError("Cantidad inválida");
            etCantidad.requestFocus();
            return;
        }

        // Convertir fecha a formato BD
        String fechaBD = DateUtils.formatearFechaBD(fecha);

        // Crear y guardar residuo
        Residuo residuo = new Residuo(tipo, cantidad, unidad, fechaBD, hora, ubicacion);
        long id = residuoDAO.insertar(residuo);

        if (id != -1) {
            Toast.makeText(this, "Registro guardado exitosamente", Toast.LENGTH_SHORT).show();
            finish(); // Volver a la actividad anterior
        } else {
            Toast.makeText(this, "Error al guardar el registro", Toast.LENGTH_SHORT).show();
        }
    }
}
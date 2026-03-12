package com.example.ecotrack;

import android.app.DatePickerDialog;
import android.os.Bundle;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import java.util.Calendar;
import java.util.List;

public class HistorialActivity extends AppCompatActivity {

    private DatabaseHelper dbHelper;
    private ResiduoDAO residuoDAO;
    private RecyclerView rvHistorial;
    private ResiduoAdapter adapter;
    private EditText etFechaInicio, etFechaFin;
    private AutoCompleteTextView actFiltroTipo;
    private Button btnFiltrar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_historial);

        initViews();
        initDatabase();
        setupSpinners();
        setupDatePickers();
        setupRecyclerView();
        cargarHistorial();

        btnFiltrar.setOnClickListener(v -> aplicarFiltros());
    }

    private void initViews() {
        rvHistorial = findViewById(R.id.rvHistorial);
        etFechaInicio = findViewById(R.id.etFechaInicio);
        etFechaFin = findViewById(R.id.etFechaFin);
        actFiltroTipo = findViewById(R.id.actFiltroTipo);
        btnFiltrar = findViewById(R.id.btnFiltrar);
    }

    private void initDatabase() {
        dbHelper = new DatabaseHelper(this);
        residuoDAO = new ResiduoDAO(dbHelper);
    }

    private void setupSpinners() {
        String[] tipos = new String[Residuo.TIPOS_RESIDUOS.length + 1];
        tipos[0] = "Todos";
        System.arraycopy(Residuo.TIPOS_RESIDUOS, 0, tipos, 1, Residuo.TIPOS_RESIDUOS.length);

        ArrayAdapter<String> adapter = new ArrayAdapter<>(this,
                android.R.layout.simple_dropdown_item_1line, tipos);
        actFiltroTipo.setAdapter(adapter);

        // Mejorar comportamiento del dropdown
        actFiltroTipo.setOnClickListener(v -> actFiltroTipo.showDropDown());
        actFiltroTipo.setOnFocusChangeListener((v, hasFocus) -> {
            if (hasFocus) {
                actFiltroTipo.showDropDown();
            }
        });
    }
    private void setupDatePickers() {
        etFechaInicio.setOnClickListener(v -> mostrarDatePicker(etFechaInicio));
        etFechaFin.setOnClickListener(v -> mostrarDatePicker(etFechaFin));

        // Fechas por defecto: últimos 7 días
        Calendar cal = Calendar.getInstance();
        String fechaFin = DateUtils.getFechaActual();

        cal.add(Calendar.DAY_OF_YEAR, -7);
        String fechaInicio = new java.text.SimpleDateFormat("yyyy-MM-dd")
                .format(cal.getTime());

        etFechaInicio.setText(DateUtils.formatearFechaMostrar(fechaInicio));
        etFechaFin.setText(DateUtils.formatearFechaMostrar(fechaFin));
    }

    private void mostrarDatePicker(EditText editText) {
        Calendar cal = Calendar.getInstance();
        DatePickerDialog datePickerDialog = new DatePickerDialog(this,
                (view, year, month, dayOfMonth) -> {
                    String fecha = String.format("%d-%02d-%02d", year, month + 1, dayOfMonth);
                    editText.setText(DateUtils.formatearFechaMostrar(fecha));
                },
                cal.get(Calendar.YEAR),
                cal.get(Calendar.MONTH),
                cal.get(Calendar.DAY_OF_MONTH));
        datePickerDialog.show();
    }

    private void setupRecyclerView() {
        rvHistorial.setLayoutManager(new LinearLayoutManager(this));
    }

    private void cargarHistorial() {
        List<Residuo> residuos = residuoDAO.listarTodos();
        actualizarAdapter(residuos);
    }

    private void aplicarFiltros() {
        String fechaInicio = DateUtils.formatearFechaBD(etFechaInicio.getText().toString());
        String fechaFin = DateUtils.formatearFechaBD(etFechaFin.getText().toString());
        String tipo = actFiltroTipo.getText().toString();

        List<Residuo> residuosFiltrados = residuoDAO.listarConFiltros(fechaInicio, fechaFin, tipo);
        actualizarAdapter(residuosFiltrados);

        Toast.makeText(this, "Mostrando " + residuosFiltrados.size() + " registros",
                Toast.LENGTH_SHORT).show();
    }

    private void actualizarAdapter(List<Residuo> residuos) {
        if (adapter == null) {
            adapter = new ResiduoAdapter(residuos,
                    residuo -> {
                        // Click para ver detalles
                        Toast.makeText(this, residuo.getTipo() + ": " +
                                        residuo.getCantidad() + " " + residuo.getUnidad(),
                                Toast.LENGTH_SHORT).show();
                    },
                    residuo -> {
                        // Long click para eliminar
                        residuoDAO.eliminar(residuo.getId());
                        aplicarFiltros(); // Recargar con filtros actuales
                        Toast.makeText(this, "Registro eliminado", Toast.LENGTH_SHORT).show();
                        return true;
                    });
            rvHistorial.setAdapter(adapter);
        } else {
            adapter.actualizarLista(residuos);
        }
    }
}
package com.example.ecotrack;

import android.app.DatePickerDialog;
import android.os.Bundle;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.card.MaterialCardView;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;

public class ReportesActivity extends AppCompatActivity {

    private EditText etFechaInicio, etFechaFin;
    private AutoCompleteTextView actFiltroTipo;
    private Button btnGenerarPDF, btnGenerarCSV, btnCompartir, btnAbrir;
    private MaterialCardView cardResultados;
    private TextView tvResultado;
    private ProgressBar progressBar;

    private DatabaseHelper dbHelper;
    private SessionManager sessionManager;
    private List<Residuo> residuosFiltrados;
    private File archivoGenerado;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        try {
            setContentView(R.layout.activity_reportes);
            initViews();
            initDatabase();
            setupSpinners();
            setupDatePickers();
            setupButtons();
            setFechasDefault();
        } catch (Exception e) {
            e.printStackTrace();
            Toast.makeText(this, "Error al cargar: " + e.getMessage(), Toast.LENGTH_LONG).show();
        }
    }

    private void initViews() {
        try {
            etFechaInicio = findViewById(R.id.etFechaInicio);
            etFechaFin = findViewById(R.id.etFechaFin);
            actFiltroTipo = findViewById(R.id.actFiltroTipo);
            btnGenerarPDF = findViewById(R.id.btnGenerarPDF);
            btnGenerarCSV = findViewById(R.id.btnGenerarCSV);
            btnCompartir = findViewById(R.id.btnCompartir);
            btnAbrir = findViewById(R.id.btnAbrir);
            cardResultados = findViewById(R.id.cardResultados);
            tvResultado = findViewById(R.id.tvResultado);
            progressBar = findViewById(R.id.progressBar);

            // Verificar que las vistas no sean nulas
            if (etFechaInicio == null) throw new NullPointerException("etFechaInicio no encontrado");
            if (etFechaFin == null) throw new NullPointerException("etFechaFin no encontrado");
            if (actFiltroTipo == null) throw new NullPointerException("actFiltroTipo no encontrado");
            if (btnGenerarPDF == null) throw new NullPointerException("btnGenerarPDF no encontrado");
            if (btnGenerarCSV == null) throw new NullPointerException("btnGenerarCSV no encontrado");

            // Ocultar card de resultados inicialmente
            cardResultados.setVisibility(View.GONE);
            progressBar.setVisibility(View.GONE);

        } catch (Exception e) {
            Toast.makeText(this, "Error en initViews: " + e.getMessage(), Toast.LENGTH_LONG).show();
            throw e;
        }
    }

    private void initDatabase() {
        try {
            dbHelper = new DatabaseHelper(this);
            sessionManager = new SessionManager(this);
        } catch (Exception e) {
            Toast.makeText(this, "Error en base de datos: " + e.getMessage(), Toast.LENGTH_LONG).show();
        }
    }

    private void setupSpinners() {
        try {
            String[] tipos = new String[Residuo.TIPOS_RESIDUOS.length + 1];
            tipos[0] = "Todos";
            System.arraycopy(Residuo.TIPOS_RESIDUOS, 0, tipos, 1, Residuo.TIPOS_RESIDUOS.length);

            ArrayAdapter<String> adapter = new ArrayAdapter<>(this,
                    android.R.layout.simple_dropdown_item_1line, tipos);
            actFiltroTipo.setAdapter(adapter);

            actFiltroTipo.setOnClickListener(v -> actFiltroTipo.showDropDown());
            actFiltroTipo.setOnFocusChangeListener((v, hasFocus) -> {
                if (hasFocus) {
                    actFiltroTipo.showDropDown();
                }
            });

        } catch (Exception e) {
            Toast.makeText(this, "Error en spinners: " + e.getMessage(), Toast.LENGTH_LONG).show();
        }
    }

    private void setupDatePickers() {
        etFechaInicio.setOnClickListener(v -> mostrarDatePicker(etFechaInicio));
        etFechaFin.setOnClickListener(v -> mostrarDatePicker(etFechaFin));
    }

    private void setFechasDefault() {
        try {
            Calendar cal = Calendar.getInstance();
            String fechaFin = DateUtils.getFechaActual();

            cal.add(Calendar.DAY_OF_YEAR, -30);
            String fechaInicio = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
                    .format(cal.getTime());

            etFechaInicio.setText(DateUtils.formatearFechaMostrar(fechaInicio));
            etFechaFin.setText(DateUtils.formatearFechaMostrar(fechaFin));
        } catch (Exception e) {
            Toast.makeText(this, "Error en fechas: " + e.getMessage(), Toast.LENGTH_LONG).show();
        }
    }

    private void mostrarDatePicker(EditText editText) {
        try {
            Calendar cal = Calendar.getInstance();
            DatePickerDialog datePickerDialog = new DatePickerDialog(this,
                    (view, year, month, dayOfMonth) -> {
                        String fecha = String.format(Locale.getDefault(), "%d-%02d-%02d", year, month + 1, dayOfMonth);
                        editText.setText(DateUtils.formatearFechaMostrar(fecha));
                    },
                    cal.get(Calendar.YEAR),
                    cal.get(Calendar.MONTH),
                    cal.get(Calendar.DAY_OF_MONTH));
            datePickerDialog.show();
        } catch (Exception e) {
            Toast.makeText(this, "Error al mostrar calendario: " + e.getMessage(), Toast.LENGTH_LONG).show();
        }
    }

    private void setupButtons() {
        btnGenerarPDF.setOnClickListener(v -> generarReporte("pdf"));
        btnGenerarCSV.setOnClickListener(v -> generarReporte("csv"));
        btnCompartir.setOnClickListener(v -> compartirArchivo());
        btnAbrir.setOnClickListener(v -> abrirArchivo());
    }

    private void generarReporte(String formato) {
        try {
            // Validar fechas
            String fechaInicioStr = etFechaInicio.getText().toString();
            String fechaFinStr = etFechaFin.getText().toString();

            if (fechaInicioStr.isEmpty() || fechaFinStr.isEmpty()) {
                Toast.makeText(this, "Seleccione fechas válidas", Toast.LENGTH_SHORT).show();
                return;
            }

            // Convertir fechas a formato BD
            String fechaInicio = DateUtils.formatearFechaBD(fechaInicioStr);
            String fechaFin = DateUtils.formatearFechaBD(fechaFinStr);

            String tipo = actFiltroTipo.getText().toString();
            if (tipo.equals("Todos")) {
                tipo = null;
            }

            int usuarioId = sessionManager.getUsuarioId();

            // Mostrar progreso
            progressBar.setVisibility(View.VISIBLE);
            cardResultados.setVisibility(View.GONE);
            btnGenerarPDF.setEnabled(false);
            btnGenerarCSV.setEnabled(false);

            // Obtener residuos filtrados
            residuosFiltrados = dbHelper.obtenerResiduosConFiltros(usuarioId, fechaInicio, fechaFin, tipo);

            if (residuosFiltrados == null || residuosFiltrados.isEmpty()) {
                progressBar.setVisibility(View.GONE);
                btnGenerarPDF.setEnabled(true);
                btnGenerarCSV.setEnabled(true);
                Toast.makeText(this, "No hay registros en el período seleccionado", Toast.LENGTH_LONG).show();
                return;
            }

            // Generar reporte según formato
            if (formato.equals("pdf")) {
                ReporteUtils.generarPDF(this, residuosFiltrados, fechaInicioStr, fechaFinStr,
                        new ReporteUtils.ReporteCallback() {
                            @Override
                            public void onSuccess(File file) {
                                runOnUiThread(() -> {
                                    progressBar.setVisibility(View.GONE);
                                    btnGenerarPDF.setEnabled(true);
                                    btnGenerarCSV.setEnabled(true);
                                    archivoGenerado = file;
                                    tvResultado.setText("✅ PDF generado:\n" + file.getName() +
                                            "\n📦 Tamaño: " + (file.length() / 1024) + " KB\n" +
                                            "📁 Ubicación: Downloads");
                                    cardResultados.setVisibility(View.VISIBLE);
                                    Toast.makeText(ReportesActivity.this,
                                            "PDF guardado en Downloads", Toast.LENGTH_LONG).show();
                                });
                            }

                            @Override
                            public void onError(String error) {
                                runOnUiThread(() -> {
                                    progressBar.setVisibility(View.GONE);
                                    btnGenerarPDF.setEnabled(true);
                                    btnGenerarCSV.setEnabled(true);
                                    Toast.makeText(ReportesActivity.this,
                                            "❌ Error: " + error, Toast.LENGTH_LONG).show();
                                });
                            }
                        });
            } else {
                ReporteUtils.generarCSV(this, residuosFiltrados, fechaInicioStr, fechaFinStr,
                        new ReporteUtils.ReporteCallback() {
                            @Override
                            public void onSuccess(File file) {
                                runOnUiThread(() -> {
                                    progressBar.setVisibility(View.GONE);
                                    btnGenerarPDF.setEnabled(true);
                                    btnGenerarCSV.setEnabled(true);
                                    archivoGenerado = file;
                                    tvResultado.setText("✅ CSV generado:\n" + file.getName() +
                                            "\n📦 Tamaño: " + (file.length() / 1024) + " KB\n" +
                                            "📁 Ubicación: Downloads");
                                    cardResultados.setVisibility(View.VISIBLE);
                                    Toast.makeText(ReportesActivity.this,
                                            "CSV guardado en Downloads", Toast.LENGTH_LONG).show();
                                });
                            }

                            @Override
                            public void onError(String error) {
                                runOnUiThread(() -> {
                                    progressBar.setVisibility(View.GONE);
                                    btnGenerarPDF.setEnabled(true);
                                    btnGenerarCSV.setEnabled(true);
                                    Toast.makeText(ReportesActivity.this,
                                            "❌ Error: " + error, Toast.LENGTH_LONG).show();
                                });
                            }
                        });
            }
        } catch (Exception e) {
            e.printStackTrace();
            progressBar.setVisibility(View.GONE);
            btnGenerarPDF.setEnabled(true);
            btnGenerarCSV.setEnabled(true);
            Toast.makeText(this, "Error: " + e.getMessage(), Toast.LENGTH_LONG).show();
        }
    }

    private void compartirArchivo() {
        if (archivoGenerado != null && archivoGenerado.exists()) {
            ReporteUtils.compartirArchivo(this, archivoGenerado);
        } else {
            Toast.makeText(this, "Primero genera un reporte", Toast.LENGTH_SHORT).show();
        }
    }

    private void abrirArchivo() {
        if (archivoGenerado != null && archivoGenerado.exists()) {
            ReporteUtils.compartirArchivo(this, archivoGenerado); // Por ahora usamos compartir
            Toast.makeText(this, "Usa 'Compartir' para abrir el archivo", Toast.LENGTH_SHORT).show();
        } else {
            Toast.makeText(this, "Primero genera un reporte", Toast.LENGTH_SHORT).show();
        }
    }
}
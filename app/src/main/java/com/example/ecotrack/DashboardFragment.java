package com.example.ecotrack;

import android.graphics.Color;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.github.mikephil.charting.charts.BarChart;
import com.github.mikephil.charting.charts.PieChart;
import com.github.mikephil.charting.data.BarData;
import com.github.mikephil.charting.data.BarDataSet;
import com.github.mikephil.charting.data.BarEntry;
import com.github.mikephil.charting.data.PieData;
import com.github.mikephil.charting.data.PieDataSet;
import com.github.mikephil.charting.data.PieEntry;
import com.github.mikephil.charting.formatter.PercentFormatter;
import com.github.mikephil.charting.utils.ColorTemplate;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class DashboardFragment extends Fragment {

    private DatabaseHelper dbHelper;
    private ResiduoDAO residuoDAO;
    private TextView tvTotalRegistros, tvTotalKg;
    private RecyclerView rvUltimosRegistros;
    private ResiduoAdapter adapter;
    private PieChart pieChart;
    private BarChart barChart;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_dashboard, container, false);

        initViews(view);
        initDatabase();
        setupRecyclerView();
        cargarDatos();
        configurarGraficos();

        return view;
    }

    private void initViews(View view) {
        tvTotalRegistros = view.findViewById(R.id.tvTotalRegistros);
        tvTotalKg = view.findViewById(R.id.tvTotalKg);
        rvUltimosRegistros = view.findViewById(R.id.rvUltimosRegistros);
        pieChart = view.findViewById(R.id.pieChart);
        barChart = view.findViewById(R.id.barChart);
    }

    private void initDatabase() {
        dbHelper = new DatabaseHelper(requireContext());
        residuoDAO = new ResiduoDAO(dbHelper);
    }

    private void setupRecyclerView() {
        rvUltimosRegistros.setLayoutManager(new LinearLayoutManager(getContext()));
    }

    private void cargarDatos() {
        List<Residuo> todos = residuoDAO.listarTodos();

        // Total de registros
        tvTotalRegistros.setText(String.valueOf(todos.size()));

        // Calcular total de kg
        double totalKg = 0;
        for (Residuo r : todos) {
            if (r.getUnidad().equals("kg")) {
                totalKg += r.getCantidad();
            } else if (r.getUnidad().equals("g")) {
                totalKg += r.getCantidad() / 1000;
            } else if (r.getUnidad().equals("lb")) {
                totalKg += r.getCantidad() * 0.453592;
            }
        }
        tvTotalKg.setText(String.format(Locale.getDefault(), "%.1f kg", totalKg));

        // Mostrar últimos 5 registros
        if (!todos.isEmpty()) {
            int size = Math.min(todos.size(), 5);
            List<Residuo> ultimos = todos.subList(0, size);

            adapter = new ResiduoAdapter(ultimos,
                    residuo -> {
                        // Click en item
                    },
                    residuo -> {
                        // Long click - eliminar
                        residuoDAO.eliminar(residuo.getId());
                        cargarDatos();
                        configurarGraficos();
                        return true;
                    });

            rvUltimosRegistros.setAdapter(adapter);
        }
    }

    private void configurarGraficos() {
        List<Residuo> todos = residuoDAO.listarTodos();

        // 1. GRÁFICO CIRCULAR - Distribución por tipo
        Map<String, Double> totalPorTipo = new HashMap<>();
        for (Residuo r : todos) {
            double cantidad = r.getCantidad();
            if (!r.getUnidad().equals("kg")) {
                if (r.getUnidad().equals("g")) {
                    cantidad = r.getCantidad() / 1000;
                } else if (r.getUnidad().equals("lb")) {
                    cantidad = r.getCantidad() * 0.453592;
                }
            }
            Double valorActual = totalPorTipo.get(r.getTipo());
            if (valorActual == null) {
                valorActual = 0.0;
            }
            totalPorTipo.put(r.getTipo(), valorActual + cantidad);
        }

        List<PieEntry> pieEntries = new ArrayList<>();
        for (Map.Entry<String, Double> entry : totalPorTipo.entrySet()) {
            if (entry.getValue() > 0) {
                pieEntries.add(new PieEntry(entry.getValue().floatValue(), entry.getKey()));
            }
        }

        if (!pieEntries.isEmpty()) {
            PieDataSet pieDataSet = new PieDataSet(pieEntries, "Distribución por Tipo");
            pieDataSet.setColors(ColorTemplate.MATERIAL_COLORS);
            pieDataSet.setValueTextSize(12f);
            pieDataSet.setValueTextColor(Color.WHITE);

            PieData pieData = new PieData(pieDataSet);
            pieData.setValueFormatter(new PercentFormatter(pieChart));

            pieChart.setData(pieData);
            pieChart.setUsePercentValues(true);
            pieChart.getDescription().setEnabled(false);
            pieChart.setCenterText("Residuos\npor Tipo");
            pieChart.setCenterTextSize(14f);
            pieChart.setHoleRadius(40f);
            pieChart.setTransparentCircleRadius(45f);
            pieChart.setDrawEntryLabels(true);
            pieChart.setEntryLabelColor(Color.WHITE);
            pieChart.setEntryLabelTextSize(11f);
            pieChart.animateY(1000);
            pieChart.invalidate();
        }

        // 2. GRÁFICO DE BARRAS - Top 5 tipos por cantidad
        List<Map.Entry<String, Double>> listaOrdenada = new ArrayList<>(totalPorTipo.entrySet());
        listaOrdenada.sort((e1, e2) -> e2.getValue().compareTo(e1.getValue()));

        ArrayList<BarEntry> barEntries = new ArrayList<>();
        ArrayList<String> barLabels = new ArrayList<>();

        int limit = Math.min(5, listaOrdenada.size());
        for (int i = 0; i < limit; i++) {
            Map.Entry<String, Double> entry = listaOrdenada.get(i);
            barEntries.add(new BarEntry(i, entry.getValue().floatValue()));
            barLabels.add(entry.getKey());
        }

        if (!barEntries.isEmpty()) {
            BarDataSet barDataSet = new BarDataSet(barEntries, "Cantidad (kg)");
            barDataSet.setColors(ColorTemplate.MATERIAL_COLORS);
            barDataSet.setValueTextSize(12f);
            barDataSet.setValueTextColor(Color.BLACK);

            BarData barData = new BarData(barDataSet);
            barData.setBarWidth(0.8f);

            barChart.setData(barData);
            barChart.getDescription().setEnabled(false);
            barChart.setFitBars(true);
            barChart.animateY(1000);

            // Configurar eje X con etiquetas
            barChart.getXAxis().setValueFormatter(new com.github.mikephil.charting.formatter.IndexAxisValueFormatter(barLabels));
            barChart.getXAxis().setGranularity(1f);
            barChart.getXAxis().setPosition(com.github.mikephil.charting.components.XAxis.XAxisPosition.BOTTOM);
            barChart.getXAxis().setDrawGridLines(false);

            barChart.getAxisLeft().setAxisMinimum(0f);
            barChart.getAxisRight().setEnabled(false);

            barChart.invalidate();
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        cargarDatos();
        configurarGraficos();
    }
}
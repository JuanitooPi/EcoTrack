package com.example.ecotrack;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

public class DateUtils {

    public static String getFechaActual() {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
        return sdf.format(new Date());
    }

    public static String getHoraActual() {
        SimpleDateFormat sdf = new SimpleDateFormat("HH:mm:ss", Locale.getDefault());
        return sdf.format(new Date());
    }

    public static String formatearFechaMostrar(String fecha) {
        // Convierte de yyyy-MM-dd a dd/MM/yyyy
        try {
            SimpleDateFormat sdfEntrada = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
            SimpleDateFormat sdfSalida = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());
            Date date = sdfEntrada.parse(fecha);
            return sdfSalida.format(date);
        } catch (Exception e) {
            return fecha;
        }
    }

    public static String formatearFechaBD(String fecha) {
        // Convierte de dd/MM/yyyy a yyyy-MM-dd
        try {
            SimpleDateFormat sdfEntrada = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());
            SimpleDateFormat sdfSalida = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
            Date date = sdfEntrada.parse(fecha);
            return sdfSalida.format(date);
        } catch (Exception e) {
            return fecha;
        }
    }
}
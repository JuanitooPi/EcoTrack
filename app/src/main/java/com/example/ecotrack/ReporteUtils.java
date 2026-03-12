package com.example.ecotrack;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.os.Environment;
import android.widget.Toast;

import androidx.core.content.FileProvider;

import com.itextpdf.kernel.colors.ColorConstants;
import com.itextpdf.kernel.font.PdfFontFactory;
import com.itextpdf.kernel.pdf.PdfDocument;
import com.itextpdf.kernel.pdf.PdfWriter;
import com.itextpdf.layout.Document;
import com.itextpdf.layout.element.Cell;
import com.itextpdf.layout.element.Paragraph;
import com.itextpdf.layout.element.Table;
import com.itextpdf.layout.property.TextAlignment;
import com.itextpdf.layout.property.UnitValue;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class ReporteUtils {

    public interface ReporteCallback {
        void onSuccess(File file);
        void onError(String error);
    }

    /**
     * Generar reporte PDF
     */
    public static void generarPDF(Context context, List<Residuo> residuos,
                                  String fechaInicio, String fechaFin,
                                  ReporteCallback callback) {
        try {
            // Verificar que haya datos
            if (residuos == null || residuos.isEmpty()) {
                callback.onError("No hay datos para generar el PDF");
                return;
            }

            // Crear nombre de archivo con timestamp
            String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(new Date());
            String fileName = "Reporte_Residuos_" + timeStamp + ".pdf";

            // Obtener directorio de descargas
            File downloadsDir;
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                downloadsDir = context.getExternalFilesDir(Environment.DIRECTORY_DOWNLOADS);
                if (downloadsDir == null) {
                    downloadsDir = context.getFilesDir();
                }
            } else {
                downloadsDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS);
            }

            if (downloadsDir != null && !downloadsDir.exists()) {
                boolean created = downloadsDir.mkdirs();
                if (!created) {
                    callback.onError("No se pudo crear el directorio");
                    return;
                }
            }

            if (downloadsDir == null) {
                callback.onError("No se pudo acceder al directorio de descargas");
                return;
            }

            File pdfFile = new File(downloadsDir, fileName);

            // Inicializar PDF writer
            PdfWriter writer = new PdfWriter(pdfFile);
            PdfDocument pdfDoc = new PdfDocument(writer);
            Document document = new Document(pdfDoc);

            // Título
            Paragraph titulo = new Paragraph("Reporte de Residuos Sólidos - ECOLIM S.A.C.")
                    .setFontSize(18)
                    .setBold()
                    .setTextAlignment(TextAlignment.CENTER)
                    .setMarginBottom(10);
            document.add(titulo);

            // Información del reporte
            SimpleDateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());
            String fechaActual = dateFormat.format(new Date());

            Paragraph info = new Paragraph(
                    "Fecha de generación: " + fechaActual + "\n" +
                            "Período: " + fechaInicio + " - " + fechaFin + "\n" +
                            "Total de registros: " + residuos.size()
            ).setFontSize(10).setMarginBottom(20);
            document.add(info);

            // Crear tabla
            float[] columnWidths = {1, 2, 1, 1, 1, 2};
            Table table = new Table(UnitValue.createPercentArray(columnWidths));
            table.setWidth(UnitValue.createPercentValue(100));

            // Encabezados
            String[] headers = {"ID", "Tipo", "Cantidad", "Unidad", "Fecha", "Ubicación"};
            for (String header : headers) {
                table.addHeaderCell(new Cell().add(new Paragraph(header))
                        .setBold()
                        .setBackgroundColor(ColorConstants.LIGHT_GRAY)
                        .setTextAlignment(TextAlignment.CENTER));
            }

            // Datos
            SimpleDateFormat formatoFecha = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());
            for (Residuo r : residuos) {
                table.addCell(new Cell().add(new Paragraph(String.valueOf(r.getId()))).setTextAlignment(TextAlignment.CENTER));
                table.addCell(new Cell().add(new Paragraph(r.getTipo() != null ? r.getTipo() : "")));
                table.addCell(new Cell().add(new Paragraph(String.format(Locale.getDefault(), "%.2f", r.getCantidad()))).setTextAlignment(TextAlignment.RIGHT));
                table.addCell(new Cell().add(new Paragraph(r.getUnidad() != null ? r.getUnidad() : "")).setTextAlignment(TextAlignment.CENTER));

                // Formatear fecha
                String fechaFormateada = r.getFecha();
                try {
                    if (r.getFecha() != null && !r.getFecha().isEmpty()) {
                        Date fecha = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).parse(r.getFecha());
                        fechaFormateada = formatoFecha.format(fecha);
                    }
                } catch (Exception e) {
                    fechaFormateada = r.getFecha();
                }
                table.addCell(new Cell().add(new Paragraph(fechaFormateada)).setTextAlignment(TextAlignment.CENTER));
                table.addCell(new Cell().add(new Paragraph(r.getUbicacion() != null ? r.getUbicacion() : "")));
            }

            document.add(table);

            // Resumen al final
            document.add(new Paragraph("\n"));
            Paragraph resumen = new Paragraph("RESUMEN ESTADÍSTICO")
                    .setBold()
                    .setFontSize(12)
                    .setMarginTop(10);
            document.add(resumen);

            // Calcular totales por tipo
            double totalPlastico = 0, totalPapel = 0, totalVidrio = 0, totalMetal = 0, totalOrganico = 0;
            for (Residuo r : residuos) {
                double cantidad = r.getCantidad();
                if (r.getUnidad() != null) {
                    if (r.getUnidad().equals("g")) cantidad = r.getCantidad() / 1000;
                    else if (r.getUnidad().equals("lb")) cantidad = r.getCantidad() * 0.453592;
                }

                if (r.getTipo() != null) {
                    switch (r.getTipo()) {
                        case "Plástico": totalPlastico += cantidad; break;
                        case "Papel/Cartón": totalPapel += cantidad; break;
                        case "Vidrio": totalVidrio += cantidad; break;
                        case "Metal": totalMetal += cantidad; break;
                        case "Orgánico": totalOrganico += cantidad; break;
                    }
                }
            }

            Paragraph totales = new Paragraph(
                    "Plástico: " + String.format(Locale.getDefault(), "%.2f kg\n", totalPlastico) +
                            "Papel/Cartón: " + String.format(Locale.getDefault(), "%.2f kg\n", totalPapel) +
                            "Vidrio: " + String.format(Locale.getDefault(), "%.2f kg\n", totalVidrio) +
                            "Metal: " + String.format(Locale.getDefault(), "%.2f kg\n", totalMetal) +
                            "Orgánico: " + String.format(Locale.getDefault(), "%.2f kg", totalOrganico)
            ).setFontSize(10);
            document.add(totales);

            document.close();

            callback.onSuccess(pdfFile);

        } catch (Exception e) {
            e.printStackTrace();
            callback.onError("Error al crear el PDF: " + e.getMessage());
        }
    }

    /**
     * Generar reporte CSV
     */
    public static void generarCSV(Context context, List<Residuo> residuos,
                                  String fechaInicio, String fechaFin,
                                  ReporteCallback callback) {
        try {
            // Verificar que haya datos
            if (residuos == null || residuos.isEmpty()) {
                callback.onError("No hay datos para generar el CSV");
                return;
            }

            // Crear nombre de archivo con timestamp
            String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(new Date());
            String fileName = "Reporte_Residuos_" + timeStamp + ".csv";

            // Obtener directorio de descargas
            File downloadsDir;
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                downloadsDir = context.getExternalFilesDir(Environment.DIRECTORY_DOWNLOADS);
                if (downloadsDir == null) {
                    downloadsDir = context.getFilesDir();
                }
            } else {
                downloadsDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS);
            }

            if (downloadsDir != null && !downloadsDir.exists()) {
                boolean created = downloadsDir.mkdirs();
                if (!created) {
                    callback.onError("No se pudo crear el directorio");
                    return;
                }
            }

            if (downloadsDir == null) {
                callback.onError("No se pudo acceder al directorio de descargas");
                return;
            }

            File csvFile = new File(downloadsDir, fileName);

            // Crear CSV writer
            FileWriter fileWriter = new FileWriter(csvFile);

            // Encabezados
            fileWriter.append("ID,Tipo,Cantidad,Unidad,Fecha,Hora,Ubicación,Sincronizado\n");

            // Datos
            SimpleDateFormat formatoFecha = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());
            for (Residuo r : residuos) {
                String fechaFormateada = r.getFecha();
                try {
                    if (r.getFecha() != null && !r.getFecha().isEmpty()) {
                        Date fecha = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).parse(r.getFecha());
                        fechaFormateada = formatoFecha.format(fecha);
                    }
                } catch (Exception e) {
                    fechaFormateada = r.getFecha();
                }

                fileWriter.append(String.valueOf(r.getId())).append(",")
                        .append(r.getTipo() != null ? r.getTipo() : "").append(",")
                        .append(String.format(Locale.getDefault(), "%.2f", r.getCantidad())).append(",")
                        .append(r.getUnidad() != null ? r.getUnidad() : "").append(",")
                        .append(fechaFormateada).append(",")
                        .append(r.getHora() != null ? r.getHora() : "").append(",")
                        .append(r.getUbicacion() != null ? r.getUbicacion() : "").append(",")
                        .append(r.getSincronizado() == 1 ? "Sí" : "No").append("\n");
            }

            // Agregar resumen al final
            fileWriter.append("\nRESUMEN ESTADÍSTICO\n");
            fileWriter.append("Total de registros,").append(String.valueOf(residuos.size())).append("\n");

            // Calcular totales por tipo
            double totalPlastico = 0, totalPapel = 0, totalVidrio = 0, totalMetal = 0, totalOrganico = 0;
            for (Residuo r : residuos) {
                double cantidad = r.getCantidad();
                if (r.getUnidad() != null) {
                    if (r.getUnidad().equals("g")) cantidad = r.getCantidad() / 1000;
                    else if (r.getUnidad().equals("lb")) cantidad = r.getCantidad() * 0.453592;
                }

                if (r.getTipo() != null) {
                    switch (r.getTipo()) {
                        case "Plástico": totalPlastico += cantidad; break;
                        case "Papel/Cartón": totalPapel += cantidad; break;
                        case "Vidrio": totalVidrio += cantidad; break;
                        case "Metal": totalMetal += cantidad; break;
                        case "Orgánico": totalOrganico += cantidad; break;
                    }
                }
            }

            fileWriter.append("Plástico (kg),").append(String.format(Locale.getDefault(), "%.2f", totalPlastico)).append("\n");
            fileWriter.append("Papel/Cartón (kg),").append(String.format(Locale.getDefault(), "%.2f", totalPapel)).append("\n");
            fileWriter.append("Vidrio (kg),").append(String.format(Locale.getDefault(), "%.2f", totalVidrio)).append("\n");
            fileWriter.append("Metal (kg),").append(String.format(Locale.getDefault(), "%.2f", totalMetal)).append("\n");
            fileWriter.append("Orgánico (kg),").append(String.format(Locale.getDefault(), "%.2f", totalOrganico)).append("\n");

            fileWriter.close();

            callback.onSuccess(csvFile);

        } catch (Exception e) {
            e.printStackTrace();
            callback.onError("Error al generar CSV: " + e.getMessage());
        }
    }

    /**
     * Compartir archivo generado
     */
    public static void compartirArchivo(Context context, File file) {
        try {
            if (file == null || !file.exists()) {
                Toast.makeText(context, "El archivo no existe", Toast.LENGTH_SHORT).show();
                return;
            }

            Uri fileUri;
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                fileUri = FileProvider.getUriForFile(context,
                        context.getPackageName() + ".provider", file);
            } else {
                fileUri = Uri.fromFile(file);
            }

            Intent shareIntent = new Intent(Intent.ACTION_SEND);
            shareIntent.setType(getMimeType(file.getName()));
            shareIntent.putExtra(Intent.EXTRA_STREAM, fileUri);
            shareIntent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);

            context.startActivity(Intent.createChooser(shareIntent, "Compartir reporte"));

        } catch (Exception e) {
            e.printStackTrace();
            Toast.makeText(context, "Error al compartir: " + e.getMessage(), Toast.LENGTH_SHORT).show();
        }
    }

    private static String getMimeType(String fileName) {
        if (fileName != null) {
            if (fileName.endsWith(".pdf")) {
                return "application/pdf";
            } else if (fileName.endsWith(".csv")) {
                return "text/csv";
            }
        }
        return "*/*";
    }
}
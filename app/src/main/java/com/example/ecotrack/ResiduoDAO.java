package com.example.ecotrack;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import java.util.ArrayList;
import java.util.List;

public class ResiduoDAO {
    private DatabaseHelper dbHelper;

    public ResiduoDAO(DatabaseHelper dbHelper) {
        this.dbHelper = dbHelper;
    }

    public long insertar(Residuo residuo) {
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        ContentValues values = new ContentValues();

        values.put(DatabaseHelper.COLUMN_TIPO, residuo.getTipo());
        values.put(DatabaseHelper.COLUMN_CANTIDAD, residuo.getCantidad());
        values.put(DatabaseHelper.COLUMN_UNIDAD, residuo.getUnidad());
        values.put(DatabaseHelper.COLUMN_FECHA, residuo.getFecha());
        values.put(DatabaseHelper.COLUMN_HORA, residuo.getHora());
        values.put(DatabaseHelper.COLUMN_UBICACION, residuo.getUbicacion());
        values.put(DatabaseHelper.COLUMN_SINCRONIZADO, residuo.getSincronizado());

        long id = db.insert(DatabaseHelper.TABLE_RESIDUOS, null, values);
        db.close();
        return id;
    }

    public List<Residuo> listarTodos() {
        List<Residuo> lista = new ArrayList<>();
        SQLiteDatabase db = dbHelper.getReadableDatabase();

        String query = "SELECT * FROM " + DatabaseHelper.TABLE_RESIDUOS +
                " ORDER BY " + DatabaseHelper.COLUMN_FECHA + " DESC, " +
                DatabaseHelper.COLUMN_HORA + " DESC";

        Cursor cursor = db.rawQuery(query, null);

        if (cursor.moveToFirst()) {
            do {
                Residuo residuo = new Residuo();
                residuo.setId(cursor.getInt(cursor.getColumnIndexOrThrow(DatabaseHelper.COLUMN_ID)));
                residuo.setTipo(cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.COLUMN_TIPO)));
                residuo.setCantidad(cursor.getDouble(cursor.getColumnIndexOrThrow(DatabaseHelper.COLUMN_CANTIDAD)));
                residuo.setUnidad(cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.COLUMN_UNIDAD)));
                residuo.setFecha(cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.COLUMN_FECHA)));
                residuo.setHora(cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.COLUMN_HORA)));
                residuo.setUbicacion(cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.COLUMN_UBICACION)));
                residuo.setSincronizado(cursor.getInt(cursor.getColumnIndexOrThrow(DatabaseHelper.COLUMN_SINCRONIZADO)));

                lista.add(residuo);
            } while (cursor.moveToNext());
        }

        cursor.close();
        db.close();
        return lista;
    }

    public List<Residuo> listarConFiltros(String fechaInicio, String fechaFin, String tipo) {
        List<Residuo> lista = new ArrayList<>();
        SQLiteDatabase db = dbHelper.getReadableDatabase();

        StringBuilder query = new StringBuilder();
        query.append("SELECT * FROM ").append(DatabaseHelper.TABLE_RESIDUOS).append(" WHERE 1=1");

        List<String> args = new ArrayList<>();

        if (fechaInicio != null && fechaFin != null && !fechaInicio.isEmpty() && !fechaFin.isEmpty()) {
            query.append(" AND ").append(DatabaseHelper.COLUMN_FECHA).append(" BETWEEN ? AND ?");
            args.add(fechaInicio);
            args.add(fechaFin);
        }

        if (tipo != null && !tipo.isEmpty() && !tipo.equals("Todos")) {
            query.append(" AND ").append(DatabaseHelper.COLUMN_TIPO).append(" = ?");
            args.add(tipo);
        }

        query.append(" ORDER BY ").append(DatabaseHelper.COLUMN_FECHA).append(" DESC, ")
                .append(DatabaseHelper.COLUMN_HORA).append(" DESC");

        Cursor cursor = db.rawQuery(query.toString(), args.toArray(new String[0]));

        if (cursor.moveToFirst()) {
            do {
                Residuo residuo = new Residuo();
                residuo.setId(cursor.getInt(cursor.getColumnIndexOrThrow(DatabaseHelper.COLUMN_ID)));
                residuo.setTipo(cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.COLUMN_TIPO)));
                residuo.setCantidad(cursor.getDouble(cursor.getColumnIndexOrThrow(DatabaseHelper.COLUMN_CANTIDAD)));
                residuo.setUnidad(cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.COLUMN_UNIDAD)));
                residuo.setFecha(cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.COLUMN_FECHA)));
                residuo.setHora(cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.COLUMN_HORA)));
                residuo.setUbicacion(cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.COLUMN_UBICACION)));
                residuo.setSincronizado(cursor.getInt(cursor.getColumnIndexOrThrow(DatabaseHelper.COLUMN_SINCRONIZADO)));

                lista.add(residuo);
            } while (cursor.moveToNext());
        }

        cursor.close();
        db.close();
        return lista;
    }

    public int actualizarSincronizado(int id) {
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(DatabaseHelper.COLUMN_SINCRONIZADO, 1);

        return db.update(DatabaseHelper.TABLE_RESIDUOS, values,
                DatabaseHelper.COLUMN_ID + " = ?", new String[]{String.valueOf(id)});
    }

    public void eliminar(int id) {
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        db.delete(DatabaseHelper.TABLE_RESIDUOS,
                DatabaseHelper.COLUMN_ID + " = ?", new String[]{String.valueOf(id)});
        db.close();
    }
}
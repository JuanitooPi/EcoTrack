package com.example.ecotrack;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import java.util.ArrayList;
import java.util.List;

public class DatabaseHelper extends SQLiteOpenHelper {

    // Nombre de la base de datos y versión
    private static final String DATABASE_NAME = "EcoTrack.db";
    private static final int DATABASE_VERSION = 2; // Incrementado a 2 por la nueva tabla

    // Tabla de residuos
    public static final String TABLE_RESIDUOS = "residuos";
    public static final String COLUMN_ID = "id";
    public static final String COLUMN_TIPO = "tipo";
    public static final String COLUMN_CANTIDAD = "cantidad";
    public static final String COLUMN_UNIDAD = "unidad";
    public static final String COLUMN_FECHA = "fecha";
    public static final String COLUMN_HORA = "hora";
    public static final String COLUMN_UBICACION = "ubicacion";
    public static final String COLUMN_SINCRONIZADO = "sincronizado";
    public static final String COLUMN_USUARIO_ID_FK = "usuario_id"; // Renombrado para evitar duplicado

    // Tabla de usuarios
    public static final String TABLE_USUARIOS = "usuarios";
    public static final String COLUMN_USUARIO_ID = "usuario_id"; // Este es el ID de usuario
    public static final String COLUMN_NOMBRE = "nombre";
    public static final String COLUMN_EMAIL = "email";
    public static final String COLUMN_ROL = "rol";
    public static final String COLUMN_PASSWORD = "password";
    public static final String COLUMN_FOTO = "foto";

    // Sentencia SQL para crear tabla de residuos (actualizada)
    private static final String CREATE_TABLE_RESIDUOS = "CREATE TABLE " + TABLE_RESIDUOS + "("
            + COLUMN_ID + " INTEGER PRIMARY KEY AUTOINCREMENT,"
            + COLUMN_TIPO + " TEXT NOT NULL,"
            + COLUMN_CANTIDAD + " REAL NOT NULL,"
            + COLUMN_UNIDAD + " TEXT NOT NULL,"
            + COLUMN_FECHA + " TEXT NOT NULL,"
            + COLUMN_HORA + " TEXT NOT NULL,"
            + COLUMN_UBICACION + " TEXT,"
            + COLUMN_SINCRONIZADO + " INTEGER DEFAULT 0,"
            + COLUMN_USUARIO_ID_FK + " INTEGER DEFAULT 1,"
            + "FOREIGN KEY(" + COLUMN_USUARIO_ID_FK + ") REFERENCES " + TABLE_USUARIOS + "(" + COLUMN_USUARIO_ID + "))";

    // Sentencia SQL para crear tabla de usuarios
    private static final String CREATE_TABLE_USUARIOS = "CREATE TABLE " + TABLE_USUARIOS + "("
            + COLUMN_USUARIO_ID + " INTEGER PRIMARY KEY AUTOINCREMENT,"
            + COLUMN_NOMBRE + " TEXT NOT NULL,"
            + COLUMN_EMAIL + " TEXT UNIQUE NOT NULL,"
            + COLUMN_ROL + " TEXT NOT NULL,"
            + COLUMN_PASSWORD + " TEXT NOT NULL,"
            + COLUMN_FOTO + " TEXT" + ")";

    // Constructor
    public DatabaseHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        // Crear tabla de usuarios primero (por la foreign key)
        db.execSQL(CREATE_TABLE_USUARIOS);
        // Crear tabla de residuos
        db.execSQL(CREATE_TABLE_RESIDUOS);

        // Insertar usuario por defecto
        insertarUsuarioPorDefecto(db);
    }

    private void insertarUsuarioPorDefecto(SQLiteDatabase db) {
        ContentValues values = new ContentValues();
        values.put(COLUMN_NOMBRE, "Operario ECOLIM");
        values.put(COLUMN_EMAIL, "operario@ecolim.com");
        values.put(COLUMN_ROL, "Recolector");
        values.put(COLUMN_PASSWORD, "123456");
        db.insert(TABLE_USUARIOS, null, values);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        if (oldVersion < 2) {
            // Crear tabla de usuarios
            db.execSQL(CREATE_TABLE_USUARIOS);

            // Agregar columna usuario_id a residuos
            db.execSQL("ALTER TABLE " + TABLE_RESIDUOS + " ADD COLUMN " + COLUMN_USUARIO_ID_FK + " INTEGER DEFAULT 1");

            // Insertar usuario por defecto si no existe
            Cursor cursor = db.rawQuery("SELECT COUNT(*) FROM " + TABLE_USUARIOS, null);
            cursor.moveToFirst();
            int count = cursor.getInt(0);
            cursor.close();

            if (count == 0) {
                insertarUsuarioPorDefecto(db);
            }
        }
    }

    // ==================== MÉTODOS PARA USUARIOS ====================

    /**
     * Insertar un nuevo usuario
     */
    public long insertarUsuario(Usuario usuario) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();

        values.put(COLUMN_NOMBRE, usuario.getNombre());
        values.put(COLUMN_EMAIL, usuario.getEmail());
        values.put(COLUMN_ROL, usuario.getRol());
        values.put(COLUMN_PASSWORD, usuario.getPassword());
        values.put(COLUMN_FOTO, usuario.getFoto());

        long id = db.insert(TABLE_USUARIOS, null, values);
        db.close();
        return id;
    }

    /**
     * Obtener usuario por nombre de usuario y password (login)
     */
    public Usuario obtenerUsuarioPorCredenciales(String usuario, String password) {
        SQLiteDatabase db = this.getReadableDatabase();

        // Buscar por nombre de usuario (COLUMN_NOMBRE) y password
        Cursor cursor = db.query(TABLE_USUARIOS, null,
                COLUMN_NOMBRE + " = ? AND " + COLUMN_PASSWORD + " = ?",
                new String[]{usuario, password}, null, null, null);

        Usuario user = null;
        if (cursor.moveToFirst()) {
            user = new Usuario();
            user.setId(cursor.getInt(cursor.getColumnIndexOrThrow(COLUMN_USUARIO_ID)));
            user.setNombre(cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_NOMBRE)));
            user.setEmail(cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_EMAIL)));
            user.setRol(cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_ROL)));
            user.setPassword(cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_PASSWORD)));
            user.setFoto(cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_FOTO)));
        }
        cursor.close();
        db.close();
        return user;
    }

    /**
     * Obtener usuario por ID
     */
    public Usuario obtenerUsuarioPorId(int id) {
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.query(TABLE_USUARIOS, null,
                COLUMN_USUARIO_ID + " = ?",
                new String[]{String.valueOf(id)}, null, null, null);

        Usuario usuario = null;
        if (cursor.moveToFirst()) {
            usuario = new Usuario();
            usuario.setId(cursor.getInt(cursor.getColumnIndexOrThrow(COLUMN_USUARIO_ID)));
            usuario.setNombre(cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_NOMBRE)));
            usuario.setEmail(cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_EMAIL)));
            usuario.setRol(cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_ROL)));
            usuario.setPassword(cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_PASSWORD)));
            usuario.setFoto(cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_FOTO)));
        }
        cursor.close();
        db.close();
        return usuario;
    }

    /**
     * Actualizar usuario
     */
    public int actualizarUsuario(Usuario usuario) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();

        values.put(COLUMN_NOMBRE, usuario.getNombre());
        values.put(COLUMN_EMAIL, usuario.getEmail());
        values.put(COLUMN_ROL, usuario.getRol());
        if (usuario.getPassword() != null && !usuario.getPassword().isEmpty()) {
            values.put(COLUMN_PASSWORD, usuario.getPassword());
        }
        values.put(COLUMN_FOTO, usuario.getFoto());

        int rowsAffected = db.update(TABLE_USUARIOS, values,
                COLUMN_USUARIO_ID + " = ?",
                new String[]{String.valueOf(usuario.getId())});
        db.close();
        return rowsAffected;
    }

    public boolean actualizarFotoUsuario(int usuarioId, String fotoUri) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(COLUMN_FOTO, fotoUri);

        int rowsAffected = db.update(TABLE_USUARIOS, values,
                COLUMN_USUARIO_ID + " = ?",
                new String[]{String.valueOf(usuarioId)});
        db.close();
        return rowsAffected > 0;
    }

    /**
     * Cambiar contraseña
     */
    public boolean cambiarPassword(int usuarioId, String passwordActual, String passwordNueva) {
        // Verificar contraseña actual
        Usuario usuario = obtenerUsuarioPorId(usuarioId);
        if (usuario != null && usuario.getPassword().equals(passwordActual)) {
            SQLiteDatabase db = this.getWritableDatabase();
            ContentValues values = new ContentValues();
            values.put(COLUMN_PASSWORD, passwordNueva);

            int rowsAffected = db.update(TABLE_USUARIOS, values,
                    COLUMN_USUARIO_ID + " = ?",
                    new String[]{String.valueOf(usuarioId)});
            db.close();
            return rowsAffected > 0;
        }
        return false;
    }

    /**
     * Verificar si un usuario o email ya existen
     */
    public boolean usuarioExiste(String usuario, String email) {
        SQLiteDatabase db = this.getReadableDatabase();
        String query = "SELECT * FROM " + TABLE_USUARIOS + " WHERE "
                + COLUMN_NOMBRE + " = ? OR " + COLUMN_EMAIL + " = ?";
        Cursor cursor = db.rawQuery(query, new String[]{usuario, email});

        boolean existe = cursor.getCount() > 0;
        cursor.close();
        db.close();
        return existe;
    }

    // ==================== MÉTODOS PARA RESIDUOS (ACTUALIZADOS) ====================

    /**
     * Insertar un nuevo residuo (asociado al usuario actual)
     */
    public long insertarResiduo(Residuo residuo, int usuarioId) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();

        values.put(COLUMN_TIPO, residuo.getTipo());
        values.put(COLUMN_CANTIDAD, residuo.getCantidad());
        values.put(COLUMN_UNIDAD, residuo.getUnidad());
        values.put(COLUMN_FECHA, residuo.getFecha());
        values.put(COLUMN_HORA, residuo.getHora());
        values.put(COLUMN_UBICACION, residuo.getUbicacion());
        values.put(COLUMN_SINCRONIZADO, residuo.getSincronizado());
        values.put(COLUMN_USUARIO_ID_FK, usuarioId);

        long id = db.insert(TABLE_RESIDUOS, null, values);
        db.close();
        return id;
    }

    /**
     * Obtener todos los residuos de un usuario específico
     */
    public List<Residuo> obtenerResiduosPorUsuario(int usuarioId) {
        List<Residuo> listaResiduos = new ArrayList<>();
        String query = "SELECT * FROM " + TABLE_RESIDUOS
                + " WHERE " + COLUMN_USUARIO_ID_FK + " = ?"
                + " ORDER BY " + COLUMN_FECHA + " DESC, " + COLUMN_HORA + " DESC";

        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery(query, new String[]{String.valueOf(usuarioId)});

        if (cursor.moveToFirst()) {
            do {
                Residuo residuo = new Residuo();
                residuo.setId(cursor.getInt(cursor.getColumnIndexOrThrow(COLUMN_ID)));
                residuo.setTipo(cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_TIPO)));
                residuo.setCantidad(cursor.getDouble(cursor.getColumnIndexOrThrow(COLUMN_CANTIDAD)));
                residuo.setUnidad(cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_UNIDAD)));
                residuo.setFecha(cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_FECHA)));
                residuo.setHora(cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_HORA)));
                residuo.setUbicacion(cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_UBICACION)));
                residuo.setSincronizado(cursor.getInt(cursor.getColumnIndexOrThrow(COLUMN_SINCRONIZADO)));

                listaResiduos.add(residuo);
            } while (cursor.moveToNext());
        }

        cursor.close();
        db.close();
        return listaResiduos;
    }

    /**
     * Obtener residuos con filtros para un usuario específico
     */
    public List<Residuo> obtenerResiduosConFiltros(int usuarioId, String fechaInicio, String fechaFin, String tipo) {
        List<Residuo> listaResiduos = new ArrayList<>();
        SQLiteDatabase db = this.getReadableDatabase();

        StringBuilder query = new StringBuilder();
        query.append("SELECT * FROM ").append(TABLE_RESIDUOS)
                .append(" WHERE ").append(COLUMN_USUARIO_ID_FK).append(" = ?");

        List<String> args = new ArrayList<>();
        args.add(String.valueOf(usuarioId));

        if (fechaInicio != null && fechaFin != null && !fechaInicio.isEmpty() && !fechaFin.isEmpty()) {
            query.append(" AND ").append(COLUMN_FECHA).append(" BETWEEN ? AND ?");
            args.add(fechaInicio);
            args.add(fechaFin);
        }

        if (tipo != null && !tipo.isEmpty() && !tipo.equals("Todos")) {
            query.append(" AND ").append(COLUMN_TIPO).append(" = ?");
            args.add(tipo);
        }

        query.append(" ORDER BY ").append(COLUMN_FECHA).append(" DESC, ")
                .append(COLUMN_HORA).append(" DESC");

        Cursor cursor = db.rawQuery(query.toString(), args.toArray(new String[0]));

        if (cursor.moveToFirst()) {
            do {
                Residuo residuo = new Residuo();
                residuo.setId(cursor.getInt(cursor.getColumnIndexOrThrow(COLUMN_ID)));
                residuo.setTipo(cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_TIPO)));
                residuo.setCantidad(cursor.getDouble(cursor.getColumnIndexOrThrow(COLUMN_CANTIDAD)));
                residuo.setUnidad(cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_UNIDAD)));
                residuo.setFecha(cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_FECHA)));
                residuo.setHora(cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_HORA)));
                residuo.setUbicacion(cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_UBICACION)));
                residuo.setSincronizado(cursor.getInt(cursor.getColumnIndexOrThrow(COLUMN_SINCRONIZADO)));

                listaResiduos.add(residuo);
            } while (cursor.moveToNext());
        }

        cursor.close();
        db.close();
        return listaResiduos;
    }

    /**
     * Obtener un residuo específico (verificando que pertenezca al usuario)
     */
    public Residuo obtenerResiduoPorId(int id, int usuarioId) {
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.query(TABLE_RESIDUOS, null,
                COLUMN_ID + " = ? AND " + COLUMN_USUARIO_ID_FK + " = ?",
                new String[]{String.valueOf(id), String.valueOf(usuarioId)},
                null, null, null);

        Residuo residuo = null;
        if (cursor.moveToFirst()) {
            residuo = new Residuo();
            residuo.setId(cursor.getInt(cursor.getColumnIndexOrThrow(COLUMN_ID)));
            residuo.setTipo(cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_TIPO)));
            residuo.setCantidad(cursor.getDouble(cursor.getColumnIndexOrThrow(COLUMN_CANTIDAD)));
            residuo.setUnidad(cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_UNIDAD)));
            residuo.setFecha(cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_FECHA)));
            residuo.setHora(cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_HORA)));
            residuo.setUbicacion(cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_UBICACION)));
            residuo.setSincronizado(cursor.getInt(cursor.getColumnIndexOrThrow(COLUMN_SINCRONIZADO)));
        }
        cursor.close();
        db.close();
        return residuo;
    }

    /**
     * Actualizar un residuo (verificando que pertenezca al usuario)
     */
    public int actualizarResiduo(Residuo residuo, int usuarioId) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();

        values.put(COLUMN_TIPO, residuo.getTipo());
        values.put(COLUMN_CANTIDAD, residuo.getCantidad());
        values.put(COLUMN_UNIDAD, residuo.getUnidad());
        values.put(COLUMN_FECHA, residuo.getFecha());
        values.put(COLUMN_HORA, residuo.getHora());
        values.put(COLUMN_UBICACION, residuo.getUbicacion());
        values.put(COLUMN_SINCRONIZADO, residuo.getSincronizado());

        int rowsAffected = db.update(TABLE_RESIDUOS, values,
                COLUMN_ID + " = ? AND " + COLUMN_USUARIO_ID_FK + " = ?",
                new String[]{String.valueOf(residuo.getId()), String.valueOf(usuarioId)});
        db.close();
        return rowsAffected;
    }

    /**
     * Eliminar un residuo (verificando que pertenezca al usuario)
     */
    public void eliminarResiduo(int id, int usuarioId) {
        SQLiteDatabase db = this.getWritableDatabase();
        db.delete(TABLE_RESIDUOS,
                COLUMN_ID + " = ? AND " + COLUMN_USUARIO_ID_FK + " = ?",
                new String[]{String.valueOf(id), String.valueOf(usuarioId)});
        db.close();
    }

    /**
     * Marcar un residuo como sincronizado (verificando que pertenezca al usuario)
     */
    public int marcarComoSincronizado(int id, int usuarioId) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(COLUMN_SINCRONIZADO, 1);

        int rowsAffected = db.update(TABLE_RESIDUOS, values,
                COLUMN_ID + " = ? AND " + COLUMN_USUARIO_ID_FK + " = ?",
                new String[]{String.valueOf(id), String.valueOf(usuarioId)});
        db.close();
        return rowsAffected;
    }

    /**
     * Obtener total de residuos de un usuario
     */
    public int contarResiduosPorUsuario(int usuarioId) {
        String query = "SELECT COUNT(*) FROM " + TABLE_RESIDUOS + " WHERE " + COLUMN_USUARIO_ID_FK + " = ?";
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery(query, new String[]{String.valueOf(usuarioId)});
        int count = 0;
        if (cursor.moveToFirst()) {
            count = cursor.getInt(0);
        }
        cursor.close();
        db.close();
        return count;
    }

    /**
     * Obtener total de kg por tipo para un usuario
     */
    public double getTotalPorTipoYUsuario(String tipo, int usuarioId) {
        String query = "SELECT SUM(" + COLUMN_CANTIDAD + ") FROM " + TABLE_RESIDUOS
                + " WHERE " + COLUMN_TIPO + " = ? AND " + COLUMN_UNIDAD + " = 'kg'"
                + " AND " + COLUMN_USUARIO_ID_FK + " = ?";
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery(query, new String[]{tipo, String.valueOf(usuarioId)});
        double total = 0;
        if (cursor.moveToFirst()) {
            total = cursor.getDouble(0);
        }
        cursor.close();
        db.close();
        return total;
    }

    /**
     * Obtener residuos por mes para un usuario
     */
    public List<Residuo> obtenerResiduosPorMes(String mes, int usuarioId) {
        List<Residuo> listaResiduos = new ArrayList<>();
        String query = "SELECT * FROM " + TABLE_RESIDUOS
                + " WHERE " + COLUMN_FECHA + " LIKE ?"
                + " AND " + COLUMN_USUARIO_ID_FK + " = ?"
                + " ORDER BY " + COLUMN_FECHA + " DESC";

        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery(query, new String[]{mes + "%", String.valueOf(usuarioId)});

        if (cursor.moveToFirst()) {
            do {
                Residuo residuo = new Residuo();
                residuo.setId(cursor.getInt(cursor.getColumnIndexOrThrow(COLUMN_ID)));
                residuo.setTipo(cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_TIPO)));
                residuo.setCantidad(cursor.getDouble(cursor.getColumnIndexOrThrow(COLUMN_CANTIDAD)));
                residuo.setUnidad(cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_UNIDAD)));
                residuo.setFecha(cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_FECHA)));
                residuo.setHora(cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_HORA)));
                residuo.setUbicacion(cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_UBICACION)));
                residuo.setSincronizado(cursor.getInt(cursor.getColumnIndexOrThrow(COLUMN_SINCRONIZADO)));

                listaResiduos.add(residuo);
            } while (cursor.moveToNext());
        }

        cursor.close();
        db.close();
        return listaResiduos;
    }

    /**
     * Obtener todos los usuarios (para administración)
     */
    public List<Usuario> obtenerTodosUsuarios() {
        List<Usuario> listaUsuarios = new ArrayList<>();
        String query = "SELECT * FROM " + TABLE_USUARIOS + " ORDER BY " + COLUMN_NOMBRE;

        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery(query, null);

        if (cursor.moveToFirst()) {
            do {
                Usuario usuario = new Usuario();
                usuario.setId(cursor.getInt(cursor.getColumnIndexOrThrow(COLUMN_USUARIO_ID)));
                usuario.setNombre(cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_NOMBRE)));
                usuario.setEmail(cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_EMAIL)));
                usuario.setRol(cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_ROL)));
                usuario.setPassword(cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_PASSWORD)));
                usuario.setFoto(cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_FOTO)));

                listaUsuarios.add(usuario);
            } while (cursor.moveToNext());
        }

        cursor.close();
        db.close();
        return listaUsuarios;
    }

    // ==================== MÉTODOS COMPATIBILIDAD (VERSIÓN ANTIGUA) ====================
    // Estos métodos se mantienen para no romper el código existente

    /**
     * @deprecated Usar insertarResiduo(Residuo, int) en su lugar
     */
    @Deprecated
    public long insertarResiduo(Residuo residuo) {
        return insertarResiduo(residuo, 1); // Usuario por defecto ID 1
    }

    /**
     * @deprecated Usar obtenerResiduosPorUsuario(int) en su lugar
     */
    @Deprecated
    public List<Residuo> obtenerTodosResiduos() {
        return obtenerResiduosPorUsuario(1); // Usuario por defecto ID 1
    }

    /**
     * @deprecated Usar obtenerResiduosConFiltros(int, String, String, String) en su lugar
     */
    @Deprecated
    public List<Residuo> obtenerResiduosConFiltros(String fechaInicio, String fechaFin, String tipo) {
        return obtenerResiduosConFiltros(1, fechaInicio, fechaFin, tipo);
    }

    /**
     * @deprecated Usar obtenerResiduoPorId(int, int) en su lugar
     */
    @Deprecated
    public Residuo obtenerResiduoPorId(int id) {
        return obtenerResiduoPorId(id, 1);
    }

    /**
     * @deprecated Usar actualizarResiduo(Residuo, int) en su lugar
     */
    @Deprecated
    public int actualizarResiduo(Residuo residuo) {
        return actualizarResiduo(residuo, 1);
    }

    /**
     * @deprecated Usar marcarComoSincronizado(int, int) en su lugar
     */
    @Deprecated
    public int marcarComoSincronizado(int id) {
        return marcarComoSincronizado(id, 1);
    }

    /**
     * @deprecated Usar eliminarResiduo(int, int) en su lugar
     */
    @Deprecated
    public void eliminarResiduo(int id) {
        eliminarResiduo(id, 1);
    }
    /**
     * MÉTODO TEMPORAL PARA DEPURACIÓN
     * Verificar qué usuarios hay en la base de datos
     */
    public void verificarUsuarios() {
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery("SELECT * FROM " + TABLE_USUARIOS, null);

        android.util.Log.d("DB_DEBUG", "=== USUARIOS EN BD ===");
        android.util.Log.d("DB_DEBUG", "Total: " + cursor.getCount());

        while (cursor.moveToNext()) {
            String nombre = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_NOMBRE));
            String email = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_EMAIL));
            String pass = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_PASSWORD));
            android.util.Log.d("DB_DEBUG", "Usuario: " + nombre + " | Email: " + email + " | Pass: " + pass);
        }

        cursor.close();
        db.close();
    }
}

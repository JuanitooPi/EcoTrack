package com.example.ecotrack;

import android.content.Context;
import android.content.SharedPreferences;
import androidx.appcompat.app.AppCompatDelegate;

public class SessionManager {
    private static final String PREF_NAME = "EcoTrackSession";
    private static final String KEY_USUARIO_ID = "usuarioId";
    private static final String KEY_NOMBRE = "nombre";
    private static final String KEY_EMAIL = "email";
    private static final String KEY_ROL = "rol";
    private static final String KEY_FOTO = "foto";
    private static final String KEY_IS_LOGGED_IN = "isLoggedIn";
    private static final String KEY_THEME = "theme_mode";

    private SharedPreferences pref;
    private SharedPreferences.Editor editor;
    private Context context;

    public SessionManager(Context context) {
        this.context = context;
        pref = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
        editor = pref.edit();
    }

    public void crearSesion(Usuario usuario) {
        editor.putInt(KEY_USUARIO_ID, usuario.getId());
        editor.putString(KEY_NOMBRE, usuario.getNombre());
        editor.putString(KEY_EMAIL, usuario.getEmail());
        editor.putString(KEY_ROL, usuario.getRol());
        editor.putString(KEY_FOTO, usuario.getFoto());
        editor.putBoolean(KEY_IS_LOGGED_IN, true);
        editor.commit();
    }

    public Usuario getUsuario() {
        Usuario usuario = new Usuario();
        usuario.setId(pref.getInt(KEY_USUARIO_ID, -1));
        usuario.setNombre(pref.getString(KEY_NOMBRE, null));
        usuario.setEmail(pref.getString(KEY_EMAIL, null));
        usuario.setRol(pref.getString(KEY_ROL, null));
        usuario.setFoto(pref.getString(KEY_FOTO, null));
        return usuario;
    }

    public boolean isLoggedIn() {
        return pref.getBoolean(KEY_IS_LOGGED_IN, false);
    }

    public void cerrarSesion() {
        // Preservar el tema al cerrar sesión
        int theme = getThemeMode();
        editor.clear();
        editor.putInt(KEY_THEME, theme);
        editor.commit();
    }

    public void actualizarPerfil(String nombre, String email, String rol) {
        editor.putString(KEY_NOMBRE, nombre);
        editor.putString(KEY_EMAIL, email);
        editor.putString(KEY_ROL, rol);
        editor.commit();
    }

    public void actualizarFoto(String fotoUri) {
        editor.putString(KEY_FOTO, fotoUri);
        editor.commit();
    }

    public int getUsuarioId() {
        return pref.getInt(KEY_USUARIO_ID, -1);
    }

    public void setThemeMode(int mode) {
        editor.putInt(KEY_THEME, mode);
        editor.apply();
    }

    public int getThemeMode() {
        return pref.getInt(KEY_THEME, AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM);
    }

    public void applyTheme() {
        AppCompatDelegate.setDefaultNightMode(getThemeMode());
    }
}

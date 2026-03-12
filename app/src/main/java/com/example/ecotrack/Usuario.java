package com.example.ecotrack;

public class Usuario {
    private int id;
    private String nombre;
    private String email;
    private String rol;
    private String password;
    private String foto;

    public Usuario() {
    }

    public Usuario(String nombre, String email, String rol, String password) {
        this.nombre = nombre;
        this.email = email;
        this.rol = rol;
        this.password = password;
    }

    // Getters y Setters
    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public String getNombre() { return nombre; }
    public void setNombre(String nombre) { this.nombre = nombre; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public String getRol() { return rol; }
    public void setRol(String rol) { this.rol = rol; }

    public String getPassword() { return password; }
    public void setPassword(String password) { this.password = password; }

    public String getFoto() { return foto; }
    public void setFoto(String foto) { this.foto = foto; }
}
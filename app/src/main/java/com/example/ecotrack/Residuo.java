package com.example.ecotrack;

public class Residuo {
    private int id;
    private String tipo;
    private double cantidad;
    private String unidad;
    private String fecha;
    private String hora;
    private String ubicacion;
    private int sincronizado;

    // Tipos de residuos predefinidos
    public static final String[] TIPOS_RESIDUOS = {
            "Plástico",
            "Papel/Cartón",
            "Vidrio",
            "Metal",
            "Orgánico",
            "Peligroso",
            "Electrónico",
            "Textil"
    };

    // Unidades de medida
    public static final String[] UNIDADES = {
            "kg",
            "g",
            "lb",
            "m³",
            "unidades"
    };

    // Constructor vacío
    public Residuo() {
    }

    // Constructor con parámetros
    public Residuo(String tipo, double cantidad, String unidad, String fecha,
                   String hora, String ubicacion) {
        this.tipo = tipo;
        this.cantidad = cantidad;
        this.unidad = unidad;
        this.fecha = fecha;
        this.hora = hora;
        this.ubicacion = ubicacion;
        this.sincronizado = 0; // Por defecto no sincronizado
    }

    // Getters y Setters
    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getTipo() {
        return tipo;
    }

    public void setTipo(String tipo) {
        this.tipo = tipo;
    }

    public double getCantidad() {
        return cantidad;
    }

    public void setCantidad(double cantidad) {
        this.cantidad = cantidad;
    }

    public String getUnidad() {
        return unidad;
    }

    public void setUnidad(String unidad) {
        this.unidad = unidad;
    }

    public String getFecha() {
        return fecha;
    }

    public void setFecha(String fecha) {
        this.fecha = fecha;
    }

    public String getHora() {
        return hora;
    }

    public void setHora(String hora) {
        this.hora = hora;
    }

    public String getUbicacion() {
        return ubicacion;
    }

    public void setUbicacion(String ubicacion) {
        this.ubicacion = ubicacion;
    }

    public int getSincronizado() {
        return sincronizado;
    }

    public void setSincronizado(int sincronizado) {
        this.sincronizado = sincronizado;
    }

    @Override
    public String toString() {
        return tipo + " - " + cantidad + " " + unidad + " - " + fecha;
    }
}
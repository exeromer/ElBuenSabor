package com.powerRanger.ElBuenSabor.entities;

import jakarta.persistence.*;

import java.util.List;

@Entity
public class Categoria {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY) // <-- Añadido
    private Integer id;  // ID de la categoría

    @Column(nullable = true)
    private String denominacion;

    @OneToMany(mappedBy = "categoria", cascade = {CascadeType.PERSIST, CascadeType.MERGE}) // Relación bidireccional con Articulo
    private List<Articulo> articulos;  // Relación uno a muchos con Articulo

    // Campo 'estadoActivo' un boolean para ver si esta activo
    @Column(name = "estadoActivo")
    private Boolean estadoActivo;

    public String getDenominacion() {
        return denominacion;
    }

    public void setDenominacion(String denominacion) {
        this.denominacion = denominacion;
    }

    public List<Articulo> getArticulos() {
        return articulos;
    }

    public void setArticulos(List<Articulo> articulos) {
        this.articulos = articulos;
    }

    public Boolean getEstadoActivo() {
        return estadoActivo;
    }

    public void setEstadoActivo(Boolean estadoActivo) {
        this.estadoActivo = estadoActivo;
    }
}
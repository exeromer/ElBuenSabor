package com.powerRanger.ElBuenSabor.entities;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;

import java.util.List;
import jakarta.persistence.*;

@Entity
public class UnidadMedida {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY) // <-- Añadido
    private Integer id;  // ID de la unidad de medida

    private String denominacion;

    @OneToMany(mappedBy = "unidadMedida", cascade = CascadeType.ALL)
    private List<Articulo> articulos;

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

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }
}
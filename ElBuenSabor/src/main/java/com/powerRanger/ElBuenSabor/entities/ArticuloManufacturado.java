package com.powerRanger.ElBuenSabor.entities;

import jakarta.persistence.*;
import java.util.ArrayList; // Asegúrate de inicializar la lista
import java.util.List;

@Entity
public class ArticuloManufacturado extends Articulo {

    @Column(nullable = true)
    private String descripcion;

    @Column(nullable = true)
    private Integer tiempoEstimadoMinutos;

    @Column(nullable = true)
    private String preparacion;

    @OneToMany(mappedBy = "articuloManufacturado", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<ArticuloManufacturadoDetalle> manufacturadoDetalles = new ArrayList<>(); // Inicializado

    // Constructores
    public ArticuloManufacturado() {
        super();
    }

    public ArticuloManufacturado(String denominacion, Double precioVenta, UnidadMedida unidadMedida, Categoria categoria, Boolean estadoActivo, String descripcion, Integer tiempoEstimadoMinutos, String preparacion) {
        super(denominacion, precioVenta, unidadMedida, categoria, estadoActivo);
        this.descripcion = descripcion;
        this.tiempoEstimadoMinutos = tiempoEstimadoMinutos;
        this.preparacion = preparacion;
    }

    // Getters y Setters (ya los tenías)
    public String getDescripcion() {
        return descripcion;
    }

    public void setDescripcion(String descripcion) {
        this.descripcion = descripcion;
    }

    public Integer getTiempoEstimadoMinutos() {
        return tiempoEstimadoMinutos;
    }

    public void setTiempoEstimadoMinutos(Integer tiempoEstimadoMinutos) {
        this.tiempoEstimadoMinutos = tiempoEstimadoMinutos;
    }

    public String getPreparacion() {
        return preparacion;
    }

    public void setPreparacion(String preparacion) {
        this.preparacion = preparacion;
    }

    public List<ArticuloManufacturadoDetalle> getManufacturadoDetalles() {
        return manufacturadoDetalles;
    }

    public void setManufacturadoDetalles(List<ArticuloManufacturadoDetalle> manufacturadoDetalles) {
        this.manufacturadoDetalles = manufacturadoDetalles;
    }

    // equals, hashCode, toString (similar a ArticuloInsumo, hereda de Articulo)
    // Ejemplo de toString específico:
    @Override
    public String toString() {
        return "ArticuloManufacturado{" +
                "id=" + getId() +
                ", denominacion='" + getDenominacion() + '\'' +
                ", descripcion='" + descripcion + '\'' +
                ", tiempoEstimadoMinutos=" + tiempoEstimadoMinutos +
                '}';
    }

    public void addManufacturadoDetalle(ArticuloManufacturadoDetalle detalle) {
        if (this.manufacturadoDetalles == null) {
            this.manufacturadoDetalles = new ArrayList<>();
        }
        this.manufacturadoDetalles.add(detalle);
        detalle.setArticuloManufacturado(this); // Establece la relación bidireccional
    }

}
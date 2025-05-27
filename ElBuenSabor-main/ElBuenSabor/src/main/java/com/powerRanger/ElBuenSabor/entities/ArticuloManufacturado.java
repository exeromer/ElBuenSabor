package com.powerRanger.ElBuenSabor.entities;

import jakarta.persistence.*;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.util.ArrayList;
import java.util.List;
// com.fasterxml.jackson.annotation.JsonIdentityInfo se hereda de Articulo

@Entity
public class ArticuloManufacturado extends Articulo {

    @Column(length = 1000)
    @Size(max = 1000, message = "La descripción no puede exceder los 1000 caracteres")
    private String descripcion;

    @NotNull(message = "El tiempo estimado en minutos es obligatorio")
    @Min(value = 1, message = "El tiempo estimado debe ser de al menos 1 minuto")
    private Integer tiempoEstimadoMinutos;

    @Lob
    @Column(columnDefinition = "TEXT")
    @NotEmpty(message = "La preparación не puede estar vacía")
    private String preparacion;

    @OneToMany(mappedBy = "articuloManufacturado", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    private List<ArticuloManufacturadoDetalle> manufacturadoDetalles = new ArrayList<>();

    public ArticuloManufacturado() {
        super();
    }

    public ArticuloManufacturado(String denominacion, Double precioVenta, UnidadMedida unidadMedida,
                                 Categoria categoria, Boolean estadoActivo, String descripcion,
                                 Integer tiempoEstimadoMinutos, String preparacion) {
        super(denominacion, precioVenta, unidadMedida, categoria, estadoActivo);
        this.descripcion = descripcion;
        this.tiempoEstimadoMinutos = tiempoEstimadoMinutos;
        this.preparacion = preparacion;
    }

    // Getters y Setters
    public String getDescripcion() { return descripcion; }
    public void setDescripcion(String descripcion) { this.descripcion = descripcion; }
    public Integer getTiempoEstimadoMinutos() { return tiempoEstimadoMinutos; }
    public void setTiempoEstimadoMinutos(Integer tiempoEstimadoMinutos) { this.tiempoEstimadoMinutos = tiempoEstimadoMinutos; }
    public String getPreparacion() { return preparacion; }
    public void setPreparacion(String preparacion) { this.preparacion = preparacion; }
    public List<ArticuloManufacturadoDetalle> getManufacturadoDetalles() { return manufacturadoDetalles; }
    public void setManufacturadoDetalles(List<ArticuloManufacturadoDetalle> manufacturadoDetalles) { this.manufacturadoDetalles = manufacturadoDetalles; }

    // Métodos Helper
    public void addManufacturadoDetalle(ArticuloManufacturadoDetalle detalle) {
        if (this.manufacturadoDetalles == null) {
            this.manufacturadoDetalles = new ArrayList<>();
        }
        this.manufacturadoDetalles.add(detalle);
        detalle.setArticuloManufacturado(this);
    }

    public void removeManufacturadoDetalle(ArticuloManufacturadoDetalle detalle) {
        if (this.manufacturadoDetalles != null) {
            this.manufacturadoDetalles.remove(detalle);
            detalle.setArticuloManufacturado(null);
        }
    }

    @Override
    public String toString() {
        return "ArticuloManufacturado{" +
                "id=" + getId() +
                ", denominacion='" + getDenominacion() + '\'' +
                ", descripcion='" + descripcion + '\'' +
                ", tiempoEstimadoMinutos=" + tiempoEstimadoMinutos +
                ", numDetalles=" + (manufacturadoDetalles != null ? manufacturadoDetalles.size() : 0) +
                '}';
    }
}
package com.powerRanger.ElBuenSabor.dtos;

import com.fasterxml.jackson.annotation.JsonTypeName;
import java.util.List;
import java.util.ArrayList;

@JsonTypeName("manufacturado") // Coincide con el 'name' en @JsonSubTypes
public class ArticuloManufacturadoResponseDTO extends ArticuloBaseResponseDTO {
    private String descripcion;
    private Integer tiempoEstimadoMinutos;
    private String preparacion;
    private List<ArticuloManufacturadoDetalleResponseDTO> manufacturadoDetalles = new ArrayList<>();
    private Integer unidadesDisponiblesCalculadas;

    // Getters y Setters
    public String getDescripcion() { return descripcion; }
    public void setDescripcion(String descripcion) { this.descripcion = descripcion; }
    public Integer getTiempoEstimadoMinutos() { return tiempoEstimadoMinutos; }
    public void setTiempoEstimadoMinutos(Integer tiempoEstimadoMinutos) { this.tiempoEstimadoMinutos = tiempoEstimadoMinutos; }
    public String getPreparacion() { return preparacion; }
    public void setPreparacion(String preparacion) { this.preparacion = preparacion; }
    public List<ArticuloManufacturadoDetalleResponseDTO> getManufacturadoDetalles() { return manufacturadoDetalles; }
    public void setManufacturadoDetalles(List<ArticuloManufacturadoDetalleResponseDTO> manufacturadoDetalles) { this.manufacturadoDetalles = manufacturadoDetalles; }
    public Integer getUnidadesDisponiblesCalculadas() {return unidadesDisponiblesCalculadas;}
    public void setUnidadesDisponiblesCalculadas(Integer unidadesDisponiblesCalculadas) {this.unidadesDisponiblesCalculadas = unidadesDisponiblesCalculadas;}
}
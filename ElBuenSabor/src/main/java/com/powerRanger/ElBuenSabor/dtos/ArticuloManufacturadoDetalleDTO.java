package com.powerRanger.ElBuenSabor.dtos;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

public class ArticuloManufacturadoDetalleDTO {

    @NotNull(message = "La cantidad es obligatoria para el detalle")
    @Min(value = 1, message = "La cantidad debe ser al menos 1") // O Double y @DecimalMin("0.001")
    private Double cantidad;

    @NotNull(message = "El ID del Artículo Insumo es obligatorio para el detalle")
    private Integer articuloInsumoId;

    // El estado activo podría ser opcional en el DTO y default a true en el servicio
    private Boolean estadoActivo = true;

    // Getters y Setters
    public Double getCantidad() { return cantidad; }
    public void setCantidad(Double cantidad) { this.cantidad = cantidad; }
    public Integer getArticuloInsumoId() { return articuloInsumoId; }
    public void setArticuloInsumoId(Integer articuloInsumoId) { this.articuloInsumoId = articuloInsumoId; }
    public Boolean getEstadoActivo() { return estadoActivo; }
    public void setEstadoActivo(Boolean estadoActivo) { this.estadoActivo = estadoActivo; }
}
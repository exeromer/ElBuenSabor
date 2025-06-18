package com.powerRanger.ElBuenSabor.dtos;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

public class PromocionDetalleRequestDTO {
    @NotNull(message = "La cantidad es obligatoria")
    @Min(value = 1, message = "La cantidad debe ser al menos 1")
    private Integer cantidad;

    @NotNull(message = "El ID del artículo es obligatorio")
    private Integer articuloId; // ID del Articulo (puede ser Insumo o Manufacturado)

    // Getters y Setters
    public Integer getCantidad() { return cantidad; }
    public void setCantidad(Integer cantidad) { this.cantidad = cantidad; }
    public Integer getArticuloId() { return articuloId; }
    public void setArticuloId(Integer articuloId) { this.articuloId = articuloId; }
}
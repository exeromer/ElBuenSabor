package com.powerRanger.ElBuenSabor.dtos;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

public class StockInsumoSucursalRequestDTO {
    @NotNull(message = "El ID del artículo insumo es obligatorio")
    private Integer articuloInsumoId;

    @NotNull(message = "El ID de la sucursal es obligatorio")
    private Integer sucursalId;

    @NotNull(message = "El stock actual no puede ser nulo")
    @Min(value = 0, message = "El stock actual no puede ser negativo")
    private Double stockActual;

    @NotNull(message = "El stock mínimo no puede ser nulo")
    @Min(value = 0, message = "El stock mínimo no puede ser negativo")
    private Double stockMinimo;

    // Getters y Setters
    public Integer getArticuloInsumoId() {
        return articuloInsumoId;
    }

    public void setArticuloInsumoId(Integer articuloInsumoId) {
        this.articuloInsumoId = articuloInsumoId;
    }

    public Integer getSucursalId() {
        return sucursalId;
    }

    public void setSucursalId(Integer sucursalId) {
        this.sucursalId = sucursalId;
    }

    public Double getStockActual() {
        return stockActual;
    }

    public void setStockActual(Double stockActual) {
        this.stockActual = stockActual;
    }

    public Double getStockMinimo() {
        return stockMinimo;
    }

    public void setStockMinimo(Double stockMinimo) {
        this.stockMinimo = stockMinimo;
    }
}
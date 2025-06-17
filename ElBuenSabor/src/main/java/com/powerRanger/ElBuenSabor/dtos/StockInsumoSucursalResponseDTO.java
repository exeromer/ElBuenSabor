package com.powerRanger.ElBuenSabor.dtos;

public class StockInsumoSucursalResponseDTO {
    private Integer id;
    private Integer articuloInsumoId;
    private String articuloInsumoDenominacion;
    private Integer sucursalId;
    private String sucursalNombre;
    private Double stockActual;
    private Double stockMinimo;

    // Getters y Setters
    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public Integer getArticuloInsumoId() {
        return articuloInsumoId;
    }

    public void setArticuloInsumoId(Integer articuloInsumoId) {
        this.articuloInsumoId = articuloInsumoId;
    }

    public String getArticuloInsumoDenominacion() {
        return articuloInsumoDenominacion;
    }

    public void setArticuloInsumoDenominacion(String articuloInsumoDenominacion) {
        this.articuloInsumoDenominacion = articuloInsumoDenominacion;
    }

    public Integer getSucursalId() {
        return sucursalId;
    }

    public void setSucursalId(Integer sucursalId) {
        this.sucursalId = sucursalId;
    }

    public String getSucursalNombre() {
        return sucursalNombre;
    }

    public void setSucursalNombre(String sucursalNombre) {
        this.sucursalNombre = sucursalNombre;
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
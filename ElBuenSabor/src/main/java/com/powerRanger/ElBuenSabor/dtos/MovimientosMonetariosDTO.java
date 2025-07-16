package com.powerRanger.ElBuenSabor.dtos;

public class MovimientosMonetariosDTO {
    private Double ingresosTotales;
    private Double costosTotales;
    private Double gananciasNetas;

    public MovimientosMonetariosDTO() {
    }

    public MovimientosMonetariosDTO(Double ingresosTotales, Double costosTotales, Double gananciasNetas) {
        this.ingresosTotales = ingresosTotales;
        this.costosTotales = costosTotales;
        this.gananciasNetas = gananciasNetas;
    }

    // Getters and Setters
    public Double getIngresosTotales() {
        return ingresosTotales;
    }

    public void setIngresosTotales(Double ingresosTotales) {
        this.ingresosTotales = ingresosTotales;
    }

    public Double getCostosTotales() {
        return costosTotales;
    }

    public void setCostosTotales(Double costosTotales) {
        this.costosTotales = costosTotales;
    }

    public Double getGananciasNetas() {
        return gananciasNetas;
    }

    public void setGananciasNetas(Double gananciasNetas) {
        this.gananciasNetas = gananciasNetas;
    }
}
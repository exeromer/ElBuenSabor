package com.powerRanger.ElBuenSabor.dtos;

public class ArticuloManufacturadoRankingDTO {
    private Integer articuloId;
    private String denominacion;
    private Long cantidadVendida;

    public ArticuloManufacturadoRankingDTO(Integer articuloId, String denominacion, Long cantidadVendida) {
        this.articuloId = articuloId;
        this.denominacion = denominacion;
        this.cantidadVendida = cantidadVendida;
    }

    public Integer getArticuloId() {
        return articuloId;
    }

    public void setArticuloId(Integer articuloId) {
        this.articuloId = articuloId;
    }

    public String getDenominacion() {
        return denominacion;
    }

    public void setDenominacion(String denominacion) {
        this.denominacion = denominacion;
    }

    public Long getCantidadVendida() {
        return cantidadVendida;
    }

    public void setCantidadVendida(Long cantidadVendida) {
        this.cantidadVendida = cantidadVendida;
    }
}
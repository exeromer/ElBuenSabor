package com.powerRanger.ElBuenSabor.dtos;

public class ArticuloManufacturadoDetalleResponseDTO {
    private Integer id;
    private Double cantidad;
    private ArticuloSimpleResponseDTO articuloInsumo; // Insumo referenciado
    private Boolean estadoActivo; // Si tu entidad Detalle lo tiene

    // Constructor, Getters y Setters
    public ArticuloManufacturadoDetalleResponseDTO() {}

    public Integer getId() { return id; }
    public void setId(Integer id) { this.id = id; }
    public Double getCantidad() { return cantidad; }
    public void setCantidad(Double cantidad) { this.cantidad = cantidad; }
    public ArticuloSimpleResponseDTO getArticuloInsumo() { return articuloInsumo; }
    public void setArticuloInsumo(ArticuloSimpleResponseDTO articuloInsumo) { this.articuloInsumo = articuloInsumo; }
    public Boolean getEstadoActivo() { return estadoActivo; }
    public void setEstadoActivo(Boolean estadoActivo) { this.estadoActivo = estadoActivo; }
}
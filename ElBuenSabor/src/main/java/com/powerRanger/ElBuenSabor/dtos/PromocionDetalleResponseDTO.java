package com.powerRanger.ElBuenSabor.dtos;

public class PromocionDetalleResponseDTO {
    private Integer id;
    private Integer cantidad;
    private ArticuloSimpleResponseDTO articulo; // Usamos el DTO simple del artículo

    // Getters y Setters
    public Integer getId() { return id; }
    public void setId(Integer id) { this.id = id; }
    public Integer getCantidad() { return cantidad; }
    public void setCantidad(Integer cantidad) { this.cantidad = cantidad; }
    public ArticuloSimpleResponseDTO getArticulo() { return articulo; }
    public void setArticulo(ArticuloSimpleResponseDTO articulo) { this.articulo = articulo; }
}
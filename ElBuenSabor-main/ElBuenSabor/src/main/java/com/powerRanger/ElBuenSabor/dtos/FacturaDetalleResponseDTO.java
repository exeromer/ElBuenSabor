package com.powerRanger.ElBuenSabor.dtos;

// Asumimos que ArticuloSimpleResponseDTO ya existe
// import com.powerRanger.ElBuenSabor.dtos.ArticuloSimpleResponseDTO;

public class FacturaDetalleResponseDTO {
    private Integer id;
    private Integer cantidad;
    private String denominacionArticulo;
    private Double precioUnitarioArticulo;
    private Double subTotal;
    private ArticuloSimpleResponseDTO articulo; // Información básica del artículo original

    // Getters y Setters
    public Integer getId() { return id; }
    public void setId(Integer id) { this.id = id; }
    public Integer getCantidad() { return cantidad; }
    public void setCantidad(Integer cantidad) { this.cantidad = cantidad; }
    public String getDenominacionArticulo() { return denominacionArticulo; }
    public void setDenominacionArticulo(String denominacionArticulo) { this.denominacionArticulo = denominacionArticulo; }
    public Double getPrecioUnitarioArticulo() { return precioUnitarioArticulo; }
    public void setPrecioUnitarioArticulo(Double precioUnitarioArticulo) { this.precioUnitarioArticulo = precioUnitarioArticulo; }
    public Double getSubTotal() { return subTotal; }
    public void setSubTotal(Double subTotal) { this.subTotal = subTotal; }
    public ArticuloSimpleResponseDTO getArticulo() { return articulo; }
    public void setArticulo(ArticuloSimpleResponseDTO articulo) { this.articulo = articulo; }
}
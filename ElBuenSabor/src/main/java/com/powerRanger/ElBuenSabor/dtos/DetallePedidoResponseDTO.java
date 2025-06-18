package com.powerRanger.ElBuenSabor.dtos;

public class DetallePedidoResponseDTO {
    private Integer id;
    private Integer cantidad;
    private Double subTotal;
    private ArticuloSimpleResponseDTO articulo;
    private Integer promocionAplicadaId;
    private Double descuentoAplicadoPorPromocion;

    // Getters y Setters
    public Integer getId() { return id; }
    public void setId(Integer id) { this.id = id; }
    public Integer getCantidad() { return cantidad; }
    public void setCantidad(Integer cantidad) { this.cantidad = cantidad; }
    public Double getSubTotal() { return subTotal; }
    public void setSubTotal(Double subTotal) { this.subTotal = subTotal; }
    public ArticuloSimpleResponseDTO getArticulo() { return articulo; }
    public void setArticulo(ArticuloSimpleResponseDTO articulo) { this.articulo = articulo; }
    public Integer getPromocionAplicadaId() {return promocionAplicadaId;}
    public void setPromocionAplicadaId(Integer promocionAplicadaId) {this.promocionAplicadaId = promocionAplicadaId;}
    public Double getDescuentoAplicadoPorPromocion() {return descuentoAplicadoPorPromocion;}
    public void setDescuentoAplicadoPorPromocion(Double descuentoAplicadoPorPromocion) {this.descuentoAplicadoPorPromocion = descuentoAplicadoPorPromocion;}
}
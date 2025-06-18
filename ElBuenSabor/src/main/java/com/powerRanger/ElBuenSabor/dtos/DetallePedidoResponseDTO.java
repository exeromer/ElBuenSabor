package com.powerRanger.ElBuenSabor.dtos;

// Asumimos que ya tienes ArticuloSimpleResponseDTO
// import com.powerRanger.ElBuenSabor.dtos.ArticuloSimpleResponseDTO;

public class DetallePedidoResponseDTO {
    private Integer id;
    private Integer cantidad;
    private Double subTotal;
    private ArticuloSimpleResponseDTO articulo; // Para mostrar info básica del artículo

    // Getters y Setters
    public Integer getId() { return id; }
    public void setId(Integer id) { this.id = id; }
    public Integer getCantidad() { return cantidad; }
    public void setCantidad(Integer cantidad) { this.cantidad = cantidad; }
    public Double getSubTotal() { return subTotal; }
    public void setSubTotal(Double subTotal) { this.subTotal = subTotal; }
    public ArticuloSimpleResponseDTO getArticulo() { return articulo; }
    public void setArticulo(ArticuloSimpleResponseDTO articulo) { this.articulo = articulo; }
}
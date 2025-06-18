package com.powerRanger.ElBuenSabor.dtos;

public class CarritoItemResponseDTO {
    private Long id;
    private Integer articuloId;
    private String articuloDenominacion;
    private Integer cantidad;
    private Double precioUnitarioAlAgregar;
    private Double subtotalItem;
    private Integer promocionAplicadaId;
    private Double descuentoAplicadoPorPromocion; // Monto del descuento que se aplicó a este ítem

    public CarritoItemResponseDTO() {
    }

    // Getters y Setters
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Integer getArticuloId() {
        return articuloId;
    }

    public void setArticuloId(Integer articuloId) {
        this.articuloId = articuloId;
    }

    public String getArticuloDenominacion() {
        return articuloDenominacion;
    }

    public void setArticuloDenominacion(String articuloDenominacion) {
        this.articuloDenominacion = articuloDenominacion;
    }

    public Integer getCantidad() {
        return cantidad;
    }

    public void setCantidad(Integer cantidad) {
        this.cantidad = cantidad;
    }

    public Double getPrecioUnitarioAlAgregar() {
        return precioUnitarioAlAgregar;
    }

    public void setPrecioUnitarioAlAgregar(Double precioUnitarioAlAgregar) {
        this.precioUnitarioAlAgregar = precioUnitarioAlAgregar;
    }

    public Double getSubtotalItem() {
        return subtotalItem;
    }

    public void setSubtotalItem(Double subtotalItem) {
        this.subtotalItem = subtotalItem;
    }

    public Integer getPromocionAplicadaId() {
        return promocionAplicadaId;
    }

    public void setPromocionAplicadaId(Integer promocionAplicadaId) {
        this.promocionAplicadaId = promocionAplicadaId;
    }

    public Double getDescuentoAplicadoPorPromocion() {
        return descuentoAplicadoPorPromocion;
    }

    public void setDescuentoAplicadoPorPromocion(Double descuentoAplicadoPorPromocion) {
        this.descuentoAplicadoPorPromocion = descuentoAplicadoPorPromocion;
    }
}
package com.powerRanger.ElBuenSabor.dtos;

public class ArticuloSimpleResponseDTO {
    private Integer id;
    private String denominacion;
    private Double precioVenta;

    // Constructor, Getters y Setters
    public ArticuloSimpleResponseDTO() {}

    public ArticuloSimpleResponseDTO(Integer id, String denominacion, Double precioVenta) {
        this.id = id;
        this.denominacion = denominacion;
        this.precioVenta = precioVenta;
    }

    public Integer getId() { return id; }
    public void setId(Integer id) { this.id = id; }
    public String getDenominacion() { return denominacion; }
    public void setDenominacion(String denominacion) { this.denominacion = denominacion; }
    public Double getPrecioVenta() { return precioVenta; }
    public void setPrecioVenta(Double precioVenta) { this.precioVenta = precioVenta; }
}
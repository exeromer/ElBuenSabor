package com.powerRanger.ElBuenSabor.dtos;

import com.fasterxml.jackson.annotation.JsonTypeName;

@JsonTypeName("insumo") // Coincide con el 'name' en @JsonSubTypes
public class ArticuloInsumoResponseDTO extends ArticuloBaseResponseDTO {
    private Double precioCompra;
    private Double stockActual;
    private Double stockMinimo;
    private Boolean esParaElaborar;

    // Getters y Setters
    public Double getPrecioCompra() { return precioCompra; }
    public void setPrecioCompra(Double precioCompra) { this.precioCompra = precioCompra; }
    public Double getStockActual() { return stockActual; }
    public void setStockActual(Double stockActual) { this.stockActual = stockActual; }
    public Double getstockMinimo() { return stockMinimo; }
    public void setstockMinimo(Double stockMinimo) { this.stockMinimo = stockMinimo; }
    public Boolean getEsParaElaborar() { return esParaElaborar; }
    public void setEsParaElaborar(Boolean esParaElaborar) { this.esParaElaborar = esParaElaborar; }
}
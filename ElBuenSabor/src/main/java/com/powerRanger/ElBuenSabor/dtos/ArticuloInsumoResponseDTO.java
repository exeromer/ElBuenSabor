package com.powerRanger.ElBuenSabor.dtos;

import com.fasterxml.jackson.annotation.JsonTypeName;

@JsonTypeName("insumo") // Coincide con el 'name' en @JsonSubTypes
public class ArticuloInsumoResponseDTO extends ArticuloBaseResponseDTO {
    private Double precioCompra;
    private Boolean esParaElaborar;

    public Double getPrecioCompra() { return precioCompra; }
    public void setPrecioCompra(Double precioCompra) { this.precioCompra = precioCompra; }
    public Boolean getEsParaElaborar() { return esParaElaborar; }
    public void setEsParaElaborar(Boolean esParaElaborar) { this.esParaElaborar = esParaElaborar; }
}
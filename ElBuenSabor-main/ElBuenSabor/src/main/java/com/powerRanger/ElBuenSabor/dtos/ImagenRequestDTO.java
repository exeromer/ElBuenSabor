package com.powerRanger.ElBuenSabor.dtos;

import jakarta.validation.constraints.NotEmpty;

public class ImagenRequestDTO {

    @NotEmpty(message = "La denominación (URL o nombre de archivo) no puede estar vacía")
    private String denominacion;

    private Integer articuloId;   // ID del Artículo al que se asocia (opcional)
    private Integer promocionId;  // ID de la Promoción a la que se asocia (opcional)
    private Boolean estadoActivo = true; // Default

    // Getters y Setters
    public String getDenominacion() { return denominacion; }
    public void setDenominacion(String denominacion) { this.denominacion = denominacion; }
    public Integer getArticuloId() { return articuloId; }
    public void setArticuloId(Integer articuloId) { this.articuloId = articuloId; }
    public Integer getPromocionId() { return promocionId; }
    public void setPromocionId(Integer promocionId) { this.promocionId = promocionId; }
    public Boolean getEstadoActivo() { return estadoActivo; }
    public void setEstadoActivo(Boolean estadoActivo) { this.estadoActivo = estadoActivo; }
}
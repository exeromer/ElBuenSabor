package com.powerRanger.ElBuenSabor.dtos;

public class ImagenResponseDTO {
    private Integer id;
    private String denominacion; // URL o nombre de archivo
    private Boolean estadoActivo;
    private Integer articuloId;   // ID del Artículo asociado (puede ser null)
    private String articuloDenominacion; // Nombre del Artículo asociado (opcional, para conveniencia)
    private Integer promocionId;  // ID de la Promoción asociada (puede ser null)
    private String promocionDenominacion; // Nombre de la Promoción asociada (opcional)

    // Getters y Setters
    public Integer getId() { return id; }
    public void setId(Integer id) { this.id = id; }
    public String getDenominacion() { return denominacion; }
    public void setDenominacion(String denominacion) { this.denominacion = denominacion; }
    public Boolean getEstadoActivo() { return estadoActivo; }
    public void setEstadoActivo(Boolean estadoActivo) { this.estadoActivo = estadoActivo; }
    public Integer getArticuloId() { return articuloId; }
    public void setArticuloId(Integer articuloId) { this.articuloId = articuloId; }
    public String getArticuloDenominacion() { return articuloDenominacion; }
    public void setArticuloDenominacion(String articuloDenominacion) { this.articuloDenominacion = articuloDenominacion; }
    public Integer getPromocionId() { return promocionId; }
    public void setPromocionId(Integer promocionId) { this.promocionId = promocionId; }
    public String getPromocionDenominacion() { return promocionDenominacion; }
    public void setPromocionDenominacion(String promocionDenominacion) { this.promocionDenominacion = promocionDenominacion; }
}
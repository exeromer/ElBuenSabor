package com.powerRanger.ElBuenSabor.dtos;

import java.util.List; // Lo mantendremos por si decidimos añadir IDs de artículos

public class CategoriaResponseDTO {
    private Integer id;
    private String denominacion;
    private Boolean estadoActivo;
    // Opcional: Podríamos incluir una lista de IDs de artículos o DTOs de artículos aquí
    // private List<Integer> articuloIds;
    // private List<ArticuloSimpleResponseDTO> articulos;


    // Getters y Setters
    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public String getDenominacion() {
        return denominacion;
    }

    public void setDenominacion(String denominacion) {
        this.denominacion = denominacion;
    }

    public Boolean getEstadoActivo() {
        return estadoActivo;
    }

    public void setEstadoActivo(Boolean estadoActivo) {
        this.estadoActivo = estadoActivo;
    }

    // Si decides añadir la lista de artículos:
    // public List<Integer> getArticuloIds() { return articuloIds; }
    // public void setArticuloIds(List<Integer> articuloIds) { this.articuloIds = articuloIds; }
}
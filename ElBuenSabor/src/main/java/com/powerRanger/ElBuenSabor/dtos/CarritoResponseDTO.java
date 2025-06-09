package com.powerRanger.ElBuenSabor.dtos;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class CarritoResponseDTO {

    private Integer id;
    private Integer clienteId;
    private LocalDateTime fechaCreacion;
    private LocalDateTime fechaUltimaModificacion;
    private List<CarritoItemResponseDTO> items = new ArrayList<>();
    private Double totalCarrito;

    // Getters y Setters
    public Integer getId() {
        return id;
    }

    public void setId(Integer id) { // <-- CAMBIO AQUÍ: de Long a Integer
        this.id = id;
    }

    public Integer getClienteId() {
        return clienteId;
    }

    public void setClienteId(Integer clienteId) {
        this.clienteId = clienteId;
    }

    public LocalDateTime getFechaCreacion() {
        return fechaCreacion;
    }

    public void setFechaCreacion(LocalDateTime fechaCreacion) {
        this.fechaCreacion = fechaCreacion;
    }

    public LocalDateTime getFechaUltimaModificacion() {
        return fechaUltimaModificacion;
    }

    public void setFechaUltimaModificacion(LocalDateTime fechaUltimaModificacion) {
        this.fechaUltimaModificacion = fechaUltimaModificacion;
    }

    public List<CarritoItemResponseDTO> getItems() {
        return items;
    }

    public void setItems(List<CarritoItemResponseDTO> items) {
        this.items = items;
    }

    public Double getTotalCarrito() {
        return totalCarrito;
    }

    public void setTotalCarrito(Double totalCarrito) {
        this.totalCarrito = totalCarrito;
    }
}
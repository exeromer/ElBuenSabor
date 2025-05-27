package com.powerRanger.ElBuenSabor.dtos;

import java.time.LocalDateTime;
import java.util.List;
import java.util.ArrayList;

public class CarritoResponseDTO {
    private Long id; // ID del Carrito
    private Integer clienteId; // Asumiendo que siempre hay un cliente asociado
    private LocalDateTime fechaCreacion;
    private LocalDateTime fechaUltimaModificacion;
    private List<CarritoItemResponseDTO> items = new ArrayList<>();
    private Double totalCarrito; // Calculado: suma de todos los subtotalItem

    public CarritoResponseDTO() {
    }

    // Getters y Setters
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
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
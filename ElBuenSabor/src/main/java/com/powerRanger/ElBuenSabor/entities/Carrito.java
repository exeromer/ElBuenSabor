package com.powerRanger.ElBuenSabor.entities;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

@Entity
@Table(name = "carrito")
public class Carrito extends BaseEntity { // HEREDA DE BaseEntity Y USA ID INTEGER

    @NotNull(message = "El cliente es obligatorio para el carrito")
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "cliente_id", nullable = false)
    private Cliente cliente;

    @Column(name = "fecha_creacion", nullable = false)
    private LocalDateTime fechaCreacion;

    @Column(name = "fecha_ultima_modificacion", nullable = false)
    private LocalDateTime fechaUltimaModificacion;

    @OneToMany(mappedBy = "carrito", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    private List<CarritoItem> items = new ArrayList<>();

    public Carrito() {
        this.fechaCreacion = LocalDateTime.now();
        this.fechaUltimaModificacion = LocalDateTime.now();
    }

    // Getters y Setters
    public Cliente getCliente() {
        return cliente;
    }

    public void setCliente(Cliente cliente) {
        this.cliente = cliente;
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

    public List<CarritoItem> getItems() {
        return items;
    }

    public void setItems(List<CarritoItem> items) {
        this.items = items;
    }

    // Métodos Helper
    public void addItem(CarritoItem item) {
        items.add(item);
        item.setCarrito(this);
        this.fechaUltimaModificacion = LocalDateTime.now();
    }

    public void removeItem(CarritoItem item) {
        items.remove(item);
        item.setCarrito(null);
        this.fechaUltimaModificacion = LocalDateTime.now();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Carrito carrito = (Carrito) o;
        return Objects.equals(this.getId(), carrito.getId());
    }

    @Override
    public int hashCode() {
        return Objects.hash(this.getId());
    }

    @PreUpdate
    protected void onUpdate() {
        this.fechaUltimaModificacion = LocalDateTime.now();
    }
}
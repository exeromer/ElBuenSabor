package com.powerRanger.ElBuenSabor.entities;

import jakarta.persistence.*;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import java.util.Objects;

@Entity
@Table(name = "carrito_item")
public class CarritoItem {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id; // Usar Long para IDs

    @NotNull(message = "El carrito es obligatorio para el ítem")
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "carrito_id", nullable = false)
    private Carrito carrito;

    @NotNull(message = "El artículo es obligatorio para el ítem del carrito")
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "articulo_id", nullable = false)
    private Articulo articulo;

    @NotNull(message = "La cantidad es obligatoria")
    @Min(value = 1, message = "La cantidad debe ser al menos 1")
    @Column(nullable = false)
    private Integer cantidad;

    @NotNull(message = "El precio unitario al agregar es obligatorio")
    @Column(name = "precio_unitario_al_agregar", nullable = false)
    private Double precioUnitarioAlAgregar; // Precio del artículo al momento de agregarlo

    public CarritoItem() {
    }

    // Getters y Setters
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Carrito getCarrito() {
        return carrito;
    }

    public void setCarrito(Carrito carrito) {
        this.carrito = carrito;
    }

    public Articulo getArticulo() {
        return articulo;
    }

    public void setArticulo(Articulo articulo) {
        this.articulo = articulo;
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

    // Subtotal calculado (no persistido, se calcula al vuelo o en DTOs/Servicio)
    @Transient // Indica a JPA que no persista este campo
    public Double getSubtotalItem() {
        if (this.cantidad != null && this.precioUnitarioAlAgregar != null) {
            return this.cantidad * this.precioUnitarioAlAgregar;
        }
        return 0.0;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        CarritoItem that = (CarritoItem) o;
        return Objects.equals(id, that.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }
}
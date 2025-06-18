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

    // Nuevo campo: Relación con la promoción aplicada a este ítem de carrito
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "promocion_aplicada_id", nullable = true) // Puede ser nulo si no hay promoción
    private Promocion promocionAplicada;

    // Nuevo campo: Monto del descuento aplicado a este ítem por la promoción
    @Column(name = "descuento_aplicado_por_promocion", nullable = true) // Puede ser nulo o 0.0
    private Double descuentoAplicadoPorPromocion = 0.0; // Inicializar a 0.0 por defecto

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

    // Getters y Setters para los nuevos campos de promoción
    public Promocion getPromocionAplicada() {
        return promocionAplicada;
    }

    public void setPromocionAplicada(Promocion promocionAplicada) {
        this.promocionAplicada = promocionAplicada;
    }

    public Double getDescuentoAplicadoPorPromocion() {
        return descuentoAplicadoPorPromocion;
    }

    public void setDescuentoAplicadoPorPromocion(Double descuentoAplicadoPorPromocion) {
        this.descuentoAplicadoPorPromocion = descuentoAplicadoPorPromocion;
    }

    // Subtotal calculado (ahora debe considerar el descuento por promoción)
    @Transient // Indica a JPA que no persista este campo
    public Double getSubtotalItem() {
        if (this.cantidad != null && this.precioUnitarioAlAgregar != null) {
            double subtotal = this.cantidad * this.precioUnitarioAlAgregar;
            // Si hay un descuento aplicado por promoción para este ítem, restarlo del subtotal
            if (this.descuentoAplicadoPorPromocion != null) {
                subtotal -= this.descuentoAplicadoPorPromocion;
            }
            return subtotal;
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
package com.powerRanger.ElBuenSabor.entities;

import com.fasterxml.jackson.annotation.JsonIdentityInfo;
import com.fasterxml.jackson.annotation.ObjectIdGenerators;
import jakarta.persistence.*;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import java.util.Objects;

@Entity
@Table(name = "factura_detalle")
@JsonIdentityInfo(
        generator = ObjectIdGenerators.PropertyGenerator.class,
        property = "id")
public class FacturaDetalle extends BaseEntity { // HEREDA DE BaseEntity

    @NotNull(message = "La cantidad es obligatoria")
    @Min(value = 1, message = "La cantidad debe ser al menos 1")
    private Integer cantidad;

    @Column(name = "denominacion_articulo", nullable = false)
    @NotEmpty(message = "La denominación del artículo en la factura no puede estar vacía")
    private String denominacionArticulo;

    @Column(name = "precio_unitario_articulo", nullable = false)
    @NotNull(message = "El precio unitario del artículo en la factura es obligatorio")
    @DecimalMin(value = "0.0", message = "El precio unitario no puede ser negativo")
    private Double precioUnitarioArticulo;

    @Column(name = "subtotal", nullable = false)
    @NotNull(message = "El subtotal del detalle de factura es obligatorio")
    @DecimalMin(value = "0.0", message = "El subtotal no puede ser negativo")
    private Double subTotal;

    @NotNull(message = "La factura es obligatoria para el detalle")
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "factura_id", nullable = false)
    private Factura factura;

    @ManyToOne(fetch = FetchType.LAZY, optional = true)
    @JoinColumn(name = "articulo_id", nullable = true)
    private Articulo articulo;

    public FacturaDetalle() {
    }

    // Getters y Setters
    public Integer getCantidad() { return cantidad; }
    public void setCantidad(Integer cantidad) { this.cantidad = cantidad; }
    public String getDenominacionArticulo() { return denominacionArticulo; }
    public void setDenominacionArticulo(String denominacionArticulo) { this.denominacionArticulo = denominacionArticulo; }
    public Double getPrecioUnitarioArticulo() { return precioUnitarioArticulo; }
    public void setPrecioUnitarioArticulo(Double precioUnitarioArticulo) { this.precioUnitarioArticulo = precioUnitarioArticulo; }
    public Double getSubTotal() { return subTotal; }
    public void setSubTotal(Double subTotal) { this.subTotal = subTotal; }
    public Factura getFactura() { return factura; }
    public void setFactura(Factura factura) { this.factura = factura; }
    public Articulo getArticulo() { return articulo; }
    public void setArticulo(Articulo articulo) { this.articulo = articulo; }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        FacturaDetalle that = (FacturaDetalle) o;
        return Objects.equals(this.getId(), that.getId());
    }

    @Override
    public int hashCode() { return Objects.hash(this.getId()); }

    @Override
    public String toString() {
        return "FacturaDetalle{" + "id=" + this.getId() + ", denominacionArticulo='" + denominacionArticulo + '\'' + '}';
    }
}
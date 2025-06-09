package com.powerRanger.ElBuenSabor.entities;

import com.fasterxml.jackson.annotation.JsonIdentityInfo;
import com.fasterxml.jackson.annotation.ObjectIdGenerators;
import jakarta.persistence.*;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import java.util.Objects;

@Entity
@Table(name = "promocion_detalle")
@JsonIdentityInfo(
        generator = ObjectIdGenerators.PropertyGenerator.class,
        property = "id")
public class PromocionDetalle extends BaseEntity { // HEREDA DE BaseEntity

    @NotNull(message = "La cantidad es obligatoria")
    @Min(value = 1, message = "La cantidad debe ser al menos 1")
    private Integer cantidad;

    @NotNull(message = "La promoción es obligatoria para el detalle")
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "promocion_id", nullable = false)
    private Promocion promocion;

    @NotNull(message = "El artículo es obligatorio para el detalle")
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "articulo_id", nullable = false)
    private Articulo articulo;

    public PromocionDetalle() {
    }

    // Getters y Setters
    public Integer getCantidad() { return cantidad; }
    public void setCantidad(Integer cantidad) { this.cantidad = cantidad; }
    public Promocion getPromocion() { return promocion; }
    public void setPromocion(Promocion promocion) { this.promocion = promocion; }
    public Articulo getArticulo() { return articulo; }
    public void setArticulo(Articulo articulo) { this.articulo = articulo; }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        PromocionDetalle that = (PromocionDetalle) o;
        return Objects.equals(this.getId(), that.getId());
    }

    @Override
    public int hashCode() { return Objects.hash(this.getId()); }

    @Override
    public String toString() {
        return "PromocionDetalle{" + "id=" + this.getId() + ", cantidad=" + cantidad + '}';
    }
}
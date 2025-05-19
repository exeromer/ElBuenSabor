package com.powerRanger.ElBuenSabor.entities;

import jakarta.persistence.*;
import java.util.Objects;

@Entity
@Table(name = "promocion_detalle")
public class PromocionDetalle {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(nullable = false)
    private Integer cantidad;

    @ManyToOne(optional = false) // optional = false para que siempre haya una promoción
    @JoinColumn(name = "promocion_id")
    private Promocion promocion;

    @ManyToOne(optional = false) // optional = false para que siempre haya un artículo
    @JoinColumn(name = "articulo_id")
    private Articulo articulo; // Puede ser ArticuloInsumo o ArticuloManufacturado

    // Constructores
    public PromocionDetalle() {
    }

    public PromocionDetalle(Integer cantidad, Promocion promocion, Articulo articulo) {
        this.cantidad = cantidad;
        this.promocion = promocion;
        this.articulo = articulo;
    }

    // Getters y Setters
    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public Integer getCantidad() {
        return cantidad;
    }

    public void setCantidad(Integer cantidad) {
        this.cantidad = cantidad;
    }

    public Promocion getPromocion() {
        return promocion;
    }

    public void setPromocion(Promocion promocion) {
        this.promocion = promocion;
    }

    public Articulo getArticulo() {
        return articulo;
    }

    public void setArticulo(Articulo articulo) {
        this.articulo = articulo;
    }

    // equals, hashCode, toString
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        PromocionDetalle that = (PromocionDetalle) o;
        return Objects.equals(id, that.id); // O podrías basarlo en la combinación de promocion_id y articulo_id si son unique juntos
    }

    @Override
    public int hashCode() {
        return Objects.hash(id); // O Objects.hash(promocion, articulo) si son la clave natural
    }

    @Override
    public String toString() {
        return "PromocionDetalle{" +
                "id=" + id +
                ", cantidad=" + cantidad +
                ", promocionId=" + (promocion != null ? promocion.getId() : "null") +
                ", articuloId=" + (articulo != null ? articulo.getId() : "null") +
                '}';
    }
}
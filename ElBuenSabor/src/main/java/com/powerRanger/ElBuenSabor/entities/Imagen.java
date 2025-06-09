package com.powerRanger.ElBuenSabor.entities;

import com.fasterxml.jackson.annotation.JsonIdentityInfo;
import com.fasterxml.jackson.annotation.ObjectIdGenerators;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import java.util.Objects;

@Entity
@JsonIdentityInfo(
        generator = ObjectIdGenerators.PropertyGenerator.class,
        property = "id")
public class Imagen extends BaseEntity { // HEREDA DE BaseEntity

    @Column(nullable = false, length = 1000)
    @NotEmpty(message = "La denominación de la imagen no puede estar vacía")
    private String denominacion;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "articulo_id")
    private Articulo articulo;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "promocion_id")
    private Promocion promocion;

    @Column(name = "estadoActivo", nullable = false)
    @NotNull(message = "El estado activo es obligatorio")
    private Boolean estadoActivo = true;

    public Imagen() {
    }

    // Getters y Setters
    public String getDenominacion() { return denominacion; }
    public void setDenominacion(String denominacion) { this.denominacion = denominacion; }
    public Articulo getArticulo() { return articulo; }
    public void setArticulo(Articulo articulo) { this.articulo = articulo; }
    public Promocion getPromocion() { return promocion; }
    public void setPromocion(Promocion promocion) { this.promocion = promocion; }
    public Boolean getEstadoActivo() { return estadoActivo; }
    public void setEstadoActivo(Boolean estadoActivo) { this.estadoActivo = estadoActivo; }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Imagen imagen = (Imagen) o;
        return Objects.equals(this.getId(), imagen.getId());
    }

    @Override
    public int hashCode() {
        return Objects.hash(this.getId());
    }

    @Override
    public String toString() {
        return "Imagen{" +
                "id=" + this.getId() +
                ", denominacion='" + denominacion + '\'' +
                ", articuloId=" + (articulo != null ? articulo.getId() : "null") +
                ", promocionId=" + (promocion != null ? promocion.getId() : "null") +
                ", estadoActivo=" + estadoActivo +
                '}';
    }
}
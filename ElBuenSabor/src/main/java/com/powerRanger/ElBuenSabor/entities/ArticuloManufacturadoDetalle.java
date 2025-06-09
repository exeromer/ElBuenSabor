package com.powerRanger.ElBuenSabor.entities;

import com.fasterxml.jackson.annotation.JsonIdentityInfo;
import com.fasterxml.jackson.annotation.JsonIdentityReference;
import com.fasterxml.jackson.annotation.ObjectIdGenerators;
import jakarta.persistence.*;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import java.util.Objects;

@Entity
@JsonIdentityInfo(
        generator = ObjectIdGenerators.PropertyGenerator.class,
        property = "id")
public class ArticuloManufacturadoDetalle extends BaseEntity { // HEREDA DE BaseEntity

    @NotNull(message = "La cantidad es obligatoria")
    @Min(value = 1, message = "La cantidad debe ser al menos 1")
    private Double cantidad;

    @NotNull(message = "El artículo insumo es obligatorio")
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "articulo_insumo_id", nullable = false)
    @JsonIdentityReference(alwaysAsId = true)
    private ArticuloInsumo articuloInsumo;

    @NotNull(message = "El artículo manufacturado es obligatorio")
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "articulo_manufacturado_id", nullable = false)
    @JsonIdentityReference(alwaysAsId = true)
    private ArticuloManufacturado articuloManufacturado;

    @Column(name = "estadoActivo")
    @NotNull(message = "El estado activo del detalle es obligatorio")
    private Boolean estadoActivo = true;

    public ArticuloManufacturadoDetalle() {}

    // Getters y Setters
    public Double getCantidad() { return cantidad; }
    public void setCantidad(Double cantidad) { this.cantidad = cantidad; }
    public ArticuloInsumo getArticuloInsumo() { return articuloInsumo; }
    public void setArticuloInsumo(ArticuloInsumo articuloInsumo) { this.articuloInsumo = articuloInsumo; }
    public ArticuloManufacturado getArticuloManufacturado() { return articuloManufacturado; }
    public void setArticuloManufacturado(ArticuloManufacturado articuloManufacturado) { this.articuloManufacturado = articuloManufacturado; }
    public Boolean getEstadoActivo() { return estadoActivo; }
    public void setEstadoActivo(Boolean estadoActivo) { this.estadoActivo = estadoActivo; }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ArticuloManufacturadoDetalle that = (ArticuloManufacturadoDetalle) o;
        return Objects.equals(this.getId(), that.getId());
    }

    @Override
    public int hashCode() {
        return Objects.hash(this.getId());
    }

    @Override
    public String toString() {
        return "ArticuloManufacturadoDetalle{" +
                "id=" + this.getId() +
                ", cantidad=" + cantidad +
                '}';
    }
}
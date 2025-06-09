package com.powerRanger.ElBuenSabor.entities;

import com.fasterxml.jackson.annotation.JsonIdentityInfo;
import com.fasterxml.jackson.annotation.ObjectIdGenerators;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotEmpty;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

@Entity
@JsonIdentityInfo(
        generator = ObjectIdGenerators.PropertyGenerator.class,
        property = "id")
public class UnidadMedida extends BaseEntity {

    @Column(nullable = false, unique = true)
    @NotEmpty(message = "La denominación no puede estar vacía")
    private String denominacion;

    @OneToMany(mappedBy = "unidadMedida", fetch = FetchType.LAZY)
    private List<Articulo> articulos = new ArrayList<>();

    public UnidadMedida() {
    }

    // Getters y Setters
    public String getDenominacion() { return denominacion; }
    public void setDenominacion(String denominacion) { this.denominacion = denominacion; }
    public List<Articulo> getArticulos() { return articulos; }
    public void setArticulos(List<Articulo> articulos) { this.articulos = articulos; }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        UnidadMedida that = (UnidadMedida) o;
        return Objects.equals(this.getId(), that.getId());
    }

    @Override
    public int hashCode() {
        return Objects.hash(this.getId());
    }

    @Override
    public String toString() {
        return "UnidadMedida{" +
                "id=" + this.getId() +
                ", denominacion='" + denominacion + '\'' +
                '}';
    }
}
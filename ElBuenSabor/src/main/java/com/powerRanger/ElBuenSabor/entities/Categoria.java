package com.powerRanger.ElBuenSabor.entities;

import com.fasterxml.jackson.annotation.JsonIdentityInfo;
import com.fasterxml.jackson.annotation.ObjectIdGenerators;
import jakarta.persistence.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

@Entity
@JsonIdentityInfo(
        generator = ObjectIdGenerators.PropertyGenerator.class,
        property = "id")
public class Categoria extends BaseEntity { // HEREDA DE BaseEntity

    @Column(nullable = true)
    private String denominacion;

    @OneToMany(mappedBy = "categoria", cascade = {CascadeType.PERSIST, CascadeType.MERGE}, fetch = FetchType.LAZY)
    private List<Articulo> articulos = new ArrayList<>();

    @Column(name = "estadoActivo")
    private Boolean estadoActivo;

    public Categoria() {
        this.articulos = new ArrayList<>();
    }

    // Getters y Setters
    public String getDenominacion() { return denominacion; }
    public void setDenominacion(String denominacion) { this.denominacion = denominacion; }
    public List<Articulo> getArticulos() { return articulos; }
    public void setArticulos(List<Articulo> articulos) { this.articulos = articulos; }
    public Boolean getEstadoActivo() { return estadoActivo; }
    public void setEstadoActivo(Boolean estadoActivo) { this.estadoActivo = estadoActivo; }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Categoria categoria = (Categoria) o;
        return Objects.equals(this.getId(), categoria.getId());
    }

    @Override
    public int hashCode() {
        return Objects.hash(this.getId());
    }
}
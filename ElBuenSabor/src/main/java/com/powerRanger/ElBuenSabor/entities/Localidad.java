package com.powerRanger.ElBuenSabor.entities;

import com.fasterxml.jackson.annotation.JsonIdentityInfo;
import com.fasterxml.jackson.annotation.ObjectIdGenerators;
import jakarta.persistence.*;
import java.util.List;
import java.util.Objects;

@Entity
@JsonIdentityInfo(
        generator = ObjectIdGenerators.PropertyGenerator.class,
        property = "id")
public class Localidad extends BaseEntity {

    @Column(nullable = true)
    private String nombre;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "provincia_id")
    private Provincia provincia;

    @OneToMany(mappedBy = "localidad", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<Domicilio> domicilios;

    // Getters y Setters
    public String getNombre() { return nombre; }
    public void setNombre(String nombre) { this.nombre = nombre; }
    public Provincia getProvincia() { return provincia; }
    public void setProvincia(Provincia provincia) { this.provincia = provincia; }
    public List<Domicilio> getDomicilios() { return domicilios; }
    public void setDomicilios(List<Domicilio> domicilios) { this.domicilios = domicilios; }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Localidad localidad = (Localidad) o;
        return Objects.equals(this.getId(), localidad.getId());
    }

    @Override
    public int hashCode() {
        return Objects.hash(this.getId());
    }
}
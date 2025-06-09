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
public class Pais extends BaseEntity { // HEREDA DE BaseEntity

    @Column(nullable = true)
    private String nombre;

    @OneToMany(mappedBy = "pais", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<Provincia> provincias;

    // Getters y Setters
    public String getNombre() { return nombre; }
    public void setNombre(String nombre) { this.nombre = nombre; }
    public List<Provincia> getProvincias() { return provincias; }
    public void setProvincias(List<Provincia> provincias) { this.provincias = provincias; }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Pais pais = (Pais) o;
        return Objects.equals(this.getId(), pais.getId());
    }

    @Override
    public int hashCode() {
        return Objects.hash(this.getId());
    }
}
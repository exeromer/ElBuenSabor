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
public class Provincia extends BaseEntity { // HEREDA DE BaseEntity

    @Column(nullable = true)
    private String nombre;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "pais_id")
    private Pais pais;

    @OneToMany(mappedBy = "provincia", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<Localidad> localidades;

    // Getters y Setters
    public String getNombre() { return nombre; }
    public void setNombre(String nombre) { this.nombre = nombre; }
    public Pais getPais() { return pais; }
    public void setPais(Pais pais) { this.pais = pais; }
    public List<Localidad> getLocalidades() { return localidades; }
    public void setLocalidades(List<Localidad> localidades) { this.localidades = localidades; }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Provincia provincia = (Provincia) o;
        return Objects.equals(this.getId(), provincia.getId());
    }

    @Override
    public int hashCode() {
        return Objects.hash(this.getId());
    }
}
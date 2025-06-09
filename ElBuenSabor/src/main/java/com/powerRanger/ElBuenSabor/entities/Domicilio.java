package com.powerRanger.ElBuenSabor.entities;

import com.fasterxml.jackson.annotation.JsonIdentityInfo;
import com.fasterxml.jackson.annotation.ObjectIdGenerators;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

@Entity
@JsonIdentityInfo(
        generator = ObjectIdGenerators.PropertyGenerator.class,
        property = "id")
public class Domicilio extends BaseEntity {
    @Column(nullable = false)
    @NotEmpty(message = "La calle no puede estar vacía")
    private String calle;

    @Column(nullable = false)
    @NotNull(message = "El número no puede ser nulo")
    private Integer numero;

    @Column(nullable = false, length = 8)
    @NotNull(message = "El código postal es obligatorio")
    private String cp;

    @NotNull(message = "La localidad es obligatoria")
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "localidad_id", nullable = false)
    private Localidad localidad;

    @ManyToMany(mappedBy = "domicilios", fetch = FetchType.LAZY)
    private List<Cliente> clientes = new ArrayList<>();

    public Domicilio() {
        this.clientes = new ArrayList<>();
    }

    // Getters y Setters
    public String getCalle() { return calle; }
    public void setCalle(String calle) { this.calle = calle; }
    public Integer getNumero() { return numero; }
    public void setNumero(Integer numero) { this.numero = numero; }
    public String getCp() { return cp; }
    public void setCp(String cp) { this.cp = cp; }
    public Localidad getLocalidad() { return localidad; }
    public void setLocalidad(Localidad localidad) { this.localidad = localidad; }
    public List<Cliente> getClientes() { return clientes; }
    public void setClientes(List<Cliente> clientes) { this.clientes = clientes; }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Domicilio domicilio = (Domicilio) o;
        return Objects.equals(this.getId(), domicilio.getId());
    }

    @Override
    public int hashCode() {
        return Objects.hash(this.getId());
    }

    @Override
    public String toString() {
        return "Domicilio{" +
                "id=" + this.getId() +
                ", calle='" + calle + '\'' +
                ", numero=" + numero +
                ", cp='" + cp + '\'' +
                ", localidad=" + (localidad != null ? localidad.getNombre() : "null") +
                '}';
    }
}
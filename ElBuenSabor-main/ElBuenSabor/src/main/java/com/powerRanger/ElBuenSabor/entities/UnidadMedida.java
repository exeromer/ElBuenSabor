package com.powerRanger.ElBuenSabor.entities;

import com.fasterxml.jackson.annotation.JsonIdentityInfo;
import com.fasterxml.jackson.annotation.ObjectIdGenerators;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotEmpty; // Para validación

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

@Entity
@JsonIdentityInfo( // Para manejo de referencias en JSON y evitar bucles
        generator = ObjectIdGenerators.PropertyGenerator.class,
        property = "id")
public class UnidadMedida {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(nullable = false, unique = true) // Denominación debe ser única y no nula
    @NotEmpty(message = "La denominación no puede estar vacía")
    private String denominacion;

    // mappedBy indica que Articulo es el dueño de la relación.
    // CascadeType.ALL aquí es peligroso: borrar una UnidadMedida borraría todos sus Artículos.
    // Usualmente, no querrías esta cascada desde UnidadMedida.
    // Se puede quitar el cascade o usar uno más restrictivo como REFRESH si es necesario.
    @OneToMany(mappedBy = "unidadMedida", fetch = FetchType.LAZY) // Quitamos CascadeType.ALL
    private List<Articulo> articulos = new ArrayList<>(); // Inicializar colección

    public UnidadMedida() {
        // La colección ya está inicializada en la declaración del campo
    }

    // Getters y Setters
    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public String getDenominacion() {
        return denominacion;
    }

    public void setDenominacion(String denominacion) {
        this.denominacion = denominacion;
    }

    public List<Articulo> getArticulos() {
        return articulos;
    }

    public void setArticulos(List<Articulo> articulos) {
        this.articulos = articulos;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        UnidadMedida that = (UnidadMedida) o;
        return Objects.equals(id, that.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }

    @Override
    public String toString() {
        return "UnidadMedida{" +
                "id=" + id +
                ", denominacion='" + denominacion + '\'' +
                '}';
    }
}
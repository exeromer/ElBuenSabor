package com.powerRanger.ElBuenSabor.entities;

import com.fasterxml.jackson.annotation.JsonIdentityInfo;
import com.fasterxml.jackson.annotation.ObjectIdGenerators;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull; // Añadido
import java.util.Objects;

@Entity
@JsonIdentityInfo(
        generator = ObjectIdGenerators.PropertyGenerator.class,
        property = "id")
public class Imagen {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(nullable = false, length = 1000) // La denominación (URL/path) es importante
    @NotEmpty(message = "La denominación de la imagen no puede estar vacía")
    private String denominacion;

    // Una imagen puede pertenecer a un artículo (opcional)
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "articulo_id") // Se crea columna articulo_id en tabla Imagen
    private Articulo articulo;

    // Una imagen puede pertenecer a una promoción (opcional)
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "promocion_id") // Se crea columna promocion_id en tabla Imagen
    private Promocion promocion;

    // Podríamos tener una relación con Cliente si un Cliente tiene UNA imagen de perfil
    // @OneToOne(fetch = FetchType.LAZY)
    // @JoinColumn(name = "cliente_id")
    // private Cliente cliente;

    @Column(name = "estadoActivo", nullable = false) // Es bueno tener un estado
    @NotNull(message = "El estado activo es obligatorio")
    private Boolean estadoActivo = true; // Default a true

    public Imagen() {
    }

    // Getters y Setters
    public Integer getId() { return id; }
    public void setId(Integer id) { this.id = id; }
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
        return Objects.equals(id, imagen.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }

    @Override
    public String toString() {
        return "Imagen{" +
                "id=" + id +
                ", denominacion='" + denominacion + '\'' +
                ", articuloId=" + (articulo != null ? articulo.getId() : "null") +
                ", promocionId=" + (promocion != null ? promocion.getId() : "null") +
                ", estadoActivo=" + estadoActivo +
                '}';
    }
}
package com.powerRanger.ElBuenSabor.entities;

import jakarta.persistence.*;

@Entity
public class Imagen {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;  // ID de la imagen

    @Column(nullable = true)
    private String denominacion;  // Denominación de la imagen

    // Relación con Articulo: Muchas imágenes pueden estar asociadas a un solo artículo
    @ManyToOne
    @JoinColumn(name = "articulo_id")  // Esta columna será la clave foránea en la tabla Imagen
    private Articulo articulo;

    // Relación con Promocion: Muchas imágenes pueden estar asociadas a una sola promoción
    @ManyToOne
    @JoinColumn(name = "promocion_id")  // Esta columna será la clave foránea en la tabla Imagen
    private Promocion promocion;

    @Column(name = "estadoActivo")
    private Boolean estadoActivo;  // Estado activo

    public String getDenominacion() {
        return denominacion;
    }

    public void setDenominacion(String denominacion) {
        this.denominacion = denominacion;
    }

    public Articulo getArticulo() {
        return articulo;
    }

    public void setArticulo(Articulo articulo) {
        this.articulo = articulo;
    }

    public Promocion getPromocion() {
        return promocion;
    }

    public void setPromocion(Promocion promocion) {
        this.promocion = promocion;
    }

    public Boolean getEstadoActivo() {
        return estadoActivo;
    }

    public void setEstadoActivo(Boolean estadoActivo) {
        this.estadoActivo = estadoActivo;
    }

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }
}

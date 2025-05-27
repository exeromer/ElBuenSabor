package com.powerRanger.ElBuenSabor.entities;

import com.fasterxml.jackson.annotation.JsonIdentityInfo; // Importar
import com.fasterxml.jackson.annotation.ObjectIdGenerators; // Importar
import jakarta.persistence.*;
import java.util.ArrayList; // Importar
import java.util.List;

@Entity
@JsonIdentityInfo( // ✅ Para prevenir bucles si Articulo tiene referencia a Categoria
        generator = ObjectIdGenerators.PropertyGenerator.class,
        property = "id")
public class Categoria {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(nullable = true) // Considera si la denominación puede ser realmente nula
    private String denominacion;

    @OneToMany(mappedBy = "categoria", cascade = {CascadeType.PERSIST, CascadeType.MERGE}, fetch = FetchType.LAZY) // FetchType.LAZY es buena práctica
    private List<Articulo> articulos = new ArrayList<>(); // ✅ Inicializar la colección

    @Column(name = "estadoActivo")
    private Boolean estadoActivo;

    // Constructor por defecto (buena práctica para JPA)
    public Categoria() {
        this.articulos = new ArrayList<>(); // ✅ Inicializar en constructor también
    }

    // Getters y Setters
    public Integer getId() {
        return id;
    }

    public void setId(Integer id) { // Generalmente no necesitas un setter para el ID si es autogenerado
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

    public Boolean getEstadoActivo() {
        return estadoActivo;
    }

    public void setEstadoActivo(Boolean estadoActivo) {
        this.estadoActivo = estadoActivo;
    }
}
package com.powerRanger.ElBuenSabor.entities;

import jakarta.persistence.*;

import java.util.List;

@Entity
public class Localidad {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(nullable = true)
    private String nombre;

    @ManyToOne
    @JoinColumn(name = "provincia_id") // La columna de unión que hace referencia a la provincia
    private Provincia provincia;

    @OneToMany(mappedBy = "localidad") // Relación bidireccional con Domicilio
    private List<Domicilio> domicilios;

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public String getNombre() {
        return nombre;
    }

    public void setNombre(String nombre) {
        this.nombre = nombre;
    }

    public Provincia getProvincia() {
        return provincia;
    }

    public void setProvincia(Provincia provincia) {
        this.provincia = provincia;
    }

    public List<Domicilio> getDomicilios() {
        return domicilios;
    }

    public void setDomicilios(List<Domicilio> domicilios) {
        this.domicilios = domicilios;
    }

}

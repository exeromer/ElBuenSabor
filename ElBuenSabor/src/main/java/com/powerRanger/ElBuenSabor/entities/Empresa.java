package com.powerRanger.ElBuenSabor.entities;

import jakarta.persistence.*;

import java.util.List;


@Entity
public class Empresa {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(nullable = true)
    private String nombre;

    @Column(nullable = true)
    private String razonSocial;

    @Column(nullable = true, length = 11)
    private Integer cuil;

    @OneToMany(cascade = {CascadeType.PERSIST, CascadeType.MERGE}) // Considera Cascade si aplica
    @JoinColumn(name = "sucursalId") // Buena práctica
    private List<Sucursal> sucursales;  // Relación uno a muchos con Articulo

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

    public String getRazonSocial() {
        return razonSocial;
    }

    public void setRazonSocial(String razonSocial) {
        this.razonSocial = razonSocial;
    }

    public Integer getCuil() {
        return cuil;
    }

    public void setCuil(Integer cuil) {
        this.cuil = cuil;
    }

    public List<Sucursal> getSucursales() {
        return sucursales;
    }

    public void setSucursales(List<Sucursal> sucursales) {
        this.sucursales = sucursales;
    }
}
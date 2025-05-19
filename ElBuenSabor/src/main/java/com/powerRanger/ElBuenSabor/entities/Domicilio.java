package com.powerRanger.ElBuenSabor.entities;

import jakarta.persistence.*;

import java.util.List;

@Entity
public class Domicilio {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(nullable = true)
    private String calle;

    @Column(nullable = true)
    private Integer numero;

    @Column(nullable = true, length = 8)
    private Integer cp;

    @ManyToOne
    @JoinColumn(name = "localidad_id") // La columna de unión que hace referencia a la localidad
    private Localidad localidad;

    @ManyToMany(mappedBy = "domicilios") // Relación ManyToMany con Cliente (bidireccional)
    private List<Cliente> clientes;  // Relación ManyToMany con Cliente

    public Integer getId() {
        return id;
    }

    public String getCalle() {
        return calle;
    }

    public void setCalle(String calle) {
        this.calle = calle;
    }

    public Integer getNumero() {
        return numero;
    }

    public void setNumero(Integer numero) {
        this.numero = numero;
    }

    public Integer getCp() {
        return cp;
    }

    public void setCp(Integer cp) {
        this.cp = cp;
    }

    public Localidad getLocalidad() {
        return localidad;
    }

    public void setLocalidad(Localidad localidad) {
        this.localidad = localidad;
    }

    public List<Cliente> getClientes() {
        return clientes;
    }

    public void setClientes(List<Cliente> clientes) {
        this.clientes = clientes;
    }

    public void setId(Integer id) {
        this.id = id;
    }

}

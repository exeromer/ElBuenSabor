package com.powerRanger.ElBuenSabor.entities;

import com.powerRanger.ElBuenSabor.entities.enums.Rol;
import jakarta.persistence.*;
import java.time.LocalDate;
import java.util.Objects;

@Entity
public class Usuario {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    private String auth0Id;
    private String username;

    @Enumerated(EnumType.STRING)
    private Rol rol;

    @Column(name = "fechaBaja")
    private LocalDate fechaBaja;

    @Column(name = "estadoActivo")
    private Boolean estadoActivo;

    // Constructores
    public Usuario() {
    }

    public Usuario(String auth0Id, String username, Rol rol, Boolean estadoActivo) {
        this.auth0Id = auth0Id;
        this.username = username;
        this.rol = rol;
        this.estadoActivo = estadoActivo;
    }

    // Getters y Setters (ya los tenías)
    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public String getAuth0Id() {
        return auth0Id;
    }

    public void setAuth0Id(String auth0Id) {
        this.auth0Id = auth0Id;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public Rol getRol() {
        return rol;
    }

    public void setRol(Rol rol) {
        this.rol = rol;
    }

    public LocalDate getFechaBaja() {
        return fechaBaja;
    }

    public void setFechaBaja(LocalDate fechaBaja) {
        this.fechaBaja = fechaBaja;
    }

    public Boolean getEstadoActivo() {
        return estadoActivo;
    }

    public void setEstadoActivo(Boolean estadoActivo) {
        this.estadoActivo = estadoActivo;
    }

    // equals, hashCode, toString
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Usuario usuario = (Usuario) o;
        return Objects.equals(id, usuario.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }

    @Override
    public String toString() {
        return "Usuario{" +
                "id=" + id +
                ", username='" + username + '\'' +
                ", rol=" + rol +
                ", estadoActivo=" + estadoActivo +
                '}';
    }
}
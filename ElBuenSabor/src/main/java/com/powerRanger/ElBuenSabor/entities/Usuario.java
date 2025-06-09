package com.powerRanger.ElBuenSabor.entities;

import com.fasterxml.jackson.annotation.JsonIdentityInfo;
import com.fasterxml.jackson.annotation.ObjectIdGenerators;
import com.powerRanger.ElBuenSabor.entities.enums.Rol;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;

import java.time.LocalDate;
import java.util.Objects;

@Entity
@Table(name = "usuario", uniqueConstraints = {
        @UniqueConstraint(columnNames = "auth0Id"),
        @UniqueConstraint(columnNames = "username")
})
@JsonIdentityInfo(
        generator = ObjectIdGenerators.PropertyGenerator.class,
        property = "id")
public class Usuario extends BaseEntity { // HEREDA DE BaseEntity

    @Column(nullable = false, unique = true)
    @NotEmpty(message = "El auth0Id no puede estar vacío")
    private String auth0Id;

    @Column(nullable = false, unique = true)
    @NotEmpty(message = "El username no puede estar vacío")
    private String username;

    @NotNull(message = "El rol es obligatorio")
    @Enumerated(EnumType.STRING)
    private Rol rol;

    @Column(name = "fechaBaja")
    private LocalDate fechaBaja;

    @Column(name = "estadoActivo", nullable = false)
    @NotNull(message = "El estado activo es obligatorio")
    private Boolean estadoActivo = true;

    public Usuario() {
    }

    public Usuario(String auth0Id, String username, Rol rol) {
        this.auth0Id = auth0Id;
        this.username = username;
        this.rol = rol;
        this.estadoActivo = true;
    }

    // Getters y Setters
    public String getAuth0Id() { return auth0Id; }
    public void setAuth0Id(String auth0Id) { this.auth0Id = auth0Id; }
    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }
    public Rol getRol() { return rol; }
    public void setRol(Rol rol) { this.rol = rol; }
    public LocalDate getFechaBaja() { return fechaBaja; }
    public void setFechaBaja(LocalDate fechaBaja) { this.fechaBaja = fechaBaja; }
    public Boolean getEstadoActivo() { return estadoActivo; }
    public void setEstadoActivo(Boolean estadoActivo) { this.estadoActivo = estadoActivo; }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Usuario usuario = (Usuario) o;
        return Objects.equals(this.getId(), usuario.getId());
    }

    @Override
    public int hashCode() {
        return Objects.hash(this.getId());
    }

    @Override
    public String toString() {
        return "Usuario{" +
                "id=" + this.getId() +
                ", username='" + username + '\'' +
                ", auth0Id='" + auth0Id + '\'' +
                ", rol=" + rol +
                '}';
    }
}
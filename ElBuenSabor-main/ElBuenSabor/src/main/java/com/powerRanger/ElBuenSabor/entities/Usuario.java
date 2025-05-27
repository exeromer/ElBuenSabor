package com.powerRanger.ElBuenSabor.entities;

import com.fasterxml.jackson.annotation.JsonIdentityInfo;
import com.fasterxml.jackson.annotation.ObjectIdGenerators;
import com.powerRanger.ElBuenSabor.entities.enums.Rol;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotEmpty; // Para Strings no vacíos
import jakarta.validation.constraints.NotNull;  // Para objetos y Boolean
import jakarta.validation.constraints.Pattern; // Para auth0Id si tiene un formato específico

import java.time.LocalDate;
import java.util.Objects;

@Entity
@Table(name = "usuario", uniqueConstraints = { // Asegurar unicidad
        @UniqueConstraint(columnNames = "auth0Id"),
        @UniqueConstraint(columnNames = "username")
})
@JsonIdentityInfo(
        generator = ObjectIdGenerators.PropertyGenerator.class,
        property = "id")
public class Usuario {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(nullable = false, unique = true) // auth0Id debe ser único y no nulo
    @NotEmpty(message = "El auth0Id no puede estar vacío")
    // Si auth0Id tiene un formato específico, podrías añadir @Pattern aquí
    private String auth0Id;

    @Column(nullable = false, unique = true) // username debe ser único y no nulo
    @NotEmpty(message = "El username no puede estar vacío")
    private String username;

    @NotNull(message = "El rol es obligatorio")
    @Enumerated(EnumType.STRING) // Correcto para guardar el nombre del enum como String
    private Rol rol;

    @Column(name = "fechaBaja")
    private LocalDate fechaBaja; // Puede ser nulo si el usuario no está dado de baja

    @Column(name = "estadoActivo", nullable = false) // No puede ser nulo
    @NotNull(message = "El estado activo es obligatorio")
    private Boolean estadoActivo = true; // Valor por defecto a true

    // Constructores
    public Usuario() {
    }

    public Usuario(String auth0Id, String username, Rol rol) {
        this.auth0Id = auth0Id;
        this.username = username;
        this.rol = rol;
        this.estadoActivo = true; // Por defecto activo al crear
    }

    // Getters y Setters
    public Integer getId() { return id; }
    public void setId(Integer id) { this.id = id; }
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
                ", auth0Id='" + auth0Id + '\'' +
                ", rol=" + rol +
                ", estadoActivo=" + estadoActivo +
                ", fechaBaja=" + fechaBaja +
                '}';
    }
}
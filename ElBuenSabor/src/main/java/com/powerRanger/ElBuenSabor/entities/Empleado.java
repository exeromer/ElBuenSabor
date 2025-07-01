package com.powerRanger.ElBuenSabor.entities;

import com.fasterxml.jackson.annotation.JsonIdentityInfo;
import com.fasterxml.jackson.annotation.ObjectIdGenerators;
import com.powerRanger.ElBuenSabor.entities.enums.RolEmpleado;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.time.LocalDate;
import java.util.Objects;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@Entity
@Table(name = "empleado")
@JsonIdentityInfo(
        generator = ObjectIdGenerators.PropertyGenerator.class,
        property = "id")
@JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
public class Empleado {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(nullable = false)
    @NotEmpty(message = "El nombre no puede estar vacío")
    private String nombre;

    @Column(nullable = false)
    @NotEmpty(message = "El apellido no puede estar vacío")
    private String apellido;

    @Column(length = 20)
    @Size(min = 7, max = 20, message = "El teléfono debe tener entre 7 y 20 caracteres")
    private String telefono;

    @Column(nullable = false)
    @NotNull(message = "El rol de empleado es obligatorio")
    @Enumerated(EnumType.STRING)
    private RolEmpleado rolEmpleado; // Rol específico del empleado (Cajero, Cocina, Delivery)

    @OneToOne(cascade = {CascadeType.PERSIST, CascadeType.MERGE, CascadeType.REFRESH})
    @JoinColumn(name = "usuario_id", referencedColumnName = "id", unique = true)
    @NotNull(message = "El usuario es obligatorio")
    private Usuario usuario;

    @Column(name = "fechaBaja")
    private LocalDate fechaBaja;

    @Column(name = "estadoActivo", nullable = false)
    @NotNull(message = "El estado activo es obligatorio")
    private Boolean estadoActivo = true;

    public Empleado() {
    }

    // Getters y Setters
    public Integer getId() { return id; }
    public void setId(Integer id) { this.id = id; }
    public String getNombre() { return nombre; }
    public void setNombre(String nombre) { this.nombre = nombre; }
    public String getApellido() { return apellido; }
    public void setApellido(String apellido) { this.apellido = apellido; }
    public String getTelefono() { return telefono; }
    public void setTelefono(String telefono) { this.telefono = telefono; }
    public RolEmpleado getRolEmpleado() { return rolEmpleado; }
    public void setRolEmpleado(RolEmpleado rolEmpleado) { this.rolEmpleado = rolEmpleado; }
    public Usuario getUsuario() { return usuario; }
    public void setUsuario(Usuario usuario) { this.usuario = usuario; }
    public LocalDate getFechaBaja() { return fechaBaja; }
    public void setFechaBaja(LocalDate fechaBaja) { this.fechaBaja = fechaBaja; }
    public Boolean getEstadoActivo() { return estadoActivo; }
    public void setEstadoActivo(Boolean estadoActivo) { this.estadoActivo = estadoActivo; }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Empleado empleado = (Empleado) o;
        return Objects.equals(id, empleado.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }

    @Override
    public String toString() {
        return "Empleado{" +
                "id=" + id +
                ", nombre='" + nombre + '\'' +
                ", apellido='" + apellido + '\'' +
                ", rolEmpleado=" + rolEmpleado +
                ", usuarioId=" + (usuario != null ? usuario.getId() : "null") +
                '}';
    }
}
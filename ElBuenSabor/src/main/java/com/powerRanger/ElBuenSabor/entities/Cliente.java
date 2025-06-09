package com.powerRanger.ElBuenSabor.entities;

import com.fasterxml.jackson.annotation.JsonIdentityInfo;
import com.fasterxml.jackson.annotation.ObjectIdGenerators;
import jakarta.persistence.*;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PastOrPresent;
import jakarta.validation.constraints.Size;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

@Entity
@JsonIdentityInfo(
        generator = ObjectIdGenerators.PropertyGenerator.class,
        property = "id")
public class Cliente extends BaseEntity {

    @Column(nullable = false)
    @NotEmpty(message = "El nombre no puede estar vacío")
    private String nombre;

    @Column(nullable = false)
    @NotEmpty(message = "El apellido no puede estar vacío")
    private String apellido;

    @Column(length = 20)
    @Size(min = 7, max = 20, message = "El teléfono debe tener entre 7 y 20 caracteres")
    private String telefono;

    @Column(unique = true, nullable = false)
    @NotEmpty(message = "El email no puede estar vacío")
    @Email(message = "El formato del email no es válido")
    private String email;

    @PastOrPresent(message = "La fecha de nacimiento no puede ser futura")
    private LocalDate fechaNacimiento;

    @ManyToMany(cascade = {CascadeType.PERSIST, CascadeType.MERGE}, fetch = FetchType.LAZY)
    @JoinTable(
            name = "cliente_domicilio",
            joinColumns = @JoinColumn(name = "cliente_id"),
            inverseJoinColumns = @JoinColumn(name = "domicilio_id")
    )
    private List<Domicilio> domicilios = new ArrayList<>();

    @OneToMany(mappedBy = "cliente", cascade = {CascadeType.REFRESH}, fetch = FetchType.LAZY)
    private List<Pedido> pedidos = new ArrayList<>();

    @OneToOne(cascade = {CascadeType.PERSIST, CascadeType.MERGE, CascadeType.REFRESH})
    @JoinColumn(name = "usuario_id", referencedColumnName = "id", unique = true)
    @NotNull(message = "El usuario es obligatorio")
    private Usuario usuario;

    @OneToOne(cascade = CascadeType.ALL, orphanRemoval = true)
    @JoinColumn(name = "imagen_id", referencedColumnName = "id")
    private Imagen imagen;

    @Column(name = "fechaBaja")
    private LocalDate fechaBaja;

    @Column(name = "estadoActivo", nullable = false)
    @NotNull(message = "El estado activo es obligatorio")
    private Boolean estadoActivo = true;

    public Cliente() {
        this.domicilios = new ArrayList<>();
        this.pedidos = new ArrayList<>();
    }

    // Getters y Setters
    public String getNombre() { return nombre; }
    public void setNombre(String nombre) { this.nombre = nombre; }
    public String getApellido() { return apellido; }
    public void setApellido(String apellido) { this.apellido = apellido; }
    public String getTelefono() { return telefono; }
    public void setTelefono(String telefono) { this.telefono = telefono; }
    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }
    public LocalDate getFechaNacimiento() { return fechaNacimiento; }
    public void setFechaNacimiento(LocalDate fechaNacimiento) { this.fechaNacimiento = fechaNacimiento; }
    public List<Domicilio> getDomicilios() { return domicilios; }
    public void setDomicilios(List<Domicilio> domicilios) { this.domicilios = domicilios; }
    public List<Pedido> getPedidos() { return pedidos; }
    public void setPedidos(List<Pedido> pedidos) { this.pedidos = pedidos; }
    public Usuario getUsuario() { return usuario; }
    public void setUsuario(Usuario usuario) { this.usuario = usuario; }
    public Imagen getImagen() { return imagen; }
    public void setImagen(Imagen imagen) { this.imagen = imagen; }
    public LocalDate getFechaBaja() { return fechaBaja; }
    public void setFechaBaja(LocalDate fechaBaja) { this.fechaBaja = fechaBaja; }
    public Boolean getEstadoActivo() { return estadoActivo; }
    public void setEstadoActivo(Boolean estadoActivo) { this.estadoActivo = estadoActivo; }

    public void addDomicilio(Domicilio domicilio) {
        if (this.domicilios == null) {
            this.domicilios = new ArrayList<>();
        }
        this.domicilios.add(domicilio);
    }

    public void removeDomicilio(Domicilio domicilio) {
        if (this.domicilios != null) {
            this.domicilios.remove(domicilio);
        }
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Cliente cliente = (Cliente) o;
        return Objects.equals(this.getId(), cliente.getId());
    }

    @Override
    public int hashCode() {
        return Objects.hash(this.getId());
    }

    @Override
    public String toString() {
        return "Cliente{" +
                "id=" + this.getId() +
                ", nombre='" + nombre + '\'' +
                ", apellido='" + apellido + '\'' +
                ", email='" + email + '\'' +
                ", usuarioId=" + (usuario != null ? usuario.getId() : "null") +
                ", estadoActivo=" + estadoActivo +
                '}';
    }
}
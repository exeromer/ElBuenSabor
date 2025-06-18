package com.powerRanger.ElBuenSabor.entities;

import com.fasterxml.jackson.annotation.JsonIdentityInfo;
import com.fasterxml.jackson.annotation.ObjectIdGenerators;
import jakarta.persistence.*;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PastOrPresent; // Para fechaNacimiento
import jakarta.validation.constraints.Size;       // Para telefono

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

@Entity
@JsonIdentityInfo(
        generator = ObjectIdGenerators.PropertyGenerator.class,
        property = "id")
public class Cliente {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(nullable = false)
    @NotEmpty(message = "El nombre no puede estar vacío")
    private String nombre;

    @Column(nullable = false)
    @NotEmpty(message = "El apellido no puede estar vacío")
    private String apellido;

    @Column(length = 20) // Aumentar longitud para incluir prefijos internacionales si es necesario
    @Size(min = 7, max = 20, message = "El teléfono debe tener entre 7 y 20 caracteres")
    private String telefono;

    @Column(unique = true, nullable = false)
    @NotEmpty(message = "El email no puede estar vacío")
    @Email(message = "El formato del email no es válido")
    private String email;

    @PastOrPresent(message = "La fecha de nacimiento no puede ser futura")
    private LocalDate fechaNacimiento;

    // Dueño de la relación ManyToMany con Domicilio
    @ManyToMany(cascade = {CascadeType.PERSIST, CascadeType.MERGE}, fetch = FetchType.LAZY)
    @JoinTable(
            name = "cliente_domicilio",
            joinColumns = @JoinColumn(name = "cliente_id"),
            inverseJoinColumns = @JoinColumn(name = "domicilio_id")
    )
    private List<Domicilio> domicilios = new ArrayList<>();

    @OneToMany(mappedBy = "cliente", cascade = {CascadeType.REFRESH}, fetch = FetchType.LAZY) // No PERSIST/MERGE desde aquí usualmente
    private List<Pedido> pedidos = new ArrayList<>();

    @OneToOne(cascade = {CascadeType.PERSIST, CascadeType.MERGE, CascadeType.REFRESH}) // Guardar/actualizar Usuario con Cliente
    @JoinColumn(name = "usuario_id", referencedColumnName = "id", unique = true) // Asegurar que un usuario solo se asocie a un cliente
    @NotNull(message = "El usuario es obligatorio")
    private Usuario usuario;

    @OneToOne(cascade = CascadeType.ALL, orphanRemoval = true) // Imagen es completamente dependiente del Cliente
    @JoinColumn(name = "imagen_id", referencedColumnName = "id")
    private Imagen imagen; // Puede ser null si el cliente no tiene imagen

    @Column(name = "fechaBaja")
    private LocalDate fechaBaja;

    @Column(name = "estadoActivo", nullable = false)
    @NotNull(message = "El estado activo es obligatorio")
    private Boolean estadoActivo = true;

    public Cliente() {
        this.domicilios = new ArrayList<>();
        this.pedidos = new ArrayList<>();
    }

    // Getters y Setters (como los tenías)
    public Integer getId() { return id; }
    public void setId(Integer id) { this.id = id; }
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

    // Métodos Helper para relaciones
    public void addDomicilio(Domicilio domicilio) {
        if (this.domicilios == null) {
            this.domicilios = new ArrayList<>();
        }
        this.domicilios.add(domicilio);
        // Si Domicilio tiene una lista de Clientes y quieres mantenerla sincronizada:
        // if (domicilio.getClientes() == null) {
        //     domicilio.setClientes(new ArrayList<>());
        // }
        // domicilio.getClientes().add(this);
    }

    public void removeDomicilio(Domicilio domicilio) {
        if (this.domicilios != null) {
            this.domicilios.remove(domicilio);
            // Si Domicilio tiene una lista de Clientes:
            // domicilio.getClientes().remove(this);
        }
    }

    // Pedidos usualmente se añaden desde el servicio de Pedido
    // public void addPedido(Pedido pedido) {
    //     if (this.pedidos == null) {
    //         this.pedidos = new ArrayList<>();
    //     }
    //     this.pedidos.add(pedido);
    //     pedido.setCliente(this);
    // }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Cliente cliente = (Cliente) o;
        return Objects.equals(id, cliente.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }

    @Override
    public String toString() {
        return "Cliente{" +
                "id=" + id +
                ", nombre='" + nombre + '\'' +
                ", apellido='" + apellido + '\'' +
                ", email='" + email + '\'' +
                ", usuarioId=" + (usuario != null ? usuario.getId() : "null") +
                ", estadoActivo=" + estadoActivo +
                '}';
    }
}
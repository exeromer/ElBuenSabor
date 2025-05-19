package com.powerRanger.ElBuenSabor.entities;

import jakarta.persistence.*;
import jakarta.validation.constraints.Email;
import java.time.LocalDate;
import java.util.ArrayList; // Asegúrate de inicializar las listas
import java.util.List;
import java.util.Objects;


@Entity
public class Cliente {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(nullable = true)
    private String nombre;

    @Column(nullable = true)
    private String apellido;

    @Column(nullable = true, length = 15) // O considera un String más largo si es necesario.
    private String telefono;

    @Column(unique = true)
    @Email
    private String email;

    @Column(nullable = true)
    private LocalDate fechaNacimiento;

    @ManyToMany(cascade = {CascadeType.PERSIST, CascadeType.MERGE})
    @JoinTable(
            name = "cliente_domicilio",
            joinColumns = @JoinColumn(name = "cliente_id"),
            inverseJoinColumns = @JoinColumn(name = "domicilio_id")
    )
    private List<Domicilio> domicilios = new ArrayList<>(); // Inicializado

    @OneToMany(mappedBy = "cliente", cascade = {CascadeType.PERSIST, CascadeType.MERGE})
    private List<Pedido> pedidos = new ArrayList<>(); // Inicializado

    @OneToOne(cascade = CascadeType.ALL) // Revisa si CascadeType.ALL es realmente necesario.
    @JoinColumn(name = "usuario_id") // Es buena práctica añadir JoinColumn para OneToOne
    private Usuario usuario;

    @OneToOne(cascade = CascadeType.ALL) // Revisa si CascadeType.ALL es realmente necesario.
    @JoinColumn(name = "imagen_id") // Es buena práctica añadir JoinColumn para OneToOne
    private Imagen imagen;

    @Column(name = "fechaBaja")
    private LocalDate fechaBaja;

    @Column(name = "estadoActivo")
    private Boolean estadoActivo;

    // Constructores
    public Cliente() {
    }

    public Cliente(String nombre, String apellido, String telefono, String email, LocalDate fechaNacimiento, Usuario usuario, Imagen imagen, Boolean estadoActivo) {
        this.nombre = nombre;
        this.apellido = apellido;
        this.telefono = telefono;
        this.email = email;
        this.fechaNacimiento = fechaNacimiento;
        this.usuario = usuario;
        this.imagen = imagen;
        this.estadoActivo = estadoActivo;
    }

    // Getters y Setters (ya los tenías)
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

    public String getApellido() {
        return apellido;
    }

    public void setApellido(String apellido) {
        this.apellido = apellido;
    }

    public String getTelefono() {
        return telefono;
    }

    public void setTelefono(String telefono) {
        this.telefono = telefono;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public LocalDate getFechaNacimiento() {
        return fechaNacimiento;
    }

    public void setFechaNacimiento(LocalDate fechaNacimiento) {
        this.fechaNacimiento = fechaNacimiento;
    }

    public List<Domicilio> getDomicilios() {
        return domicilios;
    }

    public void setDomicilios(List<Domicilio> domicilios) {
        this.domicilios = domicilios;
    }

    public List<Pedido> getPedidos() {
        return pedidos;
    }

    public void setPedidos(List<Pedido> pedidos) {
        this.pedidos = pedidos;
    }

    public Usuario getUsuario() {
        return usuario;
    }

    public void setUsuario(Usuario usuario) {
        this.usuario = usuario;
    }

    public Imagen getImagen() {
        return imagen;
    }

    public void setImagen(Imagen imagen) {
        this.imagen = imagen;
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
                ", estadoActivo=" + estadoActivo +
                '}';
    }

    public void addDomicilio(Domicilio domicilio) {
        if (this.domicilios == null) {
            this.domicilios = new ArrayList<>();
        }
        this.domicilios.add(domicilio);
        // Para relaciones ManyToMany, la entidad dueña (la que NO tiene mappedBy)
        // es la responsable de la tabla de unión. Añadir a la lista es suficiente aquí
        // si Cliente es el dueño o si la relación se maneja adecuadamente por JPA.
        // Si Domicilio también tiene una lista de Clientes y es una relación bidireccional,
        // también deberías hacer: domicilio.getClientes().add(this); (o un método helper en Domicilio)
        // pero para la inserción de datos, esto suele ser suficiente.
    }
}
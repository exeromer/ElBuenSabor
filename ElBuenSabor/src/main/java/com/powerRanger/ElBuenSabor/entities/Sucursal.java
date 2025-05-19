package com.powerRanger.ElBuenSabor.entities;

import jakarta.persistence.*;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.ArrayList; // Importar ArrayList
import java.util.List;      // Importar List
import java.util.Objects;   // Importar Objects

@Entity
public class Sucursal {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(nullable = true)
    private String nombre;

    @Column(nullable = true)
    private LocalTime horarioApertura;

    @Column(nullable = true)
    private LocalTime horarioCierre;

    @OneToOne(cascade = CascadeType.ALL)
    private Domicilio domicilio;

    @ManyToMany(cascade = {CascadeType.PERSIST, CascadeType.MERGE})
    @JoinTable(
            name = "sucursal_promocion", // Nombre de tabla corregido (era promocion_sucursal con join columns invertidos)
            joinColumns = @JoinColumn(name = "sucursal_id"),
            inverseJoinColumns = @JoinColumn(name = "promocion_id")
    )
    private List<Promocion> promociones = new ArrayList<>(); // Inicializar

    @ManyToMany(cascade = {CascadeType.PERSIST, CascadeType.MERGE})
    @JoinTable(
            name = "sucursal_categoria", // Nombre de tabla corregido (era categorias_sucursal con join columns invertidos)
            joinColumns = @JoinColumn(name = "sucursal_id"),
            inverseJoinColumns = @JoinColumn(name = "categoria_id") // Corregido a categoria_id
    )
    private List<Categoria> categorias = new ArrayList<>(); // Inicializar

    @Column(name = "fechaBaja")
    private LocalDate fechaBaja;

    @Column(name = "estadoActivo")
    private Boolean estadoActivo;

    // Constructores
    public Sucursal() {
    }

    public Sucursal(String nombre, LocalTime horarioApertura, LocalTime horarioCierre, Domicilio domicilio, Boolean estadoActivo) {
        this.nombre = nombre;
        this.horarioApertura = horarioApertura;
        this.horarioCierre = horarioCierre;
        this.domicilio = domicilio;
        this.estadoActivo = estadoActivo;
    }

    // Getters y Setters
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

    public LocalTime getHorarioApertura() {
        return horarioApertura;
    }

    public void setHorarioApertura(LocalTime horarioApertura) {
        this.horarioApertura = horarioApertura;
    }

    public LocalTime getHorarioCierre() {
        return horarioCierre;
    }

    public void setHorarioCierre(LocalTime horarioCierre) {
        this.horarioCierre = horarioCierre;
    }

    public Domicilio getDomicilio() {
        return domicilio;
    }

    public void setDomicilio(Domicilio domicilio) {
        this.domicilio = domicilio;
    }

    public List<Promocion> getPromociones() {
        return promociones;
    }

    public void setPromociones(List<Promocion> promociones) {
        this.promociones = promociones;
    }

    public List<Categoria> getCategorias() {
        return categorias;
    }

    public void setCategorias(List<Categoria> categorias) {
        this.categorias = categorias;
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

    // --- MÉTODOS HELPER AÑADIDOS ---
    public void addCategoria(Categoria categoria) {
        if (this.categorias == null) {
            this.categorias = new ArrayList<>();
        }
        this.categorias.add(categoria);
        // Si Categoria también tiene una lista de Sucursales, y quieres mantener la bidireccionalidad:
        // if (categoria.getSucursales() == null) {
        //     categoria.setSucursales(new ArrayList<>());
        // }
        // categoria.getSucursales().add(this);
    }

    public void addPromocion(Promocion promocion) {
        if (this.promociones == null) {
            this.promociones = new ArrayList<>();
        }
        this.promociones.add(promocion);
        // Similar para la bidireccionalidad con Promocion si aplica.
    }

    // equals, hashCode, toString
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Sucursal sucursal = (Sucursal) o;
        return Objects.equals(id, sucursal.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }

    @Override
    public String toString() {
        return "Sucursal{" +
                "id=" + id +
                ", nombre='" + nombre + '\'' +
                ", estadoActivo=" + estadoActivo +
                '}';
    }
}
package com.powerRanger.ElBuenSabor.entities;

import com.fasterxml.jackson.annotation.JsonIdentityInfo;
import com.fasterxml.jackson.annotation.ObjectIdGenerators;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

@Entity
@JsonIdentityInfo(
        generator = ObjectIdGenerators.PropertyGenerator.class,
        property = "id")
public class Sucursal {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(nullable = false, unique = true)
    @NotEmpty(message = "El nombre de la sucursal no puede estar vacío")
    @Size(max = 255, message = "El nombre de la sucursal no puede exceder los 255 caracteres")
    private String nombre;

    @NotNull(message = "El horario de apertura es obligatorio")
    private LocalTime horarioApertura;

    @NotNull(message = "El horario de cierre es obligatorio")
    private LocalTime horarioCierre;

    @NotNull(message = "La empresa es obligatoria para la sucursal")
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "empresa_id", nullable = false)
    private Empresa empresa;

    @OneToOne(cascade = CascadeType.ALL, orphanRemoval = true)
    @JoinColumn(name = "domicilio_id", referencedColumnName = "id", nullable = false)
    @NotNull(message = "El domicilio es obligatorio para la sucursal")
    private Domicilio domicilio;

    @ManyToMany(cascade = {CascadeType.PERSIST, CascadeType.MERGE}, fetch = FetchType.LAZY)
    @JoinTable(
            name = "sucursal_promocion",
            joinColumns = @JoinColumn(name = "sucursal_id"),
            inverseJoinColumns = @JoinColumn(name = "promocion_id")
    )
    private List<Promocion> promociones = new ArrayList<>();

    @ManyToMany(cascade = {CascadeType.PERSIST, CascadeType.MERGE}, fetch = FetchType.LAZY)
    @JoinTable(
            name = "sucursal_categoria",
            joinColumns = @JoinColumn(name = "sucursal_id"),
            inverseJoinColumns = @JoinColumn(name = "categoria_id")
    )
    private List<Categoria> categorias = new ArrayList<>();

    @Column(name = "fechaBaja")
    private LocalDate fechaBaja;

    @Column(name = "estadoActivo", nullable = false)
    @NotNull(message = "El estado activo es obligatorio")
    private Boolean estadoActivo = true;

    public Sucursal() {
        this.promociones = new ArrayList<>();
        this.categorias = new ArrayList<>();
    }

    // Getters y Setters
    public Integer getId() { return id; }
    public void setId(Integer id) { this.id = id; }
    public String getNombre() { return nombre; }
    public void setNombre(String nombre) { this.nombre = nombre; }
    public LocalTime getHorarioApertura() { return horarioApertura; }
    public void setHorarioApertura(LocalTime horarioApertura) { this.horarioApertura = horarioApertura; }
    public LocalTime getHorarioCierre() { return horarioCierre; }
    public void setHorarioCierre(LocalTime horarioCierre) { this.horarioCierre = horarioCierre; }
    public Empresa getEmpresa() { return empresa; }
    public void setEmpresa(Empresa empresa) { this.empresa = empresa; }
    public Domicilio getDomicilio() { return domicilio; }
    public void setDomicilio(Domicilio domicilio) { this.domicilio = domicilio; }
    public List<Promocion> getPromociones() { return promociones; }
    public void setPromociones(List<Promocion> promociones) { this.promociones = promociones; }
    public List<Categoria> getCategorias() { return categorias; }
    public void setCategorias(List<Categoria> categorias) { this.categorias = categorias; }
    public LocalDate getFechaBaja() { return fechaBaja; }
    public void setFechaBaja(LocalDate fechaBaja) { this.fechaBaja = fechaBaja; }
    public Boolean getEstadoActivo() { return estadoActivo; }
    public void setEstadoActivo(Boolean estadoActivo) { this.estadoActivo = estadoActivo; }

    // Métodos Helper
    public void addCategoria(Categoria categoria) {
        if (this.categorias == null) this.categorias = new ArrayList<>();
        if (!this.categorias.contains(categoria)) {
            this.categorias.add(categoria);
            // Si Categoria tuviera List<Sucursal> y fuera bidireccional gestionada también desde Categoria:
            // if(categoria.getSucursales() == null) categoria.setSucursales(new ArrayList<>());
            // if(!categoria.getSucursales().contains(this)) categoria.getSucursales().add(this);
        }
    }
    public void removeCategoria(Categoria categoria) {
        if (this.categorias != null) this.categorias.remove(categoria);
        // if (categoria != null && categoria.getSucursales() != null) categoria.getSucursales().remove(this);
    }

    public void addPromocion(Promocion promocion) {
        if (this.promociones == null) this.promociones = new ArrayList<>();
        if (!this.promociones.contains(promocion)) {
            this.promociones.add(promocion);
            // Si Promocion tuviera List<Sucursal> y fuera bidireccional gestionada también desde Promocion:
            // if(promocion.getSucursales() == null) promocion.setSucursales(new ArrayList<>());
            // if(!promocion.getSucursales().contains(this)) promocion.getSucursales().add(this);
        }
    }
    public void removePromocion(Promocion promocion) {
        if (this.promociones != null) this.promociones.remove(promocion);
        // if (promocion != null && promocion.getSucursales() != null) promocion.getSucursales().remove(this);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Sucursal sucursal = (Sucursal) o;
        return Objects.equals(id, sucursal.id);
    }

    @Override
    public int hashCode() { return Objects.hash(id); }

    @Override
    public String toString() {
        return "Sucursal{" + "id=" + id + ", nombre='" + nombre + '\'' +
                ", empresa=" + (empresa != null ? empresa.getNombre() : "null") +
                ", estadoActivo=" + estadoActivo +
                ", numPromociones=" + (promociones != null ? promociones.size() : 0) +
                ", numCategorias=" + (categorias != null ? categorias.size() : 0) +
                '}';
    }
}
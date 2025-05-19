package com.powerRanger.ElBuenSabor.entities;

import jakarta.persistence.*;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Size;
import java.util.ArrayList; // Asegúrate de inicializar las listas
import java.util.List;
import java.util.Objects;

@Entity
@Inheritance(strategy = InheritanceType.JOINED)
public class Articulo {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(nullable = true)
    @Size(min = 1)
    private String denominacion;

    @Column(nullable = true)
    @Min(1) // Aunque es Double, Size aplica al número de dígitos si se interpretara como String, o es un error.
    // Para Double, @Min o @DecimalMin serían más apropiados si la validación es sobre el valor.
    private Double precioVenta;

    @ManyToOne(cascade = CascadeType.ALL) // Considera si CascadeType.ALL es apropiado aquí.
    @JoinColumn(name = "unidad_medida_id")
    private UnidadMedida unidadMedida;

    @OneToMany(mappedBy = "articulo", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Imagen> imagenes = new ArrayList<>(); // Inicializado

    @ManyToOne
    @JoinColumn(name = "categoria_id")
    private Categoria categoria;

    @OneToMany(mappedBy = "articulo", cascade = CascadeType.ALL)
    private List<DetallePedido> detallesPedidos = new ArrayList<>(); // Inicializado

    // NUEVA RELACIÓN CON PromocionDetalle (ver punto 3)
    @OneToMany(mappedBy = "articulo", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<PromocionDetalle> detallesPromocion = new ArrayList<>();

    @Column(name = "estadoActivo")
    private Boolean estadoActivo;

    // Constructores
    public Articulo() {
    }

    public Articulo(String denominacion, Double precioVenta, UnidadMedida unidadMedida, Categoria categoria, Boolean estadoActivo) {
        this.denominacion = denominacion;
        this.precioVenta = precioVenta;
        this.unidadMedida = unidadMedida;
        this.categoria = categoria;
        this.estadoActivo = estadoActivo;
    }

    // Getters y Setters (ya los tenías casi todos, los mantengo y completo)
    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public String getDenominacion() {
        return denominacion;
    }

    public void setDenominacion(String denominacion) {
        this.denominacion = denominacion;
    }

    public Double getPrecioVenta() {
        return precioVenta;
    }

    public void setPrecioVenta(Double precioVenta) {
        this.precioVenta = precioVenta;
    }

    public UnidadMedida getUnidadMedida() {
        return unidadMedida;
    }

    public void setUnidadMedida(UnidadMedida unidadMedida) {
        this.unidadMedida = unidadMedida;
    }

    public List<Imagen> getImagenes() {
        return imagenes;
    }

    public void setImagenes(List<Imagen> imagenes) {
        this.imagenes = imagenes;
    }

    public Categoria getCategoria() {
        return categoria;
    }

    public void setCategoria(Categoria categoria) {
        this.categoria = categoria;
    }

    public List<DetallePedido> getDetallesPedidos() {
        return detallesPedidos;
    }

    public void setDetallesPedidos(List<DetallePedido> detallesPedidos) {
        this.detallesPedidos = detallesPedidos;
    }

    // Getter y Setter para la nueva relación (ver punto 3)
    public List<PromocionDetalle> getDetallesPromocion() {
        return detallesPromocion;
    }

    public void setDetallesPromocion(List<PromocionDetalle> detallesPromocion) {
        this.detallesPromocion = detallesPromocion;
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
        Articulo articulo = (Articulo) o;
        return Objects.equals(id, articulo.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }

    @Override
    public String toString() {
        return "Articulo{" +
                "id=" + id +
                ", denominacion='" + denominacion + '\'' +
                ", precioVenta=" + precioVenta +
                ", estadoActivo=" + estadoActivo +
                '}';
    }
}
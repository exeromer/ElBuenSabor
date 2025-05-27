package com.powerRanger.ElBuenSabor.entities;

import com.fasterxml.jackson.annotation.JsonIdentityInfo;
import com.fasterxml.jackson.annotation.JsonIdentityReference; // Asegúrate de importar esto
import com.fasterxml.jackson.annotation.ObjectIdGenerators;
import jakarta.persistence.*;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

@Entity
@Inheritance(strategy = InheritanceType.JOINED)
@JsonIdentityInfo(
        generator = ObjectIdGenerators.PropertyGenerator.class,
        property = "id")
public class Articulo {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(nullable = false)
    @NotEmpty(message = "La denominación no puede estar vacía")
    private String denominacion;

    @Column(nullable = false)
    @NotNull(message = "El precio de venta no puede ser nulo")
    @DecimalMin(value = "0.01", message = "El precio de venta debe ser mayor que 0")
    private Double precioVenta;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "unidad_medida_id")
    @NotNull(message = "La unidad de medida es obligatoria")
    @JsonIdentityReference(alwaysAsId = true) // Deserializará/Serializará solo el ID
    private UnidadMedida unidadMedida;

    @OneToMany(mappedBy = "articulo", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    private List<Imagen> imagenes = new ArrayList<>();

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "categoria_id")
    @NotNull(message = "La categoría es obligatoria")
    @JsonIdentityReference(alwaysAsId = true) // Deserializará/Serializará solo el ID
    private Categoria categoria;

    @OneToMany(mappedBy = "articulo", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<DetallePedido> detallesPedidos = new ArrayList<>();

    @OneToMany(mappedBy = "articulo", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    private List<PromocionDetalle> detallesPromocion = new ArrayList<>();

    @Column(name = "estadoActivo", nullable = false)
    @NotNull(message = "El estado activo es obligatorio")
    private Boolean estadoActivo;

    public Articulo() {
    }

    public Articulo(String denominacion, Double precioVenta, UnidadMedida unidadMedida, Categoria categoria, Boolean estadoActivo) {
        this.denominacion = denominacion;
        this.precioVenta = precioVenta;
        this.unidadMedida = unidadMedida;
        this.categoria = categoria;
        this.estadoActivo = estadoActivo;
    }

    // Getters y Setters
    public Integer getId() { return id; }
    public void setId(Integer id) { this.id = id; }
    public String getDenominacion() { return denominacion; }
    public void setDenominacion(String denominacion) { this.denominacion = denominacion; }
    public Double getPrecioVenta() { return precioVenta; }
    public void setPrecioVenta(Double precioVenta) { this.precioVenta = precioVenta; }
    public UnidadMedida getUnidadMedida() { return unidadMedida; }
    public void setUnidadMedida(UnidadMedida unidadMedida) { this.unidadMedida = unidadMedida; }
    public List<Imagen> getImagenes() { return imagenes; }
    public void setImagenes(List<Imagen> imagenes) { this.imagenes = imagenes; }
    public Categoria getCategoria() { return categoria; }
    public void setCategoria(Categoria categoria) { this.categoria = categoria; }
    public List<DetallePedido> getDetallesPedidos() { return detallesPedidos; }
    public void setDetallesPedidos(List<DetallePedido> detallesPedidos) { this.detallesPedidos = detallesPedidos; }
    public List<PromocionDetalle> getDetallesPromocion() { return detallesPromocion; }
    public void setDetallesPromocion(List<PromocionDetalle> detallesPromocion) { this.detallesPromocion = detallesPromocion; }
    public Boolean getEstadoActivo() { return estadoActivo; }
    public void setEstadoActivo(Boolean estadoActivo) { this.estadoActivo = estadoActivo; }

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
                ", categoriaId=" + (categoria != null ? categoria.getId() : "N/A") + // Mostrar ID por JsonIdentityReference
                ", unidadMedidaId=" + (unidadMedida != null ? unidadMedida.getId() : "N/A") + // Mostrar ID
                ", estadoActivo=" + estadoActivo +
                '}';
    }
}
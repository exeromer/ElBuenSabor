package com.powerRanger.ElBuenSabor.entities;

import com.fasterxml.jackson.annotation.JsonIdentityInfo;
import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.ObjectIdGenerators;
import jakarta.persistence.*;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.FutureOrPresent;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.HashSet;

import com.powerRanger.ElBuenSabor.entities.enums.TipoPromocion;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@Entity
@JsonIdentityInfo(
        generator = ObjectIdGenerators.PropertyGenerator.class,
        property = "id")
@JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
public class Promocion {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(nullable = false, unique = true)
    @NotEmpty(message = "La denominación de la promoción no puede estar vacía")
    @Size(max = 255, message = "La denominación no puede exceder los 255 caracteres")
    private String denominacion;

    @NotNull(message = "La fecha desde es obligatoria")
    @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDate fechaDesde;

    @NotNull(message = "La fecha hasta es obligatoria")
    @FutureOrPresent(message = "La fecha hasta no puede ser pasada")
    @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDate fechaHasta;

    @NotNull(message = "La hora desde es obligatoria")
    @JsonFormat(pattern = "HH:mm:ss")
    private LocalTime horaDesde;

    @NotNull(message = "La hora hasta es obligatoria")
    @JsonFormat(pattern = "HH:mm:ss")
    private LocalTime horaHasta;

    @Column(length = 1000)
    @Size(max = 1000, message = "La descripción del descuento no puede exceder los 1000 caracteres")
    private String descripcionDescuento;

    @DecimalMin(value = "0.0", message = "El precio promocional no puede ser negativo")
    private Double precioPromocional;

    @DecimalMin(value = "0.0", message = "El porcentaje de descuento no puede ser negativo")
    private Double porcentajeDescuento;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    @NotNull(message = "El tipo de promoción es obligatorio")
    private TipoPromocion tipoPromocion;


    @OneToMany(mappedBy = "promocion", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    private List<Imagen> imagenes = new ArrayList<>();

    @OneToMany(mappedBy = "promocion", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    private Set<PromocionDetalle> detallesPromocion = new HashSet<>();

    @ManyToMany(cascade = {CascadeType.PERSIST, CascadeType.MERGE}, fetch = FetchType.LAZY)
    @JoinTable(
            name = "promocion_sucursal",
            joinColumns = @JoinColumn(name = "promocion_id"),
            inverseJoinColumns = @JoinColumn(name = "sucursal_id")
    )
    private Set<Sucursal> sucursales = new HashSet<>();


    @Column(name = "estadoActivo", nullable = false)
    @NotNull(message = "El estado activo es obligatorio")
    private Boolean estadoActivo = true;

    public Promocion() {
        this.imagenes = new ArrayList<>();
        this.detallesPromocion = new HashSet<>();
        this.sucursales = new HashSet<>();
    }

    // Getters y Setters
    public Integer getId() { return id; }
    public void setId(Integer id) { this.id = id; }
    public String getDenominacion() { return denominacion; }
    public void setDenominacion(String denominacion) { this.denominacion = denominacion; }
    public LocalDate getFechaDesde() { return fechaDesde; }
    public void setFechaDesde(LocalDate fechaDesde) { this.fechaDesde = fechaDesde; }
    public LocalDate getFechaHasta() { return fechaHasta; }
    public void setFechaHasta(LocalDate fechaHasta) { this.fechaHasta = fechaHasta; }
    public LocalTime getHoraDesde() { return horaDesde; }
    public void setHoraDesde(LocalTime horaDesde) { this.horaDesde = horaDesde; }
    public LocalTime getHoraHasta() { return horaHasta; }
    public void setHoraHasta(LocalTime horaHasta) { this.horaHasta = horaHasta; }
    public String getDescripcionDescuento() { return descripcionDescuento; }
    public void setDescripcionDescuento(String descripcionDescuento) { this.descripcionDescuento = descripcionDescuento; }
    public Double getPrecioPromocional() { return precioPromocional; }
    public void setPrecioPromocional(Double precioPromocional) { this.precioPromocional = precioPromocional; }

    public Double getPorcentajeDescuento() { return porcentajeDescuento; }
    public void setPorcentajeDescuento(Double porcentajeDescuento) { this.porcentajeDescuento = porcentajeDescuento; }
    public TipoPromocion getTipoPromocion() { return tipoPromocion; }
    public void setTipoPromocion(TipoPromocion tipoPromocion) { this.tipoPromocion = tipoPromocion; }


    public List<Imagen> getImagenes() { return imagenes; }
    public void setImagenes(List<Imagen> imagenes) { this.imagenes = imagenes; }

    public Set<PromocionDetalle> getDetallesPromocion() { return detallesPromocion; }
    public void setDetallesPromocion(Set<PromocionDetalle> detallesPromocion) { this.detallesPromocion = detallesPromocion; }

    public Set<Sucursal> getSucursales() { return sucursales; }
    public void setSucursales(Set<Sucursal> sucursales) { this.sucursales = sucursales; }


    public Boolean getEstadoActivo() { return estadoActivo; }
    public void setEstadoActivo(Boolean estadoActivo) { this.estadoActivo = estadoActivo; }

    // Métodos Helper
    public void addImagen(Imagen imagen) {
        if (this.imagenes == null) this.imagenes = new ArrayList<>();
        this.imagenes.add(imagen);
        imagen.setPromocion(this);
    }
    public void removeImagen(Imagen imagen) {
        if (this.imagenes != null) {
            this.imagenes.remove(imagen);
            imagen.setPromocion(null);
        }
    }
    public void addDetallePromocion(PromocionDetalle detalle) {
        if (this.detallesPromocion == null) this.detallesPromocion = new HashSet<>();
        if (this.detallesPromocion.add(detalle)) {
            detalle.setPromocion(this);
        }
    }
    public void removeDetallePromocion(PromocionDetalle detalle) {
        if (this.detallesPromocion != null && this.detallesPromocion.remove(detalle)) {
            detalle.setPromocion(null);
        }
    }
    public void addSucursal(Sucursal sucursal) {
        if (this.sucursales == null) this.sucursales = new HashSet<>();
        if (this.sucursales.add(sucursal)) {
            if (sucursal.getPromociones() == null) {
                sucursal.setPromociones(new ArrayList<>());
            }
            sucursal.getPromociones().add(this);
        }
    }

    public void removeSucursal(Sucursal sucursal) {
        if (this.sucursales != null && this.sucursales.remove(sucursal)) {
            if (sucursal.getPromociones() != null) {
                sucursal.getPromociones().remove(this);
            }
        }
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Promocion promocion = (Promocion) o;
        return Objects.equals(id, promocion.id);
    }

    @Override
    public int hashCode() { return Objects.hash(id); }

    @Override
    public String toString() {
        // VERSÍON SIMPLIFICADA PARA DEPURAR EL ERROR
        return "Promocion{" +
                "id=" + id +
                ", denominacion='" + denominacion + '\'' +
                ", tipoPromocion=" + tipoPromocion +
                ", estadoActivo=" + estadoActivo +
                '}';
    }
}
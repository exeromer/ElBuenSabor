package com.powerRanger.ElBuenSabor.entities;

import jakarta.persistence.*;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

@Entity
public class Promocion {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(nullable = true)
    private String denominacion;

    @Column(nullable = true)
    private LocalDate fechaDesde;

    @Column(nullable = true)
    private LocalDate fechaHasta;

    @Column(nullable = true)
    private LocalTime horaDesde;

    @Column(nullable = true)
    private LocalTime horaHasta;

    @Column(nullable = true)
    private String descripcionDescuento;

    @Column(nullable = true)
    private Double precioPromocional;

    @OneToMany(mappedBy = "promocion", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Imagen> imagenes = new ArrayList<>(); // Inicializado

    // NUEVA RELACIÓN CON PromocionDetalle
    @OneToMany(mappedBy = "promocion", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<PromocionDetalle> detallesPromocion = new ArrayList<>();


    @Column(name = "estadoActivo")
    private Boolean estadoActivo;

    public Promocion() {
    }

    public Promocion(String denominacion, LocalDate fechaDesde, LocalDate fechaHasta, LocalTime horaDesde, LocalTime horaHasta, String descripcionDescuento, Double precioPromocional, Boolean estadoActivo) {
        this.denominacion = denominacion;
        this.fechaDesde = fechaDesde;
        this.fechaHasta = fechaHasta;
        this.horaDesde = horaDesde;
        this.horaHasta = horaHasta;
        this.descripcionDescuento = descripcionDescuento;
        this.precioPromocional = precioPromocional;
        this.estadoActivo = estadoActivo;
    }

    // Getters y Setters (ya los tenías, los mantengo)
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

    public LocalDate getFechaDesde() {
        return fechaDesde;
    }

    public void setFechaDesde(LocalDate fechaDesde) {
        this.fechaDesde = fechaDesde;
    }

    public LocalDate getFechaHasta() {
        return fechaHasta;
    }

    public void setFechaHasta(LocalDate fechaHasta) {
        this.fechaHasta = fechaHasta;
    }

    public LocalTime getHoraDesde() {
        return horaDesde;
    }

    public void setHoraDesde(LocalTime horaDesde) {
        this.horaDesde = horaDesde;
    }

    public LocalTime getHoraHasta() {
        return horaHasta;
    }

    public void setHoraHasta(LocalTime horaHasta) {
        this.horaHasta = horaHasta;
    }

    public String getDescripcionDescuento() {
        return descripcionDescuento;
    }

    public void setDescripcionDescuento(String descripcionDescuento) {
        this.descripcionDescuento = descripcionDescuento;
    }

    public Double getPrecioPromocional() {
        return precioPromocional;
    }

    public void setPrecioPromocional(Double precioPromocional) {
        this.precioPromocional = precioPromocional;
    }

    public List<Imagen> getImagenes() {
        return imagenes;
    }

    public void setImagenes(List<Imagen> imagenes) {
        this.imagenes = imagenes;
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
        Promocion promocion = (Promocion) o;
        return Objects.equals(id, promocion.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }

    @Override
    public String toString() {
        return "Promocion{" +
                "id=" + id +
                ", denominacion='" + denominacion + '\'' +
                ", fechaDesde=" + fechaDesde +
                ", fechaHasta=" + fechaHasta +
                ", precioPromocional=" + precioPromocional +
                ", estadoActivo=" + estadoActivo +
                '}';
    }
}
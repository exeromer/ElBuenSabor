package com.powerRanger.ElBuenSabor.entities;

import jakarta.persistence.*;
import java.util.Objects;

@Entity
@Table(name = "factura_detalle") // Nombre de la tabla en la base de datos
public class FacturaDetalle {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(nullable = false)
    private Integer cantidad;

    @Column(name = "denominacion_articulo", nullable = false)
    private String denominacionArticulo; // Denominación del artículo al momento de facturar

    @Column(name = "precio_unitario_articulo", nullable = false)
    private Double precioUnitarioArticulo; // Precio unitario al momento de facturar

    @Column(name = "subtotal", nullable = false)
    private Double subTotal; // Subtotal calculado para esta línea (cantidad * precioUnitarioArticulo)

    @ManyToOne(optional = false) // Un detalle de factura siempre pertenece a una factura
    @JoinColumn(name = "factura_id")
    private Factura factura;

    @ManyToOne(optional = true) // El artículo original, puede ser null si el artículo se borra después
    @JoinColumn(name = "articulo_id", nullable = true)
    private Articulo articulo; // Referencia al artículo original (opcional para consulta)

    // Constructores
    public FacturaDetalle() {
    }

    public FacturaDetalle(Integer cantidad, String denominacionArticulo, Double precioUnitarioArticulo, Double subTotal, Factura factura, Articulo articulo) {
        this.cantidad = cantidad;
        this.denominacionArticulo = denominacionArticulo;
        this.precioUnitarioArticulo = precioUnitarioArticulo;
        this.subTotal = subTotal;
        this.factura = factura;
        this.articulo = articulo;
    }

    // Getters y Setters
    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public Integer getCantidad() {
        return cantidad;
    }

    public void setCantidad(Integer cantidad) {
        this.cantidad = cantidad;
    }

    public String getDenominacionArticulo() {
        return denominacionArticulo;
    }

    public void setDenominacionArticulo(String denominacionArticulo) {
        this.denominacionArticulo = denominacionArticulo;
    }

    public Double getPrecioUnitarioArticulo() {
        return precioUnitarioArticulo;
    }

    public void setPrecioUnitarioArticulo(Double precioUnitarioArticulo) {
        this.precioUnitarioArticulo = precioUnitarioArticulo;
    }

    public Double getSubTotal() {
        return subTotal;
    }

    public void setSubTotal(Double subTotal) {
        this.subTotal = subTotal;
    }

    public Factura getFactura() {
        return factura;
    }

    public void setFactura(Factura factura) {
        this.factura = factura;
    }

    public Articulo getArticulo() {
        return articulo;
    }

    public void setArticulo(Articulo articulo) {
        this.articulo = articulo;
    }

    // equals, hashCode, toString
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        FacturaDetalle that = (FacturaDetalle) o;
        return Objects.equals(id, that.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }

    @Override
    public String toString() {
        return "FacturaDetalle{" +
                "id=" + id +
                ", cantidad=" + cantidad +
                ", denominacionArticulo='" + denominacionArticulo + '\'' +
                ", precioUnitarioArticulo=" + precioUnitarioArticulo +
                ", subTotal=" + subTotal +
                ", facturaId=" + (factura != null ? factura.getId() : "null") +
                ", articuloId=" + (articulo != null ? articulo.getId() : "null") +
                '}';
    }
}
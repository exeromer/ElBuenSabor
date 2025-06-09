package com.powerRanger.ElBuenSabor.entities;

import jakarta.persistence.*;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

@Entity
public class ArticuloInsumo extends Articulo {

    @Column(nullable = true)
    @Min(value = 0, message = "El precio de compra no puede ser negativo")
    private Double precioCompra;

    @Column(nullable = true)
    @Min(value = 0, message = "El stock actual no puede ser negativo")
    private Double stockActual;

    @Column(nullable = true)
    @Min(value = 0, message = "El stock máximo no puede ser negativo")
    private Double stockMinimo;

    @Column(nullable = false)
    @NotNull(message = "Debe especificarse si es para elaborar")
    private Boolean esParaElaborar;


    public ArticuloInsumo() {
        super();
    }

    public ArticuloInsumo(String denominacion, Double precioVenta, UnidadMedida unidadMedida,
                          Categoria categoria, Boolean estadoActivo, Double precioCompra,
                          Double stockActual, Double stockMinimo, Boolean esParaElaborar) {
        super(denominacion, precioVenta, unidadMedida, categoria, estadoActivo);
        this.precioCompra = precioCompra;
        this.stockActual = stockActual;
        this.stockMinimo = stockMinimo;
        this.esParaElaborar = esParaElaborar;
    }

    // Getters y Setters
    public Double getPrecioCompra() { return precioCompra; }
    public void setPrecioCompra(Double precioCompra) { this.precioCompra = precioCompra; }
    public Double getStockActual() { return stockActual; } // Devuelve Double
    public void setStockActual(Double stockActual) { this.stockActual = stockActual; } // Acepta Double
    public Double getstockMinimo() { return stockMinimo; } // Devuelve Double
    public void setstockMinimo(Double stockMinimo) { this.stockMinimo = stockMinimo; } // Acepta Double
    public Boolean getEsParaElaborar() { return esParaElaborar; }
    public void setEsParaElaborar(Boolean esParaElaborar) { this.esParaElaborar = esParaElaborar; }

    @Override
    public String toString() {
        // Llama al toString de la superclase y añade los campos específicos de ArticuloInsumo
        return "ArticuloInsumo{" +
                super.toString() + // Incluye los campos de Articulo
                ", precioCompra=" + precioCompra +
                ", stockActual=" + stockActual +
                ", stockMinimo=" + stockMinimo +
                ", esParaElaborar=" + esParaElaborar +
                '}';
    }

    // equals y hashCode: Los heredados de Articulo (basados en ID) son generalmente suficientes
    // ya que un ArticuloInsumo sigue siendo un Articulo identificado por su ID único.
    // No es necesario sobreescribirlos a menos que tengas una razón de negocio específica.
}
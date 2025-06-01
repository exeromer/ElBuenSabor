package com.powerRanger.ElBuenSabor.entities;

import jakarta.persistence.*;
import jakarta.validation.constraints.Min;    // Para números
import jakarta.validation.constraints.NotNull; // Para objetos y Boolean
// No necesitas importar Size o Objects aquí si no los usas directamente en esta subclase.
// Lombok fue eliminado, lo cual está bien ya que estamos definiendo getters/setters manualmente.

@Entity
// @JsonIdentityInfo ya está en la superclase Articulo, por lo que se hereda.
// No es necesario repetirlo aquí a menos que quieras una configuración diferente para la subclase.
public class ArticuloInsumo extends Articulo {

    @Column(nullable = true) // Considera si el precio de compra puede ser nulo
    @Min(value = 0, message = "El precio de compra no puede ser negativo") // Permite 0 si es posible
    private Double precioCompra;

    @Column(nullable = true) // Considera si el stock puede ser nulo
    @Min(value = 0, message = "El stock actual no puede ser negativo")
    private Double stockActual; // Cambiado a Double para consistencia con precioCompra y para permitir decimales

    @Column(nullable = true) // Considera si el stock puede ser nulo
    @Min(value = 0, message = "El stock máximo no puede ser negativo")
    private Double stockMinimo; // Cambiado a Double

    @Column(nullable = false) // Correcto si siempre debe tener un valor
    @NotNull(message = "Debe especificarse si es para elaborar")
    private Boolean esParaElaborar;

    // Constructores
    public ArticuloInsumo() {
        super(); // Llama al constructor de Articulo
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
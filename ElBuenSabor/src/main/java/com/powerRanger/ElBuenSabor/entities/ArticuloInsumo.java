package com.powerRanger.ElBuenSabor.entities;

import jakarta.persistence.*;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import java.util.ArrayList;
import java.util.List;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@Entity
@JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
public class ArticuloInsumo extends Articulo {

    @Column(nullable = true)
    @Min(value = 0, message = "El precio de compra no puede ser negativo")
    private Double precioCompra;

    @Column(nullable = false)
    @NotNull(message = "Debe especificarse si es para elaborar")
    private Boolean esParaElaborar;

    // Relación OneToMany con StockInsumoSucursal
    // mappedBy indica que 'articuloInsumo' es el campo en StockInsumoSucursal que mapea esta relación.
    // CascadeType.ALL significa que las operaciones de persistencia (guardar, actualizar, eliminar)
    // se propagarán a las entidades StockInsumoSucursal asociadas.
    // orphanRemoval = true significa que si un StockInsumoSucursal se desvincula de este ArticuloInsumo
    // (ej. se elimina de la lista `stockPorSucursal`), se borrará de la base de datos.
    // FetchType.LAZY es la estrategia de carga por defecto y recomendada para colecciones.
    @OneToMany(mappedBy = "articuloInsumo", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    private List<StockInsumoSucursal> stockPorSucursal = new ArrayList<>();

    public ArticuloInsumo() {
        super();
    }

    // Constructor actualizado sin stockActual y stockMinimo
    public ArticuloInsumo(String denominacion, Double precioVenta, UnidadMedida unidadMedida,
                          Categoria categoria, Boolean estadoActivo, Double precioCompra,
                          Boolean esParaElaborar) {
        super(denominacion, precioVenta, unidadMedida, categoria, estadoActivo);
        this.precioCompra = precioCompra;
        this.esParaElaborar = esParaElaborar;
        // Inicializar la lista aquí también si no se hace en la declaración del campo
        this.stockPorSucursal = new ArrayList<>();
    }

    // Getters y Setters
    public Double getPrecioCompra() { return precioCompra; }
    public void setPrecioCompra(Double precioCompra) { this.precioCompra = precioCompra; }

    public Boolean getEsParaElaborar() { return esParaElaborar; }
    public void setEsParaElaborar(Boolean esParaElaborar) { this.esParaElaborar = esParaElaborar; }

    public List<StockInsumoSucursal> getStockPorSucursal() {
        return stockPorSucursal;
    }

    public void setStockPorSucursal(List<StockInsumoSucursal> stockPorSucursal) {
        this.stockPorSucursal = stockPorSucursal;
    }

    // Métodos Helper para gestionar la relación bidireccional con StockInsumoSucursal
    public void addStockInsumoSucursal(StockInsumoSucursal stock) {
        if (this.stockPorSucursal == null) {
            this.stockPorSucursal = new ArrayList<>();
        }
        this.stockPorSucursal.add(stock);
        stock.setArticuloInsumo(this); // Establece la relación bidireccional
    }

    public void removeStockInsumoSucursal(StockInsumoSucursal stock) {
        if (this.stockPorSucursal != null) {
            this.stockPorSucursal.remove(stock);
            stock.setArticuloInsumo(null); // Rompe la relación bidireccional
        }
    }

    @Override
    public String toString() {
        return "ArticuloInsumo{" +
                super.toString() +
                ", precioCompra=" + precioCompra +
                ", esParaElaborar=" + esParaElaborar +
                '}';
    }
}
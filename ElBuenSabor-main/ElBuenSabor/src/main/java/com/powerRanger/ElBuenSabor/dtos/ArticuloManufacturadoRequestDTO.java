package com.powerRanger.ElBuenSabor.dtos;

import jakarta.validation.Valid;
import jakarta.validation.constraints.*;

import java.util.List;
import java.util.ArrayList;

public class ArticuloManufacturadoRequestDTO {

    // Campos heredados de Articulo
    @NotEmpty(message = "La denominación no puede estar vacía")
    private String denominacion;

    @NotNull(message = "El precio de venta no puede ser nulo")
    @DecimalMin(value = "0.01", message = "El precio de venta debe ser mayor que 0")
    private Double precioVenta;

    @NotNull(message = "El ID de la unidad de medida es obligatorio")
    private Integer unidadMedidaId;

    @NotNull(message = "El ID de la categoría es obligatorio")
    private Integer categoriaId;

    @NotNull(message = "El estado activo es obligatorio")
    private Boolean estadoActivo;

    // Campos específicos de ArticuloManufacturado
    @Size(max = 1000, message = "La descripción no puede exceder los 1000 caracteres")
    private String descripcion;

    @NotNull(message = "El tiempo estimado en minutos es obligatorio")
    @Min(value = 1, message = "El tiempo estimado debe ser de al menos 1 minuto")
    private Integer tiempoEstimadoMinutos;

    @NotEmpty(message = "La preparación no puede estar vacía")
    private String preparacion;

    @Valid // Para que se validen los DTOs de detalle anidados
    private List<ArticuloManufacturadoDetalleDTO> manufacturadoDetalles = new ArrayList<>();

    // Getters y Setters
    public String getDenominacion() { return denominacion; }
    public void setDenominacion(String denominacion) { this.denominacion = denominacion; }
    public Double getPrecioVenta() { return precioVenta; }
    public void setPrecioVenta(Double precioVenta) { this.precioVenta = precioVenta; }
    public Integer getUnidadMedidaId() { return unidadMedidaId; }
    public void setUnidadMedidaId(Integer unidadMedidaId) { this.unidadMedidaId = unidadMedidaId; }
    public Integer getCategoriaId() { return categoriaId; }
    public void setCategoriaId(Integer categoriaId) { this.categoriaId = categoriaId; }
    public Boolean getEstadoActivo() { return estadoActivo; }
    public void setEstadoActivo(Boolean estadoActivo) { this.estadoActivo = estadoActivo; }
    public String getDescripcion() { return descripcion; }
    public void setDescripcion(String descripcion) { this.descripcion = descripcion; }
    public Integer getTiempoEstimadoMinutos() { return tiempoEstimadoMinutos; }
    public void setTiempoEstimadoMinutos(Integer tiempoEstimadoMinutos) { this.tiempoEstimadoMinutos = tiempoEstimadoMinutos; }
    public String getPreparacion() { return preparacion; }
    public void setPreparacion(String preparacion) { this.preparacion = preparacion; }
    public List<ArticuloManufacturadoDetalleDTO> getManufacturadoDetalles() { return manufacturadoDetalles; }
    public void setManufacturadoDetalles(List<ArticuloManufacturadoDetalleDTO> manufacturadoDetalles) { this.manufacturadoDetalles = manufacturadoDetalles; }
}
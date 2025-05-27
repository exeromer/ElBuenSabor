package com.powerRanger.ElBuenSabor.dtos;

import com.fasterxml.jackson.annotation.JsonFormat;
import jakarta.validation.Valid;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.FutureOrPresent;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;

public class PromocionRequestDTO {

    @NotEmpty(message = "La denominación no puede estar vacía")
    @Size(max = 255)
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

    @Size(max = 1000)
    private String descripcionDescuento;

    @DecimalMin(value = "0.0", message = "El precio promocional no puede ser negativo")
    private Double precioPromocional; // Puede ser null

    private List<Integer> imagenIds = new ArrayList<>(); // IDs de Imagenes existentes a asociar

    @Valid // Para validar los DTOs de detalle anidados
    private List<PromocionDetalleRequestDTO> detallesPromocion = new ArrayList<>();

    @NotNull(message = "El estado activo es obligatorio")
    private Boolean estadoActivo = true;

    // Getters y Setters
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
    public List<Integer> getImagenIds() { return imagenIds; }
    public void setImagenIds(List<Integer> imagenIds) { this.imagenIds = imagenIds; }
    public List<PromocionDetalleRequestDTO> getDetallesPromocion() { return detallesPromocion; }
    public void setDetallesPromocion(List<PromocionDetalleRequestDTO> detallesPromocion) { this.detallesPromocion = detallesPromocion; }
    public Boolean getEstadoActivo() { return estadoActivo; }
    public void setEstadoActivo(Boolean estadoActivo) { this.estadoActivo = estadoActivo; }
}
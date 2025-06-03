package com.powerRanger.ElBuenSabor.dtos;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.powerRanger.ElBuenSabor.entities.enums.Estado;
import com.powerRanger.ElBuenSabor.entities.enums.FormaPago;
import com.powerRanger.ElBuenSabor.entities.enums.TipoEnvio;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;

public class PedidoResponseDTO {
    private Integer id;
    @JsonFormat(pattern = "HH:mm:ss")
    private LocalTime horaEstimadaFinalizacion;
    private Double total; // Este sería el total DESPUÉS de descuentos
    private Double subTotalPedido; // Subtotal ANTES de descuentos
    private Double descuentoAplicado; // Monto del descuento
    private Double totalCosto;
    @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDate fechaPedido;
    private Estado estado;
    private TipoEnvio tipoEnvio;
    private FormaPago formaPago;
    private Boolean estadoActivo;
    private LocalDate fechaBaja;

    private SucursalResponseDTO sucursal;    // DTO
    private DomicilioResponseDTO domicilio;  // DTO
    private ClienteResponseDTO cliente;     // DTO (el completo que ya tienes)

    private List<DetallePedidoResponseDTO> detalles = new ArrayList<>();

    // Campos para Mercado Pago
    private String mercadoPagoPaymentId;
    private String mercadoPagoPreferenceId; // Podría ser útil devolverlo al cliente
    private String mercadoPagoPaymentStatus;


    // Getters y Setters
    public Integer getId() { return id; }
    public void setId(Integer id) { this.id = id; }
    public LocalTime getHoraEstimadaFinalizacion() { return horaEstimadaFinalizacion; }
    public void setHoraEstimadaFinalizacion(LocalTime horaEstimadaFinalizacion) { this.horaEstimadaFinalizacion = horaEstimadaFinalizacion; }
    public Double getTotal() { return total; }
    public void setTotal(Double total) { this.total = total; }
    public Double getSubTotalPedido() { return subTotalPedido; }
    public void setSubTotalPedido(Double subTotalPedido) { this.subTotalPedido = subTotalPedido;}
    public Double getDescuentoAplicado() { return descuentoAplicado; }
    public void setDescuentoAplicado(Double descuentoAplicado) { this.descuentoAplicado = descuentoAplicado; }
    public Double getTotalCosto() { return totalCosto; }
    public void setTotalCosto(Double totalCosto) { this.totalCosto = totalCosto; }
    public LocalDate getFechaPedido() { return fechaPedido; }
    public void setFechaPedido(LocalDate fechaPedido) { this.fechaPedido = fechaPedido; }
    public Estado getEstado() { return estado; }
    public void setEstado(Estado estado) { this.estado = estado; }
    public TipoEnvio getTipoEnvio() { return tipoEnvio; }
    public void setTipoEnvio(TipoEnvio tipoEnvio) { this.tipoEnvio = tipoEnvio; }
    public FormaPago getFormaPago() { return formaPago; }
    public void setFormaPago(FormaPago formaPago) { this.formaPago = formaPago; }
    public Boolean getEstadoActivo() { return estadoActivo; }
    public void setEstadoActivo(Boolean estadoActivo) { this.estadoActivo = estadoActivo; }
    public LocalDate getFechaBaja() { return fechaBaja; }
    public void setFechaBaja(LocalDate fechaBaja) { this.fechaBaja = fechaBaja; }
    public SucursalResponseDTO getSucursal() { return sucursal; }
    public void setSucursal(SucursalResponseDTO sucursal) { this.sucursal = sucursal; }
    public DomicilioResponseDTO getDomicilio() { return domicilio; }
    public void setDomicilio(DomicilioResponseDTO domicilio) { this.domicilio = domicilio; }
    public ClienteResponseDTO getCliente() { return cliente; }
    public void setCliente(ClienteResponseDTO cliente) { this.cliente = cliente; }
    public List<DetallePedidoResponseDTO> getDetalles() { return detalles; }
    public void setDetalles(List<DetallePedidoResponseDTO> detalles) { this.detalles = detalles; }

    // Getters y Setters para Mercado Pago
    public String getMercadoPagoPaymentId() { return mercadoPagoPaymentId; }
    public void setMercadoPagoPaymentId(String mercadoPagoPaymentId) { this.mercadoPagoPaymentId = mercadoPagoPaymentId; }
    public String getMercadoPagoPreferenceId() { return mercadoPagoPreferenceId; }
    public void setMercadoPagoPreferenceId(String mercadoPagoPreferenceId) { this.mercadoPagoPreferenceId = mercadoPagoPreferenceId; }
    public String getMercadoPagoPaymentStatus() { return mercadoPagoPaymentStatus; }
    public void setMercadoPagoPaymentStatus(String mercadoPagoPaymentStatus) { this.mercadoPagoPaymentStatus = mercadoPagoPaymentStatus; }
}
package com.powerRanger.ElBuenSabor.dtos;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.powerRanger.ElBuenSabor.entities.enums.EstadoFactura;
import com.powerRanger.ElBuenSabor.entities.enums.FormaPago;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

public class FacturaResponseDTO {
    private Integer id;
    @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDate fechaFacturacion;
    private Integer mpPaymentId;
    private Integer mpMerchantOrderId;
    private String mpPreferenceId;
    private String mpPaymentType;
    private Double totalVenta;
    private FormaPago formaPago;
    private EstadoFactura estadoFactura;
    @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDate fechaAnulacion;

    private PedidoSimpleResponseDTO pedido; // DTO simple del pedido
    private List<FacturaDetalleResponseDTO> detallesFactura = new ArrayList<>();

    // Getters y Setters
    public Integer getId() { return id; }
    public void setId(Integer id) { this.id = id; }
    public LocalDate getFechaFacturacion() { return fechaFacturacion; }
    public void setFechaFacturacion(LocalDate fechaFacturacion) { this.fechaFacturacion = fechaFacturacion; }
    public Integer getMpPaymentId() { return mpPaymentId; }
    public void setMpPaymentId(Integer mpPaymentId) { this.mpPaymentId = mpPaymentId; }
    public Integer getMpMerchantOrderId() { return mpMerchantOrderId; }
    public void setMpMerchantOrderId(Integer mpMerchantOrderId) { this.mpMerchantOrderId = mpMerchantOrderId; }
    public String getMpPreferenceId() { return mpPreferenceId; }
    public void setMpPreferenceId(String mpPreferenceId) { this.mpPreferenceId = mpPreferenceId; }
    public String getMpPaymentType() { return mpPaymentType; }
    public void setMpPaymentType(String mpPaymentType) { this.mpPaymentType = mpPaymentType; }
    public Double getTotalVenta() { return totalVenta; }
    public void setTotalVenta(Double totalVenta) { this.totalVenta = totalVenta; }
    public FormaPago getFormaPago() { return formaPago; }
    public void setFormaPago(FormaPago formaPago) { this.formaPago = formaPago; }
    public EstadoFactura getEstadoFactura() { return estadoFactura; }
    public void setEstadoFactura(EstadoFactura estadoFactura) { this.estadoFactura = estadoFactura; }
    public LocalDate getFechaAnulacion() { return fechaAnulacion; }
    public void setFechaAnulacion(LocalDate fechaAnulacion) { this.fechaAnulacion = fechaAnulacion; }
    public PedidoSimpleResponseDTO getPedido() { return pedido; }
    public void setPedido(PedidoSimpleResponseDTO pedido) { this.pedido = pedido; }
    public List<FacturaDetalleResponseDTO> getDetallesFactura() { return detallesFactura; }
    public void setDetallesFactura(List<FacturaDetalleResponseDTO> detallesFactura) { this.detallesFactura = detallesFactura; }
}
package com.powerRanger.ElBuenSabor.dtos;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.powerRanger.ElBuenSabor.entities.enums.EstadoFactura;
import com.powerRanger.ElBuenSabor.entities.enums.FormaPago;
import org.apache.juli.logging.Log;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

public class FacturaResponseDTO {
    private Integer id;
    @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDate fechaFacturacion;
    private Long mpPaymentId;
    private Long mpMerchantOrderId;
    private String mpPreferenceId;
    private String mpPaymentType;
    private Double totalVenta;
    private Double subtotal;
    private Double totalDescuentos;
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
    public Long getMpPaymentId() { return mpPaymentId; }
    public void setMpPaymentId(Long mpPaymentId) { this.mpPaymentId = mpPaymentId; }
    public Long getMpMerchantOrderId() { return mpMerchantOrderId; }
    public void setMpMerchantOrderId(Long mpMerchantOrderId) { this.mpMerchantOrderId = mpMerchantOrderId; }
    public String getMpPreferenceId() { return mpPreferenceId; }
    public void setMpPreferenceId(String mpPreferenceId) { this.mpPreferenceId = mpPreferenceId; }
    public String getMpPaymentType() { return mpPaymentType; }
    public void setMpPaymentType(String mpPaymentType) { this.mpPaymentType = mpPaymentType; }
    public Double getTotalVenta() { return totalVenta; }
    public void setTotalVenta(Double totalVenta) { this.totalVenta = totalVenta; }
    public FormaPago getFormaPago() { return formaPago; }
    public Double getSubtotal() {return subtotal;}
    public void setSubtotal(Double subtotal) {this.subtotal = subtotal;}
    public Double getTotalDescuentos() {return totalDescuentos;}
    public void setTotalDescuentos(Double totalDescuentos) {this.totalDescuentos = totalDescuentos;}
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
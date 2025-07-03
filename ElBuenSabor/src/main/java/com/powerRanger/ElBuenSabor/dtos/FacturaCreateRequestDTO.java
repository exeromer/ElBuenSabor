package com.powerRanger.ElBuenSabor.dtos;

import jakarta.validation.constraints.NotNull;

public class FacturaCreateRequestDTO {
    @NotNull(message = "El ID del pedido es requerido para generar la factura")
    private Integer pedidoId;

    // Otros campos opcionales que podrían venir al momento de generar la factura,
    // como datos de MercadoPago, si no se obtienen directamente del pedido o de otra fuente.
    private Long mpPaymentId;
    private Long mpMerchantOrderId;
    private String mpPreferenceId;
    private String mpPaymentType;

    public Integer getPedidoId() { return pedidoId; }
    public void setPedidoId(Integer pedidoId) { this.pedidoId = pedidoId; }
    public Long getMpPaymentId() { return mpPaymentId; }
    public void setMpPaymentId(Long mpPaymentId) { this.mpPaymentId = mpPaymentId; }
    public Long getMpMerchantOrderId() { return mpMerchantOrderId; }
    public void setMpMerchantOrderId(Long mpMerchantOrderId) { this.mpMerchantOrderId = mpMerchantOrderId; }
    public String getMpPreferenceId() { return mpPreferenceId; }
    public void setMpPreferenceId(String mpPreferenceId) { this.mpPreferenceId = mpPreferenceId; }
    public String getMpPaymentType() { return mpPaymentType; }
    public void setMpPaymentType(String mpPaymentType) { this.mpPaymentType = mpPaymentType; }
}
package com.powerRanger.ElBuenSabor.dtos;

import jakarta.validation.constraints.NotNull;

public class MercadoPagoCreatePreferenceDTO {
    @NotNull(message = "El ID del pedido es requerido para generar la preferencia de Mercado Pago.")
    private Integer pedidoId;

    // Getters y Setters
    public Integer getPedidoId() {
        return pedidoId;
    }

    public void setPedidoId(Integer pedidoId) {
        this.pedidoId = pedidoId;
    }
}
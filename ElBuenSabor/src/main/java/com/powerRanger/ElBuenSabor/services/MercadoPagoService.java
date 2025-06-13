package com.powerRanger.ElBuenSabor.services;

import com.mercadopago.exceptions.MPException;
import com.powerRanger.ElBuenSabor.entities.Pedido;

public interface MercadoPagoService {
    String crearPreferenciaPago(Pedido pedido) throws MPException;
}
package com.powerRanger.ElBuenSabor.services;

import com.mercadopago.exceptions.MPApiException;
import com.mercadopago.exceptions.MPException;
import com.powerRanger.ElBuenSabor.entities.Pedido;

public interface MercadoPagoService {
    // Añadimos las excepciones que el método puede lanzar
    String crearPreferenciaPago(Pedido pedido) throws MPException, MPApiException;
}
package com.powerRanger.ElBuenSabor.services;

import com.mercadopago.exceptions.MPApiException;
import com.mercadopago.exceptions.MPException;
import com.mercadopago.resources.merchantorder.MerchantOrder;
import com.mercadopago.client.payment.PaymentClient;
import com.mercadopago.resources.payment.Payment;
import com.powerRanger.ElBuenSabor.entities.Pedido;

public interface MercadoPagoService {
    // Añadimos las excepciones que el método puede lanzar
    String crearPreferenciaPago(Pedido pedido) throws MPException, MPApiException;
    MerchantOrder getMerchantOrder(Long merchantOrderId) throws MPException, MPApiException;

    Payment getPaymentById(Long paymentId) throws MPException, MPApiException;

}
package com.powerRanger.ElBuenSabor.services;

import com.powerRanger.ElBuenSabor.entities.Factura;

public interface EmailService {
    void enviarFacturaPorEmail(Factura factura);
    void enviarNotaDeCreditoPorEmail(Factura facturaAnulada);

}
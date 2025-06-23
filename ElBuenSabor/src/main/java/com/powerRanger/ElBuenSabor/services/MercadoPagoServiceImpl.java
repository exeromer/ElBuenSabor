package com.powerRanger.ElBuenSabor.services;

import com.mercadopago.MercadoPagoConfig;
import com.mercadopago.client.payment.PaymentClient;
import com.mercadopago.client.preference.*;
import com.mercadopago.exceptions.MPApiException;
import com.mercadopago.exceptions.MPException;
import com.mercadopago.resources.payment.Payment;
import com.mercadopago.resources.preference.Preference;
import com.powerRanger.ElBuenSabor.entities.DetallePedido;
import com.powerRanger.ElBuenSabor.entities.Pedido;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import jakarta.annotation.PostConstruct;
import com.mercadopago.MercadoPagoConfig;
import com.mercadopago.client.merchantorder.MerchantOrderClient;
import com.mercadopago.exceptions.MPException;
import com.mercadopago.resources.merchantorder.MerchantOrder;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

@Service
public class MercadoPagoServiceImpl implements MercadoPagoService {

    private static final Logger logger = LoggerFactory.getLogger(MercadoPagoServiceImpl.class);

    // Inyecta el Access Token y las URLs desde application.properties
    @Value("${mp.access-token}")
    private String accessToken;

    @Value("${mercadopago.frontend.success_url}")
    private String successUrl;

    @Value("${mercadopago.frontend.failure_url}")
    private String failureUrl;

    @Value("${mercadopago.frontend.pending_url}")
    private String pendingUrl;

    @PostConstruct
    public void init() {
        System.out.println("MERCADOPAGO_SERVICE: Configurando SDK de Mercado Pago...");
        MercadoPagoConfig.setAccessToken(accessToken);
        System.out.println("MERCADOPAGO_SERVICE: SDK configurado correctamente.");
    }

    @Override
    public String crearPreferenciaPago(Pedido pedido) throws MPException, MPApiException {
        logger.info("MERCADOPAGO_SERVICE: Usando Access Token: {}", accessToken);
        MercadoPagoConfig.setAccessToken(accessToken);

        List<PreferenceItemRequest> items = new ArrayList<>();
        for (DetallePedido detalle : pedido.getDetalles()) {
            String pictureUrl = null;
            if (detalle.getArticulo().getImagenes() != null && !detalle.getArticulo().getImagenes().isEmpty()) {
                pictureUrl = detalle.getArticulo().getImagenes().get(0).getDenominacion();
            }
            PreferenceItemRequest itemRequest = PreferenceItemRequest.builder()
                    .id(detalle.getArticulo().getId().toString())
                    .title(detalle.getArticulo().getDenominacion())
                    .description(detalle.getArticulo().getDenominacion())
                    .pictureUrl(pictureUrl)
                    .quantity(detalle.getCantidad())
                    .currencyId("ARS")
                    .unitPrice(new BigDecimal(detalle.getSubTotal() / detalle.getCantidad()))
                    .build();
            items.add(itemRequest);
            logger.info("Añadiendo item a la preferencia: {}, Cantidad: {}", itemRequest.getTitle(), itemRequest.getQuantity());
        }
        logger.info("VERIFICANDO URLs: success='{}', failure='{}', pending='{}'", this.successUrl, this.failureUrl, this.pendingUrl);

        PreferenceBackUrlsRequest backUrls = PreferenceBackUrlsRequest.builder()
                .success(this.successUrl)
                .failure(this.failureUrl)
                .pending(this.pendingUrl)
                .build();

        // FIX: Separamos la creación de la preferencia de la de las URLs
        PreferenceRequest preferenceRequest = PreferenceRequest.builder()
                .items(items)
                .backUrls(backUrls) // Se asignan las URLs aquí
                .autoReturn("approved")
                .externalReference(pedido.getId().toString())
                .notificationUrl(null) // Opcional: la URL de notificación IPN que configuraste en el panel de MP
                .build();

        PreferenceClient client = new PreferenceClient();
        try {
            Preference preference = client.create(preferenceRequest);
            logger.info("Preferencia creada con ID: {}", preference.getId());
            return preference.getId();
        } catch (MPApiException e) {
            logger.error("Error de API al crear preferencia de Mercado Pago: {}", e.getApiResponse().getContent(), e);
            throw e;
        } catch (MPException e) {
            logger.error("Error en el SDK de Mercado Pago al crear la preferencia: {}", e.getMessage(), e);
            throw e;
        }
    }


    @Override
    public MerchantOrder getMerchantOrder(Long merchantOrderId) throws MPException, MPApiException {
        System.out.println("MERCADOPAGO_SERVICE: Buscando MerchantOrder con ID: " + merchantOrderId);
        MerchantOrderClient client = new MerchantOrderClient();
        return client.get(merchantOrderId);
    }
    @Override
    public Payment getPaymentById(Long paymentId) throws MPException, MPApiException {
        logger.info("MERCADOPAGO_SERVICE: Buscando Pago con ID: {}", paymentId);
        PaymentClient client = new PaymentClient();
        return client.get(paymentId);
    }
}
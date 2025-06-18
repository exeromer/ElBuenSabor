package com.powerRanger.ElBuenSabor.services;

import com.mercadopago.MercadoPagoConfig;
import com.mercadopago.client.preference.*;
import com.mercadopago.exceptions.MPApiException;
import com.mercadopago.exceptions.MPException;
import com.mercadopago.resources.preference.Preference;
import com.powerRanger.ElBuenSabor.entities.DetallePedido;
import com.powerRanger.ElBuenSabor.entities.Pedido;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

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

    @Override
    public String crearPreferenciaPago(Pedido pedido) throws MPException, MPApiException {
        logger.info("MERCADOPAGO_SERVICE: Usando Access Token: {}", accessToken);
        MercadoPagoConfig.setAccessToken(accessToken);

        List<PreferenceItemRequest> items = new ArrayList<>();
        for (DetallePedido detalle : pedido.getDetalles()) {
            PreferenceItemRequest itemRequest = PreferenceItemRequest.builder()
                    .id(detalle.getArticulo().getId().toString())
                    .title(detalle.getArticulo().getDenominacion())
                    .description("Artículo de El Buen Sabor")
                    .pictureUrl(detalle.getArticulo().getImagenes().isEmpty() ? null : detalle.getArticulo().getImagenes().get(0).getDenominacion())
                    .categoryId("food")
                    .quantity(detalle.getCantidad())
                    .currencyId("ARS")
                    .unitPrice(new BigDecimal(detalle.getSubTotal() / detalle.getCantidad()))
                    .build();
            items.add(itemRequest);
            logger.info("Añadiendo item a la preferencia: {}, Cantidad: {}", itemRequest.getTitle(), itemRequest.getQuantity());
        }

        // Usa las URLs inyectadas desde el archivo de propiedades
        PreferenceBackUrlsRequest backUrls = PreferenceBackUrlsRequest.builder()
                .success(this.successUrl)
                .failure(this.failureUrl)
                .pending(this.pendingUrl)
                .build();

        logger.info("Configurando Back URLs: success={}, failure={}, pending={}", this.successUrl, this.failureUrl, this.pendingUrl);

        PreferenceRequest preferenceRequest = PreferenceRequest.builder()
                .items(items)
                .backUrls(backUrls)
                .autoReturn("approved")
                .externalReference(pedido.getId().toString())
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
}
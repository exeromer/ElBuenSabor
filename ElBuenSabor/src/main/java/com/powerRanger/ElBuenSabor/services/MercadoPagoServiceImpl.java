package com.powerRanger.ElBuenSabor.services;

import com.mercadopago.MercadoPagoConfig;
import com.mercadopago.client.preference.PreferenceBackUrlsRequest;
import com.mercadopago.client.preference.PreferenceClient;
import com.mercadopago.client.preference.PreferenceItemRequest;
import com.mercadopago.client.preference.PreferenceRequest;
import com.mercadopago.exceptions.MPApiException;
import com.mercadopago.exceptions.MPException;
import com.mercadopago.resources.preference.Preference;
import com.powerRanger.ElBuenSabor.entities.DetallePedido;
import com.powerRanger.ElBuenSabor.entities.Pedido;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

@Service
public class MercadoPagoServiceImpl implements MercadoPagoService {

    @Value("${mercadopago.access_token}")
    private String mpAccessToken;

    // Estas URLs apuntarán a tu frontend. Cuando el pago se complete, Mercado Pago
    // redirigirá al usuario a estas rutas. Deberás manejarlas en tu AppRoutes.tsx.
    @Value("${mercadopago.frontend.success_url}")
    private String successUrl;

    @Value("${mercadopago.frontend.failure_url}")
    private String failureUrl;

    @Value("${mercadopago.frontend.pending_url}")
    private String pendingUrl;


    @Override
    public String crearPreferenciaPago(Pedido pedido) throws MPException {
        System.out.println("LOG: Iniciando creación de preferencia de pago para Pedido ID: " + pedido.getId());

        try {
            MercadoPagoConfig.setAccessToken(mpAccessToken);

            List<PreferenceItemRequest> items = new ArrayList<>();
            for (DetallePedido detalle : pedido.getDetalles()) {
                PreferenceItemRequest itemRequest = PreferenceItemRequest.builder()
                        .id(detalle.getId().toString())
                        .title(detalle.getArticulo().getDenominacion())
                        .description("Artículo de El Buen Sabor")
                        .pictureUrl(detalle.getArticulo().getImagenes().isEmpty() ? null : detalle.getArticulo().getImagenes().get(0).getDenominacion())
                        .categoryId("food")
                        .quantity(detalle.getCantidad())
                        .currencyId("ARS")
                        .unitPrice(new BigDecimal(detalle.getArticulo().getPrecioVenta()))
                        .build();
                items.add(itemRequest);
                System.out.println("LOG: Añadiendo item a la preferencia: " + itemRequest.getTitle() + ", Cantidad: " + itemRequest.getQuantity());
            }

            PreferenceRequest preferenceRequest = PreferenceRequest.builder()
                    .items(items)
                    .backUrls(PreferenceBackUrlsRequest.builder()
                            .success(successUrl)
                            .failure(failureUrl)
                            .pending(pendingUrl)
                            .build())
                    .autoReturn("approved")
                    .externalReference(pedido.getId().toString()) // Referencia al ID de tu pedido
                    .build();

            PreferenceClient client = new PreferenceClient();
            Preference preference = client.create(preferenceRequest);

            System.out.println("LOG: Preferencia de Mercado Pago creada exitosamente. ID: " + preference.getId());
            return preference.getId();

        } catch (MPApiException apiException) {
            System.err.println("Error de API al crear preferencia de Mercado Pago: " + apiException.getApiResponse().getContent());
            throw new MPException("Error en la API de MercadoPago: " + apiException.getMessage(), apiException);
        } catch (MPException mpException) {
            System.err.println("Error de SDK al crear preferencia de Mercado Pago: " + mpException.getMessage());
            mpException.printStackTrace();
            throw mpException;
        }
    }
}
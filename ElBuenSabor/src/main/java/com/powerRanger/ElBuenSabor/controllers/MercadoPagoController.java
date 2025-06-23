package com.powerRanger.ElBuenSabor.controllers;

import com.powerRanger.ElBuenSabor.entities.Pedido;
import com.powerRanger.ElBuenSabor.repository.PedidoRepository;
import com.powerRanger.ElBuenSabor.services.MercadoPagoService;
import com.powerRanger.ElBuenSabor.services.PedidoService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/mercado-pago")
@CrossOrigin("*") // Permitimos CORS para este controlador
public class MercadoPagoController {

    @Autowired
    private PedidoService pedidoService;
    @Autowired
    private MercadoPagoService mercadoPagoService;

    @Autowired
    private PedidoRepository pedidoRepository;

    // Este endpoint recibirá las notificaciones de Mercado Pago
    @PostMapping(value = "/notificaciones") // Ya no necesita 'consumes' si usamos @RequestParam
    public ResponseEntity<String> recibirNotificacion(@RequestParam Map<String, String> notificacion) {
        System.out.println("====================================================================");
        System.out.println("MERCADOPAGO_NOTIFICATION: Notificación recibida (Query Params):");
        System.out.println(notificacion);
        System.out.println("====================================================================");

        // Extraemos el topic y el ID del recurso de la notificación
        String topic = notificacion.get("topic");
        // FIX: El parámetro es 'id', no 'resource'
        String resourceId = notificacion.get("id");

        // FIX: El topic de prueba es 'payment', no 'merchant_order'
        if ("payment".equals(topic) && resourceId != null) {
            try {
                // Aquí el servicio debería procesar el ID del PAGO, no de la orden.
                // Esto puede requerir un ajuste en tu lógica de servicio.
                // Por ahora, lo dejamos para que puedas probar la recepción.
                System.out.println("Procesando topic 'payment' para el ID de recurso: " + resourceId);
                pedidoService.handleMercadoPagoNotification(notificacion);

            } catch (Exception e) {
                System.err.println("MERCADOPAGO_ERROR: Error al procesar la notificación: " + e.getMessage());
                return ResponseEntity.status(500).body("Error procesando la notificación.");
            }
        }

        return ResponseEntity.ok("Notificacion recibida.");
    }

    @PostMapping("/crear-preferencia-test/{pedidoId}")
    public ResponseEntity<?> crearPreferenciaTest(@PathVariable Integer pedidoId) {
        System.out.println("CONTROLLER_TEST: Solicitud para crear preferencia para Pedido ID: " + pedidoId);
        try {
            // Buscamos el pedido en nuestra BD
            Pedido pedido = pedidoRepository.findById(pedidoId)
                    .orElseThrow(() -> new Exception("Pedido no encontrado con ID: " + pedidoId));

            // Llamamos a nuestro servicio para que cree la preferencia en Mercado Pago
            String preferenciaId = mercadoPagoService.crearPreferenciaPago(pedido);

            // Devolvemos el ID de la preferencia. El frontend usaría este ID para renderizar el botón de pago.
            // Para nuestra prueba, esto confirma que se creó correctamente.
            // NOTA: El SDK v2 devuelve solo el ID. La URL de pago (init_point) se construye en el frontend.
            // Para nuestra prueba, podemos construirla aquí para facilitar.
            String urlPago = "https://www.mercadopago.com.ar/checkout/v1/redirect?pref_id=" + preferenciaId;

            System.out.println("CONTROLLER_TEST: URL de pago generada: " + urlPago);
            return ResponseEntity.ok(Map.of("urlPago", urlPago));

        } catch (Exception e) {
            System.err.println("CONTROLLER_TEST_ERROR: " + e.getMessage());
            e.printStackTrace();
            return ResponseEntity.status(500).body(Map.of("error", e.getMessage()));
        }
    }
}
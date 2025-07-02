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
   @PostMapping(value = "/notificaciones")
    public ResponseEntity<String> recibirNotificacion(@RequestBody Map<String, Object> notificacion) {
    System.out.println("====================================================================");
    System.out.println("MERCADOPAGO_NOTIFICATION: Notificación JSON recibida:");
    System.out.println(notificacion);
    System.out.println("====================================================================");

    try {
        // Simplemente pasamos el mapa completo al servicio.
        // El servicio se encargará de extraer la información necesaria.
        pedidoService.handleMercadoPagoNotification(notificacion);
        return ResponseEntity.ok("Notificacion recibida y procesada.");

    } catch (Exception e) {
        System.err.println("MERCADOPAGO_ERROR: Error al procesar la notificación en el controlador: " + e.getMessage());
        e.printStackTrace(); // Es útil ver el error completo en la consola del backend
        return ResponseEntity.status(500).body("Error procesando la notificación: " + e.getMessage());
    }
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
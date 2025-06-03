package com.powerRanger.ElBuenSabor.controllers;

import com.powerRanger.ElBuenSabor.dtos.CrearPedidoRequestDTO;
import com.powerRanger.ElBuenSabor.dtos.PedidoEstadoRequestDTO;
import com.powerRanger.ElBuenSabor.dtos.PedidoRequestDTO;
import com.powerRanger.ElBuenSabor.dtos.PedidoResponseDTO;
import com.powerRanger.ElBuenSabor.entities.Cliente;
import com.powerRanger.ElBuenSabor.entities.Pedido;
import com.powerRanger.ElBuenSabor.entities.enums.FormaPago;
import com.powerRanger.ElBuenSabor.repository.ClienteRepository;
import com.powerRanger.ElBuenSabor.repository.PedidoRepository;
import com.powerRanger.ElBuenSabor.services.PedidoService;

import com.mercadopago.MercadoPagoConfig;
import com.mercadopago.client.payment.PaymentClient; // Importación correcta para PaymentClient
import com.mercadopago.client.preference.*;
import com.mercadopago.exceptions.MPApiException;
import com.mercadopago.exceptions.MPException;
import com.mercadopago.resources.preference.Preference;
import jakarta.annotation.PostConstruct;


import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import jakarta.validation.Valid;
import jakarta.validation.ConstraintViolationException;


import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/pedidos")
@Validated
public class PedidoController {

    @Autowired
    private PedidoService pedidoService;

    @Autowired
    private ClienteRepository clienteRepository;

    @Autowired
    private PedidoRepository pedidoRepository;

    @Value("${mercadopago.access_token}")
    private String mpAccessToken;

    @Value("${mercadopago.frontend.success_url}")
    private String mpSuccessUrl;
    @Value("${mercadopago.frontend.failure_url}")
    private String mpFailureUrl;
    @Value("${mercadopago.frontend.pending_url}")
    private String mpPendingUrl;
    @Value("${mercadopago.backend.notification_url}")
    private String mpNotificationUrl;


    @PostConstruct
    public void initMercadoPago() {
        if (mpAccessToken != null && !mpAccessToken.isEmpty() && !mpAccessToken.equals("YOUR_TEST_ACCESS_TOKEN")) {
            MercadoPagoConfig.setAccessToken(mpAccessToken);
            System.out.println("INFO: MercadoPago SDK inicializado con Access Token.");
        } else {
            System.err.println("WARN: MercadoPago Access Token no configurado o es el valor por defecto. La integración con MP no funcionará correctamente.");
        }
    }


    @PostMapping
    public ResponseEntity<?> createPedidoForAuthenticatedClient(@Valid @RequestBody PedidoRequestDTO dto, Authentication authentication) {
        try {
            if (authentication == null && dto.getClienteId() == null) {
                throw new Exception("Para crear un pedido sin autenticación (modo prueba), se requiere clienteId en el DTO.");
            }

            PedidoResponseDTO nuevoPedidoDto;
            // ... (lógica de autenticación y creación de pedido)
            if (authentication != null && authentication.getPrincipal() instanceof Jwt) {
                Jwt jwt = (Jwt) authentication.getPrincipal();
                String auth0Id = jwt.getSubject();
                nuevoPedidoDto = pedidoService.createForAuthenticatedClient(auth0Id, dto);
            } else {
                nuevoPedidoDto = pedidoService.create(dto);
            }
            // Considerar la lógica de MP si este endpoint lo requiere.
            return new ResponseEntity<>(nuevoPedidoDto, HttpStatus.CREATED);

        } catch (IllegalArgumentException e) {
            return handleGenericException(e, HttpStatus.BAD_REQUEST);
        } catch (ConstraintViolationException e) {
            return handleConstraintViolation(e);
        } catch (Exception e) {
            return handleGenericException(e, HttpStatus.BAD_REQUEST);
        }
    }

    @PostMapping("/admin")
    public ResponseEntity<?> createPedidoByAdmin(@Valid @RequestBody PedidoRequestDTO dto) {
        try {
            PedidoResponseDTO nuevoPedidoDto = pedidoService.create(dto);
            // Considerar la lógica de MP si este endpoint lo requiere.
            return new ResponseEntity<>(nuevoPedidoDto, HttpStatus.CREATED);
        } catch (IllegalArgumentException e) {
            return handleGenericException(e, HttpStatus.BAD_REQUEST);
        } catch (ConstraintViolationException e) {
            return handleConstraintViolation(e);
        } catch (Exception e) {
            return handleGenericException(e, HttpStatus.BAD_REQUEST);
        }
    }

    @GetMapping
    public ResponseEntity<List<PedidoResponseDTO>> getAllPedidos() {
        try {
            List<PedidoResponseDTO> pedidos = pedidoService.getAll();
            if (pedidos.isEmpty()) {
                return ResponseEntity.noContent().build();
            }
            return ResponseEntity.ok(pedidos);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @GetMapping("/mis-pedidos")
    public ResponseEntity<?> getMisPedidos(Authentication authentication) {
        try {
            if (authentication == null || !(authentication.getPrincipal() instanceof Jwt)) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(Map.of("error", "Se requiere autenticación para ver 'mis pedidos'."));
            }
            Jwt jwt = (Jwt) authentication.getPrincipal();
            String auth0Id = jwt.getSubject();

            List<PedidoResponseDTO> pedidos = pedidoService.getPedidosByClienteAuth0Id(auth0Id);
            if (pedidos.isEmpty()) return ResponseEntity.noContent().build();
            return ResponseEntity.ok(pedidos);
        } catch (Exception e) {
            return handleGenericException(e, HttpStatus.NOT_FOUND);
        }
    }

    @GetMapping("/cliente/{clienteId}")
    public ResponseEntity<?> getPedidosByClienteId(@PathVariable Integer clienteId) {
        try {
            List<PedidoResponseDTO> pedidos = pedidoService.getPedidosByClienteId(clienteId);
            if (pedidos.isEmpty()) return ResponseEntity.noContent().build();
            return ResponseEntity.ok(pedidos);
        } catch (Exception e) {
            return handleGenericException(e, HttpStatus.NOT_FOUND);
        }
    }

    @GetMapping("/{id}")
    public ResponseEntity<?> getPedidoById(@PathVariable Integer id) {
        try {
            PedidoResponseDTO pedidoDto = pedidoService.getById(id);
            return ResponseEntity.ok(pedidoDto);
        } catch (Exception e) {
            return handleGenericException(e, HttpStatus.NOT_FOUND);
        }
    }

    @PostMapping("/cliente/{clienteId}/desde-carrito")
    public ResponseEntity<?> crearPedidoDesdeCarrito(
            @PathVariable Integer clienteId,
            @Valid @RequestBody CrearPedidoRequestDTO pedidoRequest) {
        try {
            Cliente cliente = clienteRepository.findById(clienteId)
                    .orElseThrow(() -> new Exception("Cliente no encontrado con ID: " + clienteId + ". No se puede crear pedido desde carrito."));

            PedidoResponseDTO nuevoPedidoDto = pedidoService.crearPedidoDesdeCarrito(cliente, pedidoRequest);

            if (nuevoPedidoDto.getFormaPago() == FormaPago.MERCADO_PAGO) {
                if (mpAccessToken == null || mpAccessToken.isEmpty() || mpAccessToken.equals("YOUR_TEST_ACCESS_TOKEN")) {
                    System.err.println("ERROR: MERCADOPAGO_ACCESS_TOKEN no está configurado o es el valor por defecto. No se puede crear preferencia de pago.");
                    throw new Exception("El pago con Mercado Pago no está disponible en este momento. Intente más tarde u otra forma de pago.");
                }
                Pedido pedidoParaMP = pedidoRepository.findById(nuevoPedidoDto.getId())
                        .orElseThrow(() -> new Exception("Pedido recién creado no encontrado para generar preferencia MP. ID: " + nuevoPedidoDto.getId()));

                PreferenceClient client = new PreferenceClient();
                List<PreferenceItemRequest> items = new ArrayList<>();
                items.add(PreferenceItemRequest.builder()
                        .id(pedidoParaMP.getId().toString())
                        .title("Pedido El Buen Sabor #" + pedidoParaMP.getId())
                        .description("Compra de varios productos")
                        .quantity(1)
                        .unitPrice(new BigDecimal(String.valueOf(pedidoParaMP.getTotal()))) // Asegurarse que getTotal() devuelva un valor formateable a BigDecimal
                        .currencyId("ARS")
                        .build());

                PreferenceBackUrlsRequest backUrls = PreferenceBackUrlsRequest.builder()
                        .success(mpSuccessUrl)
                        .failure(mpFailureUrl)
                        .pending(mpPendingUrl)
                        .build();

                PreferenceRequest preferenceRequest = PreferenceRequest.builder()
                        .items(items)
                        .backUrls(backUrls)
                        .notificationUrl(mpNotificationUrl)
                        .externalReference(pedidoParaMP.getId().toString())
                        .autoReturn("approved")
                        .build();

                String preferenceId;
                String initPoint;

                try {
                    Preference preference = client.create(preferenceRequest);
                    preferenceId = preference.getId();
                    initPoint = preference.getInitPoint();
                } catch (MPApiException e) {
                    System.err.println("Error API MercadoPago al crear preferencia: " + e.getApiResponse().getContent());
                    throw new Exception("Error al comunicarse con Mercado Pago para crear la preferencia de pago: " + e.getMessage());
                } catch (MPException e) {
                    System.err.println("Error MercadoPago SDK al crear preferencia: " + e.getMessage());
                    throw new Exception("Error interno al preparar el pago con Mercado Pago: " + e.getMessage());
                }

                pedidoService.actualizarPreferenciaMercadoPago(pedidoParaMP.getId(), preferenceId);

                Map<String, Object> response = new HashMap<>();
                response.put("pedido", nuevoPedidoDto);
                response.put("mercadoPagoInitPoint", initPoint);
                response.put("mercadoPagoPreferenceId", preferenceId);

                return new ResponseEntity<>(response, HttpStatus.CREATED);
            }

            return new ResponseEntity<>(nuevoPedidoDto, HttpStatus.CREATED);

        } catch (IllegalArgumentException e) {
            return handleGenericException(e, HttpStatus.BAD_REQUEST);
        } catch (ConstraintViolationException e) {
            return handleConstraintViolation(e);
        } catch (Exception e) {
            HttpStatus status = HttpStatus.BAD_REQUEST;
            if (e.getMessage() != null) {
                if (e.getMessage().contains("No se encontró un carrito") ||
                        e.getMessage().contains("El carrito está vacío") ||
                        e.getMessage().contains("no encontrado con ID:") ||
                        e.getMessage().contains("no pertenece al cliente")) {
                    status = HttpStatus.NOT_FOUND;
                } else if (e.getMessage().contains("Stock insuficiente")) {
                    status = HttpStatus.CONFLICT;
                } else if (e.getMessage().contains("Mercado Pago no está disponible") || e.getMessage().contains("Error al comunicarse con Mercado Pago")) {
                    status = HttpStatus.SERVICE_UNAVAILABLE;
                }
            }
            return handleGenericException(e, status);
        }
    }

    @PutMapping("/{id}/estado")
    public ResponseEntity<?> updatePedidoEstado(@PathVariable Integer id, @Valid @RequestBody PedidoEstadoRequestDTO estadoDto) {
        try {
            PedidoResponseDTO pedidoActualizadoDto = pedidoService.updateEstado(id, estadoDto.getNuevoEstado());
            return ResponseEntity.ok(pedidoActualizadoDto);
        } catch (Exception e) {
            return handleGenericException(e, HttpStatus.BAD_REQUEST);
        }
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<?> softDeletePedido(@PathVariable Integer id) {
        try {
            pedidoService.softDelete(id);
            Map<String, String> response = new HashMap<>();
            response.put("mensaje", "Pedido con ID " + id + " procesado para borrado lógico/cancelación.");
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            return handleGenericException(e, HttpStatus.NOT_FOUND);
        }
    }

    @PostMapping("/mp/notificaciones")
    public ResponseEntity<String> recibirNotificacionMercadoPago(@RequestBody(required = false) Map<String, Object> notificationBody, @RequestParam(required = false) String id, @RequestParam(required = false) String topic ) {
        System.out.println("INFO: Notificación de MercadoPago recibida.");
        if(notificationBody != null) System.out.println("Body: " + notificationBody.toString());
        if(id != null) System.out.println("Query Param id: " + id);
        if(topic != null) System.out.println("Query Param topic: " + topic);


        String paymentIdFromNotification = null;
        String notificationTopic = null;


        if (notificationBody != null && "payment".equals(notificationBody.get("type")) && notificationBody.containsKey("data")) {
            Map<String, String> data = (Map<String, String>) notificationBody.get("data");
            if (data != null && data.containsKey("id")) {
                paymentIdFromNotification = data.get("id");
            }
            notificationTopic = "payment"; // Asumimos que es de tipo payment por la estructura
        } else if ("payment".equals(topic) && id != null) { // Para IPN más antiguo o si se envían por query params
            paymentIdFromNotification = id;
            notificationTopic = topic;
        }


        if ("payment".equals(notificationTopic) && paymentIdFromNotification != null) {
            System.out.println("INFO: Procesando notificación de pago de MP. Payment ID: " + paymentIdFromNotification);
            try {
                if (mpAccessToken == null || mpAccessToken.isEmpty() || mpAccessToken.equals("YOUR_TEST_ACCESS_TOKEN")) {
                    System.err.println("ERROR: MERCADOPAGO_ACCESS_TOKEN no está configurado o es el valor por defecto. No se puede procesar notificación de MP.");
                    return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Configuración de MercadoPago incompleta en el servidor.");
                }

                PaymentClient paymentClient = new PaymentClient();
                com.mercadopago.resources.payment.Payment paymentInfo = paymentClient.get(Long.valueOf(paymentIdFromNotification)); // SDK v2 usa get() y espera Long

                if (paymentInfo == null) {
                    System.err.println("ERROR: No se pudo obtener información del pago desde MP para Payment ID: " + paymentIdFromNotification);
                    return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("No se encontró información para el paymentId proporcionado.");
                }

                String externalReference = paymentInfo.getExternalReference();
                String paymentStatus = paymentInfo.getStatus().toString(); // Ej. "approved", "rejected"

                if (externalReference == null || externalReference.isEmpty()) {
                    System.err.println("ERROR: External Reference nulo o vacío para Payment ID: " + paymentIdFromNotification);
                    return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("External Reference no encontrado en la información del pago.");
                }

                System.out.println("INFO: Payment ID: " + paymentIdFromNotification + ", Status: " + paymentStatus + ", External Reference: " + externalReference);
                pedidoService.procesarNotificacionMercadoPago(paymentIdFromNotification, paymentStatus, externalReference);

                return ResponseEntity.ok("Notificacion procesada");

            } catch (MPApiException e) {
                System.err.println("Error API MercadoPago al obtener info del pago: " + e.getApiResponse().getContent());
                // Loguear e.getApiResponse().getContent() para más detalles del error de MP
                return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Error al consultar MercadoPago: " + e.getMessage());
            } catch (MPException e) {
                System.err.println("Error SDK MercadoPago al obtener info del pago: " + e.getMessage());
                return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Error interno con SDK MercadoPago: " + e.getMessage());
            } catch (Exception e) {
                System.err.println("Error procesando notificación de MP: " + e.getMessage());
                e.printStackTrace();
                return ResponseEntity.ok("Error interno al procesar la notificacion, se registrará para revisión.");
            }
        } else {
            System.out.println("WARN: Notificación de MP no reconocida o sin datos suficientes para procesar. Topic: '"+notificationTopic+"', ID: '"+paymentIdFromNotification+"'");
            return ResponseEntity.ok("Notificacion recibida pero no aplicable o incompleta.");
        }
    }


    private ResponseEntity<Map<String, Object>> handleConstraintViolation(ConstraintViolationException e) {
        Map<String, Object> errorResponse = new HashMap<>();
        errorResponse.put("status", HttpStatus.BAD_REQUEST.value());
        errorResponse.put("error", "Error de validación");
        errorResponse.put("mensajes", e.getConstraintViolations().stream()
                .map(cv -> cv.getPropertyPath() + ": " + cv.getMessage())
                .collect(Collectors.toList()));
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errorResponse);
    }

    private ResponseEntity<Map<String, Object>> handleGenericException(Exception e, HttpStatus status) {
        Map<String, Object> errorResponse = new HashMap<>();
        errorResponse.put("status", status.value());
        String message = e.getMessage();
        if (message == null || message.isBlank()){
            message = "Ocurrió un error inesperado.";
        }
        errorResponse.put("error", message);
        System.err.println("Exception handled: Status=" + status.value() + ", Message=" + e.getMessage());
        // No imprimas e.printStackTrace() directamente en producción si el log no está configurado para ello.
        // Considera un framework de logging.
        e.printStackTrace();
        return ResponseEntity.status(status).body(errorResponse);
    }
}
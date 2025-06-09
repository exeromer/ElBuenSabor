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
import com.mercadopago.client.payment.PaymentClient;
import com.mercadopago.client.preference.*;
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

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/pedidos")
@Validated
public class PedidoController {

    @Autowired private PedidoService pedidoService;
    @Autowired private ClienteRepository clienteRepository;
    @Autowired private PedidoRepository pedidoRepository;

    @Value("${mercadopago.access_token}") private String mpAccessToken;
    @Value("${mercadopago.frontend.success_url}") private String mpSuccessUrl;
    @Value("${mercadopago.frontend.failure_url}") private String mpFailureUrl;
    @Value("${mercadopago.frontend.pending_url}") private String mpPendingUrl;
    @Value("${mercadopago.backend.notification_url}") private String mpNotificationUrl;

    @PostConstruct
    public void initMercadoPago() {
        if (mpAccessToken != null && !mpAccessToken.isEmpty() && !mpAccessToken.equals("YOUR_TEST_ACCESS_TOKEN")) {
            MercadoPagoConfig.setAccessToken(mpAccessToken);
        } else {
            System.err.println("WARN: MercadoPago Access Token no configurado.");
        }
    }

    @PostMapping
    public ResponseEntity<PedidoResponseDTO> createPedidoForAuthenticatedClient(@Valid @RequestBody PedidoRequestDTO dto, Authentication authentication) throws Exception {
        if (authentication == null && dto.getClienteId() == null) {
            throw new Exception("Se requiere clienteId para pedidos no autenticados.");
        }
        PedidoResponseDTO nuevoPedidoDto;
        if (authentication != null && authentication.getPrincipal() instanceof Jwt) {
            Jwt jwt = (Jwt) authentication.getPrincipal();
            String auth0Id = jwt.getSubject();
            nuevoPedidoDto = pedidoService.createForAuthenticatedClient(auth0Id, dto);
        } else {
            nuevoPedidoDto = pedidoService.create(dto);
        }
        return ResponseEntity.status(HttpStatus.CREATED).body(nuevoPedidoDto);
    }

    @GetMapping
    public ResponseEntity<List<PedidoResponseDTO>> getAllPedidos() {
        List<PedidoResponseDTO> pedidos = pedidoService.getAll();
        return ResponseEntity.ok(pedidos);
    }

    @GetMapping("/mis-pedidos")
    public ResponseEntity<List<PedidoResponseDTO>> getMisPedidos(Authentication authentication) throws Exception {
        Jwt jwt = (Jwt) authentication.getPrincipal();
        String auth0Id = jwt.getSubject();
        List<PedidoResponseDTO> pedidos = pedidoService.getPedidosByClienteAuth0Id(auth0Id);
        return ResponseEntity.ok(pedidos);
    }

    @GetMapping("/{id}")
    public ResponseEntity<PedidoResponseDTO> getPedidoById(@PathVariable Integer id) throws Exception {
        return ResponseEntity.ok(pedidoService.getById(id));
    }

    @PostMapping("/cliente/{clienteId}/desde-carrito")
    public ResponseEntity<?> crearPedidoDesdeCarrito(@PathVariable Integer clienteId, @Valid @RequestBody CrearPedidoRequestDTO pedidoRequest) throws Exception {
        Cliente cliente = clienteRepository.findById(clienteId)
                .orElseThrow(() -> new Exception("Cliente no encontrado con ID: " + clienteId));

        PedidoResponseDTO nuevoPedidoDto = pedidoService.crearPedidoDesdeCarrito(cliente, pedidoRequest);

        if (nuevoPedidoDto.getFormaPago() == FormaPago.MERCADO_PAGO) {
            Pedido pedidoParaMP = pedidoRepository.findById(nuevoPedidoDto.getId())
                    .orElseThrow(() -> new Exception("Pedido recién creado no encontrado: " + nuevoPedidoDto.getId()));

            PreferenceClient client = new PreferenceClient();
            List<PreferenceItemRequest> items = new ArrayList<>();
            items.add(PreferenceItemRequest.builder()
                    .id(pedidoParaMP.getId().toString())
                    .title("Pedido El Buen Sabor #" + pedidoParaMP.getId())
                    .quantity(1)
                    .unitPrice(new BigDecimal(String.valueOf(pedidoParaMP.getTotal())))
                    .currencyId("ARS")
                    .build());
            PreferenceBackUrlsRequest backUrls = PreferenceBackUrlsRequest.builder()
                    .success(mpSuccessUrl).failure(mpFailureUrl).pending(mpPendingUrl).build();
            PreferenceRequest preferenceRequest = PreferenceRequest.builder()
                    .items(items).backUrls(backUrls).notificationUrl(mpNotificationUrl)
                    .externalReference(pedidoParaMP.getId().toString()).autoReturn("approved").build();

            Preference preference = client.create(preferenceRequest);
            pedidoService.actualizarPreferenciaMercadoPago(pedidoParaMP.getId(), preference.getId());

            Map<String, Object> response = new HashMap<>();
            response.put("pedido", nuevoPedidoDto);
            response.put("mercadoPagoInitPoint", preference.getInitPoint());
            response.put("mercadoPagoPreferenceId", preference.getId());
            return ResponseEntity.status(HttpStatus.CREATED).body(response);
        }
        return ResponseEntity.status(HttpStatus.CREATED).body(nuevoPedidoDto);
    }

    @PutMapping("/{id}/estado")
    public ResponseEntity<PedidoResponseDTO> updatePedidoEstado(@PathVariable Integer id, @Valid @RequestBody PedidoEstadoRequestDTO estadoDto) throws Exception {
        PedidoResponseDTO pedidoActualizadoDto = pedidoService.updateEstado(id, estadoDto.getNuevoEstado());
        return ResponseEntity.ok(pedidoActualizadoDto);
    }

    @PostMapping("/mp/notificaciones")
    public ResponseEntity<String> recibirNotificacionMercadoPago(@RequestBody(required = false) Map<String, Object> notificationBody, @RequestParam(required = false) String id, @RequestParam(required = false) String topic) throws Exception {
        String paymentId = null;
        if (notificationBody != null && "payment".equals(notificationBody.get("type"))) {
            Map<String, String> data = (Map<String, String>) notificationBody.get("data");
            paymentId = data.get("id");
        } else if ("payment".equals(topic)) {
            paymentId = id;
        }

        if (paymentId != null) {
            PaymentClient paymentClient = new PaymentClient();
            com.mercadopago.resources.payment.Payment paymentInfo = paymentClient.get(Long.valueOf(paymentId));
            String externalReference = paymentInfo.getExternalReference();
            String paymentStatus = paymentInfo.getStatus().toString();
            pedidoService.procesarNotificacionMercadoPago(paymentId, paymentStatus, externalReference);
            return ResponseEntity.ok("Notificacion procesada");
        }
        return ResponseEntity.ok("Notificacion no aplicable.");
    }
}
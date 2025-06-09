package com.powerRanger.ElBuenSabor.controllers;

import com.powerRanger.ElBuenSabor.dtos.ClienteRequestDTO;
import com.powerRanger.ElBuenSabor.dtos.ClienteResponseDTO;
import com.powerRanger.ElBuenSabor.services.ClienteService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/clientes")
@Validated
public class ClienteController {

    @Autowired
    private ClienteService clienteService;

    @PostMapping
    public ResponseEntity<ClienteResponseDTO> createCliente(@Valid @RequestBody ClienteRequestDTO dto) throws Exception {
        ClienteResponseDTO nuevoClienteDto = clienteService.createCliente(dto);
        return ResponseEntity.status(HttpStatus.CREATED).body(nuevoClienteDto);
    }

    @GetMapping
    public ResponseEntity<List<ClienteResponseDTO>> getAllClientes(@RequestParam(name = "searchTerm", required = false) String searchTerm) {
        List<ClienteResponseDTO> clientes = clienteService.findAllClientes(searchTerm);
        return ResponseEntity.ok(clientes);
    }

    @GetMapping("/perfil")
    public ResponseEntity<?> getMiPerfil(Authentication authentication) {
        try {
            if (authentication == null || !(authentication.getPrincipal() instanceof Jwt)) {
                // Este caso no debería ocurrir si el endpoint está protegido por .authenticated() o .hasAuthority()
                // y el token es un JWT válido, pero es una buena verificación.
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                        .body(Map.of("error", "No autenticado o token inválido."));
            }
            Jwt jwt = (Jwt) authentication.getPrincipal();
            String auth0Id = jwt.getSubject();

            ClienteResponseDTO clienteDto = clienteService.getMyProfile(auth0Id);
            return ResponseEntity.ok(clienteDto);
        } catch (Exception e) {
            // Usar tu handleGenericException o uno más específico si es necesario
            return handleGenericException(e, e.getMessage().contains("no encontrado") ? HttpStatus.NOT_FOUND : HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @GetMapping("/{id}")
    public ResponseEntity<ClienteResponseDTO> getClienteById(@PathVariable Integer id) throws Exception {
        ClienteResponseDTO clienteDto = clienteService.findClienteById(id);
        return ResponseEntity.ok(clienteDto);
    }

    @PutMapping("/{id}")
    public ResponseEntity<ClienteResponseDTO> updateCliente(@PathVariable Integer id, @Valid @RequestBody ClienteRequestDTO dto) throws Exception {
        ClienteResponseDTO clienteActualizadoDto = clienteService.updateCliente(id, dto);
        return ResponseEntity.ok(clienteActualizadoDto);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<?> softDeleteCliente(@PathVariable Integer id) throws Exception {
        clienteService.softDeleteCliente(id);
        Map<String, String> response = new HashMap<>();
        response.put("mensaje", "Cliente con ID " + id + " marcado como inactivo (borrado lógico).");
        return ResponseEntity.ok(response);
    }
}
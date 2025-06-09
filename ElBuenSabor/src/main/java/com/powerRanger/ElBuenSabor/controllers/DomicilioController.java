package com.powerRanger.ElBuenSabor.controllers;

import com.powerRanger.ElBuenSabor.dtos.DomicilioRequestDTO;
import com.powerRanger.ElBuenSabor.dtos.DomicilioResponseDTO;
import com.powerRanger.ElBuenSabor.services.DomicilioService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/domicilios")
@Validated
public class DomicilioController {

    @Autowired
    private DomicilioService domicilioService;

    @PostMapping
    public ResponseEntity<DomicilioResponseDTO> createDomicilio(@Valid @RequestBody DomicilioRequestDTO dto) throws Exception {
        // CORRECCIÓN: Llamar al método específico del servicio que maneja el DTO.
        DomicilioResponseDTO responseDto = domicilioService.createDomicilio(dto);
        return ResponseEntity.status(HttpStatus.CREATED).body(responseDto);
    }

    @GetMapping
    public ResponseEntity<List<DomicilioResponseDTO>> getAllDomicilios() throws Exception {
        // CORRECCIÓN: Llamar al método específico del servicio.
        List<DomicilioResponseDTO> domicilios = domicilioService.findAllDomicilios();
        if (domicilios.isEmpty()) {
            return ResponseEntity.noContent().build();
        }
        return ResponseEntity.ok(domicilios);
    }

    @GetMapping("/{id}")
    public ResponseEntity<DomicilioResponseDTO> getDomicilioById(@PathVariable Integer id) throws Exception {
        // CORRECCIÓN: Llamar al método específico del servicio.
        DomicilioResponseDTO domicilioDto = domicilioService.findDomicilioById(id);
        return ResponseEntity.ok(domicilioDto);
    }

    @PutMapping("/{id}")
    public ResponseEntity<DomicilioResponseDTO> updateDomicilio(@PathVariable Integer id, @Valid @RequestBody DomicilioRequestDTO dto) throws Exception {
        // CORRECCIÓN: Llamar al método específico del servicio.
        DomicilioResponseDTO responseDto = domicilioService.updateDomicilio(id, dto);
        return ResponseEntity.ok(responseDto);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteDomicilio(@PathVariable Integer id) throws Exception {
        // Esta llamada ya era correcta porque usa el método genérico 'delete' de BaseService.
        domicilioService.delete(id);
        return ResponseEntity.noContent().build();
    }
}
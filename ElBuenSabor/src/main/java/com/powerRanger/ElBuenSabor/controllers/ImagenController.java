package com.powerRanger.ElBuenSabor.controllers;

import com.powerRanger.ElBuenSabor.dtos.ImagenRequestDTO;
import com.powerRanger.ElBuenSabor.dtos.ImagenResponseDTO;
import com.powerRanger.ElBuenSabor.services.ImagenService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/imagenes")
@Validated
public class ImagenController {

    @Autowired
    private ImagenService imagenService;

    @PostMapping
    public ResponseEntity<ImagenResponseDTO> createImagen(@Valid @RequestBody ImagenRequestDTO dto) throws Exception {
        ImagenResponseDTO nuevaImagenDto = imagenService.createImagen(dto);
        return ResponseEntity.status(HttpStatus.CREATED).body(nuevaImagenDto);
    }

    @GetMapping
    public ResponseEntity<List<ImagenResponseDTO>> getAllImagenes() throws Exception {
        List<ImagenResponseDTO> imagenes = imagenService.findAllImagenes(); // Usamos el método refactorizado del servicio
        if (imagenes.isEmpty()) {
            return ResponseEntity.noContent().build();
        }
        return ResponseEntity.ok(imagenes);
    }

    @GetMapping("/{id}")
    public ResponseEntity<ImagenResponseDTO> getImagenById(@PathVariable Integer id) throws Exception {
        ImagenResponseDTO imagenDto = imagenService.findImagenById(id); // Usamos el método refactorizado del servicio
        return ResponseEntity.ok(imagenDto);
    }

    @PutMapping("/{id}")
    public ResponseEntity<ImagenResponseDTO> updateImagen(@PathVariable Integer id, @Valid @RequestBody ImagenRequestDTO dto) throws Exception {
        ImagenResponseDTO imagenActualizadaDto = imagenService.updateImagen(id, dto);
        return ResponseEntity.ok(imagenActualizadaDto);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteImagen(@PathVariable Integer id) throws Exception {
        // En tu servicio refactorizado, este método se llama `deleteImagenCompleta`
        // para distinguirlo del `delete` genérico. Asegúrate de que el nombre coincida.
        imagenService.deleteImagenCompleta(id);
        return ResponseEntity.noContent().build();
    }
}
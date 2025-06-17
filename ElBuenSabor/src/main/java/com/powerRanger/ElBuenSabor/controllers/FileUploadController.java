package com.powerRanger.ElBuenSabor.controllers;

import com.powerRanger.ElBuenSabor.dtos.ImagenRequestDTO;
import com.powerRanger.ElBuenSabor.dtos.ImagenResponseDTO; // Importar DTO de respuesta
// import com.powerRanger.ElBuenSabor.entities.Imagen; // Ya no necesitamos la entidad aquí directamente para la respuesta
import com.powerRanger.ElBuenSabor.services.FileStorageService;
import com.powerRanger.ElBuenSabor.services.ImagenService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.io.IOException;
import java.nio.file.Files;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/files")
public class FileUploadController {

    @Autowired
    private FileStorageService fileStorageService;

    @Autowired
    private ImagenService imagenService;

    @PostMapping("/upload")
    public ResponseEntity<?> uploadFile(@RequestParam("file") MultipartFile file,
                                        @RequestParam(name = "articuloId", required = false) Integer articuloId,
                                        @RequestParam(name = "promocionId", required = false) Integer promocionId) {
        Map<String, Object> response = new HashMap<>();
        try {
            if (file.isEmpty()) {
                response.put("error", "El archivo está vacío.");
                return ResponseEntity.badRequest().body(response);
            }

            String filename = fileStorageService.store(file);
            String fileDownloadUri = ServletUriComponentsBuilder.fromCurrentContextPath()
                    .path("/api/files/view/")
                    .path(filename)
                    .toUriString();

            ImagenRequestDTO imagenDto = new ImagenRequestDTO();
            imagenDto.setDenominacion(fileDownloadUri);
            imagenDto.setEstadoActivo(true);
            imagenDto.setArticuloId(articuloId);
            imagenDto.setPromocionId(promocionId);

            // Llamar al servicio con el DTO, ahora devuelve ImagenResponseDTO
            ImagenResponseDTO savedImageDto = imagenService.createImagen(imagenDto); // ✅ CAMBIO AQUÍ

            response.put("message", "Archivo subido exitosamente: " + file.getOriginalFilename());
            response.put("filename", filename);
            response.put("url", fileDownloadUri);
            response.put("imagenDB", savedImageDto); // ✅ CAMBIO AQUÍ: Poner el DTO en la respuesta
            return ResponseEntity.ok(response);

        } catch (Exception e) {
            response.put("error", "No se pudo subir el archivo: " + (file != null ? file.getOriginalFilename() : "nombre desconocido") + ". Error: " + e.getMessage());
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }

    @PostMapping("/uploadMultiple")
    public ResponseEntity<List<Map<String, Object>>> uploadMultipleFiles(@RequestParam("files") MultipartFile[] files,
                                                                         @RequestParam(name = "articuloId", required = false) Integer articuloId,
                                                                         @RequestParam(name = "promocionId", required = false) Integer promocionId) {
        List<Map<String, Object>> responses = Arrays.stream(files)
                .map(file -> {
                    ResponseEntity<?> singleResponse = uploadFile(file, articuloId, promocionId);
                    Map<String, Object> responseMap = new HashMap<>();
                    if (singleResponse.getStatusCode().is2xxSuccessful() && singleResponse.getBody() instanceof Map) {
                        responseMap.putAll((Map<String,Object>) singleResponse.getBody());
                        responseMap.put("originalFilename", file.getOriginalFilename());
                        responseMap.put("status", "SUCCESS");
                    } else {
                        responseMap.put("originalFilename", file.getOriginalFilename());
                        responseMap.put("status", "FAILED");
                        if (singleResponse.getBody() instanceof Map) {
                            responseMap.put("errorDetails", ((Map<String,Object>) singleResponse.getBody()).get("error"));
                        } else if (singleResponse.getBody() != null) {
                            responseMap.put("errorDetails", singleResponse.getBody().toString());
                        } else {
                            responseMap.put("errorDetails", "Error desconocido durante la subida del archivo " + file.getOriginalFilename());
                        }
                    }
                    return responseMap;
                })
                .collect(Collectors.toList());
        return ResponseEntity.ok(responses);
    }

    @GetMapping("/view/{filename:.+}")
    public ResponseEntity<Resource> serveFile(@PathVariable String filename) {
        try {
            Resource file = fileStorageService.loadAsResource(filename);
            String contentType = "application/octet-stream";
            try {
                contentType = Files.probeContentType(file.getFile().toPath());
                if (contentType == null) {
                    contentType = "application/octet-stream";
                }
            } catch (IOException e) {
                System.err.println("No se pudo determinar el tipo de contenido para el archivo: " + filename + ". Usando default. Error: " + e.getMessage());
            }

            return ResponseEntity.ok()
                    .contentType(MediaType.parseMediaType(contentType))
                    .header(HttpHeaders.CONTENT_DISPOSITION, "inline; filename=\"" + file.getFilename() + "\"")
                    .body(file);
        } catch (Exception e) {
            System.err.println("Error al servir el archivo " + filename + ": " + e.getMessage());
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(null);
        }
    }

    @DeleteMapping("/delete/{filename:.+}")
    public ResponseEntity<?> deleteFile(@PathVariable String filename) {
        Map<String, String> response = new HashMap<>();
        try {
            // Este endpoint solo borra el archivo del disco.
            // La lógica para borrar la entidad Imagen y luego el archivo físico
            // está en ImagenService y se accede a través de DELETE /api/imagenes/{id}
            fileStorageService.delete(filename);
            response.put("message", "Archivo '" + filename + "' eliminado correctamente del disco (solo del disco).");
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            response.put("error", "No se pudo eliminar el archivo del disco: " + filename + ". Error: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }
}
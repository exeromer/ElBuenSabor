package com.powerRanger.ElBuenSabor.controllers;

import com.powerRanger.ElBuenSabor.dtos.ImagenRequestDTO;
import com.powerRanger.ElBuenSabor.dtos.ImagenResponseDTO;
import com.powerRanger.ElBuenSabor.services.FileStorageService;
import com.powerRanger.ElBuenSabor.services.ImagenService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
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
    public ResponseEntity<Map<String, Object>> uploadFile(@RequestParam("file") MultipartFile file,
                                                          @RequestParam(name = "articuloId", required = false) Integer articuloId,
                                                          @RequestParam(name = "promocionId", required = false) Integer promocionId) throws Exception {

        if (file.isEmpty()) {
            // Es mejor lanzar una excepción para que sea manejada globalmente.
            throw new Exception("No se puede subir un archivo vacío.");
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

        ImagenResponseDTO savedImageDto = imagenService.createImagen(imagenDto);

        Map<String, Object> response = new HashMap<>();
        response.put("message", "Archivo subido exitosamente: " + file.getOriginalFilename());
        response.put("filename", filename);
        response.put("url", fileDownloadUri);
        response.put("imagenDB", savedImageDto);

        return ResponseEntity.ok(response);
    }

    @PostMapping("/uploadMultiple")
    public ResponseEntity<List<Map<String, Object>>> uploadMultipleFiles(@RequestParam("files") MultipartFile[] files,
                                                                         @RequestParam(name = "articuloId", required = false) Integer articuloId,
                                                                         @RequestParam(name = "promocionId", required = false) Integer promocionId) {

        List<Map<String, Object>> responses = Arrays.stream(files)
                .map(file -> {
                    Map<String, Object> responseMap = new HashMap<>();
                    responseMap.put("originalFilename", file.getOriginalFilename());
                    try {
                        // Reutilizamos el método de subida individual
                        ResponseEntity<Map<String, Object>> singleResponse = uploadFile(file, articuloId, promocionId);
                        responseMap.putAll(singleResponse.getBody());
                        responseMap.put("status", "SUCCESS");
                    } catch (Exception e) {
                        responseMap.put("status", "FAILED");
                        responseMap.put("errorDetails", e.getMessage());
                    }
                    return responseMap;
                })
                .collect(Collectors.toList());

        return ResponseEntity.ok(responses);
    }

    @GetMapping("/view/{filename:.+}")
    public ResponseEntity<Resource> serveFile(@PathVariable String filename) throws Exception {
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
    }

    @DeleteMapping("/delete/{filename:.+}")
    public ResponseEntity<Map<String, String>> deleteFile(@PathVariable String filename) throws Exception {
        // Este endpoint solo borra el archivo del disco.
        // La lógica para borrar la entidad Imagen se hace desde ImagenController.
        fileStorageService.delete(filename);
        Map<String, String> response = new HashMap<>();
        response.put("message", "Archivo '" + filename + "' eliminado correctamente del disco.");
        return ResponseEntity.ok(response);
    }
}
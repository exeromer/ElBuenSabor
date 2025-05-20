package com.powerRanger.ElBuenSabor.controllers;

import com.powerRanger.ElBuenSabor.dtos.ImagenRequestDTO; // Importar el DTO
import com.powerRanger.ElBuenSabor.entities.Imagen;
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
import java.nio.file.Files; // Necesario para Files.probeContentType
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
                                        // Opcional: IDs para asociar la imagen directamente al subirla
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

            // Crear y poblar el DTO para la entidad Imagen
            ImagenRequestDTO imagenDto = new ImagenRequestDTO();
            imagenDto.setDenominacion(fileDownloadUri); // Guardamos la URL de acceso
            imagenDto.setEstadoActivo(true);
            imagenDto.setArticuloId(articuloId);     // Asociar si se proporciona el ID
            imagenDto.setPromocionId(promocionId); // Asociar si se proporciona el ID

            // Llamar al servicio con el DTO
            Imagen savedImage = imagenService.createImagen(imagenDto);

            response.put("message", "Archivo subido exitosamente: " + file.getOriginalFilename());
            response.put("filename", filename);
            response.put("url", fileDownloadUri);
            response.put("imagenDB", savedImage); // El objeto Imagen (entidad) guardado en la BD
            return ResponseEntity.ok(response);

        } catch (Exception e) {
            response.put("error", "No se pudo subir el archivo: " + (file != null ? file.getOriginalFilename() : "nombre desconocido") + ". Error: " + e.getMessage());
            // Loguear el error completo para depuración
            e.printStackTrace(); // Importante para ver la causa raíz en la consola del servidor
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }

    @PostMapping("/uploadMultiple")
    public ResponseEntity<List<Map<String, Object>>> uploadMultipleFiles(@RequestParam("files") MultipartFile[] files,
                                                                         @RequestParam(name = "articuloId", required = false) Integer articuloId,
                                                                         @RequestParam(name = "promocionId", required = false) Integer promocionId) {
        List<Map<String, Object>> responses = Arrays.stream(files)
                .map(file -> {
                    // Reutilizamos la lógica de uploadFile, pero capturamos la respuesta en un Map
                    // NOTA: uploadFile ahora espera articuloId y promocionId, debes decidir cómo pasarlos aquí
                    // si quieres que cada imagen en la subida múltiple se asocie igual, o si necesitas
                    // una lógica más compleja para asociar cada imagen individualmente.
                    // Por ahora, pasaré los mismos articuloId y promocionId a cada llamada de uploadFile.
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
                        }
                        else {
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
            String contentType = "application/octet-stream"; // Tipo por defecto
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
                    .header(HttpHeaders.CONTENT_DISPOSITION, "inline; filename=\"" + file.getFilename() + "\"") // inline para mostrar en navegador
                    .body(file);
        } catch (Exception e) {
            System.err.println("Error al servir el archivo " + filename + ": " + e.getMessage());
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(null);
        }
    }

    @DeleteMapping("/delete/{filename:.+}")
    public ResponseEntity<?> deleteFile(@PathVariable String filename) {
        // Este endpoint actualmente solo borra del disco.
        // El borrado de la entidad Imagen (y consecuentemente el archivo físico a través del ImagenService)
        // se hace a través de DELETE /api/imagenes/{idDeLaImagenDB}
        Map<String, String> response = new HashMap<>();
        try {
            fileStorageService.delete(filename);
            response.put("message", "Archivo '" + filename + "' eliminado correctamente del disco.");
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            response.put("error", "No se pudo eliminar el archivo: " + filename + ". Error: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }
}
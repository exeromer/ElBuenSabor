package com.powerRanger.ElBuenSabor.services;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import jakarta.annotation.PostConstruct;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.UUID;
import java.util.stream.Stream;

@Service
public class FileStorageServiceImpl implements FileStorageService {

    @Value("${file.upload-dir}")
    private String uploadDir;

    private Path rootLocation;

    @Override
    @PostConstruct
    public void init() {
        try {
            // Normalizar la ruta raíz aquí al inicializar
            this.rootLocation = Paths.get(uploadDir).toAbsolutePath().normalize(); // ✅ CAMBIO AQUÍ

            if (Files.notExists(this.rootLocation)) {
                Files.createDirectories(this.rootLocation);
                System.out.println("Directorio de subida creado: " + this.rootLocation.toString());
            } else {
                System.out.println("Directorio de subida ya existe: " + this.rootLocation.toString());
            }
        } catch (IOException e) {
            throw new RuntimeException("No se pudo inicializar el directorio de subida de archivos.", e);
        }
    }

    @Override
    public String store(MultipartFile file) throws Exception {
        if (file.isEmpty()) {
            throw new Exception("No se puede guardar un archivo vacío.");
        }
        String originalFilename = StringUtils.cleanPath(file.getOriginalFilename());
        if (originalFilename.contains("..")) {
            throw new Exception("Nombre de archivo inválido (contiene secuencias de ruta): " + originalFilename);
        }

        String fileExtension = "";
        try {
            fileExtension = originalFilename.substring(originalFilename.lastIndexOf("."));
        } catch (Exception e) {
            fileExtension = "";
        }
        String generatedFilename = UUID.randomUUID().toString() + fileExtension;

        try (InputStream inputStream = file.getInputStream()) {
            // Resolve, then normalize, then toAbsolutePath
            Path destinationFile = this.rootLocation.resolve(generatedFilename).normalize().toAbsolutePath();

            // La rootLocation ya está normalizada y absoluta desde init()
            Path normalizedRootLocation = this.rootLocation; // Ya es absoluta y normalizada

            // ---- LÍNEAS DE DEPURACIÓN (PUEDES MANTENERLAS O QUITARLAS DESPUÉS DE VERIFICAR) ----
            System.out.println("DEBUG: Root Location (from init, normalized): " + normalizedRootLocation);
            System.out.println("DEBUG: Destination File Parent Absolute Path: " + destinationFile.getParent());
            System.out.println("DEBUG: Are paths equal? " + destinationFile.getParent().equals(normalizedRootLocation));
            // ---- FIN DE LÍNEAS DE DEPURACIÓN ----

            if (!destinationFile.getParent().equals(normalizedRootLocation)) {
                // Modifica el mensaje de error para incluir ambas rutas normalizadas si sigue fallando
                throw new Exception("No se puede guardar el archivo fuera del directorio de subida actual. Root (normalizado): " + normalizedRootLocation + " | Dest Parent: " + destinationFile.getParent());
            }
            Files.copy(inputStream, destinationFile, StandardCopyOption.REPLACE_EXISTING);
            return generatedFilename;
        } catch (IOException e) {
            throw new Exception("Falló al guardar el archivo: " + originalFilename, e);
        }
    }

    // ... resto de los métodos (loadAll, load, loadAsResource, delete) sin cambios ...
    @Override
    public Stream<Path> loadAll() throws Exception {
        try {
            return Files.walk(this.rootLocation, 1)
                    .filter(path -> !path.equals(this.rootLocation))
                    .map(this.rootLocation::relativize);
        } catch (IOException e) {
            throw new Exception("Falló al leer los archivos guardados.", e);
        }
    }

    @Override
    public Path load(String filename) {
        return rootLocation.resolve(filename);
    }

    @Override
    public Resource loadAsResource(String filename) throws Exception {
        try {
            Path file = load(filename);
            Resource resource = new UrlResource(file.toUri());
            if (resource.exists() || resource.isReadable()) {
                return resource;
            } else {
                throw new Exception("No se pudo leer el archivo: " + filename);
            }
        } catch (MalformedURLException e) {
            throw new Exception("No se pudo leer el archivo (URL mal formada): " + filename, e);
        }
    }

    @Override
    public void delete(String filename) throws Exception {
        try {
            // Construimos la ruta completa al archivo que se quiere borrar
            Path fileToDelete = this.rootLocation.resolve(filename).normalize();

            // --- VALIDACIÓN DE SEGURIDAD MEJORADA ---
            // 1. Verificamos que el archivo realmente existe.
            if (Files.notExists(fileToDelete)) {
                System.out.println("WARN: Se intentó borrar un archivo que no existe: " + filename);
                return; // Salimos silenciosamente, el resultado es el mismo (el archivo no está).
            }

            // 2. La validación clave: nos aseguramos de que la ruta normalizada del archivo
            //    comience con la ruta normalizada del directorio raíz de uploads.
            //    Esto previene ataques de "Directory Traversal" (../../) de forma segura.
            if (!fileToDelete.toAbsolutePath().startsWith(this.rootLocation.toAbsolutePath())) {
                System.err.println("Intento de borrado de archivo fuera del directorio de uploads: " + filename);
                throw new Exception("Acceso denegado: no se puede borrar un archivo fuera del directorio raíz.");
            }

            // Si todas las validaciones pasan, borramos el archivo.
            Files.delete(fileToDelete);
            System.out.println("Archivo físico '" + filename + "' eliminado del disco exitosamente.");

        } catch (IOException e) {
            // Capturamos cualquier error de entrada/salida y lo relanzamos como una excepción genérica.
            throw new Exception("No se pudo borrar el archivo: " + filename, e);
        }
    }
}
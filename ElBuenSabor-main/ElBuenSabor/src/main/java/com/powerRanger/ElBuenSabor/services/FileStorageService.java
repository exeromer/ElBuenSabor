package com.powerRanger.ElBuenSabor.services;

import org.springframework.core.io.Resource;
import org.springframework.web.multipart.MultipartFile;
import java.nio.file.Path;
import java.util.stream.Stream;

public interface FileStorageService {
    void init(); // Para crear el directorio de subida si no existe
    String store(MultipartFile file) throws Exception; // Guarda el archivo y devuelve el nombre del archivo guardado
    Stream<Path> loadAll() throws Exception; // Carga todos los archivos (opcional)
    Path load(String filename); // Carga un archivo específico
    Resource loadAsResource(String filename) throws Exception; // Carga un archivo como Resource para servirlo
    void delete(String filename) throws Exception; // Borra un archivo
}
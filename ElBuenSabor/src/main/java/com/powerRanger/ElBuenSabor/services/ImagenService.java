package com.powerRanger.ElBuenSabor.services;

import com.powerRanger.ElBuenSabor.dtos.ImagenRequestDTO;
import com.powerRanger.ElBuenSabor.dtos.ImagenResponseDTO; // Importar DTO de respuesta
// import com.powerRanger.ElBuenSabor.entities.Imagen; // Ya no se devuelve la entidad directamente
import jakarta.validation.Valid;
import java.util.List;

public interface ImagenService {
    List<ImagenResponseDTO> getAllImagenes(); // Devuelve lista de DTOs
    ImagenResponseDTO getImagenById(Integer id) throws Exception; // Devuelve un DTO

    // createImagen y updateImagen aceptan DTO de request y devuelven DTO de respuesta
    ImagenResponseDTO createImagen(@Valid ImagenRequestDTO dto) throws Exception;
    ImagenResponseDTO updateImagen(Integer id, @Valid ImagenRequestDTO dto) throws Exception;

    void deleteImagen(Integer id) throws Exception;
}
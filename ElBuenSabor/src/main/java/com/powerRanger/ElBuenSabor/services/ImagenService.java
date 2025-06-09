package com.powerRanger.ElBuenSabor.services;

import com.powerRanger.ElBuenSabor.dtos.ImagenRequestDTO;
import com.powerRanger.ElBuenSabor.dtos.ImagenResponseDTO;
import com.powerRanger.ElBuenSabor.entities.Imagen;
import jakarta.validation.Valid;
import java.util.List;

public interface ImagenService extends BaseService<Imagen, Integer> {
    List<ImagenResponseDTO> findAllImagenes();
    ImagenResponseDTO findImagenById(Integer id) throws Exception;
    ImagenResponseDTO createImagen(@Valid ImagenRequestDTO dto) throws Exception;
    ImagenResponseDTO updateImagen(Integer id, @Valid ImagenRequestDTO dto) throws Exception;
    void deleteImagenCompleta(Integer id) throws Exception;
}
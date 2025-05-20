package com.powerRanger.ElBuenSabor.services;

import com.powerRanger.ElBuenSabor.dtos.ImagenRequestDTO;
import com.powerRanger.ElBuenSabor.entities.Imagen;
import jakarta.validation.Valid;
import java.util.List;

public interface ImagenService {
    List<Imagen> getAllImagenes();
    Imagen getImagenById(Integer id) throws Exception;
    Imagen createImagen(@Valid ImagenRequestDTO dto) throws Exception;
    Imagen updateImagen(Integer id, @Valid ImagenRequestDTO dto) throws Exception;
    void deleteImagen(Integer id) throws Exception;
}
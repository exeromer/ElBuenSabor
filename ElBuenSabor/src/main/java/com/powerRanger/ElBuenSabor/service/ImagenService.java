package com.powerRanger.ElBuenSabor.service;

import com.powerRanger.ElBuenSabor.entities.Imagen;
import com.powerRanger.ElBuenSabor.repository.ImagenRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class ImagenService {

    @Autowired
    private ImagenRepository imagenRepository;

    // Obtener todas las imágenes
    public List<Imagen> getAllImagenes() {
        return imagenRepository.findAll();
    }

    // Obtener una imagen por ID
    public Imagen getImagenById(Integer id) {
        Optional<Imagen> imagen = imagenRepository.findById(id);
        return imagen.orElse(null);  // Retorna null si no se encuentra
    }

    // Crear una nueva imagen
    public Imagen createImagen(Imagen imagen) {
        return imagenRepository.save(imagen);
    }

    // Actualizar una imagen
    public Imagen updateImagen(Integer id, Imagen imagen) {
        if (imagenRepository.existsById(id)) {
            return imagenRepository.save(imagen);
        }
        return null;
    }

    // Eliminar una imagen
    public void deleteImagen(Integer id) {
        imagenRepository.deleteById(id);
    }
}

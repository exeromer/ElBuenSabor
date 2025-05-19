package com.powerRanger.ElBuenSabor.service;

import com.powerRanger.ElBuenSabor.entities.Articulo;
import com.powerRanger.ElBuenSabor.repository.ArticuloRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class ArticuloService {

    @Autowired
    private ArticuloRepository articuloRepository;

    // Obtener todos los artículos
    public List<Articulo> getAllArticulos() {
        return articuloRepository.findAll();
    }

    // Obtener un artículo por ID
    public Articulo getArticuloById(Integer id) {
        Optional<Articulo> articulo = articuloRepository.findById(id);
        return articulo.orElse(null);  // Retorna null si no se encuentra
    }

    // Crear un nuevo artículo
    public Articulo createArticulo(Articulo articulo) {
        return articuloRepository.save(articulo);
    }

    // Actualizar un artículo
    public Articulo updateArticulo(Integer id, Articulo articulo) {
        if (articuloRepository.existsById(id)) {
            // No se necesita setear el id manualmente
            return articuloRepository.save(articulo);
        }
        return null;
    }

    // Eliminar un artículo
    public void deleteArticulo(Integer id) {
        articuloRepository.deleteById(id);
    }
}

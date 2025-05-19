package com.powerRanger.ElBuenSabor.service;

import com.powerRanger.ElBuenSabor.entities.ArticuloManufacturado;
import com.powerRanger.ElBuenSabor.repository.ArticuloManufacturadoRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class ArticuloManufacturadoService {

    @Autowired
    private ArticuloManufacturadoRepository articuloManufacturadoRepository;

    // Obtener todos los artículos manufacturados
    public List<ArticuloManufacturado> getAllArticuloManufacturados() {
        return articuloManufacturadoRepository.findAll();
    }

    // Obtener un artículo manufacturado por ID
    public ArticuloManufacturado getArticuloManufacturadoById(Integer id) {
        Optional<ArticuloManufacturado> articulo = articuloManufacturadoRepository.findById(id);
        return articulo.orElse(null);  // Retorna null si no se encuentra
    }

    // Crear un nuevo artículo manufacturado
    public ArticuloManufacturado createArticuloManufacturado(ArticuloManufacturado articulo) {
        return articuloManufacturadoRepository.save(articulo);
    }

    // Actualizar un artículo manufacturado
    public ArticuloManufacturado updateArticuloManufacturado(Integer id, ArticuloManufacturado articulo) {
        if (articuloManufacturadoRepository.existsById(id)) {
            return articuloManufacturadoRepository.save(articulo);
        }
        return null;
    }

    // Eliminar un artículo manufacturado
    public void deleteArticuloManufacturado(Integer id) {
        articuloManufacturadoRepository.deleteById(id);
    }
}

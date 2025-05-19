package com.powerRanger.ElBuenSabor.service;

import com.powerRanger.ElBuenSabor.entities.ArticuloManufacturadoDetalle;
import com.powerRanger.ElBuenSabor.repository.ArticuloManufacturadoDetalleRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class ArticuloManufacturadoDetalleService {

    @Autowired
    private ArticuloManufacturadoDetalleRepository articuloManufacturadoDetalleRepository;

    // Obtener todos los detalles de artículos manufacturados
    public List<ArticuloManufacturadoDetalle> getAllArticuloManufacturadoDetalles() {
        return articuloManufacturadoDetalleRepository.findAll();
    }

    // Obtener un detalle de artículo manufacturado por ID
    public ArticuloManufacturadoDetalle getArticuloManufacturadoDetalleById(Integer id) {
        Optional<ArticuloManufacturadoDetalle> detalle = articuloManufacturadoDetalleRepository.findById(id);
        return detalle.orElse(null);  // Retorna null si no se encuentra
    }

    // Crear un nuevo detalle de artículo manufacturado
    public ArticuloManufacturadoDetalle createArticuloManufacturadoDetalle(ArticuloManufacturadoDetalle detalle) {
        return articuloManufacturadoDetalleRepository.save(detalle);
    }

    // Actualizar un detalle de artículo manufacturado
    public ArticuloManufacturadoDetalle updateArticuloManufacturadoDetalle(Integer id, ArticuloManufacturadoDetalle detalle) {
        if (articuloManufacturadoDetalleRepository.existsById(id)) {
            return articuloManufacturadoDetalleRepository.save(detalle);
        }
        return null;
    }

    // Eliminar un detalle de artículo manufacturado
    public void deleteArticuloManufacturadoDetalle(Integer id) {
        articuloManufacturadoDetalleRepository.deleteById(id);
    }
}

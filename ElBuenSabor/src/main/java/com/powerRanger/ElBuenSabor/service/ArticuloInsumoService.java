package com.powerRanger.ElBuenSabor.service;

import com.powerRanger.ElBuenSabor.entities.ArticuloInsumo;
import com.powerRanger.ElBuenSabor.repository.ArticuloInsumoRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class ArticuloInsumoService {

    @Autowired
    private ArticuloInsumoRepository articuloInsumoRepository;

    // Obtener todos los artículos insumo
    public List<ArticuloInsumo> getAllArticuloInsumo() {
        return articuloInsumoRepository.findAll();
    }

    // Obtener un artículo insumo por ID
    public ArticuloInsumo getArticuloInsumoById(Integer id) {
        Optional<ArticuloInsumo> articuloInsumo = articuloInsumoRepository.findById(id);
        return articuloInsumo.orElse(null);  // Retorna null si no se encuentra
    }

    // Crear un nuevo artículo insumo
    public ArticuloInsumo createArticuloInsumo(ArticuloInsumo articuloInsumo) {
        return articuloInsumoRepository.save(articuloInsumo);
    }

    // Actualizar un artículo insumo
    public ArticuloInsumo updateArticuloInsumo(Integer id, ArticuloInsumo articuloInsumo) {
        if (articuloInsumoRepository.existsById(id)) {
            return articuloInsumoRepository.save(articuloInsumo);
        }
        return null;
    }

    // Eliminar un artículo insumo
    public void deleteArticuloInsumo(Integer id) {
        articuloInsumoRepository.deleteById(id);
    }
}

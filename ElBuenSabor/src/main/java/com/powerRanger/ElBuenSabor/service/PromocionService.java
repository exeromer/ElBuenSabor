package com.powerRanger.ElBuenSabor.service;

import com.powerRanger.ElBuenSabor.entities.Promocion;
import com.powerRanger.ElBuenSabor.repository.PromocionRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class PromocionService {

    @Autowired
    private PromocionRepository promocionRepository;

    // Obtener todas las promociones
    public List<Promocion> getAllPromociones() {
        return promocionRepository.findAll();
    }

    // Obtener una promoción por ID
    public Promocion getPromocionById(Integer id) {
        Optional<Promocion> promocion = promocionRepository.findById(id);
        return promocion.orElse(null);  // Retorna null si no se encuentra
    }

    // Crear una nueva promoción
    public Promocion createPromocion(Promocion promocion) {
        return promocionRepository.save(promocion);
    }

    // Actualizar una promoción
    public Promocion updatePromocion(Integer id, Promocion promocion) {
        if (promocionRepository.existsById(id)) {
            return promocionRepository.save(promocion);
        }
        return null;
    }

    // Eliminar una promoción
    public void deletePromocion(Integer id) {
        promocionRepository.deleteById(id);
    }
}

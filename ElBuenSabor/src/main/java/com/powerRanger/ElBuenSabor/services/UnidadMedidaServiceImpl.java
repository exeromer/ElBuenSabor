package com.powerRanger.ElBuenSabor.services;

import com.powerRanger.ElBuenSabor.entities.UnidadMedida;
import com.powerRanger.ElBuenSabor.repository.UnidadMedidaRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import jakarta.transaction.Transactional;
import jakarta.validation.Valid; // Para que se activen validaciones en la entidad
import org.springframework.validation.annotation.Validated; // Para la clase de servicio

import java.util.List;

@Service
@Validated // Habilita la validación para los métodos de este servicio
public class UnidadMedidaServiceImpl implements UnidadMedidaService {

    @Autowired
    private UnidadMedidaRepository unidadMedidaRepository;

    @Override
    @Transactional
    public List<UnidadMedida> getAll() {
        return unidadMedidaRepository.findAll();
    }

    @Override
    @Transactional
    public UnidadMedida getById(Integer id) throws Exception {
        return unidadMedidaRepository.findById(id)
                .orElseThrow(() -> new Exception("Unidad de Medida no encontrada con ID: " + id));
    }

    @Override
    @Transactional
    public UnidadMedida create(@Valid UnidadMedida unidadMedida) throws Exception {
        // @Valid activará las validaciones de la entidad (ej: @NotEmpty en denominacion)
        // Aquí podrías verificar si ya existe una unidad con la misma denominación si es necesario
        // if(unidadMedidaRepository.findByDenominacion(unidadMedida.getDenominacion()).isPresent()){
        //     throw new Exception("Ya existe una unidad de medida con esa denominación.");
        // }
        return unidadMedidaRepository.save(unidadMedida);
    }

    @Override
    @Transactional
    public UnidadMedida update(Integer id, @Valid UnidadMedida unidadMedidaDetails) throws Exception {
        UnidadMedida unidadExistente = unidadMedidaRepository.findById(id)
                .orElseThrow(() -> new Exception("Unidad de Medida no encontrada con ID: " + id + " para actualizar."));

        unidadExistente.setDenominacion(unidadMedidaDetails.getDenominacion());
        // Si hay otros campos para actualizar, se harían aquí.

        return unidadMedidaRepository.save(unidadExistente);
    }

    @Override
    @Transactional
    public void delete(Integer id) throws Exception {
        UnidadMedida unidadExistente = unidadMedidaRepository.findById(id)
                .orElseThrow(() -> new Exception("Unidad de Medida no encontrada con ID: " + id + " para eliminar."));

        // Lógica de negocio importante: ¿Qué pasa si esta unidad de medida está en uso por algún artículo?
        // Deberías prevenir el borrado o manejarlo. Por ejemplo:
        if (unidadExistente.getArticulos() != null && !unidadExistente.getArticulos().isEmpty()) {
            throw new Exception("No se puede eliminar la Unidad de Medida ID " + id + " porque está siendo utilizada por uno o más artículos.");
        }

        unidadMedidaRepository.deleteById(id);
    }
}
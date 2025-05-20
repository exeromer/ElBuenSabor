package com.powerRanger.ElBuenSabor.services;

import com.powerRanger.ElBuenSabor.entities.Localidad;
import com.powerRanger.ElBuenSabor.entities.Provincia; // Necesaria para la relación
import com.powerRanger.ElBuenSabor.repository.LocalidadRepository;
import com.powerRanger.ElBuenSabor.repository.ProvinciaRepository; // Necesitarás el ProvinciaRepository
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import jakarta.transaction.Transactional;

import java.util.List;
import java.util.Optional;

@Service
public class LocalidadServiceImpl implements LocalidadService {

    @Autowired
    private LocalidadRepository localidadRepository;

    @Autowired
    private ProvinciaRepository provinciaRepository; // Para validar/obtener la Provincia

    @Override
    @Transactional
    public List<Localidad> obtenerTodas() {
        return localidadRepository.findAll();
    }

    @Override
    @Transactional
    public Localidad obtenerPorId(Integer id) throws Exception {
        return localidadRepository.findById(id)
                .orElseThrow(() -> new Exception("No se encontró la localidad con ID: " + id));
    }

    @Override
    @Transactional
    public Localidad guardar(Localidad localidad) throws Exception {
        // Validar que la provincia asociada exista
        if (localidad.getProvincia() == null || localidad.getProvincia().getId() == null) {
            throw new Exception("La localidad debe estar asociada a una provincia válida.");
        }
        Provincia provinciaExistente = provinciaRepository.findById(localidad.getProvincia().getId())
                .orElseThrow(() -> new Exception("No se encontró la provincia con ID: " + localidad.getProvincia().getId()));

        localidad.setProvincia(provinciaExistente); // Asegurar que el objeto Provincia completo esté seteado
        return localidadRepository.save(localidad);
    }

    @Override
    @Transactional
    public Localidad actualizar(Integer id, Localidad localidadDetalles) throws Exception {
        Localidad localidadExistente = localidadRepository.findById(id)
                .orElseThrow(() -> new Exception("No se encontró la localidad con ID: " + id + " para actualizar."));

        // Validar y setear la provincia si se proporciona en los detalles
        if (localidadDetalles.getProvincia() != null && localidadDetalles.getProvincia().getId() != null) {
            Provincia provinciaNueva = provinciaRepository.findById(localidadDetalles.getProvincia().getId())
                    .orElseThrow(() -> new Exception("No se encontró la provincia con ID: " + localidadDetalles.getProvincia().getId()));
            localidadExistente.setProvincia(provinciaNueva);
        }

        localidadExistente.setNombre(localidadDetalles.getNombre());
        // No actualizamos la lista de domicilios aquí directamente

        return localidadRepository.save(localidadExistente);
    }

    @Override
    @Transactional
    public boolean borrar(Integer id) throws Exception {
        if (localidadRepository.existsById(id)) {
            localidadRepository.deleteById(id);
            return true;
        } else {
            throw new Exception("No se encontró la localidad con ID: " + id + " para borrar.");
        }
    }
}
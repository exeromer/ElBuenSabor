package com.powerRanger.ElBuenSabor.services;

import com.powerRanger.ElBuenSabor.entities.Pais;
import com.powerRanger.ElBuenSabor.entities.Provincia;
import com.powerRanger.ElBuenSabor.repository.PaisRepository; // Necesitarás el PaisRepository
import com.powerRanger.ElBuenSabor.repository.ProvinciaRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import jakarta.transaction.Transactional;

import java.util.List;
import java.util.Optional;

@Service
public class ProvinciaServiceImpl implements ProvinciaService {

    @Autowired
    private ProvinciaRepository provinciaRepository;

    @Autowired
    private PaisRepository paisRepository; // Para validar/obtener el Pais

    @Override
    @Transactional
    public List<Provincia> obtenerTodas() {
        return provinciaRepository.findAll();
    }

    @Override
    @Transactional
    public Provincia obtenerPorId(Integer id) throws Exception {
        Optional<Provincia> optionalProvincia = provinciaRepository.findById(id);
        if (optionalProvincia.isPresent()) {
            return optionalProvincia.get();
        } else {
            throw new Exception("No se encontró la provincia con ID: " + id);
        }
    }

    @Override
    @Transactional
    public Provincia guardar(Provincia provincia) throws Exception {
        // Validar que el país asociado exista
        if (provincia.getPais() == null || provincia.getPais().getId() == null) {
            throw new Exception("La provincia debe estar asociada a un país válido.");
        }
        Pais paisExistente = paisRepository.findById(provincia.getPais().getId())
                .orElseThrow(() -> new Exception("No se encontró el país con ID: " + provincia.getPais().getId()));

        provincia.setPais(paisExistente); // Asegurarse de que el objeto Pais completo esté seteado
        return provinciaRepository.save(provincia);
    }

    @Override
    @Transactional
    public Provincia actualizar(Integer id, Provincia provinciaDetalles) throws Exception {
        Provincia provinciaExistente = provinciaRepository.findById(id)
                .orElseThrow(() -> new Exception("No se encontró la provincia con ID: " + id + " para actualizar."));

        // Validar y setear el país si se proporciona en los detalles
        if (provinciaDetalles.getPais() != null && provinciaDetalles.getPais().getId() != null) {
            Pais paisNuevo = paisRepository.findById(provinciaDetalles.getPais().getId())
                    .orElseThrow(() -> new Exception("No se encontró el país con ID: " + provinciaDetalles.getPais().getId()));
            provinciaExistente.setPais(paisNuevo);
        }

        provinciaExistente.setNombre(provinciaDetalles.getNombre());
        // No actualizamos la lista de localidades aquí directamente

        return provinciaRepository.save(provinciaExistente);
    }

    @Override
    @Transactional
    public boolean borrar(Integer id) throws Exception {
        if (provinciaRepository.existsById(id)) {
            provinciaRepository.deleteById(id);
            return true;
        } else {
            throw new Exception("No se encontró la provincia con ID: " + id + " para borrar.");
        }
    }
}
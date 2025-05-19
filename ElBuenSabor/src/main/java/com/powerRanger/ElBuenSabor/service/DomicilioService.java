package com.powerRanger.ElBuenSabor.service;

import com.powerRanger.ElBuenSabor.entities.Domicilio;
import com.powerRanger.ElBuenSabor.repository.DomicilioRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class DomicilioService {

    @Autowired
    private DomicilioRepository domicilioRepository;

    // Obtener todos los domicilios
    public List<Domicilio> getAllDomicilios() {
        return domicilioRepository.findAll();
    }

    // Obtener un domicilio por ID
    public Domicilio getDomicilioById(Integer id) {
        Optional<Domicilio> domicilio = domicilioRepository.findById(id);
        return domicilio.orElse(null);  // Retorna null si no se encuentra
    }

    // Crear un nuevo domicilio
    public Domicilio createDomicilio(Domicilio domicilio) {
        return domicilioRepository.save(domicilio);
    }

    // Actualizar un domicilio
    public Domicilio updateDomicilio(Integer id, Domicilio domicilio) {
        if (domicilioRepository.existsById(id)) {
            // No es necesario setear el ID manualmente
            return domicilioRepository.save(domicilio);
        }
        return null;
    }

    // Eliminar un domicilio
    public void deleteDomicilio(Integer id) {
        domicilioRepository.deleteById(id);
    }
}

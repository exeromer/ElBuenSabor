package com.powerRanger.ElBuenSabor.services;

import com.powerRanger.ElBuenSabor.entities.Pais;
import com.powerRanger.ElBuenSabor.repository.PaisRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import jakarta.transaction.Transactional; // o org.springframework.transaction.annotation.Transactional

import java.util.List;
import java.util.Optional;

@Service
public class PaisServiceImpl implements PaisService {

    @Autowired
    private PaisRepository paisRepository;

    @Override
    @Transactional
    public List<Pais> obtenerTodos() {
        return paisRepository.findAll();
    }

    @Override
    @Transactional
    public Pais obtenerPorId(Integer id) throws Exception {
        Optional<Pais> optionalPais = paisRepository.findById(id);
        if (optionalPais.isPresent()) {
            return optionalPais.get();
        } else {
            throw new Exception("No se encontró el país con ID: " + id);
        }
    }

    @Override
    @Transactional
    public Pais guardar(Pais pais) {
        // El ID se genera automáticamente si es nuevo.
        return paisRepository.save(pais);
    }

    @Override
    @Transactional
    public Pais actualizar(Integer id, Pais paisDetalles) throws Exception {
        Optional<Pais> optionalPais = paisRepository.findById(id);
        if (optionalPais.isPresent()) {
            Pais paisExistente = optionalPais.get();
            paisExistente.setNombre(paisDetalles.getNombre());
            // Aquí podrías actualizar otros campos si los hubiera
            // paisExistente.setProvincias(paisDetalles.getProvincias()); // Cuidado con las colecciones
            return paisRepository.save(paisExistente);
        } else {
            throw new Exception("No se encontró el país con ID: " + id + " para actualizar.");
        }
    }

    @Override
    @Transactional
    public boolean borrar(Integer id) throws Exception {
        if (paisRepository.existsById(id)) {
            paisRepository.deleteById(id);
            return true;
        } else {
            throw new Exception("No se encontró el país con ID: " + id + " para borrar.");
        }
    }
}
package com.powerRanger.ElBuenSabor.service;

import com.powerRanger.ElBuenSabor.entities.Sucursal;
import com.powerRanger.ElBuenSabor.repository.SucursalRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class SucursalService {

    @Autowired
    private SucursalRepository sucursalRepository;

    // Obtener todas las sucursales
    public List<Sucursal> getAllSucursales() {
        return sucursalRepository.findAll();
    }

    // Obtener una sucursal por ID
    public Sucursal getSucursalById(Integer id) {
        Optional<Sucursal> sucursal = sucursalRepository.findById(id);
        return sucursal.orElse(null);  // Retorna null si no se encuentra
    }

    // Crear una nueva sucursal
    public Sucursal createSucursal(Sucursal sucursal) {
        return sucursalRepository.save(sucursal);
    }

    // Actualizar una sucursal
    public Sucursal updateSucursal(Integer id, Sucursal sucursal) {
        if (sucursalRepository.existsById(id)) {
            return sucursalRepository.save(sucursal);
        }
        return null;
    }

    // Eliminar una sucursal
    public void deleteSucursal(Integer id) {
        sucursalRepository.deleteById(id);
    }
}

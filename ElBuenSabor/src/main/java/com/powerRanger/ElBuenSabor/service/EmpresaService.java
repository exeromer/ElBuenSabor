package com.powerRanger.ElBuenSabor.service;

import com.powerRanger.ElBuenSabor.entities.Empresa;
import com.powerRanger.ElBuenSabor.repository.EmpresaRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class EmpresaService {

    @Autowired
    private EmpresaRepository empresaRepository;

    // Obtener todas las empresas
    public List<Empresa> getAllEmpresas() {
        return empresaRepository.findAll();
    }

    // Obtener una empresa por ID
    public Empresa getEmpresaById(Integer id) {
        Optional<Empresa> empresa = empresaRepository.findById(id);
        return empresa.orElse(null);  // Retorna null si no se encuentra
    }

    // Crear una nueva empresa
    public Empresa createEmpresa(Empresa empresa) {
        return empresaRepository.save(empresa);
    }

    // Actualizar una empresa
    public Empresa updateEmpresa(Integer id, Empresa empresa) {
        if (empresaRepository.existsById(id)) {
            // No es necesario setear el ID manualmente
            return empresaRepository.save(empresa);
        }
        return null;
    }


    // Eliminar una empresa
    public void deleteEmpresa(Integer id) {
        empresaRepository.deleteById(id);
    }
}

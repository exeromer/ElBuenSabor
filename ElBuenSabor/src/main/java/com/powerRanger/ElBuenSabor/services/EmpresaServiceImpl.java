package com.powerRanger.ElBuenSabor.services;

import com.powerRanger.ElBuenSabor.dtos.EmpresaRequestDTO;
import com.powerRanger.ElBuenSabor.entities.Empresa;
import com.powerRanger.ElBuenSabor.repository.EmpresaRepository;
// Importar SucursalRepository si necesitas verificar algo sobre sucursales al borrar empresa
// import com.powerRanger.ElBuenSabor.repository.SucursalRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import jakarta.validation.Valid;
import org.springframework.validation.annotation.Validated;

import java.util.List;

@Service
@Validated
public class EmpresaServiceImpl implements EmpresaService {

    @Autowired
    private EmpresaRepository empresaRepository;
    // @Autowired
    // private SucursalRepository sucursalRepository; // Para lógicas de borrado más complejas

    private void mapDtoToEntity(EmpresaRequestDTO dto, Empresa empresa) {
        empresa.setNombre(dto.getNombre());
        empresa.setRazonSocial(dto.getRazonSocial());
        empresa.setCuil(dto.getCuil());
    }

    @Override
    @Transactional(readOnly = true)
    public List<Empresa> getAll() {
        return empresaRepository.findAll();
    }

    @Override
    @Transactional(readOnly = true)
    public Empresa getById(Integer id) throws Exception {
        return empresaRepository.findById(id)
                .orElseThrow(() -> new Exception("Empresa no encontrada con ID: " + id));
    }

    @Override
    @Transactional
    public Empresa create(@Valid EmpresaRequestDTO dto) throws Exception {
        // Aquí podrías verificar si ya existe una empresa con el mismo CUIT o nombre si deben ser únicos
        // if(empresaRepository.findByCuil(dto.getCuil()).isPresent()){
        //     throw new Exception("Ya existe una empresa con el CUIT: " + dto.getCuil());
        // }
        Empresa empresa = new Empresa();
        mapDtoToEntity(dto, empresa);
        return empresaRepository.save(empresa);
    }

    @Override
    @Transactional
    public Empresa update(Integer id, @Valid EmpresaRequestDTO dto) throws Exception {
        Empresa empresaExistente = getById(id); // Verifica si existe

        // Podrías verificar unicidad del CUIT o nombre para otros registros
        // empresaRepository.findByCuil(dto.getCuil()).ifPresent(e -> {
        //     if(!e.getId().equals(id)) throw new RuntimeException("El CUIT ya está en uso por otra empresa.");
        // });

        mapDtoToEntity(dto, empresaExistente);
        return empresaRepository.save(empresaExistente);
    }

    @Override
    @Transactional
    public void delete(Integer id) throws Exception {
        Empresa empresa = getById(id); // Verifica si existe

        // La configuración CascadeType.ALL y orphanRemoval=true en Empresa.sucursales
        // se encargará de borrar las sucursales asociadas.
        // Si no quisieras ese comportamiento en cascada, aquí deberías verificar
        // si la empresa tiene sucursales y prevenir el borrado o desasociarlas primero.
        // Ejemplo: if (!empresa.getSucursales().isEmpty()) {
        //    throw new Exception("No se puede eliminar la empresa porque tiene sucursales asociadas.");
        // }

        empresaRepository.delete(empresa);
    }
}
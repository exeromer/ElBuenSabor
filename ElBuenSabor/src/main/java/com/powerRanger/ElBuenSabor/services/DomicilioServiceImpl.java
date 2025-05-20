package com.powerRanger.ElBuenSabor.services;

import com.powerRanger.ElBuenSabor.dtos.DomicilioRequestDTO;
import com.powerRanger.ElBuenSabor.entities.Domicilio;
import com.powerRanger.ElBuenSabor.entities.Localidad;
import com.powerRanger.ElBuenSabor.repository.DomicilioRepository;
import com.powerRanger.ElBuenSabor.repository.LocalidadRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import jakarta.validation.Valid;
import org.springframework.validation.annotation.Validated;

import java.util.List;

@Service
@Validated
public class DomicilioServiceImpl implements DomicilioService {

    @Autowired
    private DomicilioRepository domicilioRepository;
    @Autowired
    private LocalidadRepository localidadRepository;

    @Override
    @Transactional(readOnly = true)
    public List<Domicilio> getAll() {
        return domicilioRepository.findAll();
    }

    @Override
    @Transactional(readOnly = true)
    public Domicilio getById(Integer id) throws Exception {
        return domicilioRepository.findById(id)
                .orElseThrow(() -> new Exception("Domicilio no encontrado con ID: " + id));
    }

    @Override
    @Transactional
    public Domicilio create(@Valid DomicilioRequestDTO dto) throws Exception {
        Localidad localidad = localidadRepository.findById(dto.getLocalidadId())
                .orElseThrow(() -> new Exception("Localidad no encontrada con ID: " + dto.getLocalidadId()));

        Domicilio domicilio = new Domicilio();
        domicilio.setCalle(dto.getCalle());
        domicilio.setNumero(dto.getNumero());
        domicilio.setCp(dto.getCp());
        domicilio.setLocalidad(localidad);
        // La lista 'clientes' se maneja desde la entidad Cliente.

        return domicilioRepository.save(domicilio);
    }

    @Override
    @Transactional
    public Domicilio update(Integer id, @Valid DomicilioRequestDTO dto) throws Exception {
        Domicilio domicilioExistente = getById(id); // Reutiliza getById para verificar existencia

        Localidad localidad = localidadRepository.findById(dto.getLocalidadId())
                .orElseThrow(() -> new Exception("Localidad no encontrada con ID: " + dto.getLocalidadId()));

        domicilioExistente.setCalle(dto.getCalle());
        domicilioExistente.setNumero(dto.getNumero());
        domicilioExistente.setCp(dto.getCp());
        domicilioExistente.setLocalidad(localidad);

        return domicilioRepository.save(domicilioExistente);
    }

    @Override
    @Transactional
    public void delete(Integer id) throws Exception {
        Domicilio domicilioExistente = getById(id); // Verifica existencia

        // Lógica de negocio: ¿Qué pasa si este domicilio está en uso por Clientes o Sucursales?
        // Deberías prevenir el borrado o manejarlo.
        if (domicilioExistente.getClientes() != null && !domicilioExistente.getClientes().isEmpty()) {
            throw new Exception("No se puede eliminar el Domicilio ID " + id + " porque está siendo utilizado por uno o más clientes.");
        }
        // Aquí también deberías verificar si está en uso por Sucursal u otras entidades.
        // Ejemplo (necesitarías un SucursalRepository y una relación en Domicilio a Sucursal si es OneToOne):
        // if (sucursalRepository.existsByDomicilioId(id)) {
        //     throw new Exception("No se puede eliminar el Domicilio ID " + id + " porque está siendo utilizado por una sucursal.");
        // }

        domicilioRepository.deleteById(id);
    }
}
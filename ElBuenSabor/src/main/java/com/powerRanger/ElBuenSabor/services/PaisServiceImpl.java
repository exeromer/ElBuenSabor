package com.powerRanger.ElBuenSabor.services;

import com.powerRanger.ElBuenSabor.dtos.PaisResponseDTO; // Importar DTO
import com.powerRanger.ElBuenSabor.entities.Pais;
import com.powerRanger.ElBuenSabor.repository.PaisRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional; // Spring's Transactional

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors; // Para el mapeo de listas

@Service
public class PaisServiceImpl implements PaisService {

    @Autowired
    private PaisRepository paisRepository;

    // Método de Mapeo de Entidad a DTO
    private PaisResponseDTO convertToDto(Pais pais) {
        PaisResponseDTO dto = new PaisResponseDTO();
        dto.setId(pais.getId());
        dto.setNombre(pais.getNombre());
        // No incluimos la lista de provincias por ahora en el DTO
        return dto;
    }

    @Override
    @Transactional(readOnly = true)
    public List<PaisResponseDTO> obtenerTodos() {
        List<Pais> paises = paisRepository.findAll();
        return paises.stream()
                .map(this::convertToDto) // Mapea cada Pais a PaisResponseDTO
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public PaisResponseDTO obtenerPorId(Integer id) throws Exception {
        Pais pais = paisRepository.findById(id)
                .orElseThrow(() -> new Exception("No se encontró el país con ID: " + id));
        return convertToDto(pais); // Mapea el Pais encontrado a DTO
    }

    @Override
    @Transactional
    public Pais guardar(Pais pais) {
        // Podrías añadir validaciones aquí si es necesario
        // ej. if (pais.getNombre() == null || pais.getNombre().trim().isEmpty()) { ... }
        return paisRepository.save(pais);
    }

    @Override
    @Transactional
    public Pais actualizar(Integer id, Pais paisDetalles) throws Exception {
        Pais paisExistente = paisRepository.findById(id)
                .orElseThrow(() -> new Exception("No se encontró el país con ID: " + id + " para actualizar."));

        // Validar denominación
        if (paisDetalles.getNombre() == null || paisDetalles.getNombre().trim().isEmpty()) {
            throw new Exception("El nombre del país es obligatorio para actualizar.");
        }
        paisExistente.setNombre(paisDetalles.getNombre());
        // La lista de provincias se gestiona desde la entidad Provincia (cuando asocias una Provincia a un Pais)
        // No la actualizamos directamente aquí.
        return paisRepository.save(paisExistente);
    }

    @Override
    @Transactional
    public boolean borrar(Integer id) throws Exception {
        Pais pais = paisRepository.findById(id)
                .orElseThrow(() -> new Exception("No se encontró el país con ID: " + id + " para borrar."));

        // Lógica de negocio antes de borrar:
        // ¿Se puede borrar un país si tiene provincias asociadas?
        if (pais.getProvincias() != null && !pais.getProvincias().isEmpty()) {
            throw new Exception("No se puede eliminar el País ID " + id + " porque tiene provincias asociadas.");
        }

        paisRepository.deleteById(id);
        return true; // O simplemente void si no necesitas devolver boolean
    }
}
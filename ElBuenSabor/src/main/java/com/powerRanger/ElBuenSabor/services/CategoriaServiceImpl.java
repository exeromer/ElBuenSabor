package com.powerRanger.ElBuenSabor.services;

import com.powerRanger.ElBuenSabor.dtos.CategoriaResponseDTO;
import com.powerRanger.ElBuenSabor.entities.Categoria;
import com.powerRanger.ElBuenSabor.repository.CategoriaRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class CategoriaServiceImpl extends BaseServiceImpl<Categoria, CategoriaRepository> implements CategoriaService {

    public CategoriaServiceImpl(CategoriaRepository categoriaRepository) {
        super(categoriaRepository);
    }

    // Los métodos save, update, delete y findById(devuelve Entidad) son heredados y no necesitan implementación.

    @Override
    @Transactional(readOnly = true)
    public List<CategoriaResponseDTO> findAllCategorias() {
        try {
            // 1. Llama al método findAll() heredado de BaseServiceImpl
            List<Categoria> categorias = super.findAll();
            // 2. Mapea la lista de entidades a DTOs
            return categorias.stream()
                    .map(this::convertToDto)
                    .collect(Collectors.toList());
        } catch (Exception e) {
            throw new RuntimeException("Error al buscar las categorías: " + e.getMessage(), e);
        }
    }

    @Override
    @Transactional(readOnly = true)
    public CategoriaResponseDTO findCategoriaById(Integer id) throws Exception {
        // 1. Llama al método findById() heredado
        Categoria categoria = super.findById(id);
        // 2. Mapea la entidad a DTO
        return convertToDto(categoria);
    }

    // El mapper se mantiene igual
    private CategoriaResponseDTO convertToDto(Categoria categoria) {
        CategoriaResponseDTO dto = new CategoriaResponseDTO();
        dto.setId(categoria.getId());
        dto.setDenominacion(categoria.getDenominacion());
        dto.setEstadoActivo(categoria.getEstadoActivo());
        return dto;
    }
}
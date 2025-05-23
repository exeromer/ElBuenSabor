package com.powerRanger.ElBuenSabor.services;

import com.powerRanger.ElBuenSabor.dtos.CategoriaResponseDTO; // Importar DTO
import com.powerRanger.ElBuenSabor.entities.Categoria;
import com.powerRanger.ElBuenSabor.repository.CategoriaRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional; // Cambiado de jakarta.transaction
// import jakarta.validation.Valid; // Si create/update usaran DTOs
// import org.springframework.validation.annotation.Validated; // Si la clase usa @Valid en parámetros

import java.util.List;
import java.util.stream.Collectors; // Para el mapeo de listas

@Service
// @Validated // Necesario si los métodos de servicio aceptan DTOs con @Valid
public class CategoriaServiceImpl implements CategoriaService {

    @Autowired
    private CategoriaRepository categoriaRepository;

    // Método de Mapeo de Entidad a DTO
    private CategoriaResponseDTO convertToDto(Categoria categoria) {
        CategoriaResponseDTO dto = new CategoriaResponseDTO();
        dto.setId(categoria.getId());
        dto.setDenominacion(categoria.getDenominacion());
        dto.setEstadoActivo(categoria.getEstadoActivo());
        // Si decides incluir IDs de artículos:
        // if (categoria.getArticulos() != null) {
        //     dto.setArticuloIds(categoria.getArticulos().stream().map(Articulo::getId).collect(Collectors.toList()));
        // }
        return dto;
    }

    @Override
    @Transactional(readOnly = true) // readOnly para métodos GET
    public List<CategoriaResponseDTO> getAllCategorias() {
        List<Categoria> categorias = categoriaRepository.findAll();
        return categorias.stream()
                .map(this::convertToDto) // Mapea cada Categoria a CategoriaResponseDTO
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public CategoriaResponseDTO getCategoriaById(Integer id) throws Exception {
        Categoria categoria = categoriaRepository.findById(id)
                .orElseThrow(() -> new Exception("Categoría no encontrada con ID: " + id));
        return convertToDto(categoria); // Mapea la Categoria encontrada a DTO
    }

    @Override
    @Transactional
    public Categoria createCategoria(Categoria categoria) throws Exception {
        // Mantenemos la lógica original para create, update, delete por ahora
        if (categoria.getDenominacion() == null || categoria.getDenominacion().trim().isEmpty()) {
            throw new Exception("La denominación de la categoría es obligatoria.");
        }
        return categoriaRepository.save(categoria);
    }

    @Override
    @Transactional
    public Categoria updateCategoria(Integer id, Categoria categoriaDetails) throws Exception {
        Categoria categoriaExistente = categoriaRepository.findById(id)
                .orElseThrow(() -> new Exception("Categoría no encontrada con ID: " + id + " para actualizar."));

        if (categoriaDetails.getDenominacion() == null || categoriaDetails.getDenominacion().trim().isEmpty()) {
            throw new Exception("La denominación de la categoría es obligatoria para actualizar.");
        }

        categoriaExistente.setDenominacion(categoriaDetails.getDenominacion());
        categoriaExistente.setEstadoActivo(categoriaDetails.getEstadoActivo());
        return categoriaRepository.save(categoriaExistente);
    }

    @Override
    @Transactional
    public void deleteCategoria(Integer id) throws Exception {
        if (!categoriaRepository.existsById(id)) {
            throw new Exception("Categoría no encontrada con ID: " + id + " para eliminar.");
        }
        categoriaRepository.deleteById(id);
    }
}
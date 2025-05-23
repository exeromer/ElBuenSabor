package com.powerRanger.ElBuenSabor.services;

import com.powerRanger.ElBuenSabor.dtos.CategoriaResponseDTO; // Importar el nuevo DTO
import com.powerRanger.ElBuenSabor.entities.Categoria;
// Importar @Valid y CategoriaRequestDTO si la creación/actualización usa DTOs
// import jakarta.validation.Valid;
// import com.powerRanger.ElBuenSabor.dtos.CategoriaRequestDTO;


import java.util.List;

public interface CategoriaService {
    List<CategoriaResponseDTO> getAllCategorias(); // Devuelve lista de DTOs
    CategoriaResponseDTO getCategoriaById(Integer id) throws Exception; // Devuelve un DTO

    // Los métodos de creación y actualización pueden seguir usando la entidad directamente o un CategoriaRequestDTO
    // Por ahora, los mantenemos como estaban para no cambiar el scope de esta refactorización
    Categoria createCategoria(Categoria categoria) throws Exception;
    Categoria updateCategoria(Integer id, Categoria categoria) throws Exception;
    void deleteCategoria(Integer id) throws Exception;
}
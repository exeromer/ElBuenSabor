package com.powerRanger.ElBuenSabor.services;

import com.powerRanger.ElBuenSabor.dtos.CategoriaResponseDTO;
import com.powerRanger.ElBuenSabor.entities.Categoria;
import java.util.List;

public interface CategoriaService extends BaseService<Categoria, Integer> {
    // Aquí solo declaramos métodos que devuelven DTOs o que tienen lógica extra.
    // Si los métodos CRUD básicos son suficientes, esta interfaz podría incluso estar vacía.
    List<CategoriaResponseDTO> findAllCategorias();
    CategoriaResponseDTO findCategoriaById(Integer id) throws Exception;
}
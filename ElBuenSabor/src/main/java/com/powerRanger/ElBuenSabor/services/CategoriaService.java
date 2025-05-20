package com.powerRanger.ElBuenSabor.services;

import com.powerRanger.ElBuenSabor.entities.Categoria;
import java.util.List;

public interface CategoriaService {
    List<Categoria> getAllCategorias();
    Categoria getCategoriaById(Integer id) throws Exception; // Lanzar excepción si no se encuentra
    Categoria createCategoria(Categoria categoria) throws Exception;
    Categoria updateCategoria(Integer id, Categoria categoria) throws Exception; // Lanzar excepción
    void deleteCategoria(Integer id) throws Exception; // Lanzar excepción
}
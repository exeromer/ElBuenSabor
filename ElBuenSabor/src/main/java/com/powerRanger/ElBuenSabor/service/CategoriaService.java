package com.powerRanger.ElBuenSabor.service;

import com.powerRanger.ElBuenSabor.entities.Categoria;
import com.powerRanger.ElBuenSabor.repository.CategoriaRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class CategoriaService {

    @Autowired
    private CategoriaRepository categoriaRepository;

    // Obtener todas las categorías
    public List<Categoria> getAllCategorias() {
        return categoriaRepository.findAll();
    }

    // Obtener una categoría por ID
    public Categoria getCategoriaById(Integer id) {
        Optional<Categoria> categoria = categoriaRepository.findById(id);
        return categoria.orElse(null);  // Retorna null si no se encuentra
    }

    // Crear una nueva categoría
    public Categoria createCategoria(Categoria categoria) {
        return categoriaRepository.save(categoria);
    }

    // Actualizar una categoría
    public Categoria updateCategoria(Integer id, Categoria categoria) {
        if (categoriaRepository.existsById(id)) {
            // No es necesario setear el ID manualmente
            return categoriaRepository.save(categoria);
        }
        return null;
    }

    // Eliminar una categoría
    public void deleteCategoria(Integer id) {
        categoriaRepository.deleteById(id);
    }
}

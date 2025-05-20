package com.powerRanger.ElBuenSabor.services;

import com.powerRanger.ElBuenSabor.entities.Categoria;
import com.powerRanger.ElBuenSabor.repository.CategoriaRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import jakarta.transaction.Transactional; // Usar preferentemente de Spring

import java.util.List;
import java.util.Optional;

@Service
public class CategoriaServiceImpl implements CategoriaService { // Implementar la interfaz

    @Autowired
    private CategoriaRepository categoriaRepository;

    @Override
    @Transactional
    public List<Categoria> getAllCategorias() {
        return categoriaRepository.findAll();
    }

    @Override
    @Transactional
    public Categoria getCategoriaById(Integer id) throws Exception {
        return categoriaRepository.findById(id)
                .orElseThrow(() -> new Exception("Categoría no encontrada con ID: " + id));
    }

    @Override
    @Transactional
    public Categoria createCategoria(Categoria categoria) throws Exception {
        if (categoria.getDenominacion() == null || categoria.getDenominacion().trim().isEmpty()) {
            throw new Exception("La denominación de la categoría es obligatoria.");
        }
        // Aquí podrías añadir lógica para verificar si ya existe una categoría con la misma denominación
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
        // Manejar la lista de artículos es más complejo y usualmente se hace con DTOs o endpoints específicos
        // No actualizaremos la lista 'articulos' directamente aquí para evitar problemas.

        return categoriaRepository.save(categoriaExistente);
    }

    @Override
    @Transactional
    public void deleteCategoria(Integer id) throws Exception {
        if (!categoriaRepository.existsById(id)) {
            throw new Exception("Categoría no encontrada con ID: " + id + " para eliminar.");
        }
        // Considerar la lógica de negocio: ¿Qué pasa con los artículos de esta categoría?
        // ¿Se borran, se desasocian, se previene el borrado si tiene artículos?
        // Por ahora, solo borramos la categoría.
        categoriaRepository.deleteById(id);
    }
}
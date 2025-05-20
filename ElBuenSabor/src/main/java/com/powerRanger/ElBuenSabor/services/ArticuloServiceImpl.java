package com.powerRanger.ElBuenSabor.services;

import com.powerRanger.ElBuenSabor.entities.Articulo;
import com.powerRanger.ElBuenSabor.entities.Categoria;
import com.powerRanger.ElBuenSabor.entities.UnidadMedida;
import com.powerRanger.ElBuenSabor.repository.ArticuloRepository;
import com.powerRanger.ElBuenSabor.repository.CategoriaRepository;
import com.powerRanger.ElBuenSabor.repository.UnidadMedidaRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional; // Preferible de Spring
import jakarta.validation.Valid;
import org.springframework.validation.annotation.Validated;

import java.util.List;

@Service
@Validated // Para activar la validación a nivel de clase
public class ArticuloServiceImpl implements ArticuloService {

    @Autowired
    private ArticuloRepository articuloRepository;
    @Autowired
    private CategoriaRepository categoriaRepository;
    @Autowired
    private UnidadMedidaRepository unidadMedidaRepository;

    @Override
    @Transactional(readOnly = true) // readOnly = true para métodos de solo lectura
    public List<Articulo> getAllArticulos() {
        return articuloRepository.findAll();
    }

    @Override
    @Transactional(readOnly = true)
    public Articulo getArticuloById(Integer id) throws Exception {
        return articuloRepository.findById(id)
                .orElseThrow(() -> new Exception("Artículo no encontrado con ID: " + id));
    }

    @Override
    @Transactional
    public Articulo createArticulo(@Valid Articulo articulo) throws Exception {
        // Validar y obtener Categoria
        if (articulo.getCategoria() == null || articulo.getCategoria().getId() == null) {
            throw new Exception("La categoría es obligatoria para el artículo.");
        }
        Categoria categoria = categoriaRepository.findById(articulo.getCategoria().getId())
                .orElseThrow(() -> new Exception("Categoría no encontrada con ID: " + articulo.getCategoria().getId()));
        articulo.setCategoria(categoria);

        // Validar y obtener UnidadMedida
        if (articulo.getUnidadMedida() == null || articulo.getUnidadMedida().getId() == null) {
            throw new Exception("La unidad de medida es obligatoria para el artículo.");
        }
        UnidadMedida unidadMedida = unidadMedidaRepository.findById(articulo.getUnidadMedida().getId())
                .orElseThrow(() -> new Exception("Unidad de medida no encontrada con ID: " + articulo.getUnidadMedida().getId()));
        articulo.setUnidadMedida(unidadMedida);

        return articuloRepository.save(articulo);
    }

    @Override
    @Transactional
    public Articulo updateArticulo(Integer id, @Valid Articulo articuloDetalles) throws Exception {
        Articulo articuloExistente = getArticuloById(id); // Reutiliza getArticuloById para la validación "no encontrado"

        articuloExistente.setDenominacion(articuloDetalles.getDenominacion());
        articuloExistente.setPrecioVenta(articuloDetalles.getPrecioVenta());
        articuloExistente.setEstadoActivo(articuloDetalles.getEstadoActivo());

        if (articuloDetalles.getCategoria() != null && articuloDetalles.getCategoria().getId() != null) {
            Categoria categoria = categoriaRepository.findById(articuloDetalles.getCategoria().getId())
                    .orElseThrow(() -> new Exception("Categoría no encontrada con ID: " + articuloDetalles.getCategoria().getId()));
            articuloExistente.setCategoria(categoria);
        } else {
            if(articuloDetalles.getCategoria() == null && articuloExistente.getCategoria() != null ) {
                // No hacer nada si no se envía categoría y ya tiene una, o lanzar error si es mandatorio siempre enviar
            } else if (articuloDetalles.getCategoria() == null) { // Si es mandatorio
                throw new Exception("La categoría es obligatoria para el artículo.");
            }
        }


        if (articuloDetalles.getUnidadMedida() != null && articuloDetalles.getUnidadMedida().getId() != null) {
            UnidadMedida unidadMedida = unidadMedidaRepository.findById(articuloDetalles.getUnidadMedida().getId())
                    .orElseThrow(() -> new Exception("Unidad de medida no encontrada con ID: " + articuloDetalles.getUnidadMedida().getId()));
            articuloExistente.setUnidadMedida(unidadMedida);
        } else {
            if(articuloDetalles.getUnidadMedida() == null && articuloExistente.getUnidadMedida() != null ) {
                // No hacer nada si no se envía unidad y ya tiene una, o lanzar error si es mandatorio siempre enviar
            } else if (articuloDetalles.getUnidadMedida() == null) { // Si es mandatorio
                throw new Exception("La unidad de medida es obligatoria para el artículo.");
            }
        }

        // Las colecciones como imagenes, detallesPedidos, etc., se manejan por separado.
        // No se actualizan masivamente aquí.

        return articuloRepository.save(articuloExistente);
    }

    @Override
    @Transactional
    public void deleteArticulo(Integer id) throws Exception {
        if (!articuloRepository.existsById(id)) { // Verifica si existe antes de borrar
            throw new Exception("Artículo no encontrado con ID: " + id + " para eliminar.");
        }
        // Aquí podrías añadir lógica para verificar si el artículo está en pedidos activos, etc.
        articuloRepository.deleteById(id);
    }

    @Override
    @Transactional(readOnly = true)
    public Articulo findByDenominacion(String denominacion) throws Exception {
        return articuloRepository.findByDenominacion(denominacion)
                .orElseThrow(() -> new Exception("Artículo no encontrado con denominación: " + denominacion));
    }
}
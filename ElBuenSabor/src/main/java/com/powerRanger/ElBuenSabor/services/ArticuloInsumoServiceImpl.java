package com.powerRanger.ElBuenSabor.services;

import com.powerRanger.ElBuenSabor.entities.ArticuloInsumo;
import com.powerRanger.ElBuenSabor.entities.Categoria;
import com.powerRanger.ElBuenSabor.entities.UnidadMedida;
import com.powerRanger.ElBuenSabor.repository.ArticuloInsumoRepository;
import com.powerRanger.ElBuenSabor.repository.CategoriaRepository;
import com.powerRanger.ElBuenSabor.repository.UnidadMedidaRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import jakarta.validation.Valid;
import org.springframework.validation.annotation.Validated;

import java.util.List;

@Service
@Validated
public class ArticuloInsumoServiceImpl implements ArticuloInsumoService {

    @Autowired
    private ArticuloInsumoRepository articuloInsumoRepository;
    @Autowired
    private CategoriaRepository categoriaRepository; // Para asociar Categoria
    @Autowired
    private UnidadMedidaRepository unidadMedidaRepository; // Para asociar UnidadMedida

    @Override
    @Transactional(readOnly = true)
    public List<ArticuloInsumo> getAllArticuloInsumo() {
        return articuloInsumoRepository.findAll();
    }

    @Override
    @Transactional(readOnly = true)
    public ArticuloInsumo getArticuloInsumoById(Integer id) throws Exception {
        return articuloInsumoRepository.findById(id)
                .orElseThrow(() -> new Exception("Artículo Insumo no encontrado con ID: " + id));
    }

    @Override
    @Transactional
    public ArticuloInsumo createArticuloInsumo(@Valid ArticuloInsumo articuloInsumo) throws Exception {
        // Validar y obtener Categoria (heredado de Articulo)
        if (articuloInsumo.getCategoria() == null || articuloInsumo.getCategoria().getId() == null) {
            throw new Exception("La categoría es obligatoria para el artículo insumo.");
        }
        Categoria categoria = categoriaRepository.findById(articuloInsumo.getCategoria().getId())
                .orElseThrow(() -> new Exception("Categoría no encontrada con ID: " + articuloInsumo.getCategoria().getId()));
        articuloInsumo.setCategoria(categoria);

        // Validar y obtener UnidadMedida (heredado de Articulo)
        if (articuloInsumo.getUnidadMedida() == null || articuloInsumo.getUnidadMedida().getId() == null) {
            throw new Exception("La unidad de medida es obligatoria para el artículo insumo.");
        }
        UnidadMedida unidadMedida = unidadMedidaRepository.findById(articuloInsumo.getUnidadMedida().getId())
                .orElseThrow(() -> new Exception("Unidad de medida no encontrada con ID: " + articuloInsumo.getUnidadMedida().getId()));
        articuloInsumo.setUnidadMedida(unidadMedida);

        // Las validaciones @NotEmpty, @NotNull, @DecimalMin en Articulo y ArticuloInsumo se verificarán
        // gracias a @Valid en el parámetro y @Validated en la clase.

        return articuloInsumoRepository.save(articuloInsumo);
    }

    @Override
    @Transactional
    public ArticuloInsumo updateArticuloInsumo(Integer id, @Valid ArticuloInsumo articuloInsumoDetails) throws Exception {
        ArticuloInsumo insumoExistente = getArticuloInsumoById(id); // Reutiliza get para validación "no encontrado"

        // Actualizar campos de Articulo (superclase)
        insumoExistente.setDenominacion(articuloInsumoDetails.getDenominacion());
        insumoExistente.setPrecioVenta(articuloInsumoDetails.getPrecioVenta());
        insumoExistente.setEstadoActivo(articuloInsumoDetails.getEstadoActivo());

        if (articuloInsumoDetails.getCategoria() != null && articuloInsumoDetails.getCategoria().getId() != null) {
            Categoria categoria = categoriaRepository.findById(articuloInsumoDetails.getCategoria().getId())
                    .orElseThrow(() -> new Exception("Categoría no encontrada con ID: " + articuloInsumoDetails.getCategoria().getId()));
            insumoExistente.setCategoria(categoria);
        } else if (articuloInsumoDetails.getCategoria() == null) { // Si es mandatorio tener categoría
            throw new Exception("La categoría es obligatoria para el artículo insumo.");
        }


        if (articuloInsumoDetails.getUnidadMedida() != null && articuloInsumoDetails.getUnidadMedida().getId() != null) {
            UnidadMedida unidadMedida = unidadMedidaRepository.findById(articuloInsumoDetails.getUnidadMedida().getId())
                    .orElseThrow(() -> new Exception("Unidad de medida no encontrada con ID: " + articuloInsumoDetails.getUnidadMedida().getId()));
            insumoExistente.setUnidadMedida(unidadMedida);
        } else if (articuloInsumoDetails.getUnidadMedida() == null) { // Si es mandatorio tener unidad de medida
            throw new Exception("La unidad de medida es obligatoria para el artículo insumo.");
        }

        // Actualizar campos específicos de ArticuloInsumo
        insumoExistente.setPrecioCompra(articuloInsumoDetails.getPrecioCompra());
        insumoExistente.setStockActual(articuloInsumoDetails.getStockActual());
        insumoExistente.setStockMaximo(articuloInsumoDetails.getStockMaximo());
        insumoExistente.setEsParaElaborar(articuloInsumoDetails.getEsParaElaborar());

        return articuloInsumoRepository.save(insumoExistente);
    }

    @Override
    @Transactional
    public void deleteArticuloInsumo(Integer id) throws Exception {
        if (!articuloInsumoRepository.existsById(id)) {
            throw new Exception("Artículo Insumo no encontrado con ID: " + id + " para eliminar.");
        }
        articuloInsumoRepository.deleteById(id);
    }
}
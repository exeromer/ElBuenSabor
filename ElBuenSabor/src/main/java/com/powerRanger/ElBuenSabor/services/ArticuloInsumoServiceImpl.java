package com.powerRanger.ElBuenSabor.services;

import com.powerRanger.ElBuenSabor.dtos.ArticuloInsumoResponseDTO;
import com.powerRanger.ElBuenSabor.dtos.ArticuloInsumoRequestDTO; // CAMBIO AQUÍ
import com.powerRanger.ElBuenSabor.entities.ArticuloInsumo;
import com.powerRanger.ElBuenSabor.entities.Categoria;
import com.powerRanger.ElBuenSabor.entities.UnidadMedida;
import com.powerRanger.ElBuenSabor.mappers.Mappers; // Asumiendo que Mappers está en este paquete
import com.powerRanger.ElBuenSabor.repository.ArticuloInsumoRepository;
import com.powerRanger.ElBuenSabor.repository.CategoriaRepository;
import com.powerRanger.ElBuenSabor.repository.UnidadMedidaRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import jakarta.validation.Valid;
import org.springframework.validation.annotation.Validated;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
@Validated
public class ArticuloInsumoServiceImpl implements ArticuloInsumoService {

    @Autowired private ArticuloInsumoRepository articuloInsumoRepository;
    @Autowired private CategoriaRepository categoriaRepository;
    @Autowired private UnidadMedidaRepository unidadMedidaRepository;
    @Autowired private Mappers mappers; // Usar la clase Mappers

    // Método helper para mapear el DTO de Request a la Entidad
    private void mapDtoToEntity(ArticuloInsumoRequestDTO dto, ArticuloInsumo insumo) throws Exception {
        insumo.setDenominacion(dto.getDenominacion());
        insumo.setPrecioVenta(dto.getPrecioVenta());
        insumo.setEstadoActivo(dto.getEstadoActivo());

        Categoria categoria = categoriaRepository.findById(dto.getCategoriaId())
                .orElseThrow(() -> new Exception("Categoría no encontrada con ID: " + dto.getCategoriaId()));
        insumo.setCategoria(categoria);

        UnidadMedida unidadMedida = unidadMedidaRepository.findById(dto.getUnidadMedidaId())
                .orElseThrow(() -> new Exception("Unidad de medida no encontrada con ID: " + dto.getUnidadMedidaId()));
        insumo.setUnidadMedida(unidadMedida);

        insumo.setPrecioCompra(dto.getPrecioCompra());
        insumo.setStockActual(dto.getStockActual());
        insumo.setstockMinimo(dto.getstockMinimo());
        insumo.setEsParaElaborar(dto.getEsParaElaborar());

        // El manejo de imágenes (asociar por ID o subir nuevas) se haría aquí
        // si ArticuloInsumoRequestDTO tuviera imagenIds o si la subida fuera parte de este flujo.
        // Actualmente, tu ImagenService y FileUploadController se encargan de esto
        // después de que el artículo es creado/actualizado y se le pasa el ID del artículo.
    }

    @Override
    @Transactional(readOnly = true)
    public List<ArticuloInsumoResponseDTO> getAllArticuloInsumo(String searchTerm, Boolean estadoActivo) {
        List<ArticuloInsumo> insumos;
        String trimmedSearchTerm = (searchTerm != null && !searchTerm.trim().isEmpty()) ? searchTerm.trim() : null;

        if (trimmedSearchTerm != null) {
            insumos = articuloInsumoRepository.searchByDenominacionWithOptionalStatus(trimmedSearchTerm, estadoActivo);
            System.out.println("DEBUG: Buscando insumos con término: '" + trimmedSearchTerm + "', Estado: " + estadoActivo + ", Encontrados: " + insumos.size());
        } else {
            insumos = articuloInsumoRepository.findAllWithOptionalStatus(estadoActivo);
            System.out.println("DEBUG: Obteniendo insumos con Estado: " + estadoActivo + ", Encontrados: " + insumos.size());
        }
        return insumos.stream()
                .map(insumo -> (ArticuloInsumoResponseDTO) mappers.convertArticuloToResponseDto(insumo))
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public ArticuloInsumoResponseDTO getArticuloInsumoById(Integer id) throws Exception {
        ArticuloInsumo insumo = articuloInsumoRepository.findById(id)
                .orElseThrow(() -> new Exception("Artículo Insumo no encontrado con ID: " + id));
        return (ArticuloInsumoResponseDTO) mappers.convertArticuloToResponseDto(insumo);
    }

    @Override
    @Transactional
    public ArticuloInsumoResponseDTO createArticuloInsumo(@Valid ArticuloInsumoRequestDTO dto) throws Exception { // CAMBIO AQUÍ
        ArticuloInsumo insumo = new ArticuloInsumo();
        insumo.setImagenes(new ArrayList<>()); // Inicializar la lista si la entidad la tiene
        mapDtoToEntity(dto, insumo); // Usar el helper
        ArticuloInsumo guardado = articuloInsumoRepository.save(insumo);
        return (ArticuloInsumoResponseDTO) mappers.convertArticuloToResponseDto(guardado);
    }

    @Override
    @Transactional
    public ArticuloInsumoResponseDTO updateArticuloInsumo(Integer id, @Valid ArticuloInsumoRequestDTO dto) throws Exception { // CAMBIO AQUÍ
        ArticuloInsumo insumoExistente = articuloInsumoRepository.findById(id)
                .orElseThrow(() -> new Exception("Artículo Insumo no encontrado con ID: " + id));
        mapDtoToEntity(dto, insumoExistente); // Usar el helper
        ArticuloInsumo actualizado = articuloInsumoRepository.save(insumoExistente);
        return (ArticuloInsumoResponseDTO) mappers.convertArticuloToResponseDto(actualizado);
    }

    @Override
    @Transactional
    public void deleteArticuloInsumo(Integer id) throws Exception {
        // ... (la lógica de delete no cambia fundamentalmente, pero asegúrate que exista el insumo)
        ArticuloInsumo insumo = articuloInsumoRepository.findById(id)
                .orElseThrow(() -> new Exception("Artículo Insumo no encontrado con ID: " + id + " para eliminar."));
        // Considera si necesitas verificar si el insumo está en uso antes de borrarlo (ej. en ArticuloManufacturadoDetalle)
        // if (!insumo.getDetallesManufacturados().isEmpty()) { // Necesitarías la relación inversa
        //    throw new Exception("No se puede eliminar el insumo porque está siendo utilizado.");
        // }
        articuloInsumoRepository.delete(insumo);
    }

}
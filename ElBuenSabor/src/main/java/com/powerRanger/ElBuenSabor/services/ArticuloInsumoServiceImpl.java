package com.powerRanger.ElBuenSabor.services;

import com.powerRanger.ElBuenSabor.dtos.ArticuloInsumoResponseDTO;
import com.powerRanger.ElBuenSabor.dtos.ArticuloInsumoRequestDTO;
import com.powerRanger.ElBuenSabor.entities.ArticuloInsumo;
import com.powerRanger.ElBuenSabor.entities.Categoria;
import com.powerRanger.ElBuenSabor.entities.StockInsumoSucursal; // Importar StockInsumoSucursal
import com.powerRanger.ElBuenSabor.entities.Sucursal; // Importar Sucursal
import com.powerRanger.ElBuenSabor.entities.UnidadMedida;
import com.powerRanger.ElBuenSabor.mappers.Mappers;
import com.powerRanger.ElBuenSabor.repository.ArticuloInsumoRepository;
import com.powerRanger.ElBuenSabor.repository.CategoriaRepository;
import com.powerRanger.ElBuenSabor.repository.StockInsumoSucursalRepository; // Importar nuevo repositorio
import com.powerRanger.ElBuenSabor.repository.SucursalRepository; // Importar SucursalRepository
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
    @Autowired private Mappers mappers;
    @Autowired private StockInsumoSucursalRepository stockInsumoSucursalRepository; // Inyectar
    @Autowired private SucursalRepository sucursalRepository; // Inyectar para gestionar stock inicial

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
        insumo.setEsParaElaborar(dto.getEsParaElaborar());
        // stockActual y stockMinimo ya no están en ArticuloInsumoRequestDTO
        // Por lo tanto, no se mapean aquí.
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
                .map(insumo -> {
                    ArticuloInsumoResponseDTO dto = (ArticuloInsumoResponseDTO) mappers.convertArticuloToResponseDto(insumo);
                    // Stock actual y mínimo ya no se recuperan directamente de ArticuloInsumo.
                    // Estos campos fueron ELIMINADOS de ArticuloInsumoResponseDTO.
                    // Se elimina la asignación a null.
                    // dto.setStockActual(null); // ELIMINADO
                    // dto.setstockMinimo(null); // ELIMINADO
                    return dto;
                })
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public ArticuloInsumoResponseDTO getArticuloInsumoById(Integer id) throws Exception {
        ArticuloInsumo insumo = articuloInsumoRepository.findById(id)
                .orElseThrow(() -> new Exception("Artículo Insumo no encontrado con ID: " + id));
        ArticuloInsumoResponseDTO dto = (ArticuloInsumoResponseDTO) mappers.convertArticuloToResponseDto(insumo);
        // Stock actual y mínimo ya no se recuperan directamente de ArticuloInsumo.
        // Estos campos fueron ELIMINADOS de ArticuloInsumoResponseDTO.
        // Se elimina la asignación a null.
        // dto.setStockActual(null); // ELIMINADO
        // dto.setstockMinimo(null); // ELIMINADO
        return dto;
    }

    @Override
    @Transactional
    public ArticuloInsumoResponseDTO createArticuloInsumo(@Valid ArticuloInsumoRequestDTO dto) throws Exception {
        ArticuloInsumo insumo = new ArticuloInsumo();
        insumo.setImagenes(new ArrayList<>()); // Inicializar la lista si la entidad la tiene
        mapDtoToEntity(dto, insumo); // Usar el helper para mapear los campos del insumo
        ArticuloInsumo guardado = articuloInsumoRepository.save(insumo);

        // AHORA: La creación del insumo NO inicializa automáticamente stocks en todas las sucursales.
        // Esto se hará explícitamente vía StockInsumoSucursalController/Service
        // o en el DataInitializer para datos de prueba.

        return (ArticuloInsumoResponseDTO) mappers.convertArticuloToResponseDto(guardado);
    }

    @Override
    @Transactional
    public ArticuloInsumoResponseDTO updateArticuloInsumo(Integer id, @Valid ArticuloInsumoRequestDTO dto) throws Exception {
        ArticuloInsumo insumoExistente = articuloInsumoRepository.findById(id)
                .orElseThrow(() -> new Exception("Artículo Insumo no encontrado con ID: " + id));
        mapDtoToEntity(dto, insumoExistente); // Esto actualiza campos del insumo (denominación, precio, etc.)
        ArticuloInsumo actualizado = articuloInsumoRepository.save(insumoExistente);

        // AHORA: La actualización del insumo NO modifica sus stocks en StockInsumoSucursal.
        // Los stocks se modifican solo a través de StockInsumoSucursalController/Service
        // de forma específica por sucursal, o en el caso del ArticuloManufacturadoService,
        // cuando se descuenta por la venta.

        return (ArticuloInsumoResponseDTO) mappers.convertArticuloToResponseDto(actualizado);
    }

    @Override
    @Transactional
    public void deleteArticuloInsumo(Integer id) throws Exception {
        ArticuloInsumo insumo = articuloInsumoRepository.findById(id)
                .orElseThrow(() -> new Exception("Artículo Insumo no encontrado con ID: " + id + " para eliminar."));
        // Debido a CascadeType.ALL y orphanRemoval=true en ArticuloInsumo.stockPorSucursal,
        // al eliminar el insumo, sus registros de stock por sucursal se eliminarán automáticamente.
        // Por lo tanto, la siguiente línea es redundante y ha sido eliminada.
        // stockInsumoSucursalRepository.deleteByArticuloInsumo(insumo); // ELIMINADO
        articuloInsumoRepository.delete(insumo);
    }
}
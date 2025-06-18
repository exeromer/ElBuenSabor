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
        // Los campos stockActual y stockMinimo ya no están en ArticuloInsumo, se gestionan en StockInsumoSucursal
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
                    // Stock actual y mínimo ya no se recuperan directamente de ArticuloInsumo
                    dto.setStockActual(null); // O 0.0, ya que es stock general y ahora es por sucursal
                    dto.setstockMinimo(null); // O 0.0
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
        // Stock actual y mínimo ya no se recuperan directamente de ArticuloInsumo
        dto.setStockActual(null);
        dto.setstockMinimo(null);
        return dto;
    }

    @Override
    @Transactional
    public ArticuloInsumoResponseDTO createArticuloInsumo(@Valid ArticuloInsumoRequestDTO dto) throws Exception {
        ArticuloInsumo insumo = new ArticuloInsumo();
        insumo.setImagenes(new ArrayList<>()); // Inicializar la lista si la entidad la tiene
        mapDtoToEntity(dto, insumo); // Usar el helper
        ArticuloInsumo guardado = articuloInsumoRepository.save(insumo);

        // Al crear un nuevo insumo, se debe inicializar su stock en cada sucursal existente
        List<Sucursal> sucursales = sucursalRepository.findAll();
        for (Sucursal sucursal : sucursales) {
            StockInsumoSucursal stockInsumoSucursal = new StockInsumoSucursal(
                    guardado,
                    sucursal,
                    dto.getStockActual() != null ? dto.getStockActual() : 0.0, // Stock inicial del DTO
                    dto.getstockMinimo() != null ? dto.getstockMinimo() : 0.0  // Stock mínimo del DTO
            );
            stockInsumoSucursalRepository.save(stockInsumoSucursal);
            guardado.addStockInsumoSucursal(stockInsumoSucursal); // Asocia el stock a la entidad
        }

        return (ArticuloInsumoResponseDTO) mappers.convertArticuloToResponseDto(guardado);
    }

    @Override
    @Transactional
    public ArticuloInsumoResponseDTO updateArticuloInsumo(Integer id, @Valid ArticuloInsumoRequestDTO dto) throws Exception {
        ArticuloInsumo insumoExistente = articuloInsumoRepository.findById(id)
                .orElseThrow(() -> new Exception("Artículo Insumo no encontrado con ID: " + id));
        mapDtoToEntity(dto, insumoExistente); // Esto actualiza campos del insumo, no el stock
        ArticuloInsumo actualizado = articuloInsumoRepository.save(insumoExistente);

        // Actualizar los registros de StockInsumoSucursal asociados a este insumo
        List<StockInsumoSucursal> stocksPorSucursal = stockInsumoSucursalRepository.findByArticuloInsumo(actualizado);
        for (StockInsumoSucursal stock : stocksPorSucursal) {
            if (dto.getStockActual() != null) {
                stock.setStockActual(dto.getStockActual());
            }
            if (dto.getstockMinimo() != null) {
                stock.setStockMinimo(dto.getstockMinimo());
            }
            stockInsumoSucursalRepository.save(stock);
        }

        return (ArticuloInsumoResponseDTO) mappers.convertArticuloToResponseDto(actualizado);
    }

    @Override
    @Transactional
    public void deleteArticuloInsumo(Integer id) throws Exception {
        ArticuloInsumo insumo = articuloInsumoRepository.findById(id)
                .orElseThrow(() -> new Exception("Artículo Insumo no encontrado con ID: " + id + " para eliminar."));
        // Debido a CascadeType.ALL y orphanRemoval=true en ArticuloInsumo.stockPorSucursal,
        // al eliminar el insumo, sus registros de stock por sucursal se eliminarán automáticamente.
        articuloInsumoRepository.delete(insumo);
    }
}
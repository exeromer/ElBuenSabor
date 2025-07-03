package com.powerRanger.ElBuenSabor.services;

import com.powerRanger.ElBuenSabor.dtos.StockInsumoSucursalRequestDTO;
import com.powerRanger.ElBuenSabor.dtos.StockInsumoSucursalResponseDTO;
import com.powerRanger.ElBuenSabor.entities.ArticuloInsumo;
import com.powerRanger.ElBuenSabor.entities.StockInsumoSucursal;
import com.powerRanger.ElBuenSabor.entities.Sucursal;
import com.powerRanger.ElBuenSabor.repository.ArticuloInsumoRepository;
import com.powerRanger.ElBuenSabor.repository.StockInsumoSucursalRepository;
import com.powerRanger.ElBuenSabor.repository.SucursalRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import jakarta.validation.Valid;
import org.springframework.validation.annotation.Validated;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@Validated
public class StockInsumoSucursalServiceImpl implements StockInsumoSucursalService {

    @Autowired private StockInsumoSucursalRepository stockInsumoSucursalRepository;
    @Autowired private ArticuloInsumoRepository articuloInsumoRepository;
    @Autowired private SucursalRepository sucursalRepository;

    private static final Logger logger = LoggerFactory.getLogger(StockInsumoSucursalServiceImpl.class);

    private StockInsumoSucursalResponseDTO convertToResponseDto(StockInsumoSucursal stock) {
        if (stock == null) return null;
        StockInsumoSucursalResponseDTO dto = new StockInsumoSucursalResponseDTO();
        dto.setId(stock.getId());
        dto.setStockActual(stock.getStockActual());
        dto.setStockMinimo(stock.getStockMinimo());
        if (stock.getArticuloInsumo() != null) {
            dto.setArticuloInsumoId(stock.getArticuloInsumo().getId());
            dto.setArticuloInsumoDenominacion(stock.getArticuloInsumo().getDenominacion());
        }
        if (stock.getSucursal() != null) {
            dto.setSucursalId(stock.getSucursal().getId());
            dto.setSucursalNombre(stock.getSucursal().getNombre());
        }
        return dto;
    }

    private void mapRequestDtoToEntity(StockInsumoSucursalRequestDTO dto, StockInsumoSucursal stock) throws Exception {
        ArticuloInsumo insumo = articuloInsumoRepository.findById(dto.getArticuloInsumoId())
                .orElseThrow(() -> new Exception("Artículo Insumo no encontrado con ID: " + dto.getArticuloInsumoId()));
        Sucursal sucursal = sucursalRepository.findById(dto.getSucursalId())
                .orElseThrow(() -> new Exception("Sucursal no encontrada con ID: " + dto.getSucursalId()));

        stock.setArticuloInsumo(insumo);
        stock.setSucursal(sucursal);
        stock.setStockActual(dto.getStockActual());
        stock.setStockMinimo(dto.getStockMinimo());
    }

    @Override
    @Transactional(readOnly = true)
    public List<StockInsumoSucursalResponseDTO> getAllStockInsumoSucursal() {
        return stockInsumoSucursalRepository.findAll().stream()
                .map(this::convertToResponseDto)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public StockInsumoSucursalResponseDTO getStockInsumoSucursalById(Integer id) throws Exception {
        StockInsumoSucursal stock = stockInsumoSucursalRepository.findById(id)
                .orElseThrow(() -> new Exception("StockInsumoSucursal no encontrado con ID: " + id));
        return convertToResponseDto(stock);
    }

    @Override
    @Transactional
    public StockInsumoSucursalResponseDTO createStockInsumoSucursal(@Valid StockInsumoSucursalRequestDTO dto) throws Exception {
        Optional<StockInsumoSucursal> existingStock = stockInsumoSucursalRepository.findByArticuloInsumoAndSucursal(
                articuloInsumoRepository.findById(dto.getArticuloInsumoId()).orElseThrow(() -> new Exception("Artículo Insumo no encontrado")),
                sucursalRepository.findById(dto.getSucursalId()).orElseThrow(() -> new Exception("Sucursal no encontrada"))
        );
        if (existingStock.isPresent()) {
            throw new Exception("Ya existe un registro de stock para el Artículo Insumo " + dto.getArticuloInsumoId() + " en la Sucursal " + dto.getSucursalId());
        }

        StockInsumoSucursal stock = new StockInsumoSucursal();
        mapRequestDtoToEntity(dto, stock);
        StockInsumoSucursal savedStock = stockInsumoSucursalRepository.save(stock);
        return convertToResponseDto(savedStock);
    }

    @Override
    @Transactional
    public StockInsumoSucursalResponseDTO updateStockInsumoSucursal(Integer id, @Valid StockInsumoSucursalRequestDTO dto) throws Exception {
        StockInsumoSucursal existingStock = stockInsumoSucursalRepository.findById(id)
                .orElseThrow(() -> new Exception("StockInsumoSucursal no encontrado con ID: " + id));

        // Validar que no se intente cambiar el insumo o la sucursal de un registro existente
        if (!existingStock.getArticuloInsumo().getId().equals(dto.getArticuloInsumoId()) ||
                !existingStock.getSucursal().getId().equals(dto.getSucursalId())) {
            throw new Exception("No se puede cambiar el Artículo Insumo o la Sucursal de un registro de stock existente.");
        }

        mapRequestDtoToEntity(dto, existingStock); // Esto actualizará stockActual y stockMinimo
        StockInsumoSucursal updatedStock = stockInsumoSucursalRepository.save(existingStock);
        return convertToResponseDto(updatedStock);
    }

    @Override
    @Transactional
    public void deleteStockInsumoSucursal(Integer id) throws Exception {
        if (!stockInsumoSucursalRepository.existsById(id)) {
            throw new Exception("StockInsumoSucursal no encontrado con ID: " + id + " para eliminar.");
        }
        stockInsumoSucursalRepository.deleteById(id);
    }

    @Override
    @Transactional(readOnly = true)
    public StockInsumoSucursalResponseDTO getStockByInsumoAndSucursal(Integer insumoId, Integer sucursalId) throws Exception {
        ArticuloInsumo insumo = articuloInsumoRepository.findById(insumoId)
                .orElseThrow(() -> new Exception("Artículo Insumo no encontrado con ID: " + insumoId));
        Sucursal sucursal = sucursalRepository.findById(sucursalId)
                .orElseThrow(() -> new Exception("Sucursal no encontrada con ID: " + sucursalId));

        StockInsumoSucursal stock = stockInsumoSucursalRepository.findByArticuloInsumoAndSucursal(insumo, sucursal)
                .orElseThrow(() -> new Exception("Stock para insumo " + insumoId + " en sucursal " + sucursalId + " no encontrado."));
        return convertToResponseDto(stock);
    }

    @Override
    @Transactional
    public void reduceStock(Integer insumoId, Integer sucursalId, Double cantidad) throws Exception {
        StockInsumoSucursal stock = stockInsumoSucursalRepository.findByArticuloInsumoAndSucursal(
                articuloInsumoRepository.findById(insumoId).orElseThrow(() -> new Exception("Insumo no encontrado para reducir stock")),
                sucursalRepository.findById(sucursalId).orElseThrow(() -> new Exception("Sucursal no encontrada para reducir stock"))
        ).orElseThrow(() -> new Exception("Stock de insumo " + insumoId + " en sucursal " + sucursalId + " no encontrado."));

        if (stock.getStockActual() == null || stock.getStockActual() < cantidad) {
            throw new Exception("Stock insuficiente para insumo ID " + insumoId + " en sucursal ID " + sucursalId + ". Solicitado: " + cantidad + ", Disponible: " + (stock.getStockActual() != null ? stock.getStockActual() : 0.0));
        }
        stock.setStockActual(stock.getStockActual() - cantidad);
        stockInsumoSucursalRepository.save(stock);
    }

    @Override
    @Transactional
    public void addStock(Integer insumoId, Integer sucursalId, Double cantidad) throws Exception {
        if (cantidad <= 0) {
            logger.warn("Se intentó añadir una cantidad no positiva ({}) de stock para el insumo ID {}", cantidad, insumoId);
            return;
        }

        ArticuloInsumo insumo = articuloInsumoRepository.findById(insumoId)
                .orElseThrow(() -> new Exception("Insumo no encontrado para añadir stock con ID: " + insumoId));
        Sucursal sucursal = sucursalRepository.findById(sucursalId)
                .orElseThrow(() -> new Exception("Sucursal no encontrada para añadir stock con ID: " + sucursalId));

        StockInsumoSucursal stock = stockInsumoSucursalRepository.findByArticuloInsumoAndSucursal(insumo, sucursal)
                .orElseThrow(() -> new Exception("Registro de stock para insumo " + insumoId + " en sucursal " + sucursalId + " no encontrado."));

        if (stock.getStockActual() == null) {
            stock.setStockActual(0.0);
        }
        stock.setStockActual(stock.getStockActual() + cantidad);
        stockInsumoSucursalRepository.save(stock);
        logger.info("Stock para Insumo ID {} en Sucursal ID {} incrementado en {}. Nuevo stock: {}", insumoId, sucursalId, cantidad, stock.getStockActual());
    }

}
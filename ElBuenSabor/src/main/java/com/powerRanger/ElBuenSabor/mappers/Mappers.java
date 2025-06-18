package com.powerRanger.ElBuenSabor.mappers;

import com.powerRanger.ElBuenSabor.dtos.*;
import com.powerRanger.ElBuenSabor.entities.*;
import org.springframework.stereotype.Component;
import java.util.stream.Collectors;

@Component
public class Mappers {

    public UnidadMedidaResponseDTO convertUnidadMedidaToDto(UnidadMedida um) {
        if (um == null) return null;
        UnidadMedidaResponseDTO dto = new UnidadMedidaResponseDTO();
        dto.setId(um.getId());
        dto.setDenominacion(um.getDenominacion());
        return dto;
    }

    public CategoriaResponseDTO convertCategoriaToDto(Categoria cat) {
        if (cat == null) return null;
        CategoriaResponseDTO dto = new CategoriaResponseDTO();
        dto.setId(cat.getId());
        dto.setDenominacion(cat.getDenominacion());
        dto.setEstadoActivo(cat.getEstadoActivo());
        return dto;
    }

    // Renombrado de convertImagenToDto a convertImagenToResponseDto para consistencia
    public ImagenResponseDTO convertImagenToResponseDto(Imagen img) {
        if (img == null) return null;
        ImagenResponseDTO dto = new ImagenResponseDTO();
        dto.setId(img.getId());
        dto.setDenominacion(img.getDenominacion());
        dto.setEstadoActivo(img.getEstadoActivo());
        if (img.getArticulo() != null) dto.setArticuloId(img.getArticulo().getId());
        if (img.getPromocion() != null) dto.setPromocionId(img.getPromocion().getId());
        return dto;
    }

    public ArticuloSimpleResponseDTO convertArticuloToSimpleDto(Articulo articulo) {
        if (articulo == null) return null;
        ArticuloSimpleResponseDTO dto = new ArticuloSimpleResponseDTO();
        dto.setId(articulo.getId());
        dto.setDenominacion(articulo.getDenominacion());
        dto.setPrecioVenta(articulo.getPrecioVenta());
        return dto;
    }

    public ArticuloManufacturadoDetalleResponseDTO convertAmdToDto(ArticuloManufacturadoDetalle amd) {
        if (amd == null) return null;
        ArticuloManufacturadoDetalleResponseDTO dto = new ArticuloManufacturadoDetalleResponseDTO();
        dto.setId(amd.getId());
        dto.setCantidad(amd.getCantidad());
        dto.setEstadoActivo(amd.getEstadoActivo());
        if (amd.getArticuloInsumo() != null) {
            dto.setArticuloInsumo(convertArticuloToSimpleDto(amd.getArticuloInsumo()));
        }
        return dto;
    }

    // Mapper principal para Articulo -> ArticuloBaseResponseDTO (con polimorfismo)
    public ArticuloBaseResponseDTO convertArticuloToResponseDto(Articulo articulo) {
        if (articulo == null) return null;
        ArticuloBaseResponseDTO baseDto;

        if (articulo instanceof ArticuloInsumo) {
            ArticuloInsumo insumo = (ArticuloInsumo) articulo;
            ArticuloInsumoResponseDTO dto = new ArticuloInsumoResponseDTO();
            dto.setPrecioCompra(insumo.getPrecioCompra());
            dto.setEsParaElaborar(insumo.getEsParaElaborar());
            baseDto = dto;
        } else if (articulo instanceof ArticuloManufacturado) {
            ArticuloManufacturado manufacturado = (ArticuloManufacturado) articulo;
            ArticuloManufacturadoResponseDTO dto = new ArticuloManufacturadoResponseDTO();
            dto.setDescripcion(manufacturado.getDescripcion());
            dto.setTiempoEstimadoMinutos(manufacturado.getTiempoEstimadoMinutos());
            dto.setPreparacion(manufacturado.getPreparacion());
            if (manufacturado.getManufacturadoDetalles() != null) {
                dto.setManufacturadoDetalles(
                        manufacturado.getManufacturadoDetalles().stream()
                                .map(this::convertAmdToDto)
                                .collect(Collectors.toList())
                );
            }
            baseDto = dto;
        } else {
            throw new IllegalStateException("Tipo de Artículo desconocido: " + articulo.getClass().getName());
        }

        baseDto.setId(articulo.getId());
        baseDto.setDenominacion(articulo.getDenominacion());
        baseDto.setPrecioVenta(articulo.getPrecioVenta());
        baseDto.setEstadoActivo(articulo.getEstadoActivo());

        if (articulo.getUnidadMedida() != null) {
            baseDto.setUnidadMedida(convertUnidadMedidaToDto(articulo.getUnidadMedida()));
        }
        if (articulo.getCategoria() != null) {
            baseDto.setCategoria(convertCategoriaToDto(articulo.getCategoria()));
        }
        if (articulo.getImagenes() != null) {
            baseDto.setImagenes(articulo.getImagenes().stream()
                    .map(this::convertImagenToResponseDto) // Usar el nombre corregido
                    .collect(Collectors.toList()));
        }
        return baseDto;
    }

    /**
     * Convierte una entidad StockInsumoSucursal a su DTO de respuesta.
     * @param stock entidad StockInsumoSucursal.
     * @return DTO de respuesta StockInsumoSucursalResponseDTO.
     */
    public StockInsumoSucursalResponseDTO convertStockInsumoSucursalToDto(StockInsumoSucursal stock) {
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

    public SucursalSimpleResponseDTO convertSucursalToSimpleDto(Sucursal sucursal) {
        if (sucursal == null) return null;
        SucursalSimpleResponseDTO dto = new SucursalSimpleResponseDTO();
        dto.setId(sucursal.getId());
        dto.setNombre(sucursal.getNombre());
        return dto;
    }
}
package com.powerRanger.ElBuenSabor.mappers; // O el paquete que elijas

import com.powerRanger.ElBuenSabor.dtos.*;
import com.powerRanger.ElBuenSabor.entities.*;
import org.springframework.stereotype.Component; // Si quieres inyectarlo
import java.util.stream.Collectors;

@Component // Para poder inyectarlo si decides hacerlo así
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

    public ImagenResponseDTO convertImagenToDto(Imagen img) {
        // Este es un ejemplo, asumiendo que ImagenResponseDTO tiene estos campos.
        // Ajusta según tu ImagenResponseDTO real.
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
        dto.setEstadoActivo(amd.getEstadoActivo()); // Asumiendo que Detalle tiene estadoActivo
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
            dto.setStockActual(insumo.getStockActual());
            dto.setstockMinimo(insumo.getstockMinimo());
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
                                .map(this::convertAmdToDto) // Llama al mapper de detalle
                                .collect(Collectors.toList())
                );
            }
            baseDto = dto;
        } else {
            // Este caso es si tienes un Articulo que no es ni Insumo ni Manufacturado.
            // Si no es posible, puedes lanzar una excepción o manejarlo diferente.
            // Por ahora, creamos un ArticuloBaseResponseDTO genérico.
            // Pero como ArticuloBaseResponseDTO es abstracta, esto no compilará.
            // Debes decidir: o ArticuloBaseResponseDTO no es abstracta, o todo Articulo es Insumo/Manufacturado.
            // Asumamos que todo artículo será Insumo o Manufacturado. Si no, debes crear un ArticuloResponseDTO concreto.
            throw new IllegalStateException("Tipo de Artículo desconocido: " + articulo.getClass().getName());
        }

        // Poblar campos comunes de ArticuloBaseResponseDTO
        baseDto.setId(articulo.getId());
        baseDto.setDenominacion(articulo.getDenominacion());
        baseDto.setPrecioVenta(articulo.getPrecioVenta());
        baseDto.setEstadoActivo(articulo.getEstadoActivo());
        // baseDto.setFechaBaja(articulo.getFechaBaja()); // Si lo tienes

        if (articulo.getUnidadMedida() != null) {
            baseDto.setUnidadMedida(convertUnidadMedidaToDto(articulo.getUnidadMedida()));
        }
        if (articulo.getCategoria() != null) {
            baseDto.setCategoria(convertCategoriaToDto(articulo.getCategoria()));
        }
        if (articulo.getImagenes() != null) {
            baseDto.setImagenes(articulo.getImagenes().stream()
                    .map(this::convertImagenToDto) // Reutiliza el mapper de Imagen
                    .collect(Collectors.toList()));
        }
        return baseDto;
    }
}
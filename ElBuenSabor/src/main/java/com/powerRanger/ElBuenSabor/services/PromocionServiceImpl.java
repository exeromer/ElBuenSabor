package com.powerRanger.ElBuenSabor.services;

import com.powerRanger.ElBuenSabor.dtos.PromocionRequestDTO;
import com.powerRanger.ElBuenSabor.dtos.PromocionResponseDTO;
import com.powerRanger.ElBuenSabor.dtos.SucursalSimpleResponseDTO;
import com.powerRanger.ElBuenSabor.dtos.PromocionDetalleResponseDTO; // ¡Importar!
import com.powerRanger.ElBuenSabor.dtos.ImagenResponseDTO; // ¡Importar!
import com.powerRanger.ElBuenSabor.dtos.PromocionDetalleRequestDTO; // ¡Importar!

import com.powerRanger.ElBuenSabor.entities.*;
import com.powerRanger.ElBuenSabor.exceptions.InvalidOperationException;
import com.powerRanger.ElBuenSabor.mappers.Mappers;
import com.powerRanger.ElBuenSabor.repository.ArticuloRepository;
import com.powerRanger.ElBuenSabor.repository.ImagenRepository;
import com.powerRanger.ElBuenSabor.repository.PromocionRepository;
import com.powerRanger.ElBuenSabor.repository.SucursalRepository;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.validation.annotation.Validated;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set; // Asegurarse de importar Set
import java.util.Objects;
import java.util.stream.Collectors;

@Service
@Validated
public class PromocionServiceImpl implements PromocionService {

    @Autowired private PromocionRepository promocionRepository;
    @Autowired private ImagenRepository imagenRepository;
    @Autowired private ArticuloRepository articuloRepository;
    @Autowired private SucursalRepository sucursalRepository;
    @Autowired private Mappers mappers;

    // --- MAPPERS INTERNOS ---
    // Estos métodos convert*ToDto deben reflejar lo que Mappers.java haría, o ser eliminados si mappers.* ya es suficiente.
    // Asumo que estos métodos internos son necesarios para la lógica del service o para adaptar el output.
    // Los he corregido para que referencien los DTOs correctamente.

    // Este método ya no es necesario si Mappers.java ya tiene el método para convertir PromocionDetalle a su DTO de respuesta.
    // Si mappers.convertPromocionDetalleToResponseDto(detalle) existe y hace lo mismo, elimínalo.
    // Lo mantengo por ahora con correcciones de tipo.
    private PromocionDetalleResponseDTO convertPromocionDetalleToDto(PromocionDetalle detalle) {
        if (detalle == null) return null;
        PromocionDetalleResponseDTO dto = new PromocionDetalleResponseDTO();
        dto.setId(detalle.getId());
        dto.setCantidad(detalle.getCantidad());
        if (detalle.getArticulo() != null) {
            dto.setArticulo(mappers.convertArticuloToSimpleDto(detalle.getArticulo()));
        }
        return dto;
    }

    // Este método ya no es necesario si Mappers.java ya tiene el método para convertir Imagen a su DTO de respuesta.
    // Si mappers.convertImagenToResponseDto(imagen) existe y hace lo mismo, elimínalo.
    // Lo mantengo por ahora con correcciones de tipo.
    private ImagenResponseDTO convertImagenToDto(Imagen imagen) {
        return mappers.convertImagenToResponseDto(imagen); // Asumiendo que Mappers.java tiene este método
    }

    private PromocionResponseDTO convertToResponseDto(Promocion promocion) {
        if (promocion == null) return null;
        PromocionResponseDTO dto = new PromocionResponseDTO();
        dto.setId(promocion.getId());
        dto.setDenominacion(promocion.getDenominacion());
        dto.setFechaDesde(promocion.getFechaDesde());
        dto.setFechaHasta(promocion.getFechaHasta());
        dto.setHoraDesde(promocion.getHoraDesde());
        dto.setHoraHasta(promocion.getHoraHasta());
        dto.setDescripcionDescuento(promocion.getDescripcionDescuento());
        dto.setPrecioPromocional(promocion.getPrecioPromocional());
        dto.setPorcentajeDescuento(promocion.getPorcentajeDescuento()); // Agregado
        dto.setTipoPromocion(promocion.getTipoPromocion());             // Agregado
        dto.setEstadoActivo(promocion.getEstadoActivo());

        if (promocion.getImagenes() != null) {
            dto.setImagenes(promocion.getImagenes().stream()
                    .map(this::convertImagenToDto) // Uso el método interno (o mappers::convertImagenToResponseDto)
                    .collect(Collectors.toList()));
        }
        if (promocion.getDetallesPromocion() != null) {
            dto.setDetallesPromocion(promocion.getDetallesPromocion().stream()
                    .map(this::convertPromocionDetalleToDto) // Uso el método interno (o mappers::convertPromocionDetalleToResponseDto)
                    .collect(Collectors.toList()));
        }
        if (promocion.getSucursales() != null) {
            dto.setSucursales(promocion.getSucursales().stream()
                    .map(mappers::convertSucursalToSimpleDto)
                    .collect(Collectors.toList()));
        }
        return dto;
    }

    private void mapRequestDtoToEntity(PromocionRequestDTO dto, Promocion promocion) throws Exception {
        promocion.setDenominacion(dto.getDenominacion());
        promocion.setFechaDesde(dto.getFechaDesde());
        promocion.setFechaHasta(dto.getFechaHasta());
        promocion.setHoraDesde(dto.getHoraDesde());
        promocion.setHoraHasta(dto.getHoraHasta());
        promocion.setDescripcionDescuento(dto.getDescripcionDescuento());
        promocion.setPrecioPromocional(dto.getPrecioPromocional());
        promocion.setPorcentajeDescuento(dto.getPorcentajeDescuento()); // Asignar porcentajeDescuento
        promocion.setTipoPromocion(dto.getTipoPromocion());             // Asignar tipoPromocion
        promocion.setEstadoActivo(dto.getEstadoActivo() != null ? dto.getEstadoActivo() : true);

        // Sincronizar Imágenes
        if (promocion.getImagenes() == null) promocion.setImagenes(new ArrayList<>());
        promocion.getImagenes().clear();
        if (dto.getImagenIds() != null) {
            for (Integer imagenId : new HashSet<>(dto.getImagenIds())) {
                Imagen imagen = imagenRepository.findById(imagenId)
                        .orElseThrow(() -> new InvalidOperationException("Imagen no encontrada con ID: " + imagenId));
                promocion.addImagen(imagen);
            }
        }

        // Sincronizar Detalles de Promoción - ¡CORREGIDO! Usar HashSet para la entidad
        if (promocion.getDetallesPromocion() == null) promocion.setDetallesPromocion(new HashSet<>()); // ¡CORREGIDO! De ArrayList a HashSet
        promocion.getDetallesPromocion().clear();
        if (dto.getDetallesPromocion() != null && !dto.getDetallesPromocion().isEmpty()) {
            for (PromocionDetalleRequestDTO detalleDto : dto.getDetallesPromocion()) { // CORREGIDO: PromocionDetalleRequestDTO
                Articulo articulo = articuloRepository.findById(detalleDto.getArticuloId()) // CORREGIDO: getArticuloId()
                        .orElseThrow(() -> new InvalidOperationException("Artículo no encontrado con ID: " + detalleDto.getArticuloId()));

                if (Boolean.FALSE.equals(articulo.getEstadoActivo())) {
                    throw new InvalidOperationException("El artículo '" + articulo.getDenominacion() + "' (ID: " + articulo.getId() + ") no está activo y no puede ser parte de una promoción.");
                }
                if (articulo instanceof ArticuloInsumo) {
                    ArticuloInsumo insumo = (ArticuloInsumo) articulo;
                    if (Boolean.TRUE.equals(insumo.getEsParaElaborar())) {
                        throw new InvalidOperationException("No se puede añadir un ArticuloInsumo 'para elaborar' directamente a un detalle de promoción. Sólo artículos terminados o insumos no elaborables. ID: " + insumo.getId() + ", Denominación: " + insumo.getDenominacion());
                    }
                }
                PromocionDetalle nuevoDetalle = new PromocionDetalle();
                nuevoDetalle.setArticulo(articulo);
                nuevoDetalle.setCantidad(detalleDto.getCantidad()); // CORREGIDO: getCantidad()
                promocion.addDetallePromocion(nuevoDetalle);
            }
        } else {
            throw new InvalidOperationException("Una promoción debe tener al menos un detalle de promoción.");
        }
    }

    private void sincronizarSucursalesPromocion(Promocion promocion, List<Integer> sucursalIds) throws Exception {
        Set<Sucursal> sucursalesActualesDePromocion = new HashSet<>(promocion.getSucursales());

        // Eliminar las asociaciones antiguas que ya no están en sucursalIds
        for (Sucursal sucActual : sucursalesActualesDePromocion) {
            if (!sucursalIds.contains(sucActual.getId())) {
                promocion.removeSucursal(sucActual);
                // No es necesario llamar sucActual.removePromocion(promocion) si removeSucursal en Promocion
                // ya maneja la bidireccionalidad. Revisa ese método. Si sí lo hace, esta línea sería redundante.
                // Si la bidireccionalidad no se maneja en la entidad, entonces sí se necesita aquí.
                // Suponiendo que Promocion::removeSucursal ya es bidireccional:
                // sucursalRepository.save(sucActual); // Solo si el cambio en Sucursal necesita ser persistido explícitamente
            }
        }

        // Añadir nuevas asociaciones
        for (Integer sucursalId : new HashSet<>(sucursalIds)) {
            Sucursal sucursal = sucursalRepository.findById(sucursalId)
                    .orElseThrow(() -> new InvalidOperationException("Sucursal no encontrada con ID: " + sucursalId));
            if (!promocion.getSucursales().contains(sucursal)) {
                promocion.addSucursal(sucursal);
                // sucursalRepository.save(sucursal); // Solo si el cambio en Sucursal necesita ser persistido explícitamente
            }
        }
    }

    private void validarFechasYEstadoActivo(Promocion promocion) throws InvalidOperationException {
        LocalDate hoy = LocalDate.now();
        LocalTime ahora = LocalTime.now();

        if (promocion.getFechaDesde().isAfter(promocion.getFechaHasta())) {
            throw new InvalidOperationException("La fecha desde no puede ser posterior a la fecha hasta.");
        }
        if (promocion.getFechaDesde().isEqual(promocion.getFechaHasta()) && promocion.getHoraDesde().isAfter(promocion.getHoraHasta())) {
            throw new InvalidOperationException("La hora desde no puede ser posterior a la hora hasta si la fecha es la misma.");
        }

        boolean estaEnRangoActual = !hoy.isBefore(promocion.getFechaDesde()) && !hoy.isAfter(promocion.getFechaHasta());
        if (estaEnRangoActual && (hoy.isEqual(promocion.getFechaDesde()) || hoy.isEqual(promocion.getFechaHasta()))) {
            estaEnRangoActual = !ahora.isBefore(promocion.getHoraDesde()) && !ahora.isAfter(promocion.getHoraHasta());
        }

        if (Boolean.TRUE.equals(promocion.getEstadoActivo())) {
            if (!estaEnRangoActual) {
                throw new InvalidOperationException("La promoción no puede estar activa si no se encuentra dentro del rango de fechas y horas actual.");
            }
        }
    }


    @Override
    @Transactional(readOnly = true)
    public List<PromocionResponseDTO> getAll() {
        return promocionRepository.findAll().stream()
                .map(promocion -> {
                    // Forzar la carga de la colección LAZY de sucursales para el DTO
                    promocion.getSucursales().size();
                    promocion.getDetallesPromocion().size(); // También cargar detalles si se necesitan en el response DTO
                    promocion.getImagenes().size(); // También cargar imágenes
                    return this.convertToResponseDto(promocion);
                })
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public PromocionResponseDTO getById(Integer id) throws Exception {
        Promocion promocion = promocionRepository.findById(id)
                .orElseThrow(() -> new InvalidOperationException("Promoción no encontrada con ID: " + id));
        // Forzar la carga de la colección LAZY de sucursales, detalles e imágenes para el DTO
        promocion.getSucursales().size();
        promocion.getDetallesPromocion().size();
        promocion.getImagenes().size();
        return convertToResponseDto(promocion);
    }

    @Override
    @Transactional
    public PromocionResponseDTO create(@Valid PromocionRequestDTO dto) throws Exception {
        Promocion promocion = new Promocion();
        mapRequestDtoToEntity(dto, promocion);

        validarFechasYEstadoActivo(promocion);

        if (dto.getSucursalIds() == null || dto.getSucursalIds().isEmpty()) {
            throw new InvalidOperationException("Una promoción debe estar asociada al menos a una sucursal.");
        }

        // Antes de guardar, establecer explícitamente las sucursales para que JPA las gestione en el save.
        Set<Sucursal> sucursalesParaPromocion = new HashSet<>();
        for (Integer sucursalId : dto.getSucursalIds()) {
            Sucursal sucursal = sucursalRepository.findById(sucursalId)
                    .orElseThrow(() -> new InvalidOperationException("Sucursal no encontrada con ID: " + sucursalId));
            sucursalesParaPromocion.add(sucursal);
        }
        promocion.setSucursales(sucursalesParaPromocion); // Establecer la colección de sucursales en la entidad

        Promocion promocionGuardada = promocionRepository.save(promocion);

        // La bidireccionalidad (Sucursal -> Promocion) se debe manejar aquí o en el add/remove de la entidad Sucursal.
        // Si el save de Promocion ya maneja la relación en la tabla intermedia, no es necesario hacer un save extra en Sucursal.
        // Sin embargo, para mantener la consistencia bidireccional en memoria, se puede iterar y añadir la promo a la sucursal.
        // Opcional: si la relación es ManyToMany y JPA la gestiona bien, esto puede ser redundante si no hay otros cambios.
        for (Sucursal sucursal : promocionGuardada.getSucursales()) {
            if (!sucursal.getPromociones().contains(promocionGuardada)) {
                sucursal.getPromociones().add(promocionGuardada);
                // sucursalRepository.save(sucursal); // Podría ser necesario si no hay cascade ALL/MERGE adecuado
            }
        }


        // Recargar la promoción para que la colección de sucursales esté actualizada en el contexto de la entidad
        // Esto es esencial si las relaciones son LAZY y las modificaciones de bidireccionalidad no "refrescan" automáticamente
        promocionGuardada = promocionRepository.findById(promocionGuardada.getId())
                .orElseThrow(() -> new InvalidOperationException("Error al recargar la promoción después de guardar."));

        return convertToResponseDto(promocionGuardada);
    }

    @Override
    @Transactional
    public PromocionResponseDTO update(Integer id, @Valid PromocionRequestDTO dto) throws Exception {
        Promocion promocionExistente = promocionRepository.findById(id)
                .orElseThrow(() -> new InvalidOperationException("Promoción no encontrada con ID: " + id));

        mapRequestDtoToEntity(dto, promocionExistente);
        validarFechasYEstadoActivo(promocionExistente);

        if (dto.getSucursalIds() == null || dto.getSucursalIds().isEmpty()) {
            throw new InvalidOperationException("Una promoción debe estar asociada al menos a una sucursal.");
        }

        // Sincronizar las sucursales para el update
        // Obtener las sucursales existentes y las nuevas del DTO
        Set<Sucursal> nuevasSucursales = new HashSet<>();
        for (Integer sucursalId : dto.getSucursalIds()) {
            Sucursal sucursal = sucursalRepository.findById(sucursalId)
                    .orElseThrow(() -> new InvalidOperationException("Sucursal no encontrada con ID: " + sucursalId));
            nuevasSucursales.add(sucursal);
        }

        // Eliminar sucursales que ya no están en el DTO
        Set<Sucursal> sucursalesParaEliminar = new HashSet<>(promocionExistente.getSucursales());
        sucursalesParaEliminar.removeAll(nuevasSucursales);
        for (Sucursal sucursal : sucursalesParaEliminar) {
            promocionExistente.removeSucursal(sucursal); // Esto debería manejar la bidireccionalidad
        }

        // Añadir nuevas sucursales que no estaban antes
        Set<Sucursal> sucursalesParaAnadir = new HashSet<>(nuevasSucursales);
        sucursalesParaAnadir.removeAll(promocionExistente.getSucursales()); // Remueve las que ya están
        for (Sucursal sucursal : sucursalesParaAnadir) {
            promocionExistente.addSucursal(sucursal); // Esto debería manejar la bidireccionalidad
        }


        Promocion promocionActualizada = promocionRepository.save(promocionExistente);

        // Recargar para asegurar que la colección de sucursales esté actualizada después de las manipulaciones
        promocionActualizada = promocionRepository.findById(promocionActualizada.getId())
                .orElseThrow(() -> new InvalidOperationException("Error al recargar la promoción después de actualizar."));

        return convertToResponseDto(promocionActualizada);
    }

    @Override
    @Transactional
    public void softDelete(Integer id) throws Exception {
        Promocion promocion = promocionRepository.findById(id)
                .orElseThrow(() -> new InvalidOperationException("Promoción no encontrada con ID: " + id));
        promocion.setEstadoActivo(false);
        // Si Promocion tuviera fechaBaja, se setearía aquí.
        promocionRepository.save(promocion);
    }

    @Override
    @Transactional(readOnly = true)
    public List<PromocionResponseDTO> getPromocionesActivasPorSucursal(Integer sucursalId, LocalDate fecha, LocalTime hora) throws Exception {
        List<Promocion> promociones = promocionRepository.findPromocionesActivasPorSucursalYFechaHora(
                sucursalId, fecha, hora);

        return promociones.stream()
                .map(p -> {
                    // Cargar explícitamente las colecciones LAZY para evitar LazyInitializationException
                    p.getSucursales().size();
                    p.getDetallesPromocion().size();
                    p.getImagenes().size();
                    return convertToResponseDto(p);
                })
                .collect(Collectors.toList());
    }
}
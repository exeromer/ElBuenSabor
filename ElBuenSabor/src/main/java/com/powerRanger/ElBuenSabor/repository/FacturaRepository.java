package com.powerRanger.ElBuenSabor.repository;

import com.powerRanger.ElBuenSabor.entities.Factura;
import com.powerRanger.ElBuenSabor.entities.enums.EstadoFactura;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.Optional;

@Repository
public interface FacturaRepository extends BaseRepository<Factura, Integer> { // Cambiado a BaseRepository
    List<Factura> findByEstadoFactura(EstadoFactura estadoFactura);
    Optional<Factura> findByIdAndEstadoFactura(Integer id, EstadoFactura estadoFactura);
    Optional<Factura> findByPedidoId(Integer pedidoId);
}
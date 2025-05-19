package com.powerRanger.ElBuenSabor.repository;

import com.powerRanger.ElBuenSabor.entities.Factura;
import com.powerRanger.ElBuenSabor.entities.enums.EstadoFactura; // Importar Enum
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface FacturaRepository extends JpaRepository<Factura, Integer> {

    // Encontrar todas las facturas por estado
    List<Factura> findByEstadoFactura(EstadoFactura estadoFactura);

    // Encontrar una factura por ID y estado
    Optional<Factura> findByIdAndEstadoFactura(Integer id, EstadoFactura estadoFactura);

    // Podrías añadir más métodos específicos si los necesitas, ej:
    // List<Factura> findByFechaFacturacionBetweenAndEstadoFactura(LocalDate start, LocalDate end, EstadoFactura estado);
}
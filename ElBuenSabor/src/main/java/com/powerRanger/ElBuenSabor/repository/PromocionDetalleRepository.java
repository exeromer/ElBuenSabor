package com.powerRanger.ElBuenSabor.repository;

import com.powerRanger.ElBuenSabor.entities.PromocionDetalle;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface PromocionDetalleRepository extends JpaRepository<PromocionDetalle, Integer> {
    // Añadir métodos de consulta personalizados aquí
}
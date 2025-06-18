package com.powerRanger.ElBuenSabor.repository;

import com.powerRanger.ElBuenSabor.entities.Promocion;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

@Repository
public interface PromocionRepository extends JpaRepository<Promocion, Integer> {

    @Query("SELECT p FROM Promocion p JOIN p.sucursales s " +
            "WHERE p.estadoActivo = true " +
            "AND s.id = :sucursalId " +
            "AND :fechaActual BETWEEN p.fechaDesde AND p.fechaHasta " +
            "AND :horaActual BETWEEN p.horaDesde AND p.horaHasta")
    List<Promocion> findPromocionesActivasPorSucursalYFechaHora(
            @Param("sucursalId") Integer sucursalId,
            @Param("fechaActual") LocalDate fechaActual,
            @Param("horaActual") LocalTime horaActual);
}
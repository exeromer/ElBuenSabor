package com.powerRanger.ElBuenSabor.repository;

import com.powerRanger.ElBuenSabor.entities.ArticuloManufacturado;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
// import org.springframework.stereotype.Repository; // Opcional

public interface ArticuloManufacturadoRepository extends JpaRepository<ArticuloManufacturado, Integer> {

    @Query("SELECT am FROM ArticuloManufacturado am WHERE " +
            "LOWER(am.denominacion) LIKE LOWER(CONCAT('%', :searchTerm, '%')) " +
            "AND (:estadoActivoParam IS NULL OR am.estadoActivo = :estadoActivoParam)")
    List<ArticuloManufacturado> searchByDenominacionWithOptionalStatus(
            @Param("searchTerm") String searchTerm,
            @Param("estadoActivoParam") Boolean estadoActivoParam
    );

    @Query("SELECT am FROM ArticuloManufacturado am WHERE (:estadoActivoParam IS NULL OR am.estadoActivo = :estadoActivoParam)")
    List<ArticuloManufacturado> findAllWithOptionalStatus(@Param("estadoActivoParam") Boolean estadoActivoParam);

    // Mantén este si es usado en otras partes, o puedes reemplazar su uso por findAllWithOptionalStatus(true)
    List<ArticuloManufacturado> findByEstadoActivoTrue();
}
package com.powerRanger.ElBuenSabor.repository;

import com.powerRanger.ElBuenSabor.entities.ArticuloManufacturado;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface ArticuloManufacturadoRepository extends BaseRepository<ArticuloManufacturado, Integer> { // Cambiado a BaseRepository

    @Query("SELECT am FROM ArticuloManufacturado am WHERE " +
            "LOWER(am.denominacion) LIKE LOWER(CONCAT('%', :searchTerm, '%')) " +
            "AND (:estadoActivoParam IS NULL OR am.estadoActivo = :estadoActivoParam)")
    List<ArticuloManufacturado> searchByDenominacionWithOptionalStatus(
            @Param("searchTerm") String searchTerm,
            @Param("estadoActivoParam") Boolean estadoActivoParam
    );

    @Query("SELECT am FROM ArticuloManufacturado am WHERE (:estadoActivoParam IS NULL OR am.estadoActivo = :estadoActivoParam)")
    List<ArticuloManufacturado> findAllWithOptionalStatus(@Param("estadoActivoParam") Boolean estadoActivoParam);

    List<ArticuloManufacturado> findByEstadoActivoTrue();
}
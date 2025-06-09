package com.powerRanger.ElBuenSabor.repository;

import com.powerRanger.ElBuenSabor.entities.ArticuloInsumo;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface ArticuloInsumoRepository extends BaseRepository<ArticuloInsumo, Integer> { // Cambiado a BaseRepository

    @Query("SELECT ai FROM ArticuloInsumo ai WHERE " +
            "LOWER(ai.denominacion) LIKE LOWER(CONCAT('%', :searchTerm, '%')) " +
            "AND (:estadoActivoParam IS NULL OR ai.estadoActivo = :estadoActivoParam)")
    List<ArticuloInsumo> searchByDenominacionWithOptionalStatus(
            @Param("searchTerm") String searchTerm,
            @Param("estadoActivoParam") Boolean estadoActivoParam
    );

    @Query("SELECT ai FROM ArticuloInsumo ai WHERE (:estadoActivoParam IS NULL OR ai.estadoActivo = :estadoActivoParam)")
    List<ArticuloInsumo> findAllWithOptionalStatus(@Param("estadoActivoParam") Boolean estadoActivoParam);

    List<ArticuloInsumo> findByEstadoActivoTrue();
}
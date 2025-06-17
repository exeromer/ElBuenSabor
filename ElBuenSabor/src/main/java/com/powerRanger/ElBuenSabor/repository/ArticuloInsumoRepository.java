package com.powerRanger.ElBuenSabor.repository;

import com.powerRanger.ElBuenSabor.entities.ArticuloInsumo;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface ArticuloInsumoRepository extends JpaRepository<ArticuloInsumo, Integer> {

    // Busca por denominación, filtrando opcionalmente por estadoActivo
    @Query("SELECT ai FROM ArticuloInsumo ai WHERE " +
            "LOWER(ai.denominacion) LIKE LOWER(CONCAT('%', :searchTerm, '%')) " +
            "AND (:estadoActivoParam IS NULL OR ai.estadoActivo = :estadoActivoParam)")
    List<ArticuloInsumo> searchByDenominacionWithOptionalStatus(
            @Param("searchTerm") String searchTerm,
            @Param("estadoActivoParam") Boolean estadoActivoParam
    );

    // Encuentra todos los ArticuloInsumo, filtrando opcionalmente por estadoActivo
    @Query("SELECT ai FROM ArticuloInsumo ai WHERE (:estadoActivoParam IS NULL OR ai.estadoActivo = :estadoActivoParam)")
    List<ArticuloInsumo> findAllWithOptionalStatus(@Param("estadoActivoParam") Boolean estadoActivoParam);

    // Puedes mantener este si lo usas en otro lado específicamente para activos
    List<ArticuloInsumo> findByEstadoActivoTrue();
}


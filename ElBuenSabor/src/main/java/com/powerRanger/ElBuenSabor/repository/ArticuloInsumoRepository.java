package com.powerRanger.ElBuenSabor.repository;

import com.powerRanger.ElBuenSabor.entities.ArticuloInsumo;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface ArticuloInsumoRepository extends JpaRepository<ArticuloInsumo, Integer> {

    /**
     * Busca ArticuloInsumo activos cuya denominación contenga el término de búsqueda, ignorando mayúsculas/minúsculas.
     * @param searchTerm El término a buscar en la denominación.
     * @return Una lista de ArticuloInsumo que coinciden.
     */
    @Query("SELECT ai FROM ArticuloInsumo ai WHERE LOWER(ai.denominacion) LIKE LOWER(CONCAT('%', :searchTerm, '%')) AND ai.estadoActivo = true")
    List<ArticuloInsumo> searchByDenominacionActivos(@Param("searchTerm") String searchTerm);

    /**
     * Encuentra todos los ArticuloInsumo que están activos.
     * @return Una lista de ArticuloInsumo activos.
     */
    List<ArticuloInsumo> findByEstadoActivoTrue();


}

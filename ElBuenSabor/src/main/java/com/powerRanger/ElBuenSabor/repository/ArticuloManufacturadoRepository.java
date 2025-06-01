package com.powerRanger.ElBuenSabor.repository;

import com.powerRanger.ElBuenSabor.entities.ArticuloManufacturado;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
// import org.springframework.stereotype.Repository; // Opcional

public interface ArticuloManufacturadoRepository extends JpaRepository<ArticuloManufacturado, Integer> {

    @Query("SELECT am FROM ArticuloManufacturado am WHERE LOWER(am.denominacion) LIKE LOWER(CONCAT('%', :searchTerm, '%')) AND am.estadoActivo = true")
    List<ArticuloManufacturado> searchByDenominacionActivos(@Param("searchTerm") String searchTerm);

    List<ArticuloManufacturado> findByEstadoActivoTrue();

}
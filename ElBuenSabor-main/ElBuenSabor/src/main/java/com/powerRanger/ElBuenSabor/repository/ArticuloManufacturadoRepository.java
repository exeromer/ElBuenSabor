package com.powerRanger.ElBuenSabor.repository;

import com.powerRanger.ElBuenSabor.entities.ArticuloManufacturado;
import org.springframework.data.jpa.repository.JpaRepository;
// import org.springframework.stereotype.Repository; // Opcional

public interface ArticuloManufacturadoRepository extends JpaRepository<ArticuloManufacturado, Integer> {
}
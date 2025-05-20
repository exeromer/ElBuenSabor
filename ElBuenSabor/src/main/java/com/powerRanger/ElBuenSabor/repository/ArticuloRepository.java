package com.powerRanger.ElBuenSabor.repository;

import com.powerRanger.ElBuenSabor.entities.Articulo;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.Optional; // Importar Optional

@Repository
public interface ArticuloRepository extends JpaRepository<Articulo, Integer> {
    Optional<Articulo> findByDenominacion(String denominacion); // Devolver Optional
}
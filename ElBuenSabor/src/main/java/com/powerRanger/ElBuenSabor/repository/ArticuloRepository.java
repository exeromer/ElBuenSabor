package com.powerRanger.ElBuenSabor.repository;

import com.powerRanger.ElBuenSabor.entities.Articulo;
import org.springframework.stereotype.Repository;
import java.util.Optional;

@Repository
public interface ArticuloRepository extends BaseRepository<Articulo, Integer> { // Cambiado a BaseRepository
    Optional<Articulo> findByDenominacion(String denominacion);
}
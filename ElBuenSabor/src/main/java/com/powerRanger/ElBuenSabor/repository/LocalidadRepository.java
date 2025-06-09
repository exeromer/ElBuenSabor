package com.powerRanger.ElBuenSabor.repository;

import com.powerRanger.ElBuenSabor.entities.Localidad;
import org.springframework.stereotype.Repository;

@Repository
public interface LocalidadRepository extends BaseRepository<Localidad, Integer> { // Cambiado a BaseRepository
}
package com.powerRanger.ElBuenSabor.repository;

import com.powerRanger.ElBuenSabor.entities.Provincia;
import org.springframework.stereotype.Repository;

@Repository
public interface ProvinciaRepository extends BaseRepository<Provincia, Integer> { // Cambiado a BaseRepository
}
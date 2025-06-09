package com.powerRanger.ElBuenSabor.repository;

import com.powerRanger.ElBuenSabor.entities.Pais;
import org.springframework.stereotype.Repository;

@Repository
public interface PaisRepository extends BaseRepository<Pais, Integer> { // Cambiado a BaseRepository
}
package com.powerRanger.ElBuenSabor.repository;

import com.powerRanger.ElBuenSabor.entities.UnidadMedida;
import org.springframework.stereotype.Repository;

@Repository
public interface UnidadMedidaRepository extends BaseRepository<UnidadMedida, Integer> { // Cambiado a BaseRepository
}
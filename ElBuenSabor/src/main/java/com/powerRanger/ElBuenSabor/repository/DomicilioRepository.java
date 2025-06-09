package com.powerRanger.ElBuenSabor.repository;

import com.powerRanger.ElBuenSabor.entities.Domicilio;
import com.powerRanger.ElBuenSabor.entities.Localidad;
import org.springframework.stereotype.Repository;
import java.util.Optional;

@Repository
public interface DomicilioRepository extends BaseRepository<Domicilio, Integer> { // Cambiado a BaseRepository

    Optional<Domicilio> findByCalleAndNumeroAndCpAndLocalidad(
            String calle,
            Integer numero,
            String cp,
            Localidad localidad
    );
}
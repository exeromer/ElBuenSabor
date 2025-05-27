package com.powerRanger.ElBuenSabor.repository;

import com.powerRanger.ElBuenSabor.entities.Domicilio;
import com.powerRanger.ElBuenSabor.entities.Localidad; // Importar Localidad
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface DomicilioRepository extends JpaRepository<Domicilio, Integer> {

    // Nuevo método para buscar un domicilio por todos sus campos relevantes (excepto ID)
    // para evitar duplicados.
    Optional<Domicilio> findByCalleAndNumeroAndCpAndLocalidad(
            String calle,
            Integer numero,
            String cp,
            Localidad localidad
    );
}
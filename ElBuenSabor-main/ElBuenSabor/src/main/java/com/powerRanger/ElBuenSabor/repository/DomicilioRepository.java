package com.powerRanger.ElBuenSabor.repository;

import com.powerRanger.ElBuenSabor.entities.Domicilio;
import org.springframework.data.jpa.repository.JpaRepository;
// import org.springframework.stereotype.Repository; // Opcional

public interface DomicilioRepository extends JpaRepository<Domicilio, Integer> {
}
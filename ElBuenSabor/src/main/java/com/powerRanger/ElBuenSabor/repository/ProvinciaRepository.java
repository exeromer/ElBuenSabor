package com.powerRanger.ElBuenSabor.repository;

import com.powerRanger.ElBuenSabor.entities.Provincia;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ProvinciaRepository extends JpaRepository<Provincia, Integer> {
}

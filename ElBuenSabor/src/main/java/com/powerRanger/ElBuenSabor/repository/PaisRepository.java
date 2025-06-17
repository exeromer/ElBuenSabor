package com.powerRanger.ElBuenSabor.repository;

import com.powerRanger.ElBuenSabor.entities.Pais;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface PaisRepository extends JpaRepository<Pais, Integer> {
}
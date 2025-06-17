package com.powerRanger.ElBuenSabor.repository;

import com.powerRanger.ElBuenSabor.entities.Imagen;
import org.springframework.data.jpa.repository.JpaRepository;
// import org.springframework.stereotype.Repository; // Opcional

public interface ImagenRepository extends JpaRepository<Imagen, Integer> {
}
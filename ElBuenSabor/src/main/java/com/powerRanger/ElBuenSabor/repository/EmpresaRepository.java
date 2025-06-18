package com.powerRanger.ElBuenSabor.repository;

import com.powerRanger.ElBuenSabor.entities.Empresa;
import org.springframework.data.jpa.repository.JpaRepository;
// import org.springframework.stereotype.Repository; // Opcional

public interface EmpresaRepository extends JpaRepository<Empresa, Integer> {
    // Podrías añadir búsquedas específicas si las necesitas, ej:
    // Optional<Empresa> findByCuil(String cuil);
    // Optional<Empresa> findByNombre(String nombre);
}
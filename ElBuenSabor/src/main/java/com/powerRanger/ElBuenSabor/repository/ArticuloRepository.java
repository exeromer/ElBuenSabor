package com.powerRanger.ElBuenSabor.repository;

import com.powerRanger.ElBuenSabor.entities.Articulo;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository; // Es bueno añadir @Repository aunque no siempre estrictamente necesario para interfaces JpaRepository

@Repository // Añadido
public interface ArticuloRepository extends JpaRepository<Articulo, Integer> {

    // --- MÉTODO DE CONSULTA AÑADIDO ---
    Articulo findByDenominacion(String denominacion);
    // Opcionalmente, si puede no existir y quieres un manejo más explícito:
    // Optional<Articulo> findByDenominacion(String denominacion);
}
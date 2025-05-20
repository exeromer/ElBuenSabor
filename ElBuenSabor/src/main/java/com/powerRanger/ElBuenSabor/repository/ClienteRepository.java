package com.powerRanger.ElBuenSabor.repository;

import com.powerRanger.ElBuenSabor.entities.Cliente;
import org.springframework.data.jpa.repository.JpaRepository;
// import org.springframework.stereotype.Repository; // Opcional

public interface ClienteRepository extends JpaRepository<Cliente, Integer> {
    // Podrías añadir búsquedas específicas si las necesitas, ej:
    // Optional<Cliente> findByEmail(String email);
    // Optional<Cliente> findByUsuarioId(Integer usuarioId);
}
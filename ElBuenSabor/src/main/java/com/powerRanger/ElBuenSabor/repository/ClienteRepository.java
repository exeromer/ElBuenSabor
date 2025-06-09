package com.powerRanger.ElBuenSabor.repository;

import com.powerRanger.ElBuenSabor.entities.Cliente;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface ClienteRepository extends BaseRepository<Cliente, Integer> { // Cambiado a BaseRepository
    Optional<Cliente> findByUsuarioId(Integer usuarioId);
    Optional<Cliente> findByEmail(String email);

    @Query("SELECT c FROM Cliente c WHERE " +
            "(LOWER(c.nombre) LIKE LOWER(CONCAT('%', :searchTerm, '%')) OR " +
            "LOWER(c.apellido) LIKE LOWER(CONCAT('%', :searchTerm, '%')) OR " +
            "LOWER(c.email) LIKE LOWER(CONCAT('%', :searchTerm, '%'))) " +
            "AND c.estadoActivo = true")
    List<Cliente> searchActivosByTerm(@Param("searchTerm") String searchTerm);

    List<Cliente> findByEstadoActivoTrue();
}
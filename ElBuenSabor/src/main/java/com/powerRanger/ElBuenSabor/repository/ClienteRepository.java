package com.powerRanger.ElBuenSabor.repository;
import com.powerRanger.ElBuenSabor.entities.Cliente;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;
public interface ClienteRepository extends JpaRepository<Cliente, Integer> {
    Optional<Cliente> findByUsuarioId(Integer usuarioId);
    Optional<Cliente> findByEmail(String email); // Si lo tienes
}
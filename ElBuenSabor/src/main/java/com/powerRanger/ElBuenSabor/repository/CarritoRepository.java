package com.powerRanger.ElBuenSabor.repository;

import com.powerRanger.ElBuenSabor.entities.Carrito;
import com.powerRanger.ElBuenSabor.entities.Cliente;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface CarritoRepository extends JpaRepository<Carrito, Long> {

    // Podríamos necesitar un método para encontrar el carrito activo de un cliente.
    // Por ahora, asumiremos que la lógica de "cuál es el carrito activo"
    // se maneja en el servicio, o que un cliente solo tiene un carrito persistido.
    // Si se implementa una lógica de un solo carrito activo por cliente:
    // Optional<Carrito> findByClienteAndActivoTrue(Cliente cliente); // 'activo' sería un campo en Carrito

    // Encuentra un carrito por cliente. Si un cliente solo puede tener un carrito, esto es útil.
    // Si puede tener varios (historial de carritos), necesitaríamos más criterios.
    Optional<Carrito> findByCliente(Cliente cliente);

    // Si permitimos carritos anónimos referenciados por sessionId:
    // Optional<Carrito> findBySessionId(String sessionId);
}
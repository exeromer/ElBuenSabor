package com.powerRanger.ElBuenSabor.repository;

import com.powerRanger.ElBuenSabor.entities.Carrito;
import com.powerRanger.ElBuenSabor.entities.Cliente;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface CarritoRepository extends BaseRepository<Carrito, Integer> { // Cambiado a BaseRepository y a Integer
    Optional<Carrito> findByCliente(Cliente cliente);
}
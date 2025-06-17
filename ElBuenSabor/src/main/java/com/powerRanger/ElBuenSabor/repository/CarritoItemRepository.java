package com.powerRanger.ElBuenSabor.repository;

import com.powerRanger.ElBuenSabor.entities.Articulo;
import com.powerRanger.ElBuenSabor.entities.Carrito;
import com.powerRanger.ElBuenSabor.entities.CarritoItem;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface CarritoItemRepository extends JpaRepository<CarritoItem, Long> {

    // Método para encontrar un ítem específico en un carrito por el artículo,
    // útil para ver si el artículo ya existe y debemos actualizar la cantidad.
    Optional<CarritoItem> findByCarritoAndArticulo(Carrito carrito, Articulo articulo);
}
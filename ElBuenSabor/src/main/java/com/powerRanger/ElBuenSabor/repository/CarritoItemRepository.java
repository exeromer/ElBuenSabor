package com.powerRanger.ElBuenSabor.repository;

import com.powerRanger.ElBuenSabor.entities.Articulo;
import com.powerRanger.ElBuenSabor.entities.Carrito;
import com.powerRanger.ElBuenSabor.entities.CarritoItem;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface CarritoItemRepository extends BaseRepository<CarritoItem, Integer> { // Cambiado a BaseRepository y a Integer

    Optional<CarritoItem> findByCarritoAndArticulo(Carrito carrito, Articulo articulo);
}
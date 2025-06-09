package com.powerRanger.ElBuenSabor.services;

import com.powerRanger.ElBuenSabor.dtos.AddItemToCartRequestDTO;
import com.powerRanger.ElBuenSabor.dtos.CarritoResponseDTO;
import com.powerRanger.ElBuenSabor.entities.Cliente;

public interface CarritoService {

    CarritoResponseDTO getOrCreateCarrito(Cliente cliente) throws Exception;

    CarritoResponseDTO addItemAlCarrito(Cliente cliente, AddItemToCartRequestDTO itemRequest) throws Exception;

    CarritoResponseDTO actualizarCantidadItem(Cliente cliente, Integer carritoItemId, int nuevaCantidad) throws Exception;

    CarritoResponseDTO eliminarItemDelCarrito(Cliente cliente, Integer carritoItemId) throws Exception;

    CarritoResponseDTO vaciarCarrito(Cliente cliente) throws Exception;


}
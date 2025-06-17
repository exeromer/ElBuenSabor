package com.powerRanger.ElBuenSabor.services;

import com.powerRanger.ElBuenSabor.dtos.AddItemToCartRequestDTO;
import com.powerRanger.ElBuenSabor.dtos.CarritoResponseDTO;
import com.powerRanger.ElBuenSabor.entities.Cliente;

public interface CarritoService {

    /**
     * Obtiene el carrito activo del cliente. Si no existe, lo crea vacío.
     * @param cliente El cliente para el cual se obtiene el carrito.
     * @return CarritoResponseDTO representando el carrito del cliente.
     * @throws Exception Si ocurre un error.
     */
    CarritoResponseDTO getOrCreateCarrito(Cliente cliente) throws Exception;

    /**
     * Agrega un artículo al carrito del cliente o actualiza su cantidad si ya existe.
     * @param cliente El cliente dueño del carrito.
     * @param itemRequest DTO con el articuloId y la cantidad.
     * @return CarritoResponseDTO representando el carrito actualizado.
     * @throws Exception Si el artículo no existe, o hay problemas (ej. no está activo).
     */
    CarritoResponseDTO addItemAlCarrito(Cliente cliente, AddItemToCartRequestDTO itemRequest) throws Exception;

    /**
     * Actualiza la cantidad de un ítem específico en el carrito del cliente.
     * La cantidad debe ser 1 o mayor. Si se desea eliminar, usar eliminarItemDelCarrito.
     * @param cliente El cliente dueño del carrito.
     * @param carritoItemId El ID del CarritoItem a actualizar.
     * @param nuevaCantidad La nueva cantidad para el ítem (debe ser >= 1).
     * @return CarritoResponseDTO representando el carrito actualizado.
     * @throws Exception Si el ítem no pertenece al carrito del cliente, o la cantidad es inválida.
     */
    CarritoResponseDTO actualizarCantidadItem(Cliente cliente, Long carritoItemId, int nuevaCantidad) throws Exception;

    /**
     * Elimina un ítem específico del carrito del cliente.
     * @param cliente El cliente dueño del carrito.
     * @param carritoItemId El ID del CarritoItem a eliminar.
     * @return CarritoResponseDTO representando el carrito actualizado.
     * @throws Exception Si el ítem no pertenece al carrito del cliente.
     */
    CarritoResponseDTO eliminarItemDelCarrito(Cliente cliente, Long carritoItemId) throws Exception;

    /**
     * Vacía todos los ítems del carrito del cliente.
     * @param cliente El cliente dueño del carrito.
     * @return CarritoResponseDTO representando el carrito ahora vacío.
     * @throws Exception Si ocurre un error al acceder o modificar el carrito.
     */
    CarritoResponseDTO vaciarCarrito(Cliente cliente) throws Exception;


}
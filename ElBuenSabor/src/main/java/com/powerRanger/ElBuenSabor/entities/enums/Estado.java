package com.powerRanger.ElBuenSabor.entities.enums;

/**
 *
 * @author Hitman
 */
public enum Estado {
    PENDIENTE,    // Pedido recién creado, pendiente de confirmación o pago
    PAGADO,       // Pedido pagado (especialmente útil para flujos con Mercado Pago antes de pasar a preparación)
    PREPARACION,  // Pedido en cocina/preparación
    EN_CAMINO,    // Pedido despachado para delivery
    ENTREGADO,    // Pedido completado y entregado/retirado
    RECHAZADO,    // Pedido rechazado (por el local o por fallo de pago no recuperable)
    CANCELADO     // Pedido cancelado (por el cliente o por el local antes de procesar/pagar)
}
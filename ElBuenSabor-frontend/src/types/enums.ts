export type Rol = 'ADMIN' | 'EMPLEADO' | 'CLIENTE';
export type RolEmpleado = 'CAJERO' | 'COCINA' | 'DELIVERY';
export type Estado =     'PENDIENTE'|'PREPARACION'|'EN_CAMINO'|'LISTO'|'ENTREGADO'|'RECHAZADO'|'CANCELADO';
export type EstadoFactura = 'PENDIENTE' | 'PAGADA' | 'ANULADA';
export type FormaPago = 'EFECTIVO' | 'MERCADO_PAGO';
export type TipoEnvio = 'DELIVERY' | 'TAKEAWAY';
export type TipoPromocion = 'PORCENTAJE' | 'CANTIDAD' | 'COMBO';
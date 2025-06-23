export type Rol = 'ADMIN' | 'CLIENTE' | 'EMPLEADO' | 'COCINERO' | 'CAJERO' | 'DELIVERY';
export type Estado =     'PENDIENTE'|'PREPARACION'|'EN_CAMINO'|'ENTREGADO'|'RECHAZADO'|'CANCELADO';
export type EstadoFactura = 'PENDIENTE' | 'PAGADA' | 'ANULADA';
export type FormaPago = 'EFECTIVO' | 'MERCADO_PAGO';
export type TipoEnvio = 'DELIVERY' | 'TAKEAWAY';
export type TipoPromocion = 'PORCENTAJE' | 'CANTIDAD' | 'COMBO';
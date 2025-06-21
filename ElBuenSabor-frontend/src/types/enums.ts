export type Rol = 'ADMIN' | 'CLIENTE' | 'EMPLEADO' | 'COCINERO' | 'CAJERO' | 'DELIVERY';
export type Estado = 'PENDIENTE' | 'PAGADO' | 'PREPARACION' | 'PENDIENTE_ENTREGA' | 'EN_CAMINO' | 'CANCELADO' | 'NOTA_CREDITO' | 'COMPLETADO';
export type EstadoFactura = 'PENDIENTE' | 'PAGADA' | 'ANULADA';
export type FormaPago = 'EFECTIVO' | 'MERCADO_PAGO';
export type TipoEnvio = 'DELIVERY' | 'TAKE_AWAY';
export type TipoPromocion = 'HAPPY_HOUR' | 'PROMOCION_PORCENTAJE' | 'PROMOCION_PRECIO_FIJO';
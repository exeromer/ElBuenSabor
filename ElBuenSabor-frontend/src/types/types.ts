// --- Enums y Tipos Literales ---
import type { Rol, Estado, FormaPago, TipoEnvio, EstadoFactura, TipoPromocion } from './enums';

// --- Interfaces de DTOs ---

export interface AddItemToCartRequest {
  articuloId: number;
  cantidad: number;
}

export interface ArticuloInsumoRequest {
  denominacion: string;
  precioVenta: number;
  unidadMedidaId: number;
  categoriaId: number;
  estadoActivo: boolean;
  precioCompra?: number;
  esParaElaborar: boolean;
}

export interface ArticuloManufacturadoDetalle {
  cantidad: number;
  articuloInsumoId: number;
  estadoActivo: boolean;
}

export interface ArticuloManufacturadoDetalleResponse {
  id: number;
  cantidad: number;
  articuloInsumo: ArticuloSimpleResponse;
  estadoActivo: boolean;
}

export interface ArticuloManufacturadoRanking {
  articuloId: number;
  denominacion: string;
  cantidadVendida: number;
}

export interface ArticuloManufacturadoRequest {
  denominacion: string;
  precioVenta: number;
  unidadMedidaId: number;
  categoriaId: number;
  estadoActivo: boolean;
  descripcion?: string;
  tiempoEstimadoMinutos: number;
  preparacion: string;
  manufacturadoDetalles: ArticuloManufacturadoDetalle[];
}

export interface ArticuloSimpleResponse {
  id: number;
  denominacion: string;
  precioVenta: number;
}

export interface CarritoItemResponse {
  id: number;
  articuloId: number;
  articuloDenominacion: string;
  cantidad: number;
  precioUnitarioAlAgregar: number;
  subtotalItem: number;
  promocionAplicadaId?: number;
  descuentoAplicadoPorPromocion?: number;
}

export interface CarritoResponse {
  id: number;
  clienteId: number;
  fechaCreacion: string; // LocalDateTime -> string
  fechaUltimaModificacion: string; // LocalDateTime -> string
  items: CarritoItemResponse[];
  totalCarrito: number;
}

export interface CategoriaResponse {
  id: number;
  denominacion: string;
  estadoActivo: boolean;
}
export interface CategoriaRequest {
  denominacion: string;
  estadoActivo: boolean;
}
export interface ClienteRanking {
  clienteId: number;
  nombreCompleto: string;
  email: string;
  cantidadPedidos: number;
  montoTotalComprado: number;
}

export interface ClienteRequest {
  nombre: string;
  apellido: string;
  telefono?: string;
  email: string;
  fechaNacimiento?: string; // LocalDate -> string
  usuarioId: number;
  domicilioIds: number[];
  estadoActivo: boolean;
}

export interface ClienteResponse {
  id: number;
  nombre: string;
  apellido: string;
  telefono?: string;
  email: string;
  fechaNacimiento?: string; // LocalDate -> string
  estadoActivo: boolean;
  fechaBaja?: string; // LocalDate -> string
  usuarioId: number;
  username: string;
  rolUsuario: Rol;
  domicilios: DomicilioResponse[];
}

export interface CrearPedidoRequest {
  calleDomicilio: string;
  numeroDomicilio: number;
  cpDomicilio: string;
  localidadIdDomicilio: number;
  tipoEnvio: TipoEnvio;
  formaPago: FormaPago;
  sucursalId: number;
  horaEstimadaFinalizacion: string; // "HH:mm" o "HH:mm:ss"
  notasAdicionales?: string;
  guardarDireccionEnPerfil: boolean;
}

export interface DetallePedidoRequest {
  articuloId: number;
  cantidad: number;
}

export interface DetallePedidoResponse {
  id: number;
  cantidad: number;
  subTotal: number;
  articulo: ArticuloSimpleResponse;
  promocionAplicadaId?: number;
  descuentoAplicadoPorPromocion?: number;
}

export interface DomicilioRequest {
  calle: string;
  numero: number;
  cp: string;
  localidadId: number;
}

export interface DomicilioResponse {
  id: number;
  calle: string;
  numero: number;
  cp: string;
  localidad: LocalidadResponse;
}

export interface EmpresaRequest {
  nombre: string;
  razonSocial: string;
  cuil: string;
}

export interface EmpresaResponse {
  id: number;
  nombre: string;
  razonSocial: string;
  cuil: string;
}

export interface FacturaCreateRequest {
  pedidoId: number;
  mpPaymentId?: number;
  mpMerchantOrderId?: number;
  mpPreferenceId?: string;
  mpPaymentType?: string;
}

export interface FacturaDetalleResponse {
  id: number;
  cantidad: number;
  denominacionArticulo: string;
  precioUnitarioArticulo: number;
  subTotal: number;
  articulo: ArticuloSimpleResponse;
}

export interface FacturaResponse {
  id: number;
  fechaFacturacion: string; // LocalDate -> string
  mpPaymentId?: number;
  mpMerchantOrderId?: number;
  mpPreferenceId?: string;
  mpPaymentType?: string;
  totalVenta: number;
  subtotal?: number;
  totalDescuentos?: number;
  formaPago: FormaPago;
  estadoFactura: EstadoFactura;
  fechaAnulacion?: string; // LocalDate -> string
  pedido: PedidoSimpleResponse;
  detallesFactura: FacturaDetalleResponse[];
}

export interface ImagenRequest {
  denominacion: string;
  articuloId?: number;
  promocionId?: number;
  estadoActivo: boolean;
}

export interface ImagenResponse {
  id: number;
  denominacion: string; // URL
  estadoActivo: boolean;
  articuloId?: number;
  articuloDenominacion?: string;
  promocionId?: number;
  promocionDenominacion?: string;
}

export interface LocalidadResponse {
  id: number;
  nombre: string;
  provincia: ProvinciaResponse;
}

export interface MercadoPagoCreatePreference {
  pedidoId: number;
}

export interface PaisResponse {
  id: number;
  nombre: string;
}

export interface PedidoEstadoRequest {
  nuevoEstado: Estado;
}

export interface PedidoRequest {
  horaEstimadaFinalizacion: string; // "HH:mm" o "HH:mm:ss"
  sucursalId: number;
  domicilioId?: number;
  tipoEnvio: TipoEnvio;
  formaPago: FormaPago;
  clienteId: number;
  detalles: DetallePedidoRequest[];
}

export interface PedidoResponse {
  id: number;
  horaEstimadaFinalizacion: string; // LocalTime -> string
  total: number;
  totalCosto: number;
  fechaPedido: string; // LocalDate -> string
  estado: Estado;
  tipoEnvio: TipoEnvio;
  formaPago: FormaPago;
  estadoActivo: boolean;
  fechaBaja?: string; // LocalDate -> string
  sucursal: SucursalResponse;
  domicilio: DomicilioResponse;
  cliente: ClienteResponse;
  mpPreferenceId?: string;
  descuentoAplicado?: number;
  detalles: DetallePedidoResponse[];
  factura?: FacturaResponse;
}

export interface PedidoSimpleResponse {
  id: number;
  fechaPedido: string; // LocalDate -> string
}

export interface PromocionDetalleRequest {
  cantidad: number;
  articuloId: number;
}

export interface PromocionDetalleResponse {
  id: number;
  cantidad: number;
  articulo: ArticuloSimpleResponse;
}

export interface PromocionRequest {
  denominacion: string;
  fechaDesde: string; // LocalDate -> string
  fechaHasta: string; // LocalDate -> string
  horaDesde: string; // LocalTime -> string
  horaHasta: string; // LocalTime -> string
  descripcionDescuento?: string;
  precioPromocional?: number;
  tipoPromocion: TipoPromocion;
  porcentajeDescuento?: number;
  imagenIds: number[];
  detallesPromocion: PromocionDetalleRequest[];
  estadoActivo: boolean;
  sucursalIds: number[];
}

export interface PromocionResponse {
  id: number;
  denominacion: string;
  fechaDesde: string; // LocalDate -> string
  fechaHasta: string; // LocalDate -> string
  horaDesde: string; // LocalTime -> string
  horaHasta: string; // LocalTime -> string
  descripcionDescuento?: string;
  precioPromocional?: number;
  tipoPromocion: TipoPromocion;
  porcentajeDescuento?: number;
  estadoActivo: boolean;
  imagenes: ImagenResponse[];
  detallesPromocion: PromocionDetalleResponse[];
  sucursales: SucursalSimpleResponse[];
}

export interface PromocionSimpleResponse {
  id: number;
  denominacion: string;
}

export interface ProvinciaResponse {
  id: number;
  nombre: string;
  pais: PaisResponse;
}

export interface StockInsumoSucursalRequest {
  articuloInsumoId: number;
  sucursalId: number;
  stockActual: number;
  stockMinimo: number;
}

export interface StockInsumoSucursalResponse {
  id: number;
  articuloInsumoId: number;
  articuloInsumoDenominacion: string;
  sucursalId: number;
  sucursalNombre: string;
  stockActual: number;
  stockMinimo: number;
}

export interface SucursalRequest {
  nombre: string;
  horarioApertura: string; // "HH:mm"
  horarioCierre: string; // "HH:mm"
  empresaId: number;
  domicilio: DomicilioRequest;
  promocionIds: number[];
  categoriaIds: number[];
  estadoActivo: boolean;
}

export interface SucursalResponse {
  id: number;
  nombre: string;
  horarioApertura: string; // LocalTime -> string
  horarioCierre: string; // LocalTime -> string
  estadoActivo: boolean;
  fechaBaja?: string; // LocalDate -> string
  empresa: EmpresaResponse;
  domicilio: DomicilioResponse;
  categorias: CategoriaResponse[];
  promociones: PromocionSimpleResponse[];
}

export interface SucursalSimpleResponse {
  id: number;
  nombre: string;
}

export interface UnidadMedidaResponse {
  id: number;
  denominacion: string;
}

export interface UpdateCartItemQuantityRequest {
  nuevaCantidad: number;
}

export interface UsuarioRequest {
  auth0Id: string;
  username: string;
  rol: Rol;
  estadoActivo: boolean;
}

export interface UsuarioResponse {
  id: number;
  auth0Id: string;
  username: string;
  rol: Rol;
  estadoActivo: boolean;
  fechaBaja?: string; // LocalDate -> string
}


// --- Tipos Polimórficos (Uniones Discriminadas) ---

// Base para todos los artículos en respuestas
export interface ArticuloBaseResponse {
  id: number;
  denominacion: string;
  precioVenta: number;
  estadoActivo: boolean;
  unidadMedida: UnidadMedidaResponse;
  categoria: CategoriaResponse;
  imagenes: ImagenResponse[];
  type: 'insumo' | 'manufacturado'; // Propiedad discriminante
}

// DTO de respuesta para Artículo Insumo, extendiendo la base
export interface ArticuloInsumoResponse extends ArticuloBaseResponse {
  type: 'insumo';
  precioCompra?: number;
  esParaElaborar: boolean;
}

// DTO de respuesta para Artículo Manufacturado, extendiendo la base
export interface ArticuloManufacturadoResponse extends ArticuloBaseResponse {
  type: 'manufacturado';
  descripcion?: string;
  tiempoEstimadoMinutos: number;
  preparacion: string;
  manufacturadoDetalles: ArticuloManufacturadoDetalleResponse[];
  unidadesDisponiblesCalculadas?: number;
}

// Unión discriminada para manejar cualquier tipo de Artículo en el frontend
export type ArticuloResponse = ArticuloInsumoResponse | ArticuloManufacturadoResponse;
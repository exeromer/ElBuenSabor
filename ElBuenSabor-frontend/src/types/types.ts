// Nueva carpeta/ElBuenSabor-frontend/src/types/types.ts

/**
 * @file types.ts
 * @description Este archivo centraliza todas las definiciones de interfaces (modelos de datos) y tipos
 * utilizados en el frontend de la aplicación. Estos tipos reflejan la estructura de las entidades
 * y DTOs (Data Transfer Objects) que se intercambian con el backend de Spring Boot,
 * facilitando la tipificación fuerte y la validación en el desarrollo.
 *
 * Se agrupan los tipos por su dominio lógico (Entidades Base, DTOs de Request, DTOs de Response, Enums).
 * Las propiedades y tipos han sido ajustados para coincidir con las clases Java proporcionadas,
 * incluyendo el mapeo de `LocalDate`/`LocalTime`/`LocalDateTime` a `string` y `Integer`/`Double`/`Long` a `number`.
 */

// ==============================================================
// --- ENUMS (Mapeo de ENUMS de Java a Union Types de TypeScript) ---
// ==============================================================

/**
 * @typedef Estado
 * @description Representa el estado actual de un Pedido.
 * Corresponde a `com.powerRanger.ElBuenSabor.entities.enums.Estado`.
 */
export type Estado = 'PENDIENTE' | 'PAGADO' | 'PREPARACION' | 'EN_CAMINO' | 'ENTREGADO' | 'RECHAZADO' | 'CANCELADO';

/**
 * @typedef FormaPago
 * @description Representa la forma de pago utilizada en un Pedido o Factura.
 * Corresponde a `com.powerRanger.ElBuenSabor.entities.enums.FormaPago`.
 */
export type FormaPago = 'EFECTIVO' | 'MERCADO_PAGO';

/**
 * @typedef TipoEnvio
 * @description Representa el tipo de envío de un Pedido.
 * Corresponde a `com.powerRanger.ElBuenSabor.entities.enums.TipoEnvio`.
 */
export type TipoEnvio = 'DELIVERY' | 'TAKEAWAY';

/**
 * @typedef Rol
 * @description Representa el rol de un Usuario.
 * Corresponde a `com.powerRanger.ElBuenSabor.entities.enums.Rol`.
 */
export type Rol = 'ADMIN' | 'EMPLEADO' | 'CLIENTE';

/**
 * @typedef EstadoFactura
 * @description Representa el estado de una Factura.
 * Corresponde a `com.powerRanger.ElBuenSabor.entities.enums.EstadoFactura`.
 */
export type EstadoFactura = 'ACTIVA' | 'ANULADA';


// ==============================================================
// --- FRONTEND-SPECIFIC / UTILITY TYPES ---
// ==============================================================

/**
 * @interface EntityWithId
 * @description Un tipo utilitario que representa una entidad que necesariamente tiene un ID.
 * Útil para componentes o funciones que requieren un ID definido (ej. para keys de React).
 */
export interface EntityWithId {
  id: number;
}

// ==============================================================
// --- INTERFACES BASE Y ENTIDADES (Mapeo de clases de entidad de Java) ---
// Estas interfaces son la representación más completa de las entidades para el frontend,
// a menudo usadas para respuestas o para tipificar objetos anidados.
// El orden de declaración es crucial debido a las dependencias.
// ==============================================================

/**
 * @interface BaseEntity
 * @description Interfaz base para todas las entidades con un ID.
 * Corresponde a la clase `BaseEntity` de Java.
 */
export interface BaseEntity {
  id?: number;
}

/**
 * @interface Pais
 * @description Entidad País.
 * Corresponde a la entidad `Pais` de Java.
 */
export interface Pais extends BaseEntity {
  denominacion: string;
}

/**
 * @interface Provincia
 * @description Entidad Provincia.
 * Corresponde a la entidad `Provincia` de Java.
 */
export interface Provincia extends BaseEntity {
  denominacion: string;
  pais: Pais; // Objeto completo Pais
}

/**
 * @interface Localidad
 * @description Entidad Localidad.
 * Corresponde a la entidad `Localidad` de Java.
 */
export interface Localidad extends BaseEntity {
  denominacion: string;
  provincia: Provincia; // Objeto completo Provincia
}

/**
 * @interface Domicilio
 * @description Entidad Domicilio.
 * Corresponde a la entidad `Domicilio` de Java.
 */
export interface Domicilio extends BaseEntity {
  calle: string;
  numero: number;
  cp: string; // `String` en Java
  localidad: Localidad; // Objeto completo Localidad
}

/**
 * @interface Usuario
 * @description Entidad Usuario.
 * Corresponde a la entidad `Usuario` de Java.
 */
export interface Usuario extends BaseEntity {
  auth0Id: string;
  username: string;
  rol: Rol; // Usando el union type `Rol`
  fechaBaja?: string | null; // `LocalDate` en Java, mapeado a `string` 'YYYY-MM-DD' o null
  estadoActivo: boolean; // `Boolean` en Java
}

/**
 * @interface Imagen
 * @description Representa una imagen asociada a otras entidades.
 * Corresponde a la entidad `Imagen` en el backend.
 */
export interface Imagen extends BaseEntity {
  denominacion: string; // URL o nombre del archivo
  estadoActivo: boolean;
}

/**
 * @interface UnidadMedida
 * @description Representa una unidad de medida para artículos.
 * Corresponde a la entidad `UnidadMedida` de Java.
 */
export interface UnidadMedida extends BaseEntity {
  denominacion: string;
}

/**
 * @interface Categoria
 * @description Representa una categoría de artículos.
 * Corresponde a la entidad `Categoria` de Java.
 */
export interface Categoria extends BaseEntity {
  denominacion: string;
  estadoActivo: boolean;
}

/**
 * @interface Empresa
 * @description Entidad Empresa.
 * Corresponde a la entidad `Empresa` de Java.
 */
export interface Empresa extends BaseEntity {
  nombre: string;
  razonSocial: string;
  cuil: string;
}

/**
 * @interface ArticuloBase
 * @description Interfaz base que define las propiedades comunes a `ArticuloManufacturado` y `ArticuloInsumo`.
 * Corresponde a la superclase `Articulo` en el backend.
 */
export interface ArticuloBase extends BaseEntity {
  denominacion: string;
  precioVenta: number; // `Double` en Java
  unidadMedida: UnidadMedida; // Objeto completo
  imagenes: Imagen[]; // Lista de objetos Imagen
  categoria: Categoria; // Objeto completo
  estadoActivo: boolean;
}

/**
 * @interface ArticuloInsumo
 * @description Representa un artículo que es un insumo o materia prima.
 * Extiende `ArticuloBase`. Corresponde a la entidad `ArticuloInsumo` de Java.
 */
export interface ArticuloInsumo extends ArticuloBase {
  precioCompra?: number | null; // `Double` en Java, puede ser nulo
  stockActual: number; // `Double` en Java
  stockMinimo?: number | null; // `Double` en Java, puede ser nulo
  esParaElaborar: boolean; // `Boolean` en Java
}

/**
 * @interface ArticuloManufacturadoDetalle
 * @description Representa un detalle de un artículo manufacturado (qué insumos lo componen).
 * Corresponde a la entidad `ArticuloManufacturadoDetalle` de Java.
 */
export interface ArticuloManufacturadoDetalle extends BaseEntity {
  cantidad: number; // `Integer` en Java
  articuloInsumo: ArticuloInsumo; // Objeto completo ArticuloInsumo
  estadoActivo: boolean; // `Boolean` en Java
}

/**
 * @interface ArticuloManufacturado
 * @description Representa un artículo que se elabora a partir de insumos.
 * Extiende `ArticuloBase`. Corresponde a la entidad `ArticuloManufacturado` de Java.
 */
export interface ArticuloManufacturado extends ArticuloBase {
  descripcion: string;
  tiempoEstimadoMinutos: number; // `Integer` en Java
  preparacion: string;
  manufacturadoDetalles: ArticuloManufacturadoDetalle[]; // Lista de objetos
  unidadesDisponiblesCalculadas?: number; // Campo calculado (si es parte de la entidad respuesta)
}

/**
 * @typedef Articulo
 * @description Tipo de unión para representar cualquier tipo de Artículo (Manufacturado o Insumo).
 */
export type Articulo = ArticuloManufacturado | ArticuloInsumo;

/**
 * @interface Cliente
 * @description Representa un cliente, asociado a un usuario.
 * Corresponde a la entidad `Cliente` de Java.
 */
export interface Cliente extends BaseEntity {
  nombre: string;
  apellido: string;
  telefono: string;
  email: string;
  fechaNacimiento?: string | null; // `LocalDate` en Java, 'YYYY-MM-DD' o null
  domicilios: Domicilio[]; // Lista de objetos Domicilio
  usuario: Usuario; // Objeto completo Usuario
  imagen?: Imagen | null; // `Imagen` en Java, puede ser nulo
  fechaBaja?: string | null; // `LocalDate` en Java, 'YYYY-MM-DD' o null
  estadoActivo: boolean; // `Boolean` en Java
}

/**
 * @interface DetallePedido
 * @description Representa un ítem dentro de un pedido, con la cantidad y el artículo asociado.
 * Corresponde a la entidad `DetallePedido` de Java.
 */
export interface DetallePedido extends BaseEntity {
  cantidad: number; // `Integer` en Java
  subTotal: number; // `Double` en Java, propiedad `subTotal` con 'T' mayúscula
  articulo: Articulo; // Objeto completo Articulo
}

/**
 * @interface PromocionDetalle
 * @description Detalle de una promoción.
 * Corresponde a la entidad `PromocionDetalle` de Java.
 */
export interface PromocionDetalle extends BaseEntity {
  cantidad: number; // `Integer` en Java
  articulo: Articulo; // Objeto completo Articulo
}

/**
 * @interface Promocion
 * @description Promocion Entity.
 * Corresponds to the `Promocion` Java entity.
 */
export interface Promocion extends BaseEntity {
  denominacion: string;
  fechaDesde: string; // `LocalDate` en Java, "yyyy-MM-dd"
  fechaHasta: string; // `LocalDate` en Java, "yyyy-MM-dd"
  horaDesde: string; // `LocalTime` en Java, "HH:mm:ss"
  horaHasta: string; // `LocalTime` en Java, "HH:mm:ss"
  descripcionDescuento?: string | null; // `String` en Java, puede ser nulo
  precioPromocional: number; // `Double` en Java
  estadoActivo: boolean; // `Boolean` en Java
  imagenes: Imagen[]; // Lista de objetos Imagen
  detallesPromocion: PromocionDetalle[]; // Lista de objetos PromocionDetalle
}

/**
 * @interface Sucursal
 * @description Branch/Store Entity.
 * Corresponds to the `Sucursal` Java entity.
 */
export interface Sucursal extends BaseEntity {
  nombre: string;
  horarioApertura: string; // `LocalTime` en Java, "HH:mm:ss"
  horarioCierre: string; // `LocalTime` en Java, "HH:mm:ss"
  empresa: Empresa; // Objeto completo Empresa
  domicilio: Domicilio; // Objeto completo Domicilio
  fechaBaja?: string | null; // `LocalDate` en Java, 'YYYY-MM-DD' o null
  estadoActivo: boolean; // `Boolean` en Java
  categorias?: Categoria[]; // Lista de Categoria objects
  promociones?: Promocion[]; // Lista de Promocion objects
}

/**
 * @interface Pedido
 * @description Order Entity.
 * Corresponds to the `Pedido` Java entity.
 */
export interface Pedido extends BaseEntity {
  horaEstimadaFinalizacion: string; // `LocalTime` en Java, "HH:mm:ss"
  total: number; // `Double` en Java
  totalCosto?: number | null; // `Double` en Java, puede ser nulo
  fechaPedido: string; // `LocalDate` en Java, "yyyy-MM-dd"
  sucursal: Sucursal; // Objeto completo Sucursal
  domicilio: Domicilio; // Objeto completo Domicilio
  factura?: Factura | null; // Objeto completo Factura (definido abajo), can be null
  estado: Estado; // Using the `Estado` union type
  tipoEnvio: TipoEnvio; // Using the `TipoEnvio` union type
  formaPago: FormaPago; // Using the `FormaPago` union type
  cliente: Cliente; // Full Cliente object
  detalles: DetallePedido[]; // List of DetallePedido objects
  fechaBaja?: string | null; // `LocalDate` in Java, 'YYYY-MM-DD' o null
  estadoActivo: boolean; // `Boolean` in Java
  mercadoPagoPaymentId?: string | null; // `String` en Java, puede ser nulo
  mercadoPagoPreferenceId?: string | null; // `String` en Java, puede ser nulo
  mercadoPagoPaymentStatus?: string | null; // `String` en Java, puede ser nulo
  descuentoAplicado?: number | null; // `Double` en Java, puede ser nulo
}

/**
 * @interface Factura
 * @description Invoice Entity.
 * Corresponds to the `Factura` Java entity.
 */
export interface Factura extends BaseEntity {
  fechaFacturacion: string; // `LocalDate` in Java, "yyyy-MM-dd"
  mpPaymentId?: number | null;
  mpMerchantOrderId?: number | null;
  mpPreferenceId?: string | null;
  mpPaymentType?: string | null;
  totalVenta: number;
  // Para evitar circular dependency, aquí se usa PedidoSimpleResponseDTO o solo el ID si el backend lo permite
  pedido: PedidoSimpleResponseDTO; // Changed to simple DTO to avoid circular dependency
  formaPago: FormaPago; // Usando el union type
  estadoFactura: EstadoFactura; // Usando el union type
  fechaAnulacion?: string | null; // `LocalDate` in Java, 'YYYY-MM-DD' o null
}


// ==============================================================
// --- REQUEST DTOs (para enviar al backend) ---
// Estas interfaces son la representación de los datos que el frontend envía al backend.
// El orden de declaración es crucial debido a las dependencias.
// ==============================================================

/**
 * @interface AddItemToCartRequestDTO
 * @description DTO para añadir un ítem al carrito en el backend.
 * Corresponde a `com.powerRanger.ElBuenSabor.dtos.AddItemToCartRequestDTO`.
 */
export interface AddItemToCartRequestDTO extends BaseEntity { // Add base entity
  articuloId: number; // `Integer` en Java
  cantidad: number; // `Integer` en Java
}

/**
 * @interface ArticuloInsumoRequestDTO
 * @description DTO para crear o actualizar un artículo insumo.
 * Corresponde a `com.powerRanger.ElBuenSabor.dtos.ArticuloInsumoRequestDTO`.
 */
export interface ArticuloInsumoRequestDTO extends BaseEntity { // Add base entity
  denominacion: string;
  precioVenta: number;
  unidadMedidaId: number; // `Integer` en Java
  categoriaId: number; // `Integer` en Java
  estadoActivo: boolean;
  precioCompra?: number | null; // `Double` en Java, puede ser nulo
  stockActual: number; // `Double` en Java
  stockMinimo?: number | null; // `Double` en Java, puede ser nulo
  esParaElaborar: boolean;
  imagenIds?: number[]; // If the backend expects IDs of existing images
}

/**
 * @interface ArticuloManufacturadoDetalleRequestDTO
 * @description DTO para los detalles de un artículo manufacturado dentro de `ArticuloManufacturadoRequestDTO`.
 * Corresponde a `com.powerRanger.ElBuenSabor.dtos.ArticuloManufacturadoDetalleDTO`.
 */
export interface ArticuloManufacturadoDetalleRequestDTO extends BaseEntity { // Add base entity
  cantidad: number; // `Double` en Java (en tu DTO es Double)
  articuloInsumoId: number; // `Integer` en Java
  estadoActivo: boolean; // `Boolean` en Java
}

/**
 * @interface ArticuloManufacturadoRequestDTO
 * @description DTO para crear o actualizar un artículo manufacturado.
 * Corresponde a `com.powerRanger.ElBuenSabor.dtos.ArticuloManufacturadoRequestDTO`.
 */
export interface ArticuloManufacturadoRequestDTO extends BaseEntity { // Add base entity
  denominacion: string;
  precioVenta: number;
  unidadMedidaId: number; // `Integer` en Java
  categoriaId: number; // `Integer` en Java
  estadoActivo: boolean;
  descripcion: string;
  tiempoEstimadoMinutos: number; // `Integer` en Java
  preparacion: string;
  manufacturadoDetalles: ArticuloManufacturadoDetalleRequestDTO[]; // List of DTOs
  imagenIds?: number[]; // If the backend expects IDs of existing images
}

/**
 * @interface ClienteRequestDTO
 * @description DTO for creating or updating a client.
 * Corresponds to `com.powerRanger.ElBuenSabor.dtos.ClienteRequestDTO`.
 */
export interface ClienteRequestDTO extends BaseEntity { // Add base entity
  nombre: string;
  apellido: string;
  telefono: string;
  email: string;
  fechaNacimiento?: string | null; // `LocalDate` en Java, 'YYYY-MM-DD' o null
  usuarioId: number; // `Integer` en Java
  domicilioIds: number[]; // List of associated domicile IDs
  estadoActivo: boolean;
  imagenId?: number | null; // `Integer` en Java, ID of existing image or null
}

/**
 * @interface CrearPedidoRequestDTO
 * @description DTO for creating an order, including new domicile information if created.
 * Corresponds to `com.powerRanger.ElBuenSabor.dtos.CrearPedidoRequestDTO`.
 * (Note: It's different from `PedidoRequestDTO` which uses existing domicile IDs).
 */
export interface CrearPedidoRequestDTO {
    calleDomicilio: string;
    numeroDomicilio: number;
    cpDomicilio: string;
    localidadIdDomicilio: number;
    tipoEnvio: TipoEnvio;
    formaPago: FormaPago;
    sucursalId: number;
    horaEstimadaFinalizacion?: string; // Es opcional en algunas de tus lógicas
    notasAdicionales?: string | null;
    guardarDireccionEnPerfil?: boolean;
}

/**
 * @interface DetallePedidoRequestDTO
 * @description DTO for sending order item details to the backend.
 * Corresponds to `com.powerRanger.ElBuenSabor.dtos.DetallePedidoRequestDTO`.
 */
export interface DetallePedidoRequestDTO extends BaseEntity { // Add base entity
  articuloId: number; // `Integer` en Java
  cantidad: number; // `Integer` en Java
}

/**
 * @interface DomicilioRequestDTO
 * @description DTO for creating or updating an address.
 * Corresponds to `com.powerRanger.ElBuenSabor.dtos.DomicilioRequestDTO`.
 */
export interface DomicilioRequestDTO extends BaseEntity { // Add base entity
  calle: string;
  numero: number; // `Integer` en Java
  cp: string; // `String` en Java
  localidadId: number; // `Integer` en Java
}

/**
 * @interface EmpresaRequestDTO
 * @description DTO for creating or updating a company.
 * Corresponds to `com.powerRanger.ElBuenSabor.dtos.EmpresaRequestDTO`.
 */
export interface EmpresaRequestDTO extends BaseEntity { // Add base entity
  nombre: string;
  razonSocial: string;
  cuil: string;
}

/**
 * @interface FacturaCreateRequestDTO
 * @description DTO for creating an invoice from an existing order.
 * Corresponds to `com.powerRanger.ElBuenSabor.dtos.FacturaCreateRequestDTO`.
 */
export interface FacturaCreateRequestDTO extends BaseEntity { // Add base entity
  pedidoId: number; // `Integer` en Java
  mpPaymentId?: number | null;
  mpMerchantOrderId?: number | null;
  mpPreferenceId?: string | null;
  mpPaymentType?: string | null;
}

/**
 * @interface ImagenRequestDTO
 * @description DTO for creating a new image entity in the database.
 * Corresponds to `com.powerRanger.ElBuenSabor.dtos.ImagenRequestDTO`.
 */
export interface ImagenRequestDTO extends BaseEntity { // Add base entity
  denominacion: string; // The image URL
  articuloId?: number | null; // `Integer` in Java, can be null
  promocionId?: number | null; // `Integer` in Java, can be null
  estadoActivo?: boolean; // `Boolean` in Java (default to true in backend)
}

/**
 * @interface MercadoPagoCreatePreferenceDTO
 * @description DTO para enviar al backend para crear una preferencia de Mercado Pago.
 * Corresponds to `com.powerRanger.ElBuenSabor.dtos.MercadoPagoCreatePreferenceDTO`.
 */
export interface MercadoPagoCreatePreferenceDTO {
  pedidoId: number;
}


/**
 * @interface PedidoEstadoRequestDTO
 * @description DTO for updating the status of an order.
 * Corresponds to `com.powerRanger.ElBuenSabor.dtos.PedidoEstadoRequestDTO`.
 */
export interface PedidoEstadoRequestDTO extends BaseEntity { // Add base entity
  nuevoEstado: Estado; // Using the `Estado` union type
}

/**
 * @interface PedidoRequestDTO
 * @description DTO for an Order request, using IDs for relationships.
 * Corresponds to `com.powerRanger.ElBuenSabor.dtos.PedidoRequestDTO`.
 * This is the object the frontend will send to the backend to create an order.
 */
export interface PedidoRequestDTO extends BaseEntity { // Add base entity
  horaEstimadaFinalizacion: string; // `String` in Java (expects "HH:mm:ss" or "HH:mm")
  sucursalId: number; // `Integer` en Java
  domicilioId: number; // `Integer` en Java
  tipoEnvio: TipoEnvio; // Using the `TipoEnvio` union type
  formaPago: FormaPago; // Using the `FormaPago` union type
  clienteId: number; // `Integer` en Java
  detalles: DetallePedidoRequestDTO[]; // List of `DetallePedidoRequestDTO`
}

/**
 * @interface PromocionDetalleRequestDTO
 * @description DTO for promotion details within `PromocionRequestDTO`.
 * Corresponds to `com.powerRanger.ElBuenSabor.dtos.PromocionDetalleRequestDTO`.
 */
export interface PromocionDetalleRequestDTO extends BaseEntity { // Add base entity
  cantidad: number; // `Integer` en Java
  articuloId: number; // `Integer` en Java
}

/**
 * @interface PromocionRequestDTO
 * @description DTO for creating or updating a promotion.
 * Corresponds to `com.powerRanger.ElBuenSabor.dtos.PromocionRequestDTO`.
 */
export interface PromocionRequestDTO extends BaseEntity { // Add base entity
  denominacion: string;
  fechaDesde: string; // `LocalDate` in Java, mapped to `string` "yyyy-MM-dd"
  fechaHasta: string; // `LocalDate` in Java, mapped to `string` "yyyy-MM-dd"
  horaDesde: string; // `LocalTime` in Java, mapped to `string` "HH:mm:ss"
  horaHasta: string; // `LocalTime` in Java, mapped to `string` "HH:mm:ss"
  descripcionDescuento?: string | null; // `String` in Java, can be null
  precioPromocional: number; // `Double` in Java
  imagenIds?: number[]; // List of IDs of existing Images to associate
  detallesPromocion: PromocionDetalleRequestDTO[]; // List of DTOs
  estadoActivo: boolean;
  sucursalIds?: number[]; // If the backend expects IDs of associated branches
}

/**
 * @interface SucursalRequestDTO
 * @description DTO for creating or updating a branch.
 * Corresponds to `com.powerRanger.ElBuenSabor.dtos.SucursalRequestDTO`.
 */
export interface SucursalRequestDTO extends BaseEntity { // Add base entity
  nombre: string;
  horarioApertura: string; // `String` in Java (HH:mm)
  horarioCierre: string; // `String` in Java (HH:mm)
  empresaId: number; // `Integer` en Java
  domicilio: DomicilioRequestDTO; // DTO object
  promocionIds?: number[]; // List of IDs
  categoriaIds?: number[]; // List of IDs
  estadoActivo: boolean;
}

/**
 * @interface UpdateCartItemQuantityRequestDTO
 * @description DTO for updating the quantity of an item in the cart in the backend.
 * Corresponds to `com.powerRanger.ElBuenSabor.dtos.UpdateCartItemQuantityRequestDTO`.
 */
export interface UpdateCartItemQuantityRequestDTO extends BaseEntity { // Add base entity
  nuevaCantidad: number; // `Integer` en Java
}

/**
 * @interface UsuarioRequestDTO
 * @description DTO for creating or updating a user.
 * Corresponds to `com.powerRanger.ElBuenSabor.dtos.UsuarioRequestDTO`.
 */
export interface UsuarioRequestDTO extends BaseEntity { // Add base entity
  id?: number; // For updates
  auth0Id: string;
  username: string;
  rol: Rol; // Using the Rol type defined above
  estadoActivo: boolean;
}


// ==============================================================
// --- RESPONSE DTOs (for receiving from the backend) ---
// ==============================================================

/**
 * @interface ArticuloSimpleResponseDTO
 * @description DTO for a basic article response.
 * Corresponds to `com.powerRanger.ElBuenSabor.dtos.ArticuloSimpleResponseDTO`.
 */
export interface ArticuloSimpleResponseDTO extends BaseEntity {
  denominacion: string;
  precioVenta: number;
}

/**
 * @interface UnidadMedidaResponseDTO
 * @description DTO for a unit of measure response.
 * Corresponds to `com.powerRanger.ElBuenSabor.dtos.UnidadMedidaResponseDTO`.
 */
export interface UnidadMedidaResponseDTO extends BaseEntity {
  denominacion: string;
}

/**
 * @interface ImagenResponseDTO
 * @description DTO for an image response.
 * Corresponds to `com.powerRanger.ElBuenSabor.dtos.ImagenResponseDTO`.
 */
export interface ImagenResponseDTO extends BaseEntity {
  denominacion: string;
  estadoActivo: boolean;
  articuloId?: number | null;
  articuloDenominacion?: string | null;
  promocionId?: number | null;
  promocionDenominacion?: string | null;
}

/**
 * @interface CategoriaResponseDTO
 * @description DTO for a category response.
 * Corresponds to `com.powerRanger.ElBuenSabor.dtos.CategoriaResponseDTO`.
 */
export interface CategoriaResponseDTO extends BaseEntity {
  denominacion: string;
  estadoActivo: boolean;
}

/**
 * @interface ArticuloBaseResponseDTO
 * @description Base DTO for representing an article in the backend response.
 * Corresponds to the `Articulo` superclass when returned by the API.
 * `@JsonTypeInfo` and `@JsonSubTypes` are used in Java for polymorphic serialization.
 */
export interface ArticuloBaseResponseDTO extends BaseEntity {
  denominacion: string;
  precioVenta: number;
  unidadMedida: UnidadMedidaResponseDTO; // Full DTO
  imagenes: ImagenResponseDTO[]; // List of Image DTOs
  categoria: CategoriaResponseDTO; // Full DTO
  estadoActivo: boolean;
  type?: 'insumo' | 'manufacturado'; // Discriminator property if the backend includes it
}

/**
 * @interface ArticuloInsumoResponseDTO
 * @description DTO for representing a supply article in the backend response.
 * Corresponds to `com.powerRanger.ElBuenSabor.dtos.ArticuloInsumoResponseDTO`.
 * Extends `ArticuloBaseResponseDTO`.
 */
export interface ArticuloInsumoResponseDTO extends ArticuloBaseResponseDTO {
  precioCompra?: number | null;
  stockActual: number;
  stockMinimo?: number | null;
  esParaElaborar: boolean;
  type?: 'insumo'; // Discriminator if `ArticuloBaseResponseDTO` has it
}

/**
 * @interface ArticuloManufacturadoDetalleResponseDTO
 * @description DTO for representing a manufactured article detail in the response.
 * Corresponds to `com.powerRanger.ElBuenSabor.dtos.ArticuloManufacturadoDetalleResponseDTO`.
 */
export interface ArticuloManufacturadoDetalleResponseDTO extends BaseEntity {
  cantidad: number;
  articuloInsumo: ArticuloSimpleResponseDTO; // Uses the simple DTO
  estadoActivo?: boolean; // May be optional in the response DTO
}

/**
 * @interface ArticuloManufacturadoResponseDTO
 * @description DTO for representing a manufactured article in the backend response.
 * Corresponds to `com.powerRanger.ElBuenSabor.dtos.ArticuloManufacturadoResponseDTO`.
 * Extends `ArticuloBaseResponseDTO`.
 */
export interface ArticuloManufacturadoResponseDTO extends ArticuloBaseResponseDTO {
  descripcion: string;
  tiempoEstimadoMinutos: number;
  preparacion: string;
  manufacturadoDetalles: ArticuloManufacturadoDetalleResponseDTO[];
  unidadesDisponiblesCalculadas?: number; // Calculated field
  type?: 'manufacturado'; // Discriminator if `ArticuloBaseResponseDTO` has it
}

// Union of article response DTOs
export type ArticuloResponseDTO = ArticuloInsumoResponseDTO | ArticuloManufacturadoResponseDTO;

/**
 * @interface ArticuloManufacturadoRankingDTO
 * @description DTO for manufactured article ranking.
 * Corresponds to `com.powerRanger.ElBuenSabor.dtos.ArticuloManufacturadoRankingDTO`.
 */
export interface ArticuloManufacturadoRankingDTO extends BaseEntity {
  articuloId: number;
  denominacion: string;
  cantidadVendida: number; // `Long` in Java, mapped to `number`
}

/**
 * @interface CarritoItemResponseDTO
 * @description DTO for an individual item within a CarritoResponseDTO.
 * Corresponds to `com.powerRanger.ElBuenSabor.dtos.CarritoItemResponseDTO`.
 */
export interface CarritoItemResponseDTO extends BaseEntity {
  articuloId: number;
  articuloDenominacion: string;
  cantidad: number;
  precioUnitarioAlAgregar: number;
  subtotalItem: number; // `Double` in Java, with 't' lowercase
}

/**
 * @interface CarritoResponseDTO
 * @description DTO for the complete shopping cart response.
 * Corresponds to `com.powerRanger.ElBuenSabor.dtos.CarritoResponseDTO`.
 */
export interface CarritoResponseDTO extends BaseEntity {
  clienteId: number;
  fechaCreacion: string; // `LocalDateTime` in Java, mapped to `string`
  fechaUltimaModificacion: string; // `LocalDateTime` in Java, mapped to `string`
  items: CarritoItemResponseDTO[];
  totalCarrito: number;
}

/**
 * @interface ClienteRankingDTO
 * @description DTO for client ranking.
 * Corresponds to `com.powerRanger.ElBuenSabor.dtos.ClienteRankingDTO`.
 */
export interface ClienteRankingDTO extends BaseEntity {
  clienteId: number;
  nombreCompleto: string;
  email: string;
  cantidadPedidos: number;
  montoTotalComprado: number;
}

/**
 * @interface PaisResponseDTO
 * @description DTO for a country response.
 * Corresponds to `com.powerRanger.ElBuenSabor.dtos.PaisResponseDTO`.
 */
export interface PaisResponseDTO extends BaseEntity {
  nombre: string;
}

/**
 * @interface ProvinciaResponseDTO
 * @description DTO for a province response.
 * Corresponds to `com.powerRanger.ElBuenSabor.dtos.ProvinciaResponseDTO`.
 */
export interface ProvinciaResponseDTO extends BaseEntity {
  nombre: string;
  pais: PaisResponseDTO; // Full DTO
}

/**
 * @interface LocalidadResponseDTO
 * @description DTO for a locality response.
 * Corresponds to `com.powerRanger.ElBuenSabor.dtos.LocalidadResponseDTO`.
 */
export interface LocalidadResponseDTO extends BaseEntity {
  nombre: string;
  provincia: ProvinciaResponseDTO; // Full DTO
}

/**
 * @interface DomicilioResponseDTO
 * @description DTO for an address response.
 * Corresponds to `com.powerRanger.ElBuenSabor.dtos.DomicilioResponseDTO`.
 */
export interface DomicilioResponseDTO extends BaseEntity {
  calle: string;
  numero: number;
  cp: string;
  localidad: LocalidadResponseDTO; // Full DTO
}

/**
 * @interface EmpresaResponseDTO
 * @description DTO for a company response.
 * Corresponds to `com.powerRanger.ElBuenSabor.dtos.EmpresaResponseDTO`.
 */
export interface EmpresaResponseDTO extends BaseEntity {
  nombre: string;
  razonSocial: string;
  cuil: string;
}

/**
 * @interface ClienteResponseDTO
 * @description DTO for a client response.
 * Corresponds to `com.powerRanger.ElBuenSabor.dtos.ClienteResponseDTO`.
 */
export interface ClienteResponseDTO extends BaseEntity {
  nombre: string;
  apellido: string;
  telefono: string;
  email: string;
  fechaNacimiento?: string | null;
  estadoActivo: boolean;
  fechaBaja?: string | null;
  usuarioId: number;
  username: string;
  rolUsuario: Rol;
  domicilios: DomicilioResponseDTO[];
}

/**
 * @interface PromocionSimpleResponseDTO
 * @description DTO for a simplified promotion response.
 * Corresponds to `com.powerRanger.ElBuenSabor.dtos.PromocionSimpleResponseDTO`.
 */
export interface PromocionSimpleResponseDTO extends BaseEntity {
  denominacion: string;
}

/**
 * @interface SucursalResponseDTO
 * @description DTO for a branch response.
 * Corresponds to `com.powerRanger.ElBuenSabor.dtos.SucursalResponseDTO`.
 */
export interface SucursalResponseDTO extends BaseEntity {
  nombre: string;
  horarioApertura: string;
  horarioCierre: string;
  estadoActivo: boolean;
  fechaBaja?: string | null;
  empresa: EmpresaResponseDTO; // Full DTO
  domicilio: DomicilioResponseDTO; // Full DTO
  categorias?: CategoriaResponseDTO[];
  promociones?: PromocionSimpleResponseDTO[]; // List of simple DTOs
}

/**
 * @interface DetallePedidoResponseDTO
 * @description DTO for an order detail response.
 * Corresponds to `com.powerRanger.ElBuenSabor.dtos.DetallePedidoResponseDTO`.
 */
export interface DetallePedidoResponseDTO extends BaseEntity {
  cantidad: number;
  subTotal: number;
  articulo: ArticuloSimpleResponseDTO; // Simple DTO
}

/**
 * @interface PedidoSimpleResponseDTO
 * @description DTO for a simplified order response.
 * Corresponds to `com.powerRanger.ElBuenSabor.dtos.PedidoSimpleResponseDTO`.
 */
export interface PedidoSimpleResponseDTO extends BaseEntity {
  fechaPedido: string;
}

/**
 * @interface FacturaDetalleResponseDTO
 * @description DTO for an invoice detail.
 * Corresponds to `com.powerRanger.ElBuenSabor.dtos.FacturaDetalleResponseDTO`.
 */
export interface FacturaDetalleResponseDTO extends BaseEntity {
  cantidad: number;
  denominacionArticulo: string;
  precioUnitarioArticulo: number;
  subTotal: number;
  articulo: ArticuloSimpleResponseDTO; // Simple DTO
}

/**
 * @interface FacturaResponseDTO
 * @description DTO for an invoice response.
 * Corresponds to `com.powerRanger.ElBuenSabor.dtos.FacturaResponseDTO`.
 */
export interface FacturaResponseDTO extends BaseEntity {
  fechaFacturacion: string;
  mpPaymentId?: number | null;
  mpMerchantOrderId?: number | null;
  mpPreferenceId?: string | null;
  mpPaymentType?: string | null;
  totalVenta: number;
  pedido: PedidoSimpleResponseDTO; // Simple DTO
  formaPago: FormaPago;
  estadoFactura: EstadoFactura;
  fechaAnulacion?: string | null;
  detallesFactura: FacturaDetalleResponseDTO[]; // List of DTOs
}

/**
 * @interface PromocionDetalleResponseDTO
 * @description DTO for a promotion detail in the response.
 * Corresponds to `com.powerRanger.ElBuenSabor.dtos.PromocionDetalleResponseDTO`.
 */
export interface PromocionDetalleResponseDTO extends BaseEntity {
  cantidad: number;
  articulo: ArticuloSimpleResponseDTO; // Simple DTO
}

/**
 * @interface PromocionResponseDTO
 * @description DTO for a promotion response.
 * Corresponds to `com.powerRanger.ElBuenSabor.dtos.PromocionResponseDTO`.
 */
export interface PromocionResponseDTO extends BaseEntity {
  denominacion: string;
  fechaDesde: string;
  fechaHasta: string;
  horaDesde: string;
  horaHasta: string;
  descripcionDescuento?: string | null;
  precioPromocional: number;
  estadoActivo: boolean;
  imagenes: ImagenResponseDTO[];
  detallesPromocion: PromocionDetalleResponseDTO[];
}

/**
 * @interface UnidadMedidaResponseDTO
 * @description DTO for a unit of measure response.
 * Corresponds to `com.powerRanger.ElBuenSabor.dtos.UnidadMedidaResponseDTO`.
 */
export interface UnidadMedidaResponseDTO extends BaseEntity {
  denominacion: string;
}

/**
 * @interface UsuarioResponseDTO
 * @description DTO for a user response.
 * Corresponds to `com.powerRanger.ElBuenSabor.dtos.UsuarioResponseDTO`.
 */
export interface UsuarioResponseDTO extends BaseEntity {
  username: string;
  rol: Rol;
  estadoActivo: boolean;
  fechaBaja?: string | null;
}

/**
 * @interface PedidoResponseDTO
 * @description DTO for an order response.
 * Corresponds to `com.powerRanger.ElBuenSabor.dtos.PedidoResponseDTO`.
 */
export interface PedidoResponseDTO extends BaseEntity {
  horaEstimadaFinalizacion: string;
  total: number;
  subTotalPedido?: number | null;
  descuentoAplicado?: number | null;
  totalCosto?: number | null;
  fechaPedido: string;
  estado: Estado;
  tipoEnvio: TipoEnvio;
  formaPago: FormaPago;
  estadoActivo: boolean;
  fechaBaja?: string | null;
  sucursal: SucursalResponseDTO; // Full DTO
  domicilio: DomicilioResponseDTO; // Full DTO
  cliente: ClienteResponseDTO; // Full DTO
  detalles: DetallePedidoResponseDTO[];
  mercadoPagoPaymentId?: string | null;
  mpPreferenceId?: string | null; // <--- NOMBRE CORREGIDO
  mercadoPagoPaymentStatus?: string | null;
}

// ==============================================================
// --- FRONTEND-SPECIFIC TYPES (Do not map directly to backend entities/DTOs) ---
// These types are for internal frontend logic (e.g., shopping cart structure).
// ==============================================================

/**
 * @interface CartItem
 * @description Represents an item in the frontend shopping cart.
 * Uses the `Articulo` type (ArticuloManufacturado | ArticuloInsumo) and quantity.
 */
export interface CartItem {
  id: number;
  articulo: Articulo; // Can be ArticuloManufacturado or ArticuloInsumo
  quantity: number;
}

/**
 * @interface PreferenceMP
 * @description Represents the response from creating a Mercado Pago preference.
 * Contains the preference ID needed to redirect the user.
 */
export interface PreferenceMP {
  id: string; // This is the preference ID returned by Mercado Pago
  // If your backend directly returns `init_point`, add it here
  // init_point?: string;
}
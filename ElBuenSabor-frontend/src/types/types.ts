/**
 * @file types.ts
 * @description Este archivo centraliza todas las definiciones de interfaces (modelos de datos) y tipos
 * utilizados en el frontend de la aplicación. Estos tipos reflejan la estructura de las entidades
 * y DTOs (Data Transfer Objects) que se intercambian con el backend de Spring Boot,
 * facilitando la tipificación fuerte y la validación en el desarrollo.
 *
 * Se agrupan los tipos por su dominio lógico (Entidades, DTOs, Enums, etc.) para mejorar la legibilidad.
 */

// ==============================================================
// --- Modelos de Entidades Base (reflejan las clases del backend) ---
// ==============================================================

/**
 * @interface Imagen
 * @description Representa una imagen asociada a otras entidades.
 * Corresponde a la entidad `Imagen` en el backend.
 */
export interface Imagen {
  id: number;
  denominacion: string; // URL o nombre del archivo en el sistema de almacenamiento
  estadoActivo: boolean;
  // articuloId?: number;  // Si el backend serializa el ID del artículo asociado, podría ser opcional.
  // promocionId?: number; // Si el backend serializa el ID de la promoción asociada, podría ser opcional.
}

/**
 * @interface UnidadMedida
 * @description Representa una unidad de medida para artículos.
 * Corresponde a la entidad `UnidadMedida` en el backend.
 */
export interface UnidadMedida {
  id: number;
  denominacion: string;
}

/**
 * @interface Categoria
 * @description Representa una categoría de artículos (ej. "Bebidas", "Hamburguesas").
 * Corresponde a la entidad `Categoria` en el backend.
 */
export interface Categoria {
  id: number;
  denominacion: string;
  estadoActivo: boolean;
}

/**
 * @interface ArticuloManufacturadoDetalle
 * @description Representa un detalle de un artículo manufacturado, indicando la cantidad
 * de un insumo específico necesario.
 * Corresponde a la entidad `ArticuloManufacturadoDetalle` en el backend.
 */
export interface ArticuloManufacturadoDetalle {
  id: number;
  cantidad: number;
  // Solo necesitamos el ID del artículo insumo para referencia y evitar bucles
  articuloInsumo: { id: number };
  estadoActivo: boolean;
}

/**
 * @interface Articulo
 * @description Interfaz base que define las propiedades comunes a `ArticuloManufacturado` y `ArticuloInsumo`.
 * Corresponde a la superclase `Articulo` en el backend.
 */
export interface Articulo {
  id: number;
  denominacion: string;
  precioVenta: number;
  // Se tipifica solo lo necesario para evitar bucles o datos excesivos
  unidadMedida: { id: number; denominacion: string };
  imagenes: Imagen[]; // Lista de imágenes asociadas
  categoria: { id: number; denominacion: string };
  estadoActivo: boolean;
}

/**
 * @interface ArticuloManufacturado
 * @description Representa un artículo que se elabora a partir de insumos (ej. una hamburguesa).
 * Extiende `Articulo`. Corresponde a la entidad `ArticuloManufacturado` en el backend.
 */
export interface ArticuloManufacturado extends Articulo {
  descripcion: string;
  tiempoEstimadoMinutos: number;
  preparacion: string;
  manufacturadoDetalles: ArticuloManufacturadoDetalle[];
  unidadesDisponiblesCalculadas?: number; 
}

/**
 * @interface ArticuloInsumo
 * @description Representa un artículo que es un insumo o materia prima (ej. "Tomate", "Pan").
 * Extiende `Articulo`. Corresponde a la entidad `ArticuloInsumo` en el backend.
 */
export interface ArticuloInsumo extends Articulo {
  precioCompra: number;
  stockActual: number;
  stockMinimo: number;
  esParaElaborar: boolean;
}

/**
 * @interface Domicilio
 * @description Representa una dirección física.
 * Corresponde a la entidad `Domicilio` en el backend.
 */
export interface Domicilio {
  id: number;
  calle: string;
  numero: number;
  cp: string; // Código Postal
  // La localidad incluye provincia y país para una tipificación completa
  localidad: {
    id: number;
    nombre: string;
    provincia: {
      id: number;
      nombre: string;
      pais: { id: number; nombre: string };
    };
  };
  // clientes?: any[]; // Si se necesita, pero cuidado con bucles de serialización
}

/**
 * @interface Sucursal
 * @description Representa una sucursal o punto de venta de la empresa.
 * Corresponde a la entidad `Sucursal` en el backend.
 */
export interface Sucursal {
  id: number;
  nombre: string;
  horarioApertura: string; // Formato HH:mm
  horarioCierre: string; // Formato HH:mm
  empresa: { id: number; nombre: string }; // Solo campos necesarios de Empresa
  domicilio: Domicilio; // Se usa la interfaz Domicilio completa
  estadoActivo: boolean;
  // tiempoEstimadoMinutos?: number; // Si el backend lo provee por sucursal
}

/**
 * @interface Usuario
 * @description Representa un usuario del sistema, gestionado por Auth0 y con un rol.
 * Corresponde a la entidad `Usuario` en el backend.
 */
export interface Usuario {
  id: number;
  auth0Id: string;
  username: string;
  rol: 'ADMIN' | 'EMPLEADO' | 'CLIENTE'; // Tipos literales para el enum Rol
  estadoActivo: boolean;
}

/**
 * @interface Cliente
 * @description Representa un cliente, asociado a un usuario.
 * Corresponde a la entidad `Cliente` en el backend.
 */
export interface Cliente {
  id: number;
  nombre: string;
  apellido: string;
  telefono: string;
  email: string;
  fechaNacimiento?: string | null; // `LocalDate` en backend, aquí como string 'YYYY-MM-DD' o null
  domicilios: Domicilio[];
  // Solo el ID y auth0Id del usuario para referencia
  usuario: { id: number; auth0Id: string };
  estadoActivo: boolean;
  // imagen?: Imagen; // Opcional, si el cliente tiene una imagen asociada directamente
}

/**
 * @interface DetallePedido
 * @description Representa un ítem dentro de un pedido, con la cantidad y el artículo asociado.
 * Corresponde a la entidad `DetallePedido` en el backend.
 */
export interface DetallePedido {
  id: number;
  cantidad: number;
  subTotal: number;
  articulo: Articulo; // Puede ser ArticuloManufacturado o ArticuloInsumo en realidad
  // pedido?: { id: number }; // Evitar bucles, solo el ID si se necesita la referencia
}

/**
 * @interface Pedido
 * @description Representa un pedido realizado por un cliente.
 * Corresponde a la entidad `Pedido` en el backend.
 */
export interface Pedido {
  id: number;
  horaEstimadaFinalizacion: string; // `LocalTime` en backend, aquí como string 'HH:mm:ss' o 'HH:mm'
  total: number;
  totalCosto: number | null; // `Double` en backend, puede ser null
  fechaPedido: string; // `LocalDate` en backend, aquí como string 'YYYY-MM-DD'
  sucursal: { id: number; nombre: string }; // Solo campos necesarios de Sucursal
  domicilio: Domicilio; // Se usa la interfaz Domicilio completa
  factura: { id: number } | null; // Solo el ID de la factura, o null si no hay factura
  estado: EstadoPedido; // Enum del backend
  tipoEnvio: TipoEnvio; // Enum del backend
  formaPago: FormaPago; // Enum del backend
  cliente: { id: number; nombre: string; apellido: string }; // Solo campos necesarios de Cliente
  detalles: DetallePedido[];
  fechaBaja: string | null; // `LocalDate` en backend, aquí como string 'YYYY-MM-DD' o null
  estadoActivo: boolean;
}

/**
 * @interface Pais
 * @description Representa un país.
 * Corresponde a la entidad `Pais` en el backend.
 */
export interface Pais {
  id: number;
  nombre: string;
  // provincias?: Provincia[]; // Opcional, si necesitas la lista de provincias dentro de un país
}

/**
 * @interface Provincia
 * @description Representa una provincia dentro de un país.
 * Corresponde a la entidad `Provincia` en el backend.
 */
export interface Provincia {
  id: number;
  nombre: string;
  pais: { id: number; nombre: string }; // Referencia simplificada al país para evitar bucles
  // localidades?: Localidad[]; // Opcional, si necesitas la lista de localidades dentro de una provincia
}

/**
 * @interface Localidad
 * @description Representa una localidad dentro de una provincia.
 * Corresponde a la entidad `Localidad` en el backend.
 */
export interface Localidad {
  id: number;
  nombre: string;
  // Referencia completa a la provincia (y su país) para facilitar el acceso a la jerarquía
  provincia: {
    id: number;
    nombre: string;
    pais: { id: number; nombre: string };
  };
  // domicilios?: Domicilio[]; // Opcional
}

// ==============================================================
// --- Tipos de Enum (reflejan los enums del backend) ---
// ==============================================================

/**
 * @type TipoEnvio
 * @description Tipo de envío para un pedido.
 * Corresponde al `enum TipoEnvio` en el backend.
 */
export type TipoEnvio = 'DELIVERY' | 'TAKEAWAY';

/**
 * @type FormaPago
 * @description Forma de pago de un pedido.
 * Corresponde al `enum FormaPago` en el backend.
 */
export type FormaPago = 'EFECTIVO' | 'MERCADO_PAGO';

/**
 * @type EstadoPedido
 * @description Estados posibles de un pedido.
 * Corresponde al `enum EstadoPedido` en el backend.
 */
export type EstadoPedido = 'PREPARACION' | 'PENDIENTE' | 'CANCELADO' | 'RECHAZADO' | 'ENTREGADO';

/**
 * @type Rol
 * @description Roles de usuario en el sistema.
 * Corresponde al `enum Rol` en el backend.
 */
export type Rol = 'ADMIN' | 'EMPLEADO' | 'CLIENTE';

// ==============================================================
// --- DTOs (Data Transfer Objects) para enviar al backend ---
// Son interfaces para los cuerpos de las solicitudes POST/PUT
// ==============================================================

/**
 * @interface DetallePedidoRequestDTO
 * @description DTO para enviar los detalles de un ítem de pedido al backend.
 */
export interface DetallePedidoRequestDTO {
  articuloId: number;
  cantidad: number;
}

/**
 * @interface PedidoRequestDTO
 * @description DTO para crear o actualizar un pedido.
 * Corresponde al DTO `PedidoRequestDTO` en el backend.
 */
export interface PedidoRequestDTO {
  horaEstimadaFinalizacion: string; // Formato HH:mm:ss o HH:mm
  sucursalId: number;
  domicilioId: number;
  tipoEnvio: TipoEnvio;
  formaPago: FormaPago;
  clienteId: number; // El ID del cliente que realiza el pedido
  detalles: DetallePedidoRequestDTO[];
}

/**
 * @interface ArticuloManufacturadoRequestDTO
 * @description DTO para crear o actualizar un artículo manufacturado.
 * Corresponde al DTO `ArticuloManufacturadoRequestDTO` en el backend.
 */
export interface ArticuloManufacturadoRequestDTO {
  denominacion: string;
  precioVenta: number;
  unidadMedidaId: number;
  categoriaId: number;
  estadoActivo: boolean;
  descripcion: string;
  tiempoEstimadoMinutos: number;
  preparacion: string;
  manufacturadoDetalles: ArticuloManufacturadoDetalleDTO[];
}

/**
 * @interface ArticuloManufacturadoDetalleDTO
 * @description DTO para los detalles de un artículo manufacturado dentro de `ArticuloManufacturadoRequestDTO`.
 * Corresponde al DTO `ArticuloManufacturadoDetalleDTO` en el backend.
 */
export interface ArticuloManufacturadoDetalleDTO {
  cantidad: number;
  articuloInsumoId: number;
  estadoActivo: boolean;
}

/**
 * @interface ImagenRequestDTO
 * @description DTO para crear una nueva entidad de imagen en la base de datos.
 * Corresponde al DTO `ImagenRequestDTO` en el backend.
 */
export interface ImagenRequestDTO {
  denominacion: string; // URL o nombre del archivo
  articuloId?: number; // Opcional: ID del Artículo asociado
  promocionId?: number; // Opcional: ID de la Promoción asociada
  estadoActivo?: boolean; // Default a true en el backend
}

/**
 * @interface PromocionRequestDTO
 * @description DTO para crear o actualizar una promoción.
 * **NOTA: Revisa si este DTO es realmente utilizado en el frontend para enviar datos de promociones.**
 * Si no hay lógica de creación/edición de promociones, este tipo podría ser innecesario.
 */
export interface PromocionRequestDTO {
  id?: number; // Opcional para crear nuevas promociones
  denominacion: string; // Nombre de la promoción
  descuento: number; // Descuento en porcentaje (0-100)
  fechaInicio: string; // Fecha de inicio en formato 'YYYY-MM-DD'
  fechaFin: string; // Fecha de fin en formato 'YYYY-MM-DD'
  horaInicio: string; // Hora de inicio en formato 'HH:mm:ss'
  horaFin: string; // Hora de fin en formato 'HH:mm:ss'
  estadoActivo: boolean; // Default a true en el backend
  articulos: Articulo[]; // Lista de artículos asociados a la promoción
}

/**
 * @interface UsuarioRequestDTO
 * @description DTO para crear o actualizar un usuario.
 * Corresponde al DTO `UsuarioRequestDTO` en el backend.
 */
export interface UsuarioRequestDTO {
  auth0Id: string;
  username: string;
  rol: Rol; // Usamos el tipo Rol definido arriba
  estadoActivo: boolean;
}

/**
 * @interface ClienteRequestDTO
 * @description DTO para crear o actualizar un cliente.
 * Corresponde al DTO `ClienteRequestDTO` en el backend.
 */
export interface ClienteRequestDTO {
  nombre: string;
  apellido: string;
  telefono: string;
  email: string;
  fechaNacimiento?: string | null; // Formato 'YYYY-MM-DD' o null
  usuarioId: number; // ID del usuario asociado al cliente
  domicilioIds: number[]; // Lista de IDs de domicilios asociados
  estadoActivo: boolean;
  // imagenId?: number; // Si vas a gestionar la imagen del cliente a través de su ID aquí
}

/**
 * @interface DomicilioRequestDTO
 * @description DTO para crear o actualizar un domicilio.
 * Corresponde al DTO `DomicilioRequestDTO` en el backend.
 */
export interface DomicilioRequestDTO {
  calle: string;
  numero: number;
  cp: string; // Código Postal
  localidadId: number; // ID de la localidad a la que pertenece
}

/**
 * @interface ArticuloInsumoRequestDTO
 * @description DTO para crear o actualizar un artículo insumo.
 * Corresponde al DTO `ArticuloInsumoRequestDTO` en el backend.
 */
export interface ArticuloInsumoRequestDTO {
  denominacion: string;
  precioVenta: number;
  unidadMedidaId: number;
  categoriaId: number;
  estadoActivo: boolean;
  precioCompra?: number | null; // Hacer opcional o permitir null
  stockActual: number;
  stockMinimo?: number | null; // Hacer opcional o permitir null
  esParaElaborar: boolean;
  // imagenIds?: number[]; // Opcional, si decides manejar la asociación de imágenes existentes por ID
}
/**
 * @interface ArticuloBaseResponseDTO
 * @description DTO para representar un artículo en la respuesta del backend.
 * Corresponde al DTO `ArticuloBaseResponseDTO` en el backend.
 */
export interface ArticuloBaseResponseDTO {
  id: number;
  denominacion: string;
  precioVenta: number;
  estadoActivo: boolean;
  unidadMedida: { id: number; denominacion: string }; // Asumiendo que estos son objetos simples
  categoria: { id: number; denominacion: string };  // Asumiendo que estos son objetos simples
  imagenes: Imagen[];
  type: 'insumo' | 'manufacturado'; // Propiedad discriminadora
}

/**
 * @interface ArticuloInsumoResponseDTO
 * @description DTO para representar un artículo insumo en la respuesta del backend.
 * Corresponde al DTO `ArticuloInsumoResponseDTO` en el backend.
 */
export interface ArticuloInsumoResponseDTO extends ArticuloBaseResponseDTO {
  type: 'insumo'; // Para el discriminador si ArticuloBaseResponseDTO lo usa
  precioCompra?: number | null; // Hacer opcionales o null si pueden no venir
  stockActual: number;
  stockMinimo?: number | null;
  esParaElaborar: boolean;
  // Otros campos específicos si los tuviera
}
/**
 * @interface ArticuloManufacturadoResponseDTO
 * @description DTO para representar un artikel manufacturado en la respuesta del backend.
 * Corresponde al DTO `ArticuloManufacturadoResponseDTO` en el backend.
 */
export interface ArticuloManufacturadoDetalleResponseDTO { // Asumiendo que necesitas esto
  id: number;
  cantidad: number;
  articuloInsumo: { id: number, denominacion: string, precioVenta: number }; // ArticuloSimpleResponseDTO
  estadoActivo: boolean;
}

// ==============================================================
// --- Otros Tipos Específicos del Frontend ---
// ==============================================================

/**
 * @interface CartItem
 * @description Representa un ítem en el carrito de compras del frontend.
 */
export interface CartItem {
  articulo: ArticuloManufacturado | ArticuloInsumo;
  quantity: number;
}

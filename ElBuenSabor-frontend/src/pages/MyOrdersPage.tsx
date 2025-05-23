/**
 * @file MyOrdersPage.tsx
 * @description Página que muestra el historial de pedidos de un cliente autenticado.
 * Permite a los usuarios ver un listado de sus pedidos anteriores, incluyendo detalles
 * como el total, tipo de envío, forma de pago, sucursal, domicilio (si aplica),
 * y el estado actual del pedido. Cada pedido se visualiza con sus artículos y la opción
 * de ver la factura asociada (si existe).
 *
 * @hook `useAuth0`: Para gestionar la autenticación del usuario, obtener el ID del usuario y el token de acceso.
 * @hook `useState`: Gestiona los pedidos del usuario, estados de carga/error.
 * @hook `useEffect`: Carga los pedidos del cliente al montar el componente o al cambiar el estado de autenticación.
 * @hook `Link` de `react-router-dom`: Permite la navegación a otras páginas (ej. menú, facturas).
 *
 * @service `getPedidosByClienteAuth0Id`: Servicio para obtener los pedidos de un cliente específico.
 * @service `getImageUrl`: Función de utilidad para construir las URLs completas de las imágenes de los artículos.
 */
import React, { useEffect, useState } from 'react';
import { Container, Row, Col, Card, ListGroup, Spinner, Alert, Button, Image } from 'react-bootstrap';
import { useAuth0 } from '@auth0/auth0-react';
import { getPedidosByClienteAuth0Id } from '../services/pedidoService';
import { getImageUrl } from '../services/fileUploadService';
import type { Pedido, EstadoPedido } from '../types/types'; // Importa los tipos necesarios
import { FontAwesomeIcon } from '@fortawesome/react-fontawesome';
import {
  faHistory, // Icono para el título de la página
  faCheckCircle, // Icono para estado ENTREGADO
  faTimesCircle, // Icono para estados CANCELADO / RECHAZADO
  faClock, // Icono para estado PENDIENTE
  faCog, // Icono para estado PREPARACION (engranaje)
  faTruck, // Icono para tipo de envío DELIVERY
  faStore, // Icono para tipo de envío TAKEAWAY
  faMoneyBillWave, // Icono para forma de pago EFECTIVO
  faCreditCard, // Icono para forma de pago MERCADO_PAGO
} from '@fortawesome/free-solid-svg-icons';
import { Link } from 'react-router-dom'; // Asegúrate de importar useNavigate también

/**
 * @interface MyOrdersPageProps
 * @description No se requieren propiedades (`props`) para este componente de página,
 * por lo que se define una interfaz vacía para claridad.
 */
interface MyOrdersPageProps {}

const MyOrdersPage: React.FC<MyOrdersPageProps> = () => {
  /**
   * @hook useAuth0
   * @description Hook para acceder al estado de autenticación de Auth0, información del usuario
   * y función para obtener el token de acceso.
   */
  const { isAuthenticated, user, getAccessTokenSilently, isLoading: authLoading } = useAuth0();

  /**
   * @state pedidos
   * @description Almacena el array de objetos `Pedido` obtenidos del backend para el cliente autenticado.
   */
  const [pedidos, setPedidos] = useState<Pedido[]>([]);

  /**
   * @state loading
   * @description Estado booleano para indicar si los pedidos están cargando.
   */
  const [loading, setLoading] = useState(true);

  /**
   * @state error
   * @description Almacena un mensaje de error si ocurre un problema durante la carga de pedidos.
   */
  const [error, setError] = useState<string | null>(null);

  /**
   * @hook useEffect
   * @description Hook principal para la carga de pedidos del cliente.
   * Se ejecuta al montar el componente y cada vez que el estado de autenticación de Auth0 cambia.
   * Se encarga de:
   * 1. Esperar a que Auth0 cargue.
   * 2. Verificar la autenticación y la disponibilidad del `auth0Id` del usuario.
   * 3. Obtener el token de acceso.
   * 4. Llamar al servicio `getPedidosByClienteAuth0Id` para obtener los pedidos.
   * 5. Manejar los estados de carga y error.
   */
  useEffect(() => {
    const fetchMyOrders = async () => {
      // Esperar a que Auth0 termine de cargar antes de proceder.
      if (authLoading) {
        setLoading(true); // Muestra el spinner mientras Auth0 carga
        return;
      }

      setLoading(true); // Inicia el spinner de carga
      setError(null); // Limpia cualquier error anterior

      // Si el usuario no está autenticado o su ID de Auth0 no está disponible, muestra un error.
      if (!isAuthenticated || !user?.sub) {
        setError('Debes iniciar sesión para ver tus pedidos. Por favor, inicia sesión para continuar.');
        setLoading(false);
        return;
      }

      try {
        // Obtener el token de acceso de Auth0 para autenticar la petición al backend.
        const token = await getAccessTokenSilently({
          authorizationParams: {
            audience: import.meta.env.VITE_AUTH0_AUDIENCE,
            scope: import.meta.env.VITE_AUTH0_SCOPE,
          },
        });
        // Llama al servicio para obtener los pedidos del cliente usando su Auth0 ID.
        const fetchedPedidos = await getPedidosByClienteAuth0Id(user.sub, token);
        setPedidos(fetchedPedidos);
      } catch (err) {
        console.error('Error al obtener mis pedidos:', err);
        // Intenta extraer un mensaje de error más específico de la respuesta del backend.
        const errorMessage = (err as any).response?.data?.message || (err as any).message || 'Error desconocido al cargar los pedidos.';
        setError(`No se pudieron cargar tus pedidos: ${errorMessage}.`);
      } finally {
        setLoading(false); // Detiene el spinner de carga
      }
    };

    fetchMyOrders();
  }, [isAuthenticated, user, authLoading, getAccessTokenSilently]); // Dependencias del efecto

  /**
   * @function getEstadoIcon
   * @description Función de utilidad que devuelve un icono de FontAwesome y un color
   * basados en el `EstadoPedido` del pedido.
   * @param {EstadoPedido} estado - El estado del pedido.
   * @returns {JSX.Element | null} Un elemento `FontAwesomeIcon` con su color correspondiente, o `null` si no hay un icono definido.
   */
  const getEstadoIcon = (estado: EstadoPedido) => {
    switch (estado) {
      case 'PENDIENTE':
        return <FontAwesomeIcon icon={faClock} className="text-warning" title="Pendiente" />;
      case 'PREPARACION':
        return <FontAwesomeIcon icon={faCog} className="text-info" title="En Preparación" />;
      case 'ENTREGADO':
        return <FontAwesomeIcon icon={faCheckCircle} className="text-success" title="Entregado" />;
      case 'CANCELADO':
      case 'RECHAZADO':
        return <FontAwesomeIcon icon={faTimesCircle} className="text-danger" title="Cancelado/Rechazado" />;
      default:
        return null;
    }
  };

  // --- Renderizado condicional basado en estados de carga o error ---
  // Muestra un spinner si los pedidos están cargando
  if (loading) {
    return (
      <Container className="text-center my-5">
        <Spinner animation="border" role="status" />
        <p className="mt-3">Cargando tus pedidos...</p>
      </Container>
    );
  }

  // Muestra un mensaje de error si ocurre un problema
  if (error) {
    return (
      <Container className="my-5 text-center">
        <Alert variant="danger">
          <Alert.Heading>¡Error al Cargar Pedidos!</Alert.Heading>
          <p>{error}</p>
          <hr />
          {/* Enlace para volver al inicio */}
          <Link to="/" style={{ textDecoration: 'none' }}>
            <Button variant="primary">Volver al Inicio</Button>
          </Link>
        </Alert>
      </Container>
    );
  }

  return (
    <Container className="my-4">
      {/* Título de la página con icono */}
      <h1 className="text-center mb-4">
        <FontAwesomeIcon icon={faHistory} className="me-2 text-primary" /> Mis Pedidos
      </h1>

      {/* Renderizado condicional: si no hay pedidos o si hay */}
      {pedidos.length === 0 ? (
        <Alert variant="info" className="text-center">
          No tienes pedidos registrados todavía. ¡Anímate a pedir algo delicioso!
          <div className="mt-3">
            {/* Enlace para ir a la página de productos */}
            <Link to="/products" style={{ textDecoration: 'none' }}>
              <Button variant="primary">Explorar Menú</Button>
            </Link>
          </div>
        </Alert>
      ) : (
        // Cuadrícula responsiva de tarjetas de pedido
        <Row xs={1} md={1} lg={2} className="g-4"> {/* `g-4` para espacio entre tarjetas */}
          {pedidos.map((pedido) => (
            <Col key={pedido.id}>
              <Card className="h-100 shadow-sm">
                <Card.Header className="d-flex justify-content-between align-items-center">
                  <div>
                    <h5 className="mb-0">Pedido #{pedido.id}</h5>
                    {/* Fecha y hora estimada de finalización del pedido */}
                    <small className="text-muted">{pedido.fechaPedido} - {pedido.horaEstimadaFinalizacion}</small>
                  </div>
                  <div>
                    {/* Badge con el estado del pedido, con colores condicionales */}
                    <span className={`badge bg-${pedido.estado === 'ENTREGADO' ? 'success' : pedido.estado === 'CANCELADO' || pedido.estado === 'RECHAZADO' ? 'danger' : 'warning'} me-2`}>
                      {pedido.estado}
                    </span>
                    {/* Icono del estado del pedido */}
                    {getEstadoIcon(pedido.estado)}
                  </div>
                </Card.Header>
                <Card.Body>
                  {/* Detalles principales del pedido */}
                  <Card.Text>
                    <strong>Total:</strong> <span className="text-success">${pedido.total.toFixed(2)}</span> <br />
                    <strong>Envío:</strong> {pedido.tipoEnvio === 'DELIVERY' ? <FontAwesomeIcon icon={faTruck} className="me-1" /> : <FontAwesomeIcon icon={faStore} className="me-1" />} {pedido.tipoEnvio} <br />
                    {/* Muestra el domicilio si es Delivery y existe */}
                    {pedido.tipoEnvio === 'DELIVERY' && pedido.domicilio && (
                      <small className="text-muted d-block ms-3">
                        ({pedido.domicilio.calle} {pedido.domicilio.numero}, {pedido.domicilio.localidad.nombre})
                      </small>
                    )}
                    <strong>Pago:</strong> {pedido.formaPago === 'EFECTIVO' ? <FontAwesomeIcon icon={faMoneyBillWave} className="me-1" /> : <FontAwesomeIcon icon={faCreditCard} className="me-1" />} {pedido.formaPago} <br />
                    <strong>Sucursal:</strong> {pedido.sucursal.nombre}
                  </Card.Text>
                  <hr />
                  <h6>Detalle de los Artículos:</h6>
                  <ListGroup variant="flush">
                    {/* Mapea los detalles de los artículos del pedido */}
                    {pedido.detalles.map((detalle) => (
                      <ListGroup.Item key={detalle.id} className="d-flex justify-content-between align-items-center py-1 px-0">
                        <div className="d-flex align-items-center">
                          {/* Imagen del artículo si está disponible */}
                          {detalle.articulo.imagenes && detalle.articulo.imagenes.length > 0 && (
                            <Image
                              src={getImageUrl(detalle.articulo.imagenes[0].denominacion)}
                              alt={detalle.articulo.denominacion}
                              style={{ width: '30px', height: '30px', objectFit: 'cover' }}
                              className="me-2 rounded"
                            />
                          )}
                          {detalle.cantidad} x {detalle.articulo.denominacion}
                        </div>
                        <small className="fw-bold">${detalle.subTotal.toFixed(2)}</small>
                      </ListGroup.Item>
                    ))}
                  </ListGroup>
                  {/* Botón para ver la factura si existe una factura asociada */}
                  {pedido.factura && (
                    <div className="mt-3 text-end">
                      <Link to={`/facturas/${pedido.factura.id}`} style={{ textDecoration: 'none' }}>
                        <Button variant="outline-info" size="sm">
                          Ver Factura
                        </Button>
                      </Link>
                    </div>
                  )}
                </Card.Body>
              </Card>
            </Col>
          ))}
        </Row>
      )}
    </Container>
  );
};

export default MyOrdersPage;
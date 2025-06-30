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
 * @service `PedidoService`: Servicio para obtener los pedidos de un cliente específico.
 * @service `FileUploadService`: Servicio de utilidad para construir las URLs completas de las imágenes de los artículos.
 */

import React, { useEffect, useState } from 'react';
import { Container, Row, Col, Card, ListGroup, Spinner, Alert, Button, Image, Badge } from 'react-bootstrap';
import { useAuth0 } from '@auth0/auth0-react';
import { Link } from 'react-router-dom';

// FIX: Usamos el servicio de forma estática
import { PedidoService } from '../services/PedidoService';
import apiClient from '../services/apiClient';

// FIX: Corregimos los tipos
import type { PedidoResponse, ArticuloSimpleResponse } from '../types/types';
import type { Estado } from '../types/enums';
import { FontAwesomeIcon } from '@fortawesome/react-fontawesome';
import { faHistory, faCheckCircle, faTimesCircle, faClock, faCog, faTruck, faStore, faMoneyBillWave, faCreditCard} from '@fortawesome/free-solid-svg-icons';

function articuloTieneImagenes(articulo: ArticuloSimpleResponse): articulo is ArticuloSimpleResponse & { imagenes: { denominacion: string }[] } {
    return (articulo as any).imagenes !== undefined && Array.isArray((articulo as any).imagenes);
}

const MyOrdersPage: React.FC = () => {
  const { isAuthenticated, getAccessTokenSilently, isLoading: authLoading } = useAuth0();
  const [pedidos, setPedidos] = useState<PedidoResponse[]>([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState<string | null>(null);

  // const fileUploadService = new FileUploadService(); 
  useEffect(() => {
    const fetchMyOrders = async () => {
      if (authLoading) return;

      if (!isAuthenticated) {
        setError('Debes iniciar sesión para ver tus pedidos.');
        setLoading(false);
        return;
      }
      
      setLoading(true);
      setError(null);

      try {
        const fetchedPedidos = await PedidoService.getMisPedidos();
        setPedidos(fetchedPedidos);
      } catch (err) {
        console.error('Error al obtener mis pedidos:', err);
        setError('No se pudieron cargar tus pedidos.');
      } finally {
        setLoading(false);
      }
    };

    fetchMyOrders();
  }, [isAuthenticated, authLoading, getAccessTokenSilently]);


  /**
   * @function getEstadoIcon
   * @description Función de utilidad que devuelve un icono de FontAwesome y un color
   * basados en el `Estado` del pedido.
   * @param {Estado} estado - El estado del pedido.
   * @returns {JSX.Element | null} Un elemento `FontAwesomeIcon` con su color correspondiente, o `null` si no hay un icono definido.
   */
  const getEstadoIcon = (estado: Estado) => { // Cambiado EstadoPedido a Estado.
    switch (estado) {
      case 'PENDIENTE':
        return <FontAwesomeIcon icon={faClock} className="text-warning" title="Pendiente" />;
      case 'PREPARACION':
        return <FontAwesomeIcon icon={faCog} className="text-info" title="En Preparación" />;
      case 'EN_CAMINO': // Añadir este estado si tu backend lo usa
        return <FontAwesomeIcon icon={faTruck} className="text-primary" title="En Camino" />;
      case 'ENTREGADO':
        return <FontAwesomeIcon icon={faCheckCircle} className="text-success" title="Entregado" />;
      case 'CANCELADO':
        return <FontAwesomeIcon icon={faTimesCircle} className="text-danger" title="Cancelado" />;
      case 'RECHAZADO':
        return <FontAwesomeIcon icon={faTimesCircle} className="text-danger" title="Rechazado" />;
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
      <h1 className="text-center mb-4">
        <FontAwesomeIcon icon={faHistory} className="me-2 text-primary" /> Mis Pedidos
      </h1>

      {pedidos.length === 0 ? (
        <Alert variant="info" className="text-center">
          No tienes pedidos registrados todavía.
          <div className="mt-3">
            <Link to="/products"><Button variant="primary">Explorar Menú</Button></Link>
          </div>
        </Alert>
      ) : (
        <Row xs={1} md={1} lg={2} className="g-4">
          {pedidos.map((pedido) => (
            <Col key={pedido.id}>
              <Card className="h-100 shadow-sm">
                <Card.Header className="d-flex justify-content-between align-items-center">
                  <div>
                    <h5 className="mb-0">Pedido #{pedido.id}</h5>
                    <small className="text-muted">{new Date(pedido.fechaPedido).toLocaleDateString()} - {pedido.horaEstimadaFinalizacion}</small>
                  </div>
                  <div className="text-end">
                    <Badge bg={
                      pedido.estado === 'ENTREGADO' ? 'success' :
                      pedido.estado === 'CANCELADO' || pedido.estado === 'RECHAZADO' ? 'danger' :
                      'secondary'
                    } className="me-2">{pedido.estado}</Badge>
                    {getEstadoIcon(pedido.estado)}
                  </div>
                </Card.Header>
                <Card.Body>
                  <Card.Text>
                    <strong>Total:</strong> <span className="text-success">${pedido.total.toFixed(2)}</span> <br />
                    <strong>Envío:</strong> <FontAwesomeIcon icon={pedido.tipoEnvio === 'DELIVERY' ? faTruck : faStore} className="me-1" /> {pedido.tipoEnvio} <br />
                    {pedido.tipoEnvio === 'DELIVERY' && pedido.domicilio && (
                      <small className="text-muted d-block ms-3">
                        ({pedido.domicilio.calle} {pedido.domicilio.numero}, {pedido.domicilio.localidad?.nombre ?? 'N/A'})
                      </small>
                    )}
                    <strong>Pago:</strong> <FontAwesomeIcon icon={pedido.formaPago === 'EFECTIVO' ? faMoneyBillWave : faCreditCard} className="me-1" /> {pedido.formaPago} <br />
                    <strong>Sucursal:</strong> {pedido.sucursal.nombre}
                  </Card.Text>
                  <hr />
                  <h6>Detalle de los Artículos:</h6>
                  <ListGroup variant="flush">
                    {pedido.detalles.map((detalle) => (
                      <ListGroup.Item key={detalle.id} className="d-flex justify-content-between align-items-center py-1 px-0">
                        <div className="d-flex align-items-center">
                           {articuloTieneImagenes(detalle.articulo) && detalle.articulo.imagenes.length > 0 && (
                            <Image
                              src={`${apiClient.defaults.baseURL}/files/download/${detalle.articulo.imagenes[0].denominacion}`}
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
                  {(pedido as any).factura && (
                    <div className="mt-3 text-end">
                      <Link to={`/facturas/${(pedido as any).factura.id}`}>
                        <Button variant="outline-info" size="sm">Ver Factura</Button>
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
// src/pages/CheckoutPage.tsx

/**
 * @file CheckoutPage.tsx
 * @description Página de finalización de compra (`checkout`).
 * Permite a los usuarios revisar su carrito, seleccionar una sucursal, un tipo de envío (Delivery/Take Away),
 * un domicilio de entrega (si es Delivery), y una forma de pago. Una vez que toda la información es válida,
 * el usuario puede realizar el pedido, el cual se envía al backend.
 * También maneja la carga inicial de datos del cliente y sucursales, y la gestión de errores.
 *
 * @hook `useAuth0`: Para la autenticación del usuario y la obtención del token de acceso.
 * @hook `useCart`: Para acceder al estado del carrito de compras y sus funciones.
 * @hook `useNavigate`: Para la navegación programática tras la finalización del pedido o errores.
 * @hook `useState`: Gestiona el estado de la información del cliente, sucursales, selecciones del formulario,
 * estados de carga/envío, y mensajes de error/éxito.
 * @hook `useEffect`: Carga los datos iniciales del cliente y sucursales, y maneja la inicialización
 * de selecciones de formulario.
 */
import React, { useEffect, useState } from 'react';
import { Container, Row, Col, Card, ListGroup, Button, Form, Spinner, Alert, Image } from 'react-bootstrap';
import { useAuth0 } from '@auth0/auth0-react';
import { useCart } from '../context/CartContext';
import { setAuthToken } from '../services/apiClient'; // Función para configurar el token en Axios

// Importamos las CLASES de servicio
import { ClienteUsuarioService } from '../services/clienteUsuarioService';
import { PedidoService } from '../services/pedidoService';
import { SucursalService } from '../services/sucursalService';
import { FileUploadService } from '../services/fileUploadService';

import type { Cliente, Sucursal, TipoEnvio, FormaPago, PedidoRequestDTO, DetallePedidoRequestDTO } from '../types/types';
import { FontAwesomeIcon } from '@fortawesome/react-fontawesome';
import { faTruck, faStore, faMoneyBillWave, faCreditCard } from '@fortawesome/free-solid-svg-icons';
import { useNavigate } from 'react-router-dom';

const CheckoutPage: React.FC = () => {
  const { isAuthenticated, user, getAccessTokenSilently, isLoading: authLoading } = useAuth0();
  const { cart, getCartTotal, clearCart } = useCart();
  const navigate = useNavigate();

  const [cliente, setCliente] = useState<Cliente | null>(null);
  const [sucursales, setSucursales] = useState<Sucursal[]>([]);
  const [selectedSucursalId, setSelectedSucursalId] = useState<number | ''>('');
  const [selectedDomicilioId, setSelectedDomicilioId] = useState<number | ''>('');
  const [tipoEnvio, setTipoEnvio] = useState<TipoEnvio>('DELIVERY');
  const [formaPago, setFormaPago] = useState<FormaPago>('EFECTIVO');
  const [loadingData, setLoadingData] = useState(true);
  const [submittingOrder, setSubmittingOrder] = useState(false);
  const [error, setError] = useState<string | null>(null);
  const [successMessage, setSuccessMessage] = useState<string | null>(null);

  const defaultImage = '/placeholder-food.png';

  // Instanciamos los servicios una vez en el componente
  const clienteUsuarioService = new ClienteUsuarioService();
  const pedidoService = new PedidoService();
  const sucursalService = new SucursalService();
  const fileUploadService = new FileUploadService();

  useEffect(() => {
    const loadCheckoutData = async () => {
      if (authLoading) {
        setLoadingData(true);
        return;
      }

      setLoadingData(true);
      setError(null);

      try {
        if (!isAuthenticated || !user?.sub) {
          setError('Debes iniciar sesión para finalizar tu compra. Redirigiendo...');
          setTimeout(() => navigate('/'), 2000);
          setLoadingData(false);
          return;
        }

        const token = await getAccessTokenSilently({
          authorizationParams: {
            audience: import.meta.env.VITE_AUTH0_AUDIENCE,
            scope: import.meta.env.VITE_AUTH0_SCOPE,
          },
        });
        setAuthToken(token);

        const [fetchedCliente, fetchedSucursales] = await Promise.all([
          clienteUsuarioService.getClienteByAuth0Id(user.sub, token),
          sucursalService.getSucursales(),
        ]);
        setCliente(fetchedCliente);
        setSucursales(fetchedSucursales);

        if (fetchedSucursales.length > 0) {
          // CORRECCIÓN 1: Usar el operador '!' para afirmar que .id no es undefined
          setSelectedSucursalId(fetchedSucursales[0].id!);
        }
        if (fetchedCliente.domicilios.length > 0) {
          // CORRECCIÓN 2: Usar el operador '!' para afirmar que .id no es undefined
          setSelectedDomicilioId(fetchedCliente.domicilios[0].id!);
        }

      } catch (err) {
        console.error('Error al cargar datos del checkout:', err);
        const errorMessage = (err as any).response?.data?.message || (err as any).message || 'Error desconocido al cargar.';
        setError(`Error al cargar tu información o las sucursales: ${errorMessage}.`);
        setTimeout(() => navigate('/'), 3000);
      } finally {
        setLoadingData(false);
      }
    };

    loadCheckoutData();
  }, [isAuthenticated, user, authLoading, getAccessTokenSilently, navigate]);

  const handlePlaceOrder = async () => {
    setSubmittingOrder(true);
    setError(null);
    setSuccessMessage(null);

    if (cart.length === 0) {
      setError('Tu carrito está vacío. Por favor, añade productos antes de finalizar la compra.');
      setSubmittingOrder(false);
      return;
    }
    if (!cliente || !cliente.id) {
      setError('Información del cliente no disponible. Intenta recargar la página o iniciar sesión.');
      setSubmittingOrder(false);
      return;
    }
    if (!selectedSucursalId) {
      setError('Debes seleccionar una sucursal para tu pedido.');
      setSubmittingOrder(false);
      return;
    }
    if (tipoEnvio === 'DELIVERY' && (!selectedDomicilioId || selectedDomicilioId === 0)) {
      setError('Debes seleccionar un domicilio de entrega para el envío a domicilio.');
      setSubmittingOrder(false);
      return;
    }

    const selectedSucursal = sucursales.find(s => s.id === selectedSucursalId);
    if (selectedSucursal) {
      const now = new Date();
      const currentTime = `${now.getHours().toString().padStart(2, '0')}:${now.getMinutes().toString().padStart(2, '0')}`;
      if (currentTime < selectedSucursal.horarioApertura || currentTime > selectedSucursal.horarioCierre) {
        setError(`La sucursal seleccionada (${selectedSucursal.nombre}) está cerrada. Horario: ${selectedSucursal.horarioApertura} - ${selectedSucursal.horarioCierre}.`);
        setSubmittingOrder(false);
        return;
      }
    } else {
      setError('Sucursal seleccionada no encontrada.');
      setSubmittingOrder(false);
      return;
    }

    const orderDetails: DetallePedidoRequestDTO[] = cart.map(item => ({
      // CORRECCIÓN 3: Usar el operador '!' para afirmar que .id no es undefined
      articuloId: item.articulo.id!,
      cantidad: item.quantity,
    }));

    const now = new Date();
    const estimatedTime = new Date(now.getTime() + (30 * 60 * 1000));
    const estimatedTimeString = `${estimatedTime.getHours().toString().padStart(2, '0')}:${estimatedTime.getMinutes().toString().padStart(2, '0')}:${estimatedTime.getSeconds().toString().padStart(2, '0')}`;

    const pedidoData: PedidoRequestDTO = {
      clienteId: cliente.id!, // Usar '!' para asegurar que cliente.id no es undefined
      sucursalId: selectedSucursalId as number,
      // CORRECCIÓN 4: Usar '!' para afirmar que .id no es undefined
      domicilioId: tipoEnvio === 'DELIVERY' ? (selectedDomicilioId as number) : (cliente.domicilios.length > 0 ? cliente.domicilios[0].id! : 0),
      tipoEnvio: tipoEnvio,
      formaPago: formaPago,
      horaEstimadaFinalizacion: estimatedTimeString,
      detalles: orderDetails,
    };

    try {
      const token = await getAccessTokenSilently();

      if (formaPago === 'MERCADO_PAGO') {
        const preferenceId = await pedidoService.createPreferenceMercadoPago(pedidoData, token);
        if (preferenceId) {
          window.open(`https://www.mercadopago.com.ar/checkout/v1/redirect?pref_id=${preferenceId}`, '_blank');
          setSuccessMessage(`¡Redirigiendo a Mercado Pago para finalizar tu pedido!`);
          clearCart();
          setTimeout(() => navigate('/mis-pedidos'), 3000);
        } else {
          setError("Error al generar la preferencia de Mercado Pago. Por favor, inténtalo de nuevo.");
        }
      } else {
        const newOrder = await pedidoService.createPedido(pedidoData, token);
        setSuccessMessage(`¡Tu pedido #${newOrder.id} ha sido realizado con éxito! Será pagado en efectivo al retirar/recibir.`);
        clearCart();
        setTimeout(() => {
          navigate('/mis-pedidos');
        }, 2000);
      }

    } catch (err: any) {
      console.error('Error al realizar el pedido:', err);
      const backendErrorMessage = err.response?.data?.message || err.message || 'Por favor, inténtalo de nuevo.';
      setError(`Error al realizar el pedido: ${backendErrorMessage}`);
    } finally {
      setSubmittingOrder(false);
    }
  };

  if (loadingData || authLoading) {
    return (
      <Container className="text-center my-5">
        <Spinner animation="border" />
        <p className="mt-3">Cargando información de tu compra...</p>
      </Container>
    );
  }

  if (error && !successMessage) {
    return (
      <Container className="my-5 text-center">
        <Alert variant="danger">
          <Alert.Heading>¡Error al Cargar la Página!</Alert.Heading>
          <p>{error}</p>
          <hr />
          <Button variant="primary" onClick={() => navigate('/')}>Volver al Menú Principal</Button>
        </Alert>
      </Container>
    );
  }

  if (cart.length === 0 && !successMessage && !submittingOrder) {
    navigate('/products');
    return null;
  }

  return (
    <Container className="my-4">
      <h1 className="text-center mb-4">Finalizar Compra</h1>

      {successMessage && <Alert variant="success" className="mb-4 text-center">{successMessage}</Alert>}

      <Row>
        <Col md={6}>
          <Card className="mb-4 shadow-sm">
            <Card.Header as="h5">Productos en tu Carrito</Card.Header>
            <ListGroup variant="flush">
              {cart.length === 0 ? (
                <ListGroup.Item className="text-center text-muted">El carrito está vacío.</ListGroup.Item>
              ) : (
                cart.map((item) => (
                  <ListGroup.Item key={item.articulo.id} className="d-flex justify-content-between align-items-center py-2">
                    <div className="d-flex align-items-center">
                      <Image
                        src={
                          item.articulo.imagenes && item.articulo.imagenes.length > 0
                            ? fileUploadService.getImageUrl(item.articulo.imagenes[0].denominacion)
                            : defaultImage
                        }
                        thumbnail
                        style={{ width: '50px', height: '50px', objectFit: 'cover' }}
                        className="me-2"
                        alt={`Imagen de ${item.articulo.denominacion}`}
                      />
                      {item.quantity} x {item.articulo.denominacion}
                    </div>
                    <strong>${(item.articulo.precioVenta * item.quantity).toFixed(2)}</strong>
                  </ListGroup.Item>
                ))
              )}
            </ListGroup>
            <Card.Footer className="d-flex justify-content-between align-items-center bg-light">
              <h5 className="mb-0">Total del Pedido:</h5>
              <h5 className="mb-0"><span className="text-success">${getCartTotal().toFixed(2)}</span></h5>
            </Card.Footer>
          </Card>
        </Col>

        <Col md={6}>
          <Card className="shadow-sm">
            <Card.Header as="h5">Detalles de Entrega y Pago</Card.Header>
            <Card.Body>
              <Form.Group className="mb-3">
                <Form.Label>Sucursal:</Form.Label>
                <Form.Select
                  value={selectedSucursalId}
                  onChange={(e) => setSelectedSucursalId(Number(e.target.value))}
                  disabled={sucursales.length === 0}
                  required
                >
                  <option value="">Selecciona una sucursal</option>
                  {sucursales.map((sucursal) => (
                    <option key={sucursal.id} value={sucursal.id}>
                      {sucursal.nombre} ({sucursal.domicilio.calle} {sucursal.domicilio.numero})
                    </option>
                  ))}
                </Form.Select>
                {sucursales.length === 0 && <Form.Text className="text-danger">No hay sucursales disponibles. Por favor, contacta al soporte.</Form.Text>}
              </Form.Group>

              <Form.Group className="mb-3">
                <Form.Label>Tipo de Envío:</Form.Label>
                <div>
                  <Form.Check
                    inline
                    type="radio"
                    label={<><FontAwesomeIcon icon={faTruck} className="me-1" /> Delivery</>}
                    name="tipoEnvio"
                    id="delivery"
                    value="DELIVERY"
                    checked={tipoEnvio === 'DELIVERY'}
                    onChange={() => setTipoEnvio('DELIVERY')}
                  />
                  <Form.Check
                    inline
                    type="radio"
                    label={<><FontAwesomeIcon icon={faStore} className="me-1" /> Take Away</>}
                    name="tipoEnvio"
                    id="takeaway"
                    value="TAKEAWAY"
                    checked={tipoEnvio === 'TAKEAWAY'}
                    onChange={() => setTipoEnvio('TAKEAWAY')}
                  />
                </div>
              </Form.Group>

              {tipoEnvio === 'DELIVERY' && (
                <Form.Group className="mb-3">
                  <Form.Label>Domicilio de Entrega:</Form.Label>
                  <Form.Select
                    value={selectedDomicilioId}
                    onChange={(e) => setSelectedDomicilioId(Number(e.target.value))}
                    disabled={!cliente || cliente.domicilios.length === 0}
                    required
                  >
                    <option value="">Selecciona un domicilio</option>
                    {cliente?.domicilios.map((domicilio) => (
                      // CORRECCIÓN 5: Usar el operador '!' para afirmar que .denominacion no es undefined
                      <option key={domicilio.id} value={domicilio.id}>
                        {domicilio.calle} {domicilio.numero}, {domicilio.localidad.denominacion!}
                      </option>
                    ))}
                  </Form.Select>
                  {(!cliente || cliente.domicilios.length === 0) && (
                    <Form.Text className="text-danger">
                      No tienes domicilios registrados para Delivery. Por favor, añade uno en tu perfil.
                      <Button variant="link" size="sm" onClick={() => navigate('/profile')}>Gestionar Domicilios</Button>
                    </Form.Text>
                  )}
                  {(cliente && cliente.domicilios.length > 0) && (
                    <Button variant="link" size="sm" onClick={() => navigate('/profile')}>Gestionar Domicilios Existentes</Button>
                  )}
                </Form.Group>
              )}

              <Form.Group className="mb-3">
                <Form.Label>Forma de Pago:</Form.Label>
                <div>
                  <Form.Check
                    inline
                    type="radio"
                    label={<><FontAwesomeIcon icon={faMoneyBillWave} className="me-1" /> Efectivo</>}
                    name="formaPago"
                    id="efectivo"
                    value="EFECTIVO"
                    checked={formaPago === 'EFECTIVO'}
                    onChange={() => setFormaPago('EFECTIVO')}
                  />
                  <Form.Check
                    inline
                    type="radio"
                    label={<><FontAwesomeIcon icon={faCreditCard} className="me-1" /> Mercado Pago</>}
                    name="formaPago"
                    id="mercadoPago"
                    value="MERCADO_PAGO"
                    checked={formaPago === 'MERCADO_PAGO'}
                    onChange={() => setFormaPago('MERCADO_PAGO')}
                  />
                </div>
              </Form.Group>

              {error && <Alert variant="danger" className="mt-3">{error}</Alert>}

              <Button
                variant="primary"
                onClick={handlePlaceOrder}
                disabled={submittingOrder || cart.length === 0 || !cliente || !selectedSucursalId || (tipoEnvio === 'DELIVERY' && (!selectedDomicilioId || selectedDomicilioId === 0))}
                className="w-100 mt-3"
              >
                {submittingOrder ? <Spinner as="span" animation="border" size="sm" role="status" aria-hidden="true" className="me-2" /> : ''}
                {submittingOrder ? 'Realizando Pedido...' : 'Realizar Pedido'}
              </Button>
            </Card.Body>
          </Card>
        </Col>
      </Row>
    </Container>
  );
};

export default CheckoutPage;
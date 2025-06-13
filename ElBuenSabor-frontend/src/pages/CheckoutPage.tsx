import React, { useEffect, useState } from 'react';
import { Container, Row, Col, Card, ListGroup, Button, Form, Spinner, Alert, Image } from 'react-bootstrap';
import { useAuth0 } from '@auth0/auth0-react';
import { useCart } from '../context/CartContext';
import { setAuthToken } from '../services/apiClient';

// Importamos las CLASES de servicio
import { ClienteUsuarioService } from '../services/clienteUsuarioService';
import { PedidoService } from '../services/pedidoService';
import { SucursalService } from '../services/sucursalService';
import { FileUploadService } from '../services/fileUploadService';

import type { Cliente, Sucursal, TipoEnvio, FormaPago, CrearPedidoRequestDTO, ArticuloManufacturado } from '../types/types';
import { FontAwesomeIcon } from '@fortawesome/react-fontawesome';
import { faTruck, faStore, faMoneyBillWave, faCreditCard } from '@fortawesome/free-solid-svg-icons';
import { useNavigate } from 'react-router-dom';

const CheckoutPage: React.FC = () => {
  const { isAuthenticated, user, getAccessTokenSilently, isLoading: authLoading } = useAuth0();
  // El 'cart' de useCart ahora es de tipo CartItem[]
  const { cart, getCartTotal, clearCart } = useCart();
  const navigate = useNavigate();

  const [cliente, setCliente] = useState<Cliente | null>(null);
  const [sucursales, setSucursales] = useState<Sucursal[]>([]);
  const [selectedSucursalId, setSelectedSucursalId] = useState<number | ''>('');
  const [selectedDomicilioId, setSelectedDomicilioId] = useState<number | ''>('');
  const [tipoEnvio, setTipoEnvio] = useState<TipoEnvio>('DELIVERY');
  const [formaPago, setFormaPago] = useState<FormaPago>('MERCADO_PAGO');
  const [loadingData, setLoadingData] = useState(true);
  const [submittingOrder, setSubmittingOrder] = useState(false);
  const [error, setError] = useState<string | null>(null);
  const [successMessage, setSuccessMessage] = useState<string | null>(null);
  const [preferenceId, setPreferenceId] = useState<string | null>(null);

  const defaultImage = '/placeholder-food.png';

  // Instancias de servicios
  const clienteUsuarioService = new ClienteUsuarioService();
  const pedidoService = new PedidoService();
  const sucursalService = new SucursalService();
  const fileUploadService = new FileUploadService();

  useEffect(() => {
    const loadCheckoutData = async () => {
      if (authLoading) return;
      setLoadingData(true);
      try {
        if (!isAuthenticated || !user?.sub) {
          navigate('/');
          return;
        }
        const token = await getAccessTokenSilently();
        setAuthToken(token);

        const [fetchedCliente, fetchedSucursales] = await Promise.all([
          clienteUsuarioService.getMyProfile(token),
          sucursalService.getSucursales(),
        ]);

        setCliente(fetchedCliente);
        setSucursales(fetchedSucursales);

        if (fetchedSucursales.length > 0) setSelectedSucursalId(fetchedSucursales[0].id!);
        if (fetchedCliente.domicilios.length > 0) setSelectedDomicilioId(fetchedCliente.domicilios[0].id!);

      } catch (err) {
        console.error('Error al cargar datos del checkout:', err);
        setError('No se pudo cargar la información para el checkout.');
      } finally {
        setLoadingData(false);
      }
    };
    loadCheckoutData();
  }, [isAuthenticated, user, authLoading]);

  useEffect(() => {
    if (preferenceId) {
      const publicKey = import.meta.env.VITE_MERCADOPAGO_PUBLIC_KEY;
      if (!publicKey) {
        setError("Error de configuración de pago.");
        return;
      }
      const mp = new window.MercadoPago(publicKey, { locale: 'es-AR' });

      const container = document.getElementById("wallet_container");
      if (container) container.innerHTML = "";

      mp.bricks().create("wallet", "wallet_container", {
        initialization: { preferenceId },
        customization: { texts: { valueProp: 'smart_option' } },
      });
    }
  }, [preferenceId]);

  const handleTipoEnvioChange = (nuevoTipoEnvio: TipoEnvio) => {
    setTipoEnvio(nuevoTipoEnvio);
    if (nuevoTipoEnvio === 'DELIVERY') {
      setFormaPago('MERCADO_PAGO');
    }
  };

  const handlePlaceOrder = async () => {
    setSubmittingOrder(true);
    setError(null);
    setSuccessMessage(null);
    setPreferenceId(null);

    const domicilioSeleccionado = cliente?.domicilios.find(d => d.id === selectedDomicilioId);

    // **CORRECCIÓN**: Ahora la validación usa `cart.length` porque `cart` es un array de nuevo
    if (cart.length === 0 || !cliente?.id || !selectedSucursalId || (tipoEnvio === 'DELIVERY' && !domicilioSeleccionado)) {
      setError('Por favor, completa todos los campos requeridos antes de continuar.');
      setSubmittingOrder(false);
      return;
    }

    // **CORRECCIÓN**: La lógica de tiempo estimado vuelve a funcionar
    let maxTiempoEstimado = 0;
    cart.forEach(item => {
      if ('tiempoEstimadoMinutos' in item.articulo) {
        const tiempo = (item.articulo as ArticuloManufacturado).tiempoEstimadoMinutos;
        if (tiempo > maxTiempoEstimado) {
          maxTiempoEstimado = tiempo;
        }
      }
    });

    if (tipoEnvio === 'DELIVERY') {
      maxTiempoEstimado += 10;
    }

    const now = new Date();
    const estimatedTime = new Date(now.getTime() + maxTiempoEstimado * 60000);
    const horaEstimadaFinalizacion = `${estimatedTime.getHours().toString().padStart(2, '0')}:${estimatedTime.getMinutes().toString().padStart(2, '0')}:${estimatedTime.getSeconds().toString().padStart(2, '0')}`;

    const pedidoData: CrearPedidoRequestDTO = {
      tipoEnvio,
      formaPago,
      sucursalId: selectedSucursalId as number,
      calleDomicilio: domicilioSeleccionado?.calle ?? 'N/A',
      numeroDomicilio: domicilioSeleccionado?.numero ?? 0,
      cpDomicilio: domicilioSeleccionado?.cp ?? 'N/A',
      localidadIdDomicilio: domicilioSeleccionado?.localidad.id ?? 0,
      horaEstimadaFinalizacion,
    };

    try {
      const token = await getAccessTokenSilently();
      const response = await pedidoService.crearPedidoDesdeCarrito(cliente.id, pedidoData, token);

      if (formaPago === 'MERCADO_PAGO' && response?.mercadoPagoPreferenceId) {
        setPreferenceId(response.mercadoPagoPreferenceId);
        setSuccessMessage(`Pedido #${response.pedido.id} generado. Por favor, completa el pago.`);
      } else {
        setSuccessMessage(`¡Tu pedido #${response.id} ha sido realizado con éxito!`);
        await clearCart();
        setTimeout(() => navigate('/mis-pedidos'), 3000);
      }
    } catch (err: any) {
      console.error('Error al realizar el pedido:', err);
      const backendErrorMessage = err.response?.data?.message || err.message || 'Error desconocido.';
      setError(`Error al realizar el pedido: ${backendErrorMessage}`);
    } finally {
      setSubmittingOrder(false);
    }
  };

  if (loadingData || authLoading) {
    return <Container className="text-center my-5"><Spinner animation="border" /></Container>;
  }

  if (error && !successMessage) {
    return <Container className="my-5"><Alert variant="danger">{error}</Alert></Container>;
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
                  <ListGroup.Item key={item.id} className="d-flex justify-content-between align-items-center py-2">
                    <div className="d-flex align-items-center">
                      {/* **CORRECCIÓN**: La lógica de imagen vuelve a funcionar */}
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
                    onChange={() => handleTipoEnvioChange('DELIVERY')}
                  />
                  <Form.Check
                    inline
                    type="radio"
                    label={<><FontAwesomeIcon icon={faStore} className="me-1" /> Take Away</>}
                    name="tipoEnvio"
                    id="takeaway"
                    value="TAKEAWAY"
                    checked={tipoEnvio === 'TAKEAWAY'}
                    onChange={() => handleTipoEnvioChange('TAKEAWAY')}
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
                    disabled={tipoEnvio === 'DELIVERY'}
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
              {!preferenceId ? (
                <Button
                  variant="primary"
                  onClick={handlePlaceOrder}
                  disabled={submittingOrder || cart.length === 0 || !cliente || !selectedSucursalId || (tipoEnvio === 'DELIVERY' && (!selectedDomicilioId))}
                  className="w-100 mt-3"
                >
                  {submittingOrder ? <Spinner as="span" animation="border" size="sm" role="status" aria-hidden="true" className="me-2" /> : ''}
                  {submittingOrder ? 'Procesando...' : 'Realizar Pedido'}
                </Button>
              ) : (
                <div id="wallet_container" className="mt-3 w-100 d-flex justify-content-center"></div>
              )}
            </Card.Body>
          </Card>
        </Col>
      </Row>
    </Container>
  );
};

export default CheckoutPage;
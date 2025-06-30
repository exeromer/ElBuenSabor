import React, { useEffect, useState } from 'react';
import { Container, Row, Col, Card, ListGroup, Button, Form, Spinner, Alert, Image } from 'react-bootstrap';
import { useAuth0 } from '@auth0/auth0-react';
import { useNavigate } from 'react-router-dom';

// Hooks de Contexto
import { useUser } from '../context/UserContext';
import { useCart } from '../context/CartContext';
import { useSucursal } from '../context/SucursalContext';

// Servicios
import apiClient, { setAuthToken } from '../services/apiClient';
import { PedidoService } from '../services/PedidoService';

// Tipos y Enums
import type { TipoEnvio, FormaPago } from '../types/enums';
import type { ArticuloManufacturadoResponse, PedidoResponse, PedidoRequest } from '../types/types';

// Iconos
import { FontAwesomeIcon } from '@fortawesome/react-fontawesome';
import { faTruck, faStore, faMoneyBillWave, faCreditCard } from '@fortawesome/free-solid-svg-icons';

declare global {
  interface Window {
    MercadoPago: any;
  }
}

const CheckoutPage: React.FC = () => {
  // --- CONTEXTOS ---
  const { getAccessTokenSilently, isLoading: authLoading } = useAuth0();
  const { cliente, isLoading: userLoading } = useUser();
  const { cart, subtotal, descuento, totalFinal, clearCart, aplicarDescuentosAdicionales } = useCart();
  const { selectedSucursal, loading: sucursalLoading } = useSucursal();
  const navigate = useNavigate();

  // --- ESTADOS LOCALES ---
  const [selectedDomicilioId, setSelectedDomicilioId] = useState<number | ''>('');
  const [tipoEnvio, setTipoEnvio] = useState<TipoEnvio>('DELIVERY');
  const [formaPago, setFormaPago] = useState<FormaPago>('MERCADO_PAGO');
  const [submittingOrder, setSubmittingOrder] = useState(false);
  const [error, setError] = useState<string | null>(null);
  const [successMessage, setSuccessMessage] = useState<string | null>(null);
  const [preferenceId, setPreferenceId] = useState<string | null>(null);

  const defaultImage = '/placeholder-food.png';

  // --- EFECTOS ---
  useEffect(() => {
    if (cliente && cliente.domicilios.length > 0 && selectedDomicilioId === '') {
      setSelectedDomicilioId(cliente.domicilios[0].id);
    }
  }, [cliente]);

  useEffect(() => {
    // Le pedimos al contexto que recalcule los totales con las opciones actuales
    aplicarDescuentosAdicionales(tipoEnvio, formaPago);
  }, [tipoEnvio, formaPago, aplicarDescuentosAdicionales]);


  /** useEffect(() => {
    let discount = 0;
    if (tipoEnvio === 'TAKEAWAY' && formaPago === 'EFECTIVO') {
      discount = totalPrice * 0.10;
    }
    setCalculatedDiscount(discount);
    setFinalTotal(totalPrice - discount);
  }, [tipoEnvio, formaPago, totalPrice]); */

  useEffect(() => {
    if (preferenceId) {
      const publicKey = import.meta.env.VITE_MERCADOPAGO_PUBLIC_KEY;
      if (!publicKey) {
        setError("Error de configuración de pago.");
        return;
      }
      const mp = new window.MercadoPago(publicKey, { locale: 'es-AR' });
      const bricksContainer = document.getElementById("wallet_container");
      if (bricksContainer) {
        mp.bricks().create("wallet", "wallet_container", {
          initialization: { preferenceId: preferenceId },
        });
      }
    }
  }, [preferenceId]);

    useEffect(() => {
    // Si no hay mensaje de éxito (es decir, no acabamos de completar un pedido)
    // y el carrito está vacío, redirigimos al menú.
    if (cart.length === 0 && !successMessage) {
      navigate('/products');
    }
  }, [cart, successMessage, navigate]);

  // --- MANEJADORES ---

  const handleTipoEnvioChange = (nuevoTipoEnvio: TipoEnvio) => {
    setTipoEnvio(nuevoTipoEnvio);
    if (nuevoTipoEnvio === 'TAKEAWAY') {
      setFormaPago('EFECTIVO');
    } else {
      setFormaPago('MERCADO_PAGO');
    }
  };

  const handlePlaceOrder = async () => {
    setSubmittingOrder(true);
    setError(null);

    if (cart.length === 0 || !cliente?.id || !selectedSucursal) {
      setError('Error: Faltan datos para procesar el pedido.');
      setSubmittingOrder(false);
      return;
    }

    const domicilioSeleccionado = cliente?.domicilios.find(d => d.id === selectedDomicilioId);
    if (tipoEnvio === 'DELIVERY' && !domicilioSeleccionado) {
      setError('Por favor, selecciona un domicilio de entrega.');
      setSubmittingOrder(false);
      return;
    }

    const maxTiempoEstimado = Math.max(0, ...cart.map(item => (item.articulo as ArticuloManufacturadoResponse).tiempoEstimadoMinutos || 0));
    const tiempoAdicional = tipoEnvio === 'DELIVERY' ? 10 : 0;
    const estimatedTime = new Date(new Date().getTime() + (maxTiempoEstimado + tiempoAdicional) * 60000);
    const horaEstimadaFinalizacion = estimatedTime.toTimeString().split(' ')[0];

    const pedidoData: PedidoRequest = {
      clienteId: cliente.id,
      sucursalId: selectedSucursal.id,
      tipoEnvio,
      formaPago,
      detalles: cart.map(item => ({
        articuloId: item.articulo.id,
        cantidad: item.quantity,
      })),
      horaEstimadaFinalizacion,
      ...(tipoEnvio === 'DELIVERY' && domicilioSeleccionado ? {
        // Si es Delivery, usamos el domicilio del cliente
        calleDomicilio: domicilioSeleccionado.calle,
        numeroDomicilio: domicilioSeleccionado.numero,
        cpDomicilio: domicilioSeleccionado.cp,
        localidadIdDomicilio: domicilioSeleccionado.localidad.id,
      } : {
        // Si es Take Away, usamos el domicilio de la SUCURSAL como placeholder
        calleDomicilio: selectedSucursal.domicilio.calle,
        numeroDomicilio: selectedSucursal.domicilio.numero,
        cpDomicilio: selectedSucursal.domicilio.cp,
        localidadIdDomicilio: selectedSucursal.domicilio.localidad.id,
      })
    };


    try {
      const token = await getAccessTokenSilently();
      setAuthToken(token);

      // FIX: Ahora pasamos el ID del cliente y el DTO correcto.
      const response: PedidoResponse = await PedidoService.create(cliente.id, pedidoData);

      // La lógica de respuesta (MP o Efectivo) se mantiene igual
      if (formaPago === 'MERCADO_PAGO' && response.mpPreferenceId) {
        setSuccessMessage(`Pedido #${response.id} generado. Por favor, completa el pago.`);
        setPreferenceId(response.mpPreferenceId);
      } else {
        setSuccessMessage(`¡Tu pedido #${response.id} ha sido realizado con éxito!`);
        clearCart();
        setTimeout(() => navigate('/mis-pedidos'), 3000);
      }
    } catch (err: any) {
      const errorMsg = err.response?.data?.message || err.message || 'Error desconocido.';
      setError(`Error al realizar el pedido: ${errorMsg}`);
    } finally {
      setSubmittingOrder(false);
    }
  };

  const isLoading = authLoading || userLoading || sucursalLoading;

  if (isLoading) {
    return <Container className="text-center my-5"><Spinner animation="border" /></Container>;
  }

  if (!selectedSucursal) {
    return <Container className="my-5"><Alert variant="warning">Por favor, selecciona una sucursal para continuar.</Alert></Container>;
  }

  /** if (cart.length === 0 && !successMessage) {
    navigate('/products');
    return null;
  } */


  return (
    <Container className="my-4">
      <h1 className="text-center mb-4">Finalizar Compra</h1>
      {successMessage && <Alert variant="success" className="mb-4 text-center">{successMessage}</Alert>}
      <Row>
        <Col md={6}>
          <Card className="mb-4 shadow-sm">
            <Card.Header as="h5">Resumen Del Pedido</Card.Header>
            <ListGroup variant="flush">
              {cart.length === 0 ? (
                <ListGroup.Item className="text-center text-muted">El carrito está vacío.</ListGroup.Item>
              ) : (
                cart.map((item) => (
                  <ListGroup.Item key={item.id} className="d-flex justify-content-between align-items-center py-2">
                    <div className="d-flex align-items-center">
                      <Image
                        // FIX: Construimos la URL de la imagen correctamente
                        src={item.articulo.imagenes?.[0] ? `${apiClient.defaults.baseURL}/files/download/${item.articulo.imagenes[0].denominacion}` : defaultImage}
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
            <Card.Footer className="bg-light">
              <div className="d-flex justify-content-between">
                <h5 className="mb-0">Subtotal:</h5>
                {/* Usamos el subtotal bruto del contexto */}
                <h5 className="mb-0"><span className="text-dark">${subtotal.toFixed(2)}</span></h5>
              </div>

              {/* Usamos el descuento total del contexto */}
              {descuento > 0 && (
                <div className="d-flex justify-content-between text-danger">
                  <h5 className="mb-0">Descuentos:</h5>
                  <h5 className="mb-0">-${descuento.toFixed(2)}</h5>
                </div>
              )}
              <hr />
              <div className="d-flex justify-content-between align-items-center mt-2">
                <h5 className="mb-0">Total a Pagar:</h5>
                {/* Usamos el total final del contexto */}
                <h5 className="mb-0"><span className="text-success">${totalFinal.toFixed(2)}</span></h5>
              </div>
            </Card.Footer>
          </Card>
        </Col>
        <Col md={6}>
          <Card className="shadow-sm">
            <Card.Header as="h5">Detalles de Entrega y Pago</Card.Header>
            <Card.Body>
              {/* FIX: Reemplazamos el selector de sucursal por un campo de solo lectura */}
              <Form.Group className="mb-3">
                <Form.Label>Pedido para la Sucursal:</Form.Label>
                <Form.Control
                  type="text"
                  readOnly
                  disabled
                  value={selectedSucursal ? `${selectedSucursal.nombre} (${selectedSucursal.domicilio.calle} ${selectedSucursal.domicilio.numero})` : 'No seleccionada'}
                />
                <Form.Text className="text-muted">
                  La sucursal se selecciona desde el menú principal.
                </Form.Text>
              </Form.Group>
              <Form.Group className="mb-3">
                <Form.Label>Tipo de Envío:</Form.Label>
                <div>
                  <Form.Check inline type="radio" label={<><FontAwesomeIcon icon={faTruck} className="me-1" /> Delivery</>} name="tipoEnvio" id="delivery" value="DELIVERY" checked={tipoEnvio === 'DELIVERY'} onChange={() => handleTipoEnvioChange('DELIVERY')} />
                  <Form.Check inline type="radio" label={<><FontAwesomeIcon icon={faStore} className="me-1" /> Take Away</>} name="tipoEnvio" id="takeaway" value="TAKEAWAY" checked={tipoEnvio === 'TAKEAWAY'} onChange={() => handleTipoEnvioChange('TAKEAWAY')} />
                </div>
              </Form.Group>

              {tipoEnvio === 'DELIVERY' && (
                <Form.Group className="mb-3">
                  <Form.Label>Domicilio de Entrega:</Form.Label>
                  <Form.Select value={selectedDomicilioId} onChange={(e) => setSelectedDomicilioId(Number(e.target.value))} disabled={!cliente || cliente.domicilios.length === 0} required>
                    <option value="">Selecciona un domicilio</option>
                    {cliente?.domicilios.map((domicilio) => (
                      <option key={domicilio.id} value={domicilio.id}>
                        {domicilio.calle} {domicilio.numero}, {domicilio.localidad.nombre}
                      </option>
                    ))}
                  </Form.Select>
                  {(!cliente || cliente.domicilios.length === 0) && (
                    <Form.Text className="text-danger">No tienes domicilios registrados. <Button variant="link" size="sm" onClick={() => navigate('/profile')}>Añadir Domicilio</Button></Form.Text>
                  )}
                </Form.Group>
              )}
              <Form.Group className="mb-3">
                <Form.Label>Forma de Pago:</Form.Label>
                <div>
                  <Form.Check inline type="radio" label={<><FontAwesomeIcon icon={faMoneyBillWave} className="me-1" /> Efectivo</>} name="formaPago" value="EFECTIVO" checked={formaPago === 'EFECTIVO'} onChange={() => setFormaPago('EFECTIVO')} disabled={tipoEnvio === 'DELIVERY'} />
                  <Form.Check inline type="radio" label={<><FontAwesomeIcon icon={faCreditCard} className="me-1" /> Mercado Pago</>} name="formaPago" value="MERCADO_PAGO" checked={formaPago === 'MERCADO_PAGO'} onChange={() => setFormaPago('MERCADO_PAGO')} />
                </div>
              </Form.Group>

              {error && <Alert variant="danger" className="mt-3">{error}</Alert>}

              {!preferenceId ? (
                <Button
                  variant="primary"
                  onClick={handlePlaceOrder}
                  // FIX: La lógica 'disabled' ahora usa los contextos
                  disabled={submittingOrder || cart.length === 0 || !cliente || !selectedSucursal || (tipoEnvio === 'DELIVERY' && !selectedDomicilioId)}
                  className="w-100 mt-3"
                >
                  {submittingOrder ? <><Spinner as="span" size="sm" /> Procesando...</> : 'Realizar Pedido'}
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

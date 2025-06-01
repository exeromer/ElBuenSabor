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
 *
 * @service `setAuthToken`: Configura el token JWT para peticiones autenticadas.
 * @service `getClienteByAuth0Id`: Obtiene la información completa del cliente logueado.
 * @service `createPedido`: Envía la solicitud de creación de pedido al backend.
 * @service `getSucursales`: Obtiene la lista de sucursales disponibles.
 * @service `getImageUrl`: Función de utilidad para mostrar las imágenes de los productos en el resumen del carrito.
 */
import React, { useEffect, useState } from 'react';
import { Container, Row, Col, Card, ListGroup, Button, Form, Spinner, Alert, Image } from 'react-bootstrap';
import { useAuth0 } from '@auth0/auth0-react';
import { useCart } from '../context/CartContext';
import { setAuthToken } from '../services/apiClient'; // Función para configurar el token en Axios
import { getClienteByAuth0Id } from '../services/clienteUsuarioService';
import { createPedido } from '../services/pedidoService';
import { getSucursales } from '../services/sucursalService';
import { getImageUrl } from '../services/fileUploadService'; // Para mostrar imágenes de productos
import type { Cliente, Sucursal, TipoEnvio, FormaPago, PedidoRequestDTO, DetallePedidoRequestDTO } from '../types/types';
import { FontAwesomeIcon } from '@fortawesome/react-fontawesome';
import { faTruck, faStore, faMoneyBillWave, faCreditCard } from '@fortawesome/free-solid-svg-icons';
import { useNavigate } from 'react-router-dom';

const CheckoutPage: React.FC = () => {
  /**
   * @hook useAuth0
   * @description Hook para acceder al estado de autenticación y funciones de Auth0.
   */
  const { isAuthenticated, user, getAccessTokenSilently, isLoading: authLoading } = useAuth0();

  /**
   * @hook useCart
   * @description Hook para acceder al estado del carrito de compras y sus funciones.
   */
  const { cart, getCartTotal, clearCart } = useCart();

  /**
   * @hook useNavigate
   * @description Hook para la navegación programática.
   */
  const navigate = useNavigate();

  /**
   * @state cliente
   * @description Almacena la información del cliente logueado, obtenida del backend.
   */
  const [cliente, setCliente] = useState<Cliente | null>(null);

  /**
   * @state sucursales
   * @description Almacena la lista de sucursales disponibles, obtenida del backend.
   */
  const [sucursales, setSucursales] = useState<Sucursal[]>([]);

  /**
   * @state selectedSucursalId
   * @description ID de la sucursal seleccionada por el usuario para el pedido.
   */
  const [selectedSucursalId, setSelectedSucursalId] = useState<number | ''>('');

  /**
   * @state selectedDomicilioId
   * @description ID del domicilio de entrega seleccionado por el usuario (solo si `tipoEnvio` es 'DELIVERY').
   */
  const [selectedDomicilioId, setSelectedDomicilioId] = useState<number | ''>('');

  /**
   * @state tipoEnvio
   * @description Tipo de envío seleccionado por el usuario ('DELIVERY' o 'TAKEAWAY').
   */
  const [tipoEnvio, setTipoEnvio] = useState<TipoEnvio>('DELIVERY');

  /**
   * @state formaPago
   * @description Forma de pago seleccionada por el usuario ('EFECTIVO' o 'MERCADO_PAGO').
   */
  const [formaPago, setFormaPago] = useState<FormaPago>('EFECTIVO');

  /**
   * @state loadingData
   * @description Estado booleano para indicar si los datos iniciales del checkout están cargando.
   */
  const [loadingData, setLoadingData] = useState(true);

  /**
   * @state submittingOrder
   * @description Estado booleano para indicar si el pedido está en proceso de envío al backend.
   */
  const [submittingOrder, setSubmittingOrder] = useState(false);

  /**
   * @state error
   * @description Almacena un mensaje de error si ocurre un problema durante la carga de datos o la realización del pedido.
   */
  const [error, setError] = useState<string | null>(null);

  /**
   * @state successMessage
   * @description Almacena un mensaje de éxito después de realizar el pedido.
   */
  const [successMessage, setSuccessMessage] = useState<string | null>(null);

  /**
   * @constant defaultImage
   * @description Ruta a una imagen de marcador de posición si un artículo no tiene imágenes.
   */
  const defaultImage = '/placeholder-food.png'; // Asegúrate de que esta ruta sea accesible desde `public/`

  /**
   * @hook useEffect
   * @description Hook para cargar los datos necesarios para la página de checkout.
   * Se ejecuta al montar el componente, y cuando el estado de autenticación de Auth0 (`isAuthenticated`, `user`, `authLoading`) cambia.
   * Se encarga de:
   * 1. Verificar la autenticación del usuario.
   * 2. Obtener el token de acceso de Auth0 y configurarlo para las peticiones.
   * 3. Obtener la información del cliente desde el backend usando su `auth0Id`.
   * 4. Obtener la lista de sucursales disponibles.
   * 5. Preseleccionar la primera sucursal y domicilio si existen.
   */
  useEffect(() => {
    const loadCheckoutData = async () => {
      // Esperar a que Auth0 termine de cargar para evitar llamadas innecesarias o errores.
      if (authLoading) {
        setLoadingData(true); // Asegura que se muestre el spinner
        return;
      }

      setLoadingData(true); // Inicia el spinner de carga
      setError(null); // Limpia cualquier error anterior

      try {
        // Redirigir si el usuario no está autenticado o no tiene ID de Auth0
        if (!isAuthenticated || !user?.sub) {
          setError('Debes iniciar sesión para finalizar tu compra. Redirigiendo...');
          // Esperar un momento antes de redirigir para que el usuario vea el mensaje
          setTimeout(() => navigate('/'), 2000);
          setLoadingData(false);
          return;
        }

        // Obtener el token de acceso de Auth0
        const token = await getAccessTokenSilently({
          authorizationParams: {
            audience: import.meta.env.VITE_AUTH0_AUDIENCE,
            scope: import.meta.env.VITE_AUTH0_SCOPE,
          },
        });
        setAuthToken(token); // Configura el token para todas las peticiones futuras de apiClient

        // Cargar información del cliente y sucursales en paralelo para optimizar
        const [fetchedCliente, fetchedSucursales] = await Promise.all([
          getClienteByAuth0Id(user.sub, token),
          getSucursales(),
        ]);
        setCliente(fetchedCliente);
        setSucursales(fetchedSucursales);

        // Preseleccionar la primera sucursal y el primer domicilio si están disponibles
        if (fetchedSucursales.length > 0) {
          setSelectedSucursalId(fetchedSucursales[0].id);
        }
        if (fetchedCliente.domicilios.length > 0) {
          setSelectedDomicilioId(fetchedCliente.domicilios[0].id);
        }

      } catch (err) {
        console.error('Error al cargar datos del checkout:', err);
        const errorMessage = (err as any).response?.data?.message || (err as any).message || 'Error desconocido al cargar.';
        setError(`Error al cargar tu información o las sucursales: ${errorMessage}.`);
        // Redirigir a la página principal si hay un error crítico de carga
        setTimeout(() => navigate('/'), 3000);
      } finally {
        setLoadingData(false); // Detiene el spinner de carga
      }
    };

    loadCheckoutData();
  }, [isAuthenticated, user, authLoading, getAccessTokenSilently, navigate]); // Dependencias del efecto

  /**
   * @function handlePlaceOrder
   * @description Manejador para el botón "Realizar Pedido".
   * Realiza validaciones del carrito, información del cliente, sucursales y domicilios,
   * y luego construye y envía la solicitud de creación de pedido al backend.
   * Maneja el éxito y el fallo del pedido, limpiando el carrito y redirigiendo al usuario.
   */
  const handlePlaceOrder = async () => {
    setSubmittingOrder(true); // Activa el spinner de envío
    setError(null); // Limpia mensajes de error/éxito anteriores
    setSuccessMessage(null);

    // --- Validaciones previas al envío del pedido ---
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
    if (tipoEnvio === 'DELIVERY' && !selectedDomicilioId) {
      setError('Debes seleccionar un domicilio de entrega para el envío a domicilio.');
      setSubmittingOrder(false);
      return;
    }
    // Validación de horario de sucursal (simplificada)
    const selectedSucursal = sucursales.find(s => s.id === selectedSucursalId);
    if (selectedSucursal) {
      const now = new Date();
      // Formato HH:mm para comparar con los horarios de apertura/cierre
      const currentTime = `${now.getHours().toString().padStart(2, '0')}:${now.getMinutes().toString().padStart(2, '0')}`;
      // NOTA: Esta validación es básica y no considera fechas o cierres que pasen por la medianoche.
      // Un sistema más robusto usaría librerías de fecha-hora o validaría en el backend.
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

    // --- Preparación de los datos del pedido para el DTO ---
    const orderDetails: DetallePedidoRequestDTO[] = cart.map(item => ({
      articuloId: item.articulo.id,
      cantidad: item.quantity,
    }));

    // Cálculo de la hora estimada de finalización (simulada)
    // Idealmente, el backend debería calcular este tiempo basándose en la complejidad del pedido,
    // el tiempo de preparación de la sucursal, etc. Aquí se simula un tiempo base.
    const now = new Date();
    const estimatedTime = new Date(now.getTime() + (30 * 60 * 1000)); // 30 minutos a partir de ahora
    // Formato HH:mm:ss para el backend
    const estimatedTimeString = `${estimatedTime.getHours().toString().padStart(2, '0')}:${estimatedTime.getMinutes().toString().padStart(2, '0')}:${estimatedTime.getSeconds().toString().padStart(2, '0')}`;

    const pedidoData: PedidoRequestDTO = {
      clienteId: cliente.id,
      sucursalId: selectedSucursalId as number, // Aserción de tipo ya que sabemos que tiene un valor
      // Para TAKEAWAY, se podría enviar el ID del primer domicilio del cliente o un ID ficticio si el backend lo permite.
      // Aquí se usa el ID del primer domicilio si existe, o 0 si no hay (asumiendo que el backend maneja 0 para TAKEAWAY sin domicilio específico).
      domicilioId: tipoEnvio === 'DELIVERY' ? (selectedDomicilioId as number) : (cliente.domicilios.length > 0 ? cliente.domicilios[0].id : 0),
      tipoEnvio: tipoEnvio,
      formaPago: formaPago,
      horaEstimadaFinalizacion: estimatedTimeString,
      detalles: orderDetails,
    };

    try {
      const token = await getAccessTokenSilently();
      const newOrder = await createPedido(pedidoData, token); // Envía el pedido al backend
      setSuccessMessage(`¡Tu pedido #${newOrder.id} ha sido realizado con éxito!`);
      clearCart(); // Limpia el carrito de compras
      // Redirigir al usuario a la página de sus pedidos después de un breve retraso
      setTimeout(() => {
        navigate('/mis-pedidos');
      }, 2000);
    } catch (err: any) {
      console.error('Error al realizar el pedido:', err);
      // Intenta extraer un mensaje de error más específico de la respuesta del backend
      const backendErrorMessage = err.response?.data?.message || err.message || 'Por favor, inténtalo de nuevo.';
      setError(`Error al realizar el pedido: ${backendErrorMessage}`);
    } finally {
      setSubmittingOrder(false); // Desactiva el spinner de envío
    }
  };

  // --- Renderizado condicional basado en estados de carga o error ---
  // Muestra un spinner si Auth0 o los datos iniciales están cargando
  if (loadingData || authLoading) {
    return (
      <Container className="text-center my-5">
        <Spinner animation="border" />
        <p className="mt-3">Cargando información de tu compra...</p>
      </Container>
    );
  }

  // Muestra un mensaje de error si ocurre un problema (y no hay un mensaje de éxito)
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

  // Si el carrito está vacío y no hay mensaje de éxito, redirigir al menú.
  // Esto previene que se muestre la página de checkout con un carrito vacío si el usuario llega accidentalmente.
  if (cart.length === 0 && !successMessage && !submittingOrder) {
    navigate('/products');
    return null; // No renderizar nada mientras se redirige
  }

  return (
    <Container className="my-4">
      <h1 className="text-center mb-4">Finalizar Compra</h1>

      {/* Muestra un mensaje de éxito si el pedido se realizó correctamente */}
      {successMessage && <Alert variant="success" className="mb-4 text-center">{successMessage}</Alert>}

      <Row>
        {/* Columna para el Resumen del Carrito */}
        <Col md={6}>
          <Card className="mb-4 shadow-sm"> {/* Añadido shadow-sm */}
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
                            ? getImageUrl(item.articulo.imagenes[0].denominacion)
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
            <Card.Footer className="d-flex justify-content-between align-items-center bg-light"> {/* Fondo para el footer */}
              <h5 className="mb-0">Total del Pedido:</h5>
              <h5 className="mb-0"><span className="text-success">${getCartTotal().toFixed(2)}</span></h5>
            </Card.Footer>
          </Card>
        </Col>

        {/* Columna para los Detalles del Pedido (Entrega y Pago) */}
        <Col md={6}>
          <Card className="shadow-sm"> {/* Añadido shadow-sm */}
            <Card.Header as="h5">Detalles de Entrega y Pago</Card.Header>
            <Card.Body>
              {/* Selección de Sucursal */}
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

              {/* Tipo de Envío */}
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

              {/* Selección de Domicilio (solo si es Delivery) */}
              {tipoEnvio === 'DELIVERY' && (
                <Form.Group className="mb-3">
                  <Form.Label>Domicilio de Entrega:</Form.Label>
                  <Form.Select
                    value={selectedDomicilioId}
                    onChange={(e) => setSelectedDomicilioId(Number(e.target.value))}
                    disabled={!cliente || cliente.domicilios.length === 0}
                    required // Requiere un domicilio si es delivery
                  >
                    <option value="">Selecciona un domicilio</option>
                    {cliente?.domicilios.map((domicilio) => (
                      <option key={domicilio.id} value={domicilio.id}>
                        {domicilio.calle} {domicilio.numero}, {domicilio.localidad.nombre}
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

              {/* Forma de Pago */}
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

              {/* Mensaje de error (si existe) */}
              {error && <Alert variant="danger" className="mt-3">{error}</Alert>}

              {/* Botón para Realizar Pedido */}
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
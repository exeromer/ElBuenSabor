/**
 * @file Header.tsx
 * @description Componente de la barra de navegación superior (Header) de la aplicación.
 * Proporciona enlaces de navegación principales, acceso al carrito de compras, y funcionalidades
 * de autenticación (iniciar/cerrar sesión, perfil) a través de Auth0.
 * La visibilidad de algunos enlaces (ej. panel de administración, mis pedidos) se controla
 * según el rol del usuario.
 *
 * @hook `useState`: Gestiona la visibilidad del modal del carrito.
 * @hook `useAuth0`: Para el manejo de la autenticación y la información del usuario.
 * @hook `useCart`: Para acceder al estado del carrito y mostrar el conteo de ítems.
 * @component `Link` de `react-router-dom`: Para la navegación entre rutas.
 * @component `CartModal`: Modal que muestra el contenido detallado del carrito.
 */
import React, { useState } from 'react';
import { Navbar, Nav, Container, Button, Badge } from 'react-bootstrap';
import { Link } from 'react-router-dom';
import { useAuth0 } from '@auth0/auth0-react';
import { FontAwesomeIcon } from '@fortawesome/react-fontawesome';
import { faShoppingCart, faUser, faSignInAlt, faSignOutAlt, faClipboardList } from '@fortawesome/free-solid-svg-icons';
import { useCart } from '../../../context/CartContext';
import CartModal from '../../cart/CartModal';
import './Header.sass'

/**
 * @interface HeaderProps
 * @description No se requieren propiedades (`props`) para este componente de cabecera,
 * por lo que se define una interfaz vacía para claridad.
 */
interface HeaderProps { } // Interfaz vacía para tipificar las props

const Header: React.FC<HeaderProps> = () => {
  /**
   * @hook useAuth0
   * @description Hook para acceder a las funciones y estado de autenticación de Auth0.
   * @returns {object} Un objeto con `isAuthenticated`, `loginWithRedirect`, `logout`, `user`.
   */
  const { isAuthenticated, loginWithRedirect, logout, user } = useAuth0();

  /**
   * @hook useCart
   * @description Hook para acceder a las funciones y estado del carrito de compras.
   * @returns {object} Un objeto con `getCartItemCount` para obtener el número de ítems en el carrito.
   */
  const { getCartItemCount } = useCart();

  /**
   * @state showCartModal
   * @description Estado booleano para controlar la visibilidad del modal del carrito de compras.
   */
  const [showCartModal, setShowCartModal] = useState<boolean>(false);

  /**
   * @function getUserRole
   * @description Función temporal (dummy) para determinar el rol del usuario basándose en el email.
   * En un entorno de producción, esta lógica debería ser reemplazada por una verificación del rol
   * obtenido del backend (ej. a través de un contexto de usuario que consuma el rol de la DB).
   * @returns {string | undefined} El rol del usuario ('ADMIN', 'EMPLEADO', 'CLIENTE') o `undefined` si no se puede determinar.
   */
  const getUserRole = (): string | undefined => {
    // NOTA IMPORTANTE: Esta es una implementación temporal/dummy.
    // En un entorno de producción, el rol del usuario debería obtenerse de una fuente segura,
    // como un claim personalizado del token JWT de Auth0 (después de una regla en Auth0
    // que sincronice el rol de tu backend a Auth0) o un contexto de usuario que cargue
    // el rol directamente del backend.
    if (user?.email?.endsWith('@admin.com')) return 'ADMIN';
    if (user?.email?.endsWith('@empleado.com')) return 'EMPLEADO';
    // Si el usuario está autenticado pero no coincide con los dominios especiales, se asume CLIENTE.
    if (isAuthenticated) return 'CLIENTE';
    return undefined; // No autenticado o rol no determinado
  };

  /**
   * @constant userRole
   * @description Rol del usuario actual, determinado por la función `getUserRole`.
   */
  const userRole = getUserRole();

  return (
    <Navbar expand="lg">
      <Container>
        <Navbar.Brand as={Link} to="/">
          <img
            src="/logo.png"
            alt="logo de la página"
            className='logo'
          />
        </Navbar.Brand>
        <Navbar.Toggle aria-controls="basic-navbar-nav" />
        <Navbar.Collapse id="basic-navbar-nav">
          <Nav className="me-auto">
            <Nav.Link as={Link} to="/">Inicio</Nav.Link>
            <Nav.Link as={Link} to="/products">Menú</Nav.Link>
            {isAuthenticated && (userRole === 'ADMIN' || userRole === 'EMPLEADO') && (
              <Nav.Link as={Link} to="/admin-dashboard">Administración</Nav.Link>
            )}
            {isAuthenticated && userRole === 'CLIENTE' && (
              <Nav.Link as={Link} to="/mis-pedidos">
                <FontAwesomeIcon icon={faClipboardList} className="me-1" /> Mis Pedidos
              </Nav.Link>
            )}
          </Nav>
          <Nav>
            <Nav.Link onClick={() => setShowCartModal(true)} style={{ cursor: 'pointer' }}>
              <FontAwesomeIcon className="h" icon={faShoppingCart} />
              {getCartItemCount() > 0 && (
                <Badge pill bg="danger" className="ms-1">
                  {getCartItemCount()}
                </Badge>
              )}
            </Nav.Link>
            {!isAuthenticated ? (
              <Button variant='primary' className='boton-iniciar-sesion' onClick={() => loginWithRedirect()}>
                <FontAwesomeIcon icon={faSignInAlt} className="me-1" /> Iniciar Sesión
              </Button>
            ) : (
              <>
                <Nav.Link as={Link} to="/profile">
                  <FontAwesomeIcon icon={faUser} className="me-1" /> {user?.name || user?.nickname || 'Perfil'}
                </Nav.Link>
                <Button className='boton-cerrar-sesion' onClick={() => logout({ logoutParams: { returnTo: window.location.origin } })}>
                  <FontAwesomeIcon icon={faSignOutAlt} className="me-1" /> Cerrar Sesión
                </Button>
              </>
            )}
          </Nav>
        </Navbar.Collapse>
      </Container>
      <CartModal show={showCartModal} handleClose={() => setShowCartModal(false)} />
    </Navbar>
  );
};

export default Header;
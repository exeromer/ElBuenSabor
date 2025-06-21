/**
 * @file Header.tsx
 * @description Componente de la barra de navegación superior (Header) de la aplicación.
 * Proporciona el selector de sucursal, enlaces de navegación, acceso al carrito y autenticación.
 * ... (resto de la descripción sin cambios)
 */
import React from 'react';
import { Navbar, Nav, Container, Button, Badge } from 'react-bootstrap';
import { Link } from 'react-router-dom';
import { useAuth0 } from '@auth0/auth0-react';
import { FontAwesomeIcon } from '@fortawesome/react-fontawesome';
import { faShoppingCart, faUser, faSignInAlt, faSignOutAlt, faClipboardList } from '@fortawesome/free-solid-svg-icons';
import { useCart } from '../../../context/CartContext';
import { useSucursal } from '../../../context/SucursalContext';
import SucursalSelector from '../Sucursal/SucursalSelector';
import CartModal from '../../cart/CartModal';
import './Header.sass'

interface HeaderProps { }

const Header: React.FC<HeaderProps> = () => {
  const { isAuthenticated, loginWithRedirect, logout, user } = useAuth0();

  // El hook `useCart` ahora se usa para el contador y para abrir el modal
  const { totalItems, openCart, isCartOpen } = useCart();

  // <-- 3. USAMOS EL HOOK DE SUCURSAL -->
  // Obtenemos la lista de sucursales para decidir si mostramos el selector
  const { sucursales } = useSucursal();

  // El rol del usuario se sigue determinando igual
  const getUserRole = (): string | undefined => {
    if (user?.email?.endsWith('@admin.com')) return 'ADMIN';
    if (user?.email?.endsWith('@empleado.com')) return 'EMPLEADO';
    if (isAuthenticated) return 'CLIENTE';
    return undefined;
  };
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

          <div className="d-flex justify-content-center flex-grow-1">
             {sucursales.length > 1 && <SucursalSelector />}
          </div>
         
          <Nav>
            <Nav.Link onClick={openCart} style={{ cursor: 'pointer' }}>
              <FontAwesomeIcon className="h" icon={faShoppingCart} />
              {totalItems > 0 && (
                <Badge pill bg="danger" className="ms-1">
                  {totalItems}
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
      <CartModal />
    </Navbar>
  );
};

export default Header;
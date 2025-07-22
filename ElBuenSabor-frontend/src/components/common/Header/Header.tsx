import React from 'react';
import { Navbar, Nav, Container, Button, Badge, Image } from 'react-bootstrap';
import { Link } from 'react-router-dom';
import { useAuth0 } from '@auth0/auth0-react';
import { FontAwesomeIcon } from '@fortawesome/react-fontawesome';
import { faShoppingCart, faSignInAlt, faSignOutAlt, faClipboardList, faTachometerAlt } from '@fortawesome/free-solid-svg-icons';
import { useCart } from '../../../context/CartContext';
import { useSucursal } from '../../../context/SucursalContext';
import { useUser } from '../../../context/UserContext';
import SucursalSelector from '../Sucursal/SucursalSelector';
import CartModal from '../../cart/CartModal';
import './Header.sass'

const Header: React.FC = () => {
  const { isAuthenticated, loginWithRedirect, logout, user } = useAuth0();
  const { totalItems, openCart } = useCart();
  const { sucursales } = useSucursal();
  // OBTENEMOS LOS ROLES
  const { userRole, cliente } = useUser();

  return (
    <Navbar expand="lg">
      <Container>
        <Navbar.Brand as={Link} to="/">
          <img src="/logo.png" alt="logo de la página" className='logo' />
        </Navbar.Brand>
        <Navbar.Toggle aria-controls="basic-navbar-nav" />
        <Navbar.Collapse id="basic-navbar-nav">
          <Nav className="me-auto">
            <Nav.Link as={Link} to="/">Inicio</Nav.Link>
            <Nav.Link as={Link} to="/products">Menú</Nav.Link>
            {/* Visible solo para Clientes */}
            {userRole === 'CLIENTE' && (
              <Nav.Link as={Link} to="/mis-pedidos">
                <FontAwesomeIcon icon={faClipboardList} className="me-1" /> Mis Pedidos
              </Nav.Link>
            )}

            {/* Visible solo para Admin y Empleados */}
            {(userRole === 'ADMIN' || userRole === 'EMPLEADO') && (
              <Nav.Link as={Link} to="/admin-dashboard">
                <FontAwesomeIcon icon={faTachometerAlt} className="me-1" /> Administración
              </Nav.Link>
            )}
          </Nav>

          <div className="d-flex justify-content-center flex-grow-1">
            {sucursales.length > 1 && <SucursalSelector />}
          </div>

          <Nav>
            {/* El carrito solo es visible para clientes */}
            {userRole === 'CLIENTE' && (
              <Nav.Link onClick={openCart} style={{ cursor: 'pointer' }}>
                <FontAwesomeIcon className="h mt-1 fs-4" icon={faShoppingCart} />
                {totalItems > 0 && (
                  <Badge pill bg="danger" className="ms-2">{totalItems}</Badge>
                )}
              </Nav.Link>
            )}

            {!isAuthenticated ? (
              <Button variant="secondary" onClick={() => loginWithRedirect()}>
                <FontAwesomeIcon icon={faSignInAlt} className="me-1" /> Iniciar Sesión
              </Button>
            ) : (
              <>
                <Nav.Link as={Link} to="/profile">
                  {user?.picture && <Image src={user.picture} alt="Perfil" roundedCircle style={{ width: '30px', marginRight: '8px' }} />}
                  {/* Mostramos el nombre del cliente o un fallback */}
                  <span>{cliente?.nombre !== 'Nuevo' ? cliente?.nombre : (user?.name || user?.nickname)}</span>
                </Nav.Link>
                <Button variant="secondary" onClick={() => logout({ logoutParams: { returnTo: window.location.origin } })}>
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
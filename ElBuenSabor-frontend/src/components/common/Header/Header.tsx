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
import { faShoppingCart, faSignInAlt, faSignOutAlt, faClipboardList, faTachometerAlt, faCashRegister, faUtensils, faMotorcycle, faChartLine  } from '@fortawesome/free-solid-svg-icons';
import { useCart } from '../../../context/CartContext';
import { useSucursal } from '../../../context/SucursalContext';
import { useUser } from '../../../context/UserContext';
import SucursalSelector from '../Sucursal/SucursalSelector';
import CartModal from '../../cart/CartModal';
import { Image } from 'react-bootstrap';
import './Header.sass'

interface HeaderProps { }

const Header: React.FC<HeaderProps> = () => {
  const { isAuthenticated, loginWithRedirect, logout, user } = useAuth0();
  const { totalItems, openCart } = useCart();
  const { sucursales } = useSucursal();
  const { userRole, employeeRole, cliente } = useUser();

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

            {/* --- ENLACES CONDICIONALES --- */}

            {/* Para Clientes */}
            {userRole === 'CLIENTE' && (
              <Nav.Link as={Link} to="/mis-pedidos">
                <FontAwesomeIcon icon={faClipboardList} className="me-1" /> Mis Pedidos
              </Nav.Link>
            )}

            {/* Para Todos los Empleados y Admins */}
            {(userRole === 'ADMIN' || userRole === 'EMPLEADO') && (
              <Nav.Link as={Link} to="/admin-dashboard">
                <FontAwesomeIcon icon={faTachometerAlt} className="me-1" /> Administración
              </Nav.Link>
            )}

            {/* Específicos para cada rol de Empleado */}
            {employeeRole === 'CAJERO' && (
              <Nav.Link as={Link} to="/cajero">
                <FontAwesomeIcon icon={faCashRegister} className="me-1" /> Caja
              </Nav.Link>
            )}
            {employeeRole === 'COCINA' && (
              <Nav.Link as={Link} to="/cocina">
                <FontAwesomeIcon icon={faUtensils} className="me-1" /> Cocina
              </Nav.Link>
            )}
            {employeeRole === 'DELIVERY' && (
              <Nav.Link as={Link} to="/delivery">
                <FontAwesomeIcon icon={faMotorcycle} className="me-1" /> Delivery
              </Nav.Link>
            )}
            {userRole === 'ADMIN' && (
              <Nav.Link as={Link} to="/estadisticas">
                <FontAwesomeIcon icon={faChartLine} className="me-1" /> Estadísticas
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
                  <span>{cliente?.nombre || user?.name || user?.nickname}</span>
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
import React, { useState } from 'react';
import { Navbar, Nav, Container, Button, Badge, Image, Form, Dropdown } from 'react-bootstrap';
import { Link, useNavigate } from 'react-router-dom';
import { useAuth0 } from '@auth0/auth0-react';
import { FontAwesomeIcon } from '@fortawesome/react-fontawesome';
import { faShoppingCart, faSignInAlt, faSignOutAlt, faClipboardList, faTachometerAlt, faChartBar, faFireBurner, faTruck, faCashRegister, faUserCircle } from '@fortawesome/free-solid-svg-icons';
import { useCart } from '../../../context/CartContext';
import { useUser } from '../../../context/UserContext';
import SucursalSelector from '../Sucursal/SucursalSelector';
import CartModal from '../../cart/CartModal';
import './Header.sass';

const Header: React.FC = () => {
  const { isAuthenticated, loginWithRedirect, logout, user } = useAuth0();
  const { totalItems, openCart } = useCart();
  const { userRole, cliente, employeeRole } = useUser();
  const [searchTerm, setSearchTerm] = useState('');
  const navigate = useNavigate();

  const handleSearch = (e: React.FormEvent) => {
    e.preventDefault();
    if (searchTerm.trim()) {
      navigate(`/products?search=${encodeURIComponent(searchTerm.trim())}`);
    } else {
      navigate('/products');
    }
  };
  return (
    <Navbar expand="lg" className="px-3 header-navbar">
      <Container fluid>
        <Navbar.Brand as={Link} to="/">
          <img src="/logo.png" alt="logo de la página" className="logo" />
        </Navbar.Brand>
        <Navbar.Toggle aria-controls="basic-navbar-nav" />
        <Navbar.Collapse id="basic-navbar-nav">
          <Nav className="me-auto">
            <Nav.Link as={Link} to="/products">
              Menú
            </Nav.Link>
            {userRole === 'CLIENTE' && (
              <Nav.Link as={Link} to="/mis-pedidos">
                <FontAwesomeIcon icon={faClipboardList} className="me-1" /> Mis Pedidos
              </Nav.Link>
            )}
            {(userRole === 'ADMIN' || userRole === 'EMPLEADO') && (
              <Nav.Link as={Link} to="/admin-dashboard">
                <FontAwesomeIcon icon={faTachometerAlt} className="me-1" /> Administración
              </Nav.Link>
            )}
            {userRole === 'ADMIN' && (
              <Nav.Link as={Link} to="/estadisticas">
                <FontAwesomeIcon icon={faChartBar} className="me-1" /> Estadísticas
              </Nav.Link>
            )}
            {employeeRole === 'COCINA' && (
              <Nav.Link as={Link} to="/cocina">
                <FontAwesomeIcon icon={faFireBurner} className="me-1" /> Cocina
              </Nav.Link>
            )}
            {employeeRole === 'DELIVERY' && (
              <Nav.Link as={Link} to="/delivery">
                <FontAwesomeIcon icon={faTruck} className="me-1" /> Delivery
              </Nav.Link>
            )}
            {employeeRole === 'CAJERO' && (
              <Nav.Link as={Link} to="/cajero">
                <FontAwesomeIcon icon={faCashRegister} className="me-1" /> Caja
              </Nav.Link>
            )}
          </Nav>
          <div className="flex-grow-1 mx-lg-4" style={{ maxWidth: '500px' }}>
            <Form className="d-flex" onSubmit={handleSearch}>
              <Form.Control
                type="search"
                placeholder="Buscar comidas, bebidas..."
                className="me-2"
                aria-label="Search"
                value={searchTerm}
                onChange={(e) => setSearchTerm(e.target.value)}
              />
              <Button variant="outline-secondary" type="submit">Buscar</Button>
            </Form>
          </div>
          <Nav className="ms-auto align-items-center">
            {userRole === 'CLIENTE' && (
              <Nav.Link onClick={openCart} style={{ cursor: 'pointer' }} className="me-2">
                <FontAwesomeIcon className="h mt-1 fs-4" icon={faShoppingCart} />
                {totalItems > 0 && (
                  <Badge pill bg="danger" className="ms-1">
                    {totalItems}
                  </Badge>
                )}
              </Nav.Link>
            )}
            {!isAuthenticated ? (
              <Button variant="secondary" onClick={() => loginWithRedirect()}>
                <FontAwesomeIcon icon={faSignInAlt} className="me-1" /> Iniciar Sesión
              </Button>
            ) : (
              <Dropdown as={Nav.Item}>
                <Dropdown.Toggle as={Nav.Link} className="d-flex align-items-center">
                  {user?.picture ? (
                    <Image
                      src={user.picture}
                      alt="Perfil"
                      roundedCircle
                      style={{ width: '30px', height: '30px', marginRight: '8px' }}
                    />
                  ) : (
                    <FontAwesomeIcon icon={faUserCircle} className="fs-4 me-2" />
                  )}
                  <span>{cliente?.nombre !== 'Nuevo' ? cliente?.nombre : user?.name || user?.nickname}</span>
                </Dropdown.Toggle>
                <Dropdown.Menu align="end">
                  <Dropdown.Item as={Link} to="/profile">Mi Perfil</Dropdown.Item>
                  {userRole === 'CLIENTE' && (
                    <Dropdown.Item as={Link} to="/mis-pedidos">Mis Pedidos</Dropdown.Item>
                  )}
                  <Dropdown.Divider />
                  <div className="px-3 py-2" style={{ minWidth: '300px' }}>
                    <SucursalSelector />
                  </div>
                  <Dropdown.Divider />
                  <Dropdown.Item onClick={() => logout({ logoutParams: { returnTo: window.location.origin } })}>
                    <FontAwesomeIcon icon={faSignOutAlt} className="me-2" /> Cerrar Sesión
                  </Dropdown.Item>
                </Dropdown.Menu>
              </Dropdown>
            )}
          </Nav>
        </Navbar.Collapse>
      </Container>
      <CartModal />
    </Navbar>
  );
};


export default Header;

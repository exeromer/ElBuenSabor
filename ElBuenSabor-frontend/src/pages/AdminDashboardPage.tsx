/**
 * @file AdminDashboardPage.tsx
 * @description Página del panel de administración/empleado.
 * Actúa como un punto de acceso centralizado para las diferentes herramientas de gestión
 * (artículos, pedidos, usuarios/clientes) disponibles para los usuarios con roles de `ADMIN` o `EMPLEADO`.
 * Presenta una interfaz de usuario limpia con tarjetas informativas y enlaces directos a cada sección de gestión.
 *
 * @component `Link` de `react-router-dom`: Utilizado para la navegación a las diferentes rutas de gestión.
 * @component `FontAwesomeIcon`: Para añadir iconos visuales a las tarjetas, mejorando la usabilidad.
 */
import React from 'react';
import { Container, Card, Row, Col, Button } from 'react-bootstrap';
import { Link } from 'react-router-dom';
import { FontAwesomeIcon } from '@fortawesome/react-fontawesome';
import {
  faBoxOpen, // Icono para gestión de artículos (productos)
  faClipboardList, // Icono para gestión de pedidos
  faUsers, // Icono para gestión de usuarios y clientes
  // faTools, // Eliminado: No se utiliza en el JSX
} from '@fortawesome/free-solid-svg-icons';

/**
 * @interface AdminDashboardPageProps
 * @description No se requieren propiedades (`props`) para este componente de página,
 * por lo que se define una interfaz vacía para claridad.
 */
interface AdminDashboardPageProps {}

const AdminDashboardPage: React.FC<AdminDashboardPageProps> = () => {
  return (
    // Contenedor principal de Bootstrap para centrar el contenido y aplicar márgenes verticales
    <Container className="my-5">
      {/* Tarjeta principal que actúa como el encabezado del dashboard */}
      <Card className="shadow-sm">
        <Card.Header as="h5">Panel de Administración/Empleado</Card.Header>
        <Card.Body>
          <Card.Title className="mb-3">Bienvenido al Dashboard</Card.Title>
          <Card.Text className="mb-4">
            Aquí tendrás acceso a las herramientas de gestión según tu rol.
          </Card.Text>
          {/* Fila de tarjetas para las opciones de gestión, con diseño responsivo */}
          <Row xs={1} md={2} lg={3} className="g-4 mt-3">
            {/* Tarjeta para Gestión de Artículos */}
            <Col>
              <Card className="text-center h-100 d-flex flex-column"> {/* flex-column para h-100 */}
                <Card.Body className="d-flex flex-column"> {/* flex-column para h-100 */}
                  <FontAwesomeIcon icon={faBoxOpen} size="3x" className="mb-3 text-primary" /> {/* Color de icono */}
                  <Card.Title className="fw-bold">Gestión de Artículos</Card.Title>
                  <Card.Text className="text-muted flex-grow-1"> {/* flex-grow-1 para ocupar espacio */}
                    Administra los productos (manufacturados e insumos) disponibles en el menú.
                  </Card.Text>
                  <Link to="/manage-products" style={{ textDecoration: 'none' }} className="mt-auto"> {/* mt-auto para alinear botón abajo */}
                    <Button variant="primary">Ir a Gestión</Button>
                  </Link>
                </Card.Body>
              </Card>
            </Col>

            {/* Tarjeta para Gestión de Pedidos */}
            <Col>
              <Card className="text-center h-100 d-flex flex-column">
                <Card.Body className="d-flex flex-column">
                  <FontAwesomeIcon icon={faClipboardList} size="3x" className="mb-3 text-info" /> {/* Color de icono */}
                  <Card.Title className="fw-bold">Gestión de Pedidos</Card.Title>
                  <Card.Text className="text-muted flex-grow-1">
                    Revisa y actualiza el estado de los pedidos de los clientes.
                  </Card.Text>
                  <Link to="/manage-orders" style={{ textDecoration: 'none' }} className="mt-auto">
                    <Button variant="primary">Ir a Gestión</Button>
                  </Link>
                </Card.Body>
              </Card>
            </Col>

            {/* Tarjeta para Gestión de Usuarios y Clientes */}
            <Col>
              <Card className="text-center h-100 d-flex flex-column">
                <Card.Body className="d-flex flex-column">
                  <FontAwesomeIcon icon={faUsers} size="3x" className="mb-3 text-success" /> {/* Color de icono */}
                  <Card.Title className="fw-bold">Gestión de Usuarios y Clientes</Card.Title>
                  <Card.Text className="text-muted flex-grow-1">
                    Administra las cuentas de usuario y los perfiles de cliente. (Solo Admin)
                  </Card.Text>
                  <Link to="/manage-users" style={{ textDecoration: 'none' }} className="mt-auto">
                    <Button variant="primary">Ir a Gestión</Button>
                  </Link>
                </Card.Body>
              </Card>
            </Col>
          </Row>
        </Card.Body>
      </Card>
    </Container>
  );
};

export default AdminDashboardPage;
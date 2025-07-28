import React from 'react';
import { Container, Card, Row, Col, Button } from 'react-bootstrap';
import { Link } from 'react-router-dom';
import { FontAwesomeIcon } from '@fortawesome/react-fontawesome';
import { faBoxOpen, faClipboardList, faUsers, faStore } from '@fortawesome/free-solid-svg-icons';
import { useUser } from '../context/UserContext'; 

const AdminDashboardPage: React.FC = () => {
  
    const { userRole, employeeRole } = useUser();

    // LÓGICA PARA DETERMINAR LA RUTA DE GESTIÓN DE PEDIDOS
    let pedidosPath = '/';
    if (userRole === 'ADMIN' || employeeRole === 'CAJERO') {
        pedidosPath = '/cajero';
    } else if (employeeRole === 'COCINA') {
        pedidosPath = '/cocina';
    } else if (employeeRole === 'DELIVERY') {
        pedidosPath = '/delivery';
    }

    return (
        <Container className="my-5">
            <Card className="shadow-sm">
                <Card.Header as="h5">Panel de Administración</Card.Header>
                <Card.Body>
                    <Card.Title className="mb-3">Bienvenido al Dashboard</Card.Title>
                    <Card.Text className="mb-4">
                        Aquí tendrás acceso a las herramientas de gestión según tu rol.
                    </Card.Text>
                    <Row xs={1} md={2} lg={4} className="g-4 mt-3">
                        {/* Tarjeta para Gestión de Artículos (Admin y Cocinero) */}
                        {(userRole === 'ADMIN' || employeeRole === 'COCINA') && (
                            <Col>
                                <Card className="text-center h-100 d-flex flex-column">
                                    <Card.Body className="d-flex flex-column">
                                        <FontAwesomeIcon icon={faBoxOpen} size="3x" className="mb-3 text-primary" />
                                        <Card.Title className="fw-bold">Gestión de Artículos</Card.Title>
                                        <Card.Text className="text-muted flex-grow-1">
                                            Administra productos, insumos, categorías y promociones.
                                        </Card.Text>
                                        <Link to="/manage-products" style={{ textDecoration: 'none' }} className="mt-auto">
                                            <Button variant="primary">Ir a Gestión</Button>
                                        </Link>
                                    </Card.Body>
                                </Card>
                            </Col>
                        )}
                        {/* Tarjeta para Gestión de Pedidos (Todos los empleados y Admin) */}
                        {(userRole === 'ADMIN' || userRole === 'EMPLEADO') && (
                            <Col>
                                <Card className="text-center h-100 d-flex flex-column">
                                    <Card.Body className="d-flex flex-column">
                                        <FontAwesomeIcon icon={faClipboardList} size="3x" className="mb-3 text-info" />
                                        <Card.Title className="fw-bold">Gestión de Pedidos</Card.Title>
                                        <Card.Text className="text-muted flex-grow-1">
                                            Revisa y actualiza el estado de los pedidos de los clientes.
                                        </Card.Text>
                                        {/* El 'to' ahora es dinámico */}
                                        <Link to={pedidosPath} style={{ textDecoration: 'none' }} className="mt-auto">
                                            <Button variant="primary">Ir a Gestión</Button>
                                        </Link>
                                    </Card.Body>
                                </Card>
                            </Col>
                        )}
                        {/* Tarjeta para Gestión de Usuarios y Clientes (Solo Admin) */}
                        {userRole === 'ADMIN' && (
                            <Col>
                                <Card className="text-center h-100 d-flex flex-column">
                                    <Card.Body className="d-flex flex-column">
                                        <FontAwesomeIcon icon={faUsers} size="3x" className="mb-3 text-success" />
                                        <Card.Title className="fw-bold">Gestión de Usuarios</Card.Title>
                                        <Card.Text className="text-muted flex-grow-1">
                                            Administra las cuentas de usuario, clientes y empleados.
                                        </Card.Text>
                                        <Link to="/manage-users" style={{ textDecoration: 'none' }} className="mt-auto">
                                            <Button variant="primary">Ir a Gestión</Button>
                                        </Link>
                                    </Card.Body>
                                </Card>
                            </Col>
                        )}
                        {/* Tarjeta para Gestión de Sucursales (Solo Admin) */}
                        {userRole === 'ADMIN' && (
                             <Col>
                                <Card className="text-center h-100 d-flex flex-column">
                                    <Card.Body className="d-flex flex-column">
                                        <FontAwesomeIcon icon={faStore} size="3x" className="mb-3 text-warning" />
                                        <Card.Title className="fw-bold">Gestión de Sucursales</Card.Title>
                                        <Card.Text className="text-muted flex-grow-1">
                                            Administra las sucursales, sus horarios y domicilios.
                                        </Card.Text>
                                        <Link to="/manage-sucursales" style={{ textDecoration: 'none' }} className="mt-auto">
                                            <Button variant="primary">Ir a Gestión</Button>
                                        </Link>
                                    </Card.Body>
                                </Card>
                            </Col>
                        )}
                    </Row>
                </Card.Body>
            </Card>
        </Container>
    );
};

export default AdminDashboardPage;
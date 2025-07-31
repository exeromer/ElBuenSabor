import React, { useEffect, useState, useCallback } from 'react';
import { Container, Card, Button, Table, Spinner, Alert, Tabs, Tab, Dropdown, DropdownButton, Badge, Modal, Form } from 'react-bootstrap';
import { useAuth0 } from '@auth0/auth0-react';
import { ClienteUsuarioService } from '../services/clienteUsuarioService';
import { EmpleadoService } from '../services/EmpleadoService';
import type { UsuarioResponse, ClienteResponse, UsuarioRequest, ClienteRequest, DomicilioResponse } from '../types/types';
import type { Rol } from '../types/enums';
import { FontAwesomeIcon } from '@fortawesome/react-fontawesome';
import { faPlus, faEdit, faUserShield, faAddressBook, faBriefcase, faToggleOn, faToggleOff, faCheckCircle, faBan } from '@fortawesome/free-solid-svg-icons';
import UserForm from '../components/admin/UserForm';
import ClientForm from '../components/admin/ClientForm';
import toast from 'react-hot-toast';

interface ManageUsersPageProps { }

const ManageUsersPage: React.FC<ManageUsersPageProps> = () => {
    const { getAccessTokenSilently } = useAuth0();

    const [usuarios, setUsuarios] = useState<UsuarioResponse[]>([]);
    const [clientes, setClientes] = useState<ClienteResponse[]>([]);
    const [loading, setLoading] = useState(true);
    const [error, setError] = useState<string | null>(null);
    const [activeTab, setActiveTab] = useState<'usuarios' | 'clientes'>('usuarios');
    const [showUserForm, setShowUserForm] = useState(false);
    const [editingUser, setEditingUser] = useState<UsuarioResponse | null>(null);
    const [showClientForm, setShowClientForm] = useState(false);
    const [editingClient, setEditingClient] = useState<ClienteResponse | null>(null);
    const [showRoleModal, setShowRoleModal] = useState(false);
    const [selectedUserForRole, setSelectedUserForRole] = useState<UsuarioResponse | null>(null);
    const [newEmployeeRole, setNewEmployeeRole] = useState<'CAJERO' | 'COCINA' | 'DELIVERY'>('COCINA');

    const clienteUsuarioService = new ClienteUsuarioService();

    const fetchData = useCallback(async () => {
        setLoading(true);
        setError(null);
        try {
            const token = await getAccessTokenSilently();
            const [fetchedUsuarios, fetchedClientes] = await Promise.all([
                clienteUsuarioService.getAllUsuarios(token),
                clienteUsuarioService.getAllClientes(token),
            ]);
            setUsuarios(fetchedUsuarios);
            setClientes(fetchedClientes);
        } catch (err: any) {
            setError(err.message || 'Error al cargar datos.');
        } finally {
            setLoading(false);
        }
    }, [getAccessTokenSilently]);

    useEffect(() => {
        fetchData();
    }, [fetchData]);

    const handleEditUser = (user: UsuarioResponse) => {
        setEditingUser(user);
        setShowUserForm(true);
    };

    const handleUpdateUserRole = async (id: number, newRole: Rol) => {
        try {
            const token = await getAccessTokenSilently();
            const userToUpdate = usuarios.find(u => u.id === id);
            if (!userToUpdate) throw new Error("Usuario no encontrado.");
            const updatedData: UsuarioRequest = {
                auth0Id: userToUpdate.auth0Id,
                username: userToUpdate.username,
                rol: newRole,
                estadoActivo: userToUpdate.estadoActivo,
            };
            await clienteUsuarioService.updateUsuario(id, updatedData, token);
            toast.success('Rol de usuario actualizado con éxito.');
            fetchData();
        } catch (err: any) {
            toast.error(`Error: ${err.message || 'No se pudo actualizar el rol.'}`);
        }
    };

    const handleUpdateUserState = async (user: UsuarioResponse, newStatus: boolean) => {
        try {
            const token = await getAccessTokenSilently();
            const updatedData: UsuarioRequest = {
                auth0Id: user.auth0Id,
                username: user.username,
                rol: user.rol,
                estadoActivo: newStatus,
            };
            await clienteUsuarioService.updateUsuario(user.id, updatedData, token);
            toast.success(`Usuario ${newStatus ? 'activado' : 'desactivado'} con éxito.`);
            fetchData();
        } catch (err: any) {
            toast.error(`Error: ${err.message || 'No se pudo actualizar el estado del usuario.'}`);
        }
    };

    const handleEditClient = (client: ClienteResponse) => {
        setEditingClient(client);
        setShowClientForm(true);
    };

    const handleUpdateClientState = async (client: ClienteResponse, newStatus: boolean) => {
        try {
            const token = await getAccessTokenSilently();
            const updatedData: ClienteRequest = {
                nombre: client.nombre,
                apellido: client.apellido,
                telefono: client.telefono,
                email: client.email,
                fechaNacimiento: client.fechaNacimiento,
                usuarioId: client.usuarioId,
                domicilioIds: client.domicilios?.map((d: DomicilioResponse) => d.id) ?? [],
                estadoActivo: newStatus,
            };
            await clienteUsuarioService.updateCliente(client.id, updatedData, token);
            toast.success(`Cliente ${newStatus ? 'activado' : 'desactivado'} con éxito.`);
            fetchData();
        } catch (err: any) {
            toast.error(`Error: ${err.message || 'No se pudo actualizar el estado del cliente.'}`);
        }
    };

    const handleFormSubmit = () => {
        setShowUserForm(false);
        setShowClientForm(false);
        setEditingUser(null);
        setEditingClient(null);
        fetchData();
    };

    const handleOpenRoleModal = (user: UsuarioResponse) => {
        // Es crucial que el backend envíe el objeto 'empleado' dentro del 'usuario' para que esto funcione
        if (user.rol === 'EMPLEADO' && user.empleado) {
            setSelectedUserForRole(user);
            setNewEmployeeRole(user.empleado.rolEmpleado); // Precargamos el rol actual
            setShowRoleModal(true);
        } else {
            toast.error("Este usuario no es un empleado o sus datos de empleado no están disponibles.");
        }
    };

    const handleRoleChange = async () => {
        if (!selectedUserForRole || !selectedUserForRole.empleado?.id) {
            toast.error("No se ha seleccionado un empleado válido para el cambio de rol.");
            return;
        }

        const promise = EmpleadoService.cambiarRol(selectedUserForRole.empleado.id, newEmployeeRole);

        toast.promise(promise, {
            loading: 'Actualizando rol...',
            success: () => {
                fetchData(); 
                setShowRoleModal(false);
                return 'Rol del empleado actualizado con éxito.';
            },
            error: (err: any) => err.message || 'No se pudo actualizar el rol.',
        });
    };

    if (loading) {
        return <Container className="text-center my-5"><Spinner animation="border" /></Container>;
    }

    if (error) {
        return <Container className="my-5"><Alert variant="danger">{error}</Alert><Button onClick={fetchData}>Reintentar</Button></Container>;
    }

    return (
        <Container className="my-4">
            <h1 className="text-center mb-4">Gestión de Usuarios y Clientes</h1>
            <Tabs activeKey={activeTab} onSelect={(k) => setActiveTab(k as 'usuarios' | 'clientes')} className="mb-3 justify-content-center">
                <Tab eventKey="usuarios" title={<span><FontAwesomeIcon icon={faUserShield} className="me-2" />Usuarios</span>}>
                    <Card className="shadow-sm">
                        <Card.Header className="d-flex justify-content-between align-items-center">
                            <h5>Listado de Usuarios</h5>
                            <Button variant="success" onClick={() => { setEditingUser(null); setShowUserForm(true); }}><FontAwesomeIcon icon={faPlus} className="me-2" />Nuevo Usuario</Button>
                        </Card.Header>
                        <Card.Body>
                            <Table striped bordered hover responsive className="text-center align-middle">
                                <thead><tr><th>ID</th><th>Username</th><th>Auth0 ID</th><th>Rol</th><th>Estado</th><th>Acciones</th></tr></thead>
                                <tbody>
                                    {usuarios.map((user) => (
                                        <tr key={user.id}>
                                            <td>{user.id}</td>
                                            <td>{user.username}</td>
                                            <td>{user.auth0Id?.substring(user.auth0Id.indexOf('|') + 1) || 'No disponible'}</td>
                                            <td><Badge bg={user.rol === 'ADMIN' ? 'danger' : user.rol === 'EMPLEADO' ? 'info' : 'secondary'}>{user.rol}</Badge></td>
                                            <td>
                                                {user.estadoActivo
                                                    ? <Badge bg="success"><FontAwesomeIcon icon={faCheckCircle} className="me-1" /> Activo</Badge>
                                                    : <Badge bg="danger"><FontAwesomeIcon icon={faBan} className="me-1" /> Inactivo</Badge>
                                                }
                                            </td>
                                            <td>
                                                <Button variant="info" size="sm" className="me-2" onClick={() => handleEditUser(user)} title="Editar Usuario"><FontAwesomeIcon icon={faEdit} /></Button>
                                                <DropdownButton id={`dropdown-rol-${user.id}`} title={<FontAwesomeIcon icon={faBriefcase} />} variant="secondary" size="sm" className="me-2 d-inline-block">
                                                    {(['ADMIN', 'EMPLEADO', 'CLIENTE'] as Rol[]).map(role => (
                                                        <Dropdown.Item key={role} onClick={() => handleUpdateUserRole(user.id, role)} disabled={user.rol === role}>Cambiar a {role}</Dropdown.Item>
                                                    ))}
                                                </DropdownButton>
                                                {user.rol === 'EMPLEADO' && (
                                                    <Button
                                                        variant="outline-info"
                                                        size="sm"
                                                        className="me-2"
                                                        title="Cambiar Rol Específico de Empleado"
                                                        onClick={() => handleOpenRoleModal(user)}
                                                    >
                                                        <FontAwesomeIcon icon={faUserShield} />
                                                    </Button>
                                                )}
                                                <DropdownButton
                                                    id={`dropdown-estado-${user.id}`}
                                                    title={<FontAwesomeIcon icon={user.estadoActivo ? faToggleOn : faToggleOff} />}
                                                    variant={user.estadoActivo ? 'outline-success' : 'outline-danger'}
                                                    size="sm"
                                                    className="d-inline-block"
                                                >
                                                    <Dropdown.Item onClick={() => handleUpdateUserState(user, true)} disabled={user.estadoActivo}>Activo</Dropdown.Item>
                                                    <Dropdown.Item onClick={() => handleUpdateUserState(user, false)} disabled={!user.estadoActivo}>Inactivo</Dropdown.Item>
                                                </DropdownButton>
                                            </td>
                                        </tr>
                                    ))}
                                </tbody>
                            </Table>
                        </Card.Body>
                    </Card>
                </Tab>
                <Tab eventKey="clientes" title={<span><FontAwesomeIcon icon={faAddressBook} className="me-2" />Clientes</span>}>
                    <Card className="shadow-sm">
                        <Card.Header className="d-flex justify-content-between align-items-center">
                            <h5>Listado de Clientes</h5>
                            <Button variant="success" onClick={() => { setEditingClient(null); setShowClientForm(true); }}><FontAwesomeIcon icon={faPlus} className="me-2" />Nuevo Cliente</Button>
                        </Card.Header>
                        <Card.Body>
                            <Table striped bordered hover responsive className="text-center align-middle">
                                <thead><tr><th>ID</th><th>Nombre</th><th>Email</th><th>Teléfono</th><th>Usuario ID</th><th>Estado</th><th>Acciones</th></tr></thead>
                                <tbody>
                                    {clientes.map((client) => (
                                        <tr key={client.id}>
                                            <td>{client.id}</td>
                                            <td>{client.nombre} {client.apellido}</td>
                                            <td>{client.email}</td>
                                            <td>{client.telefono}</td>
                                            <td>{client.usuarioId}</td>
                                            <td>
                                                {client.estadoActivo
                                                    ? <Badge bg="success"><FontAwesomeIcon icon={faCheckCircle} className="me-1" /> Activo</Badge>
                                                    : <Badge bg="danger"><FontAwesomeIcon icon={faBan} className="me-1" /> Inactivo</Badge>
                                                }
                                            </td>
                                            <td>
                                                <Button variant="info" size="sm" className="me-2" onClick={() => handleEditClient(client)} title="Editar Cliente"><FontAwesomeIcon icon={faEdit} /></Button>
                                                <DropdownButton
                                                    id={`dropdown-estado-cliente-${client.id}`}
                                                    title={<FontAwesomeIcon icon={client.estadoActivo ? faToggleOn : faToggleOff} />}
                                                    variant={client.estadoActivo ? 'outline-success' : 'outline-danger'}
                                                    size="sm"
                                                    className="d-inline-block"
                                                >
                                                    <Dropdown.Item onClick={() => handleUpdateClientState(client, true)} disabled={client.estadoActivo}>Activo</Dropdown.Item>
                                                    <Dropdown.Item onClick={() => handleUpdateClientState(client, false)} disabled={!client.estadoActivo}>Inactivo</Dropdown.Item>
                                                </DropdownButton>
                                            </td>
                                        </tr>
                                    ))}
                                </tbody>
                            </Table>
                        </Card.Body>
                    </Card>
                </Tab>
            </Tabs>
            <UserForm show={showUserForm} handleClose={() => setShowUserForm(false)} onSave={handleFormSubmit} userToEdit={editingUser} />
            <ClientForm show={showClientForm} handleClose={() => setShowClientForm(false)} onSave={handleFormSubmit} clientToEdit={editingClient} />
            <Modal show={showRoleModal} onHide={() => setShowRoleModal(false)} centered>
                <Modal.Header closeButton>
                    <Modal.Title>Cambiar Rol de Empleado</Modal.Title>
                </Modal.Header>
                <Modal.Body>
                    <p>
                        Cambiando el rol para el usuario: <strong>{selectedUserForRole?.username}</strong>
                    </p>
                    <Form.Group>
                        <Form.Label>Nuevo Rol Específico:</Form.Label>
                        <Form.Select
                            value={newEmployeeRole}
                            onChange={(e) => setNewEmployeeRole(e.target.value as 'CAJERO' | 'COCINA' | 'DELIVERY')}
                        >
                            <option value="CAJERO">Cajero</option>
                            <option value="COCINA">Cocina</option>
                            <option value="DELIVERY">Delivery</option>
                        </Form.Select>
                    </Form.Group>
                </Modal.Body>
                <Modal.Footer>
                    <Button variant="secondary" onClick={() => setShowRoleModal(false)}>
                        Cancelar
                    </Button>
                    <Button variant="primary" onClick={handleRoleChange}>
                        Guardar Cambios
                    </Button>
                </Modal.Footer>
            </Modal>
        </Container>
    );
};

export default ManageUsersPage;
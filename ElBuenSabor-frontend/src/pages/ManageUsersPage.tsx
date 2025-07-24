import React, { useEffect, useState, useCallback } from 'react';
import { Container, Card, Button, Table, Spinner, Alert, Tabs, Tab, Dropdown, DropdownButton, Badge } from 'react-bootstrap';
import { useAuth0 } from '@auth0/auth0-react';
import { ClienteUsuarioService } from '../services/clienteUsuarioService';
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

    const handleSoftDeleteClient = async (id: number) => {
        if (!window.confirm(`¿Estás seguro de que quieres DESACTIVAR el cliente ID ${id}?`)) {
            return;
        }

        try {
            const token = await getAccessTokenSilently();
            await clienteUsuarioService.softDeleteCliente(id, token);
            toast.success('Cliente desactivado con éxito.');
            fetchData();

        } catch (err: any) {
            toast.error(`Error: ${err.message || 'No se pudo desactivar el cliente.'}`);
        }
    };

    const handleUpdateUserRole = async (id: number, newRole: Rol) => {
        if (!window.confirm(`¿Seguro que quieres cambiar el rol del usuario ID ${id} a ${newRole}?`)) return;

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
            alert('Rol de usuario actualizado con éxito.');
            fetchData();
        } catch (err: any) {
            alert(`Error: ${err.message}`);
        }
    };

    const handleEditClient = (client: ClienteResponse) => {
        setEditingClient(client);
        setShowClientForm(true);
    };

    const handleUpdateClientState = async (id: number, estadoActual: boolean) => {
        if (!window.confirm(`¿Estás seguro de que quieres ${estadoActual ? 'desactivar' : 'reactivar'} el cliente ID ${id}?`)) return;

        try {
            const token = await getAccessTokenSilently();
            const clientToUpdate = clientes.find(c => c.id === id);
            if (!clientToUpdate) throw new Error("Cliente no encontrado.");
            const updatedData: ClienteRequest = {
                nombre: clientToUpdate.nombre,
                apellido: clientToUpdate.apellido,
                telefono: clientToUpdate.telefono,
                email: clientToUpdate.email,
                fechaNacimiento: clientToUpdate.fechaNacimiento,
                usuarioId: clientToUpdate.usuarioId,
                domicilioIds: clientToUpdate.domicilios?.map((d: DomicilioResponse) => d.id) ?? [],
                estadoActivo: !estadoActual,
            };

            await clienteUsuarioService.updateCliente(id, updatedData, token);
            alert(`Cliente ${!estadoActual ? 'reactivado' : 'desactivado'} con éxito.`);
            fetchData();
        } catch (err: any) {
            alert(`Error: ${err.message}`);
        }
    };

    const handleFormSubmit = () => {
        setShowUserForm(false);
        setShowClientForm(false);
        setEditingUser(null);
        setEditingClient(null);
        fetchData();
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
                                            <td>{user.estadoActivo ? <><FontAwesomeIcon icon={faCheckCircle} className="text-success me-1" /> Activo</> : <><FontAwesomeIcon icon={faBan} className="text-danger me-1" /> Inactivo</>}</td>
                                            <td>
                                                <Button variant="info" size="sm" className="me-2" onClick={() => handleEditUser(user)}><FontAwesomeIcon icon={faEdit} /></Button>
                                                <DropdownButton id={`dropdown-rol-${user.id}`} title={<FontAwesomeIcon icon={faBriefcase} />} variant="secondary" size="sm" className="me-2 d-inline-block">
                                                    {(['ADMIN', 'EMPLEADO', 'CLIENTE'] as Rol[]).map(role => (
                                                        <Dropdown.Item key={role} onClick={() => handleUpdateUserRole(user.id, role)} disabled={user.rol === role}>Cambiar a {role}</Dropdown.Item>
                                                    ))}
                                                </DropdownButton>
                                                <Button variant='danger' size="sm" onClick={() => handleSoftDeleteClient(user.id)} title="Desactivar Usuario"><FontAwesomeIcon icon={faBan} /></Button>
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
                                            <td>{client.estadoActivo ? <><FontAwesomeIcon icon={faCheckCircle} className="text-success me-1" /> Activo</> : <><FontAwesomeIcon icon={faBan} className="text-danger me-1" /> Inactivo</>}</td>
                                            <td>
                                                <Button variant="info" size="sm" className="me-2" onClick={() => handleEditClient(client)}><FontAwesomeIcon icon={faEdit} /></Button>
                                                <Button variant={client.estadoActivo ? 'danger' : 'success'} size="sm" onClick={() => handleUpdateClientState(client.id, client.estadoActivo)}><FontAwesomeIcon icon={client.estadoActivo ? faToggleOff : faToggleOn} /></Button>
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
        </Container>
    );
};

export default ManageUsersPage;

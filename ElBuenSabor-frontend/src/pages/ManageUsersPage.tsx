/**
 * @file ManageUsersPage.tsx
 * @description Página de administración para la gestión de Usuarios y Clientes.
 * Proporciona interfaces separadas por pestañas para listar y realizar operaciones CRUD
 * (Creación, Edición) sobre usuarios y clientes. También permite cambiar el `estadoActivo`
 * de usuarios y clientes (simulando un "soft delete" o reactivación), y modificar roles de usuario.
 * Utiliza modales de formulario (`UserForm`, `ClientForm`) para las operaciones de C/E.
 *
 * @hook `useState`: Gestiona los listados de usuarios y clientes, estados de carga/error,
 * la pestaña activa, y la visibilidad/modo (edición/creación) de los modales de formulario.
 * @hook `useEffect`: Carga inicial de todos los usuarios y clientes al montar la página.
 * @hook `useAuth0`: Para obtener el token de autenticación necesario para las operaciones protegidas del API.
 *
 * @service `ClienteUsuarioService`: Servicios para la gestión de usuarios y clientes.
 *
 * @component `UserForm`, `ClientForm`: Modales de formulario anidados para la creación/edición.
 */
import React, { useEffect, useState } from 'react';
import { Container, Card, Button, Table, Spinner, Alert, Tabs, Tab, Dropdown, DropdownButton } from 'react-bootstrap';
import { useAuth0 } from '@auth0/auth0-react';
import { ClienteUsuarioService } from '../services/clienteUsuarioService';
import type { Usuario, Cliente, Rol, UsuarioRequestDTO, ClienteRequestDTO } from '../types/types';
import { FontAwesomeIcon } from '@fortawesome/react-fontawesome';
import { faPlus, faEdit, faUserShield, faAddressBook, faBriefcase, faToggleOn, faToggleOff, faCheckCircle, faBan } from '@fortawesome/free-solid-svg-icons';
import UserForm from '../components/admin/UserForm';
import ClientForm from '../components/admin/ClientForm';

/**
 * @interface ManageUsersPageProps
 * @description No se requieren propiedades (`props`) para este componente de página de gestión,
 * por lo que se define una interfaz vacía para claridad.
 */
interface ManageUsersPageProps {}

const ManageUsersPage: React.FC<ManageUsersPageProps> = () => {
  const { getAccessTokenSilently } = useAuth0();

  const [usuarios, setUsuarios] = useState<Usuario[]>([]);
  const [clientes, setClientes] = useState<Cliente[]>([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState<string | null>(null);
  const [activeTab, setActiveTab] = useState<'usuarios' | 'clientes'>('usuarios');
  const [showUserForm, setShowUserForm] = useState(false);
  const [editingUser, setEditingUser] = useState<Usuario | null>(null);
  const [showClientForm, setShowClientForm] = useState(false);
  const [editingClient, setEditingClient] = useState<Cliente | null>(null);

  const clienteUsuarioService = new ClienteUsuarioService();

  const fetchData = async () => {
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
    } catch (err) {
      console.error('Error al cargar usuarios/clientes:', err);
      const errorMessage = (err as any).response?.data?.message || (err as any).message || 'Error desconocido al cargar.';
      setError(`No se pudieron cargar usuarios y clientes: ${errorMessage}. Asegúrate de tener los permisos de ADMIN.`);
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => {
    fetchData();
  }, []);

  // ==============================================================
  // --- Manejo de Usuarios ---
  // ==============================================================

  const handleEditUser = (user: Usuario) => {
    setEditingUser(user);
    setShowUserForm(true);
  };

  const handleSoftDeleteUser = async (id: number | undefined, estadoActual: boolean) => {
    if (id === undefined) {
      alert('Error: ID de usuario no proporcionado para cambiar estado.');
      return;
    }
    if (!window.confirm(`¿Estás seguro de que quieres ${estadoActual ? 'desactivar' : 'reactivar'} el usuario ID ${id}?`)) {
      return;
    }
    try {
      const token = await getAccessTokenSilently();
      const userToUpdate = usuarios.find(u => u.id === id);
      if (!userToUpdate) {
        alert("Usuario no encontrado para actualizar estado.");
        return;
      }

      const newStatus = !estadoActual;
      const updatedData: UsuarioRequestDTO = {
        auth0Id: userToUpdate.auth0Id,
        username: userToUpdate.username,
        rol: userToUpdate.rol,
        estadoActivo: newStatus,
        id: id // Asegurar que el ID esté presente en el DTO de request
      };

      await clienteUsuarioService.updateUsuario(id, updatedData, token);
      alert(`Usuario ${newStatus ? 'reactivado' : 'desactivado'} con éxito.`);
      fetchData();
    } catch (err) {
      console.error('Error al cambiar estado de usuario:', err);
      const errorMessage = (err as any).response?.data?.message || (err as any).message || 'Error desconocido al cambiar estado.';
      alert(`Error al cambiar estado: ${errorMessage}`);
    }
  };

  const handleUpdateUserRole = async (id: number | undefined, newRole: Rol) => {
    if (id === undefined) {
      alert('Error: ID de usuario no proporcionado para cambiar rol.');
      return;
    }
    if (!window.confirm(`¿Estás seguro de que quieres cambiar el rol del usuario ID ${id} a ${newRole}?`)) {
      return;
    }
    try {
      const token = await getAccessTokenSilently();
      const userToUpdate = usuarios.find(u => u.id === id);
      if (!userToUpdate) {
        alert("Usuario no encontrado para actualizar rol.");
        return;
      }
      const updatedData: UsuarioRequestDTO = {
        auth0Id: userToUpdate.auth0Id,
        username: userToUpdate.username,
        rol: newRole,
        estadoActivo: userToUpdate.estadoActivo,
        id: id
      };
      await clienteUsuarioService.updateUsuario(id, updatedData, token);
      alert('Rol de usuario actualizado con éxito.');
      fetchData();
    } catch (err) {
      console.error('Error al actualizar rol de usuario:', err);
      const errorMessage = (err as any).response?.data?.message || (err as any).message || 'Error desconocido al actualizar rol.';
      alert(`Error al actualizar rol: ${errorMessage}`);
    }
  };

  // ==============================================================
  // --- Manejo de Clientes ---
  // ==============================================================

  const handleEditClient = (client: Cliente) => {
    setEditingClient(client);
    setShowClientForm(true);
  };

  const handleSoftDeleteClient = async (id: number | undefined, estadoActual: boolean) => {
    if (id === undefined) {
      alert('Error: ID de cliente no proporcionado para cambiar estado.');
      return;
    }
    if (!window.confirm(`¿Estás seguro de que quieres ${estadoActual ? 'desactivar' : 'reactivar'} el cliente ID ${id}?`)) {
      return;
    }
    try {
      const token = await getAccessTokenSilently();
      const clientToUpdate = clientes.find(c => c.id === id);
      if (!clientToUpdate) {
        alert("Cliente no encontrado para actualizar estado.");
        return;
      }

      const newStatus = !estadoActual;
      const updatedData: ClienteRequestDTO = {
        nombre: clientToUpdate.nombre,
        apellido: clientToUpdate.apellido,
        telefono: clientToUpdate.telefono,
        email: clientToUpdate.email,
        fechaNacimiento: clientToUpdate.fechaNacimiento,
        // CORRECCIÓN PARA usuarioId: aseguramos que sea un number
        usuarioId: clientToUpdate.usuario.id ?? 0, // Usamos ?? 0 como fallback, o podrías decidir lanzar un error si 0 no es válido.
        // CORRECCIÓN PARA domicilioIds: filtramos para asegurar que cada domicilio tiene un ID definido
        domicilioIds: clientToUpdate.domicilios
          ?.filter((d): d is (typeof d & { id: number }) => d.id !== undefined) // Filtra para asegurar `d.id` no es undefined
          .map(d => d.id) ?? [], // Mapea los IDs y usa [] como fallback si `domicilios` es null/undefined
        estadoActivo: newStatus,
        id: id // Asegurar que el ID esté presente en el DTO de request
      };

      await clienteUsuarioService.updateCliente(id, updatedData, token);
      alert(`Cliente ${newStatus ? 'reactivado' : 'desactivado'} con éxito.`);
      fetchData();
    } catch (err) {
      console.error('Error al cambiar estado de cliente:', err);
      const errorMessage = (err as any).response?.data?.message || (err as any).message || 'Error desconocido al cambiar estado.';
      alert(`Error al cambiar estado: ${errorMessage}`);
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
    return (
      <Container className="text-center my-5">
        <Spinner animation="border" role="status" />
        <p className="mt-3">Cargando usuarios y clientes para la gestión...</p>
      </Container>
    );
  }

  if (error) {
    return (
      <Container className="my-5 text-center">
        <Alert variant="danger">{error}</Alert>
        <Button variant="primary" onClick={fetchData} className="mt-3">Reintentar Carga</Button>
      </Container>
    );
  }

  return (
    <Container className="my-4">
      <h1 className="text-center mb-4">Gestión de Usuarios y Clientes</h1>

      <Tabs
        activeKey={activeTab}
        onSelect={(k) => setActiveTab(k as 'usuarios' | 'clientes')}
        className="mb-3 justify-content-center"
      >
        <Tab
          eventKey="usuarios"
          title={<span><FontAwesomeIcon icon={faUserShield} className="me-2" />Usuarios</span>}
        >
          <Card className="shadow-sm">
            <Card.Header className="d-flex justify-content-between align-items-center">
              <h5>Listado de Usuarios</h5>
              <Button variant="success" onClick={() => { setEditingUser(null); setShowUserForm(true); }}>
                <FontAwesomeIcon icon={faPlus} className="me-2" />Nuevo Usuario
              </Button>
            </Card.Header>
            <Card.Body>
              {usuarios.length === 0 ? (
                <Alert variant="info" className="text-center">No hay usuarios registrados.</Alert>
              ) : (
                <Table striped bordered hover responsive className="text-center align-middle">
                  <thead>
                    <tr>
                      <th>ID</th>
                      <th>Username</th>
                      <th>Auth0 ID</th>
                      <th>Rol</th>
                      <th>Estado</th>
                      <th>Acciones</th>
                    </tr>
                  </thead>
                  <tbody>
                    {usuarios.map((user) => (
                      <tr key={user.id ?? `user-${user.username}`}>
                        <td>{user.id ?? 'N/A'}</td>
                        <td>{user.username}</td>
                        <td>{user.auth0Id.substring(user.auth0Id.indexOf('|') + 1, user.auth0Id.indexOf('|') + 7)}...</td>
                        <td>
                          <span className={`badge bg-${user.rol === 'ADMIN' ? 'danger' : user.rol === 'EMPLEADO' ? 'info' : 'secondary'}`}>
                            {user.rol}
                          </span>
                        </td>
                        <td>
                          {user.estadoActivo ? <FontAwesomeIcon icon={faCheckCircle} className="text-success me-1" /> : <FontAwesomeIcon icon={faBan} className="text-danger me-1" />}
                          {user.estadoActivo ? 'Activo' : 'Inactivo'}
                        </td>
                        <td>
                          <Button variant="info" size="sm" className="me-2" onClick={() => handleEditUser(user)}>
                            <FontAwesomeIcon icon={faEdit} className="me-1" /> Editar
                          </Button>
                          <DropdownButton
                            id={`dropdown-rol-${user.id}`}
                            title={<><FontAwesomeIcon icon={faBriefcase} className="me-1" /> Rol</>}
                            variant="secondary"
                            size="sm"
                            className="me-2"
                          >
                            {(['ADMIN', 'EMPLEADO', 'CLIENTE'] as Rol[]).map(role => (
                              <Dropdown.Item
                                key={role}
                                onClick={() => handleUpdateUserRole(user.id, role)}
                                disabled={user.rol === role}
                              >
                                Cambiar a {role}
                              </Dropdown.Item>
                            ))}
                          </DropdownButton>
                          <Button
                            variant={user.estadoActivo ? 'danger' : 'success'}
                            size="sm"
                            onClick={() => handleSoftDeleteUser(user.id, user.estadoActivo)}
                          >
                            <FontAwesomeIcon icon={user.estadoActivo ? faToggleOff : faToggleOn} className="me-1" />
                            {user.estadoActivo ? 'Desactivar' : 'Activar'}
                          </Button>
                        </td>
                      </tr>
                    ))}
                  </tbody>
                </Table>
              )}
            </Card.Body>
          </Card>
        </Tab>

        <Tab
          eventKey="clientes"
          title={<span><FontAwesomeIcon icon={faAddressBook} className="me-2" />Clientes</span>}
        >
          <Card className="shadow-sm">
            <Card.Header className="d-flex justify-content-between align-items-center">
              <h5>Listado de Clientes</h5>
              <Button variant="success" onClick={() => { setEditingClient(null); setShowClientForm(true); }}>
                <FontAwesomeIcon icon={faPlus} className="me-2" />Nuevo Cliente
              </Button>
            </Card.Header>
            <Card.Body>
              {clientes.length === 0 ? (
                <Alert variant="info" className="text-center">No hay clientes registrados.</Alert>
              ) : (
                <Table striped bordered hover responsive className="text-center align-middle">
                  <thead>
                    <tr>
                      <th>ID</th>
                      <th>Nombre</th>
                      <th>Apellido</th>
                      <th>Email</th>
                      <th>Teléfono</th>
                      <th>Usuario ID</th>
                      <th>Estado</th>
                      <th>Acciones</th>
                    </tr>
                  </thead>
                  <tbody>
                    {clientes.map((client) => (
                      <tr key={client.id ?? `client-${client.email}`}>
                        <td>{client.id ?? 'N/A'}</td>
                        <td>{client.nombre}</td>
                        <td>{client.apellido}</td>
                        <td>{client.email}</td>
                        <td>{client.telefono}</td>
                        <td>
                          {client.usuario?.id ?? 'N/A'} ({client.usuario?.auth0Id ? client.usuario.auth0Id.substring(client.usuario.auth0Id.indexOf('|') + 1, client.usuario.auth0Id.indexOf('|') + 7) + '...' : 'N/A'})
                        </td>
                        <td>
                          {client.estadoActivo ? <FontAwesomeIcon icon={faCheckCircle} className="text-success me-1" /> : <FontAwesomeIcon icon={faBan} className="text-danger me-1" />}
                          {client.estadoActivo ? 'Activo' : 'Inactivo'}
                        </td>
                        <td>
                          <Button variant="info" size="sm" className="me-2" onClick={() => handleEditClient(client)}>
                            <FontAwesomeIcon icon={faEdit} className="me-1" /> Editar
                          </Button>
                          <Button
                            variant={client.estadoActivo ? 'danger' : 'success'}
                            size="sm"
                            onClick={() => handleSoftDeleteClient(client.id, client.estadoActivo)}
                          >
                            <FontAwesomeIcon icon={client.estadoActivo ? faToggleOff : faToggleOn} className="me-1" />
                            {client.estadoActivo ? 'Desactivar' : 'Activar'}
                          </Button>
                        </td>
                      </tr>
                    ))}
                  </tbody>
                </Table>
              )}
            </Card.Body>
          </Card>
        </Tab>
      </Tabs>

      <UserForm
        show={showUserForm}
        handleClose={() => setShowUserForm(false)}
        onSave={handleFormSubmit}
        userToEdit={editingUser}
      />
      <ClientForm
        show={showClientForm}
        handleClose={() => setShowClientForm(false)}
        onSave={handleFormSubmit}
        clientToEdit={editingClient}
      />
    </Container>
  );
};

export default ManageUsersPage;
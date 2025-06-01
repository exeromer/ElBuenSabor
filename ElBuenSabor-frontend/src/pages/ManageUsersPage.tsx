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
 * @service `getAllUsuarios`, `updateUsuario`: Servicios para la gestión de usuarios.
 * @service `getAllClientes`, `updateCliente`: Servicios para la gestión de clientes.
 *
 * @component `UserForm`, `ClientForm`: Modales de formulario anidados para la creación/edición.
 */
import React, { useEffect, useState } from 'react';
import { Container, Card, Button, Table, Spinner, Alert, Tabs, Tab, Dropdown, DropdownButton } from 'react-bootstrap';
import { useAuth0 } from '@auth0/auth0-react';
import { getAllUsuarios, updateUsuario, getAllClientes, updateCliente} from '../services/clienteUsuarioService'; 
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
  /**
   * @hook useAuth0
   * @description Hook para obtener el token de acceso de Auth0, necesario para autenticar
   * las peticiones al backend.
   */
  const { getAccessTokenSilently } = useAuth0();

  /**
   * @state usuarios
   * @description Lista de objetos `Usuario` obtenidos del backend.
   */
  const [usuarios, setUsuarios] = useState<Usuario[]>([]);

  /**
   * @state clientes
   * @description Lista de objetos `Cliente` obtenidos del backend.
   */
  const [clientes, setClientes] = useState<Cliente[]>([]);

  /**
   * @state loading
   * @description Estado booleano para indicar si los datos de usuarios y clientes están cargando.
   */
  const [loading, setLoading] = useState(true);

  /**
   * @state error
   * @description Almacena un mensaje de error si ocurre un problema durante la carga de datos.
   */
  const [error, setError] = useState<string | null>(null);

  /**
   * @state activeTab
   * @description Controla qué pestaña está activa ('usuarios' o 'clientes').
   */
  const [activeTab, setActiveTab] = useState<'usuarios' | 'clientes'>('usuarios');

  /**
   * @state showUserForm
   * @description Controla la visibilidad del modal `UserForm`.
   */
  const [showUserForm, setShowUserForm] = useState(false);

  /**
   * @state editingUser
   * @description Almacena el objeto `Usuario` que se está editando en el formulario.
   * Si es `null`, el formulario está en modo creación.
   */
  const [editingUser, setEditingUser] = useState<Usuario | null>(null);

  /**
   * @state showClientForm
   * @description Controla la visibilidad del modal `ClientForm`.
   */
  const [showClientForm, setShowClientForm] = useState(false);

  /**
   * @state editingClient
   * @description Almacena el objeto `Cliente` que se está editando en el formulario.
   * Si es `null`, el formulario está en modo creación.
   */
  const [editingClient, setEditingClient] = useState<Cliente | null>(null);

  /**
   * @function fetchData
   * @description Función asíncrona para cargar todos los usuarios y clientes del backend.
   * Actualiza los estados `usuarios`, `clientes`, `loading` y `error`.
   */
  const fetchData = async () => {
    setLoading(true);
    setError(null);
    try {
      const token = await getAccessTokenSilently();
      // Realiza las dos llamadas API en paralelo para mayor eficiencia
      const [fetchedUsuarios, fetchedClientes] = await Promise.all([
        getAllUsuarios(token),
        getAllClientes(token),
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

  /**
   * @hook useEffect
   * @description Hook que se ejecuta una vez al montar el componente para realizar la carga inicial de datos.
   */
  useEffect(() => {
    fetchData();
  }, []); // Dependencias vacías: se ejecuta solo una vez al montar

  // ==============================================================
  // --- Manejo de Usuarios ---
  // ==============================================================

  /**
   * @function handleEditUser
   * @description Prepara el modal para editar un `Usuario` específico.
   * @param {Usuario} user - El objeto `Usuario` a editar.
   */
  const handleEditUser = (user: Usuario) => {
    setEditingUser(user);
    setShowUserForm(true);
  };

  /**
   * @function handleSoftDeleteUser
   * @description Cambia el estado `estadoActivo` de un usuario (desactivar/reactivar).
   * Esto es un "borrado lógico" si el backend maneja el `estadoActivo` y `fechaBaja`.
   * @param {number} id - El ID del usuario cuyo estado se cambiará.
   * @param {boolean} estadoActual - El estado activo actual del usuario.
   */
  const handleSoftDeleteUser = async (id: number, estadoActual: boolean) => {
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

      const newStatus = !estadoActual; // Invierte el estado actual
      const updatedData: UsuarioRequestDTO = {
        auth0Id: userToUpdate.auth0Id,
        username: userToUpdate.username,
        rol: userToUpdate.rol,
        estadoActivo: newStatus,
      };

      await updateUsuario(id, updatedData, token); // Llama a la función de actualización
      alert(`Usuario ${newStatus ? 'reactivado' : 'desactivado'} con éxito.`);
      fetchData(); // Recargar los datos para reflejar el cambio
    } catch (err) {
      console.error('Error al cambiar estado de usuario:', err);
      const errorMessage = (err as any).response?.data?.message || (err as any).message || 'Error desconocido al cambiar estado.';
      alert(`Error al cambiar estado: ${errorMessage}`);
    }
  };

  /**
   * @function handleUpdateUserRole
   * @description Cambia el `rol` de un usuario específico.
   * @param {number} id - El ID del usuario cuyo rol se cambiará.
   * @param {Rol} newRole - El nuevo rol a asignar ('ADMIN', 'EMPLEADO', 'CLIENTE').
   */
  const handleUpdateUserRole = async (id: number, newRole: Rol) => {
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
      };
      await updateUsuario(id, updatedData, token);
      alert('Rol de usuario actualizado con éxito.');
      fetchData(); // Recargar los datos para reflejar el cambio
    } catch (err) {
      console.error('Error al actualizar rol de usuario:', err);
      const errorMessage = (err as any).response?.data?.message || (err as any).message || 'Error desconocido al actualizar rol.';
      alert(`Error al actualizar rol: ${errorMessage}`);
    }
  };

  // ==============================================================
  // --- Manejo de Clientes ---
  // ==============================================================

  /**
   * @function handleEditClient
   * @description Prepara el modal para editar un `Cliente` específico.
   * @param {Cliente} client - El objeto `Cliente` a editar.
   */
  const handleEditClient = (client: Cliente) => {
    setEditingClient(client);
    setShowClientForm(true);
  };

  /**
   * @function handleSoftDeleteClient
   * @description Cambia el estado `estadoActivo` de un cliente (desactivar/reactivar).
   * Similar al "soft delete" de usuarios.
   * @param {number} id - El ID del cliente cuyo estado se cambiará.
   * @param {boolean} estadoActual - El estado activo actual del cliente.
   */
  const handleSoftDeleteClient = async (id: number, estadoActual: boolean) => {
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

      const newStatus = !estadoActual; // Invierte el estado actual
      const updatedData: ClienteRequestDTO = {
        nombre: clientToUpdate.nombre,
        apellido: clientToUpdate.apellido,
        telefono: clientToUpdate.telefono,
        email: clientToUpdate.email,
        fechaNacimiento: clientToUpdate.fechaNacimiento,
        usuarioId: clientToUpdate.usuario.id,
        domicilioIds: clientToUpdate.domicilios.map((d: { id: any; }) => d.id), // Asegura que los IDs de domicilio se mantengan
        estadoActivo: newStatus,
      };

      await updateCliente(id, updatedData, token);
      alert(`Cliente ${newStatus ? 'reactivado' : 'desactivado'} con éxito.`);
      fetchData(); // Recargar los datos para reflejar el cambio
    } catch (err) {
      console.error('Error al cambiar estado de cliente:', err);
      const errorMessage = (err as any).response?.data?.message || (err as any).message || 'Error desconocido al cambiar estado.';
      alert(`Error al cambiar estado: ${errorMessage}`);
    }
  };

  /**
   * @function handleFormSubmit
   * @description Callback que se ejecuta cuando un formulario (de usuario o cliente)
   * se guarda exitosamente. Cierra los modales, resetea los estados de edición,
   * y recarga los datos de usuarios/clientes para reflejar los cambios.
   */
  const handleFormSubmit = () => {
    setShowUserForm(false);
    setShowClientForm(false);
    setEditingUser(null);
    setEditingClient(null);
    fetchData(); // Recargar todos los datos para reflejar los cambios
  };

  // --- Renderizado condicional basado en estados de carga o error ---
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

      {/* Componente Tabs para alternar entre Usuarios y Clientes */}
      <Tabs
        activeKey={activeTab}
        onSelect={(k) => setActiveTab(k as 'usuarios' | 'clientes')}
        className="mb-3 justify-content-center"
      >
        {/* Pestaña para Usuarios */}
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
                      <tr key={user.id}>
                        <td>{user.id}</td>
                        <td>{user.username}</td>
                        {/* Se muestra un fragmento del Auth0 ID para mayor claridad */}
                        <td>{user.auth0Id.substring(user.auth0Id.indexOf('|') + 1, user.auth0Id.indexOf('|') + 7)}...</td>
                        <td>
                          {/* Badge de Bootstrap para el rol, con colores según el rol */}
                          <span className={`badge bg-${user.rol === 'ADMIN' ? 'danger' : user.rol === 'EMPLEADO' ? 'info' : 'secondary'}`}>
                            {user.rol}
                          </span>
                        </td>
                        <td>
                          {/* Icono y texto para el estado activo/inactivo */}
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
                            {/* Opciones de rol: ADMIN, EMPLEADO, CLIENTE */}
                            {(['ADMIN', 'EMPLEADO', 'CLIENTE'] as Rol[]).map(role => (
                              <Dropdown.Item key={role} onClick={() => handleUpdateUserRole(user.id, role)} disabled={user.rol === role}>
                                Cambiar a {role}
                              </Dropdown.Item>
                            ))}
                          </DropdownButton>
                          <Button
                            variant={user.estadoActivo ? 'danger' : 'success'}
                            size="sm"
                            onClick={() => handleSoftDeleteUser(user.id, user.estadoActivo)}
                          >
                            {/* Icono y texto para activar/desactivar */}
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

        {/* Pestaña para Clientes */}
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
                      <tr key={client.id}>
                        <td>{client.id}</td>
                        <td>{client.nombre}</td>
                        <td>{client.apellido}</td>
                        <td>{client.email}</td>
                        <td>{client.telefono}</td>
                        <td>
                          {/* Muestra el ID del usuario y un fragmento de su Auth0 ID si existe */}
                          {client.usuario.id} ({client.usuario.auth0Id ? client.usuario.auth0Id.substring(client.usuario.auth0Id.indexOf('|') + 1, client.usuario.auth0Id.indexOf('|') + 7) + '...' : 'N/A'})
                        </td>
                        <td>
                          {/* Icono y texto para el estado activo/inactivo */}
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
                            {/* Icono y texto para activar/desactivar */}
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

      {/* Modales para formularios de Usuario y Cliente */}
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
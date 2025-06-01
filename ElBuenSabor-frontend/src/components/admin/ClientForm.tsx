/**
 * @file ClientForm.tsx
 * @description Componente de formulario modal para la creación y edición de Clientes.
 * Permite a los administradores gestionar los datos de un cliente, incluyendo su información personal,
 * la asociación con un `Usuario` existente (garantizando que un usuario solo pueda ser cliente una vez),
 * y la gestión de múltiples `Domicilios` asociados al cliente a través de un sub-formulario (`DomicilioForm`).
 *
 * @hook `useState`: Gestiona el estado del formulario principal (`formData`), las listas de usuarios disponibles,
 * estados de carga/envío, errores, la lista de domicilios seleccionados, y el control del modal de Domicilio.
 * @hook `useEffect`: Carga los usuarios disponibles y los domicilios asociados al cliente (si es edición)
 * al montar el componente o cuando el modal se abre/cambia el cliente a editar. También sincroniza los IDs de domicilio.
 * @hook `useAuth0`: Obtiene el token de autenticación para las operaciones protegidas de la API.
 * @component `DomicilioForm`: Componente anidado utilizado para crear o editar domicilios individualmente.
 */
import React, { useState, useEffect } from 'react';
import { Modal, Form, Button, Alert, Spinner, Row, Col, Card, ListGroup } from 'react-bootstrap';
import { useAuth0 } from '@auth0/auth0-react';
import {
  createCliente,
  updateCliente,
  getAllUsuarios,
  getAllClientes,
} from '../../services/clienteUsuarioService';
// No se usan directamente createDomicilio, updateDomicilio, deleteDomicilio aquí,
// ya que DomicilioForm es quien maneja eso. Se eliminan las importaciones redundantes.
// import { createDomicilio, updateDomicilio, deleteDomicilio } from '../../services/domicilioService';
import type {
  Cliente,
  ClienteRequestDTO,
  Usuario,
  Domicilio,
  // Los tipos Pais, Provincia, Localidad, DomicilioRequestDTO son usados por DomicilioForm,
  // no directamente por ClientForm, por lo que no es necesario importarlos aquí.
  // Pais, Provincia, Localidad, DomicilioRequestDTO
} from '../../types/types'; // Asume que types.ts es la ruta correcta
import { FontAwesomeIcon } from '@fortawesome/react-fontawesome';
import { faPlusCircle, faTrash, faMapMarkerAlt, faEdit } from '@fortawesome/free-solid-svg-icons';
import { format, parseISO } from 'date-fns'; // Para formatear y parsear fechas
import DomicilioForm from './DomicilioForm'; // Importa el componente del sub-formulario de domicilio

/**
 * @interface ClientFormProps
 * @description Propiedades que el componente `ClientForm` espera recibir.
 * @property {boolean} show - Controla la visibilidad del modal.
 * @property {() => void} handleClose - Función para cerrar el modal.
 * @property {() => void} onSave - Callback que se ejecuta después de guardar exitosamente un cliente.
 * @property {Cliente | null} [clientToEdit] - Objeto Cliente a editar. Si es `null` o `undefined`, se asume modo creación.
 */
interface ClientFormProps {
  show: boolean;
  handleClose: () => void;
  onSave: () => void;
  clientToEdit?: Cliente | null;
}

const ClientForm: React.FC<ClientFormProps> = ({ show, handleClose, onSave, clientToEdit }) => {
  /**
   * @hook useAuth0
   * @description Hook para obtener el token de acceso de Auth0, necesario para autenticar
   * las peticiones al backend.
   */
  const { getAccessTokenSilently } = useAuth0();

  /**
   * @state formData
   * @description Estado que almacena los datos del formulario principal del Cliente.
   * Utiliza `ClienteRequestDTO` para tipificar los datos que se enviarán al backend.
   */
  const [formData, setFormData] = useState<ClienteRequestDTO>({
    nombre: '',
    apellido: '',
    telefono: '',
    email: '',
    fechaNacimiento: '', // Inicializa como string vacío, que se puede mapear a null en el DTO final
    usuarioId: 0,
    domicilioIds: [], // Solo se guardan los IDs de los domicilios
    estadoActivo: true,
  });

  /**
   * @state availableUsers
   * @description Lista de usuarios que están disponibles para ser asociados a un cliente.
   * Se filtra para excluir usuarios que ya tienen un cliente asociado, a menos que sea el usuario
   * del cliente que se está editando.
   */
  const [availableUsers, setAvailableUsers] = useState<Usuario[]>([]);

  /**
   * @state loadingOptions
   * @description Estado booleano para indicar si las opciones (usuarios, domicilios)
   * están siendo cargadas del backend.
   */
  const [loadingOptions, setLoadingOptions] = useState(true);

  /**
   * @state submitting
   * @description Estado booleano para indicar si el formulario está en proceso de envío (creación/actualización).
   * Se utiliza para deshabilitar botones y mostrar un spinner.
   */
  const [submitting, setSubmitting] = useState(false);

  /**
   * @state error
   * @description Estado para almacenar cualquier mensaje de error que ocurra durante la carga de opciones
   * o el envío del formulario.
   */
  const [error, setError] = useState<string | null>(null);

  /**
   * @state selectedDomicilios
   * @description Almacena los objetos completos de los domicilios asociados al cliente.
   * Se utiliza para mostrar los detalles del domicilio en el formulario principal y gestionar su edición/eliminación.
   * Es diferente de `formData.domicilioIds` que solo contiene los IDs.
   */
  const [selectedDomicilios, setSelectedDomicilios] = useState<Domicilio[]>([]);

  /**
   * @state showDomicilioForm
   * @description Controla la visibilidad del modal del formulario de Domicilios (`DomicilioForm`).
   */
  const [showDomicilioForm, setShowDomicilioForm] = useState(false);

  /**
   * @state editingDomicilio
   * @description Almacena el objeto Domicilio que se está editando en el `DomicilioForm`.
   * Si es `null`, el `DomicilioForm` se abre en modo creación.
   */
  const [editingDomicilio, setEditingDomicilio] = useState<Domicilio | null>(null);

  /**
   * @hook useEffect
   * @description Hook que se ejecuta al montar el componente y cada vez que el modal se muestra
   * o el `clientToEdit` cambia. Se encarga de:
   * 1. Cargar la lista de todos los usuarios.
   * 2. Cargar la lista de todos los clientes para identificar qué usuarios ya están asociados.
   * 3. Filtrar los usuarios disponibles para asociación (aquellos que no son clientes, o el usuario del cliente que se está editando).
   * 4. Precargar los domicilios del cliente si se está en modo edición.
   */
  useEffect(() => {
    const loadOptions = async () => {
      setLoadingOptions(true);
      try {
        const token = await getAccessTokenSilently();
        // Carga todos los usuarios y todos los clientes en paralelo
        const [users, clients] = await Promise.all([
          getAllUsuarios(token),
          getAllClientes(token),
        ]);

        // Identifica los IDs de usuario que ya están asociados a un cliente
        const associatedUserIds = new Set(clients.map(c => c.usuario.id));

        // Filtra los usuarios para que solo se puedan asociar aquellos que no son clientes,
        // o si es edición, el usuario que ya está asociado a este cliente.
        const filteredUsers = users.filter(u =>
          !associatedUserIds.has(u.id) || (clientToEdit && u.id === clientToEdit.usuario.id)
        );
        setAvailableUsers(filteredUsers);

        // Si estamos editando un cliente, precargamos sus domicilios
        if (clientToEdit) {
          setSelectedDomicilios(clientToEdit.domicilios);
        } else {
          // Si es un nuevo cliente, asegura que no haya domicilios precargados
          setSelectedDomicilios([]);
        }

      } catch (err) {
        setError('Error al cargar opciones de usuarios o domicilios.');
        console.error('Error en loadOptions:', err);
      } finally {
        setLoadingOptions(false);
      }
    };
    // Asegura que las opciones se carguen solo cuando el modal se muestra
    // o el `clientToEdit` cambia, previniendo recargas innecesarias.
    if (show) {
      loadOptions();
    }
  }, [getAccessTokenSilently, clientToEdit, show]); // Dependencias para re-ejecutar el efecto

  /**
   * @hook useEffect
   * @description Hook para inicializar `formData` y `selectedDomicilios` cuando el modal
   * se muestra o el `clientToEdit` cambia. Esto asegura que el formulario esté listo
   * para crear un nuevo cliente o para editar uno existente.
   */
  useEffect(() => {
    if (show) { // Solo actualiza el estado si el modal está visible
      if (clientToEdit) {
        // Mapea la entidad Cliente a ClienteRequestDTO para el estado del formulario
        setFormData({
          nombre: clientToEdit.nombre,
          apellido: clientToEdit.apellido,
          telefono: clientToEdit.telefono,
          email: clientToEdit.email,
          // Formatea la fecha de nacimiento si existe, de lo contrario, string vacío.
          // El `?.trim() !== ''` asegura que no se use un string vacío como fecha válida.
          fechaNacimiento: (clientToEdit.fechaNacimiento && clientToEdit.fechaNacimiento.trim() !== '')
            ? format(parseISO(clientToEdit.fechaNacimiento), 'yyyy-MM-dd')
            : '',
          usuarioId: clientToEdit.usuario.id,
          // Se toma el ID de los domicilios existentes. `selectedDomicilios` se usa para la lista completa.
          domicilioIds: clientToEdit.domicilios.map(d => d.id),
          estadoActivo: clientToEdit.estadoActivo,
        });
        setSelectedDomicilios(clientToEdit.domicilios); // Carga los objetos completos de domicilios
      } else {
        // Resetea el formulario para un nuevo cliente
        setFormData({
          nombre: '',
          apellido: '',
          telefono: '',
          email: '',
          fechaNacimiento: '',
          usuarioId: 0,
          domicilioIds: [],
          estadoActivo: true,
        });
        setSelectedDomicilios([]); // Resetea la lista de domicilios seleccionados
      }
      setError(null); // Limpia cualquier error anterior
    }
  }, [clientToEdit, show]); // Dependencias para re-ejecutar el efecto

  /**
   * @hook useEffect
   * @description Sincroniza `formData.domicilioIds` cada vez que `selectedDomicilios` cambia.
   * Esto asegura que `formData` siempre tenga la lista actualizada de IDs de domicilio
   * antes de enviar el formulario. Se filtra por `d != null` para evitar posibles nulos/undefined.
   */
  useEffect(() => {
    setFormData(prev => ({
      ...prev,
      domicilioIds: selectedDomicilios.filter(d => d != null).map(d => d.id),
    }));
  }, [selectedDomicilios]);

  /**
   * @function handleChange
   * @description Manejador genérico para cambios en los campos de texto, select y checkboxes del formulario principal.
   * Actualiza el estado `formData` basándose en el `name` del campo y su `value` o `checked` estado.
   * @param {React.ChangeEvent<HTMLInputElement | HTMLSelectElement | HTMLTextAreaElement>} e - Evento de cambio.
   */
  const handleChange = (e: React.ChangeEvent<HTMLInputElement | HTMLSelectElement | HTMLTextAreaElement>) => {
    const { name, value } = e.target;
    const isCheckbox = (e.target as HTMLInputElement).type === 'checkbox';
    const checked = (e.target as HTMLInputElement).checked;

    setFormData((prev) => ({
      ...prev,
      // Si es un checkbox, usa `checked`, de lo contrario, usa `value`.
      // Si el nombre es 'usuarioId', convierte a número.
      [name]: isCheckbox ? checked : (name === 'usuarioId' ? Number(value) : value),
    }));
  };

  /**
   * @function handleAddDomicilio
   * @description Abre el `DomicilioForm` en modo creación (sin `domicilioToEdit`).
   */
  const handleAddDomicilio = () => {
    setEditingDomicilio(null); // Asegura que no se esté editando un domicilio existente
    setShowDomicilioForm(true); // Muestra el modal del DomicilioForm
  };

  /**
   * @function handleEditDomicilio
   * @description Abre el `DomicilioForm` en modo edición, precargando los datos del domicilio seleccionado.
   * @param {Domicilio} domicilio - El objeto Domicilio a editar.
   */
  const handleEditDomicilio = (domicilio: Domicilio) => {
    setEditingDomicilio(domicilio); // Establece el domicilio a editar
    setShowDomicilioForm(true); // Muestra el modal del DomicilioForm
  };

  /**
   * @function handleSaveDomicilioForm
   * @description Callback que se ejecuta cuando un domicilio se guarda exitosamente desde el `DomicilioForm`.
   * Actualiza la lista `selectedDomicilios` ya sea añadiendo uno nuevo o actualizando uno existente.
   * @param {Domicilio} savedDomicilio - El objeto Domicilio que fue guardado.
   */
  const handleSaveDomicilioForm = (savedDomicilio: Domicilio) => {
    setSelectedDomicilios((prev) => {
      const existingIndex = prev.findIndex(d => d.id === savedDomicilio.id);
      if (existingIndex > -1) {
        // Si el domicilio ya existe (modo edición), lo actualiza en la lista
        const newArr = [...prev];
        newArr[existingIndex] = savedDomicilio;
        return newArr;
      } else {
        // Si es un nuevo domicilio (modo creación), lo añade a la lista
        return [...prev, savedDomicilio];
      }
    });
    setShowDomicilioForm(false); // Cierra el modal del DomicilioForm
    setEditingDomicilio(null); // Resetea el domicilio en edición
  };

  /**
   * @function handleRemoveDomicilio
   * @description Desasocia un domicilio de la lista `selectedDomicilios` del cliente.
   * NOTA: Este método solo quita el domicilio del cliente en el frontend y en la `domicilioIds`
   * que se enviará al backend. NO llama a `deleteDomicilio` del servicio, ya que esa función
   * es para BORRADO LÓGICO COMPLETO. Si el domicilio aún es referenciado por otras entidades
   * (otros clientes, sucursales), no debería borrarse físicamente.
   * @param {number} domicilioId - El ID del domicilio a desasociar.
   */
  const handleRemoveDomicilio = async (domicilioId: number) => {
    // Confirmación al usuario para aclarar que es una desasociación, no un borrado físico
    if (!window.confirm('¿Estás seguro de que quieres desasociar este domicilio del cliente? (Este domicilio no se eliminará físicamente del sistema si está en uso por otros clientes o sucursales).')) {
      return;
    }
    try {
      // Elimina el domicilio de la lista local `selectedDomicilios`
      setSelectedDomicilios(prev => prev.filter(d => d.id !== domicilioId));
      alert('Domicilio desasociado correctamente del cliente.');
      // IMPORTANTE: Aquí no se llama a deleteDomicilio(domicilioId, token)
      // porque el backend debería manejar la desasociación a través del DTO ClienteRequestDTO
      // que se envía en el handleSubmit, que ya tendrá la lista actualizada de domicilioIds.
    } catch (err) {
      console.error('Error al desasociar domicilio:', err);
      // Muestra un mensaje de error más específico si está disponible en la respuesta de la API
      alert(`Error al desasociar domicilio: ${(err as any).response?.data?.message || (err as any).message}`);
    }
  };

  /**
   * @function handleSubmit
   * @description Manejador para el envío del formulario principal del Cliente.
   * Realiza validaciones y luego llama al servicio de API para crear o actualizar el cliente.
   * @param {React.FormEvent} e - Evento de envío del formulario.
   */
  const handleSubmit = async (e: React.FormEvent) => {
    e.preventDefault(); // Previene el comportamiento por defecto del formulario
    setSubmitting(true); // Activa el estado de envío
    setError(null); // Limpia cualquier error anterior

    try {
      const token = await getAccessTokenSilently(); // Obtiene el token de autenticación

      // --- Validaciones del formulario ---
      if (!formData.nombre || !formData.apellido || !formData.email || !formData.usuarioId) {
        setError('Por favor, completa los campos obligatorios: Nombre, Apellido, Email, y selecciona un Usuario Asociado.');
        setSubmitting(false);
        return;
      }
      if (selectedDomicilios.length === 0) {
        setError('El cliente debe tener al menos un domicilio asociado. Haz clic en "Añadir Domicilio".');
        setSubmitting(false);
        return;
      }

      // Prepara los datos del cliente para enviar al backend.
      // `fechaNacimiento` se envía como `null` si es una cadena vacía.
      // `domicilioIds` se toma del estado sincronizado `selectedDomicilios`.
      const clientDataToSend: ClienteRequestDTO = {
        ...formData,
        fechaNacimiento: formData.fechaNacimiento && formData.fechaNacimiento.trim() !== '' ? formData.fechaNacimiento : null,
        domicilioIds: selectedDomicilios.map(d => d.id), // Asegura que solo los IDs se envíen
      };

      // Decide si crear o actualizar basándose en la existencia de `clientToEdit`
      if (clientToEdit) {
        await updateCliente(clientToEdit.id, clientDataToSend, token);
        alert('Cliente actualizado con éxito.');
      } else {
        await createCliente(clientDataToSend, token);
        alert('Cliente creado con éxito.');
      }

      onSave(); // Llama al callback `onSave` para notificar al componente padre
      handleClose(); // Cierra el modal
    } catch (err) {
      console.error('Error al guardar cliente:', err);
      // Extrae el mensaje de error de la respuesta de Axios o un mensaje genérico
      const errorMessage = (err as any).response?.data?.message || (err as any).message || 'Error desconocido al guardar.';
      setError(`Error al guardar: ${errorMessage}`);
    } finally {
      setSubmitting(false); // Desactiva el estado de envío
    }
  };

  return (
    <Modal show={show} onHide={handleClose} size="lg">
      <Modal.Header closeButton>
        <Modal.Title>{clientToEdit ? 'Editar Cliente' : 'Crear Cliente'}</Modal.Title>
      </Modal.Header>
      <Form onSubmit={handleSubmit}>
        <Modal.Body>
          {/* Muestra un spinner mientras se cargan las opciones del formulario */}
          {loadingOptions ? (
            <div className="text-center"><Spinner animation="border" /> Cargando opciones...</div>
          ) : error ? (
            // Muestra una alerta de error si algo falla durante la carga o el envío
            <Alert variant="danger">{error}</Alert>
          ) : (
            // Formulario principal cuando no hay carga ni errores
            <>
              <Form.Group className="mb-3">
                <Form.Label>Nombre</Form.Label>
                <Form.Control type="text" name="nombre" value={formData.nombre} onChange={handleChange} required />
              </Form.Group>

              <Form.Group className="mb-3">
                <Form.Label>Apellido</Form.Label>
                <Form.Control type="text" name="apellido" value={formData.apellido} onChange={handleChange} required />
              </Form.Group>

              <Row>
                <Col>
                  <Form.Group className="mb-3">
                    <Form.Label>Email</Form.Label>
                    <Form.Control type="email" name="email" value={formData.email} onChange={handleChange} required />
                  </Form.Group>
                </Col>
                <Col>
                  <Form.Group className="mb-3">
                    <Form.Label>Teléfono</Form.Label>
                    <Form.Control type="text" name="telefono" value={formData.telefono} onChange={handleChange} maxLength={20} />
                  </Form.Group>
                </Col>
              </Row>

              <Form.Group className="mb-3">
                <Form.Label>Fecha de Nacimiento</Form.Label>
                <Form.Control
                  type="date"
                  name="fechaNacimiento"
                  // Se asegura que el valor del input sea string, incluso si es null.
                  // Un input `type="date"` con `null` o `undefined` como valor funciona bien,
                  // pero `as string` es una aserción segura si TypeScript se queja.
                  value={formData.fechaNacimiento || ''}
                  onChange={handleChange}
                />
              </Form.Group>

              <Form.Group className="mb-3">
                <Form.Label>Usuario Asociado</Form.Label>
                <Form.Select
                  name="usuarioId"
                  value={formData.usuarioId || ''}
                  onChange={handleChange}
                  required
                  // Se deshabilita el selector de usuario si estamos editando un cliente
                  // o si no hay usuarios disponibles para asociar
                  disabled={!!clientToEdit || availableUsers.length === 0}
                >
                  <option value="">Selecciona un Usuario</option>
                  {availableUsers.map((userOption) => (
                    <option key={userOption.id} value={userOption.id}>
                      {/* Muestra username, rol y un fragmento del Auth0 ID para identificación */}
                      {userOption.username} ({userOption.rol}){' '}
                      {userOption.auth0Id ? `(${userOption.auth0Id.substring(userOption.auth0Id.indexOf('|') + 1, userOption.auth0Id.indexOf('|') + 7)}...)` : ''}
                    </option>
                  ))}
                </Form.Select>
                {clientToEdit && <Form.Text className="text-muted">El usuario asociado no se puede modificar una vez creado el cliente.</Form.Text>}
                {!clientToEdit && availableUsers.length === 0 && <Form.Text className="text-danger">No hay usuarios disponibles sin un cliente asociado. Asegúrate de crear un usuario primero.</Form.Text>}
              </Form.Group>

              <Form.Group className="mb-3">
                <Form.Check
                  type="checkbox"
                  label="Estado Activo"
                  name="estadoActivo"
                  checked={formData.estadoActivo}
                  onChange={handleChange}
                />
              </Form.Group>

              {/* Sección de Gestión de Domicilios */}
              <Card className="mt-4">
                <Card.Header className="d-flex justify-content-between align-items-center">
                  <h6>Domicilios del Cliente</h6>
                  <Button variant="outline-success" size="sm" onClick={handleAddDomicilio}>
                    <FontAwesomeIcon icon={faPlusCircle} /> Añadir Domicilio
                  </Button>
                </Card.Header>
                <ListGroup variant="flush">
                  {selectedDomicilios.length === 0 ? (
                    <ListGroup.Item className="text-center text-muted">
                      Este cliente no tiene domicilios asociados.
                    </ListGroup.Item>
                  ) : (
                    selectedDomicilios.map((domicilio) => (
                      <ListGroup.Item key={domicilio.id} className="d-flex justify-content-between align-items-center">
                        <div>
                          <FontAwesomeIcon icon={faMapMarkerAlt} className="me-2 text-info" />
                          {/* Muestra la dirección completa del domicilio */}
                          {`${domicilio.calle} ${domicilio.numero}, ${domicilio.localidad.nombre}, ${domicilio.localidad.provincia.nombre} (${domicilio.cp})`}
                        </div>
                        <div>
                          <Button variant="info" size="sm" className="me-2" onClick={() => handleEditDomicilio(domicilio)}>
                            <FontAwesomeIcon icon={faEdit} /> Editar
                          </Button>
                          <Button variant="danger" size="sm" onClick={() => handleRemoveDomicilio(domicilio.id)}>
                            <FontAwesomeIcon icon={faTrash} /> Desasociar
                          </Button>
                        </div>
                      </ListGroup.Item>
                    ))
                  )}
                </ListGroup>
              </Card>
            </>
          )}
        </Modal.Body>
        <Modal.Footer>
          <Button variant="secondary" onClick={handleClose} disabled={submitting}>
            Cancelar
          </Button>
          <Button variant="primary" type="submit" disabled={submitting || loadingOptions}>
            {submitting ? <Spinner as="span" animation="border" size="sm" role="status" aria-hidden="true" className="me-2" /> : ''}
            {clientToEdit ? 'Actualizar' : 'Crear'}
          </Button>
        </Modal.Footer>
      </Form>

      {/* Modal para el formulario de Domicilios */}
      <DomicilioForm
        show={showDomicilioForm}
        handleClose={() => setShowDomicilioForm(false)}
        onSave={handleSaveDomicilioForm}
        domicilioToEdit={editingDomicilio}
      />
    </Modal>
  );
};

export default ClientForm;
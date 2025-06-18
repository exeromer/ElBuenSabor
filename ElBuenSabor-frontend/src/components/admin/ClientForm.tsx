// ClientForm.tsx
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
import { ClienteUsuarioService } from '../../services/clienteUsuarioService';
import type {
  Cliente,
  ClienteRequestDTO,
  Usuario,
  Domicilio,
} from '../../types/types';
import { FontAwesomeIcon } from '@fortawesome/react-fontawesome';
import { faPlusCircle, faTrash, faMapMarkerAlt, faEdit } from '@fortawesome/free-solid-svg-icons';
import { format, parseISO } from 'date-fns';
import DomicilioForm from './DomicilioForm';

// Instanciamos el servicio
const clienteUsuarioService = new ClienteUsuarioService();

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
  const { getAccessTokenSilently } = useAuth0();

  const [formData, setFormData] = useState<ClienteRequestDTO>({
    nombre: '',
    apellido: '',
    telefono: '',
    email: '',
    fechaNacimiento: '',
    usuarioId: 0, // Inicia con 0, que será reemplazado por la selección o por el ID del cliente a editar
    domicilioIds: [],
    estadoActivo: true,
  });

  const [availableUsers, setAvailableUsers] = useState<Usuario[]>([]);
  const [loadingOptions, setLoadingOptions] = useState(true);
  const [submitting, setSubmitting] = useState(false);
  const [error, setError] = useState<string | null>(null);

  const [selectedDomicilios, setSelectedDomicilios] = useState<Domicilio[]>([]);

  const [showDomicilioForm, setShowDomicilioForm] = useState(false);
  const [editingDomicilio, setEditingDomicilio] = useState<Domicilio | null>(null);

  useEffect(() => {
    const loadOptions = async () => {
      setLoadingOptions(true);
      try {
        const token = await getAccessTokenSilently();
        const [users, clients] = await Promise.all([
          clienteUsuarioService.getAllUsuarios(token),
          clienteUsuarioService.getAllClientes(token),
        ]);

        const associatedUserIds = new Set(clients.map(c => c.usuario.id));

        const filteredUsers = users.filter(u =>
          !associatedUserIds.has(u.id) || (clientToEdit && u.id === clientToEdit.usuario.id)
        );
        setAvailableUsers(filteredUsers);

        if (clientToEdit) {
          setSelectedDomicilios(clientToEdit.domicilios);
        } else {
          setSelectedDomicilios([]);
        }

      } catch (err) {
        setError('Error al cargar opciones de usuarios o domicilios.');
        console.error('Error en loadOptions:', err);
      } finally {
        setLoadingOptions(false);
      }
    };

    if (show) {
      loadOptions();
    }
  }, [getAccessTokenSilently, clientToEdit, show]);

  useEffect(() => {
    if (show) {
      if (clientToEdit) {
        setFormData({
          nombre: clientToEdit.nombre,
          apellido: clientToEdit.apellido,
          telefono: clientToEdit.telefono,
          email: clientToEdit.email,
          fechaNacimiento: (clientToEdit.fechaNacimiento && clientToEdit.fechaNacimiento.trim() !== '')
            ? format(parseISO(clientToEdit.fechaNacimiento), 'yyyy-MM-dd')
            : '',
          // CORRECCIÓN 1: Aseguramos que usuarioId sea un número.
          // Asumimos que clientToEdit.usuario.id siempre estará definido si clientToEdit existe.
          usuarioId: clientToEdit.usuario.id as number, // O clientToEdit.usuario.id ?? 0
          // CORRECCIÓN 2: Aseguramos que domicilioIds sea un array de números.
          domicilioIds: clientToEdit.domicilios
            .filter(d => d.id !== undefined)
            .map(d => d.id as number),
          estadoActivo: clientToEdit.estadoActivo,
          // Si tu ClienteRequestDTO puede tener imagenId e id (para updates), inclúyelos aquí.
          // imagenId: clientToEdit.imagen?.id ?? null,
          // id: clientToEdit.id
        });
        setSelectedDomicilios(clientToEdit.domicilios);
      } else {
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
        setSelectedDomicilios([]);
      }
      setError(null);
    }
  }, [clientToEdit, show]);

  useEffect(() => {
    setFormData(prev => ({
      ...prev,
      // CORRECCIÓN 3: Aseguramos que domicilioIds en formData se actualice como un array de números.
      domicilioIds: selectedDomicilios
        .filter(d => d && d.id !== undefined) // Filtra nulos/undefined y los que no tienen ID
        .map(d => d.id as number),
    }));
  }, [selectedDomicilios]);

  const handleChange = (e: React.ChangeEvent<HTMLInputElement | HTMLSelectElement | HTMLTextAreaElement>) => {
    const { name, value } = e.target;
    const isCheckbox = (e.target as HTMLInputElement).type === 'checkbox';
    const checked = (e.target as HTMLInputElement).checked;

    setFormData((prev) => ({
      ...prev,
      [name]: isCheckbox ? checked : (name === 'usuarioId' ? Number(value) : value),
    }));
  };

  const handleAddDomicilio = () => {
    setEditingDomicilio(null);
    setShowDomicilioForm(true);
  };

  const handleEditDomicilio = (domicilio: Domicilio) => {
    setEditingDomicilio(domicilio);
    setShowDomicilioForm(true);
  };

  const handleSaveDomicilioForm = (savedDomicilio: Domicilio) => {
    setSelectedDomicilios((prev) => {
      const existingIndex = prev.findIndex(d => d.id === savedDomicilio.id);
      if (existingIndex > -1) {
        const newArr = [...prev];
        newArr[existingIndex] = savedDomicilio;
        return newArr;
      } else {
        return [...prev, savedDomicilio];
      }
    });
    setShowDomicilioForm(false);
    setEditingDomicilio(null);
  };

  const handleRemoveDomicilio = async (domicilioId: number | undefined) => { // Ajustar tipo para id opcional
    if (domicilioId === undefined) { // Verificar si el ID es undefined
        alert('Error: ID de domicilio no proporcionado para desasociar.');
        return;
    }
    if (!window.confirm('¿Estás seguro de que quieres desasociar este domicilio del cliente? (Este domicilio no se eliminará físicamente del sistema si está en uso por otros clientes o sucursales).')) {
      return;
    }
    try {
      setSelectedDomicilios(prev => prev.filter(d => d.id !== domicilioId));
      alert('Domicilio desasociado correctamente del cliente.');
    } catch (err) {
      console.error('Error al desasociar domicilio:', err);
      alert(`Error al desasociar domicilio: ${(err as any).response?.data?.message || (err as any).message}`);
    }
  };

  const handleSubmit = async (e: React.FormEvent) => {
    e.preventDefault();
    setSubmitting(true);
    setError(null);

    try {
      const token = await getAccessTokenSilently();

      if (!formData.nombre || !formData.apellido || !formData.email || !formData.usuarioId) {
        setError('Por favor, completa los campos obligatorios: Nombre, Apellido, Email, y selecciona un Usuario Asociado.');
        setSubmitting(false);
        return;
      }
      // Asegurarse de que usuarioId no sea 0 si es un campo requerido por el backend para nuevos clientes
      if (!clientToEdit && formData.usuarioId === 0) {
        setError('Por favor, selecciona un Usuario Asociado válido.');
        setSubmitting(false);
        return;
      }
      if (selectedDomicilios.length === 0) {
        setError('El cliente debe tener al menos un domicilio asociado. Haz clic en "Añadir Domicilio".');
        setSubmitting(false);
        return;
      }

      const clientDataToSend: ClienteRequestDTO = {
        ...formData,
        fechaNacimiento: formData.fechaNacimiento && formData.fechaNacimiento.trim() !== '' ? formData.fechaNacimiento : null,
        // CORRECCIÓN 4: Aseguramos que domicilioIds se envíe como un array de números.
        domicilioIds: selectedDomicilios
          .filter(d => d.id !== undefined)
          .map(d => d.id as number),
      };

      if (clientToEdit) {
        // CORRECCIÓN 5: Aseguramos que clientToEdit.id sea un número al pasarlo al servicio.
        if (clientToEdit.id === undefined) {
            setError('Error: ID del cliente a editar no disponible.');
            setSubmitting(false);
            return;
        }
        await clienteUsuarioService.updateCliente(clientToEdit.id as number, clientDataToSend, token); // O clientToEdit.id!
        alert('Cliente actualizado con éxito.');
      } else {
        await clienteUsuarioService.createCliente(clientDataToSend, token);
        alert('Cliente creado con éxito.');
      }

      onSave();
      handleClose();
    } catch (err) {
      console.error('Error al guardar cliente:', err);
      const errorMessage = (err as any).response?.data?.message || (err as any).message || 'Error desconocido al guardar.';
      setError(`Error al guardar: ${errorMessage}`);
    } finally {
      setSubmitting(false);
    }
  };

  return (
    <Modal show={show} onHide={handleClose} size="lg">
      <Modal.Header closeButton>
        <Modal.Title>{clientToEdit ? 'Editar Cliente' : 'Crear Cliente'}</Modal.Title>
      </Modal.Header>
      <Form onSubmit={handleSubmit}>
        <Modal.Body>
          {loadingOptions ? (
            <div className="text-center"><Spinner animation="border" /> Cargando opciones...</div>
          ) : error ? (
            <Alert variant="danger">{error}</Alert>
          ) : (
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
                  disabled={!!clientToEdit || availableUsers.length === 0}
                >
                  <option value="">Selecciona un Usuario</option>
                  {availableUsers.map((userOption) => (
                    // Aseguramos que userOption.id se usa como key
                    <option key={userOption.id ?? userOption.username} value={userOption.id ?? ''}>
                      {userOption.username} ({userOption.rol})
                      {userOption.auth0Id ? ` (${userOption.auth0Id.substring(userOption.auth0Id.indexOf('|') + 1, userOption.auth0Id.indexOf('|') + 7)}...)` : ''}
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
                      // Aseguramos que domicilio.id se usa como key
                      <ListGroup.Item key={domicilio.id ?? `${domicilio.calle}-${domicilio.numero}`} className="d-flex justify-content-between align-items-center">
                        <div>
                          <FontAwesomeIcon icon={faMapMarkerAlt} className="me-2 text-info" />
                          {/* CORRECCIÓN 6: Usamos .denominacion y encadenamiento opcional */}
                          {/* CORRECCIÓN 7: Usamos .denominacion y encadenamiento opcional para provincia */}
                          {`${domicilio.calle} ${domicilio.numero}, ${domicilio.localidad?.denominacion ?? 'N/A'}, ${domicilio.localidad?.provincia?.denominacion ?? 'N/A'} (${domicilio.cp})`}
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
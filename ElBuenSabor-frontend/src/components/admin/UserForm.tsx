/**
 * @file UserForm.tsx
 * @description Componente de formulario modal para la creación y edición de Usuarios.
 * Permite a los administradores gestionar la información básica de un usuario, como su
 * `auth0Id`, `username`, `rol` y `estadoActivo`.
 * Este formulario es utilizado en el panel de administración para la gestión de usuarios.
 *
 * @hook `useState`: Gestiona el estado del formulario (`formData`), el estado de envío (`submitting`),
 * y cualquier mensaje de error (`error`).
 * @hook `useEffect`: Se encarga de precargar el formulario para edición o resetearlo para creación
 * cuando el modal se muestra o cambia el usuario a editar.
 * @hook `useAuth0`: Obtiene el token de autenticación para las operaciones protegidas del API.
 */
import React, { useState, useEffect } from 'react';
import { Modal, Form, Button, Alert, Spinner } from 'react-bootstrap';
import { useAuth0 } from '@auth0/auth0-react';
import { createUsuario, updateUsuario } from '../../services/clienteUsuarioService'; // Estos servicios gestionan usuarios
import type { Usuario, UsuarioRequestDTO, Rol } from '../../types/types'; // Importamos Rol desde types/types.ts

/**
 * @interface UserFormProps
 * @description Propiedades que el componente `UserForm` espera recibir.
 * @property {boolean} show - Controla la visibilidad del modal.
 * @property {() => void} handleClose - Función para cerrar el modal.
 * @property {() => void} onSave - Callback que se ejecuta después de guardar exitosamente un usuario.
 * @property {Usuario | null} [userToEdit] - Objeto Usuario a editar. Si es `null` o `undefined`, se asume modo creación.
 */
interface UserFormProps {
  show: boolean;
  handleClose: () => void;
  onSave: () => void;
  userToEdit?: Usuario | null;
}

const UserForm: React.FC<UserFormProps> = ({ show, handleClose, onSave, userToEdit }) => {
  /**
   * @hook useAuth0
   * @description Hook para obtener el token de acceso de Auth0, necesario para autenticar
   * las peticiones al backend.
   */
  const { getAccessTokenSilently } = useAuth0();

  /**
   * @state formData
   * @description Estado que almacena los datos del formulario del Usuario.
   * Utiliza `UsuarioRequestDTO` para tipificar los datos que se enviarán al backend.
   * El rol se inicializa como 'CLIENTE' por defecto para nuevos usuarios.
   */
  const [formData, setFormData] = useState<UsuarioRequestDTO>({
    auth0Id: '',
    username: '',
    rol: 'CLIENTE', // Default para nuevos usuarios
    estadoActivo: true,
  });

  /**
   * @state submitting
   * @description Estado booleano para indicar si el formulario está en proceso de envío (creación/actualización).
   * Se utiliza para deshabilitar botones y mostrar un spinner.
   */
  const [submitting, setSubmitting] = useState(false);

  /**
   * @state error
   * @description Estado para almacenar cualquier mensaje de error que ocurra durante el envío del formulario.
   */
  const [error, setError] = useState<string | null>(null);

  /**
   * @hook useEffect
   * @description Hook que se encarga de precargar el formulario con los datos del usuario
   * si se está en modo edición, o de resetearlo a los valores iniciales para el modo creación.
   * Se ejecuta cuando `userToEdit` o la visibilidad del modal (`show`) cambian.
   */
  useEffect(() => {
    if (show) { // Solo actualiza el estado si el modal está visible
      if (userToEdit) {
        // Precarga los datos del usuario a editar
        setFormData({
          auth0Id: userToEdit.auth0Id,
          username: userToEdit.username,
          rol: userToEdit.rol,
          estadoActivo: userToEdit.estadoActivo,
        });
      } else {
        // Resetea el formulario para un nuevo usuario
        setFormData({
          auth0Id: '',
          username: '',
          rol: 'CLIENTE', // Restablece el rol predeterminado
          estadoActivo: true,
        });
      }
      setError(null); // Limpia cualquier error anterior
    }
  }, [userToEdit, show]); // Dependencias para re-ejecutar el efecto

  /**
   * @function handleSubmit
   * @description Manejador para el envío del formulario.
   * Realiza validaciones básicas y luego llama al servicio de API para crear o actualizar el usuario.
   * @param {React.FormEvent} e - Evento de envío del formulario.
   */
  const handleSubmit = async (e: React.FormEvent) => {
    e.preventDefault(); // Previene el comportamiento por defecto del formulario
    setSubmitting(true); // Activa el estado de envío
    setError(null); // Limpia cualquier error anterior

    try {
      const token = await getAccessTokenSilently(); // Obtiene el token de autenticación

      // --- Validaciones básicas del formulario ---
      if (!formData.auth0Id || !formData.username || !formData.rol) {
        setError('Por favor, completa todos los campos obligatorios: Auth0 ID, Username, y Rol.');
        setSubmitting(false);
        return;
      }
      // Consideración: se podría añadir validación para el formato del Auth0 ID si es necesario.

      // Decide si crear o actualizar basándose en la existencia de `userToEdit`
      if (userToEdit) {
        await updateUsuario(userToEdit.id, formData, token);
        alert('Usuario actualizado con éxito.');
      } else {
        await createUsuario(formData, token);
        alert('Usuario creado con éxito.');
      }

      onSave(); // Llama al callback `onSave` para notificar al componente padre
      handleClose(); // Cierra el modal
    } catch (err) {
      console.error('Error al guardar usuario:', err);
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
        <Modal.Title>{userToEdit ? 'Editar Usuario' : 'Crear Usuario'}</Modal.Title>
      </Modal.Header>
      <Form onSubmit={handleSubmit}>
        <Modal.Body>
          {/* Muestra una alerta de error si existe un error */}
          {error && <Alert variant="danger">{error}</Alert>}

          <Form.Group className="mb-3">
            <Form.Label>Auth0 ID</Form.Label>
            <Form.Control
              type="text"
              name="auth0Id"
              value={formData.auth0Id}
              // Manejador inline para actualizar solo el campo `auth0Id`
              onChange={(e: React.ChangeEvent<HTMLInputElement>) => setFormData(prev => ({ ...prev, auth0Id: e.target.value }))}
              required
              // Deshabilita el campo Auth0 ID si se está editando un usuario existente
              disabled={!!userToEdit}
            />
            {userToEdit && <Form.Text className="text-muted">El Auth0 ID no se puede modificar después de la creación.</Form.Text>}
          </Form.Group>

          <Form.Group className="mb-3">
            <Form.Label>Username</Form.Label>
            <Form.Control
              type="text"
              name="username"
              value={formData.username}
              // Manejador inline para actualizar solo el campo `username`
              onChange={(e: React.ChangeEvent<HTMLInputElement>) => setFormData(prev => ({ ...prev, username: e.target.value }))}
              required
            />
          </Form.Group>

          <Form.Group className="mb-3">
            <Form.Label>Rol</Form.Label>
            <Form.Select
              name="rol"
              value={formData.rol}
              // Manejador inline para actualizar el campo `rol` y asegurar la tipificación correcta
              onChange={(e: React.ChangeEvent<HTMLSelectElement>) => setFormData(prev => ({ ...prev, rol: e.target.value as Rol }))}
              required
            >
              <option value="CLIENTE">CLIENTE</option>
              <option value="EMPLEADO">EMPLEADO</option>
              <option value="ADMIN">ADMIN</option>
            </Form.Select>
          </Form.Group>

          <Form.Group className="mb-3">
            <Form.Check
              type="checkbox"
              label="Estado Activo"
              name="estadoActivo"
              checked={formData.estadoActivo}
              // Manejador inline para actualizar el campo `estadoActivo`
              onChange={(e: React.ChangeEvent<HTMLInputElement>) => setFormData(prev => ({ ...prev, estadoActivo: e.target.checked }))}
            />
          </Form.Group>
        </Modal.Body>
        <Modal.Footer>
          <Button variant="secondary" onClick={handleClose} disabled={submitting}>
            Cancelar
          </Button>
          <Button variant="primary" type="submit" disabled={submitting}>
            {/* Muestra un spinner si el formulario está en proceso de envío */}
            {submitting ? <Spinner as="span" animation="border" size="sm" role="status" aria-hidden="true" className="me-2" /> : ''}
            {userToEdit ? 'Actualizar' : 'Crear'}
          </Button>
        </Modal.Footer>
      </Form>
    </Modal>
  );
};

export default UserForm;
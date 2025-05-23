/**
 * @file DomicilioForm.tsx
 * @description Componente de formulario modal para la creación y edición de Domicilios.
 * Permite a los usuarios ingresar detalles de una dirección como calle, número, código postal,
 * y seleccionar el País, Provincia y Localidad a través de selectores anidados (en cascada).
 * Se encarga de precargar datos para la edición y de enviar la información al backend.
 *
 * @hook `useState`: Gestiona el estado del formulario (`formData`), las listas de opciones (países, provincias, localidades),
 * el estado de los selectores anidados (`selectedPaisId`, `selectedProvinciaId`), estados de carga/envío, y errores.
 * @hook `useEffect`: Carga las opciones iniciales de localización y se encarga de la lógica en cascada
 * para cargar provincias según el país seleccionado y localidades según la provincia. También precarga
 * el formulario para edición o lo resetea para creación.
 * @hook `useAuth0`: Obtiene el token de autenticación para las operaciones protegidas del API.
 */
import React, { useState, useEffect } from 'react';
import { Modal, Form, Button, Alert, Spinner, Row, Col } from 'react-bootstrap';
import { useAuth0 } from '@auth0/auth0-react';
import { createDomicilio, updateDomicilio } from '../../services/domicilioService';
import { getAllPaises, getAllProvincias, getAllLocalidades } from '../../services/ubicacionService';
// Se ajusta la importación de tipos a la nueva ruta types.ts
import type { Domicilio, DomicilioRequestDTO, Pais, Provincia, Localidad } from '../../types/types';

/**
 * @interface DomicilioFormProps
 * @description Propiedades que el componente `DomicilioForm` espera recibir.
 * @property {boolean} show - Controla la visibilidad del modal.
 * @property {() => void} handleClose - Función para cerrar el modal.
 * @property {(domicilio: Domicilio) => void} onSave - Callback que se ejecuta después de guardar exitosamente
 * un domicilio, recibiendo el objeto `Domicilio` guardado como argumento.
 * @property {Domicilio | null} [domicilioToEdit] - Objeto Domicilio a editar. Si es `null` o `undefined`, se asume modo creación.
 */
interface DomicilioFormProps {
  show: boolean;
  handleClose: () => void;
  onSave: (domicilio: Domicilio) => void;
  domicilioToEdit?: Domicilio | null;
}

const DomicilioForm: React.FC<DomicilioFormProps> = ({ show, handleClose, onSave, domicilioToEdit }) => {
  /**
   * @hook useAuth0
   * @description Hook para obtener el token de acceso de Auth0, necesario para autenticar
   * las peticiones al backend.
   */
  const { getAccessTokenSilently } = useAuth0();

  /**
   * @state formData
   * @description Estado que almacena los datos del formulario de Domicilio.
   * Utiliza `DomicilioRequestDTO` para tipificar los datos que se enviarán al backend.
   */
  const [formData, setFormData] = useState<DomicilioRequestDTO>({
    calle: '',
    numero: 0, // Inicializado a 0 o al valor que consideres apropiado
    cp: '',
    localidadId: 0, // ID de la localidad seleccionada
  });

  /**
   * @state paises
   * @description Lista de países disponibles, obtenidos del backend.
   */
  const [paises, setPaises] = useState<Pais[]>([]);

  /**
   * @state provincias
   * @description Lista de provincias disponibles, filtradas por el país seleccionado.
   */
  const [provincias, setProvincias] = useState<Provincia[]>([]);

  /**
   * @state localidades
   * @description Lista de localidades disponibles, filtradas por la provincia seleccionada.
   */
  const [localidades, setLocalidades] = useState<Localidad[]>([]);

  /**
   * @state selectedPaisId
   * @description ID del país actualmente seleccionado en el dropdown. Se usa para la carga en cascada de provincias.
   */
  const [selectedPaisId, setSelectedPaisId] = useState<number | ''>('');

  /**
   * @state selectedProvinciaId
   * @description ID de la provincia actualmente seleccionada en el dropdown. Se usa para la carga en cascada de localidades.
   */
  const [selectedProvinciaId, setSelectedProvinciaId] = useState<number | ''>('');

  /**
   * @state loadingOptions
   * @description Estado booleano para indicar si las opciones de localización (países, provincias, localidades)
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
   * @hook useEffect
   * @description Hook para cargar todas las opciones de localización (países, provincias, localidades)
   * y para precargar los selectores de país y provincia si estamos en modo edición.
   * Se ejecuta al montar el componente y cuando la visibilidad del modal (`show`) o
   * el domicilio a editar (`domicilioToEdit`) cambian.
   */
  useEffect(() => {
    const loadAllOptions = async () => {
      if (!show) return; // Solo carga si el modal está visible

      setLoadingOptions(true);
      try {
        // Carga todos los países, provincias y localidades en paralelo
        const [fetchedPaises, allProvinces, allLocalities] = await Promise.all([
          getAllPaises(),
          getAllProvincias(),
          getAllLocalidades(),
        ]);
        setPaises(fetchedPaises);

        if (domicilioToEdit) {
          // Si estamos editando, precarga los IDs de país y provincia del domicilio existente
          const paisId = domicilioToEdit.localidad.provincia.pais.id;
          const provinciaId = domicilioToEdit.localidad.provincia.id;

          setSelectedPaisId(paisId);
          // Filtra y establece las provincias y localidades relevantes para el domicilio a editar
          setProvincias(allProvinces.filter(p => p.pais.id === paisId));
          setSelectedProvinciaId(provinciaId);
          setLocalidades(allLocalities.filter(l => l.provincia.id === provinciaId));

          // Precarga el formData para edición
          setFormData({
            calle: domicilioToEdit.calle,
            numero: domicilioToEdit.numero,
            cp: domicilioToEdit.cp,
            localidadId: domicilioToEdit.localidad.id,
          });
        } else {
          // Resetea para creación
          setFormData({ calle: '', numero: 0, cp: '', localidadId: 0 });
          setSelectedPaisId('');
          setSelectedProvinciaId('');
          setProvincias([]);
          setLocalidades([]);
        }
      } catch (err) {
        setError('Error al cargar opciones de localización.');
        console.error('Error al cargar opciones en DomicilioForm:', err);
      } finally {
        setLoadingOptions(false);
      }
    };

    loadAllOptions();
  }, [domicilioToEdit, show]); // Dependencias para re-ejecutar el efecto

  /**
   * @hook useEffect
   * @description Hook para cargar las provincias cada vez que `selectedPaisId` cambia.
   * Se encarga de filtrar las provincias que pertenecen al país seleccionado.
   */
  useEffect(() => {
    const fetchProvinciasByPais = async () => {
      if (selectedPaisId) {
        try {
          // Re-obtener todas las provincias para asegurar que estén actualizadas (aunque ya se cargaron al inicio)
          const allProvinces = await getAllProvincias();
          setProvincias(allProvinces.filter(p => p.pais.id === selectedPaisId));
          // Resetear provincia y localidad al cambiar el país
          setSelectedProvinciaId('');
          setFormData(prev => ({ ...prev, localidadId: 0 })); // También resetea la localidadId en formData
          setLocalidades([]);
        } catch (err) {
          console.error('Error al cargar provincias por país:', err);
        }
      } else {
        // Si no hay país seleccionado, limpia provincias y localidades
        setProvincias([]);
        setSelectedProvinciaId('');
        setLocalidades([]);
        setFormData(prev => ({ ...prev, localidadId: 0 }));
      }
    };
    fetchProvinciasByPais();
  }, [selectedPaisId]); // Dependencia: se ejecuta cuando cambia el ID del país seleccionado

  /**
   * @hook useEffect
   * @description Hook para cargar las localidades cada vez que `selectedProvinciaId` cambia.
   * Se encarga de filtrar las localidades que pertenecen a la provincia seleccionada.
   */
  useEffect(() => {
    const fetchLocalidadesByProvincia = async () => {
      if (selectedProvinciaId) {
        try {
          // Re-obtener todas las localidades para asegurar que estén actualizadas
          const allLocalities = await getAllLocalidades();
          setLocalidades(allLocalities.filter(l => l.provincia.id === selectedProvinciaId));
          // Si estamos en modo edición y la localidad del domicilio a editar coincide con la provincia actual,
          // entonces seleccionamos esa localidad. De lo contrario, reseteamos.
          if (domicilioToEdit && domicilioToEdit.localidad.provincia.id === selectedProvinciaId) {
             setFormData(prev => ({ ...prev, localidadId: domicilioToEdit.localidad.id }));
          } else {
             setFormData(prev => ({ ...prev, localidadId: 0 })); // Resetear localidad al cambiar provincia
          }
        } catch (err) {
          console.error('Error al cargar localidades por provincia:', err);
        }
      } else {
        // Si no hay provincia seleccionada, limpia localidades
        setLocalidades([]);
        setFormData(prev => ({ ...prev, localidadId: 0 }));
      }
    };
    fetchLocalidadesByProvincia();
  }, [selectedProvinciaId, domicilioToEdit]); // Dependencias: se ejecuta cuando cambia el ID de la provincia o el domicilio a editar

  /**
   * @function handleInputChange
   * @description Manejador para cambios en los campos de texto del formulario (calle, numero, cp).
   * Convierte el valor del `numero` a un tipo `number`.
   * @param {React.ChangeEvent<HTMLInputElement>} e - Evento de cambio del input.
   */
  const handleInputChange = (e: React.ChangeEvent<HTMLInputElement>) => {
    const { name, value } = e.target;
    setFormData((prev) => ({
      ...prev,
      // Si el campo es 'numero', convierte el valor a un número; de lo contrario, usa el valor de la cadena.
      [name]: name === 'numero' ? Number(value) : value,
    }));
  };

  /**
   * @function handleSelectChange
   * @description Manejador para cambios en los selects de País, Provincia y Localidad.
   * Actualiza los IDs seleccionados en los estados locales y en el `formData` para `localidadId`.
   * @param {React.ChangeEvent<HTMLSelectElement>} e - Evento de cambio del select.
   */
  const handleSelectChange = (e: React.ChangeEvent<HTMLSelectElement>) => {
    const { name, value } = e.target;
    const numericValue = Number(value); // Convierte el valor a número

    if (name === 'localidadId') {
      setFormData((prev) => ({
        ...prev,
        localidadId: numericValue,
      }));
    } else if (name === 'selectedPaisId') {
      setSelectedPaisId(numericValue || ''); // Actualiza el ID del país seleccionado
      // Al cambiar el país, también resetea la provincia y la localidad en el formData
      setSelectedProvinciaId('');
      setFormData(prev => ({ ...prev, localidadId: 0 }));
    } else if (name === 'selectedProvinciaId') {
      setSelectedProvinciaId(numericValue || ''); // Actualiza el ID de la provincia seleccionada
      // Al cambiar la provincia, también resetea la localidad en el formData
      setFormData(prev => ({ ...prev, localidadId: 0 }));
    }
  };

  /**
   * @function handleSubmit
   * @description Manejador para el envío del formulario.
   * Realiza validaciones y luego llama al servicio de API para crear o actualizar el domicilio.
   * @param {React.FormEvent} e - Evento de envío del formulario.
   */
  const handleSubmit = async (e: React.FormEvent) => {
    e.preventDefault();
    setSubmitting(true);
    setError(null);

    try {
      const token = await getAccessTokenSilently();

      // Validaciones básicas del formulario
      if (!formData.calle || formData.numero <= 0 || !formData.cp || formData.localidadId === 0) {
        setError('Por favor, completa todos los campos obligatorios del domicilio (Calle, Número, CP, y selecciona una Localidad). Asegúrate que el número de calle sea positivo.');
        setSubmitting(false);
        return;
      }
      if (!selectedPaisId || !selectedProvinciaId) {
        setError('Por favor, selecciona un País y una Provincia.');
        setSubmitting(false);
        return;
      }

      let savedDomicilio: Domicilio;
      // Decide si crear o actualizar basándose en la existencia de `domicilioToEdit`
      if (domicilioToEdit) {
        savedDomicilio = await updateDomicilio(domicilioToEdit.id, formData, token);
        alert('Domicilio actualizado con éxito.');
      } else {
        savedDomicilio = await createDomicilio(formData, token);
        alert('Domicilio creado con éxito.');
      }
      // Llama al callback `onSave` con el domicilio guardado (que contiene la estructura completa del backend)
      onSave(savedDomicilio);
      handleClose(); // Cierra el modal
    } catch (err) {
      console.error('Error al guardar domicilio:', err);
      // Extrae el mensaje de error de la respuesta de Axios o un mensaje genérico
      const errorMessage = (err as any).response?.data?.message || (err as any).message || 'Error desconocido al guardar.';
      setError(`Error al guardar: ${errorMessage}`);
    } finally {
      setSubmitting(false);
    }
  };

  return (
    <Modal show={show} onHide={handleClose} size="lg">
      <Modal.Header closeButton>
        <Modal.Title>{domicilioToEdit ? 'Editar Domicilio' : 'Crear Domicilio'}</Modal.Title>
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
                <Form.Label>Calle</Form.Label>
                <Form.Control
                  type="text"
                  name="calle"
                  value={formData.calle}
                  onChange={handleInputChange} // Usa handleInputChange
                  required
                />
              </Form.Group>

              <Row>
                <Col>
                  <Form.Group className="mb-3">
                    <Form.Label>Número</Form.Label>
                    <Form.Control
                      type="number"
                      name="numero"
                      value={formData.numero === 0 && !domicilioToEdit ? '' : formData.numero} // Mostrar vacío si es 0 y es nuevo
                      onChange={handleInputChange} // Usa handleInputChange
                      min="1" // Número de calle debe ser al menos 1
                      required
                    />
                  </Form.Group>
                </Col>
                <Col>
                  <Form.Group className="mb-3">
                    <Form.Label>Código Postal</Form.Label>
                    <Form.Control
                      type="text"
                      name="cp"
                      value={formData.cp}
                      onChange={handleInputChange} // Usa handleInputChange
                      required
                      maxLength={8} // Limita la longitud del CP
                    />
                  </Form.Group>
                </Col>
              </Row>

              <Form.Group className="mb-3">
                <Form.Label>País</Form.Label>
                <Form.Select
                  name="selectedPaisId" // Nombre del campo para el estado local
                  value={selectedPaisId || ''}
                  onChange={handleSelectChange} // Usa handleSelectChange
                  required
                  disabled={paises.length === 0} // Deshabilita si no hay opciones
                >
                  <option value="">Selecciona un País</option>
                  {paises.map((pais) => (
                    <option key={pais.id} value={pais.id}>{pais.nombre}</option>
                  ))}
                </Form.Select>
              </Form.Group>

              <Form.Group className="mb-3">
                <Form.Label>Provincia</Form.Label>
                <Form.Select
                  name="selectedProvinciaId" // Nombre del campo para el estado local
                  value={selectedProvinciaId || ''}
                  onChange={handleSelectChange} // Usa handleSelectChange
                  required
                  disabled={!selectedPaisId || provincias.length === 0} // Deshabilita si no hay país o provincias
                >
                  <option value="">Selecciona una Provincia</option>
                  {provincias.map((provincia) => (
                    <option key={provincia.id} value={provincia.id}>{provincia.nombre}</option>
                  ))}
                </Form.Select>
              </Form.Group>

              <Form.Group className="mb-3">
                <Form.Label>Localidad</Form.Label>
                <Form.Select
                  name="localidadId"
                  value={formData.localidadId || ''}
                  onChange={handleSelectChange} // Usa handleSelectChange
                  required
                  disabled={!selectedProvinciaId || localidades.length === 0} // Deshabilita si no hay provincia o localidades
                >
                  <option value="">Selecciona una Localidad</option>
                  {localidades.map((localidad) => (
                    <option key={localidad.id} value={localidad.id}>{localidad.nombre}</option>
                  ))}
                </Form.Select>
              </Form.Group>
            </>
          )}
        </Modal.Body>
        <Modal.Footer>
          <Button variant="secondary" onClick={handleClose} disabled={submitting}>
            Cancelar
          </Button>
          <Button variant="primary" type="submit" disabled={submitting || loadingOptions}>
            {submitting ? <Spinner as="span" animation="border" size="sm" role="status" aria-hidden="true" className="me-2" /> : ''}
            {domicilioToEdit ? 'Actualizar' : 'Crear'}
          </Button>
        </Modal.Footer>
      </Form>
    </Modal>
  );
};

export default DomicilioForm;
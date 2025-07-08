// DomicilioForm.tsx
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
import { DomicilioService } from '../../services/domicilioService';
import { UbicacionService } from '../../services/ubicacionService';
import type { Domicilio, DomicilioRequestDTO, Pais, Provincia, Localidad } from '../../types/types';

// Instanciamos los servicios
const domicilioService = new DomicilioService();
const ubicacionService = new UbicacionService();

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
  const { getAccessTokenSilently } = useAuth0();

  const [formData, setFormData] = useState<DomicilioRequestDTO>({
    calle: '',
    numero: 0,
    cp: '',
    localidadId: 0,
  });

  const [paises, setPaises] = useState<Pais[]>([]);
  const [provincias, setProvincias] = useState<Provincia[]>([]);
  const [localidades, setLocalidades] = useState<Localidad[]>([]);

  // selectedPaisId y selectedProvinciaId pueden ser number o string vacío
  const [selectedPaisId, setSelectedPaisId] = useState<number | ''>('');
  const [selectedProvinciaId, setSelectedProvinciaId] = useState<number | ''>('');

  const [loadingOptions, setLoadingOptions] = useState(true);
  const [submitting, setSubmitting] = useState(false);
  const [error, setError] = useState<string | null>(null);

  useEffect(() => {
    const loadAllOptions = async () => {
      if (!show) return;

      setLoadingOptions(true);
      try {
        const [fetchedPaises, allProvinces, allLocalities] = await Promise.all([
          ubicacionService.getAllPaises(),
          ubicacionService.getAllProvincias(),
          ubicacionService.getAllLocalidades(),
        ]);
        setPaises(fetchedPaises);

        if (domicilioToEdit) {
          // Aseguramos que los IDs sean `number` o `''` para el estado.
          // Es un casting seguro porque `domicilioToEdit` ya existe, y sus propiedades son `number`
          const paisId = domicilioToEdit.localidad.provincia.pais.id as number;
          const provinciaId = domicilioToEdit.localidad.provincia.id as number;

          // CORRECCIÓN 1: Aseguramos que el valor asignado a setSelectedPaisId sea `number | ''`
          setSelectedPaisId(paisId); // Esto es seguro ya que paisId será number
          setProvincias(allProvinces.filter(p => p.pais.id === paisId));
          // CORRECCIÓN 2: Aseguramos que el valor asignado a setSelectedProvinciaId sea `number | ''`
          setSelectedProvinciaId(provinciaId); // Esto es seguro ya que provinciaId será number
          setLocalidades(allLocalities.filter(l => l.provincia.id === provinciaId));

          setFormData({
            calle: domicilioToEdit.calle,
            numero: domicilioToEdit.numero,
            cp: domicilioToEdit.cp,
            // CORRECCIÓN 3: Aseguramos que localidadId sea `number`.
            localidadId: domicilioToEdit.localidad.id as number, // Asumimos que id existe para un domicilioToEdit
          });
        } else {
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
  }, [domicilioToEdit, show, getAccessTokenSilently]); // Añadir getAccessTokenSilently como dependencia si es que lo usas implícitamente dentro de ubicacionService


  useEffect(() => {
    const fetchProvinciasByPais = async () => {
      if (selectedPaisId) {
        try {
          const allProvinces = await ubicacionService.getAllProvincias();
          setProvincias(allProvinces.filter(p => p.pais.id === selectedPaisId));
          setSelectedProvinciaId('');
          setFormData(prev => ({ ...prev, localidadId: 0 }));
          setLocalidades([]);
        } catch (err) {
          console.error('Error al cargar provincias por país:', err);
        }
      } else {
        setProvincias([]);
        setSelectedProvinciaId('');
        setLocalidades([]);
        setFormData(prev => ({ ...prev, localidadId: 0 }));
      }
    };
    fetchProvinciasByPais();
  }, [selectedPaisId]);

  useEffect(() => {
    const fetchLocalidadesByProvincia = async () => {
      if (selectedProvinciaId) {
        try {
          const allLocalities = await ubicacionService.getAllLocalidades();
          setLocalidades(allLocalities.filter(l => l.provincia.id === selectedProvinciaId));
          // Si estamos en modo edición y la localidad del domicilio a editar coincide con la provincia actual,
          // entonces seleccionamos esa localidad. De lo contrario, reseteamos.
          if (domicilioToEdit && domicilioToEdit.localidad.provincia.id === selectedProvinciaId) {
             setFormData(prev => ({ ...prev, localidadId: domicilioToEdit.localidad.id as number })); // Aseguramos que es number
          } else {
             setFormData(prev => ({ ...prev, localidadId: 0 }));
          }
        } catch (err) {
          console.error('Error al cargar localidades por provincia:', err);
        }
      } else {
        setLocalidades([]);
        setFormData(prev => ({ ...prev, localidadId: 0 }));
      }
    };
    fetchLocalidadesByProvincia();
  }, [selectedProvinciaId, domicilioToEdit]);

  const handleInputChange = (e: React.ChangeEvent<HTMLInputElement>) => {
    const { name, value } = e.target;
    setFormData((prev) => ({
      ...prev,
      [name]: name === 'numero' ? Number(value) : value,
    }));
  };

  const handleSelectChange = (e: React.ChangeEvent<HTMLSelectElement>) => {
    const { name, value } = e.target;
    const numericValue = Number(value);

    if (name === 'localidadId') {
      setFormData((prev) => ({
        ...prev,
        localidadId: numericValue,
      }));
    } else if (name === 'selectedPaisId') {
      setSelectedPaisId(numericValue || '');
      setSelectedProvinciaId('');
      setFormData(prev => ({ ...prev, localidadId: 0 }));
    } else if (name === 'selectedProvinciaId') {
      setSelectedProvinciaId(numericValue || '');
      setFormData(prev => ({ ...prev, localidadId: 0 }));
    }
  };

  const handleSubmit = async (e: React.FormEvent) => {
    e.preventDefault();
    setSubmitting(true);
    setError(null);

    try {
      const token = await getAccessTokenSilently();

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
      if (domicilioToEdit) {
        // CORRECCIÓN 4: Aseguramos que domicilioToEdit.id sea number para updateDomicilio
        if (domicilioToEdit.id === undefined) {
          setError('Error: ID del domicilio a editar no disponible.');
          setSubmitting(false);
          return;
        }
        savedDomicilio = await domicilioService.updateDomicilio(domicilioToEdit.id as number, formData, token);
        alert('Domicilio actualizado con éxito.');
      } else {
        savedDomicilio = await domicilioService.createDomicilio(formData, token);
        alert('Domicilio creado con éxito.');
      }
      onSave(savedDomicilio);
      handleClose();
    } catch (err) {
      console.error('Error al guardar domicilio:', err);
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
          {loadingOptions ? (
            <div className="text-center"><Spinner animation="border" /> Cargando opciones...</div>
          ) : error ? (
            <Alert variant="danger">{error}</Alert>
          ) : (
            <>
              <Form.Group className="mb-3">
                <Form.Label>Calle</Form.Label>
                <Form.Control
                  type="text"
                  name="calle"
                  value={formData.calle}
                  onChange={handleInputChange}
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
                      value={formData.numero === 0 && !domicilioToEdit ? '' : formData.numero}
                      onChange={handleInputChange}
                      min="1"
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
                      onChange={handleInputChange}
                      required
                      maxLength={8}
                    />
                  </Form.Group>
                </Col>
              </Row>

              <Form.Group className="mb-3">
                <Form.Label>País</Form.Label>
                <Form.Select
                  name="selectedPaisId"
                  value={selectedPaisId || ''}
                  onChange={handleSelectChange}
                  required
                  disabled={paises.length === 0}
                >
                  <option value="">Selecciona un País</option>
                  {paises.map((pais) => (
                    // CORRECCIÓN 5: Usamos pais.denominacion y asumimos que pais.id existe como key
                    <option key={pais.id ?? pais.denominacion} value={pais.id ?? ''}>{pais.denominacion}</option>
                  ))}
                </Form.Select>
              </Form.Group>

              <Form.Group className="mb-3">
                <Form.Label>Provincia</Form.Label>
                <Form.Select
                  name="selectedProvinciaId"
                  value={selectedProvinciaId || ''}
                  onChange={handleSelectChange}
                  required
                  disabled={!selectedPaisId || provincias.length === 0}
                >
                  <option value="">Selecciona una Provincia</option>
                  {provincias.map((provincia) => (
                    // CORRECCIÓN 6: Usamos provincia.denominacion y asumimos que provincia.id existe como key
                    <option key={provincia.id ?? provincia.denominacion} value={provincia.id ?? ''}>{provincia.denominacion}</option>
                  ))}
                </Form.Select>
              </Form.Group>

              <Form.Group className="mb-3">
                <Form.Label>Localidad</Form.Label>
                <Form.Select
                  name="localidadId"
                  value={formData.localidadId || ''}
                  onChange={handleSelectChange}
                  required
                  disabled={!selectedProvinciaId || localidades.length === 0}
                >
                  <option value="">Selecciona una Localidad</option>
                  {localidades.map((localidad) => (
                    // CORRECCIÓN 7: Usamos localidad.denominacion y asumimos que localidad.id existe como key
                    <option key={localidad.id ?? localidad.denominacion} value={localidad.id ?? ''}>{localidad.denominacion}</option>
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
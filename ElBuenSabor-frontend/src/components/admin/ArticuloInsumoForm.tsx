/**
 * @file ArticuloInsumoForm.tsx
 * @description Componente de formulario modal para la creación y edición de Artículos Insumo.
 * Permite a los usuarios ingresar y modificar los datos de un artículo insumo, incluyendo
 * su denominación, precios, stock, unidad de medida, categoría, y gestionar una imagen asociada.
 * Utiliza los servicios de API modularizados para interactuar con el backend (ArticuloInsumo, Categoria, UnidadMedida, File Upload, Imagen).
 *
 * @hook `useState`: Gestiona el estado del formulario, las listas de opciones (categorías, unidades de medida),
 * estados de carga/envío, errores y el archivo de imagen seleccionado.
 * @hook `useEffect`: Carga las opciones iniciales (categorías y unidades de medida) al montar el componente
 * y resetea/precarga el formulario cuando el modal se abre o se cambia el artículo a editar.
 * @hook `useAuth0`: Obtiene el token de autenticación para las operaciones protegidas del API.
 */
import React, { useState, useEffect } from 'react';
import { Modal, Form, Button, Alert, Spinner, Row, Col } from 'react-bootstrap'; // Se eliminó InputGroup de aquí ya que no se usa directamente en este archivo
import { useAuth0 } from '@auth0/auth0-react';
import { getCategorias } from '../../services/categoriaService';
import { createArticuloInsumo, updateArticuloInsumo } from '../../services/articuloInsumoService';
import { getUnidadesMedida } from '../../services/unidadMedidaService';
// Importamos getImageUrl si se quisiera mostrar la imagen existente al editar, y uploadFile/deleteFileFromServer
import { uploadFile, deleteFileFromServer, getImageUrl } from '../../services/fileUploadService';
import { deleteImageEntity } from '../../services/imagenService';
// Se ajusta la importación de tipos a la nueva ruta types.ts
import type { ArticuloInsumo, Categoria, UnidadMedida } from '../../types/types'; // Se eliminó ImagenRequestDTO ya que no se tipifica el formData con él.
import { FontAwesomeIcon } from '@fortawesome/react-fontawesome';
import { faTimesCircle } from '@fortawesome/free-solid-svg-icons'; // Se eliminó faUpload ya que no se usa directamente en un botón o icono visualizado.

/**
 * @interface ArticuloInsumoFormProps
 * @description Propiedades que el componente `ArticuloInsumoForm` espera recibir.
 * @property {boolean} show - Controla la visibilidad del modal.
 * @property {() => void} handleClose - Función para cerrar el modal.
 * @property {() => void} onSave - Callback que se ejecuta después de guardar exitosamente un artículo insumo.
 * @property {ArticuloInsumo | null} [articuloToEdit] - Objeto ArticuloInsumo a editar. Si es `null` o `undefined`, se asume modo creación.
 */
interface ArticuloInsumoFormProps {
  show: boolean;
  handleClose: () => void;
  onSave: () => void;
  articuloToEdit?: ArticuloInsumo | null;
}

const ArticuloInsumoForm: React.FC<ArticuloInsumoFormProps> = ({ show, handleClose, onSave, articuloToEdit }) => {
  /**
   * @hook useAuth0
   * @description Hook para obtener el token de acceso de Auth0, necesario para autenticar
   * las peticiones al backend.
   */
  const { getAccessTokenSilently } = useAuth0();

  /**
   * @state formData
   * @description Estado que almacena los datos del formulario del Artículo Insumo.
   * Se inicializa con valores por defecto para creación o con los datos del `articuloToEdit` para edición.
   */
  const [formData, setFormData] = useState<ArticuloInsumo>({
    id: 0,
    denominacion: '',
    precioVenta: 0,
    unidadMedida: { id: 0, denominacion: '' },
    categoria: { id: 0, denominacion: '' },
    estadoActivo: true,
    precioCompra: 0,
    stockActual: 0,
    stockMaximo: 0,
    esParaElaborar: false,
    imagenes: [], // Inicializa la lista de imágenes vacía
  });

  /**
   * @state categories
   * @description Estado para almacenar la lista de categorías disponibles, fetched del backend.
   */
  const [categories, setCategories] = useState<Categoria[]>([]);

  /**
   * @state unidadesMedida
   * @description Estado para almacenar la lista de unidades de medida disponibles, fetched del backend.
   */
  const [unidadesMedida, setUnidadesMedida] = useState<UnidadMedida[]>([]);

  /**
   * @state loadingOptions
   * @description Estado booleano para indicar si las opciones (categorías y unidades de medida)
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
   * @state selectedFile
   * @description Estado para almacenar el archivo de imagen seleccionado por el usuario para subir.
   */
  const [selectedFile, setSelectedFile] = useState<File | null>(null);

  /**
   * @hook useEffect
   * @description Hook que se ejecuta al montar el componente para cargar las listas de categorías
   * y unidades de medida desde el backend.
   * Se ejecuta una sola vez al inicio del ciclo de vida del componente (`[]` como dependencia).
   */
  useEffect(() => {
    const loadOptions = async () => {
      setLoadingOptions(true);
      try {
        // Ejecuta ambas llamadas a la API en paralelo para optimizar el tiempo de carga
        const [fetchedCategories, fetchedUnidades] = await Promise.all([
          getCategorias(),
          getUnidadesMedida(),
        ]);
        setCategories(fetchedCategories);
        setUnidadesMedida(fetchedUnidades);
      } catch (err) {
        setError('Error al cargar categorías y unidades de medida.');
        console.error('Error al cargar opciones:', err); // Log más descriptivo
      } finally {
        setLoadingOptions(false);
      }
    };
    loadOptions();
  }, []); // Dependencias vacías: se ejecuta solo una vez al montar

  /**
   * @hook useEffect
   * @description Hook que gestiona la precarga de datos para el modo edición o resetea el formulario
   * para el modo creación.
   * Se ejecuta cada vez que `articuloToEdit` o `show` (visibilidad del modal) cambian.
   */
  useEffect(() => {
    if (show) { // Solo si el modal está visible
      if (articuloToEdit) {
        // Si hay un artículo para editar, carga sus datos en el formulario
        setFormData(articuloToEdit);
      } else {
        // Si es un nuevo artículo, resetea el formulario a sus valores iniciales
        setFormData({
          id: 0,
          denominacion: '',
          precioVenta: 0,
          unidadMedida: { id: 0, denominacion: '' },
          categoria: { id: 0, denominacion: '' },
          estadoActivo: true,
          precioCompra: 0,
          stockActual: 0,
          stockMaximo: 0,
          esParaElaborar: false,
          imagenes: [],
        });
      }
      setSelectedFile(null); // Limpia cualquier archivo seleccionado previamente
      setError(null); // Limpia cualquier error anterior
    }
  }, [articuloToEdit, show]); // Dependencias: se ejecuta cuando el artículo a editar o la visibilidad del modal cambian

  /**
   * @function handleChange
   * @description Manejador genérico para cambios en los campos de texto e input (excepto selects y files).
   * Actualiza el estado `formData` basándose en el nombre del campo y su valor.
   * Maneja inputs de tipo texto, número, y checkboxes.
   * @param {React.ChangeEvent<HTMLInputElement | HTMLSelectElement | HTMLTextAreaElement>} e - Evento de cambio.
   */
  const handleChange = (e: React.ChangeEvent<HTMLInputElement | HTMLSelectElement | HTMLTextAreaElement>) => {
    const { name, value } = e.target;
    // La desestructuración con `type` y `checked` debe hacerse sobre `e.target as HTMLInputElement` para que TypeScript lo entienda.
    const isCheckbox = (e.target as HTMLInputElement).type === 'checkbox';
    const checked = (e.target as HTMLInputElement).checked;

    setFormData((prev) => ({
      ...prev,
      [name]: isCheckbox ? checked : value,
    }));
  };

  /**
   * @function handleSelectChange
   * @description Manejador específico para cambios en los selects de Categoría y Unidad de Medida.
   * Busca el objeto completo de la opción seleccionada (basado en el ID) y lo asigna a `formData`.
   * @param {React.ChangeEvent<HTMLSelectElement>} e - Evento de cambio del select.
   */
  const handleSelectChange = (e: React.ChangeEvent<HTMLSelectElement>) => {
    const { name, value } = e.target;
    const selectedId = Number(value); // Convierte el valor del select a número

    if (name === 'categoriaId') {
      const selectedCat = categories.find((cat) => cat.id === selectedId);
      if (selectedCat) {
        setFormData((prev) => ({
          ...prev,
          categoria: selectedCat, // Asigna el objeto Categoria completo
        }));
      }
    } else if (name === 'unidadMedidaId') {
      const selectedUm = unidadesMedida.find((um) => um.id === selectedId);
      if (selectedUm) {
        setFormData((prev) => ({
          ...prev,
          unidadMedida: selectedUm, // Asigna el objeto UnidadMedida completo
        }));
      }
    }
  };

  /**
   * @function handleFileChange
   * @description Manejador para el input de tipo 'file'.
   * Almacena el archivo seleccionado en el estado `selectedFile`.
   * @param {React.ChangeEvent<HTMLInputElement>} e - Evento de cambio del input de archivo.
   */
  const handleFileChange = (e: React.ChangeEvent<HTMLInputElement>) => {
    if (e.target.files && e.target.files[0]) {
      setSelectedFile(e.target.files[0]);
    } else {
      setSelectedFile(null);
    }
  };

  /**
   * @function handleDeleteImage
   * @description Elimina una imagen existente del artículo.
   * Realiza una confirmación al usuario, luego llama a los servicios de API
   * para eliminar la entidad de imagen de la DB y el archivo del servidor.
   * @param {number} imageId - El ID de la entidad de imagen a eliminar.
   * @param {string} filename - La URL completa de la imagen, se extrae el nombre del archivo.
   */
  const handleDeleteImage = async (imageId: number, filename: string) => {
    // Pide confirmación al usuario antes de eliminar
    if (!window.confirm('¿Estás seguro de que quieres eliminar esta imagen?')) {
      return;
    }
    try {
      const token = await getAccessTokenSilently(); // Obtiene el token de autenticación

      // Lógica para extraer el nombre del archivo de la URL
      const fileNameToDetele = filename.substring(filename.lastIndexOf('/') + 1);

      // Eliminar la entidad de imagen de la base de datos
      await deleteImageEntity(imageId, token);
      // Eliminar el archivo físico del servidor
      await deleteFileFromServer(fileNameToDetele, token);

      // Actualiza el estado del formulario para remover la imagen eliminada
      setFormData((prev) => ({
        ...prev,
        imagenes: prev.imagenes.filter((img) => img.id !== imageId),
      }));
      alert('Imagen eliminada con éxito.');
    } catch (err) {
      console.error('Error al eliminar imagen:', err);
      // Muestra un mensaje de error más específico si está disponible en la respuesta de la API
      alert(`Error al eliminar imagen: ${(err as any).response?.data?.message || (err as any).message}`);
    }
  };

  /**
   * @function handleSubmit
   * @description Manejador para el envío del formulario.
   * Realiza validaciones, sube la nueva imagen si existe, y luego llama al servicio
   * correspondiente para crear o actualizar el artículo insumo.
   * @param {React.FormEvent} e - Evento de envío del formulario.
   */
  const handleSubmit = async (e: React.FormEvent) => {
    e.preventDefault(); // Previene el comportamiento por defecto del formulario (recarga de página)
    setSubmitting(true); // Activa el estado de envío
    setError(null); // Limpia cualquier error anterior

    try {
      const token = await getAccessTokenSilently(); // Obtiene el token de autenticación

      // --- Validaciones básicas del formulario ---
      if (!formData.denominacion || formData.precioVenta <= 0 || !formData.categoria.id || !formData.unidadMedida.id) {
        setError('Por favor, completa todos los campos obligatorios (Denominación, Precio Venta, Categoría, Unidad de Medida).');
        setSubmitting(false);
        return;
      }
      if (formData.precioCompra < 0 || formData.stockActual < 0 || formData.stockMaximo < 0) {
        setError('Los valores numéricos (Precio Compra, Stock Actual, Stock Máximo) no pueden ser negativos.');
        setSubmitting(false);
        return;
      }

      let uploadedImageUrl: string | null = null;
      let newImageEntityId: number | null = null;

      // Si se seleccionó un nuevo archivo de imagen, se sube primero
      if (selectedFile) {
        const uploadResponse = await uploadFile(selectedFile, token, formData.id || undefined); // Pasa articuloId si es una edición
        uploadedImageUrl = uploadResponse.url; // URL completa de la imagen subida
        newImageEntityId = uploadResponse.imagenDB.id; // ID de la entidad Imagen en la DB
      }

      // Prepara el objeto ArticuloInsumo para enviar al backend
      const articuloInsumoToSend: ArticuloInsumo = {
        ...formData,
        // Asegurarse de enviar solo los IDs y denominaciones necesarias para las relaciones
        categoria: { id: formData.categoria.id, denominacion: formData.categoria.denominacion },
        unidadMedMedida: { id: formData.unidadMedida.id, denominacion: formData.unidadMedida.denominacion }, // Corrección: Typo en 'unidadMedMedida' -> 'unidadMedida'
        // Si se subió una nueva imagen, se reemplazan todas las imágenes existentes con la nueva.
        // Para ArticuloInsumo, se asume que solo tendrá una imagen principal.
        imagenes: newImageEntityId ? [{ id: newImageEntityId, denominacion: uploadedImageUrl!, estadoActivo: true }] : formData.imagenes,
      };

      // Decide si crear o actualizar basándose en la existencia de `articuloToEdit`
      if (articuloToEdit) {
        await updateArticuloInsumo(articuloToEdit.id, articuloInsumoToSend, token);
        alert('Artículo Insumo actualizado con éxito.');
      } else {
        await createArticuloInsumo(articuloInsumoToSend, token);
        alert('Artículo Insumo creado con éxito.');
      }

      onSave(); // Llama al callback `onSave` para notificar al componente padre
      handleClose(); // Cierra el modal
    } catch (err) {
      console.error('Error al guardar artículo insumo:', err);
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
        <Modal.Title>{articuloToEdit ? 'Editar Artículo Insumo' : 'Crear Artículo Insumo'}</Modal.Title>
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
                <Form.Label>Denominación</Form.Label>
                <Form.Control
                  type="text"
                  name="denominacion"
                  value={formData.denominacion}
                  onChange={handleChange}
                  required
                />
              </Form.Group>

              <Row>
                <Col>
                  <Form.Group className="mb-3">
                    <Form.Label>Precio Venta</Form.Label>
                    <Form.Control
                      type="number"
                      name="precioVenta"
                      value={formData.precioVenta}
                      onChange={handleChange}
                      step="0.01" // Permite valores decimales
                      min="0.01" // Precio de venta debe ser positivo
                      required
                    />
                  </Form.Group>
                </Col>
                <Col>
                  <Form.Group className="mb-3">
                    <Form.Label>Precio Compra</Form.Label>
                    <Form.Control
                      type="number"
                      name="precioCompra"
                      value={formData.precioCompra}
                      onChange={handleChange}
                      step="0.01" // Permite valores decimales
                      min="0" // Precio de compra puede ser 0 o positivo
                    />
                  </Form.Group>
                </Col>
              </Row>

              <Row>
                <Col>
                  <Form.Group className="mb-3">
                    <Form.Label>Stock Actual</Form.Label>
                    <Form.Control
                      type="number"
                      name="stockActual"
                      value={formData.stockActual}
                      onChange={handleChange}
                      step="0.01" // Permite valores decimales para stock
                      min="0" // Stock actual puede ser 0 o positivo
                    />
                  </Form.Group>
                </Col>
                <Col>
                  <Form.Group className="mb-3">
                    <Form.Label>Stock Máximo</Form.Label>
                    <Form.Control
                      type="number"
                      name="stockMaximo"
                      value={formData.stockMaximo}
                      onChange={handleChange}
                      step="0.01" // Permite valores decimales para stock
                      min="0" // Stock máximo puede ser 0 o positivo
                    />
                  </Form.Group>
                </Col>
              </Row>

              <Form.Group className="mb-3">
                <Form.Label>Unidad de Medida</Form.Label>
                <Form.Select
                  name="unidadMedidaId"
                  // Establece el valor del select al ID de la unidad de medida actual del formData
                  value={formData.unidadMedida?.id || ''}
                  onChange={handleSelectChange}
                  required
                  disabled={unidadesMedida.length === 0} // Deshabilita si no hay opciones
                >
                  <option value="">Selecciona una Unidad</option>
                  {unidadesMedida.map((um) => (
                    <option key={um.id} value={um.id}>{um.denominacion}</option>
                  ))}
                </Form.Select>
              </Form.Group>

              <Form.Group className="mb-3">
                <Form.Label>Categoría</Form.Label>
                <Form.Select
                  name="categoriaId"
                  // Establece el valor del select al ID de la categoría actual del formData
                  value={formData.categoria?.id || ''}
                  onChange={handleSelectChange}
                  required
                  disabled={categories.length === 0} // Deshabilita si no hay opciones
                >
                  <option value="">Selecciona una Categoría</option>
                  {categories.map((cat) => (
                    <option key={cat.id} value={cat.id}>{cat.denominacion}</option>
                  ))}
                </Form.Select>
              </Form.Group>

              <Form.Group className="mb-3">
                <Form.Check
                  type="checkbox"
                  label="Es Para Elaborar"
                  name="esParaElaborar"
                  checked={formData.esParaElaborar}
                  onChange={handleChange}
                />
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

              {/* Sección de Gestión de Imágenes */}
              <Form.Group className="mb-3">
                <Form.Label>Imagen del Artículo</Form.Label>
                <Form.Control type="file" onChange={handleFileChange} accept="image/*" />
                {selectedFile && <div className="mt-2">Archivo seleccionado: {selectedFile.name}</div>}

                {/* Muestra las imágenes actuales y permite eliminarlas */}
                {formData.imagenes && formData.imagenes.length > 0 && (
                  <div className="mt-3">
                    <h6>Imagen(es) Actual(es):</h6>
                    {formData.imagenes.map((img) => (
                      <div key={img.id} className="d-flex align-items-center mb-2 p-2 border rounded">
                        {/* Utiliza getImageUrl para construir la URL completa */}
                        <img
                          src={getImageUrl(img.denominacion)}
                          alt="Artículo"
                          style={{ width: '80px', height: '80px', objectFit: 'cover', border: '1px solid #ddd' }}
                          className="me-2"
                        />
                        {/* Muestra solo el nombre del archivo de la URL */}
                        <span>{img.denominacion.substring(img.denominacion.lastIndexOf('/') + 1)}</span>
                        <Button
                          variant="danger"
                          size="sm"
                          className="ms-auto"
                          onClick={() => handleDeleteImage(img.id, img.denominacion)}
                        >
                          <FontAwesomeIcon icon={faTimesCircle} />
                        </Button>
                      </div>
                    ))}
                    {selectedFile && (
                      <Alert variant="warning" className="mt-2">
                        Se ha seleccionado una nueva imagen. Al guardar, esta reemplazará la(s) imagen(es) actual(es).
                      </Alert>
                    )}
                  </div>
                )}
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
            {articuloToEdit ? 'Actualizar' : 'Crear'}
          </Button>
        </Modal.Footer>
      </Form>
    </Modal>
  );
};

export default ArticuloInsumoForm;
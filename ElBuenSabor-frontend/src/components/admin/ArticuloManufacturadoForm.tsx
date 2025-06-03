/**
 * @file ArticuloManufacturadoForm.tsx
 * @description Componente de formulario modal para la creación y edición de Artículos Manufacturados.
 * Permite a los usuarios ingresar y modificar los datos de un artículo manufacturado (ej. una hamburguesa),
 * incluyendo su denominación, precios, descripción, tiempo de preparación, unidad de medida, categoría,
 * y los ingredientes (insumos) necesarios con sus cantidades. También gestiona la imagen principal.
 * Utiliza los servicios de API modularizados para interactuar con el backend
 * (ArticuloManufacturado, Categoria, UnidadMedida, ArticuloInsumo, File Upload, Imagen).
 *
 * @hook `useState`: Gestiona el estado del formulario (`formData`), las listas de opciones
 * (categorías, unidades de medida, insumos), estados de carga/envío, errores y el archivo de imagen seleccionado.
 * @hook `useEffect`: Carga las opciones iniciales al montar el componente y resetea/precarga el
 * formulario cuando el modal se abre o se cambia el artículo a editar.
 * @hook `useAuth0`: Obtiene el token de autenticación para las operaciones protegidas del API.
 */
import React, { useState, useEffect } from 'react';
import { Modal, Form, Button, Alert, Spinner, Row, Col, InputGroup, Card, ListGroup } from 'react-bootstrap';
import { useAuth0 } from '@auth0/auth0-react';
import { createArticuloManufacturado, updateArticuloManufacturado } from '../../services/articuloManufacturadoService';
import { getCategorias } from '../../services/categoriaService';
import { getUnidadesMedida } from '../../services/unidadMedidaService';
import { getArticulosInsumo } from '../../services/articuloInsumoService';
import { deleteImageEntity } from '../../services/imagenService';
import { uploadFile, deleteFileFromServer, getImageUrl } from '../../services/fileUploadService'; // Importa getImageUrl
// Se ajusta la importación de tipos a la nueva ruta types.ts
import type {
  ArticuloManufacturado,
  Categoria,
  UnidadMedida,
  ArticuloInsumo,
  ArticuloManufacturadoRequestDTO,
  // Se eliminaron los tipos no utilizados o que ya no son necesarios de importar directamente aquí
  // ArticuloManufacturadoDetalle, // No se usa directamente aquí, se mapea a DTO
  // ImagenRequestDTO // No se usa para tipificar el estado, es un DTO para el backend
} from '../../types/types';
import { FontAwesomeIcon } from '@fortawesome/react-fontawesome';
import {
  faPlusCircle,
  faMinusCircle,
} from '@fortawesome/free-solid-svg-icons'; // Se eliminó faUpload (no usado)

/**
 * @interface ArticuloManufacturadoFormProps
 * @description Propiedades que el componente `ArticuloManufacturadoForm` espera recibir.
 * @property {boolean} show - Controla la visibilidad del modal.
 * @property {() => void} handleClose - Función para cerrar el modal.
 * @property {() => void} onSave - Callback que se ejecuta después de guardar exitosamente un artículo manufacturado.
 * @property {ArticuloManufacturado | null} [articuloToEdit] - Objeto ArticuloManufacturado a editar. Si es `null` o `undefined`, se asume modo creación.
 */
interface ArticuloManufacturadoFormProps {
  show: boolean;
  handleClose: () => void;
  onSave: () => void;
  articuloToEdit?: ArticuloManufacturado | null;
}

const ArticuloManufacturadoForm: React.FC<ArticuloManufacturadoFormProps> = ({ show, handleClose, onSave, articuloToEdit }) => {
  /**
   * @hook useAuth0
   * @description Hook para obtener el token de acceso de Auth0, necesario para autenticar
   * las peticiones al backend.
   */
  const { getAccessTokenSilently } = useAuth0();

  /**
   * @state formData
   * @description Estado que almacena los datos del formulario del Artículo Manufacturado.
   * Se inicializa con valores por defecto para creación o con los datos mapeados del
   * `articuloToEdit` para edición. Este estado usa `ArticuloManufacturadoRequestDTO`
   * ya que los datos se preparan para ser enviados directamente al backend.
   */
  const [formData, setFormData] = useState<ArticuloManufacturadoRequestDTO>({
    denominacion: '',
    precioVenta: 0,
    unidadMedidaId: 0,
    categoriaId: 0,
    estadoActivo: true,
    descripcion: '',
    tiempoEstimadoMinutos: 0,
    preparacion: '',
    manufacturadoDetalles: [],
  });

  /**
   * @state categories
   * @description Estado para almacenar la lista de categorías disponibles, obtenidas del backend.
   */
  const [categories, setCategories] = useState<Categoria[]>([]);

  /**
   * @state unidadesMedida
   * @description Estado para almacenar la lista de unidades de medida disponibles, obtenidas del backend.
   */
  const [unidadesMedida, setUnidadesMedida] = useState<UnidadMedida[]>([]);

  /**
   * @state insumos
   * @description Estado para almacenar la lista de artículos insumo disponibles, utilizados
   * para seleccionar los ingredientes en los detalles de manufacturado.
   */
  const [insumos, setInsumos] = useState<ArticuloInsumo[]>([]);

  /**
   * @state loadingOptions
   * @description Estado booleano para indicar si las opciones del formulario (categorías, unidades de medida, insumos)
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
   * @description Estado para almacenar el archivo de imagen seleccionado por el usuario para subir como imagen principal.
   */
  const [selectedFile, setSelectedFile] = useState<File | null>(null);

  /**
   * @state isProductWrapper
   * @description Estado para indicar si el producto es un wrapper.
   */
  const [isProductWrapper, setIsProductWrapper] = useState<boolean>(false);

  /**
   * @hook useEffect
   * @description Hook que se ejecuta al montar el componente para cargar las listas de
   * categorías, unidades de medida y artículos insumo desde el backend.
   * Utiliza `Promise.all` para ejecutar las llamadas API en paralelo.
   */
  useEffect(() => {
    const loadOptions = async () => {
      setLoadingOptions(true);
      try {
        const [fetchedCategories, fetchedUnidades, fetchedInsumos] = await Promise.all([
          getCategorias(),
          getUnidadesMedida(),
          getArticulosInsumo(),
        ]);
        setCategories(fetchedCategories);
        setUnidadesMedida(fetchedUnidades);
        setInsumos(fetchedInsumos);
      } catch (err) {
        setError('Error al cargar opciones de categorías, unidades de medida o insumos.');
        console.error('Error al cargar opciones del formulario de artículo manufacturado:', err);
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
    if (show) {
      if (articuloToEdit) {
        // Mapea la entidad ArticuloManufacturado recibida a un ArticuloManufacturadoRequestDTO
        // para que coincida con la estructura del formData.
        setFormData({
          denominacion: articuloToEdit.denominacion,
          precioVenta: articuloToEdit.precioVenta,
          unidadMedidaId: articuloToEdit.unidadMedida.id,
          categoriaId: articuloToEdit.categoria.id,
          estadoActivo: articuloToEdit.estadoActivo,
          descripcion: articuloToEdit.descripcion,
          tiempoEstimadoMinutos: articuloToEdit.tiempoEstimadoMinutos,
          preparacion: articuloToEdit.preparacion,
          // Mapea los detalles de manufacturado a su DTO correspondiente
          manufacturadoDetalles: articuloToEdit.manufacturadoDetalles.map(d => ({
            articuloInsumoId: d.articuloInsumo.id,
            cantidad: d.cantidad,
            estadoActivo: d.estadoActivo !== undefined ? d.estadoActivo : true, // Default a true
          })),
        });
      } else {
        // Resetea el formulario a sus valores iniciales para un nuevo artículo
        setFormData({
          denominacion: '',
          precioVenta: 0,
          unidadMedidaId: 2,
          categoriaId: categories.length > 0 ? categories[0].id : 0, // Default a la primera categoría o 0
          estadoActivo: true,
          descripcion: '',
          tiempoEstimadoMinutos: 0,
          preparacion: '',
          manufacturadoDetalles: [],
        });
      }
      setIsProductWrapper(false); // Limpia el estado de producto wrapper
      setSelectedFile(null); // Limpia cualquier archivo de imagen seleccionado previamente
      setError(null); // Limpia cualquier error anterior
    }
  }, [articuloToEdit, show, categories]); // Dependencias: se ejecuta cuando el artículo a editar o la visibilidad del modal cambian

  /**
   * @function handleChange
   * @description Manejador genérico para cambios en los campos de texto, números, selects (por ID) y checkboxes.
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
      // Si el campo es un ID (como `unidadMedidaId` o `categoriaId`), se convierte a número.
      [name]: isCheckbox ? checked : (name === 'unidadMedidaId' || name === 'categoriaId' ? Number(value) : value),
    }));
  };

  /**
   * @function handleFileChange
   * @description Manejador para el input de tipo 'file' para la imagen principal.
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
   * @function handleAddDetalle
   * @description Añade un nuevo detalle de manufacturado (ingrediente) al formulario.
   * Inicializa el nuevo detalle con el primer insumo disponible y una cantidad de 1.
   */
  const handleAddDetalle = () => {
    if (isProductWrapper && formData.manufacturadoDetalles.length >= 1) {
      alert("Para un producto simple (envoltorio de insumo), solo se permite un ingrediente.");
      return;
    }
    setFormData((prev) => ({
      ...prev,
      manufacturadoDetalles: [
        ...prev.manufacturadoDetalles,
        // Inicializa el nuevo detalle con el ID del primer insumo disponible o 0 si no hay
        { articuloInsumoId: 0, cantidad: 1, estadoActivo: true },
      ],
    }));
  };

  /**
   * @function handleRemoveDetalle
   * @description Elimina un detalle de manufacturado (ingrediente) específico por su índice.
   * @param {number} index - El índice del detalle a eliminar en el array `manufacturadoDetalles`.
   */
  const handleRemoveDetalle = (index: number) => {
    setFormData((prev) => ({
      ...prev,
      manufacturadoDetalles: prev.manufacturadoDetalles.filter((_, i) => i !== index),
    }));
  };

  /**
   * @function handleDetalleChange
   * @description Manejador para los cambios en los campos de los detalles de manufacturado (ingredientes).
   * Actualiza la cantidad o el insumo seleccionado para un detalle específico.
   * @param {number} index - El índice del detalle a modificar.
   * @param {string} name - El nombre del campo del detalle que cambió ('articuloInsumoId' o 'cantidad').
   * @param {any} value - El nuevo valor del campo.
   */
  const handleDetalleChange = (index: number, name: string, value: any) => {
    setFormData((prev) => {
      const newDetails = [...prev.manufacturadoDetalles];
      // Si el nombre es 'cantidad', convierte el valor a float; de lo contrario, úsalo directamente.
      // (asumimos que 'articuloInsumoId' ya se convierte a number en el onChange del select)
      newDetails[index] = { ...newDetails[index], [name]: name === 'cantidad' ? parseFloat(value) : value };
      return { ...prev, manufacturadoDetalles: newDetails };
    });
  };

  /**
   * @function handleSubmit
   * @description Manejador para el envío del formulario.
   * Realiza validaciones, gestiona la subida y posible eliminación de imágenes antiguas,
   * y luego llama al servicio de API para crear o actualizar el artículo manufacturado.
   * @param {React.FormEvent} e - Evento de envío del formulario.
   */
  const handleSubmit = async (e: React.FormEvent) => {
    e.preventDefault(); // Previene el comportamiento por defecto del formulario (recarga de página)
    setSubmitting(true); // Activa el estado de envío
    setError(null); // Limpia cualquier error anterior

    try {
      const token = await getAccessTokenSilently(); // Obtiene el token de autenticación

      // --- Validaciones básicas del formulario ---
      if (!formData.denominacion || formData.precioVenta <= 0 || !formData.categoriaId || !formData.unidadMedidaId || !formData.descripcion || formData.tiempoEstimadoMinutos <= 0 || !formData.preparacion) {
        setError('Por favor, completa todos los campos obligatorios (Denominación, Precio Venta, Categoría, Unidad de Medida, Descripción, Tiempo Estimado, Preparación).');
        setSubmitting(false);
        return;
      }
      if (formData.manufacturadoDetalles.length === 0) {
        setError('Debes añadir al menos un ingrediente a los detalles de manufacturado.');
        setSubmitting(false);
        return;
      }
      // Valida cada detalle de manufacturado
      if (formData.manufacturadoDetalles.some(d => d.cantidad <= 0 || d.articuloInsumoId === 0)) {
        setError('Todos los ingredientes deben tener un insumo seleccionado y una cantidad positiva.');
        setSubmitting(false);
        return;
      }

      let newArticulo: ArticuloManufacturado; // Variable para almacenar el artículo recién creado/actualizado

      // Lógica de creación o actualización del artículo manufacturado
      if (articuloToEdit) {
        // En modo edición, se envía el formData con el ID del artículo a actualizar
        newArticulo = await updateArticuloManufacturado(articuloToEdit.id, formData, token);
        alert('Artículo Manufacturado actualizado con éxito.');
      } else {
        // En modo creación, se envía el formData para un nuevo artículo
        newArticulo = await createArticuloManufacturado(formData, token);
        alert('Artículo Manufacturado creado con éxito.');
      }

      // --- Lógica de gestión de imágenes ---
      if (selectedFile) {
        // Si hay una nueva imagen seleccionada, se procede a subirla.
        // Primero, se eliminan las imágenes antiguas asociadas al artículo (si existen y estamos en modo edición).
        if (articuloToEdit && articuloToEdit.imagenes && articuloToEdit.imagenes.length > 0) {
          for (const oldImage of articuloToEdit.imagenes) {
            try {
              await deleteImageEntity(oldImage.id, token); // Elimina la entidad de imagen de la DB
              // Extrae el nombre del archivo de la URL y elimina el archivo físico
              if (oldImage.denominacion.includes('/api/files/view/')) {
                const oldFilename = oldImage.denominacion.substring(oldImage.denominacion.lastIndexOf('/') + 1);
                await deleteFileFromServer(oldFilename, token);
              }
            } catch (imgDelErr) {
              // Si falla la eliminación de una imagen antigua, se loguea una advertencia
              // pero no se interrumpe el proceso principal para no afectar la creación/actualización del artículo.
              console.warn(`Error al eliminar imagen antigua ${oldImage.id}:`, imgDelErr);
            }
          }
        }

        // Sube la nueva imagen y la asocia al artículo manufacturado recién creado/actualizado.
        // El `uploadFile` está diseñado para crear la entidad `Imagen` en la DB y asociarla
        // al artículo si se le pasa el `articuloId`.
        await uploadFile(selectedFile, token, newArticulo.id);
      }
      if (isProductWrapper) {
        if (formData.manufacturadoDetalles.length !== 1) {
          setError('Para productos simples (envoltorio de insumo), se requiere exactamente un ingrediente.');
          setSubmitting(false);
          return;
        }
        const unicoDetalle = formData.manufacturadoDetalles[0];
        const insumoSeleccionado = insumos.find(i => i.id === unicoDetalle.articuloInsumoId);
        if (insumoSeleccionado && insumoSeleccionado.esParaElaborar) {
          setError('El ingrediente seleccionado para un producto simple debe ser un insumo no elaborable (ej. una bebida).');
          setSubmitting(false);
          return;
        }
      }

      onSave(); // Llama al callback `onSave` para notificar al componente padre que se guardó
      handleClose(); // Cierra el modal
    } catch (err) {
      console.error('Error al guardar artículo manufacturado:', err);
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
        <Modal.Title>{articuloToEdit ? 'Editar Artículo Manufacturado' : 'Crear Artículo Manufacturado'}</Modal.Title>
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
                    <Form.Label>Tiempo Estimado (Minutos)</Form.Label>
                    <Form.Control
                      type="number"
                      name="tiempoEstimadoMinutos"
                      value={formData.tiempoEstimadoMinutos}
                      onChange={handleChange}
                      min="1" // Tiempo estimado debe ser al menos 1 minuto
                      required
                    />
                  </Form.Group>
                </Col>
              </Row>

              <Form.Group className="mb-3">
                <Form.Label>Unidad de Medida</Form.Label>
                <Form.Select
                  name="unidadMedidaId"
                  value={formData.unidadMedidaId || ''} // Debería estar pre-seteado a la ID de "Unidad"
                  onChange={handleChange}
                  required
                  disabled // Deshabilita si no hay opciones
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
                  value={formData.categoriaId || ''} // Usamos el ID directamente en el DTO
                  onChange={handleChange}
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
                <Form.Label>Descripción</Form.Label>
                <Form.Control
                  as="textarea"
                  name="descripcion"
                  value={formData.descripcion}
                  onChange={handleChange}
                  rows={3}
                  maxLength={1000} // Límite de caracteres, si aplica en el backend
                  required
                />
              </Form.Group>

              <Form.Group className="mb-3">
                <Form.Label>Preparación</Form.Label>
                <Form.Control
                  as="textarea"
                  name="preparacion"
                  value={formData.preparacion}
                  onChange={handleChange}
                  rows={5}
                  required
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
                <Form.Label>Imagen Principal del Artículo</Form.Label>
                <Form.Control type="file" onChange={handleFileChange} accept="image/*" />
                {selectedFile && <div className="mt-2">Archivo seleccionado: {selectedFile.name}</div>}

                {/* Muestra la imagen actual solo si estamos editando, no hay nueva imagen seleccionada y existen imágenes */}
                {articuloToEdit?.imagenes && articuloToEdit.imagenes.length > 0 && !selectedFile && (
                  <div className="mt-3 p-2 border rounded d-flex align-items-center">
                    <h6>Imagen Actual:</h6>
                    <img
                      src={getImageUrl(articuloToEdit.imagenes[0].denominacion)} // Usa getImageUrl
                      alt="Artículo"
                      style={{ width: '120px', height: '120px', objectFit: 'cover', border: '1px solid #ddd' }}
                      className="ms-2 me-2" // Añadido ms-2 para un pequeño margen
                    />
                    <span>{articuloToEdit.imagenes[0].denominacion.substring(articuloToEdit.imagenes[0].denominacion.lastIndexOf('/') + 1)}</span>
                  </div>
                )}
                {selectedFile && (
                  <Alert variant="warning" className="mt-2">
                    Se ha seleccionado una nueva imagen. Al guardar, esta reemplazará cualquier imagen existente.
                  </Alert>
                )}
              </Form.Group>
              <Form.Group className="mb-3">
                <Form.Check
                  type="checkbox"
                  label="Es un producto simple (basado en un solo insumo vendible, ej. Gaseosa)"
                  checked={isProductWrapper}
                  onChange={(e) => {
                    setIsProductWrapper(e.target.checked);
                    // Opcional: Si se marca, podrías limpiar los detalles existentes o limitar a uno.
                    // Por ahora, solo cambia el estado.
                    if (e.target.checked) {
                      // Si se marca, limpiar detalles y añadir uno nuevo vacío,
                      // o simplemente dejar que el usuario añada el único detalle.
                      // También podrías deshabilitar "Añadir Ingrediente" si ya hay uno.
                      setFormData(prev => ({
                        ...prev,
                        // Si se marca, quizá limpiar detalles si ya existen y son para elaborar.
                        // Opcional: podrías forzar a que solo haya un detalle.
                        manufacturadoDetalles: prev.manufacturadoDetalles.length > 0 && !prev.manufacturadoDetalles[0].articuloInsumoId ? prev.manufacturadoDetalles : []
                      }));
                    }
                  }}
                />
                {isProductWrapper && formData.manufacturadoDetalles.length > 1 && (
                  <Alert variant="warning" className="mt-1">
                    Para productos simples, solo se espera un ingrediente. Se considerará el primero.
                  </Alert>
                )}
              </Form.Group>

              {/* Sección de Detalles de Manufacturado (Ingredientes) */}
              <Card className="mt-4">
                <Card.Header className="d-flex justify-content-between align-items-center">
                  <h6>Ingredientes y Cantidades</h6>
                  <Button variant="outline-success" size="sm" onClick={handleAddDetalle} disabled={insumos.length === 0}>
                    <FontAwesomeIcon icon={faPlusCircle} /> Añadir Ingrediente {/* Asegúrate que faPlusCircle esté importado */}
                  </Button>
                </Card.Header>
                <ListGroup variant="flush">
                  {formData.manufacturadoDetalles.length === 0 ? (
                    <ListGroup.Item className="text-center text-muted">
                      No hay ingredientes añadidos. Haz clic en "Añadir Ingrediente" para empezar.
                    </ListGroup.Item>
                  ) : (
                    formData.manufacturadoDetalles.map((detalle, index) => {
                      // IDs de insumos ya seleccionados en OTROS detalles de esta receta
                      const insumosYaEnOtrosDetalles = formData.manufacturadoDetalles
                        .filter((_, i) => i !== index) // Excluir el detalle 
                        .map(d => d.articuloInsumoId)
                        .filter(id => id !== 0 && id !== undefined); // Importante: no considerar IDs no válidos

                      // Opciones de insumos disponibles para ESTE detalle
                      const opcionesDeInsumosDisponibles = insumos
                        .filter(insumo =>
                          isProductWrapper ? !insumo.esParaElaborar : insumo.esParaElaborar // Lógica condicional
                        )
                        .filter(insumoFiltrado =>
                          !insumosYaEnOtrosDetalles.includes(insumoFiltrado.id) ||
                          insumoFiltrado.id === detalle.articuloInsumoId
                        );

                      return (
                        <ListGroup.Item key={index}>
                          <Row className="align-items-center">
                            <Col xs={12} md={6}>
                              <Form.Group className="mb-3 mb-md-0">
                                <Form.Label>Insumo</Form.Label>
                                <Form.Select
                                  value={detalle.articuloInsumoId || ''}
                                  onChange={(e) => handleDetalleChange(index, 'articuloInsumoId', Number(e.target.value))}
                                  // Deshabilitar si no hay opciones o la lista original de insumos está vacía.
                                  disabled={opcionesDeInsumosDisponibles.length === 0}
                                  required
                                >
                                  <option value="">Selecciona un insumo</option>
                                  {opcionesDeInsumosDisponibles.map((insumoOpcion) => ( // Usa opcionesDeInsumosDisponibles
                                    <option key={insumoOpcion.id} value={insumoOpcion.id}>
                                      {insumoOpcion.denominacion}
                                    </option>
                                  ))}
                                </Form.Select>
                              </Form.Group>
                            </Col>
                            <Col xs={8} md={4}>
                              <Form.Group className="mb-3 mb-md-0">
                                <Form.Label>Cantidad</Form.Label>
                                <InputGroup>
                                  <Form.Control
                                    type="number"
                                    value={detalle.cantidad} // Asegúrate que esto no sea undefined o cause problemas
                                    onChange={(e) => handleDetalleChange(index, 'cantidad', parseFloat(e.target.value))}
                                    step="0.01"
                                    min="0.01"
                                    required
                                  />
                                  <InputGroup.Text>
                                    {insumos.find(i => i.id === detalle.articuloInsumoId)?.unidadMedida.denominacion || 'Unidad'}
                                  </InputGroup.Text>
                                </InputGroup>
                              </Form.Group>
                            </Col>
                            <Col xs={4} md={2} className="d-flex justify-content-end align-items-end">
                              <Button variant="danger" size="sm" onClick={() => handleRemoveDetalle(index)} className="mb-3 mb-md-0">
                                <FontAwesomeIcon icon={faMinusCircle} />
                              </Button>
                            </Col>
                          </Row>
                        </ListGroup.Item>
                      );
                    })
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
            {articuloToEdit ? 'Actualizar' : 'Crear'}
          </Button>
        </Modal.Footer>
      </Form>
    </Modal>
  );
};

export default ArticuloManufacturadoForm;
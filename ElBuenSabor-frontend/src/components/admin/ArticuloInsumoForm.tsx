// ArticuloInsumoForm.tsx
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
import { Modal, Form, Button, Alert, Spinner, Row, Col } from 'react-bootstrap';
import { useAuth0 } from '@auth0/auth0-react';
import { CategoriaService } from '../../services/categoriaService';
import { ArticuloInsumoService } from '../../services/articuloInsumoService';
import { UnidadMedidaService } from '../../services/unidadMedidaService';
import { FileUploadService } from '../../services/fileUploadService';
import { ImagenService } from '../../services/imagenService';
import type { ArticuloInsumo, Categoria, UnidadMedida, ArticuloInsumoResponseDTO } from '../../types/types';
import { FontAwesomeIcon } from '@fortawesome/react-fontawesome';
import { faTimesCircle } from '@fortawesome/free-solid-svg-icons';

// Instanciamos los servicios
const categoriaService = new CategoriaService();
const articuloInsumoService = new ArticuloInsumoService();
const unidadMedidaService = new UnidadMedidaService();
const fileUploadService = new FileUploadService();
const imagenService = new ImagenService();

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
  const { getAccessTokenSilently } = useAuth0();

  const [formData, setFormData] = useState<ArticuloInsumo>({
    id: 0,
    denominacion: '',
    precioVenta: 0,
    unidadMedida: { id: 0, denominacion: '' },
    categoria: { id: 0, denominacion: '', estadoActivo: true },
    estadoActivo: true,
    precioCompra: 0,
    stockActual: 0,
    stockMinimo: 0,
    esParaElaborar: false,
    imagenes: [],
  });

  const [categories, setCategories] = useState<Categoria[]>([]);
  const [unidadesMedida, setUnidadesMedida] = useState<UnidadMedida[]>([]);
  const [loadingOptions, setLoadingOptions] = useState(true);
  const [submitting, setSubmitting] = useState(false);
  const [error, setError] = useState<string | null>(null);
  const [selectedFile, setSelectedFile] = useState<File | null>(null);

  useEffect(() => {
    const loadOptions = async () => {
      setLoadingOptions(true);
      try {
        const [fetchedCategories, fetchedUnidades] = await Promise.all([
          categoriaService.getCategorias(),
          unidadMedidaService.getUnidadesMedida(),
        ]);
        setCategories(fetchedCategories);
        setUnidadesMedida(fetchedUnidades);
      } catch (err) {
        setError('Error al cargar categorías y unidades de medida.');
        console.error('Error al cargar opciones:', err);
      } finally {
        setLoadingOptions(false);
      }
    };
    loadOptions();
  }, []);

  useEffect(() => {
    if (show) {
      if (articuloToEdit) {
        setFormData(articuloToEdit);
      } else {
        setFormData({
          id: 0,
          denominacion: '',
          precioVenta: 0,
          unidadMedida: { id: 0, denominacion: '' },
          categoria: { id: 0, denominacion: '', estadoActivo: true },
          estadoActivo: true,
          precioCompra: 0,
          stockActual: 0,
          stockMinimo: 0,
          esParaElaborar: false,
          imagenes: [],
        });
      }
      setSelectedFile(null);
      setError(null);
    }
  }, [articuloToEdit, show]);

  const handleChange = (e: React.ChangeEvent<HTMLInputElement | HTMLSelectElement | HTMLTextAreaElement>) => {
    const { name, value } = e.target;
    const isCheckbox = (e.target as HTMLInputElement).type === 'checkbox';
    const checked = (e.target as HTMLInputElement).checked;

    setFormData((prev) => ({
      ...prev,
      [name]: isCheckbox ? checked : value,
    }));
  };

  const handleSelectChange = (e: React.ChangeEvent<HTMLSelectElement>) => {
    const { name, value } = e.target;
    const selectedId = Number(value);

    if (name === 'categoriaId') {
      const selectedCat = categories.find((cat) => cat.id === selectedId);
      if (selectedCat) {
        setFormData((prev) => ({
          ...prev,
          categoria: selectedCat,
        }));
      }
    } else if (name === 'unidadMedidaId') {
      const selectedUm = unidadesMedida.find((um) => um.id === selectedId);
      if (selectedUm) {
        setFormData((prev) => ({
          ...prev,
          unidadMedida: selectedUm,
        }));
      }
    }
  };

  const handleFileChange = (e: React.ChangeEvent<HTMLInputElement>) => {
    if (e.target.files && e.target.files[0]) {
      setSelectedFile(e.target.files[0]);
    } else {
      setSelectedFile(null);
    }
  };

  const handleDeleteImage = async (imageId: number, filename: string) => {
    if (!window.confirm('¿Estás seguro de que quieres eliminar esta imagen?')) {
      return;
    }
    try {
      const token = await getAccessTokenSilently();

      const fileNameToDetele = filename.substring(filename.lastIndexOf('/') + 1);

      await imagenService.deleteImageEntity(imageId, token);
      await fileUploadService.deleteFileFromServer(fileNameToDetele, token);

      setFormData((prev) => ({
        ...prev,
        imagenes: prev.imagenes.filter((img) => img.id !== imageId),
      }));
      alert('Imagen eliminada con éxito.');
    } catch (err) {
      console.error('Error al eliminar imagen:', err);
      const errorMessage = (err as any).response?.data?.message || (err as any).message || 'Error desconocido al eliminar.';
      setError(`Error al eliminar: ${errorMessage}`);
    }
  };

  const handleSubmit = async (e: React.FormEvent) => {
    e.preventDefault();
    setSubmitting(true);
    setError(null);

    try {
      const token = await getAccessTokenSilently();

      if (!formData.denominacion ||
        Number(formData.precioVenta) <= 0 ||
        !formData.categoria?.id || formData.categoria.id === 0 ||
        !formData.unidadMedida?.id || formData.unidadMedida.id === 0 ||
        Number(formData.stockActual) < 0 ||
        Number(formData.precioCompra) < 0 ||
        Number(formData.stockMinimo) < 0
      ) {
        setError('Por favor, completa todos los campos obligatorios correctamente. Asegúrate que Categoría y Unidad de Medida estén seleccionados y los valores numéricos sean válidos.');
        setSubmitting(false);
        return;
      }
      
      const payload = {
        denominacion: formData.denominacion,
        precioVenta: Number(formData.precioVenta),
        unidadMedidaId: formData.unidadMedida.id,
        categoriaId: formData.categoria.id,
        estadoActivo: formData.estadoActivo,
        precioCompra: Number(formData.precioCompra),
        stockActual: Number(formData.stockActual),
        stockMinimo: Number(formData.stockMinimo),
        esParaElaborar: formData.esParaElaborar,
      };

      let savedArticulo: ArticuloInsumoResponseDTO;

      if (articuloToEdit && articuloToEdit.id) {
        savedArticulo = await articuloInsumoService.updateArticuloInsumo(articuloToEdit.id, payload, token);
        alert('Artículo Insumo actualizado con éxito.');
      } else {
        savedArticulo = await articuloInsumoService.createArticuloInsumo(payload, token);
        alert('Artículo Insumo creado con éxito.');
      }

      if (selectedFile && savedArticulo && savedArticulo.id) {
        try {
          await fileUploadService.uploadFile(selectedFile, token, savedArticulo.id, undefined);
        } catch (uploadError) {
          console.error("Error al subir la imagen después de guardar el artículo:", uploadError);
          setError((prevError) => (prevError ? prevError + " " : "") + "Artículo guardado, pero hubo un error al subir la imagen.");
        }
      }

      onSave();
      handleClose();
    } catch (err: any) {
      console.error('Error al guardar artículo insumo:', err);
      const backendError = err.response?.data?.error || err.response?.data?.message || (Array.isArray(err.response?.data?.mensajes) ? err.response.data.mensajes.join(', ') : null);
      const errorMessage = backendError || err.message || 'Error desconocido al guardar.';
      setError(`Error al guardar: ${errorMessage}`);
    } finally {
      setSubmitting(false);
    }
  };

  return (
    <Modal show={show} onHide={handleClose} size="lg">
      <Modal.Header closeButton>
        <Modal.Title>{articuloToEdit ? 'Editar Artículo Insumo' : 'Crear Artículo Insumo'}</Modal.Title>
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
                      step="0.01"
                      min="0.01"
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
                      value={formData.precioCompra || ''}
                      onChange={handleChange}
                      step="0.01"
                      min="0"
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
                      value={formData.stockActual || ''}
                      onChange={handleChange}
                      step="0.01"
                      min="0"
                    />
                  </Form.Group>
                </Col>
                <Col>
                  <Form.Group className="mb-3">
                    <Form.Label>Stock Minimo</Form.Label>
                    <Form.Control
                      type="number"
                      name="stockMinimo"
                      value={formData.stockMinimo || ''}
                      onChange={handleChange}
                      step="0.01"
                      min="0"
                    />
                  </Form.Group>
                </Col>
              </Row>

              <Form.Group className="mb-3">
                <Form.Label>Unidad de Medida</Form.Label>
                <Form.Select
                  name="unidadMedidaId"
                  value={formData.unidadMedida?.id || ''}
                  onChange={handleSelectChange}
                  required
                  disabled={unidadesMedida.length === 0}
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
                  value={formData.categoria?.id || ''}
                  onChange={handleSelectChange}
                  required
                  disabled={categories.length === 0}
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

              <Form.Group className="mb-3">
                <Form.Label>Imagen del Artículo</Form.Label>
                <Form.Control type="file" onChange={handleFileChange} accept="image/*" />
                {selectedFile && <div className="mt-2">Archivo seleccionado: {selectedFile.name}</div>}

                {formData.imagenes && formData.imagenes.length > 0 && (
                  <div className="mt-3">
                    <h6>Imagen(es) Actual(es):</h6>
                    {formData.imagenes.map((img) => (
                      <div key={img.id} className="d-flex align-items-center mb-2 p-2 border rounded">
                        <img
                          src={fileUploadService.getImageUrl(img.denominacion)}
                          alt="Artículo"
                          style={{ width: '80px', height: '80px', objectFit: 'cover', border: '1px solid #ddd' }}
                          className="me-2"
                        />
                        <span>{img.denominacion.substring(img.denominacion.lastIndexOf('/') + 1)}</span>
                        <Button
                          variant="danger"
                          size="sm"
                          className="ms-auto"
                          onClick={() => handleDeleteImage(img.id!, img.denominacion)} // [Cita: 1] AQUÍ ESTÁ EL CAMBIO PRINCIPAL: Se añade `!`
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
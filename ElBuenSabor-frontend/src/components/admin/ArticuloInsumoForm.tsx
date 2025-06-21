import React, { useState, useEffect } from 'react';
import { Modal, Form, Button, Alert, Spinner, Row, Col, Image } from 'react-bootstrap';
import { CategoriaService } from '../../services/CategoriaService';
import { ArticuloInsumoService } from '../../services/ArticuloInsumoService';
import { UnidadMedidaService } from '../../services/UnidadMedidaService';
import { FileUploadService } from '../../services/FileUploadService';
import { ImagenService } from '../../services/ImagenService';
import type { ArticuloInsumoRequest, CategoriaResponse, UnidadMedidaResponse, ArticuloInsumoResponse, ImagenResponse } from '../../types/types';
import { FontAwesomeIcon } from '@fortawesome/react-fontawesome';
import { faTimesCircle } from '@fortawesome/free-solid-svg-icons';

interface ArticuloInsumoFormProps {
  show: boolean;
  handleClose: () => void;
  onSave: () => void;
  articuloToEdit?: ArticuloInsumoResponse | null;
}

const initialFormData: ArticuloInsumoRequest = {
  denominacion: '',
  precioVenta: 0,
  unidadMedidaId: 0,
  categoriaId: 0,
  estadoActivo: true,
  precioCompra: 0,
  esParaElaborar: false,
};

const ArticuloInsumoForm: React.FC<ArticuloInsumoFormProps> = ({ show, handleClose, onSave, articuloToEdit }) => {
  // 2. SE ELIMINA LA LLAMADA AL HOOK useAuth0
  const [formData, setFormData] = useState<ArticuloInsumoRequest>(initialFormData);
  const [imagenes, setImagenes] = useState<ImagenResponse[]>([]);
  const [categories, setCategories] = useState<CategoriaResponse[]>([]);
  const [unidadesMedida, setUnidadesMedida] = useState<UnidadMedidaResponse[]>([]);
  const [loadingOptions, setLoadingOptions] = useState(true);
  const [submitting, setSubmitting] = useState(false);
  const [error, setError] = useState<string | null>(null);
  const [selectedFile, setSelectedFile] = useState<File | null>(null);

  useEffect(() => {
    const loadOptions = async () => {
      if (!show) return;
      setLoadingOptions(true);
      try {
        const [fetchedCategories, fetchedUnidades] = await Promise.all([
          CategoriaService.getAll(),
          UnidadMedidaService.getAll(),
        ]);
        setCategories(fetchedCategories);
        setUnidadesMedida(fetchedUnidades);
      } catch (err) {
        setError('Error al cargar opciones.');
      } finally {
        setLoadingOptions(false);
      }
    };
    loadOptions();
  }, [show]);

  useEffect(() => {
    if (show) {
      if (articuloToEdit) {
        setFormData({
          denominacion: articuloToEdit.denominacion,
          precioVenta: articuloToEdit.precioVenta,
          unidadMedidaId: articuloToEdit.unidadMedida.id,
          categoriaId: articuloToEdit.categoria.id,
          estadoActivo: articuloToEdit.estadoActivo,
          precioCompra: articuloToEdit.precioCompra ?? 0,
          esParaElaborar: articuloToEdit.esParaElaborar,
        });
        setImagenes(articuloToEdit.imagenes);
      } else {
        setFormData(initialFormData);
        setImagenes([]);
      }
      setSelectedFile(null);
      setError(null);
    }
  }, [articuloToEdit, show]);

  const handleChange = (e: React.ChangeEvent<HTMLInputElement | HTMLSelectElement>) => {
    const { name, value, type } = e.target;
    const isCheckbox = type === 'checkbox';
    const checked = (e.target as HTMLInputElement).checked;
    setFormData((prev) => ({
      ...prev,
      [name]: isCheckbox ? checked : (type === 'number' ? parseFloat(value) || 0 : value),
    }));
  };
  
  const handleFileChange = (e: React.ChangeEvent<HTMLInputElement>) => {
    setSelectedFile(e.target.files ? e.target.files[0] : null);
  };

  const handleDeleteImage = async (imageId: number, filename: string) => {
    if (!window.confirm('¿Seguro que quieres eliminar esta imagen?')) return;
    try {
      const fileNameToDelete = filename.substring(filename.lastIndexOf('/') + 1);
      // 3. LAS LLAMADAS AL SERVICIO YA NO PASAN EL TOKEN
      await ImagenService.delete(imageId);
      await FileUploadService.deleteFile(fileNameToDelete);
      setImagenes(prev => prev.filter(img => img.id !== imageId));
      alert('Imagen eliminada con éxito.');
    } catch (err) {
      setError('Error al eliminar la imagen.');
    }
  };

  const handleSubmit = async (e: React.FormEvent) => {
    e.preventDefault();
    setSubmitting(true);
    setError(null);

    if (!formData.denominacion || !formData.categoriaId || !formData.unidadMedidaId || formData.precioVenta <= 0) {
      setError('Por favor, completa todos los campos obligatorios.');
      setSubmitting(false);
      return;
    }
    
    try {
      // 4. LAS LLAMADAS AL SERVICIO YA NO PASAN EL TOKEN
      const savedArticulo = articuloToEdit?.id
        ? await ArticuloInsumoService.update(articuloToEdit.id, formData)
        : await ArticuloInsumoService.create(formData);

      if (selectedFile) {
        await FileUploadService.uploadFile(selectedFile, { articuloId: savedArticulo.id });
      }

      alert(`Artículo Insumo ${articuloToEdit ? 'actualizado' : 'creado'} con éxito.`);
      onSave();
      handleClose();

    } catch (err: any) {
      const errorMessage = err.response?.data?.message || err.message || 'Error desconocido.';
      setError(`Error al guardar: ${errorMessage}`);
    } finally {
      setSubmitting(false);
    }
  };

  return (
    <Modal show={show} onHide={handleClose} size="lg" backdrop="static">
      <Modal.Header closeButton>
        <Modal.Title>{articuloToEdit ? 'Editar Artículo Insumo' : 'Crear Artículo Insumo'}</Modal.Title>
      </Modal.Header>
      <Form onSubmit={handleSubmit}>
        <Modal.Body>
          {loadingOptions ? <div className="text-center"><Spinner animation="border" /></div> : error ? <Alert variant="danger">{error}</Alert> : (
            <>
              <Form.Group className="mb-3"><Form.Label>Denominación</Form.Label><Form.Control type="text" name="denominacion" value={formData.denominacion} onChange={handleChange} required /></Form.Group>
              <Row>
                <Col><Form.Group className="mb-3"><Form.Label>Precio Venta</Form.Label><Form.Control type="number" name="precioVenta" value={formData.precioVenta} onChange={handleChange} step="0.01" min="0.01" required /></Form.Group></Col>
                <Col><Form.Group className="mb-3"><Form.Label>Precio Compra</Form.Label><Form.Control type="number" name="precioCompra" value={formData.precioCompra || ''} onChange={handleChange} step="0.01" min="0" /></Form.Group></Col>
              </Row>
              <Row>
                <Col><Form.Group className="mb-3"><Form.Label>Unidad de Medida</Form.Label><Form.Select name="unidadMedidaId" value={formData.unidadMedidaId} onChange={handleChange} required><option value="">Selecciona una Unidad</option>{unidadesMedida.map((um) => <option key={um.id} value={um.id}>{um.denominacion}</option>)}</Form.Select></Form.Group></Col>
                <Col><Form.Group className="mb-3"><Form.Label>Categoría</Form.Label><Form.Select name="categoriaId" value={formData.categoriaId} onChange={handleChange} required><option value="">Selecciona una Categoría</option>{categories.map((cat) => <option key={cat.id} value={cat.id}>{cat.denominacion}</option>)}</Form.Select></Form.Group></Col>
              </Row>
              <Form.Group className="mb-3"><Form.Check type="checkbox" label="Es Para Elaborar" name="esParaElaborar" checked={formData.esParaElaborar} onChange={handleChange} /></Form.Group>
              <Form.Group className="mb-3"><Form.Check type="checkbox" label="Estado Activo" name="estadoActivo" checked={formData.estadoActivo} onChange={handleChange} /></Form.Group>
              <Form.Group className="mb-3">
                <Form.Label>Imagen</Form.Label>
                <Form.Control type="file" onChange={handleFileChange} accept="image/*" />
                {imagenes.length > 0 && <div className="mt-3">
                  <h6>Imagen Actual:</h6>
                  {imagenes.map((img) => <div key={img.id} className="d-flex align-items-center mb-2 p-2 border rounded">
                    <Image src={img.denominacion} alt="Artículo" style={{ width: '80px', height: '80px', objectFit: 'cover' }} className="me-2" />
                    <span className="text-truncate" style={{maxWidth: '200px'}}>{img.denominacion.substring(img.denominacion.lastIndexOf('/') + 1)}</span>
                    <Button variant="danger" size="sm" className="ms-auto" onClick={() => handleDeleteImage(img.id, img.denominacion)}><FontAwesomeIcon icon={faTimesCircle} /></Button>
                  </div>)}
                </div>}
              </Form.Group>
            </>
          )}
        </Modal.Body>
        <Modal.Footer>
          <Button variant="secondary" onClick={handleClose} disabled={submitting}>Cancelar</Button>
          <Button variant="primary" type="submit" disabled={submitting || loadingOptions}>
            {submitting && <Spinner as="span" size="sm" className="me-2" />}
            {articuloToEdit ? 'Actualizar' : 'Crear'}
          </Button>
        </Modal.Footer>
      </Form>
    </Modal>
  );
};

export default ArticuloInsumoForm;
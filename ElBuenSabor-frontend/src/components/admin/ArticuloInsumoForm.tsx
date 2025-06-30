import React, { useState, useEffect } from 'react';
import { Modal, Form, Button, Alert, Spinner, Row, Col, Image } from 'react-bootstrap';
import { ArticuloInsumoService } from '../../services/articuloInsumoService';
import { UnidadMedidaService } from '../../services/unidadMedidaService';
import { FileUploadService } from '../../services/fileUploadService';
import { ImagenService } from '../../services/imagenService';
import { StockInsumoSucursalService } from '../../services/StockInsumoSucursalService'; // <-- 2. Importamos el servicio de Stock

import { useSucursal } from '../../context/SucursalContext';

import type { ArticuloInsumoRequest, UnidadMedidaResponse, ArticuloInsumoResponse, ImagenResponse, StockInsumoSucursalRequest } from '../../types/types';
import { FontAwesomeIcon } from '@fortawesome/react-fontawesome';
import { faTimesCircle } from '@fortawesome/free-solid-svg-icons';
import apiClient from '../../services/apiClient';


interface ArticuloInsumoFormProps {
  show: boolean;
  handleClose: () => void;
  onSave: () => void;
  articuloToEdit?: ArticuloInsumoResponse | null;
}

const initialFormData: Omit<ArticuloInsumoRequest, 'categoriaId'> = {
  denominacion: '',
  precioVenta: 0,
  unidadMedidaId: 0,
  estadoActivo: true,
  precioCompra: 0,
  esParaElaborar: false,
};

const ArticuloInsumoForm: React.FC<ArticuloInsumoFormProps> = ({ show, handleClose, onSave, articuloToEdit }) => {
  const { selectedSucursal } = useSucursal();

  const [formData, setFormData] = useState(initialFormData);
  const [imagenes, setImagenes] = useState<ImagenResponse[]>([]);
  //const [categories, setCategories] = useState<CategoriaResponse[]>([]);
  const [unidadesMedida, setUnidadesMedida] = useState<UnidadMedidaResponse[]>([]);
  const [stockActual, setStockActual] = useState<number>(0);
  const [stockMinimo, setStockMinimo] = useState<number>(0);
  const [stockId, setStockId] = useState<number | null>(null);

  const [loadingOptions, setLoadingOptions] = useState(true);
  const [submitting, setSubmitting] = useState(false);
  const [error, setError] = useState<string | null>(null);
  const [selectedFile, setSelectedFile] = useState<File | null>(null);

  useEffect(() => {
    const loadData = async () => {
      if (!show) return;

      setLoadingOptions(true);
      setError(null);

      // Si no hay sucursal seleccionada, no se puede gestionar el stock.
      if (!selectedSucursal) {
        setError("Por favor, selecciona una sucursal para gestionar los artículos.");
        setLoadingOptions(false);
        return;
      }

      try {
        // Obtenemos las categorías las unidades de medida
        const fetchedUnidades = await UnidadMedidaService.getAll();
        setUnidadesMedida(fetchedUnidades);

        // Si estamos editando, cargamos los datos del insumo Y su stock en la sucursal
        if (articuloToEdit) {
          setFormData({
            denominacion: articuloToEdit.denominacion,
            precioVenta: articuloToEdit.precioVenta,
            unidadMedidaId: articuloToEdit.unidadMedida.id,
            //categoriaId: articuloToEdit.categoria.id,
            estadoActivo: articuloToEdit.estadoActivo,
            precioCompra: articuloToEdit.precioCompra ?? 0,
            esParaElaborar: articuloToEdit.esParaElaborar,
          });
          setImagenes(articuloToEdit.imagenes);

          try {
            // Buscamos el stock específico
            const stockInfo = await StockInsumoSucursalService.getStockByInsumoAndSucursal(articuloToEdit.id, selectedSucursal.id);
            setStockActual(stockInfo?.stockActual ?? 0);
            setStockMinimo(stockInfo?.stockMinimo ?? 0);
            setStockId(stockInfo?.id ?? null);
          } catch (stockError) {
            // Si no existe registro de stock, asumimos que es 0
            setStockActual(0);
            setStockMinimo(0);
            setStockId(null);
          }
        } else {
          // Si estamos creando, reseteamos todo
          setFormData(initialFormData);
          setImagenes([]);
          setStockActual(0);
          setStockMinimo(0);
          setStockId(null);
        }

      } catch (err) {
        setError('Error al cargar opciones del formulario.');
      } finally {
        setLoadingOptions(false);
      }
    };
    loadData();
  }, [show, articuloToEdit, selectedSucursal]);
  const handleChange = (e: React.ChangeEvent<any>) => {
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
       if (!window.confirm(`¿Seguro que quieres eliminar la imagen: ${filename}?`)) return;
    
    try {
      // FIX: Ya no es necesario hacer el substring. Pasamos el nombre del archivo directamente.
      await ImagenService.delete(imageId);
      await FileUploadService.deleteFile(filename); 
      
      // Actualizamos el estado local para que la imagen desaparezca de la UI al instante
      setImagenes(prev => prev.filter(img => img.id !== imageId));
      
      // No mostramos una alerta de éxito aquí, para no interrumpir el flujo de guardado principal.
      console.log(`Imagen ${filename} eliminada.`);

    } catch (err) {
      console.error('Error al eliminar la imagen:', err);
      // Lanzamos el error para que el handleSubmit principal lo capture y muestre un mensaje general.
      throw new Error('No se pudo eliminar la imagen antigua.');
    }
  };

  const handleSubmit = async (e: React.FormEvent) => {
    e.preventDefault();
    setSubmitting(true);
    setError(null);
    const dataToSend: ArticuloInsumoRequest = {
      ...formData,
      categoriaId: 4 // ID para la categoría "Insumos"
    };

    if (!selectedSucursal) {
      setError("No hay una sucursal seleccionada para guardar el stock.");
      setSubmitting(false);
      return;
    }

    try {
      // 1. Guardamos el artículo insumo (crear o actualizar)
      const savedArticulo = articuloToEdit
        ? await ArticuloInsumoService.update(articuloToEdit.id, dataToSend)
        : await ArticuloInsumoService.create(dataToSend);

      // 2. Creamos o actualizamos el stock para la sucursal
      const stockData: StockInsumoSucursalRequest = {
        stockActual: stockActual,
        stockMinimo: stockMinimo,
        articuloInsumoId: savedArticulo.id,
        sucursalId: selectedSucursal.id
      };

      if (stockId) { // Si ya existía un registro de stock, lo actualizamos
        await StockInsumoSucursalService.update(stockId, stockData);
      } else { // Si no, creamos uno nuevo
        await StockInsumoSucursalService.create(stockData);
      }

      // 3. Manejamos la subida de imagen si hay una nueva
              if (selectedFile) {
        // Si hay una imagen nueva, primero intentamos borrar las antiguas.
        if (imagenes.length > 0) {
            console.log("Eliminando imágenes antiguas...");
            // Usamos un for...of para asegurar que se borren secuencialmente
            for (const img of imagenes) {
                // El handleDeleteImage ahora es más simple
                await handleDeleteImage(img.id, img.denominacion.substring(img.denominacion.lastIndexOf('/') + 1));
            }
        }
        // Luego, subimos la nueva imagen
        console.log("Subiendo nueva imagen...");
        await FileUploadService.uploadFile(selectedFile, { articuloId: savedArticulo.id });
      }
      
      alert(`Artículo Insumo ${articuloToEdit ? 'actualizado' : 'creado'} con éxito.`);
      onSave();
      handleClose();
    } catch (err: any) {
      setError(`Error al guardar: ${err.message}`);
    } finally {
      setSubmitting(false);
    }
  };

  return (
    <Modal show={show} onHide={handleClose} size="lg" backdrop="static">
      <Modal.Header closeButton>
        <Modal.Title>{articuloToEdit ? 'Editar Insumo' : 'Nuevo Insumo'} en Sucursal: {selectedSucursal?.nombre ?? ''}</Modal.Title>
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
                <Col><Form.Group className="mb-3"><Form.Label>Unidad de Medida</Form.Label><Form.Select name="unidadMedidaId" value={formData.unidadMedidaId} onChange={handleChange} required><option value="">Selecciona una Unidad</option>{unidadesMedida.map((um) => <option key={um.id} value={um.id}>{um.denominacion}</option>)}</Form.Select></Form.Group></Col>              </Row>
              <Form.Group className="mb-3"><Form.Check type="checkbox" label="Es Para Elaborar" name="esParaElaborar" checked={formData.esParaElaborar} onChange={handleChange} /></Form.Group>
              <Form.Group className="mb-3"><Form.Check type="checkbox" label="Estado Activo" name="estadoActivo" checked={formData.estadoActivo} onChange={handleChange} /></Form.Group>

              <hr />
              <h5 className="mb-3">Stock en Sucursal</h5>
              <Row>
                <Col>
                  <Form.Group className="mb-3">
                    <Form.Label>Stock Actual</Form.Label>
                    <Form.Control type="number" value={stockActual} onChange={(e) => setStockActual(Number(e.target.value))} min="0" />
                  </Form.Group>
                </Col>
                <Col>
                  <Form.Group className="mb-3">
                    <Form.Label>Stock Mínimo</Form.Label>
                    <Form.Control type="number" value={stockMinimo} onChange={(e) => setStockMinimo(Number(e.target.value))} min="0" />
                  </Form.Group>
                </Col>
              </Row>

              <Form.Group className="mb-3">
                <Form.Label>Imagen</Form.Label>
                <Form.Control type="file" onChange={handleFileChange} accept="image/*" />
                {imagenes.length > 0 && <div className="mt-3">
                  <h6>Imagen Actual:</h6>
                  {imagenes.map((img) => <div key={img.id} className="d-flex align-items-center mb-2 p-2 border rounded">
                    <Image src={`${apiClient.defaults.baseURL}/files/view/${img.denominacion.substring(img.denominacion.lastIndexOf('/') + 1)}`} alt="Artículo" style={{ width: '80px', height: '80px', objectFit: 'cover' }} className="me-2" />
                    <span className="text-truncate" style={{ maxWidth: '200px' }}>{img.denominacion.substring(img.denominacion.lastIndexOf('/') + 1)}</span>
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
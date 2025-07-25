import React, { useState, useEffect } from 'react';
import { Modal, Form, Button, Alert, Spinner, Row, Col, Card, ListGroup, Image } from 'react-bootstrap';
import { FontAwesomeIcon } from '@fortawesome/react-fontawesome';
import { faPlusCircle, faMinusCircle } from '@fortawesome/free-solid-svg-icons';

// Servicios
import { PromocionService } from '../../services/PromocionService';
import { SucursalService } from '../../services/sucursalService';
import { ArticuloManufacturadoService } from '../../services/articuloManufacturadoService';
import { FileUploadService } from '../../services/fileUploadService';
import { ImagenService } from '../../services/imagenService';
import apiClient from '../../services/apiClient';

// Tipos y Enums
import type { PromocionResponse, PromocionRequest, ArticuloSimpleResponse, SucursalSimpleResponse, PromocionDetalleRequest, ImagenResponse } from '../../types/types';
import { type TipoPromocion } from '../../types/enums';

// Props del componente
interface PromocionFormProps {
    show: boolean;
    handleClose: () => void;
    onSave: () => void;
    promocionToEdit?: PromocionResponse | null;
}

const tipoPromocionOptions: TipoPromocion[] = ['PORCENTAJE', 'CANTIDAD', 'COMBO'];

// Estado inicial para el formulario de creación
const initialFormData: PromocionRequest = {
    denominacion: '',
    fechaDesde: '',
    fechaHasta: '',
    horaDesde: '00:00:00',
    horaHasta: '23:59:59',
    descripcionDescuento: '',
    precioPromocional: 0,
    tipoPromocion: 'PORCENTAJE',
    porcentajeDescuento: 0,
    detallesPromocion: [],
    estadoActivo: true,
    sucursalIds: [],
    imagenIds: [],
};

const PromocionForm: React.FC<PromocionFormProps> = ({ show, handleClose, onSave, promocionToEdit }) => {
    const [formData, setFormData] = useState<PromocionRequest>(initialFormData);
    const [articulos, setArticulos] = useState<ArticuloSimpleResponse[]>([]);
    const [sucursales, setSucursales] = useState<SucursalSimpleResponse[]>([]);
    const [loadingOptions, setLoadingOptions] = useState(true);
    const [submitting, setSubmitting] = useState(false);
    const [error, setError] = useState<string | null>(null);
    const [selectedFile, setSelectedFile] = useState<File | null>(null);
    const [currentImagenes, setCurrentImagenes] = useState<ImagenResponse[]>([]);

    useEffect(() => {
        const loadOptions = async () => {
            if (!show) return;
            setLoadingOptions(true);
            setError(null);
            try {
                const [fetchedSucursales, fetchedManufacturados] = await Promise.all([
                    SucursalService.getAll(),
                    ArticuloManufacturadoService.getAll(),
                ]);
                const allArticulos: ArticuloSimpleResponse[] = [
                    ...fetchedManufacturados.map(a => ({ id: a.id, denominacion: a.denominacion, precioVenta: a.precioVenta })),
                ];
                setSucursales(fetchedSucursales.map(s => ({ id: s.id, nombre: s.nombre })));
                setArticulos(allArticulos.sort((a, b) => a.denominacion.localeCompare(b.denominacion)));
            } catch (err) {
                setError('Error al cargar las opciones del formulario.');
                console.error(err);
            } finally {
                setLoadingOptions(false);
            }
        };
        loadOptions();
    }, [show]);

    useEffect(() => {
        if (show) {
            if (promocionToEdit) {
                setFormData({
                    denominacion: promocionToEdit.denominacion,
                    fechaDesde: promocionToEdit.fechaDesde,
                    fechaHasta: promocionToEdit.fechaHasta,
                    horaDesde: promocionToEdit.horaDesde,
                    horaHasta: promocionToEdit.horaHasta,
                    descripcionDescuento: promocionToEdit.descripcionDescuento || '',
                    precioPromocional: promocionToEdit.precioPromocional || 0,
                    tipoPromocion: promocionToEdit.tipoPromocion,
                    porcentajeDescuento: promocionToEdit.porcentajeDescuento || 0,
                    estadoActivo: promocionToEdit.estadoActivo,
                    sucursalIds: promocionToEdit.sucursales.map(s => s.id),
                    detallesPromocion: promocionToEdit.detallesPromocion.map(d => ({
                        articuloId: d.articulo.id,
                        cantidad: d.cantidad,
                    })),
                    imagenIds: promocionToEdit.imagenes.map(img => img.id),
                });
                setCurrentImagenes(promocionToEdit.imagenes);
            } else {
                setFormData(initialFormData);
                setCurrentImagenes([]);
            }
            setSelectedFile(null);
            setError(null);
        }
    }, [promocionToEdit, show]);

    const handleChange = (e: React.ChangeEvent<any>) => {
        const { name, value, type } = e.target;
        const isCheckbox = type === 'checkbox';
        const checked = (e.target as HTMLInputElement).checked;
        setFormData(prev => ({ ...prev, [name]: isCheckbox ? checked : (type === 'number' ? parseFloat(value) || 0 : value) }));
    };

    const handleFileChange = (e: React.ChangeEvent<HTMLInputElement>) => {
        setSelectedFile(e.target.files ? e.target.files[0] : null);
    };

    const handleSucursalChange = (e: React.ChangeEvent<any>) => {
        const selectedIds = Array.from(e.target.selectedOptions, (option: any) => Number(option.value));
        setFormData(prev => ({ ...prev, sucursalIds: selectedIds }));
    };

    const handleAddDetalle = () => {
        const newDetalle: PromocionDetalleRequest = { articuloId: 0, cantidad: 1 };
        setFormData(prev => ({ ...prev, detallesPromocion: [...prev.detallesPromocion, newDetalle] }));
    };

    const handleRemoveDetalle = (index: number) => {
        setFormData(prev => ({ ...prev, detallesPromocion: prev.detallesPromocion.filter((_, i) => i !== index) }));
    };

    const handleDetalleChange = (index: number, name: string, value: any) => {
        const newDetails = [...formData.detallesPromocion];
        newDetails[index] = { ...newDetails[index], [name]: Number(value) };
        setFormData(prev => ({ ...prev, detallesPromocion: newDetails }));
    };

    const handleSubmit = async (e: React.FormEvent) => {
        e.preventDefault();
        setSubmitting(true);
        setError(null);

        if (formData.detallesPromocion.length === 0 || formData.sucursalIds.length === 0) {
            setError("Debe añadir al menos un artículo y seleccionar una sucursal.");
            setSubmitting(false);
            return;
        }

        try {
            let finalImageIds = formData.imagenIds;

            if (selectedFile) {
                if (promocionToEdit && currentImagenes.length > 0) {
                    for (const oldImage of currentImagenes) {
                        await ImagenService.delete(oldImage.id);
                        const oldFilename = oldImage.denominacion.substring(oldImage.denominacion.lastIndexOf('/') + 1);
                        await FileUploadService.deleteFile(oldFilename);
                    }
                }
                const uploadResponse = await FileUploadService.uploadFile(selectedFile, {});
                finalImageIds = [uploadResponse.imagenDB.id];
            }

            const formatTime = (time: string) => time.length === 5 ? `${time}:00` : time;

            const dataToSend: PromocionRequest = {
                ...formData,
                horaDesde: formatTime(formData.horaDesde),
                horaHasta: formatTime(formData.horaHasta),
                precioPromocional: (formData.tipoPromocion === 'CANTIDAD' || formData.tipoPromocion === 'COMBO') ? formData.precioPromocional : undefined,
                porcentajeDescuento: formData.tipoPromocion === 'PORCENTAJE' ? formData.porcentajeDescuento : undefined,
                imagenIds: finalImageIds
            };

            const savedPromocion = promocionToEdit
                ? await PromocionService.update(promocionToEdit.id, dataToSend)
                : await PromocionService.create(dataToSend);

            if (selectedFile && finalImageIds.length > 0) {
                const imagenId = finalImageIds[0];
                const imagenData = await ImagenService.getById(imagenId);
                await ImagenService.update(imagenId, { ...imagenData, promocionId: savedPromocion.id });
            }

            alert(`Promoción ${promocionToEdit ? 'actualizada' : 'creada'} con éxito.`);
            onSave();
            handleClose();
        } catch (err: any) {
            setError(err?.response?.data?.message || err.message || 'Error al guardar la promoción.');
            console.error(err);
        } finally {
            setSubmitting(false);
        }
    };

    return (
        <Modal show={show} onHide={handleClose} size="lg" backdrop="static">
            <Modal.Header closeButton>
                <Modal.Title>{promocionToEdit ? 'Editar Promoción' : 'Crear Promoción'}</Modal.Title>
            </Modal.Header>
            <Form onSubmit={handleSubmit}>
                <Modal.Body>
                    {loadingOptions ? (
                        <div className="text-center"><Spinner animation="border" /> Cargando...</div>
                    ) : (
                        <>
                            {error && <Alert variant="danger">{error}</Alert>}
                            <Form.Group className="mb-3"><Form.Label>Denominación</Form.Label><Form.Control type="text" name="denominacion" value={formData.denominacion} onChange={handleChange} required /></Form.Group>
                            <Form.Group className="mb-3"><Form.Label>Descripción del Descuento</Form.Label><Form.Control as="textarea" rows={2} name="descripcionDescuento" value={formData.descripcionDescuento} onChange={handleChange} placeholder="Ej: Llevate 2 pizzas muzzarella al precio de 1" /></Form.Group>
                            <Form.Group className="mb-3">
                                <Form.Label>Imagen de la Promoción</Form.Label>
                                <Form.Control type="file" onChange={handleFileChange} accept="image/*" />
                                {currentImagenes.length > 0 && !selectedFile && (
                                    <div className="mt-2">
                                        <Image src={`${apiClient.defaults.baseURL}/files/view/${currentImagenes[0].denominacion}`} thumbnail style={{ width: '100px' }} />
                                    </div>
                                )}
                            </Form.Group>
                            <Row>
                                <Col md={6}><Form.Group className="mb-3"><Form.Label>Fecha Desde</Form.Label><Form.Control type="date" name="fechaDesde" value={formData.fechaDesde} onChange={handleChange} required /></Form.Group></Col>
                                <Col md={6}><Form.Group className="mb-3"><Form.Label>Fecha Hasta</Form.Label><Form.Control type="date" name="fechaHasta" value={formData.fechaHasta} onChange={handleChange} required /></Form.Group></Col>
                            </Row>
                            <Row>
                                <Col md={6}><Form.Group className="mb-3"><Form.Label>Hora Desde</Form.Label><Form.Control type="time" name="horaDesde" value={formData.horaDesde.substring(0, 5)} onChange={handleChange} required /></Form.Group></Col>
                                <Col md={6}><Form.Group className="mb-3"><Form.Label>Hora Hasta</Form.Label><Form.Control type="time" name="horaHasta" value={formData.horaHasta.substring(0, 5)} onChange={handleChange} required /></Form.Group></Col>
                            </Row>
                            <Row>
                                <Col md={6}><Form.Group className="mb-3"><Form.Label>Tipo de Promoción</Form.Label><Form.Select name="tipoPromocion" value={formData.tipoPromocion} onChange={handleChange} required>{tipoPromocionOptions.map(tipo => (<option key={tipo} value={tipo}>{tipo}</option>))}</Form.Select></Form.Group></Col>
                                {formData.tipoPromocion === 'PORCENTAJE' && (<Col md={6}><Form.Group className="mb-3"><Form.Label>Porcentaje de Descuento (%)</Form.Label><Form.Control type="number" name="porcentajeDescuento" value={formData.porcentajeDescuento} onChange={handleChange} min="1" max="100" required /></Form.Group></Col>)}
                                {(formData.tipoPromocion === 'CANTIDAD' || formData.tipoPromocion === 'COMBO') && (<Col md={6}><Form.Group className="mb-3"><Form.Label>Precio Promocional ($)</Form.Label><Form.Control type="number" name="precioPromocional" value={formData.precioPromocional} onChange={handleChange} step="0.01" min="0" required /></Form.Group></Col>)}
                            </Row>
                            <Form.Group className="mb-3"><Form.Label>Sucursales Aplicables</Form.Label><Form.Select multiple name="sucursalIds" value={formData.sucursalIds.map(String)} onChange={handleSucursalChange} required>{sucursales.map(s => <option key={s.id} value={s.id}>{s.nombre}</option>)}</Form.Select><Form.Text>Mantén presionado Ctrl (o Cmd en Mac) para seleccionar varias.</Form.Text></Form.Group>
                            <Card className="mt-4">
                                <Card.Header className="d-flex justify-content-between align-items-center">
                                    <h6>Artículos Incluidos</h6>
                                    <Button variant="outline-success" size="sm" onClick={handleAddDetalle}><FontAwesomeIcon icon={faPlusCircle} /> Añadir Artículo</Button>
                                </Card.Header>
                                <ListGroup variant="flush">
                                    {formData.detallesPromocion.length === 0 ? (<ListGroup.Item className="text-center text-muted">Añade al menos un artículo.</ListGroup.Item>) : (
                                        formData.detallesPromocion.map((detalle, index) => (
                                            <ListGroup.Item key={index}>
                                                <Row className="align-items-center">
                                                    <Col xs={12} md={7}><Form.Group><Form.Label className="d-none d-md-block">Artículo</Form.Label><Form.Select value={detalle.articuloId} onChange={(e) => handleDetalleChange(index, 'articuloId', e.target.value)} required><option value="">Selecciona un artículo</option>{articulos.map(art => <option key={art.id} value={art.id}>{art.denominacion}</option>)}</Form.Select></Form.Group></Col>
                                                    <Col xs={8} md={3}><Form.Group><Form.Label className="d-none d-md-block">Cantidad</Form.Label><Form.Control type="number" value={detalle.cantidad} onChange={(e) => handleDetalleChange(index, 'cantidad', e.target.value)} min="1" required /></Form.Group></Col>
                                                    <Col xs={4} md={2} className="d-flex justify-content-end align-items-end"><Button variant="danger" size="sm" onClick={() => handleRemoveDetalle(index)} className="mt-auto"><FontAwesomeIcon icon={faMinusCircle} /></Button></Col>
                                                </Row>
                                            </ListGroup.Item>
                                        ))
                                    )}
                                </ListGroup>
                            </Card>
                            <Form.Group className="mb-3 mt-3"><Form.Check type="switch" label="Promoción Activa" name="estadoActivo" checked={formData.estadoActivo} onChange={handleChange} /></Form.Group>
                        </>
                    )}
                </Modal.Body>
                <Modal.Footer>
                    <Button variant="secondary" onClick={handleClose} disabled={submitting}>Cancelar</Button>
                    <Button variant="primary" type="submit" disabled={submitting || loadingOptions}>
                        {submitting ? <Spinner as="span" animation="border" size="sm" /> : ''}
                        {promocionToEdit ? ' Actualizar' : ' Crear'}
                    </Button>
                </Modal.Footer>
            </Form>
        </Modal>
    );
};

export default PromocionForm;
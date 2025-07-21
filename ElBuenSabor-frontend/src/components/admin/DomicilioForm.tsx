import React, { useState, useEffect } from 'react';
import { Modal, Form, Button, Alert, Spinner, Row, Col } from 'react-bootstrap';
import { DomicilioService } from '../../services/domicilioService';
import { UbicacionService } from '../../services/ubicacionService';
import type { DomicilioResponse, DomicilioRequest, ProvinciaResponse, GeorefLocalidad } from '../../types/types';

const ubicacionService = new UbicacionService();

interface DomicilioFormProps {
    show: boolean;
    handleClose: () => void;
    onSave: (domicilio: DomicilioResponse) => void;
    domicilioToEdit?: DomicilioResponse | null;
}

const DomicilioForm: React.FC<DomicilioFormProps> = ({ show, handleClose, onSave, domicilioToEdit }) => {

    const [formData, setFormData] = useState<Omit<DomicilioRequest, 'provinciaId'>>({
        calle: '', numero: 0, cp: '', localidadNombre: '',
    });
    
    const [provincias, setProvincias] = useState<ProvinciaResponse[]>([]);
    const [localidades, setLocalidades] = useState<GeorefLocalidad[]>([]);
    const [selectedProvincia, setSelectedProvincia] = useState<ProvinciaResponse | null>(null);

    const [loadingOptions, setLoadingOptions] = useState(true);
    const [loadingLocalidades, setLoadingLocalidades] = useState(false);
    const [submitting, setSubmitting] = useState(false);
    const [error, setError] = useState<string | null>(null);

    useEffect(() => {
        if (show) {
            setLoadingOptions(true);
            setError(null);
            
            ubicacionService.getAllProvincias().then(fetchedProvincias => {
                setProvincias(fetchedProvincias);
                if (domicilioToEdit) {
                    const provinciaActual = fetchedProvincias.find(p => p.id === domicilioToEdit.localidad.provincia.id);
                    setFormData({
                        calle: domicilioToEdit.calle,
                        numero: domicilioToEdit.numero,
                        cp: domicilioToEdit.cp,
                        localidadNombre: domicilioToEdit.localidad.nombre,
                    });
                    setSelectedProvincia(provinciaActual || null);
                } else {
                    setFormData({ calle: '', numero: 0, cp: '', localidadNombre: '' });
                    setSelectedProvincia(null);
                }
            }).catch((err: any) => {
                setError(err.message || 'Error al cargar las provincias.');
            }).finally(() => {
                setLoadingOptions(false);
            });
        }
    }, [show, domicilioToEdit]);

    useEffect(() => {
        if (selectedProvincia) {
            setLoadingLocalidades(true);
            UbicacionService.getLocalidadesPorProvincia(selectedProvincia.nombre)
                .then(setLocalidades)
                .catch(err => setError(err.message))
                .finally(() => setLoadingLocalidades(false));
        } else {
            setLocalidades([]); 
        }
    }, [selectedProvincia]);

    const handleProvinciaChange = (e: React.ChangeEvent<HTMLSelectElement>) => {
        const provinciaId = Number(e.target.value);
        setSelectedProvincia(provincias.find(p => p.id === provinciaId) || null);
        setFormData(prev => ({ ...prev, localidadNombre: '' }));
    };

    const handleLocalidadChange = (e: React.ChangeEvent<HTMLSelectElement>) => {
        setFormData(prev => ({ ...prev, localidadNombre: e.target.value }));
    };
    
    const handleInputChange = (e: React.ChangeEvent<HTMLInputElement>) => {
        const { name, value } = e.target;
        setFormData(prev => ({ ...prev, [name]: name === 'numero' ? parseInt(value, 10) || 0 : value }));
    };

    const handleSubmit = async (e: React.FormEvent) => {
        e.preventDefault();
        if (!selectedProvincia || !formData.localidadNombre) {
            setError("Por favor, selecciona una provincia y una localidad válidas.");
            return;
        }
        setSubmitting(true);
        setError(null);
        
        const dataToSend: DomicilioRequest = {
            ...formData,
            provinciaId: selectedProvincia.id,
        };

        try {
            const savedDomicilio = domicilioToEdit
                ? await DomicilioService.update(domicilioToEdit.id, dataToSend)
                : await DomicilioService.create(dataToSend);
            onSave(savedDomicilio);
            handleClose();
        } catch (err: any) {
            setError(err.response?.data?.error || err.message || "Error al guardar el domicilio.");
        } finally {
            setSubmitting(false);
        }
    };

    return (
        <Modal show={show} onHide={handleClose} size="lg" backdrop="static">
            <Form onSubmit={handleSubmit}>
                <Modal.Header closeButton>
                    <Modal.Title>{domicilioToEdit ? 'Editar Domicilio' : 'Nuevo Domicilio'}</Modal.Title>
                </Modal.Header>
                <Modal.Body>
                    {loadingOptions ? <div className="text-center"><Spinner /></div> : error && <Alert variant="danger">{error}</Alert>}
                    {!loadingOptions && (
                        <>
                            <Form.Group className="mb-3"><Form.Label>Calle</Form.Label><Form.Control type="text" name="calle" value={formData.calle} onChange={handleInputChange} required /></Form.Group>
                            <Row>
                                <Col><Form.Group className="mb-3"><Form.Label>Número</Form.Label><Form.Control type="number" name="numero" value={formData.numero || ''} onChange={handleInputChange} required min="1" /></Form.Group></Col>
                                <Col><Form.Group className="mb-3"><Form.Label>Código Postal</Form.Label><Form.Control type="text" name="cp" value={formData.cp} onChange={handleInputChange} required /></Form.Group></Col>
                            </Row>
                            <Form.Group className="mb-3"><Form.Label>Provincia</Form.Label><Form.Select value={selectedProvincia?.id || ''} onChange={handleProvinciaChange} required><option value="">Seleccione una Provincia</option>{provincias.map(p => <option key={p.id} value={p.id}>{p.nombre}</option>)}</Form.Select></Form.Group>
                            <Form.Group className="mb-3"><Form.Label>Localidad</Form.Label><Form.Select name="localidadNombre" value={formData.localidadNombre} onChange={handleLocalidadChange} required disabled={!selectedProvincia || loadingLocalidades}>
                                <option value="">{loadingLocalidades ? 'Cargando...' : 'Seleccione una Localidad'}</option>
                                {localidades.map(l => <option key={l.id} value={l.nombre}>{l.nombre}</option>)}
                            </Form.Select></Form.Group>
                        </>
                    )}
                </Modal.Body>
                <Modal.Footer>
                    <Button variant="secondary" onClick={handleClose} disabled={submitting}>Cancelar</Button>
                    <Button variant="primary" type="submit" disabled={submitting || loadingOptions}>
                        {submitting ? <Spinner size="sm" /> : 'Guardar'}
                    </Button>
                </Modal.Footer>
            </Form>
        </Modal>
    );
};

export default DomicilioForm;
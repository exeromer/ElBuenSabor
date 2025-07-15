import React, { useState, useEffect } from 'react';
import { Modal, Form, Button, Alert, Spinner, Row, Col } from 'react-bootstrap';
import { DomicilioService } from '../../services/domicilioService';
import { UbicacionService } from '../../services/ubicacionService';
import type { DomicilioResponse, DomicilioRequest, PaisResponse, ProvinciaResponse, LocalidadResponse } from '../../types/types';

// Se instancian los servicios para usar sus métodos de instancia.
const ubicacionService = new UbicacionService();

interface DomicilioFormProps {
    show: boolean;
    handleClose: () => void;
    onSave: (domicilio: DomicilioResponse) => void;
    domicilioToEdit?: DomicilioResponse | null;
}

const DomicilioForm: React.FC<DomicilioFormProps> = ({ show, handleClose, onSave, domicilioToEdit }) => {

    const [formData, setFormData] = useState<DomicilioRequest>({
        calle: '', numero: 0, cp: '', localidadId: 0,
    });

    const [paises, setPaises] = useState<PaisResponse[]>([]);
    const [provincias, setProvincias] = useState<ProvinciaResponse[]>([]);
    const [localidades, setLocalidades] = useState<LocalidadResponse[]>([]);
    
    // Almacenamos todas las provincias y localidades para filtrar en el cliente y evitar múltiples llamadas a la API.
    const [allProvincias, setAllProvincias] = useState<ProvinciaResponse[]>([]);
    const [allLocalidades, setAllLocalidades] = useState<LocalidadResponse[]>([]);

    const [selectedPaisId, setSelectedPaisId] = useState<number | ''>('');
    const [selectedProvinciaId, setSelectedProvinciaId] = useState<number | ''>('');

    const [loadingOptions, setLoadingOptions] = useState(true);
    const [submitting, setSubmitting] = useState(false);
    const [error, setError] = useState<string | null>(null);

    // Carga todas las ubicaciones una sola vez cuando el modal se abre.
    useEffect(() => {
        if (show) {
            setLoadingOptions(true);
            setError(null);
            
            Promise.all([
                ubicacionService.getAllPaises(),
                ubicacionService.getAllProvincias(),
                ubicacionService.getAllLocalidades(),
            ]).then(([fetchedPaises, fetchedProvincias, fetchedLocalidades]) => {
                setPaises(fetchedPaises);
                setAllProvincias(fetchedProvincias);
                setAllLocalidades(fetchedLocalidades);

                if (domicilioToEdit) {
                    const paisId = domicilioToEdit.localidad.provincia.pais.id;
                    const provinciaId = domicilioToEdit.localidad.provincia.id;
                    
                    setFormData({
                        calle: domicilioToEdit.calle,
                        numero: domicilioToEdit.numero,
                        cp: domicilioToEdit.cp,
                        localidadId: domicilioToEdit.localidad.id,
                    });

                    setSelectedPaisId(paisId);
                    setProvincias(fetchedProvincias.filter(p => p.pais.id === paisId));
                    setSelectedProvinciaId(provinciaId);
                    setLocalidades(fetchedLocalidades.filter(l => l.provincia.id === provinciaId));
                } else {
                    setFormData({ calle: '', numero: 0, cp: '', localidadId: 0 });
                    setSelectedPaisId('');
                    setSelectedProvinciaId('');
                }
            }).catch((err) => {
                setError(err.message || 'Error al cargar opciones de localización.');
            }).finally(() => {
                setLoadingOptions(false);
            });
        }
    }, [show, domicilioToEdit]);

    // Maneja el cambio de País y filtra las Provincias.
    const handlePaisChange = (e: React.ChangeEvent<HTMLSelectElement>) => {
        const paisId = Number(e.target.value);
        setSelectedPaisId(paisId);
        setProvincias(allProvincias.filter(p => p.pais.id === paisId));
        setSelectedProvinciaId('');
        setLocalidades([]);
        setFormData(prev => ({ ...prev, localidadId: 0 }));
    };

    // Maneja el cambio de Provincia y filtra las Localidades.
    const handleProvinciaChange = (e: React.ChangeEvent<HTMLSelectElement>) => {
        const provinciaId = Number(e.target.value);
        setSelectedProvinciaId(provinciaId);
        setLocalidades(allLocalidades.filter(l => l.provincia.id === provinciaId));
        setFormData(prev => ({ ...prev, localidadId: 0 }));
    };

    const handleLocalidadChange = (e: React.ChangeEvent<HTMLSelectElement>) => {
        setFormData(prev => ({ ...prev, localidadId: Number(e.target.value) }));
    };
    
    const handleInputChange = (e: React.ChangeEvent<HTMLInputElement>) => {
        const { name, value } = e.target;
        setFormData(prev => ({ ...prev, [name]: name === 'numero' ? parseInt(value, 10) || 0 : value }));
    };

    const handleSubmit = async (e: React.FormEvent) => {
        e.preventDefault();
        if (formData.localidadId === 0) {
            setError("Por favor, selecciona una localidad válida.");
            return;
        }
        setSubmitting(true);
        setError(null);
        try {
            let savedDomicilio: DomicilioResponse;
            if (domicilioToEdit) {
                // CORRECCIÓN: Usamos el nombre de método correcto: 'update'
                savedDomicilio = await DomicilioService.update(domicilioToEdit.id, formData);
            } else {
                savedDomicilio = await DomicilioService.create(formData);
            }
            onSave(savedDomicilio);
            handleClose();
        } catch (err: any) {
            setError(err.message || "Error al guardar el domicilio.");
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
                            <Form.Group className="mb-3"><Form.Label>País</Form.Label><Form.Select value={selectedPaisId} onChange={handlePaisChange} required><option value="">Seleccione un País</option>{paises.map(p => <option key={p.id} value={p.id}>{p.nombre}</option>)}</Form.Select></Form.Group>
                            <Form.Group className="mb-3"><Form.Label>Provincia</Form.Label><Form.Select value={selectedProvinciaId} onChange={handleProvinciaChange} required disabled={!selectedPaisId}><option value="">Seleccione una Provincia</option>{provincias.map(p => <option key={p.id} value={p.id}>{p.nombre}</option>)}</Form.Select></Form.Group>
                            <Form.Group className="mb-3"><Form.Label>Localidad</Form.Label><Form.Select name="localidadId" value={formData.localidadId} onChange={handleLocalidadChange} required disabled={!selectedProvinciaId}><option value="">Seleccione una Localidad</option>{localidades.map(l => <option key={l.id} value={l.id}>{l.nombre}</option>)}</Form.Select></Form.Group>
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
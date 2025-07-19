import React, { useState, useEffect } from 'react';
import { Modal, Form, Button, Alert, Spinner, Row, Col } from 'react-bootstrap';
import { DomicilioService } from '../../services/domicilioService';
import { UbicacionService } from '../../services/ubicacionService';
import type { DomicilioResponse, DomicilioRequest, PaisResponse, ProvinciaResponse, GeorefLocalidad } from '../../types/types';

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

    const [paises, setPaises] = useState<PaisResponse[]>([]);
    const [provincias, setProvincias] = useState<ProvinciaResponse[]>([]);
    const [localidades, setLocalidades] = useState<GeorefLocalidad[]>([]);
    const [allProvincias, setAllProvincias] = useState<ProvinciaResponse[]>([]);
    const [selectedPaisId, setSelectedPaisId] = useState<number | ''>('');
    const [selectedProvinciaId, setSelectedProvinciaId] = useState<number | ''>('');
    const [loadingOptions, setLoadingOptions] = useState(true);
    const [loadingLocalidades, setLoadingLocalidades] = useState(false);
    const [submitting, setSubmitting] = useState(false);
    const [error, setError] = useState<string | null>(null);

    useEffect(() => {
        if (show) {
            setLoadingOptions(true);
            setError(null);
            
            Promise.all([
                ubicacionService.getAllPaises(),
                ubicacionService.getAllProvincias(),
            ]).then(([fetchedPaises, fetchedProvincias]) => {
                setPaises(fetchedPaises);
                setAllProvincias(fetchedProvincias);

                if (domicilioToEdit) {
                    const paisId = domicilioToEdit.localidad.provincia.pais.id;
                    const provinciaId = domicilioToEdit.localidad.provincia.id;
                    
                    setFormData({
                        calle: domicilioToEdit.calle,
                        numero: domicilioToEdit.numero,
                        cp: domicilioToEdit.cp,
                        localidadNombre: domicilioToEdit.localidad.nombre,
                    });
                    setSelectedPaisId(paisId);
                    setProvincias(fetchedProvincias.filter(p => p.pais.id === paisId));
                    setSelectedProvinciaId(provinciaId);
                } else {
                    setFormData({ calle: '', numero: 0, cp: '', localidadNombre: '' });
                    setSelectedPaisId('');
                    setProvincias([]);
                    setSelectedProvinciaId('');
                    setLocalidades([]);
                }
            }).catch((err: any) => {
                setError(err.message || 'Error al cargar opciones de localización.');
            }).finally(() => {
                setLoadingOptions(false);
            });
        }
    }, [show, domicilioToEdit]);

    useEffect(() => {
        if (selectedProvinciaId) {
            setLoadingLocalidades(true);
            setError(null);
            setLocalidades([]);

            const provinciaSeleccionada = allProvincias.find(p => p.id === selectedProvinciaId);
            if (provinciaSeleccionada) {
                UbicacionService.getLocalidadesPorProvincia(provinciaSeleccionada.nombre)
                    .then((data: GeorefLocalidad[]) => {
                        setLocalidades(data);
                        if (domicilioToEdit && domicilioToEdit.localidad.provincia.id === selectedProvinciaId) {
                            setFormData(prev => ({ ...prev, localidadNombre: domicilioToEdit.localidad.nombre }));
                        }
                    })
                    .catch((err: any) => setError("Error al cargar localidades: " + err.message))
                    .finally(() => setLoadingLocalidades(false));
            }
        } else {
            setLocalidades([]);
        }
    }, [selectedProvinciaId, allProvincias, domicilioToEdit]);

    const handlePaisChange = (e: React.ChangeEvent<HTMLSelectElement>) => {
        const paisId = Number(e.target.value);
        setSelectedPaisId(paisId);
        setProvincias(allProvincias.filter(p => p.pais.id === paisId));
        setSelectedProvinciaId('');
        setLocalidades([]);
        setFormData(prev => ({ ...prev, localidadNombre: '' }));
    };

    const handleProvinciaChange = (e: React.ChangeEvent<HTMLSelectElement>) => {
        const provinciaId = Number(e.target.value);
        setSelectedProvinciaId(provinciaId);
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
        if (!formData.localidadNombre || !selectedProvinciaId) {
            setError("Por favor, selecciona una provincia y localidad válidas.");
            return;
        }
        setSubmitting(true);
        setError(null);

        const dataToSend: DomicilioRequest = {
            ...formData,
            provinciaId: selectedProvinciaId,
        };

        try {
            let savedDomicilio: DomicilioResponse;
            if (domicilioToEdit) {
                savedDomicilio = await DomicilioService.update(domicilioToEdit.id, dataToSend);
            } else {
                savedDomicilio = await DomicilioService.create(dataToSend);
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
                            <Form.Group className="mb-3"><Form.Label>Localidad</Form.Label><Form.Select name="localidadNombre" value={formData.localidadNombre} onChange={handleLocalidadChange} required disabled={!selectedProvinciaId || loadingLocalidades}>
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

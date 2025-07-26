import React, { useState, useEffect, useCallback } from 'react';
import { Modal, Form, Button, Alert, Spinner, Row, Col, Card, ListGroup, } from 'react-bootstrap';
import { useAuth0 } from '@auth0/auth0-react';
import { ClienteUsuarioService } from '../../services/clienteUsuarioService';
import { ClienteService } from '../../services/ClienteService';
import DomicilioForm from './DomicilioForm';
import { DomicilioService } from '../../services/domicilioService';
import type { ClienteResponse, ClienteRequest, UsuarioResponse, DomicilioResponse } from '../../types/types';
import { FontAwesomeIcon } from '@fortawesome/react-fontawesome';
import { faMapMarkerAlt, faPlus, faEdit, faTrash } from '@fortawesome/free-solid-svg-icons';
import { format, parseISO} from 'date-fns';
import toast from 'react-hot-toast'; 

interface ClientFormProps {
    show: boolean;
    handleClose: () => void;
    onSave: () => void;
    clientToEdit?: ClienteResponse | null;
    isProfileMode?: boolean;
}

const ClientForm: React.FC<ClientFormProps> = ({ show, handleClose, onSave, clientToEdit, isProfileMode = false }) => {

    const { getAccessTokenSilently } = useAuth0();
    const [formData, setFormData] = useState<ClienteRequest>({
        nombre: '', apellido: '', telefono: '', email: '', fechaNacimiento: undefined,
        usuarioId: 0, domicilioIds: [], estadoActivo: true,
    });

    const [availableUsers, setAvailableUsers] = useState<UsuarioResponse[]>([]);
    const [selectedDomicilios, setSelectedDomicilios] = useState<DomicilioResponse[]>([]);
    const [loading, setLoading] = useState(false);
    const [submitting, setSubmitting] = useState(false);
    const [error, setError] = useState<string | null>(null);
    const [showDomicilioForm, setShowDomicilioForm] = useState(false);
    const [editingDomicilio, setEditingDomicilio] = useState<DomicilioResponse | null>(null);


    const clienteUsuarioService = new ClienteUsuarioService();

    const loadInitialData = useCallback(async () => {
        if (!isProfileMode) {
            setLoading(true);
            try {
                const token = await getAccessTokenSilently();
                const users = await clienteUsuarioService.getAllUsuarios(token);
                setAvailableUsers(users);
            } catch (err: any) {
                setError(err.message || "Error al cargar usuarios.");
            } finally {
                setLoading(false);
            }
        }
    }, [isProfileMode, getAccessTokenSilently]);

    useEffect(() => {
        if (show) {
            setError(null);
            loadInitialData();
            if (clientToEdit) {
                setFormData({
                    nombre: clientToEdit.nombre,
                    apellido: clientToEdit.apellido,
                    telefono: clientToEdit.telefono || '',
                    email: clientToEdit.email,
                    fechaNacimiento: clientToEdit.fechaNacimiento ? format(parseISO(clientToEdit.fechaNacimiento), 'yyyy-MM-dd') : undefined,
                    usuarioId: clientToEdit.usuarioId,
                    domicilioIds: clientToEdit.domicilios.map(d => d.id),
                    estadoActivo: clientToEdit.estadoActivo,
                });
                setSelectedDomicilios(clientToEdit.domicilios);
            } else {
                setFormData({ nombre: '', apellido: '', telefono: '', email: '', fechaNacimiento: undefined, usuarioId: 0, domicilioIds: [], estadoActivo: true });
                setSelectedDomicilios([]);
            }
        }
    }, [clientToEdit, show, loadInitialData]);

    const handleChange = (e: React.ChangeEvent<HTMLInputElement | HTMLSelectElement | HTMLTextAreaElement>) => {
        const { name, value } = e.target;
        setFormData(prev => ({ ...prev, [name]: value }));
    };

    const handleSubmit = async (e: React.FormEvent) => {
        e.preventDefault();
        setSubmitting(true);
        setError(null);

        try {
            const token = await getAccessTokenSilently();
            if (isProfileMode) {
                await ClienteService.updateMiPerfil(formData);
            } else if (clientToEdit) {
                await clienteUsuarioService.updateCliente(clientToEdit.id, formData, token);
            } else {
                await clienteUsuarioService.createCliente(formData, token);
            }
            onSave();
            handleClose();
        } catch (err: any) {
            setError(err.message || 'Ocurrió un error al guardar.');
        } finally {
            setSubmitting(false);
        }
    };

    // Función para guardar (crear o actualizar) un domicilio
    const handleSaveDomicilio = async (domicilio: DomicilioResponse) => {
        // Lógica para actualizar la lista de domicilios en el estado del formulario
        const isNew = !selectedDomicilios.some(d => d.id === domicilio.id);
        let updatedDomicilios;
        if (isNew) {
            updatedDomicilios = [...selectedDomicilios, domicilio];
        } else {
            updatedDomicilios = selectedDomicilios.map(d => d.id === domicilio.id ? domicilio : d);
        }
        setSelectedDomicilios(updatedDomicilios);
        setFormData(prev => ({ ...prev, domicilioIds: updatedDomicilios.map(d => d.id) }));
        setShowDomicilioForm(false);
    };

    // Función para eliminar un domicilio
    const handleDeleteDomicilio = async (domicilioId: number) => {
        if (window.confirm("¿Estás seguro de que quieres eliminar este domicilio?")) {
            try {
                await DomicilioService.delete(domicilioId);
                const updatedDomicilios = selectedDomicilios.filter(d => d.id !== domicilioId);
                setSelectedDomicilios(updatedDomicilios);
                setFormData(prev => ({ ...prev, domicilioIds: updatedDomicilios.map(d => d.id) }));
                toast.success("Domicilio eliminado con éxito.");
            } catch (err: any) {
                toast.error(err.message || "Error al eliminar el domicilio.");
            }
        }
    };

    return (
        <>
            <Modal show={show} onHide={handleClose} size="lg" backdrop="static">
                <Form onSubmit={handleSubmit}>
                    <Modal.Header closeButton>
                        <Modal.Title>{clientToEdit ? 'Editar Cliente' : 'Nuevo Cliente'}</Modal.Title>
                    </Modal.Header>
                    <Modal.Body>
                        {loading ? <div className="text-center"><Spinner animation="border" /></div> : error && <Alert variant="danger">{error}</Alert>}

                        {!loading && (
                            <>
                                <Row>
                                    <Col md={6}><Form.Group className="mb-3"><Form.Label>Nombre</Form.Label><Form.Control type="text" name="nombre" value={formData.nombre} onChange={handleChange} required /></Form.Group></Col>
                                    <Col md={6}><Form.Group className="mb-3"><Form.Label>Apellido</Form.Label><Form.Control type="text" name="apellido" value={formData.apellido} onChange={handleChange} required /></Form.Group></Col>
                                </Row>
                                <Row>
                                    <Col md={6}><Form.Group className="mb-3"><Form.Label>Email</Form.Label><Form.Control type="email" name="email" value={formData.email} onChange={handleChange} required /></Form.Group></Col>
                                    <Col md={6}><Form.Group className="mb-3"><Form.Label>Teléfono</Form.Label><Form.Control type="text" name="telefono" value={formData.telefono} onChange={handleChange} /></Form.Group></Col>
                                </Row>
                                <Row>
                                    <Col md={6}>
                                        <Form.Group className="mb-3">
                                            <Form.Label>Fecha de Nacimiento</Form.Label>
                                            <Form.Control type="date" name="fechaNacimiento" value={formData.fechaNacimiento || ''} onChange={handleChange} />
                                        </Form.Group>
                                    </Col>
                                    {!isProfileMode && (
                                        <Col md={6}>
                                            <Form.Group className="mb-3">
                                                <Form.Label>Usuario Asociado</Form.Label>
                                                <Form.Control
                                                    type="text"
                                                    value={availableUsers.find(u => u.id === formData.usuarioId)?.username || 'No disponible'}
                                                    readOnly
                                                    disabled
                                                />
                                            </Form.Group>
                                        </Col>
                                    )}
                                </Row>
                                <Card className="mt-3">
                                    <Card.Header className="d-flex justify-content-between align-items-center">
                                        Domicilios Registrados
                                        <Button variant="success" size="sm" onClick={() => { setEditingDomicilio(null); setShowDomicilioForm(true); }}>
                                            <FontAwesomeIcon icon={faPlus} /> Agregar
                                        </Button>
                                    </Card.Header>
                                    <ListGroup variant="flush">
                                        {selectedDomicilios.length > 0 ? selectedDomicilios.map(dom => (
                                            <ListGroup.Item key={dom.id} className="d-flex justify-content-between align-items-center">
                                                <span><FontAwesomeIcon icon={faMapMarkerAlt} className="me-2 text-info" />{`${dom.calle} ${dom.numero}, ${dom.localidad.nombre}`}</span>
                                                <div>
                                                    <Button variant="info" size="sm" className="me-2" onClick={() => { setEditingDomicilio(dom); setShowDomicilioForm(true); }}>
                                                        <FontAwesomeIcon icon={faEdit} />
                                                    </Button>
                                                    <Button variant="danger" size="sm" onClick={() => handleDeleteDomicilio(dom.id)}>
                                                        <FontAwesomeIcon icon={faTrash} />
                                                    </Button>
                                                </div>
                                            </ListGroup.Item>
                                        )) : <ListGroup.Item className="text-muted">No hay domicilios registrados.</ListGroup.Item>}
                                    </ListGroup>
                                </Card>
                            </>
                        )}
                    </Modal.Body>
                    <Modal.Footer>
                        <Button variant="secondary" onClick={handleClose}>Cancelar</Button>
                        <Button variant="primary" type="submit" disabled={submitting || loading}>
                            {submitting ? <Spinner as="span" size="sm" /> : 'Guardar Cambios'}
                        </Button>
                    </Modal.Footer>
                </Form>
            </Modal>
            <DomicilioForm
                show={showDomicilioForm}
                handleClose={() => setShowDomicilioForm(false)}
                onSave={handleSaveDomicilio}
                domicilioToEdit={editingDomicilio}
            />
        </>
    );
};

export default ClientForm;
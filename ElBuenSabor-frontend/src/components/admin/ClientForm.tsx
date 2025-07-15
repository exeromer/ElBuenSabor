import React, { useState, useEffect, useCallback } from 'react';
import { Modal, Form, Button, Alert, Spinner, Row, Col, Card, ListGroup } from 'react-bootstrap';
import { useAuth0 } from '@auth0/auth0-react';
import { ClienteUsuarioService } from '../../services/clienteUsuarioService';
import { ClienteService } from '../../services/ClienteService'; // ¡Importante añadir este servicio!
import type { ClienteResponse, ClienteRequest, UsuarioResponse, DomicilioResponse } from '../../types/types';
import { FontAwesomeIcon } from '@fortawesome/react-fontawesome';
import { faPlusCircle, faTrash, faMapMarkerAlt, faEdit } from '@fortawesome/free-solid-svg-icons';
import { format, parseISO } from 'date-fns';
import DomicilioForm from './DomicilioForm';

interface ClientFormProps {
    show: boolean;
    handleClose: () => void;
    onSave: () => void; // Espera una función sin argumentos.
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
    const [showDomicilioModal, setShowDomicilioModal] = useState(false);
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

    useEffect(() => {
        setFormData(prev => ({ ...prev, domicilioIds: selectedDomicilios.map(d => d.id) }));
    }, [selectedDomicilios]);

    const handleChange = (e: React.ChangeEvent<HTMLInputElement | HTMLSelectElement | HTMLTextAreaElement>) => {
        const { name, value, type } = e.target;
        const checked = (e.target as HTMLInputElement).checked;
        setFormData(prev => ({ ...prev, [name]: type === 'checkbox' ? checked : value }));
    };
    
    const handleSaveDomicilio = (savedDomicilio: DomicilioResponse) => {
        setSelectedDomicilios(prev => {
            const exists = prev.some(d => d.id === savedDomicilio.id);
            return exists ? prev.map(d => d.id === savedDomicilio.id ? savedDomicilio : d) : [...prev, savedDomicilio];
        });
        setShowDomicilioModal(false);
    };

    const handleRemoveDomicilio = (domicilioId: number) => {
        if (window.confirm("¿Seguro que quieres quitar este domicilio?")) {
            setSelectedDomicilios(prev => prev.filter(d => d.id !== domicilioId));
        }
    };
    
    const handleSubmit = async (e: React.FormEvent) => {
        e.preventDefault();
        setSubmitting(true);
        setError(null);
        if (selectedDomicilios.length === 0) {
            setError("Debes registrar al menos un domicilio.");
            setSubmitting(false);
            return;
        }
        try {
            const token = await getAccessTokenSilently();
            // CORRECCIÓN: Lógica para decidir qué servicio y método usar.
            if (isProfileMode) {
                await ClienteService.updateMiPerfil(formData);
            } else if (clientToEdit) {
                await clienteUsuarioService.updateCliente(clientToEdit.id, formData, token);
            } else {
                await clienteUsuarioService.createCliente(formData, token);
            }
            onSave(); // Llama a la función del padre sin argumentos.
            handleClose();
        } catch (err: any) {
            setError(err.message || 'Ocurrió un error al guardar.');
        } finally {
            setSubmitting(false);
        }
    };

    // ... (El JSX del return no cambia)
    return (
        <>
            <Modal show={show} onHide={handleClose} size="lg" backdrop="static">
                <Form onSubmit={handleSubmit}>
                    <Modal.Header closeButton>
                        <Modal.Title>{clientToEdit ? 'Editar Perfil' : 'Nuevo Cliente'}</Modal.Title>
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
                                    <Col md={6}><Form.Group className="mb-3"><Form.Label>Email</Form.Label><Form.Control type="email" name="email" value={formData.email} onChange={handleChange} required readOnly={isProfileMode || !!clientToEdit} /></Form.Group></Col>
                                    <Col md={6}><Form.Group className="mb-3"><Form.Label>Teléfono</Form.Label><Form.Control type="text" name="telefono" value={formData.telefono} onChange={handleChange} /></Form.Group></Col>
                                </Row>
                                <Row>
                                    <Col md={6}><Form.Group className="mb-3"><Form.Label>Fecha de Nacimiento</Form.Label><Form.Control type="date" name="fechaNacimiento" value={formData.fechaNacimiento || ''} onChange={handleChange} /></Form.Group></Col>
                                    {!isProfileMode && (
                                        <Col md={6}><Form.Group className="mb-3"><Form.Label>Usuario Asociado</Form.Label><Form.Select name="usuarioId" value={formData.usuarioId} onChange={handleChange} required disabled={!!clientToEdit}>
                                            <option value="">Selecciona un Usuario</option>
                                            {availableUsers.map(user => <option key={user.id} value={user.id}>{user.username}</option>)}
                                        </Form.Select></Form.Group></Col>
                                    )}
                                </Row>
                                <Card className="mt-3">
                                    <Card.Header className="d-flex justify-content-between align-items-center">
                                        Domicilios
                                        <Button variant="outline-success" size="sm" onClick={() => { setEditingDomicilio(null); setShowDomicilioModal(true); }}><FontAwesomeIcon icon={faPlusCircle} /> Añadir</Button>
                                    </Card.Header>
                                    <ListGroup variant="flush">
                                        {selectedDomicilios.length > 0 ? selectedDomicilios.map(dom => (
                                            <ListGroup.Item key={dom.id} className="d-flex justify-content-between align-items-center">
                                                <span><FontAwesomeIcon icon={faMapMarkerAlt} className="me-2 text-info" />{`${dom.calle} ${dom.numero}, ${dom.localidad.nombre}`}</span>
                                                <div>
                                                    <Button variant="outline-primary" size="sm" onClick={() => { setEditingDomicilio(dom); setShowDomicilioModal(true); }}><FontAwesomeIcon icon={faEdit} /></Button>
                                                    <Button variant="outline-danger" size="sm" className="ms-2" onClick={() => handleRemoveDomicilio(dom.id)}><FontAwesomeIcon icon={faTrash} /></Button>
                                                </div>
                                            </ListGroup.Item>
                                        )) : <ListGroup.Item className="text-muted">No hay domicilios. Por favor, añade uno.</ListGroup.Item>}
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
                show={showDomicilioModal}
                handleClose={() => setShowDomicilioModal(false)}
                onSave={handleSaveDomicilio}
                domicilioToEdit={editingDomicilio}
            />
        </>
    );
};
export default ClientForm;
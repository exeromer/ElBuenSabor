import React, { useState, useEffect } from 'react';
import { Modal, Form, Button, Alert, Spinner, Row, Col } from 'react-bootstrap';
import { EmpleadoService } from '../../services/EmpleadoService';
import { UsuarioService } from '../../services/usuarioService';
import type { EmpleadoRequest, EmpleadoResponse, UsuarioResponse } from '../../types/types';

interface EmpleadoFormProps {
    show: boolean;
    handleClose: () => void;
    onSave: () => void;
    empleadoToEdit?: EmpleadoResponse | null;
    isProfileMode?: boolean;
}

const rolesEmpleado: ("CAJERO" | "COCINA" | "DELIVERY")[] = ["CAJERO", "COCINA", "DELIVERY"];

const EmpleadoForm: React.FC<EmpleadoFormProps> = ({ show, handleClose, onSave, empleadoToEdit, isProfileMode = false }) => {
    const [formData, setFormData] = useState<Partial<EmpleadoRequest>>({});
    const [availableUsers, setAvailableUsers] = useState<UsuarioResponse[]>([]);
    const [loading, setLoading] = useState(false);
    const [submitting, setSubmitting] = useState(false);
    const [error, setError] = useState<string | null>(null);

    useEffect(() => {
        const loadAdminData = async () => {
            if (!isProfileMode) {
                setLoading(true);
                try {
                    const users = await UsuarioService.getAll();
                    setAvailableUsers(users.filter(u => u.rol === 'EMPLEADO'));
                } catch (err) {
                    setError("Error al cargar usuarios disponibles. Asegúrate de tener rol de Administrador.");
                } finally {
                    setLoading(false);
                }
            }
        };

        if (show) {
            setError(null);
            if (empleadoToEdit) {
                setFormData({
                    nombre: empleadoToEdit.nombre,
                    apellido: empleadoToEdit.apellido,
                    telefono: empleadoToEdit.telefono,
                    rolEmpleado: empleadoToEdit.rolEmpleado,
                    usuarioId: empleadoToEdit.usuarioId,
                    estadoActivo: empleadoToEdit.estadoActivo,
                });
            } else {
                setFormData({ nombre: '', apellido: '', telefono: '', rolEmpleado: 'COCINA', usuarioId: 0, estadoActivo: true });
            }
            loadAdminData();
        }
    }, [show, empleadoToEdit, isProfileMode]);

    const handleChange = (e: React.ChangeEvent<any>) => {
        const { name, value, type, checked } = e.target;
        setFormData(prev => ({ ...prev, [name]: type === 'checkbox' ? checked : value }));
    };

    const handleSubmit = async (e: React.FormEvent) => {
        e.preventDefault();
        setSubmitting(true);
        setError(null);
        try {
            if (empleadoToEdit) {
                let dataToSend: EmpleadoRequest;

                if (isProfileMode) {
                    dataToSend = {
                        nombre: formData.nombre || empleadoToEdit.nombre,
                        apellido: formData.apellido || empleadoToEdit.apellido,
                        telefono: formData.telefono || empleadoToEdit.telefono,
                        rolEmpleado: empleadoToEdit.rolEmpleado,
                        usuarioId: empleadoToEdit.usuarioId,     
                        estadoActivo: empleadoToEdit.estadoActivo,
                    };
                    await EmpleadoService.updateMiPerfil(dataToSend);
                } else {
                    dataToSend = formData as EmpleadoRequest;
                    await EmpleadoService.update(empleadoToEdit.id, dataToSend);
                }
            } else {
                await EmpleadoService.create(formData as EmpleadoRequest);
            }
            onSave();
            handleClose();
        } catch (err: any) {
            setError(err.message || 'Error al guardar el empleado.');
        } finally {
            setSubmitting(false);
        }
    };

    return (
        <Modal show={show} onHide={handleClose} backdrop="static" centered>
            <Form onSubmit={handleSubmit}>
                <Modal.Header closeButton>
                    <Modal.Title>{isProfileMode ? 'Editar Mi Perfil' : (empleadoToEdit ? 'Editar Empleado' : 'Nuevo Empleado')}</Modal.Title>
                </Modal.Header>
                <Modal.Body>
                    {loading && <div className="text-center"><Spinner /></div>}
                    {error && <Alert variant="danger">{error}</Alert>}
                    {!loading && (
                        <>
                            <Row>
                                <Col><Form.Group className="mb-3"><Form.Label>Nombre</Form.Label><Form.Control type="text" name="nombre" value={formData.nombre || ''} onChange={handleChange} required /></Form.Group></Col>
                                <Col><Form.Group className="mb-3"><Form.Label>Apellido</Form.Label><Form.Control type="text" name="apellido" value={formData.apellido || ''} onChange={handleChange} required /></Form.Group></Col>
                            </Row>
                            <Form.Group className="mb-3"><Form.Label>Teléfono</Form.Label><Form.Control type="text" name="telefono" value={formData.telefono || ''} onChange={handleChange} required /></Form.Group>
                            
                            {!isProfileMode && (
                                <>
                                    <hr />
                                    <h5 className="text-muted">Configuración de Administrador</h5>
                                    <Form.Group className="mb-3"><Form.Label>Rol de Empleado</Form.Label><Form.Select name="rolEmpleado" value={formData.rolEmpleado} onChange={handleChange} required>
                                        {rolesEmpleado.map(rol => <option key={rol} value={rol}>{rol}</option>)}
                                    </Form.Select></Form.Group>
                                    <Form.Group className="mb-3"><Form.Label>Usuario Asociado</Form.Label><Form.Select name="usuarioId" value={formData.usuarioId} onChange={handleChange} required disabled={!!empleadoToEdit}>
                                        <option value="">Seleccione un Usuario</option>
                                        {availableUsers.map(user => <option key={user.id} value={user.id}>{user.username}</option>)}
                                    </Form.Select></Form.Group>
                                    <Form.Check type="switch" label="Activo" name="estadoActivo" checked={formData.estadoActivo} onChange={handleChange} />
                                </>
                            )}
                        </>
                    )}
                </Modal.Body>
                <Modal.Footer>
                    <Button variant="secondary" onClick={handleClose} disabled={submitting}>Cancelar</Button>
                    <Button variant="primary" type="submit" disabled={submitting || loading}>
                        {submitting && <Spinner as="span" size="sm" />}
                        Guardar Cambios
                    </Button>
                </Modal.Footer>
            </Form>
        </Modal>
    );
};

export default EmpleadoForm;
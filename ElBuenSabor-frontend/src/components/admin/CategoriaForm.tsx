import React, { useState, useEffect } from 'react';
import { Modal, Form, Button, Alert, Spinner } from 'react-bootstrap';
import { CategoriaService } from '../../services/categoriaService';
import type { CategoriaRequest, CategoriaResponse } from '../../types/types';
import { useSucursal } from '../../context/SucursalContext';
import { SucursalService } from '../../services/sucursalService';

interface CategoriaFormProps {
    show: boolean;
    handleClose: () => void;
    onSave: () => void;
    categoriaToEdit?: CategoriaResponse | null;
}

const initialFormData: CategoriaRequest = {
    denominacion: '',
    estadoActivo: true,
};

const CategoriaForm: React.FC<CategoriaFormProps> = ({ show, handleClose, onSave, categoriaToEdit }) => {
    const { selectedSucursal } = useSucursal();
    const [formData, setFormData] = useState<CategoriaRequest>(initialFormData);
    const [submitting, setSubmitting] = useState(false);
    const [error, setError] = useState<string | null>(null);

    useEffect(() => {
        if (show) {
            if (categoriaToEdit) {
                setFormData({
                    denominacion: categoriaToEdit.denominacion,
                    estadoActivo: categoriaToEdit.estadoActivo,
                });
            } else {
                setFormData(initialFormData);
            }
            setError(null);
        }
    }, [show, categoriaToEdit]);

    const handleChange = (e: React.ChangeEvent<any>) => {
        const { name, value, type, checked } = e.target;
        setFormData(prev => ({
            ...prev,
            [name]: type === 'checkbox' ? checked : value,
        }));
    };

    const handleSubmit = async (e: React.FormEvent) => {
        e.preventDefault();
        setSubmitting(true);
        setError(null);
        try {
            if (categoriaToEdit) {
                await CategoriaService.update(categoriaToEdit.id, formData);
            } else {
                // 1. Creamos la categoría
                const nuevaCategoria = await CategoriaService.create(formData);

                // 2. Si se creó bien y hay una sucursal, la asociamos automáticamente
                if (nuevaCategoria && selectedSucursal) {
                    await SucursalService.asociarCategoria(selectedSucursal.id, nuevaCategoria.id);
                }
            }
            alert(`Categoría ${categoriaToEdit ? 'actualizada' : 'creada'} y asociada con éxito.`);
            onSave();
            handleClose();
        } catch (err: any) {
            setError(err.message || 'Error al guardar la categoría.');
        } finally {
            setSubmitting(false);
        }
    };

    return (
        <Modal show={show} onHide={handleClose} backdrop="static" centered>
            <Modal.Header closeButton>
                <Modal.Title>{categoriaToEdit ? 'Editar Categoría' : 'Nueva Categoría'}</Modal.Title>
            </Modal.Header>
            <Form onSubmit={handleSubmit}>
                <Modal.Body>
                    {error && <Alert variant="danger">{error}</Alert>}
                    <Form.Group className="mb-3">
                        <Form.Label>Denominación</Form.Label>
                        <Form.Control
                            type="text"
                            name="denominacion"
                            value={formData.denominacion}
                            onChange={handleChange}
                            required
                            autoFocus
                        />
                    </Form.Group>
                    <Form.Group className="mb-3">
                        <Form.Check
                            type="switch"
                            id="estado-activo-switch"
                            label="Activa"
                            name="estadoActivo"
                            checked={formData.estadoActivo}
                            onChange={handleChange}
                        />
                    </Form.Group>
                </Modal.Body>
                <Modal.Footer>
                    <Button variant="secondary" onClick={handleClose} disabled={submitting}>Cancelar</Button>
                    <Button variant="primary" type="submit" disabled={submitting}>
                        {submitting && <Spinner as="span" size="sm" />}
                        {categoriaToEdit ? 'Actualizar' : 'Crear'}
                    </Button>
                </Modal.Footer>
            </Form>
        </Modal>
    );
};

export default CategoriaForm;
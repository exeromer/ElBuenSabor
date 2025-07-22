import React, { useState, useEffect, useCallback } from 'react';
import { Modal, Form, Button, Alert, Spinner, Row, Col } from 'react-bootstrap';
import { SucursalService } from '../../services/sucursalService';
import { EmpresaService } from '../../services/EmpresaService';
import { CategoriaService } from '../../services/categoriaService';
import { UbicacionService } from '../../services/ubicacionService';
import type { SucursalRequest, SucursalResponse, EmpresaResponse, CategoriaResponse, ProvinciaResponse, GeorefLocalidad } from '../../types/types';

interface SucursalFormProps {
    show: boolean;
    handleClose: () => void;
    onSave: () => void;
    sucursalToEdit?: SucursalResponse | null;
}

const ubicacionService = new UbicacionService();

const SucursalForm: React.FC<SucursalFormProps> = ({ show, handleClose, onSave, sucursalToEdit }) => {
    // Estado para el formulario principal
    const [formData, setFormData] = useState<Omit<SucursalRequest, 'domicilio'>>({
        nombre: '',
        horarioApertura: '00:00',
        horarioCierre: '00:00',
        empresaId: 0,
        categoriaIds: [],
        promocionIds: [],
        estadoActivo: true,
    });

    // Estado separado para el domicilio
    const [domicilioData, setDomicilioData] = useState({
        calle: '',
        numero: 0,
        cp: '',
        localidadNombre: '',
    });

    // Estados para poblar los selectores
    const [empresas, setEmpresas] = useState<EmpresaResponse[]>([]);
    const [categorias, setCategorias] = useState<CategoriaResponse[]>([]);
    const [provincias, setProvincias] = useState<ProvinciaResponse[]>([]);
    const [localidades, setLocalidades] = useState<GeorefLocalidad[]>([]);
    const [selectedProvinciaId, setSelectedProvinciaId] = useState<number | ''>('');

    // Estados de UI
    const [loadingOptions, setLoadingOptions] = useState(true);
    const [loadingLocalidades, setLoadingLocalidades] = useState(false);
    const [submitting, setSubmitting] = useState(false);
    const [error, setError] = useState<string | null>(null);

    // Carga inicial de datos para los selectores (empresas, categorías, provincias)
    const loadInitialOptions = useCallback(async () => {
        try {
            const [fetchedEmpresas, fetchedCategorias, fetchedProvincias] = await Promise.all([
                EmpresaService.getAll(),
                CategoriaService.getAll(),
                ubicacionService.getAllProvincias()
            ]);
            setEmpresas(fetchedEmpresas);
            setCategorias(fetchedCategorias);
            setProvincias(fetchedProvincias);
        } catch (err) {
            setError('Error al cargar las opciones del formulario.');
        }
    }, []);

    // Efecto para precargar o resetear el formulario cuando se abre el modal
    useEffect(() => {
        if (show) {
            setLoadingOptions(true);
            setError(null);
            loadInitialOptions().then(() => {
                if (sucursalToEdit) {
                    // Modo Edición: precargamos los datos
                    setFormData({
                        nombre: sucursalToEdit.nombre,
                        horarioApertura: sucursalToEdit.horarioApertura.substring(0, 5),
                        horarioCierre: sucursalToEdit.horarioCierre.substring(0, 5),
                        empresaId: sucursalToEdit.empresa.id,
                        categoriaIds: sucursalToEdit.categorias.map(c => c.id),
                        promocionIds: sucursalToEdit.promociones.map(p => p.id),
                        estadoActivo: sucursalToEdit.estadoActivo,
                    });
                    setDomicilioData({
                        calle: sucursalToEdit.domicilio.calle,
                        numero: sucursalToEdit.domicilio.numero,
                        cp: sucursalToEdit.domicilio.cp,
                        localidadNombre: sucursalToEdit.domicilio.localidad.nombre,
                    });
                    setSelectedProvinciaId(sucursalToEdit.domicilio.localidad.provincia.id);
                } else {
                    // Modo Creación: reseteamos a valores iniciales
                    setFormData({ nombre: '', horarioApertura: '20:00', horarioCierre: '00:00', empresaId: 0, categoriaIds: [], promocionIds: [], estadoActivo: true });
                    setDomicilioData({ calle: '', numero: 0, cp: '', localidadNombre: '' });
                    setSelectedProvinciaId('');
                }
                setLoadingOptions(false);
            });
        }
    }, [show, sucursalToEdit, loadInitialOptions]);

    // Efecto para cargar las localidades cuando cambia la provincia seleccionada
    useEffect(() => {
        if (selectedProvinciaId) {
            setLoadingLocalidades(true);
            const provName = provincias.find(p => p.id === selectedProvinciaId)?.nombre || '';
            UbicacionService.getLocalidadesPorProvincia(provName)
                .then(setLocalidades)
                .catch(err => setError(err.message))
                .finally(() => setLoadingLocalidades(false));
        } else {
            setLocalidades([]);
        }
    }, [selectedProvinciaId, provincias]);

    // Manejadores de cambios en los inputs
    const handleChange = (e: React.ChangeEvent<any>) => {
        const { name, value } = e.target;
        setFormData(prev => ({ ...prev, [name]: name === 'empresaId' ? Number(value) : value }));
    };

    const handleDomicilioChange = (e: React.ChangeEvent<any>) => {
        const { name, value } = e.target;
        setDomicilioData(prev => ({ ...prev, [name]: name === 'numero' ? parseInt(value, 10) || 0 : value }));
    };

    const handleCategoriasChange = (e: React.ChangeEvent<any>) => {
        const selectedIds = Array.from(e.target.selectedOptions, (option: any) => Number(option.value));
        setFormData(prev => ({ ...prev, categoriaIds: selectedIds }));
    };

    // Lógica de envío del formulario
    const handleSubmit = async (e: React.FormEvent) => {
        e.preventDefault();
        if (!selectedProvinciaId || !domicilioData.localidadNombre) {
            setError("Por favor, selecciona una provincia y localidad válidas.");
            return;
        }
        setSubmitting(true);
        setError(null);

        const fullRequestData: SucursalRequest = {
            ...formData,
            // **AQUÍ ESTÁ LA CORRECCIÓN**
            // Ya no añadimos los segundos, el valor de formData es correcto.
            horarioApertura: formData.horarioApertura,
            horarioCierre: formData.horarioCierre,
            domicilio: {
                ...domicilioData,
                provinciaId: selectedProvinciaId
            }
        };

        try {
            if (sucursalToEdit) {
                await SucursalService.update(sucursalToEdit.id, fullRequestData);
            } else {
                await SucursalService.create(fullRequestData);
            }
            onSave();
            handleClose();
        } catch (err: any) {
            setError(err.response?.data?.message || err.message || "Error al guardar la sucursal.");
        } finally {
            setSubmitting(false);
        }
    };

    return (
        <Modal show={show} onHide={handleClose} size="lg" backdrop="static" centered>
            <Form onSubmit={handleSubmit}>
                <Modal.Header closeButton>
                    <Modal.Title>{sucursalToEdit ? 'Editar Sucursal' : 'Nueva Sucursal'}</Modal.Title>
                </Modal.Header>
                <Modal.Body>
                    {loadingOptions ? <div className="text-center"><Spinner /></div> : error && <Alert variant="danger">{error}</Alert>}
                    {!loadingOptions && (
                        <>
                            <Form.Group className="mb-3"><Form.Label>Nombre</Form.Label><Form.Control type="text" name="nombre" value={formData.nombre} onChange={handleChange} required /></Form.Group>
                            <Row>
                                <Col><Form.Group className="mb-3"><Form.Label>Horario Apertura</Form.Label><Form.Control type="time" name="horarioApertura" value={formData.horarioApertura} onChange={handleChange} required /></Form.Group></Col>
                                <Col><Form.Group className="mb-3"><Form.Label>Horario Cierre</Form.Label><Form.Control type="time" name="horarioCierre" value={formData.horarioCierre} onChange={handleChange} required /></Form.Group></Col>
                            </Row>
                            <Form.Group className="mb-3"><Form.Label>Empresa</Form.Label><Form.Select name="empresaId" value={formData.empresaId} onChange={handleChange} required><option value="">Seleccione una Empresa</option>{empresas.map(e => <option key={e.id} value={e.id}>{e.nombre}</option>)}</Form.Select></Form.Group>

                            <hr />
                            <h5>Domicilio</h5>
                            <Form.Group className="mb-3"><Form.Label>Calle</Form.Label><Form.Control type="text" name="calle" value={domicilioData.calle} onChange={handleDomicilioChange} required /></Form.Group>
                            <Row>
                                <Col><Form.Group className="mb-3"><Form.Label>Número</Form.Label><Form.Control type="number" name="numero" value={domicilioData.numero || ''} onChange={handleDomicilioChange} required min="1" /></Form.Group></Col>
                                <Col><Form.Group className="mb-3"><Form.Label>Código Postal</Form.Label><Form.Control type="text" name="cp" value={domicilioData.cp} onChange={handleDomicilioChange} required /></Form.Group></Col>
                            </Row>
                            <Row>
                                <Col><Form.Group className="mb-3"><Form.Label>Provincia</Form.Label><Form.Select value={selectedProvinciaId} onChange={e => setSelectedProvinciaId(Number(e.target.value))} required><option value="">Seleccione una Provincia</option>{provincias.map(p => <option key={p.id} value={p.id}>{p.nombre}</option>)}</Form.Select></Form.Group></Col>
                                <Col><Form.Group className="mb-3"><Form.Label>Localidad</Form.Label><Form.Select name="localidadNombre" value={domicilioData.localidadNombre} onChange={handleDomicilioChange} required disabled={!selectedProvinciaId || loadingLocalidades}><option value="">{loadingLocalidades ? 'Cargando...' : 'Seleccione una Localidad'}</option>{localidades.map(l => <option key={l.id} value={l.nombre}>{l.nombre}</option>)}</Form.Select></Form.Group></Col>
                            </Row>

                            <hr />
                            <Form.Group className="mb-3"><Form.Label>Categorías de Productos</Form.Label><Form.Select multiple value={formData.categoriaIds.map(String)} onChange={handleCategoriasChange} required style={{ height: '150px' }}><option value="" disabled>Seleccione categorías</option>{categorias.map(c => <option key={c.id} value={c.id}>{c.denominacion}</option>)}</Form.Select><Form.Text>Mantén presionado Ctrl (o Cmd en Mac) para seleccionar varias.</Form.Text></Form.Group>
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

export default SucursalForm;
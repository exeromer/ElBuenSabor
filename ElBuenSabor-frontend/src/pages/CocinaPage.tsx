import React, { useState, useCallback, useMemo } from 'react';
import { Container, Card, Button, Spinner, Alert, Row, Col, Badge, Tabs, Tab, Modal, Form, InputGroup } from 'react-bootstrap';
import { useSucursal } from '../context/SucursalContext';
import { PedidoService } from '../services/pedidoService';
import { useWebSocket } from '../hooks/useWebSocket';
import { useSearchableData } from '../hooks/useSearchableData';
import type { PedidoResponse } from '../types/types';
import type { Estado } from '../types/enums';
import toast from 'react-hot-toast';
import Titulo from '../components/utils/Titulo/Titulo';
import PedidoDetailModal from '../components/pedidos/PedidoDetailModal';
import { faEye, faCheck, faClock, faExclamationTriangle } from '@fortawesome/free-solid-svg-icons';
import { FontAwesomeIcon } from '@fortawesome/react-fontawesome';
import { isPast, parse } from 'date-fns';

// Componente interno para renderizar cada tarjeta de pedido
const PedidoCard: React.FC<{ pedido: PedidoResponse; onMarcarListo: () => void; onVerDetalles: () => void; onAbrirDemora: () => void; isDemorado: boolean }> = 
({ pedido, onMarcarListo, onVerDetalles, onAbrirDemora, isDemorado }) => (
    <Col>
        <Card className={`h-100 shadow-sm ${isDemorado ? 'border-danger' : ''}`}>
            <Card.Header as="h5" className={`d-flex justify-content-between align-items-center ${isDemorado ? 'bg-danger text-white' : ''}`}>
                Pedido #{pedido.id}
                {isDemorado && <FontAwesomeIcon icon={faExclamationTriangle} title="¡Este pedido está demorado!" />}
            </Card.Header>
            <Card.Body className="d-flex flex-column">
                <Card.Subtitle className="mb-2 text-muted">Cliente: {pedido.cliente.nombre}</Card.Subtitle>
                <hr />
                <div className="flex-grow-1">
                    {pedido.detalles.map(detalle => (
                        <p key={detalle.id} className="mb-1">
                            <Badge bg="primary" pill className="me-2">{detalle.cantidad}</Badge>
                            {detalle.articulo.denominacion}
                        </p>
                    ))}
                </div>
                <div className="mt-3 d-grid gap-2">
                    <Button variant="outline-info" size="sm" onClick={onAbrirDemora}>
                        <FontAwesomeIcon icon={faClock} className="me-2" />Añadir Demora
                    </Button>
                    <Button variant="outline-secondary" size="sm" onClick={onVerDetalles}>
                        <FontAwesomeIcon icon={faEye} className="me-2" />Ver Detalle
                    </Button>
                    {pedido.estado === 'PREPARACION' && (
                        <Button variant="success" onClick={onMarcarListo}>
                            <FontAwesomeIcon icon={faCheck} className="me-2" />Marcar como Listo
                        </Button>
                    )}
                </div>
            </Card.Body>
            <Card.Footer>
                <small className="text-muted">Hora Estimada: {pedido.horaEstimadaFinalizacion}</small>
            </Card.Footer>
        </Card>
    </Col>
);

const CocinaPage: React.FC = () => {
    const { selectedSucursal } = useSucursal();
    const [activeTab, setActiveTab] = useState<'enCocina' | 'demorados' | 'listos'>('enCocina');
    
    // Estados para los modales
    const [showDetailModal, setShowDetailModal] = useState(false);
    const [selectedPedido, setSelectedPedido] = useState<PedidoResponse | null>(null);
    const [showDelayModal, setShowDelayModal] = useState(false);
    const [delayMinutes, setDelayMinutes] = useState<number>(10);
    const [delayingPedidoId, setDelayingPedidoId] = useState<number | null>(null);

    // --- Hooks de datos ---
    const fetchEnPreparacion = useCallback(async () => {
        if (!selectedSucursal) return [];
        return PedidoService.getPedidosCocina(selectedSucursal.id);
    }, [selectedSucursal]);

    const fetchListos = useCallback(async () => {
        if (!selectedSucursal) return [];
        return PedidoService.getPedidosCajero(selectedSucursal.id, 'LISTO');
    }, [selectedSucursal]);

    const { items: pedidosEnPreparacion, isLoading: loadingPreparacion, error: errorPreparacion, reload: reloadPreparacion } = useSearchableData({ fetchData: fetchEnPreparacion });
    const { items: pedidosListos, isLoading: loadingListos, error: errorListos, reload: reloadListos } = useSearchableData({ fetchData: fetchListos });

    // --- Lógica para separar demorados ---
    const [pedidosEnTiempo, pedidosDemorados] = useMemo(() => {
        const enTiempo: PedidoResponse[] = [];
        const demorados: PedidoResponse[] = [];

        pedidosEnPreparacion.forEach(pedido => {
            // Se combina la fecha del pedido con la hora estimada para una comparación precisa
            const horaEstimadaCompleta = parse(pedido.horaEstimadaFinalizacion, 'HH:mm:ss', new Date(pedido.fechaPedido));
            if (isPast(horaEstimadaCompleta)) {
                demorados.push(pedido);
            } else {
                enTiempo.push(pedido);
            }
        });
        return [enTiempo, demorados];
    }, [pedidosEnPreparacion]);

    // --- WebSocket ---
    const cocinaTopic = selectedSucursal ? `/topic/pedidos/sucursal/${selectedSucursal.id}/cocina` : '';
    useWebSocket(cocinaTopic, () => {
        reloadPreparacion();
        reloadListos();
    });

    // --- MANEJADORES DE ACCIONES ---
    const handleUpdateEstado = async (pedidoId: number, nuevoEstado: Estado) => {
        if (!selectedSucursal) return;
        const promise = PedidoService.updateEstadoEmpleado(pedidoId, selectedSucursal.id, nuevoEstado);
        toast.promise(promise, { loading: 'Actualizando estado...', success: `Pedido #${pedidoId} actualizado.`, error: (err) => `Error: ${err.message}` });
        try { await promise; reloadPreparacion(); reloadListos(); } catch (e) { console.error(e); }
    };

    const handleViewDetails = (pedido: PedidoResponse) => {
        setSelectedPedido(pedido);
        setShowDetailModal(true);
    };

    const handleOpenDelayModal = (pedidoId: number) => {
        setDelayingPedidoId(pedidoId);
        setShowDelayModal(true);
    };

    const handleAddDelay = async () => {
        if (!delayingPedidoId || !selectedSucursal) return;
        const promise = PedidoService.addTiempoCocina(delayingPedidoId, selectedSucursal.id, delayMinutes);
        toast.promise(promise, { loading: 'Añadiendo demora...', success: `Demora añadida al pedido #${delayingPedidoId}.`, error: (err) => `Error: ${err.message}` });
        try { await promise; reloadPreparacion(); } catch (e) { console.error(e); } finally { setShowDelayModal(false); }
    };

    const renderPedidos = (listaPedidos: PedidoResponse[], demorado: boolean) => (
        <Row xs={1} md={2} lg={3} className="g-4 mt-2">
            {listaPedidos.map(pedido => (
                <PedidoCard 
                    key={pedido.id}
                    pedido={pedido}
                    onMarcarListo={() => handleUpdateEstado(pedido.id, 'LISTO')}
                    onVerDetalles={() => handleViewDetails(pedido)}
                    onAbrirDemora={() => handleOpenDelayModal(pedido.id)}
                    isDemorado={demorado}
                />
            ))}
        </Row>
    );

    return (
        <>
            <Container className="my-4">
                <Titulo texto="Gestión de Cocina" nivel="titulo" />

                <Tabs activeKey={activeTab} onSelect={(k) => setActiveTab(k as any)} id="cocina-tabs" className="mb-3" fill>
                    <Tab eventKey="enCocina" title={<span>En Cocina <Badge pill bg="primary">{pedidosEnTiempo.length}</Badge></span>}>
                        {loadingPreparacion ? <Spinner animation="border" /> : errorPreparacion ? <Alert variant="danger">{errorPreparacion}</Alert> :
                        pedidosEnTiempo.length === 0 ? <Alert variant="secondary" className="mt-3">No hay pedidos en tiempo.</Alert> : renderPedidos(pedidosEnTiempo, false)}
                    </Tab>
                    <Tab eventKey="demorados" title={<span>Demorados <Badge pill bg="danger">{pedidosDemorados.length}</Badge></span>}>
                        {loadingPreparacion ? <Spinner animation="border" /> : errorPreparacion ? <Alert variant="danger">{errorPreparacion}</Alert> :
                        pedidosDemorados.length === 0 ? <Alert variant="secondary" className="mt-3">No hay pedidos demorados.</Alert> : renderPedidos(pedidosDemorados, true)}
                    </Tab>
                    <Tab eventKey="listos" title={<span>Listos para Entregar <Badge pill bg="success">{pedidosListos.length}</Badge></span>}>
                        {loadingListos ? <Spinner animation="border" /> : errorListos ? <Alert variant="danger">{errorListos}</Alert> :
                        pedidosListos.length === 0 ? <Alert variant="secondary" className="mt-3">No hay pedidos listos.</Alert> : renderPedidos(pedidosListos, false)}
                    </Tab>
                </Tabs>
            </Container>

            <PedidoDetailModal show={showDetailModal} onHide={() => setShowDetailModal(false)} pedido={selectedPedido} />

            <Modal show={showDelayModal} onHide={() => setShowDelayModal(false)} centered>
                <Modal.Header closeButton>
                    <Modal.Title>Añadir Demora al Pedido #{delayingPedidoId}</Modal.Title>
                </Modal.Header>
                <Modal.Body>
                    <Form.Label>Minutos a añadir:</Form.Label>
                    <InputGroup>
                        <Form.Control type="number" value={delayMinutes} onChange={(e) => setDelayMinutes(Number(e.target.value))} min="1" autoFocus />
                        <InputGroup.Text>minutos</InputGroup.Text>
                    </InputGroup>
                </Modal.Body>
                <Modal.Footer>
                    <Button variant="secondary" onClick={() => setShowDelayModal(false)}>Cancelar</Button>
                    <Button variant="primary" onClick={handleAddDelay}>Confirmar Demora</Button>
                </Modal.Footer>
            </Modal>
        </>
    );
};

export default CocinaPage;
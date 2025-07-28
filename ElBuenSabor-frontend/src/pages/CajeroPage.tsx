import React, { useState, useCallback } from 'react';
import { Container, Card, Button, Spinner, Alert, Tabs, Tab, Badge } from 'react-bootstrap';
import { useSucursal } from '../context/SucursalContext';
import { PedidoService } from '../services/pedidoService';
import { useWebSocket } from '../hooks/useWebSocket';
import { useSearchableData } from '../hooks/useSearchableData';
import type { PedidoResponse } from '../types/types';
import type { Estado } from '../types/enums';
import toast from 'react-hot-toast';
import { SearchableTable, type ColumnDefinition } from '../components/common/Tables/SearchableTable';
import { faEye } from '@fortawesome/free-solid-svg-icons';
import { FontAwesomeIcon } from '@fortawesome/react-fontawesome';
import CajeroPedidoDetailModal from '../components/pedidos/PedidoDetailModal';

const CajeroPage: React.FC = () => {
    const { selectedSucursal } = useSucursal();

    const [activeTab, setActiveTab] = useState<Estado>('PENDIENTE');
    const [pedidoIdSearch, setPedidoIdSearch] = useState('');
    const [showDetailModal, setShowDetailModal] = useState(false);
    const [selectedPedido, setSelectedPedido] = useState<PedidoResponse | null>(null);

    const fetchPedidos = useCallback(async () => {
        if (!selectedSucursal) return [];

        const id = pedidoIdSearch ? parseInt(pedidoIdSearch, 10) : undefined;

        return PedidoService.getPedidosCajero(selectedSucursal.id, activeTab, id);
    }, [selectedSucursal, activeTab, pedidoIdSearch]);

    const {
        items: pedidos,
        isLoading,
        error,
        reload,
        requestSort,
        sortConfig
    } = useSearchableData({ fetchData: fetchPedidos });

    const handleWebSocketMessage = useCallback((pedido: PedidoResponse) => {
        if (pedido.estado === activeTab) {
            reload();
        }
    }, [activeTab, reload]);

    const cajeroTopic = selectedSucursal ? `/topic/pedidos/sucursal/${selectedSucursal.id}/cajero` : '';

    useWebSocket(cajeroTopic, handleWebSocketMessage);

    const handleTabSelect = (k: string | null) => {
        if (k) {
            setPedidoIdSearch('');
            setActiveTab(k as Estado);
        }
    };
    const handleUpdateEstado = async (pedidoId: number, nuevoEstado: Estado) => {
        if (!selectedSucursal) return;

        const promise = PedidoService.updateEstadoEmpleado(pedidoId, selectedSucursal.id, nuevoEstado);

        toast.promise(promise, {
            loading: 'Actualizando estado...',
            success: `Pedido #${pedidoId} actualizado a ${nuevoEstado}.`,
            error: (err) => `Error: ${err.message || 'No se pudo actualizar'}`,
        });

        try {
            await promise;
            reload();
        } catch (err) {
            console.error(err);
        }
    };
    const handleViewDetails = (pedido: PedidoResponse) => {
        setSelectedPedido(pedido);
        setShowDetailModal(true);
    };

    const columns: ColumnDefinition<PedidoResponse>[] = [
        { key: 'id', header: 'ID', renderCell: (p) => p.id, sortable: true },
        { key: 'cliente.nombre', header: 'Cliente', renderCell: (p) => `${p.cliente.nombre} ${p.cliente.apellido}` },
        { key: 'fechaPedido', header: 'Fecha', renderCell: (p) => new Date(p.fechaPedido).toLocaleDateString(), sortable: true },
        {
            key: 'formaPago', header: 'Método de Pago', renderCell: (p) => (
                <div>
                    {p.formaPago}
                    {p.formaPago === 'MERCADO_PAGO' && <Badge bg="success" className="ms-2">Pagado</Badge>}
                    {p.formaPago === 'EFECTIVO' && <Badge bg="warning" className="ms-2">Pendiente</Badge>}
                </div>
            )
        },
        { key: 'estado', header: 'Estado Actual', renderCell: (p) => <Badge bg="info">{p.estado}</Badge> },
    ];

    const renderRowActions = (pedido: PedidoResponse) => (
        <>
            <Button
                variant="info"
                size="sm"
                className="me-2"
                onClick={() => handleViewDetails(pedido)}
                title="Ver Detalle del Pedido"
            >
                <FontAwesomeIcon icon={faEye} />
            </Button>

            {(() => {
                switch (pedido.estado) {
                    case 'PENDIENTE':
                        return (
                            <>
                                <Button variant="danger" size="sm" className="me-2" onClick={() => handleUpdateEstado(pedido.id, 'RECHAZADO')}>Rechazar</Button>
                                <Button variant="success" size="sm" onClick={() => handleUpdateEstado(pedido.id, 'PREPARACION')}>Confirmar</Button>
                            </>
                        );
                    case 'LISTO':
                        return (
                            <>
                                {pedido.tipoEnvio === 'DELIVERY' &&
                                    <Button variant="primary" size="sm" className="me-2" onClick={() => handleUpdateEstado(pedido.id, 'EN_CAMINO')}>A Delivery</Button>
                                }
                                {pedido.tipoEnvio === 'TAKEAWAY' &&
                                    <Button variant="success" size="sm" onClick={() => handleUpdateEstado(pedido.id, 'ENTREGADO')}>Entregar</Button>
                                }
                            </>
                        );
                    default:
                        return null;
                }
            })()}
        </>
    );

    return (
        <Container className="my-4">
            <h1 className="text-center mb-4">Gestión de Pedidos (Cajero)</h1>
            <Tabs activeKey={activeTab} onSelect={handleTabSelect} id="pedidos-tabs" className="mb-3" fill>
                <Tab eventKey="PENDIENTE" title="A Confirmar" />
                <Tab eventKey="PREPARACION" title="En Cocina" />
                <Tab eventKey="LISTO" title="Listos" />
                <Tab eventKey="EN_CAMINO" title="En Delivery" />
                <Tab eventKey="ENTREGADO" title="Entregados" />
            </Tabs>
            <Card>
                <Card.Body>
                    {isLoading ? (
                        <div className="text-center"><Spinner animation="border" /></div>
                    ) : error ? (
                        <Alert variant="danger">{error}</Alert>
                    ) : (
                        <SearchableTable
                            items={pedidos}
                            columns={columns}
                            renderRowActions={renderRowActions}
                            isLoading={isLoading}
                            error={error}
                            reload={reload}
                            searchTerm={pedidoIdSearch}
                            setSearchTerm={setPedidoIdSearch}
                            sortConfig={sortConfig}
                            requestSort={requestSort}
                            searchPlaceholder="Buscar por ID..."
                        />
                    )}
                </Card.Body>
            </Card>
            <CajeroPedidoDetailModal
                show={showDetailModal}
                onHide={() => setShowDetailModal(false)}
                pedido={selectedPedido}
            />
        </Container >

    );
};
export default CajeroPage;
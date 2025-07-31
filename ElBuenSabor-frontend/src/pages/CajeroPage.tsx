import React, { useState, useCallback } from 'react';
import { Container, Card, Button, Tabs, Tab, Badge } from 'react-bootstrap';
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
import { FacturaService } from '../services/FacturaService';
import type { FacturaResponse } from '../types/types';
import FacturaDetailModal from '../components/facturas/FacturaDetailModal';
import { faFileInvoice } from '@fortawesome/free-solid-svg-icons';

const CajeroPage: React.FC = () => {
    const { selectedSucursal } = useSucursal();

    const [mainTab, setMainTab] = useState<'pedidos' | 'facturas'>('pedidos');
    const [pedidoStatusFilter, setPedidoStatusFilter] = useState<Estado>('PENDIENTE');

    const [pedidoIdSearch, setPedidoIdSearch] = useState('');
    const [facturaSearchTerm, setFacturaSearchTerm] = useState('');
    const [showDetailModal, setShowDetailModal] = useState(false);
    const [selectedPedido, setSelectedPedido] = useState<PedidoResponse | null>(null);
    const [showFacturaModal, setShowFacturaModal] = useState(false);
    const [selectedFactura, setSelectedFactura] = useState<FacturaResponse | null>(null);

    const fetchPedidos = useCallback(async () => {
        if (!selectedSucursal) return [];
        const id = pedidoIdSearch ? parseInt(pedidoIdSearch, 10) : undefined;
        return PedidoService.getPedidosCajero(selectedSucursal.id, pedidoStatusFilter, id);
    }, [selectedSucursal, pedidoStatusFilter, pedidoIdSearch]);

    const {
        items: pedidos,
        isLoading,
        error,
        reload,
        requestSort,
        sortConfig
    } = useSearchableData({ fetchData: fetchPedidos });

    const fetchFacturas = useCallback(async () => {
        if (mainTab !== 'facturas' || !selectedSucursal) return [];
        return FacturaService.getAllIncludingAnuladas();
    }, [mainTab, selectedSucursal]);

    const facturasData = useSearchableData<FacturaResponse>({ fetchData: fetchFacturas });

    const handleWebSocketMessage = useCallback(() => {
        reload();
        facturasData.reload();
    }, [reload, facturasData]);

    const cajeroTopic = selectedSucursal ? `/topic/pedidos/sucursal/${selectedSucursal.id}/cajero` : '';

    useWebSocket(cajeroTopic, handleWebSocketMessage);

    const handlePedidoStatusTabSelect = (k: string | null) => {
        if (k) {
            setPedidoIdSearch('');
            setPedidoStatusFilter(k as Estado);
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
    const handleViewFactura = (factura: FacturaResponse) => {
        setSelectedFactura(factura);
        setShowFacturaModal(true);
    };

    const handleAnularFactura = async (facturaId: number) => {
        if (window.confirm(`¿Estás seguro de que quieres anular la factura #${facturaId}? Esta acción no se puede deshacer y repondrá el stock.`)) {
            const promise = FacturaService.anularFactura(facturaId);
            toast.promise(promise, {
                loading: 'Anulando factura...',
                success: () => {
                    facturasData.reload(); // Recargamos la tabla de facturas
                    return 'Factura anulada con éxito.';
                },
                error: (err) => `Error: ${err.message || 'No se pudo anular la factura.'}`,
            });
        }
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

    const facturaColumns: ColumnDefinition<FacturaResponse>[] = [
        { key: 'id', header: 'Factura #', renderCell: (f) => f.id, sortable: true },
        { key: 'pedido.id', header: 'Pedido #', renderCell: (f) => f.pedido.id, sortable: true },
        { key: 'fechaFacturacion', header: 'Fecha', renderCell: (f) => new Date(f.fechaFacturacion).toLocaleDateString(), sortable: true },
        {
            key: 'estadoFactura', header: 'Estado', renderCell: (f) => (
                <Badge bg={f.estadoFactura === 'ANULADA' ? 'danger' : 'success'}>
                    {f.estadoFactura}
                </Badge>
            )
        },
        { key: 'totalVenta', header: 'Total', renderCell: (f) => `$${f.totalVenta.toFixed(2)}` },
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
    const renderFacturaActions = (factura: FacturaResponse) => (
        <>
            <Button variant="info" size="sm" className="me-2" onClick={() => handleViewFactura(factura)} title="Ver Detalle Factura">
                <FontAwesomeIcon icon={faEye} />
            </Button>
            {String(factura.estadoFactura) === 'ACTIVA' && (
                <Button variant="danger" size="sm" onClick={() => handleAnularFactura(factura.id)} title="Anular Factura">
                    Anular
                </Button>
            )}
        </>
    );

    return (
        <Container className="my-4">
            <h1 className="text-center mb-4">Gestión de Caja</h1>
            <Tabs activeKey={mainTab} onSelect={(k) => setMainTab(k as any)} id="main-cajero-tabs" className="mb-3" fill>
                <Tab eventKey="pedidos" title="Gestión de Pedidos">
                    <Tabs activeKey={pedidoStatusFilter} onSelect={handlePedidoStatusTabSelect} id="pedidos-status-tabs" className="mb-3" fill>
                        <Tab eventKey="PENDIENTE" title="A Confirmar" />
                        <Tab eventKey="PREPARACION" title="En Cocina" />
                        <Tab eventKey="LISTO" title="Listos" />
                        <Tab eventKey="EN_CAMINO" title="En Delivery" />
                        <Tab eventKey="ENTREGADO" title="Entregados" />
                    </Tabs>
                    <Card>
                        <Card.Body>
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
                                searchPlaceholder="Buscar por ID de pedido..."
                            />
                        </Card.Body>
                    </Card>
                </Tab>
                <Tab eventKey="facturas" title={<span><FontAwesomeIcon icon={faFileInvoice} className="me-2" />Facturación</span>}>
                    <Card>
                        <Card.Body>
                            <SearchableTable
                                items={facturasData.items}
                                columns={facturaColumns}
                                renderRowActions={renderFacturaActions}
                                isLoading={facturasData.isLoading}
                                error={facturasData.error}
                                reload={facturasData.reload}
                                sortConfig={facturasData.sortConfig}
                                requestSort={facturasData.requestSort}
                                searchPlaceholder="Buscar por ID de factura o pedido..."
                                searchTerm={facturaSearchTerm}
                                setSearchTerm={setFacturaSearchTerm}
                            />
                        </Card.Body>
                    </Card>
                </Tab>
            </Tabs>

            <CajeroPedidoDetailModal
                show={showDetailModal}
                onHide={() => setShowDetailModal(false)}
                pedido={selectedPedido}
            />

            <FacturaDetailModal
                show={showFacturaModal}
                onHide={() => setShowFacturaModal(false)}
                factura={selectedFactura}
            />
        </Container >
    );
};
export default CajeroPage;
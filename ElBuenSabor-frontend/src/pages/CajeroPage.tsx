
import React, { useState, useEffect, useCallback } from 'react';
import { Container, Card, Button, Spinner, Alert, Tabs, Tab, Badge } from 'react-bootstrap';
import { useSucursal } from '../context/SucursalContext';
import { PedidoService } from '../services/PedidoService';
import { useWebSocket } from '../hooks/useWebSocket';
import type { PedidoResponse } from '../types/types';
import type { Estado } from '../types/enums';

// Componente de tabla reutilizable que ya tienes
import { SearchableTable, type ColumnDefinition } from '../components/common/Tables/SearchableTable';

const CajeroPage: React.FC = () => {
    const { selectedSucursal } = useSucursal();
    const [pedidos, setPedidos] = useState<PedidoResponse[]>([]);
    const [isLoading, setIsLoading] = useState(true);
    const [error, setError] = useState<string | null>(null);
    const [activeTab, setActiveTab] = useState<Estado>('PENDIENTE');

    const handleNewPedido = useCallback((pedido: PedidoResponse) => {
        console.log("Nuevo pedido recibido por WebSocket:", pedido);
        // Si el pedido recibido corresponde a la pestaña que estamos viendo, lo añadimos al principio de la lista.
        if (pedido.estado === activeTab) {
            setPedidos(prevPedidos => [pedido, ...prevPedidos]);
        }
    }, [activeTab]); // Dependemos de activeTab para saber si mostrar el pedido o no

    // 2. Definimos el tema (topic) al que nos vamos a suscribir.
    const cajeroTopic = selectedSucursal ? `/topic/pedidos/sucursal/${selectedSucursal.id}/cajero` : '';

    // 3. Usamos el hook para suscribirnos.
    useWebSocket(cajeroTopic, handleNewPedido);

    const fetchPedidos = useCallback(async () => {
        if (!selectedSucursal) return;
        setIsLoading(true);
        setError(null);
        try {
            const data = await PedidoService.getPedidosCajero(selectedSucursal.id, activeTab);
            setPedidos(data);
        } catch (err: any) {
            setError(err.message || 'Error al cargar los pedidos.');
            console.error(err);
        } finally {
            setIsLoading(false);
        }
    }, [selectedSucursal, activeTab]);

    useEffect(() => {
        fetchPedidos();
    }, [fetchPedidos]);

    const handleTabSelect = (k: string | null) => {
        if (k) {
            setActiveTab(k as Estado);
        }
    };

    const handleUpdateEstado = async (pedidoId: number, nuevoEstado: Estado) => {
        if (!selectedSucursal) return;

        // Opcional: Mostrar un spinner o deshabilitar el botón específico
        try {
            await PedidoService.updateEstadoEmpleado(pedidoId, selectedSucursal.id, nuevoEstado);
            alert(`Pedido #${pedidoId} actualizado a ${nuevoEstado}.`);

            // En lugar de recargar toda la lista, simplemente quitamos el pedido de la vista actual.
            // Esto da una sensación más fluida.
            setPedidos(prev => prev.filter(p => p.id !== pedidoId));

        } catch (err: any) {
            alert(`Error al actualizar el pedido: ${err.message}`);
            console.error(err);
        }
    };

    // --- Definición de columnas y acciones  ---
    const columns: ColumnDefinition<PedidoResponse>[] = [
        { key: 'id', header: 'ID', renderCell: (p) => p.id, sortable: true },
        { key: 'cliente.nombre', header: 'Cliente', renderCell: (p) => `${p.cliente.nombre} ${p.cliente.apellido}` },
        { key: 'fechaPedido', header: 'Fecha', renderCell: (p) => new Date(p.fechaPedido).toLocaleDateString() },
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

    const renderRowActions = (pedido: PedidoResponse) => {
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
                return <span className="text-muted">Sin acciones</span>;
        }
    };


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
                            reload={fetchPedidos}
                            searchTerm="" // Dejamos la búsqueda para más adelante 
                            setSearchTerm={() => { }} // Dejamos la búsqueda para más adelante 
                            sortConfig={null} // Sin ordenar por ahora
                            requestSort={() => { }} // Sin ordenar por ahora
                        />
                    )}
                </Card.Body>
            </Card>
        </Container>
    );
};

export default CajeroPage;
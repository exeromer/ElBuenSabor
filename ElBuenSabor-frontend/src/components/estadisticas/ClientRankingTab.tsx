import React, { useState, useEffect, useCallback } from "react";
import { Container, Card, Spinner, Alert, Form, Row, Col, Button, Table, Modal } from "react-bootstrap";
import { EstadisticaService } from "../../services/EstadisticaService";
import type { ClienteRanking, PedidoResponse } from "../../types/types"; // Asegúrate de tener PedidoResponse o un tipo similar
import { BarChart, Bar, XAxis, YAxis, CartesianGrid, Tooltip, Legend, ResponsiveContainer } from "recharts";
import { format } from "date-fns";
import { faFileExcel, faEye } from "@fortawesome/free-solid-svg-icons";
import { FontAwesomeIcon } from "@fortawesome/react-fontawesome";
import { PedidoService } from "../../services/pedidoService";
import "./ClientRankingTab.sass";

type SortBy = "cantidadPedidos" | "montoTotalComprado";

const ClientRankingTab: React.FC = () => {
    const [clientRanking, setClientRanking] = useState<ClienteRanking[]>([]);
    const [loading, setLoading] = useState(true);
    const [error, setError] = useState<string | null>(null);
    const [fechaDesde, setFechaDesde] = useState<string>("");
    const [fechaHasta, setFechaHasta] = useState<string>("");
    const [sortBy, setSortBy] = useState<SortBy>("cantidadPedidos");

    const [showPedidoModal, setShowPedidoModal] = useState(false);
    const [selectedClientOrders, setSelectedClientOrders] = useState<PedidoResponse[]>([]);
    const [loadingClientOrders, setLoadingClientOrders] = useState(false);
    const [selectedClientName, setSelectedClientName] = useState<string>("");

    const fetchRankings = useCallback(async () => {
        setLoading(true);
        setError(null);
        try {
            const params = {
                fechaDesde: fechaDesde || undefined,
                fechaHasta: fechaHasta || undefined,
            };
            const serviceCall =
                sortBy === "cantidadPedidos"
                    ? EstadisticaService.getRankingClientesPorCantidad(params)
                    : EstadisticaService.getRankingClientesPorMonto(params);
            
            const fetchedRanking = await serviceCall;
            setClientRanking(fetchedRanking);
        } catch (err: any) {
            setError(err.message || "Error al cargar el ranking de clientes.");
        } finally {
            setLoading(false);
        }
    }, [fechaDesde, fechaHasta, sortBy]);

    useEffect(() => {
        fetchRankings();
    }, [fetchRankings]);

    const handleExport = async () => {
        try {
            const excelBlob = await EstadisticaService.exportRankingClientesExcel(fechaDesde, fechaHasta);
            const url = window.URL.createObjectURL(new Blob([excelBlob]));
            const link = document.createElement("a");
            link.href = url;
            link.setAttribute("download", "ranking_clientes.xlsx");
            document.body.appendChild(link);
            link.click();
            link.remove();
        } catch (err) {
            alert("Error al exportar el ranking de clientes a Excel.");
            console.error(err);
        }
    };

    const handleViewClientOrders = async (clientId: number, clientName: string) => {
        setShowPedidoModal(true);
        setLoadingClientOrders(true);
        setSelectedClientName(clientName);
        try {
            const orders = await PedidoService.getByClienteId(clientId);
            setSelectedClientOrders(orders);
        } catch (err: any) {
            setError(err.message || "Error al cargar los pedidos del cliente.");
        } finally {
            setLoadingClientOrders(false);
        }
    };

    const chartData = clientRanking.slice(0, 10).map(client => ({
        name: client.nombreCompleto,
        value: client[sortBy]
    }));

    return (
        <Container fluid className="client-ranking-tab">
            <Card className="shadow-sm mb-4">
                <Card.Header as="h5">Filtro y Ordenamiento</Card.Header>
                <Card.Body>
                    <Form>
                        <Row className="align-items-end">
                            <Col md={4}><Form.Group><Form.Label>Fecha Desde:</Form.Label><Form.Control type="date" value={fechaDesde} onChange={(e) => setFechaDesde(e.target.value)} /></Form.Group></Col>
                            <Col md={4}><Form.Group><Form.Label>Fecha Hasta:</Form.Label><Form.Control type="date" value={fechaHasta} onChange={(e) => setFechaHasta(e.target.value)} /></Form.Group></Col>
                            <Col md={4}><Form.Group><Form.Label>Ordenar por:</Form.Label><Form.Select value={sortBy} onChange={(e) => setSortBy(e.target.value as SortBy)}><option value="cantidadPedidos">Cantidad de Pedidos</option><option value="montoTotalComprado">Monto Total Comprado</option></Form.Select></Form.Group></Col>
                        </Row>
                        <Row className="mt-3">
                             <Col className="d-flex justify-content-end">
                                <Button onClick={fetchRankings} disabled={loading} className="me-2">{loading ? <Spinner as="span" animation="border" size="sm" /> : "Aplicar Filtros"}</Button>
                                <Button variant="success" onClick={handleExport}><FontAwesomeIcon icon={faFileExcel} className="me-2" />Exportar</Button>
                            </Col>
                        </Row>
                    </Form>
                </Card.Body>
            </Card>

            {loading ? <div className="text-center my-3"><Spinner /> <p>Cargando ranking...</p></div> : error ? <Alert variant="danger">{error}</Alert> :
                <Card className="shadow-sm">
                    <Card.Header as="h5">Ranking de Clientes por {sortBy === "cantidadPedidos" ? "Cantidad de Pedidos" : "Monto Total"}</Card.Header>
                    <Card.Body>
                        {clientRanking.length > 0 ? (
                            <>
                                <div style={{ width: "100%", height: 350 }}>
                                    <ResponsiveContainer>
                                        <BarChart data={chartData} margin={{ top: 5, right: 30, left: 20, bottom: 80 }}>
                                            <CartesianGrid strokeDasharray="3 3" />
                                            <XAxis dataKey="name" angle={-45} textAnchor="end" interval={0} />
                                            <YAxis />
                                            <Tooltip formatter={(value: number) => sortBy === "montoTotalComprado" ? `$${value.toFixed(2)}` : value} />
                                            <Legend verticalAlign="top" />
                                            <Bar dataKey="value" fill="#8884d8" name={sortBy === "cantidadPedidos" ? "Cantidad de Pedidos" : "Monto Total ($)"} />
                                        </BarChart>
                                    </ResponsiveContainer>
                                </div>
                                <Table striped bordered hover responsive className="mt-3 text-center align-middle">
                                    <thead><tr><th>Cliente</th><th>Email</th><th>Cantidad de Pedidos</th><th>Monto Total</th><th>Acciones</th></tr></thead>
                                    <tbody>
                                        {clientRanking.map((client) => (
                                            <tr key={client.clienteId}>
                                                <td>{client.nombreCompleto}</td>
                                                <td>{client.email}</td>
                                                <td>{client.cantidadPedidos}</td>
                                                <td>${client.montoTotalComprado.toFixed(2)}</td>
                                                <td><Button variant="info" size="sm" onClick={() => handleViewClientOrders(client.clienteId, client.nombreCompleto)}><FontAwesomeIcon icon={faEye} /> Ver Pedidos</Button></td>
                                            </tr>
                                        ))}
                                    </tbody>
                                </Table>
                            </>
                        ) : <Alert variant="info">No hay datos de ranking para mostrar.</Alert>}
                    </Card.Body>
                </Card>
            }

            <Modal show={showPedidoModal} onHide={() => setShowPedidoModal(false)} size="lg" centered>
                <Modal.Header closeButton><Modal.Title>Pedidos de {selectedClientName}</Modal.Title></Modal.Header>
                <Modal.Body>
                    {loadingClientOrders ? <div className="text-center"><Spinner /></div> : selectedClientOrders.length === 0 ? <Alert variant="info">Este cliente no tiene pedidos.</Alert> :
                        <Table striped bordered hover responsive className="text-center align-middle">
                            <thead><tr><th>N° Pedido</th><th>Fecha</th><th>Estado</th><th>Total</th></tr></thead>
                            <tbody>
                                {selectedClientOrders.map((order) => (
                                    <tr key={order.id}>
                                        <td>#{order.id}</td>
                                        <td>{format(new Date(order.fechaPedido), "dd/MM/yyyy")}</td>
                                        <td>{order.estado}</td>
                                        <td>${order.total.toFixed(2)}</td>
                                    </tr>
                                ))}
                            </tbody>
                        </Table>
                    }
                </Modal.Body>
                <Modal.Footer><Button variant="secondary" onClick={() => setShowPedidoModal(false)}>Cerrar</Button></Modal.Footer>
            </Modal>
        </Container>
    );
};

export default ClientRankingTab;
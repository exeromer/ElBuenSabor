import React, { useState, useEffect, useCallback } from "react";
import {
  Container,
  Card,
  Spinner,
  Alert,
  Form,
  Row,
  Col,
  Button,
  Table,
  Dropdown,
  Modal,
} from "react-bootstrap";
import { EstadisticaService } from "../../services/EstadisticaService";
import type { ClienteRanking } from "../../types/types";
import {
  BarChart,
  Bar,
  XAxis,
  YAxis,
  CartesianGrid,
  Tooltip,
  Legend,
  ResponsiveContainer,
} from "recharts";
import { format } from "date-fns";
import { faFileExcel, faEye } from "@fortawesome/free-solid-svg-icons";
import { FontAwesomeIcon } from "@fortawesome/react-fontawesome";
import { useNavigate } from "react-router-dom";
import PedidoDetailModal from "../pedidos/PedidoDetailModal";
import { PedidoService } from "../../services/pedidoService"; // Para obtener pedidos del cliente
// **CÓDIGO NUEVO: Importación del archivo de estilos**
import "./ClientRankingTab.sass";

type SortBy = "cantidadPedidos" | "montoTotalComprado";

const ClientRankingTab: React.FC = () => {
  const [clientRanking, setClientRanking] = useState<ClienteRanking[]>([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState<string | null>(null);
  const [fechaDesde, setFechaDesde] = useState<string>("");
  const [fechaHasta, setFechaHasta] = useState<string>("");
  const [sortBy, setSortBy] = useState<SortBy>("cantidadPedidos"); // Ordenamiento por defecto
  const navigate = useNavigate();

  // Para el modal "Ver Pedidos"
  const [showPedidoModal, setShowPedidoModal] = useState(false);
  const [selectedClientOrders, setSelectedClientOrders] = useState<any[]>([]); // Para almacenar los pedidos para el modal
  const [loadingClientOrders, setLoadingClientOrders] = useState(false);
  const [errorClientOrders, setErrorClientOrders] = useState<string | null>(
    null
  );
  const [selectedClientName, setSelectedClientName] = useState<string>("");

  const fetchRankings = useCallback(async () => {
    setLoading(true);
    setError(null);
    try {
      const params = {
        fechaDesde: fechaDesde || undefined,
        fechaHasta: fechaHasta || undefined,
        size: 100,
      }; // Obtener suficientes datos para mostrar
      let fetchedRanking: ClienteRanking[] = [];
      if (sortBy === "cantidadPedidos") {
        fetchedRanking = await EstadisticaService.getRankingClientesPorCantidad(
          params
        );
      } else {
        fetchedRanking = await EstadisticaService.getRankingClientesPorMonto(
          params
        );
      }
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
      const excelBlob = await EstadisticaService.exportRankingClientesExcel(
        fechaDesde,
        fechaHasta
      );
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

  const handleViewClientOrders = async (
    clientId: number,
    clientName: string
  ) => {
    setLoadingClientOrders(true);
    setErrorClientOrders(null);
    setSelectedClientName(clientName);
    try {
      const orders = await PedidoService.getByClienteId(clientId);
      setSelectedClientOrders(orders);
      setShowPedidoModal(true);
    } catch (err: any) {
      setErrorClientOrders(
        err.message || "Error al cargar los pedidos del cliente."
      );
    } finally {
      setLoadingClientOrders(false);
    }
  };

  return (
    // **CÓDIGO MODIFICADO: Aplicación de la clase CSS principal**
    <Container fluid className="client-ranking-tab">
      <Card className="shadow-sm mb-4">
        <Card.Header as="h5">Filtro y Ordenamiento</Card.Header>
        <Card.Body>
          <Form>
            <Row className="mb-3">
              <Col md={6}>
                <Form.Group>
                  <Form.Label>Fecha Desde:</Form.Label>
                  <Form.Control
                    type="date"
                    value={fechaDesde}
                    onChange={(e) => setFechaDesde(e.target.value)}
                  />
                </Form.Group>
              </Col>
              <Col md={6}>
                <Form.Group>
                  <Form.Label>Fecha Hasta:</Form.Label>
                  <Form.Control
                    type="date"
                    value={fechaHasta}
                    onChange={(e) => setFechaHasta(e.target.value)}
                  />
                </Form.Group>
              </Col>
            </Row>
            <Row className="mb-3">
              <Col md={6}>
                <Form.Group>
                  <Form.Label>Ordenar por:</Form.Label>
                  <Form.Select
                    value={sortBy}
                    onChange={(e) => setSortBy(e.target.value as SortBy)}
                  >
                    <option value="cantidadPedidos">Cantidad de Pedidos</option>
                    <option value="montoTotalComprado">
                      Monto Total Comprado
                    </option>
                  </Form.Select>
                </Form.Group>
              </Col>
              <Col md={6} className="d-flex align-items-end">
                <Button
                  onClick={fetchRankings}
                  disabled={loading}
                  className="me-2"
                >
                  {loading ? (
                    <Spinner as="span" animation="border" size="sm" />
                  ) : (
                    "Aplicar Filtro y Orden"
                  )}
                </Button>
                <Button variant="success" onClick={handleExport}>
                  <FontAwesomeIcon icon={faFileExcel} className="me-2" />{" "}
                  Exportar a Excel
                </Button>
              </Col>
            </Row>
          </Form>
        </Card.Body>
      </Card>

      {loading ? (
        <div className="text-center my-3">
          <Spinner animation="border" /> <p>Cargando ranking de clientes...</p>
        </div>
      ) : error ? (
        <Alert variant="danger">{error}</Alert>
      ) : (
        <Card className="shadow-sm">
          <Card.Header as="h5">
            Ranking de Clientes por{" "}
            {sortBy === "cantidadPedidos"
              ? "Cantidad de Pedidos"
              : "Monto Total Comprado"}
          </Card.Header>
          <Card.Body>
            {clientRanking.length > 0 ? (
              <>
                {/* **CÓDIGO MODIFICADO: Aplicación de la clase CSS para el gráfico** */}
                <div style={{ width: "100%", height: 350 }}>
                  <ResponsiveContainer className="recharts-wrapper">
                    <BarChart
                      data={clientRanking.slice(0, 10)} // Mostrar solo los 10 primeros en el gráfico
                      margin={{ top: 5, right: 30, left: 20, bottom: 5 }}
                    >
                      <CartesianGrid strokeDasharray="3 3" />
                      <XAxis
                        dataKey="nombreCompleto"
                        angle={-30}
                        textAnchor="end"
                        height={80}
                        interval={0}
                      />
                      <YAxis />
                      <Tooltip
                        formatter={(value: number) =>
                          sortBy === "montoTotalComprado"
                            ? `$${value.toFixed(2)}`
                            : value
                        }
                      />
                      <Legend />
                      <Bar
                        dataKey={sortBy}
                        fill="#8884d8"
                        name={
                          sortBy === "cantidadPedidos"
                            ? "Cantidad de Pedidos"
                            : "Monto Total Comprado ($)"
                        }
                      />
                    </BarChart>
                  </ResponsiveContainer>
                </div>
                {/* **CÓDIGO MODIFICADO: Aplicación de la clase CSS para la tabla** */}
                <Table striped bordered hover responsive className="mt-3">
                  <thead>
                    <tr>
                      <th>Cliente</th>
                      <th>Email</th>
                      <th>Cantidad de Pedidos</th>
                      <th>Monto Total Comprado</th>
                      <th>Acciones</th>
                    </tr>
                  </thead>
                  <tbody>
                    {clientRanking.map((client, index) => (
                      <tr key={index}>
                        <td>{client.nombreCompleto}</td>
                        <td>{client.email}</td>
                        <td>{client.cantidadPedidos}</td>
                        <td>${client.montoTotalComprado.toFixed(2)}</td>
                        <td>
                          <Button
                            variant="info"
                            size="sm"
                            onClick={() =>
                              handleViewClientOrders(
                                client.clienteId,
                                client.nombreCompleto
                              )
                            }
                          >
                            <FontAwesomeIcon icon={faEye} /> Ver Pedidos
                          </Button>
                        </td>
                      </tr>
                    ))}
                  </tbody>
                </Table>
              </>
            ) : (
              <Alert variant="info">
                No hay datos de ranking de clientes para mostrar en este
                período.
              </Alert>
            )}
          </Card.Body>
        </Card>
      )}

      {/* Modal para ver pedidos del cliente (sin cambios en su contenido) */}
      <Modal
        show={showPedidoModal}
        onHide={() => setShowPedidoModal(false)}
        size="lg"
        centered
      >
        <Modal.Header closeButton>
          <Modal.Title>Pedidos de {selectedClientName}</Modal.Title>
        </Modal.Header>
        <Modal.Body>
          {loadingClientOrders ? (
            <div className="text-center">
              <Spinner animation="border" /> <p>Cargando pedidos...</p>
            </div>
          ) : errorClientOrders ? (
            <Alert variant="danger">{errorClientOrders}</Alert>
          ) : selectedClientOrders.length === 0 ? (
            <Alert variant="info">
              Este cliente no tiene pedidos en el período seleccionado.
            </Alert>
          ) : (
            <Table
              striped
              bordered
              hover
              responsive
              className="text-center align-middle"
            >
              <thead>
                <tr>
                  <th>N° Pedido</th>
                  <th>Fecha</th>
                  <th>Estado</th>
                  <th>Total</th>
                </tr>
              </thead>
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
          )}
        </Modal.Body>
        <Modal.Footer>
          <Button variant="secondary" onClick={() => setShowPedidoModal(false)}>
            Cerrar
          </Button>
        </Modal.Footer>
      </Modal>
    </Container>
  );
};

export default ClientRankingTab;

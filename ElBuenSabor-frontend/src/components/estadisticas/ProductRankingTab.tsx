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
} from "react-bootstrap";
import { EstadisticaService } from "../../services/EstadisticaService";
// <<-- CAMBIO REALIZADO: Importar el hook para usar la sucursal activa -->>
import { useSucursal } from "../../context/SucursalContext";
import type {
  ArticuloManufacturadoRanking,
  ArticuloInsumoRanking,
} from "../../types/types";
import {
  BarChart,
  Bar,
  XAxis,
  YAxis,
  CartesianGrid,
  Tooltip,
  Legend,
  ResponsiveContainer,
  Cell,
} from "recharts";
import { faFileExcel } from "@fortawesome/free-solid-svg-icons";
import { FontAwesomeIcon } from "@fortawesome/react-fontawesome";
import "./ProductRankingTab.sass";

//Funcion de colores aleatorios
const generateRandomColor = () => {
  const letters = "0123456789ABCDEF";
  let color = "#";
  for (let i = 0; i < 6; i++) {
    color += letters[Math.floor(Math.random() * 16)];
  }
  return color;
};

const ProductRankingTab: React.FC = () => {
  // <<-- CAMBIO REALIZADO: Obtener la sucursal seleccionada del contexto global -->>
  const { selectedSucursal } = useSucursal();

  // <<-- CAMBIO REALIZADO: Dos estados separados para cada tipo de producto -->>
  const [cocinaRanking, setCocinaRanking] = useState<
    ArticuloManufacturadoRanking[]
  >([]);
  const [bebidasRanking, setBebidasRanking] = useState<ArticuloInsumoRanking[]>(
    []
  );
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState<string | null>(null);
  const [fechaDesde, setFechaDesde] = useState<string>("");
  const [fechaHasta, setFechaHasta] = useState<string>("");
  const [cocinaColors, setCocinaColors] = useState<string[]>([]);
  const [bebidasColors, setBebidasColors] = useState<string[]>([]);

  const fetchRankings = useCallback(async () => {
    // <<-- CAMBIO REALIZADO: Validar que una sucursal esté seleccionada -->>
    if (!selectedSucursal) {
      setError("Por favor, selecciona una sucursal para ver las estadísticas.");
      setLoading(false);
      return;
    }

    setLoading(true);
    setError(null);
    try {
      // <<-- CAMBIO REALIZADO: Usar el ID de la sucursal en los parámetros -->>
      const params = {
        sucursalId: selectedSucursal.id,
        fechaDesde: fechaDesde || undefined,
        fechaHasta: fechaHasta || undefined,
      };

      // <<-- CAMBIO REALIZADO: Llamar a los dos servicios a la vez y actualizar los estados por separado -->>
      const [cocina, bebidas] = await Promise.all([
        EstadisticaService.getRankingProductosCocina(params),
        EstadisticaService.getRankingBebidas(params),
      ]);
      setCocinaRanking(cocina);
      setBebidasRanking(bebidas);
      // <<-- CAMBIO: Generar array de colores al cargar los datos -->>
      setCocinaColors(cocina.map(() => generateRandomColor()));
      setBebidasColors(bebidas.map(() => generateRandomColor()));
    } catch (err: any) {
      setError(err.message || "Error al cargar los rankings de productos.");
    } finally {
      setLoading(false);
    }
    // <<-- CAMBIO REALIZADO: Añadir sucursal como dependencia para que se recargue si cambia -->>
  }, [fechaDesde, fechaHasta, selectedSucursal]);

  useEffect(() => {
    fetchRankings();
  }, [fetchRankings]);

  // <<-- CAMBIO REALIZADO: Nueva función para exportar solo productos de cocina -->>
  const handleExportCocina = async () => {
    if (!selectedSucursal) return;
    try {
      const excelBlob =
        await EstadisticaService.exportRankingProductosCocinaExcel(
          selectedSucursal.id,
          fechaDesde,
          fechaHasta
        );
      const url = window.URL.createObjectURL(new Blob([excelBlob]));
      const link = document.createElement("a");
      link.href = url;
      link.setAttribute(
        "download",
        `ranking_productos_cocina_${selectedSucursal.nombre}.xlsx`
      );
      document.body.appendChild(link);
      link.click();
      link.remove();
    } catch (err) {
      alert("Error al exportar el ranking de productos de cocina a Excel.");
    }
  };

  // <<-- CAMBIO REALIZADO: Nueva función para exportar solo bebidas -->>
  const handleExportBebidas = async () => {
    if (!selectedSucursal) return;
    try {
      const excelBlob = await EstadisticaService.exportRankingBebidasExcel(
        selectedSucursal.id,
        fechaDesde,
        fechaHasta
      );
      const url = window.URL.createObjectURL(new Blob([excelBlob]));
      const link = document.createElement("a");
      link.href = url;
      link.setAttribute(
        "download",
        `ranking_bebidas_${selectedSucursal.nombre}.xlsx`
      );
      document.body.appendChild(link);
      link.click();
      link.remove();
    } catch (err) {
      alert("Error al exportar el ranking de bebidas a Excel.");
    }
  };

  return (
    <Container fluid className="product-ranking-tab">
      <Card className="shadow-sm mb-4">
        <Card.Header as="h5">Filtro por Fechas</Card.Header>
        <Card.Body>
          <Form>
            <Row>
              <Col md={6}>
                <Form.Group className="mb-3">
                  <Form.Label>Fecha Desde:</Form.Label>
                  <Form.Control
                    type="date"
                    value={fechaDesde}
                    onChange={(e) => setFechaDesde(e.target.value)}
                  />
                </Form.Group>
              </Col>
              <Col md={6}>
                <Form.Group className="mb-3">
                  <Form.Label>Fecha Hasta:</Form.Label>
                  <Form.Control
                    type="date"
                    value={fechaHasta}
                    onChange={(e) => setFechaHasta(e.target.value)}
                  />
                </Form.Group>
              </Col>
            </Row>
            <Button
              onClick={fetchRankings}
              disabled={loading || !selectedSucursal}
            >
              {loading ? (
                <Spinner as="span" animation="border" size="sm" />
              ) : (
                "Aplicar Filtro"
              )}
            </Button>
          </Form>
        </Card.Body>
      </Card>

      {loading ? (
        <div className="text-center my-3">
          <Spinner animation="border" /> <p>Cargando rankings...</p>
        </div>
      ) : error ? (
        <Alert variant="danger">{error}</Alert>
      ) : (
        <Row>
          {/* <<-- CAMBIO REALIZADO: Grilla separada para Productos de Cocina -->> */}
          <Col lg={6}>
            <Card className="shadow-sm mb-4">
              <Card.Header as="h5">
                Productos de Cocina Más Vendidos
              </Card.Header>
              <Card.Body>
                {cocinaRanking.length > 0 ? (
                  <>
                    <ResponsiveContainer width="100%" height={300}>
                      <BarChart
                        data={cocinaRanking}
                        margin={{ top: 5, right: 30, left: 20, bottom: 80 }}
                      >
                        <CartesianGrid strokeDasharray="3 3" />
                        <XAxis
                          dataKey="denominacion"
                          tick={false}
                          axisLine={false}
                        />
                        <YAxis allowDecimals={false} />
                        <Tooltip />
                        <Bar
                          dataKey="cantidadVendida"
                          name="Cantidad Vendida"
                          maxBarSize={90}
                        >
                          {/* <<-- CAMBIO: Se usa el estado `cocinaColors` para asignar un color único a cada barra -->> */}
                          {cocinaRanking.map((entry, index) => (
                            <Cell
                              key={`cell-${index}`}
                              fill={cocinaColors[index % cocinaColors.length]}
                            />
                          ))}
                        </Bar>
                      </BarChart>
                    </ResponsiveContainer>
                    <div className="legend-container mt-3">
                      {cocinaRanking.map((item, index) => (
                        <div
                          key={`legend-cocina-${index}`}
                          className="legend-item"
                        >
                          <span
                            className="legend-color"
                            style={{
                              backgroundColor:
                                cocinaColors[index % cocinaColors.length],
                            }}
                          ></span>
                          <span className="legend-label">
                            {item.denominacion}
                          </span>
                        </div>
                      ))}
                    </div>
                    <Table striped bordered hover responsive className="mt-3">
                      <thead className="table-header-custom">
                        <tr>
                          <th>Denominación</th>
                          <th>Cantidad Vendida</th>
                        </tr>
                      </thead>
                      <tbody>
                        {cocinaRanking.map((item) => (
                          <tr key={item.id}>
                            <td>{item.denominacion}</td>
                            <td>{item.cantidadVendida}</td>
                          </tr>
                        ))}
                      </tbody>
                    </Table>
                    <Button
                      variant="success"
                      onClick={handleExportCocina}
                      className="mt-3"
                    >
                      <FontAwesomeIcon icon={faFileExcel} className="me-2" />{" "}
                      Exportar a Excel
                    </Button>
                  </>
                ) : (
                  <Alert variant="info">
                    No hay productos de cocina vendidos para los filtros
                    seleccionados.
                  </Alert>
                )}
              </Card.Body>
            </Card>
          </Col>

          {/* <<-- CAMBIO REALIZADO: Grilla separada para Bebidas -->> */}
          <Col lg={6}>
            <Card className="shadow-sm mb-4">
              <Card.Header as="h5">Bebidas Más Vendidas</Card.Header>
              <Card.Body>
                {bebidasRanking.length > 0 ? (
                  <>
                    <ResponsiveContainer width="100%" height={300}>
                      <BarChart
                        data={bebidasRanking}
                        margin={{ top: 5, right: 30, left: 20, bottom: 80 }}
                      >
                        <CartesianGrid strokeDasharray="3 3" />
                        {/* <<-- CAMBIO: Se oculta el eje X para usar la leyenda en su lugar -->> */}
                        <XAxis
                          dataKey="denominacion"
                          tick={false}
                          axisLine={false}
                        />
                        <YAxis allowDecimals={false} />
                        <Tooltip />

                        <Bar
                          dataKey="cantidadVendida"
                          name="Cantidad Vendida"
                          maxBarSize={90}
                        >
                          {/* <<-- CAMBIO: Se usa el estado `bebidasColors` para asignar un color único a cada barra -->> */}
                          {bebidasRanking.map((entry, index) => (
                            <Cell
                              key={`cell-${index}`}
                              fill={bebidasColors[index % bebidasColors.length]}
                            />
                          ))}
                        </Bar>
                      </BarChart>
                    </ResponsiveContainer>
                    {/* <<-- CAMBIO: Contenedor de la leyenda personalizada -->> */}
                    <div className="legend-container mt-3">
                      {bebidasRanking.map((item, index) => (
                        <div
                          key={`legend-bebidas-${index}`}
                          className="legend-item"
                        >
                          <span
                            className="legend-color"
                            style={{
                              backgroundColor:
                                bebidasColors[index % bebidasColors.length],
                            }}
                          ></span>
                          <span className="legend-label">
                            {item.denominacion}
                          </span>
                        </div>
                      ))}
                    </div>
                    <Table striped bordered hover responsive className="mt-3">
                      <thead className="table-header-custom">
                        <tr>
                          <th>Denominación</th>
                          <th>Cantidad Vendida</th>
                        </tr>
                      </thead>
                      <tbody>
                        {bebidasRanking.map((item) => (
                          <tr key={item.id}>
                            <td>{item.denominacion}</td>
                            <td>{item.cantidadVendida}</td>
                          </tr>
                        ))}
                      </tbody>
                    </Table>
                    <Button
                      variant="success"
                      onClick={handleExportBebidas}
                      className="mt-3"
                    >
                      <FontAwesomeIcon icon={faFileExcel} className="me-2" />{" "}
                      Exportar a Excel
                    </Button>
                  </>
                ) : (
                  <Alert variant="info">
                    No hay bebidas vendidas para los filtros seleccionados.
                  </Alert>
                )}
              </Card.Body>
            </Card>
          </Col>
        </Row>
      )}
    </Container>
  );
};

export default ProductRankingTab;

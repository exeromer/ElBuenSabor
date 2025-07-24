import React, { useState, useEffect, useCallback } from "react";
import { Container, Card, Spinner, Alert, Form, Row, Col, Button, Table, } from "react-bootstrap";
import { EstadisticaService } from "../../services/EstadisticaService";
import type { MovimientosMonetarios } from "../../types/types";
import { useSucursal } from "../../context/SucursalContext";
import { BarChart, Bar, XAxis, YAxis, CartesianGrid, Tooltip, Legend, ResponsiveContainer, } from "recharts";
import { faFileExcel } from "@fortawesome/free-solid-svg-icons";
import { FontAwesomeIcon } from "@fortawesome/react-fontawesome";
import "./MonetaryMovementTab.sass";

const MonetaryMovementTab: React.FC = () => {
  const { selectedSucursal } = useSucursal();
  const [movimientos, setMovimientos] = useState<MovimientosMonetarios | null>(null);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState<string | null>(null);
  const [fechaDesde, setFechaDesde] = useState<string>("");
  const [fechaHasta, setFechaHasta] = useState<string>("");

  const fetchMovimientos = useCallback(async () => {
    if (!selectedSucursal) {
      setError("Por favor, selecciona una sucursal.");
      setLoading(false);
      return;
    }
    setLoading(true);
    setError(null);
    try {
      const params = {
        sucursalId: selectedSucursal.id, // <-- AÑADIR
        fechaDesde: fechaDesde || undefined,
        fechaHasta: fechaHasta || undefined,
      };
      const fetchedMovimientos = await EstadisticaService.getMovimientosMonetarios(params);
      setMovimientos(fetchedMovimientos);
    } catch (err: any) {
      setError(err.message || "Error al cargar los movimientos monetarios.");
    } finally {
      setLoading(false);
    }
  }, [fechaDesde, fechaHasta, selectedSucursal]);
  useEffect(() => {
    fetchMovimientos();
  }, [fetchMovimientos]);

   const handleExport = async () => {
    if (!selectedSucursal) return;
    try {
      const excelBlob = await EstadisticaService.exportMovimientosMonetariosExcel(
          selectedSucursal.id,
          fechaDesde,
          fechaHasta
        );
      const url = window.URL.createObjectURL(new Blob([excelBlob]));
      const link = document.createElement("a");
      link.href = url;
      link.setAttribute("download", `movimientos_monetarios_${selectedSucursal.nombre}.xlsx`);
      document.body.appendChild(link);
      link.click();
      link.remove();
    } catch (err) {
      alert("Error al exportar los movimientos monetarios a Excel.");
      console.error(err);
    }
  };

  const chartData = movimientos
    ? [
      { name: "Ingresos", value: movimientos.ingresosTotales },
      { name: "Costos", value: movimientos.costosTotales },
      { name: "Ganancias", value: movimientos.gananciasNetas },
    ]
    : [];

  return (
    <Container fluid className="monetary-movement-tab">
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
            <Button onClick={fetchMovimientos} disabled={loading}>
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
          <Spinner animation="border" />{" "}
          <p>Cargando movimientos monetarios...</p>
        </div>
      ) : error ? (
        <Alert variant="danger">{error}</Alert>
      ) : (
        <Card className="shadow-sm">
          <Card.Header as="h5">Resumen de Movimientos Monetarios</Card.Header>
          <Card.Body>
            {movimientos ? (
              <>
                <Table striped bordered hover responsive className="mb-4">
                  <tbody>
                    <tr>
                      <th>Ingresos Totales</th>
                      <td>${movimientos.ingresosTotales.toFixed(2)}</td>
                    </tr>
                    <tr>
                      <th>Costos Totales</th>
                      <td>${movimientos.costosTotales.toFixed(2)}</td>
                    </tr>
                    <tr>
                      <th>Ganancias Netas</th>
                      <td>${movimientos.gananciasNetas.toFixed(2)}</td>
                    </tr>
                  </tbody>
                </Table>

                <div style={{ width: "100%", height: 300 }}>
                  <ResponsiveContainer className="recharts-wrapper">
                    <BarChart
                      data={chartData}
                      margin={{ top: 5, right: 30, left: 20, bottom: 5 }}
                    >
                      <CartesianGrid strokeDasharray="3 3" />
                      <XAxis dataKey="name" />
                      <YAxis />
                      <Tooltip
                        formatter={(value: number) => `$${value.toFixed(2)}`}
                      />
                      <Legend />
                      <Bar dataKey="value" fill="#82ca9d" name="Monto ($)" />
                    </BarChart>
                  </ResponsiveContainer>
                </div>

                {/* Botón de exportar a Excel** */}
                <Button
                  variant="success"
                  onClick={handleExport}
                  className="mt-3"
                >
                  <FontAwesomeIcon icon={faFileExcel} className="me-2" />{" "}
                  Exportar a Excel
                </Button>
              </>
            ) : (
              <Alert variant="info">
                No hay datos de movimientos monetarios para mostrar en este
                período.
              </Alert>
            )}
          </Card.Body>
        </Card>
      )}
    </Container>
  );
};

export default MonetaryMovementTab;

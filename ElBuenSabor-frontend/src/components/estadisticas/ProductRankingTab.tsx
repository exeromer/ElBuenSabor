import React, { useState, useEffect, useCallback } from 'react';
import { Container, Card, Spinner, Alert, Form, Row, Col, Button, Table } from 'react-bootstrap';
import { EstadisticaService } from '../../services/EstadisticaService';
import type { ArticuloManufacturadoRanking, ArticuloInsumoRanking } from '../../types/types';
import { BarChart, Bar, XAxis, YAxis, CartesianGrid, Tooltip, Legend, ResponsiveContainer } from 'recharts';
import { format } from 'date-fns';
import { faFileExcel } from '@fortawesome/free-solid-svg-icons';
import { FontAwesomeIcon } from '@fortawesome/react-fontawesome';
// **CÓDIGO NUEVO: Importación del archivo de estilos**
import './ProductRankingTab.sass';

const ProductRankingTab: React.FC = () => {
  const [manufacturedRanking, setManufacturedRanking] = useState<ArticuloManufacturadoRanking[]>([]);
  const [insumoRanking, setInsumoRanking] = useState<ArticuloInsumoRanking[]>([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState<string | null>(null);
  const [fechaDesde, setFechaDesde] = useState<string>('');
  const [fechaHasta, setFechaHasta] = useState<string>('');

  const fetchRankings = useCallback(async () => {
    setLoading(true);
    setError(null);
    try {
      const params = { fechaDesde: fechaDesde || undefined, fechaHasta: fechaHasta || undefined };
      const [manufactured, insumos] = await Promise.all([
        EstadisticaService.getRankingArticulosManufacturadosMasVendidos(params),
        EstadisticaService.getRankingArticulosInsumosMasVendidos(params),
      ]);
      setManufacturedRanking(manufactured);
      setInsumoRanking(insumos);
    } catch (err: any) {
      setError(err.message || 'Error al cargar los rankings de productos.');
    } finally {
      setLoading(false);
    }
  }, [fechaDesde, fechaHasta]);

  useEffect(() => {
    fetchRankings();
  }, [fetchRankings]);

  const handleExportManufactured = async () => {
    try {
      const excelBlob = await EstadisticaService.exportRankingArticulosManufacturadosExcel(fechaDesde, fechaHasta);
      const url = window.URL.createObjectURL(new Blob([excelBlob]));
      const link = document.createElement('a');
      link.href = url;
      link.setAttribute('download', 'ranking_productos_cocina.xlsx');
      document.body.appendChild(link);
      link.click();
      link.remove();
    } catch (err) {
      alert('Error al exportar el ranking de productos de cocina a Excel.');
      console.error(err);
    }
  };

  const handleExportInsumo = async () => {
    try {
      const excelBlob = await EstadisticaService.exportRankingArticulosInsumosExcel(fechaDesde, fechaHasta);
      const url = window.URL.createObjectURL(new Blob([excelBlob]));
      const link = document.createElement('a');
      link.href = url;
      link.setAttribute('download', 'ranking_bebidas.xlsx');
      document.body.appendChild(link);
      link.click();
      link.remove();
    } catch (err) {
      alert('Error al exportar el ranking de bebidas a Excel.');
      console.error(err);
    }
  };

  return (
    // **CÓDIGO MODIFICADO: Aplicación de la clase CSS principal**
    <Container fluid className="product-ranking-tab">
      <Card className="shadow-sm mb-4">
        <Card.Header as="h5">Filtro por Fechas</Card.Header>
        <Card.Body>
          <Form>
            <Row>
              <Col md={6}>
                <Form.Group className="mb-3">
                  <Form.Label>Fecha Desde:</Form.Label>
                  <Form.Control type="date" value={fechaDesde} onChange={(e) => setFechaDesde(e.target.value)} />
                </Form.Group>
              </Col>
              <Col md={6}>
                <Form.Group className="mb-3">
                  <Form.Label>Fecha Hasta:</Form.Label>
                  <Form.Control type="date" value={fechaHasta} onChange={(e) => setFechaHasta(e.target.value)} />
                </Form.Group>
              </Col>
            </Row>
            <Button onClick={fetchRankings} disabled={loading}>
              {loading ? <Spinner as="span" animation="border" size="sm" /> : 'Aplicar Filtro'}
            </Button>
          </Form>
        </Card.Body>
      </Card>

      {loading ? (
        <div className="text-center my-3"><Spinner animation="border" /> <p>Cargando rankings...</p></div>
      ) : error ? (
        <Alert variant="danger">{error}</Alert>
      ) : (
        <Row>
          <Col lg={6}>
            <Card className="shadow-sm mb-4">
              <Card.Header as="h5">Productos de Cocina (Manufacturados) Más Vendidos</Card.Header>
              <Card.Body>
                {manufacturedRanking.length > 0 ? (
                  <>
                    <div style={{ width: '100%', height: 300 }}>
                      {/* **CÓDIGO MODIFICADO: Aplicación de la clase CSS para el gráfico** */}
                      <ResponsiveContainer className="recharts-wrapper">
                        <BarChart
                          data={manufacturedRanking}
                          margin={{ top: 5, right: 30, left: 20, bottom: 5 }}
                        >
                          <CartesianGrid strokeDasharray="3 3" />
                          <XAxis dataKey="denominacion" angle={-30} textAnchor="end" height={80} interval={0} />
                          <YAxis />
                          <Tooltip />
                          <Legend />
                          <Bar dataKey="cantidadVendida" fill="#8884d8" name="Cantidad Vendida" />
                        </BarChart>
                      </ResponsiveContainer>
                    </div>
                    {/* **CÓDIGO MODIFICADO: Aplicación de la clase CSS para la tabla** */}
                    <Table striped bordered hover responsive className="mt-3">
                      <thead>
                        <tr>
                          <th>Denominación</th>
                          <th>Cantidad Vendida</th>
                        </tr>
                      </thead>
                      <tbody>
                        {manufacturedRanking.map((item, index) => (
                          <tr key={index}>
                            <td>{item.denominacion}</td>
                            <td>{item.cantidadVendida}</td>
                          </tr>
                        ))}
                      </tbody>
                    </Table>
                    <Button variant="success" onClick={handleExportManufactured} className="mt-3">
                      <FontAwesomeIcon icon={faFileExcel} className="me-2" /> Exportar a Excel
                    </Button>
                  </>
                ) : (
                  <Alert variant="info">No hay productos manufacturados vendidos en este período.</Alert>
                )}
              </Card.Body>
            </Card>
          </Col>

          <Col lg={6}>
            <Card className="shadow-sm mb-4">
              <Card.Header as="h5">Bebidas/Insumos Directos Más Vendidos</Card.Header>
              <Card.Body>
                {insumoRanking.length > 0 ? (
                  <>
                    <div style={{ width: '100%', height: 300 }}>
                      {/* **CÓDIGO MODIFICADO: Aplicación de la clase CSS para el gráfico** */}
                      <ResponsiveContainer className="recharts-wrapper">
                        <BarChart
                          data={insumoRanking}
                          margin={{ top: 5, right: 30, left: 20, bottom: 5 }}
                        >
                          <CartesianGrid strokeDasharray="3 3" />
                          <XAxis dataKey="denominacion" angle={-30} textAnchor="end" height={80} interval={0} />
                          <YAxis />
                          <Tooltip />
                          <Legend />
                          <Bar dataKey="cantidadVendida" fill="#82ca9d" name="Cantidad Vendida" />
                        </BarChart>
                      </ResponsiveContainer>
                    </div>
                    {/* **CÓDIGO MODIFICADO: Aplicación de la clase CSS para la tabla** */}
                    <Table striped bordered hover responsive className="mt-3">
                      <thead>
                        <tr>
                          <th>Denominación</th>
                          <th>Cantidad Vendida</th>
                        </tr>
                      </thead>
                      <tbody>
                        {insumoRanking.map((item, index) => (
                          <tr key={index}>
                            <td>{item.denominacion}</td>
                            <td>{item.cantidadVendida}</td>
                          </tr>
                        ))}
                      </tbody>
                    </Table>
                    <Button variant="success" onClick={handleExportInsumo} className="mt-3">
                      <FontAwesomeIcon icon={faFileExcel} className="me-2" /> Exportar a Excel
                    </Button>
                  </>
                ) : (
                  <Alert variant="info">No hay insumos directos (bebidas) vendidos en este período.</Alert>
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
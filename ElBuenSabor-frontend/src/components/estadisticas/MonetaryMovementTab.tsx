import React, { useState, useEffect, useCallback } from 'react';
import { Container, Card, Spinner, Alert, Form, Row, Col, Button, Table } from 'react-bootstrap';
import { EstadisticaService } from '../../services/EstadisticaService';
import type { MovimientosMonetarios } from '../../types/types';
import { useSucursal } from '../../context/SucursalContext';
import { BarChart, Bar, XAxis, YAxis, CartesianGrid, Tooltip, ResponsiveContainer, Cell } from 'recharts';
import { faFileExcel } from '@fortawesome/free-solid-svg-icons';
import { FontAwesomeIcon } from '@fortawesome/react-fontawesome';
import './MonetaryMovementTab.sass';

const MonetaryMovementTab: React.FC = () => {
  const { selectedSucursal } = useSucursal();
  const [movimientos, setMovimientos] = useState<MovimientosMonetarios | null>(null);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState<string | null>(null);
  const [fechaDesde, setFechaDesde] = useState<string>('');
  const [fechaHasta, setFechaHasta] = useState<string>('');
  const [isExporting, setIsExporting] = useState(false);

  const fetchMovimientos = useCallback(async () => {
    if (!selectedSucursal) {
      setError('Por favor, selecciona una sucursal.');
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
      setError(err.message || 'Error al cargar los movimientos monetarios.');
    } finally {
      setLoading(false);
    }
  }, [fechaDesde, fechaHasta, selectedSucursal]);
  useEffect(() => {
    fetchMovimientos();
  }, [fetchMovimientos]);

  const handleExport = async () => {
    if (!selectedSucursal) {
      alert('Por favor, selecciona una sucursal.');
      return;
    }
    setIsExporting(true);

    try {
      // 1. Llamar al servicio, que devuelve un ArrayBuffer
      const excelArrayBuffer = await EstadisticaService.exportMovimientosMonetariosExcel(
        selectedSucursal.id,
        fechaDesde,
        fechaHasta,
      );

      // 2. Crear un Blob desde el ArrayBuffer, especificando el tipo MIME de Excel.
      // Este paso es crucial para que el navegador sepa qué tipo de archivo es.
      const blob = new Blob([excelArrayBuffer], {
        type: 'application/vnd.openxmlformats-officedocument.spreadsheetml.sheet',
      });

      // 3. Crear una URL temporal para el Blob
      const url = window.URL.createObjectURL(blob);

      // 4. Crear un enlace <a> invisible para iniciar la descarga
      const link = document.createElement('a');
      link.href = url;
      link.setAttribute('download', `movimientos_monetarios_${selectedSucursal.nombre}.xlsx`);

      // 5. Añadir, hacer clic y remover el enlace
      document.body.appendChild(link);
      link.click();
      document.body.removeChild(link);

      // 6. Limpiar la URL temporal
      window.URL.revokeObjectURL(url);
    } catch (err) {
      console.error('Error al exportar a Excel:', err);
      alert('Hubo un error al generar el archivo Excel.');
    } finally {
      setIsExporting(false); // <-- Desactiva el estado de carga (incluso si hay error)
    }
  };

  const chartData = movimientos
    ? [
        { name: 'Ingresos', value: movimientos.ingresosTotales },
        { name: 'Costos', value: movimientos.costosTotales },
        { name: 'Ganancias', value: movimientos.gananciasNetas },
      ]
    : [];
  const COLORS = ['#0088FE', '#db1507', '#07db0e'];

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
            <Button onClick={fetchMovimientos} disabled={loading}>
              {loading ? <Spinner as="span" animation="border" size="sm" /> : 'Aplicar Filtro'}
            </Button>
          </Form>
        </Card.Body>
      </Card>

      {loading ? (
        <div className="text-center my-3">
          <Spinner animation="border" /> <p>Cargando movimientos monetarios...</p>
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
                      <td className="ingresos-color">${movimientos.ingresosTotales.toFixed(2)}</td>
                    </tr>
                    <tr>
                      <th>Costos Totales</th>
                      <td className="costos-color">${movimientos.costosTotales.toFixed(2)}</td>
                    </tr>
                    <tr>
                      <th>Ganancias Netas</th>
                      <td className="ganancias-color">${movimientos.gananciasNetas.toFixed(2)}</td>
                    </tr>
                  </tbody>
                </Table>

                <div style={{ width: '100%', height: 300 }}>
                  <ResponsiveContainer className="recharts-wrapper">
                    <BarChart data={chartData} margin={{ top: 5, right: 30, left: 20, bottom: 5 }}>
                      <CartesianGrid strokeDasharray="3 3" />
                      <XAxis dataKey="name" />
                      <YAxis />
                      <Tooltip formatter={(value: number) => `$${value.toFixed(2)}`} />

                      <Bar dataKey="value" name="Monto ($)" maxBarSize={90}>
                        {' '}
                        {chartData.map((_, index) => (
                          <Cell key={`cell-${index}`} fill={COLORS[index % COLORS.length]} />
                        ))}
                      </Bar>
                    </BarChart>
                  </ResponsiveContainer>
                </div>

                {/* Botón de exportar a Excel** */}
                <Button variant="success" onClick={handleExport} className="mt-3" disabled={isExporting}>
                  {isExporting ? (
                    <>
                      <Spinner as="span" animation="border" size="sm" role="status" aria-hidden="true" />
                      <span className="ms-2">Exportando...</span>
                    </>
                  ) : (
                    <>
                      <FontAwesomeIcon icon={faFileExcel} className="me-2" /> Exportar a Excel
                    </>
                  )}
                </Button>
              </>
            ) : (
              <Alert variant="info">No hay datos de movimientos monetarios para mostrar en este período.</Alert>
            )}
          </Card.Body>
        </Card>
      )}
    </Container>
  );
};

export default MonetaryMovementTab;

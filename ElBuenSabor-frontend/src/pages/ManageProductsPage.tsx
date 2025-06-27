/**
 * @file ManageProductsPage.tsx
 * @description Página de administración para la gestión de Artículos (Artículos Manufacturados y Artículos Insumo).
 * Permite a los usuarios (administradores/empleados) ver un listado de ambos tipos de artículos
 * en pestañas separadas, y realizar operaciones de Creación, Edición, Visualización y Eliminación (CRUD).
 * Utiliza modales de formulario (`ArticuloInsumoForm`, `ArticuloManufacturadoForm`) para las operaciones de C/E
 * y modales de detalle (`ArticuloInsumoDetailModal`, `ArticuloManufacturadoDetailModal`) para la visualización.
 *
 * @hook `useState`: Gestiona los listados de artículos, estados de carga/error, la pestaña activa,
 * términos de búsqueda, y la visibilidad/modo de los modales.
 * @hook `useEffect`: Carga los artículos correspondientes a la pestaña activa o cuando cambia el término de búsqueda.
 * @hook `useCallback`: Para memoizar las funciones de carga de datos y debounce.
 * @hook `useAuth0`: Para obtener el token de autenticación necesario para las operaciones protegidas del API.
 *
 * @service `ArticuloInsumoService`: Servicios para Artículos Insumo.
 * @service `ArticuloManufacturadoService`: Servicios para Artículos Manufacturados.
 *
 * @component `ArticuloInsumoForm`, `ArticuloManufacturadoForm`: Modales de formulario.
 * @component `ArticuloInsumoDetailModal`, `ArticuloManufacturadoDetailModal`: Modales de detalle.
 */
import React, { useState, useCallback, useEffect } from 'react';
import { Container, Tabs, Tab, Button, Badge, Card, Form, Row, Col } from 'react-bootstrap'; // Se añade Form, Row, Col
//import { useAuth0 } from '@auth0/auth0-react';

// Servicios
import { ArticuloInsumoService } from '../services/articuloInsumoService';
import { ArticuloManufacturadoService } from '../services/articuloManufacturadoService';
import { StockInsumoSucursalService } from '../services/StockInsumoSucursalService';
import { CategoriaService } from '../services/categoriaService';
import { SucursalService } from '../services/sucursalService';

// Tipos
import type { ArticuloManufacturadoResponse, ArticuloInsumoResponse, CategoriaResponse } from '../types/types';

// Tabla Genérica y Hook
import { SearchableTable, type ColumnDefinition } from '../components/common/Tables/SearchableTable';
import { useSearchableData } from '../hooks/useSearchableData';
import { useSucursal } from '../context/SucursalContext';

// Iconos
import { FontAwesomeIcon } from '@fortawesome/react-fontawesome';
import { faEdit, faTrash, faBoxOpen, faTools, faEye, faExclamationTriangle, faCheckCircle } from '@fortawesome/free-solid-svg-icons';

// Componentes de UI anidados
import ArticuloInsumoForm from '../components/admin/ArticuloInsumoForm';
import ArticuloManufacturadoForm from '../components/admin/ArticuloManufacturadoForm';
import ArticuloManufacturadoDetailModal from '../components/admin/ArticuloManufacturadoDetailModal';
import ArticuloInsumoDetailModal from '../components/admin/ArticuloInsumoDetailModal';
import CategoriaForm from '../components/admin/CategoriaForm';


// INSTANCIAS DE SERVICIOS
interface InsumoConStock extends ArticuloInsumoResponse {
  stockActualSucursal?: number;
  stockMinimoSucursal?: number;
}

// ------ COMPONENTE PRINCIPAL ------

const ManageProductsPage: React.FC = () => {
  //  const { getAccessTokenSilently } = useAuth0();
  const { selectedSucursal, reloadSucursales } = useSucursal();
  const [activeTab, setActiveTab] = useState<'manufacturados' | 'insumos' | 'categorias'>('manufacturados');

  // --- ESTADOS Y MANEJADORES PARA MODALES (Común a ambas pestañas) ---
  // Estos estados se quedan en ManageProductsPage porque los modales se renderizan aquí.

  // Para ArticuloInsumo
  const [showInsumoForm, setShowInsumoForm] = useState(false);
  const [editingInsumo, setEditingInsumo] = useState<ArticuloInsumoResponse | null>(null);
  const [showInsumoDetailModal, setShowInsumoDetailModal] = useState(false);
  const [selectedInsumoForDetail, setSelectedInsumoForDetail] = useState<ArticuloInsumoResponse | null>(null);

  // Para ArticuloManufacturado
  const [showManufacturadoForm, setShowManufacturadoForm] = useState(false);
  const [editingManufacturado, setEditingManufacturado] = useState<ArticuloManufacturadoResponse | null>(null);
  const [showManufacturadoDetailModal, setShowManufacturadoDetailModal] = useState(false);
  const [selectedManufacturadoForDetail, setSelectedManufacturadoForDetail] = useState<ArticuloManufacturadoResponse | null>(null);
  const [filtroCategoriaManuf, setFiltroCategoriaManuf] = useState<number | ''>('');

  // Para Categorias
  const [showCategoriaForm, setShowCategoriaForm] = useState(false);
  const [editingCategoria, setEditingCategoria] = useState<CategoriaResponse | null>(null);

  //  -- Lógica para Artículos Insumo con el Hook ---
  const fetchInsumosConStock = useCallback(async (searchTerm: string) => {
    if (!selectedSucursal) return []; // Si no hay sucursal, no cargamos nada
    const insumosBase = await ArticuloInsumoService.getAll(searchTerm);
    const insumosConStockPromises = insumosBase.map(async (insumo) => {
      try {
        const stockInfo = await StockInsumoSucursalService.getStockByInsumoAndSucursal(insumo.id, selectedSucursal.id);
        return { ...insumo, stockActualSucursal: stockInfo?.stockActual, stockMinimoSucursal: stockInfo?.stockMinimo };
      } catch {
        return { ...insumo, stockActualSucursal: 0, stockMinimoSucursal: 0 };
      }
    });
    return Promise.all(insumosConStockPromises);
  }, [selectedSucursal]);

  const insumosData = useSearchableData<InsumoConStock>({ fetchData: fetchInsumosConStock });

  // --- Lógica para Artículos Manufacturados con el Hook ---
  /**
  * @function fetchManufacturadosFunction
  * @description Función envuelta en useCallback que llama al servicio para obtener manufacturados.
  */
  const fetchManufacturadosPorSucursal = useCallback(async (searchTerm: string) => {
    if (!selectedSucursal) return [];
    const todosLosManufacturados = await ArticuloManufacturadoService.getAll(searchTerm);
    const idsCategoriasSucursal = selectedSucursal.categorias.map(c => c.id);
    return todosLosManufacturados.filter(p => idsCategoriasSucursal.includes(p.categoria.id));
  }, [selectedSucursal]);

  const manufacturadosData = useSearchableData<ArticuloManufacturadoResponse>({ fetchData: fetchManufacturadosPorSucursal });

  //  -- Lógica para Categorias sin el Hook ---
  const fetchCategorias = useCallback((_: string) => {
    return CategoriaService.getAll();
  }, []);
  const categoriasData = useSearchableData<CategoriaResponse>({ fetchData: fetchCategorias });


  // --- FILTRADO LOCAL DE DATOS ---
  const manufacturadosFiltrados = filtroCategoriaManuf
    ? manufacturadosData.items.filter(m => m.categoria.id === filtroCategoriaManuf)
    : manufacturadosData.items;

  useEffect(() => {
    if (activeTab === 'insumos') insumosData.reload();
    else if (activeTab === 'manufacturados') manufacturadosData.reload();
    else if (activeTab === 'categorias') categoriasData.reload();
  }, [selectedSucursal, activeTab]);

  // --- MANEJADORES DE ACCIONES ---
  /**
   * @function handleOpenInsumoForm
   * @description Abre el modal de formulario para ArticuloInsumo.
   */
  const handleOpenInsumoForm = (insumo: ArticuloInsumoResponse | null) => { setEditingInsumo(insumo); setShowInsumoForm(true); };
  /**
   * @function handleViewInsumo
   * @description Abre el modal para ver los detalles de un ArticuloInsumo.
   */
  const handleViewInsumo = (insumo: ArticuloInsumoResponse) => { setSelectedInsumoForDetail(insumo); setShowInsumoDetailModal(true); };
  /**
   * @function handleDeleteInsumo
   * @description Maneja la eliminación de un ArticuloInsumo.
   */
  const handleDeleteInsumo = async (id: number) => {
    if (window.confirm(`¿Seguro que quieres eliminar el insumo ID ${id}?`)) {
      try {
        await ArticuloInsumoService.delete(id);
        insumosData.reload();
      } catch (err) { alert(`Error al eliminar: ${err}`); }
    }
  };

  /**
   * @function handleOpenManufacturadoForm
   * @description Abre el modal de formulario para ArticuloManufacturado.
   */
  const handleOpenManufacturadoForm = (mf: ArticuloManufacturadoResponse | null) => { setEditingManufacturado(mf); setShowManufacturadoForm(true); };
  /**
   * @function handleViewManufacturado
   * @description Abre el modal para ver los detalles de un ArticuloManufacturado.
   */
  const handleViewManufacturado = (mf: ArticuloManufacturadoResponse) => { setSelectedManufacturadoForDetail(mf); setShowManufacturadoDetailModal(true); };
  /**
 * @function handleDeleteManufacturado
 * @description Maneja la eliminación de un ArticuloManufacturado.
 */
  const handleDeleteManufacturado = async (id: number) => {
    if (window.confirm(`¿Seguro que quieres eliminar el manufacturado ID ${id}?`)) {
      try {
        await ArticuloManufacturadoService.delete(id);
        manufacturadosData.reload();
      } catch (err) { alert(`Error al eliminar: ${err}`); }
    }
  };

  /**
 * @function handleFormSubmit
 * @description Callback para cuando un formulario se guarda.
 */
  const handleFormSubmit = () => {
    setShowInsumoForm(false);
    setShowManufacturadoForm(false);
    setShowCategoriaForm(false);
    if (activeTab === 'categorias') {
      categoriasData.reload();
      reloadSucursales();
    } else if (activeTab === 'insumos') {
      insumosData.reload();
    } else {
      manufacturadosData.reload();
    }
  };

  const handleOpenCategoriaForm = (categoria: CategoriaResponse | null) => {
    setEditingCategoria(categoria);
    setShowCategoriaForm(true);
  };

  const handleAsociarDesasociar = async (categoriaId: number, estaAsociada: boolean) => {
    if (!selectedSucursal) return;
    const action = estaAsociada ? 'desasociar' : 'asociar';
    if (window.confirm(`¿Seguro que quieres ${action} esta categoría de la sucursal ${selectedSucursal.nombre}?`)) {
      try {
        if (estaAsociada) {
          await SucursalService.desasociarCategoria(selectedSucursal.id, categoriaId);
        } else {
          await SucursalService.asociarCategoria(selectedSucursal.id, categoriaId);
        }
        // FIX: En lugar de recargar la página, recargamos solo los datos del contexto
        await reloadSucursales();
        // Recargamos también los datos de la tabla de categorías para reflejar cualquier cambio
        categoriasData.reload();
      } catch (err) {
        alert(`Error al ${action} la categoría.`);
      }
    }
  };

  const handleToggleEstadoCategoria = async (categoria: CategoriaResponse) => {
    if (window.confirm(`¿Seguro que quieres ${categoria.estadoActivo ? 'desactivar' : 'activar'} la categoría ${categoria.denominacion}?`)) {
      try {
        await CategoriaService.update(categoria.id, {
          denominacion: categoria.denominacion,
          estadoActivo: !categoria.estadoActivo,
        });
        categoriasData.reload();
        reloadSucursales();
      } catch (err) { alert('Error al cambiar el estado.'); }
    }
  };

  // --- DEFINICIÓN DE COLUMNAS  ---
  /**
     * @constant insumoColumns
     * @description Definición de columnas para la tabla de Artículos Insumo.
     */
  const insumoColumns: ColumnDefinition<InsumoConStock>[] = [
    {
      key: 'id',
      header: 'Alerta',
      renderCell: (insumo) => {
        const stockActual = insumo.stockActualSucursal ?? 0;
        const stockMinimo = insumo.stockMinimoSucursal ?? 0;
        if (stockMinimo > 0 && stockActual <= stockMinimo) {
          return <FontAwesomeIcon icon={faExclamationTriangle} style={{ color: "red" }} title={`Stock Insuficiente. Actual: ${stockActual}, Mínimo: ${stockMinimo}`} />;
        }
        if (stockMinimo > 0 && stockActual <= stockMinimo * 1.2) {
          return <FontAwesomeIcon icon={faExclamationTriangle} style={{ color: "orange" }} title={`Stock bajo. Actual: ${stockActual}, Mínimo: ${stockMinimo}`} />;
        }
        return <FontAwesomeIcon icon={faCheckCircle} style={{ color: "green" }} title="Stock OK" />;
      }
    },
    { key: 'denominacion', header: 'Denominación', renderCell: (i) => i.denominacion, sortable: true },
    { key: 'stockActualSucursal', header: 'Stock Sucursal', renderCell: (i) => `${i.stockActualSucursal ?? 'N/A'} / ${i.stockMinimoSucursal ?? 'N/A'}` },
    { key: 'esParaElaborar', header: 'Para Elaborar', renderCell: (i) => i.esParaElaborar ? 'Si' : 'No' },
    { key: 'estadoActivo', header: 'Estado', renderCell: (i) => <Badge bg={i.estadoActivo ? 'success' : 'danger'}>{i.estadoActivo ? 'Activo' : 'Inactivo'}</Badge> },
  ];

  // Definición de columnas para la tabla de ArticuloManufacturado
  const manufacturadoColumns: ColumnDefinition<ArticuloManufacturadoResponse>[] = [
    { key: 'id', header: 'ID', renderCell: (am) => am.id, sortable: true },
    { key: 'denominacion', header: 'Denominación', renderCell: (am) => am.denominacion, sortable: true },
    { key: 'precioVenta', header: 'Precio', renderCell: (am) => `$${am.precioVenta.toFixed(2)}` },
    { key: 'categoria', header: 'Categoría', renderCell: (am) => am.categoria.denominacion },
    { key: 'estadoActivo', header: 'Estado', renderCell: (am) => <Badge bg={am.estadoActivo ? 'success' : 'danger'}>{am.estadoActivo ? 'Activo' : 'Inactivo'}</Badge> },
  ];

  // Definicion de columnas para la tabla de Categorias
  const categoriaColumns: ColumnDefinition<CategoriaResponse>[] = [
    { key: 'id', header: 'ID', renderCell: (c) => c.id },
    { key: 'denominacion', header: 'Denominación', renderCell: (c) => c.denominacion },
    { key: 'estadoActivo', header: 'Estado Global', renderCell: (c) => <Badge bg={c.estadoActivo ? 'success' : 'danger'}>{c.estadoActivo ? 'Activa' : 'Inactiva'}</Badge> },
    {
      key: 'asociadaEnSucursal',
      header: 'En Sucursal Actual',
      renderCell: (categoria) => {
        if (!selectedSucursal) return <Badge bg="secondary">Seleccione Sucursal</Badge>;
        const estaAsociada = selectedSucursal.categorias.some(cs => cs.id === categoria.id);
        return <Badge bg={estaAsociada ? 'success' : 'secondary'}>{estaAsociada ? 'Sí' : 'No'}</Badge>;
      }
    },
  ];

  /**
     * @function renderInsumoActions
     * @description Renderiza los botones de acción para cada fila de la tabla de insumos.
     */
  const renderInsumoActions = (insumo: InsumoConStock, reloadData: () => void) => (
    <>
      <Button variant="secondary" size="sm" className="me-1" onClick={() => handleViewInsumo(insumo)} title="Ver Detalles"><FontAwesomeIcon icon={faEye} /></Button>
      <Button variant="info" size="sm" className="me-1" onClick={() => handleOpenInsumoForm(insumo)} title="Editar / Compra"><FontAwesomeIcon icon={faEdit} /></Button>
      <Button
        variant="danger"
        size="sm"
        onClick={async () => {
          if (insumo.id !== undefined) {
            await handleDeleteInsumo(insumo.id);
            reloadData();
          } else {
            alert('Error: ID de insumo no disponible para eliminar.');
          }
        }}
        title="Eliminar"
      ><FontAwesomeIcon icon={faTrash} /></Button>
    </>
  );
  const renderManufacturadoActions = (manufacturado: ArticuloManufacturadoResponse, reloadData: () => void) => (
    <>
      <Button variant="secondary" size="sm" className="me-1" onClick={() => handleViewManufacturado(manufacturado)} title="Ver Detalles"><FontAwesomeIcon icon={faEye} /></Button>
      <Button variant="info" size="sm" className="me-1" onClick={() => handleOpenManufacturadoForm(manufacturado)} title="Editar"><FontAwesomeIcon icon={faEdit} /></Button>
      <Button
        variant="danger"
        size="sm"
        onClick={async () => {
          if (manufacturado.id !== undefined) {
            await handleDeleteManufacturado(manufacturado.id);
            reloadData();
          } else {
            alert('Error: ID de manufacturado no disponible.');
          }
        }}
        title="Eliminar"
      ><FontAwesomeIcon icon={faTrash} /></Button>
    </>
  );
  const renderCategoriaActions = (categoria: CategoriaResponse) => {
    if (!selectedSucursal) return null;
    const estaAsociada = selectedSucursal.categorias.some(cs => cs.id === categoria.id);
    return (
      <>
        <Button variant={estaAsociada ? "warning" : "success"} size="sm" className="me-1" onClick={() => handleAsociarDesasociar(categoria.id, estaAsociada)}>
          {estaAsociada ? 'Desasociar' : 'Asociar'}
        </Button>
        <Button variant="info" size="sm" className="me-1" onClick={() => handleOpenCategoriaForm(categoria)}>Editar</Button>
        <Button variant={categoria.estadoActivo ? "danger" : "success"} size="sm" onClick={() => handleToggleEstadoCategoria(categoria)}>
          {categoria.estadoActivo ? 'Desactivar' : 'Activar'}
        </Button>
      </>
    );
  };

  const handleTabSelect = (key: string | null) => {
    if (key) setActiveTab(key as any);
  };


  return (
    <Container className="my-4">
      <h1 className="text-center mb-4">Gestión de Artículos</h1>
      <Tabs activeKey={activeTab} onSelect={handleTabSelect} className="mb-3 justify-content-center">
        {/* PESTAÑA DE MANUFACTURADOS */}
        <Tab eventKey="manufacturados" title={<span><FontAwesomeIcon icon={faBoxOpen} /> Art. Manufacturados</span>}>
          <Card className="shadow-sm">
            <Card.Body>
              {/* FIX: Filtro por categoría para manufacturados */}
              <Form.Group as={Row} className="mb-3 align-items-center">
                <Form.Label column sm={2} className="fw-bold">Filtrar por Categoría:</Form.Label>
                <Col sm={4}>
                  <Form.Select onChange={(e) => setFiltroCategoriaManuf(Number(e.target.value) || '')} value={filtroCategoriaManuf} disabled={!selectedSucursal}>
                    <option value="">Todas</option>
                    {selectedSucursal?.categorias.map(c => <option key={c.id} value={c.id}>{c.denominacion}</option>)}
                  </Form.Select>
                </Col>
              </Form.Group>
              <hr />
              <SearchableTable
                {...manufacturadosData}
                items={manufacturadosFiltrados}
                columns={manufacturadoColumns}
                renderRowActions={(item) => renderManufacturadoActions(item, manufacturadosData.reload)}
                createButtonText="Nuevo Manufacturado"
                onCreate={() => handleOpenManufacturadoForm(null)}
              />
            </Card.Body>
          </Card>
        </Tab>

        {/* PESTAÑA DE INSUMOS */}
        <Tab eventKey="insumos" title={<span><FontAwesomeIcon icon={faTools} /> Art. Insumo</span>}>
          <Card className="shadow-sm">
            <Card.Body>
              {/*Filtro por categoría para insumos */}
              <SearchableTable
                {...insumosData}
                columns={insumoColumns}
                renderRowActions={(item) => renderInsumoActions(item, insumosData.reload)}
                createButtonText="Nuevo Insumo"
                onCreate={() => handleOpenInsumoForm(null)}
              />
            </Card.Body>
          </Card>
        </Tab>
        {/* Nueva pestaña para Categorías */}
        <Tab eventKey="categorias" title={<span><FontAwesomeIcon icon={faTools} /> Categorías</span>}>
          <Card className="shadow-sm">
            <Card.Body>
              <SearchableTable
                {...categoriasData}
                columns={categoriaColumns}
                renderRowActions={(item) => renderCategoriaActions(item)}
                createButtonText="Nueva Categoría"
                onCreate={() => handleOpenCategoriaForm(null)}
              />
            </Card.Body>
          </Card>
        </Tab>
      </Tabs>
      {/* Modales */}
      <ArticuloInsumoForm show={showInsumoForm} handleClose={() => setShowInsumoForm(false)} onSave={handleFormSubmit} articuloToEdit={editingInsumo} />
      <ArticuloManufacturadoForm show={showManufacturadoForm} handleClose={() => setShowManufacturadoForm(false)} onSave={handleFormSubmit} articuloToEdit={editingManufacturado} />
      <ArticuloManufacturadoDetailModal show={showManufacturadoDetailModal} handleClose={() => setShowManufacturadoDetailModal(false)} articulo={selectedManufacturadoForDetail} />
      <ArticuloInsumoDetailModal show={showInsumoDetailModal} handleClose={() => setShowInsumoDetailModal(false)} articulo={selectedInsumoForDetail} />
      <CategoriaForm show={showCategoriaForm} handleClose={() => setShowCategoriaForm(false)} onSave={handleFormSubmit} categoriaToEdit={editingCategoria} />
    </Container>
  );
};

export default ManageProductsPage;
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
import { Container, Tabs, Tab, Button, Badge, Card, Form, Row, Col } from 'react-bootstrap';
import { ArticuloInsumoService } from '../services/articuloInsumoService';
import { ArticuloManufacturadoService } from '../services/ArticuloManufacturadoService';
import { StockInsumoSucursalService } from '../services/StockInsumoSucursalService';
import { CategoriaService } from '../services/categoriaService';
import { SucursalService } from '../services/sucursalService';
import { PromocionService } from '../services/PromocionService';
import type { ArticuloManufacturadoResponse, ArticuloInsumoResponse, CategoriaResponse, PromocionResponse } from '../types/types';
import { SearchableTable, type ColumnDefinition } from '../components/common/Tables/SearchableTable';
import { useSearchableData } from '../hooks/useSearchableData';
import { useSucursal } from '../context/SucursalContext';
import { FontAwesomeIcon } from '@fortawesome/react-fontawesome';
import { faEdit, faTrash, faBoxOpen, faTools, faEye, faExclamationTriangle, faCheckCircle, faTags } from '@fortawesome/free-solid-svg-icons';
import ArticuloInsumoForm from '../components/admin/ArticuloInsumoForm';
import ArticuloManufacturadoForm from '../components/admin/ArticuloManufacturadoForm';
import ArticuloManufacturadoDetailModal from '../components/admin/ArticuloManufacturadoDetailModal';
import ArticuloInsumoDetailModal from '../components/admin/ArticuloInsumoDetailModal';
import CategoriaForm from '../components/admin/CategoriaForm';
import PromocionForm from '../components/admin/PromocionForm';
import PromocionDetailModal from '../components/promociones/DetalleModal/DetallePromo';
import Titulo from '../components/utils/Titulo/Titulo';

interface InsumoConStock extends ArticuloInsumoResponse {
  stockActualSucursal?: number;
  stockMinimoSucursal?: number;
}

const ManageProductsPage: React.FC = () => {
  const { selectedSucursal, reloadSucursales } = useSucursal();
  const [activeTab, setActiveTab] = useState<'manufacturados' | 'insumos' | 'categorias' | 'promociones'>('manufacturados');

  // ESTADOS Y MANEJADORES PARA MODALES
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

  // Para Promociones
  const [showPromocionForm, setShowPromocionForm] = useState(false);
  const [editingPromocion, setEditingPromocion] = useState<PromocionResponse | null>(null);
  const [showPromocionDetailModal, setShowPromocionDetailModal] = useState(false);
  const [selectedPromocionForDetail, setSelectedPromocionForDetail] = useState<PromocionResponse | null>(null);


  // Lógica para Artículos Insumo
  const fetchInsumosConStock = useCallback(async (searchTerm: string) => {
    if (!selectedSucursal) return [];
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

  // Lógica para Artículos Manufacturados
  const fetchManufacturadosPorSucursal = useCallback(async (searchTerm: string) => {
    if (!selectedSucursal) return [];
    const todosLosManufacturados = await ArticuloManufacturadoService.getAll(searchTerm);
    const idsCategoriasSucursal = selectedSucursal.categorias.map(c => c.id);
    return todosLosManufacturados.filter(p => idsCategoriasSucursal.includes(p.categoria.id));
  }, [selectedSucursal]);

  const manufacturadosData = useSearchableData<ArticuloManufacturadoResponse>({ fetchData: fetchManufacturadosPorSucursal });

  // Lógica para Categorias 
  const fetchCategorias = useCallback((_: string) => {
    return CategoriaService.getAll();
  }, []);
  const categoriasData = useSearchableData<CategoriaResponse>({ fetchData: fetchCategorias });

  // Lógica para Promociones
  const fetchPromocionesPorSucursal = useCallback(async (_: string) => {
    if (!selectedSucursal || !selectedSucursal.id) {
      console.warn('Búsqueda de promociones OMITIDA: La sucursal o su ID no son válidos.');
      return [];
    }
    const todasLasPromos = await PromocionService.getAll();
    const promosDeLaSucursal = todasLasPromos.filter(promo =>
      promo.sucursales.some(suc => suc.id === selectedSucursal.id)
    );

    return promosDeLaSucursal;
  }, [selectedSucursal]);

  const promocionesData = useSearchableData<PromocionResponse>({ fetchData: fetchPromocionesPorSucursal });

  // FILTRADO LOCAL DE DATOS
  const manufacturadosFiltrados = filtroCategoriaManuf
    ? manufacturadosData.items.filter(m => m.categoria.id === filtroCategoriaManuf)
    : manufacturadosData.items;

  useEffect(() => {
    if (activeTab === 'insumos') insumosData.reload();
    else if (activeTab === 'manufacturados') manufacturadosData.reload();
    else if (activeTab === 'categorias') categoriasData.reload();
    else if (activeTab === 'promociones') promocionesData.reload();
  }, [selectedSucursal, activeTab]);

  // MANEJADORES DE ACCIONES
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
    } else if (activeTab === 'manufacturados') {
      manufacturadosData.reload();
    } else if (activeTab === 'promociones') promocionesData.reload();
    setShowPromocionForm(false);
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
        await reloadSucursales();
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

  const handleOpenPromocionForm = (promocion: PromocionResponse | null) => {
    setEditingPromocion(promocion);
    setShowPromocionForm(true);
  };
  const handleViewPromocion = (promocion: PromocionResponse) => {
    setSelectedPromocionForDetail(promocion);
    setShowPromocionDetailModal(true);
  };

  const handleTogglePromocionState = async (promocion: PromocionResponse) => {
    const action = promocion.estadoActivo ? 'desactivar' : 'activar';
    if (window.confirm(`¿Seguro que quieres ${action} la promoción "${promocion.denominacion}"?`)) {
      try {
        // Creamos el objeto de actualización con todos los datos requeridos
        const promocionRequest: any = {
          denominacion: promocion.denominacion,
          fechaDesde: promocion.fechaDesde,
          fechaHasta: promocion.fechaHasta,
          horaDesde: promocion.horaDesde,
          horaHasta: promocion.horaHasta,
          descripcionDescuento: promocion.descripcionDescuento,
          precioPromocional: promocion.precioPromocional,
          tipoPromocion: promocion.tipoPromocion,
          porcentajeDescuento: promocion.porcentajeDescuento,
          imagenIds: promocion.imagenes.map(img => img.id),
          detallesPromocion: promocion.detallesPromocion.map(d => ({
            articuloId: d.articulo.id,
            cantidad: d.cantidad,
          })),
          sucursalIds: promocion.sucursales.map(s => s.id),
          estadoActivo: !promocion.estadoActivo, // Invertimos el estado
        };

        await PromocionService.update(promocion.id, promocionRequest);
        alert(`Promoción ${action} con éxito.`);
        promocionesData.reload(); // ¡El paso clave para refrescar la tabla!
      } catch (err: any) {
        alert(`Error al ${action} la promoción: ${err.message}`);
      }
    }
  };



  // Columnas para la tabla de Artículos Insumo
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

  // Columnas para la tabla de ArticuloManufacturado
  const manufacturadoColumns: ColumnDefinition<ArticuloManufacturadoResponse>[] = [
    { key: 'id', header: 'ID', renderCell: (am) => am.id, sortable: true },
    { key: 'denominacion', header: 'Denominación', renderCell: (am) => am.denominacion, sortable: true },
    { key: 'precioVenta', header: 'Precio', renderCell: (am) => `$${am.precioVenta.toFixed(2)}` },
    { key: 'categoria', header: 'Categoría', renderCell: (am) => am.categoria.denominacion },
    { key: 'estadoActivo', header: 'Estado', renderCell: (am) => <Badge bg={am.estadoActivo ? 'success' : 'danger'}>{am.estadoActivo ? 'Activo' : 'Inactivo'}</Badge> },
  ];

  // Columnas para la tabla de Categorias
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

  // Columnas para la tabla de Promociones
  const promocionColumns: ColumnDefinition<PromocionResponse>[] = [
    { key: 'id', header: 'ID', renderCell: (p) => p.id, sortable: true },
    { key: 'denominacion', header: 'Denominación', renderCell: (p) => p.denominacion, sortable: true },
    { key: 'tipoPromocion', header: 'Tipo', renderCell: (p) => p.tipoPromocion },
    { key: 'fechas', header: 'Vigencia', renderCell: (p) => `${p.fechaDesde} - ${p.fechaHasta}` },
    { key: 'estadoActivo', header: 'Estado', renderCell: (p) => <Badge bg={p.estadoActivo ? 'success' : 'danger'}>{p.estadoActivo ? 'Activa' : 'Inactiva'}</Badge> },
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

  const renderPromocionActions = (promocion: PromocionResponse) => (
    <>
      <Button variant="secondary" size="sm" className="me-1" onClick={() => handleViewPromocion(promocion)} title="Ver Detalles"> <FontAwesomeIcon icon={faEye} /></Button>
      <Button variant="info" size="sm" className="me-1" onClick={() => handleOpenPromocionForm(promocion)} title="Editar"><FontAwesomeIcon icon={faEdit} /></Button>
      <Button variant={promocion.estadoActivo ? 'warning' : 'success'} size="sm" className="me-1" onClick={() => handleTogglePromocionState(promocion)} title={promocion.estadoActivo ? 'Desactivar' : 'Activar'}>{promocion.estadoActivo ? 'Desactivar' : 'Activar'}</Button>
    </>
  );

  const handleTabSelect = (key: string | null) => {
    if (key) setActiveTab(key as any);
  };

  return (
    <Container className="my-4">
      <Titulo texto='Gestión de artículos' nivel='titulo' />
      <Tabs activeKey={activeTab} onSelect={handleTabSelect} className="mb-3 justify-content-center">

        {/* PESTAÑA DE ARTICULOS MANUFACTURADOS */}
        <Tab eventKey="manufacturados" title={<span><FontAwesomeIcon icon={faBoxOpen} /> Art. Manufacturados</span>}>
          <Form.Group as={Row} className="mb-3 align-items-center">
            <Form.Label className='filtro-categoria-articuloManufacturado' column sm={2}>Filtrar por Categoría:</Form.Label>
            <Col sm={4}>
              <Form.Select onChange={(e) => setFiltroCategoriaManuf(Number(e.target.value) || '')} value={filtroCategoriaManuf} disabled={!selectedSucursal}>
                <option value="">Todas</option>
                {selectedSucursal?.categorias.map(c => <option key={c.id} value={c.id}>{c.denominacion}</option>)}
              </Form.Select>
            </Col>
          </Form.Group>
          <Card className="shadow-sm">
            <Card.Body>
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

        {/* PESTAÑA DE ARTICULOS INSUMOS */}
        <Tab eventKey="insumos" title={<span><FontAwesomeIcon icon={faTools} /> Art. Insumo</span>}>
          <Card className="shadow-sm">
            <Card.Body>
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

        {/* PESTAÑA DE CATEGORIAS */}
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

        {/* PESTAÑA DE PROMOCIONES */}
        <Tab eventKey="promociones" title={<span><FontAwesomeIcon icon={faTags} /> Promociones</span>}>
          <Card className="shadow-sm">
            <Card.Body>
              <SearchableTable
                {...promocionesData}
                columns={promocionColumns}
                renderRowActions={renderPromocionActions}
                createButtonText="Nueva Promoción"
                onCreate={() => handleOpenPromocionForm(null)}
                searchPlaceholder="Buscar promociones..."
              />
            </Card.Body>
          </Card>
        </Tab>
      </Tabs>

      {/* MODALES */}
      <ArticuloInsumoForm show={showInsumoForm} handleClose={() => setShowInsumoForm(false)} onSave={handleFormSubmit} articuloToEdit={editingInsumo} />
      <ArticuloManufacturadoForm show={showManufacturadoForm} handleClose={() => setShowManufacturadoForm(false)} onSave={handleFormSubmit} articuloToEdit={editingManufacturado} />
      <ArticuloManufacturadoDetailModal show={showManufacturadoDetailModal} handleClose={() => setShowManufacturadoDetailModal(false)} articulo={selectedManufacturadoForDetail} />
      <ArticuloInsumoDetailModal show={showInsumoDetailModal} handleClose={() => setShowInsumoDetailModal(false)} articulo={selectedInsumoForDetail} />
      <CategoriaForm show={showCategoriaForm} handleClose={() => setShowCategoriaForm(false)} onSave={handleFormSubmit} categoriaToEdit={editingCategoria} />
      <PromocionForm show={showPromocionForm} handleClose={() => setShowPromocionForm(false)} onSave={handleFormSubmit} promocionToEdit={editingPromocion} />
      <PromocionDetailModal show={showPromocionDetailModal} handleClose={() => setShowPromocionDetailModal(false)} promocion={selectedPromocionForDetail} />
    </Container>
  );
};

export default ManageProductsPage;
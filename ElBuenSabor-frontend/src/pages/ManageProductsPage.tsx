// src/pages/ManageProductsPage.tsx

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
import React, { useState, useCallback } from 'react';
import { Container, Tabs, Tab, Button, Badge, Card } from 'react-bootstrap';
import { useAuth0 } from '@auth0/auth0-react';

// Servicios
import { ArticuloInsumoService } from '../services/articuloInsumoService';
import { ArticuloManufacturadoService } from '../services/articuloManufacturadoService';

// Tipos
import type { ArticuloManufacturado, ArticuloInsumo, EntityWithId } from '../types/types';

// Tabla Genérica y Hook
import { SearchableTable, type ColumnDefinition } from '../components/common/Tables/SearchableTable';
import { useSearchableData, type SortConfig } from '../hooks/useSearchableData';


// Iconos
import { FontAwesomeIcon } from '@fortawesome/react-fontawesome';
import { faEdit, faTrash, faBoxOpen, faTools, faEye, faExclamationTriangle, faCheckCircle } from '@fortawesome/free-solid-svg-icons';

// Componentes de UI anidados
import ArticuloInsumoForm from '../components/admin/ArticuloInsumoForm';
import ArticuloManufacturadoForm from '../components/admin/ArticuloManufacturadoForm';
import ArticuloManufacturadoDetailModal from '../components/admin/ArticuloManufacturadoDetailModal';
import ArticuloInsumoDetailModal from '../components/admin/ArticuloInsumoDetailModal';

// ------ COMPONENTE PRINCIPAL ------

const ManageProductsPage: React.FC = () => {
  const { getAccessTokenSilently } = useAuth0();
  const [activeTab, setActiveTab] = useState<'manufacturados' | 'insumos'>('manufacturados');

  // INSTANCIAS DE SERVICIOS
  const articuloInsumoService = new ArticuloInsumoService();
  const articuloManufacturadoService = new ArticuloManufacturadoService();

  // --- ESTADOS Y MANEJADORES PARA MODALES (Común a ambas pestañas) ---
  // Estos estados se quedan en ManageProductsPage porque los modales se renderizan aquí.

  // Para ArticuloInsumo
  const [showInsumoForm, setShowInsumoForm] = useState(false);
  const [editingInsumo, setEditingInsumo] = useState<ArticuloInsumo | null>(null);
  const [showInsumoDetailModal, setShowInsumoDetailModal] = useState(false);
  const [selectedInsumoForDetail, setSelectedInsumoForDetail] = useState<ArticuloInsumo | null>(null);

  // Para ArticuloManufacturado
  const [showManufacturadoForm, setShowManufacturadoForm] = useState(false);
  const [editingManufacturado, setEditingManufacturado] = useState<ArticuloManufacturado | null>(null);
  const [showManufacturadoDetailModal, setShowManufacturadoDetailModal] = useState(false);
  const [selectedManufacturadoForDetail, setSelectedManufacturadoForDetail] = useState<ArticuloManufacturado | null>(null);

  //  -- Lógica para Artículos Insumo con el Hook ---
  /**
   * @function fetchInsumosFunction
   * @description Función envuelta en useCallback que llama al servicio para obtener insumos.
   * Se pasa al hook `useSearchableData`.
   * @param {string} term - Término de búsqueda.
   * @returns {Promise<ArticuloInsumo[]>}
   */
  const fetchInsumosFunction = useCallback((term: string) => {
    return articuloInsumoService.getArticulosInsumo(term, null);
  }, [articuloInsumoService]);

  const insumoDataHook = useSearchableData<ArticuloInsumo>({ fetchData: fetchInsumosFunction });

  /**
   * @function handleOpenInsumoForm
   * @description Abre el modal de formulario para ArticuloInsumo.
   */
  const handleOpenInsumoForm = (insumo: ArticuloInsumo | null) => { setShowInsumoForm(true); setEditingInsumo(insumo); }; // Orden de asignación ajustado para coherencia

  /**
   * @function handleViewInsumo
   * @description Abre el modal para ver los detalles de un ArticuloInsumo.
   */
  const handleViewInsumo = (insumo: ArticuloInsumo) => { setSelectedInsumoForDetail(insumo); setShowInsumoDetailModal(true); };

  /**
   * @function handleDeleteInsumo
   * @description Maneja la eliminación de un ArticuloInsumo.
   */
  const handleDeleteInsumo = async (id: number | undefined) => {
    if (id === undefined) {
      alert('Error: ID de insumo no proporcionado para eliminar.');
      return;
    }
    if (!window.confirm(`¿Seguro que quieres eliminar el insumo ID ${id}?`)) return;
    try {
      const token = await getAccessTokenSilently();
      await articuloInsumoService.deleteArticuloInsumo(id, token);
      alert('Insumo eliminado.');
      insumoDataHook.reload();
    } catch (err) { alert(`Error al eliminar insumo: ${err instanceof Error ? err.message : 'Error desconocido.'}`); console.error(err); }
  };

  /**
   * @constant insumoColumns
   * @description Definición de columnas para la tabla de Artículos Insumo.
   */
  const insumoColumns: ColumnDefinition<ArticuloInsumo>[] = [
    {
      key: 'alerta' as keyof ArticuloInsumo, // [CORRECCIÓN 1]: Casting para 'alerta'
      header: 'Alerta',
      renderCell: (ai) => {
        let stockStatusIcon = faCheckCircle;
        let iconColor = "green";
        const stockActual = typeof ai.stockActual === 'number' ? ai.stockActual : 0;
        const stockMinimo = typeof ai.stockMinimo === 'number' ? ai.stockMinimo : 0;
        if (stockMinimo > 0) {
          if (stockActual <= stockMinimo) { stockStatusIcon = faExclamationTriangle; iconColor = "red"; }
          else if (stockActual <= stockMinimo + (stockMinimo * 0.20)) { stockStatusIcon = faExclamationTriangle; iconColor = "orange"; }
        } else if (stockMinimo === 0 && stockActual === 0) { stockStatusIcon = faExclamationTriangle; iconColor = "orange"; }
        return <FontAwesomeIcon icon={stockStatusIcon} style={{ color: iconColor, fontSize: '1.2em' }} title={`Stock: ${stockActual}/${stockMinimo > 0 ? stockMinimo : 'N/A'}`} />;
      }
    },
    { key: 'denominacion', header: 'Denominación', renderCell: (ai) => ai.denominacion, sortable: true },
    { key: 'precioVenta', header: 'Precio Venta', renderCell: (ai) => `$${ai.precioVenta.toFixed(2)}` },
    {
      key: 'stockActual' as keyof ArticuloInsumo, // [CORRECCIÓN 2]: Usamos una propiedad real del objeto o hacemos casting
      header: 'Stock (Actual/Mínimo)',
      renderCell: (ai) => `${typeof ai.stockActual === 'number' ? ai.stockActual : 'N/A'} / ${typeof ai.stockMinimo === 'number' && ai.stockMinimo > 0 ? ai.stockMinimo : 'N/A'}`
    },
    { key: 'unidadMedida', header: 'U. Medida', renderCell: (ai) => ai.unidadMedida.denominacion },
    { key: 'esParaElaborar', header: 'Para Elaborar', renderCell: (ai) => (ai.esParaElaborar ? 'Sí' : 'No') },
    { key: 'estadoActivo', header: 'Estado', renderCell: (ai) => <Badge bg={ai.estadoActivo ? 'success' : 'danger'}>{ai.estadoActivo ? 'Activo' : 'Inactivo'}</Badge> },
  ];

  /**
     * @function renderInsumoActions
     * @description Renderiza los botones de acción para cada fila de la tabla de insumos.
     */
  const renderInsumoActions = (insumo: ArticuloInsumo, reloadData: () => void) => (
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


  // --- Lógica para Artículos Manufacturados con el Hook ---
  /**
  * @function fetchManufacturadosFunction
  * @description Función envuelta en useCallback que llama al servicio para obtener manufacturados.
  */
  const fetchManufacturadosFunction = useCallback((term: string) => {
    return articuloManufacturadoService.getArticulosManufacturados(term, null);
  }, [articuloManufacturadoService]);

  const manufacturadoDataHook = useSearchableData<ArticuloManufacturado>({ fetchData: fetchManufacturadosFunction });


  /**
   * @function handleOpenManufacturadoForm
   * @description Abre el modal de formulario para ArticuloManufacturado.
   */
  const handleOpenManufacturadoForm = (mf: ArticuloManufacturado | null) => { setShowManufacturadoForm(true); setEditingManufacturado(mf); }; // Orden de asignación ajustado

  /**
   * @function handleViewManufacturado
   * @description Abre el modal para ver los detalles de un ArticuloManufacturado.
   */
  const handleViewManufacturado = (mf: ArticuloManufacturado) => { setSelectedManufacturadoForDetail(mf); setShowManufacturadoDetailModal(true); };
  /**
 * @function handleDeleteManufacturado
 * @description Maneja la eliminación de un ArticuloManufacturado.
 */
  const handleDeleteManufacturado = async (id: number | undefined) => {
    if (id === undefined) {
      alert('Error: ID de manufacturado no proporcionado para eliminar.');
      return;
    }
    if (!window.confirm(`¿Seguro que quieres eliminar el manufacturado ID ${id}?`)) return;
    try {
      const token = await getAccessTokenSilently();
      await articuloManufacturadoService.deleteArticuloManufacturado(id, token);
      alert('Artículo manufacturado eliminado.');
      manufacturadoDataHook.reload();
    } catch (err) { alert(`Error al eliminar manufacturado: ${err instanceof Error ? err.message : 'Error desconocido.'}`); console.error(err); }
  };

  // manufacturadoColumns se define para ArticuloManufacturado original, el casting se hace al pasarlo a SearchableTable
  const manufacturadoColumns: ColumnDefinition<ArticuloManufacturado>[] = [
    {
      key: 'unidadesDisponiblesCalculadas' as keyof ArticuloManufacturado, // [CORRECCIÓN 3]: Usamos la propiedad real 'unidadesDisponiblesCalculadas' o hacemos casting
      header: 'Unids. Disp.',
      renderCell: (am) => (
        typeof am.unidadesDisponiblesCalculadas === 'number' ? am.unidadesDisponiblesCalculadas : 'N/A'), sortable: true
    },
    { key: 'denominacion', header: 'Denominación', renderCell: (am) => am.denominacion, sortable: true },
    { key: 'precioVenta', header: 'Precio Venta', renderCell: (am) => `$${am.precioVenta.toFixed(2)}` },
    {
      key: 'tiempoEstimadoMinutos' as keyof ArticuloManufacturado, // [CORRECCIÓN 4]: Usamos la propiedad real 'tiempoEstimadoMinutos'
      header: 'Tiempo Estimado',
      renderCell: (am) => `${am.tiempoEstimadoMinutos} min`
    },
    { key: 'categoria', header: 'Categoría', renderCell: (am) => am.categoria.denominacion },
    { key: 'estadoActivo', header: 'Estado', renderCell: (am) => <Badge bg={am.estadoActivo ? 'success' : 'danger'}>{am.estadoActivo ? 'Activo' : 'Inactivo'}</Badge> },
  ];

  const renderManufacturadoActions = (manufacturado: ArticuloManufacturado, reloadData: () => void) => (
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

  // --- Lógica Común ---
  /**
   * @function handleTabSelect
   * @description Maneja el cambio de pestañas.
   */
  const handleTabSelect = (key: string | null) => {
    if (key === 'insumos' || key === 'manufacturados') {
      const newActiveTab = key as 'manufacturados' | 'insumos';
      setActiveTab(newActiveTab);

      if (newActiveTab === 'insumos') {
        console.log("ManageProductsPage: Tab changed to Insumos, calling insumoDataHook.reload()");
        insumoDataHook.reload();
      } else if (newActiveTab === 'manufacturados') {
        console.log("ManageProductsPage: Tab changed to Manufacturados, calling manufacturadoDataHook.reload()");
        manufacturadoDataHook.reload();
      }
    }
  };

  /**
   * @function handleFormSubmit
   * @description Callback para cuando un formulario se guarda.
   */
  const handleFormSubmit = () => {
    setShowInsumoForm(false); setEditingInsumo(null);
    setShowManufacturadoForm(false); setEditingManufacturado(null);
    if (activeTab === 'insumos') {
      insumoDataHook.reload();
    } else if (activeTab === 'manufacturados') {
      manufacturadoDataHook.reload();
    }
  };

  return (
    <Container className="my-4">
      <h1 className="text-center mb-4">Gestión de Artículos</h1>
      <Tabs activeKey={activeTab} onSelect={handleTabSelect} className="mb-3 justify-content-center">
        <Tab eventKey="manufacturados" title={<span><FontAwesomeIcon icon={faBoxOpen} className="me-2" />Artículos Manufacturados</span>}>
          <Card className="shadow-sm">
            <Card.Body>
              <SearchableTable<ArticuloManufacturado & EntityWithId>
                items={manufacturadoDataHook.items as (ArticuloManufacturado & EntityWithId)[]}
                searchTerm={manufacturadoDataHook.searchTerm}
                setSearchTerm={manufacturadoDataHook.setSearchTerm}
                isLoading={manufacturadoDataHook.isLoading}
                error={manufacturadoDataHook.error}
                reload={manufacturadoDataHook.reload}
                sortConfig={manufacturadoDataHook.sortConfig as SortConfig<ArticuloManufacturado & EntityWithId>}
                requestSort={manufacturadoDataHook.requestSort as (key: keyof (ArticuloManufacturado & EntityWithId)) => void}
                columns={manufacturadoColumns as ColumnDefinition<ArticuloManufacturado & EntityWithId>[]}
                renderRowActions={renderManufacturadoActions as (item: ArticuloManufacturado & EntityWithId, reloadData: () => void) => React.ReactNode}
                searchPlaceholder="Buscar manufacturados..."
                createButtonText="Nuevo Manufacturado"
                onCreate={() => handleOpenManufacturadoForm(null)}
              />
            </Card.Body>
          </Card>
        </Tab>
        <Tab eventKey="insumos" title={<span><FontAwesomeIcon icon={faTools} className="me-2" />Artículos Insumo</span>}>
          <Card className="shadow-sm">
            <Card.Body>
              <SearchableTable<ArticuloInsumo & EntityWithId>
                items={insumoDataHook.items as (ArticuloInsumo & EntityWithId)[]}
                searchTerm={insumoDataHook.searchTerm}
                setSearchTerm={insumoDataHook.setSearchTerm}
                isLoading={insumoDataHook.isLoading}
                error={insumoDataHook.error}
                reload={insumoDataHook.reload}
                sortConfig={insumoDataHook.sortConfig as SortConfig<ArticuloInsumo & EntityWithId>}
                requestSort={insumoDataHook.requestSort as (key: keyof (ArticuloInsumo & EntityWithId)) => void}
                columns={insumoColumns as ColumnDefinition<ArticuloInsumo & EntityWithId>[]}
                renderRowActions={renderInsumoActions as (item: ArticuloInsumo & EntityWithId, reloadData: () => void) => React.ReactNode}
                searchPlaceholder="Buscar insumos..."
                createButtonText="Nuevo Insumo"
                onCreate={() => handleOpenInsumoForm(null)}
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
    </Container>
  );
};

export default ManageProductsPage;
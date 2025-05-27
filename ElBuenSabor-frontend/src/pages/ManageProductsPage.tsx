/**
 * @file ManageProductsPage.tsx
 * @description Página de administración para la gestión de Artículos (Artículos Manufacturados y Artículos Insumo).
 * Permite a los usuarios (administradores/empleados) ver un listado de ambos tipos de artículos
 * en pestañas separadas, y realizar operaciones de Creación, Edición y Eliminación (CRUD).
 * Utiliza modales de formulario (`ArticuloInsumoForm`, `ArticuloManufacturadoForm`) para las operaciones de C/E.
 *
 * @hook `useState`: Gestiona los listados de artículos, estados de carga/error, la pestaña activa,
 * y la visibilidad/modo (edición/creación) de los modales de formulario.
 * @hook `useEffect`: Carga inicial de todos los artículos al montar la página.
 * @hook `useAuth0`: Para obtener el token de autenticación necesario para las operaciones protegidas del API.
 *
 * @service `getArticulosInsumo`, `deleteArticuloInsumo`: Servicios para Artículos Insumo.
 * @service `getArticulosManufacturados`, `deleteArticuloManufacturado`: Servicios para Artículos Manufacturados.
 * @service `deleteImageEntity`, `deleteFileFromServer`: Servicios para la gestión de imágenes asociadas.
 *
 * @component `ArticuloInsumoForm`, `ArticuloManufacturadoForm`: Modales de formulario anidados.
 */
import React, { useEffect, useState } from 'react';
import { Container, Card, Button, Table, Spinner, Alert, Tabs, Tab } from 'react-bootstrap';
import { useAuth0 } from '@auth0/auth0-react';
import { getArticulosInsumo, deleteArticuloInsumo } from '../services/articuloInsumoService';
import { getArticulosManufacturados, deleteArticuloManufacturado } from '../services/articuloManufacturadoService';
import { deleteImageEntity } from '../services/imagenService';
import { deleteFileFromServer } from '../services/fileUploadService';
import type { ArticuloManufacturado, ArticuloInsumo, Imagen } from '../types/types';
import { FontAwesomeIcon } from '@fortawesome/react-fontawesome';
import { faPlus, faEdit, faTrash, faBoxOpen, faTools, faEye } from '@fortawesome/free-solid-svg-icons';
import ArticuloInsumoForm from '../components/admin/ArticuloInsumoForm';
import ArticuloManufacturadoForm from '../components/admin/ArticuloManufacturadoForm';
import ArticuloManufacturadoDetailModal from '../components/admin/ArticuloManufacturadoDetailModal';

/**
 * @interface ManageProductsPageProps
 * @description No se requieren propiedades (`props`) para este componente de página de gestión,
 * por lo que se define una interfaz vacía para claridad.
 */
interface ManageProductsPageProps { }

const ManageProductsPage: React.FC<ManageProductsPageProps> = () => {
  /**
   * @hook useAuth0
   * @description Hook para obtener el token de acceso de Auth0, necesario para autenticar
   * las peticiones de modificación/eliminación al backend.
   */
  const { getAccessTokenSilently } = useAuth0();

  /**
   * @state manufacturados
   * @description Lista de `ArticuloManufacturado`s obtenidos del backend.
   */
  const [manufacturados, setManufacturados] = useState<ArticuloManufacturado[]>([]);

  /**
   * @state insumos
   * @description Lista de `ArticuloInsumo`s obtenidos del backend.
   */
  const [insumos, setInsumos] = useState<ArticuloInsumo[]>([]);

  /**
   * @state loading
   * @description Estado booleano para indicar si los datos de los artículos están cargando.
   */
  const [loading, setLoading] = useState(true);

  /**
   * @state error
   * @description Almacena un mensaje de error si ocurre un problema durante la carga de datos.
   */
  const [error, setError] = useState<string | null>(null);

  /**
   * @state activeTab
   * @description Controla qué pestaña está activa ('manufacturados' o 'insumos').
   */
  const [activeTab, setActiveTab] = useState<'manufacturados' | 'insumos'>('manufacturados');

  /**
   * @state showInsumoForm
   * @description Controla la visibilidad del modal `ArticuloInsumoForm`.
   */
  const [showInsumoForm, setShowInsumoForm] = useState(false);

  /**
   * @state editingInsumo
   * @description Almacena el objeto `ArticuloInsumo` que se está editando en el formulario.
   * Si es `null`, el formulario está en modo creación.
   */
  const [editingInsumo, setEditingInsumo] = useState<ArticuloInsumo | null>(null);

  /**
   * @state showManufacturadoForm
   * @description Controla la visibilidad del modal `ArticuloManufacturadoForm`.
   */
  const [showManufacturadoForm, setShowManufacturadoForm] = useState(false);

  /**
   * @state editingManufacturado
   * @description Almacena el objeto `ArticuloManufacturado` que se está editando en el formulario.
   * Si es `null`, el formulario está en modo creación.
   */
  const [editingManufacturado, setEditingManufacturado] = useState<ArticuloManufacturado | null>(null);

  /**
   * @state showManufacturadoDetailModal
   * @description Controla la visibilidad del modal `ArticuloManufactuadoDetailModal`.
   */
  const [showManufacturadoDetailModal, setShowManufacturadoDetailModal] = useState(false);
  const [selectedManufacturadoForDetail, setSelectedManufacturadoForDetail] = useState<ArticuloManufacturado | null>(null);

  /**
   * @function fetchData
   * @description Función asíncrona para cargar todos los artículos manufacturados e insumos del backend.
   * Actualiza los estados `manufacturados`, `insumos`, `loading` y `error`.
   */
  const fetchData = async () => {
    setLoading(true);
    setError(null);
    try {
      // Realiza las dos llamadas API en paralelo para mayor eficiencia
      const [fetchedManufacturados, fetchedInsumos] = await Promise.all([
        getArticulosManufacturados(),
        getArticulosInsumo(),
      ]);
      setManufacturados(fetchedManufacturados);
      setInsumos(fetchedInsumos);
    } catch (err) {
      console.error('Error al cargar artículos:', err);
      // Extrae el mensaje de error de la respuesta si está disponible
      const errorMessage = (err as any).response?.data?.message || (err as any).message || 'Error desconocido al cargar.';
      setError(`No se pudieron cargar los artículos: ${errorMessage}. Intenta de nuevo.`);
    } finally {
      setLoading(false);
    }
  };

  /**
   * @hook useEffect
   * @description Hook que se ejecuta una vez al montar el componente para realizar la carga inicial de datos.
   */
  useEffect(() => {
    fetchData();
  }, []); // Dependencias vacías: se ejecuta solo una vez al montar

  const handleViewManufacturado = async (manufacturado: ArticuloManufacturado) => {
    setSelectedManufacturadoForDetail(manufacturado);
    setShowManufacturadoDetailModal(true);
  };

  /**
   * @function handleEditInsumo
   * @description Prepara el modal para editar un `ArticuloInsumo` específico.
   * @param {ArticuloInsumo} insumo - El objeto `ArticuloInsumo` a editar.
   */
  const handleEditInsumo = (insumo: ArticuloInsumo) => {
    setEditingInsumo(insumo);
    setShowInsumoForm(true);
  };

  /**
   * @function handleDeleteInsumo
   * @description Maneja la eliminación de un `ArticuloInsumo` y sus imágenes asociadas.
   * Solicita confirmación al usuario antes de proceder.
   * @param {number} id - El ID del `ArticuloInsumo` a eliminar.
   * @param {Imagen[]} imagenes - Array de objetos `Imagen` asociados al insumo, con `id` y `denominacion`.
   */
  const handleDeleteInsumo = async (id: number, imagenes: Imagen[]) => {
    if (!window.confirm('¿Estás seguro de que quieres eliminar este insumo? Esta acción es irreversible y también eliminará las imágenes asociadas.')) {
      return;
    }
    try {
      const token = await getAccessTokenSilently();
      await deleteArticuloInsumo(id, token);

      // Eliminar todas las imágenes asociadas al insumo (entidad en DB y archivo físico)
      for (const img of imagenes) {
        try {
          await deleteImageEntity(img.id, token); // Elimina la entidad de imagen de la DB
          // Extraer el nombre de archivo de la URL si es una URL completa del servidor de archivos
          if (img.denominacion.includes('/api/files/view/')) {
            const filename = img.denominacion.substring(img.denominacion.lastIndexOf('/') + 1);
            await deleteFileFromServer(filename, token); // Elimina el archivo físico del servidor
          }
        } catch (imgErr) {
          // Registra una advertencia si falla la eliminación de una imagen, pero no detiene el proceso
          console.warn(`Error al eliminar la imagen ${img.denominacion} (ID: ${img.id}) del insumo:`, imgErr);
        }
      }

      alert('Insumo eliminado con éxito.');
      fetchData(); // Recargar la lista de artículos después de la eliminación
    } catch (err) {
      console.error('Error al eliminar insumo:', err);
      const errorMessage = (err as any).response?.data?.message || (err as any).message || 'Error desconocido al eliminar.';
      alert(`Error al eliminar insumo: ${errorMessage}`);
    }
  };

  /**
   * @function handleEditManufacturado
   * @description Prepara el modal para editar un `ArticuloManufacturado` específico.
   * @param {ArticuloManufacturado} manufacturado - El objeto `ArticuloManufacturado` a editar.
   */
  const handleEditManufacturado = (manufacturado: ArticuloManufacturado) => {
    setEditingManufacturado(manufacturado);
    setShowManufacturadoForm(true);
  };

  /**
   * @function handleDeleteManufacturado
   * @description Maneja la eliminación de un `ArticuloManufacturado` y sus imágenes asociadas.
   * Solicita confirmación al usuario antes de proceder.
   * @param {number} id - El ID del `ArticuloManufacturado` a eliminar.
   * @param {Imagen[]} imagenes - Array de objetos `Imagen` asociados al manufacturado, con `id` y `denominacion`.
   */
  const handleDeleteManufacturado = async (id: number, imagenes: Imagen[]) => {
    if (!window.confirm('¿Estás seguro de que quieres eliminar este artículo manufacturado? Esta acción es irreversible y también eliminará las imágenes asociadas.')) {
      return;
    }
    try {
      const token = await getAccessTokenSilently();
      await deleteArticuloManufacturado(id, token);

      // Eliminar todas las imágenes asociadas al manufacturado (entidad en DB y archivo físico)
      for (const img of imagenes) {
        try {
          await deleteImageEntity(img.id, token);
          if (img.denominacion.includes('/api/files/view/')) {
            const filename = img.denominacion.substring(img.denominacion.lastIndexOf('/') + 1);
            await deleteFileFromServer(filename, token);
          }
        } catch (imgErr) {
          console.warn(`Error al eliminar la imagen ${img.denominacion} (ID: ${img.id}) del manufacturado:`, imgErr);
        }
      }

      alert('Artículo manufacturado eliminado con éxito.');
      fetchData(); // Recargar la lista de artículos después de la eliminación
    } catch (err) {
      console.error('Error al eliminar artículo manufacturado:', err);
      const errorMessage = (err as any).response?.data?.message || (err as any).message || 'Error desconocido al eliminar.';
      alert(`Error al eliminar artículo manufacturado: ${errorMessage}`);
    }
  };

  /**
   * @function handleFormSubmit
   * @description Callback que se ejecuta cuando un formulario de artículo (insumo o manufacturado)
   * se guarda exitosamente. Cierra los modales y resetea los estados de edición, luego recarga los datos.
   */
  const handleFormSubmit = () => {
    setShowInsumoForm(false);
    setShowManufacturadoForm(false);
    setEditingInsumo(null);
    setEditingManufacturado(null);
    fetchData(); // Recargar todos los datos para reflejar los cambios
  };

  // --- Renderizado condicional basado en estados de carga o error ---
  if (loading) {
    return (
      <Container className="text-center my-5">
        <Spinner animation="border" role="status" />
        <p className="mt-3">Cargando artículos para la gestión...</p>
      </Container>
    );
  }

  if (error) {
    return (
      <Container className="my-5">
        <Alert variant="danger">{error}</Alert>
        <Button variant="primary" onClick={fetchData} className="mt-3">Reintentar Carga</Button>
      </Container>
    );
  }

  return (
    <Container className="my-4">
      <h1 className="text-center mb-4">Gestión de Artículos</h1>

      {/* Componente Tabs para alternar entre Artículos Manufacturados e Insumos */}
      <Tabs
        activeKey={activeTab}
        onSelect={(k) => setActiveTab(k as 'manufacturados' | 'insumos')}
        className="mb-3 justify-content-center" // Centrar las pestañas
      >
        {/* Pestaña para Artículos Manufacturados */}
        <Tab
          eventKey="manufacturados"
          title={<span><FontAwesomeIcon icon={faBoxOpen} className="me-2" />Artículos Manufacturados</span>}
        >
          <Card className="shadow-sm">
            <Card.Header className="d-flex justify-content-between align-items-center">
              <h5>Listado de Artículos Manufacturados</h5>
              <Button variant="success" onClick={() => { setEditingManufacturado(null); setShowManufacturadoForm(true); }}>
                <FontAwesomeIcon icon={faPlus} className="me-2" />Nuevo Manufacturado
              </Button>
            </Card.Header>
            <Card.Body>
              {manufacturados.length === 0 ? (
                <Alert variant="info" className="text-center">No hay artículos manufacturados registrados.</Alert>
              ) : (
                <Table striped bordered hover responsive className="text-center align-middle"> {/* align-middle para centrar contenido */}
                  <thead>
                    <tr>
                      <th>ID</th>
                      <th>Denominación</th>
                      <th>Estado</th>
                      <th>Acciones</th>
                    </tr>
                  </thead>
                  <tbody>
                    {manufacturados.map((am) => (
                      <tr key={am.id}>
                        <td>{am.id}</td>
                        <td>{am.denominacion}</td>
                        <td>{am.estadoActivo ? 'Activo' : 'Inactivo'}</td>
                        <td>
                          <Button variant='ver' size="sm" className="me-3" onClick={() => handleViewManufacturado(am)}>
                            <FontAwesomeIcon icon={faEye} className="me-1" /> Ver
                          </Button>
                          <Button variant="edit" size="sm" className="me-2" onClick={() => handleEditManufacturado(am)}>
                            <FontAwesomeIcon icon={faEdit} className="me-1" /> Editar
                          </Button>
                          <Button variant="danger" size="sm" onClick={() => handleDeleteManufacturado(am.id, am.imagenes)}>
                            <FontAwesomeIcon icon={faTrash} className="me-1" /> Eliminar
                          </Button>
                        </td>
                      </tr>
                    ))}
                  </tbody>
                </Table>
              )}
            </Card.Body>
          </Card>
        </Tab>

        {/* Pestaña para Artículos Insumo */}
        <Tab
          eventKey="insumos"
          title={<span><FontAwesomeIcon icon={faTools} className="me-2" />Artículos Insumo</span>}
        >
          <Card className="shadow-sm">
            <Card.Header className="d-flex justify-content-between align-items-center">
              <h5>Listado de Artículos Insumo</h5>
              <Button variant="success" onClick={() => { setEditingInsumo(null); setShowInsumoForm(true); }}>
                <FontAwesomeIcon icon={faPlus} className="me-2" />Nuevo Insumo
              </Button>
            </Card.Header>
            <Card.Body>
              {insumos.length === 0 ? (
                <Alert variant="info" className="text-center">No hay artículos insumo registrados.</Alert>
              ) : (
                <Table striped bordered hover responsive className="text-center align-middle">
                  <thead>
                    <tr>
                      <th>ID</th>
                      <th>Denominación</th>
                      <th>Precio Venta</th>
                      <th>Stock Actual</th>
                      <th>U. Medida</th>
                      <th>Es Para Elaborar</th>
                      <th>Estado</th>
                      <th>Acciones</th>
                    </tr>
                  </thead>
                  <tbody>
                    {insumos.map((ai) => (
                      <tr key={ai.id}>
                        <td>{ai.id}</td>
                        <td>{ai.denominacion}</td>
                        <td>${ai.precioVenta.toFixed(2)}</td>
                        <td>{ai.stockActual}</td>
                        <td>{ai.unidadMedida.denominacion}</td>
                        <td>{ai.esParaElaborar ? 'Sí' : 'No'}</td>
                        <td>{ai.estadoActivo ? 'Activo' : 'Inactivo'}</td>
                        <td>
                          <Button variant="info" size="sm" className="me-2" onClick={() => handleEditInsumo(ai)}>
                            <FontAwesomeIcon icon={faEdit} className="me-1" /> Editar
                          </Button>
                          {/* Al eliminar, se pasa el array de imágenes para su manejo */}
                          <Button variant="danger" size="sm" onClick={() => handleDeleteInsumo(ai.id, ai.imagenes)}>
                            <FontAwesomeIcon icon={faTrash} className="me-1" /> Eliminar
                          </Button>
                        </td>
                      </tr>
                    ))}
                  </tbody>
                </Table>
              )}
            </Card.Body>
          </Card>
        </Tab>
      </Tabs>

      {/* Modales para formularios de Articulo Insumo y Articulo Manufacturado */}
      <ArticuloInsumoForm
        show={showInsumoForm}
        handleClose={() => setShowInsumoForm(false)}
        onSave={handleFormSubmit}
        articuloToEdit={editingInsumo}
      />
      <ArticuloManufacturadoForm
        show={showManufacturadoForm}
        handleClose={() => setShowManufacturadoForm(false)}
        onSave={handleFormSubmit}
        articuloToEdit={editingManufacturado}
      />
      <ArticuloManufacturadoDetailModal
        show={showManufacturadoDetailModal}
        handleClose={() => setShowManufacturadoDetailModal(false)}
        articulo={selectedManufacturadoForDetail}
      />
    </Container>
  );
};

export default ManageProductsPage;
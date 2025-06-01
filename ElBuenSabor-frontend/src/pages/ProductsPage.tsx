/**
 * @file ProductsPage.tsx
 * @description Página principal que muestra el menú de Artículos Manufacturados (productos) disponibles.
 * Permite a los usuarios visualizar los productos, filtrarlos por categoría, y ver el estado de carga o errores.
 * Cada producto se presenta mediante el componente `ProductCard`.
 *
 * @hook `useState`: Gestiona los listados de productos y categorías, estados de carga/error,
 * y la categoría seleccionada para el filtro.
 * @hook `useEffect`: Realiza la carga inicial de productos y categorías desde el backend.
 *
 * @service `getArticulosManufacturados`: Para obtener la lista de productos.
 * @service `getCategorias`: Para obtener la lista de categorías para el filtro.
 * @component `ProductCard`: Componente individual para mostrar cada producto.
 */
import React, { useEffect, useState } from 'react';
import { Container, Row, Col, Spinner, Alert, Form, Button } from 'react-bootstrap';
import { getArticulosManufacturados } from '../services/articuloManufacturadoService';
import { getCategorias } from '../services/categoriaService';
import type { ArticuloManufacturado, Categoria } from '../types/types';
import ProductCard from '../components/products/Card/ProductCard'; 
import Titulo from '../components/utils/Titulo/Titulo';
/**
 * @interface ProductsPageProps
 * @description No se requieren propiedades (`props`) para este componente de página,
 * por lo que se define una interfaz vacía para claridad.
 */
interface ProductsPageProps {}

const ProductsPage: React.FC<ProductsPageProps> = () => {
  /**
   * @state products
   * @description Almacena el array de `ArticuloManufacturado`s (productos) obtenidos del backend.
   */
  const [products, setProducts] = useState<ArticuloManufacturado[]>([]);

  /**
   * @state categories
   * @description Almacena el array de `Categoria`s disponibles para el filtro.
   */
  const [categories, setCategories] = useState<Categoria[]>([]);

  /**
   * @state loading
   * @description Estado booleano para indicar si los productos y categorías están cargando.
   */
  const [loading, setLoading] = useState<boolean>(true);

  /**
   * @state error
   * @description Almacena un mensaje de error si ocurre un problema durante la carga de datos.
   */
  const [error, setError] = useState<string | null>(null);

  /**
   * @state selectedCategory
   * @description ID de la categoría seleccionada para filtrar los productos.
   * `''` (cadena vacía) indica que se muestran todas las categorías.
   */
  const [selectedCategory, setSelectedCategory] = useState<number | ''>('');

  /**
   * @hook useEffect
   * @description Hook principal para la carga de productos y categorías.
   * Se ejecuta una vez al montar el componente. Realiza ambas llamadas API en paralelo.
   */
  useEffect(() => {
    const fetchProductsAndCategories = async () => {
      setLoading(true); // Inicia el spinner de carga
      setError(null); // Limpia cualquier error anterior
      try {
        // Realiza ambas llamadas API en paralelo para optimizar el tiempo de carga
        const [fetchedProducts, fetchedCategories] = await Promise.all([
          getArticulosManufacturados(),
          getCategorias(),
        ]);
        setProducts(fetchedProducts);
        setCategories(fetchedCategories);
      } catch (err) {
        console.error('Error al cargar productos o categorías:', err);
        const errorMessage = (err as any).response?.data?.message || (err as any).message || 'Error desconocido al cargar.';
        setError(`No se pudieron cargar los productos o categorías. Por favor, intenta de nuevo más tarde: ${errorMessage}.`);
      } finally {
        setLoading(false); // Detiene el spinner de carga
      }
    };

    fetchProductsAndCategories();
  }, []); // Dependencias vacías: se ejecuta solo una vez al montar

  /**
   * @constant filteredProducts
   * @description Variable calculada que devuelve los productos filtrados por la categoría seleccionada.
   * Si no hay categoría seleccionada, devuelve todos los productos.
   */
  const filteredProducts = selectedCategory
    ? products.filter((product) => product.categoria.id === selectedCategory)
    : products;

  // --- Renderizado condicional basado en estados de carga o error ---
  // Muestra un spinner si los productos están cargando
  if (loading) {
    return (
      <Container className="text-center my-5">
        <Spinner animation="border" role="status" />
        <p className="mt-3">Cargando nuestro delicioso menú...</p>
      </Container>
    );
  }

  // Muestra un mensaje de error si ocurre un problema
  if (error) {
    return (
      <Container className="my-5 text-center">
        <Alert variant="danger">
          <Alert.Heading>¡Error al Cargar Productos!</Alert.Heading>
          <p>{error}</p>
          <hr />
          <Button variant="primary" onClick={() => window.location.reload()}>Recargar Página</Button> {/* Botón para recargar */}
        </Alert>
      </Container>
    );
  }

  return (
    <Container className="my-4">
      <Titulo texto="Nuestros productos" nivel="titulo" />
      {/* Sección de filtro por categoría */}
      <Row className="mb-4 justify-content-center"> {/* Centrar la columna de filtro */}
        <Col md={4}>
          <Form.Group controlId="categorySelect">
            <Form.Label>Filtrar por Categoría:</Form.Label>
            <Form.Select
              value={selectedCategory}
              onChange={(e) => setSelectedCategory(Number(e.target.value) || '')} // Convierte a número o string vacío
              disabled={categories.length === 0} // Deshabilita si no hay categorías
            >
              <option value="">Todas las Categorías</option>
              {categories.map((category) => (
                <option key={category.id} value={category.id}>
                  {category.denominacion}
                </option>
              ))}
            </Form.Select>
            {categories.length === 0 && !loading && !error && (
              <Form.Text className="text-muted">No hay categorías disponibles.</Form.Text>
            )}
          </Form.Group>
        </Col>
      </Row>

      {/* Cuadrícula de tarjetas de productos */}
      <Row xs={1} sm={2} md={3} lg={4} xl={4} className="g-4"> {/* Ajuste de columnas para más responsividad */}
        {/* Renderizado condicional: si hay productos filtrados o no */}
        {filteredProducts.length > 0 ? (
          filteredProducts.map((product) => (
            <Col key={product.id}>
              <ProductCard product={product} />
            </Col>
          ))
        ) : (
          // Mensaje si no se encontraron productos en la categoría seleccionada
          <Col xs={12}> {/* Ocupa todo el ancho */}
            <Alert variant="info" className="text-center">
              No se encontraron productos en esta categoría o no hay productos registrados.
            </Alert>
          </Col>
        )}
      </Row>
    </Container>
  );
};

export default ProductsPage;
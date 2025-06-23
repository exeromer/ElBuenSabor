import React, { useEffect, useState } from 'react';
import { Container, Row, Col, Spinner, Alert, Form } from 'react-bootstrap';
// FIX: Importamos los hooks de contexto necesarios
import { useSucursal } from '../context/SucursalContext';

// FIX: Usamos los servicios con sus métodos estáticos
import { ArticuloManufacturadoService } from '../services/articuloManufacturadoService';
import { CategoriaService } from '../services/categoriaService';

// FIX: Corregimos los tipos para que coincidan con los DTOs de respuesta
import type { ArticuloManufacturadoResponse, CategoriaResponse } from '../types/types';
import ProductCard from '../components/products/Card/ProductCard';
import Titulo from '../components/utils/Titulo/Titulo';

const ProductsPage: React.FC = () => {
  // FIX: Usamos el contexto para saber la sucursal actual
  const { selectedSucursal } = useSucursal();

  const [products, setProducts] = useState<ArticuloManufacturadoResponse[]>([]);
  const [categories, setCategories] = useState<CategoriaResponse[]>([]);
  const [loading, setLoading] = useState<boolean>(true);
  const [error, setError] = useState<string | null>(null);
  const [selectedCategory, setSelectedCategory] = useState<number | ''>('');

  useEffect(() => {
    // La función ahora depende de la sucursal seleccionada
    const fetchProductsAndCategories = async () => {
      // Si no hay sucursal seleccionada, no hacemos nada.
      if (!selectedSucursal) {
        setProducts([]);
        setCategories([]);
        setLoading(false);
        return;
      }

      setLoading(true);
      setError(null);

            try {
        // 1. Obtenemos las categorías específicas de la sucursal.
        const sucursalCategories = selectedSucursal.categorias || [];
        setCategories(sucursalCategories);

        if (sucursalCategories.length > 0) {
            // 2. Obtenemos TODOS los productos manufacturados activos.
            const allActiveProducts = (await ArticuloManufacturadoService.getAll()).filter(p => p.estadoActivo);

            // 3. FIX: Filtramos los productos para quedarnos solo con los que pertenecen a las categorías de la sucursal.
            const categoryIds = sucursalCategories.map(c => c.id);
            const productosDeLaSucursal = allActiveProducts.filter(p => categoryIds.includes(p.categoria.id));
            
            setProducts(productosDeLaSucursal);
        } else {
            // Si la sucursal no tiene categorías, no tendrá productos.
            setProducts([]);
        }

      } catch (err) {
        console.error('Error al cargar productos o categorías:', err);
        setError("No se pudieron cargar los datos del menú. Intenta de nuevo más tarde.");
      } finally {
        setLoading(false);
      }
    };

    fetchProductsAndCategories();
  }, [selectedSucursal]);
  
  const filteredProducts = selectedCategory
    ? products.filter((product) => product.categoria.id === selectedCategory)
    : products;

  if (loading) {
    return (
      <Container className="text-center my-5">
        <Spinner animation="border" role="status" />
        <p className="mt-3">Cargando nuestro delicioso menú...</p>
      </Container>
    );
  }

  // Si no hay sucursal seleccionada, mostramos un mensaje amigable
  if (!selectedSucursal) {
    return (
      <Container className="my-5">
        <Alert variant="info" className="text-center">Por favor, selecciona una sucursal en el menú superior para ver nuestros productos.</Alert>
      </Container>
    )
  }

  if (error) { /* ... manejo de error ... */ }

  return (
    <Container className="my-4">
      <Titulo texto="Nuestros productos" nivel="titulo" />
      <Row className="mb-4 justify-content-center">
        <Col md={4}>
          <Form.Group controlId="categorySelect">
            <Form.Label>Filtrar por Categoría:</Form.Label>
            <Form.Select
              value={selectedCategory}
              onChange={(e) => setSelectedCategory(Number(e.target.value) || '')}
              disabled={categories.length === 0}
            >
              <option value="">Todas las Categorías</option>
              {/* Ahora las categorías son específicas de la sucursal */}
              {categories.map((category) => (
                <option key={category.id} value={category.id}>
                  {category.denominacion}
                </option>
              ))}
            </Form.Select>
          </Form.Group>
        </Col>
      </Row>

      <Row xs={1} sm={2} md={3} lg={4} xl={4} className="g-4">
        {filteredProducts.length > 0 ? (
          filteredProducts.map((product) => (
            <Col key={product.id}>
              <ProductCard product={product} />
            </Col>
          ))
        ) : (
          <Col xs={12}>
            <Alert variant="info" className="text-center">
              No se encontraron productos para esta sucursal o categoría.
            </Alert>
          </Col>
        )}
      </Row>
    </Container>
  );
};

export default ProductsPage;
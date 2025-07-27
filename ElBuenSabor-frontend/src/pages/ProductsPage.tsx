import React, { useEffect, useState } from 'react';
import { Container, Row, Col, Spinner, Alert, Form } from 'react-bootstrap';
import { useLocation } from 'react-router-dom';
import { useSucursal } from '../context/SucursalContext';
import { ArticuloManufacturadoService } from '../services/articuloManufacturadoService';
import type { ArticuloManufacturadoResponse, CategoriaResponse } from '../types/types';
import ProductCard from '../components/products/Card/ProductCard';
import Titulo from '../components/utils/Titulo/Titulo';

//Busqueda de productos
function useQuery() {
  return new URLSearchParams(useLocation().search);
}

const ProductsPage: React.FC = () => {
  const { selectedSucursal } = useSucursal();
  const query = useQuery();
  const searchTerm = query.get('search') || ''

  const [products, setProducts] = useState<ArticuloManufacturadoResponse[]>([]);
  const [categories, setCategories] = useState<CategoriaResponse[]>([]);
  const [loading, setLoading] = useState<boolean>(true);
  const [error, setError] = useState<string | null>(null);
  const [selectedCategory, setSelectedCategory] = useState<number | ''>('');

  useEffect(() => {
    const fetchProductsAndCategories = async () => {
      if (!selectedSucursal) {
        setProducts([]);
        setCategories([]);
        setLoading(false);
        return;
      }

      setLoading(true);
      setError(null);

      try {
        const sucursalCategories = selectedSucursal.categorias || [];
        setCategories(sucursalCategories);

        if (sucursalCategories.length > 0) {
          const allActiveProducts = (await ArticuloManufacturadoService.getAll(searchTerm)).filter(p => p.estadoActivo);
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
  }, [selectedSucursal, searchTerm]);

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

  if (!selectedSucursal) {
    return (
      <Container className="my-5">
        <Alert variant="info" className="text-center">Por favor, selecciona una sucursal en el menú superior para ver nuestros productos.</Alert>
      </Container>
    )
  }
  if (error) {
    return (
      <Container className="my-5">
        <Alert variant="danger" className="text-center">{error}</Alert>
      </Container>
    );
  }

  return (
    <Container className="my-4">
      <Titulo texto={searchTerm ? `Resultados para: "${searchTerm}"` : "Nuestros productos"} nivel="titulo" />
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
              {searchTerm
                ? `No se encontraron productos que coincidan con "${searchTerm}".`
                : "No hay productos disponibles para esta sucursal o categoría."
              }
            </Alert>
          </Col>
        )}
      </Row>
    </Container>
  );
};

export default ProductsPage;
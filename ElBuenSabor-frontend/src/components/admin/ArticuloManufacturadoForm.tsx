// ArticuloManufacturadoForm.tsx
/**
 * @file ArticuloManufacturadoForm.tsx
 * @description Componente de formulario modal para la creación y edición de Artículos Manufacturados.
 * Permite a los usuarios ingresar y modificar los datos de un artículo manufacturado (ej. una hamburguesa),
 * incluyendo su denominación, precios, descripción, tiempo de preparación, unidad de medida, categoría,
 * y los ingredientes (insumos) necesarios con sus cantidades. También gestiona la imagen principal.
 * Utiliza los servicios de API modularizados para interactuar con el backend
 * (ArticuloManufacturado, Categoria, UnidadMedida, ArticuloInsumo, File Upload, Imagen).
 *
 * @hook `useState`: Gestiona el estado del formulario (`formData`), las listas de opciones
 * (categorías, unidades de medida, insumos), estados de carga/envío, errores y el archivo de imagen seleccionado.
 * @hook `useEffect`: Carga las opciones iniciales al montar el componente y resetea/precarga el
 * formulario cuando el modal se abre o se cambia el artículo a editar.
 * @hook `useAuth0`: Obtiene el token de autenticación para las operaciones protegidas del API.
 */
import React, { useState, useEffect, useMemo } from "react";
import {
  Modal,
  Form,
  Button,
  Alert,
  Spinner,
  Row,
  Col,
  InputGroup,
  Card,
  ListGroup,
  Image,
} from "react-bootstrap";
import { useSucursal } from "../../context/SucursalContext";
import { ArticuloManufacturadoService } from "../../services/articuloManufacturadoService";
import { ArticuloInsumoService } from "../../services/articuloInsumoService";
import { FileUploadService } from "../../services/fileUploadService";
import { ImagenService } from "../../services/imagenService";

import type {
  ArticuloManufacturadoResponse,
  CategoriaResponse,
  ArticuloInsumoResponse,
  ArticuloManufacturadoRequest,
  ArticuloManufacturadoDetalleResponse,
  ImagenResponse,
} from "../../types/types";
import { FontAwesomeIcon } from "@fortawesome/react-fontawesome";
import { faPlusCircle, faMinusCircle } from "@fortawesome/free-solid-svg-icons";
import apiClient from "../../services/apiClient";

interface ArticuloManufacturadoFormProps {
  show: boolean;
  handleClose: () => void;
  onSave: () => void;
  articuloToEdit?: ArticuloManufacturadoResponse | null;
}

const initialFormData: ArticuloManufacturadoRequest = {
  denominacion: "",
  precioVenta: 0,
  unidadMedidaId: 4,
  categoriaId: 0,
  estadoActivo: true,
  descripcion: "",
  tiempoEstimadoMinutos: 0,
  preparacion: "",
  manufacturadoDetalles: [],
};

const ArticuloManufacturadoForm: React.FC<ArticuloManufacturadoFormProps> = ({
  show,
  handleClose,
  onSave,
  articuloToEdit,
}) => {
  const { selectedSucursal } = useSucursal();

  const [formData, setFormData] = useState<ArticuloManufacturadoRequest>({
    denominacion: "",
    precioVenta: 0,
    unidadMedidaId: 0,
    categoriaId: 0,
    estadoActivo: true,
    descripcion: "",
    tiempoEstimadoMinutos: 0,
    preparacion: "",
    manufacturadoDetalles: [],
  });
  const [imagenes, setImagenes] = useState<ImagenResponse[]>([]);
  const [categories, setCategories] = useState<CategoriaResponse[]>([]);
  const [insumos, setInsumos] = useState<ArticuloInsumoResponse[]>([]);
  const [loadingOptions, setLoadingOptions] = useState(true);
  const [submitting, setSubmitting] = useState(false);
  const [error, setError] = useState<string | null>(null);
  const [selectedFile, setSelectedFile] = useState<File | null>(null);
  const [isProductWrapper, setIsProductWrapper] = useState<boolean>(false);

  const costoTotalCalculado = useMemo(() => {
    if (!formData.manufacturadoDetalles || insumos.length === 0) {
      return 0;
    }
    return formData.manufacturadoDetalles.reduce((total, detalle) => {
      const insumo = insumos.find(i => i.id === detalle.articuloInsumoId);
      const precioCompra = insumo?.precioCompra ?? 0;
      return total + (detalle.cantidad * precioCompra);
    }, 0);
  }, [formData.manufacturadoDetalles, insumos]);

  /**
   * @hook useEffect
   * @description Hook que se ejecuta al montar el componente para cargar las listas de
   * categorías, unidades de medida y artículos insumo desde el backend.
   * Utiliza `Promise.all` para ejecutar las llamadas API en paralelo.
   */
  useEffect(() => {
    const loadOptions = async () => {
      if (!show || !selectedSucursal) return;
      setLoadingOptions(true);
      setError(null);
      try {
        // FIX: Las categorías ahora vienen directamente del contexto, no de una llamada a la API.
        const sucursalCategories = selectedSucursal.categorias || [];
        setCategories(
          sucursalCategories.filter(
            (c) => c.estadoActivo && c.denominacion.toLowerCase() !== "insumos"
          )
        );

        const fetchedInsumos = await ArticuloInsumoService.getAll();
        setInsumos(fetchedInsumos);
      } catch (err) {
        setError("Error al cargar las opciones del formulario.");
      } finally {
        setLoadingOptions(false);
      }
    };
    loadOptions();
  }, [show, selectedSucursal]);

  /**
   * @hook useEffect
   * @description Hook que gestiona la precarga de datos para el modo edición o resetea el formulario
   * para el modo creación.
   * Se ejecuta cada vez que `articuloToEdit` o `show` (visibilidad del modal) cambian.
   */
  useEffect(() => {
    if (show) {
      if (articuloToEdit) {
        setFormData({
          denominacion: articuloToEdit.denominacion,
          precioVenta: articuloToEdit.precioVenta,
          unidadMedidaId: articuloToEdit.unidadMedida.id,
          categoriaId: articuloToEdit.categoria.id,
          estadoActivo: articuloToEdit.estadoActivo,
          descripcion: articuloToEdit.descripcion,
          tiempoEstimadoMinutos: articuloToEdit.tiempoEstimadoMinutos,
          preparacion: articuloToEdit.preparacion,
          manufacturadoDetalles: articuloToEdit.manufacturadoDetalles.map(
            (d: ArticuloManufacturadoDetalleResponse) => ({
              articuloInsumoId: d.articuloInsumo.id,
              cantidad: d.cantidad,
              estadoActivo: true,
            })
          ),
        });
        setImagenes(articuloToEdit.imagenes);
      } else {
        setFormData(initialFormData);
        setImagenes([]);
      }
      setSelectedFile(null);
      setError(null);
    }
  }, [articuloToEdit, show]);

  /**
   * @function handleChange
   * @description Manejador genérico para cambios en los campos de texto, números, selects (por ID) y checkboxes.
   * Actualiza el estado `formData` basándose en el `name` del campo y su `value` o `checked` estado.
   * @param {React.ChangeEvent<HTMLInputElement | HTMLSelectElement | HTMLTextAreaElement>} e - Evento de cambio.
   */
  const handleChange = (e: React.ChangeEvent<any>) => {
    const { name, value, type, checked } = e.target;
    setFormData((prev) => ({
      ...prev,
      [name]:
        type === "checkbox"
          ? checked
          : type === "number"
            ? parseFloat(value) || 0
            : value,
    }));
  };

  /**
   * @function handleFileChange
   * @description Manejador para el input de tipo 'file' para la imagen principal.
   * Almacena el archivo seleccionado en el estado `selectedFile`.
   * @param {React.ChangeEvent<HTMLInputElement>} e - Evento de cambio del input de archivo.
   */
  const handleFileChange = (e: React.ChangeEvent<HTMLInputElement>) => {
    setSelectedFile(e.target.files ? e.target.files[0] : null);
  };

  /**
   * @function handleAddDetalle
   * @description Añade un nuevo detalle de manufacturado (ingrediente) al formulario.
   * Inicializa el nuevo detalle con el primer insumo disponible y una cantidad de 1.
   */
  const handleAddDetalle = () => {
    setFormData((prev) => ({
      ...prev,
      manufacturadoDetalles: [
        ...prev.manufacturadoDetalles,
        { articuloInsumoId: 0, cantidad: 1, estadoActivo: true },
      ],
    }));
  };

  /**
   * @function handleRemoveDetalle
   * @description Elimina un detalle de manufacturado (ingrediente) específico por su índice.
   * @param {number} index - El índice del detalle a eliminar en el array `manufacturadoDetalles`.
   */
  const handleRemoveDetalle = (index: number) => {
    setFormData((prev) => ({
      ...prev,
      manufacturadoDetalles: prev.manufacturadoDetalles.filter(
        (_, i) => i !== index
      ),
    }));
  };

  /**
   * @function handleDetalleChange
   * @description Manejador para los cambios en los campos de los detalles de manufacturado (ingredientes).
   * Actualiza la cantidad o el insumo seleccionado para un detalle específico.
   * @param {number} index - El índice del detalle a modificar.
   * @param {string} name - El nombre del campo del detalle que cambió ('articuloInsumoId' o 'cantidad').
   * @param {any} value - El nuevo valor del campo.
   */
  const handleDetalleChange = (index: number, name: string, value: any) => {
    const newDetails = [...formData.manufacturadoDetalles];
    newDetails[index] = {
      ...newDetails[index],
      [name]: name === "cantidad" ? parseFloat(value) : Number(value),
    };
    setFormData((prev) => ({ ...prev, manufacturadoDetalles: newDetails }));
  };

  /**
   * @function handleSubmit
   * @description Manejador para el envío del formulario.
   * Realiza validaciones, gestiona la subida y posible eliminación de imágenes antiguas,
   * y luego llama al servicio de API para crear o actualizar el artículo manufacturado.
   * @param {React.FormEvent} e - Evento de envío del formulario.
   */
  const handleSubmit = async (e: React.FormEvent) => {
    e.preventDefault();
    setSubmitting(true);
    setError(null);

    if (formData.precioVenta < costoTotalCalculado) {
      setError(`El precio de venta ($${formData.precioVenta}) no puede ser menor que el costo total calculado de los ingredientes ($${costoTotalCalculado.toFixed(2)}).`);
      setSubmitting(false);
      return;
    }

    const dataToSend: ArticuloManufacturadoRequest = {
      ...formData,
      unidadMedidaId: 2,
    };

    try {
      // 1. Guardamos el artículo y obtenemos el objeto guardado (con su ID)
      const savedArticulo = articuloToEdit
        ? await ArticuloManufacturadoService.update(
          articuloToEdit.id,
          dataToSend
        )
        : await ArticuloManufacturadoService.create(dataToSend);

      // 2. FIX: Ahora usamos 'savedArticulo' para la lógica de la imagen
      if (selectedFile) {
        // Si hay una imagen nueva, primero borramos las antiguas si estamos editando
        if (articuloToEdit && articuloToEdit.imagenes.length > 0) {
          for (const oldImage of articuloToEdit.imagenes) {
            await ImagenService.delete(oldImage.id);
            // Extraemos el nombre del archivo de la URL para el borrado físico
            const oldFilename = oldImage.denominacion.substring(
              oldImage.denominacion.lastIndexOf("/") + 1
            );
            await FileUploadService.deleteFile(oldFilename);
          }
        }
        // Subimos la nueva imagen y la asociamos usando el ID del artículo guardado
        await FileUploadService.uploadFile(selectedFile, {
          articuloId: savedArticulo.id,
        });
      }

      alert(
        `Artículo Manufacturado ${articuloToEdit ? "actualizado" : "creado"
        } con éxito.`
      );
      onSave(); // Esto recargará la tabla en la página principal
      handleClose(); // Cerramos el modal
    } catch (err: any) {
      setError(err.message || "Error al guardar.");
    } finally {
      setSubmitting(false);
    }
  };

  return (
    <Modal show={show} onHide={handleClose} size="lg">
      <Modal.Header closeButton>
        <Modal.Title>
          {articuloToEdit
            ? "Editar Artículo Manufacturado"
            : "Crear Artículo Manufacturado"}
        </Modal.Title>
      </Modal.Header>
      <Form onSubmit={handleSubmit}>
        <Modal.Body>
          {loadingOptions ? (
            <div className="text-center">
              <Spinner animation="border" /> Cargando opciones...
            </div>
          ) : error ? (
            <Alert variant="danger">{error}</Alert>
          ) : (
            <>
              <Form.Group className="mb-3">
                <Form.Label>Denominación</Form.Label>
                <Form.Control
                  type="text"
                  name="denominacion"
                  value={formData.denominacion}
                  onChange={handleChange}
                  required
                />
              </Form.Group>

              <Row>
                <Col>
                  <Form.Group className="mb-3">
                    <Form.Label>Precio Venta</Form.Label>
                    <Form.Control
                      type="number"
                      name="precioVenta"
                      value={formData.precioVenta}
                      onChange={handleChange}
                      step="0.01"
                      min="0.01"
                      required
                    />
                  </Form.Group>
                </Col>
                <Col>
                  <Form.Group className="mb-3">
                    <Form.Label>Costo Calculado</Form.Label>
                    <Form.Control
                      type="text"
                      value={`$${costoTotalCalculado.toFixed(2)}`}
                      readOnly
                      disabled
                    />
                    <Form.Text className="text-muted">
                      Costo total basado en los ingredientes.
                    </Form.Text>
                  </Form.Group>
                </Col>
                <Col>
                  <Form.Group className="mb-3">
                    <Form.Label>Tiempo Estimado (Minutos)</Form.Label>
                    <Form.Control
                      type="number"
                      name="tiempoEstimadoMinutos"
                      value={formData.tiempoEstimadoMinutos}
                      onChange={handleChange}
                      min="1"
                      required
                    />
                  </Form.Group>
                </Col>
              </Row>
              <Form.Group className="mb-3">
                <Form.Label>Categoría</Form.Label>
                <Form.Select
                  name="categoriaId"
                  value={formData.categoriaId || ""}
                  onChange={handleChange}
                  required
                  disabled={categories.length === 0}
                >
                  <option value="">Selecciona una Categoría</option>
                  {categories.map((cat) => (
                    <option key={cat.id} value={cat.id}>
                      {cat.denominacion}
                    </option>
                  ))}
                </Form.Select>
              </Form.Group>
              <Form.Group className="mb-3">
                <Form.Label>Descripción</Form.Label>
                <Form.Control
                  as="textarea"
                  name="descripcion"
                  value={formData.descripcion}
                  onChange={handleChange}
                  rows={3}
                  maxLength={1000}
                  required
                />
              </Form.Group>
              <Form.Group className="mb-3">
                <Form.Label>Preparación</Form.Label>
                <Form.Control
                  as="textarea"
                  name="preparacion"
                  value={formData.preparacion}
                  onChange={handleChange}
                  rows={5}
                  required
                />
              </Form.Group>

              <Form.Group className="mb-3">
                <Form.Check
                  type="checkbox"
                  label="Estado Activo"
                  name="estadoActivo"
                  checked={formData.estadoActivo}
                  onChange={handleChange}
                />
              </Form.Group>

              <Form.Group className="mb-3">
                <Form.Label>Imagen</Form.Label>
                <Form.Control
                  type="file"
                  onChange={handleFileChange}
                  accept="image/*"
                />
                {imagenes.length > 0 && !selectedFile && (
                  <div className="mt-2">
                    <Image
                      src={`${apiClient.defaults.baseURL}/files/view/${imagenes[0].denominacion}`}
                      thumbnail
                      style={{ width: "100px" }}
                    />
                  </div>
                )}
              </Form.Group>
              <Form.Group className="mb-3">
                <Form.Check
                  type="checkbox"
                  label="Es un producto simple (basado en un solo insumo vendible, ej. Gaseosa)"
                  checked={isProductWrapper}
                  onChange={(e) => {
                    setIsProductWrapper(e.target.checked);
                    if (e.target.checked) {
                      setFormData((prev) => ({
                        ...prev,
                        manufacturadoDetalles:
                          prev.manufacturadoDetalles.length > 0 &&
                            !prev.manufacturadoDetalles[0].articuloInsumoId
                            ? prev.manufacturadoDetalles
                            : [],
                      }));
                    }
                  }}
                />
                {isProductWrapper &&
                  formData.manufacturadoDetalles.length > 1 && (
                    <Alert variant="warning" className="mt-1">
                      Para productos simples, solo se espera un ingrediente. Se
                      considerará el primero.
                    </Alert>
                  )}
              </Form.Group>

              <Card className="mt-4">
                <Card.Header className="d-flex justify-content-between align-items-center">
                  <h6>Ingredientes y Cantidades</h6>
                  <Button
                    variant="outline-success"
                    size="sm"
                    onClick={handleAddDetalle}
                    disabled={insumos.length === 0}
                  >
                    <FontAwesomeIcon icon={faPlusCircle} /> Añadir Ingrediente
                  </Button>
                </Card.Header>
                <ListGroup variant="flush">
                  {formData.manufacturadoDetalles.length === 0 ? (
                    <ListGroup.Item className="text-center text-muted">
                      No hay ingredientes añadidos. Haz clic en "Añadir
                      Ingrediente" para empezar.
                    </ListGroup.Item>
                  ) : (
                    formData.manufacturadoDetalles.map((detalle, index) => {
                      const insumosYaEnOtrosDetalles =
                        formData.manufacturadoDetalles
                          .filter((_, i) => i !== index)
                          .map((d) => d.articuloInsumoId)
                          .filter((id) => id !== 0 && id !== undefined);

                      const opcionesDeInsumosDisponibles = insumos
                        .filter((insumo) =>
                          isProductWrapper
                            ? !insumo.esParaElaborar
                            : insumo.esParaElaborar
                        )
                        .filter(
                          (insumoFiltrado) =>
                            !insumosYaEnOtrosDetalles.includes(
                              insumoFiltrado.id!
                            ) || insumoFiltrado.id === detalle.articuloInsumoId
                        );

                      return (
                        <ListGroup.Item key={index}>
                          <Row className="align-items-center">
                            <Col xs={12} md={6}>
                              <Form.Group className="mb-3 mb-md-0">
                                <Form.Label>Insumo</Form.Label>
                                <Form.Select
                                  value={detalle.articuloInsumoId || ""}
                                  onChange={(e) =>
                                    handleDetalleChange(
                                      index,
                                      "articuloInsumoId",
                                      Number(e.target.value)
                                    )
                                  }
                                  disabled={
                                    opcionesDeInsumosDisponibles.length === 0
                                  }
                                  required
                                >
                                  <option value="">Selecciona un insumo</option>
                                  {opcionesDeInsumosDisponibles.map(
                                    (insumoOpcion) => (
                                      <option
                                        key={insumoOpcion.id}
                                        value={insumoOpcion.id}
                                      >
                                        {insumoOpcion.denominacion}
                                      </option>
                                    )
                                  )}
                                </Form.Select>
                              </Form.Group>
                            </Col>
                            <Col xs={8} md={4}>
                              <Form.Group className="mb-3 mb-md-0">
                                <Form.Label>Cantidad</Form.Label>
                                <InputGroup>
                                  <Form.Control
                                    type="number"
                                    value={detalle.cantidad}
                                    onChange={(e) =>
                                      handleDetalleChange(
                                        index,
                                        "cantidad",
                                        parseFloat(e.target.value)
                                      )
                                    }
                                    step="0.01"
                                    min="0.01"
                                    required
                                  />
                                  <InputGroup.Text>
                                    {insumos.find(
                                      (i) => i.id === detalle.articuloInsumoId
                                    )?.unidadMedida.denominacion || "Unidad"}
                                  </InputGroup.Text>
                                </InputGroup>
                              </Form.Group>
                            </Col>
                            <Col
                              xs={4}
                              md={2}
                              className="d-flex justify-content-end align-items-end"
                            >
                              <Button
                                variant="danger"
                                size="sm"
                                onClick={() => handleRemoveDetalle(index)}
                                className="mb-3 mb-md-0"
                              >
                                <FontAwesomeIcon icon={faMinusCircle} />
                              </Button>
                            </Col>
                          </Row>
                        </ListGroup.Item>
                      );
                    })
                  )}
                </ListGroup>
              </Card>
            </>
          )}
        </Modal.Body>
        <Modal.Footer>
          <Button
            variant="secondary"
            onClick={handleClose}
            disabled={submitting}
          >
            Cancelar
          </Button>
          <Button
            variant="primary"
            type="submit"
            disabled={submitting || loadingOptions}
          >
            {submitting ? (
              <Spinner
                as="span"
                animation="border"
                size="sm"
                role="status"
                aria-hidden="true"
                className="me-2"
              />
            ) : (
              ""
            )}
            {articuloToEdit ? "Actualizar" : "Crear"}
          </Button>
        </Modal.Footer>
      </Form>
    </Modal>
  );
};

export default ArticuloManufacturadoForm;

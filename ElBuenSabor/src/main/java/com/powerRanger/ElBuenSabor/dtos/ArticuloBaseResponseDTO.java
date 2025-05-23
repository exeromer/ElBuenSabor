package com.powerRanger.ElBuenSabor.dtos;

import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import java.util.List;
import java.util.ArrayList;

@JsonTypeInfo(
        use = JsonTypeInfo.Id.NAME,
        include = JsonTypeInfo.As.PROPERTY, // O As.EXISTING_PROPERTY si tienes un campo explícito para el tipo
        property = "type" // Nombre de la propiedad JSON que indicará el tipo
)
@JsonSubTypes({
        @JsonSubTypes.Type(value = ArticuloInsumoResponseDTO.class, name = "insumo"),
        @JsonSubTypes.Type(value = ArticuloManufacturadoResponseDTO.class, name = "manufacturado")
        // Si tienes una representación DTO para Articulo que NO es ni Insumo ni Manufacturado,
        // la añadirías aquí. Si no, puedes omitir esta clase base DTO y tener dos DTOs separados
        // que no hereden, y el servicio decidiría cuál crear y devolver Object o un DTO común.
        // Pero este enfoque polimórfico es más elegante para listas de Articulos.
})
public abstract class ArticuloBaseResponseDTO { // Marcada como abstracta
    private Integer id;
    private String denominacion;
    private Double precioVenta;
    private Boolean estadoActivo;
    // private java.time.LocalDate fechaBaja; // Si Articulo lo tiene

    private UnidadMedidaResponseDTO unidadMedida;
    private CategoriaResponseDTO categoria;
    private List<ImagenResponseDTO> imagenes = new ArrayList<>();

    // Getters y Setters
    public Integer getId() { return id; }
    public void setId(Integer id) { this.id = id; }
    public String getDenominacion() { return denominacion; }
    public void setDenominacion(String denominacion) { this.denominacion = denominacion; }
    public Double getPrecioVenta() { return precioVenta; }
    public void setPrecioVenta(Double precioVenta) { this.precioVenta = precioVenta; }
    public Boolean getEstadoActivo() { return estadoActivo; }
    public void setEstadoActivo(Boolean estadoActivo) { this.estadoActivo = estadoActivo; }
    // public java.time.LocalDate getFechaBaja() { return fechaBaja; }
    // public void setFechaBaja(java.time.LocalDate fechaBaja) { this.fechaBaja = fechaBaja; }
    public UnidadMedidaResponseDTO getUnidadMedida() { return unidadMedida; }
    public void setUnidadMedida(UnidadMedidaResponseDTO unidadMedida) { this.unidadMedida = unidadMedida; }
    public CategoriaResponseDTO getCategoria() { return categoria; }
    public void setCategoria(CategoriaResponseDTO categoria) { this.categoria = categoria; }
    public List<ImagenResponseDTO> getImagenes() { return imagenes; }
    public void setImagenes(List<ImagenResponseDTO> imagenes) { this.imagenes = imagenes; }
}
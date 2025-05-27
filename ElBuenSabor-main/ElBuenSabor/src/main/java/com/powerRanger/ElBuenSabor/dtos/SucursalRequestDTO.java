package com.powerRanger.ElBuenSabor.dtos;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import java.util.List;
import java.util.ArrayList;

public class SucursalRequestDTO {

    @NotEmpty(message = "El nombre de la sucursal no puede estar vacío")
    @Size(max = 255)
    private String nombre;

    @NotNull(message = "El horario de apertura es obligatorio")
    @Pattern(regexp = "^([01]?[0-9]|2[0-3]):[0-5][0-9]$", message = "El formato del horario de apertura debe ser HH:mm")
    private String horarioApertura;

    @NotNull(message = "El horario de cierre es obligatorio")
    @Pattern(regexp = "^([01]?[0-9]|2[0-3]):[0-5][0-9]$", message = "El formato del horario de cierre debe ser HH:mm")
    private String horarioCierre;

    @NotNull(message = "El ID de la empresa es obligatorio")
    private Integer empresaId;

    @NotNull(message = "El Domicilio es obligatorio para la sucursal")
    @Valid // Para validar el DomicilioRequestDTO anidado
    private DomicilioRequestDTO domicilio;

    private List<Integer> promocionIds = new ArrayList<>();
    private List<Integer> categoriaIds = new ArrayList<>();

    @NotNull(message = "El estado activo es obligatorio")
    private Boolean estadoActivo = true;

    // Getters y Setters
    public String getNombre() { return nombre; }
    public void setNombre(String nombre) { this.nombre = nombre; }
    public String getHorarioApertura() { return horarioApertura; }
    public void setHorarioApertura(String horarioApertura) { this.horarioApertura = horarioApertura; }
    public String getHorarioCierre() { return horarioCierre; }
    public void setHorarioCierre(String horarioCierre) { this.horarioCierre = horarioCierre; }
    public Integer getEmpresaId() { return empresaId; }
    public void setEmpresaId(Integer empresaId) { this.empresaId = empresaId; }
    public DomicilioRequestDTO getDomicilio() { return domicilio; }
    public void setDomicilio(DomicilioRequestDTO domicilio) { this.domicilio = domicilio; }
    public List<Integer> getPromocionIds() { return promocionIds; }
    public void setPromocionIds(List<Integer> promocionIds) { this.promocionIds = promocionIds; }
    public List<Integer> getCategoriaIds() { return categoriaIds; }
    public void setCategoriaIds(List<Integer> categoriaIds) { this.categoriaIds = categoriaIds; }
    public Boolean getEstadoActivo() { return estadoActivo; }
    public void setEstadoActivo(Boolean estadoActivo) { this.estadoActivo = estadoActivo; }
}
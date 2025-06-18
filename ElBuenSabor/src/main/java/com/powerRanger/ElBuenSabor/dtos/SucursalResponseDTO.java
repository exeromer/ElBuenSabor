package com.powerRanger.ElBuenSabor.dtos;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import java.util.ArrayList;

public class SucursalResponseDTO {
    private Integer id;
    private String nombre;
    private LocalTime horarioApertura;
    private LocalTime horarioCierre;
    private Boolean estadoActivo;
    private LocalDate fechaBaja;

    private EmpresaResponseDTO empresa; // Usaremos el DTO de Empresa
    private DomicilioResponseDTO domicilio; // Usaremos el DTO de Domicilio

    // Para Categorias y Promociones, podríamos usar DTOs simples o solo sus IDs
    private List<CategoriaResponseDTO> categorias = new ArrayList<>(); // Lista de DTOs de Categoria
    private List<PromocionSimpleResponseDTO> promociones = new ArrayList<>(); // O List<Integer> promocionIds

    // Getters y Setters
    public Integer getId() { return id; }
    public void setId(Integer id) { this.id = id; }
    public String getNombre() { return nombre; }
    public void setNombre(String nombre) { this.nombre = nombre; }
    public LocalTime getHorarioApertura() { return horarioApertura; }
    public void setHorarioApertura(LocalTime horarioApertura) { this.horarioApertura = horarioApertura; }
    public LocalTime getHorarioCierre() { return horarioCierre; }
    public void setHorarioCierre(LocalTime horarioCierre) { this.horarioCierre = horarioCierre; }
    public Boolean getEstadoActivo() { return estadoActivo; }
    public void setEstadoActivo(Boolean estadoActivo) { this.estadoActivo = estadoActivo; }
    public LocalDate getFechaBaja() { return fechaBaja; }
    public void setFechaBaja(LocalDate fechaBaja) { this.fechaBaja = fechaBaja; }
    public EmpresaResponseDTO getEmpresa() { return empresa; }
    public void setEmpresa(EmpresaResponseDTO empresa) { this.empresa = empresa; }
    public DomicilioResponseDTO getDomicilio() { return domicilio; }
    public void setDomicilio(DomicilioResponseDTO domicilio) { this.domicilio = domicilio; }
    public List<CategoriaResponseDTO> getCategorias() { return categorias; }
    public void setCategorias(List<CategoriaResponseDTO> categorias) { this.categorias = categorias; }
    public List<PromocionSimpleResponseDTO> getPromociones() { return promociones; }
    public void setPromociones(List<PromocionSimpleResponseDTO> promociones) { this.promociones = promociones; }
}
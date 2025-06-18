package com.powerRanger.ElBuenSabor.dtos;

// Si decidimos incluir información de sucursales, importaríamos SucursalSimpleResponseDTO aquí
// import java.util.List;
// import java.util.ArrayList;

public class EmpresaResponseDTO {
    private Integer id;
    private String nombre;
    private String razonSocial;
    private String cuil;
    // private List<SucursalSimpleResponseDTO> sucursales = new ArrayList<>(); // Opcional para después

    // Getters y Setters
    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public String getNombre() {
        return nombre;
    }

    public void setNombre(String nombre) {
        this.nombre = nombre;
    }

    public String getRazonSocial() {
        return razonSocial;
    }

    public void setRazonSocial(String razonSocial) {
        this.razonSocial = razonSocial;
    }

    public String getCuil() {
        return cuil;
    }

    public void setCuil(String cuil) {
        this.cuil = cuil;
    }

    // public List<SucursalSimpleResponseDTO> getSucursales() { return sucursales; }
    // public void setSucursales(List<SucursalSimpleResponseDTO> sucursales) { this.sucursales = sucursales; }
}
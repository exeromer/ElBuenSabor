package com.powerRanger.ElBuenSabor.dtos;

import com.powerRanger.ElBuenSabor.entities.enums.Rol;
import java.time.LocalDate; // Para fechaBaja

public class UsuarioResponseDTO {
    private Integer id;
    private String auth0Id;
    private String username;
    private Rol rol;
    private Boolean estadoActivo;
    private LocalDate fechaBaja;
    private EmpleadoResponseDTO empleado;

    // Getters y Setters
    public Integer getId() { return id; }
    public void setId(Integer id) { this.id = id; }
    public String getAuth0Id() { return auth0Id; }
    public void setAuth0Id(String auth0Id) { this.auth0Id = auth0Id; }
    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }
    public Rol getRol() { return rol; }
    public void setRol(Rol rol) { this.rol = rol; }
    public Boolean getEstadoActivo() { return estadoActivo; }
    public void setEstadoActivo(Boolean estadoActivo) { this.estadoActivo = estadoActivo; }
    public LocalDate getFechaBaja() { return fechaBaja; }
    public void setFechaBaja(LocalDate fechaBaja) { this.fechaBaja = fechaBaja; }
    public EmpleadoResponseDTO getEmpleado() {return empleado;}
    public void setEmpleado(EmpleadoResponseDTO empleado) {this.empleado = empleado;}
}
package com.powerRanger.ElBuenSabor.dtos;

import com.powerRanger.ElBuenSabor.entities.enums.Rol;
import java.time.LocalDate; // Para fechaBaja

public class UsuarioResponseDTO {
    private Integer id;
    private String username;
    private Rol rol;
    private Boolean estadoActivo;
    private LocalDate fechaBaja; // Podría ser útil mostrarla
    // Omitimos auth0Id por seguridad en respuestas generales

    // Getters y Setters
    public Integer getId() { return id; }
    public void setId(Integer id) { this.id = id; }
    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }
    public Rol getRol() { return rol; }
    public void setRol(Rol rol) { this.rol = rol; }
    public Boolean getEstadoActivo() { return estadoActivo; }
    public void setEstadoActivo(Boolean estadoActivo) { this.estadoActivo = estadoActivo; }
    public LocalDate getFechaBaja() { return fechaBaja; }
    public void setFechaBaja(LocalDate fechaBaja) { this.fechaBaja = fechaBaja; }
}
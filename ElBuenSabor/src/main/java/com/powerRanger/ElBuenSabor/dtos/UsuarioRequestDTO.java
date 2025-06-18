package com.powerRanger.ElBuenSabor.dtos;

import com.powerRanger.ElBuenSabor.entities.enums.Rol; // Importar el Enum Rol
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;

public class UsuarioRequestDTO {

    @NotEmpty(message = "El auth0Id no puede estar vacío")
    private String auth0Id;

    @NotEmpty(message = "El username no puede estar vacío")
    private String username;

    @NotNull(message = "El rol es obligatorio")
    private Rol rol; // El cliente enviará el nombre del Enum ("ADMIN", "CLIENTE", "EMPLEADO")

    @NotNull(message = "El estado activo es obligatorio")
    private Boolean estadoActivo = true;

    // Getters y Setters
    public String getAuth0Id() { return auth0Id; }
    public void setAuth0Id(String auth0Id) { this.auth0Id = auth0Id; }
    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }
    public Rol getRol() { return rol; }
    public void setRol(Rol rol) { this.rol = rol; }
    public Boolean getEstadoActivo() { return estadoActivo; }
    public void setEstadoActivo(Boolean estadoActivo) { this.estadoActivo = estadoActivo; }
}
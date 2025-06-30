package com.powerRanger.ElBuenSabor.dtos;

import com.powerRanger.ElBuenSabor.entities.enums.RolEmpleado;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public class EmpleadoRequestDTO {

    @NotEmpty(message = "El nombre no puede estar vacío")
    private String nombre;

    @NotEmpty(message = "El apellido no puede estar vacío")
    private String apellido;

    @Size(min = 7, max = 20, message = "El teléfono debe tener entre 7 y 20 caracteres")
    private String telefono;

    @NotNull(message = "El rol de empleado es obligatorio")
    private RolEmpleado rolEmpleado;

    @NotNull(message = "El ID de usuario es obligatorio")
    private Integer usuarioId; // ID del Usuario a asociar

    @NotNull(message = "El estado activo es obligatorio")
    private Boolean estadoActivo = true;

    // Getters y Setters
    public String getNombre() { return nombre; }
    public void setNombre(String nombre) { this.nombre = nombre; }
    public String getApellido() { return apellido; }
    public void setApellido(String apellido) { this.apellido = apellido; }
    public String getTelefono() { return telefono; }
    public void setTelefono(String telefono) { this.telefono = telefono; }
    public RolEmpleado getRolEmpleado() { return rolEmpleado; }
    public void setRolEmpleado(RolEmpleado rolEmpleado) { this.rolEmpleado = rolEmpleado; }
    public Integer getUsuarioId() { return usuarioId; }
    public void setUsuarioId(Integer usuarioId) { this.usuarioId = usuarioId; }
    public Boolean getEstadoActivo() { return estadoActivo; }
    public void setEstadoActivo(Boolean estadoActivo) { this.estadoActivo = estadoActivo; }
}
package com.powerRanger.ElBuenSabor.dtos;

import com.powerRanger.ElBuenSabor.entities.enums.RolEmpleado;
import java.time.LocalDate;

public class EmpleadoResponseDTO {
    private Integer id;
    private String nombre;
    private String apellido;
    private String telefono;
    private RolEmpleado rolEmpleado;
    private Integer usuarioId;
    private String usernameUsuario;
    private Boolean estadoActivo;
    private LocalDate fechaBaja;

    // Getters y Setters
    public Integer getId() { return id; }
    public void setId(Integer id) { this.id = id; }
    public String getNombre() { return nombre; }
    public void setNombre(String nombre) { this.nombre = nombre; }
    public String getApellido() { return apellido; }
    public void setApellido(String apellido) { this.apellido = apellido; }
    public String getTelefono() { return telefono; }
    public void setTelefono(String telefono) { this.telefono = telefono; }
    public RolEmpleado getRolEmpleado() { return rolEmpleado; }
    public void setRolEmpleado(RolEmpleado rolEmpleado) { this.rolEmpleado = rolEmpleado; }
    public Integer getUsuarioId() { return usuarioId; }
    public void voidSetUsuarioId(Integer usuarioId) { this.usuarioId = usuarioId; }
    public String getUsernameUsuario() { return usernameUsuario; }
    public void setUsernameUsuario(String usernameUsuario) { this.usernameUsuario = usernameUsuario; }
    public Boolean getEstadoActivo() { return estadoActivo; }
    public void setEstadoActivo(Boolean estadoActivo) { this.estadoActivo = estadoActivo; }
    public LocalDate getFechaBaja() { return fechaBaja; }
    public void setFechaBaja(LocalDate fechaBaja) { this.fechaBaja = fechaBaja; }
}
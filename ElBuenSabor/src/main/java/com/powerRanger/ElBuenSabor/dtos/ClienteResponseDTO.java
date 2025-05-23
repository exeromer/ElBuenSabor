package com.powerRanger.ElBuenSabor.dtos;

import com.powerRanger.ElBuenSabor.entities.enums.Rol; // Asumiendo que quieres mostrar el Rol
import java.time.LocalDate;
import java.util.List;
import java.util.ArrayList;

public class ClienteResponseDTO {
    private Integer id;
    private String nombre;
    private String apellido;
    private String telefono;
    private String email;
    private LocalDate fechaNacimiento;
    private Boolean estadoActivo;
    private LocalDate fechaBaja;

    // Información simplificada del Usuario asociado
    private Integer usuarioId;
    private String username;
    private Rol rolUsuario; // Mostrar el rol del usuario

    // Lista de DTOs de Domicilio (usaremos DomicilioResponseDTO que ya creamos)
    private List<DomicilioResponseDTO> domicilios = new ArrayList<>();

    // Opcional: URL de la imagen (si ImagenResponseDTO es simple o solo la URL)
    // private String imagenUrl;
    // O un ImagenResponseDTO completo
    // private ImagenResponseDTO imagen;


    // Getters y Setters
    public Integer getId() { return id; }
    public void setId(Integer id) { this.id = id; }
    public String getNombre() { return nombre; }
    public void setNombre(String nombre) { this.nombre = nombre; }
    public String getApellido() { return apellido; }
    public void setApellido(String apellido) { this.apellido = apellido; }
    public String getTelefono() { return telefono; }
    public void setTelefono(String telefono) { this.telefono = telefono; }
    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }
    public LocalDate getFechaNacimiento() { return fechaNacimiento; }
    public void setFechaNacimiento(LocalDate fechaNacimiento) { this.fechaNacimiento = fechaNacimiento; }
    public Boolean getEstadoActivo() { return estadoActivo; }
    public void setEstadoActivo(Boolean estadoActivo) { this.estadoActivo = estadoActivo; }
    public LocalDate getFechaBaja() { return fechaBaja; }
    public void setFechaBaja(LocalDate fechaBaja) { this.fechaBaja = fechaBaja; }
    public Integer getUsuarioId() { return usuarioId; }
    public void setUsuarioId(Integer usuarioId) { this.usuarioId = usuarioId; }
    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }
    public Rol getRolUsuario() { return rolUsuario; }
    public void setRolUsuario(Rol rolUsuario) { this.rolUsuario = rolUsuario; }
    public List<DomicilioResponseDTO> getDomicilios() { return domicilios; }
    public void setDomicilios(List<DomicilioResponseDTO> domicilios) { this.domicilios = domicilios; }
    // public String getImagenUrl() { return imagenUrl; }
    // public void setImagenUrl(String imagenUrl) { this.imagenUrl = imagenUrl; }
    // public ImagenResponseDTO getImagen() { return imagen; }
    // public void setImagen(ImagenResponseDTO imagen) { this.imagen = imagen; }
}
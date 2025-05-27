package com.powerRanger.ElBuenSabor.dtos;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PastOrPresent;
import jakarta.validation.constraints.Size;
import java.time.LocalDate;
import java.util.List;
import java.util.ArrayList;

public class ClienteRequestDTO {

    @NotEmpty(message = "El nombre no puede estar vacío")
    private String nombre;

    @NotEmpty(message = "El apellido no puede estar vacío")
    private String apellido;

    @Size(min = 7, max = 20, message = "El teléfono debe tener entre 7 y 20 caracteres")
    private String telefono;

    @NotEmpty(message = "El email no puede estar vacío")
    @Email(message = "El formato del email no es válido")
    private String email; // Se podría validar unicidad en el servicio

    @PastOrPresent(message = "La fecha de nacimiento no puede ser futura")
    private LocalDate fechaNacimiento;

    @NotNull(message = "El ID de usuario es obligatorio")
    private Integer usuarioId; // ID del Usuario a asociar

    // Para la imagen, la subida de archivos se maneja diferente.
    // Podríamos tener un campo para una URL de imagen o un ID de imagen si ya está subida.
    // Por ahora, lo omitimos del DTO de creación/actualización simple.
    // private Integer imagenId;
    // private String imagenUrl;

    // Los domicilios se podrían asociar mediante una lista de IDs o DTOs de domicilio.
    // Para simplificar la creación inicial, podríamos pedir una lista de IDs de Domicilios existentes.
    private List<Integer> domicilioIds = new ArrayList<>();

    @NotNull(message = "El estado activo es obligatorio")
    private Boolean estadoActivo = true;


    // Getters y Setters
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
    public Integer getUsuarioId() { return usuarioId; }
    public void setUsuarioId(Integer usuarioId) { this.usuarioId = usuarioId; }
    public List<Integer> getDomicilioIds() { return domicilioIds; }
    public void setDomicilioIds(List<Integer> domicilioIds) { this.domicilioIds = domicilioIds; }
    public Boolean getEstadoActivo() { return estadoActivo; }
    public void setEstadoActivo(Boolean estadoActivo) { this.estadoActivo = estadoActivo; }
}
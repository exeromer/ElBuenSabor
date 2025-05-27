package com.powerRanger.ElBuenSabor.dtos;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
// import jakarta.validation.constraints.Pattern; // Si usas validación de patrón para cp

public class DomicilioRequestDTO {
    @NotEmpty(message = "La calle no puede estar vacía")
    private String calle;

    @NotNull(message = "El número no puede ser nulo")
    private Integer numero;

    @NotNull(message = "El código postal es obligatorio")
    // @Pattern(regexp = "^[0-9]{4}$", message = "El código postal debe tener 4 dígitos") // Ejemplo, ajusta
    private String cp;

    @NotNull(message = "El ID de la localidad es obligatorio")
    private Integer localidadId;

    // Getters y Setters
    public String getCalle() { return calle; }
    public void setCalle(String calle) { this.calle = calle; }
    public Integer getNumero() { return numero; }
    public void setNumero(Integer numero) { this.numero = numero; }
    public String getCp() { return cp; }
    public void setCp(String cp) { this.cp = cp; }
    public Integer getLocalidadId() { return localidadId; }
    public void setLocalidadId(Integer localidadId) { this.localidadId = localidadId; }
}
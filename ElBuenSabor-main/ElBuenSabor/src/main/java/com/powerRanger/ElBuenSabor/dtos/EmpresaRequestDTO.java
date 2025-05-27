package com.powerRanger.ElBuenSabor.dtos;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
// Los DTOs para Sucursal se manejarían al crear/actualizar Sucursal,
// o podrías tener un SucursalIdDTO aquí si quisieras asociar existentes.
// Por ahora, la creación de Empresa no asociará sucursales directamente vía DTO.

public class EmpresaRequestDTO {

    @NotEmpty(message = "El nombre de la empresa no puede estar vacío")
    @Size(max = 255, message = "El nombre de la empresa no puede exceder los 255 caracteres")
    private String nombre;

    @NotEmpty(message = "La razón social no puede estar vacía")
    @Size(max = 255, message = "La razón social no puede exceder los 255 caracteres")
    private String razonSocial;

    @NotEmpty(message = "El CUIL/CUIT es obligatorio")
    @Pattern(regexp = "^(20|23|24|27|30|33|34)-[0-9]{8}-[0-9]{1}$", message = "El formato del CUIL/CUIT no es válido. Ejemplo: 20-12345678-9")
    private String cuil;

    // Las sucursales se gestionarán a través de los endpoints de Sucursal,
    // que luego se asociarán a una Empresa. No las incluimos en el DTO de Empresa
    // para la creación/actualización básica de la Empresa misma.

    // Getters y Setters
    public String getNombre() { return nombre; }
    public void setNombre(String nombre) { this.nombre = nombre; }
    public String getRazonSocial() { return razonSocial; }
    public void setRazonSocial(String razonSocial) { this.razonSocial = razonSocial; }
    public String getCuil() { return cuil; }
    public void setCuil(String cuil) { this.cuil = cuil; }
}
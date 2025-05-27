package com.powerRanger.ElBuenSabor.dtos;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

public class UpdateCartItemQuantityRequestDTO {

    @NotNull(message = "La nueva cantidad no puede ser nula")
    @Min(value = 0, message = "La nueva cantidad no puede ser negativa.")
    // Si es 0, el servicio lo interpretará como una eliminación.
    // Si quieres que el mínimo sea 1 aquí, cámbialo a @Min(value = 1)
    // y el servicio debería lanzar error si es 0 en lugar de eliminar.
    private Integer nuevaCantidad;

    public UpdateCartItemQuantityRequestDTO() {
    }

    public Integer getNuevaCantidad() {
        return nuevaCantidad;
    }

    public void setNuevaCantidad(Integer nuevaCantidad) {
        this.nuevaCantidad = nuevaCantidad;
    }
}
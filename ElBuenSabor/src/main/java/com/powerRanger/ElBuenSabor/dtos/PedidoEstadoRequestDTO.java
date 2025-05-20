package com.powerRanger.ElBuenSabor.dtos;
import com.powerRanger.ElBuenSabor.entities.enums.Estado;
import jakarta.validation.constraints.NotNull;
public class PedidoEstadoRequestDTO {
    @NotNull(message = "El nuevo estado es obligatorio")
    private Estado nuevoEstado;
    public Estado getNuevoEstado() { return nuevoEstado; }
    public void setNuevoEstado(Estado nuevoEstado) { this.nuevoEstado = nuevoEstado; }
}
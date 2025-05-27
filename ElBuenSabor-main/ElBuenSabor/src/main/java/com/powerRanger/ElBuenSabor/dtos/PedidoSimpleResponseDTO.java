package com.powerRanger.ElBuenSabor.dtos;

import com.fasterxml.jackson.annotation.JsonFormat;
import java.time.LocalDate;
//CLASE PARA SER MÁS CORTA LA RESPUESTA DE FACTURA SIN DETALLES INNECESARIOS
public class PedidoSimpleResponseDTO {
    private Integer id;
    @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDate fechaPedido;

    // Getters y Setters
    public Integer getId() { return id; }
    public void setId(Integer id) { this.id = id; }
    public LocalDate getFechaPedido() { return fechaPedido; }
    public void setFechaPedido(LocalDate fechaPedido) { this.fechaPedido = fechaPedido; }
}
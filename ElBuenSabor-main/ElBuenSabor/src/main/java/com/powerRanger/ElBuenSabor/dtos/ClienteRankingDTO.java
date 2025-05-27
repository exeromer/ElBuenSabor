package com.powerRanger.ElBuenSabor.dtos;

public class ClienteRankingDTO {
    private Integer clienteId;
    private String nombreCompleto; // Concatenación de nombre y apellido
    private String email;
    private Long cantidadPedidos;    // Cuántos pedidos ha hecho
    private Double montoTotalComprado; // Suma total de sus compras

    // Constructor para usar en la consulta JPQL del repositorio
    public ClienteRankingDTO(Integer clienteId, String nombreCompleto, String email, Long cantidadPedidos, Double montoTotalComprado) {
        this.clienteId = clienteId;
        this.nombreCompleto = nombreCompleto;
        this.email = email;
        this.cantidadPedidos = cantidadPedidos;
        this.montoTotalComprado = montoTotalComprado;
    }

    // Constructor por defecto (buena práctica)
    public ClienteRankingDTO() {}

    // Getters y Setters
    public Integer getClienteId() { return clienteId; }
    public void setClienteId(Integer clienteId) { this.clienteId = clienteId; }
    public String getNombreCompleto() { return nombreCompleto; }
    public void setNombreCompleto(String nombreCompleto) { this.nombreCompleto = nombreCompleto; }
    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }
    public Long getCantidadPedidos() { return cantidadPedidos; }
    public void setCantidadPedidos(Long cantidadPedidos) { this.cantidadPedidos = cantidadPedidos; }
    public Double getMontoTotalComprado() { return montoTotalComprado; }
    public void setMontoTotalComprado(Double montoTotalComprado) { this.montoTotalComprado = montoTotalComprado; }
}
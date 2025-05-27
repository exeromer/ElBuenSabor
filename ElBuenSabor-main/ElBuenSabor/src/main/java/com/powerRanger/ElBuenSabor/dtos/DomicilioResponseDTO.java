package com.powerRanger.ElBuenSabor.dtos;

// Asumimos que LocalidadResponseDTO ya existe y tiene al menos id y nombre.
// Si LocalidadResponseDTO anida ProvinciaResponseDTO, y este a su vez PaisResponseDTO,
// esa estructura se reflejará aquí.
// import com.powerRanger.ElBuenSabor.dtos.LocalidadResponseDTO;

import java.util.List; // Para los IDs de clientes

public class DomicilioResponseDTO {
    private Integer id;
    private String calle;
    private Integer numero;
    private String cp;
    private LocalidadResponseDTO localidad; // Para anidar la información de la localidad
    // Opcional: podríamos incluir una lista de IDs de clientes asociados
    // private List<Integer> clienteIds;

    // Getters y Setters
    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public String getCalle() {
        return calle;
    }

    public void setCalle(String calle) {
        this.calle = calle;
    }

    public Integer getNumero() {
        return numero;
    }

    public void setNumero(Integer numero) {
        this.numero = numero;
    }

    public String getCp() {
        return cp;
    }

    public void setCp(String cp) {
        this.cp = cp;
    }

    public LocalidadResponseDTO getLocalidad() {
        return localidad;
    }

    public void setLocalidad(LocalidadResponseDTO localidad) {
        this.localidad = localidad;
    }

    // public List<Integer> getClienteIds() { return clienteIds; }
    // public void setClienteIds(List<Integer> clienteIds) { this.clienteIds = clienteIds; }
}
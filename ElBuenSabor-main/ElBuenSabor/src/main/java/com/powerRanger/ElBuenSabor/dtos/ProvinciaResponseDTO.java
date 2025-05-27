package com.powerRanger.ElBuenSabor.dtos;

// Asumimos que ya tienes PaisResponseDTO
// import com.powerRanger.ElBuenSabor.dtos.PaisResponseDTO;

public class ProvinciaResponseDTO {
    private Integer id;
    private String nombre;
    private PaisResponseDTO pais; // Para mostrar información del país
    // Podríamos añadir List<Integer> localidadIds; si quisiéramos los IDs de sus localidades

    // Getters y Setters
    public Integer getId() { return id; }
    public void setId(Integer id) { this.id = id; }
    public String getNombre() { return nombre; }
    public void setNombre(String nombre) { this.nombre = nombre; }
    public PaisResponseDTO getPais() { return pais; }
    public void setPais(PaisResponseDTO pais) { this.pais = pais; }
}
package com.powerRanger.ElBuenSabor.dtos;

// Asumimos que ya tienes ProvinciaResponseDTO
// import com.powerRanger.ElBuenSabor.dtos.ProvinciaResponseDTO;

public class LocalidadResponseDTO {
    private Integer id;
    private String nombre;
    private ProvinciaResponseDTO provincia; // Para mostrar información de la provincia

    // Getters y Setters
    public Integer getId() { return id; }
    public void setId(Integer id) { this.id = id; }
    public String getNombre() { return nombre; }
    public void setNombre(String nombre) { this.nombre = nombre; }
    public ProvinciaResponseDTO getProvincia() { return provincia; }
    public void setProvincia(ProvinciaResponseDTO provincia) { this.provincia = provincia; }
}
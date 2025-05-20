package com.powerRanger.ElBuenSabor.entities;

import com.fasterxml.jackson.annotation.JsonIdentityInfo; // Importa esta
import com.fasterxml.jackson.annotation.ObjectIdGenerators; // Importa esta
import jakarta.persistence.*;
import java.util.List;

@Entity
@JsonIdentityInfo(
        generator = ObjectIdGenerators.PropertyGenerator.class,
        property = "id") // Le dice a Jackson que use el campo 'id' de Pais como identificador único
public class Pais {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(nullable = true)
    private String nombre;

    // No es necesario cambiar @JsonManagedReference si ya lo tenías,
    // pero @JsonIdentityInfo a menudo lo hace innecesario o puede usarse en conjunto.
    // Para empezar, prueba solo con @JsonIdentityInfo en ambas clases.
    @OneToMany(mappedBy = "pais", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<Provincia> provincias;

    // Getters y Setters...
    public Integer getId() { return id; }
    public void setId(Integer id) { this.id = id; }
    public String getNombre() { return nombre; }
    public void setNombre(String nombre) { this.nombre = nombre; }
    public List<Provincia> getProvincias() { return provincias; }
    public void setProvincias(List<Provincia> provincias) { this.provincias = provincias; }
}
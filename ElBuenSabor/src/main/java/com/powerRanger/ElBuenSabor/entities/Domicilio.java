package com.powerRanger.ElBuenSabor.entities;

import com.fasterxml.jackson.annotation.JsonIdentityInfo;
import com.fasterxml.jackson.annotation.ObjectIdGenerators;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern; // Para el código postal

import java.util.ArrayList; // Para inicializar la lista de clientes
import java.util.List;
import java.util.Objects;

@Entity
@JsonIdentityInfo(
        generator = ObjectIdGenerators.PropertyGenerator.class,
        property = "id")
public class Domicilio {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(nullable = false)
    @NotEmpty(message = "La calle no puede estar vacía")
    private String calle;

    @Column(nullable = false)
    @NotNull(message = "El número no puede ser nulo")
    // @Min(1) si el número debe ser positivo
    private Integer numero;

    @Column(nullable = false, length = 8) // Asumiendo que el CP siempre es obligatorio
    @NotNull(message = "El código postal es obligatorio")
    // Ejemplo de validación para CP argentino (4 dígitos numéricos, opcionalmente una letra y 3 más)
    // Ajusta el patrón según tus necesidades exactas. Este es solo un ejemplo.
    // @Pattern(regexp = "^[0-9]{4}(?:[A-Z]{1}[0-9]{3})?|[A-Z]{1}[0-9]{4}[A-Z]{3}$", message = "El formato del código postal no es válido")
    private String cp; // Cambiado a String para mayor flexibilidad con CPs que pueden tener letras o guiones

    @NotNull(message = "La localidad es obligatoria")
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "localidad_id", nullable = false)
    private Localidad localidad;

    // mappedBy indica que Cliente es el dueño de la relación ManyToMany.
    // Un Domicilio puede estar asociado a varios Clientes (ej. familia en misma dirección).
    // Y un Cliente puede tener varios Domicilios (ej. casa, trabajo).
    // CascadeType aquí debe ser cuidadoso. Usualmente no se eliminan Clientes si se borra un Domicilio.
    // FetchType.LAZY es bueno para colecciones.
    @ManyToMany(mappedBy = "domicilios", fetch = FetchType.LAZY)
    private List<Cliente> clientes = new ArrayList<>(); // Inicializar colección

    // Constructor
    public Domicilio() {
        this.clientes = new ArrayList<>();
    }

    // Getters y Setters
    public Integer getId() { return id; }
    public void setId(Integer id) { this.id = id; }
    public String getCalle() { return calle; }
    public void setCalle(String calle) { this.calle = calle; }
    public Integer getNumero() { return numero; }
    public void setNumero(Integer numero) { this.numero = numero; }
    public String getCp() { return cp; } // Devuelve String
    public void setCp(String cp) { this.cp = cp; } // Acepta String
    public Localidad getLocalidad() { return localidad; }
    public void setLocalidad(Localidad localidad) { this.localidad = localidad; }
    public List<Cliente> getClientes() { return clientes; }
    public void setClientes(List<Cliente> clientes) { this.clientes = clientes; }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Domicilio domicilio = (Domicilio) o;
        return Objects.equals(id, domicilio.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }

    @Override
    public String toString() {
        return "Domicilio{" +
                "id=" + id +
                ", calle='" + calle + '\'' +
                ", numero=" + numero +
                ", cp='" + cp + '\'' +
                ", localidad=" + (localidad != null ? localidad.getNombre() : "null") +
                '}';
    }
}
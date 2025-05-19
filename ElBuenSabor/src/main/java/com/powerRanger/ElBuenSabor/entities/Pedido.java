package com.powerRanger.ElBuenSabor.entities;

import com.powerRanger.ElBuenSabor.entities.enums.Estado;
import com.powerRanger.ElBuenSabor.entities.enums.FormaPago;
import com.powerRanger.ElBuenSabor.entities.enums.TipoEnvio;
import jakarta.persistence.*;
import java.util.ArrayList;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

@Entity
public class Pedido {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY) // <-- Añadido
    private Integer id;  // ID del pedido

    @Column(nullable = true)
    private LocalTime horaEstimadaFinalizacion;

    @Column(nullable = true)
    private Double total;

    @Column(nullable = true)
    private Double totalCosto;

    @Column(nullable = true)
    private LocalDate fechaPedido;

    @ManyToOne
    private Sucursal sucursal;  // Relación muchos a uno con Sucursal

    @ManyToOne(cascade = CascadeType.PERSIST) // Considera Cascade si Domicilio debe crearse con Pedido
    private Domicilio domicilio;

    // mappedBy indica que Factura es la dueña de la relación
    @OneToOne(mappedBy = "pedido", cascade = CascadeType.ALL, orphanRemoval = true)
    private Factura factura;

    @Enumerated(EnumType.STRING) // <-- Añadido
    private Estado estado;

    @Enumerated(EnumType.STRING) // <-- Añadido
    private TipoEnvio tipoEnvio;

    @Enumerated(EnumType.STRING) // <-- Añadido
    private FormaPago formaPago;

    @ManyToOne // Relación ManyToOne con Cliente
    @JoinColumn(name = "cliente_id") // La columna de unión que hace referencia a Cliente
    private Cliente cliente;

    @OneToMany(mappedBy = "pedido", cascade = CascadeType.ALL, orphanRemoval = true)  // Relación bidireccional con DetallePedido
    private List<DetallePedido> detalles;  // Relación uno a muchos con DetallePedido

    @Column(name = "estadoActivo")
    private Boolean estadoActivo;  // Estado activo

    public Integer getId() {
        return id;
    }

    public LocalTime getHoraEstimadaFinalizacion() {
        return horaEstimadaFinalizacion;
    }

    public Double getTotal() {
        return total;
    }

    public Double getTotalCosto() {
        return totalCosto;
    }

    public LocalDate getFechaPedido() {
        return fechaPedido;
    }

    public Sucursal getSucursal() {
        return sucursal;
    }

    public Domicilio getDomicilio() {
        return domicilio;
    }

    public Factura getFactura() {
        return factura;
    }

    public Estado getEstado() {
        return estado;
    }

    public TipoEnvio getTipoEnvio() {
        return tipoEnvio;
    }

    public FormaPago getFormaPago() {
        return formaPago;
    }

    public Cliente getCliente() {
        return cliente;
    }

    public List<DetallePedido> getDetalles() {
        return detalles;
    }

    public Boolean getEstadoActivo() {
        return estadoActivo;
    }

    public void setHoraEstimadaFinalizacion(LocalTime horaEstimadaFinalizacion) {
        this.horaEstimadaFinalizacion = horaEstimadaFinalizacion;
    }

    public void setTotal(Double total) {
        this.total = total;
    }

    public void setTotalCosto(Double totalCosto) {
        this.totalCosto = totalCosto;
    }

    public void setFechaPedido(LocalDate fechaPedido) {
        this.fechaPedido = fechaPedido;
    }

    public void setSucursal(Sucursal sucursal) {
        this.sucursal = sucursal;
    }

    public void setDomicilio(Domicilio domicilio) {
        this.domicilio = domicilio;
    }

    public void setFactura(Factura factura) {
        this.factura = factura;
    }

    public void setEstado(Estado estado) {
        this.estado = estado;
    }

    public void setTipoEnvio(TipoEnvio tipoEnvio) {
        this.tipoEnvio = tipoEnvio;
    }

    public void setFormaPago(FormaPago formaPago) {
        this.formaPago = formaPago;
    }

    public void setCliente(Cliente cliente) {
        this.cliente = cliente;
    }

    public void setDetalles(List<DetallePedido> detalles) {
        this.detalles = detalles;
    }

    public void setEstadoActivo(Boolean estadoActivo) {
        this.estadoActivo = estadoActivo;
    }

    public void addDetalle(DetallePedido detalle) {
        if (this.detalles == null) {
            this.detalles = new ArrayList<>();
        }
        this.detalles.add(detalle);
        detalle.setPedido(this); // Establece la relación bidireccional
    }
}

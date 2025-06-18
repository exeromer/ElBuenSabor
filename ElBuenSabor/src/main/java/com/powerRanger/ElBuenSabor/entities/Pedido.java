package com.powerRanger.ElBuenSabor.entities;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonIdentityInfo;
import com.fasterxml.jackson.annotation.ObjectIdGenerators;
import com.powerRanger.ElBuenSabor.entities.enums.Estado;
import com.powerRanger.ElBuenSabor.entities.enums.FormaPago;
import com.powerRanger.ElBuenSabor.entities.enums.TipoEnvio;
import jakarta.persistence.*;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PastOrPresent;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

@Entity
@JsonIdentityInfo(
        generator = ObjectIdGenerators.PropertyGenerator.class,
        property = "id")
public class Pedido {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @NotNull(message = "La hora estimada de finalización es obligatoria")
    @JsonFormat(pattern = "HH:mm:ss")
    private LocalTime horaEstimadaFinalizacion;

    @NotNull(message = "El total del pedido no puede ser nulo")
    @DecimalMin(value = "0.0", message = "El total no puede ser negativo")
    private Double total;

    @DecimalMin(value = "0.0", message = "El total de costo no puede ser negativo")
    private Double totalCosto;

    @NotNull(message = "La fecha del pedido es obligatoria")
    @PastOrPresent(message = "La fecha del pedido no puede ser futura")
    @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDate fechaPedido;

    @NotNull(message = "La sucursal es obligatoria")
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "sucursal_id", nullable = false)
    private Sucursal sucursal;

    @NotNull(message = "El domicilio de entrega es obligatorio")
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "domicilio_id", nullable = false)
    private Domicilio domicilio;

    @OneToOne(mappedBy = "pedido", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    private Factura factura;

    @NotNull(message = "El estado del pedido es obligatorio")
    @Enumerated(EnumType.STRING)
    private Estado estado;

    @NotNull(message = "El tipo de envío es obligatorio")
    @Enumerated(EnumType.STRING)
    private TipoEnvio tipoEnvio;

    @NotNull(message = "La forma de pago es obligatoria")
    @Enumerated(EnumType.STRING)
    private FormaPago formaPago;

    @NotNull(message = "El cliente es obligatorio")
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "cliente_id", nullable = false)
    private Cliente cliente;

    @OneToMany(mappedBy = "pedido", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    private List<DetallePedido> detalles = new ArrayList<>();

    @Column(name = "fechaBaja")
    private LocalDate fechaBaja;

    @Column(name = "estadoActivo", nullable = false)
    @NotNull(message = "El estado activo es obligatorio")
    private Boolean estadoActivo = true;

    @Column(name = "mp_preference_id")
    private String mpPreferenceId;

    @Column(name = "mp_payment_id")
    private String mpPaymentId;

    @Column(name = "mp_payment_status")
    private String mpPaymentStatus;

    @Column(name = "descuento_aplicado")
    private Double descuentoAplicado;

    public Pedido() {
        this.detalles = new ArrayList<>();
        this.fechaPedido = LocalDate.now();
        this.estado = Estado.PENDIENTE;
        this.estadoActivo = true;
    }

    // Getters y Setters (completos)
    public Integer getId() { return id; }
    public void setId(Integer id) { this.id = id; }
    public LocalTime getHoraEstimadaFinalizacion() { return horaEstimadaFinalizacion; }
    public void setHoraEstimadaFinalizacion(LocalTime horaEstimadaFinalizacion) { this.horaEstimadaFinalizacion = horaEstimadaFinalizacion; }
    public Double getTotal() { return total; }
    public void setTotal(Double total) { this.total = total; }
    public Double getTotalCosto() { return totalCosto; }
    public void setTotalCosto(Double totalCosto) { this.totalCosto = totalCosto; }
    public LocalDate getFechaPedido() { return fechaPedido; }
    public void setFechaPedido(LocalDate fechaPedido) { this.fechaPedido = fechaPedido; }
    public Sucursal getSucursal() { return sucursal; }
    public void setSucursal(Sucursal sucursal) { this.sucursal = sucursal; }
    public Domicilio getDomicilio() { return domicilio; }
    public void setDomicilio(Domicilio domicilio) { this.domicilio = domicilio; }
    public Factura getFactura() { return factura; }
    public void setFactura(Factura factura) { this.factura = factura; }
    public Estado getEstado() { return estado; }
    public void setEstado(Estado estado) { this.estado = estado; }
    public TipoEnvio getTipoEnvio() { return tipoEnvio; }
    public void setTipoEnvio(TipoEnvio tipoEnvio) { this.tipoEnvio = tipoEnvio; }
    public FormaPago getFormaPago() { return formaPago; }
    public void setFormaPago(FormaPago formaPago) { this.formaPago = formaPago; }
    public Cliente getCliente() { return cliente; }
    public void setCliente(Cliente cliente) { this.cliente = cliente; }
    public List<DetallePedido> getDetalles() { return detalles; }
    public void setDetalles(List<DetallePedido> detalles) { this.detalles = detalles; }
    public LocalDate getFechaBaja() { return fechaBaja; }
    public void setFechaBaja(LocalDate fechaBaja) { this.fechaBaja = fechaBaja; }
    public Boolean getEstadoActivo() { return estadoActivo; }
    public void setEstadoActivo(Boolean estadoActivo) { this.estadoActivo = estadoActivo; }

    public String getMpPreferenceId() {return mpPreferenceId;}

    public void setMpPreferenceId(String mpPreferenceId) {this.mpPreferenceId = mpPreferenceId;}

    public String getMpPaymentId() {return mpPaymentId;}

    public void setMpPaymentId(String mpPaymentId) {this.mpPaymentId = mpPaymentId;}

    public String getMpPaymentStatus() {return mpPaymentStatus;}

    public void setMpPaymentStatus(String mpPaymentStatus) {this.mpPaymentStatus = mpPaymentStatus;}

    public Double getDescuentoAplicado() {return descuentoAplicado;}

    public void setDescuentoAplicado(Double descuentoAplicado) {this.descuentoAplicado = descuentoAplicado;}

    // Métodos Helper
    public void addDetalle(DetallePedido detalle) {
        if (this.detalles == null) {
            this.detalles = new ArrayList<>();
        }
        this.detalles.add(detalle);
        detalle.setPedido(this);
    }

    public void removeDetalle(DetallePedido detalle) {
        if (this.detalles != null) {
            this.detalles.remove(detalle);
            detalle.setPedido(null);
        }
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Pedido pedido = (Pedido) o;
        return Objects.equals(id, pedido.id);
    }

    @Override
    public int hashCode() { return Objects.hash(id); }

    @Override
    public String toString() {
        return "Pedido{" + "id=" + id + ", fechaPedido=" + fechaPedido + ", estado=" + estado + ", total=" + total +
                ", cliente=" + (cliente != null ? cliente.getNombre() + " " + cliente.getApellido() : "null") + '}';
    }
}
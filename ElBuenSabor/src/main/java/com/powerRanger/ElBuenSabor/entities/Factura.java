package com.powerRanger.ElBuenSabor.entities;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonIdentityInfo;
import com.fasterxml.jackson.annotation.ObjectIdGenerators;
import com.powerRanger.ElBuenSabor.entities.enums.EstadoFactura;
import com.powerRanger.ElBuenSabor.entities.enums.FormaPago;
import jakarta.persistence.*;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PastOrPresent;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

@Entity
@JsonIdentityInfo(
        generator = ObjectIdGenerators.PropertyGenerator.class,
        property = "id")
public class Factura {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @NotNull(message = "La fecha de facturación es obligatoria")
    @PastOrPresent(message = "La fecha de facturación no puede ser futura")
    @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDate fechaFacturacion;

    // Los campos de MercadoPago son opcionales
    private Integer mpPaymentId;
    private Integer mpMerchantOrderId;
    private String mpPreferenceId;
    private String mpPaymentType;

    @NotNull(message = "El total de venta es obligatorio")
    @DecimalMin(value = "0.0", message = "El total de venta no puede ser negativo")
    private Double totalVenta;

    @OneToOne(fetch = FetchType.LAZY) // Es importante que Pedido exista
    @JoinColumn(name = "pedido_id", nullable = false, unique = true) // Una factura por pedido
    @NotNull(message = "El pedido asociado es obligatorio")
    private Pedido pedido;

    @NotNull(message = "La forma de pago es obligatoria")
    @Enumerated(EnumType.STRING)
    private FormaPago formaPago;

    @NotNull(message = "El estado de la factura es obligatorio")
    @Enumerated(EnumType.STRING)
    @Column(name = "estado_factura", nullable = false)
    private EstadoFactura estadoFactura;

    @Column(name = "fecha_anulacion")
    @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDate fechaAnulacion; // Se establece solo si se anula

    @OneToMany(mappedBy = "factura", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    private List<FacturaDetalle> detallesFactura = new ArrayList<>();

    public Factura() {
        this.fechaFacturacion = LocalDate.now(); // Valor por defecto
        this.estadoFactura = EstadoFactura.ACTIVA; // Estado por defecto
        this.detallesFactura = new ArrayList<>();
    }

    // Getters y Setters (los que ya tenías)
    public Integer getId() { return id; }
    public void setId(Integer id) { this.id = id; }
    public LocalDate getFechaFacturacion() { return fechaFacturacion; }
    public void setFechaFacturacion(LocalDate fechaFacturacion) { this.fechaFacturacion = fechaFacturacion; }
    public Integer getMpPaymentId() { return mpPaymentId; }
    public void setMpPaymentId(Integer mpPaymentId) { this.mpPaymentId = mpPaymentId; }
    public Integer getMpMerchantOrderId() { return mpMerchantOrderId; }
    public void setMpMerchantOrderId(Integer mpMerchantOrderId) { this.mpMerchantOrderId = mpMerchantOrderId; }
    public String getMpPreferenceId() { return mpPreferenceId; }
    public void setMpPreferenceId(String mpPreferenceId) { this.mpPreferenceId = mpPreferenceId; }
    public String getMpPaymentType() { return mpPaymentType; }
    public void setMpPaymentType(String mpPaymentType) { this.mpPaymentType = mpPaymentType; }
    public Double getTotalVenta() { return totalVenta; }
    public void setTotalVenta(Double totalVenta) { this.totalVenta = totalVenta; }
    public Pedido getPedido() { return pedido; }
    public void setPedido(Pedido pedido) { this.pedido = pedido; }
    public FormaPago getFormaPago() { return formaPago; }
    public void setFormaPago(FormaPago formaPago) { this.formaPago = formaPago; }
    public EstadoFactura getEstadoFactura() { return estadoFactura; }
    public void setEstadoFactura(EstadoFactura estadoFactura) { this.estadoFactura = estadoFactura; }
    public LocalDate getFechaAnulacion() { return fechaAnulacion; }
    public void setFechaAnulacion(LocalDate fechaAnulacion) { this.fechaAnulacion = fechaAnulacion; }
    public List<FacturaDetalle> getDetallesFactura() { return detallesFactura; }
    public void setDetallesFactura(List<FacturaDetalle> detallesFactura) { this.detallesFactura = detallesFactura; }

    // Métodos Helper
    public void addDetalleFactura(FacturaDetalle detalle) {
        if (this.detallesFactura == null) {
            this.detallesFactura = new ArrayList<>();
        }
        this.detallesFactura.add(detalle);
        detalle.setFactura(this); // Establece la relación bidireccional
    }

    public void removeDetalleFactura(FacturaDetalle detalle) {
        if (this.detallesFactura != null) {
            this.detallesFactura.remove(detalle);
            detalle.setFactura(null);
        }
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Factura factura = (Factura) o;
        return Objects.equals(id, factura.id);
    }

    @Override
    public int hashCode() { return Objects.hash(id); }

    @Override
    public String toString() {
        return "Factura{" + "id=" + id + ", fechaFacturacion=" + fechaFacturacion + ", totalVenta=" + totalVenta +
                ", pedidoId=" + (pedido != null ? pedido.getId() : "null") + ", estadoFactura=" + estadoFactura + '}';
    }
}
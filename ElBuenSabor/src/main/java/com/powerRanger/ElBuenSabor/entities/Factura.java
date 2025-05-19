package com.powerRanger.ElBuenSabor.entities;

import com.powerRanger.ElBuenSabor.entities.enums.*; // Importar el Enum
import jakarta.persistence.*;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

@Entity
public class Factura {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(nullable = false)
    private LocalDate fechaFacturacion;

    @Column(nullable = true)
    private Integer mpPaymentId;

    @Column(nullable = true)
    private Integer mpMerchantOrderId;

    @Column(nullable = true)
    private String mpPreferenceId;

    @Column(nullable = true)
    private String mpPaymentType;

    @Column(nullable = false)
    private Double totalVenta;

    @OneToOne
    @JoinColumn(name = "pedido_id", nullable = false)
    private Pedido pedido;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private FormaPago formaPago;

    // REEMPLAZO de estadoActivo por estadoFactura
    @Enumerated(EnumType.STRING)
    @Column(name = "estado_factura", nullable = false) // Nombre de columna explícito
    private EstadoFactura estadoFactura;

    @Column(name = "fecha_anulacion") // Nueva columna para la fecha de anulación
    private LocalDate fechaAnulacion;

    @OneToMany(mappedBy = "factura", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    private List<FacturaDetalle> detallesFactura = new ArrayList<>();

    // Constructores
    public Factura() {
        this.estadoFactura = EstadoFactura.ACTIVA; // Por defecto, una nueva factura está activa
        this.fechaFacturacion = LocalDate.now(); // Por defecto, fecha actual
    }

    // Constructor modificado para incluir el estado inicial (aunque se setea por defecto)
    public Factura(LocalDate fechaFacturacion, Integer mpPaymentId, Integer mpMerchantOrderId, String mpPreferenceId, String mpPaymentType, Double totalVenta, Pedido pedido, FormaPago formaPago) {
        this(); // Llama al constructor por defecto para inicializar estadoFactura y fechaFacturacion
        if (fechaFacturacion != null) this.fechaFacturacion = fechaFacturacion; // Permite sobreescribir la fecha por defecto
        this.mpPaymentId = mpPaymentId;
        this.mpMerchantOrderId = mpMerchantOrderId;
        this.mpPreferenceId = mpPreferenceId;
        this.mpPaymentType = mpPaymentType;
        this.totalVenta = totalVenta;
        this.pedido = pedido;
        this.formaPago = formaPago;
    }


    // Getters y Setters
    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public LocalDate getFechaFacturacion() {
        return fechaFacturacion;
    }

    public void setFechaFacturacion(LocalDate fechaFacturacion) {
        this.fechaFacturacion = fechaFacturacion;
    }

    public Integer getMpPaymentId() {
        return mpPaymentId;
    }

    public void setMpPaymentId(Integer mpPaymentId) {
        this.mpPaymentId = mpPaymentId;
    }

    public Integer getMpMerchantOrderId() {
        return mpMerchantOrderId;
    }

    public void setMpMerchantOrderId(Integer mpMerchantOrderId) {
        this.mpMerchantOrderId = mpMerchantOrderId;
    }

    public String getMpPreferenceId() {
        return mpPreferenceId;
    }

    public void setMpPreferenceId(String mpPreferenceId) {
        this.mpPreferenceId = mpPreferenceId;
    }

    public String getMpPaymentType() {
        return mpPaymentType;
    }

    public void setMpPaymentType(String mpPaymentType) {
        this.mpPaymentType = mpPaymentType;
    }

    public Double getTotalVenta() {
        return totalVenta;
    }

    public void setTotalVenta(Double totalVenta) {
        this.totalVenta = totalVenta;
    }

    public Pedido getPedido() {
        return pedido;
    }

    public void setPedido(Pedido pedido) {
        this.pedido = pedido;
    }

    public FormaPago getFormaPago() {
        return formaPago;
    }

    public void setFormaPago(FormaPago formaPago) {
        this.formaPago = formaPago;
    }

    public EstadoFactura getEstadoFactura() {
        return estadoFactura;
    }

    public void setEstadoFactura(EstadoFactura estadoFactura) {
        this.estadoFactura = estadoFactura;
    }

    public LocalDate getFechaAnulacion() {
        return fechaAnulacion;
    }

    public void setFechaAnulacion(LocalDate fechaAnulacion) {
        this.fechaAnulacion = fechaAnulacion;
    }

    public List<FacturaDetalle> getDetallesFactura() {
        return detallesFactura;
    }

    public void setDetallesFactura(List<FacturaDetalle> detallesFactura) {
        this.detallesFactura = detallesFactura;
    }

    public void addDetalleFactura(FacturaDetalle detalle) {
        this.detallesFactura.add(detalle);
        detalle.setFactura(this);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Factura factura = (Factura) o;
        return Objects.equals(id, factura.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }

    @Override
    public String toString() {
        return "Factura{" +
                "id=" + id +
                ", fechaFacturacion=" + fechaFacturacion +
                ", totalVenta=" + totalVenta +
                ", pedidoId=" + (pedido != null ? pedido.getId() : "null") +
                ", estadoFactura=" + estadoFactura +
                ", fechaAnulacion=" + fechaAnulacion +
                ", numeroDeLineas=" + (detallesFactura != null ? detallesFactura.size() : 0) +
                '}';
    }
}
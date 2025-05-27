package com.powerRanger.ElBuenSabor.dtos;
import com.powerRanger.ElBuenSabor.entities.enums.FormaPago;
import com.powerRanger.ElBuenSabor.entities.enums.TipoEnvio;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import java.util.ArrayList;
import java.util.List;
public class PedidoRequestDTO {
    @NotNull(message = "La hora estimada de finalización es obligatoria")
    @Pattern(regexp = "^([01]?[0-9]|2[0-3]):[0-5][0-9](:[0-5][0-9])?$", message = "Formato de hora inválido. Use HH:mm o HH:mm:ss")
    private String horaEstimadaFinalizacion;
    @NotNull(message = "El ID de la sucursal es obligatorio")
    private Integer sucursalId;
    @NotNull(message = "El ID del domicilio de entrega es obligatorio")
    private Integer domicilioId;
    @NotNull(message = "El tipo de envío es obligatorio")
    private TipoEnvio tipoEnvio;
    @NotNull(message = "La forma de pago es obligatoria")
    private FormaPago formaPago;
    @NotNull(message = "El ID del cliente es obligatorio")
    private Integer clienteId;
    @NotEmpty(message = "El pedido debe tener al menos un detalle")
    @Size(min = 1, message = "El pedido debe tener al menos un detalle")
    @Valid
    private List<DetallePedidoRequestDTO> detalles = new ArrayList<>();
    public String getHoraEstimadaFinalizacion() { return horaEstimadaFinalizacion; }
    public void setHoraEstimadaFinalizacion(String horaEstimadaFinalizacion) { this.horaEstimadaFinalizacion = horaEstimadaFinalizacion; }
    public Integer getSucursalId() { return sucursalId; }
    public void setSucursalId(Integer sucursalId) { this.sucursalId = sucursalId; }
    public Integer getDomicilioId() { return domicilioId; }
    public void setDomicilioId(Integer domicilioId) { this.domicilioId = domicilioId; }
    public TipoEnvio getTipoEnvio() { return tipoEnvio; }
    public void setTipoEnvio(TipoEnvio tipoEnvio) { this.tipoEnvio = tipoEnvio; }
    public FormaPago getFormaPago() { return formaPago; }
    public void setFormaPago(FormaPago formaPago) { this.formaPago = formaPago; }
    public Integer getClienteId() { return clienteId; }
    public void setClienteId(Integer clienteId) { this.clienteId = clienteId; }
    public List<DetallePedidoRequestDTO> getDetalles() { return detalles; }
    public void setDetalles(List<DetallePedidoRequestDTO> detalles) { this.detalles = detalles; }
}
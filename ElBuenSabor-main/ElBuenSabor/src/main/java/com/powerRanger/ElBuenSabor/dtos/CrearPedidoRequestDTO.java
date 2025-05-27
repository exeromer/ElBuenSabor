package com.powerRanger.ElBuenSabor.dtos;

import com.powerRanger.ElBuenSabor.entities.enums.FormaPago;
import com.powerRanger.ElBuenSabor.entities.enums.TipoEnvio;
import jakarta.validation.constraints.NotBlank; // Para Strings que no deben ser vacíos ni solo espacios
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size; // Para longitud de Strings

public class CrearPedidoRequestDTO {

    // Campos para la dirección de envío (obligatorios)
    @NotBlank(message = "La calle del domicilio es obligatoria")
    @Size(max = 255, message = "La calle no puede exceder los 255 caracteres")
    private String calleDomicilio;

    @NotNull(message = "El número del domicilio es obligatorio")
    private Integer numeroDomicilio;

    @NotBlank(message = "El código postal del domicilio es obligatorio")
    @Size(max = 10, message = "El código postal no puede exceder los 10 caracteres") // Ajusta max si es necesario
    private String cpDomicilio;

    @NotNull(message = "El ID de la localidad del domicilio es obligatorio")
    private Integer localidadIdDomicilio; // ID de una Localidad existente

    // Otros campos del pedido
    @NotNull(message = "El tipo de envío es obligatorio")
    private TipoEnvio tipoEnvio;

    @NotNull(message = "La forma de pago es obligatoria")
    private FormaPago formaPago;

    @NotNull(message = "El ID de la sucursal es obligatorio")
    private Integer sucursalId;

    @NotNull(message = "La hora estimada de finalización es obligatoria")
    @Pattern(regexp = "^([01]?[0-9]|2[0-3]):[0-5][0-9](:[0-5][0-9])?$", message = "Formato de hora inválido. Use HH:mm o HH:mm:ss")
    private String horaEstimadaFinalizacion;

    private String notasAdicionales; // Opcional

    // Opcional: para decidir si guardar esta dirección en el perfil del cliente
    private Boolean guardarDireccionEnPerfil = false; // Por defecto no se guarda


    // Constructores, Getters y Setters
    public CrearPedidoRequestDTO() {
    }

    public String getCalleDomicilio() {
        return calleDomicilio;
    }

    public void setCalleDomicilio(String calleDomicilio) {
        this.calleDomicilio = calleDomicilio;
    }

    public Integer getNumeroDomicilio() {
        return numeroDomicilio;
    }

    public void setNumeroDomicilio(Integer numeroDomicilio) {
        this.numeroDomicilio = numeroDomicilio;
    }

    public String getCpDomicilio() {
        return cpDomicilio;
    }

    public void setCpDomicilio(String cpDomicilio) {
        this.cpDomicilio = cpDomicilio;
    }

    public Integer getLocalidadIdDomicilio() {
        return localidadIdDomicilio;
    }

    public void setLocalidadIdDomicilio(Integer localidadIdDomicilio) {
        this.localidadIdDomicilio = localidadIdDomicilio;
    }

    public TipoEnvio getTipoEnvio() {
        return tipoEnvio;
    }

    public void setTipoEnvio(TipoEnvio tipoEnvio) {
        this.tipoEnvio = tipoEnvio;
    }

    public FormaPago getFormaPago() {
        return formaPago;
    }

    public void setFormaPago(FormaPago formaPago) {
        this.formaPago = formaPago;
    }

    public Integer getSucursalId() {
        return sucursalId;
    }

    public void setSucursalId(Integer sucursalId) {
        this.sucursalId = sucursalId;
    }

    public String getHoraEstimadaFinalizacion() {
        return horaEstimadaFinalizacion;
    }

    public void setHoraEstimadaFinalizacion(String horaEstimadaFinalizacion) {
        this.horaEstimadaFinalizacion = horaEstimadaFinalizacion;
    }

    public String getNotasAdicionales() {
        return notasAdicionales;
    }

    public void setNotasAdicionales(String notasAdicionales) {
        this.notasAdicionales = notasAdicionales;
    }

    public Boolean getGuardarDireccionEnPerfil() {
        return guardarDireccionEnPerfil;
    }

    public void setGuardarDireccionEnPerfil(Boolean guardarDireccionEnPerfil) {
        this.guardarDireccionEnPerfil = guardarDireccionEnPerfil;
    }
}
package com.powerRanger.ElBuenSabor.services;

import com.powerRanger.ElBuenSabor.entities.ArticuloInsumo;
import jakarta.validation.Valid;
import java.util.List;

public interface ArticuloInsumoService {
    List<ArticuloInsumo> getAllArticuloInsumo();
    ArticuloInsumo getArticuloInsumoById(Integer id) throws Exception;
    ArticuloInsumo createArticuloInsumo(@Valid ArticuloInsumo articuloInsumo) throws Exception;
    ArticuloInsumo updateArticuloInsumo(Integer id, @Valid ArticuloInsumo articuloInsumoDetails) throws Exception;
    void deleteArticuloInsumo(Integer id) throws Exception;
}
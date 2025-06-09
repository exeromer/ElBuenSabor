package com.powerRanger.ElBuenSabor.controllers;

import com.powerRanger.ElBuenSabor.entities.UnidadMedida;
import com.powerRanger.ElBuenSabor.services.UnidadMedidaServiceImpl;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/unidadesmedida")
@CrossOrigin(origins = "*") // Buena práctica
public class UnidadMedidaController extends BaseControllerImpl<UnidadMedida, UnidadMedidaServiceImpl> {

    // Se añade este constructor para satisfacer la dependencia de la clase padre.
    public UnidadMedidaController(UnidadMedidaServiceImpl servicio) {
        super(servicio);
    }
}
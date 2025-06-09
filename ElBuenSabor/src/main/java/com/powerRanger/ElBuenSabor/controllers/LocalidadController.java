package com.powerRanger.ElBuenSabor.controllers;

import com.powerRanger.ElBuenSabor.entities.Localidad;
import com.powerRanger.ElBuenSabor.services.LocalidadServiceImpl;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/localidades")
@CrossOrigin(origins = "*") // Buena práctica añadir CORS
public class LocalidadController extends BaseControllerImpl<Localidad, LocalidadServiceImpl> {

    // Se añade este constructor para satisfacer la dependencia de la clase padre.
    // Spring inyectará el servicio aquí y lo pasará a super().
    public LocalidadController(LocalidadServiceImpl servicio) {
        super(servicio);
    }
}
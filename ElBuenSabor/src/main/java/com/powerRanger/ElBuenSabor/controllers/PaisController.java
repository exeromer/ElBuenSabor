package com.powerRanger.ElBuenSabor.controllers;

import com.powerRanger.ElBuenSabor.entities.Pais;
import com.powerRanger.ElBuenSabor.services.PaisServiceImpl;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/paises")
@CrossOrigin(origins = "*") // Buena práctica
public class PaisController extends BaseControllerImpl<Pais, PaisServiceImpl> {

    // Se añade este constructor para inyectar el servicio específico
    // en la clase base genérica.
    public PaisController(PaisServiceImpl servicio) {
        super(servicio);
    }
}
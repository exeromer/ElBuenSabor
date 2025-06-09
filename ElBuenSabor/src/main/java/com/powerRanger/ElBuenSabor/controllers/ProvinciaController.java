package com.powerRanger.ElBuenSabor.controllers;

import com.powerRanger.ElBuenSabor.entities.Provincia;
import com.powerRanger.ElBuenSabor.services.ProvinciaServiceImpl;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/provincias")
@CrossOrigin(origins = "*") // Buena práctica
public class ProvinciaController extends BaseControllerImpl<Provincia, ProvinciaServiceImpl> {


    public ProvinciaController(ProvinciaServiceImpl servicio) {
        super(servicio);
    }
}
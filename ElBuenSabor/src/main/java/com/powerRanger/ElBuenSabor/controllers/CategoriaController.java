package com.powerRanger.ElBuenSabor.controllers;

import com.powerRanger.ElBuenSabor.entities.Categoria;
import com.powerRanger.ElBuenSabor.services.CategoriaServiceImpl;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/categorias")
@CrossOrigin(origins = "*") // Es buena práctica añadir CORS
public class CategoriaController extends BaseControllerImpl<Categoria, CategoriaServiceImpl> {

    // Se añade este constructor para satisfacer la dependencia de la clase padre
    public CategoriaController(CategoriaServiceImpl servicio) {
        super(servicio);
    }
}
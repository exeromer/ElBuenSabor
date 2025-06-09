package com.powerRanger.ElBuenSabor.controllers;

import com.powerRanger.ElBuenSabor.entities.BaseEntity;
import com.powerRanger.ElBuenSabor.services.BaseService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;

public abstract class BaseControllerImpl<E extends BaseEntity, S extends BaseService<E, Integer>> implements BaseController<E, Integer> {

    // 1. Se elimina @Autowired
    protected S servicio;

    // 2. Se añade un constructor para la inyección de dependencias
    public BaseControllerImpl(S servicio) {
        this.servicio = servicio;
    }

    @GetMapping("")
    @Override
    public ResponseEntity<?> getAll() throws Exception {
        return ResponseEntity.status(HttpStatus.OK).body(servicio.findAll());
    }

    @GetMapping("/{id}")
    @Override
    public ResponseEntity<?> getOne(@PathVariable Integer id) throws Exception {
        return ResponseEntity.status(HttpStatus.OK).body(servicio.findById(id));
    }

    @PostMapping("")
    @Override
    public ResponseEntity<?> save(@RequestBody E entity) throws Exception {
        return ResponseEntity.status(HttpStatus.CREATED).body(servicio.save(entity));
    }

    @PutMapping("/{id}")
    @Override
    public ResponseEntity<?> update(@PathVariable Integer id, @RequestBody E entity) throws Exception {
        return ResponseEntity.status(HttpStatus.OK).body(servicio.update(id, entity));
    }

    @DeleteMapping("/{id}")
    @Override
    public ResponseEntity<?> delete(@PathVariable Integer id) throws Exception {
        servicio.delete(id);
        return ResponseEntity.status(HttpStatus.NO_CONTENT).build();
    }
}
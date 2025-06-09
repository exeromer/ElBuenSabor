package com.powerRanger.ElBuenSabor.controllers;

import com.powerRanger.ElBuenSabor.entities.BaseEntity;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import java.io.Serializable;

public interface BaseController<E extends BaseEntity, ID extends Serializable> {
    public ResponseEntity<?> getAll() throws Exception; // <-- CAMBIO AQUÍ
    public ResponseEntity<?> getOne(@PathVariable ID id) throws Exception; // <-- CAMBIO AQUÍ
    public ResponseEntity<?> save(@RequestBody E entity) throws Exception; // <-- CAMBIO AQUÍ
    public ResponseEntity<?> update(@PathVariable ID id, @RequestBody E entity) throws Exception; // <-- CAMBIO AQUÍ
    public ResponseEntity<?> delete(@PathVariable ID id) throws Exception; // <-- CAMBIO AQUÍ
}
package com.powerRanger.ElBuenSabor.services;

import com.powerRanger.ElBuenSabor.entities.Localidad;
import java.util.List;

public interface LocalidadService {
    List<Localidad> obtenerTodas();
    Localidad obtenerPorId(Integer id) throws Exception;
    Localidad guardar(Localidad localidad) throws Exception;
    Localidad actualizar(Integer id, Localidad localidadDetalles) throws Exception;
    boolean borrar(Integer id) throws Exception;
}
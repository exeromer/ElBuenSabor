package com.powerRanger.ElBuenSabor.services;

import com.powerRanger.ElBuenSabor.entities.Provincia;
import java.util.List;

public interface ProvinciaService {
    List<Provincia> obtenerTodas();
    Provincia obtenerPorId(Integer id) throws Exception;
    Provincia guardar(Provincia provincia) throws Exception; // Modificado para simplificar
    Provincia actualizar(Integer id, Provincia provinciaDetalles) throws Exception;
    boolean borrar(Integer id) throws Exception;
}
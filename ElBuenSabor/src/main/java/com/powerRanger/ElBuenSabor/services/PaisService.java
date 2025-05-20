package com.powerRanger.ElBuenSabor.services;

import com.powerRanger.ElBuenSabor.entities.Pais;
import java.util.List;

public interface PaisService {
    List<Pais> obtenerTodos();
    Pais obtenerPorId(Integer id) throws Exception;
    Pais guardar(Pais pais);
    Pais actualizar(Integer id, Pais pais) throws Exception;
    boolean borrar(Integer id) throws Exception;
}
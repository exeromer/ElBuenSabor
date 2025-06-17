package com.powerRanger.ElBuenSabor.repository;

import com.powerRanger.ElBuenSabor.entities.Sucursal;
import org.springframework.data.jpa.repository.JpaRepository;
// import org.springframework.stereotype.Repository; // Opcional

public interface SucursalRepository extends JpaRepository<Sucursal, Integer> {
    // Aquí puedes añadir métodos de búsqueda personalizados si los necesitas
    // Ejemplo: List<Sucursal> findByEmpresaId(Integer empresaId);
}
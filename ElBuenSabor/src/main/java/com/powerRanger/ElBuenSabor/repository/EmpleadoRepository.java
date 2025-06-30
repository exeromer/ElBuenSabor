package com.powerRanger.ElBuenSabor.repository;

import com.powerRanger.ElBuenSabor.entities.Empleado;
import com.powerRanger.ElBuenSabor.entities.enums.RolEmpleado;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface EmpleadoRepository extends JpaRepository<Empleado, Integer> {
    Optional<Empleado> findByUsuarioId(Integer usuarioId);
    List<Empleado> findByRolEmpleado(RolEmpleado rolEmpleado);

    @Query("SELECT e FROM Empleado e WHERE " +
            "(LOWER(e.nombre) LIKE LOWER(CONCAT('%', :searchTerm, '%')) OR " +
            "LOWER(e.apellido) LIKE LOWER(CONCAT('%', :searchTerm, '%')) OR " +
            "LOWER(e.usuario.username) LIKE LOWER(CONCAT('%', :searchTerm, '%'))) " +
            "AND e.estadoActivo = true " +
            "AND (:rol IS NULL OR e.rolEmpleado = :rol)")
    List<Empleado> searchByTermAndRol(@Param("searchTerm") String searchTerm, @Param("rol") RolEmpleado rol);

    List<Empleado> findByEstadoActivoTrue();
}
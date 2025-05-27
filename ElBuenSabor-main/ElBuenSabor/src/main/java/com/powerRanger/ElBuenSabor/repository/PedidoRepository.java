package com.powerRanger.ElBuenSabor.repository;
import com.powerRanger.ElBuenSabor.entities.Pedido;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
public interface PedidoRepository extends JpaRepository<Pedido, Integer> {
    List<Pedido> findByClienteIdAndEstadoActivoTrueOrderByFechaPedidoDesc(Integer clienteId);
    List<Pedido> findByClienteUsuarioAuth0IdAndEstadoActivoTrueOrderByFechaPedidoDesc(String auth0Id);
}
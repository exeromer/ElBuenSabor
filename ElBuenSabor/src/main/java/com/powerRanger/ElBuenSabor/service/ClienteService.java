package com.powerRanger.ElBuenSabor.service;

import com.powerRanger.ElBuenSabor.entities.Cliente;
import com.powerRanger.ElBuenSabor.repository.ClienteRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class ClienteService {

    @Autowired
    private ClienteRepository clienteRepository;

    // Obtener todos los clientes
    public List<Cliente> getAllClientes() {
        return clienteRepository.findAll();
    }

    // Obtener un cliente por ID
    public Cliente getClienteById(Integer id) {
        Optional<Cliente> cliente = clienteRepository.findById(id);
        return cliente.orElse(null);  // Retorna null si no se encuentra
    }

    // Crear un nuevo cliente
    public Cliente createCliente(Cliente cliente) {
        return clienteRepository.save(cliente);
    }

    // Actualizar un cliente
    public Cliente updateCliente(Integer id, Cliente cliente) {
        if (clienteRepository.existsById(id)) {
            // No es necesario setear el ID manualmente
            return clienteRepository.save(cliente);
        }
        return null;
    }

    // Eliminar un cliente
    public void deleteCliente(Integer id) {
        clienteRepository.deleteById(id);
    }
}

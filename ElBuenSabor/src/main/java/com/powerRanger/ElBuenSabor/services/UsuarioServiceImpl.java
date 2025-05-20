package com.powerRanger.ElBuenSabor.services;

import com.powerRanger.ElBuenSabor.dtos.UsuarioRequestDTO;
import com.powerRanger.ElBuenSabor.entities.Usuario;
import com.powerRanger.ElBuenSabor.entities.enums.Rol; // Importar Rol
import com.powerRanger.ElBuenSabor.repository.UsuarioRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import jakarta.validation.Valid;
import org.springframework.validation.annotation.Validated;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Service
@Validated
public class UsuarioServiceImpl implements UsuarioService {

    @Autowired
    private UsuarioRepository usuarioRepository;

    // ... (métodos getAll, getById, getByUsername, getByAuth0Id, create, update, softDelete sin cambios) ...

    @Override
    @Transactional(readOnly = true)
    public List<Usuario> getAll() {
        return usuarioRepository.findAll();
    }

    @Override
    @Transactional(readOnly = true)
    public Usuario getById(Integer id) throws Exception {
        return usuarioRepository.findById(id)
                .orElseThrow(() -> new Exception("Usuario no encontrado con ID: " + id));
    }

    @Override
    @Transactional(readOnly = true)
    public Usuario getByUsername(String username) throws Exception {
        return usuarioRepository.findByUsername(username)
                .orElseThrow(() -> new Exception("Usuario no encontrado con username: " + username));
    }

    @Override
    @Transactional(readOnly = true)
    public Usuario getByAuth0Id(String auth0Id) throws Exception {
        return usuarioRepository.findByAuth0Id(auth0Id)
                .orElseThrow(() -> new Exception("Usuario no encontrado con Auth0 ID: " + auth0Id));
    }

    @Override
    @Transactional
    public Usuario create(@Valid UsuarioRequestDTO dto) throws Exception {
        if (usuarioRepository.findByUsername(dto.getUsername()).isPresent()) {
            throw new Exception("El username '" + dto.getUsername() + "' ya está en uso.");
        }
        if (usuarioRepository.findByAuth0Id(dto.getAuth0Id()).isPresent()) {
            throw new Exception("El Auth0 ID '" + dto.getAuth0Id() + "' ya está registrado.");
        }

        Usuario usuario = new Usuario();
        usuario.setAuth0Id(dto.getAuth0Id());
        usuario.setUsername(dto.getUsername());
        usuario.setRol(dto.getRol());
        usuario.setEstadoActivo(dto.getEstadoActivo() != null ? dto.getEstadoActivo() : true);
        return usuarioRepository.save(usuario);
    }

    @Override
    @Transactional
    public Usuario update(Integer id, @Valid UsuarioRequestDTO dto) throws Exception {
        Usuario usuarioExistente = getById(id);

        usuarioRepository.findByUsername(dto.getUsername()).ifPresent(u -> {
            if (!u.getId().equals(id)) {
                throw new RuntimeException("El username '" + dto.getUsername() + "' ya está en uso por otro usuario.");
            }
        });
        usuarioRepository.findByAuth0Id(dto.getAuth0Id()).ifPresent(u -> {
            if (!u.getId().equals(id)) {
                throw new RuntimeException("El Auth0 ID '" + dto.getAuth0Id() + "' ya está registrado por otro usuario.");
            }
        });

        usuarioExistente.setAuth0Id(dto.getAuth0Id());
        usuarioExistente.setUsername(dto.getUsername());
        usuarioExistente.setRol(dto.getRol());
        usuarioExistente.setEstadoActivo(dto.getEstadoActivo());

        return usuarioRepository.save(usuarioExistente);
    }

    @Override
    @Transactional
    public void softDelete(Integer id) throws Exception {
        Usuario usuario = getById(id);
        usuario.setEstadoActivo(false);
        usuario.setFechaBaja(LocalDate.now());
        usuarioRepository.save(usuario);
    }


    // ---- NUEVO MÉTODO PARA JIT PROVISIONING ----
    @Override
    @Transactional
    public Usuario findOrCreateUsuario(String auth0Id, String username, String email /* otros campos del token si los necesitas */) throws Exception {
        Optional<Usuario> optionalUsuario = usuarioRepository.findByAuth0Id(auth0Id);

        if (optionalUsuario.isPresent()) {
            // Usuario encontrado, podrías actualizar alguna información si es necesario (ej. último login)
            // pero por ahora solo lo devolvemos.
            return optionalUsuario.get();
        } else {
            // Usuario no encontrado, creamos uno nuevo.
            System.out.println("Creando nuevo usuario para auth0Id: " + auth0Id);

            // Verificar si el username sugerido ya existe
            if (username != null && usuarioRepository.findByUsername(username).isPresent()) {
                // El username sugerido (ej. del email) ya existe.
                // Necesitas una estrategia para generar un username único.
                // Por ejemplo, añadirle un sufijo numérico, o usar el auth0Id (que es único).
                // Para este ejemplo, usaremos el auth0Id como username si el sugerido está tomado.
                // En un caso real, el username podría no ser necesario si auth0Id es el identificador principal.
                System.out.println("Username '" + username + "' ya existe, usando auth0Id como username alternativo.");
                // Podrías incluso quitar el campo username de tu entidad Usuario si solo te basas en auth0Id
                // y el email/nombre para mostrar vienen del token o de la entidad Cliente.
                // Por ahora, si username es null o está tomado, usamos una parte del auth0Id o el email.
                if (username == null || usuarioRepository.findByUsername(username).isPresent()){
                    username = (email != null && !email.isEmpty()) ? email : "user_" + auth0Id.replace("|", "_").substring(0, Math.min(10, auth0Id.length()));
                    // Asegurarse de que este username generado también sea único
                    int i = 0;
                    String finalUsername = username;
                    while(usuarioRepository.findByUsername(finalUsername).isPresent()){
                        finalUsername = username + "_" + i++;
                    }
                    username = finalUsername;
                }
            }
            if (username == null || username.trim().isEmpty()){ // Fallback si todo falla
                username = "user_" + auth0Id.replace("|", "_");
            }


            Usuario nuevoUsuario = new Usuario();
            nuevoUsuario.setAuth0Id(auth0Id);
            nuevoUsuario.setUsername(username); // Usar el username (posiblemente derivado del email o nickname del token)
            nuevoUsuario.setRol(Rol.CLIENTE); // Rol por defecto para nuevos usuarios registrados
            nuevoUsuario.setEstadoActivo(true);
            // Otros campos podrían setearse con información del token si está disponible (ej. email, nombre)
            // o se completarán cuando se cree la entidad Cliente asociada.

            return usuarioRepository.save(nuevoUsuario);
        }
    }
}
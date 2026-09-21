package Agropacayales.valleGrande.service.impl;

import Agropacayales.valleGrande.exception.BusinessValidationException;
import Agropacayales.valleGrande.exception.ResourceNotFoundException;
import Agropacayales.valleGrande.model.Usuario;
import Agropacayales.valleGrande.repository.mongo.UsuarioRepository;
import Agropacayales.valleGrande.service.UsuarioService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class UsuarioServiceImpl implements UsuarioService {

    private final UsuarioRepository usuarioRepository;

    @Override
    public Flux<Usuario> listarTodos() {
        return usuarioRepository.findAll();
    }

    @Override
    public Flux<Usuario> listarPorEstado(Boolean estado) {
        return usuarioRepository.findByEstado(estado);
    }

    @Override
    public Mono<Usuario> buscarPorId(String id) {
        return usuarioRepository.findById(id);
    }

    @Override
    public Mono<Usuario> guardar(Usuario usuario) {
        if (usuario.getCorreo() == null || usuario.getCorreo().isBlank()) {
            return Mono.error(new BusinessValidationException("El correo del usuario es obligatorio."));
        }

        String correoTrimmed = usuario.getCorreo().trim();
        usuario.setCorreo(correoTrimmed);

        return usuarioRepository.existsByCorreoIgnoreCase(correoTrimmed)
                .flatMap(existe -> {
                    if (Boolean.TRUE.equals(existe)) {
                        return Mono.error(new BusinessValidationException(
                                "Ya existe un usuario registrado con el correo '" + correoTrimmed + "'."
                        ));
                    }
                    usuario.setEstado(true);
                    usuario.setCreatedAt(LocalDateTime.now());
                    usuario.setUpdatedAt(null);
                    usuario.setDeletedAt(null);
                    usuario.setRestoredAt(null);
                    return usuarioRepository.save(usuario);
                });
    }

    @Override
    public Mono<Usuario> actualizar(String id, Usuario datos) {
        return usuarioRepository.findById(id)
                .switchIfEmpty(Mono.error(new ResourceNotFoundException("Usuario no encontrado con ID: " + id)))
                .flatMap(existente -> {
                    String correoTrimmed = datos.getCorreo() != null ? datos.getCorreo().trim() : existente.getCorreo();

                    return usuarioRepository.existsByCorreoIgnoreCaseAndIdNot(correoTrimmed, id)
                            .flatMap(existeDuplicado -> {
                                if (Boolean.TRUE.equals(existeDuplicado)) {
                                    return Mono.error(new BusinessValidationException(
                                            "Ya existe otro usuario registrado con el correo '" + correoTrimmed + "'."
                                    ));
                                }

                                existente.setNombre(datos.getNombre());
                                existente.setApellido(datos.getApellido());
                                existente.setCorreo(correoTrimmed);
                                if (datos.getPassword() != null && !datos.getPassword().trim().isEmpty()) {
                                    existente.setPassword(datos.getPassword().trim());
                                }
                                existente.setRol(datos.getRol());
                                existente.setFechaNacimiento(datos.getFechaNacimiento());
                                existente.setFechaContratacion(datos.getFechaContratacion());
                                existente.setUpdatedAt(LocalDateTime.now());

                                return usuarioRepository.save(existente);
                            });
                });
    }

    @Override
    public Mono<Usuario> eliminarLogico(String id) {
        return usuarioRepository.findById(id)
                .switchIfEmpty(Mono.error(new ResourceNotFoundException("Usuario no encontrado con ID: " + id)))
                .flatMap(existente -> {
                    existente.setEstado(false);
                    existente.setDeletedAt(LocalDateTime.now());
                    return usuarioRepository.save(existente);
                });
    }

    @Override
    public Mono<Usuario> restaurarLogico(String id) {
        return usuarioRepository.findById(id)
                .switchIfEmpty(Mono.error(new ResourceNotFoundException("Usuario no encontrado con ID: " + id)))
                .flatMap(existente -> {
                    existente.setEstado(true);
                    existente.setRestoredAt(LocalDateTime.now());
                    return usuarioRepository.save(existente);
                });
    }
}

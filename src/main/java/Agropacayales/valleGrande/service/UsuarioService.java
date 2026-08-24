package Agropacayales.valleGrande.service;

import Agropacayales.valleGrande.model.Usuario;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

public interface UsuarioService {

    Flux<Usuario> listarTodos();

    Flux<Usuario> listarPorEstado(Boolean estado);

    Mono<Usuario> buscarPorId(String id);

    Mono<Usuario> guardar(Usuario usuario);

    Mono<Usuario> actualizar(String id, Usuario usuario);

    Mono<Usuario> eliminarLogico(String id);

    Mono<Usuario> restaurarLogico(String id);
}

package Agropacayales.valleGrande.service;

import Agropacayales.valleGrande.model.Parcela;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

public interface ParcelaService {

    Flux<Parcela> listarTodos();

    Flux<Parcela> listarPorEstado(Boolean estado);

    Mono<Parcela> listarPorId(String id);

    Mono<Parcela> crear(Parcela parcela);

    Mono<Parcela> editar(String id, Parcela parcela);

    Mono<Parcela> eliminar(String id);

    Mono<Parcela> restaurar(String id);
}

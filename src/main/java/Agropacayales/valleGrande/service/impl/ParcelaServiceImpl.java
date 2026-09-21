package Agropacayales.valleGrande.service.impl;

import Agropacayales.valleGrande.exception.BusinessValidationException;
import Agropacayales.valleGrande.exception.ResourceNotFoundException;
import Agropacayales.valleGrande.model.Parcela;
import Agropacayales.valleGrande.repository.mongo.ParcelaRepository;
import Agropacayales.valleGrande.service.ParcelaService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class ParcelaServiceImpl implements ParcelaService {

    private final ParcelaRepository parcelaRepository;

    @Override
    public Flux<Parcela> listarTodos() {
        return parcelaRepository.findAll();
    }

    @Override
    public Flux<Parcela> listarPorEstado(Boolean estado) {
        return parcelaRepository.findByEstado(estado);
    }

    @Override
    public Mono<Parcela> listarPorId(String id) {
        return parcelaRepository.findById(id);
    }

    @Override
    public Mono<Parcela> crear(Parcela parcela) {
        if (parcela.getNombre() == null || parcela.getNombre().isBlank()) {
            return Mono.error(new BusinessValidationException("El nombre de la parcela es obligatorio."));
        }

        String nombreTrimmed = parcela.getNombre().trim();
        parcela.setNombre(nombreTrimmed);

        return parcelaRepository.existsByNombreIgnoreCase(nombreTrimmed)
                .flatMap(existe -> {
                    if (Boolean.TRUE.equals(existe)) {
                        return Mono.error(new BusinessValidationException(
                                "Ya existe una parcela con el nombre: " + nombreTrimmed
                        ));
                    }

                    parcela.setEstado(true);
                    parcela.setEnUso(Boolean.TRUE.equals(parcela.getEnUso()));
                    parcela.setCreatedAt(LocalDateTime.now());
                    parcela.setUpdatedAt(null);
                    parcela.setDeletedAt(null);
                    parcela.setRestoredAt(null);
                    return parcelaRepository.save(parcela);
                });
    }

    @Override
    public Mono<Parcela> editar(String id, Parcela datos) {
        if (datos.getNombre() == null || datos.getNombre().isBlank()) {
            return Mono.error(new BusinessValidationException("El nombre de la parcela es obligatorio."));
        }

        String nombreTrimmed = datos.getNombre().trim();

        return parcelaRepository.findById(id)
                .switchIfEmpty(Mono.error(new ResourceNotFoundException("Parcela no encontrada con ID: " + id)))
                .flatMap(existente -> parcelaRepository.existsByNombreIgnoreCaseAndIdNot(nombreTrimmed, id)
                        .flatMap(existeDuplicado -> {
                            if (Boolean.TRUE.equals(existeDuplicado)) {
                                return Mono.error(new BusinessValidationException(
                                        "Ya existe otra parcela con el nombre: " + nombreTrimmed
                                ));
                            }

                            existente.setNombre(nombreTrimmed);
                            existente.setUbicacion(datos.getUbicacion());
                            existente.setAreaHectareas(datos.getAreaHectareas());
                            existente.setTipoSuelo(datos.getTipoSuelo());
                            existente.setResponsable(datos.getResponsable());
                            existente.setEstadoRiego(datos.getEstadoRiego());
                            existente.setFechaUltimaSiembra(datos.getFechaUltimaSiembra());
                            existente.setProduccionEstimada(datos.getProduccionEstimada());
                            existente.setCultivoActual(datos.getCultivoActual());
                            existente.setObservaciones(datos.getObservaciones());
                            if (datos.getEnUso() != null) {
                                existente.setEnUso(datos.getEnUso());
                            }
                            existente.setUpdatedAt(LocalDateTime.now());

                            return parcelaRepository.save(existente);
                        }));
    }

    @Override
    public Mono<Parcela> eliminar(String id) {
        return parcelaRepository.findById(id)
                .switchIfEmpty(Mono.error(new ResourceNotFoundException("Parcela no encontrada con ID: " + id)))
                .flatMap(existente -> {
                    existente.setEstado(false);
                    existente.setDeletedAt(LocalDateTime.now());
                    return parcelaRepository.save(existente);
                });
    }

    @Override
    public Mono<Parcela> restaurar(String id) {
        return parcelaRepository.findById(id)
                .switchIfEmpty(Mono.error(new ResourceNotFoundException("Parcela no encontrada con ID: " + id)))
                .flatMap(existente -> {
                    existente.setEstado(true);
                    existente.setRestoredAt(LocalDateTime.now());
                    return parcelaRepository.save(existente);
                });
    }
}

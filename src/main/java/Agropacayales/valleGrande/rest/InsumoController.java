package Agropacayales.valleGrande.rest;

import Agropacayales.valleGrande.model.Insumo;
import Agropacayales.valleGrande.service.InsumoService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@RestController
@RequestMapping("/api/insumos")
@RequiredArgsConstructor
@Tag(name = "Insumo-Controller", description = "Operaciones reactivas de gestión de insumos agrícolas (WebFlux + MongoDB)")
public class InsumoController {

    private final InsumoService insumoService;

    // GET - Listar todos los insumos (FLUX)
    @GetMapping
    @Operation(summary = "Listar todos los insumos", description = "Obtiene un flujo reactivo con todos los insumos registrados")
    public Flux<Insumo> listarTodos() {
        return insumoService.listarTodos();
    }

    // GET - Listar insumos por estado (FLUX)
    @GetMapping("/estado/{estado}")
    @Operation(summary = "Listar insumos por estado", description = "Filtra insumos activos o inactivos retornando un Flux")
    public Flux<Insumo> listarPorEstado(@PathVariable Boolean estado) {
        return insumoService.listarPorEstado(estado);
    }

    // GET - Listar insumo por ID (MONO)
    @GetMapping("/{id}")
    @Operation(summary = "Obtener insumo por ID", description = "Busca un insumo específico por su identificador único")
    @ApiResponse(responseCode = "200", description = "Insumo encontrado")
    @ApiResponse(responseCode = "404", description = "Insumo no encontrado")
    public Mono<ResponseEntity<Insumo>> listarPorId(@PathVariable String id) {
        return insumoService.listarPorId(id)
                .map(ResponseEntity::ok)
                .defaultIfEmpty(ResponseEntity.notFound().build());
    }

    // POST - Crear nuevo insumo (MONO)
    @PostMapping
    @Operation(summary = "Registrar nuevo insumo", description = "Crea un insumo validando reglas de negocio e integridad")
    @ApiResponse(responseCode = "201", description = "Insumo creado exitosamente")
    @ApiResponse(responseCode = "400", description = "Datos de entrada inválidos o nombre de insumo duplicado")
    public Mono<ResponseEntity<Insumo>> crear(@Valid @RequestBody Insumo insumo) {
        return insumoService.crear(insumo)
                .map(nuevo -> ResponseEntity.status(HttpStatus.CREATED).body(nuevo));
    }

    // PUT - Editar insumo existente (MONO)
    @PutMapping("/{id}")
    @Operation(summary = "Actualizar insumo existente", description = "Modifica los datos de un insumo activo existente")
    @ApiResponse(responseCode = "200", description = "Insumo actualizado exitosamente")
    @ApiResponse(responseCode = "400", description = "Datos inválidos o conflicto de nombre")
    @ApiResponse(responseCode = "404", description = "Insumo no encontrado o inactivo")
    public Mono<ResponseEntity<Insumo>> editar(@PathVariable String id, @Valid @RequestBody Insumo insumo) {
        return insumoService.editar(id, insumo)
                .map(ResponseEntity::ok);
    }

    // PATCH - Eliminar lógico (MONO)
    @PatchMapping("/{id}/eliminar")
    @Operation(summary = "Eliminar insumo (Lógico)", description = "Desactiva un insumo cambiando su estado a false, validando stock")
    @ApiResponse(responseCode = "200", description = "Insumo eliminado lógicamente")
    @ApiResponse(responseCode = "400", description = "Conflicto por existencia de stock remanente")
    @ApiResponse(responseCode = "404", description = "Insumo no encontrado")
    public Mono<ResponseEntity<Insumo>> eliminar(@PathVariable String id) {
        return insumoService.eliminar(id)
                .map(ResponseEntity::ok);
    }

    // PATCH - Restaurar lógico (MONO)
    @PatchMapping("/{id}/restaurar")
    @Operation(summary = "Restaurar insumo (Lógico)", description = "Activa nuevamente un insumo previamente inactivo")
    @ApiResponse(responseCode = "200", description = "Insumo restaurado exitosamente")
    @ApiResponse(responseCode = "404", description = "Insumo no encontrado")
    public Mono<ResponseEntity<Insumo>> restaurar(@PathVariable String id) {
        return insumoService.restaurar(id)
                .map(ResponseEntity::ok);
    }

    // GET - Buscar insumos por nombre (FLUX)
    @GetMapping("/buscar")
    @Operation(summary = "Buscar insumos por nombre", description = "Busca insumos cuyo nombre contenga la cadena proporcionada")
    public Flux<Insumo> buscarPorNombre(@RequestParam String nombre) {
        return insumoService.buscarPorNombre(nombre);
    }

    // GET - Filtrar insumos por tipo (FLUX)
    @GetMapping("/filtrar")
    @Operation(summary = "Filtrar insumos por tipo", description = "Filtra insumos según su categoría o tipo")
    public Flux<Insumo> filtrarPorTipo(@RequestParam String tipo) {
        return insumoService.filtrarPorTipo(tipo);
    }
}

package com.gomes.photographer_manager.domain.event;

import com.gomes.photographer_manager.domain.event.request.EventRequest;
import com.gomes.photographer_manager.domain.event.response.EventResponse;
import com.gomes.photographer_manager.domain.event.response.EventStatsResponse;
import com.gomes.photographer_manager.domain.usuario.User;
import com.gomes.photographer_manager.exception.ErrorResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/events")
@Tag(name = "Eventos", description = "Gerenciamento de eventos fotográficos")
public class EventController {

    private final EventService service;

    public EventController(EventService service) {
        this.service = service;
    }

    @GetMapping
    @Operation(summary = "Listar eventos", description = "Retorna os eventos do fotógrafo autenticado, com filtros opcionais por status e data")
    @ApiResponse(responseCode = "200", description = "Lista retornada com sucesso")
    public ResponseEntity<List<EventResponse>> findAll(
            @AuthenticationPrincipal User user,
            @RequestParam(required = false) EventStatus status,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date,
            @RequestParam(required = false) String clientId) {
        if (clientId != null) {
            return ResponseEntity.ok(service.findByClient(clientId, status));
        }
        return ResponseEntity.ok(service.findAll(user.getId(), status, date));
    }

    @GetMapping("/upcoming")
    @Operation(summary = "Próximos eventos", description = "Retorna os 5 próximos eventos futuros do fotógrafo autenticado")
    @ApiResponse(responseCode = "200", description = "Lista retornada com sucesso")
    public ResponseEntity<List<EventResponse>> findUpcoming(@AuthenticationPrincipal User user) {
        return ResponseEntity.ok(service.findUpcoming(user.getId()));
    }

    @GetMapping("/stats")
    @Operation(summary = "Estatísticas de eventos", description = "Retorna estatísticas agregadas dos eventos do fotógrafo autenticado")
    @ApiResponse(responseCode = "200", description = "Estatísticas retornadas com sucesso")
    public ResponseEntity<EventStatsResponse> getStats(@AuthenticationPrincipal User user) {
        return ResponseEntity.ok(service.getStats(user.getId()));
    }

    @GetMapping("/{id}")
    @Operation(summary = "Buscar evento por ID", description = "Retorna um evento pelo seu ULID")
    @ApiResponse(responseCode = "200", description = "Evento encontrado")
    @ApiResponse(responseCode = "404", description = "Evento não encontrado",
            content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    public ResponseEntity<EventResponse> findById(@PathVariable String id) {
        return ResponseEntity.ok(service.findById(id));
    }

    @PostMapping
    @Operation(summary = "Criar evento", description = "Cadastra um novo evento vinculado ao fotógrafo autenticado")
    @ApiResponse(responseCode = "201", description = "Evento criado com sucesso")
    @ApiResponse(responseCode = "400", description = "Dados inválidos",
            content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    public ResponseEntity<EventResponse> create(@AuthenticationPrincipal User user,
                                                @Valid @RequestBody EventRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(service.create(request, user.getId()));
    }

    @PutMapping("/{id}")
    @Operation(summary = "Atualizar evento", description = "Atualiza um evento do fotógrafo autenticado")
    @ApiResponse(responseCode = "200", description = "Evento atualizado com sucesso")
    @ApiResponse(responseCode = "404", description = "Evento não encontrado",
            content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    public ResponseEntity<EventResponse> update(@AuthenticationPrincipal User user,
                                                @PathVariable String id,
                                                @Valid @RequestBody EventRequest request) {
        return ResponseEntity.ok(service.update(id, request, user.getId()));
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Deletar evento", description = "Remove um evento do fotógrafo autenticado")
    @ApiResponse(responseCode = "204", description = "Evento deletado com sucesso")
    @ApiResponse(responseCode = "404", description = "Evento não encontrado",
            content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    public ResponseEntity<Void> delete(@AuthenticationPrincipal User user, @PathVariable String id) {
        service.delete(id, user.getId());
        return ResponseEntity.noContent().build();
    }
}

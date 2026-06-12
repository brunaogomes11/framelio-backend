package com.gomes.photographer_manager.domain.team;

import com.gomes.photographer_manager.domain.team.request.TeamRequest;
import com.gomes.photographer_manager.domain.team.response.TeamResponse;
import com.gomes.photographer_manager.exception.ErrorResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/teams")
@Tag(name = "Equipes", description = "Gerenciamento de equipes de fotógrafos")
public class TeamController {

    private final TeamService service;

    public TeamController(TeamService service) {
        this.service = service;
    }

    @PostMapping
    @Operation(summary = "Criar equipe", description = "Cadastra uma nova equipe com seus membros iniciais")
    @ApiResponse(responseCode = "201", description = "Equipe criada com sucesso")
    @ApiResponse(responseCode = "400", description = "Dados inválidos",
            content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    public ResponseEntity<TeamResponse> create(@Valid @RequestBody TeamRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(service.create(request));
    }

    @GetMapping
    @Operation(summary = "Listar equipes", description = "Retorna todas as equipes cadastradas com seus membros")
    @ApiResponse(responseCode = "200", description = "Lista retornada com sucesso")
    public ResponseEntity<List<TeamResponse>> listAll() {
        return ResponseEntity.ok(service.listAll());
    }

    @GetMapping("/{id}")
    @Operation(summary = "Buscar equipe por ID", description = "Retorna uma equipe pelo seu ULID, incluindo a lista de membros")
    @ApiResponse(responseCode = "200", description = "Equipe encontrada")
    @ApiResponse(responseCode = "404", description = "Equipe não encontrada",
            content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    public ResponseEntity<TeamResponse> findById(@PathVariable String id) {
        return ResponseEntity.ok(service.findById(id));
    }

    @PutMapping("/{id}")
    @Operation(summary = "Atualizar equipe", description = "Atualiza os dados de uma equipe, substituindo completamente a lista de membros")
    @ApiResponse(responseCode = "200", description = "Equipe atualizada com sucesso")
    @ApiResponse(responseCode = "404", description = "Equipe não encontrada",
            content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    public ResponseEntity<TeamResponse> update(@PathVariable String id,
                                               @Valid @RequestBody TeamRequest request) {
        return ResponseEntity.ok(service.update(id, request));
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Deletar equipe", description = "Remove uma equipe do sistema pelo seu ULID")
    @ApiResponse(responseCode = "204", description = "Equipe deletada com sucesso")
    @ApiResponse(responseCode = "404", description = "Equipe não encontrada",
            content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    public ResponseEntity<Void> delete(@PathVariable String id) {
        service.delete(id);
        return ResponseEntity.noContent().build();
    }
}

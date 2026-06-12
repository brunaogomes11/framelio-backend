package com.gomes.photographer_manager.domain.photographer.team;

import com.gomes.photographer_manager.domain.usuario.User;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
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
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/photographer/team")
@Tag(name = "Equipe do Fotógrafo", description = "Gerenciamento de colaboradores individuais do fotógrafo")
public class TeamMemberController {

    private final TeamMemberService teamMemberService;

    public TeamMemberController(TeamMemberService teamMemberService) {
        this.teamMemberService = teamMemberService;
    }

    @GetMapping
    @Operation(summary = "Listar colaboradores", description = "Retorna os colaboradores do fotógrafo autenticado")
    @ApiResponse(responseCode = "200", description = "Lista retornada com sucesso")
    public ResponseEntity<List<TeamMemberResponse>> findByPhotographer(@AuthenticationPrincipal User user) {
        return ResponseEntity.ok(teamMemberService.findByPhotographer(user.getId()));
    }

    @PostMapping
    @Operation(summary = "Criar colaborador", description = "Cadastra um novo colaborador vinculado ao fotógrafo autenticado")
    @ApiResponse(responseCode = "201", description = "Colaborador criado com sucesso")
    public ResponseEntity<TeamMemberResponse> create(@AuthenticationPrincipal User user,
                                                     @Valid @RequestBody TeamMemberRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(teamMemberService.create(request, user.getId()));
    }

    @PutMapping("/{id}")
    @Operation(summary = "Atualizar colaborador", description = "Atualiza um colaborador do fotógrafo autenticado")
    @ApiResponse(responseCode = "200", description = "Colaborador atualizado com sucesso")
    @ApiResponse(responseCode = "404", description = "Colaborador não encontrado")
    public ResponseEntity<TeamMemberResponse> update(@AuthenticationPrincipal User user,
                                                     @PathVariable String id,
                                                     @Valid @RequestBody TeamMemberRequest request) {
        return ResponseEntity.ok(teamMemberService.update(id, request, user.getId()));
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Remover colaborador", description = "Remove um colaborador do fotógrafo autenticado")
    @ApiResponse(responseCode = "204", description = "Colaborador removido com sucesso")
    @ApiResponse(responseCode = "404", description = "Colaborador não encontrado")
    public ResponseEntity<Void> delete(@AuthenticationPrincipal User user, @PathVariable String id) {
        teamMemberService.delete(id, user.getId());
        return ResponseEntity.noContent().build();
    }
}

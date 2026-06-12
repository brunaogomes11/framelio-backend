package com.gomes.photographer_manager.domain.agency;

import com.gomes.photographer_manager.domain.agency.request.AgencyRequest;
import com.gomes.photographer_manager.domain.agency.response.AgencyResponse;
import com.gomes.photographer_manager.domain.usuario.User;
import com.gomes.photographer_manager.exception.ErrorResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.annotation.Secured;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/agencies")
@Tag(name = "Agências", description = "Gerenciamento de agências")
public class AgencyController {

    private final AgencyService service;

    public AgencyController(AgencyService service) {
        this.service = service;
    }

    @PostMapping
    @Secured({"ROLE_AGENCY", "ROLE_ADMIN"})
    @Operation(summary = "Criar agência", description = "Cadastra uma nova agência vinculada ao usuário autenticado")
    @ApiResponse(responseCode = "201", description = "Agência criada com sucesso")
    @ApiResponse(responseCode = "400", description = "Já existe uma agência para o usuário",
            content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    public ResponseEntity<AgencyResponse> create(@AuthenticationPrincipal User user,
                                                 @Valid @RequestBody AgencyRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(service.create(request, user.getId()));
    }

    @GetMapping("/me")
    @Secured({"ROLE_AGENCY", "ROLE_ADMIN"})
    @Operation(summary = "Buscar minha agência", description = "Retorna a agência vinculada ao usuário autenticado")
    @ApiResponse(responseCode = "200", description = "Agência encontrada")
    @ApiResponse(responseCode = "404", description = "Agência não encontrada",
            content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    public ResponseEntity<AgencyResponse> findMine(@AuthenticationPrincipal User user) {
        return ResponseEntity.ok(service.findByUserId(user.getId()));
    }

    @PutMapping("/me")
    @Secured({"ROLE_AGENCY", "ROLE_ADMIN"})
    @Operation(summary = "Atualizar minha agência", description = "Atualiza a agência vinculada ao usuário autenticado")
    @ApiResponse(responseCode = "200", description = "Agência atualizada com sucesso")
    @ApiResponse(responseCode = "404", description = "Agência não encontrada",
            content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    public ResponseEntity<AgencyResponse> update(@AuthenticationPrincipal User user,
                                                 @Valid @RequestBody AgencyRequest request) {
        return ResponseEntity.ok(service.update(user.getId(), request));
    }

    @DeleteMapping("/me")
    @Secured({"ROLE_AGENCY", "ROLE_ADMIN"})
    @Operation(summary = "Deletar minha agência", description = "Remove a agência vinculada ao usuário autenticado")
    @ApiResponse(responseCode = "204", description = "Agência deletada com sucesso")
    @ApiResponse(responseCode = "404", description = "Agência não encontrada",
            content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    public ResponseEntity<Void> delete(@AuthenticationPrincipal User user) {
        service.delete(user.getId());
        return ResponseEntity.noContent().build();
    }
}

package com.gomes.photographer_manager.domain.client;

import com.gomes.photographer_manager.domain.usuario.User;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/client")
@Tag(name = "Painel do Cliente", description = "Dados agregados do painel do cliente")
public class ClientController {

    private final ClientService clientService;

    public ClientController(ClientService clientService) {
        this.clientService = clientService;
    }

    @GetMapping("/stats")
    @Operation(summary = "Estatísticas do cliente", description = "Retorna métricas resumidas do cliente autenticado")
    @ApiResponse(responseCode = "200", description = "Estatísticas retornadas com sucesso")
    public ResponseEntity<ClientStatsResponse> getStats(@AuthenticationPrincipal User user) {
        return ResponseEntity.ok(clientService.getStats(user.getId()));
    }
}

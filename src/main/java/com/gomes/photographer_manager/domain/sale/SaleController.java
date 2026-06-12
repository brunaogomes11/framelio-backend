package com.gomes.photographer_manager.domain.sale;

import com.gomes.photographer_manager.domain.sale.request.SaleRequest;
import com.gomes.photographer_manager.domain.sale.response.SaleResponse;
import com.gomes.photographer_manager.domain.sale.response.SaleStatsResponse;
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

import java.util.List;

@RestController
@RequestMapping("/sales")
@Tag(name = "Vendas", description = "Gerenciamento de vendas")
public class SaleController {

    private final SaleService service;

    public SaleController(SaleService service) {
        this.service = service;
    }

    @GetMapping
    @Operation(summary = "Listar vendas", description = "Retorna as vendas do fotógrafo autenticado, com filtro opcional por status de pagamento")
    @ApiResponse(responseCode = "200", description = "Lista retornada com sucesso")
    public ResponseEntity<List<SaleResponse>> findAll(
            @AuthenticationPrincipal User user,
            @RequestParam(required = false) PaymentStatus status) {
        return ResponseEntity.ok(service.findAll(user.getId(), status));
    }

    @GetMapping("/recent")
    @Operation(summary = "Vendas recentes", description = "Retorna as 5 vendas mais recentes do fotógrafo autenticado")
    @ApiResponse(responseCode = "200", description = "Lista retornada com sucesso")
    public ResponseEntity<List<SaleResponse>> findRecent(@AuthenticationPrincipal User user) {
        return ResponseEntity.ok(service.findRecent(user.getId()));
    }

    @GetMapping("/stats")
    @Operation(summary = "Estatísticas de vendas", description = "Retorna estatísticas financeiras das vendas do fotógrafo autenticado")
    @ApiResponse(responseCode = "200", description = "Estatísticas retornadas com sucesso")
    public ResponseEntity<SaleStatsResponse> getStats(@AuthenticationPrincipal User user) {
        return ResponseEntity.ok(service.getStats(user.getId()));
    }

    @GetMapping("/{id}")
    @Operation(summary = "Buscar venda por ID", description = "Retorna uma venda pelo seu ULID")
    @ApiResponse(responseCode = "200", description = "Venda encontrada")
    @ApiResponse(responseCode = "404", description = "Venda não encontrada",
            content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    public ResponseEntity<SaleResponse> findById(@PathVariable String id) {
        return ResponseEntity.ok(service.findById(id));
    }

    @PostMapping
    @Operation(summary = "Criar venda", description = "Cadastra uma nova venda vinculada ao fotógrafo autenticado")
    @ApiResponse(responseCode = "201", description = "Venda criada com sucesso")
    @ApiResponse(responseCode = "400", description = "Dados inválidos",
            content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    public ResponseEntity<SaleResponse> create(@AuthenticationPrincipal User user,
                                               @Valid @RequestBody SaleRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(service.create(request, user.getId()));
    }

    @PutMapping("/{id}")
    @Operation(summary = "Atualizar venda", description = "Atualiza uma venda do fotógrafo autenticado")
    @ApiResponse(responseCode = "200", description = "Venda atualizada com sucesso")
    @ApiResponse(responseCode = "404", description = "Venda não encontrada",
            content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    public ResponseEntity<SaleResponse> update(@AuthenticationPrincipal User user,
                                               @PathVariable String id,
                                               @Valid @RequestBody SaleRequest request) {
        return ResponseEntity.ok(service.update(id, request, user.getId()));
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Deletar venda", description = "Remove uma venda do fotógrafo autenticado")
    @ApiResponse(responseCode = "204", description = "Venda deletada com sucesso")
    @ApiResponse(responseCode = "404", description = "Venda não encontrada",
            content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    public ResponseEntity<Void> delete(@AuthenticationPrincipal User user, @PathVariable String id) {
        service.delete(id, user.getId());
        return ResponseEntity.noContent().build();
    }
}

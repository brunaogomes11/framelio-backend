package com.gomes.photographer_manager.domain.photographer.team;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;

@Schema(description = "Dados para criação ou atualização de um colaborador")
public record TeamMemberRequest(

        @Schema(description = "Nome do colaborador")
        @NotBlank String name,

        @Schema(description = "Função do colaborador na equipe")
        @NotBlank String role,

        @Schema(description = "Situação do colaborador: ACTIVE, VACATION ou INACTIVE. Padrão ACTIVE")
        String status
) {
}

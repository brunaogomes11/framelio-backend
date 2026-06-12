package com.gomes.photographer_manager.domain.team.request;

import io.swagger.v3.oas.annotations.media.Schema;

import java.util.List;

@Schema(description = "Dados para criação ou atualização de uma equipe")
public record TeamRequest(

        @Schema(description = "ID ULID da empresa à qual a equipe pertence", example = "01J8G5T3K2N4P6Q8R0S2T4V6W8")
        String companyId,

        @Schema(description = "Lista de IDs ULID dos membros (usuários) da equipe")
        List<String> memberIds
) {
}

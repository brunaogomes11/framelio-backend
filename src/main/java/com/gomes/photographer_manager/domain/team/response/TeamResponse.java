package com.gomes.photographer_manager.domain.team.response;

import com.gomes.photographer_manager.domain.team.Team;
import com.gomes.photographer_manager.domain.usuario.response.UserResponse;
import io.swagger.v3.oas.annotations.media.Schema;

import java.util.List;

@Schema(description = "Dados de retorno de uma equipe")
public record TeamResponse(

        @Schema(description = "Identificador único ULID da equipe")
        String id,

        @Schema(description = "ID ULID da empresa à qual a equipe pertence")
        String companyId,

        @Schema(description = "Lista de membros da equipe")
        List<UserResponse> members
) {
    public TeamResponse(Team team) {
        this(
                team.getId(),
                team.getCompanyId(),
                team.getMembers().stream()
                        .map(UserResponse::new)
                        .toList()
        );
    }
}

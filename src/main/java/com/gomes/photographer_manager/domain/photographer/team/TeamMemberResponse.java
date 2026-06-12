package com.gomes.photographer_manager.domain.photographer.team;

import io.swagger.v3.oas.annotations.media.Schema;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Schema(description = "Dados de retorno de um colaborador")
public record TeamMemberResponse(

        @Schema(description = "Identificador único ULID do colaborador")
        String id,

        @Schema(description = "Nome do colaborador")
        String name,

        @Schema(description = "Função do colaborador na equipe")
        String role,

        @Schema(description = "Situação do colaborador: ACTIVE, VACATION ou INACTIVE")
        String status,

        @Schema(description = "Quantidade de eventos no mês atual")
        int eventsThisMonth,

        @Schema(description = "Quantidade de fotos vendidas")
        int photosSold,

        @Schema(description = "Receita gerada pelo colaborador")
        BigDecimal revenue,

        @Schema(description = "Data e hora de criação do colaborador")
        LocalDateTime createdAt
) {
    public TeamMemberResponse(TeamMember member) {
        this(
                member.getId(),
                member.getName(),
                member.getRole(),
                member.getStatus(),
                0,
                0,
                BigDecimal.ZERO,
                member.getCreatedAt()
        );
    }
}

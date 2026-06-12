package com.gomes.photographer_manager.domain.client;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Estatísticas resumidas do painel do cliente")
public record ClientStatsResponse(

        @Schema(description = "Quantidade de sessões concluídas")
        int completedSessions,

        @Schema(description = "Quantidade de fotos recebidas em galerias visíveis")
        int photosReceived,

        @Schema(description = "Quantidade de mensagens não lidas")
        int unreadMessages
) {
}

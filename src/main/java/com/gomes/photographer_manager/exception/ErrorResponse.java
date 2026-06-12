package com.gomes.photographer_manager.exception;

import io.swagger.v3.oas.annotations.media.Schema;

import java.time.LocalDateTime;

@Schema(description = "Resposta padrão de erro da API")
public record ErrorResponse(

        @Schema(description = "Mensagem descritiva do erro")
        String message,

        @Schema(description = "Código HTTP do erro", example = "404")
        int status,

        @Schema(description = "Timestamp do momento do erro")
        LocalDateTime timestamp
) {
}

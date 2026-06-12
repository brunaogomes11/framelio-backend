package com.gomes.photographer_manager.domain.auth.response;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Resposta de autenticação contendo o JWT e dados básicos do usuário")
public record AuthResponse(

        @Schema(description = "JWT Bearer token para uso nas requisições autenticadas")
        String token
) {}

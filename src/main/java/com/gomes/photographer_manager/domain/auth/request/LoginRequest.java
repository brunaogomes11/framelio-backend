package com.gomes.photographer_manager.domain.auth.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

@Schema(description = "Credenciais para autenticação via e-mail e senha")
public record LoginRequest(

        @Email
        @NotBlank
        @Schema(description = "E-mail do usuário", example = "joao@email.com")
        String email,

        @NotBlank
        @Schema(description = "Senha do usuário", example = "senhaSegura123")
        String password
) {}

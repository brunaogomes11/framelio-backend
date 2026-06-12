package com.gomes.photographer_manager.domain.auth.request;

import com.gomes.photographer_manager.enums.ProfileEnum;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.util.List;

@Schema(description = "Dados para registro de novo usuário")
public record RegisterRequest(

        @NotBlank
        @Schema(description = "Nome completo do usuário", example = "João Silva")
        String name,

        @Schema(description = "CPF do usuário (somente dígitos, opcional)", example = "12345678901")
        String cpf,

        @NotBlank
        @Email
        @Schema(description = "E-mail do usuário", example = "joao@email.com")
        String email,

        @NotBlank
        @Schema(description = "Senha do usuário (mínimo 6 caracteres)", example = "senha123")
        String password,

        @NotNull
        @Schema(description = "Perfil do usuário. Apenas CLIENT ou PHOTOGRAPHER são permitidos no registro.",
                allowableValues = {"CLIENT", "PHOTOGRAPHER"})
        ProfileEnum profile,

        @Schema(description = "Categorias de serviço (apenas para PHOTOGRAPHER)", example = "[\"Casamento\", \"Família\"]")
        List<String> categories
) {}

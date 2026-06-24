package com.gomes.photographer_manager.domain.usuario.response;

import com.gomes.photographer_manager.domain.usuario.User;
import com.gomes.photographer_manager.enums.ProfileEnum;
import io.swagger.v3.oas.annotations.media.Schema;

import java.util.List;

@Schema(description = "Dados de retorno de um usuário")
public record UserResponse(

        @Schema(description = "Identificador único ULID do usuário")
        String id,

        @Schema(description = "Nome completo do usuário")
        String name,

        @Schema(description = "CPF do usuário")
        String cpf,

        @Schema(description = "E-mail do usuário")
        String email,

        @Schema(description = "Indica se o usuário está ativo")
        boolean active,

        @Schema(description = "Perfil de acesso do usuário")
        ProfileEnum profile,

        @Schema(description = "Telefone do usuário")
        String phone,

        @Schema(description = "URL da foto de perfil do usuário")
        String profilePhoto,

        @Schema(description = "Biografia do usuário")
        String bio,

        @Schema(description = "Categorias de serviço do usuário")
        List<String> categories,

        @Schema(description = "URL da marca d'água do fotógrafo")
        String watermarkPath
) {
    public UserResponse(User user) {
        this(
                user.getId(),
                user.getName(),
                user.getCpf(),
                user.getEmail(),
                user.isActive(),
                user.getProfile(),
                user.getPhone(),
                user.getProfilePhoto(),
                user.getBio(),
                user.getCategories(),
                user.getWatermarkPath() != null ? "/files/" + user.getWatermarkPath() : null
        );
    }
}

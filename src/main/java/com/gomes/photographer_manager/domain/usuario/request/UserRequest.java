package com.gomes.photographer_manager.domain.usuario.request;

import com.gomes.photographer_manager.enums.ProfileEnum;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Size;

import java.util.List;

@Schema(description = "Dados para criação ou atualização de um usuário")
public record UserRequest(
        @Schema(description = "Nome completo do usuário", example = "João da Silva")
        String name,
        @Schema(description = "CPF do usuário (somente dígitos)", example = "12345678901")
        String cpf,
        @Email
        @Schema(description = "E-mail do usuário", example = "joao@email.com")
        String email,
        @Schema(description = "Senha do usuário. Obrigatório na criação. Na atualização, deixe em branco para manter a senha atual.", example = "senhaSegura123")
        String password,
        @Schema(description = "Status ativo/inativo do usuário. Padrão: true.", example = "true")
        Boolean active,
        @Schema(description = "Perfil de acesso: PHOTOGRAPHER, CLIENT, ADMIN ou AGENCY", example = "PHOTOGRAPHER")
        ProfileEnum profile,
        @Schema(description = "Telefone do usuário", example = "11999998888")
        String phone,
        @Schema(description = "URL da foto de perfil do usuário", example = "https://cdn.exemplo.com/fotos/joao.jpg")
        String profilePhoto,
        @Size(max = 500, message = "A biografia deve ter no máximo 500 caracteres")
        @Schema(description = "Biografia do usuário", example = "Fotógrafo de casamentos há 10 anos")
        String bio,
        @Schema(description = "Categorias de serviço (apenas para PHOTOGRAPHER)", example = "[\"Casamento\", \"Família\"]")
        List<String> categories
) {
}

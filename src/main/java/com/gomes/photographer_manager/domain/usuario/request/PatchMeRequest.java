package com.gomes.photographer_manager.domain.usuario.request;

import io.swagger.v3.oas.annotations.media.Schema;

import java.util.List;

@Schema(description = "Campos editáveis do perfil do usuário autenticado")
public record PatchMeRequest(
        String name,
        String phone,
        String cpf,
        String bio,
        String portfolioGradient,
        List<String> categories
) {}

package com.gomes.photographer_manager.domain.auth.response;

/**
 * Par de tokens produzido pela autenticação: o access token (vai no corpo da resposta)
 * e o refresh token bruto (vai num cookie httpOnly definido pelo controller).
 */
public record AuthTokens(String accessToken, String refreshToken) {
}

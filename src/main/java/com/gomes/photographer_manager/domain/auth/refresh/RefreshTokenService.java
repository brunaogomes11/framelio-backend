package com.gomes.photographer_manager.domain.auth.refresh;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.time.LocalDateTime;
import java.util.Base64;
import java.util.HexFormat;

@Service
public class RefreshTokenService {

    private final RefreshTokenRepository repository;
    private final long expirationMs;
    private final SecureRandom secureRandom = new SecureRandom();
    private final Base64.Encoder urlEncoder = Base64.getUrlEncoder().withoutPadding();

    public RefreshTokenService(RefreshTokenRepository repository,
                               @Value("${app.refresh-token.expiration-ms:604800000}") long expirationMs) {
        this.repository = repository;
        this.expirationMs = expirationMs;
    }

    /** Resultado de uma rotação: novo token bruto (vai pro cookie) + dono. */
    public record RotationResult(String rawToken, String userId) {}

    /** Emite um novo refresh token e devolve o valor bruto (somente o hash é persistido). */
    @Transactional
    public String issue(String userId) {
        String rawToken = generateRawToken();
        LocalDateTime expiresAt = LocalDateTime.now().plusNanos(expirationMs * 1_000_000);
        repository.save(new RefreshToken(userId, hash(rawToken), expiresAt));
        return rawToken;
    }

    /**
     * Valida e rotaciona o refresh token. Se o token apresentado já estava revogado,
     * trata como reuso (possível roubo) e revoga toda a cadeia do usuário.
     */
    @Transactional
    public RotationResult rotate(String rawToken) {
        RefreshToken current = repository.findByTokenHash(hash(rawToken))
                .orElseThrow(() -> new IllegalArgumentException("Refresh token inválido"));

        if (current.isRevoked()) {
            // Reuso de um token já rotacionado → revoga tudo do usuário por segurança.
            repository.revokeAllForUser(current.getUserId());
            throw new IllegalStateException("Refresh token reutilizado — sessões revogadas");
        }
        if (current.isExpired()) {
            throw new IllegalArgumentException("Refresh token expirado");
        }

        String newRaw = generateRawToken();
        LocalDateTime expiresAt = LocalDateTime.now().plusNanos(expirationMs * 1_000_000);
        RefreshToken rotated = repository.save(new RefreshToken(current.getUserId(), hash(newRaw), expiresAt));

        current.setRevoked(true);
        current.setReplacedById(rotated.getId());
        repository.save(current);

        return new RotationResult(newRaw, current.getUserId());
    }

    @Transactional
    public void revoke(String rawToken) {
        repository.findByTokenHash(hash(rawToken)).ifPresent(token -> {
            token.setRevoked(true);
            repository.save(token);
        });
    }

    @Transactional
    public void revokeAllForUser(String userId) {
        repository.revokeAllForUser(userId);
    }

    private String generateRawToken() {
        byte[] bytes = new byte[32];
        secureRandom.nextBytes(bytes);
        return urlEncoder.encodeToString(bytes);
    }

    private String hash(String rawToken) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hashed = digest.digest(rawToken.getBytes(StandardCharsets.UTF_8));
            return HexFormat.of().formatHex(hashed);
        } catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException("SHA-256 indisponível", e);
        }
    }
}

package com.gomes.photographer_manager.domain.auth.refresh;

import com.github.f4b6a3.ulid.UlidCreator;
import jakarta.persistence.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "tb_refresh_token", indexes = {
        @Index(name = "idx_refresh_token_hash", columnList = "token_hash"),
        @Index(name = "idx_refresh_user_id", columnList = "user_id")
})
public class RefreshToken {

    @Id
    @Column(length = 26)
    private String id;

    @Column(name = "user_id", nullable = false)
    private String userId;

    /** SHA-256 hex do token bruto — o valor bruto nunca é persistido. */
    @Column(name = "token_hash", nullable = false, unique = true, length = 64)
    private String tokenHash;

    @Column(name = "expira_em", nullable = false)
    private LocalDateTime expiresAt;

    @Column(name = "revogado", nullable = false)
    private boolean revoked;

    /** Id do token que substituiu este (cadeia de rotação, para detecção de reuso). */
    @Column(name = "substituido_por", length = 26)
    private String replacedById;

    @Column(name = "criado_em", nullable = false)
    private LocalDateTime createdAt;

    @PrePersist
    private void prePersist() {
        this.id = UlidCreator.getUlid().toString();
        this.createdAt = LocalDateTime.now();
    }

    public RefreshToken() {
    }

    public RefreshToken(String userId, String tokenHash, LocalDateTime expiresAt) {
        this.userId = userId;
        this.tokenHash = tokenHash;
        this.expiresAt = expiresAt;
    }

    public boolean isExpired() {
        return !expiresAt.isAfter(LocalDateTime.now());
    }

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }
    public String getUserId() { return userId; }
    public void setUserId(String userId) { this.userId = userId; }
    public String getTokenHash() { return tokenHash; }
    public void setTokenHash(String tokenHash) { this.tokenHash = tokenHash; }
    public LocalDateTime getExpiresAt() { return expiresAt; }
    public void setExpiresAt(LocalDateTime expiresAt) { this.expiresAt = expiresAt; }
    public boolean isRevoked() { return revoked; }
    public void setRevoked(boolean revoked) { this.revoked = revoked; }
    public String getReplacedById() { return replacedById; }
    public void setReplacedById(String replacedById) { this.replacedById = replacedById; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
}

package com.gomes.photographer_manager.domain.auth.passwordreset;

import com.github.f4b6a3.ulid.UlidCreator;
import jakarta.persistence.*;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "tb_token_reset")
public class PasswordResetToken {

    @Id
    @Column(length = 26)
    private String id;

    @Column(name = "user_id", nullable = false)
    private String userId;

    @Column(name = "token", nullable = false, unique = true)
    private String token;

    @Column(name = "expira_em", nullable = false)
    private LocalDateTime expiresAt;

    @Column(name = "usado", nullable = false)
    private boolean used;

    @PrePersist
    private void prePersist() {
        this.id = UlidCreator.getUlid().toString();
        this.token = UUID.randomUUID().toString();
        this.expiresAt = LocalDateTime.now().plusHours(1);
    }

    public PasswordResetToken() {
    }

    public PasswordResetToken(String userId) {
        this.userId = userId;
    }

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }
    public String getUserId() { return userId; }
    public void setUserId(String userId) { this.userId = userId; }
    public String getToken() { return token; }
    public void setToken(String token) { this.token = token; }
    public LocalDateTime getExpiresAt() { return expiresAt; }
    public void setExpiresAt(LocalDateTime expiresAt) { this.expiresAt = expiresAt; }
    public boolean isUsed() { return used; }
    public void setUsed(boolean used) { this.used = used; }
}

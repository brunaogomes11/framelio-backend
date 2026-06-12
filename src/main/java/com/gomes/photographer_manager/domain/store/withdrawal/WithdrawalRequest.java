package com.gomes.photographer_manager.domain.store.withdrawal;

import com.github.f4b6a3.ulid.UlidCreator;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "tb_saque")
public class WithdrawalRequest {

    @Id
    private String id;

    @Column(name = "fotografo_id")
    private String photographerId;

    @Column(name = "valor", precision = 10, scale = 2)
    private BigDecimal amount;

    @Column(name = "chave_pix")
    private String pixKey;

    @Column(name = "tipo_chave_pix")
    private String pixKeyType;

    @Column(name = "status")
    private String status = "PENDING";

    @Column(name = "criado_em")
    private LocalDateTime createdAt;

    @Column(name = "processado_em")
    private LocalDateTime processedAt;

    @PrePersist
    void prePersist() {
        id = UlidCreator.getUlid().toString();
        createdAt = LocalDateTime.now();
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getPhotographerId() {
        return photographerId;
    }

    public void setPhotographerId(String photographerId) {
        this.photographerId = photographerId;
    }

    public BigDecimal getAmount() {
        return amount;
    }

    public void setAmount(BigDecimal amount) {
        this.amount = amount;
    }

    public String getPixKey() {
        return pixKey;
    }

    public void setPixKey(String pixKey) {
        this.pixKey = pixKey;
    }

    public String getPixKeyType() {
        return pixKeyType;
    }

    public void setPixKeyType(String pixKeyType) {
        this.pixKeyType = pixKeyType;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public LocalDateTime getProcessedAt() {
        return processedAt;
    }

    public void setProcessedAt(LocalDateTime processedAt) {
        this.processedAt = processedAt;
    }
}
